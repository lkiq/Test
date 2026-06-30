import request from './request'

/** 管理端 API */
export const getUsers = (params: any) => request.get('/admin/users', { params })
export const getUserDetail = (id: number) => request.get(`/admin/users/${id}`)
export const updateUserStatus = (id: number, status: string) =>
  request.put(`/admin/users/${id}/status`, { status })
export const resetUserPassword = (id: number, newPassword: string) =>
  request.put(`/admin/users/${id}/reset-password`, { newPassword })
export const getSkills = (params: any) => request.get('/admin/skills', { params })
export const addSkill = (data: any) => request.post('/admin/skills', data)
export const updateSkill = (id: number, data: any) => request.put(`/admin/skills/${id}`, data)
export const deleteSkill = (id: number) => request.delete(`/admin/skills/${id}`)
export const getJobSkills = (jobId: number) => request.get(`/admin/jobs/${jobId}/skills`)
export const updateJobSkills = (jobId: number, data: any) => request.put(`/admin/jobs/${jobId}/skills`, data)
export const getPositions = (params: any) => request.get('/admin/positions', { params })
export const getDashboard = () => request.get('/admin/dashboard')
