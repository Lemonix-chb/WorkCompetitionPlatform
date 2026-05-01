# FastAPI routes for AI review service
import asyncio
import logging
from fastapi import APIRouter, HTTPException
from app.api.schemas import ReviewRequest, ReviewResponse
from app.config import settings

logger = logging.getLogger(__name__)

router = APIRouter()


@router.post("/review", response_model=ReviewResponse)
async def submit_review(
    request: ReviewRequest
):
    """
    接收Spring Boot审核请求，立即执行审核（同步调用async函数）

    流程：
    1. Spring Boot调用此接口提交审核任务
    2. Python Agent立即启动审核（不等待）
    3. 审核完成后调用Spring Boot回调接口返回结果
    """
    logger.info(f"🚀 Received review request for submission {request.submission_id}")

    # 立即执行审核任务（fire and forget）
    asyncio.ensure_future(execute_review_and_callback(request))

    logger.info(f"✅ Async task started for submission {request.submission_id}")

    return ReviewResponse(
        submission_id=request.submission_id,
        status="processing",
        message="AI审核已启动，完成后将通过回调接口返回结果"
    )


@router.get("/status/{submission_id}")
async def get_review_status(submission_id: int):
    """查询审核状态"""
    logger.info(f"Checking status for submission {submission_id}")

    # TODO: 从数据库或缓存查询状态
    return {
        "submission_id": submission_id,
        "status": "completed",
        "message": "审核已完成"
    }


async def execute_review_and_callback(request: ReviewRequest):
    """
    执行审核并回调Spring Boot

    Args:
        request: Spring Boot提交的审核请求
    """
    logger.info(f"🚀 Starting background review task for submission {request.submission_id}")

    try:
        # 导入服务
        from app.agent.langchain_agent import AIReviewAgent
        from app.services.review_service import ReviewService
        from app.services.callback_service import CallbackService

        logger.info(f"📝 Executing review for submission {request.submission_id}")

        # 1. LangChain Agent执行审核
        agent = AIReviewAgent()
        result = agent.review_work(request.submission_id, request.work_type, request.file_path, request.work_description)
        logger.info(f"✅ Agent review completed")

        # 2. 解析结果并计算综合评分
        review_service = ReviewService()
        report = review_service.compile_report(result)
        logger.info(f"📊 Report compiled: overallScore={report.overallScore}")

        # 3. 回调Spring Boot接口返回结果
        callback_service = CallbackService()
        success = await callback_service.send_callback(request.submission_id, report)

        if success:
            logger.info(f"✅ Review completed successfully")
        else:
            logger.error(f"❌ Review callback failed")

    except Exception as e:
        logger.error(f"❌ Review execution error: {e}", exc_info=True)