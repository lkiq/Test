import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi } from '@/api/auth'
import router from '@/router'

/** 用户状态管理 */
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const role = ref(localStorage.getItem('role') || '')
  const userId = ref(Number(localStorage.getItem('userId')) || 0)

  const isLoggedIn = computed(() => !!token.value)

  /** 登录 */
  async function login(data: { username: string; password: string }) {
    const res: any = await loginApi(data)
    setUserInfo(res.data)
    return res
  }

  /** 注册 */
  async function register(data: {
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
  }) {
    const res: any = await registerApi(data)
    setUserInfo(res.data)
    return res
  }

  /** 设置用户信息并持久化 */
  function setUserInfo(data: any) {
    token.value = data.token
    username.value = data.username
    role.value = data.role
    userId.value = data.userId
    localStorage.setItem('token', data.token)
    localStorage.setItem('username', data.username)
    localStorage.setItem('role', data.role)
    localStorage.setItem('userId', String(data.userId))
  }

  /** 登出 */
  function logout() {
    token.value = ''
    username.value = ''
    role.value = ''
    userId.value = 0
    localStorage.clear()
    router.push('/login')
  }

  return { token, username, role, userId, isLoggedIn, login, register, logout, setUserInfo }
})
