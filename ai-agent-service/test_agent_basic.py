"""
最小化测试：验证VideoAnalyzerAgent初始化和DeepSeek API调用
"""

import os
import sys

# 设置环境变量
os.environ["DEEPSEEK_API_KEY"] = "your-deepseek-api-key"

# 测试视频路径（当前目录）
test_video = "test_videos/compliant_150s.mp4"

if not os.path.exists(test_video):
    print(f"[ERROR] 测试视频不存在：{test_video}")
    sys.exit(1)

print("="*80)
print("VideoAnalyzerAgent最小化测试")
print("="*80)

# 步骤1：测试FFmpegTool独立导入
print("\n[步骤1] 测试FFmpegTool导入...")
try:
    from app.tools.ffmpeg_tool import FFmpegTool
    tool = FFmpegTool(ffmpeg_path=r"E:\ffmpeg-8.1-essentials_build\ffmpeg-8.1-essentials_build\bin")
    print("[OK] FFmpegTool初始化成功")
    print(f"  - ffprobe路径：{tool._get_ffprobe_exe()}")
except Exception as e:
    print(f"[ERROR] FFmpegTool初始化失败：{e}")
    sys.exit(1)

# 步骤2：测试FFmpegTool元数据提取
print("\n[步骤2] 测试FFmpegTool元数据提取...")
try:
    result = tool._run(test_video)
    if result["success"]:
        print("[OK] 元数据提取成功")
        print(f"  - 时长：{result['metadata']['duration_seconds']}秒")
        print(f"  - 分辨率：{result['metadata']['width']}x{result['metadata']['height']}")
        print(f"  - 合规性：{'全部合规' if result['compliance_check']['all_valid'] else '部分不合规'}")
    else:
        print(f"[ERROR] 元数据提取失败：{result.get('error')}")
        sys.exit(1)
except Exception as e:
    print(f"[ERROR] 元数据提取异常：{e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)

print("\n" + "="*80)
print("[SUCCESS] 基础功能验证通过")
print("="*80)
print("\n下一步：集成DeepSeek LLM评审推理")