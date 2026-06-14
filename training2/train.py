# -*- coding: utf-8 -*-
"""
动漫标题解析模型 — CPU / AMD GPU 训练脚本
=========================================
支持: CPU 训练 / AMD 显卡 (DirectML) 加速
用法: python train.py
"""

import os, sys, json, random
from pathlib import Path

BASE = Path(__file__).parent
SRC_DATA = BASE.parent / "training" / "data"
DATA_DIR = BASE / "data"
MODEL_DIR = Path("F:/Download/cache/modelscope")
OUT_DIR = Path("F:/Download/cache/training_output")
HF_CACHE = Path("F:/Download/cache/huggingface")
PIP_CACHE = Path("F:/Download/cache/pip")
DS_CACHE = Path("F:/Download/cache/datasets")

# 全局设备: "cpu" 或 dml 设备对象
_DEVICE = None


# ════════════════════════════════════════════════════════
# Step 0: 环境
# ════════════════════════════════════════════════════════
def step0_setup():
    import subprocess as sp
    print("=" * 55)
    print("Step 0: 安装依赖 + 复制数据")
    print("=" * 55)

    # 代理环境清除
    for k in list(os.environ.keys()):
        if "proxy" in k.lower():
            del os.environ[k]

    # 所有缓存目录指向 F 盘
    for d in [MODEL_DIR, OUT_DIR, HF_CACHE, PIP_CACHE, DS_CACHE, DATA_DIR]:
        d.mkdir(parents=True, exist_ok=True)
    os.environ["HF_HOME"] = str(HF_CACHE)
    os.environ["HUGGINGFACE_HUB_CACHE"] = str(HF_CACHE / "hub")
    os.environ["HF_HUB_CACHE"] = str(HF_CACHE / "hub")
    os.environ["TRANSFORMERS_CACHE"] = str(HF_CACHE / "transformers")
    os.environ["XDG_CACHE_HOME"] = str(HF_CACHE)
    os.environ["DATASETS_CACHE"] = str(DS_CACHE)

    pkgs = [
        "modelscope",
        "transformers",
        "peft",
        "datasets",
        "accelerate",
        "trl",
        "sentencepiece",
    ]
    print(f"pip install {' '.join(pkgs)} ...")
    PIP_CACHE.mkdir(parents=True, exist_ok=True)
    sp.run(
        [sys.executable, "-m", "pip", "install", "--upgrade",
         "--cache-dir", str(PIP_CACHE),
         "-i", "https://pypi.tuna.tsinghua.edu.cn/simple",
         "--trusted-host", "pypi.tuna.tsinghua.edu.cn"] + pkgs,
        check=True, capture_output=False,
        env={**os.environ, "no_proxy": "*", "NO_PROXY": "*"},
    )

    # 安装 DirectML 版 PyTorch（覆盖其他包拉进来的标准 torch）
    print("pip install torch-directml (AMD GPU 支持) ...")
    sp.run(
        [sys.executable, "-m", "pip", "install", "--force-reinstall",
         "--cache-dir", str(PIP_CACHE),
         "-i", "https://pypi.tuna.tsinghua.edu.cn/simple",
         "--trusted-host", "pypi.tuna.tsinghua.edu.cn",
         "torch-directml"],
        check=False, capture_output=False,
        env={**os.environ, "no_proxy": "*", "NO_PROXY": "*"},
    )

    # 复制数据
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    for name in ["real_train.json", "real_val.json", "real_test.json", "merged_all.json"]:
        src = SRC_DATA / name
        dst = DATA_DIR / name
        if src.exists() and not dst.exists():
            import shutil
            shutil.copy(src, dst)
    print("OK\n")


# ════════════════════════════════════════════════════════
# 获取训练设备
# ════════════════════════════════════════════════════════
def get_device():
    """自动检测可用设备: DirectML (AMD) > CUDA (N卡) > CPU"""
    global _DEVICE
    if _DEVICE is not None:
        return _DEVICE

    import torch

    # 1. 优先尝试 DirectML (AMD / Intel Arc 等)
    try:
        import torch_directml
        _DEVICE = torch_directml.device()
        print("=" * 55)
        print(f"  使用 DirectML 设备: {_DEVICE}")
        print("=" * 55)
        return _DEVICE
    except ImportError:
        pass

    # 2. CUDA
    if torch.cuda.is_available():
        _DEVICE = torch.device("cuda")
        print("=" * 55)
        print(f"  使用 CUDA 设备: {torch.cuda.get_device_name(0)}")
        print("=" * 55)
        return _DEVICE

    # 3. 回退 CPU
    _DEVICE = torch.device("cpu")
    print("=" * 55)
    print("  ⚠ 未检测到 GPU，回退到 CPU 训练")
    print("  AMD 用户请先运行: pip install torch-directml")
    print("=" * 55)
    return _DEVICE


# ════════════════════════════════════════════════════════
# Step 1: 加载数据
# ════════════════════════════════════════════════════════
def step1_load_data():
    print("=" * 55)
    print("Step 1: 加载训练数据")
    print("=" * 55)
    f = DATA_DIR / "merged_all.json"
    if not f.exists():
        f = DATA_DIR / "real_train.json"
    with open(f, encoding="utf-8") as fp:
        raw = json.load(fp)
    seen = set()
    data = [d for d in raw if (i := d.get("input", "")) and i not in seen and not seen.add(i)]
    print(f"  {len(data)} 条（去重后）\n")
    return data


# ════════════════════════════════════════════════════════
# Step 2: 下载模型 (ModelScope → 本地)
# ════════════════════════════════════════════════════════
def step2_download_model():
    print("=" * 55)
    print("Step 2: 下载 Qwen2.5-1.5B-Instruct (ModelScope)")
    print("=" * 55)

    # ModelScope 写死 C 盘存凭证，必须在 import 前猴子补丁
    os.environ["MODELSCOPE_CACHE"] = str(MODEL_DIR)
    modelscope_cred = str(MODEL_DIR / ".modelscope")
    os.makedirs(modelscope_cred, exist_ok=True)

    import modelscope.hub.api as _api
    _api.ModelScopeConfig.path_credential = modelscope_cred
    from modelscope import snapshot_download

    # 清除代理
    for k in ["HTTP_PROXY", "HTTPS_PROXY", "http_proxy", "https_proxy", "ALL_PROXY"]:
        os.environ.pop(k, None)

    path = snapshot_download("Qwen/Qwen2.5-1.5B-Instruct")
    print(f"  模型路径: {path}\n")
    return path


# ════════════════════════════════════════════════════════
# Step 3: 训练
# ════════════════════════════════════════════════════════
def step3_train(model_path, data):
    import torch
    from datasets import Dataset
    from transformers import AutoTokenizer, AutoModelForCausalLM
    from peft import LoraConfig, TaskType, get_peft_model
    from trl import SFTTrainer, SFTConfig

    device = get_device()
    is_cpu = (device.type == "cpu")
    is_dml = (device.type == "privateuseone")  # DirectML 的设备类型

    print("=" * 55)
    print("Step 3: 加载模型 + LoRA 配置")
    print("=" * 55)

    # 模型加载: DML 不能直接用 device_map，先 load 到 cpu 再 .to()
    if is_dml:
        model = AutoModelForCausalLM.from_pretrained(
            model_path,
            torch_dtype=torch.float32,
            trust_remote_code=True,
        )
        model = model.to(device)
    else:
        model = AutoModelForCausalLM.from_pretrained(
            model_path,
            torch_dtype=torch.float32,
            trust_remote_code=True,
            low_cpu_mem_usage=True,
        )
        if not is_cpu:
            model = model.to(device)

    tok = AutoTokenizer.from_pretrained(model_path, trust_remote_code=True)
    if tok.pad_token is None:
        tok.pad_token = tok.eos_token

    lora = LoraConfig(
        task_type=TaskType.CAUSAL_LM,
        r=8,
        lora_alpha=16,
        lora_dropout=0.05,
        target_modules=["q_proj", "k_proj", "v_proj", "o_proj",
                        "gate_proj", "up_proj", "down_proj"],
    )
    model = get_peft_model(model, lora)
    model = model.to(device)  # LoRA 新参数也移到 DML
    model.print_trainable_parameters()

    # ── 格式化 ──
    SYS = (
        "你是一个动漫种子标题解析专家。从番剧标题中提取结构化信息。"
        "仅返回合法JSON，不要代码块或解释。字段: episode(集数字符串,例05/12.5,未识别0), "
        "season(季数整数,未识别1), nameEn, nameJp, nameZh, sub, dpi, source, group。"
    )

    def fmt(item):
        return {"messages": [
            {"role": "system", "content": SYS},
            {"role": "user", "content": f"请解析以下番剧标题：\n{item['input']}"},
            {"role": "assistant", "content": item["output"]},
        ]}

    ds = Dataset.from_list([fmt(d) for d in data])
    ds = ds.train_test_split(test_size=0.1, seed=42)
    print(f"  训练集: {len(ds['train'])}  验证集: {len(ds['test'])}")

    # ── 训练参数 ──
    # DML 不支持 autocast，CPU 更不行，都用 fp32
    use_fp16 = (not is_cpu and not is_dml)

    args = SFTConfig(
        output_dir=str(OUT_DIR / "checkpoints"),
        num_train_epochs=3,
        per_device_train_batch_size=1,
        per_device_eval_batch_size=1,
        gradient_accumulation_steps=8,
        warmup_ratio=0.05,
        learning_rate=2e-4,
        lr_scheduler_type="cosine",
        logging_steps=20,
        eval_strategy="steps",
        eval_steps=100,
        save_strategy="steps",
        save_steps=200,
        save_total_limit=2,
        load_best_model_at_end=True,
        metric_for_best_model="eval_loss",
        greater_is_better=False,
        fp16=use_fp16,
        bf16=False,
        report_to="none",
        dataset_text_field="messages",
        packing=False,
        seed=42,
        use_cpu=is_cpu,
    )

    trainer = SFTTrainer(
        model=model,
        args=args,
        train_dataset=ds["train"],
        eval_dataset=ds["test"],
        processing_class=tok,
    )

    print("=" * 55)
    if is_cpu:
        dev_name = "CPU (~2-3 小时)"
    elif is_dml:
        dev_name = "DirectML / AMD GPU (~20-40 分钟)"
    else:
        # CUDA GPU
        gpu_name = torch.cuda.get_device_name(0) if torch.cuda.is_available() else "GPU"
        dev_name = f"CUDA / {gpu_name} (~20-40 分钟)"
    print(f"Step 4: 开始训练 ({dev_name})")
    print("=" * 55)
    trainer.train()

    adapter = str(OUT_DIR / "anime_title_parser")
    trainer.save_model(adapter)
    tok.save_pretrained(adapter)
    print(f"  LoRA 已保存: {adapter}\n")
    return adapter


# ════════════════════════════════════════════════════════
# Step 4: 合并
# ════════════════════════════════════════════════════════
def step4_merge(model_path, adapter_path):
    import torch
    from transformers import AutoTokenizer, AutoModelForCausalLM
    from peft import PeftModel

    device = get_device()
    is_dml = (device.type == "privateuseone")

    print("=" * 55)
    print("Step 5: 合并 LoRA → 完整模型")
    print("=" * 55)

    if is_dml:
        base = AutoModelForCausalLM.from_pretrained(
            model_path, torch_dtype=torch.float32,
            trust_remote_code=True,
        )
        base = base.to(device)
    else:
        base = AutoModelForCausalLM.from_pretrained(
            model_path, torch_dtype=torch.float32,
            trust_remote_code=True, low_cpu_mem_usage=True,
        )
        if device.type != "cpu":
            base = base.to(device)

    m = PeftModel.from_pretrained(base, adapter_path)
    m = m.merge_and_unload()

    # 合并后移回 CPU 保存
    m = m.to("cpu")

    out = str(OUT_DIR / "merged")
    m.save_pretrained(out, safe_serialization=True)
    AutoTokenizer.from_pretrained(adapter_path).save_pretrained(out)

    print(f"  合并模型: {out}")
    print("=" * 55)
    print("完成！运行: ollama create anime-title-parser -f Modelfile")
    print("=" * 55)


# ════════════════════════════════════════════════════════
def main():
    step0_setup()
    data = step1_load_data()
    model_path = step2_download_model()
    adapter = step3_train(model_path, data)
    step4_merge(model_path, adapter)


if __name__ == "__main__":
    main()
