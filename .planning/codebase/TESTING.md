# 测试模式

**分析日期：** 2026-09-29

## 测试框架

**状态：** 未配置

这是一个学习/演示项目。根据项目规范（`CLAUDE.md` 第 8 节）：
- "单元测试（中小公司基本不要求）" — 本学习项目不需要单元测试

**未检测到测试框架：**
- 未配置 Jest、Vitest 或其他测试运行器
- 未找到 `*.test.*` 或 `*.spec.*` 文件
- `package.json` 中无测试脚本
- 无 `jest.config.*`、`vitest.config.*` 或类似配置

**运行命令：** 不适用

## 测试文件组织

**位置：** N/A — 项目中不存在测试

**命名：** N/A

**结构：** N/A

## 测试结构

**套件组织：** N/A

**模式：** N/A

## Mocking

**框架：** N/A — 未使用 mock 框架

**Mock 数据：**
- Mock API 位于 `vue-counter/src/mock/api.js`
- 使用内存 JavaScript 对象（`mockDB`）
- 用 `setTimeout` 模拟网络延迟
- 条件引入模式：
```javascript
const USE_MOCK = false

export async function fetchTodos() {
  if (USE_MOCK) {
    const { default: mockApi } = await import('../mock/api')
    const res = await mockApi.fetchTodos()
    return res.data || []
  }
  return http.get('/todos')
}
```

**Mock 实现**（`src/mock/api.js`）：
```javascript
const mockDB = {
  todos: [
    { id: 1, text: '学 Vue 3 基础', done: false, priority: '中' },
    { id: 2, text: '看 Java 对照表', done: true, priority: '低' },
    { id: 3, text: '写第一个组件', done: false, priority: '高' }
  ],
  nextId: 4
}

function delay(ms = 500) {
  return new Promise(resolve => setTimeout(resolve, ms))
}
```

## Fixtures 和工厂

**测试数据：** N/A — 无测试 fixtures

**位置：** N/A

## 覆盖率

**要求：** 无强制要求

**视图覆盖率：** N/A

## 测试类型

**单元测试：** N/A — 本项目未使用

**集成测试：** N/A — 本项目未使用

**端到端测试：** N/A — 本项目未使用

## 常见模式

**异步测试：** N/A

**错误测试：** N/A

## 手动验证方式

由于未使用自动化测试，功能通过以下方式验证：

1. **开发服务器测试：** `npm run dev` + 浏览器验证
2. **Vue DevTools：** 检查组件状态、props、Pinia store
3. **Network 标签：** 验证 API 请求/响应
4. **Console：** 检查 JavaScript 错误

## Mock API 模式

Mock API（`src/mock/api.js`）作为自动化测试的替代：

```javascript
// Mock 返回一致结构
return {
  code: 200,
  data: { ... }
}

// 通过 code 字段模拟错误
return { code: 401, message: '用户名或密码错误' }
```

**Mock DB 操作：**
- `login(username, password)` — 返回 admin/123456 的 token
- `fetchTodos()` — 返回所有 todos
- `createTodo({ text, priority })` — 添加到 mockDB
- `updateTodo(id, patch)` — 更新现有 todo
- `deleteTodo(id)` — 从 mockDB 删除

---

*测试分析：2026-09-29*
