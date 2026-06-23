"""
Prepare the fine-tuned model for Ubuntu + Ollama deployment.

This script runs on Windows after train.py finishes. By default it creates:
  traning/models/Modelfile
  traning/models/deploy_ubuntu.sh

If --package-hf is passed, it also creates:
  traning/models/bangumi-parser-hf.zip

For an Ubuntu server with only Ollama installed, run convert_windows_gguf.bat
on Windows, then upload bangumi-parser-Q4_K_M.gguf and Modelfile.
"""

from __future__ import annotations

import argparse
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent
MODEL_DIR = BASE_DIR / "models"
MERGED_MODEL_DIR = MODEL_DIR / "merged"
PACKAGE_OUTPUT = MODEL_DIR / "bangumi-parser-hf.zip"
MODELFILE_PATH = MODEL_DIR / "Modelfile"
DEPLOY_SCRIPT_PATH = MODEL_DIR / "deploy_ubuntu.sh"
GGUF_NAME = "bangumi-parser-Q4_K_M.gguf"

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
"""


def check_merged_model() -> None:
    required = ["config.json", "tokenizer.json", "tokenizer_config.json"]
    missing = [name for name in required if not (MERGED_MODEL_DIR / name).exists()]
    weights = list(MERGED_MODEL_DIR.glob("*.safetensors")) + list(MERGED_MODEL_DIR.glob("*.bin"))
    if missing or not weights:
        raise FileNotFoundError(
            "Merged model is incomplete. Run `python train.py` first. "
            f"missing={missing}, weights={len(weights)}"
        )


def generate_modelfile() -> Path:
    print(f"[1/3] Writing Modelfile: {MODELFILE_PATH}", flush=True)
    content = f"""# Bangumi title parser, fine-tuned from Qwen/Qwen3.5-0.8B
# Create with: ollama create bangumi-parser -f Modelfile

FROM ./{GGUF_NAME}

SYSTEM \"\"\"{SYSTEM_PROMPT}\"\"\"

TEMPLATE \"\"\"{{{{ if .System }}}}<|im_start|>system
{{{{ .System }}}}<|im_end|>
{{{{ end }}}}{{{{ if .Prompt }}}}<|im_start|>user
{{{{ .Prompt }}}}<|im_end|>
{{{{ end }}}}<|im_start|>assistant
\"\"\"

PARAMETER temperature 0.1
PARAMETER top_p 0.9
PARAMETER top_k 20
PARAMETER num_predict 256
PARAMETER repeat_penalty 1.05
PARAMETER stop "<|im_end|>"
PARAMETER stop "<|im_start|>"
"""
    MODELFILE_PATH.write_text(content, encoding="utf-8", newline="\n")
    return MODELFILE_PATH


def generate_deploy_script() -> Path:
    print(f"[2/3] Writing optional Ubuntu conversion script: {DEPLOY_SCRIPT_PATH}", flush=True)
    content = f"""#!/usr/bin/env bash
set -euo pipefail

MODEL_NAME="${{1:-bangumi-parser}}"
WORKDIR="${{2:-$HOME/bangumi-parser}}"
ZIP_FILE="${{3:-bangumi-parser-hf.zip}}"
PIP_INDEX="https://pypi.tuna.tsinghua.edu.cn/simple"
LLAMA_CPP_REPO="${{LLAMA_CPP_REPO:-https://github.com/ggerganov/llama.cpp.git}}"
PROXY="${{PROXY:-}}"

if [ -n "$PROXY" ]; then
  export HTTP_PROXY="$PROXY"
  export HTTPS_PROXY="$PROXY"
  export http_proxy="$PROXY"
  export https_proxy="$PROXY"
  echo "Using proxy: $PROXY"
fi

mkdir -p "$WORKDIR"
cd "$WORKDIR"

if [[ "$ZIP_FILE" != /* ]]; then
  ZIP_FILE="$PWD/$ZIP_FILE"
fi

if [ ! -d hf-model ]; then
  unzip -o "$ZIP_FILE" -d hf-model
fi

if [ ! -d venv ]; then
  python3 -m venv venv
fi
source venv/bin/activate
pip install -U pip -i "$PIP_INDEX"
pip install -U numpy sentencepiece safetensors transformers protobuf -i "$PIP_INDEX"

if [ ! -d llama.cpp ]; then
  if [ -n "$PROXY" ]; then
    git clone -c http.proxy="$PROXY" -c https.proxy="$PROXY" "$LLAMA_CPP_REPO" llama.cpp
  else
    git clone "$LLAMA_CPP_REPO" llama.cpp
  fi
fi

cmake -S llama.cpp -B llama.cpp/build -DLLAMA_NATIVE=ON
cmake --build llama.cpp/build --config Release -j"$(nproc)"

python3 llama.cpp/convert_hf_to_gguf.py hf-model --outfile bangumi-parser-f16.gguf --outtype f16

QUANT_BIN="./llama.cpp/build/bin/llama-quantize"
if [ ! -x "$QUANT_BIN" ]; then
  QUANT_BIN="./llama.cpp/build/bin/quantize"
fi
"$QUANT_BIN" bangumi-parser-f16.gguf {GGUF_NAME} Q4_K_M

cat > Modelfile <<'EOF'
{MODELFILE_PATH.read_text(encoding="utf-8") if MODELFILE_PATH.exists() else ""}
EOF

ollama create "$MODEL_NAME" -f Modelfile
echo "Created Ollama model: $MODEL_NAME"
echo "Test with: ollama run $MODEL_NAME '请解析以下番剧标题： [ANi] Test Anime - 01 [1080P][Baha][CHT]'"
"""
    DEPLOY_SCRIPT_PATH.write_text(content, encoding="utf-8", newline="\n")
    return DEPLOY_SCRIPT_PATH


def package_model() -> Path:
    print(f"[3/3] Packaging Hugging Face model: {PACKAGE_OUTPUT}", flush=True)
    if PACKAGE_OUTPUT.exists():
        PACKAGE_OUTPUT.unlink()

    files = [
        path
        for path in sorted(MERGED_MODEL_DIR.rglob("*"))
        if path.is_file() and "__pycache__" not in path.parts
    ]
    total_mb = sum(path.stat().st_size for path in files) / 1024 / 1024
    print(f"  files={len(files)}, size={total_mb:.1f} MB", flush=True)

    # Model weight files are already compressed-like; ZIP_STORED avoids a long
    # no-output deflate step on Windows.
    with zipfile.ZipFile(PACKAGE_OUTPUT, "w", zipfile.ZIP_STORED) as zf:
        for index, path in enumerate(files, 1):
            rel = path.relative_to(MERGED_MODEL_DIR)
            size_mb = path.stat().st_size / 1024 / 1024
            print(f"  [{index}/{len(files)}] add {rel} ({size_mb:.1f} MB)", flush=True)
            zf.write(path, rel)
    return PACKAGE_OUTPUT


def try_convert_on_windows(outtype: str) -> Path | None:
    converter = shutil.which("convert_hf_to_gguf.py")
    if converter is None:
        print("convert_hf_to_gguf.py not found in PATH; skipping Windows conversion.")
        return None

    out = MODEL_DIR / f"bangumi-parser-{outtype}.gguf"
    cmd = [
        sys.executable,
        converter,
        str(MERGED_MODEL_DIR),
        "--outfile",
        str(out),
        "--outtype",
        outtype,
    ]
    subprocess.run(cmd, check=True)
    return out


def print_next_steps(package_path: Path, modelfile_path: Path, deploy_path: Path) -> None:
    print("\nFiles created:")
    if package_path:
        print(f"  HF zip:        {package_path}")
    print(f"  Modelfile:     {modelfile_path}")
    print(f"  Ubuntu script: {deploy_path}")
    print("\nWindows GGUF conversion:")
    print("  .\\convert_windows_gguf.bat")
    print("  .\\test_ollama_windows.bat")
    print("\nUbuntu with Ollama only:")
    print("  Upload models/bangumi-parser-Q4_K_M.gguf and models/Modelfile")
    print("  cd ~/bangumi-parser")
    print("  ollama create bangumi-parser -f Modelfile")
    print("\nOptional Ubuntu conversion from HF zip:")
    print("  mkdir -p ~/bangumi-parser")
    if package_path:
        print(f"  scp {package_path.name} deploy_ubuntu.sh user@server:~/bangumi-parser/")
    print("  ssh user@server")
    print("  cd ~/bangumi-parser")
    print("  chmod +x deploy_ubuntu.sh")
    print("  ./deploy_ubuntu.sh bangumi-parser")
    print("\nThe final Ollama model name will be: bangumi-parser")


def main() -> None:
    parser = argparse.ArgumentParser(description="Prepare Qwen fine-tuned model for Ollama")
    parser.add_argument("--package-hf", action="store_true", help="also create bangumi-parser-hf.zip")
    parser.add_argument("--convert-windows", action="store_true", help="try direct GGUF conversion on Windows")
    parser.add_argument("--outtype", default="f16", help="GGUF outtype for --convert-windows")
    args = parser.parse_args()

    print("Checking merged Hugging Face model...", flush=True)
    check_merged_model()
    modelfile = generate_modelfile()
    deploy = generate_deploy_script()
    package = package_model() if args.package_hf else None
    if not args.package_hf:
        print("[3/3] Skipping HF zip package. Use --package-hf only if Ubuntu will convert HF -> GGUF.", flush=True)

    if args.convert_windows:
        gguf = try_convert_on_windows(args.outtype)
        if gguf:
            print(f"Windows GGUF created: {gguf}")

    print_next_steps(package, modelfile, deploy)


if __name__ == "__main__":
    main()
