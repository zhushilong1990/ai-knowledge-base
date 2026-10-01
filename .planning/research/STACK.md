# 技术栈调研

**领域：** RAG 知识库问答应用
**调研时间：** 2026-09-29
**置信度：** 高

## 推荐技术栈

### 核心技术

| 技术 | 版本 | 用途 | 推荐理由 |
|------------|---------|---------|----------------|
| Python | 3.10+ | AI 微服务运行时 | LangChain、chromadb 及大多数 LLM 集成需要 |
| FastAPI | 0.104+ | AI 微服务 HTTP 框架 | 原生异步、Pydantic 校验、OpenAPI 自动生成、uvicorn ASGI 服务器 |
| LangChain | 0.1.x | RAG 编排框架 | 模块化检索/生成链、文档加载器、文本分割 |
| LangChain-Community | 0.0.x | 第三方集成 | Chroma 加载器、非结构化文档解析器、硅基流动集成 |
| ChromaDB | 0.4.x | 向量数据库 | 轻量、嵌入式、Python 原生，单节点 RAG 完美 |
| unstructured | 0.10+ | 文档解析 | PDF/Word/HTML 解析，用于 RAG 文档摄入 |
| Spring Boot | 3.2.x | 后端 REST API | Jakarta EE 9+、JWT 支持、MySQL 驱动、成熟生态 |
| MySQL | 8.0+ | 关系数据库 | 存储用户、聊天历史、文档元数据、认证 token |
| Vue 3 | 3.4+ | 前端框架 | Composition API + `<script setup>`、Vite 5.x 构建工具 |
| uni-app | 3.x | 跨平台包装 | 一套代码覆盖 H5 + 微信小程序 + Android App |
| Pinia | 2.1+ | 状态管理 | 官方 Vue store、devtools 支持、持久化插件 |
| Element Plus | 2.5+ | Web UI 组件 | 企业级 Vue 3 组件库 |
| uView Plus | 3.x | uni-app UI 组件 | 兼容微信小程序 + App 的组件集 |

### 支持库

| 库 | 版本 | 用途 | 使用场景 |
|---------|---------|---------|-------------|
| langchain-openai | 0.0.x | OpenAI 兼容 API 客户端 | 用于硅基流动 DeepSeek 模型集成 |
| httpx | 0.25+ | 异步 HTTP 客户端 | FastAPI 调用外部 LLM API |
| sentence-transformers | 2.2+ | 文本 embedding | 本地 embedding 模型（可选，硅基流动也提供 embedding） |
| PyPDF2 / pypdf | 3.16+ | PDF 文本提取 | 文档摄入管道 |
| python-multipart | 0.0.6 | 表单数据解析 | FastAPI 文件上传支持 |
| uvicorn | 0.24+ | ASGI 服务器 | 运行 FastAPI 应用 |
| pydantic | 2.x | 数据校验 | FastAPI 请求/响应模型 |
| JJWT | 0.12.x | JWT 处理 | Spring Boot JWT 生成和校验 |
| MyBatis-Plus | 3.5.x | ORM 增强 | Spring Boot MySQL 访问，含 CRUD 工具 |
| H2 Database | 2.x | 开发/测试数据库 | 本地开发（可选，生产换 MySQL） |
| Vue Router | 4.2+ | 前端路由 | SPA 导航，含守卫 |
| Axios | 1.6+ | HTTP 客户端 | 前端调用后端 API |

### 开发工具

| 工具 | 用途 | 备注 |
|------|---------|-------|
| Docker + docker-compose | 容器编排 | 在容器中运行 Chroma、MySQL、AI 微服务 |
| PyCharm / VSCode | Python IDE | Python 语言服务器、调试 |
| IDEA (Ultimate) | Java IDE | Spring Boot 支持、代码补全 |
| Vite | 构建工具 | Vue 3 开发服务器和打包器 |
| pnpm / npm | 包管理器 | Vue 依赖；推荐 monorepo 用 pnpm |

## 安装

### AI 微服务（Python）

```bash
# 核心 RAG 依赖
pip install fastapi==0.104.1 uvicorn==0.24.0
pip install langchain==0.1.20 langchain-community==0.0.38
pip install chromadb==0.4.24
pip install unstructured==0.10.30
pip install httpx==0.25.2 pydantic==2.5.3
pip install python-multipart==0.0.6

# Embedding + LLM（硅基流动 DeepSeek）
pip install langchain-openai==0.0.8
pip install sentence-transformers==2.2.2  # 可选，本地 embedding

# 可选：PDF 支持
pip install pypdf==3.17.4
```

### 后端（Java Spring Boot）

```xml
<!-- pom.xml 关键依赖 -->
<dependencies>
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.3</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.3</version>
    </dependency>

    <!-- MySQL + MyBatis -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>3.5.5</version>
    </dependency>

    <!-- H2 开发用（可选） -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### 前端（Vue 3 + Vite）

```bash
# 创建 Vite + Vue 3 项目
npm create vite@latest frontend -- --template vue

# 安装核心依赖
cd frontend
npm install vue@3.4.21 vue-router@4.2.5 pinia@2.1.7 axios@1.6.7

# Element Plus Web
npm install element-plus@2.5.6 @element-plus/icons-vue@2.3.1

# 开发依赖
npm install -D vite@5.1.4 @vitejs/plugin-vue@5.0.4
```

### uni-app（跨平台）

```bash
# 全局安装 vue-cli
npm install -g @vue/cli@5.0.8

# 创建 uni-app 项目
npx degit dcloudio/uni-preset-vue#vite frontend-uni

# 安装依赖
cd frontend-uni
npm install

# uView Plus 跨平台 UI
npm install uview-plus@3.3.36

# 安装 Pinia + Vue Router（uni-app 兼容版本）
npm install pinia@2.1.7 vue-router@4.2.5
```

### Docker / docker-compose

```yaml
# docker-compose.yml
version: '3.8'

services:
  chroma:
    image: chromadb/chroma:0.4.24
    ports:
      - "8000:8000"
    volumes:
      - chroma_data:/chroma/chroma/chroma_db
    networks:
      - rag-network

  mysql:
    image: mysql:8.2.0
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: rag_knowledge_base
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - rag-network

  ai-service:
    build: ./ai-service
    ports:
      - "8001:8001"
    environment:
      OPENAI_API_KEY: ${SILICONFLOW_API_KEY}
      CHROMA_HOST: chroma
      CHROMA_PORT: 8000
    depends_on:
      - chroma
    networks:
      - rag-network

networks:
  rag-network:

volumes:
  chroma_data:
  mysql_data:
```

## 备选方案

| 推荐 | 备选 | 使用备选场景 |
|-------------|-------------|------------------------|
| ChromaDB | Milvus / Qdrant / Weaviate | 需要分布式向量搜索规模时 |
| LangChain | LlamaIndex / Haystack | 更喜欢 LlamaIndex 以文档为中心的 API 时 |
| 硅基流动（DeepSeek）| OpenAI GPT-4 / Azure OpenAI | 需要 GPT-4 质量时；用硅基流动为省成本 |
| MySQL | PostgreSQL + pgvector | 喜欢一体化数据库加向量支持时 |
| FastAPI | Flask / Django | 需要 Django admin 面板或 Flask 简洁性时 |
| Spring Boot 3.2 | Spring Boot 2.7（遗留）| 必须使用 Java 8/11 遗留运行时时 |
| Element Plus | Ant Design Vue | 更喜欢 Ant Design 生态时 |
| uView Plus | Vant Weapp | 更注重微信小程序而非 App/H5 时 |

## 不推荐使用的技术

| 避免 | 原因 | 改用 |
|-------|-----|-------------|
| Vue 2 / Options API | 已废弃，维护模式 | Vue 3 Composition API |
| Webpack（vue-cli 默认）| 构建慢，配置复杂 | Vite（开发服务器快 10 倍）|
| Vant 2 / Element UI（Vue 2）| 仅支持 Vue 2 | Element Plus / uView Plus |
| LangChain 0.0.x（旧）| 破坏性变更，不稳定 | LangChain 0.1.x（稳定）|
| Chroma 0.3.x | 缺失功能，有 bug | Chroma 0.4.x |
| Python 3.9 或更早 | LangChain 需要 3.10+ | Python 3.10、3.11、3.12 |
| MyBatis（原始 XML）| 模板代码多，易出错 | MyBatis-Plus（注解驱动）|
| Session-based 认证 | 首选无状态 REST | JWT Bearer tokens |

## 技术栈变体

**如果本地生成 embedding（不用外部 API）：**
- 在 Python 依赖中添加 `sentence-transformers`
- 在 LangChain 中配置 `HuggingFaceEmbeddings`
- 无 embedding API 成本，但需要更多 CPU/GPU

**如果只用微信小程序（不用 H5/App）：**
- 跳过 uView Plus，改为 Vant Weapp
- 减少跨平台复杂度

**如果 Chroma 外部管理（不在 docker-compose 中）：**
- 从 docker-compose 移除 `chroma` 服务
- 在 AI 服务环境中将 `CHROMA_HOST` 设为外部 IP

## 版本兼容性

| 包 A | 兼容版本 | 备注 |
|-----------|-----------------|-------|
| LangChain 0.1.x | ChromaDB 0.4.x、Python 3.10+ | LangChain 0.0.x 和 0.1.x 之间有破坏性变更 |
| FastAPI 0.104+ | Pydantic 2.x、uvicorn 0.24+ | Pydantic 1.x 与 FastAPI 0.104+ 不兼容 |
| Spring Boot 3.2.x | Java 17+ | Spring Boot 3.x 需要 Jakarta EE 9+ |
| Vue 3.4+ | Vite 5.x、Vue Router 4.x | Vue 3.3 或更早版本兼容性最高 |
| MySQL 8.0+ | MyBatis-Plus 3.5.x | MySQL 5.7 仍可用但推荐 8.0 |
| JJWT 0.12.x | Spring Boot 3.x | JJWT 0.11.x 用于 Spring Boot 2.x |
| uni-app 3.x | Vue 3.2+ | uni-app 2.x 使用 Vue 2 |

## 来源

- LangChain 文档 — https://python.langchain.com/docs/get_started/introduction — 高
- ChromaDB 文档 — https://docs.trychroma.com — 高
- FastAPI 文档 — https://fastapi.tiangolo.com — 高
- Spring Boot 3.2 发布说明 — https://github.com/spring-projects/spring-boot/releases/tag/v3.2.0 — 高
- Element Plus 文档 — https://element-plus.org — 中
- uni-app 文档 — https://uniapp.dcloud.net.cn — 中
- JJWT GitHub — https://github.com/jwtk/jjwt — 高
- MyBatis-Plus 文档 — https://baomidou.com/pages/24112f — 中

---

*技术栈调研：RAG 知识库问答应用*
*调研时间：2026-09-29*
