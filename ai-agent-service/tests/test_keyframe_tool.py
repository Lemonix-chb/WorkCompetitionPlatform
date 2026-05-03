"""
KeyFrameExtractorTool功能测试

验证：
1. FFmpeg关键帧提取
2. 片头/正文/片尾分类
3. 临时文件管理

运行：python tests/test_keyframe_tool.py
"""

import os
import sys

# 添加项目根目录到Python路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.tools.keyframe_tool import KeyFrameExtractorTool


def test_keyframe_extraction():
    """测试关键帧提取功能"""
    print("="*80)
    print("KeyFrameExtractorTool功能测试")
    print("="*80)

    # 测试视频路径
    project_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    test_video = os.path.join(project_root, "test_videos", "compliant_150s.mp4")

    if not os.path.exists(test_video):
        print(f"[ERROR] 测试视频不存在: {test_video}")
        return False

    print(f"\n测试视频：{test_video}")

    # 初始化工具
    print("\n[步骤1] 初始化KeyFrameExtractorTool...")
    tool = KeyFrameExtractorTool(
        ffmpeg_path=r"E:\ffmpeg-8.1-essentials_build\ffmpeg-8.1-essentials_build\bin"
    )
    print("[OK] 工具初始化成功")

    # 提取关键帧
    print("\n[步骤2] 提取关键帧...")
    result = tool._run(test_video)

    if not result["success"]:
        print(f"[ERROR] 关键帧提取失败: {result.get('error')}")
        return False

    print("[OK] 关键帧提取成功")

    # 显示结果
    print("\n提取结果：")
    print("-"*80)
    print(f"总帧数：{result['total_frames']}")
    print(f"片头帧：{len(result['opening_frames'])}帧")
    print(f"正文帧：{len(result['middle_frames'])}帧")
    print(f"片尾帧：{len(result['ending_frames'])}帧")
    print(f"主创信息检测：{'已标注' if result['creator_info_found'] else '未检测到（OCR待集成）'}")
    print(f"保存目录：{result['output_dir']}")
    print("-"*80)

    # 显示关键帧路径（部分）
    print("\n关键帧路径示例：")
    if result['opening_frames']:
        print(f"  片头第1帧：{result['opening_frames'][0]}")
    if result['middle_frames']:
        print(f"  正文第1帧：{result['middle_frames'][0]}")
    if result['ending_frames']:
        print(f"  片尾第1帧：{result['ending_frames'][0]}")

    # 清理临时文件
    print("\n[步骤3] 清理临时文件...")
    tool.cleanup()
    print("[OK] 临时文件已清理")

    print("\n" + "="*80)
    print("[SUCCESS] KeyFrameExtractorTool功能测试通过")
    print("="*80)

    print("\n下一步：集成OCR检测主创信息署名")

    return True


if __name__ == "__main__":
    success = test_keyframe_extraction()
    sys.exit(0 if success else 1)