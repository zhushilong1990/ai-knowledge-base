# 外部集成

**分析日期：** 2026-09-29

## API 和外部服务

**后端 API（Spring Boot）：**
- Spring Boot REST API 运行在端口 8080
- 生产端点：`https://siwei7905.cloud:8443/api`（HTTPS，端口 8443）
- 开发代理：Vite 开发服务器将 `/api` 代理到 `http://123.207.69.23:8080`
- 认证：JWT Bearer Token（登录后收到，存 localStorage，Axios 拦截器注入）
- CORS：通过 `CorsConfig.java` 配置允许跨域请求

## 数据存储

**后端数据库：**
- H2 内存数据库（Spring Boot 默认自动配置）
- JDBC 连接：Spring Boot 从 starter 自动配置 H2
- 未检测到外部数据库服务

**前端状态：**
- localStorage — JWT Token 和用户对象在页面刷新后持久化
- Pinia stores（内存）— 认证状态（`stores/auth.js`）、Todo 状态（`stores/todos.js`）

**文件存储：**
- 无（暂无文件上传功能）

## 认证与身份

**认证提供商：**
- 自定义基于 JWT 的认证
- 实现：`backend/util/JwtUtil.java` — HS256 签名，7 天过期
- 登录端点：`POST /api/auth/login` 返回 `{ token, userId, username }`
- Token 注入：Axios 请求拦截器 `src/api/http.js` 设置 `Authorization: Bearer <token>`
- Token 存储：`localStorage.setItem('token', ...)` 在 Pinia auth store 中
- 路由守卫：`src/router/index.js` 中的 `router.beforeEach` 检查 `useAuthStore().token`

**无外部认证提供商**（无 OAuth，无第三方认证服务）

## 监控与可观测性

**错误追踪：**
- 未检测到（Sentry、LogRocket 等均无）

**日志：**
- 前端：`console.error` 记录全局 Vue 错误和 Vite 代理错误
- 后端：标准 Spring Boot 日志（SLF4J）
- Axios 拦截器通过 ElMessage（Element Plus toast）记录错误

## CI/CD 与部署

**托管：**
- 前端：GitHub Pages（`vite.config.js` 中 `base: './'` 配置）
- 后端：腾讯云服务器（IP：123.207.69.23，端口 8080）

**CI 流水线：**
- 未检测到（无 GitHub Actions，无自动化部署）

## 环境配置

**所需环境变量：**
- 未检测到（演示阶段 JWT 密钥写死在 `application.yml` 中）

**密钥位置：**
- `backend/src/main/resources/application.yml` — JWT 密钥写死（演示可接受）
- 仓库中未检测到 `.env` 文件

## Webhook 和回调

**入站：**
- 无

**出站：**
- 无（无出站 webhook 调用）

---

*集成审计：2026-09-29*
