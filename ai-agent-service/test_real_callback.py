#!/usr/bin/env python3
"""
测试回调到真实的submission_id=1
"""
import asyncio
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from app.agent.langchain_agent import AIReviewAgent
from app.services.review_service import ReviewService
from app.services.callback_service import CallbackService
import logging

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)


async def test_real_callback():
    """测试回调到真实存在的submission"""
    submission_id = 1  # 使用真实存在的submission_id

    logger.info(f"🚀 Testing callback with real submission_id={submission_id}")

    agent = AIReviewAgent()
    result = agent.review_work(submission_id, 'CODE', '/test.zip')

    review_service = ReviewService()
    report = review_service.compile_report(result)

    callback_service = CallbackService()
    success = await callback_service.send_callback(submission_id, report)

    logger.info(f"Result: {success}")
    return success


if __name__ == "__main__":
    asyncio.run(test_real_callback())