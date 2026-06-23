$ErrorActionPreference = "Stop"

$BaseDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ModelDir = Join-Path $BaseDir "models"
$Gguf = Join-Path $ModelDir "bangumi-parser-Q4_K_M.gguf"
$Modelfile = Join-Path $ModelDir "Modelfile"
$Deploy = Join-Path $BaseDir "deploy_ollama_only.sh"
$Package = Join-Path $BaseDir "bangumi-parser-ollama.zip"

if (!(Test-Path -LiteralPath $Gguf)) {
    throw "GGUF file not found: $Gguf. Run convert_windows_gguf.bat first."
}

if (!(Test-Path -LiteralPath $Modelfile)) {
    throw "Modelfile not found: $Modelfile. Run export_to_gguf.py first."
}

$deployContent = @'
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
echo "  ollama run $MODEL_NAME --think false --format json '请解析以下番剧标题： [ANi] Test Anime - 01 [1080P][Baha][CHT]'"
'@

[System.IO.File]::WriteAllText($Deploy, $deployContent, [System.Text.UTF8Encoding]::new($false))

if (Test-Path -LiteralPath $Package) {
    Remove-Item -LiteralPath $Package -Force
}

Write-Host "Packaging $Package ..."
Compress-Archive -LiteralPath $Gguf, $Modelfile, $Deploy -DestinationPath $Package -Force

Write-Host ""
Write-Host "Package created: $Package"
Write-Host ""
Write-Host "Upload it to Ubuntu, then run:"
Write-Host "  unzip bangumi-parser-ollama.zip -d bangumi-parser"
Write-Host "  cd bangumi-parser"
Write-Host "  chmod +x deploy_ollama_only.sh"
Write-Host "  ./deploy_ollama_only.sh bangumi-parser"
