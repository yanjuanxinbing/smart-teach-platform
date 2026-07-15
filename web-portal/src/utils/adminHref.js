import { getToken } from './request'

/**
 * 跳管理中心 (8081) 时构造 URL,把 portal 当前登录态带过去,
 * 避免用户先在 8081 用 A 账号登录后,再到 8082 用 B 账号登录,
 * 然后点"管理中心"又被 8081 残留的 A cookie 串号的问题。
 *
 * 附加参数说明:
 *   - t=Date.now()    防止浏览器缓存旧的 admin HTML
 *   - from=portal     来源标记,方便 8081 做访问来源统计 / 日志
 *   - access_token    portal 当前 token;admin 启动时会读这个值写 cookie,直接采用 portal 身份
 *   - forceLogin=true 不传 token,强制 admin 清 cookie + 显示登录页(用于"切换账号"场景)
 */
const ADMIN_BASE = 'http://localhost:8081'

export const buildAdminHref = (path = '/', { forceLogin = false } = {}) => {
  const params = new URLSearchParams({
    t: String(Date.now()),
    from: 'portal'
  })
  if (forceLogin) {
    params.set('forceLogin', 'true')
  } else {
    const token = getToken()
    if (token) params.set('access_token', token)
  }
  return `${ADMIN_BASE}${path}?${params.toString()}`
}
