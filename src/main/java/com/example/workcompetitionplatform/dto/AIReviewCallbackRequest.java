package com.example.workcompetitionplatform.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * AI审核回调请求DTO
 * Python Agent回调给Spring Boot的数据结构
 *
 * @author 陈海波
 * @since 2026-04-22
 */
public class AIReviewCallbackRequest {

    private Long submissionId;

    private AIReviewReportDTO report;

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public AIReviewReportDTO getReport() {
        return report;
    }

    public void setReport(AIReviewReportDTO report) {
        this.report = report;
    }

    /**
     * AI审核报告DTO（Python Agent返回）
     */
    public static class AIReviewReportDTO {

        private BigDecimal overallScore;
        private BigDecimal innovationScore;
        private BigDecimal practicalityScore;
        private BigDecimal userExperienceScore;
        private BigDecimal documentationScore;

        private BigDecimal duplicateRate;
        private BigDecimal codeQualityScore;

        private String reviewSummary;
        private List<String> improvementSuggestions;

        private String aiModel;
        private String riskLevel;

        // Getters and Setters
        public BigDecimal getOverallScore() {
            return overallScore;
        }

        public void setOverallScore(BigDecimal overallScore) {
            this.overallScore = overallScore;
        }

        public BigDecimal getInnovationScore() {
            return innovationScore;
        }

        public void setInnovationScore(BigDecimal innovationScore) {
            this.innovationScore = innovationScore;
        }

        public BigDecimal getPracticalityScore() {
            return practicalityScore;
        }

        public void setPracticalityScore(BigDecimal practicalityScore) {
            this.practicalityScore = practicalityScore;
        }

        public BigDecimal getUserExperienceScore() {
            return userExperienceScore;
        }

        public void setUserExperienceScore(BigDecimal userExperienceScore) {
            this.userExperienceScore = userExperienceScore;
        }

        public BigDecimal getDocumentationScore() {
            return documentationScore;
        }

        public void setDocumentationScore(BigDecimal documentationScore) {
            this.documentationScore = documentationScore;
        }

        public BigDecimal getDuplicateRate() {
            return duplicateRate;
        }

        public void setDuplicateRate(BigDecimal duplicateRate) {
            this.duplicateRate = duplicateRate;
        }

        public BigDecimal getCodeQualityScore() {
            return codeQualityScore;
        }

        public void setCodeQualityScore(BigDecimal codeQualityScore) {
            this.codeQualityScore = codeQualityScore;
        }

        public String getReviewSummary() {
            return reviewSummary;
        }

        public void setReviewSummary(String reviewSummary) {
            this.reviewSummary = reviewSummary;
        }

        public List<String> getImprovementSuggestions() {
            return improvementSuggestions;
        }

        public void setImprovementSuggestions(List<String> improvementSuggestions) {
            this.improvementSuggestions = improvementSuggestions;
        }

        public String getAiModel() {
            return aiModel;
        }

        public void setAiModel(String aiModel) {
            this.aiModel = aiModel;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
        }
    }
}