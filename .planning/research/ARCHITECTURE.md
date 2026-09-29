# Architecture Research

**Domain:** RAG Knowledge Base Q&A Applications
**Researched:** 2026-09-29
**Confidence:** MEDIUM

## Standard Architecture

### System Overview

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

### Component Responsibilities

| Component | Responsibility | Typical Implementation |
|-----------|----------------|------------------------|
| Web/Mobile Client | UI rendering, user input, state management | Vue 3 + Vite / uni-app + uView Plus |
| Java Backend | Auth, business logic, data persistence, orchestration | Spring Boot 3 + JWT + Spring Data JPA |
| Python AI Service | Embedding generation, vector ops, LLM inference | FastAPI + Ollama (self-hosted LLM) |
| PostgreSQL | Structured data: users, documents metadata, chat history | PostgreSQL 15+ |
| MinIO / S3 | Binary file storage: PDFs, DOCs, raw uploads | MinIO (self-hosted) or AWS S3 |
| Chroma | Vector embeddings storage and similarity search | Chroma DB (persistent mode) |
| Ollama | Local LLM inference (no API keys, privacy) | Ollama with mistral/llama2 model |

## Recommended Project Structure

### Java Backend (Spring Boot)

```
backend/
├── src/main/java/com/ragkb/
│   ├── RAGkbApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java       # JWT filter chain
│   │   ├── CorsConfig.java           # Cross-origin for frontend
│   │   ├── RestTemplateConfig.java   # HTTP client to Python
│   │   └── OllamaProperties.java     # Python service endpoint config
│   ├── controller/
│   │   ├── AuthController.java       # /api/auth/*
│   │   ├── DocumentController.java   # /api/documents/*
│   │   └── ChatController.java       # /api/chat/*
│   ├── service/
│   │   ├── AuthService.java          # JWT generation/validation
│   │   ├── DocumentService.java       # Upload → MinIO, metadata → PG
│   │   ├── ChatService.java          # Orchestrate Python service calls
│   │   └── RAGService.java           # Java-side RAG orchestration
│   ├── repository/
│   │   ├── UserRepository.java       # JPA for users table
│   │   ├── DocumentRepository.java    # JPA for documents table
│   │   └── ChatHistoryRepository.java # JPA for chat_history table
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
│   └── db/migration/                  # Flyway migrations
├── pom.xml
└── docker-compose.yml                 # PostgreSQL + MinIO
```

### Python AI Service

```
ai-service/
├── app/
│   ├── main.py                        # FastAPI app, startup events
│   ├── api/
│   │   ├── routes/
│   │   │   ├── ingest.py              # POST /ingest, DELETE /ingest/{id}
│   │   │   ├── embedding.py           # POST /embed, POST /search
│   │   │   └── chat.py                # POST /chat, streaming support
│   │   └── deps.py                    # Dependency injection
│   ├── core/
│   │   ├── config.py                  # Settings: Chroma path, Ollama URL
│   │   ├── chunker.py                 # Document chunking logic
│   │   └── embedding.py              # Embedding generation via Ollama
│   ├── services/
│   │   ├── chroma_service.py          # Chroma collection ops
│   │   └── llm_service.py             # Ollama chat/completion
│   └── models/
│       ├── schemas.py                # Pydantic models
│       └── document.py                # Document chunk model
├── data/
│   └── chroma/                        # Chroma persistent storage
├── Dockerfile
├── requirements.txt
└── docker-compose.yml
```

### Frontend (Vue 3)

```
frontend/
├── src/
│   ├── api/
│   │   ├── axios.ts                   # Axios instance with interceptors
│   │   ├── auth.ts                    # /api/auth/* calls
│   │   ├── documents.ts               # /api/documents/* calls
│   │   └── chat.ts                    # /api/chat/* calls
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
│   │   ├── auth.ts                    # Pinia: user + JWT token
│   │   └── chat.ts                    # Pinia: conversation history
│   ├── router/
│   │   └── index.ts                  # Vue Router with guards
│   └── App.vue
├── .env
└── vite.config.ts
```

## Architectural Patterns

### Pattern 1: API Gateway (BFF for Python Service)

**What:** Java backend acts as a Backend-for-Frontend, proxying all Python AI service calls.
**When to use:** Always in this stack. Keeps AI service private, adds auth, enables auditing.
**Trade-offs:** Adds latency (one extra hop); keeps AI service simple and stateless.

**Example:**
```java
@Service
public class RAGService {
    private final RestTemplate ollamaClient;

    public String chat(String query, String userId) {
        // Java validates JWT, enriches request, then proxies
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = Map.of(
            "query", query,
            "user_id", userId,
            "collection", "doc_chunks"
        );
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);
        // Python service handles embedding + retrieval + LLM
        ResponseEntity<Map> resp = ollamaClient.postForEntity(
            pythonServiceUrl + "/chat", req, Map.class
        );
        return resp.getBody().get("answer");
    }
}
```

### Pattern 2: Async Ingestion Pipeline

**What:** Document upload returns immediately; actual embedding happens async via a queue or background thread.
**When to use:** Large files or slow embedding generation; need to unblock user immediately.
**Trade-offs:** Complexity in status tracking; eventual consistency.

**Simpler alternative for small scale:** Synchronous ingestion with progress indicator.

**Example (async with Spring @Async):**
```java
@Async
public void ingestDocumentAsync(Long docId, String filePath) {
    // 1. Extract text from file (PDF/DOCX parser)
    // 2. Chunk text (overlap=100 chars, chunk_size=500)
    // 3. Call Python /ingest endpoint with chunks
    // 4. Update document status to COMPLETED in PostgreSQL
}
```

### Pattern 3: Hybrid Search (Vector + Keyword)

**What:** Combine Chroma vector similarity with BM25 keyword matching for better retrieval.
**When to use:** When documents contain domain-specific terms that embeddings miss.
**Trade-offs:** Twice the retrieval time; more complex ranking logic.

**Example (simplified):**
```python
# Python service retrieval logic
async def hybrid_search(query: str, collection: str, top_k: int = 5):
    # 1. Vector search
    vector_results = chroma_service.query_vector(query, collection, top_k * 2)
    # 2. BM25 keyword search
    keyword_results = keyword_search(query, collection, top_k * 2)
    # 3. RRF fusion (Reciprocal Rank Fusion)
    fused = rrf_fusion([vector_results, keyword_results], k=60)
    return fused[:top_k]
```

## Data Flow

### Document Upload Flow

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

### Chat/QA Flow

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

### Authentication Flow (JWT)

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

## API Design Between Java and Python Services

### Internal REST Contract

All endpoints return JSON. Java calls Python; Python never calls Java.

| Python Endpoint | Method | Request Body | Response | Purpose |
|-----------------|--------|--------------|----------|---------|
| `/ingest` | POST | `{ file_path, collection, metadata }` | `{ chunk_count, status }` | Ingest document chunks |
| `/ingest/{doc_id}` | DELETE | — | `{ deleted }` | Delete document from vector DB |
| `/embed` | POST | `{ text: string }` | `{ embedding: float[] }` | Generate single embedding |
| `/search` | POST | `{ query, collection, top_k }` | `{ results: [{chunk, score}] }` | Vector similarity search |
| `/chat` | POST | `{ query, collection, history? }` | `{ answer, sources }` | Full RAG chat |

### Request/Response Examples

**Ingest Request:**
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

**Ingest Response:**
```json
{
  "chunk_count": 47,
  "status": "completed"
}
```

**Search Request:**
```json
POST /search
{
  "query": "revenue growth trends",
  "collection": "doc_chunks",
  "top_k": 5
}
```

**Search Response:**
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

**Chat Request:**
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

**Chat Response:**
```json
{
  "answer": "Based on the documents, revenue grew by 15% YoY...",
  "sources": [
    { "doc_id": 123, "chunk_id": "abc123", "text": "The revenue grew by 15%..." }
  ]
}
```

## Chroma Vector DB Integration Patterns

### Collection Management

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
        # Chroma does not support delete by metadata directly;
        # query all, filter, then delete by ID
        result = collection.get(where={"doc_id": doc_id})
        if result["ids"]:
            collection.delete(ids=result["ids"])
```

### Embedding with Ollama

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

### Chunking Strategy

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

## Scaling Considerations

| Scale | Architecture Adjustments |
|-------|-------------------------|
| 0-100 users | Monolith fine. PostgreSQL on same host. Single Ollama instance. Chroma on local disk. |
| 100-1k users | Move PostgreSQL and MinIO to separate containers. Add Redis for JWT cache. Ollama on GPU host. |
| 1k-10k users | Add connection pooling (PgBouncer). Separate Ollama instances per model. Chroma replica or switch to Milvus. Background job queue (Redis/RabbitMQ) for ingestion. |
| 10k+ users | Split into microservices: auth service, document service, chat service. Add API gateway. Switch to managed vector DB (Pinecone, Weaviate Cloud). |

### Scaling Priorities

1. **First bottleneck:** Ollama LLM inference latency. Fix: Use smaller/faster model (mistral over llama3), add GPU, or switch to quantized model.
2. **Second bottleneck:** Chroma query speed with large collections. Fix: Switch to Milvus/Pinecone, add indexes, or shard by collection.
3. **Third bottleneck:** PostgreSQL write throughput on chat_history. Fix: Add read replicas, partition chat_history by month.

## Anti-Patterns

### Anti-Pattern 1: Embedding Everything

**What people do:** Embedding entire documents without chunking, or using huge fixed-size chunks.
**Why it's wrong:** Degrades retrieval precision, hits token limits, slow queries.
**Do this instead:** Chunk to 300-800 chars with 10-20% overlap, respect semantic boundaries (paragraphs).

### Anti-Pattern 2: No Metadata Filtering

**What people do:** Storing chunks with no metadata, retrieving everything by vector distance only.
**Why it's wrong:** Cannot filter by user, document, date, or department. Security risk (data leakage across users).
**Do this instead:** Always store user_id and doc_id in metadata. Filter by user_id at query time: `where={"user_id": user_id}`.

### Anti-Pattern 3: Storing Embeddings in PostgreSQL JSON Column

**What people do:** "Let's keep it simple and store vectors in a JSONB column."
**Why it's wrong:** PostgreSQL JSON storage does not support vector operations (cosine similarity, dot product). Queries become slow full-table scans.
**Do this instead:** Use Chroma (small scale) or a dedicated vector DB (Milvus, Qdrant, Pinecone) for vector operations.

### Anti-Pattern 4: Python Service Directly Exposed to Internet

**What people do:** Exposing FastAPI directly as public API, calling Ollama from frontend.
**Why it's wrong:** No authentication, no audit logging, Ollama has no access control.
**Do this instead:** Java backend is the only public-facing service. Python service lives on internal network/VPC.

### Anti-Pattern 5: Synchronous File Upload Blocking LLM Inference

**What people do:** Uploading a 50-page PDF, waiting for embedding to complete in the same HTTP request.
**Why it's wrong:** HTTP timeout, poor UX, blocks connection pool.
**Do this instead:** Return 202 Accepted immediately with a job ID. Process async. Poll or use WebSocket for status updates.

## Integration Points

### External Services

| Service | Integration Pattern | Notes |
|---------|---------------------|-------|
| Ollama | HTTP POST to `/api/generate` and `/api/embeddings` | Self-hosted LLM. Manage via systemd or Docker. |
| Chroma | Python client (local process) | Persistent storage on shared volume if using Docker. |
| MinIO | S3-compatible SDK | Same API as AWS S3; switch to real S3 by changing endpoint. |
| OpenAI (optional) | SDK + proxy | If Ollama insufficient, add OpenAI as fallback via same interface. |

### Internal Boundaries

| Boundary | Communication | Notes |
|----------|---------------|-------|
| Frontend ↔ Java | REST over HTTPS | JWT in Authorization header. Axios interceptor handles 401 redirect to login. |
| Java ↔ Python | REST over HTTP (internal) | No auth between services if on same Docker network; add API key if across VPCs. |
| Python ↔ Ollama | HTTP to localhost:11434 | Ollama must be on same host or accessible network. |
| Python ↔ Chroma | In-process Python client | Chroma persistent dir mounted as Docker volume. |

## Sources

- Chroma Documentation: https://docs.trychroma.com
- Ollama Documentation: https://github.com/ollama/ollama
- Spring Boot Security with JWT: https://spring.io/guides/gs/securing-web
- FastAPI + Chroma RAG tutorial: https://docs.llamaindex.ai/examples/vector_stores/chroma
- LangChain Text Splitters: https://python.langchain.com/docs/modules/data_connection/document_transformers

---
*Architecture research for: RAG Knowledge Base Q&A*
*Researched: 2026-09-29*
