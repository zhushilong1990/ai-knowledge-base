# Phase 1: Foundation & Security - Context

**Gathered:** 2026-09-29
**Status:** Ready for planning

<domain>
## Phase Boundary

用户可以安全地注册、登录，并访问受保护的系统接口。Phase 1 交付：用户注册、登录、JWT Token 认证、登出功能。
</domain>

<decisions>
## Implementation Decisions

### 用户注册
- **D-01:** 注册方式为**邮箱 + 密码**，不使用手机号或第三方登录

### Token 存储
- **D-02:** JWT Token 存储在 **localStorage**，不采用 httpOnly Cookie

### Token 刷新策略
- **D-03:** 采用**主动刷新**策略 — Token 剩余有效期低于阈值时，前端静默调用刷新接口获取新 Token

### 登出处理
- **D-04:** 登出时**仅客户端清除** localStorage 中的 Token，后端 Token 自然过期失效，不做服务端黑名单

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
- `vue-counter/src/stores/auth.js` — 现有 Pinia auth store，可参考 token 存取模式
- `vue-counter/src/api/http.js` — Axios 拦截器模式，可参考 token 注入逻辑

### Established Patterns
- Pinia + localStorage 持久化模式（参考 `vue-counter` 的 auth store）
- Axios 请求拦截器注入 `Authorization: Bearer` 头

### Integration Points
- 前端登录页 → Pinia auth store → localStorage
- 所有 API 请求通过 Axios 拦截器自动注入 Token

</code_context>

<specifics>
## Specific Ideas

- 参考 `mallee-muji` 项目（`CookieUtil.java` / `AuthGlobalFilter.java`）的 Cookie 方案，但本项目采用 localStorage
- Token 主动刷新时机：剩余有效期 < 5 分钟时触发静默刷新

</specifics>

<deferred>
## Deferred Ideas

无

---

*Phase: 1-Foundation & Security*
*Context gathered: 2026-09-29*
