"""
CodeReviewerAgent - 代码作品评审专家

职责：评审程序设计作品（CODE_TRACK）

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
"""

import os
import re
import json
import shutil
import zipfile
import tempfile
import logging
from typing import Dict, Any, Optional, List
from collections import Counter
from pydantic import BaseModel, Field
from langchain_openai import ChatOpenAI
from langchain_core.messages import HumanMessage, SystemMessage
from app.utils.llm_utils import extract_json_from_llm_output

logger = logging.getLogger(__name__)


class CodeReviewOutput(BaseModel):
    """代码评审结构化输出（官方评分维度）"""

    # 总分（100分）
    overall_score: float = Field(description="总体评分（0-100分）", ge=0, le=100)

    # 官方评分维度（各25分）
    innovation_score: float = Field(description="创新性评分（0-20分）", ge=0, le=20)
    practicality_score: float = Field(description="实用性评分（0-20分）", ge=0, le=20)
    user_experience_score: float = Field(description="用户体验评分（0-20分）", ge=0, le=20)
    code_quality_score: float = Field(description="代码质量评分（0-20分）", ge=0, le=20)
    documentation_score: float = Field(description="文档完整性评分（0-20分）", ge=0, le=20)

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
    代码评审专家Agent

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
        model_name: str = "deepseek-v4-pro",
        api_key: Optional[str] = None,
        base_url: Optional[str] = None
    ):
        """
        初始化代码评审Agent

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
        完整代码评审流程

        Args:
            code_path: 代码文件/目录路径
            language: 编程语言（Python/C/C++/Java等）
            work_description: 作品说明文档内容
            readme_content: README文件内容（可选）
            additional_files: 其他附加文件路径列表

        Returns:
            CodeReviewOutput: 结构化评审报告
        """
        logger.info(f"开始代码评审：{code_path}, 语言：{language}")

        # ========== 步骤0：处理压缩包 ==========
        extracted_dir = None
        actual_code_path = code_path

        if code_path.endswith('.zip') or code_path.endswith('.rar'):
            logger.info("步骤0：解压压缩包...")
            extracted_dir = self._extract_zip_file(code_path)
            if extracted_dir:
                actual_code_path = extracted_dir
                logger.info(f"压缩包已解压到：{extracted_dir}")
            else:
                logger.warning("压缩包解压失败，将使用原始路径")

        # ========== 步骤1：代码元数据提取 ==========
        logger.info("步骤1：代码元数据提取...")
        code_metadata = self._extract_code_metadata(actual_code_path, language)

        # ========== 步骤1.5：项目结构扫描 ==========
        logger.info("步骤1.5：项目结构扫描...")
        project_structure = self._scan_project_structure(actual_code_path, language)

        # ========== 步骤2：硬性要求检查 ==========
        logger.info("步骤2：硬性要求检查...")
        compliance_check = self._check_compliance(code_metadata, readme_content, work_description)

        # ========== 步骤3.5：读取实际代码内容（提前读取供分析使用） ==========
        logger.info("步骤3.5：读取实际代码内容...")
        code_content = self._read_code_content(code_metadata)

        # ========== 步骤3：多维度静态分析 ==========
        logger.info("步骤3：多维度静态分析...")

        code_quality_evidence = self._analyze_code_quality_enhanced(
            code_metadata, code_content, language
        )
        # 用项目结构补充依赖管理信息
        code_quality_evidence["has_dependency_management"] = (
            len(project_structure.get("dependency_files", [])) > 0
        )

        innovation_evidence = self._analyze_innovation_evidence(
            project_structure, code_metadata, code_content
        )

        practicality_evidence = self._analyze_practicality_evidence(
            project_structure, code_metadata, code_content
        )

        ux_evidence = self._analyze_ux_evidence(
            project_structure, code_metadata, code_content
        )

        code_quality_score = code_quality_evidence.get("code_quality_score_hint", 8)

        # ========== 步骤4：DeepSeek LLM评审推理 ==========
        logger.info("步骤4：DeepSeek LLM评审推理...")

        # 构建评审prompt
        prompt = self._build_review_prompt(
            code_metadata=code_metadata,
            compliance_check=compliance_check,
            work_description=work_description,
            readme_content=readme_content,
            code_quality_score=code_quality_score,
            code_content=code_content,
            innovation_evidence=innovation_evidence,
            practicality_evidence=practicality_evidence,
            ux_evidence=ux_evidence,
            code_quality_evidence=code_quality_evidence,
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

        logger.info(f"代码评审完成，总分：{review_result.overall_score}")

        # 清理临时解压目录
        if extracted_dir:
            shutil.rmtree(extracted_dir, ignore_errors=True)
            logger.info(f"已清理临时目录：{extracted_dir}")

        return review_result

    def _extract_zip_file(self, zip_path: str) -> Optional[str]:
        """
        解压压缩包到临时目录

        Args:
            zip_path: 压缩包路径

        Returns:
            str: 解压后的目录路径，失败返回None
        """
        try:
            # 创建临时目录
            temp_dir = tempfile.mkdtemp(prefix="code_review_")

            # 解压
            with zipfile.ZipFile(zip_path, 'r') as zip_ref:
                zip_ref.extractall(temp_dir)

            logger.info(f"压缩包解压成功：{zip_path} -> {temp_dir}")
            return temp_dir

        except Exception as e:
            logger.error(f"压缩包解压失败：{e}")
            return None

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
        readme_content: Optional[str],
        work_description: Optional[str] = None
    ) -> Dict[str, Any]:
        """
        检查硬性要求合规性

        检查项：
        1. 源代码文件存在性
        2. 说明文档完整性（README或docx均可）
        3. 代码文件数量合理性（至少有代码）
        4. 代码行数合理性（至少100行）

        Args:
            code_metadata: 代码元数据
            readme_content: README文件内容
            work_description: 作品说明（可能包含docx文档内容）

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

        # 2. 说明文档完整性（README、docx等均可）
        # 检查readme_content或work_description中是否包含说明文档内容
        has_readme = readme_content is not None and len(readme_content) > 100
        has_doc_in_description = "【说明文档内容】" in (work_description or "")
        compliance["readme_exists"] = has_readme or has_doc_in_description
        compliance["readme_message"] = (
            "说明文档完整，符合要求"
            if compliance["readme_exists"]
            else "未提供说明文档（README、docx、pdf、txt等均可）"
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

    def _read_code_content(self, code_metadata: Dict[str, Any], max_chars: int = 15000) -> str:
        """
        读取实际源代码内容（用于传给LLM评审）

        Args:
            code_metadata: 代码元数据（含code_files列表和main_file）
            max_chars: 总字符上限，防止超过LLM上下文窗口

        Returns:
            str: 格式化的代码内容字符串
        """
        MAX_FILE_CHARS = 5000
        code_files = code_metadata.get("code_files", [])
        main_file = code_metadata.get("main_file")

        if not code_files:
            return "（未找到源代码文件）"

        # 排序：主文件优先
        ordered_files = []
        if main_file and main_file in code_files:
            ordered_files.append(main_file)
        ordered_files.extend(f for f in code_files if f != main_file)

        parts = []
        total_chars = 0

        for file_path in ordered_files:
            if total_chars >= max_chars:
                parts.append(f"\n... 还有 {len(ordered_files) - len(parts)} 个文件未展示（已达内容上限）")
                break

            try:
                with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                    content = f.read(MAX_FILE_CHARS + 1)

                file_name = os.path.basename(file_path)

                # 单文件截断
                if len(content) > MAX_FILE_CHARS:
                    content = content[:MAX_FILE_CHARS] + f"\n... （文件过长，已截断）"

                entry = f"\n===== {file_name} =====\n{content}"
                parts.append(entry)
                total_chars += len(entry)

            except Exception as e:
                logger.warning(f"无法读取代码文件 {file_path}: {e}")

        return '\n'.join(parts)

    # ==================== 辅助方法 ====================

    def _read_file_safe(self, file_path: str, max_chars: int = 3000) -> Optional[str]:
        try:
            with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                return f.read(max_chars)
        except Exception:
            return None

    def _parse_python_requirements(self, content: str) -> List[str]:
        libs = []
        for line in content.strip().split('\n'):
            line = line.strip()
            if not line or line.startswith('#'):
                continue
            lib_name = re.split(r'[>=<\[]', line)[0].strip().lower()
            if lib_name:
                libs.append(lib_name)
        return libs

    def _parse_maven_dependencies(self, content: str) -> List[str]:
        return [g.strip() for g in re.findall(r'<groupId>(.*?)</groupId>', content)]

    def _parse_npm_dependencies(self, content: str) -> List[str]:
        try:
            data = json.loads(content)
            deps = list(data.get("dependencies", {}).keys())
            deps += list(data.get("devDependencies", {}).keys())
            return [d.lower() for d in deps]
        except (json.JSONDecodeError, AttributeError):
            return []

    # ==================== 项目结构扫描 ====================

    def _scan_project_structure(self, code_path: str, language: str) -> Dict[str, Any]:
        """
        扫描项目目录结构，发现各类文件（依赖、配置、前端、测试等）
        """
        result = {
            "dependency_files": [],
            "config_files": [],
            "test_files": [],
            "frontend_files": [],
            "doc_files": [],
            "docker_files": [],
            "all_file_extensions": Counter(),
            "module_dirs": [],
        }

        IGNORED_DIRS = {
            "node_modules", "__pycache__", ".git", ".idea", "target",
            "dist", "build", ".vscode", "venv", ".venv", "env", ".mvn",
            "bin", ".gradle", ".mvn", "out", ".settings",
        }

        DEPENDENCY_FILES = {
            "requirements.txt", "Pipfile", "pyproject.toml", "setup.py",
            "pom.xml", "build.gradle", "package.json", "Cargo.toml",
            "go.mod", "CMakeLists.txt", "Makefile",
        }
        CONFIG_FILES = {
            ".env", ".env.local", "config.yaml", "config.json",
            "application.properties", "application.yml", "settings.py",
            "settings.json", ".gitignore", "tsconfig.json",
            "vite.config.js", "vite.config.ts", "webpack.config.js",
        }
        FRONTEND_EXTS = {
            ".html", ".css", ".js", ".ts", ".jsx", ".tsx", ".vue",
            ".scss", ".less", ".sass", ".ejs", ".pug",
        }

        if not os.path.isdir(code_path):
            return result

        for root, dirs, files in os.walk(code_path):
            dirs[:] = [d for d in dirs if d not in IGNORED_DIRS]

            for file in files:
                ext = os.path.splitext(file)[1].lower()
                result["all_file_extensions"][ext] += 1
                full_path = os.path.join(root, file)

                if file in DEPENDENCY_FILES:
                    result["dependency_files"].append(full_path)
                elif file in CONFIG_FILES:
                    result["config_files"].append(full_path)
                elif file.startswith("test_") and file.endswith(".py") \
                        or file.endswith("Test.java") \
                        or file.endswith("test.js") \
                        or file.endswith("spec.js") \
                        or file.endswith("spec.ts"):
                    result["test_files"].append(full_path)
                elif ext in FRONTEND_EXTS:
                    result["frontend_files"].append(full_path)
                elif file in {"Dockerfile", "docker-compose.yml", "docker-compose.yaml"}:
                    result["docker_files"].append(full_path)
                elif ext in {".md", ".rst", ".txt"} and not file.startswith("."):
                    result["doc_files"].append(full_path)

                if file == "__init__.py":
                    result["module_dirs"].append(root)

        return result

    # ==================== 维度分析方法 ====================

    def _analyze_innovation_evidence(
        self, project_structure: Dict, code_metadata: Dict, code_content: str
    ) -> Dict[str, Any]:
        """创新性证据分析（非计科友好）"""
        evidence = {
            "detected_libraries": [],
            "has_data_visualization": False,
            "ai_ml_indicators": [],
            "external_api_count": 0,
            "uses_framework": False,
            "innovation_score_hint": 8,
        }

        # 解析依赖文件
        detected_libs = set()
        for dep_file in project_structure["dependency_files"]:
            content = self._read_file_safe(dep_file, max_chars=5000)
            if not content:
                continue
            fname = os.path.basename(dep_file).lower()
            if "requirements" in fname or "pipfile" in fname:
                detected_libs.update(self._parse_python_requirements(content))
            elif "pom.xml" in fname:
                detected_libs.update(self._parse_maven_dependencies(content))
            elif "package.json" in fname:
                detected_libs.update(self._parse_npm_dependencies(content))
        evidence["detected_libraries"] = sorted(detected_libs)

        # 是否使用了框架
        framework_keywords = {
            "flask", "django", "fastapi", "spring", "express", "vue",
            "react", "angular", "koa", "tornado", "bottle", "echarts",
        }
        evidence["uses_framework"] = any(
            kw in lib for lib in detected_libs for kw in framework_keywords
        )

        # 数据可视化
        viz_keywords = {
            "matplotlib", "plotly", "echarts", "seaborn", "bokeh",
            "chart.js", "d3", "pyecharts", "highcharts",
        }
        evidence["has_data_visualization"] = any(
            kw in lib for lib in detected_libs for kw in viz_keywords
        )

        # AI/ML 相关
        ai_ml_keywords = {
            "openai", "langchain", "tensorflow", "torch", "keras",
            "sklearn", "scikit-learn", "paddle", "mindspore", "transformers",
        }
        evidence["ai_ml_indicators"] = [
            lib for lib in detected_libs
            if any(kw in lib for kw in ai_ml_keywords)
        ]

        # 外部 API 调用
        api_urls = re.findall(r'https?://[^\s"\'<>\)]+', code_content)
        # 过滤掉常见的文档/示例 URL
        filtered = {
            url for url in api_urls
            if not any(skip in url for skip in ("example.com", "localhost", "127.0.0.1", "docs.", "github.com"))
        }
        evidence["external_api_count"] = len(filtered)

        # 计算 score_hint
        score = 8
        if evidence["uses_framework"]:
            score += 2
        if evidence["has_data_visualization"]:
            score += 2
        if evidence["ai_ml_indicators"]:
            score += 3
        if evidence["external_api_count"] >= 3:
            score += 2
        elif evidence["external_api_count"] >= 1:
            score += 1
        if len(detected_libs) >= 5:
            score += 2
        elif len(detected_libs) >= 3:
            score += 1
        evidence["innovation_score_hint"] = min(score, 20)

        return evidence

    def _analyze_practicality_evidence(
        self, project_structure: Dict, code_metadata: Dict, code_content: str
    ) -> Dict[str, Any]:
        """实用性证据分析（关注"能不能用"）"""
        evidence = {
            "code_scale": "小",
            "has_user_entry": False,
            "error_handling_count": 0,
            "has_data_persistence": False,
            "has_config": False,
            "completeness_indicators": [],
            "practicality_score_hint": 8,
        }

        total_lines = code_metadata.get("total_lines", 0)
        file_count = code_metadata.get("file_count", 0)

        # 代码规模
        if total_lines >= 500 and file_count >= 5:
            evidence["code_scale"] = "较大"
        elif total_lines >= 200 and file_count >= 3:
            evidence["code_scale"] = "中等"

        # 用户交互入口
        language = code_metadata.get("language", "").lower()
        if language == "python":
            entry_patterns = [
                r'@app\.(get|post|put|delete|patch|route)',
                r'@router\.(get|post|put|delete|patch)',
                r'argparse', r'input\s*\(', r'Tkinter', r'tkinter',
            ]
        elif language in ("java",):
            entry_patterns = [
                r'@(Get|Post|Put|Delete|Request)Mapping',
                r'@Controller|@RestController',
                r'Scanner', r'JFrame', r'static\s+void\s+main',
            ]
        else:
            entry_patterns = [r'main\s*\(', r'route|endpoint|handler']
        evidence["has_user_entry"] = any(
            re.search(p, code_content) for p in entry_patterns
        )

        # 错误处理
        try_patterns = [r'\btry\s*[:\{]', r'\bexcept\s', r'\bcatch\s*\(']
        evidence["error_handling_count"] = sum(
            len(re.findall(p, code_content)) for p in try_patterns
        )

        # 数据持久化
        persistence_keywords = [
            "sqlite", "mysql", "postgresql", "mongodb", "redis",
            "sqlalchemy", "jdbc", "mybatis", "jpa", "hibernate",
            "open(", "with open", "csv", "json.dump", "json.load",
            "pickle", "shelve", "file", "save", "load",
        ]
        code_lower = code_content.lower()
        evidence["has_data_persistence"] = any(kw in code_lower for kw in persistence_keywords)

        # 配置管理
        evidence["has_config"] = len(project_structure.get("config_files", [])) > 0

        # 完整性指标
        if evidence["code_scale"] in ("中等", "较大"):
            evidence["completeness_indicators"].append("合理代码规模")
        if evidence["has_user_entry"]:
            evidence["completeness_indicators"].append("有用户入口")
        if evidence["error_handling_count"] >= 2:
            evidence["completeness_indicators"].append("有错误处理")
        if evidence["has_data_persistence"]:
            evidence["completeness_indicators"].append("有数据存储")
        if evidence["has_config"]:
            evidence["completeness_indicators"].append("有配置管理")

        # score_hint
        score = 8
        if evidence["code_scale"] == "较大":
            score += 3
        elif evidence["code_scale"] == "中等":
            score += 2
        if evidence["has_user_entry"]:
            score += 2
        if evidence["error_handling_count"] >= 3:
            score += 2
        elif evidence["error_handling_count"] >= 1:
            score += 1
        if evidence["has_data_persistence"]:
            score += 2
        if evidence["has_config"]:
            score += 1
        evidence["practicality_score_hint"] = min(score, 20)

        return evidence

    def _analyze_ux_evidence(
        self, project_structure: Dict, code_metadata: Dict, code_content: str
    ) -> Dict[str, Any]:
        """用户体验证据分析（降低门槛）"""
        evidence = {
            "has_frontend": False,
            "frontend_file_count": 0,
            "frontend_framework": None,
            "has_ui_library": False,
            "has_error_messages": False,
            "has_input_handling": False,
            "has_cli_help": False,
            "is_cli_program": False,
            "ux_score_hint": 8,
        }

        frontend_files = project_structure.get("frontend_files", [])
        evidence["frontend_file_count"] = len(frontend_files)
        evidence["has_frontend"] = len(frontend_files) > 0

        # 前端框架/UI库检测
        dep_content = ""
        for dep_file in project_structure.get("dependency_files", []):
            content = self._read_file_safe(dep_file, max_chars=3000)
            if content:
                dep_content += content.lower()

        ui_libs = {
            "bootstrap": ["bootstrap"],
            "element_ui": ["element-ui", "element-plus"],
            "ant_design": ["antd", "ant-design"],
            "vant": ["vant"],
            "tailwind": ["tailwindcss", "tailwind"],
            "vue": ["vue"],
            "react": ["react"],
            "angular": ["@angular/core"],
        }
        for framework, keywords in ui_libs.items():
            if any(kw in dep_content for kw in keywords):
                evidence["frontend_framework"] = framework
                evidence["has_ui_library"] = True
                break

        # 错误提示给用户
        user_error_patterns = [
            r'print\(.*[错错误失败]', r'alert\(', r'toast',
            r'message\.error', r'message\.success', r'flash\(',
            r'raise\s+\w+Error\(["\']',
            r'系统提示|操作成功|操作失败',
        ]
        evidence["has_error_messages"] = any(
            re.search(p, code_content, re.IGNORECASE) for p in user_error_patterns
        )

        # 输入处理
        input_patterns = [
            r'input\s*\(', r'argparse', r'<form', r'<input',
            r'readline', r'scanner', r'sys\.argv',
        ]
        evidence["has_input_handling"] = any(
            re.search(p, code_content, re.IGNORECASE) for p in input_patterns
        )

        # CLI 帮助信息
        cli_help_patterns = [
            r'argparse', r'--help', r'usage:', r'用法', r'帮助',
            r'print\("用法', r'help\s*=',
        ]
        evidence["has_cli_help"] = any(
            re.search(p, code_content, re.IGNORECASE) for p in cli_help_patterns
        )

        # 是否纯 CLI 程序
        evidence["is_cli_program"] = not evidence["has_frontend"]

        # score_hint
        score = 6  # 基础分，纯 CLI 有基本输出也能拿到
        if evidence["has_frontend"]:
            score += 3
            if evidence["has_ui_library"]:
                score += 3
        else:
            # CLI 程序
            if evidence["has_cli_help"]:
                score += 3
            if evidence["has_input_handling"]:
                score += 2
        if evidence["has_error_messages"]:
            score += 2
        if evidence["has_input_handling"]:
            score += 1
        evidence["ux_score_hint"] = min(score, 20)

        return evidence

    def _analyze_code_quality_enhanced(
        self, code_metadata: Dict, code_content: str, language: str
    ) -> Dict[str, Any]:
        """代码质量增强分析（关注基本素养）"""
        evidence = {
            "comment_ratio": 0.0,
            "has_bad_naming": False,
            "multi_file": False,
            "has_error_handling": False,
            "has_dependency_management": False,
            "code_quality_score_hint": 8,
        }

        total_lines = code_metadata.get("total_lines", 0)
        comment_lines = code_metadata.get("comment_lines", 0)

        # 注释比例
        if total_lines > 0:
            evidence["comment_ratio"] = round(comment_lines / total_lines, 3)

        # 命名检查：检测明显的无意义命名
        bad_name_patterns = [
            r'\b(a|b|c|aa|bb|cc|xx|yy|zz|tmp|temp|foo|bar|test1|test2)\s*=',
        ]
        bad_matches = 0
        for pattern in bad_name_patterns:
            bad_matches += len(re.findall(pattern, code_content))
        evidence["has_bad_naming"] = bad_matches >= 5

        # 多文件结构
        evidence["multi_file"] = code_metadata.get("file_count", 0) >= 3

        # 基本错误处理
        try_patterns = [r'\btry\s*[:\{]', r'\bexcept\s', r'\bcatch\s*\(']
        evidence["has_error_handling"] = any(
            re.search(p, code_content) for p in try_patterns
        )

        # 依赖管理（从项目结构中判断）
        dep_files_count = 0
        # 这里会在 review_code 中由 project_structure 传入补充
        # 暂时用文件名检测
        for keyword in ["requirements", "package.json", "pom.xml", "Cargo.toml"]:
            if keyword in code_content.lower():
                dep_files_count += 1
        evidence["has_dependency_management"] = dep_files_count > 0

        # score_hint
        score = 8
        if evidence["comment_ratio"] >= 0.1:
            score += 3
        elif evidence["comment_ratio"] >= 0.05:
            score += 2
        elif evidence["comment_ratio"] > 0:
            score += 1
        if not evidence["has_bad_naming"]:
            score += 2
        if evidence["multi_file"]:
            score += 2
        if evidence["has_error_handling"]:
            score += 2
        if evidence["has_dependency_management"]:
            score += 1
        evidence["code_quality_score_hint"] = min(score, 20)

        return evidence

    def _get_system_prompt(self) -> str:
        """系统提示词：定义代码评审角色和标准（非计算机专业友好版）"""
        return """你是一位经验丰富的代码作品评审专家，负责评审非计算机专业大学生的程序设计作品。

重要背景：参赛者为非计算机专业学生，编程能力和经验参差不齐。请以鼓励性评价为主，在指出不足的同时肯定其努力和亮点。评分应体现公平性，不应以计算机专业的标准要求参赛者。

【评分维度】（5个维度各20分，总分100分）：

1. 创新性（0-20分）：
   16-20分：作品解决了有意义的实际问题，且解决方式有独到之处；尝试使用了超出课程要求的技术（如AI、数据可视化、外部API调用等）
   11-15分：有明确的问题场景，功能设计有一定巧思；使用了框架或第三方库而非从零手写
   6-10分：功能基本实现但缺乏亮点，思路较为常规
   0-5分：作品完成度极低，看不出创新点

2. 实用性（0-20分）：
   16-20分：功能完整可运行，覆盖主要使用场景；有数据存储/持久化，有基本错误处理；有明确的目标用户和使用场景
   11-15分：核心功能可用，但部分功能不完整或缺少错误处理；有一定的实用价值
   6-10分：功能有缺失但能看出设计意图；基本可运行但体验粗糙
   0-5分：无法正常运行或功能极不完整

3. 用户体验（0-20分）：
   16-20分：有友好的界面设计，操作流程清晰；使用了UI组件库或模板，有错误提示；非图形界面程序有清晰的帮助信息和输出格式
   11-15分：有基本的界面或交互方式；能完成操作但界面/交互较简陋
   6-10分：有简单的界面但设计粗糙；或命令行程序有基本参数但缺乏说明
   0-5分：无任何用户交互设计，运行方式不明确
   注意：纯算法/数据处理/命令行类作品若有清晰的输出格式、使用说明或进度提示，应给予6-10分，不宜直接给0分。

4. 代码质量（0-20分）：
   16-20分：代码结构清晰，函数/模块划分合理；有适当的注释说明关键逻辑；变量命名有意义，代码可读性好；有依赖管理文件
   11-15分：代码基本可读，有部分注释；命名大体合理但有少量不规范；代码组织有一定逻辑
   6-10分：代码能运行但缺少注释；命名不太规范（大量a、b、tmp等）；大量代码堆在一个文件中
   0-5分：代码混乱难以理解，无任何注释

5. 文档完整性（0-20分）：
   16-20分：有完整README（项目介绍、安装步骤、使用方法）；有技术架构或设计说明；文档内容详实、格式规范
   11-15分：有README但内容不够详实；有部分技术文档；文档基本覆盖项目功能但缺少细节
   6-10分：仅有简单的README或说明；文档内容简短，缺少关键技术信息
   0-5分：无任何说明文档，或文档内容与代码严重不符

【硬性要求检查】：
- 源代码文件存在且可运行
- 说明文档完整性（README、docx、pdf、txt均可）
- 代码文件数量合理性（至少有代码）
- 代码行数合理性（至少100行）

评审原则：
1. 参赛者为非计算机专业学生，请以鼓励性评价为主
2. 在指出不足的同时肯定其努力和亮点
3. 硬性要求不合规项需在评审意见中指出，但语气要温和
4. 提供具体、可操作的改进建议
5. 评审总结应兼顾客观评价和积极鼓励

输出格式：
请严格按照JSON格式输出，包含以下字段：
{
  "overall_score": 总分（0-100）,
  "innovation_score": 创新性评分（0-20）,
  "practicality_score": 实用性评分（0-20）,
  "user_experience_score": 用户体验评分（0-20）,
  "code_quality_score": 代码质量评分（0-20）,
  "documentation_score": 文档完整性评分（0-20）,
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
        code_quality_score: float,
        code_content: str = "",
        innovation_evidence: Optional[Dict] = None,
        practicality_evidence: Optional[Dict] = None,
        ux_evidence: Optional[Dict] = None,
        code_quality_evidence: Optional[Dict] = None,
    ) -> str:
        """构建代码评审提示词"""

        compliance_status = "符合所有硬性要求" if compliance_check["all_valid"] else "部分硬性要求不合规"

        # 构建静态分析报告
        analysis_report = ""
        if innovation_evidence:
            analysis_report += f"""
--- 创新性指标 ---
- 检测到的库：{', '.join(innovation_evidence.get('detected_libraries', [])) or '无'}
- 使用了框架：{'是' if innovation_evidence.get('uses_framework') else '否'}
- 数据可视化：{'有' if innovation_evidence.get('has_data_visualization') else '无'}
- AI/ML相关功能：{', '.join(innovation_evidence.get('ai_ml_indicators', [])) or '无'}
- 外部API调用：{innovation_evidence.get('external_api_count', 0)}个
- 建议分数参考：{innovation_evidence.get('innovation_score_hint', 8)}/20
"""

        if practicality_evidence:
            analysis_report += f"""
--- 实用性指标 ---
- 代码规模：{practicality_evidence.get('code_scale', '未知')}
- 用户交互入口：{'有' if practicality_evidence.get('has_user_entry') else '无'}
- 错误处理数量：{practicality_evidence.get('error_handling_count', 0)}处
- 数据持久化：{'有' if practicality_evidence.get('has_data_persistence') else '无'}
- 配置管理：{'有' if practicality_evidence.get('has_config') else '无'}
- 完整性指标：{', '.join(practicality_evidence.get('completeness_indicators', [])) or '无'}
- 建议分数参考：{practicality_evidence.get('practicality_score_hint', 8)}/20
"""

        if ux_evidence:
            analysis_report += f"""
--- 用户体验指标 ---
- 前端文件数量：{ux_evidence.get('frontend_file_count', 0)}个
- 前端框架/UI库：{ux_evidence.get('frontend_framework') or '未检测到'}
- 面向用户的错误提示：{'有' if ux_evidence.get('has_error_messages') else '无'}
- 输入处理：{'有' if ux_evidence.get('has_input_handling') else '无'}
- CLI帮助信息：{'有' if ux_evidence.get('has_cli_help') else '无'}
- 程序类型：{'命令行程序' if ux_evidence.get('is_cli_program') else '图形界面程序'}
- 建议分数参考：{ux_evidence.get('ux_score_hint', 8)}/20
"""

        if code_quality_evidence:
            analysis_report += f"""
--- 代码质量指标 ---
- 注释比例：{code_quality_evidence.get('comment_ratio', 0):.1%}
- 命名规范性：{'存在较多无意义命名' if code_quality_evidence.get('has_bad_naming') else '基本规范'}
- 多文件结构：{'是' if code_quality_evidence.get('multi_file') else '否'}
- 错误处理：{'有' if code_quality_evidence.get('has_error_handling') else '无'}
- 依赖管理：{'有' if code_quality_evidence.get('has_dependency_management') else '无'}
- 建议分数参考：{code_quality_evidence.get('code_quality_score_hint', 8)}/20
"""

        analysis_section = ""
        if analysis_report:
            analysis_section = f"""
【静态分析参考】（系统自动检测，仅供参考）：
{analysis_report}
说明：以上"建议分数参考"是系统基于静态分析计算的参考值。请以实际代码内容为主要依据进行评分。如果你的判断与参考值偏差较大（>5分），请在评审意见中说明原因。
"""

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
{analysis_section}
【源代码内容】：
{code_content if code_content else '（未读取到代码内容）'}

【作品说明】：
{work_description}

【README文档】：
{readme_content if readme_content else '未提供README文档'}

请根据评审标准，对代码作品进行全面评价。注意参赛者为非计算机专业学生，请以鼓励为主，肯定其努力和亮点。请以JSON格式输出评审结果。"""

        return prompt

    def _parse_llm_output(
        self,
        llm_output: str,
        code_metadata: Dict[str, Any],
        compliance_check: Dict[str, Any],
        code_quality_score: float
    ) -> CodeReviewOutput:
        """解析LLM输出为结构化评审结果"""
        try:
            result_dict = extract_json_from_llm_output(llm_output)

            # 构建CodeReviewOutput对象
            review_output = CodeReviewOutput(
                overall_score=result_dict.get("overall_score", code_quality_score + 50),
                innovation_score=result_dict.get("innovation_score", 12),
                practicality_score=result_dict.get("practicality_score", 12),
                user_experience_score=result_dict.get("user_experience_score", 12),
                code_quality_score=result_dict.get("code_quality_score", code_quality_score),
                documentation_score=result_dict.get("documentation_score", 10),
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
                innovation_score=12,
                practicality_score=12,
                user_experience_score=12,
                code_quality_score=code_quality_score,
                documentation_score=8,
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

    os.environ["DEEPSEEK_API_KEY"] = "your-deepseek-api-key"

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