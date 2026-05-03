"""
FFmpegTool - 视频元数据提取和合规性检查工具

功能：
1. 提取视频元数据（时长、分辨率、编码、文件大小等）
2. 检查官方硬性要求合规性：
   - 时长：60-180秒
   - 画面比例：16:9
   - 分辨率：≥1920x1080（1080p）
   - 格式：MP4
   - 文件大小：≤300MB

作者：AI Agent架构重构项目
创建时间：2026-05-03
"""

import os
import json
import subprocess
from typing import Dict, Any, Optional
from langchain.tools import BaseTool
from pydantic import Field


class FFmpegTool(BaseTool):
    """
    FFmpeg视频元数据提取工具（LangChain BaseTool实现）
    """

    name: str = "ffmpeg_metadata"
    description: str = "提取视频元数据并检查官方硬性要求合规性。输入视频文件路径，返回元数据和合规性检查结果。"

    # FFmpeg可执行文件路径配置（支持自定义路径）
    ffmpeg_path: Optional[str] = Field(
        default=None,
        description="FFmpeg bin目录路径。如果未设置，将尝试从系统PATH查找"
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
        """
        执行工具：提取视频元数据并检查合规性

        Args:
            video_path: 视频文件路径

        Returns:
            dict: {
                "metadata": {...},  # 视频元数据
                "compliance_check": {...},  # 合规性检查结果
                "success": True/False,
                "error": "错误信息"  # 如果失败
            }
        """
        try:
            # 验证文件存在
            if not os.path.exists(video_path):
                return {
                    "success": False,
                    "error": f"视频文件不存在：{video_path}"
                }

            # 提取元数据
            metadata = self._extract_metadata(video_path)

            if not metadata:
                return {
                    "success": False,
                    "error": "元数据提取失败"
                }

            # 检查合规性
            compliance_check = self._check_compliance(metadata)

            return {
                "success": True,
                "metadata": metadata,
                "compliance_check": compliance_check
            }

        except Exception as e:
            return {
                "success": False,
                "error": str(e)
            }

    def _extract_metadata(self, video_path: str) -> Optional[Dict[str, Any]]:
        """
        使用ffprobe提取视频元数据

        Args:
            video_path: 视频文件路径

        Returns:
            dict: 视频元数据字典
        """
        try:
            ffprobe_exe = self._get_ffprobe_exe()

            # ffprobe命令：提取format和streams信息（JSON格式）
            cmd = [
                ffprobe_exe,
                "-v", "quiet",  # 静默模式
                "-print_format", "json",  # JSON输出
                "-show_format",  # 显示format信息
                "-show_streams",  # 显示stream信息
                video_path
            ]

            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                check=True,
                timeout=10
            )

            data = json.loads(result.stdout)

            # 解析关键元数据
            format_info = data.get("format", {})
            streams = data.get("streams", [])

            # 找到视频流和音频流
            video_stream = None
            audio_stream = None
            for stream in streams:
                if stream.get("codec_type") == "video":
                    video_stream = stream
                elif stream.get("codec_type") == "audio":
                    audio_stream = stream

            # 构建元数据字典
            metadata = {
                "file_path": video_path,
                "duration_seconds": round(float(format_info.get("duration", 0)), 2),
                "width": video_stream.get("width", 0) if video_stream else 0,
                "height": video_stream.get("height", 0) if video_stream else 0,
                "codec": video_stream.get("codec_name", "") if video_stream else "",
                "format": format_info.get("format_name", ""),
                "file_size_mb": round(float(format_info.get("size", 0)) / (1024 * 1024), 2),
                "bitrate": int(format_info.get("bit_rate", 0)) if format_info.get("bit_rate") else 0,
                "fps": self._parse_fps(video_stream) if video_stream else 0
            }

            # 计算画面比例（简化形式）
            if metadata["width"] and metadata["height"]:
                from math import gcd
                divisor = gcd(metadata["width"], metadata["height"])
                w = metadata["width"] // divisor
                h = metadata["height"] // divisor
                metadata["ratio_simplified"] = f"{w}:{h}"
            else:
                metadata["ratio_simplified"] = "unknown"

            # 格式化时长（MM:SS格式）
            duration_sec = metadata["duration_seconds"]
            minutes = int(duration_sec // 60)
            seconds = int(duration_sec % 60)
            metadata["duration_formatted"] = f"{minutes}:{seconds:02d}"

            return metadata

        except subprocess.CalledProcessError as e:
            print(f"ffprobe执行失败：{e.stderr}")
            return None
        except json.JSONDecodeError as e:
            print(f"JSON解析失败：{e}")
            return None
        except Exception as e:
            print(f"元数据提取错误：{e}")
            return None

    def _parse_fps(self, video_stream: Dict) -> float:
        """解析视频帧率"""
        fps_str = video_stream.get("r_frame_rate", "0/1")
        try:
            if "/" in fps_str:
                num, den = fps_str.split("/")
                return round(float(num) / float(den), 2)
            return float(fps_str)
        except:
            return 0.0

    def _check_compliance(self, metadata: Dict[str, Any]) -> Dict[str, Any]:
        """
        检查官方硬性要求合规性

        根据校教发〔2024〕77号文件，数媒动漫与短视频作品要求：
        - 时长：60-180秒
        - 画面比例：16:9
        - 分辨率：1080p（1920x1080或更高）
        - 格式：MP4
        - 文件大小：≤300MB

        Args:
            metadata: 视频元数据

        Returns:
            dict: 合规性检查结果
        """
        compliance = {}

        # 1. 时长检查：60-180秒
        duration = metadata.get("duration_seconds", 0)
        compliance["duration_valid"] = 60 <= duration <= 180
        compliance["duration_value"] = duration
        compliance["duration_message"] = (
            f"时长{duration}秒（{metadata.get('duration_formatted', 'unknown')}），符合要求（60-180秒）"
            if compliance["duration_valid"]
            else f"时长{duration}秒，不符合要求（应在60-180秒范围）"
        )

        # 2. 画面比例检查：16:9
        ratio = metadata.get("ratio_simplified", "unknown")
        compliance["ratio_valid"] = ratio == "16:9"
        compliance["ratio_value"] = ratio
        compliance["ratio_message"] = (
            f"画面比例{ratio}，符合要求（16:9）"
            if compliance["ratio_valid"]
            else f"画面比例{ratio}，不符合要求（应为16:9）"
        )

        # 3. 分辨率检查：≥1920x1080
        width = metadata.get("width", 0)
        height = metadata.get("height", 0)
        compliance["resolution_valid"] = width >= 1920 and height >= 1080
        compliance["resolution_value"] = f"{width}x{height}"
        compliance["resolution_message"] = (
            f"分辨率{width}x{height}，符合要求（1080p或更高）"
            if compliance["resolution_valid"]
            else f"分辨率{width}x{height}，不符合要求（应≥1920x1080）"
        )

        # 4. 格式检查：MP4
        format_name = metadata.get("format", "unknown")
        compliance["format_valid"] = "mp4" in format_name.lower()
        compliance["format_value"] = format_name
        compliance["format_message"] = (
            f"格式{format_name}，符合要求（MP4）"
            if compliance["format_valid"]
            else f"格式{format_name}，不符合要求（应为MP4）"
        )

        # 5. 文件大小检查：≤300MB
        file_size_mb = metadata.get("file_size_mb", 0)
        compliance["size_valid"] = file_size_mb <= 300
        compliance["size_value"] = file_size_mb
        compliance["size_message"] = (
            f"文件大小{file_size_mb:.2f}MB，符合要求（≤300MB）"
            if compliance["size_valid"]
            else f"文件大小{file_size_mb:.2f}MB，不符合要求（应≤300MB）"
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
            else "视频不符合部分官方硬性要求，详见上述检查项"
        )

        # 7. 合规项数量统计
        valid_count = sum([
            compliance["duration_valid"],
            compliance["ratio_valid"],
            compliance["resolution_valid"],
            compliance["format_valid"],
            compliance["size_valid"]
        ])
        compliance["valid_count"] = valid_count
        compliance["total_checks"] = 5

        return compliance


# 工具使用示例
if __name__ == "__main__":
    # 示例1：使用系统PATH中的FFmpeg
    tool1 = FFmpegTool()

    # 示例2：使用自定义路径的FFmpeg
    tool2 = FFmpegTool(
        ffmpeg_path=r"E:\ffmpeg-8.1-essentials_build\ffmpeg-8.1-essentials_build\bin"
    )

    # 测试视频文件路径（需要提供实际文件）
    test_video = "test_video.mp4"

    if os.path.exists(test_video):
        result = tool2._run(test_video)

        if result["success"]:
            print("\n视频元数据：")
            print(json.dumps(result["metadata"], indent=2, ensure_ascii=False))

            print("\n合规性检查：")
            print(json.dumps(result["compliance_check"], indent=2, ensure_ascii=False))
        else:
            print(f"错误：{result['error']}")
    else:
        print(f"测试视频不存在：{test_video}")