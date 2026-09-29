# AI Knowledge Base - 智能知识库问答平台

## What This Is

一个面向面试的全栈 AI 应用项目：用户上传文档构建私有知识库，AI 基于知识库内容回答问题（RAG 检索增强生成）。前端用 Vue 3 + uni-app 实现多端覆盖，后端用 Java Spring Boot 提供 API，Python FastAPI 微服务处理 AI 推理。

## Core Value

**让面试官看到一个真实的 AI 应用是如何从 0 到 1 构建的** — 包括架构设计、技术选型、难点处理和部署运维。

## Business Context

- **用户**：面试官 / 招聘方
- ** Revenue model**：展示项目（无商业化需求）
- **Success metric**：面试时能完整演示 RAG 问答流程，能讲清架构设计和技术细节
- **Strategy notes**：分阶段交付，第一阶段聚焦 RAG 核心功能，第二阶段扩展管理功能

## Requirements

### Validated

(None yet — ship to validate)

### Active

- [ ] **RAG-01**: 用户可以上传 PDF/Word/TXT 文档
- [ ] **RAG-02**: 系统自动解析文档并生成向量存储到 Chroma
- [ ] **RAG-03**: 用户可以对话提问，AI 基于知识库检索结果生成答案
- [ ] **AUTH-01**: 用户可以注册和登录
- [ ] **AUTH-02**: JWT Token 认证，支持刷新 token
- [ ] **AUTH-03**: 用户可以登出
- [ ] **KB-01**: 用户可以查看自己的知识库列表
- [ ] **KB-02**: 用户可以删除知识库中的文档
- [ ] **CHAT-01**: 用户可以和 AI 进行多轮对话
- [ ] **CHAT-02**: 对话历史保存，可回溯

### Out of Scope

- 即时通讯 / 消息通知功能
- 视频 / 图片处理
- 复杂的多租户权限系统
- 付费订阅 / 计费系统

## Context

- **现有代码库**：`D:\code\ai-knowledge-base` 包含 Vue 学习项目和 `vue-counter` 子目录
- **学习目标**：从学习 demo 项目升级到可用于面试的真实项目
- **技术储备**：Vue 3 基础知识已具备，Java 后端有经验，AI 应用需要加强

## Constraints

- **多端展示**：H5 + 微信小程序 + Android App，部署后能公网访问
- **预算限制**：个人开发不付费，优先使用免费服务和 API
- **LLM 选择**：硅基流动 API + DeepSeek 模型
- **时间限制**：1 个月内完成第一阶段可用版本
- **向量数据库**：Chroma（免费，本地存储）

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Python AI 微服务 | RAG + LangChain 主流架构，Java 对 AI 支持有限 | — Pending |
| 硅基流动 + DeepSeek | 国内可用，价格低，有免费额度 | — Pending |
| Chroma 向量数据库 | 免费、易用、Python 原生支持 | — Pending |
| uni-app 多端 | 一套代码覆盖 H5/小程序/App，面试展示方便 | — Pending |
| 分阶段交付 | 第一阶段聚焦 RAG 核心，第二阶段扩展管理功能 | — Pending |

---

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state (users, feedback, metrics)

---

*Last updated: 2026-09-29 after initial definition*
