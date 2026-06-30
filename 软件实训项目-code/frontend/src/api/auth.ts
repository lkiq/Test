import request from './request'

/** 认证相关 API */

/** 用户注册 */
export const register = (data: {
  username: string
  password: string
  realName: string
  phone: string
  email: string
  education?: string
  school?: string
  major?: string
  role: string
  companyName?: string
  companyIndustry?: string
  companySize?: string
  companyAddress?: string
  contactPosition?: string
}) => request.post('/auth/register', data)

/** 用户登录 */
export const login = (data: { username: string; password: string }) =>
  request.post('/auth/login', data)

/** 忘记密码 - 发送验证码 */
export const forgotPassword = (data: { email: string }) =>
  request.post('/auth/forgot-password', data)

/** 重置密码 - 验证验证码并更新密码（成功后自动登录，返回 LoginResponse） */
export const resetPassword = (data: { email: string; code: string; newPassword: string }) =>
  request.post('/auth/reset-password', data)

/** 邮箱验证码登录 - 发送验证码 */
export const sendLoginCode = (data: { email: string }) =>
  request.post('/auth/send-login-code', data)

/** 邮箱验证码登录 */
export const loginByCode = (data: { email: string; code: string }) =>
  request.post('/auth/login-by-code', data)

