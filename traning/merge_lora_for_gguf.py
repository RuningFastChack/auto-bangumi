"""
Merge the trained LoRA adapter into a clean fp16 Hugging Face model.

Why this exists:
  train.py uses QLoRA/bitsandbytes to fit training into 12 GB VRAM. A direct
  merge from that k-bit model can leave bitsandbytes tensors in models/merged,
  and llama.cpp cannot convert those to GGUF.

Run this after training and before convert_windows_gguf.bat:

  .\.venv\Scripts\python.exe merge_lora_for_gguf.py
"""

from __future__ import annotations

import json
import shutil
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent
MODEL_DIR = BASE_DIR / "models"
BASE_MODEL_DIR = MODEL_DIR / "qwen3.5-0.8b"
LORA_ADAPTER_DIR = MODEL_DIR / "lora_adapter"
MERGED_MODEL_DIR = MODEL_DIR / "merged"
TMP_MERGED_DIR = BASE_DIR / "tmp" / "merged-fp16-tmp"


def require_file(path: Path) -> None:
    if not path.exists():
        raise FileNotFoundError(path)


def load_base_model(model_path: Path):
    import torch
    from transformers import AutoModelForCausalLM

    kwargs = {
        "device_map": "cpu",
        "low_cpu_mem_usage": True,
        "torch_dtype": torch.float16,
        "trust_remote_code": True,
    }

    print(f"Loading fp16 base model on CPU: {model_path}", flush=True)
    return AutoModelForCausalLM.from_pretrained(str(model_path), **kwargs)


def sanitize_config(path: Path) -> None:
    config_path = path / "config.json"
    data = json.loads(config_path.read_text(encoding="utf-8"))
    data.pop("quantization_config", None)
    data["dtype"] = "float16"
    config_path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    require_file(BASE_MODEL_DIR / "config.json")
    require_file(LORA_ADAPTER_DIR / "adapter_model.safetensors")

    if TMP_MERGED_DIR.exists():
        shutil.rmtree(TMP_MERGED_DIR)
    TMP_MERGED_DIR.mkdir(parents=True, exist_ok=True)

    from peft import PeftModel
    from transformers import AutoTokenizer

    base = load_base_model(BASE_MODEL_DIR)
    print(f"Loading LoRA adapter: {LORA_ADAPTER_DIR}", flush=True)
    model = PeftModel.from_pretrained(base, str(LORA_ADAPTER_DIR), is_trainable=False)

    print("Merging LoRA into fp16 base model...", flush=True)
    merged = model.merge_and_unload()
    merged.config.use_cache = True

    print(f"Saving clean merged model: {TMP_MERGED_DIR}", flush=True)
    merged.save_pretrained(str(TMP_MERGED_DIR), safe_serialization=True, max_shard_size="4GB")

    tokenizer = AutoTokenizer.from_pretrained(str(LORA_ADAPTER_DIR), trust_remote_code=True)
    tokenizer.save_pretrained(str(TMP_MERGED_DIR))
    sanitize_config(TMP_MERGED_DIR)

    if MERGED_MODEL_DIR.exists():
        shutil.rmtree(MERGED_MODEL_DIR)
    TMP_MERGED_DIR.rename(MERGED_MODEL_DIR)

    print(f"Done. Clean GGUF-ready model is at: {MERGED_MODEL_DIR}", flush=True)
    print("Next: .\\convert_windows_gguf.bat", flush=True)


if __name__ == "__main__":
    main()
