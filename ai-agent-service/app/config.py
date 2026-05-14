# Configuration management with Pydantic
from pydantic import Field
from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    """Application settings loaded from environment variables or config.yaml"""

    # DeepSeek API Configuration
    deepseek_api_key: str = Field(default="", description="DeepSeek API密钥，从环境变量DEEPSEEK_API_KEY加载")
    deepseek_api_url: str = "https://api.deepseek.com/v1/chat/completions"
    deepseek_model: str = "deepseek-v4-pro"

    # Spring Boot Callback URL
    spring_boot_api_url: str = "http://localhost:8080/api"

    # FFmpeg Configuration (for video analysis)
    ffmpeg_path: Optional[str] = Field(
        default=None,
        description="FFmpeg bin目录路径，从环境变量FFMPEG_PATH加载。如果未设置，将尝试从系统PATH查找"
    )

    # Docker Tools Configuration
    jplag_docker_image: str = "jplag/jplag:latest"
    checkstyle_docker_image: str = "checkstyle/checkstyle:latest"

    # File Storage Paths
    uploads_dir: str = "./uploads"
    temp_dir: str = "./temp"

    # Logging
    log_level: str = "INFO"

    # Timeout settings (seconds)
    jplag_timeout: int = 30
    checkstyle_timeout: int = 30
    deepseek_timeout: int = 60

    # Agent settings
    agent_verbose: bool = True

    class Config:
        env_file = ".env"
        case_sensitive = False
        extra = "ignore"


# Global settings instance
settings = Settings()