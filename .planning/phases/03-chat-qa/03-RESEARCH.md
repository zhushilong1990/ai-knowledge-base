# Phase 3: Chat/QA - Research

**Researched:** 2026-10-01
**Domain:** RAG-based chat with ChromaDB retrieval and LLM generation
**Confidence:** MEDIUM-HIGH

## Summary

Phase 3 implements the chat/QA functionality where users ask questions and receive answers grounded in retrieved knowledge base content. The architecture follows a Java BFF calling a Python script that performs ChromaDB retrieval and SiliconFlow LLM generation, following the established ProcessBuilder pattern from Phase 2. Key decisions locked: MySQL for chat history, Top-3 + 0.7 threshold for retrieval, non-streaming responses, numbered citation format【1】【2】【3】.

**Primary recommendation:** Reuse the ProcessBuilder-to-Python pattern from DocumentService for the chat flow, with a new `chat_and_answer.py` script handling Chroma query + LLM generation. Chat history tables (session + message) extend the existing schema.sql.

## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-15:** Chat history stored in backend MySQL, cross-session persistence, multi-device access
- **D-16:** Top-3 + similarity threshold 0.7 — retrieve top 3 chunks, discard below 0.7
- **D-17:** One-shot (non-streaming) response — MVP fastest delivery, simple implementation
- **D-18:** Numbered list citation format【1】【2】【3】— concise, good LLM output compatibility
- **D-19:** Return error prompt + suggest retry — no fallback mechanism

### Claude's Discretion
None — all decisions are locked by user

### Deferred Ideas (OUT OF SCOPE)
- Streaming SSE output (post-Phase 3 MVP)
- Keyword match fallback (post-Phase 3 MVP)
- Inline citation format (post-Phase 3 MVP evaluation)

## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| RAG-03 | RAG retrieval + LLM generation | ChromaDB query() with metadata filter + SiliconFlow chat completions |
| CHAT-01 | Multi-turn conversation | MySQL chat_session + chat_message tables with sessionId linking |

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Chat history persistence | Backend (MySQL) | — | D-15 locked: cross-session, multi-device |
| RAG retrieval | Python script (Chroma) | — | Vector search happens in Python |
| LLM generation | Python script (SiliconFlow) | — | API call in Python, follows Phase 2 pattern |
| Chat API endpoint | Java Spring Boot | — | REST controller, ProcessBuilder orchestration |
| Chat UI | Vue 3 frontend | — | Pinia store, Element Plus components |
| Session management | Frontend Pinia | Backend MySQL | Frontend tracks active session, MySQL persists history |

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| ChromaDB Python client | 0.4.x | Vector retrieval | Local, free, Phase 2 already in use |
| SiliconFlow API | v1 | LLM inference | OpenAI-compatible, DeepSeek models available |
| DeepSeek model | V3 | LLM generation | Per project CLAUDE.md constraints |
| MySQL | 8.x | Chat history storage | Already in project stack |
| Vue 3 + Element Plus | 3.5.x / 2.14.x | Chat UI | Per project CLAUDE.md |
| Pinia | 4.0.x | Session state | Per project CLAUDE.md |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| requests | 2.x | Python HTTP client | Python scripts calling SiliconFlow API |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| MySQL chat history | SQLite | SQLite simpler but not multi-device accessible |
| ProcessBuilder | FastAPI direct call | ProcessBuilder pattern already established in Phase 2 |

**Installation:**
No new npm packages required for backend. Python side uses existing `chromadb` and `requests`.

## Architecture Patterns

### System Architecture Diagram

```
User (Vue 3 UI)
    |
    | POST /api/chat/ask
    v
ChatController (Java Spring Boot)
    | ProcessBuilder (stdin JSON)
    v
chat_and_answer.py (Python)
    |
    +---> ChromaDB query()
    |       collection: user_{userId}_kb_{kbId}
    |       n_results=3, where filter by kbId
    |       post-filter by similarity >= 0.7
    |
    +---> SiliconFlow /v1/chat/completions
    |       model: deepseek-ai/DeepSeek-V3
    |       system prompt with citations
    |
    v
Return JSON: { answer, sources: [{id, text, score}] }
    |
    v
ChatController --> ResponseEntity to Vue
    |
    v
Vue updates Pinia chat store + renders messages
```

### Recommended Project Structure

```
backend/
├── src/main/java/com/aikb/
│   ├── controller/
│   │   └── ChatController.java       # REST endpoint, auth extraction
│   ├── service/
│   │   └── ChatService.java          # ProcessBuilder orchestration, DB persistence
│   ├── entity/
│   │   ├── ChatSession.java          # session metadata
│   │   └── ChatMessage.java          # individual messages
│   ├── mapper/
│   │   ├── ChatSessionMapper.java
│   │   └── ChatMessageMapper.java
│   └── dto/
│       ├── ChatRequest.java          # { question, kbId, sessionId? }
│       └── ChatResponse.java         # { answer, sources, sessionId }
├── src/main/resources/
│   └── schema.sql                    # ADD: chat_session, chat_message tables
└── scripts/
    └── chat_and_answer.py            # RAG retrieval + LLM generation

frontend/src/
├── pages/
│   └── chat.vue                      # Main chat interface
├── stores/
│   └── chat.js                       # Pinia store: sessions, messages
└── api/
    └── chat.js                       # Axios API module
```

### Pattern 1: ProcessBuilder-to-Python (Established in Phase 2)

**What:** Java Service calls Python script via ProcessBuilder, passing JSON on stdin, reading JSON from stdout.

**When to use:** Integration with Python ML services (Chroma, LLM APIs).

**Example:**
```java
// Source: DocumentService.java:119-176 (existing Phase 2 pattern)
private String executePythonScript(String scriptPath, String requestJson) throws Exception {
    ProcessBuilder pb = new ProcessBuilder("python", scriptPath);
    pb.redirectErrorStream(false);
    Process process = pb.start();
    // Write JSON to stdin, read from stdout
    // Handle timeout, capture stderr
    return stdout.toString().trim();
}
```

### Pattern 2: RAG Query with Post-Filtering

**What:** ChromaDB `collection.query()` returns all n_results, then filter by similarity threshold in Python.

**When to use:** When you need both Top-K retrieval and a minimum similarity threshold.

**Example:**
```python
# Source: [ASSUMED based on ChromaDB 0.4.x documentation]
results = collection.query(
    query_texts=[question],
    n_results=10,  # Fetch more than needed
    where={"kbId": str(kb_id)},  # Filter by knowledge base
    include=["documents", "distances", "metadatas"]
)

# Post-filter by similarity threshold 0.7
# Cosine distance: 0 = identical, 2 = opposite
# similarity = 1 - distance/2
SIMILARITY_THRESHOLD = 0.7
MAX_DISTANCE = 1 - (2 * SIMILARITY_THRESHOLD)  # = 0.6

filtered = []
for doc, dist, meta in zip(results["documents"][0], results["distances"][0], results["metadatas"][0]):
    if dist <= MAX_DISTANCE:
        filtered.append({"document": doc, "distance": dist, "metadata": meta})
    if len(filtered) >= 3:
        break
```

### Pattern 3: RAG System Prompt with Citations

**What:** System prompt instructs LLM to cite sources using numbered markers【N】.

**When to use:** When building a RAG pipeline that returns attributed answers.

**Example:**
```python
# Source: [ASSUMED based on RAG best practices]
SYSTEM_PROMPT = """你是一个基于知识库的问答助手。

参考信息：
{context}

要求：
1. 仅基于上述参考信息回答问题
2. 每个引用使用【N】格式，例如【1】【2】【3】
3. 如果参考信息不足以回答，请明确说明
4. 回答简洁明了

问题：{question}"""

def build_context(sources):
    """Build context string with numbered citations"""
    context_parts = []
    for i, source in enumerate(sources, 1):
        context_parts.append(f"【{i}】{source['document']}")
    return "\n\n".join(context_parts)
```

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Vector search | Custom ANN index | ChromaDB | Already integrated in Phase 2 |
| LLM API integration | Raw HTTP with manual retry | requests library + OpenAI SDK | OpenAI-compatible SDK simplifies auth, streaming, error handling |
| Chat history across devices | localStorage or sessionStorage | MySQL tables | D-15 locked decision |
| JSON parsing in Java | String concatenation | ObjectMapper (Jackson) | Already in project, Phase 2 uses it |

**Key insight:** Phase 2 established the ProcessBuilder pattern for Java→Python integration. Phase 3 reuses this pattern with a new `chat_and_answer.py` script.

## Common Pitfalls

### Pitfall 1: ChromaDB Distance Threshold Not Applied
**What goes wrong:** Retrieving chunks with low similarity, causing LLM to generate irrelevant answers.
**Why it happens:** ChromaDB `query()` has no native distance threshold — filtering must be done post-query.
**How to avoid:** Always apply post-filter in Python: `if similarity >= 0.7`.
**Warning signs:** LLM answer unrelated to user question, high citation numbers but wrong content.

### Pitfall 2: Missing Metadata Filter Causes Cross-User Data Leak
**What goes wrong:** User A sees User B's documents.
**Why it happens:** Forgetting `where` filter on userId/kbId in Chroma query.
**How to avoid:** Always include `where={"kbId": kb_id, ...}` filter; collection name alone is insufficient.
**Warning signs:** Wrong document content appearing in answers for different users.

### Pitfall 3: Chat Session Not Created Before First Message
**What goes wrong:** First message in a new session fails because sessionId is null.
**Why it happens:** Frontend creates session after receiving response, but backend expects existing sessionId.
**How to avoid:** If `sessionId` is null in request, create new session in ChatService before processing.
**Warning signs:** 500 error on first message of new conversation.

### Pitfall 4: LLM Hallucinating Citations for Missing Context
**What goes wrong:** LLM cites【1】【2】【3】but the chunks don't support the answer.
**Why it happens:** Prompt doesn't explicitly instruct LLM to only cite provided context.
**How to avoid:** System prompt must say "仅基于上述参考信息回答" and "如果不足以回答请说明".

### Pitfall 5: Chat History Loading Bottleneck
**What goes wrong:** Loading 100+ messages on chat page causes UI freeze.
**Why it happens:** Fetching all messages synchronously without pagination.
**How to avoid:** Backend pagination (limit/offset), frontend virtual scrolling for long lists.

## Code Examples

### ChromaDB RAG Query (Python)
```python
# Source: [ASSUMED based on ChromaDB 0.4.x API]
import chromadb
from chromadb.config import Settings

def rag_retrieve(question, kb_id, user_id, chroma_path, top_k=10, threshold=0.7):
    """Retrieve relevant chunks from ChromaDB with similarity filtering."""
    collection_name = f"user_{user_id}_kb_{kb_id}"
    client = chromadb.PersistentClient(path=chroma_path)
    collection = client.get_or_create_collection(name=collection_name)
    
    # Query with metadata filter
    results = collection.query(
        query_texts=[question],
        n_results=top_k,
        where={"kbId": str(kb_id)},
        include=["documents", "distances", "metadatas"]
    )
    
    # Post-filter by similarity threshold
    MAX_DISTANCE = 1 - (2 * threshold)  # 0.6 for threshold=0.7
    filtered = []
    for doc, dist, meta in zip(
        results["documents"][0],
        results["distances"][0],
        results["metadatas"][0]
    ):
        if dist <= MAX_DISTANCE:
            filtered.append({
                "document": doc,
                "distance": dist,
                "metadata": meta
            })
        if len(filtered) >= 3:
            break
    
    return filtered
```

### SiliconFlow Chat Completions (Python)
```python
# Source: [ASSUMED based on SiliconFlow OpenAI-compatible API]
import requests

SILICONFLOW_API = "https://api.siliconflow.cn/v1/chat/completions"

def generate_answer(question, context, api_key):
    """Generate answer using SiliconFlow DeepSeek model."""
    system_prompt = f"""你是一个基于知识库的问答助手。

参考信息：
{context}

要求：
1. 仅基于上述参考信息回答问题
2. 每个引用使用【N】格式
3. 如果参考信息不足以回答，请明确说明"""

    payload = {
        "model": "deepseek-ai/DeepSeek-V3",
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": question}
        ],
        "max_tokens": 2048,
        "temperature": 0.3  # Low temperature for factual answers
    }
    
    response = requests.post(
        SILICONFLOW_API,
        headers={"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"},
        json=payload
    )
    response.raise_for_status()
    return response.json()["choices"][0]["message"]["content"]
```

### ChatController (Java)
```java
// Source: [ASSUMED based on DocumentController.java pattern]
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;
    
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }
    
    @PostMapping("/ask")
    public ResponseEntity<?> ask(
            @RequestBody ChatRequest request,
            Authentication auth) {
        Long userId = extractUserId(auth);
        try {
            ChatResponse response = chatService.askQuestion(request, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to generate answer. Please try again later."));
        }
    }
}
```

### Chat Pinia Store (Vue 3)
```javascript
// Source: [ASSUMED based on auth.js store pattern]
import { defineStore } from 'pinia'

export const useChatStore = defineStore('chat', {
  state: () => ({
    sessions: [],           // List of chat sessions
    currentSessionId: null, // Active session
    messages: {},           // messages by sessionId
    loading: false
  }),
  
  actions: {
    async askQuestion(question, kbId) {
      this.loading = true
      try {
        const response = await api.post('/chat/ask', {
          question,
          kbId,
          sessionId: this.currentSessionId
        })
        // Add assistant message
        this.addMessage(response.data.sessionId, 'assistant', response.data.answer)
        return response.data
      } finally {
        this.loading = false
      }
    },
    
    addMessage(sessionId, role, content) {
      if (!this.messages[sessionId]) {
        this.$patch((state) => {
          state.messages[sessionId] = []
        })
      }
      this.messages[sessionId].push({ role, content, createdAt: Date.now() })
    }
  }
})
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| LocalStorage chat history | MySQL persistence | Phase 3 | Cross-device, persistent history |
| Single-turn Q&A | Multi-turn with sessionId | Phase 3 | Conversation context preserved |
| Keyword match fallback | Pure RAG (no fallback) | Phase 3 MVP | Simpler, per D-19 |

**Deprecated/outdated:**
- Streaming SSE: Deferred to post-Phase 3 MVP

## Assumptions Log

> List all claims tagged `[ASSUMED]` in this research.

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | ChromaDB 0.4.x `collection.query()` accepts `where` filter and returns `distances` in `[0,2]` cosine range | Common Pitfalls, Code Examples | Chroma version mismatch could break query syntax |
| A2 | SiliconFlow API uses OpenAI-compatible `/v1/chat/completions` endpoint with DeepSeek-V3 model | Code Examples | API endpoint or model name may differ |
| A3 | `SIMILARITY_THRESHOLD = 0.7` converts to `MAX_DISTANCE = 0.6` using formula `1 - (2 * threshold)` | Code Examples | If ChromaDB uses different distance metric, threshold mapping is wrong |
| A4 | ChatController and ChatService follow same patterns as DocumentController/Service | Architecture | Implementation may need adaptation for new entities |
| A5 | Vue 3 chat component uses same Element Plus patterns as existing Vue components | Code Examples | UI implementation may differ |

## Open Questions

1. **Should the LLM use chain-of-thought reasoning?**
   - What we know: DeepSeek-R1 supports thinking mode, DeepSeek-V3 is faster
   - What's unclear: Whether thinking mode should be enabled for MVP
   - Recommendation: Use DeepSeek-V3 without thinking for MVP (faster, sufficient for factual QA)

2. **What happens when no chunks meet the similarity threshold?**
   - What we know: D-16 says "below 0.7 does not return"
   - What's unclear: Does the system return empty sources, or return an error, or skip citation?
   - Recommendation: Return answer with no citations, frontend shows "No relevant documents found"

3. **Should the frontend auto-scroll to latest message?**
   - What we know: Standard chat UX pattern
   - What's unclear: Not explicitly specified
   - Recommendation: Auto-scroll enabled, with "jump to bottom" button for long conversations

## Environment Availability

> Step 2.6: SKIPPED (no external dependencies beyond project stack)

Phase 3 uses existing infrastructure:
- Java Spring Boot 2.7.18 (already in backend)
- Python 3.x with chromadb + requests (already used in Phase 2 scripts)
- MySQL 8.x (already configured)
- ChromaDB (already installed and used in Phase 2)

No new environment setup required.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | Jest (frontend), JUnit (backend) |
| Config file | `vue-counter/jest.config.js` (if exists), `backend/pom.xml` |
| Quick run command | `npm test` (frontend), `mvn test` (backend) |
| Full suite command | `npm run test:unit && npm run test:integration` (frontend) |

### Phase Requirements to Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| RAG-03 | Chroma retrieval returns top-3 chunks above 0.7 similarity | unit | `pytest tests/test_rag_retrieval.py` | N/A (Python) |
| RAG-03 | LLM returns answer with citations in【N】format | integration | `pytest tests/test_chat_and_answer.py` | N/A (Python) |
| RAG-03 | ChatController returns JSON with answer + sources | unit | `mvn test -Dtest=ChatServiceTest` | N/A |
| CHAT-01 | Messages persist to MySQL with correct sessionId | integration | `mvn test -Dtest=ChatMessageMapperTest` | N/A |
| CHAT-01 | Frontend displays chat history from API | e2e | `npm run test:e2e` | N/A |

### Sampling Rate
- **Per task commit:** `npm test -- --passWithNoTests && mvn test -DskipITs`
- **Per wave merge:** Full suite
- **Phase gate:** All tests green before `/gsd-verify-work`

### Wave 0 Gaps
- [ ] `backend/src/test/java/com/aikb/service/ChatServiceTest.java` — tests ProcessBuilder invocation
- [ ] `backend/src/test/java/com/aikb/mapper/ChatSessionMapperTest.java` — tests chat persistence
- [ ] `frontend/src/__tests__/stores/chat.test.js` — tests Pinia store actions
- [ ] `backend/scripts/tests/test_chat_and_answer.py` — tests RAG retrieval + LLM pipeline
- [ ] Framework install: Ensure Jest and pytest are available in project

*(If no gaps: "None — existing test infrastructure covers all phase requirements")*

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | yes | JWT token validated by Spring Security filter (Phase 1) |
| V3 Session Management | yes | sessionId validated against user ownership in ChatService |
| V4 Access Control | yes | User can only query their own kbId; Chroma filter by userId |
| V5 Input Validation | yes | `@Valid` on ChatRequest, sanitization in Python script |
| V6 Cryptography | no | No cryptography needed beyond Phase 1 JWT |

### Known Threat Patterns for RAG Chat Stack

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Prompt injection via user question | Information Disclosure | LLM system prompt: "仅基于参考信息回答" |
| Cross-user chat history access | Information Disclosure | SQL WHERE userId = ? on all queries |
| Chroma collection enumeration | Information Disclosure | Collection name derived from userId, not user-controlled |
| LLM generating harmful content | Repudiation | Temperature 0.3, content filtering on API provider side |

## Sources

### Primary (HIGH confidence)
- ChromaDB Python client API — [ASSUMED] based on 0.4.x documentation, not verified via Context7
- SiliconFlow API — [ASSUMED] based on OpenAI-compatible format from web search

### Secondary (MEDIUM confidence)
- RAG prompt engineering best practices — [WebSearch] https://www.aussieai.com/book/rag-book-prompt-optimization
- ChromaDB distance filtering patterns — [WebSearch] https://pythonhowtoprogram.com/how-to-use-python-chromadb-as-a-vector-database/

### Tertiary (LOW confidence)
- Vue 3 chat UI patterns — [ASSUMED] based on project conventions, not verified

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - all technologies already in use from Phase 1/2
- Architecture: HIGH - ProcessBuilder pattern established, simple extension
- Pitfalls: MEDIUM - ChromaDB distance metric details not verified with Context7

**Research date:** 2026-10-01
**Valid until:** 2026-10-31 (30 days — stable technology stack)
