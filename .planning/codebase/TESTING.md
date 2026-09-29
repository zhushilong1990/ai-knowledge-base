# Testing Patterns

**Analysis Date:** 2026-09-29

## Test Framework

**Status:** Not configured

This is a learning/demo project. Per project guidelines (`CLAUDE.md` Section 8):
- "单元测试（中小公司基本不要求）" — Unit tests are not required for this learning project

**No test framework detected:**
- No Jest, Vitest, or other test runner configured
- No `*.test.*` or `*.spec.*` files found
- No test scripts in `package.json`
- No `jest.config.*`, `vitest.config.*`, or similar

**Run Commands:** Not applicable

## Test File Organization

**Location:** N/A - No tests exist in this project

**Naming:** N/A

**Structure:** N/A

## Test Structure

**Suite Organization:** N/A

**Patterns:** N/A

## Mocking

**Framework:** N/A - No mocking framework used

**Mock Data:**
- Mock API exists at `vue-counter/src/mock/api.js`
- Uses in-memory JavaScript objects (`mockDB`)
- Simulates network delay with `setTimeout`
- Conditional import pattern:
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

**Mock Implementation** (`src/mock/api.js`):
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

## Fixtures and Factories

**Test Data:** N/A - No test fixtures

**Location:** N/A

## Coverage

**Requirements:** None enforced

**View Coverage:** N/A

## Test Types

**Unit Tests:** N/A - Not used in this project

**Integration Tests:** N/A - Not used in this project

**E2E Tests:** N/A - Not used in this project

## Common Patterns

**Async Testing:** N/A

**Error Testing:** N/A

## Manual Verification Approach

Since automated tests are not used, functionality is verified through:

1. **Dev server testing:** `npm run dev` + browser verification
2. **Vue DevTools:** Inspect component state, props, Pinia store
3. **Network tab:** Verify API requests/responses
4. **Console:** Check for JavaScript errors

## Mock API Pattern

The mock API (`src/mock/api.js`) serves as a substitute for automated tests:

```javascript
// Mock returns consistent structure
return {
  code: 200,
  data: { ... }
}

// Error simulation via code field
return { code: 401, message: '用户名或密码错误' }
```

**Mock DB Operations:**
- `login(username, password)` - Returns token for admin/123456
- `fetchTodos()` - Returns all todos
- `createTodo({ text, priority })` - Adds to mockDB
- `updateTodo(id, patch)` - Updates existing todo
- `deleteTodo(id)` - Removes from mockDB

---

*Testing analysis: 2026-09-29*
