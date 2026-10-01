# Phase 2 Plan 01: Document Upload Tracer - Summary

**Phase:** 02-document-ingestion
**Plan:** 01
**Status:** complete

## Objective

Implement end-to-end document upload tracer: upload → extract → embed → Chroma index.

## Tasks Completed

| Task | Type | Files |
|------|------|-------|
| End-to-end document upload tracer | tracer | DocumentController.java, DocumentService.java, DocumentUploadResponse.java, WebClientConfig.java, DocumentUploadConfig.java, extract_and_embed.py, backend/data/.gitkeep, application.yml |
| Python script unit tests | auto | test_extract_and_embed.py |
| Security configuration for document endpoints | auto | SecurityConfig.java |

## Files Created/Modified

### Backend Java

| File | Change |
|------|--------|
| `backend/pom.xml` | Added `spring-boot-starter-webflux` dependency |
| `backend/src/main/resources/application.yml` | Added multipart (10MB), siliconflow (api-key, base-url), chroma (persist-directory), upload (temp-directory, allowed-extensions, max-size) configs |
| `backend/src/main/java/com/aikb/config/WebClientConfig.java` | New - WebClient bean for SiliconFlow API calls with connection pooling |
| `backend/src/main/java/com/aikb/config/DocumentUploadConfig.java` | New - MultipartConfigElement (10MB) and upload directory Path bean |
| `backend/src/main/java/com/aikb/dto/DocumentUploadResponse.java` | New - DTO with documentId, fileName, chunkCount, knowledgeBaseId, status, message |
| `backend/src/main/java/com/aikb/service/DocumentService.java` | New - Upload orchestration: validates file, saves temp, calls Python script via ProcessBuilder, deletes temp in finally block |
| `backend/src/main/java/com/aikb/controller/DocumentController.java` | New - POST /api/documents/upload endpoint with MultipartFile + knowledgeBaseId params |
| `backend/src/main/java/com/aikb/config/SecurityConfig.java` | Updated - Added `antMatchers(HttpMethod.POST, "/api/documents/upload").permitAll()` |

### Backend Python

| File | Change |
|------|--------|
| `backend/scripts/extract_and_embed.py` | New - Standalone script: extract_text (PDF/DOCX/TXT), chunk_text (500 tokens/50 overlap), get_embedding (SiliconFlow BAAI/bge-m3), Chroma indexing with per-user collection |
| `backend/scripts/test_extract_and_embed.py` | New - pytest unit tests for chunk_text and extract_text |

### Data

| File | Change |
|------|--------|
| `backend/data/.gitkeep` | New - ensures data/chroma/ directory is tracked |
| `backend/uploads/.gitkeep` | New - ensures uploads/ directory is tracked |
| `.gitignore` | Updated - added backend/uploads/ and backend/data/chroma/ |

## Implementation Details

### DocumentController
- `POST /api/documents/upload`
- Accepts `multipart/form-data` with `file` and `knowledgeBaseId` params
- Extracts userId from Authentication (defaults to 1L for anonymous)
- Returns `DocumentUploadResponse` or error map

### DocumentService
- Validates extension (pdf/docx/txt only) and file size (10MB max)
- Saves temp file using `Files.createTempFile(uploadPath, "upload_", "." + extension)`
- Passes JSON to Python script via stdin: `{"filePath", "kbId", "userId", "chromaPath"}`
- Reads JSON result from stdout
- Deletes temp file in `finally` block
- ProcessBuilder timeout: 120 seconds

### Python Script (extract_and_embed.py)
- Input: JSON via stdin
- Output: JSON via stdout
- Text extraction: `pypdf.PdfReader` for PDF, `python-docx.Document` for DOCX, built-in for TXT
- Chunking: 500 tokens / 50 overlap (char heuristic: 665 chars / 67 chars overlap)
- Embedding: SiliconFlow `BAAI/bge-m3` model via POST `/v1/embeddings`
- Chroma: PersistentClient at `backend/data/chroma/`, collection `user_{userId}_kb_{kbId}`
- Non-zero exit on any error

## Security Notes

- File extension validation: allowlist (pdf, docx, txt)
- File size: 10MB hard limit via MultipartConfig
- Temp files: UUID-based names, deleted after processing
- Collection isolation: per-user per-knowledge-base collection name
- POST /api/documents/upload: permitAll() for tracer (full JWT enforcement in later plan)

## Deviations from Plan

None - plan executed exactly as written.

## Known Stubs

None.

## Metrics

| Metric | Value |
|--------|-------|
| tasks | 3 |
| files_created | 10 |
| files_modified | 3 |

---

*Plan executed: 2026-10-01*
