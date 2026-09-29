# Phase 1: Foundation & Security - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-09-29
**Phase:** 1-Foundation & Security
**Areas discussed:** 注册流程, Token 存储, 刷新策略, 登出处理

---

## 注册流程

| Option | Description | Selected |
|--------|-------------|----------|
| 邮箱 + 密码 | 最通用，前端实现简单，后端也容易对接 | ✓ |
| 手机号 + 验证码 | 国内常见，但需要短信 API，成本和复杂度增加 | |
| 邮箱 + 密码，并且支持手机号登录 | 两套并行，用户可选，更接近真实生产项目 | |

**User's choice:** 邮箱 + 密码
**Notes:** 用户明确选择最简单的邮箱+密码方案

---

## Token 存储

| Option | Description | Selected |
|--------|-------------|----------|
| localStorage | 简单好实现，但容易受到 XSS 攻击窃取 | ✓ |
| httpOnly Cookie | 更安全，JS 读不到，防 XSS，但需要配合 CSRF 防护 | |

**User's choice:** localStorage
**Notes:** 用户查看了 mallee-muji 项目（采用 httpOnly Cookie + CSRF 方案），但本项目选择 localStorage，理由是简单且适合面试展示

---

## 刷新策略

| Option | Description | Selected |
|--------|-------------|----------|
| 主动刷新（推荐） | Token 剩余有效期低于阈值时，页面静默调用刷新接口获取新 Token，体验好 | ✓ |
| 被动刷新 | 请求时发现 Token 失效才刷新，可能导致一次请求失败，用户能看到闪一下 | |

**User's choice:** 主动刷新
**Notes:** 用户选择推荐方案

---

## 登出处理

| Option | Description | Selected |
|--------|-------------|----------|
| 客户端清除（推荐） | 只在客户端删除 localStorage 中的 Token，简单，后端 Token 过期自动失效 | ✓ |
| 服务端失效 | 调用服务端接口使 Token 黑名单化，更安全但更复杂，需要服务端维护黑名单 | |

**User's choice:** 客户端清除
**Notes:** 用户选择最简单的方案

---

## Claude's Discretion

无 — 所有决策均由用户明确选择

## Deferred Ideas

无
