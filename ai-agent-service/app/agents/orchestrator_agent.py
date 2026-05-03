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

作者：AI Agent架构重构项目
创建时间：2026-05-03
"""

import os
import logging
from typing import Dict, Any, Literal, TypedDict, Optional
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
    - CODE_TRACK → CodeReviewerAgent（代码审核）
    - PPT_TRACK → PPTReviewerAgent（PPT审核）
    - VIDEO_TRACK → VideoAnalyzerAgent（视频审核）

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
            ffmpeg_path: FFmpeg路径（视频审核需要）
        """
        self.deepseek_api_key = deepseek_api_key or os.getenv("DEEPSEEK_API_KEY")
        self.ffmpeg_path = ffmpeg_path

        if not self.deepseek_api_key:
            raise ValueError("DeepSeek API密钥未设置")

        # 初始化专业Agent（延迟导入）
        self._video_agent = None
        self._code_agent = None
        self._ppt_agent = None

        # 构建LangGraph状态图
        self.graph = self._build_graph()

        logger.info("OrchestratorAgent初始化完成，LangGraph状态图已构建")

    def _get_video_agent(self):
        """获取VideoAnalyzerAgent（延迟加载）"""
        if self._video_agent is None:
            from .video_analyzer_agent_complete import VideoAnalyzerAgent
            self._video_agent = VideoAnalyzerAgent(
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

        try:
            agent = self._get_video_agent()

            result = agent.review_video(
                video_path=state["file_path"],
                work_description=state["work_description"],
                additional_files=state.get("additional_files")
            )

            # 转换为字典格式
            state["review_result"] = {
                "overall_score": result.overall_score,
                "story_score": result.story_score,
                "visual_effect_score": result.visual_effect_score,
                "director_skill_score": result.director_skill_score,
                "originality_score": result.originality_score,
                "compliance_check": result.compliance_check,
                "review_summary": result.review_summary,
                "strengths": result.strengths,
                "weaknesses": result.weaknesses,
                "improvement_suggestions": result.improvement_suggestions,
                "video_metadata": result.video_metadata
            }
            state["agent_type"] = "VideoAnalyzerAgent"

            logger.info(f"视频分析完成：总分{result.overall_score}")

        except Exception as e:
            logger.error(f"视频分析失败：{e}")
            state["error"] = str(e)

        return state

    def _analyze_code_node(self, state: ReviewState) -> ReviewState:
        """
        代码分析节点：调用CodeReviewerAgent（预留）
        """
        logger.info("代码分析节点：CodeReviewerAgent未实现")

        state["review_result"] = {
            "overall_score": 0,
            "review_summary": "CodeReviewerAgent未实现",
            "strengths": [],
            "weaknesses": ["代码审核Agent尚未实现"],
            "improvement_suggestions": ["需要实现CodeReviewerAgent"]
        }
        state["agent_type"] = "CodeReviewerAgent"

        return state

    def _get_ppt_agent(self):
        """获取PPTReviewerAgent（延迟加载）"""
        if self._ppt_agent is None:
            from .ppt_reviewer_agent import PPTReviewerAgent
            self._ppt_agent = PPTReviewerAgent(
                api_key=self.deepseek_api_key
            )
        return self._ppt_agent

    def _analyze_ppt_node(self, state: ReviewState) -> ReviewState:
        """
        PPT分析节点：调用PPTReviewerAgent
        """
        logger.info("PPT分析节点：启动PPTReviewerAgent")

        try:
            agent = self._get_ppt_agent()

            result = agent.review_ppt(
                ppt_path=state["file_path"],
                work_description=state["work_description"],
                additional_files=state.get("additional_files")
            )

            # 转换为字典格式
            state["review_result"] = {
                "overall_score": result.overall_score,
                "creativity_score": result.creativity_score,
                "visual_effect_score": result.visual_effect_score,
                "content_presentation_score": result.content_presentation_score,
                "originality_score": result.originality_score,
                "compliance_check": result.compliance_check,
                "review_summary": result.review_summary,
                "strengths": result.strengths,
                "weaknesses": result.weaknesses,
                "improvement_suggestions": result.improvement_suggestions,
                "ppt_metadata": result.ppt_metadata
            }
            state["agent_type"] = "PPTReviewerAgent"

            logger.info(f"PPT分析完成：总分{result.overall_score}")

        except Exception as e:
            logger.error(f"PPT分析失败：{e}")
            state["error"] = str(e)

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
        additional_files: Optional[list] = None
    ) -> ReviewOutput:
        """
        评审提交作品

        Args:
            submission_id: 提交ID
            work_type: 作品类型（CODE/PPT/VIDEO）
            file_path: 作品文件路径
            work_description: 作品说明文档
            additional_files: 其他附加文件

        Returns:
            ReviewOutput: 统一评审报告
        """
        logger.info(f"开始评审：submission_id={submission_id}, work_type={work_type}")

        # 初始化状态
        initial_state = ReviewState(
            submission_id=submission_id,
            work_type=work_type,
            file_path=file_path,
            work_description=work_description,
            additional_files=additional_files,
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
            metadata=review_result.get("video_metadata", {}),
            agent_type=final_state.get("agent_type", "Unknown")
        )

        logger.info(f"评审完成：总分{output.overall_score}")

        return output


# 使用示例
if __name__ == "__main__":
    import os

    os.environ["DEEPSEEK_API_KEY"] = "sk-325ae1ccf357480ab353a41e8b26ee32"

    orchestrator = OrchestratorAgent(
        ffmpeg_path=r"E:\ffmpeg-8.1-essentials_build\ffmpeg-8.1-essentials_build\bin"
    )

    # 测试视频审核
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