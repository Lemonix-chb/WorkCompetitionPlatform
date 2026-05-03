# FFmpegTool 测试指南

## 快速测试（推荐）

**一键测试脚本**：
```bash
cd E:\JavaProject\WorkCompetitionPlatform\ai-agent-service
run_ffmpeg_tests.bat
```

自动完成：
1. ✅ 检查FFmpeg安装
2. ✅ 生成8个测试视频（不同场景）
3. ✅ 运行合规性、超长、低分辨率测试
4. ✅ 显示详细测试报告

---

## 手动测试步骤

### **步骤1：安装FFmpeg**

参考详细指南：`docs/FFMPEG_INSTALLATION_GUIDE.md`

快速安装：
1. 下载：https://www.gyan.dev/ffmpeg/builds/
2. 选择 `ffmpeg-release-essentials.7z`
3. 解压到 `C:\ffmpeg`
4. 添加到PATH：`C:\ffmpeg\bin`
5. 重启命令行窗口

验证：
```bash
ffmpeg -version
```

---

### **步骤2：生成测试数据**

创建测试视频文件：
```bash
cd tests
python generate_test_videos.py
```

生成文件：
- ✅ `test_videos/compliant_150s.mp4`（合规视频）
- ✅ `test_videos/compliant_60s.mp4`（最小时长）
- ✅ `test_videos/compliant_180s.mp4`（最大时长）
- ✅ `test_videos/invalid_duration_200s.mp4`（超长）
- ✅ `test_videos/invalid_duration_50s.mp4`（过短）
- ✅ `test_videos/invalid_resolution_720p.mp4`（720p）
- ✅ `test_videos/invalid_ratio_4_3.mp4`（4:3比例）
- ✅ `test_videos/invalid_both_480p.mp4`（多项不合规）

---

### **步骤3：运行测试**

**测试合规视频**：
```bash
python tests\test_ffmpeg_tool.py test_videos\compliant_150s.mp4
```

预期输出：
```
✅ FFmpeg已安装
✅ 测试成功！

视频元数据：
{
  "duration_seconds": 150,
  "duration_formatted": "2:30",
  "width": 1920,
  "height": 1080,
  "ratio_simplified": "16:9",
  ...
}

合规性检查：
{
  "duration_valid": true,  ✅
  "ratio_valid": true,     ✅
  "resolution_valid": true, ✅
  "format_valid": true,    ✅
  "size_valid": true,      ✅
  "all_valid": true        ✅
}
```

---

**测试超长视频**：
```bash
python tests\test_ffmpeg_tool.py test_videos\invalid_duration_200s.mp4
```

预期：
```
合规性检查：
{
  "duration_valid": false, ❌（200秒 > 180秒）
  "duration_message": "时长200秒，不符合要求（应在60-180秒范围）",
  ...
  "all_valid": false       ❌
}
```

---

**测试低分辨率**：
```bash
python tests\test_ffmpeg_tool.py test_videos\invalid_resolution_720p.mp4
```

预期：
```
合规性检查：
{
  "resolution_valid": false, ❌（720p < 1080p）
  "resolution_message": "分辨率1280x720，不符合要求（应≥1920x1080）",
  "ratio_valid": true,       ✅（16:9比例）
  ...
  "all_valid": false         ❌
}
```

---

## 测试覆盖场景

| 测试视频 | 时长 | 分辨率 | 比例 | 预期结果 |
|---------|------|--------|------|---------|
| compliant_150s.mp4 | 150s | 1080p | 16:9 | ✅ 全部合规 |
| compliant_60s.mp4 | 60s | 1080p | 16:9 | ✅ 全部合规（边界） |
| compliant_180s.mp4 | 180s | 1080p | 16:9 | ✅ 全部合规（边界） |
| invalid_duration_200s.mp4 | 200s | 1080p | 16:9 | ❌ 超时长 |
| invalid_duration_50s.mp4 | 50s | 1080p | 16:9 | ❌ 过短 |
| invalid_resolution_720p.mp4 | 120s | 720p | 16:9 | ❌ 低分辨率 |
| invalid_ratio_4_3.mp4 | 120s | 800x600 | 4:3 | ❌ 错误比例 |
| invalid_both_480p.mp4 | 120s | 480p | 4:3 | ❌ 多项不合规 |

---

## 验证检查项

### **成功标准**

FFmpegTool测试通过应满足：
- ✅ 元数据提取准确（时长、分辨率、编码等）
- ✅ 合规性判断正确（所有检查项）
- ✅ 错误提示清晰（不符合时显示原因）
- ✅ 边界情况处理正确（60s/180s边界值）

### **检查要点**

**时长检查**：
- 60s → ✅ valid（最小时长）
- 150s → ✅ valid（中间值）
- 180s → ✅ valid（最大时长）
- 50s → ❌ invalid（低于60秒）
- 200s → ❌ invalid（超过180秒）

**分辨率检查**：
- 1920x1080 → ✅ valid（1080p）
- 1280x720 → ❌ invalid（720p）
- 640x480 → ❌ invalid（480p）

**比例检查**：
- 1920x1080 → ✅ 16:9
- 1280x720 → ✅ 16:9
- 800x600 → ❌ 4:3
- 640x480 → ❌ 4:3

---

## 测试完成后

✅ **测试成功后，继续开发**：
1. 实现其他视频工具：
   - KeyFrameExtractorTool（关键帧提取）
   - WhisperTool（语音转文字）
   - OCRTool（字幕识别）
2. 完成VideoAnalyzerAgent完整功能
3. 创建OrchestratorAgent主控协调者
4. 集成RAG知识库

---

## 问题排查

### **问题1：FFmpeg未找到**

错误：
```
'ffmpeg' 不是内部或外部命令
```

解决：
1. 确认安装路径：`C:\ffmpeg\bin`
2. 确认添加到PATH
3. **重启命令行窗口**（必须新窗口）

---

### **问题2：测试视频生成失败**

错误：
```
❌ 生成失败：[Errno 2] No such file or directory: 'ffmpeg'
```

解决：
同问题1，确保系统PATH正确

---

### **问题3：元数据提取失败**

错误：
```
❌ 测试失败！错误信息：FFprobe执行失败
```

解决：
1. 检查视频文件是否损坏
2. 尝试手动运行：`ffprobe test_video.mp4`
3. 确认视频格式支持（MP4/AVI/MOV）

---

## 性能指标

**测试时间**：
- 元数据提取：约0.5-1秒（150秒视频）
- 合规性检查：即时（<0.1秒）
- 总体测试：约5秒（包括视频生成）

**资源消耗**：
- CPU：低（ffprobe轻量级）
- 内存：约50-100MB
- 磁盘：测试视频约10-20MB每个

---

**开始测试：运行 `run_ffmpeg_tests.bat`** 🚀