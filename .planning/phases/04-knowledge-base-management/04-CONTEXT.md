# Phase 4: Knowledge Base Management — Context

**Gathered:** 2026-10-01
**Status:** Ready for planning

<domain>
## Phase Boundary

用户可以将文档组织到知识库（Knowledge Base）中管理。Phase 4 交付：知识库 CRUD、文档与知识库的关联管理、删除文档时同步清理 Chroma 向量。
</domain>

<decisions>
## Implementation Decisions

### 知识库存储
- **D-20：** 知识库信息存储在 **MySQL** `knowledge_base` 表中，与文档关联

### 知识库-文档关系
- **D-21：** 一个知识库可包含多个文档（1:N）
- **D-22：** 删除知识库时，不自动删除其中的文档（仅解除关联）

### Chroma 向量删除
- **D-23：** 删除文档时同步从 Chroma 删除其向量 — 通过 Python 脚本调用 Chroma delete API
</decisions>

<code_context>
## Existing Code Insights

### Database Schema
- `schema.sql` 中已有 `documents` 表，含 `knowledge_base_id` 字段
- 缺少 `knowledge_base` 表 — Phase 4 需要创建

### Reusable Assets
- `backend/src/main/java/com/aikb/entity/Document.java` — 文档实体，含 knowledgeBaseId
- `backend/src/main/java/com/aikb/mapper/DocumentMapper.java` — MyBatis-Plus mapper
- `backend/scripts/extract_and_embed.py` — Phase 2 的文档处理脚本（含 Chroma 操作）
- `backend/scripts/chat_and_answer.py` — Phase 3 的 RAG 脚本（ProcessBuilder JSON 模式）

### Integration Points
- Chroma collection: `user_{userId}_kb_{kbId}` per D-12
- Python 脚本通过 ProcessBuilder 调用（JSON stdin/stdout 模式）
- Chroma delete API: `collection.delete(where={"kbId": kbId})` 等效操作

### Established Patterns
- ProcessBuilder 调用 Python 脚本（Phase 2/3 模式）
- MyBatis-Plus BaseMapper + QueryWrapper
- Axios 请求拦截器模式（前端）
</code_context>

<specifics>
## Specific Ideas

- 知识库表设计：id, user_id, name, description, created_at, updated_at
- 文档删除流程：先从 MySQL 删除元数据 → 再从 Chroma 删除向量
- Chroma 删除：调用 Python 脚本清理特定 kbId 的所有向量
- 知识库列表：返回知识库及其包含的文档数量
</specifics>

<deferred>
## Deferred Ideas

- 知识库重命名（Phase 4 MVP 后加入）
- 批量移动文档到其他知识库（Phase 4 MVP 后加入）
</deferred>

---
*Phase: 4-Knowledge-Base-Management*
*Context gathered: 2026-10-01*
