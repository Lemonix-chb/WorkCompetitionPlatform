# Pydantic request/response models
from pydantic import BaseModel, Field, ConfigDict
from typing import Optional, List


class ReviewRequest(BaseModel):
    """Spring Boot提交的审核请求"""
    submission_id: int = Field(..., description="提交ID")
    work_type: str = Field(..., description="作品类型：CODE/PPT/VIDEO")
    file_path: str = Field(..., description="作品文件路径")
    work_description: Optional[str] = Field(None, description="作品描述")


class ReviewResponse(BaseModel):
    """审核响应"""
    submission_id: int
    status: str = Field(..., description="processing/completed/failed")
    message: str = Field(..., description="状态说明")


class AIReviewReport(BaseModel):
    """AI审核报告（回调给Spring Boot）"""
    model_config = ConfigDict(populate_by_name=True)

    overallScore: float = Field(default=80.0, ge=0, le=100, alias="overall_score")
    innovationScore: float = Field(default=20.0, ge=0, le=25, alias="innovation_score")
    practicalityScore: float = Field(default=20.0, ge=0, le=25, alias="practicality_score")
    userExperienceScore: float = Field(default=20.0, ge=0, le=25, alias="user_experience_score")
    documentationScore: float = Field(default=20.0, ge=0, le=25, alias="documentation_score")

    duplicateRate: Optional[float] = Field(None, description="代码重复率", alias="duplicate_rate")
    codeQualityScore: Optional[float] = Field(None, description="代码质量评分", alias="code_quality_score")

    reviewSummary: str = Field(default="作品整体良好", description="评审摘要", alias="review_summary")
    improvementSuggestions: List[str] = Field(default_factory=list, description="改进建议", alias="improvement_suggestions")

    aiModel: str = Field(..., description="使用的AI模型", alias="ai_model")
    riskLevel: str = Field(default="LOW", description="风险等级：LOW/MEDIUM/HIGH", alias="risk_level")


class AIReviewCallbackRequest(BaseModel):
    """Python Agent回调给Spring Boot的请求"""
    submission_id: int
    report: AIReviewReport