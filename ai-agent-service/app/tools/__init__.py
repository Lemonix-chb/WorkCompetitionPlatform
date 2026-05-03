# Tools Package
# 工具目录：包含视频分析、代码检查、PPT分析等工具

# Video Analysis Tools (已实现)
from .ffmpeg_tool import FFmpegTool
from .keyframe_tool import KeyFrameExtractorTool
from .ocr_tool import OCRTool
from .whisper_tool import WhisperTool

# Code Analysis Tools (待实现)
# from .jplag_tool import JPlagTool

# PPT Analysis Tools (待实现)
# from .ppt_structure_tool import PPTStructureAnalyzerTool

__all__ = [
    "FFmpegTool",
    "KeyFrameExtractorTool",
    "OCRTool",
    "WhisperTool",
    # "JPlagTool",  # 待实现
    # "PPTStructureAnalyzerTool",  # 待实现
]