# External Integrations

**Analysis Date:** 2026-09-29

## APIs & External Services

**Backend API (Spring Boot):**
- Spring Boot REST API running on port 8080
- Production endpoint: `https://siwei7905.cloud:8443/api` (HTTPS on port 8443)
- Dev proxy: Vite dev server proxies `/api` to `http://123.207.69.23:8080`
- Auth: JWT Bearer token (received on login, stored in localStorage, injected by Axios interceptor)
- CORS: Configured via `CorsConfig.java` allowing cross-origin requests

## Data Storage

**Backend Database:**
- H2 in-memory database (default Spring Boot auto-config)
- JDBC connection: Spring Boot auto-configures H2 from starter
- No external database service detected

**Frontend State:**
- localStorage - Persists JWT token and user object across page refreshes
- Pinia stores (in-memory) - Auth state (`stores/auth.js`), Todo state (`stores/todos.js`)

**File Storage:**
- None (no file upload functionality)

## Authentication & Identity

**Auth Provider:**
- Custom JWT-based authentication
- Implementation: `backend/util/JwtUtil.java` - HS256 signing, 7-day expiration
- Login endpoint: `POST /api/auth/login` returns `{ token, userId, username }`
- Token injection: Axios request interceptor in `src/api/http.js` sets `Authorization: Bearer <token>`
- Token storage: `localStorage.setItem('token', ...)` in Pinia auth store
- Route guard: `router.beforeEach` in `src/router/index.js` checks `useAuthStore().token`

**No external auth providers** (no OAuth, no third-party auth services)

## Monitoring & Observability

**Error Tracking:**
- None detected (no Sentry, LogRocket, etc.)

**Logs:**
- Frontend: `console.error` for global Vue errors and Vite proxy errors
- Backend: Standard Spring Boot logging (SLF4J)
- Axios interceptor logs errors to ElMessage (Element Plus toast)

## CI/CD & Deployment

**Hosting:**
- Frontend: GitHub Pages (configured with `base: './'` in `vite.config.js`)
- Backend: Tencent Cloud server (IP: 123.207.69.23, port 8080)

**CI Pipeline:**
- None detected (no GitHub Actions, no automated deploy)

## Environment Configuration

**Required env vars:**
- None detected (demo stage hardcodes JWT secret in `application.yml`)

**Secrets location:**
- `backend/src/main/resources/application.yml` - JWT secret hardcoded (demo acceptable)
- No `.env` files detected in repo

## Webhooks & Callbacks

**Incoming:**
- None

**Outgoing:**
- None (no webhook outbound calls)

---

*Integration audit: 2026-09-29*
