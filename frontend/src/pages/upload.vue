<template>
  <div class="upload-container">
    <el-card class="upload-card">
      <template #header>
        <div class="card-header">
          <span>Upload Document</span>
        </div>
      </template>

      <el-form label-width="120px">
        <el-form-item label="Knowledge Base">
          <el-select v-model="knowledgeBaseId" placeholder="Select knowledge base">
            <el-option label="Knowledge Base 1" :value="1" />
            <!-- Future: load from API -->
          </el-select>
        </el-form-item>

        <el-form-item label="Select File">
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
              Drop file here or <em>click to upload</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                PDF, DOCX, TXT files only. Max size: 10MB.
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
            {{ documentsStore.isUploading ? 'Uploading...' : 'Upload Document' }}
          </el-button>
        </el-form-item>

        <el-form-item v-if="documentsStore.lastUploadResult">
          <el-alert
            :title="`Upload complete: ${documentsStore.lastUploadResult.chunkCount} chunks indexed`"
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
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { useDocumentsStore } from '../stores/documents.js'

const uploadRef = ref(null)
const knowledgeBaseId = ref(1)
const selectedFile = ref(null)
const fileList = ref([])

const documentsStore = useDocumentsStore()

const canUpload = computed(() => {
  return selectedFile.value && knowledgeBaseId.value && !documentsStore.isUploading
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
    ElMessage.warning('Please select a file first')
    return
  }
  if (!knowledgeBaseId.value) {
    ElMessage.warning('Please select a knowledge base')
    return
  }

  try {
    await documentsStore.upload(selectedFile.value, knowledgeBaseId.value)
    // Reset after success
    uploadRef.value?.clearFiles()
    selectedFile.value = null
    fileList.value = []
  } catch (error) {
    // Error already shown by store
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
