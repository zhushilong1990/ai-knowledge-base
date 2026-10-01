# Phase 2 Plan 02-03: Frontend Upload Component + API Module Summary

## Overview

**Plan:** 02-03
**Phase:** 02-document-ingestion
**Status:** complete
**Completed:** 2026-10-01

## Objective

Implement the frontend document upload component with file validation, drag-and-drop, progress feedback, and proper error handling. This completes the document ingestion pipeline by giving users a working upload UI that integrates with the backend API.

## Changes Made

### 1. Frontend API Module (documents.js)

**File:** `frontend/src/api/documents.js` (NEW)

Created upload API module with:
- `uploadDocument(file, knowledgeBaseId, onProgress)` - async upload function
- Client-side validation for file type (PDF, DOCX, TXT only)
- Client-side validation for file size (10MB limit)
- Progress callback wired to Axios `onUploadProgress`
- Returns parsed response data directly

### 2. Pinia Store (documents.js)

**File:** `frontend/src/stores/documents.js` (NEW)

Created documents store with:
- `isUploading` - reactive state for upload in progress
- `uploadProgress` - reactive progress percentage (0-100)
- `lastUploadResult` - stores successful upload response
- `upload(file, knowledgeBaseId)` - action that calls API and manages state
- `clearLastResult()` - clears success message
- ElMessage feedback for success/error

### 3. Upload Page Component (upload.vue)

**File:** `frontend/src/pages/upload.vue` (NEW)

Created upload page with:
- Drag-and-drop file upload area (el-upload with drag enabled)
- File type restriction (accept=".pdf,.docx,.txt")
- Knowledge base selector (dropdown with default option)
- File info display showing name and size
- Progress bar during upload (el-progress)
- Upload button with loading state and disabled when no file selected
- Success alert showing chunk count after upload
- Uses Element Plus components consistently with project style

### 4. Router Integration

**File:** `frontend/src/router/index.js`

Added `/upload` route:
- Path: `/upload`
- Component: `upload.vue`
- Auth: `requiresAuth: true` (protected route)

### 5. Navigation Link

**File:** `frontend/src/pages/home.vue`

Added "Upload Document" button on home page that navigates to `/upload`.

## Files Created

| File | Description |
|------|-------------|
| `frontend/src/api/documents.js` | Upload API module with Axios |
| `frontend/src/stores/documents.js` | Pinia store for upload state |
| `frontend/src/pages/upload.vue` | Upload page component |

## Files Modified

| File | Description |
|------|-------------|
| `frontend/src/router/index.js` | Added /upload route |
| `frontend/src/pages/home.vue` | Added navigation link to upload page |

## Key Design Decisions

1. **Composition API for Pinia store** - Used `defineStore` with setup function style for the documents store, consistent with Vue 3 best practices even though auth store uses Options API.

2. **Client-side validation mirrors backend** - File type and size validation on client before upload, providing immediate feedback and reducing unnecessary API calls.

3. **Progress callback pattern** - Upload function accepts optional `onProgress` callback that receives percentage updates, enabling reactive progress bar.

4. **Store-managed error handling** - Store action catches errors and shows ElMessage.error, components don't need to handle display errors.

## Dependencies

- Plan 02-02 (JWT enforcement + Document entity) - already implemented
- http.js Axios instance - auto-injects JWT token
- Element Plus - UI components used throughout
- Pinia - state management

## Deviations

None - plan executed exactly as written.

## Threat Mitigations Implemented

| Threat ID | Mitigation |
|-----------|------------|
| T-02-21 | Frontend validates file.size <= 10MB before upload; shows error immediately |
| T-02-22 | Token theft mitigation deferred to Phase 6 (HTTPS + CSP) |
| T-02-23 | Progress spoofing accepted - server-side validation is authoritative |
