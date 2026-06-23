@echo off
setlocal EnableExtensions

cd /d "%~dp0"

set "MODEL_NAME=bangumi-parser"
set "MODEL_DIR=models"
set "GGUF=%MODEL_DIR%\bangumi-parser-Q4_K_M.gguf"
set "MODELFILE=%MODEL_DIR%\Modelfile"
set "DEPLOY=%MODEL_DIR%\deploy_ollama_only.sh"
set "PACKAGE=%MODEL_DIR%\bangumi-parser-ollama.zip"

if not exist "%GGUF%" goto missing_gguf
if not exist "%MODELFILE%" goto missing_modelfile

echo Writing Ubuntu Ollama-only deploy script...
> "%DEPLOY%" echo #!/usr/bin/env bash
>> "%DEPLOY%" echo set -euo pipefail
>> "%DEPLOY%" echo MODEL_NAME="${1:-bangumi-parser}"
>> "%DEPLOY%" echo cd "$(dirname "$0")"
>> "%DEPLOY%" echo if ! command -v ollama ^>/dev/null 2^>^&1; then
>> "%DEPLOY%" echo   echo "ollama command not found. Install Ollama first."
>> "%DEPLOY%" echo   exit 1
>> "%DEPLOY%" echo fi
>> "%DEPLOY%" echo ollama create "$MODEL_NAME" -f Modelfile
>> "%DEPLOY%" echo echo "Created Ollama model: $MODEL_NAME"
>> "%DEPLOY%" echo echo "Test:"
>> "%DEPLOY%" echo echo "  ollama run $MODEL_NAME --think false --format json '请解析以下番剧标题： [ANi] Test Anime - 01 [1080P][Baha][CHT]'"

if exist "%PACKAGE%" del /f /q "%PACKAGE%"

echo Packaging %PACKAGE% ...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "Compress-Archive -LiteralPath '%GGUF%','%MODELFILE%','%DEPLOY%' -DestinationPath '%PACKAGE%' -Force"
if errorlevel 1 goto failed

echo.
echo Package created: %PACKAGE%
echo Upload this file to your Ubuntu server, then run:
echo   unzip bangumi-parser-ollama.zip -d bangumi-parser
echo   cd bangumi-parser
echo   chmod +x deploy_ollama_only.sh
echo   ./deploy_ollama_only.sh bangumi-parser
exit /b 0

:missing_gguf
echo GGUF file not found: %GGUF%
echo Run convert_windows_gguf.bat first.
exit /b 1

:missing_modelfile
echo Modelfile not found: %MODELFILE%
echo Run: .venv\Scripts\python.exe export_to_gguf.py
exit /b 1

:failed
echo.
echo Packaging failed.
exit /b 1
