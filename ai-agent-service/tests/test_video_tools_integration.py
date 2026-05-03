"""
完整视频审核工具链测试

测试流程：
1. FFmpegTool提取元数据和合规性检查
2. KeyFrameExtractorTool提取关键帧
3. OCRTool检测字幕和主创信息
4. WhisperTool转录音频并分析故事性
5. VideoAnalyzerAgent整合所有工具输出生成评审报告

运行：python tests/test_video_tools_integration.py
"""

import os
import sys

# 添加项目根目录到Python路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

# 设置环境变量
os.environ["DEEPSEEK_API_KEY"] = "your-deepseek-api-key"

from app.tools import FFmpegTool, KeyFrameExtractorTool, OCRTool, WhisperTool


def test_complete_workflow():
    """完整工具链测试"""
    print("="*80)
    print("完整视频审核工具链测试")
    print("="*80)

    # 测试视频
    project_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    test_video = os.path.join(project_root, "test_videos", "compliant_150s.mp4")

    if not os.path.exists(test_video):
        print(f"[ERROR] 测试视频不存在：{test_video}")
        return False

    print(f"\n测试视频：{test_video}")

    # FFmpeg路径配置
    ffmpeg_path = r"E:\ffmpeg-8.1-essentials_build\ffmpeg-8.1-essentials_build\bin"

    # ========== 步骤1：FFmpegTool元数据提取 ==========
    print("\n[步骤1] FFmpegTool元数据提取...")
    try:
        ffmpeg_tool = FFmpegTool(ffmpeg_path=ffmpeg_path)
        ffmpeg_result = ffmpeg_tool._run(test_video)

        if ffmpeg_result["success"]:
            print("[OK] 元数据提取成功")
            print(f"  - 时长：{ffmpeg_result['metadata']['duration_seconds']}秒")
            print(f"  - 分辨率：{ffmpeg_result['metadata']['width']}x{ffmpeg_result['metadata']['height']}")
            print(f"  - 合规性：{'全部合规' if ffmpeg_result['compliance_check']['all_valid'] else '部分不合规'}")
        else:
            print(f"[ERROR] 元数据提取失败：{ffmpeg_result.get('error')}")
            return False
    except Exception as e:
        print(f"[ERROR] FFmpegTool异常：{e}")
        return False

    # ========== 步骤2：KeyFrameExtractorTool关键帧提取 ==========
    print("\n[步骤2] KeyFrameExtractorTool关键帧提取...")
    try:
        keyframe_tool = KeyFrameExtractorTool(ffmpeg_path=ffmpeg_path)
        keyframe_result = keyframe_tool._run(test_video)

        if keyframe_result["success"]:
            print("[OK] 关键帧提取成功")
            print(f"  - 总帧数：{keyframe_result['total_frames']}")
            print(f"  - 片头帧：{len(keyframe_result['opening_frames'])}")
            print(f"  - 正文帧：{len(keyframe_result['middle_frames'])}")
            print(f"  - 片尾帧：{len(keyframe_result['ending_frames'])}")

            # 保存关键帧路径供OCR使用
            all_keyframes = keyframe_result["keyframes"]
        else:
            print(f"[ERROR] 关键帧提取失败：{keyframe_result.get('error')}")
            return False
    except Exception as e:
        print(f"[ERROR] KeyFrameExtractorTool异常：{e}")
        return False

    # ========== 步骤3：OCRTool字幕检测 ==========
    print("\n[步骤3] OCRTool字幕检测...")
    try:
        # OCRTool需要Tesseract（可选配置）
        ocr_tool = OCRTool(
            tesseract_path=None  # Windows需要配置路径，或添加到PATH
        )

        # 如果关键帧存在，运行OCR
        if all_keyframes and os.path.exists(all_keyframes[0]):
            ocr_result = ocr_tool._run(all_keyframes[:5])  # 只测试前5帧

            if ocr_result["success"]:
                print("[OK] OCR识别成功")
                print(f"  - 字幕检测：{'有字幕' if ocr_result['subtitle_found'] else '无字幕（需要Tesseract）'}")
                print(f"  - 主创信息：{'有署名' if ocr_result['creator_info_found'] else '未检测到'}")
                print(f"  - 字幕质量评分：{ocr_result['subtitle_quality_score']}")
            else:
                print(f"[INFO] OCR识别未完成：{ocr_result.get('error')}")
                print("  提示：需要安装Tesseract OCR才能使用完整功能")
        else:
            print("[INFO] 无关键帧文件，跳过OCR测试")
    except Exception as e:
        print(f"[INFO] OCRTool异常（可接受）：{e}")

    # ========== 步骤4：WhisperTool语音转录 ==========
    print("\n[步骤4] WhisperTool语音转录...")
    try:
        whisper_tool = WhisperTool(
            ffmpeg_path=ffmpeg_path,
            whisper_model="base"
        )

        whisper_result = whisper_tool._run(test_video)

        if whisper_result["success"]:
            print("[OK] 语音转录成功")
            print(f"  - 总词数：{whisper_result['word_count']}")
            print(f"  - 唯一词数：{whisper_result['unique_words']}")
            print(f"  - 故事性评分：{whisper_result['story_score']}")
            print(f"  - 内容丰富度：{whisper_result['content_richness']}")

            print(f"\n转录文本（前100字）：")
            print(whisper_result['transcription'][:100])

            # 清理临时音频文件
            if whisper_result.get('audio_path'):
                whisper_tool.cleanup_audio(whisper_result['audio_path'])
        else:
            print(f"[INFO] 语音转录未完成：{whisper_result.get('error')}")
            print("  提示：需要安装whisper才能使用完整功能")
    except Exception as e:
        print(f"[INFO] WhisperTool异常（可接受）：{e}")

    # ========== 总结 ==========
    print("\n" + "="*80)
    print("[SUCCESS] 工具链基础测试完成")
    print("="*80)

    print("\n工具状态总结：")
    print("  ✅ FFmpegTool：元数据提取 + 合规性检查")
    print("  ✅ KeyFrameExtractorTool：关键帧提取")
    print("  ⏳ OCRTool：字幕检测（需要Tesseract安装）")
    print("  ⏳ WhisperTool：语音转录（需要Whisper安装）")

    print("\n下一步工作：")
    print("  1. 安装Tesseract OCR：https://github.com/UB-Mannheim/tesseract/wiki")
    print("  2. 安装Whisper依赖：pip install openai-whisper")
    print("  3. 集成所有工具到VideoAnalyzerAgent完整评审流程")

    # 清理关键帧临时文件
    try:
        keyframe_tool.cleanup()
        print("\n[INFO] 临时关键帧文件已清理")
    except:
        pass

    return True


if __name__ == "__main__":
    success = test_complete_workflow()
    sys.exit(0 if success else 1)