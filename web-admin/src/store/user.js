import { defineStore } from 'pinia'
import { login, register, getUserInfo, logout } from '@/api/auth'
import Cookies from 'js-cookie'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: Cookies.get('token') || '',
    userInfo: null,
    permissions: [],
    roles: []
  }),
  actions: {
    async loginAction(form) {
      const { token, permissions, roles } = await login(form)
      this.token = token
      this.permissions = permissions || []
      this.roles = roles || []
      Cookies.set('token', token, { expires: 7 })
    },
    async registerAction(form) {
      const { token, permissions, roles } = await register(form)
      this.token = token
      this.permissions = permissions || []
      this.roles = roles || []
      Cookies.set('token', token, { expires: 7 })
    },
    async fetchUserInfo() {
      const data = await getUserInfo()
      this.userInfo = data
      // 刷新页面后 Pinia 状态被重置，但 /auth/me 会同时返回 roleNames + permissions，
      // 这里把它们灌回 store，hasAuthority() 才能正常工作
      if (data && data.roleNames) this.roles = data.roleNames
      if (data && data.permissions) this.permissions = data.permissions
      return data
    },
    async fetchMyMenu() {
      const { default: request } = await import('@/utils/request')
      return request.get('/system/menu/my-menu')
    },
    async logout() {
      try { await logout() } catch (e) {}
      this.token = ''
      this.userInfo = null
      this.permissions = []
      this.roles = []
      Cookies.remove('token')
    },
    hasAuthority(code) {
      if (this.roles.includes('ROLE_ADMIN')) return true
      return this.permissions.includes(code)
    }
  }
})
