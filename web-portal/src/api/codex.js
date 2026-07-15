import request from '@/utils/request'

// 笔记库（公开访问为主，私有片段需登录）
export const listCode = (params) => request.get('/portal/codex/page', { params })
export const codeDetail = (id) => request.get(`/portal/codex/${id}`)
