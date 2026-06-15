"""
OrchestratorAgent - 主控协调者

职责：
1. 接收评审请求，根据作品类型分发到专业Agent
2. 协调多Agent并行工作（CodeReviewerAgent、PPTReviewerAgent、VideoAnalyzerAgent）
3. 整合评审结果，返回统一评审报告
4. 调用RAG知识库辅助决策（未来扩展）

作品类型路由：
- CODE_TRACK → CodeReviewerAgent
- PPT_TRACK → PPTReviewerAgent
- VIDEO_TRACK → VideoAnalyzerAgent

状态图（LangGraph）：
dispatch → analyze → aggregate → finalize
"""

import os
import shutil
import zipfile
import tempfile
import logging
from typing import Dict, Any, Literal, TypedDict, Optional, List
from langgraph.graph import StateGraph, END
from pydantic import BaseModel, Field

logger = logging.getLogger(__name__)


# ==================== 状态定义 ====================

class ReviewState(TypedDict):
    """评审状态（LangGraph状态图）"""

    # 输入信息
    submission_id: int
    work_type: Literal["CODE", "PPT", "VIDEO"]  # 作品类型
    file_path: str
    work_description: str
    additional_files: Optional[list]
    college: Optional[str]  # 学生学院

    # 评审结果
    review_result: Optional[Dict[str, Any]]
    agent_type: Optional[str]

    # 错误信息
    error: Optional[str]


class ReviewOutput(BaseModel):
    """统一评审输出"""

    submission_id: int = Field(description="提交ID")
    work_type: str = Field(description="作品类型")

    # 总分
    overall_score: float = Field(description="总体评分（0-100）", ge=0, le=100)

    # PPT作品评分维度（各20分）
    creativity_score: Optional[float] = Field(default=None, description="创意评分（PPT作品，0-20分）")
    visual_effect_score: Optional[float] = Field(default=None, description="视觉效果评分（PPT作品，0-20分）")
    content_presentation_score: Optional[float] = Field(default=None, description="内容呈现评分（PPT作品，0-20分）")
    originality_score: Optional[float] = Field(default=None, description="原创性评分（PPT作品，0-20分）")

    # VIDEO作品评分维度（各20分）
    story_score: Optional[float] = Field(default=None, description="故事性评分（VIDEO作品，0-20分）")
    director_skill_score: Optional[float] = Field(default=None, description="导演技巧评分（VIDEO作品，0-20分）")

    # CODE作品评分维度（各20分）
    innovation_score: Optional[float] = Field(default=None, description="创新性评分（CODE作品，0-20分）")
    practicality_score: Optional[float] = Field(default=None, description="实用性评分（CODE作品，0-20分）")
    user_experience_score: Optional[float] = Field(default=None, description="用户体验评分（CODE作品，0-20分）")
    code_quality_score: Optional[float] = Field(default=None, description="代码质量评分（CODE作品，0-20分）")
    documentation_score: Optional[float] = Field(default=None, description="文档完整性评分（CODE作品，0-20分）")
    duplicate_rate: Optional[float] = Field(default=None, description="代码重复率（CODE作品）")

    # 评审详情
    review_summary: str = Field(description="评审总结")
    strengths: list = Field(description="作品亮点")
    weaknesses: list = Field(description="不足之处")
    improvement_suggestions: list = Field(description="改进建议")

    # 元数据
    metadata: Dict[str, Any] = Field(description="作品元数据")

    # 评审来源
    agent_type: str = Field(description="评审Agent类型")


# ==================== OrchestratorAgent ====================

class OrchestratorAgent:
    """
    主控协调者Agent（LangGraph状态图实现）

    路由策略：
    - CODE_TRACK → CodeReviewerAgent（代码评审）
    - PPT_TRACK → PPTReviewerAgent（PPT评审）
    - VIDEO_TRACK → VideoAnalyzerAgent（视频评审）

    状态图流程：
    1. dispatch：根据作品类型分发到专业Agent
    2. analyze：专业Agent执行评审
    3. aggregate：整合评审结果（预留，多Agent场景）
    4. finalize：返回统一评审报告
    """

    def __init__(
        self,
        deepseek_api_key: Optional[str] = None,
        ffmpeg_path: Optional[str] = None
    ):
        """
        初始化OrchestratorAgent

        Args:
            deepseek_api_key: DeepSeek API密钥
            ffmpeg_path: FFmpeg路径（视频评审需要）
        """
        # 从参数或环境变量或settings加载API密钥
        from app.config import settings

        self.deepseek_api_key = deepseek_api_key or os.getenv("DEEPSEEK_API_KEY") or settings.deepseek_api_key
        self.ffmpeg_path = ffmpeg_path

        if not self.deepseek_api_key:
            raise ValueError("DeepSeek API密钥未设置，请检查.env文件中的DEEPSEEK_API_KEY配置")

        # 初始化专业Agent（延迟导入）
        self._video_agent = None
        self._code_agent = None
        self._ppt_agent = None

        # 构建LangGraph状态图
        self.graph = self._build_graph()

        logger.info(f"OrchestratorAgent初始化完成，API密钥: {self.deepseek_api_key[:10]}...")

    def _get_video_agent(self):
        """获取VideoAnalyzerAgent（延迟加载）"""
        if self._video_agent is None:
            from .video_analyzer_agent_complete import VideoAnalyzerAgent
            from app.config import settings
            self._video_agent = VideoAnalyzerAgent(
                model_name=settings.deepseek_model,
                api_key=self.deepseek_api_key,
                ffmpeg_path=self.ffmpeg_path
            )
        return self._video_agent

    def _build_graph(self) -> StateGraph:
        """
        构建LangGraph状态图

        状态节点：
        - dispatch_node：作品类型路由分发
        - analyze_node：专业Agent评审
        - aggregate_node：结果整合（预留）
        - finalize_node：返回报告
        """
        workflow = StateGraph(ReviewState)

        # 添加节点
        workflow.add_node("dispatch", self._dispatch_node)
        workflow.add_node("analyze_video", self._analyze_video_node)
        workflow.add_node("analyze_code", self._analyze_code_node)
        workflow.add_node("analyze_ppt", self._analyze_ppt_node)
        workflow.add_node("aggregate", self._aggregate_node)
        workflow.add_node("finalize", self._finalize_node)

        # 设置入口点
        workflow.set_entry_point("dispatch")

        # 添加边（路由逻辑）
        workflow.add_conditional_edges(
            "dispatch",
            self._route_by_work_type,
            {
                "VIDEO": "analyze_video",
                "CODE": "analyze_code",
                "PPT": "analyze_ppt",
                "error": END
            }
        )

        # 分析节点 → 整合节点
        workflow.add_edge("analyze_video", "aggregate")
        workflow.add_edge("analyze_code", "aggregate")
        workflow.add_edge("analyze_ppt", "aggregate")

        # 整合节点 → 最终节点
        workflow.add_edge("aggregate", "finalize")

        # 最终节点 → 结束
        workflow.add_edge("finalize", END)

        return workflow.compile()

    def _dispatch_node(self, state: ReviewState) -> ReviewState:
        """
        分发节点：验证输入，准备评审
        """
        logger.info(f"分发节点：submission_id={state['submission_id']}, work_type={state['work_type']}")

        # 验证作品类型
        valid_types = ["CODE", "PPT", "VIDEO"]
        if state["work_type"] not in valid_types:
            state["error"] = f"不支持的作品类型：{state['work_type']}"
            return state

        # 验证文件存在性
        if not os.path.exists(state["file_path"]):
            state["error"] = f"作品文件不存在：{state['file_path']}"
            return state

        return state

    def _route_by_work_type(self, state: ReviewState) -> str:
        """
        路由函数：根据作品类型分发到对应分析节点
        """
        if state.get("error"):
            return "error"

        work_type = state["work_type"]

        logger.info(f"路由分发：{work_type} → analyze_{work_type.lower()}")

        return work_type

    def _analyze_video_node(self, state: ReviewState) -> ReviewState:
        """
        视频分析节点：调用VideoAnalyzerAgent
        """
        logger.info("视频分析节点：启动VideoAnalyzerAgent")

        # 处理压缩包/目录，收集文档
        actual_file_path, temp_dir, additional_docs = self._extract_file_if_zip(state["file_path"], "VIDEO")

        # 合并文档文件到state
        if additional_docs:
            state["additional_files"] = (state.get("additional_files") or []) + additional_docs

        try:
            agent = self._get_video_agent()

            enriched_description = state["work_description"]
            doc_contents = self._read_doc_files(state.get("additional_files") or [])
            if doc_contents:
                enriched_description += "\n\n【说明文档内容】\n" + doc_contents

            result = agent.review_video(
                video_path=actual_file_path,
                work_description=enriched_description,
                additional_files=state.get("additional_files")
            )

            state["review_result"] = result.model_dump()
            state["agent_type"] = "VideoAnalyzerAgent"

            logger.info(f"视频分析完成：总分{result.overall_score}")

        except Exception as e:
            logger.error(f"视频分析失败：{e}")
            state["error"] = str(e)

        # 清理临时目录
        if temp_dir:
            shutil.rmtree(temp_dir, ignore_errors=True)
            logger.info(f"已清理临时目录：{temp_dir}")

        return state

    def _analyze_code_node(self, state: ReviewState) -> ReviewState:
        """
        代码分析节点：调用CodeReviewerAgent
        """
        logger.info("代码分析节点：启动CodeReviewerAgent")

        # 处理压缩包/目录（双重保险：OrchestratorAgent和CodeReviewerAgent都可以解压）
        actual_file_path, temp_dir, additional_docs = self._extract_file_if_zip(state["file_path"], "CODE")

        # 合并文档文件到state
        if additional_docs:
            state["additional_files"] = (state.get("additional_files") or []) + additional_docs

        try:
            agent = self._get_code_agent()

            # 检测代码语言
            language = self._detect_code_language(actual_file_path)

            # 查找README文件
            readme_content = self._find_readme(actual_file_path)

            # 读取附加文档文件内容（docx/txt/md等），拼接到作品说明
            enriched_description = state["work_description"]
            doc_contents = self._read_doc_files(state.get("additional_files") or [])
            if doc_contents:
                enriched_description += "\n\n【说明文档内容】\n" + doc_contents
                logger.info(f"已读取{len(state.get('additional_files', []))}个文档文件内容")

            # 信息与智能科学技术学院（信科院）学生评审标准适当从严
            college = state.get("college", "")
            if college and "信息与智能科学技术" in college:
                enriched_description += (
                    "\n\n【评审标准调整】\n"
                    "该学生来自信息与智能科学技术学院（信科院），属于计算机相关专业。"
                    "评审时请适当提高标准：\n"
                    "1. 代码质量：代码结构、命名规范应体现专业性，注释完整\n"
                    "2. 创新性：技术方案应有一定的新意，不宜过于简单\n"
                    "3. 代码规范：建议有模块划分和基本的异常处理\n"
                    "4. 整体评分适当从严，但不应过度扣分"
                )
                logger.info(f"已为信科院学生应用严格评审标准")

            result = agent.review_code(
                code_path=actual_file_path,
                language=language,
                work_description=enriched_description,
                readme_content=readme_content
            )

            state["review_result"] = result.model_dump()
            state["agent_type"] = "CodeReviewerAgent"

            logger.info(f"代码分析完成：总分{result.overall_score}")

        except Exception as e:
            logger.error(f"代码分析失败：{e}")
            state["error"] = str(e)

        # 清理临时目录（如果OrchestratorAgent解压了）
        if temp_dir:
            shutil.rmtree(temp_dir, ignore_errors=True)
            logger.info(f"已清理临时目录：{temp_dir}")

        return state

    def _get_code_agent(self):
        """获取CodeReviewerAgent（延迟加载）"""
        if self._code_agent is None:
            from .code_reviewer_agent import CodeReviewerAgent
            from app.config import settings
            self._code_agent = CodeReviewerAgent(
                model_name=settings.deepseek_model,
                api_key=self.deepseek_api_key
            )
        return self._code_agent

    def _detect_code_language(self, code_path: str) -> str:
        """检测代码语言（支持压缩包）"""
        # 如果是压缩包，需要检查压缩包内的文件
        if code_path.endswith('.zip') or code_path.endswith('.rar'):
            try:
                with zipfile.ZipFile(code_path, 'r') as zip_ref:
                    file_list = zip_ref.namelist()

                    java_count = sum(1 for f in file_list if f.endswith('.java'))
                    py_count = sum(1 for f in file_list if f.endswith('.py'))
                    c_count = sum(1 for f in file_list if f.endswith('.c'))
                    cpp_count = sum(1 for f in file_list if f.endswith('.cpp'))

                    logger.info(f"压缩包语言检测：Java={java_count}, Python={py_count}, C={c_count}, C++={cpp_count}")

                    # 根据数量判断语言（优先Java）
                    if java_count > 0:
                        return "java"
                    elif py_count > 0:
                        return "python"
                    elif cpp_count > 0:
                        return "cpp"
                    elif c_count > 0:
                        return "c"
                    else:
                        return "java"  # 默认Java而不是Python
            except Exception as e:
                logger.error(f"压缩包语言检测失败：{e}")
                return "java"  # 默认Java

        # 单文件检测
        if os.path.isfile(code_path):
            if code_path.endswith(".py"):
                return "python"
            elif code_path.endswith(".java"):
                return "java"
            elif code_path.endswith(".c"):
                return "c"
            elif code_path.endswith(".cpp") or code_path.endswith(".cxx"):
                return "cpp"
            else:
                return "java"  # 默认Java

        # 目录检测
        elif os.path.isdir(code_path):
            java_count = 0
            py_count = 0
            c_count = 0
            cpp_count = 0

            for root, dirs, files in os.walk(code_path):
                for file in files:
                    if file.endswith(".java"):
                        java_count += 1
                    elif file.endswith(".py"):
                        py_count += 1
                    elif file.endswith(".cpp") or file.endswith(".cxx"):
                        cpp_count += 1
                    elif file.endswith(".c"):
                        c_count += 1

            logger.info(f"目录语言检测：Java={java_count}, Python={py_count}, C={c_count}, C++={cpp_count}")

            # 根据数量判断（修复逻辑：只有确实有Python文件才返回python）
            if java_count > 0 and java_count >= py_count:
                return "java"
            elif py_count > 0 and py_count > java_count:
                return "python"
            elif cpp_count > 0:
                return "cpp"
            elif c_count > 0:
                return "c"
            else:
                return "java"  # 默认Java

        return "java"  # 全部默认Java而不是Python

    def _find_readme(self, code_path: str) -> str:
        """查找README文件或docx说明文档"""
        readme_names = ["README.md", "readme.md", "README.txt", "readme.txt"]

        if os.path.isdir(code_path):
            # 优先查找README文件
            for name in readme_names:
                readme_path = os.path.join(code_path, name)
                if os.path.exists(readme_path):
                    try:
                        with open(readme_path, 'r', encoding='utf-8', errors='ignore') as f:
                            return f.read()
                    except Exception as e:
                        logger.warning(f"无法读取README文件：{e}")

            # 没有README时，查找docx/pdf/txt说明文档，委托_read_doc_files读取内容
            doc_patterns = ['说明', '文档', 'readme', 'README', 'design', '技术']
            doc_extensions = ('.docx', '.pdf', '.txt')
            for root, _dirs, files in os.walk(code_path):
                for file in files:
                    file_lower = file.lower()
                    if not file.endswith(doc_extensions) or not any(p in file_lower for p in doc_patterns):
                        continue
                    doc_path = os.path.join(root, file)
                    text = self._read_doc_files([doc_path])
                    if text and len(text) > 50:
                        logger.info(f"使用{os.path.splitext(file)[1]}文档作为说明文档：{doc_path}")
                        return text

        return ""

    def _read_doc_files(self, file_paths: List[str]) -> str:
        """读取文档文件内容（支持txt/md/docx/pdf）"""
        contents = []
        for file_path in file_paths:
            ext = os.path.splitext(file_path)[1].lower()
            try:
                if ext in ('.txt', '.md'):
                    with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                        contents.append(f.read())
                elif ext == '.docx':
                    import docx
                    doc = docx.Document(file_path)
                    text = '\n'.join(p.text for p in doc.paragraphs if p.text.strip())
                    if text:
                        contents.append(text)
                elif ext == '.pdf':
                    import fitz
                    pdf_doc = fitz.open(file_path)
                    text = '\n'.join(page.get_text() for page in pdf_doc)
                    pdf_doc.close()
                    if text.strip():
                        contents.append(text.strip())
            except Exception as e:
                logger.warning(f"无法读取文档文件 {file_path}: {e}")
        return '\n\n'.join(contents)

    def _collect_doc_files(self, root_dir: str) -> List[str]:
        """收集目录中的文档文件（README、说明文档等）"""
        doc_patterns = ['readme', '说明', '文档', 'design', '技术']
        doc_extensions = ('.md', '.txt', '.doc', '.docx', '.pdf')
        docs = []
        for root, _dirs, files in os.walk(root_dir):
            for file in files:
                file_lower = file.lower()
                if (any(p in file_lower for p in doc_patterns) or
                        file_lower.endswith(doc_extensions)):
                    docs.append(os.path.join(root, file))
        return docs

    def _find_main_file_in_dir(self, root_dir: str, work_type: str) -> Optional[str]:
        """在目录中查找主文件（PPT/视频作品需要定位具体文件）"""
        if work_type == "PPT":
            for root, _dirs, files in os.walk(root_dir):
                for file in files:
                    if file.endswith('.pptx') or file.endswith('.ppt'):
                        return os.path.join(root, file)
        elif work_type == "VIDEO":
            video_exts = ('.mp4', '.avi', '.mov', '.mkv', '.flv', '.wmv')
            for root, _dirs, files in os.walk(root_dir):
                for file in files:
                    if file.endswith(video_exts):
                        return os.path.join(root, file)
        return None  # CODE类型不需要找主文件，直接用目录

    def _extract_file_if_zip(self, file_path: str, work_type: str) -> tuple[str, Optional[str], List[str]]:
        """处理文件路径：支持单文件、目录、压缩包三种情况，并收集附加文档"""
        temp_dir = None

        # 处理压缩包：先解压，然后当作目录处理
        if file_path.endswith('.zip') or file_path.endswith('.rar'):
            try:
                temp_dir = tempfile.mkdtemp(prefix=f"{work_type.lower()}_review_")
                with zipfile.ZipFile(file_path, 'r') as zip_ref:
                    zip_ref.extractall(temp_dir)
                logger.info(f"压缩包解压成功：{file_path} -> {temp_dir}")
                file_path = temp_dir
            except Exception as e:
                logger.error(f"压缩包解压失败：{e}")
                return file_path, None, []

        # 处理目录
        if os.path.isdir(file_path):
            additional_docs = self._collect_doc_files(file_path)
            main_file = self._find_main_file_in_dir(file_path, work_type)
            if main_file:
                return main_file, temp_dir, additional_docs
            return file_path, temp_dir, additional_docs

        # 单个文件
        return file_path, None, []

    def _get_ppt_agent(self):
        """获取PPTReviewerAgent（延迟加载）"""
        if self._ppt_agent is None:
            from .ppt_reviewer_agent import PPTReviewerAgent
            from app.config import settings
            self._ppt_agent = PPTReviewerAgent(
                model_name=settings.deepseek_model,
                api_key=self.deepseek_api_key
            )
        return self._ppt_agent

    def _analyze_ppt_node(self, state: ReviewState) -> ReviewState:
        """
        PPT分析节点：调用PPTReviewerAgent
        """
        logger.info("PPT分析节点：启动PPTReviewerAgent")

        # 处理压缩包/目录，收集文档
        actual_file_path, temp_dir, additional_docs = self._extract_file_if_zip(state["file_path"], "PPT")

        # 合并文档文件到state
        if additional_docs:
            state["additional_files"] = (state.get("additional_files") or []) + additional_docs

        try:
            agent = self._get_ppt_agent()

            enriched_description = state["work_description"]
            doc_contents = self._read_doc_files(state.get("additional_files") or [])
            if doc_contents:
                enriched_description += "\n\n【说明文档内容】\n" + doc_contents

            result = agent.review_ppt(
                ppt_path=actual_file_path,
                work_description=enriched_description,
                additional_files=state.get("additional_files")
            )

            state["review_result"] = result.model_dump()
            state["agent_type"] = "PPTReviewerAgent"

            logger.info(f"PPT分析完成：总分{result.overall_score}")

        except Exception as e:
            logger.error(f"PPT分析失败：{e}")
            state["error"] = str(e)

        # 清理临时目录
        if temp_dir:
            shutil.rmtree(temp_dir, ignore_errors=True)
            logger.info(f"已清理临时目录：{temp_dir}")

        return state

    def _aggregate_node(self, state: ReviewState) -> ReviewState:
        """
        整合节点：多Agent结果整合（预留，未来支持多Agent并行）
        """
        logger.info("整合节点：单Agent场景，直接传递结果")

        return state

    def _finalize_node(self, state: ReviewState) -> ReviewState:
        """
        最终节点：格式化输出，返回统一评审报告
        """
        logger.info("最终节点：格式化评审报告")

        if state.get("error"):
            state["review_result"] = {
                "overall_score": 0,
                "review_summary": f"评审失败：{state['error']}",
                "strengths": [],
                "weaknesses": [state["error"]],
                "improvement_suggestions": []
            }

        return state

    def review_submission(
        self,
        submission_id: int,
        work_type: str,
        file_path: str,
        work_description: str,
        additional_files: Optional[list] = None,
        college: Optional[str] = None
    ) -> ReviewOutput:
        """
        评审提交作品

        Args:
            submission_id: 提交ID
            work_type: 作品类型（CODE/PPT/VIDEO）
            file_path: 作品文件路径
            work_description: 作品说明文档
            additional_files: 其他附加文件
            college: 学生所在学院

        Returns:
            ReviewOutput: 统一评审报告
        """
        logger.info(f"开始评审：submission_id={submission_id}, work_type={work_type}, college={college}")

        # 初始化状态
        initial_state = ReviewState(
            submission_id=submission_id,
            work_type=work_type,
            file_path=file_path,
            work_description=work_description,
            additional_files=additional_files,
            college=college,
            review_result=None,
            agent_type=None,
            error=None
        )

        # 执行LangGraph状态图
        final_state = self.graph.invoke(initial_state)

        # 构建输出
        review_result = final_state["review_result"]

        output = ReviewOutput(
            submission_id=submission_id,
            work_type=work_type,
            overall_score=review_result.get("overall_score", 0),
            review_summary=review_result.get("review_summary", ""),
            strengths=review_result.get("strengths", []),
            weaknesses=review_result.get("weaknesses", []),
            improvement_suggestions=review_result.get("improvement_suggestions", []),
            metadata=review_result.get("video_metadata", review_result.get("ppt_metadata", review_result.get("code_metadata", {}))),
            agent_type=final_state.get("agent_type") or "Unknown",
            # PPT作品评分字段
            creativity_score=review_result.get("creativity_score"),
            visual_effect_score=review_result.get("visual_effect_score"),
            content_presentation_score=review_result.get("content_presentation_score"),
            originality_score=review_result.get("originality_score"),
            # VIDEO作品评分字段
            story_score=review_result.get("story_score"),
            director_skill_score=review_result.get("director_skill_score"),
            # CODE作品评分字段
            innovation_score=review_result.get("innovation_score"),
            practicality_score=review_result.get("practicality_score"),
            user_experience_score=review_result.get("user_experience_score"),
            code_quality_score=review_result.get("code_quality_score"),
            documentation_score=review_result.get("documentation_score"),
            duplicate_rate=review_result.get("duplicate_rate")
        )

        logger.info(f"评审完成：总分{output.overall_score}, PPT评分=[{output.creativity_score}, {output.visual_effect_score}, {output.content_presentation_score}, {output.originality_score}]")

        return output


# 使用示例
if __name__ == "__main__":
    import os

    os.environ["DEEPSEEK_API_KEY"] = "your-deepseek-api-key"

    orchestrator = OrchestratorAgent(
        ffmpeg_path=r"E:\ffmpeg-8.1-essentials_build\ffmpeg-8.1-essentials_build\bin"
    )

    # 测试视频评审
    test_video = "test_videos/compliant_150s.mp4"

    if os.path.exists(test_video):
        print("="*80)
        print("OrchestratorAgent完整流程测试")
        print("="*80)

        result = orchestrator.review_submission(
            submission_id=123,
            work_type="VIDEO",
            file_path=test_video,
            work_description="AI教育短视频《机器学习入门教程》"
        )

        print(f"\n评审结果：")
        print(f"  - 提交ID：{result.submission_id}")
        print(f"  - 作品类型：{result.work_type}")
        print(f"  - 总分：{result.overall_score}")
        print(f"  - 评审Agent：{result.agent_type}")

        print(f"\n评审总结：")
        print(result.review_summary[:200])

        print("="*80)
    else:
        print(f"测试视频不存在：{test_video}")