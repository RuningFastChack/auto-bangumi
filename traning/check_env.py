from __future__ import annotations

import importlib
import sys
import traceback


def check_module(name: str) -> None:
    print(f"\n=== {name} ===", flush=True)
    try:
        module = importlib.import_module(name)
        version = getattr(module, "__version__", "unknown")
        print(f"OK: {name} {version}", flush=True)
    except BaseException:
        print(f"FAILED: {name}", flush=True)
        traceback.print_exc()


print(f"Python executable: {sys.executable}", flush=True)
print(f"Python version: {sys.version}", flush=True)

check_module("torch")

try:
    import torch

    print(f"CUDA available: {torch.cuda.is_available()}", flush=True)
    if torch.cuda.is_available():
        print(f"CUDA version: {torch.version.cuda}", flush=True)
        print(f"GPU: {torch.cuda.get_device_name(0)}", flush=True)
        total = torch.cuda.get_device_properties(0).total_memory / 1024**3
        free = torch.cuda.mem_get_info()[0] / 1024**3
        print(f"VRAM: {free:.1f} GB free / {total:.1f} GB total", flush=True)
except BaseException:
    print("FAILED: torch CUDA check", flush=True)
    traceback.print_exc()

check_module("transformers")
check_module("accelerate")
check_module("peft")
check_module("bitsandbytes")
check_module("safetensors")
