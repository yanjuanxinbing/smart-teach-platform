import request from '@/utils/request'

// 资源分类
export const resCategoryTree = () => request.get('/biz/resource/category/tree')
export const resCategoryAdd = (data) => request.post('/biz/resource/category', data)
export const resCategoryEdit = (data) => request.put('/biz/resource/category', data)
export const resCategoryRemove = (id) => request.delete(`/biz/resource/category/${id}`)

// 资源
export const resPage = (params) => request.get('/biz/resource/page', { params })
export const resDetail = (id) => request.get(`/biz/resource/${id}`)
export const resAdd = (data) => request.post('/biz/resource', data)
export const resEdit = (data) => request.put('/biz/resource', data)
export const resRemove = (ids) => request.delete('/biz/resource', { data: ids })
export const resChangeStatus = (id, status) => request.put(`/biz/resource/${id}/status`, null, { params: { status } })
export const resDownload = (id) => request.post(`/biz/resource/${id}/download`)

// 文件上传
export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/biz/resource/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
}
