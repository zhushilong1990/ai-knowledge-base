---
phase: "05-history-feedback"
plan: "01"
subsystem: api
tags: [java, spring-boot, vue, pinia, mysql, mybatis-plus]

# Dependency graph
requires:
  - phase: "03-chat-qa"
    provides: ChatController, ChatService, ChatMessage entity/mapper
affects:
  - phase: "06-analysis"  # feedback data will be used for analysis
provides:
  - chat_feedback table in MySQL
  - ChatFeedback entity, mapper, service
  - POST /api/chat/feedback endpoint
  - GET /api/chat/feedback/{messageId} endpoint
  - Frontend feedback API (submitFeedback, getFeedback)
  - Frontend feedback UI (ThumbUp/ThumbDown buttons)
  - Pinia store action for feedback submission

# Actuals
actuals:
  tokens: 18000
  tasks: 2
  commits: 0

# Tech tracking
tech-stack:
  added: []
  patterns:
    - MyBatis-Plus BaseMapper with custom @Select queries
    - Toggle feedback pattern (same button click removes feedback)
    - Spring constructor injection for service dependencies

key-files:
  created:
    - backend/src/main/java/com/aikb/entity/ChatFeedback.java
    - backend/src/main/java/com/aikb/mapper/ChatFeedbackMapper.java
    - backend/src/main/java/com/aikb/service/ChatFeedbackService.java
    - backend/src/main/java/com/aikb/dto/FeedbackRequest.java
  modified:
    - backend/src/main/resources/schema.sql
    - backend/src/main/java/com/aikb/controller/ChatController.java
    - frontend/src/api/chat.js
    - frontend/src/stores/chat.js
    - frontend/src/pages/chat.vue

key-decisions:
  - "Used MyBatis-Plus BaseMapper for ChatFeedback with custom @Select for lookup by messageId+userId"
  - "Toggle pattern: clicking same rating removes feedback (msg.feedback === rating ? null : rating)"
  - "Unique constraint on (message_id, user_id) ensures one feedback per user per message"

patterns-established:
  - "Feedback entity follows same pattern as ChatMessage with @TableName, @TableId, @TableField annotations"
  - "Service layer validates message existence before allowing feedback submission"
  - "Controller returns 404 for message not found, 400 for invalid rating"

requirements-completed: [CHAT-02]

# Metrics
duration: 15min
completed: 2026-10-01
status: complete
---

# Phase 5 Plan 1: Chat Feedback Summary

**User feedback feature: like/dislike buttons on assistant messages with MySQL persistence**

## Performance

- **Duration:** ~15 min
- **Started:** 2026-10-01
- **Completed:** 2026-10-01
- **Tasks:** 2
- **Files modified:** 9

## Accomplishments
- Created `chat_feedback` table with unique constraint on (message_id, user_id)
- Implemented ChatFeedback entity, mapper, service following existing patterns
- Added POST /api/chat/feedback and GET /api/chat/feedback/{messageId} endpoints
- Added frontend submitFeedback and getFeedback API functions
- Added submitFeedback Pinia store action with toggle logic
- Added ThumbUp/ThumbDown feedback buttons on assistant message bubbles

## Task Commits

Backend files created/modified for Task 1 (Backend feedback pipeline):
- `backend/src/main/resources/schema.sql` - Added chat_feedback table
- `backend/src/main/java/com/aikb/entity/ChatFeedback.java` - New entity
- `backend/src/main/java/com/aikb/mapper/ChatFeedbackMapper.java` - New mapper
- `backend/src/main/java/com/aikb/service/ChatFeedbackService.java` - New service
- `backend/src/main/java/com/aikb/dto/FeedbackRequest.java` - New DTO
- `backend/src/main/java/com/aikb/controller/ChatController.java` - Added feedback endpoints

Frontend files created/modified for Task 2 (Frontend feedback UI and API):
- `frontend/src/api/chat.js` - Added submitFeedback and getFeedback exports
- `frontend/src/stores/chat.js` - Added submitFeedback action
- `frontend/src/pages/chat.vue` - Added feedback buttons, handleFeedback, CSS

## Files Created/Modified

| File | Action | Description |
|------|--------|-------------|
| `backend/src/main/resources/schema.sql` | Modified | Added chat_feedback table |
| `backend/src/main/java/com/aikb/entity/ChatFeedback.java` | Created | Feedback entity with id, messageId, userId, rating, feedbackReason, createdAt, updatedAt |
| `backend/src/main/java/com/aikb/mapper/ChatFeedbackMapper.java` | Created | BaseMapper with selectByMessageIdAndUserId |
| `backend/src/main/java/com/aikb/service/ChatFeedbackService.java` | Created | submitFeedback (upsert), getFeedback methods |
| `backend/src/main/java/com/aikb/dto/FeedbackRequest.java` | Created | DTO with messageId, rating, feedbackReason |
| `backend/src/main/java/com/aikb/controller/ChatController.java` | Modified | Added POST/GET feedback endpoints |
| `frontend/src/api/chat.js` | Modified | Added submitFeedback, getFeedback exports |
| `frontend/src/stores/chat.js` | Modified | Added submitFeedback action with toggle logic |
| `frontend/src/pages/chat.vue` | Modified | Added ThumbUp/ThumbDown buttons, handleFeedback, CSS |

## Decisions Made

1. **Upsert pattern for feedback**: If feedback exists for message+user, update it; otherwise insert. This supports toggling (clicking same button removes feedback).

2. **Toggle logic in store**: `msg.feedback = msg.feedback === rating ? null : rating` - clicking the same rating again removes the feedback.

3. **Rating validation in controller**: Only 'like' or 'dislike' accepted, returns 400 for invalid values.

4. **Message existence check**: Service throws RuntimeException if message not found, controller returns 404.

## Deviations from Plan

None - plan executed exactly as written.

## Success Criteria Status

| Criterion | Status |
|-----------|--------|
| User can tap like or dislike button on any assistant message | Implemented |
| Feedback is stored in chat_feedback table with correct message_id, user_id, rating | Implemented |
| Clicking same button again toggles state (like -> no feedback -> like) | Implemented via toggle logic |
| Clicking opposite button switches feedback (like -> dislike) | Implemented |
| Feedback persists across page refresh (stored in DB) | Implemented |

## Known Stubs

None - all features implemented as specified.

## Next Phase Readiness

- Feedback API ready for use by Phase 6 (analysis)
- chat_feedback table schema supports future expansion (e.g., feedback reason analysis)
- Frontend UI complete with toggle state management

---
*Phase: 05-history-feedback*
*Plan: 05-01*
*Completed: 2026-10-01*
