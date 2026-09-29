# Codebase Structure

**Analysis Date:** 2026-09-29

## Directory Layout

```
D:\code\fullstack-learning\
├── CLAUDE.md              # Project instructions and roadmap
├── HANDOFF.md             # Cross-session progress handoff
├── docs\                  # Teaching materials and exercises
│   ├── main.mjs           # ESM module demo entry
│   ├── utils.mjs          # ESM utility functions
│   ├── day1-*.mjs         # Day 1 exercises
│   ├── day1-*.html        # Day 1 HTML demos
│   └── day2-*.html        # Day 2 HTML demos
├── vue-counter\           # Main Vue 3 SPA demo (Week 1-2)
│   ├── index.html         # HTML entry point
│   ├── package.json       # Dependencies and scripts
│   ├── vite.config.js      # Vite configuration with proxy
│   └── src\
│       ├── main.js         # Vue app initialization
│       ├── App.vue         # Root component with nav
│       ├── router\
│       │   └── index.js    # Vue Router config + guards
│       ├── stores\
│       │   ├── auth.js     # Auth state (Pinia)
│       │   └── todos.js    # Todos state + field mapping
│       ├── api\
│       │   ├── http.js     # Axios instance + interceptors
│       │   ├── auth.js     # Auth API calls
│       │   └── todos.js    # Todos API calls
│       ├── mock\
│       │   └── api.js      # Mock API for offline dev
│       ├── views\
│       │   ├── Home.vue        # Landing page
│       │   ├── Login.vue       # Login page
│       │   ├── TodoList.vue    # List with pagination
│       │   ├── TodoDetail.vue  # Detail view
│       │   ├── TodoFormPage.vue # Create/edit form
│       │   ├── Stats.vue       # ECharts visualization
│       │   └── NotFound.vue    # 404 page
│       └── components\
│           ├── TodoItem.vue         # Single todo row
│           ├── TodoForm.vue         # Todo form component
│           ├── TodoStats.vue        # Stats display
│           └── TodoDeleteConfirm.vue # Delete confirmation
└── uni-app-demo\            # Cross-platform demo (Week 2+)
    ├── index.html           # HTML entry (not used in uni-app)
    ├── main.js              # uni-app entry point
    ├── App.vue              # Root component
    ├── manifest.json        # App configuration
    ├── pages.json           # Page routing config
    ├── mock\
    │   ├── index.js         # Mock interceptor setup
    │   └── api.js           # Mock API functions
    └── pages\
        ├── index\
        │   └── index.vue    # Home page
        └── todo\
            ├── list.vue     # Todo list page
            └── detail.vue   # Todo detail page
```

## Directory Purposes

**`vue-counter/`:**
- Purpose: Primary learning demo - full Vue 3 SPA with routing, state, API integration
- Contains: Vite project with Vue 3, Element Plus, Pinia, Vue Router, Axios, ECharts
- Key files: `src/main.js`, `src/router/index.js`, `src/stores/*.js`, `src/api/*.js`

**`uni-app-demo/`:**
- Purpose: Cross-platform variant demonstrating H5 + WeChat Mini Program + App from single codebase
- Contains: uni-app project structure with `pages.json` routing, `manifest.json` platform config
- Key files: `main.js`, `pages.json`, `mock/index.js`

**`docs/`:**
- Purpose: Standalone teaching exercises without build step
- Contains: ESM `.mjs` files, HTML demos, JavaScript exercises
- Key files: `main.mjs`, `utils.mjs`, `day1-*.html`

## Key File Locations

**Entry Points:**
- `vue-counter/index.html`: HTML shell that mounts Vue app (`<div id="app">`)
- `vue-counter/src/main.js`: Vue app initialization (registers plugins, mounts)
- `uni-app-demo/main.js`: uni-app bootstrap (creates SSR app, sets up mock)

**Configuration:**
- `vue-counter/vite.config.js`: Vite config with `/api` proxy to backend
- `vue-counter/package.json`: Vue 3, Element Plus, Pinia, Vue Router, Axios, ECharts
- `uni-app-demo/manifest.json`: Platform-specific configs (WeChat appid, permissions)
- `uni-app-demo/pages.json`: Page routes and navigation bar titles

**Core Logic:**
- `vue-counter/src/stores/auth.js`: Auth store (token, user, login, logout)
- `vue-counter/src/stores/todos.js`: Todos store (CRUD, pagination, field mapping)
- `vue-counter/src/api/http.js`: Axios instance with interceptors
- `vue-counter/src/router/index.js`: Router with auth guard

**Testing:**
- No formal test directory - learning demos with manual L1-L4 verification

## Naming Conventions

**Files:**
- Vue components: PascalCase (`TodoList.vue`, `TodoDetail.vue`)
- JavaScript modules: camelCase (`auth.js`, `todos.js`, `http.js`)
- Page directories (uni-app): lowercase (`pages/todo/list.vue`)

**Directories:**
- `src/stores/`: Pinia stores
- `src/api/`: API modules
- `src/views/`: Page components
- `src/components/`: Reusable components
- `pages/`: uni-app page components
- `mock/`: Mock API modules

**Variables/Functions:**
- camelCase: `fetchTodos`, `createTodo`, `loadFromApi`
- Store actions: camelCase verbs: `login`, `logout`, `loadFromApi`, `setPage`
- Store state: camelCase nouns: `token`, `user`, `todos`, `currentPage`

## Where to Add New Code

**New Feature (vue-counter):**
- Page component: `vue-counter/src/views/NewFeature.vue`
- Reusable component: `vue-counter/src/components/NewFeatureItem.vue`
- Store (if needed): `vue-counter/src/stores/newFeature.js`
- API (if needed): `vue-counter/src/api/newFeature.js`
- Route: Add to `vue-counter/src/router/index.js` routes array

**New Component/Module:**
- Implementation: Create in appropriate `src/` subdirectory
- For shared utilities: Add to existing file or create new module in `src/` root

**Utilities:**
- Shared helpers: Create in `vue-counter/src/utils/` (not currently present)
- ESM utilities: Add to `docs/utils.mjs`

## Special Directories

**`vue-counter/node_modules/`:**
- Purpose: npm dependencies (not committed per .gitignore)
- Generated: Yes (via `npm install`)
- Committed: No

**`uni-app-demo/unpackage/`:**
- Purpose: Build output for each platform
- Generated: Yes (via `npm run dev:mp-weixin` etc.)
- Committed: No

**`.claude/`:**
- Purpose: Claude Code settings and memory
- Contains: `settings.local.json`, `memory/MEMORY.md`
- Generated: Claude Code creates automatically

---

*Structure analysis: 2026-09-29*
