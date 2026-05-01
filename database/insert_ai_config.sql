-- 插入DeepSeek API配置
-- 执行时间：2026-04-13
-- 作者：陈海波

INSERT INTO system_config (config_key, config_value, config_type, config_group, description, deleted) VALUES
('deepseek_api_key', 'sk-325ae1ccf357480ab353a41e8b26ee32', 'STRING', 'AI_CONFIG', 'DeepSeek API密钥', 0),
('deepseek_api_url', 'https://api.deepseek.com/v1/chat/completions', 'STRING', 'AI_CONFIG', 'DeepSeek API地址', 0),
('ai_model', 'deepseek-chat', 'STRING', 'AI_CONFIG', '使用的AI模型名称', 0),
('ai_duplicate_threshold', '30', 'NUMBER', 'AI_THRESHOLD', '重复率高风险阈值(%)', 0),
('ai_comment_threshold', '30', 'NUMBER', 'AI_THRESHOLD', '注释覆盖率最低阈值(%)', 0),
('ai_max_file_size', '300', 'NUMBER', 'AI_LIMIT', '最大文件大小限制(MB)', 0);

-- 更新已存在的配置（如果之前已插入）
UPDATE system_config SET config_value = 'sk-325ae1ccf357480ab353a41e8b26ee32' WHERE config_key = 'deepseek_api_key' AND deleted = 0;
UPDATE system_config SET config_value = 'https://api.deepseek.com/v1/chat/completions' WHERE config_key = 'deepseek_api_url' AND deleted = 0;
UPDATE system_config SET config_value = 'deepseek-chat' WHERE config_key = 'ai_model' AND deleted = 0;