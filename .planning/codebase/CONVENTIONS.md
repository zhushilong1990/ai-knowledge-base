# Coding Conventions

**Analysis Date:** 2026-09-29

## Naming Patterns

**Files:**
- Vue components: PascalCase `.vue` files (e.g., `TodoList.vue`, `TodoDeleteConfirm.vue`)
- JavaScript modules: camelCase (e.g., `http.js`, `auth.js`)
- Directories: camelCase or kebab-case (e.g., `stores/`, `api/`, `mock/`)

**Functions:**
- camelCase for all JavaScript functions (e.g., `handleLogin`, `loadFromApi`)
- Event handlers prefixed with `handle` (e.g., `handleLogout`, `handleLogin`)
- Actions async-prefixed when they call APIs (e.g., `createViaApi`, `deleteViaApi`)

**Variables:**
- camelCase (e.g., `newTodo`, `currentPage`, `currentPriority`)
- Constants in camelCase or UPPER_SNAKE_CASE for truly constant values
- Boolean variables prefixed with `is`, `has`, `can`, or `show` (e.g., `isLoggedIn`, `canAdd`, `showUser`)

**Types:**
- Not explicitly defined (no TypeScript in this project)
- Props use Object notation with `type` and `required` keys

## Code Style

**Formatting:**
- Tool used: None detected (no Prettier, ESLint, or Biome config files)
- Manual formatting with consistent 2-space indentation
- Template literals for string interpolation

**Linting:**
- Tool used: None detected
- No `.eslintrc`, `.prettierrc`, or `eslint.config.*` files found

**Vue SFC Structure:**
```vue
<script setup>
// Imports first
import { ref, computed } from 'vue'

// Props and emits
const props = defineProps({ ... })
const emit = defineEmits(['event'])

// Reactive state
const state = ref(initialValue)

// Computed
const derived = computed(() => { ... })

// Methods
function handler() { ... }

// Lifecycle
onMounted(() => { ... })
</script>

<template>
  <!-- HTML with Vue directives -->
</template>

<style scoped>
/* Component-scoped styles */
</style>
```

## Import Organization

**Order:**
1. Vue core imports (`ref`, `computed`, `onMounted`, etc.)
2. Vue Router imports (`useRouter`, `useRoute`)
3. Pinia store imports (`useAuthStore`, `useTodosStore`)
4. UI library imports (Element Plus: `ElMessage`, `ElButton`, etc.)
5. Local component imports (`./components/`, `../stores/`, `../api/`)
6. Mock imports (conditional, inside functions)

**Path Aliases:**
- No path aliases configured
- Relative paths used throughout (`../stores/`, `./components/`)

## Error Handling

**Patterns:**

1. **Try-catch in async functions:**
```javascript
async function loadFromApi() {
  loading.value = true
  error.value = null
  try {
    const raw = await todosApi.fetchTodos()
    todos.value = raw.map(fromBackend)
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}
```

2. **Axios interceptors for API errors** (`src/api/http.js`):
```javascript
http.interceptors.response.use(
  (response) => {
    const { code, data, message } = response.data
    if (code === 200) return data
    if (code === 401) {
      localStorage.removeItem('token')
      ElMessage.error(message || '请重新登录')
      window.location.hash = '#/login'
    }
    return Promise.reject(new Error(message || '请求失败'))
  },
  (error) => {
    if (error.response?.status === 401) { ... }
    return Promise.reject(error)
  }
)
```

3. **Vue global error handler** (`src/main.js`):
```javascript
app.config.errorHandler = (err, instance, info) => {
  console.error('[Vue Global Error]', err, info)
  const msg = err?.message || '页面出错了'
  if (msg.includes('Failed to fetch') || msg.includes('Network')) {
    ElMessage.error('网络错误：请确认 Spring Boot 后端是否在 8080 运行')
  } else {
    ElMessage.error(`页面错误：${msg}`)
  }
}
```

## Logging

**Framework:** console.log/console.error (no logging library)

**Patterns:**
- Console only for development debugging
- Comments document intent (Java comparisons in Chinese)
- No runtime logging for production

## Comments

**When to Comment:**
- Explain Java Spring Boot对照 for Java developers learning Vue
- Document API integration points
- Explain business logic transformations (field mapping, etc.)

**Examples:**
```javascript
// D7 新建：Pinia 认证 store
// Java 对照：类似 Spring Security 的 SecurityContextHolder，跨组件共享登录态
```

**JSDoc/TSDoc:**
- Not used (no TypeScript)

## Function Design

**Size:** Keep under 30 lines; single responsibility per function

**Parameters:**
- Props via `defineProps` object notation
- Function parameters explicit and named
- Maximum 3-4 parameters before grouping into object

**Return Values:**
- Async functions return Promises
- Computed properties for derived state
- Store actions return necessary data

## Module Design

**Exports:**
- Named exports: `export function fetchTodos() { ... }`
- Store exports: `export const useTodosStore = defineStore(...)`

**Barrel Files:**
- No barrel files (index.js re-exports not used)
- Direct imports to specific modules

## Component Patterns

**Props:**
```javascript
defineProps({
  todo: { type: Object, required: true },
  errorMsg: { type: String, default: '' },
  canAdd: { type: Boolean, default: false },
  priorities: { type: Array, default: () => ['高', '中', '低'] }
})
```

**Emits:**
```javascript
const emit = defineEmits(['toggle', 'delete', 'view'])
```

**v-model:**
```javascript
// In child component
const props = defineProps({ modelValue: String })
const emit = defineEmits(['update:modelValue'])
const text = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})
```

## State Management (Pinia)

**Store Structure:**
```javascript
export const useTodosStore = defineStore('todos', () => {
  // State
  const todos = ref([])
  const loading = ref(false)

  // Getters
  const doneCount = computed(() => todos.value.filter(t => t.done).length)

  // Actions
  async function loadFromApi() { ... }

  return { todos, loading, doneCount, loadFromApi, ... }
})
```

---

*Convention analysis: 2026-09-29*
