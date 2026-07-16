import request from '@/utils/request'

// 教师：班级维度统计 + AI 报告
export const classStats = (params) =>
  request.get('/assignment/analytics/class', { params })

export const generateClassReport = (data) =>
  request.post('/assignment/analytics/class/generate', data, { timeout: 120000 })

// 学生：个人维度统计 + AI 报告（学生 ID 服务端从 UserContext 注入）
export const myStats = () =>
  request.get('/assignment/analytics/student/me')

export const generateStudentReport = (data) =>
  request.post('/assignment/analytics/student/generate', data, { timeout: 120000 })
