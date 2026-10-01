# 代码库结构

**分析日期：** 2026-09-29

## 目录布局

```
D:\code\fullstack-learning\
├── CLAUDE.md              # 项目说明和路线图
├── HANDOFF.md             # 跨会话进度交接
├── docs\                  # 教学材料和练习
│   ├── main.mjs           # ESM 模块演示入口
│   ├── utils.mjs          # ESM 工具函数
│   ├── day1-*.mjs         # 第 1 天练习
│   ├── day1-*.html        # 第 1 天 HTML 演示
│   └── day2-*.html        # 第 2 天 HTML 演示
├── vue-counter\           # 主 Vue 3 SPA 演示（第 1-2 周）
│   ├── index.html         # HTML 入口
│   ├── package.json       # 依赖和脚本
│   ├── vite.config.js      # Vite 配置（含代理）
│   └── src\
│       ├── main.js         # Vue 应用初始化
│       ├── App.vue         # 根组件，含导航
│       ├── router\
│       │   └── index.js    # Vue Router 配置 + 守卫
│       ├── stores\
│       │   ├── auth.js     # 认证状态（Pinia）
│       │   └── todos.js    # Todos 状态 + 字段映射
│       ├── api\
│       │   ├── http.js     # Axios 实例 + 拦截器
│       │   ├── auth.js     # 认证 API 调用
│       │   └── todos.js    # Todos API 调用
│       ├── mock\
│       │   └── api.js      # 离线开发用 Mock API
│       ├── views\
│       │   ├── Home.vue        # 落地页
│       │   ├── Login.vue       # 登录页
│       │   ├── TodoList.vue    # 列表，含分页
│       │   ├── TodoDetail.vue  # 详情页
│       │   ├── TodoFormPage.vue # 创建/编辑表单
│       │   ├── Stats.vue       # ECharts 可视化
│       │   └── NotFound.vue    # 404 页面
│       └── components\
│           ├── TodoItem.vue         # 单条 todo 行
│           ├── TodoForm.vue         # Todo 表单组件
│           ├── TodoStats.vue        # 统计展示
│           └── TodoDeleteConfirm.vue # 删除确认
└── uni-app-demo\            # 跨平台演示（第 2 周+）
    ├── index.html           # HTML 入口（uni-app 中未使用）
    ├── main.js              # uni-app 入口
    ├── App.vue              # 根组件
    ├── manifest.json        # App 配置
    ├── pages.json           # 页面路由配置
    ├── mock\
    │   ├── index.js         # Mock 拦截器设置
    │   └── api.js           # Mock API 函数
    └── pages\
        ├── index\
        │   └── index.vue    # 首页
        └── todo\
            ├── list.vue     # Todo 列表页
            └── detail.vue   # Todo 详情页
```

## 目录用途

**`vue-counter/`：**
- 用途：主要学习演示 — 完整 Vue 3 SPA，含路由、状态、API 集成
- 内容：Vite 项目，Vue 3、Element Plus、Pinia、Vue Router、Axios、ECharts
- 关键文件：`src/main.js`、`src/router/index.js`、`src/stores/*.js`、`src/api/*.js`

**`uni-app-demo/`：**
- 用途：跨平台变体，展示从同一代码库构建 H5 + 微信小程序 + App
- 内容：uni-app 项目结构，含 `pages.json` 路由、`manifest.json` 平台配置
- 关键文件：`main.js`、`pages.json`、`mock/index.js`

**`docs/`：**
- 用途：无构建步骤的独立教学练习
- 内容：ESM `.mjs` 文件、HTML 演示、JavaScript 练习
- 关键文件：`main.mjs`、`utils.mjs`、`day1-*.html`

## 关键文件位置

**入口点：**
- `vue-counter/index.html`：HTML 壳，挂载 Vue 应用（`<div id="app">`）
- `vue-counter/src/main.js`：Vue 应用初始化（注册插件、挂载）
- `uni-app-demo/main.js`：uni-app 引导（创建 SSR 应用，设置 mock）

**配置：**
- `vue-counter/vite.config.js`：Vite 配置，`/api` 代理到后端
- `vue-counter/package.json`：Vue 3、Element Plus、Pinia、Vue Router、Axios、ECharts
- `uni-app-demo/manifest.json`：平台特定配置（微信 appid、权限）
- `uni-app-demo/pages.json`：页面路由和导航栏标题

**核心逻辑：**
- `vue-counter/src/stores/auth.js`：认证 store（token、user、login、logout）
- `vue-counter/src/stores/todos.js`：Todos store（CRUD、分页、字段映射）
- `vue-counter/src/api/http.js`：Axios 实例，含拦截器
- `vue-counter/src/router/index.js`：带认证守卫的路由

**测试：**
- 无正式测试目录 — 学习演示，手动 L1-L4 验证

## 命名约定

**文件：**
- Vue 组件：PascalCase（`TodoList.vue`、`TodoDetail.vue`）
- JavaScript 模块：camelCase（`auth.js`、`todos.js`、`http.js`）
- 页面目录（uni-app）：小写（`pages/todo/list.vue`）

**目录：**
- `src/stores/`：Pinia stores
- `src/api/`：API 模块
- `src/views/`：页面组件
- `src/components/`：可复用组件
- `pages/`：uni-app 页面组件
- `mock/`：Mock API 模块

**变量/函数：**
- camelCase：`fetchTodos`、`createTodo`、`loadFromApi`
- Store actions：camelCase 动词：`login`、`logout`、`loadFromApi`、`setPage`
- Store state：camelCase 名词：`token`、`user`、`todos`、`currentPage`

## 新增代码的位置

**新功能（vue-counter）：**
- 页面组件：`vue-counter/src/views/NewFeature.vue`
- 可复用组件：`vue-counter/src/components/NewFeatureItem.vue`
- Store（需要时）：`vue-counter/src/stores/newFeature.js`
- API（需要时）：`vue-counter/src/api/newFeature.js`
- 路由：添加到 `vue-counter/src/router/index.js` 的 routes 数组

**新组件/模块：**
- 实现：在适当的 `src/` 子目录中创建
- 共享工具：在现有文件或 `src/` 根目录新建模块

**工具类：**
- 共享帮助函数：在 `vue-counter/src/utils/` 创建（目前不存在）
- ESM 工具：添加到 `docs/utils.mjs`

## 特殊目录

**`vue-counter/node_modules/`：**
- 用途：npm 依赖（按 .gitignore 不提交）
- 生成方式：通过 `npm install`
- 提交：否

**`uni-app-demo/unpackage/`：**
- 用途：各平台构建产物
- 生成方式：通过 `npm run dev:mp-weixin` 等
- 提交：否

**`.claude/`：**
- 用途：Claude Code 设置和记忆
- 内容：`settings.local.json`、`memory/MEMORY.md`
- 生成方式：Claude Code 自动创建

---

*结构分析：2026-09-29*
