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

/**
 * Submit feedback for a chat message.
 * @param {number} messageId - Message ID
 * @param {string} rating - 'like' or 'dislike'
 * @param {string|null} reason - Optional feedback reason
 * @returns {Promise<Object>} Feedback object
 */
export async function submitFeedback(messageId, rating, reason) {
  const response = await api.post('/chat/feedback', {
    messageId,
    rating,
    feedbackReason: reason || null
  })
  return response.data
}

/**
 * Get feedback for a chat message.
 * @param {number} messageId - Message ID
 * @returns {Promise<Object>} Feedback object
 */
export async function getFeedback(messageId) {
  const response = await api.get(`/chat/feedback/${messageId}`)
  return response.data
}
