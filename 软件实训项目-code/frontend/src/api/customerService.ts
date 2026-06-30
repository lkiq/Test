import request from './request'

/** 智能客服 API */
export const chat = (data: { question: string; userRole?: string }) =>
  request.post('/customer-service/chat', data)
export const getFAQs = () => request.get('/customer-service/faqs')
