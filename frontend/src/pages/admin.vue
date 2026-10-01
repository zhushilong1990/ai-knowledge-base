<template>
  <div class="admin-container">
    <el-card class="admin-card">
      <template #header>
        <div class="card-header">
          <span>管理面板</span>
        </div>
      </template>

      <el-form :model="form" label-width="120px">
        <el-form-item label="知识库">
          <el-select v-model="form.kbId" placeholder="请选择知识库" @change="loadStatus">
            <el-option
              v-for="kb in knowledgeBases"
              :key="kb.id"
              :label="kb.name"
              :value="kb.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态" v-if="form.kbId">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="文档数量">
              {{ status.documentCount || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="文本块总数">
              {{ status.totalChunks || 0 }}
            </el-descriptions-item>
          </el-descriptions>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="reindexing"
            :disabled="!form.kbId"
            @click="handleReindex"
          >
            重新索引知识库
          </el-button>
        </el-form-item>
      </el-form>

      <el-alert
        v-if="message.text"
        :type="message.type"
        :title="message.text"
        :closable="false"
        style="margin-top: 16px"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getKnowledgeBases } from '../api/knowledgeBase.js'
import api from '../api/http.js'
import { ElMessage } from 'element-plus'

const knowledgeBases = ref([])
const reindexing = ref(false)
const message = reactive({ text: '', type: 'info' })

const form = reactive({
  kbId: null
})

const status = reactive({
  documentCount: 0,
  totalChunks: 0
})

async function loadKnowledgeBases() {
  try {
    const res = await getKnowledgeBases()
    knowledgeBases.value = res.data || []
  } catch (err) {
    ElMessage.error('加载知识库失败')
  }
}

async function loadStatus() {
  if (!form.kbId) return
  try {
    const res = await api.get('/admin/status', { params: { kbId: form.kbId } })
    status.documentCount = res.data.documentCount || 0
    status.totalChunks = res.data.totalChunks || 0
    message.text = ''
  } catch (err) {
    status.documentCount = 0
    status.totalChunks = 0
  }
}

async function handleReindex() {
  if (!form.kbId) return

  reindexing.value = true
  message.text = ''

  try {
    const res = await api.post(`/admin/reindex/${form.kbId}`)
    message.type = 'success'
    message.text = `重新索引完成：${res.data.reindexed || 0} 个文档，${res.data.totalChunks || 0} 个文本块`
    await loadStatus()
  } catch (err) {
    message.type = 'error'
    message.text = err.response?.data?.error || '重新索引失败'
  } finally {
    reindexing.value = false
  }
}

onMounted(() => {
  loadKnowledgeBases()
})
</script>

<style scoped>
.admin-container {
  padding: 20px;
}

.admin-card {
  max-width: 600px;
  margin: 0 auto;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}
</style>
