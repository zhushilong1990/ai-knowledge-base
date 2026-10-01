# 架构

**分析日期：** 2026-09-29

## 系统概览

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                        fullstack-learning 代码仓库                            │
├─────────────────────────────────┬───────────────────────────────────────────┤
│        vue-counter/             │           uni-app-demo/                   │
│   （第 1-2 周 Vite + Vue 3）     │       （第 2 周跨平台）                   │
│   [src/*]                       │       [pages/*]                          │
└─────────────────────────────────┴───────────────────────────────────────────┘
         │                                     │
         ▼                                     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           docs/（教学材料）                                   │
│                    [day1-*.mjs, day1-*.html, utils.mjs]                   │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 组件职责

| 组件 | 职责 | 文件 |
|-----------|----------------|------|
| `vue-counter` | 主 SPA 演示（Vite + Vue 3 + Element Plus） | `vue-counter/src/*` |
| `uni-app-demo` | 跨平台演示（H5 + 微信小程序 + App） | `uni-app-demo/pages/*` |
| `docs/` | 独立教学练习（ESM 模块、HTML 演示） | `docs/*` |

## 模式概览

**总体：** 多演示学习仓库，复杂度逐步递进

**关键特征：**
- 两个并行演示项目，共享 Vue 3 基础
- `vue-counter`：功能完整的 SPA，含路由、状态管理、API 集成
- `uni-app-demo`：跨平台变体，使用 `uni.request` 和 `uni.navigateTo`
- Mock API 层用于离线开发，生产环境用真实 API
- Pinia stores 集中管理状态，localStorage 持久化

## 分层

### `vue-counter/`（主 SPA）

**展示层：**
- 位置：`vue-counter/src/views/` 和 `vue-counter/src/components/`
- 内容：页面组件（`Home`、`Login`、`TodoList`、`TodoDetail`、`Stats`）和可复用组件（`TodoItem`、`TodoForm`、`TodoStats`、`TodoDeleteConfirm`）
- 依赖：Pinia stores、Vue Router

**状态层：**
- 位置：`vue-counter/src/stores/`
- 内容：`auth.js`（token + 用户会话）、`todos.js`（CRUD + 分页 + 字段映射）
- 模式：Pinia + `defineStore`，使用 Composition API（`ref`、`computed`）
- 持久化：auth token/user 用 localStorage，todos 仅通过 API

**API 层：**
- 位置：`vue-counter/src/api/`
- 内容：`http.js`（Axios 实例 + 拦截器）、`auth.js`、`todos.js`
- 模式：Axios 请求拦截器注入 `Authorization: Bearer` Token，响应拦截器处理 401 和解包错误
- Base URL：`https://siwei7905.cloud:8443/api`（生产）

**路由层：**
- 位置：`vue-counter/src/router/index.js`
- 模式：Vue Router 4，hash 历史模式
- 守卫：`beforeEach` 检查 Pinia store 中的 auth token

### `uni-app-demo/`（跨平台）

**展示层：**
- 位置：`uni-app-demo/pages/*`
- 内容：`pages/index/index.vue`、`pages/todo/list.vue`、`pages/todo/detail.vue`

**Mock 层：**
- 位置：`uni-app-demo/mock/`
- 内容：`index.js`（通过 `uni.addInterceptor` 设置拦截器）、`api.js`
- 模式：拦截 `uni.request` 调用并路由到本地 mock API

### `docs/`（独立练习）

**教学材料：**
- 位置：`docs/`
- 内容：ESM 模块练习（`utils.mjs`、`main.mjs`）、HTML 演示（`day1-morning.html`、`day1-my-card.html`、`day2-css-practice.html`）、JS 练习（`day1-js-exercises.js`）
- 模式：无构建步骤的独立 HTML/JS 文件

## 数据流

### 主要请求路径（vue-counter）

1. **UI 层**（`vue-counter/src/views/TodoList.vue`）调用 `useTodosStore().loadFromApi()`
2. **Store**（`vue-counter/src/stores/todos.js`）调用 `todosApi.fetchTodos()`
3. **API 层**（`vue-counter/src/api/todos.js`）调用 `http.get('/todos')`
4. **Axios**（`vue-counter/src/api/http.js`）通过请求拦截器注入 `Authorization` 头
5. **后端** 返回 `ApiResponse { code, data, message }`
6. **响应拦截器** 解包 `data` 并返回给 store
7. **Store** 映射后端字段（`title`/`priority int`）到前端字段（`text`/`priority string`）
8. **UI** 通过 Vue 响应式重新渲染

### 认证流

1. 用户在 `Login.vue` 提交凭证
2. `useAuthStore().login()` 调用 `authApi.login()`
3. 后端返回 `{ token, userId, username }`
4. Store 保存到 Pinia（`token`、`user`）+ localStorage
5. 路由守卫从 store 读取，允许访问受保护路由

### uni-app Mock 流

1. `pages/todo/list.vue` 调用 `uni.request({ url: '/todos' })`
2. `mock/index.js` 中的 `uni.addInterceptor` 拦截请求
3. 路由到 `mock/api.js` 函数，返回 mock 数据
4. 页面直接使用返回的数据

## 关键抽象

**Pinia Store（状态容器）：**
- 目的：应用状态的单一真相来源
- 示例：`useAuthStore`、`useTodosStore`
- 模式：Composition API 风格的 `defineStore`

**Axios HTTP 客户端：**
- 目的：带拦截器的集中式 HTTP
- 示例：`http.js`
- 模式：单例实例，含请求/响应拦截器

**uni-app 拦截器：**
- 目的：将 API 调用重定向到 mock 或真实后端
- 示例：`uni-app-demo/mock/index.js`
- 模式：`uni.addInterceptor('request', { invoke(args) {...} })`

**字段映射器：**
- 目的：API 和 UI 字段名之间转换
- 示例：`backToFrontPriority()`、`frontToBackPriority()` 在 `stores/todos.js`
- 模式：双向映射函数

## 入口点

**`vue-counter/src/main.js`：**
- 触发：`npm run dev` 或 `npm run build`
- 职责：创建 Vue 应用，注册 Element Plus、Pinia、Vue Router、全局错误处理器
- 挂载到：`<div id="app">`

**`uni-app-demo/main.js`：**
- 触发：`npm run dev:%PLATFORM%`（mp-weixin、h5 等）
- 职责：创建 SSR 应用，调用 `setupMock()`，导出 `createApp()`
- 挂载到：`App.vue` 组件

**`docs/main.mjs`：**
- 触发：`node docs/main.mjs`
- 职责：ESM 模块导入演示

## 架构约束

- **无 TypeScript：** 仅使用纯 JavaScript（按 CLAUDE.md D8）
- **无路由懒加载：** 所有组件静态导入（演示规模不需要）
- **无服务端渲染：** `vue-counter` 是纯客户端 SPA
- **全局错误处理：** `main.js` 中的 Vue `app.config.errorHandler` 捕获未处理错误
- **Pinia 先于 Router：** Pinia 必须在路由之前注册（路由守卫依赖 stores）

## 反模式

### Store 中的字段映射

**问题：** 字段映射（`title` vs `text`、`priority` int vs string）分散在 store actions 中
**为什么错误：** 业务逻辑和数据转换混在一起；API 契约变更时改动痛苦
**正确做法：** 创建专用的 `transformers/` 或 `mappers/` 层，或使用计算属性

### API 层中的 localStorage

**问题：** `http.js` 直接读取 `localStorage.getItem('token')` 而不是使用 Pinia store
**为什么错误：** 认证状态有多个真相来源；store 已经在登录/登出时同步 localStorage
**正确做法：** 在请求拦截器中通过 Pinia store 注入 token

### 硬编码 API URL

**问题：** `http.js` 中硬编码 `baseURL: 'https://siwei7905.cloud:8443/api'`
**为什么错误：** 开发/生产切换需要改代码
**正确做法：** 使用 `.env` 文件和 `VITE_API_BASE_URL`

## 错误处理

**策略：** 集中在 Axios 响应拦截器 + Vue 全局错误处理器

**模式：**
- HTTP 错误（401、5xx）：Axios 拦截器显示 `ElMessage.error`，401 时跳转登录页
- 网络错误（`Failed to fetch`）：`main.js` 中的全局错误处理器捕获并显示友好消息
- 后端错误（业务 code != 200）：响应拦截器用 message 拒绝

## 横切关注点

**日志：** `console.error` 记录错误，`console.log` 用于调试输出
**校验：** Element Plus 表单校验（`el-form` 的 `rules` prop）客户端校验
**认证：** Token 存在 Pinia + localStorage，Axios 拦截器和路由守卫读取

---

*架构分析：2026-09-29*
