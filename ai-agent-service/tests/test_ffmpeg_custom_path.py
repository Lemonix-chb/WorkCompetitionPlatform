# FFmpegTool测试脚本（使用完整路径）
# 测试视频元数据提取和合规性检查功能

import sys
import os
import json
import subprocess
import io

# 设置UTF-8编码输出（避免Windows GBK编码问题）
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

# FFmpeg完整路径配置
FFMPEG_PATH = r"E:\ffmpeg-8.1-essentials_build\ffmpeg-8.1-essentials_build\bin"
FFPROBE_EXE = os.path.join(FFMPEG_PATH, "ffprobe.exe")
FFMPEG_EXE = os.path.join(FFMPEG_PATH, "ffmpeg.exe")

print("="*80)
print("FFmpegTool 测试脚本（自定义路径版）")
print("="*80)
print(f"\nFFmpeg路径：{FFMPEG_PATH}")

# 验证FFmpeg安装
try:
    result = subprocess.run(
        [FFMPEG_EXE, "-version"],
        capture_output=True,
        text=True,
        timeout=5
    )
    if result.returncode == 0:
        print("[OK] FFmpeg安装验证成功")
        print(f"版本：{result.stdout.split('\\n')[0]}")
    else:
        print("[ERROR] FFmpeg安装验证失败")
        sys.exit(1)
except Exception as e:
    print(f"[ERROR] 无法找到FFmpeg：{e}")
    sys.exit(1)

print("\n" + "="*80)

def extract_metadata(video_path: str) -> dict:
    """提取视频元数据"""
    print(f"\n正在分析视频：{video_path}")

    try:
        # 使用ffprobe提取元数据
        cmd = [
            FFPROBE_EXE,
            "-v", "quiet",
            "-print_format", "json",
            "-show_format",
            "-show_streams",
            video_path
        ]

        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            check=True,
            timeout=10
        )

        data = json.loads(result.stdout)

        # 解析关键元数据
        format_info = data.get("format", {})
        streams = data.get("streams", [])

        # 找到视频流
        video_stream = None
        audio_stream = None
        for stream in streams:
            if stream.get("codec_type") == "video":
                video_stream = stream
            elif stream.get("codec_type") == "audio":
                audio_stream = stream

        metadata = {
            "file_path": video_path,
            "duration_seconds": round(float(format_info.get("duration", 0)), 2),
            "width": video_stream.get("width", 0) if video_stream else 0,
            "height": video_stream.get("height", 0) if video_stream else 0,
            "codec": video_stream.get("codec_name", "") if video_stream else "",
            "format": format_info.get("format_name", ""),
            "file_size_mb": round(float(format_info.get("size", 0)) / (1024 * 1024), 2)
        }

        # 计算画面比例
        if metadata["width"] and metadata["height"]:
            from math import gcd
            divisor = gcd(metadata["width"], metadata["height"])
            w = metadata["width"] // divisor
            h = metadata["height"] // divisor
            metadata["ratio_simplified"] = f"{w}:{h}"

        return metadata

    except subprocess.CalledProcessError as e:
        print(f"[ERROR] ffprobe执行失败：{e.stderr}")
        return {}
    except json.JSONDecodeError as e:
        print(f"[ERROR] JSON解析失败：{e}")
        return {}
    except Exception as e:
        print(f"[ERROR] 错误：{e}")
        return {}

def check_compliance(metadata: dict) -> dict:
    """检查官方硬性要求合规性"""
    compliance = {}

    # 1. 时长检查：60-180秒
    duration = metadata.get("duration_seconds", 0)
    compliance["duration_valid"] = 60 <= duration <= 180
    compliance["duration_message"] = (
        f"时长{duration}秒，符合要求（60-180秒） [OK]"
        if compliance["duration_valid"]
        else f"时长{duration}秒，不符合要求（应在60-180秒范围） [ERROR]"
    )

    # 2. 画面比例检查：16:9
    ratio = metadata.get("ratio_simplified", "")
    compliance["ratio_valid"] = ratio == "16:9"
    compliance["ratio_message"] = (
        f"画面比例{ratio}，符合要求（16:9） [OK]"
        if compliance["ratio_valid"]
        else f"画面比例{ratio}，不符合要求（应为16:9） [ERROR]"
    )

    # 3. 分辨率检查：1080p
    width = metadata.get("width", 0)
    height = metadata.get("height", 0)
    compliance["resolution_valid"] = width >= 1920 and height >= 1080
    compliance["resolution_message"] = (
        f"分辨率{width}x{height}，符合要求（1080p或更高） [OK]"
        if compliance["resolution_valid"]
        else f"分辨率{width}x{height}，不符合要求（应≥1920x1080） [ERROR]"
    )

    # 4. 格式检查：MP4
    format_name = metadata.get("format", "")
    compliance["format_valid"] = "mp4" in format_name.lower()
    compliance["format_message"] = (
        f"格式{format_name}，符合要求（MP4） [OK]"
        if compliance["format_valid"]
        else f"格式{format_name}，不符合要求（应为MP4） [ERROR]"
    )

    # 5. 文件大小检查：≤300MB
    file_size_mb = metadata.get("file_size_mb", 0)
    compliance["size_valid"] = file_size_mb <= 300
    compliance["size_message"] = (
        f"文件大小{file_size_mb}MB，符合要求（≤300MB） [OK]"
        if compliance["size_valid"]
        else f"文件大小{file_size_mb}MB，不符合要求（应≤300MB） [ERROR]"
    )

    # 6. 总体合规性
    compliance["all_valid"] = all([
        compliance["duration_valid"],
        compliance["ratio_valid"],
        compliance["resolution_valid"],
        compliance["format_valid"],
        compliance["size_valid"]
    ])

    compliance["overall_message"] = (
        "[OK] 视频符合所有官方硬性要求！"
        if compliance["all_valid"]
        else "[ERROR] 视频不符合部分官方要求，详见上述检查项"
    )

    return compliance

def test_video(video_path: str):
    """完整测试流程"""
    print("-"*80)
    print(f"测试文件：{video_path}")
    print("-"*80)

    if not os.path.exists(video_path):
        print("[ERROR] 文件不存在，跳过测试")
        return

    # 提取元数据
    metadata = extract_metadata(video_path)

    if not metadata:
        print("[ERROR] 元数据提取失败")
        return

    print("\n视频元数据：")
    print(json.dumps(metadata, indent=2, ensure_ascii=False))

    # 检查合规性
    compliance = check_compliance(metadata)

    print("\n合规性检查：")
    print("-"*80)
    for key, value in compliance.items():
        if key.endswith("_message"):
            print(f"  {value}")
    print("-"*80)

    # 总结
    print("\n总体结论：")
    print(compliance["overall_message"])

if __name__ == "__main__":
    print("\n提示：如需测试视频文件，请提供视频路径作为参数")
    print("示例：python test_ffmpeg_custom_path.py <视频文件路径>")

    if len(sys.argv) >= 2:
        test_video(sys.argv[1])
    else:
        print("\n未提供测试视频，仅验证FFmpeg安装")
        print("[OK] 测试完成：FFmpeg已正确安装并可用")

print("\n" + "="*80)
print("下一步：请将FFmpeg添加到系统PATH以简化调用")
print("  1. 打开环境变量设置")
print("  2. 在系统变量中找到'Path'")
print("  3. 添加路径：E:\\ffmpeg-8.1-essentials_build\\ffmpeg-8.1-essentials_build\\bin")
print("="*80)