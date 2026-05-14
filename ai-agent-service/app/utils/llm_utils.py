"""LLM输出解析工具"""
import json
import logging
from typing import Dict, Any

logger = logging.getLogger(__name__)


def extract_json_from_llm_output(llm_output: str) -> Dict[str, Any]:
    """从LLM响应中提取JSON字典（处理markdown代码块包裹）"""
    json_str = llm_output
    if "```json" in llm_output:
        json_str = llm_output.split("```json")[1].split("```")[0].strip()
    elif "```" in llm_output:
        json_str = llm_output.split("```")[1].split("```")[0].strip()

    try:
        return json.loads(json_str)
    except json.JSONDecodeError:
        logger.error(f"LLM输出JSON解析失败，原始内容前200字符: {llm_output[:200]}")
        raise
