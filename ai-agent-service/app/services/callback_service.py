# Callback Service - Send results back to Spring Boot
from app.api.schemas import AIReviewCallbackRequest
from app.config import settings
import logging
import httpx

logger = logging.getLogger(__name__)


class CallbackService:
    """回调服务 - 向Spring Boot返回审核结果"""

    def __init__(self):
        """初始化回调服务"""
        self.spring_boot_url = settings.spring_boot_api_url
        logger.info(f"Callback service initialized with Spring Boot URL: {self.spring_boot_url}")

    async def send_callback(self, submission_id: int, report: dict) -> bool:
        """
        发送审核结果回调给Spring Boot

        Args:
            submission_id: 提交ID
            report: AI审核报告（Pydantic对象或dict）

        Returns:
            bool: 是否成功
        """
        callback_url = f"{self.spring_boot_url.rstrip('/')}/ai-reviews/callback"
        logger.info(f"🌐 Sending callback for submission {submission_id}")

        # 构造符合Java DTO期望的JSON格式（camelCase）
        from pydantic import BaseModel
        if isinstance(report, BaseModel):
            report_dict = report.model_dump(by_alias=False)
            logger.debug(f"Report serialized")
        else:
            report_dict = report

        callback_payload = {
            "submissionId": submission_id,
            "report": report_dict
        }

        logger.debug(f"Callback payload prepared")

        try:
            async with httpx.AsyncClient(timeout=10.0) as client:
                response = await client.post(callback_url, json=callback_payload)
                logger.debug(f"Response: status={response.status_code}")

                if response.status_code == 200:
                    logger.info(f"✅ Callback successful")
                    return True
                else:
                    logger.error(f"❌ Callback failed: status={response.status_code}")
                    return False

        except Exception as e:
            logger.error(f"❌ Callback error: {e}", exc_info=True)
            return False