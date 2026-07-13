import { defineStore } from 'pinia'
import { login, getUserInfo, logout } from '@/api/auth'
import { getToken, setToken, clearToken } from '@/utils/request'

/**
 * 角色编码约定（与管理后台保持一致）
 *  - ADMIN  系统管理员
 *  - TEACHER 教师
 *  - STUDENT 学生
 *  - GUEST  访客（未登录）
 */
export const ROLE_LABELS = {
  ADMIN: '管理员',
  TEACHER: '教师',
  STUDENT: '学生',
  GUEST: '访客'
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    userInfo: null,
    roles: [],
    permissions: []
  }),
  getters: {
    isLogin: (s) => !!s.token,
    roleCode: (s) => {
      if (!s.roles || !s.roles.length) return 'GUEST'
      if (s.roles.includes('ROLE_ADMIN')) return 'ADMIN'
      if (s.roles.includes('ROLE_TEACHER')) return 'TEACHER'
      if (s.roles.includes('ROLE_STUDENT')) return 'STUDENT'
      return 'GUEST'
    },
    roleLabel: (s) => {
      if (!s.roles || !s.roles.length) return ROLE_LABELS.GUEST
      return s.roles.map(r => ROLE_LABELS[r] || r).join(' / ')
    }
  },
  actions: {
    async loginAction(form) {
      const { token, roles, permissions, userInfo } = await login(form)
      setToken(token)
      this.token = token
      this.roles = roles || []
      this.permissions = permissions || []
      this.userInfo = userInfo || null
    },
    async fetchUserInfo() {
      const data = await getUserInfo()
      this.userInfo = data
      if (data?.roleNames) this.roles = data.roleNames
      if (data?.permissions) this.permissions = data.permissions
      return data
    },
    async logout() {
      try { await logout() } catch (e) { /* swallow */ }
      clearToken()
      this.token = ''
      this.userInfo = null
      this.roles = []
      this.permissions = []
    },
    hasAuthority(code) {
      if (this.roles.includes('ROLE_ADMIN')) return true
      return this.permissions.includes(code)
    }
  }
})
