package com.example.workcompetitionplatform.service.impl;

import com.example.workcompetitionplatform.entity.AIReviewDetail;
import com.example.workcompetitionplatform.entity.AIReviewReport;
import com.example.workcompetitionplatform.service.CodeQualityService;
import com.example.workcompetitionplatform.service.FileProcessingService;
import com.example.workcompetitionplatform.util.FileUtils;
import com.example.workcompetitionplatform.util.ProcessExecutor;
import com.example.workcompetitionplatform.util.ProcessExecutor.ProcessResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * 代码质量分析服务实现类
 * 提供代码查重、风格检查、复杂度分析功能
 *
 * 集成真实工具：
 * - JPlag: 专业代码查重工具（支持多语言）
 * - Checkstyle: Java代码风格检查工具
 *
 * @author 陈海波
 * @since 2026-04-13
 */
@Service
public class CodeQualityServiceImpl implements CodeQualityService {

    private static final Logger log = LoggerFactory.getLogger(CodeQualityServiceImpl.class);

    // 进程执行超时配置（秒）
    private static final int JPLAG_TIMEOUT_SECONDS = 30;
    private static final int CHECKSTYLE_TIMEOUT_SECONDS = 30;

    @Autowired
    private FileProcessingService fileProcessingService;

    @Override
    public BigDecimal checkDuplicateRate(Long submissionId, List<File> codeFiles) {
        if (codeFiles == null || codeFiles.isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            // 真实集成JPlag代码查重工具
            // 支持两种方式：Docker容器 或 命令行工具

            // 1. 准备代码目录
            String codeDir = prepareCodeDirectory(submissionId, codeFiles);

            // 2. 检测代码语言（根据文件扩展名）
            String language = detectCodeLanguage(codeFiles);

            // 3. 尝试使用Docker方式运行JPlag
            BigDecimal duplicateRate = runJPlagWithDocker(codeDir, language, submissionId);

            // 4. 如果Docker失败，fallback到命令行方式
            if (duplicateRate == null) {
                duplicateRate = runJPlagCommandLine(codeDir, language, submissionId);
            }

            // 5. 如果都失败，使用简化算法作为最终fallback
            if (duplicateRate == null) {
                duplicateRate = calculateSimpleDuplicateRate(codeFiles);
            }

            return duplicateRate;

        } catch (Exception e) {
            // 异常时返回简化算法结果
            return calculateSimpleDuplicateRate(codeFiles);
        }
    }

    /**
     * 准备代码目录（将所有代码文件复制到统一目录）
     */
    private String prepareCodeDirectory(Long submissionId, List<File> codeFiles) {
        String baseDir = fileProcessingService.getTempDirPath(submissionId);
        String codeDir = baseDir + "/jplag_code";

        File dir = new File(codeDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 复制代码文件到目录（保持相对路径结构）
        for (File file : codeFiles) {
            try {
                String relativePath = file.getName();
                File targetFile = new File(codeDir, relativePath);

                // 简单复制文件内容
                Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                // 忽略复制失败的文件
            }
        }

        return codeDir;
    }

    /**
     * 检测代码语言类型
     */
    private String detectCodeLanguage(List<File> codeFiles) {
        if (codeFiles.isEmpty()) {
            return "java";
        }

        String fileName = codeFiles.get(0).getName().toLowerCase();

        if (fileName.endsWith(".py")) {
            return "python3";
        } else if (fileName.endsWith(".c")) {
            return "c";
        } else if (fileName.endsWith(".cpp") || fileName.endsWith(".cc")) {
            return "cpp";
        } else if (fileName.endsWith(".java")) {
            return "java";
        } else {
            return "java"; // 默认Java
        }
    }

    /**
     * 使用Docker容器运行JPlag（带超时）
     */
    private BigDecimal runJPlagWithDocker(String codeDir, String language, Long submissionId) {
        try {
            List<String> command = List.of(
                "docker", "run", "--rm",
                "-v", codeDir + ":/data",
                "jplag/jplag:latest",
                "-l", language,
                "-s", "/data",
                "-r", "/data/result",
                "-n", "-t", "20"
            );

            ProcessResult result = ProcessExecutor.execute(command, JPLAG_TIMEOUT_SECONDS);

            if (!result.isSuccess()) {
                log.warn("JPlag Docker execution failed with exit code {}", result.getExitCode());
                return null;
            }

            // 解析结果文件
            File resultFile = new File(codeDir + "/result/matches.csv");
            if (resultFile.exists()) {
                return parseJPlagResult(resultFile);
            }

            // 从输出中解析
            return parseJPlagOutput(result.getOutput());

        } catch (Exception e) {
            log.warn("JPlag Docker execution failed for submission {}: {}", submissionId, e.getMessage());
            return null;
        }
    }

    /**
     * 使用命令行工具运行JPlag（带超时）
     */
    private BigDecimal runJPlagCommandLine(String codeDir, String language, Long submissionId) {
        try {
            List<String> command = List.of(
                "jplag",
                "-l", language,
                "-s", codeDir,
                "-r", codeDir + "/result",
                "-n"
            );

            ProcessResult result = ProcessExecutor.execute(command, JPLAG_TIMEOUT_SECONDS);

            if (!result.isSuccess()) {
                log.warn("JPlag command-line execution failed with exit code {}", result.getExitCode());
                return null;
            }

            File resultFile = new File(codeDir + "/result/matches.csv");
            if (resultFile.exists()) {
                return parseJPlagResult(resultFile);
            }

            return parseJPlagOutput(result.getOutput());

        } catch (Exception e) {
            log.warn("JPlag command-line execution failed for submission {}: {}", submissionId, e.getMessage());
            return null;
        }
    }

    /**
     * 解析JPlag结果CSV文件
     */
    private BigDecimal parseJPlagResult(File resultFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(resultFile))) {
            String line;
            BigDecimal maxSimilarity = BigDecimal.ZERO;

            while ((line = reader.readLine()) != null) {
                if (line.contains(",")) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        try {
                            BigDecimal similarity = new BigDecimal(parts[2].trim());
                            if (similarity.compareTo(maxSimilarity) > 0) {
                                maxSimilarity = similarity;
                            }
                        } catch (NumberFormatException e) {
                            // 忽略
                        }
                    }
                }
            }

            return maxSimilarity;

        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 从JPlag输出文本中解析重复率
     */
    private BigDecimal parseJPlagOutput(String output) {
        try {
            String[] lines = output.split("\n");
            for (String line : lines) {
                if (line.toLowerCase().contains("similarity") || line.contains("%")) {
                    String[] parts = line.split(":");
                    if (parts.length >= 2) {
                        String percentage = parts[parts.length - 1].trim()
                            .replace("%", "")
                            .replace(" ", "");

                        try {
                            return new BigDecimal(percentage);
                        } catch (NumberFormatException e) {
                            // 继续
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 解析失败
        }

        return BigDecimal.ZERO;
    }

    /**
     * 简化算法：基于行匹配的重复率计算（Fallback）
     * 使用FileUtils消除重复的文件读取逻辑
     */
    private BigDecimal calculateSimpleDuplicateRate(List<File> codeFiles) {
        try {
            List<String> codeContents = new ArrayList<>();
            for (File file : codeFiles) {
                String content = FileUtils.readFileContent(file, -1);
                codeContents.add(content);
            }

            int totalLines = 0;
            int duplicateLines = 0;

            for (int i = 0; i < codeContents.size(); i++) {
                String[] lines1 = codeContents.get(i).split("\n");
                totalLines += lines1.length;

                for (int j = i + 1; j < codeContents.size(); j++) {
                    String[] lines2 = codeContents.get(j).split("\n");
                    for (String line1 : lines1) {
                        for (String line2 : lines2) {
                            if (line1.equals(line2) && line1.length() > 10) {
                                duplicateLines++;
                                break;
                            }
                        }
                    }
                }
            }

            if (totalLines == 0) {
                return BigDecimal.ZERO;
            }

            BigDecimal duplicateRate = BigDecimal.valueOf(duplicateLines)
                .divide(BigDecimal.valueOf(totalLines), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

            return duplicateRate.min(BigDecimal.valueOf(100));

        } catch (Exception e) {
            return BigDecimal.valueOf(5.0);
        }
    }

    @Override
    public AIReviewDetail checkCodeStyle(File codeFile) {
        AIReviewDetail detail = new AIReviewDetail();
        detail.setCheckItem("代码风格检查");

        if (codeFile == null || !codeFile.exists()) {
            detail.setCheckResult("FAIL");
            detail.setScore(BigDecimal.ZERO);
            detail.setWeight(BigDecimal.valueOf(0.20));
            detail.setComment("文件不存在");
            return detail;
        }

        // 尝试使用真实Checkstyle工具
        BigDecimal checkstyleScore = runCheckstyle(codeFile);

        if (checkstyleScore != null) {
            detail.setCheckResult(checkstyleScore.compareTo(BigDecimal.valueOf(15)) >= 0 ? "PASS" : "FAIL");
            detail.setScore(checkstyleScore);
            detail.setWeight(BigDecimal.valueOf(0.20));
            detail.setComment(String.format("Checkstyle评分: %.1f分", checkstyleScore.doubleValue()));
            detail.setFilePath(codeFile.getName());
            return detail;
        }

        // Fallback: 简化检查
        return checkCodeStyleSimple(codeFile);
    }

    /**
     * 使用真实Checkstyle工具检查代码风格
     */
    private BigDecimal runCheckstyle(File codeFile) {
        try {
            // 检查文件是否为Java文件
            if (!codeFile.getName().toLowerCase().endsWith(".java")) {
                return null; // Checkstyle只支持Java
            }

            // 方式1: Docker运行Checkstyle
            BigDecimal score = runCheckstyleWithDocker(codeFile);
            if (score != null) {
                return score;
            }

            // 方式2: 命令行运行Checkstyle
            score = runCheckstyleCommandLine(codeFile);
            if (score != null) {
                return score;
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Docker方式运行Checkstyle（带超时）
     */
    private BigDecimal runCheckstyleWithDocker(File codeFile) {
        try {
            String filePath = codeFile.getAbsolutePath();

            List<String> command = List.of(
                "docker", "run", "--rm",
                "-v", filePath + ":/data/" + codeFile.getName(),
                "checkstyle/checkstyle:latest",
                "-c", "/google_checks.xml",
                "/data/" + codeFile.getName()
            );

            ProcessResult result = ProcessExecutor.execute(command, CHECKSTYLE_TIMEOUT_SECONDS);

            // 解析输出中的违规数量
            int violations = parseCheckstyleOutput(result.getOutput());

            // 计算评分：0违规=20分，每1个违规扣2分，最低5分
            BigDecimal score = BigDecimal.valueOf(Math.max(5, 20 - violations * 2));

            return score;

        } catch (Exception e) {
            log.warn("Checkstyle Docker execution failed for file {}: {}", codeFile.getName(), e.getMessage());
            return null;
        }
    }

    /**
     * 命令行方式运行Checkstyle（带超时）
     */
    private BigDecimal runCheckstyleCommandLine(File codeFile) {
        try {
            List<String> command = List.of(
                "checkstyle",
                "-c", "google_checks.xml",
                codeFile.getAbsolutePath()
            );

            ProcessResult result = ProcessExecutor.execute(command, CHECKSTYLE_TIMEOUT_SECONDS);

            int violations = parseCheckstyleOutput(result.getOutput());
            BigDecimal score = BigDecimal.valueOf(Math.max(5, 20 - violations * 2));

            return score;

        } catch (Exception e) {
            log.warn("Checkstyle command-line execution failed for file {}: {}", codeFile.getName(), e.getMessage());
            return null;
        }
    }

    /**
     * 解析Checkstyle输出获取违规数量
     */
    private int parseCheckstyleOutput(String output) {
        try {
            // Checkstyle输出格式：
            // [ERROR] /path/to/file.java:10: Line is longer than 100 characters.
            int violations = 0;

            String[] lines = output.split("\n");
            for (String line : lines) {
                if (line.contains("[ERROR]") || line.contains("[WARN]")) {
                    violations++;
                }
            }

            return violations;

        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 简化检查：基于基本规则（Fallback）
     * 使用FileUtils消除重复文件读取
     */
    private AIReviewDetail checkCodeStyleSimple(File codeFile) {
        AIReviewDetail detail = new AIReviewDetail();

        int violations = 0;
        StringBuilder comment = new StringBuilder();

        try {
            List<String> lines = FileUtils.readFileLines(codeFile, -1);
            int lineNum = 0;

            for (String line : lines) {
                lineNum++;

                if (line.length() > 120) {
                    violations++;
                    if (comment.length() == 0) {
                        comment.append("行").append(lineNum).append("过长");
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to read file for style check: {}", codeFile.getName(), e);
            detail.setCheckResult("FAIL");
            detail.setScore(BigDecimal.ZERO);
            detail.setWeight(BigDecimal.valueOf(0.20));
            detail.setComment("文件读取失败");
            return detail;
        }

        BigDecimal score;
        if (violations == 0) {
            score = BigDecimal.valueOf(20);
            comment.append("代码风格良好");
        } else if (violations <= 3) {
            score = BigDecimal.valueOf(15);
            comment.append("有").append(violations).append("处风格问题");
        } else {
            score = BigDecimal.valueOf(Math.max(5, 20 - violations * 3));
            comment.append("有").append(violations).append("处风格问题，建议改进");
        }

        detail.setCheckResult(violations <= 3 ? "PASS" : "FAIL");
        detail.setScore(score);
        detail.setWeight(BigDecimal.valueOf(0.20));
        detail.setComment(comment.toString());
        detail.setFilePath(codeFile.getName());

        return detail;
    }

    @Override
    public AIReviewReport.ComplexityLevel analyzeFunctionComplexity(List<File> codeFiles) {
        if (codeFiles == null || codeFiles.isEmpty()) {
            return AIReviewReport.ComplexityLevel.LOW;
        }

        int totalFunctions = 0;
        int totalFunctionLines = 0;

        for (File file : codeFiles) {
            try {
                List<String> lines = FileUtils.readFileLines(file, -1);
                int braceCount = 0;
                boolean inFunction = false;

                for (String line : lines) {
                    // 检测函数定义
                    if (line.contains("def ") || line.contains("function ") ||
                        line.contains("public ") || line.contains("private ") ||
                        line.contains("void ") || line.contains("int ") ||
                        line.contains("class ")) {

                        if (inFunction) {
                            totalFunctions++;
                            totalFunctionLines += braceCount;
                        }

                        inFunction = true;
                        braceCount = 0;
                    }

                    // 计算代码块深度
                    for (char c : line.toCharArray()) {
                        if (c == '{') braceCount++;
                        if (c == '}') braceCount--;
                    }
                }

                if (inFunction) {
                    totalFunctions++;
                    totalFunctionLines += braceCount;
                }
            } catch (Exception e) {
                log.warn("Failed to analyze complexity for file: {}", file.getName());
            }
        }

        if (totalFunctions == 0) {
            return AIReviewReport.ComplexityLevel.LOW;
        }

        int avgFunctionLines = totalFunctionLines / totalFunctions;

        if (avgFunctionLines <= 20) {
            return AIReviewReport.ComplexityLevel.LOW;
        } else if (avgFunctionLines <= 40) {
            return AIReviewReport.ComplexityLevel.MEDIUM;
        } else {
            return AIReviewReport.ComplexityLevel.HIGH;
        }
    }

    @Override
    public BigDecimal calculateCodeQualityScore(AIReviewDetail styleCheckResult,
                                                BigDecimal duplicateRate,
                                                AIReviewReport.ComplexityLevel complexityLevel) {
        BigDecimal styleScore = BigDecimal.ZERO;
        if (styleCheckResult != null && styleCheckResult.getScore() != null) {
            styleScore = styleCheckResult.getScore();
        }

        BigDecimal duplicateScore;
        if (duplicateRate.compareTo(BigDecimal.valueOf(10)) < 0) {
            duplicateScore = BigDecimal.valueOf(20);
        } else if (duplicateRate.compareTo(BigDecimal.valueOf(30)) > 0) {
            duplicateScore = BigDecimal.ZERO;
        } else {
            duplicateScore = BigDecimal.valueOf(20)
                .subtract(duplicateRate.subtract(BigDecimal.valueOf(10))
                    .multiply(BigDecimal.valueOf(1)));
        }

        BigDecimal complexityScore;
        switch (complexityLevel) {
            case LOW:
                complexityScore = BigDecimal.valueOf(20);
                break;
            case MEDIUM:
                complexityScore = BigDecimal.valueOf(15);
                break;
            case HIGH:
                complexityScore = BigDecimal.valueOf(10);
                break;
            default:
                complexityScore = BigDecimal.valueOf(15);
        }

        BigDecimal totalScore = styleScore.multiply(BigDecimal.valueOf(0.4))
            .add(duplicateScore.multiply(BigDecimal.valueOf(0.4))
            .add(complexityScore.multiply(BigDecimal.valueOf(0.2))));

        return totalScore.min(BigDecimal.valueOf(100));
    }
}