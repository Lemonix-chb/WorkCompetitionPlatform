"""
VideoAnalyzerAgent完整实现 - 集成所有视频评审工具

完整工具链集成：
1. FFmpegTool：元数据提取 + 硬性要求合规性检查
2. KeyFrameExtractorTool：关键帧提取（片头/正文/片尾）
3. OCRTool：字幕识别 + 主创信息检测
4. WhisperTool：语音转录 + 故事性分析

DeepSeek LLM评审推理：
- 基于工具输出生成评审报告
- 官方评分维度：故事性20 + 视觉效果20 + 导演技巧20 + 原创性20 + 文档完整性20
- 结构化输出保证评分一致性

"""

import os
import json
import logging
from typing import Dict, Any, Optional, List
from pydantic import BaseModel, Field
from langchain_openai import ChatOpenAI
from langchain_core.messages import HumanMessage, SystemMessage
from app.utils.llm_utils import extract_json_from_llm_output

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

    # 官方评分维度（各20分，5维度合计100分）
    story_score: float = Field(description="故事性评分（0-20分）", ge=0, le=20)
    visual_effect_score: float = Field(description="视觉效果评分（0-20分）", ge=0, le=20)
    director_skill_score: float = Field(description="导演技巧评分（0-20分）", ge=0, le=20)
    originality_score: float = Field(description="原创性评分（0-20分）", ge=0, le=20)
    documentation_score: float = Field(description="文档完整性评分（0-20分）", ge=0, le=20)

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
        完整视频评审流程

        Args:
            video_path: 视频文件路径
            work_description: 作品说明文档内容
            additional_files: 其他附加文件路径列表

        Returns:
            VideoReviewOutput: 结构化评审报告
        """
        logger.info(f"开始完整视频评审：{video_path}")

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
        content_richness = "未知"
        word_count = 0

        if whisper_result["success"]:
            transcription = whisper_result["transcription"]
            story_score = whisper_result["story_score"]
            content_richness = whisper_result.get("content_richness", "未知")
            word_count = whisper_result.get("word_count", 0)
            logger.info(f"语音转录成功：故事性评分{story_score}，字数{word_count}")

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
            story_score=story_score,
            content_richness=content_richness,
            word_count=word_count,
            keyframe_count=len(keyframes),
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

        logger.info(f"视频评审完成，总分：{review_result.overall_score}")

        return review_result

    def _get_system_prompt(self) -> str:
        """系统提示词：定义评审角色和标准（非计算机专业友好版）"""
        return """你是一位经验丰富的视频作品评审专家，负责评审非计算机专业大学生的视频作品（涵盖数媒动漫、短视频、教学演示等各类视频）。

重要背景：参赛者为非计算机专业学生，视频制作经验和技能参差不齐。请以鼓励性评价为主，在指出不足的同时肯定其努力和亮点。评分应体现公平性，不应以专业影视制作的标准要求参赛者。

【评分维度】（5个维度各20分，总分100分）：

1. 故事性（0-20分）：
   16-20分：故事主题明确，情节有吸引力；叙事逻辑清晰（有起承转合）；内容丰富，有情感表达或思想深度
   11-15分：有明确的故事线；内容较为充实；叙事基本流畅但节奏可能不够紧凑
   6-10分：有基本的主题但情节简单；叙事有些零散或跳跃；内容较为单薄
   0-5分：无明确故事线，内容松散；主题不明确
   注意：教学/演示类视频若有清晰的知识点讲解结构，应给 8-12 分；vlog/记录类作品的真实叙事也是一种风格。

2. 视觉效果（0-20分）：
   16-20分：画面清晰，构图合理；色调统一有风格感；剪辑流畅，节奏恰当；转场自然，字幕清晰规范
   11-15分：画面质量较好；剪辑基本流畅；有基本的字幕；转场效果一般
   6-10分：画面质量一般；剪辑有明显跳跃或重复；字幕不完整或有小错误
   0-5分：画面模糊或抖动严重；无剪辑痕迹或剪辑混乱；无字幕

3. 导演技巧（0-20分）：
   16-20分：镜头运用有设计感（不同景别、角度搭配）；音画配合好（配乐、音效与内容契合）；有开场和结尾设计
   11-15分：有基本的镜头切换；配乐选择合理；有简单的片头或片尾
   6-10分：镜头运用单一；音画配合基本可以；开头/结尾较粗糙
   0-5分：单一固定镜头全程；无配乐或配乐与内容不搭
   注意：vlog/记录类作品中，真实感本身是一种导演风格，不宜过低评分；手机拍摄的便捷性也是一种创作手段。

4. 原创性（0-20分）：
   16-20分：创意构思新颖，表达方式独特；内容为原创拍摄/制作；有个人风格
   11-15分：主题有一定新意；大部分内容为原创；部分素材来自网络但已注明
   6-10分：主题常见但有一定自己的想法；素材使用较多但基本有加工
   0-5分：内容大量照搬；无明显原创成分

5. 文档完整性（0-20分）：
   16-20分：有完整README（作品介绍、创作思路、制作团队）；有拍摄/制作说明和素材来源说明；文档内容详实
   11-15分：有README但内容不够详实；有部分制作说明；文档基本覆盖主题但缺少细节
   6-10分：仅有简单的README或说明；文档内容简短
   0-5分：无任何说明文档，或文档内容与作品严重不符

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
1. 参赛者为非计算机专业学生，请以鼓励性评价为主
2. 在指出不足的同时肯定其努力和亮点
3. 硬性要求不合规项需指出，但语气要温和
4. 评分应体现公平性，不应以专业影视标准要求参赛者
5. 提供具体、可操作的改进建议

输出格式：
请严格按照JSON格式输出，包含以下字段：
{
  "overall_score": 总分（0-100）,
  "story_score": 故事性评分（0-20）,
  "visual_effect_score": 视觉效果评分（0-20）,
  "director_skill_score": 导演技巧评分（0-20）,
  "originality_score": 原创性评分（0-20）,
  "documentation_score": 文档完整性评分（0-20）,
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
        story_score: float,
        content_richness: str = "未知",
        word_count: int = 0,
        keyframe_count: int = 0,
    ) -> str:
        """构建评审提示词"""

        compliance_status = "符合所有硬性要求" if compliance_check["all_valid"] else "部分硬性要求不合规"

        # 语速分析
        duration_seconds = metadata.get("duration_seconds", 0)
        speaking_rate = 0
        speaking_rate_level = "未知"
        if word_count > 0 and duration_seconds > 0:
            speaking_rate = round(word_count / (duration_seconds / 60), 1)
            if 150 <= speaking_rate <= 280:
                speaking_rate_level = "适中"
            elif speaking_rate < 150:
                speaking_rate_level = "偏慢"
            else:
                speaking_rate_level = "偏快"

        # 将 Whisper 的 story_score (0-25) 映射到 0-20
        story_score_hint = min(round(story_score * 20 / 25), 20) if story_score > 0 else 8

        # 构建静态分析报告
        analysis_section = f"""
【静态分析参考】（系统自动检测，仅供参考）：

--- 故事性指标 ---
- 语音转录字数：{word_count}字
- 内容丰富度：{content_richness}
- 语速：{speaking_rate}字/分钟（{speaking_rate_level}）
- 建议分数参考：{story_score_hint}/20

--- 技术指标 ---
- 视频时长：{metadata.get('duration_formatted', '未知')}（{duration_seconds}秒）
- 分辨率：{metadata.get('width', '?')}x{metadata.get('height', '?')}
- 关键帧提取数量：{keyframe_count}帧
- 字幕检测：{'有' if subtitle_found else '未检测到'}
- 创作者信息：{'有' if creator_info_found else '未检测到'}

说明：以上"建议分数参考"是系统基于静态分析计算的参考值。请以实际视频内容为主要依据进行评分。如果你的判断与参考值偏差较大（>5分），请在评审意见中说明原因。
"""

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
{analysis_section}
【语音转录】（前500字）：
{transcription[:500] if transcription else '无转录文本'}

【作品说明】：
{work_description}

请根据评审标准，对作品进行全面评价。注意参赛者为非计算机专业学生，请以鼓励为主，肯定其努力和亮点。请以JSON格式输出评审结果。"""

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
        try:
            result_dict = extract_json_from_llm_output(llm_output)

            # 构建VideoReviewOutput对象
            review_output = VideoReviewOutput(
                overall_score=result_dict.get("overall_score", story_score + 50),
                story_score=result_dict.get("story_score", story_score),
                visual_effect_score=result_dict.get("visual_effect_score", 12),
                director_skill_score=result_dict.get("director_skill_score", 12),
                originality_score=result_dict.get("originality_score", 12),
                documentation_score=result_dict.get("documentation_score", 8),
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
                visual_effect_score=12,
                director_skill_score=12,
                originality_score=12,
                documentation_score=8,
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
    os.environ["DEEPSEEK_API_KEY"] = "your-deepseek-api-key"

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