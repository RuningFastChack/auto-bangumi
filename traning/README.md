# Bangumi Parser Training

本目录用于在 Windows 上训练一个番剧标题解析模型，并导出为 Ubuntu Server 上 Ollama 可直接运行的 GGUF 模型。

## 目标环境

- 开发环境：Windows 10，32 GB 内存，NVIDIA GeForce RTX 3060 12 GB，已安装 ModelScope 和 Ollama。
- 生产环境：Ubuntu Server，R7 5600H 核显版，32 GB 内存，只安装 Ollama。
- 基座模型：`Qwen/Qwen3.5-0.8B`
- 数据来源：`../db/app.db` 的 `rss_item.torrent_name`
- 训练方式：Windows 上 QLoRA 微调，合并为 fp16 HF 模型，再转换为 Q4_K_M GGUF。
- Ollama 推理要求：运行时使用 `--think false --format json`，避免输出 `<think>`。

## 目录说明

建议提交到仓库的文件：

```text
traning/
  check_env.py
  download_model.py
  extract_and_label.py
  train.py
  merge_lora_for_gguf.py
  export_to_gguf.py
  convert_windows_gguf.bat
  test_ollama_windows.bat
  package_ollama_server.ps1
  install_windows.bat
  requirements.txt
  README.md
```

可再生成产物，已在 `.gitignore` 中忽略：

```text
traning/.venv/
traning/data/
traning/models/
traning/llama.cpp/
traning/tmp/
traning/*.log
traning/*.zip
```

## 1. 安装 Windows 训练环境

进入目录：

```powershell
cd D:\work\my-porject\auto-bangumi\traning
```

安装依赖：

```powershell
.\install_windows.bat
```

脚本会创建 `.venv`，安装 CUDA 版 PyTorch 和训练依赖。后续命令都建议使用：

```powershell
.\.venv\Scripts\python.exe
```

### 大陆下载说明

- 普通 Python 依赖使用清华 PyPI 源。
- 基座模型优先用 ModelScope 下载，失败后回退到 `https://hf-mirror.com`。
- CUDA 版 PyTorch 的 wheel 较大，如果官方源下载不稳定，可以先手动下载 wheel，再指定本地文件安装：

```powershell
$env:TORCH_WHL="D:\Downloads\torch-2.5.1+cu121-cp311-cp311-win_amd64.whl"
.\install_windows.bat
```

## 2. 生成训练数据

从 `../db/app.db` 的 `rss_item.torrent_name` 生成 JSONL：

```powershell
.\.venv\Scripts\python.exe extract_and_label.py
```

输出：

```text
traning/data/train.jsonl
traning/data/val.jsonl
```

样本格式：

```json
{"input":"[ANi] Example - 01 [1080P][Baha][CHT]","output":{"episode":"1","season":1,"nameEn":"Example","nameJp":"","nameZh":"","sub":"CHT","dpi":"1080","source":"Baha","group":"ANi"}}
```

`train.py` 也会在缺少 `data/train.jsonl` 或 `data/val.jsonl` 时自动生成数据。

## 3. 下载基座模型

```powershell
.\.venv\Scripts\python.exe download_model.py
```

模型保存到：

```text
traning/models/qwen3.5-0.8b
```

## 4. 检查环境

```powershell
.\.venv\Scripts\python.exe check_env.py
```

确认输出里 CUDA 可用，并能看到 RTX 3060。

## 5. 训练模型

推荐先用较快配置训练：

```powershell
.\.venv\Scripts\python.exe train.py --epochs 2 --max-seq-length 256 --batch-size 6 --gradient-accumulation-steps 3 --save-steps 300 --eval-steps 300 --logging-steps 20
```

如果显存不足，退回更稳配置：

```powershell
.\.venv\Scripts\python.exe train.py --epochs 2 --max-seq-length 256 --batch-size 4 --gradient-accumulation-steps 4 --save-steps 300 --eval-steps 300 --logging-steps 20
```

训练完成后会生成：

```text
traning/models/lora_adapter/
traning/models/merged/
```

说明：

- `lora_adapter/` 是真正的训练成果。
- `merged/` 是合并后的 Hugging Face 模型，用于转换 GGUF。
- 因为训练使用 QLoRA/bitsandbytes，不能直接把 4bit/bnb 权重转换 GGUF，必须用 fp16 基座模型重新合并 LoRA。

## 6. 重新合并 LoRA 为 GGUF 可转换模型

如果 `train.py` 已经自动完成合并，可以跳过这步。若转换 GGUF 时遇到 `bitsandbytes` 错误，手动执行：

```powershell
.\.venv\Scripts\python.exe merge_lora_for_gguf.py
```

成功后会重新生成干净的：

```text
traning/models/merged/
```

## 7. 生成 Modelfile

```powershell
.\.venv\Scripts\python.exe export_to_gguf.py
```

默认生成：

```text
traning/models/Modelfile
traning/models/deploy_ubuntu.sh
```

生产服务器只安装 Ollama 时，不需要 `bangumi-parser-hf.zip` 和 `deploy_ubuntu.sh` 这条路线。

## 8. Windows 转换 GGUF

Windows 转换需要：

- Git
- CMake
- Visual Studio 2022 Build Tools，包含 MSVC C++ 工具链

安装命令：

```powershell
winget install Kitware.CMake
winget install Microsoft.VisualStudio.2022.BuildTools --override "--wait --passive --add Microsoft.VisualStudio.Workload.VCTools --includeRecommended"
```

安装后重新打开 PowerShell，再执行：

```powershell
.\convert_windows_gguf.bat
```

脚本会：

1. 安装转换依赖。
2. 下载并编译 `llama.cpp`。
3. 使用 `--no-mtp` 转换 Qwen3.5，避免 Ollama 加载时报 `missing tensor 'blk.24...'`。
4. 量化为 Q4_K_M。

成功后生成：

```text
traning/models/bangumi-parser-Q4_K_M.gguf
```

## 9. Windows 本机 Ollama 测试

```powershell
.\test_ollama_windows.bat
```

或手动测试：

```powershell
ollama rm bangumi-parser
ollama create bangumi-parser -f .\models\Modelfile
ollama run bangumi-parser --think false --format json "请解析以下番剧标题： [ANi] 9nine Rulers Crown / 9-nine- 支配者的王冠 - 11 [1080P][Baha][WEB-DL][AAC AVC][CHT][MP4]"
```

期望输出是纯 JSON，例如：

```json
{"episode":"11","season":1,"nameEn":"9nine Rulers Crown","nameJp":"","nameZh":"9-nine- 支配者的王冠","sub":"CHT","dpi":"1080","source":"Baha","group":"ANi"}
```

如果不加 `--think false --format json`，模型可能输出 `<think>`，这是不符合应用接入要求的。

## 10. 打包给 Ubuntu Server

生产环境只安装 Ollama 时，使用一键打包脚本：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\package_ollama_server.ps1
```

生成：

```text
traning/bangumi-parser-ollama.zip
```

包内包含：

```text
bangumi-parser-Q4_K_M.gguf
Modelfile
deploy_ollama_only.sh
```

## 11. 上传到 Ubuntu Server

从 Windows 上传：

```powershell
scp .\bangumi-parser-ollama.zip 用户名@服务器IP:~/bangumi-parser-ollama.zip
```

服务器上执行：

```bash
unzip bangumi-parser-ollama.zip -d bangumi-parser
cd bangumi-parser
chmod +x deploy_ollama_only.sh
./deploy_ollama_only.sh bangumi-parser
```

测试：

```bash
ollama run bangumi-parser --think false --format json '请解析以下番剧标题： [ANi] 9nine Rulers Crown / 9-nine- 支配者的王冠 - 11 [1080P][Baha][WEB-DL][AAC AVC][CHT][MP4]'
```

## 12. Java 应用接入注意事项

`OllamaParser.java` 请求 Ollama 时应包含：

```json
{
  "stream": false,
  "format": "json",
  "think": false,
  "temperature": 0
}
```

响应解析时建议提取第一个 JSON 对象，并对字符串字段做 `trim()`，防止模型偶尔输出前后空白或换行。

## 常见问题

### PyTorch 下载 hash 不一致

通常是大文件下载中断或缓存损坏。脚本已使用 `--no-cache-dir` 和 `pip cache purge`。仍失败时手动下载 wheel 并设置 `TORCH_WHL`。

### CMake 报 `nmake` 找不到

普通 PowerShell 不是 VS Developer Prompt。脚本已指定：

```text
-G "Visual Studio 17 2022" -A x64
```

如果仍失败，确认 Visual Studio Build Tools 安装完成后重新打开 PowerShell。

### 转换时报 `bitsandbytes`

执行：

```powershell
.\.venv\Scripts\python.exe merge_lora_for_gguf.py
.\convert_windows_gguf.bat
```

### Ollama 报 `missing tensor 'blk.24...'`

重新运行新版 `convert_windows_gguf.bat`，确保转换命令包含 `--no-mtp`。

### 输出 `<think>`

运行 Ollama 时必须加：

```text
--think false --format json
```

Java 请求里也要设置 `think=false` 和 `format=json`。
