# Review Service - Compile review results
from app.api.schemas import AIReviewReport
from app.config import settings
import logging

logger = logging.getLogger(__name__)


class ReviewService:
    """审核结果编译服务"""

    def compile_report(self, agent_result: dict) -> AIReviewReport:
        """
        将Agent结果编译为标准报告

        Args:
            agent_result: Agent返回的审核结果

        Returns:
            AIReviewReport: 标准化审核报告
        """
        logger.info("Compiling AI review report...")

        # Pydantic会自动处理缺失字段，使用模型默认值
        report = AIReviewReport(**agent_result)

        logger.info(f"Report compiled: overallScore={report.overallScore}")
        return report