"""
PPTReviewerAgent - 演示文稿审核专家

职责：审核演示文稿作品（PPT_TRACK）

官方评分维度（校教发〔2024〕77号文件）：
- 创意（内容创意、视觉设计创新）: 0-25分
- 视觉效果（排版、色彩、图文比例）: 0-25分
- 内容呈现（逻辑结构、信息密度）: 0-25分
- 原创性（原创元素使用）: 0-25分

总分：100分（AI评分 × weight + 评委评分 × weight）

硬性要求检查：
- 页数：至少12页
- 比例：16:9
- 格式：PPTX，无密码保护
- 原创性（不得抄袭）
- 内容健康积极

工具集成（未来实现）：
- PPTStructureAnalyzerTool：幻灯片结构分析
- PPTMetadataTool：元数据检查
- DesignQualityTool：设计质量评估
- OriginalityDetectorTool：原创性检测

作者：AI Agent架构重构项目
创建时间：2026-05-03
"""

import os
import logging
from typing import Dict, Any, Optional, List
from pydantic import BaseModel, Field
from langchain_openai import ChatOpenAI
from langchain_core.messages import HumanMessage, SystemMessage

logger = logging.getLogger(__name__)


class PPTReviewOutput(BaseModel):
    """PPT评审结构化输出（官方评分维度）"""

    # 总分（100分）
    overall_score: float = Field(description="总体评分（0-100分）", ge=0, le=100)

    # 官方评分维度（各25分）
    creativity_score: float = Field(description="创意评分（0-25分）", ge=0, le=25)
    visual_effect_score: float = Field(description="视觉效果评分（0-25分）", ge=0, le=25)
    content_presentation_score: float = Field(description="内容呈现评分（0-25分）", ge=0, le=25)
    originality_score: float = Field(description="原创性评分（0-25分）", ge=0, le=25)

    # 硬性要求合规性
    compliance_check: Dict[str, Any] = Field(description="硬性要求合规性检查结果")

    # 评审意见
    review_summary: str = Field(description="评审总结（200-500字）")
    strengths: List[str] = Field(description="作品亮点（3-5条）")
    weaknesses: List[str] = Field(description="不足之处（2-3条）")
    improvement_suggestions: List[str] = Field(description="改进建议（3-5条）")

    # PPT元数据
    ppt_metadata: Dict[str, Any] = Field(description="PPT元数据")


class PPTReviewerAgent:
    """
    PPT审核专家Agent

    官方评分维度：
    - 创意（内容创意、视觉设计创新）
    - 视觉效果（排版、色彩、图文比例）
    - 内容呈现（逻辑结构、信息密度）
    - 原创性（原创元素使用）

    硬性要求检查：
    - 页数：至少12页
    - 比例：16:9
    - 格式：PPTX，无密码保护
    """

    def __init__(
        self,
        model_name: str = "deepseek-chat",
        api_key: Optional[str] = None,
        base_url: Optional[str] = None
    ):
        """
        初始化PPT审核Agent

        Args:
            model_name: DeepSeek模型名称
            api_key: DeepSeek API密钥
            base_url: DeepSeek API地址
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

        logger.info("PPTReviewerAgent初始化完成")

    def review_ppt(
        self,
        ppt_path: str,
        work_description: str,
        readme_content: Optional[str] = None,
        additional_files: Optional[List[str]] = None
    ) -> PPTReviewOutput:
        """
        完整PPT审核流程

        Args:
            ppt_path: PPT文件路径
            work_description: 作品说明文档内容
            readme_content: README文件内容（可选）
            additional_files: 其他附加文件路径列表

        Returns:
            PPTReviewOutput: 结构化评审报告
        """
        logger.info(f"开始PPT审核：{ppt_path}")

        # ========== 步骤1：PPT元数据提取 ==========
        logger.info("步骤1：PPT元数据提取...")
        ppt_metadata = self._extract_ppt_metadata(ppt_path)

        # ========== 步骤2：硬性要求检查 ==========
        logger.info("步骤2：硬性要求检查...")
        compliance_check = self._check_compliance(ppt_metadata, readme_content)

        # ========== 步骤3：PPT质量分析（基础版） ==========
        logger.info("步骤3：PPT质量分析...")
        visual_effect_score = self._analyze_ppt_quality(ppt_metadata)

        # ========== 步骤4：DeepSeek LLM评审推理 ==========
        logger.info("步骤4：DeepSeek LLM评审推理...")

        # 构建评审prompt
        prompt = self._build_review_prompt(
            ppt_metadata=ppt_metadata,
            compliance_check=compliance_check,
            work_description=work_description,
            readme_content=readme_content,
            visual_effect_score=visual_effect_score
        )

        # 调用LLM
        response = self.llm.invoke([
            SystemMessage(content=self._get_system_prompt()),
            HumanMessage(content=prompt)
        ])

        # 解析LLM输出
        review_result = self._parse_llm_output(
            llm_output=response.content,
            ppt_metadata=ppt_metadata,
            compliance_check=compliance_check,
            visual_effect_score=visual_effect_score
        )

        logger.info(f"PPT审核完成，总分：{review_result.overall_score}")

        return review_result

    def _extract_ppt_metadata(self, ppt_path: str) -> Dict[str, Any]:
        """
        提取PPT元数据（基础版，使用python-pptx）

        Args:
            ppt_path: PPT文件路径

        Returns:
            dict: PPT元数据
        """
        metadata = {
            "ppt_path": ppt_path,
            "file_size": 0,
            "slide_count": 0,
            "format": "Unknown",
            "has_password": False,
            "aspect_ratio": "Unknown",
            "slide_dimensions": None
        }

        try:
            # 检查文件是否存在
            if not os.path.exists(ppt_path):
                logger.error(f"PPT文件不存在：{ppt_path}")
                return metadata

            # 文件大小
            metadata["file_size"] = os.path.getsize(ppt_path)

            # 格式检查（文件扩展名）
            file_ext = os.path.splitext(ppt_path)[1].lower()
            if file_ext == ".pptx":
                metadata["format"] = "PPTX"
            elif file_ext == ".ppt":
                metadata["format"] = "PPT"
            else:
                metadata["format"] = "Unknown"

            # 尝试使用python-pptx提取详细信息
            try:
                from pptx import Presentation

                prs = Presentation(ppt_path)

                # 幻灯片数量
                metadata["slide_count"] = len(prs.slides)

                # 幻灯片尺寸（判断16:9比例）
                slide_width = prs.slide_width
                slide_height = prs.slide_height

                # EMUs (English Metric Units) to inches conversion
                # 1 inch = 914400 EMUs
                width_inches = slide_width / 914400
                height_inches = slide_height / 914400

                # 计算比例
                if height_inches > 0:
                    ratio = width_inches / height_inches
                    if abs(ratio - 16/9) < 0.1:  # 允许10%误差
                        metadata["aspect_ratio"] = "16:9"
                    elif abs(ratio - 4/3) < 0.1:
                        metadata["aspect_ratio"] = "4:3"
                    else:
                        metadata["aspect_ratio"] = f"{ratio:.2f}:1"

                metadata["slide_dimensions"] = {
                    "width_inches": round(width_inches, 2),
                    "height_inches": round(height_inches, 2),
                    "width_emus": slide_width,
                    "height_emus": slide_height
                }

                # 尝试检测密码保护（通过尝试打开判断）
                # 如果成功打开，说明无密码保护
                metadata["has_password"] = False

            except ImportError:
                logger.warning("python-pptx未安装，无法提取详细元数据")
                # 基础检查：至少文件存在且格式正确
                metadata["slide_count"] = 0  # 未知

            except Exception as e:
                logger.error(f"PPT元数据提取失败：{e}")
                # 可能是密码保护导致无法打开
                if "password" in str(e).lower() or "encrypted" in str(e).lower():
                    metadata["has_password"] = True

        except Exception as e:
            logger.error(f"PPT文件读取失败：{e}")

        return metadata

    def _check_compliance(
        self,
        ppt_metadata: Dict[str, Any],
        readme_content: Optional[str]
    ) -> Dict[str, Any]:
        """
        检查硬性要求合规性

        检查项：
        1. 页数：至少12页
        2. 比例：16:9
        3. 格式：PPTX，无密码保护
        4. 文件大小合理性

        Args:
            ppt_metadata: PPT元数据
            readme_content: README文件内容

        Returns:
            dict: 合规性检查结果
        """
        compliance = {}

        # 1. 页数要求（至少12页）
        compliance["slide_count_valid"] = ppt_metadata["slide_count"] >= 12
        compliance["slide_count_message"] = (
            f"幻灯片数量{ppt_metadata['slide_count']}页，符合要求（≥12页）"
            if compliance["slide_count_valid"]
            else f"幻灯片数量{ppt_metadata['slide_count']}页，不符合要求（应≥12页）"
        )

        # 2. 比例要求（16:9）
        compliance["ratio_valid"] = ppt_metadata["aspect_ratio"] == "16:9"
        compliance["ratio_message"] = (
            f"幻灯片比例{ppt_metadata['aspect_ratio']},符合要求"
            if compliance["ratio_valid"]
            else f"幻灯片比例{ppt_metadata['aspect_ratio']},不符合要求（应为16:9）"
        )

        # 3. 格式要求（PPTX）
        compliance["format_valid"] = ppt_metadata["format"] == "PPTX"
        compliance["format_message"] = (
            f"文件格式{ppt_metadata['format']},符合要求"
            if compliance["format_valid"]
            else f"文件格式{ppt_metadata['format']},不符合要求（应为PPTX）"
        )

        # 4. 密码保护检查（无密码）
        compliance["password_valid"] = not ppt_metadata["has_password"]
        compliance["password_message"] = (
            "文件无密码保护，符合要求"
            if compliance["password_valid"]
            else "文件有密码保护，不符合要求"
        )

        # 5. 文件大小合理性（≤300MB）
        file_size_mb = ppt_metadata["file_size"] / (1024 * 1024)
        compliance["size_valid"] = file_size_mb <= 300
        compliance["size_message"] = (
            f"文件大小{file_size_mb:.2f}MB，符合要求（≤300MB）"
            if compliance["size_valid"]
            else f"文件大小{file_size_mb:.2f}MB，不符合要求（应≤300MB）"
        )

        # 6. 说明文档完整性
        compliance["readme_exists"] = readme_content is not None and len(readme_content) > 100
        compliance["readme_message"] = (
            "说明文档完整，符合要求"
            if compliance["readme_exists"]
            else "说明文档不完整或不存在"
        )

        # 7. 总体合规性
        compliance["all_valid"] = all([
            compliance["slide_count_valid"],
            compliance["ratio_valid"],
            compliance["format_valid"],
            compliance["password_valid"],
            compliance["size_valid"],
            compliance["readme_exists"]
        ])

        compliance["overall_message"] = (
            "PPT作品符合所有硬性要求"
            if compliance["all_valid"]
            else "PPT作品不符合部分硬性要求"
        )

        return compliance

    def _analyze_ppt_quality(self, ppt_metadata: Dict[str, Any]) -> float:
        """
        分析PPT质量（基础版，评分0-25）

        Args:
            ppt_metadata: PPT元数据

        Returns:
            float: PPT质量评分
        """
        # 基础分：PPT存在（10分）
        base_score = 10 if ppt_metadata["file_size"] > 0 else 0

        # 幻灯片数量加分（最多5分）
        slide_score = min(ppt_metadata["slide_count"] / 20 * 5, 5)

        # 文件大小加分（最多5分）
        file_size_mb = ppt_metadata["file_size"] / (1024 * 1024)
        size_score = min(file_size_mb / 50 * 5, 5)

        # 格式加分（PPTX格式5分）
        format_score = 5 if ppt_metadata["format"] == "PPTX" else 0

        return round(base_score + slide_score + size_score + format_score, 2)

    def _get_system_prompt(self) -> str:
        """系统提示词：定义PPT评审角色和标准"""
        return """你是一位专业的演示文稿评审专家，负责审核大学生计算机设计大赛的演示文稿作品。

评审标准（根据校教发〔2024〕77号文件）：

【评分维度】（每个维度0-25分，总分100分）：
1. 创意（25分）：
   - 内容创意（主题新颖度、观点独特性）
   - 视觉设计创新（版式创新、配色创新）
   - 动画效果创新（过渡动画、交互效果）

2. 视觉效果（25分）：
   - 排版质量（布局合理性、层次清晰度）
   - 色彩搭配（色彩协调性、视觉美感）
   - 图文比例（图片与文字平衡、信息密度）

3. 内容呈现（25分）：
   - 逻辑结构（章节划分、内容连贯性）
   - 信息密度（每页信息量适中、重点突出）
   - 文字质量（语言表达、错别字检查）

4. 原创性（25分）：
   - 原创元素（原创图片、原创图表、原创动画）
   - 内容原创（观点原创、数据原创）
   - 设计原创（模板原创、配色原创）

【硬性要求检查】：
- 页数：至少12页
- 比例：16:9
- 格式：PPTX，无密码保护
- 原创性（不得抄袭）
- 内容健康积极

评审原则：
1. 严格遵循评分标准，客观公正
2. 硬性要求不合规项需在评审意见中明确指出
3. 既肯定作品亮点，也指出不足之处
4. 提供具体、可操作的改进建议

输出格式：
请严格按照JSON格式输出，包含以下字段：
{
  "overall_score": 总分（0-100）,
  "creativity_score": 创意评分（0-25）,
  "visual_effect_score": 视觉效果评分（0-25）,
  "content_presentation_score": 内容呈现评分（0-25）,
  "originality_score": 原创性评分（0-25）,
  "review_summary": "评审总结（200-500字）",
  "strengths": ["亮点1", "亮点2", ...],
  "weaknesses": ["不足1", "不足2", ...],
  "improvement_suggestions": ["建议1", "建议2", ...]
}"""

    def _build_review_prompt(
        self,
        ppt_metadata: Dict[str, Any],
        compliance_check: Dict[str, Any],
        work_description: str,
        readme_content: Optional[str],
        visual_effect_score: float
    ) -> str:
        """构建PPT评审提示词"""

        compliance_status = "符合所有硬性要求" if compliance_check["all_valid"] else "部分硬性要求不合规"

        prompt = f"""请评审以下演示文稿作品：

【PPT元数据】：
- 文件路径：{ppt_metadata['ppt_path']}
- 文件格式：{ppt_metadata['format']}
- 幻灯片数量：{ppt_metadata['slide_count']}页
- 幻灯片比例：{ppt_metadata['aspect_ratio']}
- 幻灯片尺寸：{ppt_metadata.get('slide_dimensions', '未知')}
- 文件大小：{ppt_metadata['file_size'] / (1024*1024):.2f}MB
- 密码保护：{'有' if ppt_metadata['has_password'] else '无'}

【硬性要求合规性】：
状态：{compliance_status}
检查详情：
- 幻灯片数量：{'✓合规' if compliance_check['slide_count_valid'] else '✗不合规'}（{compliance_check['slide_count_message']}）
- 幻灯片比例：{'✓合规' if compliance_check['ratio_valid'] else '✗不合规'}（{compliance_check['ratio_message']}）
- 文件格式：{'✓合规' if compliance_check['format_valid'] else '✗不合规'}（{compliance_check['format_message']}）
- 密码保护：{'✓合规' if compliance_check['password_valid'] else '✗不合规'}（{compliance_check['password_message']}）
- 文件大小：{'✓合规' if compliance_check['size_valid'] else '✗不合规'}（{compliance_check['size_message']}）
- 说明文档：{'✓合规' if compliance_check['readme_exists'] else '✗不合规'}（{compliance_check['readme_message']}）

【视觉效果初步评分】：{visual_effect_score}/25分（基于幻灯片数量、文件大小等）

【作品说明】：
{work_description}

【README文档】：
{readme_content if readme_content else '未提供README文档'}

请根据评审标准，对演示文稿作品进行全面评价。请以JSON格式输出评审结果。"""

        return prompt

    def _parse_llm_output(
        self,
        llm_output: str,
        ppt_metadata: Dict[str, Any],
        compliance_check: Dict[str, Any],
        visual_effect_score: float
    ) -> PPTReviewOutput:
        """解析LLM输出为结构化评审结果"""
        import json

        try:
            # 提取JSON部分
            json_str = llm_output
            if "```json" in llm_output:
                json_str = llm_output.split("```json")[1].split("```")[0].strip()
            elif "```" in llm_output:
                json_str = llm_output.split("```")[1].split("```")[0].strip()

            result_dict = json.loads(json_str)

            # 构建PPTReviewOutput对象
            review_output = PPTReviewOutput(
                overall_score=result_dict.get("overall_score", visual_effect_score + 50),
                creativity_score=result_dict.get("creativity_score", 15),
                visual_effect_score=result_dict.get("visual_effect_score", visual_effect_score),
                content_presentation_score=result_dict.get("content_presentation_score", 15),
                originality_score=result_dict.get("originality_score", 15),
                compliance_check=compliance_check,
                review_summary=result_dict.get("review_summary", ""),
                strengths=result_dict.get("strengths", []),
                weaknesses=result_dict.get("weaknesses", []),
                improvement_suggestions=result_dict.get("improvement_suggestions", []),
                ppt_metadata=ppt_metadata
            )

            return review_output

        except json.JSONDecodeError as e:
            logger.error(f"LLM输出JSON解析失败：{e}")

            # 返回默认结果
            return PPTReviewOutput(
                overall_score=visual_effect_score + 50,
                creativity_score=15,
                visual_effect_score=visual_effect_score,
                content_presentation_score=15,
                originality_score=15,
                compliance_check=compliance_check,
                review_summary="评审结果解析失败",
                strengths=["评审解析失败"],
                weaknesses=["LLM输出格式错误"],
                improvement_suggestions=["请检查LLM输出格式"],
                ppt_metadata=ppt_metadata
            )


# 使用示例
if __name__ == "__main__":
    import os

    os.environ["DEEPSEEK_API_KEY"] = "sk-325ae1ccf357480ab353a41e8b26ee32"

    agent = PPTReviewerAgent()

    # 测试PPT路径（需要提供实际PPT）
    test_ppt = "test_ppt/demo.pptx"
    readme = """
    # 作品说明
    本演示文稿讲解人工智能基础知识。
    内容包括：AI定义、发展历程、应用场景等。
    共15页，采用16:9比例，原创设计。
    """

    if os.path.exists(test_ppt):
        print("="*80)
        print("PPTReviewerAgent完整测试")
        print("="*80)

        result = agent.review_ppt(
            ppt_path=test_ppt,
            work_description="人工智能基础演示文稿",
            readme_content=readme
        )

        print(f"\n总分：{result.overall_score}")
        print(f"创意：{result.creativity_score}")
        print(f"视觉效果：{result.visual_effect_score}")
        print(f"内容呈现：{result.content_presentation_score}")
        print(f"原创性：{result.originality_score}")

        print("="*80)
    else:
        print(f"测试PPT不存在：{test_ppt}")