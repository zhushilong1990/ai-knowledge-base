# Phase 3: Chat/QA — Context

**Gathered:** 2026-10-01
**Status:** Ready for planning

<domain>
## Phase Boundary

用户提问 → Chroma 检索相关文档块 → LLM 生成答案 → 返回带来源引用的回复。Phase 3 交付：聊天界面、Chroma 检索、LLM 增强生成、会话历史。
</domain>

<decisions>
## Implementation Decisions

### 对话历史存储
- **D-15：** 存储在**后端数据库**（MySQL），跨会话持久化，支持多端访问

### RAG 检索策略
- **D-16：** **Top-3 + 相似度阈值 0.7** — 检索最相关的 3 个块，低于 0.7 不返回

### LLM 回复方式
- **D-17：** 采用**一次性返回**（非流式）— MVP 最快交付，实现简单

### 来源引用格式
- **D-18：** **编号列表【1】【2】【3】** — 简洁，与 LLM 输出格式兼容好

### 错误处理策略
- **D-19：** 返回**错误提示 + 建议稍后重试** — 不做 fallback，用户明确知道失败原因

### Claude's Discretion
无 — 所有决策均由用户明确选择
</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

无外部依赖文档 — 所有需求和约束均已在上述 decisions 中记录。

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `backend/src/main/java/com/aikb/controller/DocumentController.java` — REST Controller 模式
- `backend/src/main/java/com/aikb/service/DocumentService.java` — Service 层组织方式
- `backend/scripts/extract_and_embed.py` — Chroma 检索调用模式
- `vue-counter/src/stores/auth.js` — Pinia store 模式
- `vue-counter/src/api/http.js` — Axios 拦截器模式

### Integration Points
- Chroma collection: `user_{userId}_kb_{kbId}` per D-12
- SiliconFlow API: `/v1/embeddings` and `/v1/chat/completions`
- 后端 ChatService → Python script → Chroma query → SiliconFlow LLM

### Established Patterns
- Pinia + localStorage 持久化模式
- Axios 请求拦截器注入 `Authorization: Bearer` 头
- ProcessBuilder 调用 Python 脚本（Phase 2 模式）

</code_context>

<specifics>
## Specific Ideas

- 对话表设计：id, userId, sessionId, question, answer, sources(JSON), createdAt
- 检索调用：Chroma `query()` with `n_results=3`, `where` filter by userId/kbId
- LLM 调用：SiliconFlow `/v1/chat/completions` with DeepSeek model
- 引用注入：LLM 输出时附带来源编号，前端解析渲染

</specifics>

<deferred>
## Deferred Ideas

- 流式 SSE 输出（Phase 3 MVP 后加入）
- 关键词匹配 fallback（Phase 3 MVP 后加入）
- Inline 引用格式（Phase 3 MVP 后评估）

---

*Phase: 3-Chat-QA*
*Context gathered: 2026-10-01*
