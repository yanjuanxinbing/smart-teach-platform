import request from '@/utils/request'

// 系统监控
export const serverInfo = () => request.get('/monitor/server')
export const loginLogPage = (params) => request.get('/monitor/login-log/page', { params })
export const operationLogPage = (params) => request.get('/monitor/operation-log/page', { params })
export const operationLogClean = (beforeDays) => request.delete('/monitor/operation-log/clean', { params: { beforeDays } })
export const operationLogRemove = (ids) => request.delete('/monitor/operation-log', { data: ids })
