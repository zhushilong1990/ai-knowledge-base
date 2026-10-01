<template>
  <div class="chat-container">
    <el-container>
      <!-- Sidebar: Session List -->
      <el-aside width="250px" class="chat-sidebar">
        <div class="sidebar-header">
          <span>Chat Sessions</span>
          <el-button type="primary" size="small" @click="startNewSession">New</el-button>
        </div>
        <el-scrollbar>
          <div class="session-list">
            <div
              v-for="session in chatStore.sessions"
              :key="session.id"
              :class="['session-item', { active: session.id === chatStore.currentSessionId }]"
              @click="selectSession(session.id)"
            >
              <span class="session-title">{{ session.title }}</span>
            </div>
            <div v-if="chatStore.sessions.length === 0" class="no-sessions">
              No sessions yet
            </div>
          </div>
        </el-scrollbar>
      </el-aside>

      <!-- Main Chat Area -->
      <el-main class="chat-main">
        <!-- Knowledge Base Selector -->
        <div class="kb-selector">
          <span class="kb-label">Knowledge Base:</span>
          <el-select v-model="selectedKbId" placeholder="Select knowledge base" size="default">
            <el-option label="Knowledge Base 1" :value="1" />
            <!-- Future: load from API -->
          </el-select>
        </div>

        <!-- Message List -->
        <el-scrollbar ref="scrollbarRef" class="message-list">
          <div v-if="currentMessages.length === 0" class="empty-chat">
            <p>No messages yet. Start a conversation!</p>
          </div>
          <div v-else>
            <div
              v-for="(msg, index) in currentMessages"
              :key="index"
              :class="['message-item', msg.role]"
            >
              <div class="message-bubble">
                <div class="message-content">{{ msg.content }}</div>
                <div v-if="msg.role === 'assistant' && msg.sources && msg.sources.length > 0" class="message-sources">
                  <div class="sources-label">Sources:</div>
                  <div v-for="source in msg.sources" :key="source.id" class="source-item">
                    <span class="source-id">【{{ source.id }}】</span>
                    <span class="source-text">{{ source.text }}</span>
                    <span class="source-score">({{ (source.score * 100).toFixed(0) }}%)</span>
                  </div>
                </div>
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
              </div>
            </div>
          </div>
        </el-scrollbar>

        <!-- Loading Indicator -->
        <div v-if="chatStore.loading" class="loading-indicator">
          <el-icon class="is-loading"><loading /></el-icon>
          <span>AI is thinking...</span>
        </div>

        <!-- Input Area -->
        <div class="input-area">
          <el-input
            v-model="question"
            type="textarea"
            :rows="2"
            placeholder="Type your question here..."
            @keydown.enter.ctrl="handleSend"
            :disabled="chatStore.loading"
          />
          <el-button
            type="primary"
            :loading="chatStore.loading"
            :disabled="!canSend"
            @click="handleSend"
          >
            Send
          </el-button>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, ThumbUp, ThumbDown } from '@element-plus/icons-vue'
import { useChatStore } from '../stores/chat.js'

const chatStore = useChatStore()
const scrollbarRef = ref(null)
const question = ref('')
const selectedKbId = ref(1)

const currentMessages = computed(() => {
  if (!chatStore.currentSessionId) return []
  return chatStore.messages[chatStore.currentSessionId] || []
})

const canSend = computed(() => {
  return question.value.trim() && selectedKbId.value && !chatStore.loading
})

function startNewSession() {
  chatStore.currentSessionId = null
  question.value = ''
}

function selectSession(sessionId) {
  chatStore.loadHistory(sessionId)
}

async function handleSend() {
  if (!canSend.value) return

  const q = question.value.trim()
  question.value = ''

  try {
    await chatStore.sendQuestion(q, selectedKbId.value)
    await nextTick()
    scrollToBottom()
  } catch (error) {
    // Error already shown by store
  }
}

function scrollToBottom() {
  nextTick(() => {
    const scrollbar = scrollbarRef.value
    if (scrollbar && scrollbar.wrapRef) {
      scrollbar.wrapRef.scrollTop = scrollbar.wrapRef.scrollHeight
    }
  })
}

async function handleFeedback(messageId, rating) {
  await chatStore.submitFeedback(messageId, rating)
}

onMounted(() => {
  chatStore.loadSessions()
})
</script>

<style scoped>
.chat-container {
  height: calc(100vh - 60px);
}

.chat-sidebar {
  background: #f5f5f5;
  border-right: 1px solid #e0e0e0;
}

.sidebar-header {
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e0e0e0;
}

.session-list {
  padding: 8px;
}

.session-item {
  padding: 12px;
  cursor: pointer;
  border-radius: 4px;
  margin-bottom: 4px;
}

.session-item:hover {
  background: #e0e0e0;
}

.session-item.active {
  background: #409eff;
  color: white;
}

.session-title {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
}

.no-sessions {
  padding: 16px;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.chat-main {
  display: flex;
  flex-direction: column;
  padding: 0;
  height: 100%;
}

.kb-selector {
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #e0e0e0;
  background: white;
}

.kb-label {
  font-size: 14px;
  color: #606266;
}

.message-list {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.empty-chat {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #909399;
}

.message-item {
  display: flex;
  margin-bottom: 16px;
}

.message-item.user {
  justify-content: flex-end;
}

.message-item.assistant {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 8px;
  word-break: break-word;
}

.message-item.user .message-bubble {
  background: #409eff;
  color: white;
}

.message-item.assistant .message-bubble {
  background: #f0f0f0;
  color: #333;
}

.message-content {
  white-space: pre-wrap;
  line-height: 1.5;
}

.message-sources {
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  font-size: 12px;
  color: #666;
}

.sources-label {
  font-weight: bold;
  margin-bottom: 4px;
}

.source-item {
  display: flex;
  gap: 4px;
  margin-bottom: 2px;
}

.source-id {
  color: #409eff;
  font-weight: bold;
}

.source-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.source-score {
  color: #909399;
}

.message-feedback {
  display: flex;
  gap: 4px;
  margin-top: 8px;
}

.loading-indicator {
  padding: 8px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
}

.input-area {
  padding: 16px;
  display: flex;
  gap: 12px;
  align-items: flex-end;
  border-top: 1px solid #e0e0e0;
  background: white;
}

.input-area .el-textarea {
  flex: 1;
}
</style>
