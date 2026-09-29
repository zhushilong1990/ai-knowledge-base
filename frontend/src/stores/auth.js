import { defineStore } from 'pinia'

const REFRESH_THRESHOLD_MS = 5 * 60 * 1000  // 5 minutes per D-03

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: localStorage.getItem('accessToken') || null,
    refreshToken: localStorage.getItem('refreshToken') || null,
  }),

  actions: {
    setTokens(access, refresh) {
      this.accessToken = access
      this.refreshToken = refresh
      if (access) localStorage.setItem('accessToken', access)
      if (refresh) localStorage.setItem('refreshToken', refresh)
    },

    clearTokens() {
      this.accessToken = null
      this.refreshToken = null
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
    },

    parseJwtPayload(token) {
      try {
        const base64 = token.split('.')[1]
        return JSON.parse(atob(base64))
      } catch {
        return null
      }
    },

    isTokenExpiringSoon() {
      if (!this.accessToken) return true
      const payload = this.parseJwtPayload(this.accessToken)
      if (!payload || !payload.exp) return true
      const expMs = payload.exp * 1000
      return Date.now() >= expMs - REFRESH_THRESHOLD_MS
    },

    async refresh() {
      if (!this.refreshToken) throw new Error('No refresh token')
      const response = await fetch('/api/auth/refresh', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: this.refreshToken })
      })
      if (!response.ok) throw new Error('Refresh failed')
      const data = await response.json()
      this.setTokens(data.accessToken, this.refreshToken)  // keep same refreshToken
      return data.accessToken
    },

    async logout() {
      try {
        await fetch('/api/auth/logout', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refreshToken: this.refreshToken })
        })
      } catch (e) {
        // Network error: proceed with local logout anyway per D-04
      }
      this.clearTokens()
    }
  }
})
