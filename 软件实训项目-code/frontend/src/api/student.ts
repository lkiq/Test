import request from './request'

/** 学生端 API */

export const getProfile = () => request.get('/student/profile')
export const saveProfile = (data: any) => request.post('/student/profile', data)

export const getAssessmentQuestions = (type = 'COMPREHENSIVE') =>
  request.get('/student/assessment/questions', { params: { type } })
export const submitAssessment = (data: any) => request.post('/student/assessment/submit', data)
export const getAssessmentResult = (id: number) => request.get(`/student/assessment/result/${id}`)
export const getAssessmentHistory = () => request.get('/student/assessment/history')
export const getLatestAssessmentResult = () => request.get('/student/assessment/latest-result')

export const exploreCareer = (data: any) => request.post('/student/career/explore', data)
export const exploreFromAssessment = (resultId: number) =>
  request.post(`/student/career/explore/from-assessment/${resultId}`, {})
export const getExploreHistory = () => request.get('/student/career/explore/history')

export const recommendJobs = () => request.post('/student/jobs/recommend')
export const getJobDetail = (id: number) => request.get(`/student/jobs/${id}`)
export const searchJobs = (keyword?: string, city?: string) =>
  request.get('/student/jobs/search', { params: { keyword, city } })

export const analyzeGap = (jobId: number) => request.post(`/student/gap/analyze/${jobId}`)
export const analyzeMultipleGap = (jobIds: number[]) => request.post('/student/gap/analyze/multi', jobIds)
export const getGapReport = (id: number) => request.get(`/student/gap/report/${id}`)
export const getRecommendedJobs = () => request.get('/student/gap/recommended-jobs')

export const generateLearningPath = (jobId?: number) =>
  request.post('/student/learning/generate', null, { params: jobId ? { jobId } : {} })
export const generateLearningPathMulti = (jobIds: number[], mode: string) =>
  request.post('/student/learning/generate/multi', { jobIds, mode })
export const regenerateLearningPathMulti = (jobIds: number[], mode: string) =>
  request.post('/student/learning/regenerate/multi', { jobIds, mode })
export const getLearningPath = () => request.get('/student/learning/path')
export const getLearningPaths = () => request.get('/student/learning/paths')
export const getPathListWithStats = () => request.get('/student/learning/path-list-with-stats')
export const getLearningPathMeta = (pathId: number) => request.get(`/student/learning/path/${pathId}/meta`)
export const getPathSkillsMatrix = (pathId: number) => request.get(`/student/learning/path/${pathId}/skills-matrix`)
export const updateTaskStatus = (id: number, status: string) =>
  request.put(`/student/learning/tasks/${id}`, { status })
export const getLearningTasks = (pathId?: number) =>
  request.get('/student/learning/tasks', { params: { pathId } })
export const getLearningResources = (skill?: number, stage?: string) =>
  request.get('/student/learning/resources', { params: { skill, stage } })
export const startTaskTest = (taskId: number) => request.post(`/student/learning/tasks/${taskId}/test-start`)
export const submitTaskTest = (taskId: number, answers: Record<string, string>) =>
  request.post(`/student/learning/tasks/${taskId}/test-submit`, { answers })
export const getMasteredSkills = () => request.get('/student/learning/mastered-skills')
export const recordSkillReview = (skillId: number) => request.post(`/student/learning/mastered-skills/${skillId}/review`)

export const uploadResume = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/student/resume/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
export const listResumes = () => request.get('/student/resume/list')
export const deleteResume = (id: number) => request.delete(`/student/resume/${id}`)
export const getResumeFileUrl = (id: number) => `/api/student/resume/${id}/file`
export const analyzeResume = (data: any) => request.post('/student/resume/analyze', data)
export const getResumeAnalysis = (id: number) => request.get(`/student/resume/analysis/${id}`)
export const getResumeHistory = () => request.get('/student/resume/history')

export const startInterview = (data: any) => request.post('/student/interview/start', data)
export const submitAnswer = (sid: string, answer: string) =>
  request.post(`/student/interview/${sid}/answer`, { answer })
export const endInterview = (sid: string) => request.post(`/student/interview/${sid}/end`)
export const getInterviewReport = (id: number) => request.get(`/student/interview/report/${id}`)
export const getInterviewHistory = () => request.get('/student/interview/history')

export const getProgressOverview = () => request.get('/student/progress/overview')
export const getSkillProgress = () => request.get('/student/progress/skills')
export const getGrowthReport = () => request.get('/student/progress/report')

// 岗位投递
export const applyJob = (jobId: number, resumeId?: number) =>
  request.post('/student/application', { jobId, resumeId })
export const getApplications = () => request.get('/student/application/list')
export const cancelApplication = (id: number) => request.delete(`/student/application/${id}`)

// 收藏岗位
export const addFavorite = (jobId: number) => request.post('/student/job/favorites', { jobId })
export const removeFavorite = (jobId: number) => request.delete(`/student/job/favorites/${jobId}`)
export const getFavorites = () => request.get('/student/job/favorites')
export const checkFavorite = (jobId: number) => request.get(`/student/job/favorites/check/${jobId}`)
