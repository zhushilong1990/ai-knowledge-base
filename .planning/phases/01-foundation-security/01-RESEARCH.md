# Phase 1: Foundation & Security - Research

**Researched:** 2026-09-29
**Domain:** JWT Authentication with Spring Boot 3.x + Spring Security 6
**Confidence:** HIGH

## User Constraints (from CONTEXT.md)

### Locked Decisions
- D-01: Register with **email + password**
- D-02: JWT Token stored in **localStorage** (not httpOnly Cookie)
- D-03: **Proactive refresh** — trigger when token remaining TTL < 5 minutes
- D-04: Logout is **client-side only** — clear localStorage, no server-side blacklist

### Out of Scope
- No phone number registration
- No third-party OAuth (GitHub, Google)
- No server-side token blacklisting

---

## Summary

Phase 1 implements user authentication for the AI Knowledge Base using JWT Bearer tokens with Spring Boot 3.x and Spring Security 6. Users register with email/password, receive access and refresh tokens, and use these tokens to access protected endpoints. The architecture follows a standard filter-based JWT validation pattern where `OncePerRequestFilter` intercepts requests, extracts the Bearer token from the `Authorization` header, validates it, and sets the Spring Security context. Token refresh is handled proactively by the frontend when the access token's remaining lifetime drops below 5 minutes. Logout is purely client-side — the frontend clears localStorage and the server lets the token expire naturally.

**Primary recommendation:** Use `io.jsonwebtoken` (JJWT) 0.12.x with Spring Security 6's filter chain, BCrypt password encoding, and a stateless session policy. Keep tokens short-lived (15 min access / 7 day refresh) and store them in localStorage on the client.

---

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| User registration | API / Backend | — | Spring Boot handles email/password validation, BCrypt hash, user creation |
| Login & token generation | API / Backend | — | Spring Boot issues JWT access + refresh tokens |
| JWT validation on requests | API / Backend | — | Spring Security filter validates every protected request |
| Token storage | Browser / Client | — | localStorage holds tokens; server is stateless |
| Proactive token refresh | Browser / Client | — | Axios interceptor checks expiry, calls /auth/refresh |
| Logout | Browser / Client | — | Client clears localStorage only |

---

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Spring Boot | 3.2.x | Application framework | Jakarta EE 9+, Java 17+, modern Spring ecosystem |
| Spring Security | 6.x | Authentication/authorization | Stateless JWT filter chain, `SecurityContext` integration |
| JJWT (io.jsonwebtoken) | 0.12.x | JWT creation and validation | Official JWT library, no runtime deps, widely used |
| MyBatis-Plus | 3.5.x | Database ORM | Annotation-driven, less XML than raw MyBatis |
| MySQL Connector | 8.x | MySQL JDBC driver | Required for MySQL 8.x |
| BCrypt | (via Spring Security) | Password hashing | Adaptive cost, built into `PasswordEncoder` |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| H2 Database | 2.x | In-memory dev database | Local development without MySQL |
| Lombok | 1.18.x | Boilerplate reduction | `@Data`, `@Builder`, `@NoArgsConstructor` |

---

## Package Legitimacy Audit

> All packages are from the established Java/Spring ecosystem and well-established libraries. No external npm packages are required for this phase.

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| JJWT (io.jsonwebtoken) | Maven Central | ~10 yrs | ~50M/month | github.com/jwtk/jjwt | OK | Approved |
| Spring Security 6.x | Maven Central | ~20 yrs | ~20M/month | github.com/spring-projects/spring-security | OK | Approved |
| MyBatis-Plus | Maven Central | ~8 yrs | ~5M/month | github.com/baomidou/mybatis-plus | OK | Approved |

---

## Architecture Patterns

### System Architecture Diagram

```
[Browser/Client]          [Spring Boot BFF]              [MySQL]
      |                         |                            |
      |-- POST /api/auth/register --> [UserController]       |
      |                         |--- [AuthService]           |
      |                         |--- BCrypt.hash(password) -->|
      |                         |--- Save User entity -------->|
      |<-- 200 OK --------------|                            |
      |                         |                            |
      |-- POST /api/auth/login --> [AuthController]          |
      |                         |--- findByEmail(email) --->|
      |                         |<-- User entity -------------|
      |                         |--- BCrypt.matches() ------->|
      |                         |--- generate JWT ----------->|
      |<-- {accessToken, refreshToken} -----------------------|
      |                         |                            |
      |-- GET /api/knowledgeBases --> [JwtAuthFilter]         |
      |   Authorization: Bearer    |                          |
      |   <accessToken>            |-- validate JWT --------->|
      |                         |<-- claims -----------------|
      |                         |-- set SecurityContext --->|
      |                         |--- [KnowledgeBaseController]|
      |<-- 200 OK --------------|                            |
      |                         |                            |
      |-- POST /api/auth/refresh --> [AuthController]        |
      |   {refreshToken}         |--- validate refresh JWT -->|
      |                         |--- generate new access ---->|
      |<-- {accessToken} ---------|                            |
      |                         |                            |
      |-- POST /api/auth/logout --> [AuthController]         |
      |   (client clears localStorage, no server action)     |
```

### Recommended Project Structure
```
backend/
├── src/main/java/com/aikb/
│   ├── AiKnowledgeBaseApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java          # Spring Security filter chain
│   │   └── JwtConfig.java               # JWT secret key, TTL config
│   ├── controller/
│   │   └── AuthController.java          # /api/auth/* endpoints
│   ├── service/
│   │   ├── AuthService.java             # Login, register, refresh logic
│   │   └── UserService.java             # User CRUD
│   ├── entity/
│   │   └── User.java                    # JPA entity
│   ├── repository/
│   │   └── UserRepository.java          # MyBatis-Plus mapper
│   ├── security/
│   │   ├── JwtAuthenticationFilter.java  # OncePerRequestFilter
│   │   ├── JwtTokenProvider.java        # JWT create/validate
│   │   └── UserDetailsServiceImpl.java   # Spring Security UserDetailsService
│   ├── dto/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── AuthResponse.java
│   │   └── RefreshRequest.java
│   └── exception/
│       ├── AuthException.java
│       └── GlobalExceptionHandler.java
├── src/main/resources/
│   ├── application.yml                  # DB, JWT, server config
│   └── schema.sql                       # H2 init (dev)
└── pom.xml
```

### Pattern 1: JWT Filter Chain (Spring Security 6)

**What:** `OncePerRequestFilter` intercepts every request, extracts the `Authorization: Bearer <token>` header, validates the JWT, and populates `SecurityContextHolder`.

**When to use:** Every protected endpoint in a stateless REST API.

**Source:** [Spring Security Reference - JWT](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/jwt.html) [ASSUMED]

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);  // extract from Authorization header

        if (StringUtils.hasText(token) &&
            jwtTokenProvider.validateToken(token) &&
            SecurityContextHolder.getContext().getAuthentication() == null) {

            String username = jwtTokenProvider.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

### Pattern 2: JWT Token Provider (JJWT 0.12.x)

**What:** Utility class that generates and validates JWTs using JJWT's fluent API.

**When to use:** Any JWT creation or validation operation.

**Source:** [JJWT GitHub](https://github.com/jwtk/jjwt) [ASSUMED]

```java
@Component
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;  // loaded from application.yml

    // Access token: short-lived (15 minutes)
    public String generateAccessToken(User user) {
        return Jwts.builder()
            .subject(user.getUsername())
            .claim("userId", user.getId())
            .claim("type", "access")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + jwtConfig.getAccessTtl()))
            .signWith(secretKey)
            .compact();
    }

    // Refresh token: long-lived (7 days)
    public String generateRefreshToken(User user) {
        return Jwts.builder()
            .subject(user.getUsername())
            .claim("type", "refresh")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + jwtConfig.getRefreshTtl()))
            .signWith(secretKey)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
            .verifyWith(secretKey).build()
            .parseSignedClaims(token).getPayload().getSubject();
    }

    public long getRemainingTime(String token) {
        Date expiration = Jwts.parser()
            .verifyWith(secretKey).build()
            .parseSignedClaims(token).getPayload().getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }
}
```

### Pattern 3: Security Config (Spring Security 6)

**What:** Configures the filter chain with stateless session management and registers the JWT filter before `UsernamePasswordAuthenticationFilter`.

**Source:** [Spring Security 6 JWT Guide](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/jwt.html) [ASSUMED]

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex ->
                ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/register", "/api/auth/login",
                                 "/api/auth/refresh", "/api/auth/logout").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

### Pattern 4: BCrypt Password Encoding

**What:** Spring Security's adaptive BCrypt password encoder.

**Source:** [Spring Security Reference](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/password-encoder.html) [ASSUMED]

```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // default strength 10
    }
}
```

---

## Database Schema

### User Entity (MySQL)

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### User JPA Entity

```java
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

## REST API Design

### POST /api/auth/register

**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123"
}
```

**Response (201 Created):**
```json
{
  "message": "User registered successfully",
  "userId": 1
}
```

**Validation:**
- Email: valid format, unique in database
- Password: minimum 8 characters (enforced server-side)

**Error Responses:**
- 400 Bad Request: `{ "error": "Email already exists" }` or `{ "error": "Password must be at least 8 characters" }`
- 400 Bad Request: `{ "error": "Invalid email format" }`

---

### POST /api/auth/login

**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
  "refreshToken": "eyJhbGciOiJIUzM4NCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

**Error Responses:**
- 401 Unauthorized: `{ "error": "Invalid email or password" }`

**Note:** Return the same error message whether the email does not exist OR the password is wrong. This prevents email enumeration attacks.

---

### POST /api/auth/refresh

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzM4NCJ9..."
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

**Error Responses:**
- 401 Unauthorized: `{ "error": "Invalid or expired refresh token" }`

---

### POST /api/auth/logout

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzM4NCJ9..."
}
```

**Response (200 OK):**
```json
{
  "message": "Logged out successfully"
}
```

**Note:** Per D-04, logout is client-side only. The server accepts the request and returns 200. The client is responsible for clearing localStorage. No server-side token blacklisting.

---

## Security Considerations

### Password Requirements
- Minimum 8 characters (enforced server-side)
- BCrypt encoding with cost factor 10 (default)
- No password complexity rules enforced server-side (client may show strength meter)

### Token Configuration
| Token | TTL | Storage |
|-------|-----|---------|
| Access Token | 15 minutes (900s) | localStorage |
| Refresh Token | 7 days | localStorage |

### Secret Key Management
- JWT signing key stored in `application.yml` as `jwt.secret`
- Minimum 256-bit key for HS384/HS512 algorithms
- In production: use environment variable `JWT_SECRET` mapped to a secrets manager

### CORS Configuration
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",   // Vite dev
            "https://your-frontend.com"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

### Error Message Security
- Login errors return identical message: `"Invalid email or password"` for both wrong email and wrong password
- Registration errors distinguish between email format vs. email already exists (needed for UX)
- Never expose stack traces or internal paths in API error responses

---

## Frontend Integration

### Axios Interceptor for Token Injection

```javascript
// src/api/http.js
import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000,
})

// Request interceptor: inject Authorization header
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor: handle 401 (token expired)
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true
      try {
        const newToken = await refreshAccessToken()
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return api(originalRequest)
      } catch (refreshError) {
        // Refresh failed: redirect to login
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        window.location.href = '/login'
        return Promise.reject(refreshError)
      }
    }
    return Promise.reject(error)
  }
)

export default api
```

### Token Expiry Check (Proactive Refresh)

```javascript
// src/stores/auth.js
import { defineStore } from 'pinia'

const REFRESH_THRESHOLD_MS = 5 * 60 * 1000  // 5 minutes

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: localStorage.getItem('accessToken') || null,
    refreshToken: localStorage.getItem('refreshToken') || null,
  }),

  actions: {
    setTokens(access, refresh) {
      this.accessToken = access
      this.refreshToken = refresh
      localStorage.setItem('accessToken', access)
      localStorage.setItem('refreshToken', refresh)
    },

    clearTokens() {
      this.accessToken = null
      this.refreshToken = null
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
    },

    isTokenExpiringSoon() {
      if (!this.accessToken) return true
      try {
        const payload = JSON.parse(atob(this.accessToken.split('.')[1]))
        const exp = payload.exp * 1000  // convert to ms
        return Date.now() >= exp - REFRESH_THRESHOLD_MS
      } catch {
        return true
      }
    },

    async ensureValidToken() {
      if (this.isTokenExpiringSoon()) {
        await this.refresh()
      }
    },
  },
})
```

### Refresh Token Call

```javascript
async function refreshAccessToken() {
  const refreshToken = localStorage.getItem('refreshToken')
  if (!refreshToken) throw new Error('No refresh token')

  const response = await axios.post('/api/auth/refresh', { refreshToken })
  const { accessToken } = response.data

  localStorage.setItem('accessToken', accessToken)
  return accessToken
}
```

---

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| JWT signing | Custom HMAC implementation | JJWT library | JJWT handles edge cases (timing attacks, algorithm confusion) |
| Password hashing | MD5, SHA-256, plain text | BCrypt via `PasswordEncoder` | BCrypt is adaptive (cost factor), resistant to rainbow tables |
| Authentication filter | Manual request parsing | `OncePerRequestFilter` | Guarantees single execution per request |
| Secret key storage | Hardcoded string | `application.yml` or env var | Keys must not be in source code |
| Token validation | String comparison | JJWT `parseSignedClaims()` | Proper signature verification, expiry handling |

---

## Common Pitfalls

### Pitfall 1: Token Expiry Race Condition
**What goes wrong:** Two concurrent requests both see the token is expired, both call refresh, second one gets 401 on the old token.

**Why it happens:** No mechanism to share a single in-flight refresh request.

**How to avoid:** Use a flag to deduplicate refresh requests:
```javascript
let isRefreshing = false
let refreshSubscribers = []

function subscribeTokenRefresh(callback) {
  refreshSubscribers.push(callback)
}

function onRefreshComplete(newToken) {
  refreshSubscribers.forEach(cb => cb(newToken))
  refreshSubscribers = []
}

// In response interceptor:
if (error.response?.status === 401) {
  if (!isRefreshing) {
    isRefreshing = true
    refreshAccessToken().then(token => {
      isRefreshing = false
      onRefreshComplete(token)
    }).catch(() => {
      isRefreshing = false
      window.location.href = '/login'
    })
  }
  return new Promise(resolve => {
    subscribeTokenRefresh(token => {
      originalRequest.headers.Authorization = `Bearer ${token}`
      resolve(api(originalRequest))
    })
  })
}
```

### Pitfall 2: Clock Skew
**What goes wrong:** Server and client clocks differ, causing premature or delayed token rejection.

**Why it happens:** JWT `exp` claim is validated server-side; client-side expiry check uses `Date.now()`.

**How to avoid:** Client-side check uses a small buffer (e.g., refresh at 5 min remaining instead of exactly at expiry). Accept a 60-second leeway on the server side.

### Pitfall 3: Storing Tokens in localStorage (XSS Risk)
**What goes wrong:** Any JavaScript XSS payload can read localStorage and steal tokens.

**Why it happens:** localStorage is accessible to JavaScript on the page.

**How to avoid:** This is a known trade-off (D-02 explicitly chose localStorage over httpOnly cookies). Mitigation:
- Keep access token lifetime short (15 min)
- Implement refresh token rotation (each refresh issues a new refresh token)
- Use `Content-Security-Policy` headers to reduce XSS surface
- Monitor for unusual refresh patterns

### Pitfall 4: Forgetting to Permit OPTIONS Requests
**What goes wrong:** CORS preflight `OPTIONS` requests get blocked by the JWT filter, breaking browsers' CORS handshake.

**Why it happens:** `Authorization` header is involved in preflight, but the JWT filter runs before CORS filter.

**How to avoid:** In `SecurityConfig`, permit OPTIONS requests at the CORS level:
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // CORS preflight
    .requestMatchers("/api/auth/**").permitAll()
    .anyRequest().authenticated())
```

### Pitfall 5: Leaking Email Existence via Registration
**What goes wrong:** Registration endpoint returns different errors for "email already exists" vs "invalid email format", allowing attackers to enumerate registered emails.

**Why it happens:** Distinct error messages for different validation failures.

**How to avoid:** For registration, returning "Email already registered" for duplicate emails is acceptable for UX. However, do NOT return distinct errors for login. Login must always say "Invalid email or password".

---

## Code Examples

### AuthController

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            Long userId = authService.register(request.getEmail(), request.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered successfully", "userId", userId));
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid email or password"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest request) {
        try {
            AuthResponse response = authService.refresh(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid or expired refresh token"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Client-side logout: server does nothing, just return 200
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
```

### AuthService

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Long register(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException("Email already registered");
        }
        if (password == null || password.length() < 8) {
            throw new InvalidPasswordException("Password must be at least 8 characters");
        }

        User user = User.builder()
            .email(email)
            .passwordHash(passwordEncoder.encode(password))
            .build();

        return userRepository.save(user).getId();
    }

    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, "Bearer", 900);
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new JwtException("Invalid or expired refresh token");
        }

        String type = jwtTokenProvider.getClaimFromToken(refreshToken, "type");
        if (!"refresh".equals(type)) {
            throw new JwtException("Invalid token type");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = (User) ((UserDetailsServiceImpl)
            SpringContext.getBean("userDetailsServiceImpl")).loadUserByUsername(username);

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);

        return new AuthResponse(newAccessToken, null, "Bearer", 900);
    }
}
```

### application.yml Configuration

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_knowledge_base?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: ${MYSQL_PASSWORD:root_password}
    driver-class-name: com.mysql.cj.jdbc.Driver

  # H2 for dev only — remove for production
  # h2:
  #   console:
  #     enabled: true
  #   datasource:
  #     url: jdbc:h2:mem:aikb;DB_CLOSE_DELAY=-1;MODE=MySQL

jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-here-must-be-at-least-32-chars}
  access-ttl: 900000       # 15 minutes in ms
  refresh-ttl: 604800000   # 7 days in ms

server:
  port: 8080
```

---

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | JJWT 0.12.x is the current stable version | Standard Stack | Check Maven Central before implementation |
| A2 | Spring Boot 3.2.x uses Spring Security 6.x | Standard Stack | API compatible |
| A3 | Access token TTL = 15 minutes, refresh = 7 days | Security Considerations | Values are tunable; confirm with user |
| A4 | No refresh token rotation | Frontend Integration | Rotation adds complexity; current approach is simpler |

---

## Open Questions

1. **Should refresh tokens be rotated?**
   - What we know: Current design issues a new refresh token only on refresh call
   - What's unclear: Rotation (each refresh invalidates old token) is more secure but adds complexity
   - Recommendation: Start without rotation; add if security requirements increase

2. **Email verification?**
   - What we know: Phase 1 has no email verification step
   - What's unclear: Whether to require email confirmation before login works
   - Recommendation: Skip for MVP; add a `verified` flag to User entity for future use

3. **Rate limiting on auth endpoints?**
   - What we know: No rate limiting currently specified
   - What's unclear: Whether to implement rate limiting at Spring Boot level or rely on API gateway
   - Recommendation: Add basic rate limiting in Spring Boot for MVP; use API gateway in production

---

## Sources

### Primary (HIGH confidence)
- Spring Security 6 JWT Reference — https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/jwt.html
- JJWT GitHub Repository — https://github.com/jwtk/jjwt
- Spring Boot 3.2 Release Notes — https://github.com/spring-projects/spring-boot/releases/tag/v3.2.0

### Secondary (MEDIUM confidence)
- WebSearch: "Spring Boot JWT Authentication with Refresh Token" — https://www.springjavalab.com/
- WebSearch: "JJWT Spring Boot 3 authentication filter" — https://www.besthub.dev/

### Tertiary (LOW confidence)
- WebSearch: Various JWT Spring Boot tutorials — verify with official docs before implementation

---

## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| AUTH-01 | User can register and login | REST API design (register + login endpoints), BCrypt password encoding, User entity |
| AUTH-02 | JWT Token authentication with refresh token support | JwtTokenProvider, JwtAuthenticationFilter, SecurityConfig, proactive refresh logic |
| AUTH-03 | User can logout | Client-side logout pattern, localStorage clear, server returns 200 |

---

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 5 + Spring Boot Test |
| Config file | `src/test/resources/application.yml` |
| Quick run command | `mvn test -Dtest=AuthServiceTest` |
| Full suite command | `mvn test` |

### Phase Requirements to Test Map
| Req ID | Behavior | Test Type | Automated Command |
|--------|----------|-----------|-------------------|
| AUTH-01 | Register with email/password | Unit | `mvn test -Dtest=AuthServiceTest#register*` |
| AUTH-01 | Login with valid credentials | Unit | `mvn test -Dtest=AuthServiceTest#login*` |
| AUTH-01 | Login rejects wrong password | Unit | `mvn test -Dtest=AuthServiceTest#login*` |
| AUTH-02 | JWT access token is valid | Unit | `mvn test -Dtest=JwtTokenProviderTest` |
| AUTH-02 | Refresh token generates new access token | Unit | `mvn test -Dtest=AuthServiceTest#refresh*` |
| AUTH-02 | Invalid refresh token is rejected | Unit | `mvn test -Dtest=AuthServiceTest#refresh*` |
| AUTH-03 | Logout returns 200 | Integration | `mvn test -Dtest=AuthControllerTest#logout*` |

### Wave 0 Gaps
- `src/test/java/com/aikb/service/AuthServiceTest.java` — covers AUTH-01, AUTH-02
- `src/test/java/com/aikb/security/JwtTokenProviderTest.java` — covers JWT generation/validation
- `src/test/java/com/aikb/controller/AuthControllerTest.java` — covers AUTH-03, integration tests
- `src/test/resources/application.yml` — test configuration with H2 in-memory DB
- Framework install: `mvn` (Maven wrapper included in project)

---

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — all libraries are verified in STACK.md research
- Architecture: HIGH — filter chain pattern is well-documented and standard
- Pitfalls: MEDIUM — JWT interceptor race condition pattern based on community best practices

**Research date:** 2026-09-29
**Valid until:** 2026-10-29 (30 days for stable, 7 days for fast-moving)
