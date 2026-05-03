"""
VideoAnalyzerAgent完整功能测试

验证：
1. DeepSeek API连接
2. FFmpegTool元数据提取
3. LLM评审推理
4. 结构化输出解析

运行：python tests/test_video_analyzer_agent.py
"""

import os
import sys

# 添加项目根目录到Python路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

# 设置环境变量
os.environ["DEEPSEEK_API_KEY"] = "your-deepseek-api-key"

from app.agents.video_analyzer_agent import VideoAnalyzerAgent

def test_agent_initialization():
    """测试Agent初始化"""
    print("="*80)
    print("VideoAnalyzerAgent完整功能测试")
    print("="*80)

    print("\n[步骤1] 初始化Agent...")
    try:
        agent = VideoAnalyzerAgent(
            model_name="deepseek-chat",
            ffmpeg_path=r"E:\ffmpeg-8.1-essentials_build\ffmpeg-8.1-essentials_build\bin"
        )
        print("[OK] Agent初始化成功")
        print(f"  - LLM模型: {agent.llm.model_name}")
        print(f"  - FFmpeg路径: {agent.ffmpeg_tool.ffmpeg_path}")
        return agent
    except Exception as e:
        print(f"[ERROR] Agent初始化失败: {e}")
        return None


def test_ffmpeg_extraction(agent, video_path):
    """测试FFmpeg元数据提取"""
    print("\n[步骤2] 提取视频元数据...")
    try:
        result = agent.ffmpeg_tool._run(video_path)

        if result["success"]:
            print("[OK] 元数据提取成功")
            print(f"  - 时长: {result['metadata']['duration_formatted']}")
            print(f"  - 分辨率: {result['metadata']['width']}x{result['metadata']['height']}")
            print(f"  - 比例: {result['metadata']['ratio_simplified']}")
            print(f"  - 编码: {result['metadata']['codec']}")
            print(f"  - 合规性: {'全部合规' if result['compliance_check']['all_valid'] else '部分不合规'}")
            return result
        else:
            print(f"[ERROR] 元数据提取失败: {result.get('error')}")
            return None
    except Exception as e:
        print(f"[ERROR] FFmpeg执行异常: {e}")
        return None


def test_llm_review(agent, video_path, ffmpeg_result):
    """测试LLM评审推理"""
    print("\n[步骤3] LLM评审推理...")

    work_description = """
    作品名称：《机器学习入门教程》
    作品类型：AI教育短视频
    内容简介：通过动画演示讲解机器学习基础概念，适合初学者观看。
    制作团队：张三（导演）、李四（编剧）、王五（剪辑）
    """

    try:
        print("  正在调用DeepSeek API...")
        review_output = agent.review_video(
            video_path=video_path,
            work_description=work_description
        )

        print("[OK] LLM评审完成")
        print(f"\n评审结果：")
        print("-"*80)
        print(f"总分：{review_output.overall_score}")
        print(f"故事性：{review_output.story_score}")
        print(f"视觉效果：{review_output.visual_effect_score}")
        print(f"导演技巧：{review_output.director_skill_score}")
        print(f"原创性：{review_output.originality_score}")
        print("-"*80)

        print(f"\n评审总结：")
        print(review_output.review_summary)

        print(f"\n作品亮点：")
        for strength in review_output.strengths:
            print(f"  + {strength}")

        print(f"\n不足之处：")
        for weakness in review_output.weaknesses:
            print(f"  - {weakness}")

        print(f"\n改进建议：")
        for suggestion in review_output.improvement_suggestions:
            print(f"  → {suggestion}")

        print("="*80)
        print("[SUCCESS] VideoAnalyzerAgent完整功能测试通过")
        print("="*80)

        return review_output

    except Exception as e:
        print(f"[ERROR] LLM评审失败: {e}")
        import traceback
        traceback.print_exc()
        return None


if __name__ == "__main__":
    # 测试视频路径（相对于项目根目录）
    project_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    test_video = os.path.join(project_root, "test_videos", "compliant_150s.mp4")

    if not os.path.exists(test_video):
        print(f"[ERROR] 测试视频不存在: {test_video}")
        print("请先运行：python tests/generate_test_videos.py")
        sys.exit(1)

    # 步骤1：初始化Agent
    agent = test_agent_initialization()
    if not agent:
        sys.exit(1)

    # 步骤2：FFmpeg元数据提取
    ffmpeg_result = test_ffmpeg_extraction(agent, test_video)
    if not ffmpeg_result:
        sys.exit(1)

    # 步骤3：LLM评审推理
    review_output = test_llm_review(agent, test_video, ffmpeg_result)

    if review_output:
        print("\n下一步：实现KeyFrameExtractorTool扩展视频分析能力")
    else:
        sys.exit(1)