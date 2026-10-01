# 技术栈

**分析日期：** 2026-09-29

## 语言

**主要语言：**
- JavaScript ES6+ — 前端源代码（Vue SFC、API 模块、stores）
- Java 1.8 — Spring Boot 后端
- HTML5 — 入口标记

**次要语言：**
- CSS — Element Plus 组件样式（作为库引入）

## 运行时

**前端构建/开发：**
- Node.js（Vite 需要）
- NPM（包管理）

**后端：**
- Java 1.8（JDk 8）

**包管理器：**
- NPM 6+（前端 `vue-counter`）
- Maven（后端 `todo-backend`）
- Lockfile：`vue-counter/package-lock.json`（已存在）

## 框架

**核心前端：**
- Vue 3.5.42 — UI 框架（Composition API）
- Vite 8.2.2 — 构建工具和开发服务器
- Vue Router 4.6.4 — 客户端路由（hash 模式）
- Pinia 4.0.3 — 状态管理（替代 Vuex）
- Axios 1.20.0 — HTTP 客户端

**UI 库：**
- Element Plus 2.14.6 — Web UI 组件库

**数据可视化：**
- ECharts 6.1.0 — 图表（统计页）

**核心后端：**
- Spring Boot 2.7.18 — Web 框架（内嵌 Tomcat）
- Spring MVC — REST 控制器层
- Spring Boot Starter Validation — 参数校验

**认证：**
- jjwt-api/impl/jackson 0.11.5 — JWT Token 生成和校验

**样板代码消除：**
- Lombok — 自动生成 getter/setter/builder

## 关键依赖

**前端核心：**
- `vue` 3.5.42 — 核心框架
- `vue-router` 4.6.4 — 路由
- `pinia` 4.0.3 — 状态管理
- `axios` 1.20.0 — HTTP 客户端，带拦截器
- `element-plus` 2.14.6 — UI 组件
- `echarts` 6.1.0 — 图表

**后端核心：**
- `spring-boot-starter-web` — MVC + Tomcat
- `spring-boot-starter-validation` — Bean Validation
- `jjwt-api/impl/jackson` 0.11.5 — JWT
- `lombok` — 样板代码消除

**前端构建工具：**
- `vite` 8.2.2 — 打包和开发服务器
- `@vitejs/plugin-vue` 6.0.8 — Vue SFC 的 Vite 插件

**后端构建工具：**
- `spring-boot-maven-plugin` — JAR 打包

## 配置

**前端构建：**
- `vue-counter/vite.config.js` — Vite 配置（含代理和基础路径）
- `vue-counter/index.html` — 入口 HTML
- `vue-counter/package.json` — 依赖和 npm 脚本

**后端配置：**
- `backend/src/main/resources/application.yml` — 服务器端口（8080）、JWT 密钥、过期时间
- `backend/pom.xml` — Maven 依赖和 Spring Boot 版本（2.7.18）

**环境变量：**
- 未检测到（演示目的写死在 application.yml 中）

## 平台要求

**开发环境：**
- Node.js 18+（Vite 8 需要）
- JDK 8（Spring Boot 2.7.18 需要）
- NPM 6+

**生产环境：**
- 前端：静态托管（GitHub Pages，`vite.config.js` 中 `base: './'`）
- 后端：Java 8 运行时，内嵌 Tomcat，端口 8080
- 目标服务器：腾讯云 `123.207.69.23:8080`（生产）

---

*技术栈分析：2026-09-29*
