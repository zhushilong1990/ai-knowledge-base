<template>
  <div class="kb-container">
    <el-card class="kb-card">
      <template #header>
        <div class="card-header">
          <span>Knowledge Base Management</span>
          <el-button type="primary" @click="showCreateDialog = true">Create Knowledge Base</el-button>
        </div>
      </template>

      <div v-if="kbStore.knowledgeBases.length === 0" class="empty-state">
        <el-empty description="No knowledge bases yet. Create one to get started." />
      </div>

      <div v-else class="kb-list">
        <el-card
          v-for="kb in kbStore.knowledgeBases"
          :key="kb.id"
          class="kb-item"
          shadow="hover"
        >
          <template #header>
            <div class="kb-header">
              <div class="kb-info">
                <span class="kb-name">{{ kb.name }}</span>
                <span class="kb-desc" v-if="kb.description">{{ kb.description }}</span>
              </div>
              <div class="kb-actions">
                <el-tag size="small">{{ kb.docCount }} documents</el-tag>
                <el-button type="primary" size="small" @click="expandKB(kb)">
                  {{ expandedKBId === kb.id ? 'Collapse' : 'View Documents' }}
                </el-button>
              </div>
            </div>
          </template>

          <div v-if="expandedKBId === kb.id" class="documents-section">
            <el-divider content-position="left">Documents</el-divider>
            <div v-if="kbDocuments.length === 0" class="empty-docs">
              <el-empty description="No documents in this knowledge base." :image-size="60" />
            </div>
            <div v-else class="doc-list">
              <div v-for="doc in kbDocuments" :key="doc.id" class="doc-item">
                <div class="doc-info">
                  <span class="doc-name">{{ doc.fileName }}</span>
                  <span class="doc-meta">{{ formatFileSize(doc.fileSize) }} - {{ doc.chunkCount }} chunks</span>
                </div>
                <el-button type="danger" size="small" @click="handleDeleteDoc(doc.id, kb.id)">
                  Delete
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>

    <!-- Create KB Dialog -->
    <el-dialog v-model="showCreateDialog" title="Create Knowledge Base" width="400px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="Name" required>
          <el-input v-model="createForm.name" placeholder="Enter knowledge base name" />
        </el-form-item>
        <el-form-item label="Description">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="3"
            placeholder="Enter description (optional)"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">Cancel</el-button>
        <el-button type="primary" @click="handleCreateKB">Create</el-button>
      </template>
    </el-dialog>

    <!-- Delete Confirm Dialog -->
    <el-dialog v-model="showDeleteDialog" title="Confirm Delete" width="300px">
      <p>Are you sure you want to delete this document? This action cannot be undone.</p>
      <template #footer>
        <el-button @click="showDeleteDialog = false">Cancel</el-button>
        <el-button type="danger" @click="confirmDeleteDoc">Delete</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useKnowledgeBaseStore } from '../stores/knowledgeBase.js'
import { getDocuments } from '../api/knowledgeBase.js'

const kbStore = useKnowledgeBaseStore()

const showCreateDialog = ref(false)
const showDeleteDialog = ref(false)
const expandedKBId = ref(null)
const kbDocuments = ref([])
const createForm = ref({ name: '', description: '' })
const pendingDeleteDoc = ref({ docId: null, kbId: null })

onMounted(() => {
  kbStore.loadKnowledgeBases()
})

async function expandKB(kb) {
  if (expandedKBId.value === kb.id) {
    expandedKBId.value = null
    kbDocuments.value = []
  } else {
    expandedKBId.value = kb.id
    try {
      const res = await getDocuments(kb.id)
      kbDocuments.value = res.data
    } catch (error) {
      kbDocuments.value = []
    }
  }
}

async function handleCreateKB() {
  if (!createForm.value.name) return
  await kbStore.createKB(createForm.value.name, createForm.value.description)
  showCreateDialog.value = false
  createForm.value = { name: '', description: '' }
}

function handleDeleteDoc(docId, kbId) {
  pendingDeleteDoc.value = { docId, kbId }
  showDeleteDialog.value = true
}

async function confirmDeleteDoc() {
  const { docId, kbId } = pendingDeleteDoc.value
  await kbStore.removeDocument(docId, kbId)
  // Refresh document list
  try {
    const res = await getDocuments(kbId)
    kbDocuments.value = res.data
  } catch (error) {
    kbDocuments.value = []
  }
  showDeleteDialog.value = false
}

function formatFileSize(size) {
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / 1024 / 1024).toFixed(1) + ' MB'
}
</script>

<style scoped>
.kb-container {
  padding: 20px;
}

.kb-card {
  max-width: 900px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.kb-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.kb-item {
  margin-bottom: 0;
}

.kb-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.kb-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.kb-name {
  font-weight: bold;
  font-size: 16px;
}

.kb-desc {
  color: #666;
  font-size: 13px;
}

.kb-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.documents-section {
  margin-top: 12px;
}

.empty-docs {
  padding: 20px 0;
}

.doc-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.doc-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 4px;
}

.doc-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.doc-name {
  font-weight: 500;
}

.doc-meta {
  color: #999;
  font-size: 12px;
}

.empty-state {
  padding: 40px 0;
}
</style>
