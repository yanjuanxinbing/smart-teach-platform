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
    /**
     * 从 URL query 接管 portal 端带过来的 token
     *   - access_token=<token>  → 直接采用 portal 当前身份,写 cookie
     *   - 读完后清理 URL,避免刷新重复触发 + token 残留在浏览器历史
     *   - 没有 access_token 时什么都不做,保留原有登录态 / 走 login 流程
     */
    bootFromQuery() {
      if (typeof window === 'undefined') return
      const sp = new URLSearchParams(window.location.search)
      const tk = sp.get('access_token')
      if (!tk) return
      this.token = tk
      Cookies.set('token', tk, { expires: 7 })
      // 清理 URL:保留 from 标记(token 必须清掉)
      const from = sp.get('from')
      const cleanSearch = from ? `?from=${encodeURIComponent(from)}` : ''
      const cleanUrl = window.location.pathname + cleanSearch + window.location.hash
      window.history.replaceState({}, '', cleanUrl)
    },
    hasAuthority(code) {
      if (this.roles.includes('ROLE_ADMIN')) return true
      return this.permissions.includes(code)
    }
  }
})
