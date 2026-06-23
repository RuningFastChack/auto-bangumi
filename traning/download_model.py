"""
Download Qwen/Qwen3.5-0.8B to traning/models/qwen3.5-0.8b.

Mainland China path:
  1. ModelScope first
  2. Hugging Face mirror fallback: https://hf-mirror.com
"""

from __future__ import annotations

import argparse
import os
import shutil
import sys
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent
LOCAL_DIR = BASE_DIR / "models" / "qwen3.5-0.8b"
MODEL_ID = "Qwen/Qwen3.5-0.8B"
HF_MIRROR = "https://hf-mirror.com"


def dir_size_mb(path: Path) -> float:
    return sum(f.stat().st_size for f in path.rglob("*") if f.is_file()) / 1024 / 1024


def download_modelscope() -> str:
    from modelscope import snapshot_download

    print(f"[ModelScope] {MODEL_ID} -> {LOCAL_DIR}")
    return snapshot_download(MODEL_ID, local_dir=str(LOCAL_DIR))


def download_hf_mirror() -> str:
    from huggingface_hub import snapshot_download

    os.environ["HF_ENDPOINT"] = HF_MIRROR
    print(f"[HF mirror] {MODEL_ID} -> {LOCAL_DIR}")
    return snapshot_download(MODEL_ID, local_dir=str(LOCAL_DIR), resume_download=True)


def main() -> None:
    parser = argparse.ArgumentParser(description="Download Qwen3.5-0.8B")
    parser.add_argument("--force", action="store_true", help="remove existing local model first")
    args = parser.parse_args()

    if args.force and LOCAL_DIR.exists():
        print(f"Removing existing model: {LOCAL_DIR}")
        shutil.rmtree(LOCAL_DIR)

    LOCAL_DIR.mkdir(parents=True, exist_ok=True)

    if (LOCAL_DIR / "config.json").exists():
        print(f"Model already exists: {LOCAL_DIR} ({dir_size_mb(LOCAL_DIR):.0f} MB)")
        return

    try:
        model_dir = download_modelscope()
    except Exception as e:
        print(f"ModelScope failed: {e}")
        print("Trying Hugging Face mirror...")
        try:
            model_dir = download_hf_mirror()
        except Exception as e2:
            print(f"HF mirror failed: {e2}")
            print("Manual fallback:")
            print(f"  1. Open {HF_MIRROR}/{MODEL_ID}")
            print(f"  2. Download all files into {LOCAL_DIR}")
            sys.exit(1)

    print(f"Download finished: {model_dir}")
    print(f"Local size: {dir_size_mb(LOCAL_DIR):.0f} MB")


if __name__ == "__main__":
    main()
