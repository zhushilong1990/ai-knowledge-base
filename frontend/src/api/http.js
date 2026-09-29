import axios from 'axios'
import { useAuthStore } from '../stores/auth.js'
import { ElMessage } from 'element-plus'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL || '/api',
  timeout: 10000,
})

// Refresh deduplication state
let isRefreshing = false
let refreshSubscribers = []

function subscribeTokenRefresh(callback) {
  refreshSubscribers.push(callback)
}

function onRefreshComplete(newToken) {
  refreshSubscribers.forEach(cb => cb(newToken))
  refreshSubscribers = []
  isRefreshing = false
}

// Request interceptor: proactive refresh
api.interceptors.request.use(
  async (config) => {
    const authStore = useAuthStore()

    // Proactive refresh if token is expiring soon
    if (authStore.isTokenExpiringSoon() && authStore.refreshToken) {
      try {
        if (!isRefreshing) {
          isRefreshing = true
          const newToken = await authStore.refresh()
          onRefreshComplete(newToken)
        } else {
          // Wait for refresh to complete
          await new Promise(resolve => {
            subscribeTokenRefresh((token) => {
              resolve(token)
            })
          })
        }
      } catch (error) {
        console.error('Proactive refresh failed:', error)
      }
    }

    // Attach token to request
    if (authStore.accessToken) {
      config.headers.Authorization = `Bearer ${authStore.accessToken}`
    }

    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor: handle 401 and retry
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    const authStore = useAuthStore()

    // Handle 401 - token expired
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      try {
        if (!isRefreshing) {
          isRefreshing = true
          const newToken = await authStore.refresh()
          onRefreshComplete(newToken)
          originalRequest.headers.Authorization = `Bearer ${newToken}`
          return api(originalRequest)
        } else {
          // Wait for existing refresh to complete
          const newToken = await new Promise(resolve => {
            subscribeTokenRefresh((token) => {
              resolve(token)
            })
          })
          originalRequest.headers.Authorization = `Bearer ${newToken}`
          return api(originalRequest)
        }
      } catch (refreshError) {
        // Refresh failed - clear tokens and redirect to login
        authStore.clearTokens()
        window.location.href = '/login'
        return Promise.reject(refreshError)
      }
    }

    // Handle network errors
    if (!error.response) {
      ElMessage.error('Network error. Please check your connection.')
    }

    return Promise.reject(error)
  }
)

export default api
