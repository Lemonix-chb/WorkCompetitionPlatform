"""
SkillRegistry - 动态技能扩展系统

功能：
1. 从skills.yaml配置文件动态加载技能
2. 技能组合：一个Skill可包含多个Tools
3. 技能注册和管理机制
4. Agent动态获取技能能力

架构：
- skills.yaml：技能配置文件
- SkillRegistry：技能注册中心
- SkillLoader：动态加载器
- AgentFactory：Agent工厂（根据Skill创建Agent）

作者：AI Agent架构重构项目
创建时间：2026-05-03
"""

import os
import logging
import yaml
from typing import Dict, Any, List, Optional
from pydantic import BaseModel, Field

logger = logging.getLogger(__name__)


class SkillConfig(BaseModel):
    """技能配置"""

    name: str = Field(description="技能名称")
    description: str = Field(description="技能描述")
    tools: List[str] = Field(description="工具列表")
    agent: str = Field(description="关联Agent")
    enabled: bool = Field(default=True, description="是否启用")

    # 可选配置
    config: Optional[Dict[str, Any]] = Field(default=None, description="技能配置参数")


class Skill(BaseModel):
    """已加载的技能"""

    name: str
    description: str
    tools: List[Any]  # 实际加载的工具对象
    agent: str
    enabled: bool
    config: Optional[Dict[str, Any]] = None


class SkillRegistry:
    """
    技能注册中心

    功能：
    1. 从skills.yaml加载技能配置
    2. 动态加载工具和Agent
    3. 技能注册和管理
    4. 提供技能查询接口
    """

    def __init__(
        self,
        config_file: str = "config/skills.yaml",
        tools_package: str = "app.tools",
        agents_package: str = "app.agents"
    ):
        """
        初始化SkillRegistry

        Args:
            config_file: skills.yaml配置文件路径
            tools_package: 工具包路径
            agents_package: Agent包路径
        """
        self.config_file = config_file
        self.tools_package = tools_package
        self.agents_package = agents_package

        # 技能注册表
        self.skills: Dict[str, Skill] = {}

        # 工具注册表
        self.tools_registry: Dict[str, Any] = {}

        # Agent注册表
        self.agents_registry: Dict[str, Any] = {}

        # 加载配置文件
        self._load_config()

        logger.info(f"SkillRegistry初始化完成，已注册{len(self.skills)}个技能")

    def _load_config(self):
        """加载skills.yaml配置文件"""
        try:
            if not os.path.exists(self.config_file):
                logger.warning(f"配置文件不存在：{self.config_file}")
                self._create_default_config()
                return

            with open(self.config_file, 'r', encoding='utf-8') as f:
                config_data = yaml.safe_load(f)

            if not config_data or 'skills' not in config_data:
                logger.warning("配置文件格式错误或无技能定义")
                return

            # 加载每个技能
            for skill_data in config_data['skills']:
                skill_config = SkillConfig(**skill_data)

                if skill_config.enabled:
                    self._register_skill(skill_config)

            logger.info(f"配置文件加载完成，启用了{len(self.skills)}个技能")

        except Exception as e:
            logger.error(f"配置文件加载失败：{e}")

    def _create_default_config(self):
        """创建默认配置文件"""
        default_config = {
            'skills': [
                {
                    'name': 'video_analysis',
                    'description': '完整的视频分析能力',
                    'tools': ['ffmpeg_tool', 'keyframe_tool', 'ocr_tool', 'whisper_tool'],
                    'agent': 'video_analyzer_agent_complete',
                    'enabled': True
                },
                {
                    'name': 'code_quality',
                    'description': '代码质量检查能力',
                    'tools': [],
                    'agent': 'code_reviewer_agent',
                    'enabled': True
                },
                {
                    'name': 'ppt_design',
                    'description': 'PPT设计评估能力',
                    'tools': [],
                    'agent': 'ppt_reviewer_agent',
                    'enabled': True
                }
            ]
        }

        # 创建配置目录
        config_dir = os.path.dirname(self.config_file)
        if config_dir:
            os.makedirs(config_dir, exist_ok=True)

        # 写入默认配置
        with open(self.config_file, 'w', encoding='utf-8') as f:
            yaml.dump(default_config, f, allow_unicode=True, default_flow_style=False)

        logger.info(f"默认配置文件创建完成：{self.config_file}")

        # 加载默认配置
        for skill_data in default_config['skills']:
            skill_config = SkillConfig(**skill_data)
            if skill_config.enabled:
                self._register_skill(skill_config)

    def _load_tool(self, tool_name: str) -> Optional[Any]:
        """
        动态加载工具

        Args:
            tool_name: 工具名称

        Returns:
            工具对象或None
        """
        # 检查工具注册表
        if tool_name in self.tools_registry:
            return self.tools_registry[tool_name]

        try:
            # 动态导入工具模块
            import importlib

            # 转换工具名称为模块名（例如 ffmpeg_tool → FFmpegTool）
            module_name = f"{self.tools_package}.{tool_name}"
            module = importlib.import_module(module_name)

            # 获取工具类（使用特殊映射）
            class_name = self._to_camel_case(tool_name)
            tool_class = getattr(module, class_name)

            # 注册工具类
            self.tools_registry[tool_name] = tool_class

            logger.info(f"工具加载成功：{tool_name} → {class_name}")

            return tool_class

        except Exception as e:
            logger.error(f"工具加载失败：{tool_name}, {e}")
            return None

    def _load_agent(self, agent_name: str) -> Optional[Any]:
        """
        动态加载Agent

        Args:
            agent_name: Agent名称

        Returns:
            Agent类或None
        """
        # 检查Agent注册表
        if agent_name in self.agents_registry:
            return self.agents_registry[agent_name]

        try:
            # 动态导入Agent模块
            import importlib

            module_name = f"{self.agents_package}.{agent_name}"
            module = importlib.import_module(module_name)

            # 获取Agent类（使用特殊映射）
            class_name = self._to_camel_case(agent_name)
            agent_class = getattr(module, class_name)

            # 注册Agent类
            self.agents_registry[agent_name] = agent_class

            logger.info(f"Agent加载成功：{agent_name} → {class_name}")

            return agent_class

        except Exception as e:
            logger.error(f"Agent加载失败：{agent_name}, {e}")
            return None

    def _register_skill(self, skill_config: SkillConfig):
        """
        注册技能

        Args:
            skill_config: 技能配置
        """
        try:
            # 加载工具
            loaded_tools = []
            for tool_name in skill_config.tools:
                tool_class = self._load_tool(tool_name)
                if tool_class:
                    loaded_tools.append(tool_class)

            # 加载Agent
            agent_class = self._load_agent(skill_config.agent)

            if agent_class is None:
                logger.warning(f"技能{skill_config.name}的Agent未加载")

            # 创建Skill对象
            skill = Skill(
                name=skill_config.name,
                description=skill_config.description,
                tools=loaded_tools,
                agent=skill_config.agent,
                enabled=skill_config.enabled,
                config=skill_config.config
            )

            # 注册到技能表
            self.skills[skill_config.name] = skill

            logger.info(f"技能注册成功：{skill_config.name}（{len(loaded_tools)}个工具）")

        except Exception as e:
            logger.error(f"技能注册失败：{skill_config.name}, {e}")

    def _to_camel_case(self, snake_str: str) -> str:
        """
        转换蛇形命名到驼峰命名（处理特殊命名规则）

        Args:
            snake_str: 蛇形命名字符串

        Returns:
            驼峰命名字符串
        """
        # 特殊映射规则（已知类名）
        special_mappings = {
            'ffmpeg_tool': 'FFmpegTool',
            'keyframe_tool': 'KeyFrameExtractorTool',
            'ocr_tool': 'OCRTool',
            'whisper_tool': 'WhisperTool',
            'video_analyzer_agent_complete': 'VideoAnalyzerAgent',
            'video_analyzer_agent': 'VideoAnalyzerAgent',
            'code_reviewer_agent': 'CodeReviewerAgent',
            'ppt_reviewer_agent': 'PPTReviewerAgent',
            'orchestrator_agent': 'OrchestratorAgent'
        }

        # 检查特殊映射
        if snake_str in special_mappings:
            return special_mappings[snake_str]

        # 默认转换规则
        components = snake_str.split('_')
        return ''.join(x.title() for x in components)

    def get_skill(self, skill_name: str) -> Optional[Skill]:
        """
        获取技能

        Args:
            skill_name: 技能名称

        Returns:
            Skill对象或None
        """
        return self.skills.get(skill_name)

    def get_all_skills(self) -> List[Skill]:
        """
        获取所有已注册技能

        Returns:
            List[Skill]: 技能列表
        """
        return list(self.skills.values())

    def get_enabled_skills(self) -> List[Skill]:
        """
        获取所有启用的技能

        Returns:
            List[Skill]: 启用的技能列表
        """
        return [skill for skill in self.skills.values() if skill.enabled]

    def enable_skill(self, skill_name: str) -> bool:
        """
        启用技能

        Args:
            skill_name: 技能名称

        Returns:
            bool: 是否成功
        """
        skill = self.skills.get(skill_name)
        if skill:
            skill.enabled = True
            logger.info(f"技能已启用：{skill_name}")
            return True
        return False

    def disable_skill(self, skill_name: str) -> bool:
        """
        禁用技能

        Args:
            skill_name: 技能名称

        Returns:
            bool: 是否成功
        """
        skill = self.skills.get(skill_name)
        if skill:
            skill.enabled = False
            logger.info(f"技能已禁用：{skill_name}")
            return True
        return False

    def reload_config(self):
        """
        重新加载配置文件

        用途：配置文件更新后，动态刷新技能注册
        """
        logger.info("重新加载配置文件...")

        # 清空现有注册
        self.skills.clear()

        # 重新加载
        self._load_config()

        logger.info(f"配置重新加载完成，已注册{len(self.skills)}个技能")


# 使用示例
if __name__ == "__main__":
    import os

    os.environ["DEEPSEEK_API_KEY"] = "your-deepseek-api-key"

    print("="*80)
    print("SkillRegistry测试")
    print("="*80)

    # 初始化SkillRegistry
    registry = SkillRegistry()

    # 获取所有技能
    skills = registry.get_all_skills()
    print(f"\n已注册技能数量：{len(skills)}")

    for skill in skills:
        print(f"\n技能名称：{skill.name}")
        print(f"  - 描述：{skill.description}")
        print(f"  - 工具数量：{len(skill.tools)}")
        print(f"  - Agent：{skill.agent}")
        print(f"  - 启用状态：{skill.enabled}")

    # 获取特定技能
    video_skill = registry.get_skill('video_analysis')
    if video_skill:
        print(f"\n[OK] video_analysis技能获取成功")
        print(f"  - 工具列表：{len(video_skill.tools)}个工具")

    # 启用/禁用技能
    registry.disable_skill('video_analysis')
    print(f"\n[OK] video_analysis技能已禁用")

    registry.enable_skill('video_analysis')
    print(f"[OK] video_analysis技能已启用")

    # 重新加载配置
    registry.reload_config()
    print(f"\n[OK] 配置重新加载完成")

    print("="*80)