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

// ---------------------------------------------------------------------
//  userInfo 落盘 —— 防止页面刷新后 store.userInfo.userId 丢失
//  /my/* 接口虽然已不要求前端带 studentId (后端用 JWT 取 UserContext),
//  但前端 UI 仍依赖 userInfo.userId / realName 等做头像展示;
//  持久化保证 store hydration 时同步拿到真实 userId。
// ---------------------------------------------------------------------
const USER_INFO_KEY = 'portal_user_info'

const loadPersistedUserInfo = () => {
  try {
    const raw = localStorage.getItem(USER_INFO_KEY)
    if (!raw) return null
    const parsed = JSON.parse(raw)
    // 失效场景:被外部清空/被破坏为非对象,直接视为未登录
    if (!parsed || typeof parsed !== 'object') return null
    // 强制要求 userId 是有限正整数,否则视为污染
    const uid = Number(parsed.userId)
    if (!Number.isFinite(uid) || uid <= 0) return null
    return { ...parsed, userId: uid }
  } catch (e) {
    return null
  }
}
const savePersistedUserInfo = (info) => {
  try {
    if (!info || typeof info !== 'object') return
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
  } catch (e) { /* swallow */ }
}
const clearPersistedUserInfo = () => {
  try { localStorage.removeItem(USER_INFO_KEY) } catch (e) {}
}

// ---------------------------------------------------------------------
//  /auth/me 暂未下发 phone / bio / deptName 时的占位数据。
//  TODO: [profile 数据契约] [P1]
//    当前 GET /auth/me 响应只包含 username / realName / avatar / roles / permissions,
//    前端展示用的 phone / bio / deptName 在数据库层尚未落表;
//    等后端在 SysUser / SysUserProfile 表加列并改 LoginVO / UserInfoVO 后,
//    删除此函数并清理 fetchUserInfo 的 mock 回退分支。
// ---------------------------------------------------------------------
const buildMockProfile = (data, prev) => {
  const seed = (() => {
    // 用 userId 派生稳定但唯一的占位,避免不同用户之间数据碰撞
    const uid = Number(data?.userId ?? prev?.userId) || 0
    return uid
  })()
  // 不同用户得到不同的"看似真实"的手机号(末四位 = 1000 + (uid % 9000))
  const tail = String(1000 + (seed % 9000)).padStart(4, '0')
  return {
    // 形如 138****1234 —— 前台肉眼可识别是 mock,后端真值一来即覆盖
    phone:    prev?.phone    || `138****${tail}`,
    // 简介给一段通用占位文案,UI 上有内容才看得出"可编辑"
    bio:      prev?.bio      || '这个人很懒，还没有填写简介。',
    // 部门硬编码为"待分配",管理员在 8081 后台改完后会覆盖
    deptName: prev?.deptName || '待分配'
  }
}

// 默认导出 store(同时被 request.js 通过事件总线触发;事件名 'portal:auth-expired')
// 这里提供一个独立的清空方法,便于监听 window 事件
const onAuthExpired = () => {
  // 由于 store 是动态生成的,我们不在模块顶层捕获,改为在 store 创建后由 main.js 注册监听
};

export const useUserStore = defineStore('user', {
  state: () => {
    // 关键:store 创建时同步从 localStorage 恢复 userInfo / roles / permissions;
    // 确保路由 guard / 我的学习中心等读取 userId / roleCode 时不会有 null 窗口,
    // 也避免刷新后 roles 为空导致「我的学习」入口消失(需先访问 /profile 才回填)。
    const persisted = loadPersistedUserInfo()
    return {
      token: getToken(),
      userInfo: persisted,
      roles: persisted?.roles || [],
      permissions: persisted?.permissions || []
    }
  },
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
      // 角色码存的是 ROLE_STUDENT / ROLE_TEACHER / ROLE_ADMIN,
      // 而 ROLE_LABELS 的 key 是 STUDENT / TEACHER / ADMIN —— 兼容两套写法
      return s.roles.map(r => {
        const key = r.startsWith('ROLE_') ? r.slice(5) : r
        return ROLE_LABELS[key] || ROLE_LABELS[r] || r
      }).join(' / ')
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
      const info = {
        userId:   Number(vo.userId),
        username: vo.username,
        realName: vo.realName,
        avatar:   vo.avatar,
        email:    vo.email || this.userInfo?.email || '',
        phone:    vo.phone || this.userInfo?.phone || '',
        bio:      vo.bio   || this.userInfo?.bio   || '',
        deptName: vo.deptName || this.userInfo?.deptName || '',
        roles:    this.roles,
        permissions: this.permissions
      }
      this.userInfo = info
      savePersistedUserInfo(info)
      return vo
    },
    async registerAction(form) {
      const vo = await register(form)
      setToken(vo.token)
      this.token = vo.token
      this.roles = vo.roles || []
      this.permissions = vo.permissions || []
      const info = {
        userId:   Number(vo.userId),
        username: vo.username,
        realName: vo.realName,
        avatar:   vo.avatar,
        email:    vo.email || '',
        phone:    vo.phone || '',
        bio:      vo.bio   || '',
        deptName: vo.deptName || '',
        roles:    this.roles,
        permissions: this.permissions
      }
      this.userInfo = info
      savePersistedUserInfo(info)
      return vo
    },
    async fetchUserInfo() {
      const data = await getUserInfo()
      // 先回填 roles / permissions,再据此构建持久化对象,保证存盘的 roles 是最新值
      if (Array.isArray(data?.roleNames)) this.roles = data.roleNames
      if (Array.isArray(data?.permissions)) this.permissions = data.permissions

      // ---- Mock fallback -----------------------------------------------------
      // 后端 GET /auth/me 暂未下发 phone / bio / deptName 字段时,
      // 为了让 ProfileIndex 不显示空白,先用占位字符串填充并打 TODO。
      // 等 SysUser / SysUserProfile 表加上对应列后,这一段可以直接删除。
      // 触发条件:连续三层回退(本次响应 → 持久化 → mock)都拿不到值。
      const mock = buildMockProfile(data, this.userInfo)

      // 只保留稳定可序列化的字段,避免把不可 JSON 化的脏数据写盘
      const safe = {
        userId:   Number(data?.userId ?? data?.id ?? this.userInfo?.userId),
        username: data?.username ?? data?.userName ?? this.userInfo?.username,
        realName: data?.realName ?? this.userInfo?.realName,
        avatar:   data?.avatar ?? this.userInfo?.avatar,
        email:    data?.email    ?? this.userInfo?.email    ?? mock.email,
        phone:    data?.phone    ?? this.userInfo?.phone    ?? mock.phone,
        bio:      data?.bio ?? data?.introduction ?? this.userInfo?.bio ?? mock.bio,
        deptName: data?.deptName ?? data?.dept?.name ?? this.userInfo?.deptName ?? mock.deptName,
        className: data?.className ?? this.userInfo?.className,
        roleNames: data?.roleNames ?? this.roles,
        roles:    this.roles,
        permissions: this.permissions
      }
      this.userInfo = safe
      savePersistedUserInfo(safe)
      return data
    },
    async logout() {
      // 即便后端 /auth/logout 因为 token 过期返回 401，由 silentError 跳过 toast
      try { await logout() } catch (e) { /* swallow */ }
      this._clearLocal()
    },
    // 强制清理本地凭据与状态（含 LocalStorage / SessionStorage 中残留的 portal_token + userInfo）
    _clearLocal() {
      try { clearToken() } catch (e) {}
      try { sessionStorage.removeItem('portal_token') } catch (e) {}
      clearPersistedUserInfo()
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
