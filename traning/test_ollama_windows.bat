@echo off
setlocal EnableExtensions

cd /d "%~dp0"

set "MODEL_NAME=bangumi-parser"
set "MODEL_DIR=models"
set "GGUF=%MODEL_DIR%\bangumi-parser-Q4_K_M.gguf"
set "MODELFILE=%MODEL_DIR%\Modelfile"

if not exist "%GGUF%" goto missing_gguf
if not exist "%MODELFILE%" goto missing_modelfile

where ollama >nul 2>nul
if errorlevel 1 goto missing_ollama

echo [1/2] Creating Ollama model: %MODEL_NAME%
pushd "%MODEL_DIR%"
ollama create "%MODEL_NAME%" -f Modelfile
set "CREATE_CODE=%ERRORLEVEL%"
popd
if not "%CREATE_CODE%"=="0" exit /b %CREATE_CODE%

echo.
echo [2/2] Running test prompt...
ollama run "%MODEL_NAME%" --think false --format json "Parse this bangumi title as JSON only: [ANi] Test Anime - 01 [1080P][Baha][WEB-DL][AAC AVC][CHT][MP4]"

echo.
echo Done. You can run:
echo   ollama run %MODEL_NAME% --think false --format json "Parse this bangumi title as JSON only: [ANi] Test Anime - 01 [1080P][Baha][CHT]"
exit /b 0

:missing_gguf
echo GGUF file not found: %GGUF%
echo Run convert_windows_gguf.bat first.
exit /b 1

:missing_modelfile
echo Modelfile not found: %MODELFILE%
echo Run: .venv\Scripts\python.exe export_to_gguf.py
exit /b 1

:missing_ollama
echo ollama command not found.
echo Install Ollama for Windows and restart PowerShell.
exit /b 1
