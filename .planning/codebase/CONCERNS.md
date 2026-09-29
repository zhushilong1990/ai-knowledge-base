# Codebase Concerns

**Analysis Date:** 2026-09-29

## Tech Debt

**Mock API File Still Present (Dead Code):**
- Issue: `vue-counter/src/mock/api.js` exists but `USE_MOCK = false` in both `api/auth.js` and `api/todos.js`. The mock file is never loaded in production.
- Files: `vue-counter/src/mock/api.js`
- Impact: Dead code that increases bundle size slightly and creates confusion about which API path is live.
- Fix approach: Delete `vue-counter/src/mock/api.js` when mock is fully replaced. Keep as offline fallback only if `USE_MOCK` is ever re-enabled.

**Pagination Total Count Bug:**
- Issue: In `stores/todos.js` line 69, after fetching and mapping backend data, `total.value = todos.length` sets total to the MAPPED array length (frontend field names), not the raw backend count. This breaks pagination when backend returns fewer items than `pageSize`.
- Files: `vue-counter/src/stores/todos.js:69`
- Impact: Pagination may show incorrect total count; "回到第 1 页" link appears incorrectly.
- Fix approach: Backend does not return total count currently (`list()` returns all todos). Either: (a) backend adds a count endpoint, or (b) frontend tracks total as `raw.length` before mapping.

**Duplicate Filter Logic on Paginated Data:**
- Issue: `TodoList.vue` `sortedFilteredTodos` filters `store.paginatedTodos` (already sliced to current page), so filtering by "pending"/"done" tabs happens AFTER pagination. This means tab filters only work within the current page, not across all pages.
- Files: `vue-counter/src/views/TodoList.vue:52-60`
- Impact: If user is on page 2 and switches to "已完成" tab, they see empty page even if completed todos exist on page 1.
- Fix approach: Filter `store.todos` (full dataset) first, then apply pagination to the filtered result. Add a `filteredTodos` getter to the store.

**Stats Chart Has No Real Backend Data:**
- Issue: `Stats.vue` computes "过去 7 天完成趋势" by filtering `store.todos` (current in-memory list) for `updatedAt` matching recent dates. But backend has no historical tracking and `store.todos` only contains current todos, not historical completion events.
- Files: `vue-counter/src/views/Stats.vue:16-36`
- Impact: Chart always shows 0 or very low counts unless user happened to complete todos in the past 7 days and they are still in the list. Not a real trend chart.
- Fix approach: Backend needs a `/api/stats/trend` endpoint that aggregates completion history, or accept this as a frontend-only visualization placeholder.

**Hardcoded Backend IP in Vite Config:**
- Issue: `vite.config.js` proxy target is hardcoded to `http://123.207.69.23:8080` for development. This is intentional per design but creates a fragile dependency.
- Files: `vue-counter/vite.config.js:17`
- Impact: If backend IP changes, local dev breaks. No env var fallback.
- Fix approach: Read from environment variable (`process.env.VITE_API_TARGET`) with a fallback to localhost for dev.

---

## Known Bugs

**Stats Chart Empty on Fresh Load:**
- Symptoms: `Stats.vue` shows "暂无 todo 数据" when `store.todos.length === 0`, but this condition may pass before `loadFromApi()` finishes.
- Files: `vue-counter/src/views/Stats.vue:100-102`
- Trigger: Navigate to /stats before any todo data is loaded.
- Workaround: The `onMounted` calls `loadFromApi()` if `todos.length === 0`, but the `v-if="!store.loading && store.todos.length === 0"` renders before the async load completes.

**Clear Completed Uses Local State, Not Backend:**
- Symptoms: `TodoList.vue` `clearCompleted()` function (lines 106-116) loops through `store.todos` and calls `deleteViaApi()` one by one. But `store.clearCompleted()` (used in stats display) is a local-only operation that does NOT call the API.
- Files: `vue-counter/src/stores/todos.js:120-124` vs `vue-counter/src/views/TodoList.vue:106-116`
- Impact: The "清空已完成" button in the UI uses the API loop, but the `doneCount` computed still references local state. If one delete fails in the loop, the UI state is inconsistent.

---

## Security Considerations

**JWT Secret Hardcoded in Source:**
- Risk: `application.yml` contains the JWT secret in plain text. If the repo is ever made public or the jar is decompiled, the secret is exposed.
- Files: `backend/src/main/resources/application.yml:14`
- Current mitigation: Demo is not a production system; secret has random suffix to avoid撞车 with common demo secrets.
- Recommendations: Move to environment variable (`${APP_JWT_SECRET}`) for any real deployment.

**CORS Allows All Origins in Production:**
- Risk: `CorsConfig.java` uses `allowedOriginPatterns("*")`. Any website can make requests to the backend API on behalf of users if credentials are compromised.
- Files: `backend/src/main/java/com/example/todobackend/config/CorsConfig.java:22`
- Current mitigation: None (wildcard in production).
- Recommendations: Restrict to specific domains in production (e.g., `https://zhushilong1990.github.io`, `https://siwei7905.cloud`).

**No Rate Limiting on Auth Endpoint:**
- Risk: `/api/auth/login` has no rate limiting. An attacker could brute force credentials.
- Files: `backend/src/main/java/com/example/todobackend/controller/AuthController.java`
- Impact: Demo credential (admin/123456) is trivially guessable.
- Recommendations: Add a simple rate limit filter or use a library like Bucket4j.

**No Password Hashing:**
- Risk: Auth service validates password as plain string comparison.
- Files: `backend/src/main/java/com/example/todobackend/service/AuthService.java`
- Impact: Any database leak exposes plain text passwords. Demo uses simple comparison for `admin/123456`.
- Recommendations: Use BCrypt or Argon2 for password hashing in production.

---

## Performance Bottlenecks

**Stats Chart Re-renders Entire List on Every Data Change:**
- Problem: `watch(trendData, renderChart)` re-renders the ECharts instance whenever any todo changes, even unrelated changes like toggling a different todo.
- Files: `vue-counter/src/views/Stats.vue:70`
- Cause: `trendData` is a computed that iterates ALL `store.todos` for every render.
- Improvement path: Memoize the trend calculation or only recalculate when `store.todos` changes structurally.

**Pagination Triggers Full API Reload:**
- Problem: `setPageSize()` in `stores/todos.js` only changes `pageSize` and `currentPage`, but the frontend paginates locally (it fetches ALL todos from backend and slices locally). Changing page size doesn't reduce network payload.
- Files: `vue-counter/src/stores/todos.js:131-134`
- Cause: Backend has no pagination API (`list()` returns all todos).
- Improvement path: Backend should support `?page=1&size=10` query params; frontend should send these to reduce payload.

**ECharts Instance Never Disposed on Route Change:**
- Problem: If user navigates from Stats to another page and back, `chartInstance` may be recreated instead of reused. The `onUnmounted` cleanup only runs if user leaves the page, not on route change within the same component.
- Files: `vue-counter/src/views/Stats.vue:72-77`
- Cause: Vue Router does not unmount component on nested route changes; `onUnmounted` is not called.
- Improvement path: Use `onBeforeUnmount` or check `chartInstance` exists before calling `dispose()`.

---

## Fragile Areas

**Spring Boot Process Not Daemonized on Tencent Cloud:**
- Why fragile: Backend runs as foreground process started manually via SSH. Server reboot or process crash kills the API with no auto-restart.
- Files: Server-side `/home/ubuntu/todo-backend-0.0.1-SNAPSHOT.jar`
- Safe modification: Create a systemd service file or use `nohup java -jar ... &` with a monitoring tool. Document recovery steps in HANDOFF.md.
- Test coverage: None (manual verification required after each server restart).

**Vite HMR + Pinia Store State Persistence:**
- Why fragile: Vite HMR does not always reset Pinia store state correctly. After certain hot reloads, the store appears to hold stale data even after `loadFromApi()` runs.
- Files: `vue-counter/src/stores/todos.js`
- Safe modification: Restart `npm run dev` and clear browser site data (Ctrl+Shift+Delete) if state appears inconsistent. This is a known Vite+Pina interaction issue.
- Test coverage: Cannot be reliably tested in the current setup.

**H5 Dev Server Proxy Cannot Proxy to HTTPS Backend:**
- Why fragile: `vite.config.js` proxy target uses `http://123.207.69.23:8080`. The production backend is HTTPS on port 443, but the dev proxy cannot target HTTPS endpoints in this Vite version without additional configuration.
- Files: `vue-counter/vite.config.js:14-17`
- Impact: Production build uses `https://siwei7905.cloud:8443/api` directly (no proxy), but dev uses the hardcoded IP. Different code paths for dev vs prod.
- Safe modification: When backend HTTPS is properly configured, update dev proxy to use HTTPS target.

**uni-app Demo Incomplete Backend Integration:**
- Why fragile: `uni-app-demo` was updated to use `uni.request` in D16, but has not been tested on actual WeChat miniprogram or Android App runtime. Only H5 was verified.
- Files: `uni-app-demo/pages/todo/list.vue`, `uni-app-demo/pages/todo/detail.vue`
- Safe modification: Test each platform separately with the actual backend before claiming full-stack capability.
- Test coverage: Manual only; no automated tests.

---

## Scaling Limits

**In-Memory Todo List (No Database Persistence):**
- Current capacity: Backend uses in-memory `ConcurrentHashMap` with no H2 persistence configured. Each Spring Boot restart wipes all todos.
- Limit: Data loss on restart. Cannot scale to multiple backend instances (no shared state).
- Scaling path: Configure H2 with file-based storage (`spring.datasource.url=jdbc:h2:file:./data/todo`) or migrate to MySQL/PostgreSQL.

**No Backend Pagination:**
- Current capacity: `GET /api/todos` returns ALL todos in one response. With 1000+ todos, this becomes a large JSON payload.
- Limit: Frontend performance degrades; mobile data transfer wasteful.
- Scaling path: Backend implements `Pageable` Spring Data interface; frontend sends page/size params.

**JWT Not Revocable:**
- Current capacity: JWT tokens are stateless. Logout just removes token from client. If a token is stolen, it remains valid until expiration (7 days).
- Limit: No token revocation for security incidents.
- Scaling path: Implement a token blacklist in Redis, or use shorter expiration (1 hour) with refresh tokens.

---

## Dependencies at Risk

**jjwt 0.11.5 (JDK 8 Compatibility Last Version):**
- Risk: jjwt 0.11.5 is the last version supporting JDK 8. No security patches will be backported. Current version is 0.12.x.
- Impact: Any new JWT vulnerabilities discovered will not be fixed in 0.11.5.
- Migration plan: Upgrade to jjwt 0.12.x requires JDK 11+. Since the target is JDK 8 demo compatibility, stick with 0.11.5 and document the limitation.

**Element Plus 2.14.6 (Old Minor Version):**
- Risk: Several minor versions behind current. Security patches may be missing.
- Impact: Low for a demo project; UI library with no direct server-side attack surface.
- Migration plan: `npm update element-plus` when ready.

**Vue Router 4.6.4 (Old Minor Version):**
- Risk: Minor version behind; no major security issues known.
- Impact: Low.
- Migration plan: `npm update vue-router` when ready.

---

## Missing Critical Features

**No Backend Health Check Endpoint:**
- Problem: No `/api/health` or `/actuator/health` endpoint to verify backend is running.
- Blocks: Automated monitoring, load balancer health checks, CI/CD pipeline verification.
- Missing in: `backend/src/main/java/com/example/todobackend/controller/`

**No Login Registration Flow:**
- Problem: No user registration endpoint. Only demo credentials work.
- Blocks: Real multi-user usage of the API.
- Missing in: `backend/src/main/java/com/example/todobackend/controller/`

**No Input Sanitization on Todo Title:**
- Problem: `TodoRequest` has `@Valid` but no explicit length or character restrictions. Extremely long titles could cause display issues.
- Blocks: Safe handling of malicious input.
- Missing in: `backend/src/main/java/com/example/todobackend/dto/TodoRequest.java`

---

## Test Coverage Gaps

**No Unit Tests in Frontend:**
- What's not tested: All Vue components, Pinia stores, API modules, and router guards have zero unit tests.
- Files: `vue-counter/src/**/*.js`, `vue-counter/src/**/*.vue`
- Risk: Any refactoring or dependency update could silently break functionality.
- Priority: Low for demo/learning project (per CLAUDE.md Section 8: "单元测试（中小公司基本不要求）").

**No Integration Tests in Backend:**
- What's not tested: Controller endpoints, JWT interceptor, CORS configuration, service layer logic.
- Files: `backend/src/main/java/com/example/todobackend/**/*.java`
- Risk: Business logic changes (e.g., JWT claims, priority enum) have no regression protection.
- Priority: Low for demo project but should be added before production use.

**No E2E Tests:**
- What's not tested: Full user flows like "login → create todo → toggle → delete".
- Risk: Integration between frontend and backend can silently break without detection.
- Priority: Low for demo/learning project.

---

*Concerns audit: 2026-09-29*
