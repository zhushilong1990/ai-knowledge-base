<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <span>AI Knowledge Base - Register</span>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" type="email" placeholder="Enter your email" />
        </el-form-item>

        <el-form-item label="Password" prop="password">
          <el-input v-model="form.password" type="password" placeholder="Minimum 8 characters" />
        </el-form-item>

        <el-form-item label="Confirm" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="Confirm your password" @keyup.enter="handleRegister" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleRegister" style="width: 100%">
            Register
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-link">
        <span>Already have an account? </span>
        <router-link to="/login">Login</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api/http.js'

const router = useRouter()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('Passwords do not match'))
  } else {
    callback()
  }
}

const rules = {
  email: [
    { required: true, message: 'Please enter your email', trigger: 'blur' },
    { type: 'email', message: 'Please enter a valid email', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Please enter your password', trigger: 'blur' },
    { min: 8, message: 'Password must be at least 8 characters', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: 'Please confirm your password', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await api.post('/auth/register', {
        email: form.email,
        password: form.password
      })

      ElMessage.success('Registration successful! Please login.')
      router.push('/login')
    } catch (error) {
      if (error.response?.data?.error) {
        ElMessage.error(error.response.data.error)
      } else {
        ElMessage.error('Registration failed')
      }
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-card {
  width: 400px;
}

.card-header {
  font-size: 18px;
  font-weight: 600;
}

.login-link {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
}

.login-link a {
  color: #409eff;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>
