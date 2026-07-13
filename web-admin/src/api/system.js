import request from '@/utils/request'

// 用户管理
export const userPage = (params) => request.get('/system/user/page', { params })
export const userDetail = (id) => request.get(`/system/user/${id}`)
export const userAdd = (data) => request.post('/system/user', data)
export const userEdit = (data) => request.put('/system/user', data)
export const userRemove = (ids) => request.delete('/system/user', { data: ids })
export const userResetPassword = (id, password) => request.put(`/system/user/${id}/reset-password`, { password })
export const userChangeStatus = (id, status) => request.put(`/system/user/${id}/status`, null, { params: { status } })
export const changeOwnPassword = (data) => request.put('/system/user/change-password', data)

// 角色
export const rolePage = (params) => request.get('/system/role/page', { params })
export const roleList = () => request.get('/system/role/list')
export const roleAdd = (data) => request.post('/system/role', data)
export const roleEdit = (data) => request.put('/system/role', data)
export const roleRemove = (ids) => request.delete('/system/role', { data: ids })
export const roleMenuIds = (id) => request.get(`/system/role/${id}/menu-ids`)

// 菜单
export const menuTree = () => request.get('/system/menu/tree')
export const myMenu = () => request.get('/system/menu/my-menu')
export const menuAdd = (data) => request.post('/system/menu', data)
export const menuEdit = (data) => request.put('/system/menu', data)
export const menuRemove = (id) => request.delete(`/system/menu/${id}`)

// 部门
export const deptTree = () => request.get('/system/dept/tree')
export const deptAdd = (data) => request.post('/system/dept', data)
export const deptEdit = (data) => request.put('/system/dept', data)
export const deptRemove = (id) => request.delete(`/system/dept/${id}`)

// 班级
export const classPage        = (params) => request.get('/system/class/page', { params })
export const classDetail      = (id)     => request.get(`/system/class/${id}`)
export const classAdd         = (data)   => request.post('/system/class', data)
export const classEdit        = (data)   => request.put('/system/class', data)
export const classRemove      = (ids)    => request.delete('/system/class', { data: ids })
export const classListByDept  = (deptId) => request.get('/system/class/list-by-dept', { params: { deptId } })
export const classListAll     = ()       => request.get('/system/class/list-all')
export const classMyClasses   = ()       => request.get('/system/class/my-classes')
export const classListMembers = (id, roleName) => request.get(`/system/class/${id}/members`, { params: { roleName } })
export const classAssignMembers = (data) => request.post('/system/class/members', data)
export const classBatchAddMembers = (data) => request.post('/system/class/members/batch-add', data)

// 字典
export const dictTypePage = (params) => request.get('/system/dict/type/page', { params })
export const dictTypeList = () => request.get('/system/dict/type/list')
export const dictTypeAdd = (data) => request.post('/system/dict/type', data)
export const dictTypeEdit = (data) => request.put('/system/dict/type', data)
export const dictTypeRemove = (ids) => request.delete('/system/dict/type', { data: ids })
export const dictDataPage = (params) => request.get('/system/dict/data/page', { params })
export const dictDataList = (dictType) => request.get('/system/dict/data/list', { params: { dictType } })
export const dictDataAdd = (data) => request.post('/system/dict/data', data)
export const dictDataEdit = (data) => request.put('/system/dict/data', data)
export const dictDataRemove = (ids) => request.delete('/system/dict/data', { data: ids })

// 参数
export const configPage = (params) => request.get('/system/config/page', { params })
export const configAdd = (data) => request.post('/system/config', data)
export const configEdit = (data) => request.put('/system/config', data)
export const configRemove = (ids) => request.delete('/system/config', { data: ids })
