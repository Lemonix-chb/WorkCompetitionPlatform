package com.example.workcompetitionplatform.service.impl;

import com.example.workcompetitionplatform.entity.AIReviewDetail;
import com.example.workcompetitionplatform.entity.Work;
import com.example.workcompetitionplatform.service.RuleEngineService;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 规则引擎服务实现类
 * 执行基础规范性检查
 *
 * @author 陈海波
 * @since 2026-04-13
 */
@Service
public class RuleEngineServiceImpl implements RuleEngineService {

    @Override
    public List<AIReviewDetail> performBasicChecks(Long submissionId, Work.WorkType workType, Map<String, List<File>> files) {
        List<AIReviewDetail> details = new ArrayList<>();

        switch (workType) {
            case CODE:
                // 程序设计作品检查
                details.add(checkDirectoryStructure(files.get("code").isEmpty() ? null : files.get("code").get(0).getParentFile()));
                details.add(checkFileNaming(files.get("code"), workType));
                if (!files.get("code").isEmpty()) {
                    details.add(calculateCommentCoverage(files.get("code")));
                }
                break;

            case PPT:
                // 演示文稿作品检查
                if (!files.get("ppt").isEmpty()) {
                    details.add(checkPPTPageCount(files.get("ppt").get(0)));
                }
                break;

            case VIDEO:
                // 数媒视频作品检查
                if (!files.get("video").isEmpty()) {
                    details.add(checkVideoDuration(files.get("video").get(0)));
                }
                break;
        }

        return details;
    }

    @Override
    public AIReviewDetail checkDirectoryStructure(File dir) {
        AIReviewDetail detail = new AIReviewDetail();
        detail.setCheckItem("目录结构完整性");

        if (dir == null || !dir.exists()) {
            detail.setCheckResult("FAIL");
            detail.setScore(BigDecimal.ZERO);
            detail.setWeight(BigDecimal.valueOf(0.10));
            detail.setComment("目录不存在");
            return detail;
        }

        // 检查是否有README、src目录、文档
        boolean hasREADME = false;
        boolean hasSrcDir = false;
        boolean hasDocs = false;

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                String name = file.getName().toLowerCase();
                if (name.contains("readme")) {
                    hasREADME = true;
                }
                if (file.isDirectory() && name.equals("src")) {
                    hasSrcDir = true;
                }
                if (name.endsWith(".md") || name.endsWith(".txt") || name.endsWith(".doc") || name.endsWith(".docx")) {
                    hasDocs = true;
                }
            }
        }

        int score = 0;
        StringBuilder comment = new StringBuilder();

        if (hasREADME) {
            score += 4;
            comment.append("包含README文件；");
        } else {
            comment.append("缺少README文件；");
        }

        if (hasSrcDir) {
            score += 3;
            comment.append("包含src目录；");
        } else {
            comment.append("缺少src目录；");
        }

        if (hasDocs) {
            score += 3;
            comment.append("包含文档文件；");
        } else {
            comment.append("缺少文档文件；");
        }

        detail.setCheckResult(score >= 7 ? "PASS" : "FAIL");
        detail.setScore(BigDecimal.valueOf(score));
        detail.setWeight(BigDecimal.valueOf(0.10));
        detail.setComment(comment.toString());

        return detail;
    }

    @Override
    public AIReviewDetail checkFileNaming(List<File> files, Work.WorkType workType) {
        AIReviewDetail detail = new AIReviewDetail();
        detail.setCheckItem("文件命名规范");

        if (files == null || files.isEmpty()) {
            detail.setCheckResult("FAIL");
            detail.setScore(BigDecimal.ZERO);
            detail.setWeight(BigDecimal.valueOf(0.05));
            detail.setComment("没有找到文件");
            return detail;
        }

        // 检查文件名是否符合规范（无特殊字符、无中文）
        int validCount = 0;
        StringBuilder comment = new StringBuilder();

        for (File file : files) {
            String name = file.getName();
            // 简单检查：只允许字母、数字、下划线、连字符、点号
            if (name.matches("[a-zA-Z0-9_\\-\\.]+")) {
                validCount++;
            } else {
                comment.append("文件 ").append(name).append(" 命名不规范；");
            }
        }

        BigDecimal score = BigDecimal.valueOf(files.size())
            .divide(BigDecimal.valueOf(5), 2, RoundingMode.HALF_UP)
            .min(BigDecimal.valueOf(5));

        detail.setCheckResult(validCount >= files.size() * 0.8 ? "PASS" : "FAIL");
        detail.setScore(score);
        detail.setWeight(BigDecimal.valueOf(0.05));
        detail.setComment(comment.length() > 0 ? comment.toString() : "文件命名规范");

        return detail;
    }

    @Override
    public AIReviewDetail calculateCommentCoverage(List<File> codeFiles) {
        AIReviewDetail detail = new AIReviewDetail();
        detail.setCheckItem("注释覆盖率");

        if (codeFiles == null || codeFiles.isEmpty()) {
            detail.setCheckResult("FAIL");
            detail.setScore(BigDecimal.ZERO);
            detail.setWeight(BigDecimal.valueOf(0.10));
            detail.setComment("没有代码文件");
            return detail;
        }

        int totalLines = 0;
        int commentLines = 0;

        for (File file : codeFiles) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    totalLines++;
                    String trimmed = line.trim();
                    // 检查是否为注释行（支持多种语言）
                    if (trimmed.startsWith("//") || trimmed.startsWith("#") ||
                        trimmed.startsWith("/*") || trimmed.startsWith("*") ||
                        trimmed.startsWith("--")) {
                        commentLines++;
                    }
                }
            } catch (Exception e) {
                // 忽略读取失败的文件
            }
        }

        if (totalLines == 0) {
            detail.setCheckResult("FAIL");
            detail.setScore(BigDecimal.ZERO);
            detail.setWeight(BigDecimal.valueOf(0.10));
            detail.setComment("代码文件为空");
            return detail;
        }

        // 计算注释覆盖率
        BigDecimal coverage = BigDecimal.valueOf(commentLines)
            .divide(BigDecimal.valueOf(totalLines), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));

        // 评分标准：≥30%满分，<10% 0分，中间线性
        BigDecimal score;
        if (coverage.compareTo(BigDecimal.valueOf(30)) >= 0) {
            score = BigDecimal.valueOf(10);
        } else if (coverage.compareTo(BigDecimal.valueOf(10)) < 0) {
            score = BigDecimal.ZERO;
        } else {
            // 线性映射 10-30% → 0-10分
            score = coverage.subtract(BigDecimal.valueOf(10))
                .divide(BigDecimal.valueOf(20), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(10))
                .min(BigDecimal.valueOf(10));
        }

        detail.setCheckResult(coverage.compareTo(BigDecimal.valueOf(15)) >= 0 ? "PASS" : "FAIL");
        detail.setScore(score);
        detail.setWeight(BigDecimal.valueOf(0.10));
        detail.setComment(String.format("注释覆盖率 %.1f%% (%d/%d行)",
            coverage.doubleValue(), commentLines, totalLines));

        return detail;
    }

    @Override
    public AIReviewDetail checkPPTPageCount(File pptFile) {
        AIReviewDetail detail = new AIReviewDetail();
        detail.setCheckItem("演示文稿页数");

        if (pptFile == null || !pptFile.exists()) {
            detail.setCheckResult("FAIL");
            detail.setScore(BigDecimal.ZERO);
            detail.setWeight(BigDecimal.valueOf(0.20));
            detail.setComment("文件不存在");
            return detail;
        }

        // 简化实现：仅基于文件大小估算页数
        // 实际应使用Apache POI解析PPT文件
        long fileSizeKB = pptFile.length() / 1024;

        // 假设每页平均100KB
        int estimatedPages = (int) (fileSizeKB / 100);
        estimatedPages = Math.max(1, estimatedPages); // 至少1页

        BigDecimal score;
        String checkResult;

        if (estimatedPages >= 12) {
            score = BigDecimal.valueOf(20);
            checkResult = "PASS";
        } else if (estimatedPages >= 8) {
            score = BigDecimal.valueOf(estimatedPages * 1.5);
            checkResult = "FAIL";
        } else {
            score = BigDecimal.valueOf(estimatedPages);
            checkResult = "FAIL";
        }

        detail.setCheckResult(checkResult);
        detail.setScore(score.min(BigDecimal.valueOf(20)));
        detail.setWeight(BigDecimal.valueOf(0.20));
        detail.setComment(String.format("估算页数约 %d 页（文件大小 %d KB）", estimatedPages, fileSizeKB));

        return detail;
    }

    @Override
    public AIReviewDetail checkVideoDuration(File videoFile) {
        AIReviewDetail detail = new AIReviewDetail();
        detail.setCheckItem("视频时长");

        if (videoFile == null || !videoFile.exists()) {
            detail.setCheckResult("FAIL");
            detail.setScore(BigDecimal.ZERO);
            detail.setWeight(BigDecimal.valueOf(0.15));
            detail.setComment("文件不存在");
            return detail;
        }

        // 简化实现：基于文件大小估算时长
        // 实际应使用FFmpeg或MediaInfo库获取精确时长
        long fileSizeMB = videoFile.length() / (1024 * 1024);

        // 假设1080p视频，每秒约5MB
        int estimatedDurationSeconds = (int) (fileSizeMB / 5);
        estimatedDurationSeconds = Math.max(10, estimatedDurationSeconds); // 至少10秒

        BigDecimal score;
        String checkResult;

        if (estimatedDurationSeconds >= 60 && estimatedDurationSeconds <= 180) {
            score = BigDecimal.valueOf(15);
            checkResult = "PASS";
        } else if (estimatedDurationSeconds < 60) {
            score = BigDecimal.valueOf(estimatedDurationSeconds / 4);
            checkResult = "FAIL";
        } else {
            // 超过180秒，逐渐扣分
            score = BigDecimal.valueOf(15)
                .subtract(BigDecimal.valueOf(Math.min(10, (estimatedDurationSeconds - 180) / 10)));
            checkResult = "FAIL";
        }

        detail.setCheckResult(checkResult);
        detail.setScore(score.max(BigDecimal.ZERO).min(BigDecimal.valueOf(15)));
        detail.setWeight(BigDecimal.valueOf(0.15));
        detail.setComment(String.format("估算时长约 %d 秒（文件大小 %d MB）", estimatedDurationSeconds, fileSizeMB));

        return detail;
    }

    @Override
    public BigDecimal calculateBasicScore(List<AIReviewDetail> basicChecks) {
        if (basicChecks == null || basicChecks.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (AIReviewDetail detail : basicChecks) {
            if (detail.getScore() != null && detail.getWeight() != null) {
                totalScore = totalScore.add(detail.getScore());
                totalWeight = totalWeight.add(detail.getWeight());
            }
        }

        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // 计算加权平均分（转换为百分制）
        return totalScore.divide(totalWeight, 2, RoundingMode.HALF_UP);
    }
}