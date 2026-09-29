# Pitfalls Research

**Domain:** RAG Knowledge Base Applications
**Researched:** 2026-09-29
**Confidence:** MEDIUM-HIGH

## Critical Pitfalls

### Pitfall 1: Chunking Strategy Mismatch

**What goes wrong:**
RAG systems retrieve irrelevant or incomplete context, producing hallucinations or incomplete answers. Approximately 80% of RAG production failures trace back to chunking decisions, not embedding quality or retrieval algorithms.

**Why it happens:**
- Uniform fixed-size chunking ignores document structure (headers, tables, code blocks)
- Chunk sizes too small lose cross-sentence context
- Chunk sizes too large introduce noise from irrelevant content
- No overlap causes boundary sentences to be orphaned

**How to avoid:**
- Use 400-600 token chunks with 10-20% overlap for enterprise documents
- Apply semantic chunking for mixed-format documents (tables, narrative, code)
- Isolate tables as separate chunks; split code on AST boundaries
- Test with 10-20 real queries to measure accuracy before production

**Warning signs:**
- Answers reference wrong sections
- Key context missing from responses
- Answers accurate but incomplete
- High variance in response quality across queries

**Phase to address:**
Phase 2 (Data Processing Pipeline)

---

### Pitfall 2: Document Parsing Failures

**What goes wrong:**
PDF and Word documents lose critical structure during parsing. Headers become plain text, tables become unreadable garbage, embedded images with key figures are discarded entirely.

**Why it happens:**
- Basic text extraction (pdftotext) ignores layout and reading order
- Multi-column documents get lines interleaved
- Tables parsed as space-separated gibberish
- Complex formatting (headers in margins, footnotes) dropped

**How to avoid:**
- Use layout-aware parsers (LayoutPDFReader, PyMuPDF, docling)
- Configure table extraction explicitly; verify output
- Preserve metadata (page numbers, section headers) for traceback
- Test parsing on representative documents before scaling

**Warning signs:**
- "参差不齐" output from Chinese documents (wrong reading order)
- Tables render as jumbled characters
- Page boundaries unclear in extracted text
- Footnotes and captions missing

**Phase to address:**
Phase 2 (Document Ingestion)

---

### Pitfall 3: Embedding Model Context Window Overflow

**What goes wrong:**
Documents longer than the embedding model's maximum input length are silently truncated. Relevant content at the end of long documents is never indexed.

**Why it happens:**
- Using models with 512-token ceilings (BGE-base, Cohere v3) without awareness
- Long documents exceed limits during ingestion
- No warning or error when truncation occurs
- Semantic meaning lost at document boundaries

**How to avoid:**
- Verify embedding model context limits before selection
- For long documents, use hierarchical chunking (document -> section -> paragraph)
- Track chunk metadata (source page, document ID) for traceback
- Consider models with larger contexts (1024-4096 tokens) for longer documents

**Warning signs:**
- Answers only reference beginning of documents
- "Long document" queries always fail
- No chunks from later sections of long PDFs
- Embedding API returns warnings about truncated input

**Phase to address:**
Phase 2 (Embedding Configuration)

---

### Pitfall 4: Vector DB Accuracy Degradation at Scale

**What goes wrong:**
Semantic similarity search returns plausible but factually incorrect results. "Error 221" query returns "Error 222" because they cluster near each other in embedding space.

**Why it happens:**
- Vector precision degrades 12% at 100,000 pages (EyeLevel.ai research, 2024)
- Pure semantic search cannot distinguish between similar but distinct entities
- No exact-match fallback for numerical codes, product IDs, proper nouns

**How to avoid:**
- Implement hybrid search (keyword + vector) as default
- Add metadata filters for exact-match fields (IDs, dates, categories)
- Use reranking to boost exact matches above semantic clusters
- Monitor retrieval precision metrics; alert on degradation

**Warning signs:**
- Querying for specific codes returns wrong codes
- "X" vs "X Plus" or "X Pro" confusion
- Dates or numbers consistently wrong in answers
- Retrieval latency spikes at scale

**Phase to address:**
Phase 3 (Retrieval Pipeline)

---

### Pitfall 5: LLM Context Window Saturation

**What goes wrong:**
Retrieving too many chunks overwhelms the LLM context. Critical information gets lost in noise, and response quality degrades as context grows.

**Why it happens:**
- Naive Top-K retrieval without relevance thresholding
- Increasing chunk count "for better coverage" backfires
- No context compression or reranking strategy
- Different LLMs have different optimal context loads

**How to avoid:**
- Limit retrieved chunks to 4-8 for most LLMs; test empirically
- Implement reranking to get fewer but better chunks
- Use context compression on retrieved chunks before injection
- Route to different model sizes based on query complexity

**Warning signs:**
- Response quality degrades with more context
- Late-mentioned facts absent from answers
- Hallucinations increase with long documents
- Token counts in prompts are excessive (>16K for GPT-4)

**Phase to address:**
Phase 3 (Generation Pipeline)

---

### Pitfall 6: Index Staleness

**What goes wrong:**
Users receive outdated answers because document updates are not reflected in the vector index. Knowledge base drift accumulates silently.

**Why it happens:**
- No re-indexing pipeline for document updates
- Incremental updates to source files not propagated to embeddings
- No version tracking between documents and index
- Deletions not reflected in index

**How to avoid:**
- Implement document versioning with index timestamps
- Trigger re-embedding on document update detection
- Set up scheduled full re-index cycles (daily/weekly based on update frequency)
- Log document-to-chunk lineage for targeted reindexing

**Warning signs:**
- Users report "old information" without specifics
- Querying recent documents returns no results
- Index size doesn't match document count
- No way to determine which chunks are stale

**Phase to address:**
Phase 4 (Maintenance & Monitoring)

---

### Pitfall 7: Java-Python Service Integration Latency

**What goes wrong:**
HTTP calls from Java backend to Python RAG services timeout or add 50-100ms+ latency per request. Long RAG inference requests (embedding, generation) block Java threads.

**Why it happens:**
- Synchronous HTTP blocking on Python ML services
- Default timeout values (30s) insufficient for ML inference
- No connection pooling between Java and Python services
- Python GIL limits concurrent request handling

**How to avoid:**
- Configure read timeout to 600000ms (10 minutes) for ML inference calls
- Use connection pooling (Apache HttpClient, OkHttp)
- Implement async/queue-based processing with callbacks
- Consider gRPC for high-throughput Python-Java communication (0.8-12ms vs 2-50ms latency)

**Warning signs:**
- Thread pool exhaustion under load
- Timeout exceptions on RAG calls
- High p95 latency on retrieval endpoints
- Java service OOM despite low actual load

**Phase to address:**
Phase 3 (Backend Integration)

---

### Pitfall 8: API Key Exposure in RAG Pipelines

**What goes wrong:**
Embedding service API keys, LLM API keys, or vector DB credentials logged in plaintext, committed to git, or passed in environment variables that leak.

**Why it happens:**
- Keys logged in request headers or response bodies
- Keys stored in code or config files committed to repository
- Environment variables printed in error messages
- No key rotation when developers leave

**How to avoid:**
- Use secret management services (AWS Secrets Manager, HashiCorp Vault)
- Never log request headers or full URLs with API keys
- Implement key rotation automation
- Use IAM roles/service accounts instead of static keys where possible
- Add pre-commit hooks to detect key-like patterns

**Warning signs:**
- API key in git history
- Keys visible in logs or error traces
- No key rotation in past 90 days
- Multiple services sharing same API key

**Phase to address:**
Phase 1 (Security Architecture)

---

### Pitfall 9: CORS Misconfiguration in RAG APIs

**What goes wrong:**
Browser-based RAG clients cannot access the API due to missing or overly restrictive CORS headers. API works in Postman but fails in web apps.

**Why it happens:**
- CORS not configured for FastAPI/Flask Python services
- Allowed origins set to wildcard in production
- CORS preflight requests not handled
- Different CORS policy needed for different client types (web, mobile, server)

**How to avoid:**
- Explicitly configure allowed origins per environment
- Use environment variables for origin allowlist
- Handle preflight OPTIONS requests in middleware
- Test with actual browser DevTools, not just Postman

**Warning signs:**
- CORS errors in browser console
- Works on localhost but fails on deployed domain
- Mobile app works but web fails (or vice versa)
- OPTIONS requests returning 404

**Phase to address:**
Phase 4 (Deployment)

---

### Pitfall 10: Poor Retrieval Quality Without Feedback Loops

**What goes wrong:**
RAG system silently fails in production. Users get wrong answers, no one notices because there is no logging or evaluation of answer quality.

**Why it happens:**
- No logging of queries and retrieved documents
- No user feedback mechanism (thumbs up/down, corrections)
- No automated quality metrics
- No human review pipeline for edge cases

**How to avoid:**
- Log every query with retrieved chunks and final answer
- Implement user feedback collection at point of answer
- Run periodic manual evaluation on sampled queries
- Track "unanswerable" rate and "hallucination" rate
- Set up alerts for sudden drops in user satisfaction

**Warning signs:**
- No visibility into what users are asking
- User complaints about wrong answers but no system to track
- Answer quality cannot be audited retroactively
- No way to measure improvement over time

**Phase to address:**
Phase 4 (Monitoring & Iteration)

---

## Technical Debt Patterns

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| Fixed 512-token chunks for everything | Simplicity | Misses document structure, loses context | Never in production |
| Single embedding model for all languages | One model to manage | Non-English quality suffers | MVP only; multilingual requires multilingual model |
| No chunk overlap | Half the storage/indexing | Boundary context lost | Never |
| Top-10 retrieval without reranking | Simple pipeline | Lower precision | MVP; production needs reranking |
| Sync HTTP to Python services | Easier debugging | Latency, thread blocking | Development only |
| Store API keys in .env | Works immediately | Security breach risk | Never in production |

---

## Integration Gotchas

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| OpenAI Embeddings | Not handling rate limits | Implement exponential backoff with jitter |
| Pinecone/Qdrant | Not setting namespace for multi-tenant | Use namespace per user/customer |
| Python ML Service | Blocking async Java thread | Use reactive client with non-blocking I/O |
| Document Parser | Ignoring parse confidence scores | Log and alert on low-confidence parses |
| LLM Generation | Not truncating context to model limit | Pre-compress or truncate before injection |

---

## Performance Traps

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|----------------|
| Pure vector search | Accuracy drops at 10K+ docs | Hybrid search with keyword fallback | >10,000 pages |
| No connection pooling | High latency under load | Pool size = expected concurrency | >50 concurrent users |
| Synchronous RAG calls | Request timeout | Async queue with callbacks | >5 second SLA |
| No caching | High API costs, slow repeated queries | Semantic cache for similar queries | High query overlap |
| Unbounded result set | Memory pressure, slow responses | Hard limit on Top-K | Large corpus, high QPS |

---

## Security Mistakes

| Mistake | Risk | Prevention |
|---------|------|------------|
| Logging full prompts with user data | GDPR/PIPL violation | Redact PII from logs; log only query hashes |
| No input validation on user queries | Prompt injection | Sanitize and validate all user input |
| Storing embeddings with PII | Data breach exposure | Embed only anonymized content |
| No access controls on RAG endpoint | Unauthorized data access | Implement authentication and authorization |
| Third-party API keys in code | Credential leakage | Use secret management; rotate keys |
| No audit trail for RAG queries | Compliance failure | Log who queried what and when |

---

## UX Pitfalls

| Pitfall | User Impact | Better Approach |
|---------|-------------|-----------------|
| "I don't know" not communicated | User thinks wrong answer is correct | Explicitly state when answer not in knowledge base |
| No source citations | User cannot verify | Include document/page references in answers |
| Generic error messages | User frustrated, no recourse | Specific errors: "Document X could not be parsed" |
| No query clarification | Wrong question answered | Ask follow-up when query is ambiguous |
| Slow responses | User abandons | Show progress indicator; async for >3s responses |

---

## "Looks Done But Isn't" Checklist

- [ ] **Chunking:** Uniform chunking configured without testing on real documents
- [ ] **Parsing:** PDF extraction works on sample but fails on complex layouts
- [ ] **Embedding:** Model context limit not verified against longest document
- [ ] **Retrieval:** No hybrid search; pure vector returns wrong codes/IDs
- [ ] **Generation:** Top-K retrieval with no limit; context overflows LLM
- [ ] **Indexing:** No re-indexing pipeline; index goes stale immediately
- [ ] **Security:** API keys in environment variables not secret manager
- [ ] **Monitoring:** Query logging exists but no answer quality evaluation
- [ ] **CORS:** Tested with Postman only, not browser clients
- [ ] **Timeouts:** Python service timeouts not configured for ML inference

---

## Recovery Strategies

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|----------------|
| Stale index | MEDIUM | Run full re-index; implement incremental update pipeline |
| Chunking rework | HIGH | Re-chunk all documents; re-embed; update index |
| API key exposure | CRITICAL | Rotate key immediately; audit access logs; revoke all copies |
| Context overflow hallucinations | MEDIUM | Reduce Top-K; add reranking; implement context compression |
| Parsing failures on production docs | MEDIUM | Add new parser; re-parse failed documents; quarantine bad chunks |
| Hybrid search not implemented | HIGH | Add keyword search index; re-architect retrieval layer |

---

## Pitfall-to-Phase Mapping

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| Document parsing failures | Phase 2 (Ingestion) | Parse 10 diverse docs; verify structure preserved |
| Chunking strategy | Phase 2 (Chunking) | Test 20 queries; measure precision/recall |
| Embedding context overflow | Phase 2 (Embedding) | Verify no truncation warnings; check chunk metadata |
| Vector DB accuracy at scale | Phase 3 (Retrieval) | Load 10K+ docs; verify specific entity retrieval |
| LLM context saturation | Phase 3 (Generation) | Test with 8+ chunks; verify quality doesn't degrade |
| Index staleness | Phase 4 (Maintenance) | Update doc; verify update reflected in <1 hour |
| Java-Python latency | Phase 3 (Integration) | Load test; verify p95 <500ms |
| API key exposure | Phase 1 (Security) | Scan repo; verify no keys in history |
| CORS misconfiguration | Phase 4 (Deployment) | Test from browser; verify actual client works |
| No feedback loops | Phase 4 (Monitoring) | Verify query logs exist and are queryable |

---

## Sources

- [Chunking Strategies for RAG (Unstructured.io)](https://unstructured.io/blog/chunking-for-rag-best-practices)
- [RAG Explosion 2024 (Quantum Encoding)](https://quantumencoding.io/blog/rag-explosion-2024-what-actually-matters)
- [Vector Databases 2025: The Hype Is Dead (BuzzTraf)](https://www.buzztraf.com/tech/vector-databases-2025-hype-over-graphrag-reality-6249186)
- [Do Vector Databases Lose Accuracy at Scale? (EyeLevel.ai)](http://www.eyelevel.ai/post/do-vector-databases-lose-accuracy-at-scale)
- [RAG in Production: Caching, Costs, and Scaling (Shift Quality)](https://www.shiftquality.com/post/rag-in-production-caching-costs-and-scaling-retrieval)
- [RAG System 7 Common Challenges (Baidu Cloud)](https://cloud.baidu.com/article/3373361)
- [6 Common RAG Failure Points (Hubwiz)](https://www.hubwiz.com/blog/top6-rag-failure-points)
- [Seven Ways RAG Could Fail (Label Studio)](https://labelstudio.com.cn/blog/seven-ways-your-rag-system-could-be-failing-and-how-to-fix-them)
- [Python Java Interop Guide (LearnModernPython)](https://learnmodernpython.com/python-and-java-seamless-integration-via-rest-apis/)
- [Building Robust RAG (Solita)](https://www.solita.fi/blogs/building-robust-language-models-with-rag-one-pitfall-at-a-time)

---
*Pitfalls research for: RAG Knowledge Base Applications*
*Researched: 2026-09-29*
