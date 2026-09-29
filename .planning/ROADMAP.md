# Roadmap: AI Knowledge Base

## Overview

Build an AI-powered RAG knowledge base application where users upload documents, create knowledge bases, and ask questions answered by retrieving relevant chunks augmented with LLM generation. The system uses a 3-tier architecture: Vue 3 + uni-app frontend, Java Spring Boot BFF, and Python FastAPI AI service with Chroma vector database.

## Phases

- [ ] **Phase 1: Foundation & Security** - JWT auth, CORS, API structure
- [ ] **Phase 2: Document Ingestion** - Upload, parsing, chunking, embedding, Chroma indexing
- [ ] **Phase 3: Chat/QA** - RAG retrieval, context injection, LLM response
- [ ] **Phase 4: Knowledge Base Management** - KB CRUD, document-KB association
- [ ] **Phase 5: History & Feedback** - Conversation history, thumbs up/down
- [ ] **Phase 6: Maintenance & Polish** - Deployment, monitoring, multi-endpoint

## Phase Details

### Phase 1: Foundation & Security
**Goal**: Users can securely authenticate and access the system
**Depends on**: Nothing (first phase)
**Requirements**: AUTH-01, AUTH-02, AUTH-03
**Mode**: mvp
**Success Criteria** (what must be TRUE):
  1. User can register with email/password and receive confirmation
  2. User can log in and receive JWT token that persists across sessions
  3. User can access protected endpoints using JWT token
  4. User can log out and token is invalidated
  5. System rejects requests with expired or invalid tokens
**Plans:** 3 plans
- [ ] 01-01-PLAN.md — Core Auth tracer: Spring Boot project + User entity + Register + Login + JWT filter
- [ ] 01-02-PLAN.md — Refresh token: backend refresh endpoint + frontend Axios interceptor with auto-refresh
- [ ] 01-03-PLAN.md — Logout + error handling: client-side clear + 401 redirect + global error toasts

### Phase 2: Document Ingestion
**Goal**: Users can upload documents that are parsed, chunked, embedded, and stored in Chroma
**Depends on**: Phase 1
**Requirements**: RAG-01, RAG-02
**Mode**: mvp
**Success Criteria** (what must be TRUE):
  1. User can upload PDF, Word, or TXT files via API endpoint
  2. System extracts text content from uploaded documents
  3. System chunks text into optimal segments (400-600 tokens with overlap)
  4. System generates embeddings and stores in Chroma vector database
  5. Upload process handles errors gracefully with user feedback
**Plans**: TBD

### Phase 3: Chat/QA
**Goal**: Users can ask questions and receive answers based on retrieved knowledge base content
**Depends on**: Phase 2
**Requirements**: RAG-03, CHAT-01
**Mode**: mvp
**Success Criteria** (what must be TRUE):
  1. User can send a question via chat interface
  2. System retrieves relevant document chunks from Chroma based on question
  3. System injects retrieved context into LLM prompt
  4. User receives generated answer with citations to source documents
  5. User can ask follow-up questions in same conversation thread
**Plans**: TBD

### Phase 4: Knowledge Base Management
**Goal**: Users can organize documents into knowledge bases and manage them
**Depends on**: Phase 2
**Requirements**: KB-01, KB-02
**Mode**: mvp
**Success Criteria** (what must be TRUE):
  1. User can view list of their knowledge bases with document counts
  2. User can create new knowledge base with name and description
  3. User can view documents within a specific knowledge base
  4. User can delete documents from a knowledge base
  5. Deleting a document removes its vectors from Chroma
**Plans**: TBD

### Phase 5: History & Feedback
**Goal**: Users can review past conversations and provide feedback
**Depends on**: Phase 3
**Requirements**: CHAT-02
**Mode**: mvp
**Success Criteria** (what must be TRUE):
  1. User can view list of past conversation sessions
  2. User can click into a session and see full conversation history
  3. User can give thumbs up or thumbs down to a response
  4. User feedback is stored and associated with the response
  5. Conversations persist across browser sessions
**Plans**: TBD

### Phase 6: Maintenance & Polish
**Goal**: System is production-ready with monitoring and multi-endpoint deployment
**Depends on**: Phase 5
**Requirements**: None additional
**Mode**: mvp
**Success Criteria** (what must be TRUE):
  1. Application deploys successfully to hosting platform (H5 accessible via public URL)
  2. System handles network errors gracefully with user-friendly messages
  3. API endpoints have basic health check and monitoring
  4. Multi-endpoint variants (H5/miniprogram/App) can be built from same codebase
  5. Re-indexing pipeline can refresh stale document vectors
**Plans**: TBD

## Progress

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Foundation & Security | 0/3 | Not started | - |
| 2. Document Ingestion | 0/? | Not started | - |
| 3. Chat/QA | 0/? | Not started | - |
| 4. Knowledge Base Management | 0/? | Not started | - |
| 5. History & Feedback | 0/? | Not started | - |
| 6. Maintenance & Polish | 0/? | Not started | - |
