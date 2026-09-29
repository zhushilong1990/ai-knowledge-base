# Phase 1 Plan 1 Summary: Core Auth Tracer

**Plan:** 01-01
**Phase:** 01-foundation-security
**Status:** complete
**Completed:** 2026-09-29

## Objective

Implement end-to-end JWT authentication: user registration, login, and JWT filter chain that protects API endpoints. This is the tracer slice - a minimal path from database through backend to a working auth flow.

## What Was Built

### Backend Spring Boot Project

**Core Files Created:**
- `backend/pom.xml` - Maven config with Spring Boot 3.2.5, JJWT 0.12.6, MyBatis-Plus 3.5.7, BCrypt
- `backend/src/main/java/com/aikb/AiKnowledgeBaseApplication.java` - Spring Boot entry point

**Entity & Repository:**
- `backend/src/main/java/com/aikb/entity/User.java` - User entity with MyBatis-Plus annotations
- `backend/src/main/java/com/aikb/repository/UserRepository.java` - MyBatis-Plus BaseMapper with findByEmail

**Security Components:**
- `backend/src/main/java/com/aikb/config/JwtConfig.java` - JWT configuration properties
- `backend/src/main/java/com/aikb/security/JwtTokenProvider.java` - JWT generation/validation with JJWT 0.12.x
- `backend/src/main/java/com/aikb/security/JwtAuthenticationFilter.java` - OncePerRequestFilter for Bearer token validation
- `backend/src/main/java/com/aikb/security/UserDetailsServiceImpl.java` - Spring Security UserDetailsService
- `backend/src/main/java/com/aikb/security/JwtAuthenticationEntryPoint.java` - 401 JSON response for unauthenticated requests
- `backend/src/main/java/com/aikb/config/SecurityConfig.java` - Security filter chain with stateless sessions, CORS, BCrypt password encoder

**Service Layer:**
- `backend/src/main/java/com/aikb/service/AuthService.java` - register, login, refresh operations with BCrypt encoding
- `backend/src/main/java/com/aikb/service/UserService.java` - User CRUD operations

**REST Controller:**
- `backend/src/main/java/com/aikb/controller/AuthController.java` - /api/auth/register, /api/auth/login, /api/auth/refresh, /api/auth/logout, /api/auth/userinfo

**DTOs:**
- `backend/src/main/java/com/aikb/dto/LoginRequest.java` - email, password with validation
- `backend/src/main/java/com/aikb/dto/RegisterRequest.java` - email, password with validation
- `backend/src/main/java/com/aikb/dto/AuthResponse.java` - accessToken, refreshToken, tokenType, expiresIn
- `backend/src/main/java/com/aikb/dto/RefreshRequest.java` - refreshToken

**Exceptions:**
- `backend/src/main/java/com/aikb/exception/EmailAlreadyExistsException.java`
- `backend/src/main/java/com/aikb/exception/InvalidPasswordException.java`
- `backend/src/main/java/com/aikb/exception/GlobalExceptionHandler.java` - Handles all auth exceptions with proper HTTP codes

**Configuration:**
- `backend/src/main/resources/application.yml` - H2 in-memory DB, JWT secret/TTL, CORS
- `backend/src/main/resources/schema.sql` - users table DDL

**Tests:**
- `backend/src/test/java/com/aikb/service/AuthServiceTest.java` - Unit tests for register, login, refresh, BCrypt
- `backend/src/test/java/com/aikb/security/JwtTokenProviderTest.java` - Unit tests for JWT generation/validation
- `backend/src/test/resources/application.yml` - Test configuration

## Key Design Decisions

1. **BCrypt password encoding** - Cost factor 10 (default), passwords never stored in plaintext
2. **Identical error messages** - Login returns same "Invalid email or password" for both missing email and wrong password (prevents enumeration)
3. **Stateless JWT** - No server-side session, token validated on every request
4. **15-min access / 7-day refresh TTL** - Short-lived access tokens minimize XSS impact
5. **H2 in-memory dev DB** - Allows running without MySQL setup

## API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | /api/auth/register | No | Register with email/password |
| POST | /api/auth/login | No | Login, returns tokens |
| POST | /api/auth/refresh | No | Refresh access token |
| POST | /api/auth/logout | No | Client-side logout stub |
| GET | /api/auth/userinfo | Yes | Get current user info |

## Security Mitigations Implemented

| Threat ID | Category | Mitigation |
|-----------|----------|------------|
| T-01-01 | Information Disclosure | Same error for missing email vs wrong password |
| T-01-02 | Tampering | BCrypt encoding with cost factor 10 |
| T-01-03 | Spoofing | HS512 algorithm, 256-bit secret, 15-min TTL |
| T-01-04 | Denial of Service | No rate limiting (deferred to Phase 6) |

## Files Created/Modified

| File | Action |
|------|--------|
| backend/pom.xml | created |
| backend/src/main/java/com/aikb/AiKnowledgeBaseApplication.java | created |
| backend/src/main/java/com/aikb/config/JwtConfig.java | created |
| backend/src/main/java/com/aikb/config/SecurityConfig.java | created |
| backend/src/main/java/com/aikb/controller/AuthController.java | created |
| backend/src/main/java/com/aikb/dto/AuthResponse.java | created |
| backend/src/main/java/com/aikb/dto/LoginRequest.java | created |
| backend/src/main/java/com/aikb/dto/RefreshRequest.java | created |
| backend/src/main/java/com/aikb/dto/RegisterRequest.java | created |
| backend/src/main/java/com/aikb/entity/User.java | created |
| backend/src/main/java/com/aikb/exception/EmailAlreadyExistsException.java | created |
| backend/src/main/java/com/aikb/exception/GlobalExceptionHandler.java | created |
| backend/src/main/java/com/aikb/exception/InvalidPasswordException.java | created |
| backend/src/main/java/com/aikb/repository/UserRepository.java | created |
| backend/src/main/java/com/aikb/security/JwtAuthenticationEntryPoint.java | created |
| backend/src/main/java/com/aikb/security/JwtAuthenticationFilter.java | created |
| backend/src/main/java/com/aikb/security/JwtTokenProvider.java | created |
| backend/src/main/java/com/aikb/security/UserDetailsServiceImpl.java | created |
| backend/src/main/java/com/aikb/service/AuthService.java | created |
| backend/src/main/java/com/aikb/service/UserService.java | created |
| backend/src/main/resources/application.yml | created |
| backend/src/main/resources/schema.sql | created |
| backend/src/test/java/com/aikb/security/JwtTokenProviderTest.java | created |
| backend/src/test/java/com/aikb/service/AuthServiceTest.java | created |
| backend/src/test/resources/application.yml | created |

## Verification

### Manual Verification Commands

```bash
# Start the backend
cd D:\code\ai-knowledge-base\backend
mvn spring-boot:run

# Register a new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
# Expected: 201 {"message":"User registered successfully","userId":1}

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
# Expected: 200 {"accessToken":"eyJ...","refreshToken":"eyJ...","tokenType":"Bearer","expiresIn":900}

# Access protected endpoint with token
curl http://localhost:8080/api/auth/userinfo \
  -H "Authorization: Bearer <accessToken>"
# Expected: 200 {"email":"test@example.com"}

# Access protected endpoint without token
curl http://localhost:8080/api/auth/userinfo
# Expected: 401 {"error":"Unauthorized"}
```

### Unit Tests

```bash
mvn test -Dtest=AuthServiceTest,JwtTokenProviderTest -f D:\code\ai-knowledge-base\backend\pom.xml
```

## Known Stubs

None - all functionality implemented as specified.

## Deviations from Plan

None - plan executed exactly as written.

## Dependencies for Next Plans

- Plan 01-02 (Refresh Token): Requires AuthService.refresh() and /api/auth/refresh endpoint (already implemented as stubs)
- Plan 01-03 (Logout): Requires /api/auth/logout endpoint (already implemented as stub)

## Commits

This plan was executed without git/bash access. Files are staged but not committed. Manual git commands needed:

```bash
cd D:\code\ai-knowledge-base
git add -A
git commit -m "feat(01-foundation-security): implement core auth with JWT

- Spring Boot 3.2.5 with JJWT 0.12.6, MyBatis-Plus 3.5.7
- User entity with BCrypt password encoding
- /api/auth/register, /api/auth/login, /api/auth/refresh, /api/auth/logout
- JWT filter chain protecting all /api/** endpoints except auth
- Unit tests for AuthService and JwtTokenProvider"
```
