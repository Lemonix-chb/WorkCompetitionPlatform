"""
OCRTool - 字幕识别和主创信息检测工具

功能：
1. 关键帧OCR文字识别（使用pytesseract）
2. 字幕检测（官方强制要求：必须有字幕）
3. 片头片尾主创信息署名检测
4. 字幕质量评估（清晰度、同步度）

输出：
- subtitle_found: 是否检测到字幕
- subtitles: 提取的字幕文本列表
- creator_info_found: 是否检测到主创信息署名
- subtitle_quality_score: 字幕质量评分（0-25）

作者：AI Agent架构重构项目
创建时间：2026-05-03
"""

import os
import logging
from typing import Dict, Any, List, Optional
from langchain.tools import BaseTool
from pydantic import Field

# OCR依赖（需要安装）
try:
    import pytesseract
    from PIL import Image
    TESSERACT_AVAILABLE = True
except ImportError:
    TESSERACT_AVAILABLE = False
    logging.warning("pytesseract未安装，OCR功能不可用。请运行：pip install pytesseract pillow")

logger = logging.getLogger(__name__)


class OCRTool(BaseTool):
    """
    OCR字幕识别工具（LangChain BaseTool实现）

    官方硬性要求：数媒动漫与短视频作品必须有字幕
    """

    name: str = "ocr_subtitle"
    description: str = """
    识别视频关键帧中的字幕文字并检测主创信息署名。
    输入：关键帧图片路径列表
    输出：字幕检测结果、字幕文本、主创信息检测结果

    检查项：
    - 字幕是否存在（官方强制要求）
    - 字幕清晰度和可读性
    - 片头片尾主创信息署名
    """

    # Tesseract配置
    tesseract_path: Optional[str] = Field(
        default=None,
        description="Tesseract OCR可执行文件路径（Windows需要配置）"
    )

    # OCR语言配置
    ocr_language: str = Field(
        default="chi_sim+eng",  # 中文简体+英文
        description="OCR识别语言（chi_sim=中文简体，eng=英文）"
    )

    def __init__(self, **kwargs):
        super().__init__(**kwargs)

        # 配置Tesseract路径（Windows）
        if self.tesseract_path and TESSERACT_AVAILABLE:
            pytesseract.pytesseract.tesseract_cmd = self.tesseract_path

        # 验证Tesseract是否可用
        if TESSERACT_AVAILABLE:
            try:
                # 测试Tesseract版本
                version = pytesseract.get_tesseract_version()
                logger.info(f"Tesseract OCR可用，版本：{version}")
            except Exception as e:
                logger.warning(f"Tesseract OCR不可用：{e}")

    def _run(self, keyframe_paths: List[str]) -> Dict[str, Any]:
        """
        执行OCR识别

        Args:
            keyframe_paths: 关键帧图片路径列表

        Returns:
            dict: {
                "success": True/False,
                "subtitle_found": True/False,
                "subtitles": [字幕文本列表],
                "creator_info_found": True/False,
                "creator_texts": [主创信息文本列表],
                "subtitle_quality_score": 0-25,
                "ocr_texts": [所有OCR识别文本],
                "error": "错误信息"  # 如果失败
            }
        """
        if not TESSERACT_AVAILABLE:
            return {
                "success": False,
                "error": "pytesseract未安装，OCR功能不可用",
                "subtitle_found": False,
                "subtitles": [],
                "creator_info_found": False
            }

        try:
            if not keyframe_paths:
                return {
                    "success": False,
                    "error": "关键帧路径列表为空",
                    "subtitle_found": False
                }

            # 验证关键帧文件存在
            valid_frames = [p for p in keyframe_paths if os.path.exists(p)]
            if not valid_frames:
                return {
                    "success": False,
                    "error": "所有关键帧文件不存在",
                    "subtitle_found": False
                }

            logger.info(f"开始OCR识别，共{len(valid_frames)}个关键帧")

            # 提取所有OCR文本
            all_ocr_texts = []
            subtitle_texts = []
            creator_texts = []

            for frame_path in valid_frames:
                ocr_result = self._ocr_frame(frame_path)

                if ocr_result["success"]:
                    all_ocr_texts.append(ocr_result["text"])

                    # 检测字幕（通常在画面底部）
                    if ocr_result["is_subtitle"]:
                        subtitle_texts.append(ocr_result["text"])

                    # 检测主创信息（片头片尾）
                    if ocr_result["is_creator_info"]:
                        creator_texts.append(ocr_result["text"])

            # 判断字幕是否存在
            subtitle_found = len(subtitle_texts) > 0

            # 判断主创信息是否存在
            creator_info_found = len(creator_texts) > 0

            # 评估字幕质量
            subtitle_quality_score = self._evaluate_subtitle_quality(
                subtitle_texts,
                subtitle_found
            )

            return {
                "success": True,
                "subtitle_found": subtitle_found,
                "subtitles": subtitle_texts,
                "creator_info_found": creator_info_found,
                "creator_texts": creator_texts,
                "subtitle_quality_score": subtitle_quality_score,
                "ocr_texts": all_ocr_texts,
                "total_frames": len(valid_frames),
                "frames_with_text": len([t for t in all_ocr_texts if t.strip()])
            }

        except Exception as e:
            logger.error(f"OCR识别失败：{e}")
            return {
                "success": False,
                "error": str(e),
                "subtitle_found": False,
                "subtitles": [],
                "creator_info_found": False
            }

    def _ocr_frame(self, frame_path: str) -> Dict[str, Any]:
        """
        对单个关键帧进行OCR识别

        Args:
            frame_path: 关键帧图片路径

        Returns:
            dict: {
                "success": True/False,
                "text": "识别文本",
                "is_subtitle": True/False,
                "is_creator_info": True/False,
                "text_regions": [文本区域列表]
            }
        """
        try:
            # 加载图片
            image = Image.open(frame_path)

            # OCR识别
            ocr_text = pytesseract.image_to_string(
                image,
                lang=self.ocr_language,
                config='--psm 6'  # 单块文本模式
            ).strip()

            # 获取文本区域（检测字幕位置）
            ocr_data = pytesseract.image_to_data(
                image,
                lang=self.ocr_language,
                output_type=pytesseract.Output.DICT
            )

            # 判断是否为字幕（画面底部区域）
            is_subtitle = self._detect_subtitle_region(ocr_data, image.height)

            # 判断是否为主创信息（片头片尾，包含特定关键词）
            filename = os.path.basename(frame_path)
            is_creator_info = False

            if filename.startswith("opening_") or filename.startswith("ending_"):
                is_creator_info = self._detect_creator_info_keywords(ocr_text)

            return {
                "success": True,
                "text": ocr_text,
                "is_subtitle": is_subtitle,
                "is_creator_info": is_creator_info,
                "text_regions": ocr_data.get("text", [])
            }

        except Exception as e:
            logger.error(f"关键帧OCR失败 {frame_path}：{e}")
            return {
                "success": False,
                "text": "",
                "is_subtitle": False,
                "is_creator_info": False
            }

    def _detect_subtitle_region(self, ocr_data: Dict, image_height: int) -> bool:
        """
        检测文本是否在字幕区域（画面底部1/3）

        Args:
            ocr_data: pytesseract输出的OCR数据
            image_height: 图片高度

        Returns:
            bool: 是否在字幕区域
        """
        # 字幕通常在画面底部1/3区域
        subtitle_threshold = image_height * 2 / 3

        # 检查文本块位置
        text_positions = ocr_data.get("top", [])

        if not text_positions:
            return False

        # 如果大部分文本在底部区域，认为是字幕
        bottom_texts = [pos for pos in text_positions if pos > subtitle_threshold]

        return len(bottom_texts) > len(text_positions) * 0.5

    def _detect_creator_info_keywords(self, text: str) -> bool:
        """
        检测主创信息关键词

        主创信息常见关键词：
        - 导演、编剧、制片人、摄影、剪辑
        - 主演、演员、配音
        - 制作团队、出品方
        - 姓名+职务格式

        Args:
            text: OCR识别文本

        Returns:
            bool: 是否检测到主创信息
        """
        creator_keywords = [
            "导演", "编剧", "制片", "摄影", "剪辑", "主演", "演员",
            "配音", "制作", "出品", "团队", "工作室",
            "导演:", "编剧:", "主演:", "制作:"
        ]

        # 检测关键词出现
        keyword_count = sum(1 for kw in creator_keywords if kw in text)

        # 至少出现2个关键词才认为有主创信息
        return keyword_count >= 2

    def _evaluate_subtitle_quality(
        self,
        subtitle_texts: List[str],
        subtitle_found: bool
    ) -> float:
        """
        评估字幕质量（0-25分）

        评分依据：
        - 字幕存在性：必须有字幕（官方强制要求）
        - 字幕数量：字幕覆盖度
        - 字幕清晰度：OCR识别成功率

        Args:
            subtitle_texts: 字幕文本列表
            subtitle_found: 是否检测到字幕

        Returns:
            float: 字幕质量评分（0-25）
        """
        # 如果没有字幕，直接0分（官方强制要求）
        if not subtitle_found:
            return 0

        # 基础分：字幕存在（15分）
        base_score = 15

        # 字幕数量加分（最多5分）
        subtitle_count = len(subtitle_texts)
        count_score = min(subtitle_count * 0.5, 5)

        # 字幕清晰度加分（最多5分）
        # 根据OCR识别文本长度判断清晰度
        avg_text_length = sum(len(t) for t in subtitle_texts) / max(subtitle_count, 1)
        clarity_score = min(avg_text_length / 10, 5)

        return round(base_score + count_score + clarity_score, 2)


# 使用示例
if __name__ == "__main__":
    # 示例配置（Windows需要配置Tesseract路径）
    tool = OCRTool(
        tesseract_path=r"C:\Program Files\Tesseract-OCR\tesseract.exe"
    )

    # 测试关键帧路径列表（需要实际文件）
    test_frames = [
        "test_videos/keyframes/opening_1s.jpg",
        "test_videos/keyframes/middle_30s.jpg",
        "test_videos/keyframes/ending_145s.jpg"
    ]

    # 模拟测试
    if TESSERACT_AVAILABLE:
        print("OCRTool测试：")
        print(f"  - Tesseract路径：{tool.tesseract_path}")
        print(f"  - OCR语言：{tool.ocr_language}")

        # 如果有实际关键帧文件，运行测试
        existing_frames = [f for f in test_frames if os.path.exists(f)]

        if existing_frames:
            result = tool._run(existing_frames)

            if result["success"]:
                print(f"\nOCR识别成功：")
                print(f"  - 总帧数：{result['total_frames']}")
                print(f"  - 字幕检测：{'有字幕' if result['subtitle_found'] else '无字幕'}")
                print(f"  - 主创信息：{'有署名' if result['creator_info_found'] else '未检测到'}")
                print(f"  - 字幕质量评分：{result['subtitle_quality_score']}")
            else:
                print(f"\nOCR识别失败：{result['error']}")
        else:
            print(f"\n提示：需要关键帧图片文件才能测试OCR功能")
            print(f"  请先运行KeyFrameExtractorTool生成关键帧")
    else:
        print("[WARNING] pytesseract未安装，OCR功能不可用")
        print("安装方法：")
        print("  pip install pytesseract pillow")
        print("  下载Tesseract：https://github.com/UB-Mannheim/tesseract/wiki")