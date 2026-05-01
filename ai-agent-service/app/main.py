# FastAPI main entry point
import sys
from fastapi import FastAPI
from app.api.routes import router
from app.config import settings
import logging

# Configure logging with more detail
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout)
    ]
)

# Set httpx logging to INFO to see HTTP requests
logging.getLogger("httpx").setLevel(logging.INFO)

logger = logging.getLogger(__name__)

app = FastAPI(
    title="AI Review Agent Service",
    description="独立AI审核微服务，使用LangChain Agent进行智能作品评审",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# Include API routes
app.include_router(router, prefix="/api")


@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "service": "AI Review Agent",
        "version": "1.0.0",
        "status": "running"
    }


@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {
        "status": "healthy",
        "agent": "ready",
        "deepseek_api": "configured",
        "docker_tools": "available"
    }


@app.on_event("startup")
async def startup_event():
    """Startup event - initialize agent and check connections"""
    logger.info("AI Review Agent Service starting...")
    logger.info(f"DeepSeek API URL: {settings.deepseek_api_url}")
    logger.info(f"Spring Boot API URL: {settings.spring_boot_api_url}")
    logger.info("AI Review Agent Service ready!")


@app.on_event("shutdown")
async def shutdown_event():
    """Shutdown event"""
    logger.info("AI Review Agent Service shutting down...")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8001,
        reload=True,
        log_level="info"
    )