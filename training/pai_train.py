# -*- coding: utf-8 -*-
"""
PAI-DSW 云端训练脚本
镜像: torcheasyrec:1.2.0-pytorch2.11.0-gpu-py311-cu126-ubuntu22.04

一键运行（数据已上传到 /mnt/workspace/）:
  cd /mnt/workspace/training
  python pai_train.py

产生:
  output/adapter/   LoRA 权重 (~36MB)
  output/merged/    合并后完整模型 (~3GB)
"""

import os, sys, json, random
from pathlib import Path

BASE = Path(__file__).parent
DATA_DIR = BASE / "data"
OUT_DIR = BASE / "output"
MIRROR = "https://mirrors.cloud.aliyuncs.com/pypi/simple"


# ════════════════════════════════════════════════════════
# Step 0: 安装依赖（PyTorch 已预装）
# ════════════════════════════════════════════════════════
def setup():
    import subprocess as sp
    print("=" * 55)
    print("Step 0: 安装依赖")
    print("=" * 55)
    pkgs = [
        "modelscope",           # 阿里模型库，秒下 Qwen
        "transformers",
        "peft",
        "datasets",
        "accelerate",
        "trl",
        "bitsandbytes",         # QLoRA 4bit
    ]
    sp.run(
        [sys.executable, "-m", "pip", "install",
         "-i", MIRROR, "--trusted-host", "mirrors.cloud.aliyuncs.com"] + pkgs,
        check=True,
    )
    print("OK\n")


# ════════════════════════════════════════════════════════
# Step 1: 加载数据
# ════════════════════════════════════════════════════════
def load_data():
    print("=" * 55)
    print("Step 1: 加载训练数据")
    print("=" * 55)
    f = DATA_DIR / "merged_all.json"
    if not f.exists():
        print(f"× 找不到 {f}，请先本地运行 extract_data.py")
        sys.exit(1)
    with open(f, encoding="utf-8") as fp:
        raw = json.load(fp)
    # 去重
    seen = set()
    data = []
    for item in raw:
        inp = item.get("input", "")
        if inp and inp not in seen:
            seen.add(inp)
            data.append(item)
    print(f"  {len(data)} 条（去重后）\n")
    return data


# ════════════════════════════════════════════════════════
# Step 2: 加载模型 & LoRA 配置
# ════════════════════════════════════════════════════════
def load_model():
    import torch
    from modelscope import snapshot_download
    from transformers import (
        AutoTokenizer,
        AutoModelForCausalLM,
        BitsAndBytesConfig,
    )
    from peft import LoraConfig, TaskType

    print("=" * 55)
    print("Step 2: 加载 Qwen2.5-1.5B-Instruct (ModelScope)")
    print("=" * 55)

    gpu = torch.cuda.get_device_name(0)
    vram = torch.cuda.get_device_properties(0).total_memory / 1024**3
    print(f"  GPU: {gpu}  |  VRAM: {vram:.1f} GB")

    # 用 ModelScope SDK 下载到本地（阿里内网秒下）
    print("  从 ModelScope 下载模型…")
    local_dir = snapshot_download("Qwen/Qwen2.5-1.5B-Instruct")
    print(f"  本地路径: {local_dir}")

    bnb = BitsAndBytesConfig(
        load_in_4bit=True,
        bnb_4bit_compute_dtype=torch.float16,
        bnb_4bit_quant_type="nf4",
        bnb_4bit_use_double_quant=True,
    )

    model = AutoModelForCausalLM.from_pretrained(
        local_dir,
        quantization_config=bnb,
        torch_dtype=torch.float16,
        trust_remote_code=True,
        device_map="auto",
        local_files_only=True,
    )
    tok = AutoTokenizer.from_pretrained(local_dir, trust_remote_code=True)
    if tok.pad_token is None:
        tok.pad_token = tok.eos_token

    lora = LoraConfig(
        task_type=TaskType.CAUSAL_LM,
        r=16,
        lora_alpha=32,
        lora_dropout=0.05,
        target_modules=["q_proj", "k_proj", "v_proj", "o_proj",
                        "gate_proj", "up_proj", "down_proj"],
    )
    model.config.use_cache = False
    print("  OK\n")
    return model, tok, lora


# ════════════════════════════════════════════════════════
# Step 3: 训练
# ════════════════════════════════════════════════════════
def train(model, tokenizer, lora_config, data):
    from datasets import Dataset
    from trl import SFTTrainer, SFTConfig

    print("=" * 55)
    print("Step 3: 训练 (QLoRA, 3 epochs)")
    print("=" * 55)

    SYS = (
        "你是一个动漫种子标题解析专家。从番剧标题中提取结构化信息。"
        "仅返回合法JSON，不要代码块或解释。字段: episode(集数字符串,例05/12.5,未识别0), "
        "season(季数整数,未识别1), nameEn, nameJp, nameZh, sub, dpi, source, group。"
    )

    # 显式拼 Qwen2.5 chat 格式，不依赖 tokenizer.chat_template
    # 格式: <|im_start|>system\n...<|im_end|>\n<|im_start|>user\n...<|im_end|>\n<|im_start|>assistant\n...<|im_end|>\n
    def formatting_func(samples):
        texts = []
        for i in range(len(samples["input"])):
            text = (
                f"<|im_start|>system\n{SYS}<|im_end|>\n"
                f"<|im_start|>user\n请解析以下番剧标题：\n{samples['input'][i]}<|im_end|>\n"
                f"<|im_start|>assistant\n{samples['output'][i]}<|im_end|>\n"
            )
            texts.append(text)
        return texts

    ds = Dataset.from_list([{"input": d["input"], "output": d["output"]} for d in data])
    ds = ds.train_test_split(test_size=0.1, seed=42)
    print(f"  训练集: {len(ds['train'])}  验证集: {len(ds['test'])}")

    bs, ga = 4, 2
    epochs = 3

    args = SFTConfig(
        output_dir=str(OUT_DIR / "adapter"),
        num_train_epochs=epochs,
        per_device_train_batch_size=bs,
        per_device_eval_batch_size=bs,
        gradient_accumulation_steps=ga,
        warmup_ratio=0.05,
        learning_rate=2e-4,
        lr_scheduler_type="cosine",
        logging_steps=10,
        eval_strategy="steps",
        eval_steps=100,
        save_strategy="steps",
        save_steps=200,
        save_total_limit=2,
        load_best_model_at_end=True,
        metric_for_best_model="eval_loss",
        greater_is_better=False,
        fp16=False,
        bf16=False,
        report_to="none",
        packing=False,
        seed=42,
    )

    trainer = SFTTrainer(
        model=model,
        args=args,
        train_dataset=ds["train"],
        eval_dataset=ds["test"],
        processing_class=tokenizer,
        formatting_func=formatting_func,
        peft_config=lora_config,
    )

    print(f"  开始训练（约 5-8 分钟）…")
    trainer.train()
    adapter = str(OUT_DIR / "adapter" / "final")
    trainer.save_model(adapter)
    tokenizer.save_pretrained(adapter)
    print(f"  LoRA 已保存: {adapter}\n")
    return adapter


# ════════════════════════════════════════════════════════
# Step 4: 合并 & 导出
# ════════════════════════════════════════════════════════
def merge(adapter_path):
    import torch
    from modelscope import snapshot_download
    from transformers import AutoTokenizer, AutoModelForCausalLM
    from peft import PeftModel

    print("=" * 55)
    print("Step 4: 合并 LoRA → 完整模型")
    print("=" * 55)

    local_dir = snapshot_download("Qwen/Qwen2.5-1.5B-Instruct")
    base = AutoModelForCausalLM.from_pretrained(
        local_dir, torch_dtype=torch.float16, trust_remote_code=True, device_map="auto",
        local_files_only=True,
    )
    m = PeftModel.from_pretrained(base, adapter_path)
    m = m.merge_and_unload()

    out = str(OUT_DIR / "merged")
    m.save_pretrained(out, safe_serialization=True)
    out_tok = AutoTokenizer.from_pretrained(adapter_path)
    out_tok.save_pretrained(out)

    # 注入 chat_template 到 tokenizer_config.json（Ollama 需要）
    # Qwen2.5 将其存为独立 chat_template.jinja 文件，Ollama 不识别
    from pathlib import Path as P
    jinja_file = P(out) / "chat_template.jinja"
    tok_cfg_file = P(out) / "tokenizer_config.json"
    if jinja_file.exists():
        with open(jinja_file, encoding="utf-8") as f:
            cht = f.read()
        with open(tok_cfg_file, encoding="utf-8") as f:
            cfg = json.load(f)
        cfg["chat_template"] = cht
        with open(tok_cfg_file, "w", encoding="utf-8") as f:
            json.dump(cfg, f, indent=2, ensure_ascii=False)
        print("  chat_template 已注入 tokenizer_config.json")

    print(f"  合并模型: {out}")
    print("=" * 55)
    print("完成！下载 output/merged/ 到本地，用 Modelfile 导入 Ollama")
    print("=" * 55)


# ════════════════════════════════════════════════════════
def main():
    os.environ["MODELSCOPE_CACHE"] = "/mnt/workspace/.cache/modelscope"
    setup()
    data = load_data()
    model, tok, lora = load_model()
    adapter = train(model, tok, lora, data)
    merge(adapter)


if __name__ == "__main__":
    main()
