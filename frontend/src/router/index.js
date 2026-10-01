import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../pages/login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../pages/register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../pages/home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/upload',
    name: 'Upload',
    component: () => import('../pages/upload.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('../pages/chat.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/knowledge-bases',
    name: 'KnowledgeBase',
    component: () => import('../pages/knowledgeBase.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../pages/admin.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// Navigation guard
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth) {
    // Check if user is authenticated
    if (!authStore.accessToken) {
      return next('/login')
    }

    // Check if token is expiring soon and try proactive refresh
    if (authStore.isTokenExpiringSoon() && authStore.refreshToken) {
      try {
        await authStore.refresh()
      } catch (error) {
        // Refresh failed - redirect to login
        authStore.clearTokens()
        return next('/login')
      }
    }
  }

  // Allow access to public routes
  next()
})

export default router
