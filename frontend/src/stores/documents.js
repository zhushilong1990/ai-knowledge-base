import { defineStore } from 'pinia'
import { ref } from 'vue'
import { uploadDocument } from '../api/documents.js'
import { ElMessage } from 'element-plus'

export const useDocumentsStore = defineStore('documents', () => {
  const isUploading = ref(false)
  const uploadProgress = ref(0)
  const lastUploadResult = ref(null)

  async function upload(file, knowledgeBaseId) {
    if (isUploading.value) {
      ElMessage.warning('An upload is already in progress')
      return
    }

    isUploading.value = true
    uploadProgress.value = 0
    lastUploadResult.value = null

    try {
      const result = await uploadDocument(file, knowledgeBaseId, (percent) => {
        uploadProgress.value = percent
      })
      lastUploadResult.value = result
      ElMessage.success(`Document uploaded successfully: ${result.chunkCount} chunks indexed`)
      return result
    } catch (error) {
      const message = error.response?.data?.message || error.message || 'Upload failed'
      ElMessage.error(message)
      throw error
    } finally {
      isUploading.value = false
      uploadProgress.value = 0
    }
  }

  function clearLastResult() {
    lastUploadResult.value = null
  }

  return {
    isUploading,
    uploadProgress,
    lastUploadResult,
    upload,
    clearLastResult,
  }
})
