import request from './request'

/** 学习成果测评 API */

// 开始测评 - 获取题目
export const getQuestions = (stage: string, skillId?: number) =>
  request.get('/student/learning-result/questions', { params: { stage, skillId } })

// 提交测评答案
export const submitTest = (data: {
  pathId?: number
  skillId?: number
  stage: string
  answers: Record<string, string>
}) => request.post('/student/learning-result/submit', data)

// 查看测评详情
export const getResultDetail = (id: number) =>
  request.get(`/student/learning-result/${id}`)

// 查看测评历史
export const getResultHistory = () =>
  request.get('/student/learning-result/history')
