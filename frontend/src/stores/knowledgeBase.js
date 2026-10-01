import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getKnowledgeBases, createKnowledgeBase, getDocuments, deleteDocument } from '../api/knowledgeBase.js'
import { ElMessage } from 'element-plus'

export const useKnowledgeBaseStore = defineStore('knowledgeBase', () => {
  const knowledgeBases = ref([])

  async function loadKnowledgeBases() {
    try {
      const res = await getKnowledgeBases()
      knowledgeBases.value = res.data || []
    } catch (error) {
      ElMessage.error('加载知识库列表失败')
    }
  }

  async function createKB(name, description) {
    try {
      const res = await createKnowledgeBase({ name, description })
      knowledgeBases.value.unshift({
        id: res.data.id,
        name: res.data.name,
        description: res.data.description,
        docCount: 0
      })
      ElMessage.success('Knowledge base created')
      return res.data
    } catch (error) {
      ElMessage.error('Failed to create knowledge base')
    }
  }

  async function removeDocument(docId, kbId) {
    try {
      await deleteDocument(docId)
      const kb = knowledgeBases.value.find(k => k.id === kbId)
      if (kb) kb.docCount--
      ElMessage.success('Document deleted')
    } catch (error) {
      ElMessage.error('Failed to delete document')
    }
  }

  return { knowledgeBases, loadKnowledgeBases, createKB, removeDocument }
})
