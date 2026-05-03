"""
RAG知识库实现 - ChromaDB向量数据库集成

功能：
1. 历史评审记录向量化和存储
2. 相似案例检索机制
3. Agent集成RAG查询辅助决策

架构：
- ChromaDB（本地持久化向量数据库）
- OpenAI text-embedding-3-small（嵌入模型）
- LangChain VectorStoreRetriever（检索机制）

作者：AI Agent架构重构项目
创建时间：2026-05-03
"""

import os
import logging
from typing import Dict, Any, List, Optional
from pydantic import BaseModel

logger = logging.getLogger(__name__)


class ReviewCase(BaseModel):
    """历史评审案例"""

    case_id: str
    work_type: str  # CODE/PPT/VIDEO
    title: str
    description: str

    # 评分信息
    overall_score: float
    dimension_scores: Dict[str, float]  # 各维度评分

    # 评审意见
    review_summary: str
    strengths: List[str]
    weaknesses: List[str]
    improvement_suggestions: List[str]

    # 元数据
    metadata: Dict[str, Any]


class RAGKnowledgeBase:
    """
    RAG知识库（ChromaDB集成）

    功能：
    1. 历史评审记录存储
    2. 相似案例检索
    3. 评分标准参考
    """

    def __init__(
        self,
        persist_directory: str = "./knowledge_data/chroma_db",
        embedding_model: str = "text-embedding-3-small"
    ):
        """
        初始化RAG知识库

        Args:
            persist_directory: ChromaDB持久化目录
            embedding_model: 嵌入模型名称
        """
        self.persist_directory = persist_directory
        self.embedding_model = embedding_model

        # 创建持久化目录
        os.makedirs(persist_directory, exist_ok=True)

        # 初始化ChromaDB（延迟加载）
        self._vector_store = None
        self._embeddings = None

        logger.info(f"RAGKnowledgeBase初始化完成，持久化目录：{persist_directory}")

    def _get_embeddings(self):
        """获取嵌入模型（延迟加载）"""
        if self._embeddings is None:
            try:
                from langchain_openai import OpenAIEmbeddings

                # 使用DeepSeek API（需要配置）
                # 注意：DeepSeek可能不支持嵌入API，考虑使用OpenAI
                api_key = os.getenv("OPENAI_API_KEY")

                if not api_key:
                    logger.warning("OPENAI_API_KEY未设置，RAG功能受限")
                    return None

                self._embeddings = OpenAIEmbeddings(
                    model=self.embedding_model,
                    api_key=api_key
                )

                logger.info(f"嵌入模型加载完成：{self.embedding_model}")

            except Exception as e:
                logger.error(f"嵌入模型加载失败：{e}")
                return None

        return self._embeddings

    def _get_vector_store(self):
        """获取向量存储（延迟加载）"""
        if self._vector_store is None:
            try:
                import chromadb
                from langchain_chroma import Chroma

                embeddings = self._get_embeddings()

                if embeddings is None:
                    logger.warning("嵌入模型未加载，无法创建向量存储")
                    return None

                self._vector_store = Chroma(
                    persist_directory=self.persist_directory,
                    embedding_function=embeddings,
                    collection_name="review_cases"
                )

                logger.info("ChromaDB向量存储创建完成")

            except Exception as e:
                logger.error(f"向量存储创建失败：{e}")
                return None

        return self._vector_store

    def add_review_case(self, case: ReviewCase) -> bool:
        """
        添加评审案例到知识库

        Args:
            case: 评审案例

        Returns:
            bool: 是否添加成功
        """
        try:
            vector_store = self._get_vector_store()

            if vector_store is None:
                logger.warning("向量存储未初始化，无法添加案例")
                return False

            # 构建文档内容
            document_content = f"""
作品类型：{case.work_type}
作品标题：{case.title}
作品说明：{case.description}

总分：{case.overall_score}
评分维度：{case.dimension_scores}

评审总结：{case.review_summary}
作品亮点：{case.strengths}
不足之处：{case.weaknesses}
改进建议：{case.improvement_suggestions}
"""

            # 添加到向量存储
            vector_store.add_texts(
                texts=[document_content],
                metadatas=[{
                    "case_id": case.case_id,
                    "work_type": case.work_type,
                    "overall_score": case.overall_score
                }]
            )

            logger.info(f"评审案例添加成功：{case.case_id}")

            return True

        except Exception as e:
            logger.error(f"添加评审案例失败：{e}")
            return False

    def search_similar_cases(
        self,
        work_type: str,
        description: str,
        top_k: int = 3
    ) -> List[Dict[str, Any]]:
        """
        检索相似评审案例

        Args:
            work_type: 作品类型（CODE/PPT/VIDEO）
            description: 作品描述
            top_k: 返回案例数量

        Returns:
            List[Dict]: 相似案例列表
        """
        try:
            vector_store = self._get_vector_store()

            if vector_store is None:
                logger.warning("向量存储未初始化，无法检索案例")
                return []

            # 构建查询文本
            query_text = f"作品类型：{work_type}\n作品说明：{description}"

            # 检索相似案例
            results = vector_store.similarity_search(
                query=query_text,
                k=top_k
            )

            similar_cases = []

            for doc in results:
                similar_cases.append({
                    "content": doc.page_content,
                    "metadata": doc.metadata,
                    "score": doc.metadata.get("overall_score", 0)
                })

            logger.info(f"检索到{len(similar_cases)}个相似案例")

            return similar_cases

        except Exception as e:
            logger.error(f"检索相似案例失败：{e}")
            return []

    def get_scoring_standards(self, work_type: str) -> Dict[str, Any]:
        """
        获取评分标准参考

        Args:
            work_type: 作品类型

        Returns:
            Dict: 评分标准
        """
        # 评分标准（基于官方文件）
        scoring_standards = {
            "VIDEO": {
                "dimensions": {
                    "故事性": {
                        "max_score": 25,
                        "description": "叙事手法、情节逻辑、内容丰富度"
                    },
                    "视觉效果": {
                        "max_score": 25,
                        "description": "画面质量、剪辑节奏、转场合理性"
                    },
                    "导演技巧": {
                        "max_score": 25,
                        "description": "镜头运用、视听效果"
                    },
                    "原创性": {
                        "max_score": 25,
                        "description": "创意构思、原创内容"
                    }
                },
                "hard_requirements": [
                    "时长：60-180秒",
                    "比例：16:9",
                    "分辨率：1080p",
                    "格式：MP4",
                    "文件大小：≤300MB",
                    "字幕：必须存在",
                    "片头片尾：必须包含"
                ]
            },
            "CODE": {
                "dimensions": {
                    "创新性": {
                        "max_score": 25,
                        "description": "技术方案创新程度"
                    },
                    "实用性": {
                        "max_score": 25,
                        "description": "功能完整性、解决实际问题能力"
                    },
                    "用户体验": {
                        "max_score": 25,
                        "description": "界面设计、交互质量"
                    },
                    "代码质量": {
                        "max_score": 25,
                        "description": "规范、可读性、注释完整性"
                    }
                },
                "hard_requirements": [
                    "源代码可运行",
                    "原创性（代码查重）",
                    "说明文档完整性",
                    "代码规范"
                ]
            },
            "PPT": {
                "dimensions": {
                    "创意": {
                        "max_score": 25,
                        "description": "内容创意、视觉设计创新"
                    },
                    "视觉效果": {
                        "max_score": 25,
                        "description": "排版、色彩、图文比例"
                    },
                    "内容呈现": {
                        "max_score": 25,
                        "description": "逻辑结构、信息密度"
                    },
                    "原创性": {
                        "max_score": 25,
                        "description": "原创元素使用"
                    }
                },
                "hard_requirements": [
                    "页数：至少12页",
                    "比例：16:9",
                    "格式：PPTX，无密码保护",
                    "原创性",
                    "内容健康积极"
                ]
            }
        }

        return scoring_standards.get(work_type, {})


# 使用示例
if __name__ == "__main__":
    import os

    # 设置OpenAI API Key（用于嵌入）
    os.environ["OPENAI_API_KEY"] = "your-openai-api-key"

    rag_kb = RAGKnowledgeBase()

    # 添加示例案例
    sample_case = ReviewCase(
        case_id="CASE-001",
        work_type="VIDEO",
        title="AI教育短视频",
        description="讲解机器学习基础知识的教学视频",
        overall_score=85,
        dimension_scores={
            "故事性": 22,
            "视觉效果": 23,
            "导演技巧": 20,
            "原创性": 20
        },
        review_summary="视频制作质量优秀...",
        strengths=["内容丰富", "画面清晰", "转场流畅"],
        weaknesses=["字幕同步度稍差"],
        improvement_suggestions=["优化字幕同步"]
    )

    print("="*80)
    print("RAG知识库测试")
    print("="*80)

    # 添加案例
    success = rag_kb.add_review_case(sample_case)
    print(f"\n添加案例：{success}")

    # 检索相似案例
    similar = rag_kb.search_similar_cases(
        work_type="VIDEO",
        description="人工智能教学视频",
        top_k=2
    )

    print(f"\n检索到{len(similar)}个相似案例")

    # 获取评分标准
    standards = rag_kb.get_scoring_standards("VIDEO")
    print(f"\nVIDEO评分标准：{standards}")

    print("="*80)