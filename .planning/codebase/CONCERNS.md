# 代码库问题

**分析日期：** 2026-09-29

## 技术债务

**Mock API 文件仍存在（死代码）：**
- 问题：`vue-counter/src/mock/api.js` 存在，但 `api/auth.js` 和 `api/todos.js` 中 `USE_MOCK = false`。Mock 文件在生产中从未被加载。
- 文件：`vue-counter/src/mock/api.js`
- 影响：增加包体积的无效代码，造成困扰（不清楚哪个 API 路径是实际的）
- 修复方案：当 mock 被完全替代时删除 `vue-counter/src/mock/api.js`。仅在 `USE_MOCK` 重新启用时才作为离线备选保留。

**分页总数 Bug：**
- 问题：`stores/todos.js` 第 69 行，获取并映射后端数据后，`total.value = todos.length` 将总数设为映射后数组长度（前端字段名），而不是原始后端数量。当后端返回少于 `pageSize` 的条目时，分页会显示错误的总数。
- 文件：`vue-counter/src/stores/todos.js:69`
- 影响：分页可能显示错误的总数；"回到第 1 页"链接出现不正确。
- 修复方案：后端目前不返回总数（`list()` 返回所有 todos）。方案：(a) 后端添加计数端点，或 (b) 前端在映射前用 `raw.length` 跟踪总数。

**分页数据上的重复过滤逻辑：**
- 问题：`TodoList.vue` 的 `sortedFilteredTodos` 过滤 `store.paginatedTodos`（已切片到当前页），所以按"待办"/"已完成"标签过滤发生在分页之后。这意味着标签过滤器只在当前页内有效，而非全量数据。
- 文件：`vue-counter/src/views/TodoList.vue:52-60`
- 影响：如果用户在第 2 页，切换到"已完成"标签，看到空页面，即使第 1 页有已完成的 todos。
- 修复方案：先过滤 `store.todos`（完整数据集），再对过滤结果应用分页。在 store 中添加 `filteredTodos` getter。

**统计图表无真实后端数据：**
- 问题：`Stats.vue` 通过过滤 `store.todos`（当前内存列表）中 `updatedAt` 匹配最近日期的条目来计算"过去 7 天完成趋势"。但后端无历史追踪，且 `store.todos` 只包含当前 todos，不含历史完成事件。
- 文件：`vue-counter/src/views/Stats.vue:16-36`
- 影响：图表总是显示 0 或非常低的数量，除非用户恰好在过去 7 天完成了 todos 且它们仍在列表中。不是真正的趋势图。
- 修复方案：后端需要 `/api/stats/trend` 端点聚合完成历史，或接受这作为前端仅的可视化占位符。

**Vite Config 中硬编码后端 IP：**
- 问题：`vite.config.js` 代理目标硬编码为 `http://123.207.69.23:8080`。按设计是有意的，但造成脆弱依赖。
- 文件：`vue-counter/vite.config.js:17`
- 影响：如果后端 IP 变化，本地开发会挂。无环境变量备选。
- 修复方案：从环境变量读取（`process.env.VITE_API_TARGET`），开发时回退到 localhost。

---

## 已知 Bug

**统计图表首次加载为空：**
- 症状：`Stats.vue` 在 `store.todos.length === 0` 时显示"暂无 todo 数据"，但此条件可能在 `loadFromApi()` 完成前就通过了。
- 文件：`vue-counter/src/views/Stats.vue:100-102`
- 触发：导航到 /stats 时 todos 数据尚未加载。
- 临时方案：`onMounted` 在 `todos.length === 0` 时调用 `loadFromApi()`，但 `v-if="!store.loading && store.todos.length === 0"` 在异步加载完成前就渲染了。

**清空已完成用的是本地状态，而非后端：**
- 症状：`TodoList.vue` 的 `clearCompleted()` 函数（第 106-116 行）遍历 `store.todos` 并逐个调用 `deleteViaApi()`。但 `store.clearCompleted()`（用于统计展示）是纯本地操作，**不**调用 API。
- 文件：`vue-counter/src/stores/todos.js:120-124` vs `vue-counter/src/views/TodoList.vue:106-116`
- 影响：UI 中的"清空已完成"按钮用 API 循环，但 `doneCount` 计算仍引用本地状态。如果循环中一次删除失败，UI 状态会不一致。

---

## 安全考虑

**JWT 密钥硬编码在源码中：**
- 风险：`application.yml` 包含明文 JWT 密钥。如果仓库公开或 jar 被反编译，密钥暴露。
- 文件：`backend/src/main/resources/application.yml:14`
- 当前缓解：演示不是生产系统；密钥带随机后缀避免与常见演示密钥撞车。
- 建议：任何真实部署都移到环境变量（`${APP_JWT_SECRET}`）。

**生产环境 CORS 允许所有来源：**
- 风险：`CorsConfig.java` 使用 `allowedOriginPatterns("*")`。任何网站在凭证泄露时可代表用户向后端 API 发起请求。
- 文件：`backend/src/main/java/com/example/todobackend/config/CorsConfig.java:22`
- 当前缓解：无（生产用通配符）。
- 建议：生产环境限制到特定域名（如 `https://zhushilong1990.github.io`、`https://siwei7905.cloud`）。

**认证端点无速率限制：**
- 风险：`/api/auth/login` 无速率限制。攻击者可以暴力破解凭证。
- 文件：`backend/src/main/java/com/example/todobackend/controller/AuthController.java`
- 影响：演示凭证（admin/123456）很容易被猜到。
- 建议：添加简单速率限制过滤器或使用 Bucket4j 等库。

**无密码哈希：**
- 风险：认证服务用明文字符串比较验证密码。
- 文件：`backend/src/main/java/com/example/todobackend/service/AuthService.java`
- 影响：任何数据库泄露都会暴露明文密码。演示用简单比较处理 `admin/123456`。
- 建议：生产环境使用 BCrypt 或 Argon2。

---

## 性能瓶颈

**统计图表每次数据变化重新渲染整个列表：**
- 问题：`watch(trendData, renderChart)` 每次任何 todo 变化都重新渲染 ECharts 实例，即使是不相关的变化（如切换另一个 todo）。
- 文件：`vue-counter/src/views/Stats.vue:70`
- 原因：`trendData` 是计算属性，每次渲染都遍历所有 `store.todos`。
- 改进方向：对趋势计算结果 memoize，或仅在 `store.todos` 结构变化时重算。

**分页触发全量 API 重新加载：**
- 问题：`stores/todos.js` 中 `setPageSize()` 只改变 `pageSize` 和 `currentPage`，但前端在本地对数据进行分页（从后端获取所有 todos 后在本地切片）。改变页面大小不会减少网络负载。
- 文件：`vue-counter/src/stores/todos.js:131-134`
- 原因：后端无分页 API（`list()` 返回所有 todos）。
- 改进方向：后端应支持 `?page=1&size=10` 查询参数；前端应发送这些参数以减少负载。

**路由变化时 ECharts 实例从不销毁：**
- 问题：如果用户从统计页导航到其他页面再回来，`chartInstance` 可能被重建而不是复用。`onUnmounted` 清理仅在用户离开页面时运行，而非同一组件内的路由变化。
- 文件：`vue-counter/src/views/Stats.vue:72-77`
- 原因：Vue Router 在嵌套路由变化时不卸载组件；`onUnmounted` 不会被调用。
- 改进方向：使用 `onBeforeUnmount`，或在调用 `dispose()` 前检查 `chartInstance` 是否存在。

---

## 脆弱区域

**腾讯云上 Spring Boot 进程未守护进程化：**
- 为什么脆弱：后端以前台进程运行，通过 SSH 手动启动。服务器重启或进程崩溃会导致 API 停止，无自动重启。
- 文件：服务器端 `/home/ubuntu/todo-backend-0.0.1-SNAPSHOT.jar`
- 安全修改：创建 systemd 服务文件或使用 `nohup java -jar ... &` 加监控工具。在 HANDOFF.md 中记录恢复步骤。
- 测试覆盖：每次服务器重启后需要手动验证。

**Vite HMR + Pinia Store 状态持久化：**
- 为什么脆弱：Vite HMR 并不总是正确重置 Pinia store 状态。某些热重载后，store 似乎持有过期数据，即使 `loadFromApi()` 已运行。
- 文件：`vue-counter/src/stores/todos.js`
- 安全修改：重启 `npm run dev` 并清除浏览器站点数据（Ctrl+Shift+Delete）当状态看起来不一致时。这是已知的 Vite + Pinia 交互问题。
- 测试覆盖：在当前设置下无法可靠测试。

**H5 开发服务器代理无法代理到 HTTPS 后端：**
- 为什么脆弱：`vite.config.js` 代理目标使用 `http://123.207.69.23:8080`。生产后端在 443 端口是 HTTPS，但此 Vite 版本开发代理无法在不额外配置的情况下代理到 HTTPS 端点。
- 文件：`vue-counter/vite.config.js:14-17`
- 影响：生产构建直接使用 `https://siwei7905.cloud:8443/api`（无代理），但开发用硬编码 IP。开发版和生产版走不同的代码路径。
- 安全修改：后端 HTTPS 正确配置后，更新开发代理使用 HTTPS 目标。

**uni-app 演示未完成的后端集成：**
- 为什么脆弱：`uni-app-demo` 在 D16 更新为使用 `uni.request`，但未在实际微信小程序或 Android App 运行时测试过。只验证了 H5。
- 文件：`uni-app-demo/pages/todo/list.vue`、`uni-app-demo/pages/todo/detail.vue`
- 安全修改：各平台使用真实后端分别测试，然后才能声称全栈能力。
- 测试覆盖：仅手动；无自动化测试。

---

## 规模限制

**内存 Todo 列表（无数据库持久化）：**
- 当前容量：后端使用内存 `ConcurrentHashMap`，未配置 H2 持久化。每次 Spring Boot 重启会清除所有 todos。
- 限制：重启后数据丢失。无法扩展到多个后端实例（无共享状态）。
- 扩展路径：配置 H2 文件存储（`spring.datasource.url=jdbc:h2:file:./data/todo`）或迁移到 MySQL/PostgreSQL。

**后端无分页：**
- 当前容量：`GET /api/todos` 一次返回所有 todos。1000+ todos 时，这是很大的 JSON payload。
- 限制：前端性能下降；移动数据传输浪费。
- 扩展路径：后端实现 `Pageable` Spring Data 接口；前端发送 page/size 参数。

**JWT 不可撤销：**
- 当前容量：JWT Token 是无状态的。登出只是从客户端移除 token。如果 token 被盗，在过期前（7 天）仍有效。
- 限制：安全事件时无 token 撤销。
- 扩展路径：在 Redis 中实现 token 黑名单，或使用更短过期时间（1 小时）加刷新 token。

---

## 有风险的依赖

**jjwt 0.11.5（JDK 8 兼容最后版本）：**
- 风险：jjwt 0.11.5 是支持 JDK 8 的最后版本。不会有安全补丁回移植。最新版本是 0.12.x。
- 影响：发现新 JWT 漏洞将不会在 0.11.5 中修复。
- 迁移计划：升级到 jjwt 0.12.x 需要 JDK 11+。由于目标是 JDK 8 演示兼容性，保持 0.11.5 并记录此限制。

**Element Plus 2.14.6（旧 minor 版本）：**
- 风险：落后几个 minor 版本。安全补丁可能缺失。
- 影响：对演示项目较低；UI 库无直接服务端攻击面。
- 迁移计划：准备好时 `npm update element-plus`。

**Vue Router 4.6.4（旧 minor 版本）：**
- 风险：minor 版本落后；无已知重大安全问题。
- 影响：低。
- 迁移计划：准备好时 `npm update vue-router`。

---

## 缺失的关键功能

**无后端健康检查端点：**
- 问题：无 `/api/health` 或 `/actuator/health` 端点来验证后端运行状态。
- 阻碍：自动化监控、负载均衡器健康检查、CI/CD 流水线验证。
- 缺失位置：`backend/src/main/java/com/example/todobackend/controller/`

**无登录注册流程：**
- 问题：无用户注册端点。只有演示凭证可用。
- 阻碍：真实多用户使用 API。
- 缺失位置：`backend/src/main/java/com/example/todobackend/controller/`

**Todo 标题无输入清理：**
- 问题：`TodoRequest` 有 `@Valid` 但无明确长度或字符限制。极长的标题可能导致展示问题。
- 阻碍：恶意输入的安全处理。
- 缺失位置：`backend/src/main/java/com/example/todobackend/dto/TodoRequest.java`

---

## 测试覆盖缺口

**前端无单元测试：**
- 未测试：所有 Vue 组件、Pinia stores、API 模块和路由守卫零单元测试。
- 文件：`vue-counter/src/**/*.js`、`vue-counter/src/**/*.vue`
- 风险：任何重构或依赖更新都可能静默破坏功能。
- 优先级：对演示/学习项目低（按 CLAUDE.md 第 8 节："单元测试（中小公司基本不要求）"）。

**后端无集成测试：**
- 未测试：控制器端点、JWT 拦截器、CORS 配置、服务层逻辑。
- 文件：`backend/src/main/java/com/example/todobackend/**/*.java`
- 风险：业务逻辑变更（如 JWT claims、priority 枚举）无回归保护。
- 优先级：对演示项目低，但生产使用前应添加。

**无 E2E 测试：**
- 未测试：完整用户流程如"登录 → 创建 todo → 切换 → 删除"。
- 风险：前端和后端集成可能静默中断而未被检测。
- 优先级：对演示/学习项目低。

---

*问题审计：2026-09-29*
