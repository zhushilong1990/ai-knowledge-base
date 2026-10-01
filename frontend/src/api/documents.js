import api from './http.js'

const MAX_FILE_SIZE = 10 * 1024 * 1024  // 10MB
const ALLOWED_TYPES = ['pdf', 'docx', 'txt']

/**
 * Upload a document to the knowledge base.
 * @param {File} file - The file to upload
 * @param {number} knowledgeBaseId - Target knowledge base ID
 * @param {Function} onProgress - Progress callback (0-100)
 * @returns {Promise<{documentId: string, chunkCount: number, status: string}>}
 */
export async function uploadDocument(file, knowledgeBaseId, onProgress) {
  // Client-side validation
  if (!file) {
    throw new Error('No file selected')
  }

  const ext = file.name.split('.').pop()?.toLowerCase()
  if (!ALLOWED_TYPES.includes(ext)) {
    throw new Error(`Unsupported file type: ${ext}. Allowed: ${ALLOWED_TYPES.join(', ')}`)
  }

  if (file.size > MAX_FILE_SIZE) {
    throw new Error(`File too large. Maximum size is 10MB. Your file: ${(file.size / 1024 / 1024).toFixed(2)}MB`)
  }

  const formData = new FormData()
  formData.append('file', file)
  formData.append('knowledgeBaseId', String(knowledgeBaseId))

  const response = await api.post('/documents/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percent)
      }
    },
  })

  return response.data
}

export { MAX_FILE_SIZE, ALLOWED_TYPES }
