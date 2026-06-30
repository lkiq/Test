import request from './request'

/** 企业端 API */

// 首页仪表盘
export const getDashboardStats = () => request.get('/enterprise/dashboard')

// 人才推荐
export const recommend = (data: {
  projectDescription: string
  filters?: {
    education?: string
    city?: string
    skillPreference?: string
  }
}) => request.post('/enterprise/recommend', data)

// 人才推荐 - PDF 上传
export const recommendByPdf = (file: File, filters?: {
  education?: string
  city?: string
  skillPreference?: string
}) => {
  const formData = new FormData()
  formData.append('file', file)
  if (filters?.education) formData.append('education', filters.education)
  if (filters?.city) formData.append('city', filters.city)
  if (filters?.skillPreference) formData.append('skillPreference', filters.skillPreference)
  return request.post('/enterprise/recommend/pdf', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000,
  })
}

export const getRecommendHistory = () => request.get('/enterprise/recommend/history')

// 岗位管理
export const publishJob = (data: any) => request.post('/enterprise/jobs', data)
export const listMyJobs = () => request.get('/enterprise/jobs')
export const updateJob = (id: number, data: any) => request.put(`/enterprise/jobs/${id}`, data)
export const offlineJob = (id: number) => request.put(`/enterprise/jobs/${id}/offline`)
export const republishJob = (id: number) => request.put(`/enterprise/jobs/${id}/republish`)
export const deleteJob = (id: number) => request.delete(`/enterprise/jobs/${id}`)

// 图片上传
export const uploadJobImage = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/enterprise/jobs/upload-image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 投递管理
export const getJobApplications = (jobId: number) =>
  request.get(`/enterprise/jobs/${jobId}/applications`)
export const updateApplicationStatus = (id: number, status: string) =>
  request.put(`/enterprise/applications/${id}/status`, { status })
export const deleteApplication = (id: number) =>
  request.delete(`/enterprise/applications/${id}`)
export const getResumeDownloadUrl = (appId: number) =>
  `/api/enterprise/applications/${appId}/resume`
