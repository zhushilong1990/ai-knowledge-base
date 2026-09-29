# Stack Research

**Domain:** RAG Knowledge Base Q&A Application
**Researched:** 2026-09-29
**Confidence:** HIGH

## Recommended Stack

### Core Technologies

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| Python | 3.10+ | AI microservice runtime | Required for LangChain, chromadb, and most LLM integrations |
| FastAPI | 0.104+ | AI microservice HTTP framework | Native async, Pydantic validation, OpenAPI auto-gen, uvicorn ASGI server |
| LangChain | 0.1.x | RAG orchestration framework | Modular retrieval/generation chain, document loaders, text splitting |
| LangChain-Community | 0.0.x | Third-party integrations | Chroma loader, unstructured document parsers, SiliconFlow integration |
| ChromaDB | 0.4.x | Vector database | Lightweight, embedded, Python-native, perfect for single-node RAG |
| unstructured | 0.10+ | Document parsing | PDF/Word/HTML parsing for RAG document ingestion |
| Spring Boot | 3.2.x | Backend REST API | Jakarta EE 9+, JWT support, MySQL driver, mature ecosystem |
| MySQL | 8.0+ | Relational database | Stores users, chat history, document metadata, auth tokens |
| Vue 3 | 3.4+ | Frontend framework | Composition API with `<script setup>`, Vite 5.x build tool |
| uni-app | 3.x | Cross-platform wrapper | Single codebase for H5 + WeChat MiniApp + Android App |
| Pinia | 2.1+ | State management | Official Vue store, devtools support, persistence plugin |
| Element Plus | 2.5+ | Web UI components | Enterprise-grade Vue 3 component library |
| uView Plus | 3.x | uni-app UI components | WeChat miniapp + App compatible component set |

### Supporting Libraries

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| langchain-openai | 0.0.x | OpenAI-compatible API client | For SiliconFlow DeepSeek model integration |
| httpx | 0.25+ | Async HTTP client | FastAPI calling external LLM APIs |
| sentence-transformers | 2.2+ | Text embeddings | Local embedding model for Chroma (optional, SiliconFlow also provides embeddings) |
| PyPDF2 / pypdf | 3.16+ | PDF text extraction | Document ingestion pipeline |
| python-multipart | 0.0.6 | Form data parsing | FastAPI file upload support |
| uvicorn | 0.24+ | ASGI server | Running FastAPI application |
| pydantic | 2.x | Data validation | FastAPI request/response models |
| JJWT | 0.12.x | JWT handling | Spring Boot JWT generation and validation |
| MyBatis-Plus | 3.5.x | ORM enhancement | Spring Boot MySQL access with CRUD utilities |
| H2 Database | 2.x | Dev/test database | Local development (optional, swap for MySQL in prod) |
| Vue Router | 4.2+ | Frontend routing | SPA navigation with guards |
| Axios | 1.6+ | HTTP client | Frontend to backend API calls |

### Development Tools

| Tool | Purpose | Notes |
|------|---------|-------|
| Docker + docker-compose | Container orchestration | Run Chroma, MySQL, AI microservice in containers |
| PyCharm / VSCode | Python IDE | Python language server, debugging |
| IDEA (Ultimate) | Java IDE | Spring Boot support, code completion |
| Vite | Build tool | Vue 3 dev server and bundler |
| pnpm / npm | Package managers | Vue dependencies; pnpm recommended for monorepo |

## Installation

### AI Microservice (Python)

```bash
# Core RAG dependencies
pip install fastapi==0.104.1 uvicorn==0.24.0
pip install langchain==0.1.20 langchain-community==0.0.38
pip install chromadb==0.4.24
pip install unstructured==0.10.30
pip install httpx==0.25.2 pydantic==2.5.3
pip install python-multipart==0.0.6

# Embedding + LLM (SiliconFlow DeepSeek)
pip install langchain-openai==0.0.8
pip install sentence-transformers==2.2.2  # Optional, for local embeddings

# Optional: PDF support
pip install pypdf==3.17.4
```

### Backend (Java Spring Boot)

```xml
<!-- pom.xml key dependencies -->
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

    <!-- H2 for dev (optional) -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### Frontend (Vue 3 + Vite)

```bash
# Create Vite + Vue 3 project
npm create vite@latest frontend -- --template vue

# Install core dependencies
cd frontend
npm install vue@3.4.21 vue-router@4.2.5 pinia@2.1.7 axios@1.6.7

# Element Plus for web
npm install element-plus@2.5.6 @element-plus/icons-vue@2.3.1

# Dev dependencies
npm install -D vite@5.1.4 @vitejs/plugin-vue@5.0.4
```

### uni-app (Cross-platform)

```bash
# Install vue-cli globally
npm install -g @vue/cli@5.0.8

# Create uni-app project
npx degit dcloudio/uni-preset-vue#vite frontend-uni

# Install dependencies
cd frontend-uni
npm install

# uView Plus for cross-platform UI
npm install uview-plus@3.3.36

# Install Pinia + Vue Router (uni-app compatible versions)
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

## Alternatives Considered

| Recommended | Alternative | When to Use Alternative |
|-------------|-------------|------------------------|
| ChromaDB | Milvus / Qdrant / Weaviate | When you need distributed vector search at scale |
| LangChain | LlamaIndex / Haystack | When you prefer LlamaIndex's document-centric API |
| SiliconFlow (DeepSeek) | OpenAI GPT-4 / Azure OpenAI | When you need GPT-4 quality; SiliconFlow for cost efficiency |
| MySQL | PostgreSQL + pgvector | When you prefer all-in-one database with vector support |
| FastAPI | Flask / Django | When you need Django's admin panel or Flask's simplicity |
| Spring Boot 3.2 | Spring Boot 2.7 (legacy) | When you must use Java 8/11 legacy runtime |
| Element Plus | Ant Design Vue | When you prefer Ant Design ecosystem |
| uView Plus | Vant Weapp | When you prioritize WeChat miniapp over App/H5 |

## What NOT to Use

| Avoid | Why | Use Instead |
|-------|-----|-------------|
| Vue 2 / Options API | Deprecated, maintenance mode | Vue 3 Composition API |
| Webpack (vue-cli default) | Slow builds, complex config | Vite (10x faster dev server) |
| Vant 2 / Element UI (Vue 2) | Vue 2 only | Element Plus / uView Plus |
| LangChain 0.0.x (old) | Breaking changes, unstable | LangChain 0.1.x (stable) |
| Chroma 0.3.x | Missing features, bugs | Chroma 0.4.x |
| Python 3.9 or earlier | LangChain requires 3.10+ | Python 3.10, 3.11, 3.12 |
| MyBatis (raw XML) | Boilerplate, error-prone | MyBatis-Plus (annotation-driven) |
| Session-based auth | Stateless REST preferred | JWT Bearer tokens |

## Stack Patterns by Variant

**If embedding generation is done locally (no external API for embeddings):**
- Add `sentence-transformers` to Python dependencies
- Configure `HuggingFaceEmbeddings` in LangChain
- No embedding API costs, but requires more CPU/GPU

**If using WeChat miniapp only (no H5/App):**
- Skip uView Plus, use Vant Weapp instead
- Reduce cross-platform complexity

**If Chroma is managed externally (not in docker-compose):**
- Remove `chroma` service from docker-compose
- Set `CHROMA_HOST` to external IP in AI service environment

## Version Compatibility

| Package A | Compatible With | Notes |
|-----------|-----------------|-------|
| LangChain 0.1.x | ChromaDB 0.4.x, Python 3.10+ | LangChain breaking changes between 0.0.x and 0.1.x |
| FastAPI 0.104+ | Pydantic 2.x, uvicorn 0.24+ | Pydantic 1.x incompatible with FastAPI 0.104+ |
| Spring Boot 3.2.x | Java 17+ | Spring Boot 3.x requires Jakarta EE 9+ |
| Vue 3.4+ | Vite 5.x, Vue Router 4.x | Vue 3.3 or earlier for maximum compatibility |
| MySQL 8.0+ | MyBatis-Plus 3.5.x | MySQL 5.7 still works but 8.0 recommended |
| JJWT 0.12.x | Spring Boot 3.x | JJWT 0.11.x for Spring Boot 2.x |
| uni-app 3.x | Vue 3.2+ | uni-app 2.x uses Vue 2 |

## Sources

- LangChain Documentation — https://python.langchain.com/docs/get_started/introduction — HIGH
- ChromaDB Documentation — https://docs.trychroma.com — HIGH
- FastAPI Documentation — https://fastapi.tiangolo.com — HIGH
- Spring Boot 3.2 Release Notes — https://github.com/spring-projects/spring-boot/releases/tag/v3.2.0 — HIGH
- Element Plus Documentation — https://element-plus.org — MEDIUM
- uni-app Documentation — https://uniapp.dcloud.net.cn — MEDIUM
- JJWT GitHub — https://github.com/jwtk/jjwt — HIGH
- MyBatis-Plus Documentation — https://baomidou.com/pages/24112f — MEDIUM

---

*Stack research for: RAG Knowledge Base Q&A Application*
*Researched: 2026-09-29*
