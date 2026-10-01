<template>
  <div class="upload-container">
    <el-card class="upload-card">
      <template #header>
        <div class="card-header">
          <span>上传文档</span>
        </div>
      </template>

      <el-form label-width="120px">
        <el-form-item label="知识库">
          <el-select v-model="knowledgeBaseId" placeholder="请选择知识库">
            <el-option
              v-for="kb in kbStore.knowledgeBases"
              :key="kb.id"
              :label="kb.name"
              :value="kb.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="选择文件">
          <el-upload
            ref="uploadRef"
            class="upload-area"
            drag
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :file-list="fileList"
            accept=".pdf,.docx,.txt"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖拽到此处，或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 PDF、DOCX、TXT 格式，最大 10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item v-if="selectedFile">
          <div class="file-info">
            <span>{{ selectedFile.name }}</span>
            <span class="file-size">({{ (selectedFile.size / 1024).toFixed(1) }} KB)</span>
          </div>
        </el-form-item>

        <el-form-item v-if="documentsStore.isUploading">
          <el-progress
            :percentage="documentsStore.uploadProgress"
            :status="documentsStore.uploadProgress === 100 ? 'success' : undefined"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="documentsStore.isUploading"
            :disabled="!canUpload"
            @click="handleUpload"
            style="width: 100%"
          >
            {{ documentsStore.isUploading ? '上传中...' : '上传文档' }}
          </el-button>
        </el-form-item>

        <el-form-item v-if="documentsStore.lastUploadResult">
          <el-alert
            :title="`上传成功：已索引 ${documentsStore.lastUploadResult.chunkCount} 个文本块`"
            type="success"
            :closable="true"
            @close="documentsStore.clearLastResult()"
          />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { useDocumentsStore } from '../stores/documents.js'
import { useKnowledgeBaseStore } from '../stores/knowledgeBase.js'

const uploadRef = ref(null)
const knowledgeBaseId = ref(null)
const selectedFile = ref(null)
const fileList = ref([])

const documentsStore = useDocumentsStore()
const kbStore = useKnowledgeBaseStore()

const canUpload = computed(() => {
  return selectedFile.value && knowledgeBaseId.value && !documentsStore.isUploading
})

onMounted(() => {
  kbStore.loadKnowledgeBases()
})

function handleFileChange(file, files) {
  selectedFile.value = file.raw
  fileList.value = files
}

function handleFileRemove() {
  selectedFile.value = null
  fileList.value = []
}

async function handleUpload() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  if (!knowledgeBaseId.value) {
    ElMessage.warning('请先选择知识库')
    return
  }

  try {
    await documentsStore.upload(selectedFile.value, knowledgeBaseId.value)
    uploadRef.value?.clearFiles()
    selectedFile.value = null
    fileList.value = []
  } catch (error) {
    // 错误已由 store 处理
  }
}
</script>

<style scoped>
.upload-container {
  display: flex;
  justify-content: center;
  padding: 40px 20px;
}
.upload-card {
  width: 500px;
  max-width: 100%;
}
.upload-area {
  width: 100%;
}
.file-info {
  display: flex;
  gap: 8px;
  align-items: center;
}
.file-size {
  color: #909399;
  font-size: 12px;
}
</style>
