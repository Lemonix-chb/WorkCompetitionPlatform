# LangChain AI Review Agent - Real DeepSeek integration
import json
import re
import logging
from typing import Optional

from langchain_openai import ChatOpenAI
from langchain_core.messages import SystemMessage, HumanMessage
from pydantic import BaseModel, Field

from app.config import settings
from app.agent.prompts import SYSTEM_PROMPTS, build_user_prompt
from app.utils.file_utils import extract_and_analyze

logger = logging.getLogger(__name__)


class ReviewOutput(BaseModel):
    """LLM结构化输出Schema"""
    innovation_score: float = Field(ge=0, le=25, description="创新性评分")
    practicality_score: float = Field(ge=0, le=25, description="实用性评分")
    user_experience_score: float = Field(ge=0, le=25, description="用户体验评分")
    documentation_score: float = Field(ge=0, le=25, description="文档质量评分")
    review_summary: str = Field(description="评审摘要")
    improvement_suggestions: list[str] = Field(description="改进建议列表")


def determine_risk_level(duplicate_rate: Optional[float], overall_score: float) -> str:
    if duplicate_rate is not None and duplicate_rate > 30:
        return "HIGH"
    if (duplicate_rate is not None and duplicate_rate > 20) or overall_score < 60:
        return "MEDIUM"
    return "LOW"


class AIReviewAgent:

    def __init__(self):
        logger.info("Initializing LangChain AI Review Agent...")

        base_url = settings.deepseek_api_url.replace("/chat/completions", "").rstrip("/")

        self.llm = ChatOpenAI(
            api_key=settings.deepseek_api_key,
            base_url=base_url,
            model=settings.deepseek_model,
            temperature=0.3,
            max_tokens=2000,
            max_retries=2,
            timeout=60,
        )
        self.structured_llm = self.llm.with_structured_output(
            ReviewOutput, method="json_mode"
        )
        logger.info(f"Agent initialized: model={settings.deepseek_model}, base_url={base_url}")

    def review_work(self, submission_id: int, work_type: str,
                    file_path: str, work_description: str = None) -> dict:
        logger.info(f"Starting review: submission={submission_id}, type={work_type}")

        # 1. 提取并分析文件
        file_analysis = extract_and_analyze(file_path, work_type)
        logger.info(f"File analysis: {len(file_analysis['file_manifest'])} chars manifest, "
                     f"{len(file_analysis['file_contents'])} chars content, "
                     f"stats={file_analysis.get('stats')}")

        # 2. 调用LLM评审（带降级策略）
        llm_result, error_reason = self._evaluate(work_type, work_description, file_analysis)

        # 3. 计算综合分数
        overall_score = (
            llm_result.innovation_score
            + llm_result.practicality_score
            + llm_result.user_experience_score
            + llm_result.documentation_score
        )

        # 4. 确定风险等级
        risk_level = determine_risk_level(
            file_analysis.get("duplicate_rate"),
            overall_score
        )

        # 5. 构建返回结果
        result = {
            "submission_id": submission_id,
            "work_type": work_type,
            "overall_score": round(overall_score, 1),
            "innovation_score": round(llm_result.innovation_score, 1),
            "practicality_score": round(llm_result.practicality_score, 1),
            "user_experience_score": round(llm_result.user_experience_score, 1),
            "documentation_score": round(llm_result.documentation_score, 1),
            "duplicate_rate": file_analysis.get("duplicate_rate"),
            "code_quality_score": file_analysis.get("code_quality_score"),
            "review_summary": llm_result.review_summary,
            "improvement_suggestions": llm_result.improvement_suggestions,
            "ai_model": f"{settings.deepseek_model}{'(fallback)' if error_reason else ''}",
            "risk_level": risk_level
        }

        if error_reason:
            logger.warning(f"Review completed with fallback: {error_reason}, score={overall_score}")
        else:
            logger.info(f"Review done: score={overall_score}, risk={risk_level}")
        return result

    def _evaluate(self, work_type: str, work_description: Optional[str],
                  file_analysis: dict) -> tuple[ReviewOutput, Optional[str]]:
        """调用LLM进行评审，带降级策略。返回 (结果, 错误原因)"""

        # 第一层：结构化输出
        try:
            result = self._call_structured(work_type, work_description, file_analysis)
            return result, None
        except Exception as e:
            logger.warning(f"Structured output failed: {type(e).__name__}: {e}")

        # 第二层：原始调用 + JSON解析
        try:
            result = self._call_raw(work_type, work_description, file_analysis)
            return result, None
        except Exception as e:
            logger.warning(f"Raw LLM call failed: {type(e).__name__}: {e}")
            error_reason = f"{type(e).__name__}: {str(e)[:100]}"

        # 第三层：硬编码降级
        return self._fallback_result(error_reason), error_reason

    def _call_structured(self, work_type: str, work_description: Optional[str],
                         file_analysis: dict) -> ReviewOutput:
        system_prompt = SYSTEM_PROMPTS.get(work_type, SYSTEM_PROMPTS["CODE"])
        user_prompt = build_user_prompt(
            work_type=work_type,
            work_description=work_description,
            file_manifest=file_analysis["file_manifest"],
            file_contents=file_analysis["file_contents"],
            additional_data=file_analysis.get("stats")
        )

        messages = [
            SystemMessage(content=system_prompt),
            HumanMessage(content=user_prompt)
        ]

        result = self.structured_llm.invoke(messages)
        if result is None:
            raise ValueError("Structured output returned None")
        logger.info("Structured LLM call succeeded")
        return result

    def _call_raw(self, work_type: str, work_description: Optional[str],
                  file_analysis: dict) -> ReviewOutput:
        system_prompt = SYSTEM_PROMPTS.get(work_type, SYSTEM_PROMPTS["CODE"])
        user_prompt = build_user_prompt(
            work_type=work_type,
            work_description=work_description,
            file_manifest=file_analysis["file_manifest"],
            file_contents=file_analysis["file_contents"],
            additional_data=file_analysis.get("stats")
        )

        messages = [
            SystemMessage(content=system_prompt + "\n请严格按照以下JSON格式输出，不要包含其他内容：\n"
                          '{"innovation_score": 数字, "practicality_score": 数字, '
                          '"user_experience_score": 数字, "documentation_score": 数字, '
                          '"review_summary": "文字", "improvement_suggestions": ["建议1", "建议2"]}'),
            HumanMessage(content=user_prompt)
        ]

        response = self.llm.invoke(messages)
        content = response.content
        logger.info(f"Raw LLM response length: {len(content)}")
        logger.debug(f"Raw LLM response: {content[:500]}")

        json_str = self._extract_json(content)
        if json_str:
            try:
                data = json.loads(json_str)
                return ReviewOutput(**data)
            except (json.JSONDecodeError, Exception) as e:
                logger.warning(f"JSON parse failed: {e}, json_str preview: {json_str[:200]}")
                raise ValueError(f"JSON schema mismatch: {e}")

        raise ValueError("Cannot extract JSON from LLM response")

    def _extract_json(self, text: str) -> Optional[str]:
        text = text.strip()

        # 直接是JSON
        if text.startswith("{") and text.rstrip().endswith("}"):
            return text

        # markdown 代码块中的JSON
        patterns = [
            r"```(?:json)?\s*\n?(.*?)\n?```",
            r"`(.*?)`",
        ]
        for pattern in patterns:
            match = re.search(pattern, text, re.DOTALL)
            if match:
                candidate = match.group(1).strip()
                if candidate.startswith("{"):
                    return candidate

        # 查找第一个 { 到最后一个 }
        start = text.find("{")
        end = text.rfind("}")
        if start != -1 and end > start:
            return text[start:end + 1]

        return None

    def _fallback_result(self, error_reason: str = "") -> ReviewOutput:
        reason = error_reason or "未知错误"
        logger.warning(f"Using fallback result, reason: {reason}")
        return ReviewOutput(
            innovation_score=15.0,
            practicality_score=15.0,
            user_experience_score=15.0,
            documentation_score=15.0,
            review_summary=f"AI评审未能完成（{reason}），使用默认评分。建议人工复审确认作品质量。",
            improvement_suggestions=[
                "AI自动评审未成功，建议评委人工评审",
                "请检查AI评审服务状态后重试"
            ]
        )
