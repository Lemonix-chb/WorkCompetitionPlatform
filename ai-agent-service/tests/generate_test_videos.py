# Generate Test Videos for FFmpegTool Testing
# 创建测试视频文件用于验证合规性检查逻辑

import subprocess
import os
import sys

def generate_test_video(output_path: str, duration: int, width: int, height: int, color: str):
    """
    使用FFmpeg生成测试视频

    Args:
        output_path: 输出文件路径
        duration: 视频时长（秒）
        width: 视频宽度
        height: 视频高度
        color: 背景颜色（blue/green/red等）
    """
    print(f"正在生成测试视频：{output_path}")
    print(f"  - 时长：{duration}秒")
    print(f"  - 分辨率：{width}x{height}")
    print(f"  - 背景：{color}")

    cmd = [
        "ffmpeg",
        "-f", "lavfi",
        "-i", f"color=c={color}:s={width}x{height}:d={duration}",
        "-f", "lavfi",
        "-i", f"sine=frequency=1000:duration={duration}",
        "-c:v", "libx264",
        "-c:a", "aac",
        "-y",  # 覆盖已存在文件
        output_path
    ]

    try:
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            timeout=30,
            check=True
        )
        print(f"✅ 成功生成：{output_path}\n")
        return True
    except subprocess.CalledProcessError as e:
        print(f"❌ 生成失败：{e.stderr}\n")
        return False
    except Exception as e:
        print(f"❌ 错误：{e}\n")
        return False

def generate_all_test_videos():
    """生成所有测试场景的视频"""

    print("="*80)
    print("批量生成测试视频")
    print("="*80)
    print("\n确保FFmpeg已安装：ffmpeg -version\n")

    # 创建测试目录
    test_dir = "test_videos"
    os.makedirs(test_dir, exist_ok=True)

    # 测试场景列表
    test_cases = [
        {
            "name": "合规视频（150秒，1080p，16:9）",
            "file": f"{test_dir}/compliant_150s.mp4",
            "duration": 150,
            "width": 1920,
            "height": 1080,
            "color": "green",
            "expected": "all_valid=True"
        },
        {
            "name": "合规视频（60秒，最小时长）",
            "file": f"{test_dir}/compliant_60s.mp4",
            "duration": 60,
            "width": 1920,
            "height": 1080,
            "color": "blue",
            "expected": "all_valid=True"
        },
        {
            "name": "合规视频（180秒，最大时长）",
            "file": f"{test_dir}/compliant_180s.mp4",
            "duration": 180,
            "width": 1920,
            "height": 1080,
            "color": "cyan",
            "expected": "all_valid=True"
        },
        {
            "name": "超长视频（200秒，超出范围）",
            "file": f"{test_dir}/invalid_duration_200s.mp4",
            "duration": 200,
            "width": 1920,
            "height": 1080,
            "color": "red",
            "expected": "duration_valid=False"
        },
        {
            "name": "过短视频（50秒，低于60秒）",
            "file": f"{test_dir}/invalid_duration_50s.mp4",
            "duration": 50,
            "width": 1920,
            "height": 1080,
            "color": "orange",
            "expected": "duration_valid=False"
        },
        {
            "name": "低分辨率视频（720p）",
            "file": f"{test_dir}/invalid_resolution_720p.mp4",
            "duration": 120,
            "width": 1280,
            "height": 720,
            "color": "yellow",
            "expected": "resolution_valid=False (but ratio=16:9)"
        },
        {
            "name": "错误比例视频（4:3）",
            "file": f"{test_dir}/invalid_ratio_4_3.mp4",
            "duration": 120,
            "width": 800,
            "height": 600,
            "color": "purple",
            "expected": "ratio_valid=False"
        },
        {
            "name": "低分辨率+错误比例（480p 4:3）",
            "file": f"{test_dir}/invalid_both_480p.mp4",
            "duration": 120,
            "width": 640,
            "height": 480,
            "color": "pink",
            "expected": "resolution_valid=False, ratio_valid=False"
        }
    ]

    # 生成每个测试视频
    success_count = 0
    for case in test_cases:
        print(f"\n测试场景：{case['name']}")
        print(f"预期结果：{case['expected']}")

        success = generate_test_video(
            output_path=case["file"],
            duration=case["duration"],
            width=case["width"],
            height=case["height"],
            color=case["color"]
        )

        if success:
            success_count += 1

    # 总结
    print("="*80)
    print(f"生成完成：{success_count}/{len(test_cases)}个测试视频")
    print("="*80)
    print("\n生成的文件列表：")
    for case in test_cases:
        if os.path.exists(case["file"]):
            size_mb = os.path.getsize(case["file"]) / (1024 * 1024)
            print(f"  - {case['file']} ({size_mb:.2f}MB)")

    print("\n下一步测试：")
    print(f"  python tests/test_ffmpeg_tool.py {test_dir}/compliant_150s.mp4")

if __name__ == "__main__":
    # 检查FFmpeg是否安装
    try:
        subprocess.run(
            ["ffmpeg", "-version"],
            capture_output=True,
            check=True,
            timeout=5
        )
        print("✅ FFmpeg已安装\n")
        generate_all_test_videos()
    except (subprocess.CalledProcessError, FileNotFoundError):
        print("❌ FFmpeg未安装，无法生成测试视频")
        print("\n请先安装FFmpeg：")
        print("  参考：docs/FFMPEG_INSTALLATION_GUIDE.md")
        sys.exit(1)