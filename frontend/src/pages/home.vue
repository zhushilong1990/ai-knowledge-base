<template>
  <div class="home-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h2>AI Knowledge Base</h2>
          <div class="user-info">
            <span class="user-email">{{ userEmail }}</span>
            <el-button type="danger" size="small" @click="handleLogout">Logout</el-button>
          </div>
        </div>
      </el-header>

      <el-main>
        <el-card>
          <template #header>
            <span>Welcome to AI Knowledge Base</span>
          </template>
          <div class="welcome-content">
            <p>You are successfully logged in!</p>
            <p class="token-info">
              Token status:
              <el-tag :type="tokenExpiringSoon ? 'warning' : 'success'" size="small">
                {{ tokenExpiringSoon ? 'Expiring soon' : 'Valid' }}
              </el-tag>
            </p>
            <p class="user-detail">Logged in as: {{ userEmail }}</p>
            <p class="upload-link">
              <router-link to="/upload">
                <el-button type="primary" size="small">Upload Document</el-button>
              </router-link>
            </p>
            <p class="chat-link">
              <router-link to="/chat">
                <el-button type="success" size="small">Start Chat</el-button>
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
    console.error('Failed to fetch user info:', error)
  }
})

const handleLogout = async () => {
  await authStore.logout()
  ElMessage.success('Logged out successfully')
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
</style>
