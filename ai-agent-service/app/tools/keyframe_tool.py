"""
KeyFrameExtractorTool - 关键帧提取工具

功能：
1. 提取视频关键帧截图
2. 分片头（前5秒）、正文（每5秒）、片尾（最后5秒）提取
3. OCR检测片头片尾文字（寻找主创信息）
4. Vision模型分析画面质量（视觉效果评分依据）

输出：
- 关键帧图片路径列表
- 片头片尾帧检测结果
- 主创信息是否标注
- 画面质量评分（0-25）

作者：AI Agent架构重构项目
创建时间：2026-05-03
"""

import os
import subprocess
import tempfile
from typing import Dict, Any, List, Optional
from langchain.tools import BaseTool
from pydantic import Field


class KeyFrameExtractorTool(BaseTool):
    """
    关键帧提取工具（LangChain BaseTool实现）
    """

    name: str = "keyframe_extractor"
    description: str = "提取视频关键帧并检测片头片尾主创信息。输入视频文件路径，返回关键帧图片路径和检测结果。"

    # FFmpeg路径配置
    ffmpeg_path: Optional[str] = Field(
        default=None,
        description="FFmpeg bin目录路径"
    )

    # 关键帧保存目录
    output_dir: Optional[str] = Field(
        default=None,
        description="关键帧图片保存目录（默认临时目录）"
    )

    def _get_ffmpeg_exe(self) -> str:
        """获取ffmpeg可执行文件路径"""
        if self.ffmpeg_path:
            return os.path.join(self.ffmpeg_path, "ffmpeg.exe")
        return "ffmpeg"

    def _run(self, video_path: str) -> Dict[str, Any]:
        """
        执行工具：提取关键帧并检测片头片尾

        Args:
            video_path: 视频文件路径

        Returns:
            dict: {
                "success": True/False,
                "keyframes": [图片路径列表],
                "opening_frames": [片头帧],
                "ending_frames": [片尾帧],
                "creator_info_found": True/False,
                "visual_quality_score": 0-25,
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

            # 创建输出目录
            if not self.output_dir:
                self.output_dir = tempfile.mkdtemp(prefix="keyframes_")

            os.makedirs(self.output_dir, exist_ok=True)

            # 提取关键帧
            keyframe_paths = self._extract_keyframes(video_path)

            if not keyframe_paths:
                return {
                    "success": False,
                    "error": "关键帧提取失败"
                }

            # 分类关键帧（片头、正文、片尾）
            categorized = self._categorize_keyframes(video_path, keyframe_paths)

            # OCR检测片头片尾（主创信息检测）
            creator_info_found = self._detect_creator_info(
                categorized["opening_frames"],
                categorized["ending_frames"]
            )

            # Vision模型分析画面质量（后续集成）
            visual_quality_score = 0  # 待实现Vision分析

            return {
                "success": True,
                "keyframes": keyframe_paths,
                "opening_frames": categorized["opening_frames"],
                "middle_frames": categorized["middle_frames"],
                "ending_frames": categorized["ending_frames"],
                "creator_info_found": creator_info_found,
                "visual_quality_score": visual_quality_score,
                "total_frames": len(keyframe_paths),
                "output_dir": self.output_dir
            }

        except Exception as e:
            return {
                "success": False,
                "error": str(e)
            }

    def _extract_keyframes(self, video_path: str) -> List[str]:
        """
        使用FFmpeg提取关键帧

        策略：
        - 片头：前5秒，每秒1帧（共5帧）
        - 正文：每5秒1帧
        - 片尾：最后5秒，每秒1帧（共5帧）

        Args:
            video_path: 视频文件路径

        Returns:
            list: 关键帧图片路径列表
        """
        ffmpeg_exe = self._get_ffmpeg_exe()
        keyframe_paths = []

        try:
            # 先获取视频时长（需要FFmpegTool支持）
            # 这里简化实现：直接提取关键帧

            # 提取片头关键帧（前5秒，每秒1帧）
            for i in range(5):
                output_path = os.path.join(self.output_dir, f"opening_{i+1}s.jpg")

                cmd = [
                    ffmpeg_exe,
                    "-ss", str(i),  # 时间点（秒）
                    "-i", video_path,
                    "-vframes", "1",  # 提取1帧
                    "-q:v", "2",  # 高质量
                    "-y",  # 覆盖已存在文件
                    output_path
                ]

                result = subprocess.run(
                    cmd,
                    capture_output=True,
                    text=True,
                    timeout=10
                )

                if result.returncode == 0 and os.path.exists(output_path):
                    keyframe_paths.append(output_path)

            # 提取正文关键帧（每5秒1帧，最多提取20帧）
            for i in range(5, 105, 5):  # 5s到100s，每5秒
                output_path = os.path.join(self.output_dir, f"middle_{i}s.jpg")

                cmd = [
                    ffmpeg_exe,
                    "-ss", str(i),
                    "-i", video_path,
                    "-vframes", "1",
                    "-q:v", "2",
                    "-y",
                    output_path
                ]

                result = subprocess.run(
                    cmd,
                    capture_output=True,
                    text=True,
                    timeout=10
                )

                if result.returncode == 0 and os.path.exists(output_path):
                    keyframe_paths.append(output_path)

            # 提取片尾关键帧（最后5秒，假设视频至少150秒）
            for i in range(145, 150):
                output_path = os.path.join(self.output_dir, f"ending_{i}s.jpg")

                cmd = [
                    ffmpeg_exe,
                    "-ss", str(i),
                    "-i", video_path,
                    "-vframes", "1",
                    "-q:v", "2",
                    "-y",
                    output_path
                ]

                result = subprocess.run(
                    cmd,
                    capture_output=True,
                    text=True,
                    timeout=10
                )

                if result.returncode == 0 and os.path.exists(output_path):
                    keyframe_paths.append(output_path)

            return keyframe_paths

        except subprocess.CalledProcessError as e:
            print(f"FFmpeg关键帧提取失败：{e.stderr}")
            return []
        except Exception as e:
            print(f"关键帧提取异常：{e}")
            return []

    def _categorize_keyframes(
        self,
        video_path: str,
        keyframe_paths: List[str]
    ) -> Dict[str, List[str]]:
        """
        分类关键帧（片头、正文、片尾）

        Args:
            video_path: 视频文件路径
            keyframe_paths: 关键帧路径列表

        Returns:
            dict: {
                "opening_frames": [片头帧],
                "middle_frames": [正文帧],
                "ending_frames": [片尾帧]
            }
        """
        opening_frames = []
        middle_frames = []
        ending_frames = []

        for path in keyframe_paths:
            filename = os.path.basename(path)

            if filename.startswith("opening_"):
                opening_frames.append(path)
            elif filename.startswith("middle_"):
                middle_frames.append(path)
            elif filename.startswith("ending_"):
                ending_frames.append(path)

        return {
            "opening_frames": opening_frames,
            "middle_frames": middle_frames,
            "ending_frames": ending_frames
        }

    def _detect_creator_info(
        self,
        opening_frames: List[str],
        ending_frames: List[str]
    ) -> bool:
        """
        OCR检测片头片尾文字（寻找主创信息）

        Args:
            opening_frames: 片头关键帧路径列表
            ending_frames: 片尾关键帧路径列表

        Returns:
            bool: 是否检测到主创信息署名
        """
        # 待实现OCR集成（pytesseract / EasyOCR）
        # 现在返回False（未检测到）
        return False

    def cleanup(self):
        """清理临时关键帧图片"""
        if self.output_dir and os.path.exists(self.output_dir):
            import shutil
            shutil.rmtree(self.output_dir, ignore_errors=True)


# 使用示例
if __name__ == "__main__":
    tool = KeyFrameExtractorTool(
        ffmpeg_path=r"E:\ffmpeg-8.1-essentials_build\ffmpeg-8.1-essentials_build\bin"
    )

    test_video = "test_videos/compliant_150s.mp4"

    if os.path.exists(test_video):
        result = tool._run(test_video)

        if result["success"]:
            print("\n关键帧提取结果：")
            print(f"  总帧数：{result['total_frames']}")
            print(f"  片头帧：{len(result['opening_frames'])}")
            print(f"  正文帧：{len(result['middle_frames'])}")
            print(f"  片尾帧：{len(result['ending_frames'])}")
            print(f"  主创信息：{'已标注' if result['creator_info_found'] else '未检测到'}")
            print(f"  保存目录：{result['output_dir']}")
        else:
            print(f"错误：{result['error']}")

        # 清理临时文件
        tool.cleanup()
    else:
        print(f"测试视频不存在：{test_video}")