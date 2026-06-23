"""
Bangumi title parser fine-tuning script.

Target machine:
  Windows 10, 32 GB RAM, NVIDIA GeForce RTX 3060 12 GB

Model:
  Qwen/Qwen3.5-0.8B, loaded from traning/models/qwen3.5-0.8b when present.

Outputs:
  traning/models/checkpoints      intermediate checkpoints
  traning/models/lora_adapter     LoRA adapter
  traning/models/merged           merged Hugging Face model for GGUF/Ollama export
"""

from __future__ import annotations

import argparse
import faulthandler
import inspect
import json
import os
import sys
import time
import traceback
from dataclasses import dataclass
from pathlib import Path
from typing import Any

os.environ.setdefault("TOKENIZERS_PARALLELISM", "false")
os.environ.setdefault("HF_HUB_DISABLE_PROGRESS_BARS", "1")

HF_MIRROR = "https://hf-mirror.com"

BASE_DIR = Path(__file__).resolve().parent
DATA_DIR = BASE_DIR / "data"
MODEL_DIR = BASE_DIR / "models"
CACHE_DIR = MODEL_DIR / "cache"
LOCAL_MODEL_DIR = MODEL_DIR / "qwen3.5-0.8b"
CHECKPOINT_DIR = MODEL_DIR / "checkpoints"
LORA_ADAPTER_DIR = MODEL_DIR / "lora_adapter"
MERGED_MODEL_DIR = MODEL_DIR / "merged"
LOG_FILE = BASE_DIR / "train.log"

TRAIN_FILE = DATA_DIR / "train.jsonl"
VAL_FILE = DATA_DIR / "val.jsonl"
APP_DB_PATH = BASE_DIR.parent / "db" / "app.db"
BASE_MODEL = "Qwen/Qwen3.5-0.8B"

SYSTEM_PROMPT = """你是一个番剧标题结构化解析器。

任务：从标题中提取字段并只返回 JSON。

严格要求：
1. 只允许输出 JSON
2. 不允许输出 markdown
3. 不允许输出解释
4. 不允许输出思考过程
5. 不允许输出代码块

返回格式：
{"episode":"","season":1,"nameEn":"","nameJp":"","nameZh":"","sub":"","dpi":"","source":"","group":""}

规则：
- episode 剧集编号，默认 1，识别不了返回 NaN
- season 季数，默认 1，识别不了返回 NaN
- nameEn 英文名称
- nameJp 日文名称
- nameZh 中文名称
- sub 字幕语言
- dpi 分辨率，仅返回数字
- source 来源
- group 字幕组
"""


def init_logging() -> None:
    LOG_FILE.parent.mkdir(parents=True, exist_ok=True)
    with LOG_FILE.open("w", encoding="utf-8") as f:
        f.write(f"train.py started at {time.strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write(f"python={sys.executable}\n")
        f.write(f"cwd={Path.cwd()}\n")
        f.write(f"argv={sys.argv}\n")
        f.write("-" * 72 + "\n")


def log(message: str) -> None:
    line = f"[{time.strftime('%H:%M:%S')}] {message}"
    print(line, flush=True)
    try:
        with LOG_FILE.open("a", encoding="utf-8") as f:
            f.write(line + "\n")
    except Exception:
        pass


def log_exception(title: str, exc: BaseException | None = None) -> None:
    print(f"\n{title}", file=sys.stderr, flush=True)
    if exc is None:
        traceback.print_exc()
        text = traceback.format_exc()
    else:
        text = "".join(traceback.format_exception(type(exc), exc, exc.__traceback__))
        print(text, file=sys.stderr, flush=True)
    try:
        with LOG_FILE.open("a", encoding="utf-8") as f:
            f.write("\n" + title + "\n")
            f.write(text)
            f.write("\n")
    except Exception:
        pass


def install_exception_hooks() -> None:
    try:
        fault_log = open(BASE_DIR / "train_faulthandler.log", "w", encoding="utf-8")
        faulthandler.enable(file=fault_log, all_threads=True)
    except Exception as e:
        log(f"warning: faulthandler enable failed: {e}")

    def excepthook(exc_type, exc_value, exc_tb):
        text = "".join(traceback.format_exception(exc_type, exc_value, exc_tb))
        print("\nUNHANDLED EXCEPTION", file=sys.stderr, flush=True)
        print(text, file=sys.stderr, flush=True)
        try:
            with LOG_FILE.open("a", encoding="utf-8") as f:
                f.write("\nUNHANDLED EXCEPTION\n")
                f.write(text)
        except Exception:
            pass

    sys.excepthook = excepthook


def step(title: str) -> None:
    log(f">>> {title}")


def setup_china_mirrors() -> None:
    os.environ.setdefault("HF_ENDPOINT", HF_MIRROR)
    os.environ.setdefault("HF_HOME", str(CACHE_DIR / "hf_home"))
    os.environ.setdefault("TRANSFORMERS_CACHE", str(CACHE_DIR / "transformers"))
    log(f"HF_ENDPOINT={os.environ['HF_ENDPOINT']}")


def ensure_data_files() -> None:
    if TRAIN_FILE.exists() and VAL_FILE.exists():
        return

    step("训练数据不存在，自动从 db/app.db 生成")
    if not APP_DB_PATH.exists():
        raise FileNotFoundError(
            f"missing training data and database. Run `python extract_and_label.py` "
            f"after placing app.db at: {APP_DB_PATH}"
        )

    try:
        from extract_and_label import generate_training_data
    except Exception as e:
        raise RuntimeError("cannot import extract_and_label.py to generate training data") from e

    generate_training_data(APP_DB_PATH, DATA_DIR, val_ratio=0.05, seed=42)


def load_jsonl(path: Path) -> list[dict[str, Any]]:
    if not path.exists():
        raise FileNotFoundError(f"missing data file: {path}")

    rows: list[dict[str, Any]] = []
    with path.open("r", encoding="utf-8") as f:
        for line_no, line in enumerate(f, 1):
            line = line.strip()
            if not line:
                continue
            item = json.loads(line)
            if "input" not in item or "output" not in item:
                raise ValueError(f"{path}:{line_no} must contain input/output")
            rows.append(item)
    if not rows:
        raise ValueError(f"{path} is empty")
    return rows


def normalize_output(output: Any) -> str:
    if isinstance(output, str):
        return output.strip()
    return json.dumps(output, ensure_ascii=False, separators=(",", ":"))


def build_messages(title: str, answer: str | None = None) -> list[dict[str, str]]:
    messages = [
        {"role": "system", "content": SYSTEM_PROMPT},
        {"role": "user", "content": f"请解析以下番剧标题：\n{title}"},
    ]
    if answer is not None:
        messages.append({"role": "assistant", "content": answer})
    return messages


@dataclass
class TokenizedExample:
    input_ids: list[int]
    attention_mask: list[int]
    labels: list[int]


class JsonlSftDataset:
    def __init__(self, rows: list[dict[str, Any]], tokenizer: Any, max_seq_length: int):
        self.examples = [
            self._tokenize(row, tokenizer, max_seq_length)
            for row in rows
        ]

    def __len__(self) -> int:
        return len(self.examples)

    def __getitem__(self, idx: int) -> dict[str, list[int]]:
        item = self.examples[idx]
        return {
            "input_ids": item.input_ids,
            "attention_mask": item.attention_mask,
            "labels": item.labels,
        }

    @staticmethod
    def _render(tokenizer: Any, messages: list[dict[str, str]], add_generation_prompt: bool) -> str:
        if hasattr(tokenizer, "apply_chat_template"):
            return tokenizer.apply_chat_template(
                messages,
                tokenize=False,
                add_generation_prompt=add_generation_prompt,
            )

        rendered = []
        for msg in messages:
            rendered.append(f"<|im_start|>{msg['role']}\n{msg['content']}<|im_end|>")
        if add_generation_prompt:
            rendered.append("<|im_start|>assistant\n")
        return "\n".join(rendered)

    def _tokenize(self, row: dict[str, Any], tokenizer: Any, max_seq_length: int) -> TokenizedExample:
        answer = normalize_output(row["output"])
        prompt_text = self._render(tokenizer, build_messages(row["input"]), True)
        full_text = self._render(tokenizer, build_messages(row["input"], answer), False)

        prompt_ids = tokenizer(prompt_text, add_special_tokens=False)["input_ids"]
        full = tokenizer(
            full_text,
            add_special_tokens=False,
            truncation=True,
            max_length=max_seq_length,
        )

        input_ids = full["input_ids"]
        attention_mask = full["attention_mask"]
        prompt_len = min(len(prompt_ids), len(input_ids))
        labels = [-100] * prompt_len + input_ids[prompt_len:]

        if not any(label != -100 for label in labels):
            labels[-1] = input_ids[-1]

        return TokenizedExample(input_ids, attention_mask, labels)


def check_cuda() -> None:
    step("检查 CUDA / GPU")
    import torch

    if not torch.cuda.is_available():
        raise RuntimeError("未检测到 CUDA GPU。请确认已安装 NVIDIA 驱动和 CUDA 版 PyTorch。")

    props = torch.cuda.get_device_properties(0)
    log(f"GPU: {props.name}, VRAM={props.total_memory / 1024 ** 3:.1f} GB")
    log(f"PyTorch={torch.__version__}, CUDA={torch.version.cuda}")
    log(f"bf16 supported={torch.cuda.is_bf16_supported()}")


def load_tokenizer(model_path: str, trust_remote_code: bool):
    step("导入 transformers.AutoTokenizer")
    from transformers import AutoTokenizer

    step(f"加载 tokenizer: {model_path}")
    tokenizer = AutoTokenizer.from_pretrained(
        model_path,
        cache_dir=str(CACHE_DIR),
        trust_remote_code=trust_remote_code,
    )
    if tokenizer.pad_token is None:
        tokenizer.pad_token = tokenizer.eos_token
    tokenizer.padding_side = "right"
    log(f"tokenizer loaded, vocab_size={len(tokenizer)}")
    return tokenizer


def load_base_model(model_path: str, trust_remote_code: bool):
    step("导入 torch / transformers 模型加载组件")
    import torch
    from transformers import AutoModelForCausalLM, BitsAndBytesConfig

    compute_dtype = torch.bfloat16 if torch.cuda.is_bf16_supported() else torch.float16
    log(f"model compute dtype={compute_dtype}")
    bnb_config = BitsAndBytesConfig(
        load_in_4bit=True,
        bnb_4bit_quant_type="nf4",
        bnb_4bit_compute_dtype=compute_dtype,
        bnb_4bit_use_double_quant=True,
    )
    kwargs = {
        "cache_dir": str(CACHE_DIR),
        "device_map": "auto",
        "low_cpu_mem_usage": True,
        "quantization_config": bnb_config,
        "torch_dtype": compute_dtype,
        "trust_remote_code": trust_remote_code,
    }

    try:
        step(f"加载 4-bit base model: {model_path}")
        return AutoModelForCausalLM.from_pretrained(model_path, **kwargs)
    except Exception as causal_error:
        try:
            from transformers import AutoModelForImageTextToText

            log(f"AutoModelForCausalLM 加载失败，尝试 ImageTextToText: {causal_error}")
            return AutoModelForImageTextToText.from_pretrained(model_path, **kwargs)
        except Exception:
            raise causal_error


def lora_targets(model: Any) -> list[str]:
    step("扫描 LoRA target modules")
    import torch.nn as nn

    preferred = {
        "q_proj", "k_proj", "v_proj", "o_proj",
        "gate_proj", "up_proj", "down_proj",
        "in_proj", "x_proj", "dt_proj", "out_proj",
    }
    found = set()
    linear = set()
    for name, module in model.named_modules():
        leaf = name.rsplit(".", 1)[-1]
        if leaf in preferred:
            found.add(leaf)
        if isinstance(module, nn.Linear):
            linear.add(leaf)

    targets = sorted(found)
    if not targets:
        blocked = {"lm_head", "embed_tokens", "embed_in", "wte", "wpe"}
        targets = sorted(m for m in linear if m not in blocked)

    if not targets:
        raise RuntimeError("没有找到可注入 LoRA 的 Linear 模块")
    log(f"LoRA target modules: {targets}")
    return targets


def prepare_lora_model(model: Any, args: argparse.Namespace):
    step("导入 PEFT 并准备 QLoRA")
    from peft import LoraConfig, get_peft_model, prepare_model_for_kbit_training

    step("prepare_model_for_kbit_training")
    model.config.use_cache = False
    model = prepare_model_for_kbit_training(model)
    config = LoraConfig(
        r=args.lora_r,
        lora_alpha=args.lora_alpha,
        lora_dropout=args.lora_dropout,
        bias="none",
        task_type="CAUSAL_LM",
        target_modules=lora_targets(model),
    )
    step("注入 LoRA adapter")
    model = get_peft_model(model, config)
    model.print_trainable_parameters()
    return model


def build_training_args(args: argparse.Namespace):
    step("构建 TrainingArguments")
    import torch
    from transformers import TrainingArguments

    params = inspect.signature(TrainingArguments.__init__).parameters
    eval_key = "eval_strategy" if "eval_strategy" in params else "evaluation_strategy"
    use_bf16 = torch.cuda.is_bf16_supported()

    kwargs: dict[str, Any] = {
        "output_dir": str(CHECKPOINT_DIR),
        "per_device_train_batch_size": args.batch_size,
        "per_device_eval_batch_size": args.batch_size,
        "gradient_accumulation_steps": args.gradient_accumulation_steps,
        "num_train_epochs": args.epochs,
        "learning_rate": args.learning_rate,
        "warmup_ratio": args.warmup_ratio,
        "lr_scheduler_type": "cosine",
        "logging_steps": args.logging_steps,
        "save_steps": args.save_steps,
        "save_total_limit": 3,
        eval_key: "steps",
        "eval_steps": args.eval_steps,
        "bf16": use_bf16,
        "fp16": not use_bf16,
        "optim": args.optim,
        "report_to": "none",
        "seed": args.seed,
        "dataloader_num_workers": 0,
        "dataloader_pin_memory": True,
        "remove_unused_columns": False,
        "gradient_checkpointing": True,
        "max_grad_norm": 1.0,
        "load_best_model_at_end": True,
        "metric_for_best_model": "eval_loss",
        "greater_is_better": False,
    }
    return TrainingArguments(**kwargs)


def train(args: argparse.Namespace) -> None:
    step("导入 torch")
    import torch
    log(f"torch imported: version={torch.__version__}")

    step("导入 transformers.DataCollatorForSeq2Seq")
    from transformers import DataCollatorForSeq2Seq
    log("DataCollatorForSeq2Seq imported")

    step("导入 transformers.Trainer")
    from transformers import Trainer
    log("Trainer imported")

    step("创建输出目录")
    MODEL_DIR.mkdir(parents=True, exist_ok=True)
    CACHE_DIR.mkdir(parents=True, exist_ok=True)

    model_path = str(LOCAL_MODEL_DIR) if (LOCAL_MODEL_DIR / "config.json").exists() else args.base_model
    log(f"base model: {model_path}")

    check_cuda()
    tokenizer = load_tokenizer(model_path, args.trust_remote_code)

    ensure_data_files()

    step(f"读取训练数据: {TRAIN_FILE}")
    train_rows = load_jsonl(TRAIN_FILE)
    step(f"读取验证数据: {VAL_FILE}")
    val_rows = load_jsonl(VAL_FILE)
    log(f"train samples={len(train_rows)}, val samples={len(val_rows)}")

    step("构建 tokenized train dataset")
    train_ds = JsonlSftDataset(train_rows, tokenizer, args.max_seq_length)
    step("构建 tokenized val dataset")
    val_ds = JsonlSftDataset(val_rows, tokenizer, args.max_seq_length)

    step("清理 CUDA cache")
    torch.cuda.empty_cache()
    model = load_base_model(model_path, args.trust_remote_code)
    model = prepare_lora_model(model, args)

    step("构建 DataCollator")
    collator = DataCollatorForSeq2Seq(
        tokenizer=tokenizer,
        model=model,
        label_pad_token_id=-100,
        pad_to_multiple_of=8,
    )

    effective_batch = args.batch_size * args.gradient_accumulation_steps
    log(
        "training config: "
        f"epochs={args.epochs}, batch={args.batch_size}, grad_accum={args.gradient_accumulation_steps}, "
        f"effective_batch={effective_batch}, max_seq={args.max_seq_length}, lr={args.learning_rate}"
    )

    step("构建 Trainer")
    trainer_kwargs: dict[str, Any] = {
        "model": model,
        "args": build_training_args(args),
        "train_dataset": train_ds,
        "eval_dataset": val_ds,
        "data_collator": collator,
    }
    trainer_params = inspect.signature(Trainer.__init__).parameters
    if "processing_class" in trainer_params:
        trainer_kwargs["processing_class"] = tokenizer
    elif "tokenizer" in trainer_params:
        trainer_kwargs["tokenizer"] = tokenizer
    trainer = Trainer(**trainer_kwargs)

    step("开始训练 trainer.train")
    started = time.time()
    trainer.train(resume_from_checkpoint=args.resume_from_checkpoint)
    log(f"training finished in {(time.time() - started) / 60:.1f} minutes")

    if torch.cuda.is_available():
        log(f"peak VRAM={torch.cuda.max_memory_allocated() / 1024 ** 3:.2f} GB")

    step(f"保存 LoRA adapter -> {LORA_ADAPTER_DIR}")
    model.save_pretrained(str(LORA_ADAPTER_DIR), safe_serialization=True)
    tokenizer.save_pretrained(str(LORA_ADAPTER_DIR))

    step(f"使用 fp16 基座模型重新合并 LoRA -> {MERGED_MODEL_DIR}")
    del trainer
    del model
    torch.cuda.empty_cache()
    from merge_lora_for_gguf import main as merge_lora_for_gguf

    merge_lora_for_gguf()
    log("训练、保存、合并全部完成")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Fine-tune Qwen3.5-0.8B for bangumi title parsing")
    parser.add_argument("--base-model", default=BASE_MODEL)
    parser.add_argument("--no-mirror", action="store_true", help="do not set HF mainland mirror")
    parser.add_argument("--trust-remote-code", action="store_true", default=True)
    parser.add_argument("--resume-from-checkpoint", default=None)
    parser.add_argument("--max-seq-length", type=int, default=512)
    parser.add_argument("--epochs", type=float, default=3)
    parser.add_argument("--batch-size", type=int, default=4)
    parser.add_argument("--gradient-accumulation-steps", type=int, default=4)
    parser.add_argument("--learning-rate", type=float, default=2e-4)
    parser.add_argument("--warmup-ratio", type=float, default=0.1)
    parser.add_argument("--lora-r", type=int, default=16)
    parser.add_argument("--lora-alpha", type=int, default=32)
    parser.add_argument("--lora-dropout", type=float, default=0.05)
    parser.add_argument("--logging-steps", type=int, default=5)
    parser.add_argument("--save-steps", type=int, default=100)
    parser.add_argument("--eval-steps", type=int, default=100)
    parser.add_argument("--optim", default="paged_adamw_8bit")
    parser.add_argument("--seed", type=int, default=42)
    return parser.parse_args()


def main() -> None:
    init_logging()
    install_exception_hooks()
    args = parse_args()
    if not args.no_mirror:
        setup_china_mirrors()

    print("=" * 72)
    print("Bangumi Title Parser Training")
    print("Windows 10 / RTX 3060 12GB / Qwen3.5-0.8B / QLoRA")
    print("=" * 72)
    log(f"log file: {LOG_FILE}")
    log(f"python: {sys.executable}")
    log(f"working dir: {Path.cwd()}")
    log(f"args: {vars(args)}")

    try:
        train(args)
    except KeyboardInterrupt:
        log("interrupted")
        sys.exit(130)
    except SystemExit as e:
        log(f"SystemExit: code={e.code}")
        raise
    except BaseException as e:
        log_exception("FATAL ERROR", e)
        sys.exit(1)

    print("=" * 72)
    print(f"Merged model: {MERGED_MODEL_DIR}")
    print("Next: python export_to_gguf.py")
    print("=" * 72)


if __name__ == "__main__":
    main()
