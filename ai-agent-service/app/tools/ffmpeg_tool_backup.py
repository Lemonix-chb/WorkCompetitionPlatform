# FFmpeg Tool - Extract video metadata and check compliance
# 提取视频元数据并检查官方硬性要求

import os
import subprocess
import json
import logging
from typing import Dict, Any
from langchain.tools import BaseTool
from pydantic import Field

logger = logging.getLogger(__name__)


class FFmpegTool(BaseTool):
    """
    FFmpeg元数据提取工具

    功能：
    1. 提取视频技术参数（时长、分辨率、编码、码率等）
    2. 检查官方硬性要求合规性：
       - 时长：60-180秒
       - 比例：16:9
       - 分辨率：1080p（1920x1080或更高）
       - 格式：MP4
       - 文件大小：≤300MB

    适用作品类型：VIDEO（数媒动漫与短视频）
    """

    name: str = "ffmpeg_metadata"
    description: str = """
    提取视频元数据并检查是否符合官方要求。
    输入：视频文件路径
    输出：元数据字典 + 合规性检查结果

    检查项目：
    - 时长60-180秒
    - 16:9画面比例
    - 1080p分辨率
    - MP4格式
    - 文件大小≤300MB
    """

    # FFmpeg可执行文件路径（可选配置）
    ffmpeg_path: str = Field(
        default=None,
        description="FFmpeg bin目录路径。如果未设置，使用系统PATH"
    )

    def _get_ffprobe_exe(self) -> str:
        """获取ffprobe可执行文件路径"""
        if self.ffmpeg_path:
            return os.path.join(self.ffmpeg_path, "ffprobe.exe")
        return "ffprobe"  # 依赖系统PATH

    def _get_ffmpeg_exe(self) -> str:
        """获取ffmpeg可执行文件路径"""
        if self.ffmpeg_path:
            return os.path.join(self.ffmpeg_path, "ffmpeg.exe")
        return "ffmpeg"  # 依赖系统PATH

    def _run(self, video_path: str) -> Dict[str, Any]:
        """执行元数据提取和合规性检查"""
        logger.info(f"FFmpegTool starting: {video_path}")

        try:
            # 使用ffprobe提取元数据
            metadata = self._extract_metadata(video_path)

            # 执行合规性检查
            compliance_check = self._check_compliance(metadata)

            result = {
                "metadata": metadata,
                "compliance_check": compliance_check,
                "tool_name": self.name,
                "success": True
            }

            logger.info(f"FFmpegTool completed successfully: {result}")
            return result

        except Exception as e:
            logger.error(f"FFmpegTool failed: {e}")
            return {
                "metadata": {},
                "compliance_check": {},
                "error": str(e),
                "success": False
            }

    def _extract_metadata(self, video_path: str) -> Dict[str, Any]:
        """使用ffprobe提取视频元数据"""
        try:
            # ffprobe命令：提取JSON格式的元数据
            cmd = [
                "ffprobe",
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
                check=True
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
                # 基础信息
                "file_path": video_path,
                "file_name": format_info.get("filename", ""),
                "format": format_info.get("format_name", ""),
                "file_size_mb": round(float(format_info.get("size", 0)) / (1024 * 1024), 2),

                # 视频参数
                "duration_seconds": round(float(format_info.get("duration", 0)), 2),
                "duration_formatted": self._format_duration(float(format_info.get("duration", 0))),
                "bit_rate_kbps": round(float(format_info.get("bit_rate", 0)) / 1000, 2),

                # 视频流参数
                "width": video_stream.get("width", 0) if video_stream else 0,
                "height": video_stream.get("height", 0) if video_stream else 0,
                "codec": video_stream.get("codec_name", "") if video_stream else "",
                "fps": eval(video_stream.get("r_frame_rate", "0/1")) if video_stream else 0,
                "aspect_ratio": video_stream.get("display_aspect_ratio", "N/A") if video_stream else "N/A",

                # 音频参数
                "audio_codec": audio_stream.get("codec_name", "") if audio_stream else "",
                "audio_channels": audio_stream.get("channels", 0) if audio_stream else 0,
                "audio_sample_rate": audio_stream.get("sample_rate", "0") if audio_stream else "0",
            }

            # 计算画面比例
            if metadata["width"] and metadata["height"]:
                metadata["ratio"] = f"{metadata['width']}:{metadata['height']}"
                metadata["ratio_simplified"] = self._simplify_ratio(metadata["width"], metadata["height"])

            return metadata

        except subprocess.CalledProcessError as e:
            logger.error(f"ffprobe command failed: {e}")
            raise Exception(f"FFprobe执行失败: {e.stderr}")
        except json.JSONDecodeError as e:
            logger.error(f"JSON parsing failed: {e}")
            raise Exception(f"元数据解析失败: {e}")

    def _check_compliance(self, metadata: Dict[str, Any]) -> Dict[str, bool]:
        """检查官方硬性要求合规性"""
        compliance = {}

        # 1. 时长检查：60-180秒
        duration = metadata.get("duration_seconds", 0)
        compliance["duration_valid"] = 60 <= duration <= 180
        compliance["duration_seconds"] = duration
        compliance["duration_message"] = (
            f"时长{duration}秒，符合要求（60-180秒）"
            if compliance["duration_valid"]
            else f"时长{duration}秒，不符合要求（应在60-180秒范围）"
        )

        # 2. 画面比例检查：16:9
        width = metadata.get("width", 0)
        height = metadata.get("height", 0)
        ratio_simplified = metadata.get("ratio_simplified", "")
        compliance["ratio_valid"] = ratio_simplified == "16:9"
        compliance["ratio_message"] = (
            f"画面比例{ratio_simplified}，符合要求（16:9）"
            if compliance["ratio_valid"]
            else f"画面比例{ratio_simplified}，不符合要求（应为16:9）"
        )

        # 3. 分辨率检查：1080p（1920x1080或更高）
        compliance["resolution_valid"] = width >= 1920 and height >= 1080
        compliance["resolution_message"] = (
            f"分辨率{width}x{height}，符合要求（1080p或更高）"
            if compliance["resolution_valid"]
            else f"分辨率{width}x{height}，不符合要求（应≥1920x1080）"
        )

        # 4. 格式检查：MP4
        format_name = metadata.get("format", "")
        # MP4的format_name可能是"mov,mp4,m4a,3gp,3g2,mj2"
        compliance["format_valid"] = "mp4" in format_name.lower() or metadata.get("file_name", "").lower().endswith(".mp4")
        compliance["format_message"] = (
            f"格式{format_name}，符合要求（MP4）"
            if compliance["format_valid"]
            else f"格式{format_name}，不符合要求（应为MP4）"
        )

        # 5. 文件大小检查：≤300MB
        file_size_mb = metadata.get("file_size_mb", 0)
        compliance["size_valid"] = file_size_mb <= 300
        compliance["size_message"] = (
            f"文件大小{file_size_mb}MB，符合要求（≤300MB）"
            if compliance["size_valid"]
            else f"文件大小{file_size_mb}MB，不符合要求（应≤300MB）"
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
            "视频符合所有官方硬性要求"
            if compliance["all_valid"]
            else "视频不符合部分官方要求，详见上述检查项"
        )

        return compliance

    def _format_duration(self, seconds: float) -> str:
        """将秒数格式化为 MM:SS 格式"""
        minutes = int(seconds // 60)
        secs = int(seconds % 60)
        return f"{minutes}:{secs:02d}"

    def _simplify_ratio(self, width: int, height: int) -> str:
        """简化画面比例"""
        # 计算最大公约数
        def gcd(a, b):
            while b:
                a, b = b, a % b
            return a

        divisor = gcd(width, height)
        w = width // divisor
        h = height // divisor

        # 常见比例标准化
        if w == 16 and h == 9:
            return "16:9"
        elif w == 4 and h == 3:
            return "4:3"
        elif w == 21 and h == 9:
            return "21:9"
        else:
            return f"{w}:{h}"