# 架构调研

**领域：** RAG 知识库问答应用
**调研时间：** 2026-09-29
**置信度：** 中

## 标准架构

### 系统概览

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           PRESENTATION TIER                                  │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐             │
│  │   Web Client    │  │  Mobile Client  │  │   Admin Portal   │            │
│  │  (Vue 3 + SPA)  │  │  (uni-app/App)  │  │   (Vue 3)        │            │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘             │
│           │                      │                     │                     │
│           └──────────────────────┼─────────────────────┘                     │
│                                  │                                           │
│                           REST API (HTTPS)                                    │
│                                  │                                           │
├──────────────────────────────────┼───────────────────────────────────────────┤
│                           BUSINESS TIER                                       │
│  ┌───────────────────────────────▼───────────────────────────────────┐      │
│  │                     Java Spring Boot Backend                         │      │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐               │      │
│  │  │ Auth Module │  │ Doc Module  │  │ Chat Module  │               │      │
│  │  │ (JWT)       │  │ (CRUD)      │  │ (History)   │               │      │
│  │  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘               │      │
│  │         │                │                │                        │      │
│  │  ┌──────▼────────────────▼────────────────▼────────────┐           │      │
│  │  │              Service Layer (Business Logic)          │           │      │
│  │  └──────┬────────────────┬────────────────┬────────────┘           │      │
│  │         │                │                │                        │      │
│  │  ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐                │      │
│  │  │  PostgreSQL │  │    MinIO    │  │ HTTP Client │                │      │
│  │  │  (Metadata)  │  │  (Files)    │  │ to Python   │                │      │
│  │  └─────────────┘  └─────────────┘  └─────────────┘                │      │
│  └──────────────────────────────────────────────────────────────────────┘      │
│                                    │                                           │
│                            HTTP/REST (Internal)                               │
│                                    │                                           │
├────────────────────────────────────┼─────────────────────────────────────────┤
│                           AI SERVICE TIER                                     │
│  ┌─────────────────────────────────▼─────────────────────────────────┐      │
│  │                     Python FastAPI Service                           │      │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐      │      │
│  │  │  Document       │  │  Embedding      │  │  Chat/LLM       │      │      │
│  │  │  Ingestion      │  │  Service        │  │  Service        │      │      │
│  │  │  (Chroma)       │  │  (Ollama)       │  │  (Ollama)       │      │      │
│  │  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘      │      │
│  │           │                     │                     │               │      │
│  │  ┌────────▼─────────────────────▼─────────────────────▼────────┐      │      │
│  │  │                    Chroma Vector Database                      │      │      │
│  │  │              (Persistent, Collections: doc_chunks)            │      │      │
│  │  └───────────────────────────────────────────────────────────────┘      │      │
│  └─────────────────────────────────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### 组件职责

| 组件 | 职责 | 典型实现 |
|-----------|----------------|------------------------|
| Web/移动客户端 | UI 渲染、用户输入、状态管理 | Vue 3 + Vite / uni-app + uView Plus |
| Java 后端 | 认证、业务逻辑、数据持久化、编排 | Spring Boot 3 + JWT + Spring Data JPA |
| Python AI 服务 | Embedding 生成、向量操作、LLM 推理 | FastAPI + Ollama（自托管 LLM）|
| PostgreSQL | 结构化数据：用户、文档元数据、聊天历史 | PostgreSQL 15+ |
| MinIO / S3 | 二进制文件存储：PDF、DOC、原始上传 | MinIO（自托管）或 AWS S3 |
| Chroma | 向量 embedding 存储和相似性搜索 | Chroma DB（持久化模式）|
| Ollama | 本地 LLM 推理（无 API 密钥，隐私）| Ollama + mistral/llama2 模型 |

## 推荐项目结构

### Java 后端（Spring Boot）

```
backend/
├── src/main/java/com/ragkb/
│   ├── RAGkbApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java       # JWT 过滤器链
│   │   ├── CorsConfig.java           # 前端跨域
│   │   ├── RestTemplateConfig.java   # 到 Python 的 HTTP 客户端
│   │   └── OllamaProperties.java     # Python 服务端点配置
│   ├── controller/
│   │   ├── AuthController.java       # /api/auth/*
│   │   ├── DocumentController.java   # /api/documents/*
│   │   └── ChatController.java       # /api/chat/*
│   ├── service/
│   │   ├── AuthService.java          # JWT 生成/校验
│   │   ├── DocumentService.java      # 上传 → MinIO，元数据 → PG
│   │   ├── ChatService.java          # 编排 Python 服务调用
│   │   └── RAGService.java           # Java 端 RAG 编排
│   ├── repository/
│   │   ├── UserRepository.java       # users 表 JPA
│   │   ├── DocumentRepository.java   # documents 表 JPA
│   │   └── ChatHistoryRepository.java # chat_history 表 JPA
│   ├── model/
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── Document.java
│   │   │   └── ChatHistory.java
│   │   └── dto/
│   │       ├── LoginRequest.java
│   │       ├── ChatRequest.java
│   │       └── ChatResponse.java
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       └── RAGServiceException.java
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/                  # Flyway 迁移
├── pom.xml
└── docker-compose.yml                  # PostgreSQL + MinIO
```

### Python AI 服务

```
ai-service/
├── app/
│   ├── main.py                        # FastAPI app，启动事件
│   ├── api/
│   │   ├── routes/
│   │   │   ├── ingest.py             # POST /ingest, DELETE /ingest/{id}
│   │   │   ├── embedding.py          # POST /embed, POST /search
│   │   │   └── chat.py               # POST /chat, streaming 支持
│   │   └── deps.py                   # 依赖注入
│   ├── core/
│   │   ├── config.py                 # 设置：Chroma 路径、Ollama URL
│   │   ├── chunker.py                # 文档分块逻辑
│   │   └── embedding.py              # 通过 Ollama 生成 embedding
│   ├── services/
│   │   ├── chroma_service.py         # Chroma collection 操作
│   │   └── llm_service.py            # Ollama chat/completion
│   └── models/
│       ├── schemas.py                # Pydantic 模型
│       └── document.py                # 文档块模型
├── data/
│   └── chroma/                        # Chroma 持久化存储
├── Dockerfile
├── requirements.txt
└── docker-compose.yml
```

### 前端（Vue 3）

```
frontend/
├── src/
│   ├── api/
│   │   ├── axios.ts                   # Axios 实例，含拦截器
│   │   ├── auth.ts                    # /api/auth/* 调用
│   │   ├── documents.ts               # /api/documents/* 调用
│   │   └── chat.ts                    # /api/chat/* 调用
│   ├── components/
│   │   ├── layout/
│   │   │   ├── AppHeader.vue
│   │   │   └── AppSidebar.vue
│   │   ├── document/
│   │   │   ├── DocumentUploader.vue
│   │   │   └── DocumentList.vue
│   │   └── chat/
│   │       ├── ChatWindow.vue
│   │       └── ChatMessage.vue
│   ├── pages/
│   │   ├── Login.vue
│   │   ├── DocumentManage.vue
│   │   └── ChatQA.vue
│   ├── stores/
│   │   ├── auth.ts                    # Pinia：用户 + JWT token
│   │   └── chat.ts                    # Pinia：对话历史
│   ├── router/
│   │   └── index.ts                   # Vue Router，含守卫
│   └── App.vue
├── .env
└── vite.config.ts
```

## 架构模式

### 模式 1：API Gateway（Python 服务的 BFF）

**是什么：** Java 后端作为 Backend-for-Frontend，代理所有 Python AI 服务调用。
**何时使用：** 此技术栈中始终使用。保持 AI 服务私有，增加认证，支持审计。
**权衡：** 增加延迟（多一跳）；保持 AI 服务简单和无状态。

**示例：**
```java
@Service
public class RAGService {
    private final RestTemplate ollamaClient;

    public String chat(String query, String userId) {
        // Java 校验 JWT，丰富请求，然后代理
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = Map.of(
            "query", query,
            "user_id", userId,
            "collection", "doc_chunks"
        );
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);
        // Python 服务处理 embedding + 检索 + LLM
        ResponseEntity<Map> resp = ollamaClient.postForEntity(
            pythonServiceUrl + "/chat", req, Map.class
        );
        return resp.getBody().get("answer");
    }
}
```

### 模式 2：异步摄入管道

**是什么：** 文档上传立即返回；实际 embedding 通过队列或后台线程异步完成。
**何时使用：** 大文件或慢 embedding 生成；需要立即解除用户阻塞。
**权衡：** 状态追踪复杂；最终一致性。

**小型规模的更简单替代方案：** 带进度指示器的同步摄入。

**示例（带 Spring @Async 的异步）：**
```java
@Async
public void ingestDocumentAsync(Long docId, String filePath) {
    // 1. 从文件提取文本（PDF/DOCX 解析器）
    // 2. 分块文本（overlap=100 chars，chunk_size=500）
    // 3. 调用 Python /ingest 端点发送 chunks
    // 4. 在 PostgreSQL 中更新文档状态为 COMPLETED
}
```

### 模式 3：混合搜索（向量 + 关键词）

**是什么：** 将 Chroma 向量相似性与 BM25 关键词匹配结合，以获得更好的检索效果。
**何时使用：** 当文档包含 embedding 可能遗漏的领域特定术语时。
**权衡：** 检索时间加倍；排名逻辑更复杂。

**示例（简化）：**
```python
# Python 服务检索逻辑
async def hybrid_search(query: str, collection: str, top_k: int = 5):
    # 1. 向量搜索
    vector_results = chroma_service.query_vector(query, collection, top_k * 2)
    # 2. BM25 关键词搜索
    keyword_results = keyword_search(query, collection, top_k * 2)
    # 3. RRF 融合（倒数排名融合）
    fused = rrf_fusion([vector_results, keyword_results], k=60)
    return fused[:top_k]
```

## 数据流

### 文档上传流

```
[User] ──upload PDF/DOCX──▶ [Vue Frontend]
                                    │
                              POST /api/documents/upload
                              (multipart/form-data, JWT header)
                                    │
                                    ▼
                           [Java Spring Boot]
                                    │
                      ┌─────────────┴─────────────┐
                      │                           │
               [MinIO/S3]               [PostgreSQL]
               (store file)            (doc metadata:
                                        id, name, status=PENDING,
                                        user_id, created_at)
                      │                           │
                      └─────────────┬─────────────┘
                                    │
                         @Async trigger
                                    │
                                    ▼
                           [Python /ingest]
                           (file path + metadata)
                                    │
                         ┌──────────┴──────────┐
                         │  1. Parse file       │
                         │     (PyMuPDF/pooch)  │
                         │  2. Chunk text       │
                         │     (RecursiveChar)  │
                         │  3. Embed chunks     │
                         │     (Ollama embed)   │
                         │  4. Store in Chroma  │
                         └──────────────────────┘
                                    │
                         Update doc status=COMPLETED
                                    │
                                    ▼
                           [User sees "Ready" in UI]
```

### 聊天问答流

```
[User] ──"What is X?"──▶ [Vue Frontend]
                                  │
                            POST /api/chat
                            { message: "What is X?" }
                                  │
                                  ▼
                         [Java Spring Boot]
                         (validates JWT)
                                  │
                         ┌────────┴────────┐
                         │ 1. Save user     │
                         │    message to    │
                         │    chat_history  │
                         └────────┬────────┘
                                  │
                            POST /api/chat
                            to Python service
                            { query, user_id,
                              collection }
                                  │
                                  ▼
                         [Python FastAPI]
                                  │
                         ┌────────┴────────┐
                         │ 1. Embed query   │
                         │    (Ollama embed)│
                         │ 2. Vector search │
                         │    in Chroma     │
                         │ 3. Build context │
                         │    from chunks   │
                         │ 4. Call Ollama   │
                         │    /chat with    │
                         │    RAG prompt    │
                         └────────┬────────┘
                                  │
                            Return { answer, sources }
                                  │
                                  ▼
                         [Java] ──Save assistant──▶ [PostgreSQL]
                          response to chat_history
                                  │
                                  ▼
                         [Vue] ──render answer + sources──▶ [User]
```

### 认证流（JWT）

```
[Login]
User ──POST /api/auth/login──▶ [Java AuthController]
                                      │
                              Validate credentials
                                      │
                              ┌───────┴───────┐
                              │ Generate JWT: │
                              │ { sub: user_id│
                              │  exp: +24h }  │
                              │ sign: HS256   │
                              │ secret from   │
                              │ env var       │
                              └───────┬───────┘
                                      │
                                      ▼
                              { token: "eyJ..." }
                                      │
                              ◀─── Response
                                      │
[Subsequent Requests]
Client ──GET /api/documents──▶ [Java Security Filter]
                                    │
                          ┌─────────┴─────────┐
                          │ Extract Bearer     │
                          │ token from header  │
                          │ Validate signature │
                          │ Check exp          │
                          │ Load user_id       │
                          └─────────┬─────────┘
                                    │
                          ┌─────────┴─────────┐
                          │ Authorized:        │
                          │ proceed to         │
                          │ controller         │
                          └─────────┬─────────┘
                                    │
                          ┌─────────┴─────────┐
                          │ 401 Unauthorized   │
                          │ if token missing/  │
                          │ invalid/expired    │
                          └────────────────────┘
```

## Java 和 Python 服务间的 API 设计

### 内部 REST 契约

所有端点返回 JSON。Java 调用 Python；Python 不调用 Java。

| Python 端点 | 方法 | 请求体 | 响应 | 目的 |
|-----------------|--------|--------------|----------|---------|
| `/ingest` | POST | `{ file_path, collection, metadata }` | `{ chunk_count, status }` | 摄入文档块 |
| `/ingest/{doc_id}` | DELETE | — | `{ deleted }` | 从向量 DB 删除文档 |
| `/embed` | POST | `{ text: string }` | `{ embedding: float[] }` | 生成单个 embedding |
| `/search` | POST | `{ query, collection, top_k }` | `{ results: [{chunk, score}] }` | 向量相似性搜索 |
| `/chat` | POST | `{ query, collection, history? }` | `{ answer, sources }` | 完整 RAG 聊天 |

### 请求/响应示例

**摄入请求：**
```json
POST /ingest
{
  "file_path": "/storage/docs/123.pdf",
  "collection": "doc_chunks",
  "metadata": {
    "doc_id": 123,
    "user_id": "user_456",
    "filename": "annual-report.pdf"
  }
}
```

**摄入响应：**
```json
{
  "chunk_count": 47,
  "status": "completed"
}
```

**搜索请求：**
```json
POST /search
{
  "query": "revenue growth trends",
  "collection": "doc_chunks",
  "top_k": 5
}
```

**搜索响应：**
```json
{
  "results": [
    {
      "chunk_id": "abc123",
      "text": "The revenue grew by 15% year over year...",
      "score": 0.87,
      "metadata": { "doc_id": 123 }
    }
  ]
}
```

**聊天请求：**
```json
POST /chat
{
  "query": "What was the revenue growth?",
  "collection": "doc_chunks",
  "history": [
    { "role": "user", "content": "Show me financials" },
    { "role": "assistant", "content": "Here are the financials..." }
  ]
}
```

**聊天响应：**
```json
{
  "answer": "Based on the documents, revenue grew by 15% YoY...",
  "sources": [
    { "doc_id": 123, "chunk_id": "abc123", "text": "The revenue grew by 15%..." }
  ]
}
```

## Chroma 向量数据库集成模式

### Collection 管理

```python
# app/services/chroma_service.py
import chromadb
from chromadb.config import Settings

class ChromaService:
    def __init__(self, persist_dir: str = "./data/chroma"):
        self.client = chromadb.PersistentClient(
            path=persist_dir,
            settings=Settings(allow_reset=True)
        )
    
    def get_or_create_collection(self, name: str):
        return self.client.get_or_create_collection(
            name=name,
            metadata={"description": f"RAG chunks for {name}"}
        )
    
    def add_chunks(self, collection_name: str, chunks: list[dict]):
        collection = self.get_or_create_collection(collection_name)
        collection.add(
            ids=[c["id"] for c in chunks],
            documents=[c["text"] for c in chunks],
            metadatas=[c["metadata"] for c in chunks]
        )
    
    def query(self, collection_name: str, query_text: str, top_k: int = 5):
        collection = self.get_or_create_collection(collection_name)
        return collection.query(
            query_texts=[query_text],
            n_results=top_k
        )
    
    def delete_by_doc_id(self, collection_name: str, doc_id: str):
        collection = self.get_or_create_collection(collection_name)
        # Chroma 不直接支持按元数据删除；
        # 查询所有，过滤，然后按 ID 删除
        result = collection.get(where={"doc_id": doc_id})
        if result["ids"]:
            collection.delete(ids=result["ids"])
```

### 用 Ollama 做 Embedding

```python
# app/core/embedding.py
import ollama

class EmbeddingService:
    def __init__(self, model: str = "nomic-embed-text"):
        self.model = model
    
    def embed(self, text: str) -> list[float]:
        response = ollama.embeddings(model=self.model, prompt=text)
        return response["embedding"]
    
    def embed_batch(self, texts: list[str]) -> list[list[float]]:
        return [self.embed(t) for t in texts]
```

### 分块策略

```python
# app/core/chunker.py
from langchain.text_splitter import RecursiveCharacterTextSplitter

class DocumentChunker:
    def __init__(
        self,
        chunk_size: int = 500,
        chunk_overlap: int = 100,
        separators: list[str] = ["\n\n", "\n", "。", " ", ""]
    ):
        self.splitter = RecursiveCharacterTextSplitter(
            chunk_size=chunk_size,
            chunk_overlap=chunk_overlap,
            separators=separators
        )
    
    def chunk(self, text: str, doc_id: str, metadata: dict) -> list[dict]:
        docs = self.splitter.create_documents([text], metadatas=[metadata])
        return [
            {
                "id": f"{doc_id}_{i}",
                "text": doc.page_content,
                "metadata": {**doc.metadata, "chunk_index": i}
            }
            for i, doc in enumerate(docs)
        ]
```

## 扩展考虑

| 规模 | 架构调整 |
|-------|-------------------------|
| 0-100 用户 | 单体足够。PostgreSQL 同主机。单个 Ollama 实例。Chroma 本地磁盘。 |
| 100-1k 用户 | PostgreSQL 和 MinIO 移至独立容器。加 Redis 做 JWT 缓存。Ollama 在 GPU 主机。 |
| 1k-10k 用户 | 加连接池（PgBouncer）。每个模型独立 Ollama 实例。Chroma 副本或切换到 Milvus。摄入用后台任务队列（Redis/RabbitMQ）。 |
| 10k+ 用户 | 拆分为微服务：认证服务、文档服务、聊天服务。加 API 网关。切换到托管向量 DB（Pinecone、Weaviate Cloud）。 |

### 扩展优先级

1. **第一个瓶颈：** Ollama LLM 推理延迟。解决方案：用更小/更快的模型（mistral 而非 llama3），加 GPU，或切换到量化模型。
2. **第二个瓶颈：** 大 collection 时 Chroma 查询速度。解决方案：切换到 Milvus/Pinecone，加索引，或按 collection 分片。
3. **第三个瓶颈：** chat_history 上 PostgreSQL 写吞吐量。解决方案：加读副本，按月分区 chat_history。

## 反模式

### 反模式 1：Embedding 一切

**人们做的：** 不分块就 embedding 整个文档，或使用巨大的固定块。
**为什么错误：** 降低检索精度，触及 token 限制，查询变慢。
**正确做法：** 分块到 300-800 字符，10-20% 重叠，遵守语义边界（段落）。

### 反模式 2：无元数据过滤

**人们做的：** 存储块时不带元数据，仅通过向量距离检索一切。
**为什么错误：** 无法按用户、文档、日期或部门过滤。安全风险（跨用户数据泄露）。
**正确做法：** 始终在元数据中存储 user_id 和 doc_id。查询时按 user_id 过滤：`where={"user_id": user_id}`。

### 反模式 3：在 PostgreSQL JSON 列中存储 Embedding

**人们做的：** "保持简单，把向量存在 JSONB 列里"。
**为什么错误：** PostgreSQL JSON 存储不支持向量操作（余弦相似度、点积）。查询变成慢的全表扫描。
**正确做法：** 小规模用 Chroma（Pinecone、Milvus、Qdrant 用于向量操作的专用向量 DB）。

### 反模式 4：Python 服务直接暴露在公网

**人们做的：** 将 FastAPI 直接作为公开 API 暴露，从前端调用 Ollama。
**为什么错误：** 无认证，无审计日志，Ollama 无访问控制。
**正确做法：** Java 后端是唯一公开的服务。Python 服务在内网/VPC。

### 反模式 5：同步文件上传阻塞 LLM 推理

**人们做的：** 上传 50 页 PDF，在同一 HTTP 请求中等待 embedding 完成。
**为什么错误：** HTTP 超时，UX 差，阻塞连接池。
**正确做法：** 立即返回 202 Accepted 带 job ID。异步处理。用 WebSocket 或轮询状态更新。

## 集成点

### 外部服务

| 服务 | 集成模式 | 备注 |
|---------|---------------------|-------|
| Ollama | HTTP POST 到 `/api/generate` 和 `/api/embeddings` | 自托管 LLM。通过 systemd 或 Docker 管理。 |
| Chroma | Python 客户端（本地进程）| 如果用 Docker，持久化存储目录挂载为共享卷。 |
| MinIO | S3 兼容 SDK | 与 AWS S3 API 相同；改端点即可切换到真实 S3。 |
| OpenAI（可选）| SDK + 代理 | 如果 Ollama 不够，加 OpenAI 作为备选，接口相同。 |

### 内部边界

| 边界 | 通信 | 备注 |
|----------|---------------|-------|
| 前端 ↔ Java | REST over HTTPS | JWT 在 Authorization 头。Axios 拦截器处理 401 重定向到登录。 |
| Java ↔ Python | REST over HTTP（内部）| 如果在同一 Docker 网络，服务间无认证；跨 VPC 加 API 密钥。 |
| Python ↔ Ollama | HTTP 到 localhost:11434 | Ollama 必须在同一主机或可达网络。 |
| Python ↔ Chroma | 进程内 Python 客户端 | Chroma 持久化目录作为 Docker 卷挂载。 |

## 来源

- Chroma 文档: https://docs.trychroma.com
- Ollama 文档: https://github.com/ollama/ollama
- Spring Boot Security with JWT: https://spring.io/guides/gs/securing-web
- FastAPI + Chroma RAG 教程: https://docs.llamaindex.ai/examples/vector_stores/chroma
- LangChain 文本分割器: https://python.langchain.com/docs/modules/data_connection/document_transformers

---
*架构调研：RAG 知识库问答应用*
*调研时间：2026-09-29*
