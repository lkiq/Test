import axios, { type AxiosInstance, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// Axios 实例 - 统一请求封装
const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器：添加 JWT Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一错误处理
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code === 200) {
      return res
    }
    // 业务错误：MODE_SWITCH_REQUIRED 由组件自行处理，不弹 Toast
    if (res.message?.includes('MODE_SWITCH_REQUIRED')) {
      return Promise.reject(new Error(res.message))
    }
    ElMessage.error(res.message || '请求失败')
    if (res.code === 401) {
      localStorage.removeItem('token')
      router.push('/login')
    }
    return Promise.reject(new Error(res.message))
  },
  (error) => {
    // 优先使用后端返回的业务错误信息
    const serverMsg = error.response?.data?.message
    if (error.response?.status === 401) {
      // 显示后端返回的错误信息（如"用户名或密码错误"）
      ElMessage.error(serverMsg || '认证失败，请重新登录')
      // 非登录页才跳转（防止登录失败时刷新页面）
      const isLoginPage = window.location.hash.includes('/login')
      if (!isLoginPage) {
        localStorage.removeItem('token')
        router.push('/login')
      }
    } else if (error.response?.status === 403) {
      ElMessage.error(serverMsg || '权限不足')
    } else {
      ElMessage.error(serverMsg || '网络错误，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
