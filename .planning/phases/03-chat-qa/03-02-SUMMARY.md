---
phase: 03-chat-qa
plan: '02'
gap_closure: true

# Dependency graph
requires:
  - 03-01
provides:
  - ChatService.getSessionsByUserId()
  - ChatService.getChatHistory()
  - ChatController GET /api/chat/sessions
  - ChatController GET /api/chat/history/{sessionId}

# Actuals
actuals:
  tokens: 3000
  tasks: 1
  commits: 1

# Tech tracking
tech-stack:
  patterns:
    - MyBatis-Plus QueryWrapper for conditional queries
    - Session ownership validation before returning data

requirements-completed: [CHAT-01]

# Metrics
duration: 5min
completed: 2026-10-01
status: complete
---

# Phase 3 Plan 2 Summary (Gap Closure)

**补全 Phase 3 后端会话管理端点**

## Accomplishments

### Task 1 (Backend - Session Management)
- **ChatService.java** 新增方法:
  - `getSessionsByUserId(Long userId)` — 查询用户所有会话，按更新时间倒序
  - `getChatHistory(Long sessionId, Long userId)` — 获取会话历史，带权限校验
- **ChatController.java** 新增端点:
  - `GET /api/chat/sessions` — 返回用户会话列表
  - `GET /api/chat/history/{sessionId}` — 返回会话消息历史
  - 权限校验：访问他人会话返回 403

## Files Modified

- `backend/src/main/java/com/aikb/service/ChatService.java`
  - 新增 `getSessionsByUserId()` 和 `getChatHistory()` 方法
  - 新增 `QueryWrapper` 导入
- `backend/src/main/java/com/aikb/controller/ChatController.java`
  - 新增 `GET /api/chat/sessions` 端点
  - 新增 `GET /api/chat/history/{sessionId}` 端点
  - 新增 `ChatSession` 和 `ChatMessage` 实体导入

## Decisions Made

- 复用 `extractUserId()` 方法从 Authentication 获取用户ID
- 使用 `QueryWrapper` 而非自定义 SQL — 遵循 MyBatis-Plus 模式
- 权限校验在 Service 层完成，Controller 仅处理异常

## Verification

- mvn compile 通过
- ChatController 现在有 3 个端点：POST /ask, GET /sessions, GET /history/{sessionId}
- 前端 store 的 `getChatHistory` 和 `getChatSessions` 现在有对应后端支持

## Next for Phase 3

- 03-01 和 03-02 均完成，Phase 3 后端会话管理功能完整
- 前端可以调用 /api/chat/sessions 和 /api/chat/history/{sessionId} 加载历史会话
