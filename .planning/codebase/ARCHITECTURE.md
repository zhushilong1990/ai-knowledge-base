# Architecture

**Analysis Date:** 2026-09-29

## System Overview

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                        fullstack-learning Repository                        │
├─────────────────────────────────┬───────────────────────────────────────────┤
│        vue-counter/             │           uni-app-demo/                   │
│   (Week 1-2 Vite + Vue 3)        │       (Week 2 cross-platform)           │
│   [src/*]                       │       [pages/*]                          │
└─────────────────────────────────┴───────────────────────────────────────────┘
         │                                     │
         ▼                                     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           docs/ (Teaching Materials)                        │
│                    [day1-*.mjs, day1-*.html, utils.mjs]                     │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Component Responsibilities

| Component | Responsibility | File |
|-----------|----------------|------|
| `vue-counter` | Main SPA demo (Vite + Vue 3 + Element Plus) | `vue-counter/src/*` |
| `uni-app-demo` | Cross-platform demo (H5 + WeChat + App) | `uni-app-demo/pages/*` |
| `docs/` | Standalone teaching exercises (ESM modules, HTML demos) | `docs/*` |

## Pattern Overview

**Overall:** Multi-demo learning repository with progressive complexity

**Key Characteristics:**
- Two parallel demo projects sharing Vue 3 fundamentals
- `vue-counter`: Full-featured SPA with routing, state management, API integration
- `uni-app-demo`: Cross-platform variant using `uni.request` and `uni.navigateTo`
- Mock API layer for offline development, real API for production
- Pinia stores for centralized state with localStorage persistence

## Layers

### `vue-counter/` (Primary SPA)

**Presentation Layer:**
- Location: `vue-counter/src/views/` and `vue-counter/src/components/`
- Contains: Page components (`Home`, `Login`, `TodoList`, `TodoDetail`, `Stats`) and reusable components (`TodoItem`, `TodoForm`, `TodoStats`, `TodoDeleteConfirm`)
- Depends on: Pinia stores, Vue Router

**State Layer:**
- Location: `vue-counter/src/stores/`
- Contains: `auth.js` (token + user session), `todos.js` (CRUD + pagination + field mapping)
- Pattern: Pinia with `defineStore` using Composition API (`ref`, `computed`)
- Persistence: `localStorage` for auth token/user, API-only for todos

**API Layer:**
- Location: `vue-counter/src/api/`
- Contains: `http.js` (Axios instance with interceptors), `auth.js`, `todos.js`
- Pattern: Axios with request interceptor (injects `Authorization: Bearer` token) and response interceptor (401 handling, error unwrapping)
- Base URL: `https://siwei7905.cloud:8443/api` (production)

**Routing Layer:**
- Location: `vue-counter/src/router/index.js`
- Pattern: Vue Router 4 with hash history
- Guards: `beforeEach` checks auth token from Pinia store

### `uni-app-demo/` (Cross-Platform)

**Presentation Layer:**
- Location: `uni-app-demo/pages/*`
- Contains: `pages/index/index.vue`, `pages/todo/list.vue`, `pages/todo/detail.vue`

**Mock Layer:**
- Location: `uni-app-demo/mock/`
- Contains: `index.js` (interceptor setup via `uni.addInterceptor`), `api.js`
- Pattern: Intercepts `uni.request` calls and routes to local mock API

### `docs/` (Standalone Exercises)

**Teaching Materials:**
- Location: `docs/`
- Contains: ESM module exercises (`utils.mjs`, `main.mjs`), HTML demos (`day1-morning.html`, `day1-my-card.html`, `day2-css-practice.html`), JS exercises (`day1-js-exercises.js`)
- Pattern: Standalone HTML/JS files without build step

## Data Flow

### Primary Request Path (vue-counter)

1. **UI Layer** (`vue-counter/src/views/TodoList.vue`) calls `useTodosStore().loadFromApi()`
2. **Store** (`vue-counter/src/stores/todos.js`) calls `todosApi.fetchTodos()`
3. **API Layer** (`vue-counter/src/api/todos.js`) calls `http.get('/todos')`
4. **Axios** (`vue-counter/src/api/http.js`) injects `Authorization` header via request interceptor
5. **Backend** returns `ApiResponse { code, data, message }`
6. **Response interceptor** unwraps `data` and returns it to store
7. **Store** maps backend fields (`title`/`priority int`) to frontend fields (`text`/`priority string`)
8. **UI** re-renders via Vue reactivity

### Auth Flow

1. User submits credentials on `Login.vue`
2. `useAuthStore().login()` calls `authApi.login()`
3. Backend returns `{ token, userId, username }`
4. Store saves to Pinia (`token`, `user`) + localStorage
5. Route guard reads from store, allows access to protected routes

### uni-app Mock Flow

1. `pages/todo/list.vue` calls `uni.request({ url: '/todos' })`
2. `uni.addInterceptor` in `mock/index.js` intercepts the request
3. Routes to `mock/api.js` functions, returns mock data
4. Page uses returned data directly

## Key Abstractions

**Pinia Store (State Container):**
- Purpose: Single source of truth for application state
- Examples: `useAuthStore`, `useTodosStore`
- Pattern: Composition API style with `defineStore`

**Axios HTTP Client:**
- Purpose: Centralized HTTP with interceptors
- Examples: `http.js`
- Pattern: Singleton instance with request/response interceptors

**uni-app Interceptor:**
- Purpose: Redirect API calls to mock or real backend
- Examples: `uni-app-demo/mock/index.js`
- Pattern: `uni.addInterceptor('request', { invoke(args) {...} })`

**Field Mapper:**
- Purpose: Transform between API and UI field names
- Examples: `backToFrontPriority()`, `frontToBackPriority()` in `stores/todos.js`
- Pattern: Bidirectional mapping functions

## Entry Points

**`vue-counter/src/main.js`:**
- Triggers: `npm run dev` or `npm run build`
- Responsibilities: Creates Vue app, registers Element Plus, Pinia, Vue Router, global error handler
- Mounts to: `<div id="app">`

**`uni-app-demo/main.js`:**
- Triggers: `npm run dev:%PLATFORM%` (mp-weixin, h5, etc.)
- Responsibilities: Creates SSR app, calls `setupMock()`, exports `createApp()`
- Mounts to: `App.vue` component

**`docs/main.mjs`:**
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

**What happens:** Field mapping (`title` vs `text`, `priority` int vs string) scattered across store actions
**Why it's wrong:** Mixes business logic with data transformation; makes API contract changes painful
**Do this instead:** Create a dedicated `transformers/` or `mappers/` layer, or use computed properties

### localStorage in API Layer

**What happens:** `http.js` reads `localStorage.getItem('token')` directly instead of using Pinia store
**Why it's wrong:** Multiple sources of truth for auth state; store already syncs localStorage on login/logout
**Do this instead:** Inject token via Pinia store in the request interceptor

### Hardcoded API URLs

**What happens:** `http.js` has hardcoded `baseURL: 'https://siwei7905.cloud:8443/api'`
**Why it's wrong:** Dev/prod switching requires code changes
**Do this instead:** Use `.env` files with `VITE_API_BASE_URL`

## Error Handling

**Strategy:** Centralized in Axios response interceptor + Vue global error handler

**Patterns:**
- HTTP errors (401, 5xx): Axios interceptor shows `ElMessage.error`, redirects on 401
- Network errors (`Failed to fetch`): Global error handler in `main.js` catches and displays friendly message
- Backend errors (business code != 200): Response interceptor rejects with message

## Cross-Cutting Concerns

**Logging:** `console.error` for errors, `console.log` for debug output
**Validation:** Element Plus form validation (`el-form` with `rules` prop) on client side
**Authentication:** Token stored in Pinia + localStorage, read by Axios interceptor and router guard

---

*Architecture analysis: 2026-09-29*
