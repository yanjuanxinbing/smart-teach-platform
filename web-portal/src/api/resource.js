import request from '@/utils/request'

/**
 * 门户侧「我的资源」API
 *
 * 后端契约（PortalResourceController）：
 *   GET /api/portal/resource/page
 *     Query: pageNum, pageSize, resourceName, resourceType, categoryId, courseId, tags
 *     Resp : { records: PortalResourceVO[], total, pageNum, pageSize }
 *   GET /api/portal/resource/{id}      -> PortalResourceVO  详情(自增浏览数)
 *   GET /api/portal/resource/{id}/file -> Blob               下载原始文件
 *
 * 复用 admin 的 biz_resource 表,但只暴露 status=1(上架)资源;
 * 字段精简(PortalResourceVO)不暴露 file_path / create_by / update_by 等内部审计字段。
 *
 * silentError: true 让后端 4xx/5xx 不弹 toast,
 * 由 MyResources.vue 的 state 字段统一展示「资源加载中… / 暂无内容」等占位。
 */
export const listPortalResources = (params) =>
  request.get('/portal/resource/page', { params, silentError: true })

export const getPortalResource = (id) =>
  request.get(`/portal/resource/${id}`, { silentError: true })

export const downloadPortalResource = (id) =>
  request.get(`/portal/resource/${id}/file`, { silentError: true, responseType: 'blob' })
