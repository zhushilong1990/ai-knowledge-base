# Phase 2 Plan 02-02: JWT Enforcement + Document Persistence Summary

## Overview

**Plan:** 02-02
**Phase:** 02-document-ingestion
**Status:** complete
**Completed:** 2026-10-01

## Objective

Implement JWT authentication enforcement on document upload endpoint and add document metadata persistence to MySQL.

## Changes Made

### 1. JWT Enforcement (SecurityConfig.java)

**File:** `backend/src/main/java/com/aikb/config/SecurityConfig.java`

Changed `/api/documents/upload` from `permitAll()` to `authenticated()`:

```java
// Before
.antMatchers(HttpMethod.POST, "/api/documents/upload").permitAll()

// After
.antMatchers(HttpMethod.POST, "/api/documents/upload").authenticated()
```

This ensures all document upload requests require valid JWT authentication.

### 2. Document Entity (Document.java)

**File:** `backend/src/main/java/com/aikb/entity/Document.java` (NEW)

Created MyBatis-Plus entity with fields:
- `id` - Auto-increment primary key
- `fileName` - Original filename
- `fileSize` - File size in bytes
- `fileType` - File extension
- `knowledgeBaseId` - Associated knowledge base
- `userId` - Owner user ID
- `externalDocumentId` - ID from Chroma/vector store
- `chunkCount` - Number of text chunks
- `status` - Processing status
- `createdAt` / `updatedAt` - Timestamps

### 3. Document Mapper (DocumentMapper.java)

**File:** `backend/src/main/java/com/aikb/mapper/DocumentMapper.java` (NEW)

Created MyBatis-Plus BaseMapper interface for Document entity.

### 4. Document Service Persistence (DocumentService.java)

**File:** `backend/src/main/java/com/aikb/service/DocumentService.java`

Modified `uploadDocument()` to:
- Inject `DocumentMapper` via constructor
- After successful Python script execution, persist document metadata to database
- Return the internal `documentId` (database ID) instead of external ID in `DocumentUploadResponse`

### 5. Schema Update (schema.sql)

**File:** `backend/src/main/resources/schema.sql`

Added `documents` table:
```sql
CREATE TABLE IF NOT EXISTS documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    knowledge_base_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    external_document_id VARCHAR(255),
    chunk_count INT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_knowledge_base_id (knowledge_base_id),
    INDEX idx_user_id (user_id),
    INDEX idx_external_document_id (external_document_id)
);
```

## Files Created

| File | Description |
|------|-------------|
| `backend/src/main/java/com/aikb/entity/Document.java` | Document entity with MyBatis-Plus annotations |
| `backend/src/main/java/com/aikb/mapper/DocumentMapper.java` | MyBatis-Plus mapper interface |

## Files Modified

| File | Description |
|------|-------------|
| `backend/src/main/java/com/aikb/config/SecurityConfig.java` | Changed upload endpoint to require authentication |
| `backend/src/main/java/com/aikb/service/DocumentService.java` | Added DocumentMapper dependency and persistence logic |
| `backend/src/main/resources/schema.sql` | Added documents table schema |

## Decisions

1. **Document ID Strategy:** Return internal database ID as `documentId` in API response, store external (Chroma) ID in `externalDocumentId` field. This allows future Plan 02-03 queries to use simple internal IDs while maintaining the link to vector store.

2. **Status Default:** Set default status to "pending" in schema, updated to "completed" after successful processing.

## Dependencies

- Plan 02-01 (DocumentController, DocumentService, extract_and_embed.py) - already implemented
- MyBatis-Plus - already in project dependencies
- JWT authentication infrastructure - already in place from Phase 1

## Next Steps (Plan 02-03)

- Document listing endpoint (`GET /api/documents?knowledgeBaseId=X`)
- Document deletion with vector store cleanup
- Document status query

## Deviations

None - plan executed exactly as written.
