# FastAPI routes for AI review service
import os
import logging
import requests
from concurrent.futures import ThreadPoolExecutor
from fastapi import APIRouter, HTTPException
from app.api.schemas import ReviewRequest, ReviewResponse
from app.config import settings

logger = logging.getLogger(__name__)

router = APIRouter()

# 线程池用于执行阻塞的AI评审任务
_review_executor = ThreadPoolExecutor(max_workers=2, thread_name_prefix="ai-review-")


def _run_review_sync(request: ReviewRequest):
    """在线程池中同步执行评审并回调（避免阻塞asyncio事件循环）"""
    logger.info(f"Starting review for submission {request.submission_id} in thread {os.getpid()}")

    try:
        from app.agents.orchestrator_agent import OrchestratorAgent

        orchestrator = OrchestratorAgent(
            ffmpeg_path=settings.ffmpeg_path if hasattr(settings, 'ffmpeg_path') else None
        )
        result = orchestrator.review_submission(
            submission_id=request.submission_id,
            work_type=request.work_type,
            file_path=request.file_path,
            work_description=request.work_description,
            college=getattr(request, 'college', None)
        )
        logger.info(f"Review completed: score={result.overall_score}")

        # Build report
        from app.api.schemas import AIReviewReport

        innovation_score = 0
        practicality_score = 0
        user_experience_score = 0
        documentation_score = 0
        code_quality_score = 0
        duplicate_rate = 0

        if request.work_type == "CODE":
            innovation_score = getattr(result, 'innovation_score', 0)
            practicality_score = getattr(result, 'practicality_score', 0)
            user_experience_score = getattr(result, 'user_experience_score', 0)
            code_quality_score = getattr(result, 'code_quality_score', 0)
            documentation_score = getattr(result, 'documentation_score', 0)
            duplicate_rate = getattr(result, 'duplicate_rate', 0)
        elif request.work_type == "PPT":
            innovation_score = getattr(result, 'creativity_score', 0)
            user_experience_score = getattr(result, 'visual_effect_score', 0)
            documentation_score = getattr(result, 'content_presentation_score', 0)
            practicality_score = getattr(result, 'originality_score', 0)
        elif request.work_type == "VIDEO":
            innovation_score = getattr(result, 'originality_score', 0)
            practicality_score = getattr(result, 'story_score', 0)
            user_experience_score = getattr(result, 'visual_effect_score', 0)
            documentation_score = getattr(result, 'director_skill_score', 0)

        report = AIReviewReport(
            overallScore=result.overall_score or 0,
            innovationScore=innovation_score or 0,
            practicalityScore=practicality_score or 0,
            userExperienceScore=user_experience_score or 0,
            documentationScore=documentation_score or 0,
            codeQualityScore=code_quality_score or 0,
            duplicateRate=duplicate_rate or 0,
            reviewSummary=result.review_summary or "",
            improvementSuggestions=result.improvement_suggestions or [],
            strengths=getattr(result, 'strengths', []) or [],
            weaknesses=getattr(result, 'weaknesses', []) or [],
            aiModel=result.agent_type or "Unknown",
            riskLevel='HIGH' if (result.overall_score or 0) < 60 else ('MEDIUM' if (result.overall_score or 0) < 75 else 'LOW')
        )

        # Send callback to Spring Boot
        callback_url = f"{settings.spring_boot_api_url.rstrip('/')}/ai-reviews/callback"
        callback_payload = {
            "submissionId": request.submission_id,
            "report": report.model_dump()
        }
        resp = requests.post(callback_url, json=callback_payload, timeout=10)
        if resp.status_code == 200:
            logger.info(f"Callback successful for submission {request.submission_id}")
        else:
            logger.error(f"Callback failed: {resp.status_code}")

    except Exception as e:
        logger.error(f"Review execution error: {e}", exc_info=True)

        # Error callback
        try:
            error_report = {
                "overallScore": 0,
                "innovationScore": 0, "practicalityScore": 0,
                "userExperienceScore": 0, "documentationScore": 0,
                "codeQualityScore": 0, "duplicateRate": 0,
                "reviewSummary": str(e)[:500],
                "improvementSuggestions": ["请联系管理员重新触发评审"],
                "strengths": [], "weaknesses": [str(e)[:200]],
                "aiModel": "Error",
                "riskLevel": "HIGH"
            }
            callback_url = f"{settings.spring_boot_api_url.rstrip('/')}/ai-reviews/callback"
            requests.post(callback_url, json={
                "submissionId": request.submission_id,
                "report": error_report
            }, timeout=10)
            logger.info(f"Error callback sent for submission {request.submission_id}")
        except Exception as cb_err:
            logger.error(f"Failed to send error callback: {cb_err}")


@router.post("/review", response_model=ReviewResponse)
async def submit_review(request: ReviewRequest):
    """接收Spring Boot评审请求，在独立线程中执行评审"""
    logger.info(f"Received review request for submission {request.submission_id}")

    # 在线程池中执行评审，不阻塞事件循环
    _review_executor.submit(_run_review_sync, request)

    return ReviewResponse(
        submission_id=request.submission_id,
        status="processing",
        message="AI评审已启动，完成后将通过回调接口返回结果"
    )


@router.get("/status/{submission_id}")
async def get_review_status(submission_id: int):
    """查询评审状态"""
    return {
        "submission_id": submission_id,
        "status": "completed",
        "message": "评审已完成"
    }
