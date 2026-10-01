import { defineStore } from 'pinia'
import { ref } from 'vue'
import { askQuestion, getChatHistory, getChatSessions } from '../api/chat.js'
import { ElMessage } from 'element-plus'

export const useChatStore = defineStore('chat', () => {
  const sessions = ref([])
  const currentSessionId = ref(null)
  const messages = ref({})  // { sessionId: [{role, content, sources, createdAt}] }
  const loading = ref(false)

  async function sendQuestion(question, kbId) {
    loading.value = true
    try {
      const result = await askQuestion(question, kbId, currentSessionId.value)

      // Set current session if new
      if (!currentSessionId.value) {
        currentSessionId.value = result.sessionId
        // Add to sessions list if not already there
        if (!sessions.value.find(s => s.id === result.sessionId)) {
          sessions.value.unshift({ id: result.sessionId, title: question.substring(0, 30) })
        }
      }

      // Ensure messages array exists for session
      if (!messages.value[result.sessionId]) {
        messages.value[result.sessionId] = []
      }

      // Add user message
      messages.value[result.sessionId].push({
        role: 'user',
        content: question,
        createdAt: Date.now()
      })

      // Add assistant message with sources
      messages.value[result.sessionId].push({
        role: 'assistant',
        content: result.answer,
        sources: result.sources,
        createdAt: Date.now()
      })

      return result
    } catch (error) {
      ElMessage.error(error.response?.data?.error || 'Failed to get answer. Please try again later.')
      throw error
    } finally {
      loading.value = false
    }
  }

  async function loadHistory(sessionId) {
    try {
      const history = await getChatHistory(sessionId)
      messages.value[sessionId] = history
      currentSessionId.value = sessionId
    } catch (error) {
      ElMessage.error('Failed to load chat history')
    }
  }

  async function loadSessions() {
    try {
      sessions.value = await getChatSessions()
    } catch (error) {
      console.error('Failed to load sessions:', error)
    }
  }

  return {
    sessions,
    currentSessionId,
    messages,
    loading,
    sendQuestion,
    loadHistory,
    loadSessions
  }
})
