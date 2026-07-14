import { defineStore } from 'pinia'
import { login, register, getUserInfo, logout } from '@/api/auth'
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
      const vo = await login(form)
      // 后端 LoginVO 直接平铺字段 (token / userId / username / realName / avatar / roles / permissions)
      setToken(vo.token)
      this.token = vo.token
      this.roles = vo.roles || []
      this.permissions = vo.permissions || []
      this.userInfo = {
        userId: vo.userId,
        username: vo.username,
        realName: vo.realName,
        avatar: vo.avatar
      }
      return vo
    },
    async registerAction(form) {
      const vo = await register(form)
      setToken(vo.token)
      this.token = vo.token
      this.roles = vo.roles || []
      this.permissions = vo.permissions || []
      this.userInfo = {
        userId: vo.userId,
        username: vo.username,
        realName: vo.realName,
        avatar: vo.avatar
      }
      return vo
    },
    async fetchUserInfo() {
      const data = await getUserInfo()
      this.userInfo = data
      if (data?.roleNames) this.roles = data.roleNames
      if (data?.permissions) this.permissions = data.permissions
      return data
    },
    async logout() {
      // 即便后端 /auth/logout 因为 token 过期返回 401，由 silentError 跳过 toast
      try { await logout() } catch (e) { /* swallow */ }
      this._clearLocal()
    },
    // 强制清理本地凭据与状态（含 LocalStorage / SessionStorage 中残留的 portal_token）
    _clearLocal() {
      try { clearToken() } catch (e) {}
      try { sessionStorage.removeItem('portal_token') } catch (e) {}
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
