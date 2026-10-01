# 路线图：AI 知识库

## 概览

构建一个 AI 驱动的 RAG 知识库应用，用户上传文档创建知识库，AI 基于知识库内容检索相关块并增强 LLM 生成来回答问题。系统采用 3 层架构：Vue 3 + uni-app 前端、Java Spring Boot BFF、Python FastAPI AI 服务 + Chroma 向量数据库。

## 阶段

- [x] **Phase 1: Foundation & Security** — JWT 认证、CORS、API 结构
- [ ] **Phase 2: Document Ingestion** — 上传、解析、分块、embedding、Chroma 索引
- [ ] **Phase 3: Chat/QA** — RAG 检索、上下文注入、LLM 响应
- [ ] **Phase 4: Knowledge Base Management** — 知识库 CRUD、文档-知识库关联
- [ ] **Phase 5: History & Feedback** — 对话历史、点赞/点踩
- [ ] **Phase 6: Maintenance & Polish** — 部署、监控、多端点

## 阶段详情

### Phase 1: Foundation & Security
**目标**：用户可以安全认证并访问系统
**依赖**：无（第一阶段）
**需求**：AUTH-01, AUTH-02, AUTH-03
**模式**：MVP
**成功标准**（必须为真）：
  1. 用户可以邮箱/密码注册并收到确认
  2. 用户可以登录并收到跨会话持久化的 JWT token
  3. 用户可以使用 JWT token 访问受保护端点
  4. 用户可以登出且 token 失效
  5. 系统拒绝过期或无效 token 的请求
**计划：** 3 个计划
- [x] 01-01-PLAN.md — 核心认证追踪：Spring Boot 项目 + User 实体 + 注册 + 登录 + JWT 过滤器
- [x] 01-02-PLAN.md — 刷新 token：后端刷新端点 + 前端 Axios 拦截器自动刷新
- [x] 01-03-PLAN.md — 登出 + 错误处理：客户端清除 + 401 重定向 + 全局错误 toast

### Phase 2: Document Ingestion
**目标**：用户上传的文档被解析、分块、embedding 并存储到 Chroma
**依赖**：Phase 1
**需求**：RAG-01, RAG-02
**模式**：MVP
**成功标准**（必须为真）：
  1. 用户可以通过 API 端点上传 PDF、Word 或 TXT 文件
  2. 系统从上传的文档中提取文本内容
  3. 系统将文本分块为最优片段（400-600 token，带重叠）
  4. 系统生成 embedding 并存储到 Chroma 向量数据库
  5. 上传过程优雅处理错误并给用户反馈
**计划**：3 个计划
- [ ] 02-01-PLAN.md — 文档上传追踪切块：端到端上传 → 提取 → embedding → Chroma 索引
- [ ] 02-02-PLAN.md — JWT 强制执行 + 文档持久化 + Java 文本分块
- [ ] 02-03-PLAN.md — 前端上传组件 + API 模块 + Pinia 状态

### Phase 3: Chat/QA
**目标**：用户可以提问并收到基于检索到的知识库内容的答案
**依赖**：Phase 2
**需求**：RAG-03, CHAT-01
**模式**：MVP
**成功标准**（必须为真）：
  1. 用户可以通过聊天界面发送问题
  2. 系统从 Chroma 基于问题检索相关文档块
  3. 系统将检索到的上下文注入 LLM 提示词
  4. 用户收到带源文档引用的生成答案
  5. 用户可以在同一会话线程中追问
**计划**：待定

### Phase 4: Knowledge Base Management
**目标**：用户可以将文档组织到知识库中进行管理
**依赖**：Phase 2
**需求**：KB-01, KB-02
**模式**：MVP
**成功标准**（必须为真）：
  1. 用户可以查看自己的知识库列表及文档数量
  2. 用户可以创建新知识库（名称和描述）
  3. 用户可以查看特定知识库内的文档
  4. 用户可以删除知识库中的文档
  5. 删除文档时同步从 Chroma 删除其向量
**计划**：待定

### Phase 5: History & Feedback
**目标**：用户可以回顾过去的对话并提供反馈
**依赖**：Phase 3
**需求**：CHAT-02
**模式**：MVP
**成功标准**（必须为真）：
  1. 用户可以查看过去会话列表
  2. 用户可以点击进入会话查看完整对话历史
  3. 用户可以对回答点赞或点踩
  4. 用户反馈被存储并与回答关联
  5. 对话跨浏览器会话持久化
**计划**：待定

### Phase 6: Maintenance & Polish
**目标**：系统通过监控和多端点部署达到生产就绪
**依赖**：Phase 5
**需求**：无额外
**模式**：MVP
**成功标准**（必须为真）：
  1. 应用成功部署到托管平台（H5 可通过公网 URL 访问）
  2. 系统优雅处理网络错误，显示用户友好消息
  3. API 端点有基本健康检查和监控
  4. 多端点变体（H5/小程序/App）可从同一代码库构建
  5. 重新索引管道可以刷新陈旧的文档向量
**计划**：待定

## 进度

| 阶段 | 已完成计划 | 状态 | 完成时间 |
|-------|----------------|--------|-----------|
| 1. Foundation & Security | 3/3 | Complete | 2026-09-29 |
| 2. Document Ingestion | 0/? | Not started | - |
| 3. Chat/QA | 0/? | Not started | - |
| 4. Knowledge Base Management | 0/? | Not started | - |
| 5. History & Feedback | 0/? | Not started | - |
| 6. Maintenance & Polish | 0/? | Not started | - |
