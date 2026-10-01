<template>
  <div class="home-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h2>AI 知识库</h2>
          <div class="user-info">
            <span class="user-email">{{ userEmail }}</span>
            <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </el-header>

      <el-main>
        <el-card>
          <template #header>
            <span>欢迎使用 AI 知识库</span>
          </template>
          <div class="welcome-content">
            <p>您已成功登录！</p>
            <p class="token-info">
              Token 状态：
              <el-tag :type="tokenExpiringSoon ? 'warning' : 'success'" size="small">
                {{ tokenExpiringSoon ? '即将过期' : '有效' }}
              </el-tag>
            </p>
            <p class="user-detail">登录账号：{{ userEmail }}</p>
            <p class="upload-link">
              <router-link to="/upload">
                <el-button type="primary" size="small">上传文档</el-button>
              </router-link>
            </p>
            <p class="chat-link">
              <router-link to="/chat">
                <el-button type="success" size="small">开始聊天</el-button>
              </router-link>
            </p>
            <p class="kb-link">
              <router-link to="/knowledge-bases">
                <el-button type="info" size="small">管理知识库</el-button>
              </router-link>
            </p>
          </div>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth.js'
import api from '../api/http.js'

const router = useRouter()
const authStore = useAuthStore()

const userEmail = ref('')

const tokenExpiringSoon = computed(() => authStore.isTokenExpiringSoon())

onMounted(async () => {
  try {
    const response = await api.get('/auth/userinfo')
    userEmail.value = response.data.email
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
})

const handleLogout = async () => {
  await authStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h2 {
  margin: 0;
  color: #333;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-email {
  color: #666;
  font-size: 14px;
}

.welcome-content {
  padding: 20px 0;
}

.welcome-content p {
  margin: 10px 0;
  color: #333;
}

.token-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-detail {
  color: #666;
  font-size: 14px;
}

.upload-link {
  margin-top: 20px;
}

.upload-link a {
  text-decoration: none;
}

.chat-link {
  margin-top: 12px;
}

.chat-link a {
  text-decoration: none;
}

.kb-link {
  margin-top: 12px;
}

.kb-link a {
  text-decoration: none;
}
</style>
