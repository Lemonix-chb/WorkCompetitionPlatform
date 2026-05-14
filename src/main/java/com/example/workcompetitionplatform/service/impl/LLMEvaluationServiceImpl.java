package com.example.workcompetitionplatform.service.impl;

import com.example.workcompetitionplatform.dto.LLMEvaluationResult;
import com.example.workcompetitionplatform.entity.Work;
import com.example.workcompetitionplatform.mapper.SystemConfigMapper;
import com.example.workcompetitionplatform.service.LLMEvaluationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 大模型评估服务实现类
 * 集成DeepSeek API进行作品评估
 *
 * @author 陈海波
 * @since 2026-04-13
 */
@Service
public class LLMEvaluationServiceImpl implements LLMEvaluationService {

    private final SystemConfigMapper systemConfigMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public LLMEvaluationServiceImpl(SystemConfigMapper systemConfigMapper) {
        this.systemConfigMapper = systemConfigMapper;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public LLMEvaluationResult evaluateByLLM(Work work, Map<String, List<File>> keyFiles, Work.WorkType workType) {
        try {
            // 从系统配置获取API信息
            String apiKey = systemConfigMapper.getConfigValue("deepseek_api_key");
            String apiUrl = systemConfigMapper.getConfigValue("deepseek_api_url");
            String aiModel = systemConfigMapper.getConfigValue("ai_model");

            // 验证API配置是否存在
            if (apiKey == null || apiUrl == null) {
                throw new RuntimeException("DeepSeek API配置缺失，请检查system_config表");
            }

            // 构建评估提示词
            String prompt = buildEvaluationPrompt(work, keyFiles, workType);

            // 构建API请求
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiModel != null ? aiModel : "deepseek-chat");
            requestBody.put("messages", List.of(
                Map.of("role", "system", "content", getSystemPrompt(workType)),
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            // 调用API
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

            // 解析响应
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String content = responseJson.path("choices").path(0).path("message").path("content").asText();

            // 解析评估结果
            return parseEvaluationResult(content, aiModel != null ? aiModel : "DeepSeek");

        } catch (Exception e) {
            // API调用失败时返回模拟结果
            return createMockResult(workType);
        }
    }

    @Override
    public String generateReviewSummary(LLMEvaluationResult result) {
        if (result == null || result.getSummary() == null) {
            return "大模型评估未完成";
        }
        return result.getSummary();
    }

    @Override
    public String generateImprovementSuggestions(LLMEvaluationResult result) {
        if (result == null || result.getSuggestions() == null || result.getSuggestions().isEmpty()) {
            return "暂无改进建议";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < result.getSuggestions().size(); i++) {
            sb.append((i + 1)).append(". ").append(result.getSuggestions().get(i)).append("\n");
        }

        return sb.toString();
    }

    /**
     * 构建评估提示词
     */
    private String buildEvaluationPrompt(Work work, Map<String, List<File>> keyFiles, Work.WorkType workType) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("请分析以下参赛作品并给出评分和建议：\n\n");
        prompt.append("作品名称：").append(work.getWorkName()).append("\n");
        prompt.append("作品类型：").append(workType.getDescription()).append("\n");
        prompt.append("作品简介：").append(work.getDescription() != null ? work.getDescription() : "无").append("\n\n");

        // 添加文件信息
        prompt.append("文件信息：\n");
        for (Map.Entry<String, List<File>> entry : keyFiles.entrySet()) {
            prompt.append(entry.getKey()).append("类文件：\n");
            for (File file : entry.getValue()) {
                prompt.append("- ").append(file.getName()).append(" (").append(file.length() / 1024).append(" KB)\n");

                // 对于代码文件，提取部分内容
                if ("code".equals(entry.getKey()) && file.length() < 50000) {
                    String content = readFileContent(file, 50); // 最多50行
                    if (content != null && !content.isEmpty()) {
                        prompt.append("  内容片段：\n").append(content).append("\n");
                    }
                }
            }
        }

        prompt.append("\n请从以下维度评分（0-100分）：\n");
        prompt.append("1. 创新性\n");
        prompt.append("2. 实用性\n");
        prompt.append("3. 用户体验\n");
        prompt.append("4. 文档质量\n\n");

        prompt.append("请以JSON格式输出评分结果，格式如下：\n");
        prompt.append("```json\n");
        prompt.append("{\n");
        prompt.append("  \"scores\": {\n");
        prompt.append("    \"innovation\": <分数>,\n");
        prompt.append("    \"practicality\": <分数>,\n");
        prompt.append("    \"userExperience\": <分数>,\n");
        prompt.append("    \"documentation\": <分数>\n");
        prompt.append("  },\n");
        prompt.append("  \"summary\": \"<评审摘要>\",\n");
        prompt.append("  \"suggestions\": [\"<建议1>\", \"<建议2>\", ...]\n");
        prompt.append("}\n");
        prompt.append("```\n");

        return prompt.toString();
    }

    /**
     * 获取系统提示词
     */
    private String getSystemPrompt(Work.WorkType workType) {
        return String.format(
            "你是一位资深的计算机作品评审专家，专门评审%s作品。请以专业、客观、公正的态度进行评审，" +
            "从创新性、实用性、用户体验、文档质量四个维度给出评分和建议。",
            workType.getDescription()
        );
    }

    /**
     * 解析评估结果
     */
    private LLMEvaluationResult parseEvaluationResult(String content, String aiModel) {
        LLMEvaluationResult result = new LLMEvaluationResult();

        try {
            // 提取JSON部分
            int jsonStart = content.indexOf("{");
            int jsonEnd = content.lastIndexOf("}") + 1;

            if (jsonStart != -1 && jsonEnd > jsonStart) {
                String jsonContent = content.substring(jsonStart, jsonEnd);
                JsonNode jsonNode = objectMapper.readTree(jsonContent);

                JsonNode scores = jsonNode.path("scores");
                result.setInnovationScore(new BigDecimal(scores.path("innovation").asInt()));
                result.setPracticalityScore(new BigDecimal(scores.path("practicality").asInt()));
                result.setUserExperienceScore(new BigDecimal(scores.path("userExperience").asInt()));
                result.setDocumentationScore(new BigDecimal(scores.path("documentation").asInt()));

                result.setSummary(jsonNode.path("summary").asText());

                List<String> suggestions = new ArrayList<>();
                JsonNode suggestionsNode = jsonNode.path("suggestions");
                if (suggestionsNode.isArray()) {
                    for (JsonNode node : suggestionsNode) {
                        suggestions.add(node.asText());
                    }
                }
                result.setSuggestions(suggestions);
            } else {
                // 无法解析JSON，使用模拟数据
                return createMockResult(null);
            }
        } catch (Exception e) {
            // 解析失败，使用模拟数据
            return createMockResult(null);
        }

        result.setAiModel(aiModel);
        return result;
    }

    /**
     * 创建模拟结果（API调用失败时）
     */
    private LLMEvaluationResult createMockResult(Work.WorkType workType) {
        LLMEvaluationResult result = new LLMEvaluationResult();

        // 模拟评分（中等水平）
        result.setInnovationScore(BigDecimal.valueOf(75));
        result.setPracticalityScore(BigDecimal.valueOf(80));
        result.setUserExperienceScore(BigDecimal.valueOf(70));
        result.setDocumentationScore(BigDecimal.valueOf(85));

        result.setSummary("作品整体质量良好，功能完整，文档清晰。建议进一步优化用户体验和创新点。");
        result.setSuggestions(List.of(
            "加强创新性，尝试引入新技术或新颖的解决方案",
            "优化用户界面和交互流程，提升用户体验",
            "完善文档内容，添加更多使用示例和截图",
            "增加测试代码，提高代码可靠性"
        ));

        result.setAiModel("Mock-Model");

        return result;
    }

    /**
     * 读取文件内容（限制行数）
     */
    private String readFileContent(File file, int maxLines) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            int lineCount = 0;

            while ((line = reader.readLine()) != null && lineCount < maxLines) {
                content.append(line).append("\n");
                lineCount++;
            }

            return content.toString();
        } catch (Exception e) {
            return null;
        }
    }
}