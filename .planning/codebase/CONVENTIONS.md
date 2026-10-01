# 编码规范

**分析日期：** 2026-09-29

## 命名规范

**文件：**
- Vue 组件：PascalCase `.vue` 文件（如 `TodoList.vue`、`TodoDeleteConfirm.vue`）
- JavaScript 模块：camelCase（如 `http.js`、`auth.js`）
- 目录：camelCase 或 kebab-case（如 `stores/`、`api/`、`mock/`）

**函数：**
- 所有 JavaScript 函数使用 camelCase（如 `handleLogin`、`loadFromApi`）
- 事件处理函数以 `handle` 开头（如 `handleLogout`、`handleLogin`）
- 调用 API 的异步操作以 `async` 开头（如 `createViaApi`、`deleteViaApi`）

**变量：**
- camelCase（如 `newTodo`、`currentPage`、`currentPriority`）
- 常量使用 camelCase 或 UPPER_SNAKE_CASE（真正不变的常量）
- 布尔变量以 `is`、`has`、`can` 或 `show` 开头（如 `isLoggedIn`、`canAdd`、`showUser`）

**类型：**
- 未明确定义（项目未使用 TypeScript）
- Props 使用对象写法，附带 `type` 和 `required` 键

## 代码风格

**格式化：**
- 未检测到格式化工具（无 Prettier、ESLint、Biome 配置文件）
- 统一 2 空格缩进，手动格式化
- 模板字符串用于字符串插值

**代码检查：**
- 未检测到代码检查工具
- 无 `.eslintrc`、`.prettierrc` 或 `eslint.config.*` 文件

**Vue SFC 结构：**
```vue
<script setup>
// 先写 import
import { ref, computed } from 'vue'

// Props 和 emits
const props = defineProps({ ... })
const emit = defineEmits(['event'])

// 响应式状态
const state = ref(initialValue)

// 计算属性
const derived = computed(() => { ... })

// 方法
function handler() { ... }

// 生命周期
onMounted(() => { ... })
</script>

<template>
  <!-- HTML + Vue 指令 -->
</template>

<style scoped>
/* 组件级样式 */
</style>
```

## Import 组织顺序

1. Vue 核心 import（`ref`、`computed`、`onMounted` 等）
2. Vue Router import（`useRouter`、`useRoute`）
3. Pinia store import（`useAuthStore`、`useTodosStore`）
4. UI 库 import（Element Plus：`ElMessage`、`ElButton` 等）
5. 本地组件 import（`./components/`、`../stores/`、`../api/`）
6. Mock import（条件引入，函数内部使用）

**路径别名：**
- 未配置路径别名
- 全部使用相对路径（`../stores/`、`./components/`）

## 错误处理

**模式：**

1. **异步函数中的 try-catch：**
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

2. **Axios 拦截器处理 API 错误**（`src/api/http.js`）：
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

3. **Vue 全局错误处理器**（`src/main.js`）：
```javascript
app.config.errorHandler = (err, instance, info) => {
  console.error('[Vue 全局错误]', err, info)
  const msg = err?.message || '页面出错了'
  if (msg.includes('Failed to fetch') || msg.includes('Network')) {
    ElMessage.error('网络错误：请确认 Spring Boot 后端是否在 8080 运行')
  } else {
    ElMessage.error(`页面错误：${msg}`)
  }
}
```

## 日志

**框架：** console.log/console.error（无日志库）

**模式：**
- 仅开发调试用 console
- 注释说明意图（Java 对照，供学习 Vue 的 Java 开发者参考）
- 生产环境无运行时日志

## 注释

**何时写注释：**
- 解释 Java Spring Boot 对照，供 Java 开发者学习 Vue
- 记录 API 集成点
- 解释业务逻辑转换（字段映射等）

**示例：**
```javascript
// D7 新建：Pinia 认证 store
// Java 对照：类似 Spring Security 的 SecurityContextHolder，跨组件共享登录态
```

**JSDoc/TSDoc：**
- 未使用（无 TypeScript）

## 函数设计

**大小：** 控制在 30 行以内；每个函数单一职责

**参数：**
- Props 通过 `defineProps` 对象写法
- 函数参数显式命名
- 超过 3-4 个参数时封装为对象

**返回值：**
- 异步函数返回 Promise
- 计算属性用于派生状态
- Store actions 返回必要数据

## 模块设计

**导出：**
- 命名导出：`export function fetchTodos() { ... }`
- Store 导出：`export const useTodosStore = defineStore(...)`

**Barrel 文件：**
- 不使用 barrel 文件（不使用 index.js 重新导出）
- 直接 import 具体模块

## 组件模式

**Props：**
```javascript
defineProps({
  todo: { type: Object, required: true },
  errorMsg: { type: String, default: '' },
  canAdd: { type: Boolean, default: false },
  priorities: { type: Array, default: () => ['高', '中', '低'] }
})
```

**Emits：**
```javascript
const emit = defineEmits(['toggle', 'delete', 'view'])
```

**v-model：**
```javascript
// 子组件
const props = defineProps({ modelValue: String })
const emit = defineEmits(['update:modelValue'])
const text = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})
```

## 状态管理（Pinia）

**Store 结构：**
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

*规范分析：2026-09-29*
