"""
VideoAnalyzerAgent - 视频作品审核专家

职责：审核数媒动漫与短视频作品

官方评分维度（校教发〔2024〕77号）：
- 故事性（叙事手法、情节逻辑、内容丰富度）: 0-25分
- 视觉效果（画面质量、剪辑节奏、转场合理性）: 0-25分
- 导演技巧（镜头运用、视听效果）: 0-25分
- 原创性（创意构思、原创内容）: 0-25分

总分：100分（AI评分 × weight + 评委评分 × weight）

硬性要求检查：
- 时长：60-180秒
- 画面比例：16:9
- 分辨率：≥1920x1080（1080p）
- 格式：MP4
- 文件大小：≤300MB
- 字幕：必须存在
- 片头片尾：必须包含
- 主创信息标注：片头片尾是否有署名

作者：AI Agent架构重构项目
创建时间：2026-05-03
"""

import os
import json
from typing import Dict, Any, Optional, List
from pydantic import BaseModel, Field
from langchain_openai import ChatOpenAI
from langchain_core.messages import HumanMessage, SystemMessage

# 使用相对导入
from ..tools.ffmpeg_tool import FFmpegTool


class VideoReviewOutput(BaseModel):
    """视频评审结构化输出"""

    # 总分
    overall_score: float = Field(description="总体评分（0-100分）", ge=0, le=100)

    # 官方评分维度（各25分）
    story_score: float = Field(description="故事性评分（0-25分）", ge=0, le=25)
    visual_effect_score: float = Field(description="视觉效果评分（0-25分）", ge=0, le=25)
    director_skill_score: float = Field(description="导演技巧评分（0-25分）", ge=0, le=25)
    originality_score: float = Field(description="原创性评分（0-25分）", ge=0, le=25)

    # 硬性要求合规性
    compliance_check: Dict[str, Any] = Field(description="硬性要求合规性检查结果")

    # 评审意见
    review_summary: str = Field(description="评审总结（200-500字）")
    strengths: List[str] = Field(description="作品亮点（3-5条）")
    weaknesses: List[str] = Field(description="不足之处（2-3条）")
    improvement_suggestions: List[str] = Field(description="改进建议（3-5条）")

    # 元数据
    video_metadata: Dict[str, Any] = Field(description="视频元数据")


class VideoAnalyzerAgent:
    """
    视频分析专家Agent

    使用工具：
    - FFmpegTool：元数据提取和硬性要求检查
    - DeepSeek LLM：评审推理和评分生成

    输出：
    - VideoReviewOutput：结构化评审报告
    """

    def __init__(
        self,
        model_name: str = "deepseek-chat",
        api_key: Optional[str] = None,
        base_url: Optional[str] = None,
        ffmpeg_path: Optional[str] = None
    ):
        """
        初始化视频分析Agent

        Args:
            model_name: DeepSeek模型名称（deepseek-chat或deepseek-coder）
            api_key: DeepSeek API密钥（如果不提供，尝试从环境变量读取）
            base_url: DeepSeek API地址（默认https://api.deepseek.com）
            ffmpeg_path: FFmpeg bin目录路径（如果不提供，依赖系统PATH）
        """
        # 配置DeepSeek API
        self.api_key = api_key or os.getenv("DEEPSEEK_API_KEY")
        self.base_url = base_url or os.getenv("DEEPSEEK_BASE_URL", "https://api.deepseek.com")

        if not self.api_key:
            raise ValueError("DeepSeek API密钥未设置。请设置DEEPSEEK_API_KEY环境变量或传入api_key参数")

        # 初始化LLM
        self.llm = ChatOpenAI(
            model=model_name,
            api_key=self.api_key,
            base_url=self.base_url,
            temperature=0.3,  # 较低温度保证评分稳定性
        )

        # 初始化工具
        self.ffmpeg_tool = FFmpegTool(ffmpeg_path=ffmpeg_path)

    def review_video(
        self,
        video_path: str,
        work_description: str,
        additional_files: Optional[List[str]] = None
    ) -> VideoReviewOutput:
        """
        完整视频审核流程

        Args:
            video_path: 视频文件路径
            work_description: 作品说明文档内容（剧本、拍摄说明等）
            additional_files: 其他附加文件路径列表

        Returns:
            VideoReviewOutput: 结构化评审报告
        """
        # 步骤1：使用FFmpegTool提取元数据并检查硬性要求
        ffmpeg_result = self.ffmpeg_tool._run(video_path)

        if not ffmpeg_result.get("success"):
            raise ValueError(f"视频元数据提取失败：{ffmpeg_result.get('error')}")

        metadata = ffmpeg_result["metadata"]
        compliance_check = ffmpeg_result["compliance_check"]

        # 步骤2：构建LLM评审prompt
        prompt = self._build_review_prompt(metadata, compliance_check, work_description)

        # 步骤3：调用LLM生成评审意见
        response = self.llm.invoke([
            SystemMessage(content=self._get_system_prompt()),
            HumanMessage(content=prompt)
        ])

        # 步骤4：解析LLM输出为结构化结果
        review_result = self._parse_llm_output(response.content, metadata, compliance_check)

        return review_result

    def _get_system_prompt(self) -> str:
        """系统提示词：定义评审角色和标准"""
        return """你是一位专业的视频作品评审专家，负责审核大学生计算机设计大赛的数媒动漫与短视频作品。

评审标准（根据校教发〔2024〕77号文件）：

【评分维度】（每个维度0-25分，总分100分）：
1. 故事性（25分）：
   - 叙事手法是否得当（线性/非线性叙事是否清晰）
   - 情节逻辑是否连贯（有无明显逻辑漏洞）
   - 内容丰富度（主题深度、细节呈现、情感表达）

2. 视觉效果（25分）：
   - 画面质量（清晰度、色彩搭配、构图美感）
   - 剪辑节奏（节奏是否流畅、有无拖沓或跳跃）
   - 转场合理性（转场效果是否自然、是否服务于内容）

3. 导演技巧（25分）：
   - 镜头运用（镜头角度、景别变化、运动镜头使用）
   - 视听效果（音画同步、背景音乐、音效使用）
   - 导演调度（场面调度、演员表演指导、整体把控）

4. 原创性（25分）：
   - 创意构思（题材新颖度、表达方式创新）
   - 原创内容（原创素材占比、有无明显抄袭）
   - 个人风格（独特的视觉语言、叙事风格）

【硬性要求检查】：
- 时长：60-180秒
- 画面比例：16:9
- 分辨率：≥1920x1080（1080p）
- 格式：MP4
- 文件大小：≤300MB

评审原则：
1. 严格遵循评分标准，客观公正
2. 硬性要求不合规项需在评审意见中明确指出
3. 既肯定作品亮点，也指出不足之处
4. 提供具体、可操作的改进建议

输出格式：
请严格按照JSON格式输出，包含以下字段：
- overall_score（总分）
- story_score（故事性评分）
- visual_effect_score（视觉效果评分）
- director_skill_score（导演技巧评分）
- originality_score（原创性评分）
- review_summary（评审总结，200-500字）
- strengths（作品亮点，3-5条）
- weaknesses（不足之处，2-3条）
- improvement_suggestions（改进建议，3-5条）"""

    def _build_review_prompt(
        self,
        metadata: Dict[str, Any],
        compliance_check: Dict[str, Any],
        work_description: str
    ) -> str:
        """构建评审提示词"""

        compliance_status = "符合所有硬性要求" if compliance_check["all_valid"] else "部分硬性要求不合规"

        prompt = f"""请评审以下视频作品：

【视频元数据】：
- 文件路径：{metadata['file_path']}
- 时长：{metadata['duration_formatted']}（{metadata['duration_seconds']}秒）
- 分辨率：{metadata['width']}x{metadata['height']}
- 画面比例：{metadata['ratio_simplified']}
- 编码格式：{metadata['codec']}
- 文件大小：{metadata['file_size_mb']:.2f}MB

【硬性要求合规性】：
状态：{compliance_status}
检查详情：
- 时长要求（60-180秒）：{'✓合规' if compliance_check['duration_valid'] else '✗不合规 - ' + compliance_check['duration_message']}
- 画面比例（16:9）：{'✓合规' if compliance_check['ratio_valid'] else '✗不合规 - ' + compliance_check['ratio_message']}
- 分辨率要求（1080p）：{'✓合规' if compliance_check['resolution_valid'] else '✗不合规 - ' + compliance_check['resolution_message']}
- 格式要求（MP4）：{'✓合规' if compliance_check['format_valid'] else '✗不合规 - ' + compliance_check['format_message']}
- 文件大小（≤300MB）：{'✓合规' if compliance_check['size_valid'] else '✗不合规 - ' + compliance_check['size_message']}

【作品说明】：
{work_description}

请根据评审标准，对作品进行全面评价。请以JSON格式输出评审结果。"""

        return prompt

    def _parse_llm_output(
        self,
        llm_output: str,
        metadata: Dict[str, Any],
        compliance_check: Dict[str, Any]
    ) -> VideoReviewOutput:
        """
        解析LLM输出为结构化评审结果

        Args:
            llm_output: LLM返回的文本（JSON格式）
            metadata: 视频元数据
            compliance_check: 合规性检查结果

        Returns:
            VideoReviewOutput: 结构化评审报告
        """
        try:
            # 尝试解析JSON
            # LLM可能输出Markdown格式，需要提取JSON部分
            json_str = llm_output
            if "```json" in llm_output:
                json_str = llm_output.split("```json")[1].split("```")[0].strip()
            elif "```" in llm_output:
                json_str = llm_output.split("```")[1].split("```")[0].strip()

            result_dict = json.loads(json_str)

            # 构建VideoReviewOutput对象
            review_output = VideoReviewOutput(
                overall_score=result_dict.get("overall_score", 0),
                story_score=result_dict.get("story_score", 0),
                visual_effect_score=result_dict.get("visual_effect_score", 0),
                director_skill_score=result_dict.get("director_skill_score", 0),
                originality_score=result_dict.get("originality_score", 0),
                compliance_check=compliance_check,
                review_summary=result_dict.get("review_summary", ""),
                strengths=result_dict.get("strengths", []),
                weaknesses=result_dict.get("weaknesses", []),
                improvement_suggestions=result_dict.get("improvement_suggestions", []),
                video_metadata=metadata
            )

            return review_output

        except json.JSONDecodeError as e:
            print(f"LLM输出JSON解析失败：{e}")
            print(f"原始输出：\n{llm_output}")

            # 返回默认结果（解析失败时的fallback）
            return VideoReviewOutput(
                overall_score=0,
                story_score=0,
                visual_effect_score=0,
                director_skill_score=0,
                originality_score=0,
                compliance_check=compliance_check,
                review_summary="评审结果解析失败，请重新评审",
                strengths=[],
                weaknesses=["LLM输出格式错误"],
                improvement_suggestions=["请检查LLM输出是否为标准JSON格式"],
                video_metadata=metadata
            )


# 使用示例
if __name__ == "__main__":
    # 示例配置
    import os

    # 设置环境变量（需要在运行前设置）
    # os.environ["DEEPSEEK_API_KEY"] = "your_api_key_here"

    # 初始化Agent
    agent = VideoAnalyzerAgent(
        model_name="deepseek-chat",
        ffmpeg_path=r"E:\ffmpeg-8.1-essentials_build\ffmpeg-8.1-essentials_build\bin"
    )

    # 测试视频路径
    test_video = "test_videos/compliant_150s.mp4"
    work_desc = """
    作品名称：《机器学习入门教程》
    作品类型：AI教育短视频
    内容简介：通过动画演示讲解机器学习基础概念，适合初学者观看。
    制作团队：张三（导演）、李四（编剧）、王五（剪辑）
    """

    if os.path.exists(test_video):
        result = agent.review_video(test_video, work_desc)

        print("\n评审报告：")
        print("="*80)
        print(f"总分：{result.overall_score}")
        print(f"故事性：{result.story_score}")
        print(f"视觉效果：{result.visual_effect_score}")
        print(f"导演技巧：{result.director_skill_score}")
        print(f"原创性：{result.originality_score}")

        print(f"\n硬性要求合规性：{'全部合规' if result.compliance_check['all_valid'] else '部分不合规'}")

        print(f"\n评审总结：\n{result.review_summary}")

        print(f"\n作品亮点：")
        for strength in result.strengths:
            print(f"  - {strength}")

        print(f"\n不足之处：")
        for weakness in result.weaknesses:
            print(f"  - {weakness}")

        print(f"\n改进建议：")
        for suggestion in result.improvement_suggestions:
            print(f"  - {suggestion}")

        print("="*80)
    else:
        print(f"测试视频不存在：{test_video}")