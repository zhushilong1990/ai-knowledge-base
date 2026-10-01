# 项目调研总结

**项目：** RAG 知识库问答应用
**领域：** AI 驱动的知识库 RAG（检索增强生成）问答
**调研时间：** 2026-09-29
**置信度：** 中高

---

## 执行摘要

这是一个 AI 驱动的 RAG 知识库应用，使用户能够上传文档、创建知识库，并通过检索相关块增强 LLM 生成来回答问题。

关键架构：**3 层 BFF 模式** — Vue 前端 → Java Spring Boot BFF → Python FastAPI AI 服务 + ChromaDB + Ollama/DeepSeek

---

## 关键发现

### 技术栈
- **AI 服务**：Python 3.10+ / FastAPI + LangChain 0.1.x + ChromaDB 0.4.x
- **后端**：Spring Boot 3.2.x + MySQL + JWT
- **前端**：Vue 3 + uni-app + Pinia + Element Plus/uView Plus
- **LLM**：硅基流动 API + DeepSeek 模型
- **向量数据库**：Chroma（本地、免费）

### 基础功能（必须有）
- 文档上传 / 分块 / 索引
- 知识库 CRUD
- 带引用的 RAG 聊天
- JWT 认证
- 对话历史

### 差异化功能（做得更好的地方）
- 混合搜索（精度提升 10-15%）
- 流式 SSE 响应
- 可调 Top-K 检索
- 用户反馈（点赞/点踩）

### 关键陷阱
1. **分块不匹配**（80% 的 RAG 失败）— 使用 400-600 token，10-20% 重叠
2. **文档解析失败** — 布局感知解析器，中文文档处理
3. **Embedding 上下文溢出** — 验证模型限制，分层分块
4. **Java-Python 延迟** — 10 分钟超时，连接池
5. **索引陈旧** — 版本控制 + 重新索引管道

---

## 对路线图的影响

建议 6 阶段结构：
1. **Foundation & Security** — JWT 认证、CORS、API 密钥
2. **Document Ingestion** — 上传、解析、分块、embedding、Chroma 索引
3. **Chat/QA** — RAG 检索、上下文注入、LLM 响应、引文
4. **Knowledge Base Management** — 知识库 CRUD、文档-知识库关联
5. **History & Feedback** — 对话历史、点赞/点踩
6. **Maintenance & Polish** — 重新索引管道、部署、监控

---

## 调研标记

**规划期间需要调研：**
- Phase 2：真实文档分块验证
- Phase 3：混合搜索、Top-K 调优

**标准模式（跳过调研）：**
- Phase 1：JWT/Spring Boot
- Phase 4：CRUD/RBAC
- Phase 5：历史

---

## 空白

- 中文 PDF 解析未验证 — Phase 2 尽早测试
- DeepSeek 模型选择（中文内容）未验证
- 规模估算（100-1000 用户）仅为方向性参考

---

## 来源

- `.planning/research/STACK.md`（219 行）
- `.planning/research/FEATURES.md`（245 行）
- `.planning/research/ARCHITECTURE.md`（380 行）
- `.planning/research/PITFALLS.md`（438 行）

---

*调研综合：2026-09-29*
