@echo off
setlocal

cd /d "%~dp0"

set PYTHON=.venv\Scripts\python.exe
set PIP_INDEX=https://pypi.tuna.tsinghua.edu.cn/simple
set TORCH_INDEX=https://download.pytorch.org/whl/cu121
set TORCH_VERSION=torch==2.5.1+cu121
set PIP_COMMON=--no-cache-dir --retries 10 --timeout 120

echo [1/4] Creating Python virtual environment...
if not exist "%PYTHON%" (
  python -m venv .venv
)

echo [2/4] Upgrading pip from Tsinghua mirror...
"%PYTHON%" -m pip install -U pip -i %PIP_INDEX% %PIP_COMMON%
if errorlevel 1 goto failed

echo [3/4] Installing CUDA PyTorch for RTX 3060...
"%PYTHON%" -m pip cache purge
if not "%TORCH_WHL%"=="" (
  echo Installing PyTorch from local wheel: %TORCH_WHL%
  "%PYTHON%" -m pip install "%TORCH_WHL%" %PIP_COMMON%
) else (
  echo Installing PyTorch from: %TORCH_INDEX%
  echo If this download is unstable, download the wheel manually and run:
  echo   $env:TORCH_WHL="D:\path\to\torch-2.5.1+cu121-cp311-cp311-win_amd64.whl"
  echo   .\install_windows.bat
  "%PYTHON%" -m pip install %TORCH_VERSION% --index-url %TORCH_INDEX% %PIP_COMMON%
)
if errorlevel 1 goto failed

echo [4/4] Installing training dependencies from Tsinghua mirror...
"%PYTHON%" -m pip install -r requirements.txt %PIP_COMMON%
if errorlevel 1 goto failed

echo.
echo Install finished.
echo Next commands:
echo   .venv\Scripts\python.exe extract_and_label.py
echo   .venv\Scripts\python.exe download_model.py
echo   .venv\Scripts\python.exe train.py
echo   .venv\Scripts\python.exe export_to_gguf.py
exit /b 0

:failed
echo.
echo Install failed. Check the error above.
exit /b 1
