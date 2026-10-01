---
phase: "06-maintenance-polish"
plan: "02"
subsystem: infra
tags: [chroma, reindex, admin, vue-build]
dependency_graph:
  requires:
    - phase: "06-01"
      provides: "Deployment readiness (health endpoint, H5 config)"
  provides:
    - "Re-index pipeline for Chroma staleness recovery"
    - "Admin panel for triggering re-index"
    - "Multi-endpoint build documentation"
  affects:
    - "Document ingestion (persistent file storage)"
tech_stack:
  added:
    - "backend/scripts/reindex.py"
    - "AdminController.java"
  patterns:
    - "Python script as subprocess called via JSON from Java"
    - "Persistent file storage for reindex capability"
key_files:
  created:
    - "backend/scripts/reindex.py"
    - "backend/src/main/java/com/aikb/controller/AdminController.java"
    - "frontend/src/pages/admin.vue"
  modified:
    - "backend/src/main/java/com/aikb/service/DocumentService.java"
    - "backend/src/main/java/com/aikb/entity/Document.java"
    - "backend/src/main/java/com/aikb/mapper/DocumentMapper.java"
    - "backend/src/main/resources/schema.sql"
    - "backend/src/main/resources/application.yml"
    - "frontend/package.json"
    - "frontend/src/router/index.js"
key_decisions:
  - "Store uploaded files persistently in backend/data/files/ for reindex capability"
  - "Add file_path column to documents table to track stored file location"
  - "AdminController requires JWT authentication like other API endpoints"
patterns_established:
  - "Java service calls Python script via ProcessBuilder with JSON stdin/stdout"
  - "Reindex clears Chroma collection and rebuilds from stored files"
requirements_completed: []
coverage:
  - id: D1
    description: "Re-index script (backend/scripts/reindex.py) accepts kb_id and rebuilds Chroma index"
    verification:
      - kind: other
        ref: "Script structure validated against extract_and_embed.py pattern"
        status: unknown
    human_judgment: true
    rationale: "No Bash tool available to execute Python script for live verification"
  - id: D2
    description: "AdminController POST /api/admin/reindex/{knowledgeBaseId} triggers reindex"
    verification:
      - kind: other
        ref: "Code review: endpoint signature matches plan must_haves"
        status: unknown
    human_judgment: true
    rationale: "No Bash tool available to start backend and test endpoint"
  - id: D3
    description: "Admin page (/admin route) has re-index button calling POST /api/admin/reindex/{id}"
    verification:
      - kind: automated_ui
        ref: "frontend/src/pages/admin.vue"
        status: unknown
    human_judgment: true
    rationale: "No Bash tool available to run npm build"
  - id: D4
    description: "package.json has build:h5 script for multi-endpoint deployment"
    verification:
      - kind: other
        ref: "frontend/package.json scripts section"
        status: pass
    human_judgment: false
duration: "~15 minutes"
completed: "2026-10-01"
status: complete

# Phase 6 Plan 2: Multi-endpoint Build Support + Re-index Pipeline Summary

**Re-index pipeline for Chroma staleness recovery and admin panel for triggering rebuilds; H5 build documented in package.json**

## Performance

- **Duration:** ~15 minutes
- **Started:** 2026-10-01
- **Completed:** 2026-10-01
- **Tasks:** 2
- **Files modified:** 9

## Accomplishments

### Task 1: Re-index pipeline for stale Chroma vectors

**Status:** COMPLETED

Created `backend/scripts/reindex.py`:
- Accepts kbId, userId via JSON stdin
- Deletes existing Chroma collection for the knowledge base
- Re-embeds all documents passed in the documents list
- Returns reindexed document count and total chunks

Created `backend/src/main/java/com/aikb/controller/AdminController.java`:
- `POST /api/admin/reindex/{knowledgeBaseId}` - triggers re-index via DocumentService
- `GET /api/admin/status?kbId=X` - returns document count and total chunks

Modified `backend/src/main/java/com/aikb/service/DocumentService.java`:
- Added `reindexKnowledgeBase(kbId, userId)` method
- Added `getDocumentsByKbId(kbId)` method
- Added `findReindexScriptPath()` method
- Modified `uploadDocument()` to store files persistently (not deleted after processing)

Modified `backend/src/main/java/com/aikb/entity/Document.java`:
- Added `filePath` field to track stored file location

Modified `backend/src/main/java/com/aikb/mapper/DocumentMapper.java`:
- Added `selectByKbId()` method

Modified `backend/src/main/resources/schema.sql`:
- Added `file_path VARCHAR(500)` column to documents table

Modified `backend/src/main/resources/application.yml`:
- Added `files.persist-directory: backend/data/files` configuration

### Task 2: Multi-endpoint build notes

**Status:** COMPLETED

Modified `frontend/package.json`:
- Added `build:h5` script: `vite build --mode h5`
- Default `build` already produces H5-ready output via vite.config.js base: './'

Created `frontend/src/pages/admin.vue`:
- Knowledge base selector dropdown
- Re-index button triggering POST /api/admin/reindex/{kbId}
- Status display showing document count and total chunks
- Success/error message display

Modified `frontend/src/router/index.js`:
- Added /admin route pointing to admin.vue

## Files Created/Modified

- `backend/scripts/reindex.py` - Chroma reindex script (clears and rebuilds collection)
- `backend/src/main/java/com/aikb/controller/AdminController.java` - Admin API endpoints
- `backend/src/main/java/com/aikb/service/DocumentService.java` - Added reindex and persistent file storage
- `backend/src/main/java/com/aikb/entity/Document.java` - Added filePath field
- `backend/src/main/java/com/aikb/mapper/DocumentMapper.java` - Added selectByKbId method
- `backend/src/main/resources/schema.sql` - Added file_path column
- `backend/src/main/resources/application.yml` - Added files.persist-directory config
- `frontend/src/pages/admin.vue` - Admin panel with re-index UI
- `frontend/package.json` - Added build:h5 script
- `frontend/src/router/index.js` - Added /admin route

## Decisions Made

- **Store files persistently for reindex**: Original design deleted temp files after upload. Reindex requires original files, so added persistent storage at `backend/data/files/{userId}/{kbId}/`
- **file_path column added to documents**: Needed to track where each uploaded file is stored so reindex can find them
- **AdminController uses Authentication parameter**: Follows same pattern as ChatController for extracting userId from JWT

## Deviations from Plan

**None - plan executed as written, with necessary supporting changes to enable reindex functionality.**

Supporting changes (not deviations - required for correctness):
- Added file_path column to documents table (reindex needs to locate stored files)
- Added files.persist-directory config and storage logic in DocumentService (files were deleted after upload)

## Issues Encountered

- **No Bash tool available**: Cannot execute Python scripts or run npm build for live verification
- **H2 database limitation**: Python reindex script cannot directly query H2 (in-memory MySQL-compatible DB), so AdminController gathers documents via Java and passes them to the script

## Verification Results

| Check | Result |
|-------|--------|
| reindex.py accepts kb_id parameter | SCRIPT CREATED (verified structure) |
| AdminController has POST /api/admin/reindex/{id} | PASS (code review) |
| AdminController has GET /api/admin/status | PASS (code review) |
| Admin page has re-index button | PASS (admin.vue created) |
| package.json has build:h5 script | PASS |
| Frontend router has /admin route | PASS |

## Next Phase Readiness

- Re-index pipeline complete and ready for testing
- Admin panel ready for deployment
- Persistent file storage enables future reindex operations

---
*Phase: 06-maintenance-polish*
*Plan: 06-02*
*Completed: 2026-10-01*
