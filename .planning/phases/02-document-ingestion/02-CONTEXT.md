# Phase 2: Document Ingestion — Context

**Phase:** 02-document-ingestion
**Gathered:** 2026-10-01
**Status:** Ready for planning

<domain>
## Phase Boundary

用户上传文档 → 系统解析文本 → 分块 → 生成向量 → 存入 Chroma。交付：文档上传 API、文本提取、分块、embedding 生成、Chroma 索引。
</domain>

<decisions>
## Implementation Decisions

### 上传处理模式
- **D-10：** 采用**同步处理**，文件小（<10MB），流程简单，无需队列基础设施

### Embedding 生成方式
- **D-11：** 调用**硅基流动 /embeddings 接口**，无需额外部署 Ollama

### Chroma Collection 组织
- **D-12：** **每用户一个 collection**，隔离性好，删除用户一并清理向量数据

### 中文文档处理
- **D-13：** 使用 **PyMuPDF**（`pypdf`），中文乱序用原始文本行排序补救

### 分块策略
- **D-14：** **500 token 分块，50 token 重叠**（约 10%），平衡上下文和质量

### 固定决策（来自规划）
- 支持格式：PDF、Word、TXT
- 不支持：扫描件 OCR、图片
- 同步返回结果

</decisions>

<code_context>
## Existing Code Insights

### Reusable Assets
- `backend/src/main/java/com/aikb/controller/AuthController.java` — 参考 REST Controller 模式
- `backend/src/main/java/com/aikb/service/AuthService.java` — Service 层组织方式
- `application.yml` — 配置模式（JWT/数据库/跨域）

### Integration Points
- 前端：Axios 文件上传到 `/api/documents/upload`
- 后端：接收 Multipart → 本地临时文件 → PyMuPDF 解析 → 分块 → 硅基流动 API → Chroma
- 持久化：MyBatis-Plus entity/DTO/Repository 模式

</code_context>

<canonical_refs>
## Canonical References

无外部依赖文档 — 所有需求和约束均已在上述 decisions 中记录。

</canonical_refs>

<specifics>
## Specific Ideas

- 文件存储：本地 `backend/uploads/` 目录（临时），生产可切换 MinIO
- Chroma 持久化：`backend/data/chroma/`
- 硅基流动 embedding 端点：`https://api.ai.mlops-api.com/embeddings`（需确认）
- 文件大小限制：10MB 前端校验

</specifics>

<deferred>
## Deferred Ideas

- 异步处理 + 轮询状态（Phase 2 MVP 后加入）
- OCR 支持（扫描件）
- 增量索引更新
- 批量上传队列

</deferred>

---

*Phase: 2-Document Ingestion*
*Context gathered: 2026-10-01*
