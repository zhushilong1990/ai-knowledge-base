# Phase 3: Chat/QA — Discussion Log

**Discussion:** 2026-10-01
**Mode:** default

## Areas Discussed

### 1. 对话历史存储

**Question:** 多轮对话历史存在哪里？

**Options presented:**
- A. 前端状态（Pinia store，刷新页面丢失）
- B. 后端数据库（跨会话持久化）
- C. Session 存储

**Selected:** B — 后端数据库

---

### 2. RAG 检索策略

**Question:** 检索多少个相关块？相似度阈值？

**Options presented:**
- A. Top-3 + 阈值 0.7
- B. Top-5，无阈值
- C. Top-1 + 扩展上下文

**Selected:** A — Top-3 + 阈值 0.7

---

### 3. LLM 回复方式

**Question:** 流式还是一次性返回？

**Options presented:**
- A. 流式（SSE）
- B. 一次性返回

**Selected:** B — 一次性返回

---

### 4. 来源引用格式

**Question:** 答案中的文档引用怎么展示？

**Options presented:**
- A. 编号列表【1】【2】【3】
- B. Inline 引用
- C. 脚注式

**Selected:** A — 编号列表

---

### 5. 错误处理策略

**Question:** LLM 服务不可用时怎么降级？

**Options presented:**
- A. 预设回复 + 记录日志
- B. 返回错误提示 + 建议稍后重试
- C. 关键词匹配降级

**Selected:** B — 错误提示 + 重试建议

---

## Deferred Ideas

- 流式 SSE 输出（Phase 3 MVP 后加入）
- 关键词匹配 fallback（Phase 3 MVP 后加入）
- Inline 引用格式（Phase 3 MVP 后评估）
