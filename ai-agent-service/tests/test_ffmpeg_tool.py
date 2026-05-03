# FFmpegTool Testing Script
# 测试视频元数据提取和合规性检查功能

import sys
import os
import json

# Add parent directory to path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.tools.ffmpeg_tool import FFmpegTool

def test_ffmpeg_tool(video_path: str):
    """
    测试FFmpegTool功能

    Args:
        video_path: 测试视频文件路径
    """
    print("="*80)
    print("FFmpegTool 测试脚本")
    print("="*80)
    print(f"\n测试文件：{video_path}\n")

    # 创建工具实例
    tool = FFmpegTool(video_path=video_path)

    # 执行测试
    print("正在执行元数据提取...\n")
    result = tool._run(video_path)

    # 检查是否成功
    if result.get("success"):
        print("✅ 测试成功！\n")
        print("-"*80)
        print("视频元数据：")
        print("-"*80)

        metadata = result["metadata"]
        print(json.dumps(metadata, indent=2, ensure_ascii=False))

        print("\n" + "-"*80)
        print("合规性检查：")
        print("-"*80)

        compliance = result["compliance_check"]
        print(json.dumps(compliance, indent=2, ensure_ascii=False))

        # 总体结论
        print("\n" + "="*80)
        if compliance["all_valid"]:
            print("✅ 视频符合所有官方硬性要求")
        else:
            print("❌ 视频不符合部分官方要求，详见上述检查项")
        print("="*80)

    else:
        print("❌ 测试失败！")
        print(f"错误信息：{result.get('error')}")
        print("\n可能的原因：")
        print("1. FFmpeg未安装（需要系统级FFmpeg 6.0+）")
        print("2. 视频文件路径错误")
        print("3. 视频文件损坏或格式不支持")

def check_ffmpeg_installation():
    """检查FFmpeg是否已安装"""
    import subprocess
    try:
        result = subprocess.run(
            ["ffmpeg", "-version"],
            capture_output=True,
            text=True,
            timeout=5
        )
        if result.returncode == 0:
            print("✅ FFmpeg已安装")
            print(f"版本信息：\n{result.stdout.split('\\n')[0]}")
            return True
        else:
            print("❌ FFmpeg未正确安装")
            return False
    except FileNotFoundError:
        print("❌ FFmpeg未安装")
        print("\n安装方法：")
        print("-"*80)
        print("Windows安装FFmpeg：")
        print("1. 下载预编译版本：https://www.gyan.dev/ffmpeg/builds/")
        print("2. 选择 ffmpeg-release-essentials.7z")
        print("3. 解压到 C:\\ffmpeg")
        print("4. 添加到系统PATH：")
        print("   - 打开'环境变量'设置")
        print("   - 在系统变量中找到'Path'")
        print("   - 添加：C:\\ffmpeg\\bin")
        print("5. 重启命令行窗口")
        print("6. 验证安装：ffmpeg -version")
        print("-"*80)
        return False
    except Exception as e:
        print(f"❌ 检查FFmpeg时出错：{e}")
        return False

if __name__ == "__main__":
    # 先检查FFmpeg安装
    ffmpeg_installed = check_ffmpeg_installation()

    if not ffmpeg_installed:
        print("\n⚠️ 请先安装FFmpeg后再运行测试")
        sys.exit(1)

    # 检查命令行参数
    if len(sys.argv) < 2:
        print("\n使用方法：")
        print(f"python {sys.argv[0]} <视频文件路径>")
        print("\n示例：")
        print(f"python {sys.argv[0]} test_video.mp4")
        print(f"python {sys.argv[0]} uploads/works/2026/demo.mp4")
        sys.exit(1)

    # 执行测试
    test_video_path = sys.argv[1]
    test_ffmpeg_tool(test_video_path)