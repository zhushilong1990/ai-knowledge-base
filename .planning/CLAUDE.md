<!-- GSD:project-start source:PROJECT.md -->

## Project

**AI Knowledge Base - 智能知识库问答平台**

一个面向面试的全栈 AI 应用项目：用户上传文档构建私有知识库，AI 基于知识库内容回答问题（RAG 检索增强生成）。前端用 Vue 3 + uni-app 实现多端覆盖，后端用 Java Spring Boot 提供 API，Python FastAPI 微服务处理 AI 推理。

**Core Value:** **让面试官看到一个真实的 AI 应用是如何从 0 到 1 构建的** — 包括架构设计、技术选型、难点处理和部署运维。

### Constraints

- **多端展示**：H5 + 微信小程序 + Android App，部署后能公网访问
- **预算限制**：个人开发不付费，优先使用免费服务和 API
- **LLM 选择**：硅基流动 API + DeepSeek 模型
- **时间限制**：1 个月内完成第一阶段可用版本
- **向量数据库**：Chroma（免费，本地存储）

<!-- GSD:project-end -->

<!-- GSD:stack-start source:codebase/STACK.md -->

## Technology Stack

## Languages

- JavaScript ES6+ - Frontend source code (Vue SFCs, API modules, stores)
- Java 1.8 - Spring Boot backend
- HTML5 - Entry point markup
- CSS - Element Plus component styling (imported as library)

## Runtime

- Node.js (implied by Vite)
- NPM (package management)
- Java 1.8 (JDK 8)
- NPM 6+ (for frontend `vue-counter`)
- Maven (for backend `todo-backend`)
- Lockfile: `vue-counter/package-lock.json` (present)

## Frameworks

- Vue 3.5.42 - UI framework (Composition API)
- Vite 8.2.2 - Build tool and dev server
- Vue Router 4.6.4 - Client-side routing (hash mode)
- Pinia 4.0.3 - State management (replaces Vuex)
- Axios 1.20.0 - HTTP client
- Element Plus 2.14.6 - Web UI component library
- ECharts 6.1.0 - Charting (Stats page)
- Spring Boot 2.7.18 - Web framework (embedded Tomcat)
- Spring MVC - REST controller layer
- Spring Boot Starter Validation - Parameter validation
- jjwt-api/impl/jackson 0.11.5 - JWT token generation and validation
- Lombok - Auto-generate getters/setters/builders

## Key Dependencies

- `vue` 3.5.42 - Core framework
- `vue-router` 4.6.4 - Routing
- `pinia` 4.0.3 - State management
- `axios` 1.20.0 - HTTP client with interceptors
- `element-plus` 2.14.6 - UI components
- `echarts` 6.1.0 - Charts
- `spring-boot-starter-web` - MVC + Tomcat
- `spring-boot-starter-validation` - Bean Validation
- `jjwt-api/impl/jackson` 0.11.5 - JWT
- `lombok` - Boilerplate elimination
- `vite` 8.2.2 - Bundler and dev server
- `@vitejs/plugin-vue` 6.0.8 - Vue SFC plugin for Vite
- `spring-boot-maven-plugin` - JAR packaging

## Configuration

- `vue-counter/vite.config.js` - Vite configuration with proxy and base path
- `vue-counter/index.html` - Entry HTML
- `vue-counter/package.json` - Dependencies and npm scripts
- `backend/src/main/resources/application.yml` - Server port (8080), JWT secret, expiration
- `backend/pom.xml` - Maven dependencies and Spring Boot version (2.7.18)
- Not detected (hardcoded in application.yml for demo purposes)

## Platform Requirements

- Node.js 18+ (for Vite 8)
- JDK 8 (for Spring Boot 2.7.18)
- NPM 6+
- Frontend: Static hosting (GitHub Pages configured with `base: './'`)
- Backend: Java 8 runtime with embedded Tomcat on port 8080
- Target server: Tencent Cloud `123.207.69.23:8080` (production)

<!-- GSD:stack-end -->

<!-- GSD:conventions-start source:CONVENTIONS.md -->

## Conventions

## Naming Patterns

- Vue components: PascalCase `.vue` files (e.g., `TodoList.vue`, `TodoDeleteConfirm.vue`)
- JavaScript modules: camelCase (e.g., `http.js`, `auth.js`)
- Directories: camelCase or kebab-case (e.g., `stores/`, `api/`, `mock/`)
- camelCase for all JavaScript functions (e.g., `handleLogin`, `loadFromApi`)
- Event handlers prefixed with `handle` (e.g., `handleLogout`, `handleLogin`)
- Actions async-prefixed when they call APIs (e.g., `createViaApi`, `deleteViaApi`)
- camelCase (e.g., `newTodo`, `currentPage`, `currentPriority`)
- Constants in camelCase or UPPER_SNAKE_CASE for truly constant values
- Boolean variables prefixed with `is`, `has`, `can`, or `show` (e.g., `isLoggedIn`, `canAdd`, `showUser`)
- Not explicitly defined (no TypeScript in this project)
- Props use Object notation with `type` and `required` keys

## Code Style

- Tool used: None detected (no Prettier, ESLint, or Biome config files)
- Manual formatting with consistent 2-space indentation
- Template literals for string interpolation
- Tool used: None detected
- No `.eslintrc`, `.prettierrc`, or `eslint.config.*` files found

## Import Organization

- No path aliases configured
- Relative paths used throughout (`../stores/`, `./components/`)

## Error Handling

## Logging

- Console only for development debugging
- Comments document intent (Java comparisons in Chinese)
- No runtime logging for production

## Comments

- Explain Java Spring Boot对照 for Java developers learning Vue
- Document API integration points
- Explain business logic transformations (field mapping, etc.)
- Not used (no TypeScript)

## Function Design

- Props via `defineProps` object notation
- Function parameters explicit and named
- Maximum 3-4 parameters before grouping into object
- Async functions return Promises
- Computed properties for derived state
- Store actions return necessary data

## Module Design

- Named exports: `export function fetchTodos() { ... }`
- Store exports: `export const useTodosStore = defineStore(...)`
- No barrel files (index.js re-exports not used)
- Direct imports to specific modules

## Component Patterns

## State Management (Pinia)

<!-- GSD:conventions-end -->

<!-- GSD:architecture-start source:ARCHITECTURE.md -->

## Architecture

## System Overview

```text

```

## Component Responsibilities

| Component | Responsibility | File |
|-----------|----------------|------|
| `vue-counter` | Main SPA demo (Vite + Vue 3 + Element Plus) | `vue-counter/src/*` |
| `uni-app-demo` | Cross-platform demo (H5 + WeChat + App) | `uni-app-demo/pages/*` |
| `docs/` | Standalone teaching exercises (ESM modules, HTML demos) | `docs/*` |

## Pattern Overview

- Two parallel demo projects sharing Vue 3 fundamentals
- `vue-counter`: Full-featured SPA with routing, state management, API integration
- `uni-app-demo`: Cross-platform variant using `uni.request` and `uni.navigateTo`
- Mock API layer for offline development, real API for production
- Pinia stores for centralized state with localStorage persistence

## Layers

### `vue-counter/` (Primary SPA)

- Location: `vue-counter/src/views/` and `vue-counter/src/components/`
- Contains: Page components (`Home`, `Login`, `TodoList`, `TodoDetail`, `Stats`) and reusable components (`TodoItem`, `TodoForm`, `TodoStats`, `TodoDeleteConfirm`)
- Depends on: Pinia stores, Vue Router
- Location: `vue-counter/src/stores/`
- Contains: `auth.js` (token + user session), `todos.js` (CRUD + pagination + field mapping)
- Pattern: Pinia with `defineStore` using Composition API (`ref`, `computed`)
- Persistence: `localStorage` for auth token/user, API-only for todos
- Location: `vue-counter/src/api/`
- Contains: `http.js` (Axios instance with interceptors), `auth.js`, `todos.js`
- Pattern: Axios with request interceptor (injects `Authorization: Bearer` token) and response interceptor (401 handling, error unwrapping)
- Base URL: `https://siwei7905.cloud:8443/api` (production)
- Location: `vue-counter/src/router/index.js`
- Pattern: Vue Router 4 with hash history
- Guards: `beforeEach` checks auth token from Pinia store

### `uni-app-demo/` (Cross-Platform)

- Location: `uni-app-demo/pages/*`
- Contains: `pages/index/index.vue`, `pages/todo/list.vue`, `pages/todo/detail.vue`
- Location: `uni-app-demo/mock/`
- Contains: `index.js` (interceptor setup via `uni.addInterceptor`), `api.js`
- Pattern: Intercepts `uni.request` calls and routes to local mock API

### `docs/` (Standalone Exercises)

- Location: `docs/`
- Contains: ESM module exercises (`utils.mjs`, `main.mjs`), HTML demos (`day1-morning.html`, `day1-my-card.html`, `day2-css-practice.html`), JS exercises (`day1-js-exercises.js`)
- Pattern: Standalone HTML/JS files without build step

## Data Flow

### Primary Request Path (vue-counter)

### Auth Flow

### uni-app Mock Flow

## Key Abstractions

- Purpose: Single source of truth for application state
- Examples: `useAuthStore`, `useTodosStore`
- Pattern: Composition API style with `defineStore`
- Purpose: Centralized HTTP with interceptors
- Examples: `http.js`
- Pattern: Singleton instance with request/response interceptors
- Purpose: Redirect API calls to mock or real backend
- Examples: `uni-app-demo/mock/index.js`
- Pattern: `uni.addInterceptor('request', { invoke(args) {...} })`
- Purpose: Transform between API and UI field names
- Examples: `backToFrontPriority()`, `frontToBackPriority()` in `stores/todos.js`
- Pattern: Bidirectional mapping functions

## Entry Points

- Triggers: `npm run dev` or `npm run build`
- Responsibilities: Creates Vue app, registers Element Plus, Pinia, Vue Router, global error handler
- Mounts to: `<div id="app">`
- Triggers: `npm run dev:%PLATFORM%` (mp-weixin, h5, etc.)
- Responsibilities: Creates SSR app, calls `setupMock()`, exports `createApp()`
- Mounts to: `App.vue` component
- Triggers: `node docs/main.mjs`
- Responsibilities: ESM module imports demonstration

## Architectural Constraints

- **No TypeScript:** Plain JavaScript only (per CLAUDE.md D8)
- **No Routing Lazy Loading:** All components statically imported (demo scale does not require it)
- **No Server-Side Rendering:** `vue-counter` is pure client-side SPA
- **Global Error Handling:** Vue `app.config.errorHandler` in `main.js` for uncaught errors
- **Pinia Before Router:** Pinia must be registered before router (router guards use stores)

## Anti-Patterns

### Field Mapping in Store

### localStorage in API Layer

### Hardcoded API URLs

## Error Handling

- HTTP errors (401, 5xx): Axios interceptor shows `ElMessage.error`, redirects on 401
- Network errors (`Failed to fetch`): Global error handler in `main.js` catches and displays friendly message
- Backend errors (business code != 200): Response interceptor rejects with message

## Cross-Cutting Concerns

<!-- GSD:architecture-end -->

<!-- GSD:skills-start source:skills/ -->

## Project Skills

No project skills found. Add skills to any of: `.claude/skills/`, `.agents/skills/`, `.cursor/skills/`, `.github/skills/`, or `.codex/skills/` with a `SKILL.md` index file.
<!-- GSD:skills-end -->

<!-- GSD:workflow-start source:GSD defaults -->

## GSD Workflow Enforcement

Before using Edit, Write, or other file-changing tools, start work through a GSD command so planning artifacts and execution context stay in sync.

Use these entry points:
- `/gsd-quick` for small fixes, doc updates, and ad-hoc tasks
- `/gsd-debug` for investigation and bug fixing
- `/gsd-execute-phase` for planned phase work

Do not make direct repo edits outside a GSD workflow unless the user explicitly asks to bypass it.
<!-- GSD:workflow-end -->

<!-- GSD:profile-start -->

## Developer Profile

> Profile not yet configured. Run `/gsd-profile-user` to generate your developer profile.
> This section is managed by `generate-claude-profile` -- do not edit manually.
<!-- GSD:profile-end -->
