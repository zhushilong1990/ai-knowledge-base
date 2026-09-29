# Technology Stack

**Analysis Date:** 2026-09-29

## Languages

**Primary:**
- JavaScript ES6+ - Frontend source code (Vue SFCs, API modules, stores)
- Java 1.8 - Spring Boot backend
- HTML5 - Entry point markup

**Secondary:**
- CSS - Element Plus component styling (imported as library)

## Runtime

**Frontend Build/Dev:**
- Node.js (implied by Vite)
- NPM (package management)

**Backend:**
- Java 1.8 (JDK 8)

**Package Manager:**
- NPM 6+ (for frontend `vue-counter`)
- Maven (for backend `todo-backend`)
- Lockfile: `vue-counter/package-lock.json` (present)

## Frameworks

**Core Frontend:**
- Vue 3.5.42 - UI framework (Composition API)
- Vite 8.2.2 - Build tool and dev server
- Vue Router 4.6.4 - Client-side routing (hash mode)
- Pinia 4.0.3 - State management (replaces Vuex)
- Axios 1.20.0 - HTTP client

**UI Library:**
- Element Plus 2.14.6 - Web UI component library

**Data Visualization:**
- ECharts 6.1.0 - Charting (Stats page)

**Core Backend:**
- Spring Boot 2.7.18 - Web framework (embedded Tomcat)
- Spring MVC - REST controller layer
- Spring Boot Starter Validation - Parameter validation

**Authentication:**
- jjwt-api/impl/jackson 0.11.5 - JWT token generation and validation

**Boilerplate:**
- Lombok - Auto-generate getters/setters/builders

## Key Dependencies

**Critical (Frontend):**
- `vue` 3.5.42 - Core framework
- `vue-router` 4.6.4 - Routing
- `pinia` 4.0.3 - State management
- `axios` 1.20.0 - HTTP client with interceptors
- `element-plus` 2.14.6 - UI components
- `echarts` 6.1.0 - Charts

**Critical (Backend):**
- `spring-boot-starter-web` - MVC + Tomcat
- `spring-boot-starter-validation` - Bean Validation
- `jjwt-api/impl/jackson` 0.11.5 - JWT
- `lombok` - Boilerplate elimination

**Build Tools (Frontend):**
- `vite` 8.2.2 - Bundler and dev server
- `@vitejs/plugin-vue` 6.0.8 - Vue SFC plugin for Vite

**Build Tools (Backend):**
- `spring-boot-maven-plugin` - JAR packaging

## Configuration

**Frontend Build:**
- `vue-counter/vite.config.js` - Vite configuration with proxy and base path
- `vue-counter/index.html` - Entry HTML
- `vue-counter/package.json` - Dependencies and npm scripts

**Backend Config:**
- `backend/src/main/resources/application.yml` - Server port (8080), JWT secret, expiration
- `backend/pom.xml` - Maven dependencies and Spring Boot version (2.7.18)

**Environment Variables:**
- Not detected (hardcoded in application.yml for demo purposes)

## Platform Requirements

**Development:**
- Node.js 18+ (for Vite 8)
- JDK 8 (for Spring Boot 2.7.18)
- NPM 6+

**Production:**
- Frontend: Static hosting (GitHub Pages configured with `base: './'`)
- Backend: Java 8 runtime with embedded Tomcat on port 8080
- Target server: Tencent Cloud `123.207.69.23:8080` (production)

---

*Stack analysis: 2026-09-29*
