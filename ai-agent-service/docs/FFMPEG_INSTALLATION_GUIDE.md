# FFmpeg安装指南（Windows）

## 为什么需要FFmpeg？

FFmpeg是视频审核的核心工具，用于：
- ✅ 提取视频元数据（时长、分辨率、编码）
- ✅ 检查官方硬性要求合规性
- ✅ 提取关键帧截图
- ✅ 分离音频轨道（用于Whisper转录）

---

## 安装步骤

### **方法1：下载预编译版本（推荐）**

**步骤1：下载FFmpeg**

访问官方网站：
- 🔗 **官方下载**：https://www.gyan.dev/ffmpeg/builds/
- 🔗 **备选下载**：https://github.com/BtbN/FFmpeg-Builds/releases

选择版本：
- **ffmpeg-release-essentials.7z**（推荐，体积小）
- 或 **ffmpeg-release-full.7z**（包含所有工具）

**步骤2：解压文件**

1. 下载7-Zip：https://www.7-zip.org/download.html
2. 右键点击下载的.7z文件 → "7-Zip" → "提取到当前文件夹"
3. 得到文件夹：`ffmpeg-xxxx-essentials_build`

**步骤3：移动到固定位置**

将解压的文件夹移动到：
```
C:\ffmpeg\
```

最终目录结构：
```
C:\ffmpeg\
  ├── bin\
  │   ├── ffmpeg.exe
  │   ├── ffplay.exe
  │   └── ffprobe.exe
  ├── doc\
  └── presets\
```

**步骤4：添加到系统PATH**

1. 按 `Win + X` → 选择"系统"
2. 点击"高级系统设置"
3. 点击"环境变量"
4. 在"系统变量"区域找到"Path"
5. 点击"编辑"
6. 点击"新建"
7. 输入：`C:\ffmpeg\bin`
8. 点击"确定"保存所有窗口

**步骤5：验证安装**

打开新的命令行窗口（必须新窗口）：
```bash
ffmpeg -version
```

预期输出：
```
ffmpeg version 6.0-essentials_build ...
...
configuration: --enable-gpl ...
```

如果看到版本信息，说明安装成功！

---

### **方法2：使用Chocolatey包管理器**

如果你已安装Chocolatey：

```powershell
choco install ffmpeg
```

验证：
```bash
ffmpeg -version
```

---

### **方法3：使用Conda**

如果你使用Anaconda/Miniconda：

```bash
conda install -c conda-forge ffmpeg
```

---

## 验证安装

### **测试脚本验证**

运行测试脚本：
```bash
cd E:\JavaProject\WorkCompetitionPlatform\ai-agent-service
python tests\test_ffmpeg_tool.py test_video.mp4
```

预期输出：
```
✅ FFmpeg已安装
版本信息：ffmpeg version 6.0...

FFmpegTool 测试脚本
================================================================================

测试文件：test_video.mp4

正在执行元数据提取...

✅ 测试成功！

--------------------------------------------------------------------------------
视频元数据：
--------------------------------------------------------------------------------
{
  "duration_seconds": 150,
  "duration_formatted": "2:30",
  "width": 1920,
  "height": 1080,
  ...
}

--------------------------------------------------------------------------------
合规性检查：
--------------------------------------------------------------------------------
{
  "duration_valid": true,
  "ratio_valid": true,
  ...
}
```

---

## 测试数据准备

### **创建测试视频**

**方法1：使用现有视频**

找任意一个MP4视频文件：
- 下载短视频（YouTube、Bilibili等）
- 使用手机录制测试视频
- 从项目uploads目录找现有作品

**方法2：用FFmpeg创建测试视频**

```bash
# 创建60秒测试视频（纯色背景+音频）
ffmpeg -f lavfi -i color=c=blue:s=1920x1080:d=60 \
       -f lavfi -i sine=frequency=1000:duration=60 \
       -c:v libx264 -c:a aac \
       test_video_60s.mp4

# 创建150秒测试视频（符合官方要求）
ffmpeg -f lavfi -i color=c=green:s=1920x1080:d=150 \
       -f lavfi -i sine=frequency=800:duration=150 \
       -c:v libx264 -c:a aac \
       test_video_150s.mp4

# 创建200秒测试视频（超出时长限制，用于测试失败场景）
ffmpeg -f lavfi -i color=c=red:s=1920x1080:d=200 \
       -f lavfi -i sine=frequency=600:duration=200 \
       -c:v libx264 -c:a aac \
       test_video_200s_invalid.mp4
```

---

## 功能验证清单

### **测试场景1：合规视频**

```bash
python tests\test_ffmpeg_tool.py test_video_150s.mp4
```

预期检查结果：
- ✅ duration_valid: true（60-180秒）
- ✅ ratio_valid: true（16:9）
- ✅ resolution_valid: true（1080p）
- ✅ format_valid: true（MP4）
- ✅ size_valid: true（≤300MB）
- ✅ all_valid: true

---

### **测试场景2：超长视频**

```bash
python tests\test_ffmpeg_tool.py test_video_200s_invalid.mp4
```

预期检查结果：
- ❌ duration_valid: false（200秒 > 180秒）
- ✅ ratio_valid: true
- ✅ resolution_valid: true
- ✅ format_valid: true
- ✅ size_valid: true
- ❌ all_valid: false

---

### **测试场景3：低分辨率视频**

创建720p视频：
```bash
ffmpeg -f lavfi -i color=c=yellow:s=1280x720:d=100 \
       -c:v libx264 test_video_720p.mp4
```

测试：
```bash
python tests\test_ffmpeg_tool.py test_video_720p.mp4
```

预期：
- ✅ duration_valid: true
- ✅ ratio_valid: true（1280x720 = 16:9）
- ❌ resolution_valid: false（720p < 1080p）
- ❌ all_valid: false

---

## 常见问题

### **问题1：找不到ffmpeg命令**

错误：
```
'ffmpeg' 不是内部或外部命令
```

解决：
1. 确认已添加到PATH：`C:\ffmpeg\bin`
2. **重启命令行窗口**（必须新窗口）
3. 系统环境变量需要生效

---

### **问题2：Python调用失败**

错误：
```python
FileNotFoundError: ffprobe not found
```

解决：
1. 确认ffprobe.exe存在于 `C:\ffmpeg\bin\`
2. 测试：`ffprobe -version`
3. Python脚本使用subprocess调用，需要系统PATH正确

---

### **问题3：权限问题**

错误：
```
PermissionError: [WinError 5] 拒绝访问
```

解决：
1. 不要将FFmpeg放在需要管理员权限的目录
2. 推荐位置：`C:\ffmpeg`（普通用户可访问）
3. 避免放在 `C:\Program Files\`

---

## 性能优化建议

### **1. 使用硬件加速**

如果你的CPU支持：
```bash
# 使用Intel Quick Sync
ffmpeg -hwaccel qsv ...

# 使用NVIDIA NVENC
ffmpeg -hwaccel cuda ...
```

### **2. 批量处理优化**

对于多个视频文件：
```python
import concurrent.futures

def batch_process(video_paths):
    with concurrent.futures.ThreadPoolExecutor(max_workers=4) as executor:
        results = executor.map(lambda p: ffmpeg_tool._run(p), video_paths)
    return list(results)
```

---

## 安装验证完成后的下一步

✅ **FFmpeg安装成功后**：
1. 运行 `python tests\test_ffmpeg_tool.py test_video.mp4`
2. 确认元数据提取正确
3. 确认合规性检查逻辑正确
4. 继续开发其他视频工具（Whisper、OCR等）

---

**安装完成后请告知，我将继续测试FFmpegTool功能！** 🎬