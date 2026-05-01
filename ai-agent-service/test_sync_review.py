#!/usr/bin/env python3
"""
同步版本的审核接口 - 用于诊断后台任务问题
"""
from fastapi import FastAPI
from pydantic import BaseModel, Field
from typing import Optional
import logging
import asyncio

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

app = FastAPI(title="Test Sync Review")


class ReviewRequest(BaseModel):
    submission_id: int
    work_type: str
    file_path: str
    work_description: Optional[str] = None


@app.get("/health")
async def health():
    return {"status": "ok"}


@app.post("/api/review-sync")
async def review_sync(request: ReviewRequest):
    """同步执行审核（立即执行，不使用BackgroundTasks）"""
    logger.info(f"🚀 Sync review started for submission {request.submission_id}")

    try:
        # 直接在接口中执行审核
        from app.agent.langchain_agent import AIReviewAgent
        from app.services.review_service import ReviewService
        from app.services.callback_service import CallbackService

        logger.info("📝 Step 1: Agent review")
        agent = AIReviewAgent()
        result = agent.review_work(
            request.submission_id,
            request.work_type,
            request.file_path,
            request.work_description
        )
        logger.info(f"✅ Agent result: {result['overall_score']}")

        logger.info("📊 Step 2: Compile report")
        review_service = ReviewService()
        report = review_service.compile_report(result)
        logger.info(f"✅ Report: overallScore={report.overallScore}")

        logger.info("🌐 Step 3: Send callback")
        callback_service = CallbackService()
        success = await callback_service.send_callback(request.submission_id, report)

        if success:
            logger.info("🎉 Complete success!")
            return {
                "submission_id": request.submission_id,
                "status": "completed",
                "message": "同步审核完成，回调成功",
                "overall_score": report.overallScore
            }
        else:
            logger.error("❌ Callback failed")
            return {
                "submission_id": request.submission_id,
                "status": "failed",
                "message": "回调失败"
            }

    except Exception as e:
        logger.error(f"❌ Exception: {e}", exc_info=True)
        return {
            "submission_id": request.submission_id,
            "status": "error",
            "message": str(e)
        }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=8002)