"""
AgentFactory - Agent工厂

功能：
1. 根据Skill动态创建Agent实例
2. 配置Agent的工具链
3. 统一Agent初始化流程
4. 支持Agent依赖注入

作者：AI Agent架构重构项目
创建时间：2026-05-03
"""

import os
import logging
from typing import Dict, Any, Optional

logger = logging.getLogger(__name__)


class AgentFactory:
    """
    Agent工厂

    功能：
    1. 根据Skill创建Agent实例
    2. 配置Agent的工具链
    3. 管理Agent依赖注入
    """

    def __init__(self, skill_registry):
        """
        初始化AgentFactory

        Args:
            skill_registry: SkillRegistry实例
        """
        self.skill_registry = skill_registry

        # Agent实例缓存
        self.agent_instances: Dict[str, Any] = {}

        logger.info("AgentFactory初始化完成")

    def create_agent(
        self,
        skill_name: str,
        **kwargs
    ) -> Optional[Any]:
        """
        根据Skill创建Agent实例

        Args:
            skill_name: 技能名称
            **kwargs: Agent初始化参数

        Returns:
            Agent实例或None
        """
        try:
            # 获取技能
            skill = self.skill_registry.get_skill(skill_name)

            if skill is None:
                logger.warning(f"技能不存在：{skill_name}")
                return None

            if not skill.enabled:
                logger.warning(f"技能未启用：{skill_name}")
                return None

            # 检查缓存
            cache_key = f"{skill_name}_{hash(str(kwargs))}"
            if cache_key in self.agent_instances:
                logger.info(f"使用缓存的Agent实例：{skill_name}")
                return self.agent_instances[cache_key]

            # 获取Agent类
            agent_class = self.skill_registry.agents_registry.get(skill.agent)

            if agent_class is None:
                logger.warning(f"Agent类未加载：{skill.agent}")
                return None

            # 合并配置参数
            agent_config = {}

            # 只保留Agent支持的参数
            supported_params = ['api_key', 'model_name', 'base_url', 'ffmpeg_path']

            if skill.config:
                for key in supported_params:
                    if key in skill.config:
                        agent_config[key] = skill.config[key]

            # 添加kwargs参数（优先级更高）
            agent_config.update(kwargs)

            # 添加DeepSeek API配置（如果未提供）
            if 'api_key' not in agent_config:
                agent_config['api_key'] = os.getenv("DEEPSEEK_API_KEY")

            # 创建Agent实例
            agent_instance = agent_class(**agent_config)

            # 缓存实例
            self.agent_instances[cache_key] = agent_instance

            logger.info(f"Agent创建成功：{skill.agent}（技能：{skill_name}）")

            return agent_instance

        except Exception as e:
            logger.error(f"Agent创建失败：{skill_name}, {e}")
            return None

    def get_or_create_agent(
        self,
        work_type: str,
        **kwargs
    ) -> Optional[Any]:
        """
        根据作品类型获取或创建Agent

        Args:
            work_type: 作品类型（CODE/PPT/VIDEO）
            **kwargs: Agent初始化参数

        Returns:
            Agent实例或None
        """
        # 作品类型到技能名称映射
        work_type_skill_map = {
            "VIDEO": "video_analysis",
            "CODE": "code_quality",
            "PPT": "ppt_design"
        }

        skill_name = work_type_skill_map.get(work_type)

        if skill_name is None:
            logger.warning(f"不支持的作品类型：{work_type}")
            return None

        return self.create_agent(skill_name, **kwargs)

    def clear_cache(self):
        """
        清空Agent实例缓存

        用途：重新创建Agent实例
        """
        self.agent_instances.clear()
        logger.info("Agent实例缓存已清空")


# 使用示例
if __name__ == "__main__":
    import os

    os.environ["DEEPSEEK_API_KEY"] = "sk-325ae1ccf357480ab353a41e8b26ee32"

    from .skill_registry import SkillRegistry

    print("="*80)
    print("AgentFactory测试")
    print("="*80)

    # 初始化SkillRegistry
    registry = SkillRegistry()

    # 初始化AgentFactory
    factory = AgentFactory(registry)

    # 创建VideoAnalyzerAgent
    video_agent = factory.create_agent('video_analysis')
    if video_agent:
        print(f"\n[OK] VideoAnalyzerAgent创建成功")
        print(f"  - 类型：{type(video_agent).__name__}")

    # 创建CodeReviewerAgent
    code_agent = factory.create_agent('code_quality')
    if code_agent:
        print(f"\n[OK] CodeReviewerAgent创建成功")
        print(f"  - 类型：{type(code_agent).__name__}")

    # 根据作品类型创建Agent
    ppt_agent = factory.get_or_create_agent('PPT')
    if ppt_agent:
        print(f"\n[OK] PPTReviewerAgent创建成功（作品类型路由）")
        print(f"  - 类型：{type(ppt_agent).__name__}")

    print("="*80)