"""
VideoAnalyzerAgent完整实现 - 集成所有视频审核工具

完整工具链集成：
1. FFmpegTool：元数据提取 + 硬性要求合规性检查
2. KeyFrameExtractorTool：关键帧提取（片头/正文/片尾）
3. OCRTool：字幕识别 + 主创信息检测
4. WhisperTool：语音转录 + 故事性分析

DeepSeek LLM评审推理：
- 基于工具输出生成评审报告
- 官方评分维度：故事性25 + 视觉效果25 + 导演技巧25 + 原创性25
- 结构化输出保证评分一致性

作者：AI Agent架构重构项目
更新时间：2026-05-03
"""

import os
import logging
from typing import Dict, Any, Optional, List
from pydantic import BaseModel, Field
from langchain_openai import ChatOpenAI
from langchain_core.messages import HumanMessage, SystemMessage

# 导入所有工具
from ..tools.ffmpeg_tool import FFmpegTool
from ..tools.keyframe_tool import KeyFrameExtractorTool
from ..tools.ocr_tool import OCRTool
from ..tools.whisper_tool import WhisperTool

logger = logging.getLogger(__name__)


class VideoReviewOutput(BaseModel):
    """视频评审结构化输出（官方评分维度）"""

    # 总分（100分）
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

    # 视频元数据
    video_metadata: Dict[str, Any] = Field(description="视频元数据")


class VideoAnalyzerAgent:
    """
    视频分析专家Agent（完整工具链集成）

    工具链：
    - FFmpegTool：元数据 + 合规性（✅）
    - KeyFrameExtractorTool：关键帧提取（✅）
    - OCRTool：字幕 + 主创信息（✅）
    - WhisperTool：语音转录 + 故事性（✅）

    LLM评审：
    - DeepSeek Chat模型
    - 结构化输出（VideoReviewOutput）
    """

    def __init__(
        self,
        model_name: str = "deepseek-chat",
        api_key: Optional[str] = None,
        base_url: Optional[str] = None,
        ffmpeg_path: Optional[str] = None,
        whisper_model: str = "base"
    ):
        """
        初始化视频分析Agent

        Args:
            model_name: DeepSeek模型名称
            api_key: DeepSeek API密钥
            base_url: DeepSeek API地址
            ffmpeg_path: FFmpeg bin目录路径
            whisper_model: Whisper模型大小（tiny/base/small/medium）
        """
        # 配置DeepSeek API
        self.api_key = api_key or os.getenv("DEEPSEEK_API_KEY")
        self.base_url = base_url or os.getenv("DEEPSEEK_BASE_URL", "https://api.deepseek.com")

        if not self.api_key:
            raise ValueError("DeepSeek API密钥未设置")

        # 初始化LLM
        self.llm = ChatOpenAI(
            model=model_name,
            api_key=self.api_key,
            base_url=self.base_url,
            temperature=0.3
        )

        # 初始化工具链
        self.ffmpeg_tool = FFmpegTool(ffmpeg_path=ffmpeg_path)
        self.keyframe_tool = KeyFrameExtractorTool(ffmpeg_path=ffmpeg_path)
        self.ocr_tool = OCRTool()
        self.whisper_tool = WhisperTool(
            ffmpeg_path=ffmpeg_path,
            whisper_model=whisper_model
        )

        logger.info("VideoAnalyzerAgent初始化完成，工具链已加载")

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
            work_description: 作品说明文档内容
            additional_files: 其他附加文件路径列表

        Returns:
            VideoReviewOutput: 结构化评审报告
        """
        logger.info(f"开始完整视频审核：{video_path}")

        # ========== 步骤1：FFmpegTool元数据提取 ==========
        logger.info("步骤1：FFmpegTool元数据提取...")
        ffmpeg_result = self.ffmpeg_tool._run(video_path)

        if not ffmpeg_result["success"]:
            raise ValueError(f"视频元数据提取失败：{ffmpeg_result.get('error')}")

        metadata = ffmpeg_result["metadata"]
        compliance_check = ffmpeg_result["compliance_check"]

        logger.info(f"元数据提取成功：时长{metadata['duration_seconds']}秒，合规性{'全部合规' if compliance_check['all_valid'] else '部分不合规'}")

        # ========== 步骤2：KeyFrameExtractorTool关键帧提取 ==========
        logger.info("步骤2：KeyFrameExtractorTool关键帧提取...")
        keyframe_result = self.keyframe_tool._run(video_path)

        if keyframe_result["success"]:
            keyframes = keyframe_result["keyframes"]
            opening_frames = keyframe_result["opening_frames"]
            ending_frames = keyframe_result["ending_frames"]
            logger.info(f"关键帧提取成功：{len(keyframes)}帧")
        else:
            keyframes = []
            opening_frames = []
            ending_frames = []
            logger.warning(f"关键帧提取失败：{keyframe_result.get('error')}")

        # ========== 步骤3：OCRTool字幕检测 ==========
        logger.info("步骤3：OCRTool字幕检测...")
        ocr_result = self.ocr_tool._run(keyframes[:10] if keyframes else [])

        subtitle_found = ocr_result.get("subtitle_found", False)
        creator_info_found = ocr_result.get("creator_info_found", False)
        subtitle_quality_score = ocr_result.get("subtitle_quality_score", 0)

        logger.info(f"OCR检测完成：字幕{'存在' if subtitle_found else '未检测'}, 主创信息{'有署名' if creator_info_found else '未检测'}")

        # ========== 步骤4：WhisperTool语音转录 ==========
        logger.info("步骤4：WhisperTool语音转录...")
        whisper_result = self.whisper_tool._run(video_path)

        transcription = ""
        story_score = 0
        content_richness = "无"

        if whisper_result["success"]:
            transcription = whisper_result["transcription"]
            story_score = whisper_result["story_score"]
            content_richness = whisper_result["content_richness"]
            logger.info(f"语音转录成功：故事性评分{story_score}")

            # 清理临时音频文件
            if whisper_result.get("audio_path"):
                self.whisper_tool.cleanup_audio(whisper_result["audio_path"])
        else:
            logger.warning(f"语音转录失败：{whisper_result.get('error')}")

        # ========== 步骤5：DeepSeek LLM评审推理 ==========
        logger.info("步骤5：DeepSeek LLM评审推理...")

        # 构建评审prompt
        prompt = self._build_review_prompt(
            metadata=metadata,
            compliance_check=compliance_check,
            work_description=work_description,
            subtitle_found=subtitle_found,
            creator_info_found=creator_info_found,
            transcription=transcription,
            story_score=story_score
        )

        # 调用LLM
        response = self.llm.invoke([
            SystemMessage(content=self._get_system_prompt()),
            HumanMessage(content=prompt)
        ])

        # 解析LLM输出
        review_result = self._parse_llm_output(
            llm_output=response.content,
            metadata=metadata,
            compliance_check=compliance_check,
            story_score=story_score
        )

        # 清理关键帧临时文件
        if keyframe_result.get("success"):
            self.keyframe_tool.cleanup()

        logger.info(f"视频审核完成，总分：{review_result.overall_score}")

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
- 字幕：必须存在（官方强制要求）
- 片头片尾：必须包含
- 主创信息标注：片头片尾是否有署名

评审原则：
1. 严格遵循评分标准，客观公正
2. 硬性要求不合规项需在评审意见中明确指出
3. 既肯定作品亮点，也指出不足之处
4. 提供具体、可操作的改进建议

输出格式：
请严格按照JSON格式输出，包含以下字段：
{
  "overall_score": 总分（0-100）,
  "story_score": 故事性评分（0-25）,
  "visual_effect_score": 视觉效果评分（0-25）,
  "director_skill_score": 导演技巧评分（0-25）,
  "originality_score": 原创性评分（0-25）,
  "review_summary": "评审总结（200-500字）",
  "strengths": ["亮点1", "亮点2", ...],
  "weaknesses": ["不足1", "不足2", ...],
  "improvement_suggestions": ["建议1", "建议2", ...]
}"""

    def _build_review_prompt(
        self,
        metadata: Dict[str, Any],
        compliance_check: Dict[str, Any],
        work_description: str,
        subtitle_found: bool,
        creator_info_found: bool,
        transcription: str,
        story_score: float
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
- 时长要求（60-180秒）：{'✓合规' if compliance_check['duration_valid'] else '✗不合规'}
- 画面比例（16:9）：{'✓合规' if compliance_check['ratio_valid'] else '✗不合规'}
- 分辨率要求（1080p）：{'✓合规' if compliance_check['resolution_valid'] else '✗不合规'}
- 格式要求（MP4）：{'✓合规' if compliance_check['format_valid'] else '✗不合规'}
- 文件大小（≤300MB）：{'✓合规' if compliance_check['size_valid'] else '✗不合规'}

【字幕检测】：
- 字幕是否存在：{'✓有字幕' if subtitle_found else '✗未检测到字幕（官方强制要求）'}
- 主创信息署名：{'✓有署名' if creator_info_found else '✗未检测到'}

【语音转录】：
- 转录文本（前500字）：{transcription[:500] if transcription else '无转录文本'}
- 故事性初步评分：{story_score}/25分（基于文本分析）

【作品说明】：
{work_description}

请根据评审标准，对作品进行全面评价。请以JSON格式输出评审结果。"""

        return prompt

    def _parse_llm_output(
        self,
        llm_output: str,
        metadata: Dict[str, Any],
        compliance_check: Dict[str, Any],
        story_score: float
    ) -> VideoReviewOutput:
        """
        解析LLM输出为结构化评审结果
        """
        import json

        try:
            # 提取JSON部分
            json_str = llm_output
            if "```json" in llm_output:
                json_str = llm_output.split("```json")[1].split("```")[0].strip()
            elif "```" in llm_output:
                json_str = llm_output.split("```")[1].split("```")[0].strip()

            result_dict = json.loads(json_str)

            # 构建VideoReviewOutput对象
            review_output = VideoReviewOutput(
                overall_score=result_dict.get("overall_score", story_score + 50),
                story_score=result_dict.get("story_score", story_score),
                visual_effect_score=result_dict.get("visual_effect_score", 15),
                director_skill_score=result_dict.get("director_skill_score", 15),
                originality_score=result_dict.get("originality_score", 15),
                compliance_check=compliance_check,
                review_summary=result_dict.get("review_summary", ""),
                strengths=result_dict.get("strengths", []),
                weaknesses=result_dict.get("weaknesses", []),
                improvement_suggestions=result_dict.get("improvement_suggestions", []),
                video_metadata=metadata
            )

            return review_output

        except json.JSONDecodeError as e:
            logger.error(f"LLM输出JSON解析失败：{e}")

            # 返回默认结果
            return VideoReviewOutput(
                overall_score=story_score + 50,
                story_score=story_score,
                visual_effect_score=15,
                director_skill_score=15,
                originality_score=15,
                compliance_check=compliance_check,
                review_summary="评审结果解析失败，请重新评审",
                strengths=["评审解析失败"],
                weaknesses=["LLM输出格式错误"],
                improvement_suggestions=["请检查LLM输出格式"],
                video_metadata=metadata
            )


# 使用示例
if __name__ == "__main__":
    import os

    # 设置环境变量
    os.environ["DEEPSEEK_API_KEY"] = "sk-325ae1ccf357480ab353a41e8b26ee32"

    # 初始化Agent
    agent = VideoAnalyzerAgent(
        ffmpeg_path=r"E:\ffmpeg-8.1-essentials_build\ffmpeg-8.1-essentials_build\bin",
        whisper_model="base"
    )

    # 测试视频
    test_video = "test_videos/compliant_150s.mp4"
    work_desc = """
    作品名称：《机器学习入门教程》
    作品类型：AI教育短视频
    内容简介：通过动画演示讲解机器学习基础概念。
    """

    if os.path.exists(test_video):
        print("="*80)
        print("VideoAnalyzerAgent完整工具链测试")
        print("="*80)

        result = agent.review_video(test_video, work_desc)

        print(f"\n总分：{result.overall_score}")
        print(f"故事性：{result.story_score}")
        print(f"视觉效果：{result.visual_effect_score}")
        print(f"导演技巧：{result.director_skill_score}")
        print(f"原创性：{result.originality_score}")

        print(f"\n评审总结：")
        print(result.review_summary)

        print("="*80)
    else:
        print(f"测试视频不存在：{test_video}")