#!/usr/bin/env bash
set -euo pipefail

MODEL_NAME="${1:-bangumi-parser}"
cd "$(dirname "$0")"

if ! command -v ollama >/dev/null 2>&1; then
  echo "ollama command not found. Install Ollama first."
  exit 1
fi

ollama create "$MODEL_NAME" -f Modelfile
echo "Created Ollama model: $MODEL_NAME"
echo "Test:"
echo "  ollama run $MODEL_NAME --think false --format json '璇疯В鏋愪互涓嬬暘鍓ф爣棰橈細 [ANi] Test Anime - 01 [1080P][Baha][CHT]'"