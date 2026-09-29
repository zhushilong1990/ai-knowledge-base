# Project Research Summary

**Project:** RAG Knowledge Base Q&A Application
**Domain:** AI-powered knowledge base with RAG (Retrieval-Augmented Generation) Q&A
**Researched:** 2026-09-29
**Confidence:** MEDIUM-HIGH

---

## Executive Summary

This is an AI-powered RAG knowledge base application enabling users to upload documents, create knowledge bases, and ask questions answered by retrieving relevant chunks augmented with LLM generation.

Key architecture: **3-tier BFF pattern** — Vue frontend → Java Spring Boot BFF → Python FastAPI AI service + ChromaDB + Ollama/DeepSeek

---

## Key Findings

### Stack
- **AI Service**: Python 3.10+ / FastAPI + LangChain 0.1.x + ChromaDB 0.4.x
- **Backend**: Spring Boot 3.2.x + MySQL + JWT
- **Frontend**: Vue 3 + uni-app + Pinia + Element Plus/uView Plus
- **LLM**: SiliconFlow API + DeepSeek model
- **Vector DB**: Chroma (local, free)

### Table Stakes (Must-have)
- Document upload / chunking / indexing
- Knowledge base CRUD
- RAG chat with citations
- JWT authentication
- Conversation history

### Differentiators (What makes it better)
- Hybrid search (10-15% accuracy gain)
- Streaming SSE responses
- Adjustable top-K retrieval
- User feedback (thumbs up/down)

### Top Pitfalls
1. **Chunking mismatch** (80% of RAG failures) — use 400-600 tokens with 10-20% overlap
2. **Document parsing failures** — layout-aware parsers, Chinese document handling
3. **Embedding context overflow** — verify model limits, hierarchical chunking
4. **Java-Python latency** — 10min timeout, connection pooling
5. **Index staleness** — versioning + re-indexing pipeline

---

## Implications for Roadmap

Suggested 6-phase structure:
1. **Foundation & Security** — JWT auth, CORS, API keys
2. **Document Ingestion** — upload, parsing, chunking, embedding, Chroma indexing
3. **Chat/QA** — RAG retrieval, context injection, LLM response, citations
4. **Knowledge Base Management** — KB CRUD, document-KB association
5. **History & Feedback** — conversation history, thumbs up/down
6. **Maintenance & Polish** — re-indexing pipeline, deployment, monitoring

---

## Research Flags

**Needs research during planning:**
- Phase 2: chunking validation with real docs
- Phase 3: hybrid search, top-K tuning

**Standard patterns (skip research):**
- Phase 1: JWT/Spring Boot
- Phase 4: CRUD/RBAC
- Phase 5: history

---

## Gaps

- Chinese PDF parsing not validated — test early in Phase 2
- DeepSeek model selection for Chinese content unvalidated
- Scaling estimates (100-1000 users) are directional only

---

## Sources

- `.planning/research/STACK.md` (219 lines)
- `.planning/research/FEATURES.md` (245 lines)
- `.planning/research/ARCHITECTURE.md` (380 lines)
- `.planning/research/PITFALLS.md` (438 lines)

---

*Research synthesized: 2026-09-29*
