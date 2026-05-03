"""
WhisperTool - 语音转文字和故事性分析工具

功能：
1. FFmpeg提取音频轨道
2. Whisper API语音转录（带时间戳）
3. 故事性分析（文本连贯性、词汇多样性、情节逻辑）
4. 内容丰富度评估

输出：
- transcription: 完整转录文本
- segments: 分段转录（带时间戳）
- story_score: 故事性评分（0-25）
- content_richness: 内容丰富度评级

作者：AI Agent架构重构项目
创建时间：2026-05-03
"""

import os
import subprocess
import tempfile
import logging
from typing import Dict, Any, List, Optional
from langchain.tools import BaseTool
from pydantic import Field

# Whisper依赖（需要安装）
try:
    import whisper
    WHISPER_AVAILABLE = True
except ImportError:
    WHISPER_AVAILABLE = False
    logging.warning("whisper未安装，语音转录功能不可用。请运行：pip install openai-whisper")

logger = logging.getLogger(__name__)


class WhisperTool(BaseTool):
    """
    Whisper语音转录工具（LangChain BaseTool实现）

    官方评分维度：故事性（叙事手法、情节逻辑、内容丰富度）
    """

    name: str = "whisper_transcription"
    description: str = """
    转录视频音频并分析故事性。
    输入：视频文件路径
    输出：转录文本、分段信息、故事性评分

    分析维度：
    - 文本连贯性（叙事手法）
    - 词汇多样性（内容丰富度）
    - 情节逻辑（时间戳分段分析）
    """

    # FFmpeg路径配置
    ffmpeg_path: Optional[str] = Field(
        default=None,
        description="FFmpeg bin目录路径"
    )

    # Whisper模型配置
    whisper_model: str = Field(
        default="base",  # tiny/base/small/medium/large
        description="Whisper模型大小（base平衡速度和质量）"
    )

    # Whisper模型实例（延迟加载）
    _model_instance: Optional[Any] = None

    def _get_ffmpeg_exe(self) -> str:
        """获取ffmpeg可执行文件路径"""
        if self.ffmpeg_path:
            return os.path.join(self.ffmpeg_path, "ffmpeg.exe")
        return "ffmpeg"

    def _load_whisper_model(self):
        """加载Whisper模型（延迟加载）"""
        if not WHISPER_AVAILABLE:
            raise RuntimeError("whisper未安装，无法加载模型")

        if self._model_instance is None:
            logger.info(f"加载Whisper模型：{self.whisper_model}")
            self._model_instance = whisper.load_model(self.whisper_model)

        return self._model_instance

    def _run(self, video_path: str) -> Dict[str, Any]:
        """
        执行语音转录和故事性分析

        Args:
            video_path: 视频文件路径

        Returns:
            dict: {
                "success": True/False,
                "transcription": "完整转录文本",
                "segments": [
                    {"start": 0, "end": 30, "text": "..."},
                    ...
                ],
                "story_score": 0-25,
                "content_richness": "高/中/低",
                "word_count": 总词数,
                "duration_coverage": 音频覆盖时长比例,
                "error": "错误信息"  # 如果失败
            }
        """
        if not WHISPER_AVAILABLE:
            return {
                "success": False,
                "error": "whisper未安装，语音转录功能不可用",
                "transcription": "",
                "segments": [],
                "story_score": 0
            }

        try:
            if not os.path.exists(video_path):
                return {
                    "success": False,
                    "error": f"视频文件不存在：{video_path}",
                    "transcription": ""
                }

            logger.info(f"开始语音转录：{video_path}")

            # 步骤1：提取音频轨道
            audio_path = self._extract_audio(video_path)

            if not audio_path:
                return {
                    "success": False,
                    "error": "音频提取失败",
                    "transcription": ""
                }

            # 步骤2：Whisper转录
            transcription_result = self._transcribe_audio(audio_path)

            if not transcription_result["success"]:
                return transcription_result

            # 步骤3：分析故事性
            story_analysis = self._analyze_story(
                transcription_result["transcription"],
                transcription_result["segments"]
            )

            # 合并结果
            result = {
                "success": True,
                "transcription": transcription_result["transcription"],
                "segments": transcription_result["segments"],
                "story_score": story_analysis["story_score"],
                "content_richness": story_analysis["content_richness"],
                "word_count": story_analysis["word_count"],
                "unique_words": story_analysis["unique_words"],
                "duration_coverage": transcription_result["duration_coverage"],
                "audio_path": audio_path  # 临时音频文件路径（可选清理）
            }

            logger.info(f"语音转录完成，故事性评分：{story_analysis['story_score']}")

            return result

        except Exception as e:
            logger.error(f"语音转录失败：{e}")
            return {
                "success": False,
                "error": str(e),
                "transcription": "",
                "segments": [],
                "story_score": 0
            }

    def _extract_audio(self, video_path: str) -> Optional[str]:
        """
        使用FFmpeg提取音频轨道

        Args:
            video_path: 视频文件路径

        Returns:
            str: 音频文件路径（临时文件）
        """
        try:
            ffmpeg_exe = self._get_ffmpeg_exe()

            # 创建临时音频文件
            audio_path = tempfile.mktemp(suffix=".wav")

            # FFmpeg命令：提取音频为WAV格式
            cmd = [
                ffmpeg_exe,
                "-i", video_path,  # 输入视频
                "-vn",  # 不包含视频
                "-acodec", "pcm_s16le",  # WAV编码
                "-ar", "16000",  # 16kHz采样率（Whisper推荐）
                "-ac", "1",  # 单声道
                "-y",  # 覆盖已存在文件
                audio_path
            ]

            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                timeout=60
            )

            if result.returncode == 0 and os.path.exists(audio_path):
                logger.info(f"音频提取成功：{audio_path}")
                return audio_path
            else:
                logger.error(f"音频提取失败：{result.stderr}")
                return None

        except subprocess.CalledProcessError as e:
            logger.error(f"FFmpeg音频提取失败：{e.stderr}")
            return None
        except Exception as e:
            logger.error(f"音频提取异常：{e}")
            return None

    def _transcribe_audio(self, audio_path: str) -> Dict[str, Any]:
        """
        使用Whisper转录音频

        Args:
            audio_path: 音频文件路径

        Returns:
            dict: {
                "success": True/False,
                "transcription": "完整文本",
                "segments": [分段信息],
                "duration_coverage": 音频覆盖时长比例,
                "error": "错误信息"
            }
        """
        try:
            # 加载Whisper模型
            model = self._load_whisper_model()

            logger.info(f"开始Whisper转录：{audio_path}")

            # 转录音频
            result = model.transcribe(audio_path, language="zh")

            # 提取完整文本
            transcription = result.get("text", "")

            # 提取分段信息（带时间戳）
            segments = []
            for seg in result.get("segments", []):
                segments.append({
                    "start": seg.get("start", 0),
                    "end": seg.get("end", 0),
                    "text": seg.get("text", "")
                })

            # 计算音频覆盖时长
            if segments:
                total_duration = segments[-1]["end"]
                covered_duration = sum(seg["end"] - seg["start"] for seg in segments)
                duration_coverage = covered_duration / total_duration if total_duration > 0 else 0
            else:
                duration_coverage = 0

            return {
                "success": True,
                "transcription": transcription,
                "segments": segments,
                "duration_coverage": duration_coverage
            }

        except Exception as e:
            logger.error(f"Whisper转录失败：{e}")
            return {
                "success": False,
                "error": str(e),
                "transcription": "",
                "segments": []
            }

    def _analyze_story(
        self,
        transcription: str,
        segments: List[Dict]
    ) -> Dict[str, Any]:
        """
        分析故事性（官方评分维度）

        评分依据：
        - 文本连贯性（叙事手法）：15分
        - 词汇多样性（内容丰富度）：5分
        - 情节逻辑（分段分析）：5分

        Args:
            transcription: 完整转录文本
            segments: 分段转录信息

        Returns:
            dict: {
                "story_score": 0-25,
                "content_richness": "高/中/低",
                "word_count": 总词数,
                "unique_words": 唯一词数,
                "coherence_score": 连贯性评分,
                "diversity_score": 多样性评分
            }
        """
        # 基础统计
        words = transcription.split()
        word_count = len(words)
        unique_words = len(set(words))

        # 如果没有文本，直接返回0分
        if word_count == 0:
            return {
                "story_score": 0,
                "content_richness": "无",
                "word_count": 0,
                "unique_words": 0
            }

        # 1. 文本连贯性评分（叙事手法）
        # 根据文本长度和分段连贯性判断
        coherence_score = min(word_count / 50, 15)  # 每50词1分，最多15分

        # 2. 词汇多样性评分（内容丰富度）
        # 根据词汇丰富度判断
        diversity_ratio = unique_words / word_count if word_count > 0 else 0
        diversity_score = min(diversity_ratio * 10, 5)  # 最多5分

        # 3. 情节逻辑评分（分段分析）
        # 根据分段数量和时间分布判断
        segment_count = len(segments)
        logic_score = min(segment_count * 0.5, 5)  # 每2个分段1分，最多5分

        # 总分（0-25）
        story_score = round(coherence_score + diversity_score + logic_score, 2)

        # 内容丰富度评级
        if story_score >= 20:
            content_richness = "高"
        elif story_score >= 10:
            content_richness = "中"
        else:
            content_richness = "低"

        return {
            "story_score": story_score,
            "content_richness": content_richness,
            "word_count": word_count,
            "unique_words": unique_words,
            "coherence_score": round(coherence_score, 2),
            "diversity_score": round(diversity_score, 2),
            "logic_score": round(logic_score, 2)
        }

    def cleanup_audio(self, audio_path: str):
        """清理临时音频文件"""
        if audio_path and os.path.exists(audio_path):
            try:
                os.remove(audio_path)
                logger.info(f"临时音频文件已清理：{audio_path}")
            except Exception as e:
                logger.warning(f"临时音频文件清理失败：{e}")


# 使用示例
if __name__ == "__main__":
    if WHISPER_AVAILABLE:
        tool = WhisperTool(
            ffmpeg_path=r"E:\ffmpeg-8.1-essentials_build\ffmpeg-8.1-essentials_build\bin",
            whisper_model="base"
        )

        test_video = "test_videos/compliant_150s.mp4"

        if os.path.exists(test_video):
            print("="*80)
            print("WhisperTool语音转录测试")
            print("="*80)

            result = tool._run(test_video)

            if result["success"]:
                print(f"\n转录成功：")
                print(f"  - 总词数：{result['word_count']}")
                print(f"  - 唯一词数：{result['unique_words']}")
                print(f"  - 故事性评分：{result['story_score']}")
                print(f"  - 内容丰富度：{result['content_richness']}")
                print(f"  - 音频覆盖：{result['duration_coverage']:.2%}")

                print(f"\n转录文本（前200字）：")
                print(result['transcription'][:200])

                print(f"\n分段数量：{len(result['segments'])}")
                if result['segments']:
                    print("第1段：", result['segments'][0])

                # 清理临时音频
                if result.get('audio_path'):
                    tool.cleanup_audio(result['audio_path'])
            else:
                print(f"\n转录失败：{result['error']}")

            print("="*80)
        else:
            print(f"测试视频不存在：{test_video}")
    else:
        print("[WARNING] whisper未安装，语音转录功能不可用")
        print("安装方法：pip install openai-whisper")