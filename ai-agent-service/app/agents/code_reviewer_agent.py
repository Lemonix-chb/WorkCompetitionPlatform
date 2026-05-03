"""
CodeReviewerAgent - 代码作品审核专家

职责：审核程序设计作品（CODE_TRACK）

官方评分维度（校教发〔2024〕77号文件）：
- 创新性（技术方案创新程度）: 0-25分
- 实用性（功能完整性、解决实际问题能力）: 0-25分
- 用户体验（界面设计、交互质量）: 0-25分
- 代码质量（规范、可读性、注释完整性）: 0-25分

总分：100分（AI评分 × weight + 评委评分 × weight）

硬性要求检查：
- 源代码可运行（编译/运行测试）
- 原创性（代码查重，严禁抄袭）
- 说明文档完整性（README、技术文档）
- 代码规范（符合语言标准）

工具集成（未来实现）：
- JPlagTool：代码相似度检测
- CodeExecutionTool：代码运行测试
- CheckstyleTool：代码规范检查

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


class CodeReviewOutput(BaseModel):
    """代码评审结构化输出（官方评分维度）"""

    # 总分（100分）
    overall_score: float = Field(description="总体评分（0-100分）", ge=0, le=100)

    # 官方评分维度（各25分）
    innovation_score: float = Field(description="创新性评分（0-25分）", ge=0, le=25)
    practicality_score: float = Field(description="实用性评分（0-25分）", ge=0, le=25)
    user_experience_score: float = Field(description="用户体验评分（0-25分）", ge=0, le=25)
    code_quality_score: float = Field(description="代码质量评分（0-25分）", ge=0, le=25)

    # 硬性要求合规性
    compliance_check: Dict[str, Any] = Field(description="硬性要求合规性检查结果")

    # 评审意见
    review_summary: str = Field(description="评审总结（200-500字）")
    strengths: List[str] = Field(description="作品亮点（3-5条）")
    weaknesses: List[str] = Field(description="不足之处（2-3条）")
    improvement_suggestions: List[str] = Field(description="改进建议（3-5条）")

    # 代码元数据
    code_metadata: Dict[str, Any] = Field(description="代码元数据")


class CodeReviewerAgent:
    """
    代码审核专家Agent

    官方评分维度：
    - 创新性（技术方案创新程度）
    - 实用性（功能完整性、解决实际问题能力）
    - 用户体验（界面设计、交互质量）
    - 代码质量（规范、可读性、注释完整性）

    硬性要求检查：
    - 源代码可运行
    - 原创性（代码查重）
    - 说明文档完整性
    """

    def __init__(
        self,
        model_name: str = "deepseek-chat",
        api_key: Optional[str] = None,
        base_url: Optional[str] = None
    ):
        """
        初始化代码审核Agent

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

        logger.info("CodeReviewerAgent初始化完成")

    def review_code(
        self,
        code_path: str,
        language: str,
        work_description: str,
        readme_content: Optional[str] = None,
        additional_files: Optional[List[str]] = None
    ) -> CodeReviewOutput:
        """
        完整代码审核流程

        Args:
            code_path: 代码文件/目录路径
            language: 编程语言（Python/C/C++/Java等）
            work_description: 作品说明文档内容
            readme_content: README文件内容（可选）
            additional_files: 其他附加文件路径列表

        Returns:
            CodeReviewOutput: 结构化评审报告
        """
        logger.info(f"开始代码审核：{code_path}")

        # ========== 步骤1：代码元数据提取 ==========
        logger.info("步骤1：代码元数据提取...")
        code_metadata = self._extract_code_metadata(code_path, language)

        # ========== 步骤2：硬性要求检查 ==========
        logger.info("步骤2：硬性要求检查...")
        compliance_check = self._check_compliance(code_metadata, readme_content)

        # ========== 步骤3：代码质量分析（基础版） ==========
        logger.info("步骤3：代码质量分析...")
        code_quality_score = self._analyze_code_quality(code_metadata)

        # ========== 步骤4：DeepSeek LLM评审推理 ==========
        logger.info("步骤4：DeepSeek LLM评审推理...")

        # 构建评审prompt
        prompt = self._build_review_prompt(
            code_metadata=code_metadata,
            compliance_check=compliance_check,
            work_description=work_description,
            readme_content=readme_content,
            code_quality_score=code_quality_score
        )

        # 调用LLM
        response = self.llm.invoke([
            SystemMessage(content=self._get_system_prompt()),
            HumanMessage(content=prompt)
        ])

        # 解析LLM输出
        review_result = self._parse_llm_output(
            llm_output=response.content,
            code_metadata=code_metadata,
            compliance_check=compliance_check,
            code_quality_score=code_quality_score
        )

        logger.info(f"代码审核完成，总分：{review_result.overall_score}")

        return review_result

    def _extract_code_metadata(self, code_path: str, language: str) -> Dict[str, Any]:
        """
        提取代码元数据

        Args:
            code_path: 代码路径（文件或目录）
            language: 编程语言

        Returns:
            dict: 代码元数据
        """
        metadata = {
            "code_path": code_path,
            "language": language,
            "file_count": 0,
            "total_lines": 0,
            "comment_lines": 0,
            "code_files": [],
            "main_file": None
        }

        try:
            if os.path.isfile(code_path):
                # 单文件场景
                metadata["file_count"] = 1
                metadata["code_files"] = [code_path]

                # 统计代码行数
                with open(code_path, 'r', encoding='utf-8', errors='ignore') as f:
                    lines = f.readlines()
                    metadata["total_lines"] = len(lines)

                    # 统计注释行数（简单统计）
                    if language.lower() == "python":
                        comment_lines = [l for l in lines if l.strip().startswith('#')]
                        metadata["comment_lines"] = len(comment_lines)
                    elif language.lower() in ["java", "c", "cpp"]:
                        comment_lines = [l for l in lines if l.strip().startswith('//') or l.strip().startswith('/*')]
                        metadata["comment_lines"] = len(comment_lines)

            elif os.path.isdir(code_path):
                # 目录场景：统计所有代码文件
                code_extensions = {
                    "python": [".py"],
                    "java": [".java"],
                    "c": [".c", ".h"],
                    "cpp": [".cpp", ".hpp", ".cxx"]
                }

                extensions = code_extensions.get(language.lower(), [])

                for root, dirs, files in os.walk(code_path):
                    for file in files:
                        if any(file.endswith(ext) for ext in extensions):
                            file_path = os.path.join(root, file)
                            metadata["code_files"].append(file_path)
                            metadata["file_count"] += 1

                            # 统计代码行数
                            try:
                                with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                                    lines = f.readlines()
                                    metadata["total_lines"] += len(lines)
                            except Exception as e:
                                logger.warning(f"无法读取文件：{file_path}")

                # 尝试找到主文件（main.py、Main.java等）
                main_file_patterns = {
                    "python": ["main.py", "app.py", "run.py"],
                    "java": ["Main.java", "Application.java"],
                    "c": ["main.c"],
                    "cpp": ["main.cpp"]
                }

                patterns = main_file_patterns.get(language.lower(), [])
                for pattern in patterns:
                    for file in metadata["code_files"]:
                        if os.path.basename(file) == pattern:
                            metadata["main_file"] = file
                            break

        except Exception as e:
            logger.error(f"代码元数据提取失败：{e}")

        return metadata

    def _check_compliance(
        self,
        code_metadata: Dict[str, Any],
        readme_content: Optional[str]
    ) -> Dict[str, Any]:
        """
        检查硬性要求合规性

        检查项：
        1. 源代码文件存在性
        2. 说明文档完整性（README存在）
        3. 代码文件数量合理性（至少有代码）
        4. 代码行数合理性（至少100行）

        Args:
            code_metadata: 代码元数据
            readme_content: README文件内容

        Returns:
            dict: 合规性检查结果
        """
        compliance = {}

        # 1. 源代码文件存在性
        compliance["code_exists"] = code_metadata["file_count"] > 0
        compliance["code_exists_message"] = (
            f"源代码文件{code_metadata['file_count']}个，符合要求"
            if compliance["code_exists"]
            else "源代码文件不存在"
        )

        # 2. 说明文档完整性
        compliance["readme_exists"] = readme_content is not None and len(readme_content) > 100
        compliance["readme_message"] = (
            "说明文档完整，符合要求"
            if compliance["readme_exists"]
            else "说明文档不完整或不存在"
        )

        # 3. 代码文件数量合理性
        compliance["file_count_valid"] = code_metadata["file_count"] >= 1
        compliance["file_count_message"] = (
            f"代码文件数量{code_metadata['file_count']}个，符合要求"
            if compliance["file_count_valid"]
            else "代码文件数量不足"
        )

        # 4. 代码行数合理性
        compliance["line_count_valid"] = code_metadata["total_lines"] >= 100
        compliance["line_count_message"] = (
            f"代码行数{code_metadata['total_lines']}行，符合要求（≥100行）"
            if compliance["line_count_valid"]
            else f"代码行数{code_metadata['total_lines']}行，不符合要求（应≥100行）"
        )

        # 5. 总体合规性
        compliance["all_valid"] = all([
            compliance["code_exists"],
            compliance["readme_exists"],
            compliance["file_count_valid"],
            compliance["line_count_valid"]
        ])

        compliance["overall_message"] = (
            "代码作品符合所有硬性要求"
            if compliance["all_valid"]
            else "代码作品不符合部分硬性要求"
        )

        return compliance

    def _analyze_code_quality(self, code_metadata: Dict[str, Any]) -> float:
        """
        分析代码质量（基础版，评分0-25）

        Args:
            code_metadata: 代码元数据

        Returns:
            float: 代码质量评分
        """
        # 基础分：代码存在（10分）
        base_score = 10 if code_metadata["file_count"] > 0 else 0

        # 代码行数加分（最多5分）
        line_score = min(code_metadata["total_lines"] / 200, 5)

        # 注释比例加分（最多5分）
        if code_metadata["total_lines"] > 0:
            comment_ratio = code_metadata["comment_lines"] / code_metadata["total_lines"]
            comment_score = min(comment_ratio * 20, 5)
        else:
            comment_score = 0

        # 文件数量加分（最多5分）
        file_score = min(code_metadata["file_count"] * 0.5, 5)

        return round(base_score + line_score + comment_score + file_score, 2)

    def _get_system_prompt(self) -> str:
        """系统提示词：定义代码评审角色和标准"""
        return """你是一位专业的代码作品评审专家，负责审核大学生计算机设计大赛的程序设计作品。

评审标准（根据校教发〔2024〕77号文件）：

【评分维度】（每个维度0-25分，总分100分）：
1. 创新性（25分）：
   - 技术方案创新程度（是否采用新技术、新算法）
   - 问题解决思路新颖度
   - 技术栈选型合理性

2. 实用性（25分）：
   - 功能完整性（核心功能是否完整实现）
   - 解决实际问题能力（是否有实际应用场景）
   - 系统稳定性（是否能正常运行）

3. 用户体验（25分）：
   - 界面设计（UI美观度、交互逻辑）
   - 交互质量（操作流畅性、响应速度）
   - 易用性（用户学习成本、操作指南）

4. 代码质量（25分）：
   - 代码规范（是否符合语言标准）
   - 可读性（命名规范、代码结构）
   - 注释完整性（关键逻辑是否有注释）
   - 文档完整性（README、技术文档）

【硬性要求检查】：
- 源代码可运行（编译/运行测试）
- 原创性（代码查重，严禁抄袭）
- 说明文档完整性（README、技术文档）
- 代码规范（符合语言标准）

评审原则：
1. 严格遵循评分标准，客观公正
2. 硬性要求不合规项需在评审意见中明确指出
3. 既肯定作品亮点，也指出不足之处
4. 提供具体、可操作的改进建议

输出格式：
请严格按照JSON格式输出，包含以下字段：
{
  "overall_score": 总分（0-100）,
  "innovation_score": 创新性评分（0-25）,
  "practicality_score": 实用性评分（0-25）,
  "user_experience_score": 用户体验评分（0-25）,
  "code_quality_score": 代码质量评分（0-25）,
  "review_summary": "评审总结（200-500字）",
  "strengths": ["亮点1", "亮点2", ...],
  "weaknesses": ["不足1", "不足2", ...],
  "improvement_suggestions": ["建议1", "建议2", ...]
}"""

    def _build_review_prompt(
        self,
        code_metadata: Dict[str, Any],
        compliance_check: Dict[str, Any],
        work_description: str,
        readme_content: Optional[str],
        code_quality_score: float
    ) -> str:
        """构建代码评审提示词"""

        compliance_status = "符合所有硬性要求" if compliance_check["all_valid"] else "部分硬性要求不合规"

        prompt = f"""请评审以下代码作品：

【代码元数据】：
- 代码路径：{code_metadata['code_path']}
- 编程语言：{code_metadata['language']}
- 文件数量：{code_metadata['file_count']}个
- 总代码行数：{code_metadata['total_lines']}行
- 注释行数：{code_metadata['comment_lines']}行
- 主文件：{code_metadata.get('main_file', '未找到')}

【硬性要求合规性】：
状态：{compliance_status}
检查详情：
- 源代码存在性：{'✓合规' if compliance_check['code_exists'] else '✗不合规'}
- 说明文档完整性：{'✓合规' if compliance_check['readme_exists'] else '✗不合规'}
- 代码文件数量：{'✓合规' if compliance_check['file_count_valid'] else '✗不合规'}
- 代码行数要求：{'✓合规' if compliance_check['line_count_valid'] else '✗不合规'}

【代码质量初步评分】：{code_quality_score}/25分（基于代码行数、注释比例等）

【作品说明】：
{work_description}

【README文档】：
{readme_content if readme_content else '未提供README文档'}

请根据评审标准，对代码作品进行全面评价。请以JSON格式输出评审结果。"""

        return prompt

    def _parse_llm_output(
        self,
        llm_output: str,
        code_metadata: Dict[str, Any],
        compliance_check: Dict[str, Any],
        code_quality_score: float
    ) -> CodeReviewOutput:
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

            # 构建CodeReviewOutput对象
            review_output = CodeReviewOutput(
                overall_score=result_dict.get("overall_score", code_quality_score + 50),
                innovation_score=result_dict.get("innovation_score", 15),
                practicality_score=result_dict.get("practicality_score", 15),
                user_experience_score=result_dict.get("user_experience_score", 15),
                code_quality_score=result_dict.get("code_quality_score", code_quality_score),
                compliance_check=compliance_check,
                review_summary=result_dict.get("review_summary", ""),
                strengths=result_dict.get("strengths", []),
                weaknesses=result_dict.get("weaknesses", []),
                improvement_suggestions=result_dict.get("improvement_suggestions", []),
                code_metadata=code_metadata
            )

            return review_output

        except json.JSONDecodeError as e:
            logger.error(f"LLM输出JSON解析失败：{e}")

            # 返回默认结果
            return CodeReviewOutput(
                overall_score=code_quality_score + 50,
                innovation_score=15,
                practicality_score=15,
                user_experience_score=15,
                code_quality_score=code_quality_score,
                compliance_check=compliance_check,
                review_summary="评审结果解析失败",
                strengths=["评审解析失败"],
                weaknesses=["LLM输出格式错误"],
                improvement_suggestions=["请检查LLM输出格式"],
                code_metadata=code_metadata
            )


# 使用示例
if __name__ == "__main__":
    import os

    os.environ["DEEPSEEK_API_KEY"] = "sk-325ae1ccf357480ab353a41e8b26ee32"

    agent = CodeReviewerAgent()

    # 测试代码路径（需要提供实际代码）
    test_code = "test_code/main.py"
    readme = """
    # 作品说明
    本项目是一个Python爬虫程序，用于抓取网页数据。
    功能完整，代码规范。
    """

    if os.path.exists(test_code):
        print("="*80)
        print("CodeReviewerAgent完整测试")
        print("="*80)

        result = agent.review_code(
            code_path=test_code,
            language="python",
            work_description="Python爬虫程序",
            readme_content=readme
        )

        print(f"\n总分：{result.overall_score}")
        print(f"创新性：{result.innovation_score}")
        print(f"实用性：{result.practicality_score}")
        print(f"用户体验：{result.user_experience_score}")
        print(f"代码质量：{result.code_quality_score}")

        print("="*80)
    else:
        print(f"测试代码不存在：{test_code}")