import request from '@/utils/request'

/**
 * 个人中心 API 集合
 *
 * 备注：
 *  - 后端 baseURL 走 /api（见 utils/request.js 的 service.baseURL）；
 *  - 业务 code=200 由响应拦截器自动解出 res.data；
 *  - 401 由响应拦截器统一触发"登录已失效"toast + 清 token + 派发 auth-expired 事件。
 *
 * 后端契约（详见 src/store/user.js 的 buildMockProfile TODO 注释）：
 *  - GET    /auth/me            -> SysUser + 角色/权限（不含 phone/bio/deptName 时前端走 mock 占位）
 *  - PUT    /profile/me         -> 接受 { realName, email, phone, avatar, bio }
 *  - POST   /profile/me/password-> 修改自己的登录密码
 *  - GET    /profile/me/logs    -> 个人操作日志（分页）
 *  - GET    /message/page       -> 消息分页
 *  - POST   /message/read       -> 标记指定 id 已读
 *  - POST   /message/read-all   -> 全部已读
 *  - GET    /message/unread-count -> 未读数
 */

/**
 * 更新我的基础资料。
 * @param {{ realName?: string, email?: string, phone?: string, avatar?: string, bio?: string }} data
 * @returns {Promise<void>}
 */
export const updateMyProfile = (data) => request.put('/profile/me', data)

/**
 * 仅保存个人简介 —— 后端目前共用 PUT /profile/me 的 { bio } 字段,
 * 单独封装一层,便于后续若后端把 bio 拆到独立表或独立接口时,
 * 前端调用点不用改。
 */
export const updateMyBio = (bio) => updateMyProfile({ bio })

export const changeOwnPassword = (data) => request.post('/profile/me/password', data)
export const myOperationLog = (params) => request.get('/profile/me/logs', { params })

// 消息中心
export const myMessages = (params) => request.get('/message/page', { params })
export const markRead = (ids) => request.post('/message/read', { ids })
export const markAllRead = () => request.post('/message/read-all')
export const unreadCount = () => request.get('/message/unread-count')