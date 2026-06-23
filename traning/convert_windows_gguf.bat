@echo off
setlocal

cd /d "%~dp0"

set PYTHON=.venv\Scripts\python.exe
set PIP_INDEX=https://pypi.tuna.tsinghua.edu.cn/simple
set LLAMA_CPP_REPO=%LLAMA_CPP_REPO%
if "%LLAMA_CPP_REPO%"=="" set LLAMA_CPP_REPO=https://github.com/ggerganov/llama.cpp.git

set MERGED=models\merged
set F16_GGUF=models\bangumi-parser-f16.gguf
set Q4_GGUF=models\bangumi-parser-Q4_K_M.gguf
set CMAKE_GENERATOR=Visual Studio 17 2022
set CMAKE_ARCH=x64

if not exist "%PYTHON%" (
  echo Python venv not found: %PYTHON%
  echo Run install_windows.bat first.
  exit /b 1
)

if not exist "%MERGED%\config.json" (
  echo Merged model not found: %MERGED%
  echo Run training first: %PYTHON% train.py
  exit /b 1
)

where git >nul 2>nul
if errorlevel 1 (
  echo git command not found.
  echo Install Git for Windows, then restart PowerShell.
  exit /b 1
)

where cmake >nul 2>nul
if errorlevel 1 (
  echo cmake command not found.
  echo Install CMake first, then restart PowerShell:
  echo   winget install Kitware.CMake
  echo.
  echo If the build step later cannot find a C++ compiler, install Visual Studio Build Tools:
  echo   winget install Microsoft.VisualStudio.2022.BuildTools --override "--wait --passive --add Microsoft.VisualStudio.Workload.VCTools --includeRecommended"
  exit /b 1
)

echo [1/5] Installing converter dependencies...
"%PYTHON%" -m pip install -U numpy sentencepiece safetensors transformers protobuf -i %PIP_INDEX%
if errorlevel 1 goto failed

echo [2/5] Getting llama.cpp...
if not exist "llama.cpp" (
  git clone "%LLAMA_CPP_REPO%" llama.cpp
  if errorlevel 1 goto failed
)

echo [3/5] Building llama.cpp...
cmake -S llama.cpp -B llama.cpp\build -G "%CMAKE_GENERATOR%" -A %CMAKE_ARCH% -DLLAMA_NATIVE=ON
if errorlevel 1 goto failed
cmake --build llama.cpp\build --config Release
if errorlevel 1 goto failed

echo [4/5] Converting Hugging Face model to f16 GGUF...
"%PYTHON%" llama.cpp\convert_hf_to_gguf.py "%MERGED%" --outfile "%F16_GGUF%" --outtype f16 --no-mtp
if errorlevel 1 goto failed

echo [5/5] Quantizing to Q4_K_M...
set QUANT=
for %%F in (
  "llama.cpp\build\bin\Release\llama-quantize.exe"
  "llama.cpp\build\bin\llama-quantize.exe"
  "llama.cpp\build\bin\Release\quantize.exe"
  "llama.cpp\build\bin\quantize.exe"
) do (
  if exist %%~F set QUANT=%%~F
)

if "%QUANT%"=="" (
  echo Quantize executable not found under llama.cpp\build\bin
  exit /b 1
)

"%QUANT%" "%F16_GGUF%" "%Q4_GGUF%" Q4_K_M
if errorlevel 1 goto failed

echo.
echo GGUF created: %Q4_GGUF%
echo Next: test_ollama_windows.bat
exit /b 0

:failed
echo.
echo GGUF conversion failed. Check the error above.
exit /b 1
