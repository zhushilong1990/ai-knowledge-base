<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>AI Knowledge Base - Login</span>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" type="email" placeholder="Enter your email" />
        </el-form-item>

        <el-form-item label="Password" prop="password">
          <el-input v-model="form.password" type="password" placeholder="Enter your password" @keyup.enter="handleLogin" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" style="width: 100%">
            Login
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-link">
        <span>Don't have an account? </span>
        <router-link to="/register">Register</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api/http.js'
import { useAuthStore } from '../stores/auth.js'

const router = useRouter()
const authStore = useAuthStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  email: '',
  password: ''
})

const rules = {
  email: [
    { required: true, message: 'Please enter your email', trigger: 'blur' },
    { type: 'email', message: 'Please enter a valid email', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Please enter your password', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const response = await api.post('/auth/login', {
        email: form.email,
        password: form.password
      })

      const { accessToken, refreshToken } = response.data
      authStore.setTokens(accessToken, refreshToken)

      ElMessage.success('Login successful')
      router.push('/home')
    } catch (error) {
      if (error.response?.data?.error) {
        ElMessage.error(error.response.data.error)
      } else {
        ElMessage.error('Login failed')
      }
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
}

.card-header {
  font-size: 18px;
  font-weight: 600;
}

.register-link {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
}

.register-link a {
  color: #409eff;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>
