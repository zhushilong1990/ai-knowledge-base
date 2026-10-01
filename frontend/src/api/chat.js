import api from './http.js'

/**
 * Send a chat question and get AI response.
 * @param {string} question - User question
 * @param {number} kbId - Knowledge base ID
 * @param {number|null} sessionId - Chat session ID (null for new session)
 * @returns {Promise<{answer: string, sources: Array, sessionId: number}>}
 */
export async function askQuestion(question, kbId, sessionId) {
  const response = await api.post('/chat/ask', {
    question,
    kbId,
    sessionId: sessionId || null
  })
  return response.data
}

/**
 * Get chat history for a session.
 * @param {number} sessionId - Chat session ID
 * @returns {Promise<Array>} List of messages
 */
export async function getChatHistory(sessionId) {
  const response = await api.get(`/chat/history/${sessionId}`)
  return response.data
}

/**
 * Get list of chat sessions.
 * @returns {Promise<Array>} List of sessions
 */
export async function getChatSessions() {
  const response = await api.get('/chat/sessions')
  return response.data
}
