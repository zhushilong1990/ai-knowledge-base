---
phase: 03-chat-qa
plan: '01'
subsystem: rag
tags: [python, chromadb, siliconflow, deepseek, rag, chat]

# Dependency graph
requires:
  - phase: 02-document-ingestion
    provides: ChromaDB collection pattern, extract_and_embed.py ProcessBuilder pattern, SILICONFLOW_API_KEY env var
provides:
  - backend/scripts/chat_and_answer.py - RAG retrieval + LLM generation script
affects: [03-chat-qa (Tasks 2-3)]

# Actuals (#2632)
actuals:
  tokens: 4800
  tasks: 1
  commits: 0

# Tech tracking
tech-stack:
  added: [chromadb, requests]
  patterns: [RAG retrieval with post-filtering, ProcessBuilder JSON stdin/stdout, SiliconFlow chat completions]

key-files:
  created:
    - backend/scripts/chat_and_answer.py - RAG chat script with ChromaDB query and LLM generation
  modified: []

key-decisions:
  - "Top-3 chunks after similarity filter (>= 0.7) per D-16"
  - "SiliconFlow /v1/chat/completions with DeepSeek-V3, temperature=0.3"
  - "Context string format: 【1】chunk1\n\n【2】chunk2\n\n【3】chunk3"
  - "Error handling: JSON error + exit non-zero per D-19"

patterns-established:
  - "ProcessBuilder-to-Python: JSON stdin/stdout pattern (reused from extract_and_embed.py)"
  - "RAG retrieval: ChromaDB query with where filter + post-filter by distance threshold"
  - "Citation format: 【N】numbered markers in LLM system prompt"

requirements-completed: [RAG-03]

coverage:
  - id: D1
    description: "chat_and_answer.py accepts JSON stdin with question/kbId/userId/chromaPath, queries ChromaDB with similarity filter, calls SiliconFlow LLM, returns answer with citations"
    requirement: RAG-03
    verification:
      - kind: integration
        ref: "python backend/scripts/chat_and_answer.py (manual test with echo)"
        status: unknown
    human_judgment: true
    rationale: "Requires ChromaDB with data and SiliconFlow API key - not available in automation environment"

# Metrics
duration: 5min
completed: 2026-10-01
status: complete
---

# Phase 3 Plan 1 Summary

**RAG chat script: ChromaDB retrieval with top-3 similarity filtering and SiliconFlow DeepSeek-V3 LLM generation**

## Performance

- **Duration:** 5 min
- **Started:** 2026-10-01T04:39:00Z
- **Completed:** 2026-10-01T04:44:00Z
- **Tasks:** 1
- **Files modified:** 1

## Accomplishments

- Created `backend/scripts/chat_and_answer.py` following ProcessBuilder JSON stdin/stdout pattern
- Implements RAG retrieval: ChromaDB query with `where={"kbId": kbId}` filter
- Post-filtering: discards chunks with cosine distance > 0.6 (similarity < 0.7)
- Keeps top-3 chunks after filtering
- Calls SiliconFlow `/v1/chat/completions` with DeepSeek-V3 model, temperature=0.3
- System prompt instructs LLM to cite sources using【N】format
- Returns JSON: `{ "answer": str, "sources": [{"id": str, "text": str, "score": float}] }`
- Error handling: outputs JSON error and exits non-zero per D-19

## Files Created/Modified

- `backend/scripts/chat_and_answer.py` - RAG retrieval + LLM generation script

## Decisions Made

- Reused ProcessBuilder JSON stdin/stdout pattern from `extract_and_embed.py`
- ChromaDB distance-to-similarity formula: `similarity = 1 - (distance / 2)`
- MAX_DISTANCE = 0.6 corresponds to similarity threshold 0.7
- Empty result handling: returns friendly message instead of error
- Sources truncated to 200 chars in response (full text in ChromaDB)

## Deviations from Plan

None - plan executed exactly as written.

## Next Phase Readiness

- Task 1 (Wave 1) complete - chat_and_answer.py ready
- Wave 2 (Task 2: Backend MySQL schema + entities + service + controller) can proceed
- Wave 3 (Task 3: Frontend chat page + API + store + router) depends on Wave 2

---
*Phase: 03-chat-qa*
*Plan: 01*
*Completed: 2026-10-01*
