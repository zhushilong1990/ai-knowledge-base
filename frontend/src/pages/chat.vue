<template>
  <div class="chat-container">
    <el-container>
      <!-- 侧边栏：会话列表 -->
      <el-aside width="250px" class="chat-sidebar">
        <div class="sidebar-header">
          <router-link to="/home"><el-button type="text" size="small">🏠</el-button></router-link>
          <span style="flex:1;text-align:center;font-weight:bold">聊天记录</span>
          <el-button type="primary" size="small" @click="startNewSession">+</el-button>
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
              <el-button
                type="danger"
                size="small"
                class="delete-btn"
                @click.stop="handleDeleteSession(session.id)"
                :icon="Delete"
                circle
              />
            </div>
            <div v-if="chatStore.sessions.length === 0" class="no-sessions">
              暂无会话
            </div>
          </div>
        </el-scrollbar>
      </el-aside>

      <!-- 主聊天区域 -->
      <el-main class="chat-main">
        <!-- 返回按钮 -->
        <div v-if="chatStore.currentSessionId" class="back-bar">
          <el-button :icon="ArrowLeft" @click="goBack">返回会话列表</el-button>
        </div>

        <!-- 知识库选择器 -->
        <div class="kb-selector">
          <span class="kb-label">知识库：</span>
          <el-select v-model="selectedKbId" placeholder="请选择知识库" size="default">
            <el-option
              v-for="kb in kbStore.knowledgeBases"
              :key="kb.id"
              :label="kb.name"
              :value="kb.id"
            />
          </el-select>
        </div>

        <!-- 消息列表 -->
        <el-scrollbar ref="scrollbarRef" class="message-list">
          <div v-if="currentMessages.length === 0" class="empty-chat">
            <p>开始提问吧！</p>
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
                  <div class="sources-label">参考来源：</div>
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
                    @click="handleFeedback(msg.id, 'like')"
                  >
                    👍 有用
                  </el-button>
                  <el-button
                    :type="msg.feedback === 'dislike' ? 'danger' : 'default'"
                    size="small"
                    @click="handleFeedback(msg.id, 'dislike')"
                  >
                    👎 没用
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-scrollbar>

        <!-- 加载指示器 -->
        <div v-if="chatStore.loading" class="loading-indicator">
          <el-icon class="is-loading"><loading /></el-icon>
          <span>AI 思考中...</span>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <el-input
            v-model="question"
            type="textarea"
            :rows="2"
            placeholder="请输入您的问题，按 Ctrl+Enter 发送..."
            @keydown.enter.ctrl="handleSend"
            :disabled="chatStore.loading"
          />
          <el-button
            type="primary"
            :loading="chatStore.loading"
            :disabled="!canSend"
            @click="handleSend"
          >
            发送
          </el-button>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Delete, ArrowLeft } from '@element-plus/icons-vue'
import { useChatStore } from '../stores/chat.js'
import { useKnowledgeBaseStore } from '../stores/knowledgeBase.js'

const chatStore = useChatStore()
const kbStore = useKnowledgeBaseStore()
const scrollbarRef = ref(null)
const question = ref('')
const selectedKbId = ref(null)

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

function goBack() {
  chatStore.currentSessionId = null
  question.value = ''
}

async function handleDeleteSession(sessionId) {
  try {
    await ElMessageBox.confirm('确定要删除这个会话吗？删除后无法恢复。', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await chatStore.deleteSession(sessionId)
    ElMessage.success('会话已删除')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Delete session failed:', error)
    }
  }
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
    // 错误已由 store 处理
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
  kbStore.loadKnowledgeBases()
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
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  flex: 1;
}

.delete-btn {
  flex-shrink: 0;
  margin-left: 8px;
  opacity: 0.6;
}

.session-item:hover .delete-btn {
  opacity: 1;
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

.back-bar {
  padding: 12px 16px;
  border-bottom: 1px solid #e0e0e0;
  background: #fafafa;
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
