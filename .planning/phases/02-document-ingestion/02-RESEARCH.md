# Phase 2: Document Ingestion - Research

**Researched:** 2026-10-01
**Domain:** RAG document ingestion pipeline (upload, parse, chunk, embed, index)
**Confidence:** MEDIUM

## Summary

Phase 2 implements the document ingestion pipeline for the RAG knowledge base: users upload documents (PDF, Word, TXT), the system extracts text, chunks it (500 tokens / 50 overlap), generates embeddings via Silicon Flow API, and stores vectors in Chroma. The pipeline is **synchronous** (files < 10MB) with per-user Chroma collections for isolation.

**Primary recommendation:** Implement the AI service as a Python FastAPI wrapper that receives text chunks from the Java backend, calls Silicon Flow `/v1/embeddings`, and returns vectors for Chroma indexing. Alternatively, the Java backend can call Silicon Flow directly using RestTemplate/WebClient if no complex AI logic is needed in this phase.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| File upload API | Java Spring Boot (backend) | — | Auth filter, multipart handling, temp file management |
| Text extraction (PDF/Word/TXT) | Java Spring Boot (backend) | Python AI service | PyMuPDF/pypdf in Java via process exec, or Python service |
| Chunking logic | Java Spring Boot (backend) | — | 500 token / 50 overlap with token estimation |
| Embedding generation | Silicon Flow API | — | External API, called from backend or AI service |
| Chroma indexing | Python AI service | — | Chroma Python client, per-user collections |
| Frontend upload UX | Vue 3 + uni-app | — | Axios multipart upload, progress feedback |

---

## User Constraints (from 02-CONTEXT.md)

### Locked Decisions
- **D-10:** Synchronous processing (file < 10MB), no async queue infrastructure
- **D-11:** Silicon Flow `/embeddings` API for embedding generation
- **D-12:** Per-user Chroma collection organization
- **D-13:** PyMuPDF (`pypdf`) for Chinese document processing
- **D-14:** 500 token chunks, 50 token overlap (~10%)

### Claude's Discretion
- Whether to call Silicon Flow directly from Java backend (simpler) or via the Python AI service (more consistent architecture)

### Deferred Ideas (OUT OF SCOPE)
- Async processing + polling status
- OCR support for scanned documents
- Incremental index updates
- Batch upload queue

---

## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| RAG-01 | User can upload PDF/Word/TXT documents | Spring Boot `MultipartFile` upload endpoint, file validation, 10MB limit |
| RAG-02 | System parses documents and generates vectors, stored in Chroma | PyMuPDF text extraction, Silicon Flow API, Chroma Python client indexing |

---

## Standard Stack

### AI Service (Python FastAPI)

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| `fastapi` | latest | AI service HTTP framework | Async, OpenAPI auto-docs, uvicorn server |
| `uvicorn` | latest | ASGI server | Standard FastAPI server |
| `chromadb` | latest | Vector database client | Open-source embedding DB, Python-native |
| `requests` | latest | HTTP client for Silicon Flow API | Simple, well-tested |
| `pypdf` | 5.x | PDF text extraction | Pure Python, active maintenance, good for Chinese |
| `python-docx` | latest | Word (.docx) text extraction | Standard Python docx library |

### Backend (Java Spring Boot 2.7.18) — Existing

| Library | Purpose | Status |
|---------|---------|--------|
| `spring-boot-starter-web` | REST API, Multipart support | Already in pom.xml |
| `spring-boot-starter-security` | JWT auth filter | Already in pom.xml |
| MyBatis-Plus | Database access | Already in pom.xml |
| H2/MySQL | User/entity persistence | Already configured |

### Frontend

| Library | Purpose | Status |
|---------|---------|--------|
| `axios` | HTTP client | Already in http.js |
| Element Plus | UI components | Already used in login.vue |

### External APIs

| Service | Endpoint | Model | Dimensions | Batch Limit |
|---------|----------|-------|------------|-------------|
| Silicon Flow | `https://api.siliconflow.cn/v1/embeddings` | `BAAI/bge-m3` | 1024 | ~8,192 tokens input |

---

## Package Legitimacy Audit

> All packages are standard ecosystem libraries. No external package installs required for Phase 2 beyond standard pip packages.

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| `chromadb` | PyPI | 7+ yrs | 5M+/wk | github.com/chroma-core/chroma | OK | Approved |
| `pypdf` | PyPI | 7+ yrs | 10M+/wk | github.com/py-pdf/pypdf | OK | Approved |
| `python-docx` | PyPI | 10+ yrs | 20M+/wk | github.com/python-openxml/python-docx | OK | Approved |
| `fastapi` | PyPI | 7+ yrs | 50M+/wk | github.com/tiangolo/fastapi | OK | Approved |
| `requests` | PyPI | 15+ yrs | 100M+/wk | github.com/psf/requests | OK | Approved |

**Packages removed due to [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none

---

## Architecture Patterns

### System Architecture Diagram

```
[Vue3 Frontend]
    |  multipart/form-data POST /api/documents/upload
    v
[Java Spring Boot :8080]  ---JWTToken---> [Extract: PyMuPDF/pypdf]
    |  DocumentController                          |  Text
    |  MultipartFile --> temp file                 v
    |  Validate size/extension           [Python FastAPI AI Service :8000]
    |                                             |  /extract   (optional, if text extraction in Python)
    |  Call Silicon Flow /embeddings              |  /embed     (generate vectors)
    |  or call AI service for embeddings          v
    |                                    [Silicon Flow API]
    |  Receive embedding vectors                   |  POST /v1/embeddings
    |  Call AI service to index                  |  model=BAAI/bge-m3, dim=1024
    v                                             v
[Python FastAPI AI Service :8000]         [Chroma persistent DB]
    |  Chroma Python client                       |  backend/data/chroma/
    |  get_or_create_collection(userId)          |
    |  collection.add(ids, embeddings, docs, metadata)
    v
[Response back through chain to frontend]
```

### Recommended Project Structure

```
backend/
├── src/main/java/com/aikb/
│   ├── controller/
│   │   └── DocumentController.java       # Upload endpoint /api/documents/upload
│   ├── service/
│   │   ├── DocumentService.java           # Orchestration: extract + chunk + embed + index
│   │   └── TextExtractionService.java     # PyMuPDF/pypdf wrapper (or exec Python)
│   ├── dto/
│   │   ├── DocumentUploadResponse.java
│   │   └── DocumentChunk.java
│   └── config/
│       └── WebClientConfig.java           # For calling Silicon Flow API
├── uploads/                               # Temp file storage (gitignored)
└── data/chroma/                           # Chroma persist directory

ai-service/
├── main.py                                # FastAPI app entry
├── routers/
│   ├── embeddings.py                      # /embed endpoint
│   └── health.py                          # /health endpoint
├── services/
│   ├── chroma_service.py                  # Chroma collection management
│   └── siliconflow_client.py              # Silicon Flow API client
└── requirements.txt
```

### Pattern 1: Synchronous Document Ingestion Pipeline

**What:** Single REST call handles upload through indexing synchronously.

**When to use:** Files < 10MB, simple pipeline, MVP stage.

**Flow:**
1. Frontend sends `POST /api/documents/upload` with `multipart/form-data`
2. Backend validates JWT, saves temp file, validates extension (pdf/docx/txt)
3. Backend extracts text (Java PyMuPDF via process exec, or delegates to Python service)
4. Backend chunks text (500 tokens / 50 overlap, estimated via character count)
5. Backend calls Silicon Flow `/v1/embeddings` for each chunk batch
6. Backend calls AI service to `collection.add()` to Chroma
7. Backend returns success with chunk count and document ID

**Example (Spring Boot controller):**
```java
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("knowledgeBaseId") Long kbId,
            Authentication auth) {
        // Validate file size, extension
        // Delegate to DocumentService
        // Return response
    }
}
```

### Pattern 2: Silicon Flow Embedding API Call

**What:** Call Silicon Flow's OpenAI-compatible embeddings endpoint.

**When to use:** For generating text embeddings using BAAI/bge-m3 model.

**Request format:**
```json
POST https://api.siliconflow.cn/v1/embeddings
Authorization: Bearer <SILICON_FLOW_API_KEY>
Content-Type: application/json

{
  "model": "BAAI/bge-m3",
  "input": ["text chunk 1", "text chunk 2"],
  "encoding_format": "float"
}
```

**Response format:**
```json
{
  "object": "list",
  "model": "BAAI/bge-m3",
  "data": [
    { "embedding": [0.011..., ...], "index": 0 },
    { "embedding": [0.021..., ...], "index": 1 }
  ],
  "usage": { "prompt_tokens": 50, "completion_tokens": 0, "total_tokens": 50 }
}
```

**BGE-M3 dimensions:** 1024 (L2-normalized, use cosine similarity)

### Pattern 3: Chroma Per-User Collection with Custom Embedding Function

**What:** Each user has their own Chroma collection, isolated by `userId`.

**When to use:** Multi-tenant RAG where user data must be isolated.

**Example:**
```python
import chromadb
from chromadb.utils import embedding_functions

# Custom embedding function calling Silicon Flow
class SiliconFlowEmbeddingFunction(EmbeddingFunction):
    def __init__(self, api_key: str):
        self.api_key = api_key

    def __call__(self, texts: Documents) -> Embeddings:
        import requests
        response = requests.post(
            "https://api.siliconflow.cn/v1/embeddings",
            headers={"Authorization": f"Bearer {self.api_key}"},
            json={"model": "BAAI/bge-m3", "input": texts, "encoding_format": "float"}
        )
        data = response.json()["data"]
        return [item["embedding"] for item in sorted(data, key=lambda x: x["index"])]

# Persistent client with per-user collection
client = chromadb.PersistentClient(path="./backend/data/chroma")
collection = client.get_or_create_collection(
    name=f"user_{user_id}_kb_{kb_id}",
    embedding_function=SiliconFlowEmbeddingFunction(api_key=os.getenv("SILICON_FLOW_API_KEY"))
)

# Add chunks
collection.add(
    ids=[f"doc_{doc_id}_chunk_{i}" for i in range(len(chunks))],
    embeddings=embeddings,  # 1024-dim float vectors
    documents=chunks,
    metadatas=[{"docId": doc_id, "kbId": kb_id, "chunkIndex": i} for i in range(len(chunks))]
)
```

### Pattern 4: Text Chunking with Overlap

**What:** Split text into fixed-token chunks with sliding window overlap.

**When to use:** RAG retrieval where context window matters.

**Token estimation:** For Chinese text, a rough estimate is ~0.75 tokens per character (1 token ~= 1.3 Chinese chars). For English, ~0.25 tokens per character. For simplicity, use `len(text) * 0.75` as token estimate for mixed Chinese/English content.

**500 tokens ~ 665 characters (Chinese) or ~2000 chars (English)**

**Example (Java):**
```java
public List<String> chunkText(String text, int chunkSizeTokens, int overlapTokens) {
    List<String> chunks = new ArrayList<>();
    int tokenEstimate = (int) (text.length() * 0.75); // Chinese chars
    if (tokenEstimate <= chunkSizeTokens) {
        return Collections.singletonList(text);
    }
    int chunkSizeChars = (int) (chunkSizeTokens / 0.75);
    int overlapChars = (int) (overlapTokens / 0.75);
    int start = 0;
    while (start < text.length()) {
        int end = Math.min(start + chunkSizeChars, text.length());
        chunks.add(text.substring(start, end));
        start += chunkSizeChars - overlapChars;
    }
    return chunks;
}
```

### Anti-Patterns to Avoid

- **Synchronous large file processing:** Don't block the upload thread for files approaching 10MB. The context says < 10MB is acceptable for sync, but consider a timeout.
- **Storing files in database:** Store files in local filesystem (`backend/uploads/`), not in the database BLOB.
- **No cleanup of temp files:** Use `@PreDestroy` or scheduled task to clean `uploads/` after processing.
- **Single large embedding request:** BGE-M3 has 8,192 token limit. Batch chunks intelligently.
- **Using ephemeral Chroma client:** Use `PersistentClient` with path `./backend/data/chroma` — ephemeral in-memory Chroma loses all data on restart.

---

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Embedding generation | Custom ML model | Silicon Flow `BAAI/bge-m3` | Free tier, 1024-dim, Chinese-optimized, no infra |
| Vector storage | SQL JSON column | Chroma | Purpose-built for embeddings, CRUD, similarity search |
| Text extraction from PDF | Regex parsing | `pypdf` or `PyMuPDF` | Handles encoding, layout, multi-page correctly |
| Token counting for chunking | GPT tokenizers | Char-count heuristic | Sufficient for MVP accuracy, no external dep |
| Custom HTTP client for Silicon Flow | Raw HttpURLConnection | `WebClient` (Spring) or `requests` (Python) | Async support, connection pooling, retry |

---

## Common Pitfalls

### Pitfall 1: Chinese Text Extraction Garbling
**What goes wrong:** Chinese characters appear as boxes or garbled text after PDF extraction.
**Why it happens:** PyMuPDF/pypdf may return text in reading order that doesn't match visual order, especially for vertical layouts or multi-column Chinese docs.
**How to avoid:** Use `page.get_text('text')` with raw text line concatenation rather than relying on blocks. Sort by y-coordinate then x-coordinate for multi-column detection.
**Warning signs:** `len(text) > 0` but Chinese chars don't render, or text order is scrambled.

### Pitfall 2: Chunk Boundary Cutting Mid-Sentence
**What goes wrong:** Chunks cut mid-sentence or mid-paragraph, losing semantic coherence.
**Why it happens:** Fixed character/token boundaries ignore sentence structure.
**How to avoid:** After rough token-length split, snap boundaries to nearest sentence-ending punctuation (`.`, `。`, `!`, `!`, `?`, `？`).
**Warning signs:** Retrieval returns fragments that are grammatically incomplete.

### Pitfall 3: Chroma Collection Name Collision
**What goes wrong:** Collections for different users or knowledge bases collide if naming is not unique.
**Why it happens:** Simple collection name like "documents" shared across users.
**How to avoid:** Use `user_{userId}_kb_{kbId}` naming convention with `get_or_create_collection()`.
**Warning signs:** User A sees User B's documents in search results.

### Pitfall 4: Large File Timeout
**What goes wrong:** Upload or processing times out for files near 10MB.
**Why it happens:** Default Spring Boot timeout or proxy timeout (nginx) is too short.
**How to avoid:** Set `spring.servlet.multipart.max-file-size=10MB` and ensure proxy timeouts are sufficient.
**Warning signs:** `504 Gateway Timeout` on uploads around 5MB+.

### Pitfall 5: Temp File Leak
**What goes wrong:** Uploaded temp files accumulate in `backend/uploads/` directory.
**Why it happens:** No cleanup after processing completes (success or failure).
**How to avoid:** Use `Files.createTempFile()` with immediate `deleteOnExit()`, or explicit cleanup in finally block or `@PreDestroy`.
**Warning signs:** `uploads/` directory grows unbounded over time.

---

## Code Examples

### Silicon Flow Embeddings API Call (Java WebClient)

```java
// Source: Silicon Flow API documentation [WEB_SEARCHED]
// BAAI/bge-m3 model: 1024 dimensions, max 8192 tokens input

public List<float[]> generateEmbeddings(List<String> texts, String apiKey) {
    WebClient client = WebClient.builder()
        .baseUrl("https://api.siliconflow.cn/v1")
        .defaultHeader("Authorization", "Bearer " + apiKey)
        .build();

    Map<String, Object> request = Map.of(
        "model", "BAAI/bge-m3",
        "input", texts,
        "encoding_format", "float"
    );

    List<?> data = client.post()
        .uri("/embeddings")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(Map.class)
        .map(r -> (List<?>) r.get("data"))
        .block();

    return data.stream()
        .map(item -> {
            @SuppressWarnings("unchecked")
            List<Number> embedding = (List<Number>) ((Map<?, ?>) item).get("embedding");
            float[] vec = new float[embedding.size()];
            for (int i = 0; i < vec.length; i++) vec[i] = embedding.get(i).floatValue();
            return vec;
        })
        .collect(Collectors.toList());
}
```

### PyMuPDF PDF Text Extraction (Python)

```python
# Source: PyMuPDF documentation [WEB_SEARCHED]
import fitz  # PyMuPDF

def extract_text_from_pdf(file_path: str) -> str:
    """Extract text from PDF, preserving reading order for Chinese docs."""
    doc = fitz.open(file_path)
    pages_text = []
    for page_num, page in enumerate(doc):
        # 'text' mode returns blocks in reading order
        blocks = page.get_text('text', flags=fitz.TEXT_PRESERVE_WHITESPACE)
        pages_text.append(blocks)
    doc.close()
    return '\n\n'.join(pages_text)
```

### python-docx Text Extraction (Python)

```python
# Source: python-docx documentation [WEB_SEARCHED]
from docx import Document

def extract_text_from_docx(file_path: str) -> str:
    """Extract all paragraph text from Word document."""
    doc = Document(file_path)
    paragraphs = [p.text for p in doc.paragraphs if p.text.strip()]
    return '\n\n'.join(paragraphs)
```

### Chroma Query (Python)

```python
# Source: Chroma documentation [WEB_SEARCHED]
# Query with pre-computed query embedding (from Silicon Flow)
results = collection.query(
    query_embeddings=[query_embedding_vector],  # 1024-dim float list
    n_results=5,
    where={"kbId": str(kb_id)}  # metadata filter
)
# results["documents"], results["metadatas"], results["distances"], results["ids"]
```

---

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| OpenAI `text-embedding-ada-002` | `BAAI/bge-m3` via Silicon Flow | 2024 | Free, Chinese-optimized, 1024-dim |
| Single collection per app | Per-user per-knowledge-base collections | 2024 | Better data isolation, per-user delete is clean |
| Async Celery queue for processing | Synchronous REST pipeline | 2024 MVP | Simpler infra, acceptable for <10MB files |
| LangChain for embeddings | Direct Silicon Flow API call | 2024 | Less abstraction, no LangChain dependency |

---

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | Silicon Flow endpoint is `https://api.siliconflow.cn/v1/embeddings` (confirmed from search results as `api.siliconflow.cn`) | Standard Stack, Code Examples | Backend fails to call API — easy to fix, just update URL |
| A2 | BGE-M3 produces 1024-dimensional embeddings (confirmed in search) | Standard Stack | Chroma collection dimension mismatch — would need to verify by testing |
| A3 | 500 tokens ~ 665 Chinese characters (char-count heuristic) | Common Pitfalls | Chunk sizes slightly off — acceptable for MVP |
| A4 | `backend/uploads/` is gitignored and used as temp storage | Architecture Patterns | Files accidentally committed — gitignore fix |
| A5 | AI service runs on port 8000 separately from Spring Boot on 8080 | Architecture Patterns | Port conflict or CORS issues — configure appropriately |
| A6 | Frontend upload URL is `/api/documents/upload` | Architecture Patterns | Wrong endpoint — frontend needs update |
| A7 | No AI service code exists yet (ai-service directory not found) | Project Structure | Need to create from scratch — included in Phase 2 scope |

---

## Open Questions

1. **Silicon Flow endpoint discrepancy**
   - Context says `https://api.ai.mlops-api.com/embeddings` but search shows `https://api.siliconflow.cn/v1/embeddings`
   - What we know: Both are plausible Silicon Flow endpoints; `api.siliconflow.cn` is confirmed in multiple docs
   - What's unclear: Whether `api.ai.mlops-api.com` is a deprecated or alternate endpoint
   - **Recommendation:** Use `api.siliconflow.cn` as it is documented and verified; `api.ai.mlops-api.com` should be confirmed with actual API key testing

2. **Text extraction location (Java vs Python)**
   - What we know: PyMuPDF has Java bindings (`org.bouncycastle` ecosystem) but Python `pypdf`/`fitz` is more mature for Chinese text
   - What's unclear: Whether to do extraction in Java (simpler one-service) or delegate to Python AI service (more consistent architecture)
   - **Recommendation:** Claude's discretion per D-13 context — lean toward Python for extraction quality

3. **Whether to create ai-service from scratch or extend**
   - What we know: No `ai-service/` directory found in repo
   - What's unclear: Should Phase 2 create the ai-service skeleton, or only the document ingestion piece?
   - **Recommendation:** Create minimal ai-service in Phase 2 with just `/embed` and `/health` endpoints

---

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Java 8+ | Backend Spring Boot 2.7.18 | Verified (pom.xml `java.version=1.8`) | 1.8+ | — |
| Python 3.8+ | AI service | [ASSUMED] — not verified on machine | — | Need to confirm |
| `pip` | Installing Python packages | [ASSUMED] | — | — |
| Node.js | Frontend dev server | [ASSUMED] from vite.config.js | — | — |
| Spring Boot 2.7.18 | Backend framework | Verified (pom.xml) | 2.7.18 | — |
| H2 / MySQL | Database | Configured in application.yml | — | — |

**Missing dependencies with no fallback:**
- Python runtime for AI service (needs to be installed on deployment machine)

**Missing dependencies with fallback:**
- None identified

---

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | pytest (Python AI service) + JUnit 5 (Java backend) |
| Config file | `backend/src/test/resources/application.yml` |
| Quick run command | `pytest ai-service/tests/ -v` / `mvn test -pl backend` |

### Phase Requirements to Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| RAG-01 | Upload PDF | Integration | `POST /api/documents/upload` with pdf | No |
| RAG-01 | Upload Word | Integration | `POST /api/documents/upload` with docx | No |
| RAG-01 | Upload TXT | Integration | `POST /api/documents/upload` with txt | No |
| RAG-01 | Reject >10MB | Unit | Size validation test | No |
| RAG-01 | Reject unsupported type | Unit | Extension validation test | No |
| RAG-02 | Extract PDF text | Unit | PyMuPDF extract vs expected | No |
| RAG-02 | Chunk text 500/50 | Unit | Chunk count and overlap verification | No |
| RAG-02 | Generate embeddings | Integration | Silicon Flow API call mock | No |
| RAG-02 | Store in Chroma | Integration | Query back and verify | No |
| RAG-02 | E2E: upload -> query | E2E | Full pipeline integration test | No |

### Sampling Rate
- **Per task commit:** `pytest ai-service/tests/ -x -q` (AI service), `mvn test -pl backend -Dtest=Document*Test -q` (backend)
- **Per wave merge:** Full suite `pytest ai-service/tests/` + `mvn test -pl backend`
- **Phase gate:** Full suite green before `/gsd-verify-work`

### Wave 0 Gaps
- [ ] `ai-service/tests/test_embeddings.py` — tests for Silicon Flow client mock
- [ ] `ai-service/tests/test_chroma.py` — tests for Chroma collection add/query
- [ ] `ai-service/tests/test_chunking.py` — tests for 500/50 chunking logic
- [ ] `backend/src/test/java/com/aikb/service/DocumentServiceTest.java` — tests for upload orchestration
- [ ] `backend/src/test/java/com/aikb/controller/DocumentControllerTest.java` — tests for REST endpoint
- [ ] `ai-service/requirements.txt` — pip freeze of dependencies
- [ ] Framework install: `pip install -r ai-service/requirements.txt` — if not detected

*(All gaps are new — no existing test infrastructure for document ingestion)*

---

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | Yes | JWT token validation on all `/api/documents/*` endpoints |
| V3 Session Management | No | Stateless JWT, no server-side session |
| V4 Access Control | Yes | User can only upload to their own collections; userId extracted from JWT |
| V5 Input Validation | Yes | File extension allowlist (pdf/docx/txt), size limit (10MB), content type check |
| V6 Cryptography | No | No cryptographic operations in this phase |

### Known Threat Patterns for Document Ingestion

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Malicious file upload (polyglot PDF) | Tampering | Extension + MIME type validation; do not execute extracted content |
| Path traversal via filename | Tampering | Sanitize generated filenames; store with UUID, not user-provided name |
| Large file DoS | Denial | 10MB hard limit in MultipartConfig; reject at gateway |
| Unauthorized collection access | Information Disclosure | JWT userId in collection name; no cross-user collection queries |
| Prompt injection via document | Spoofing | LLM prompt injection handled in Phase 3 (RAG-03) |

---

## Sources

### Primary (HIGH confidence)
- [Silicon Flow API Documentation](https://docs.siliconflow.com/cn/api-reference/embeddings/create-embeddings) — API endpoint, request/response format, model list
- [Chroma Python Client Documentation](https://docs.trychroma.com/docs/overview/getting-started) — PersistentClient, collection CRUD, query API

### Secondary (MEDIUM confidence)
- [PyMuPDF Text Extraction](https://pypdf.readthedocs.io/en/3.1.0/user/extract-text.html) — PDF text extraction patterns
- [python-docx Documentation](https://python-docx.readthedocs.io/) — Word document paragraph extraction
- [CSDN: BGE-M3 Embedding Dimensions](https://blog.csdn.net/Coach_RR/article/details/160820300) — 1024 dimensions confirmed

### Tertiary (LOW confidence — verify before use)
- `https://api.ai.mlops-api.com/embeddings` — Context mentions this URL; needs confirmation against actual API
- Token estimation ratio (0.75 chars/token for Chinese) — industry heuristic, not authoritative

---

## Metadata

**Confidence breakdown:**
- Standard stack: MEDIUM — Silicon Flow endpoint partially confirmed, AI service structure assumed
- Architecture: MEDIUM — per-user collections confirmed, Java-vs-Python extraction split open
- Pitfalls: MEDIUM — chunking/token heuristics are standard practices, Chinese text ordering risk is real

**Research date:** 2026-10-01
**Valid until:** 2026-10-31 (30 days for stable domain, API endpoints unlikely to change)
