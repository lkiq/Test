import request from './request'

/** 通用用户 API */

/** 获取当前用户个人信息（含企业信息 for HR） */
export const getUserProfile = () => request.get('/user/profile')

/** 更新当前用户个人信息 */
export const updateUserProfile = (data: any) => request.put('/user/profile', data)

/** 修改当前用户密码 */
export const changePassword = (data: { oldPassword: string; newPassword: string }) =>
  request.put('/user/password', data)
