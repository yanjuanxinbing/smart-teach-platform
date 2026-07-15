import request from '@/utils/request'

/**
 * 文档查看（8082 内容展示端）API 集合
 *
 * 重要边界 —— 8082 / 8081 角色分工:
 *  - 8082 (web-portal):学生/教师前台,文档的**查看与下载**端,无写操作;
 *  - 8081 (admin):管理员后台,文档的**上传/编辑/删除**端,生产内容。
 *  因此本文件只暴露 GET 类接口;PUT/POST/DELETE 都放在管理后台的 admin API 里。
 *
 * 后端契约（当前为占位）:
 *  - GET /api/document/me             -> PortalDocumentVO[]   当前用户可见文档列表
 *  - GET /api/document/{id}           -> PortalDocumentVO     单篇文档详情
 *  - GET /api/document/{id}/download  -> Blob                  下载原始文件
 *
 * 字段约定（待后端落表后核对）:
 *  PortalDocumentVO = {
 *    id, title, summary, docType: 'pdf'|'doc'|'image'|'link',
 *    url,         // 文件直链 / 外部链接
 *    size,        // 字节
 *    uploaderId,
 *    uploaderName,
 *    createdAt,
 *    updatedAt
 *  }
 *
 * TODO: [document 数据契约] [P1]
 *   当前后端尚无 /api/document/* 接口,前端调用统一走 silentError:true,
 *   让 DocumentView 优雅降级到占位文案;接口就绪后去掉 silentError 即可。
 */

// ---------------------------------------------------------------------
// 当前用户可见的文档列表(用于"我的文档"页)
// silentError:true —— 后端未就绪时不弹错误 toast,让前端渲染占位
// ---------------------------------------------------------------------
export const listMyDocuments = (params) =>
  request.get('/document/me', { params, silentError: true })

// 单篇文档详情(预览/查看)
export const getDocument = (id) =>
  request.get(`/document/${id}`, { silentError: true })

// 下载原始文件 —— 返回 Blob,前端用 window.URL.createObjectURL 触发下载
export const downloadDocument = (id) =>
  request.get(`/document/${id}/download`, { silentError: true, responseType: 'blob' })