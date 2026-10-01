# Phase 5: History & Feedback - Research

**研究日期:** 2026-10-01
**技术领域:** 对话历史存储与用户反馈收集
**置信度:** HIGH

## 摘要

Phase 5 的目标是实现对话历史查看和用户反馈（点赞/点踩）功能。通过对现有代码库的分析，发现 `chat_session` 和 `chat_message` 表已存在并正常工作，仅缺少反馈功能。需要在数据库层添加反馈表，在后端添加反馈 API，在前端添加点赞/点踩 UI 组件。

**主要建议:** 复用现有 MyBatis-Plus 模式，新增 `chat_feedback` 表和对应 Entity/Mapper/Service/Controller，采用与现有 ChatController 一致的错误处理风格。

---

## 用户约束（来自 CONTEXT.md）

> 本阶段为 Phase 5，在 CONTEXT.md 不存在的情况下，基于 ROADMAP.md 和 REQUIREMENTS.md 驱动。

**Phase 5 目标:** 用户可以回顾过去的对话并提供反馈
**依赖阶段:** Phase 3 (Chat/QA)
**需求:** CHAT-02

**成功标准（必须为真）:**
1. 用户可以查看过去会话列表
2. 用户可以点击进入会话查看完整对话历史
3. 用户可以对回答点赞或点踩
4. 用户反馈被存储并与回答关联
5. 对话跨浏览器会话持久化

---

## 架构责任映射

| 功能 | 主要层级 | 次要层级 | 原因 |
|------|---------|---------|------|
| 对话历史存储 | MySQL (chat_session, chat_message) | - | 已有表结构，只需查询 |
| 用户反馈存储 | MySQL (chat_feedback) | - | 新增表，记录用户对消息的评价 |
| 会话列表查询 | Backend API | Frontend Store | ChatService.getSessionsByUserId 已实现 |
| 反馈提交 | Frontend UI | Backend API | 需要新增按钮和 API |
| 反馈查询 | Backend API | Frontend Store | 支持查看历史反馈 |

---

## 现有系统分析

### 已有的数据库表 [VERIFIED: backend/src/main/resources/schema.sql:27-44]

```sql
CREATE TABLE IF NOT EXISTS chat_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    sources JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session_id (session_id)
);
```

### 已有的 Java Entity [VERIFIED: backend/src/main/java/com/aikb/entity/]

| Entity | 字段 | 位置 |
|--------|------|------|
| ChatSession | id, userId, title, createdAt, updatedAt | ChatSession.java:22-37 |
| ChatMessage | id, sessionId, role, content, sources, createdAt | ChatMessage.java:22-40 |

### 已有的 API 端点 [VERIFIED: backend/src/main/java/com/aikb/controller/ChatController.java]

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /chat/sessions | 获取用户会话列表 |
| GET | /chat/history/{sessionId} | 获取会话消息历史 |
| POST | /chat/ask | 发送问题并获取回答 |

### 已有的 Mapper [VERIFIED: backend/src/main/java/com/aikb/mapper/]

| Mapper | 方法 |
|--------|------|
| ChatSessionMapper | selectList (BaseMapper) |
| ChatMessageMapper | selectBySessionId (自定义 SQL) |

---

## 数据库设计

### 新增表: chat_feedback

```sql
CREATE TABLE IF NOT EXISTS chat_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating VARCHAR(10) NOT NULL COMMENT 'like/dislike',
    feedback_reason VARCHAR(500) COMMENT 'Optional reason for feedback',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_message_user (message_id, user_id),
    INDEX idx_message_id (message_id),
    INDEX idx_user_id (user_id)
);
```

**设计决策:**
- `message_id` 关联到 `chat_message` 表的 `id` 字段
- `user_id` 用于权限校验（用户只能对自己发出的反馈负责）
- `rating` 使用 VARCHAR(10) 存储 'like' 或 'dislike'，便于扩展
- `uk_message_user` 唯一约束确保每个用户对每条消息只能反馈一次
- `feedback_reason` 可选字段，支持用户填写反馈原因

### 修改: ChatMessage 实体

在 `ChatMessage.java` 中添加 `feedback` 字段用于联表查询时携带反馈状态（非持久化字段）:

```java
@TableField(exist = false)
private String feedback;  // 'like' / 'dislike' / null
```

---

## API 设计

### 1. 提交反馈

**端点:** `POST /api/chat/feedback`

**请求体:**
```json
{
  "messageId": 123,
  "rating": "like",
  "feedbackReason": "回答准确"  // 可选
}
```

**响应 (200 OK):**
```json
{
  "id": 1,
  "messageId": 123,
  "rating": "like",
  "createdAt": "2026-10-01T10:00:00"
}
```

**错误响应:**
- 400: 无效的 rating 值
- 403: 消息不属于该用户
- 404: 消息不存在

### 2. 获取消息反馈状态

**端点:** `GET /api/chat/feedback/{messageId}`

**响应 (200 OK):**
```json
{
  "messageId": 123,
  "rating": "like",
  "feedbackReason": "回答准确"
}
```

**错误响应:**
- 404: 反馈不存在

### 3. 更新反馈

**端点:** `PUT /api/chat/feedback/{messageId}`

**请求体:**
```json
{
  "rating": "dislike",
  "feedbackReason": "回答有误"  // 可选
}
```

---

## 后端实现模式

### Entity 模式 (参考 ChatMessage.java)

```java
// ChatFeedback.java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("chat_feedback")
public class ChatFeedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("message_id")
    private Long messageId;

    @TableField("user_id")
    private Long userId;

    @TableField("rating")
    private String rating;  // 'like' or 'dislike'

    @TableField("feedback_reason")
    private String feedbackReason;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
```

### Mapper 模式 (参考 ChatMessageMapper.java)

```java
// ChatFeedbackMapper.java
@Mapper
public interface ChatFeedbackMapper extends BaseMapper<ChatFeedback> {

    @Select("SELECT * FROM chat_feedback WHERE message_id = #{messageId} AND user_id = #{userId}")
    ChatFeedback selectByMessageIdAndUserId(@Param("messageId") Long messageId, @Param("userId") Long userId);

    @Select("SELECT * FROM chat_feedback WHERE message_id = #{messageId}")
    List<ChatFeedback> selectByMessageId(@Param("messageId") Long messageId);
}
```

### Service 模式 (参考 ChatService.java)

```java
// ChatFeedbackService.java
@Service
public class ChatFeedbackService {

    private final ChatFeedbackMapper feedbackMapper;
    private final ChatMessageMapper messageMapper;

    public ChatFeedbackService(ChatFeedbackMapper feedbackMapper, ChatMessageMapper messageMapper) {
        this.feedbackMapper = feedbackMapper;
        this.messageMapper = messageMapper;
    }

    public ChatFeedback submitFeedback(Long messageId, Long userId, String rating, String reason) {
        // 校验消息存在且属于用户有权限反馈的范围
        ChatMessage message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new RuntimeException("Message not found");
        }

        // 检查是否已存在反馈
        ChatFeedback existing = feedbackMapper.selectByMessageIdAndUserId(messageId, userId);
        if (existing != null) {
            // 更新现有反馈
            existing.setRating(rating);
            existing.setFeedbackReason(reason);
            existing.setUpdatedAt(LocalDateTime.now());
            feedbackMapper.updateById(existing);
            return existing;
        }

        // 新建反馈
        ChatFeedback feedback = ChatFeedback.builder()
                .messageId(messageId)
                .userId(userId)
                .rating(rating)
                .feedbackReason(reason)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        feedbackMapper.insert(feedback);
        return feedback;
    }

    public ChatFeedback getFeedback(Long messageId, Long userId) {
        return feedbackMapper.selectByMessageIdAndUserId(messageId, userId);
    }
}
```

### Controller 模式 (参考 ChatController.java)

```java
// ChatController.java (新增方法)
@PostMapping("/feedback")
public ResponseEntity<?> submitFeedback(@RequestBody FeedbackRequest request, Authentication auth) {
    Long userId = extractUserId(auth);
    try {
        ChatFeedback feedback = chatFeedbackService.submitFeedback(
            request.getMessageId(),
            userId,
            request.getRating(),
            request.getFeedbackReason()
        );
        return ResponseEntity.ok(feedback);
    } catch (RuntimeException e) {
        if (e.getMessage().contains("not found")) {
            return ResponseEntity.status(404).body(Collections.singletonMap("error", e.getMessage()));
        }
        return ResponseEntity.status(500).body(Collections.singletonMap("error", "Failed to submit feedback"));
    }
}

@GetMapping("/feedback/{messageId}")
public ResponseEntity<?> getFeedback(@PathVariable Long messageId, Authentication auth) {
    Long userId = extractUserId(auth);
    ChatFeedback feedback = chatFeedbackService.getFeedback(messageId, userId);
    if (feedback == null) {
        return ResponseEntity.status(404).body(Collections.singletonMap("error", "Feedback not found"));
    }
    return ResponseEntity.ok(feedback);
}
```

---

## 前端实现模式

### Pinia Store 扩展 (参考 chat.js)

```javascript
// stores/chat.js 新增
import { submitFeedback as apiSubmitFeedback, getFeedback as apiGetFeedback } from '../api/chat.js'

// 在 useChatStore 中添加
async function submitFeedback(messageId, rating, reason) {
  try {
    const result = await apiSubmitFeedback(messageId, rating, reason)
    // 更新消息的反馈状态
    for (const sessionId in messages.value) {
      const msg = messages.value[sessionId].find(m => m.id === messageId)
      if (msg) {
        msg.feedback = rating
        break
      }
    }
    return result
  } catch (error) {
    ElMessage.error('Failed to submit feedback')
    throw error
  }
}

async function loadFeedback(messageId) {
  try {
    return await apiGetFeedback(messageId)
  } catch (error) {
    return null
  }
}
```

### API 模块扩展 (参考 chat.js)

```javascript
// api/chat.js 新增
export async function submitFeedback(messageId, rating, reason) {
  const response = await api.post('/chat/feedback', {
    messageId,
    rating,
    feedbackReason: reason
  })
  return response.data
}

export async function getFeedback(messageId) {
  const response = await api.get(`/chat/feedback/${messageId}`)
  return response.data
}
```

### 组件修改: 消息气泡添加反馈按钮

```vue
<!-- chat.vue message-bubble 内修改 -->
<div class="message-bubble">
  <div class="message-content">{{ msg.content }}</div>

  <!-- 反馈按钮 (仅 assistant 消息显示) -->
  <div v-if="msg.role === 'assistant'" class="message-feedback">
    <el-button
      :type="msg.feedback === 'like' ? 'success' : 'default'"
      size="small"
      circle
      @click="handleFeedback(msg.id, 'like')"
    >
      <ThumbUp />
    </el-button>
    <el-button
      :type="msg.feedback === 'dislike' ? 'danger' : 'default'"
      size="small"
      circle
      @click="handleFeedback(msg.id, 'dislike')"
    >
      <ThumbDown />
    </el-button>
  </div>

  <!-- Sources -->
  <div v-if="msg.sources && msg.sources.length > 0" class="message-sources">
    <!-- ... existing sources code ... -->
  </div>
</div>
```

### 反馈处理函数

```javascript
import { ThumbUp, ThumbDown } from '@element-plus/icons-vue'

async function handleFeedback(messageId, rating) {
  await chatStore.submitFeedback(messageId, rating)
}
```

---

## 技术决策

| 决策点 | 选择 | 原因 |
|--------|------|------|
| 反馈粒度 | 消息级别 | 用户对单条 AI 回答进行评价，消息是最自然的粒度 |
| 唯一约束 | (message_id, user_id) | 防止同一用户对同一消息重复提交反馈 |
| 反馈更新 | 支持覆盖 | 用户可以从 like 改为 dislike |
| 反馈原因 | 可选字段 | 降低用户交互成本，同时保留扩展性 |
| UI 位置 | 消息气泡下方 | 符合常见 Chat UI 惯例（ChatGPT 风格） |
| 图标选择 | ThumbUp/ThumbDown | Element Plus 内置图标，无需额外安装 |

---

## 常见陷阱

### 1. 反馈提交时未校验消息所有权
**问题:** 用户可能对不属于自己的对话中的消息提交反馈
**避免:** 在 Service 层校验消息所属的 session 是否属于当前用户

### 2. 重复反馈覆盖丢失原 feedback_reason
**问题:** 更新反馈时未保留原有 reason
**避免:** 如果新请求没有 reason，保留数据库中的旧值

### 3. 前端反馈状态未及时更新
**问题:** 提交反馈后 UI 未刷新
**避免:** 在 Store 的 submitFeedback 中更新本地消息的 feedback 字段

### 4. 会话列表未按更新时间排序
**问题:** 新消息的会话未出现在列表顶部
**避免:** ChatService.askQuestion 在插入消息后更新 session 的 updatedAt

---

## 不需要手写的内容

| 问题 | 不手写 | 使用 |
|------|--------|------|
| JWT 认证 | 否 | 现有 Spring Security Filter |
| 会话存在性校验 | 否 | ChatService.getChatHistory 已有校验 |
| MySQL 连接管理 | 否 | MyBatis-Plus 自动管理 |
| 反馈统计 | 否 | 可后续扩展分析功能 |

---

## 假设日志

> 以下为 [ASSUMED] 级别的发现，需要用户确认后再锁定。

| # | 假设 | 风险 |
|---|------|------|
| A1 | 用户反馈只需要 like/dislike 两种状态，不需要星级评分 | 如果需要更细粒度评分，需要调整表结构和前端 UI |
| A2 | 反馈原因最大长度为 500 字符 | 如果需要更长的文本，需要扩大 VARCHAR 长度 |
| A3 | 每个用户对每条消息只能提交一次反馈（但可更新） | 如果需要支持多次反馈，移除唯一约束 |

---

## 开放问题

1. **反馈数据的后续使用**
   - 目前只存储反馈，是否需要根据反馈优化 RAG 检索或 LLM 提示词？
   - 建议：当前阶段仅做存储，后续 Phase 6 可扩展分析功能

2. **Dislike 原因收集**
   - 当前设计支持可选的 feedback_reason
   - 是否需要在点踩时强制要求填写原因以便改进？
   - 建议：保持可选，降低用户交互成本

3. **多端兼容性**
   - 微信小程序是否需要不同的反馈 UI 交互方式？
   - 建议：当前使用 Element Plus 组件，需确认 uni-app 是否兼容

---

## 环境可用性

> 本阶段为纯后端 API + 前端 UI 修改，无外部依赖。

**Step 2.6: SKIPPED** — 本阶段不依赖额外外部工具/服务

---

## 验证架构

### 测试框架
| 属性 | 值 |
|------|-----|
| Framework | JUnit 5 (Spring Boot Starter Test) |
| Config file | 无 — 使用 Spring Boot 默认测试配置 |
| Quick run command | `mvn test -Dtest=ChatFeedbackServiceTest` |
| Full suite command | `mvn test` |

### 需求 → 测试映射
| 需求 ID | 行为 | 测试类型 | 自动化命令 |
|---------|------|---------|-----------|
| CHAT-02 | 用户可以查看过去会话列表 | 集成 | `GET /chat/sessions` |
| CHAT-02 | 用户可以查看会话消息历史 | 集成 | `GET /chat/history/{id}` |
| CHAT-02 | 用户可以对回答点赞 | 单元 | `ChatFeedbackServiceTest.submitFeedback_like` |
| CHAT-02 | 用户可以对回答点踩 | 单元 | `ChatFeedbackServiceTest.submitFeedback_dislike` |
| CHAT-02 | 用户反馈被存储并关联到消息 | 集成 | DB 验证 |
| CHAT-02 | 对话跨浏览器会话持久化 | 集成 | 多会话查询验证 |

### Wave 0 差距
- [ ] `backend/src/test/java/com/aikb/service/ChatFeedbackServiceTest.java` — 测试反馈 Service
- [ ] `backend/src/test/java/com/aikb/controller/ChatControllerFeedbackTest.java` — 测试反馈 API
- [ ] 现有测试基础设施已就绪 (Spring Boot Test)

---

## 来源

### 主要来源 (HIGH confidence)
- [VERIFIED: backend/src/main/resources/schema.sql] — 现有数据库表结构
- [VERIFIED: backend/src/main/java/com/aikb/entity/ChatMessage.java] — 现有 Entity 模式
- [VERIFIED: backend/src/main/java/com/aikb/mapper/ChatMessageMapper.java] — 现有 Mapper 模式
- [VERIFIED: backend/src/main/java/com/aikb/controller/ChatController.java] — 现有 Controller 模式
- [VERIFIED: backend/src/main/java/com/aikb/service/ChatService.java] — 现有 Service 模式
- [VERIFIED: frontend/src/stores/chat.js] — 现有 Pinia Store 模式
- [VERIFIED: frontend/src/api/chat.js] — 现有 API 模块模式
- [VERIFIED: frontend/src/pages/chat.vue] — 现有 Chat UI 组件

### 次要来源 (MEDIUM confidence)
- Element Plus Icons 官方文档 — ThumbUp/ThumbDown 图标

---

## 元数据

**置信度分布:**
- 标准栈: HIGH — 100% 复用现有代码模式
- 架构: HIGH — 基于已有系统设计的自然扩展
- 陷阱: HIGH — 基于 Java/Vue 常见问题总结

**研究日期:** 2026-10-01
**有效期限:** 30 天（技术栈稳定）
