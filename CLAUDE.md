# AI Knowledge Base - 智能知识库问答平台

> AI 智能知识库 RAG 应用项目。
> 所有跨会话协作必须先读本文件 + HANDOFF.md。

---

## 1. 项目基本信息

| 项 | 值 |
|---|---|
| 项目名 | `ai-knowledge-base` |
| 创建时间 | 2026-09-29 |
| 项目根目录 | `D:\code\ai-knowledge-base\` |
| 目标 | 构建 AI 智能知识库 RAG 应用，用于面试展示 |

## 2. 项目概述

**目标：** 让面试官看到一个真实的 AI 应用是如何从 0 到 1 构建的 — 包括架构设计、技术选型、难点处理和部署运维。

**核心功能：** 用户上传文档构建私有知识库，AI 基于知识库内容回答问题（RAG 检索增强生成）。

**技术栈：**
- 前端：Vue 3 + uni-app（多端覆盖）
- 后端：Java Spring Boot（BFF 层）
- AI 服务：Python FastAPI（处理 AI 推理）
- 向量数据库：Chroma（免费、本地存储）
- LLM：硅基流动 API + DeepSeek 模型

## 3. 项目约束

- **多端展示**：H5 + 微信小程序 + Android App，部署后能公网访问
- **预算限制**：个人开发不付费，优先使用免费服务和 API
- **时间限制**：1 个月内完成第一阶段可用版本

## 4. 当前进度

Phase 1: Foundation & Security — 已完成讨论阶段，准备进入 Planning

详见：`.planning/ROADMAP.md` 和 `.planning/phases/01-foundation-security/01-CONTEXT.md`

## 5. Phase 1 决策摘要

| 决策区 | 选择 |
|---|---|
| 注册流程 | 邮箱 + 密码 |
| Token 存储 | localStorage |
| 刷新策略 | 主动刷新（剩余 < 5 分钟时触发） |
| 登出处理 | 客户端清除 |

---

*最后更新：2026-09-29*
