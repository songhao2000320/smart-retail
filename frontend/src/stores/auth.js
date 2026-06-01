import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, register as registerApi, getCurrentUser } from '@/api/auth'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  // 是否已登录
  const isLoggedIn = () => !!token.value

  // 是否为管理员
  const isAdmin = () => user.value?.role === 'admin'

  // 是否为店长
  const isManager = () => user.value?.role === 'manager'

  // 是否为数据分析师
  const isAnalyst = () => user.value?.role === 'analyst'

  // 设置认证信息
  function setAuth(newToken, newUser) {
    token.value = newToken
    user.value = newUser
    localStorage.setItem('token', newToken)
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  // 登录
  async function login(username, password) {
    const res = await loginApi(username, password)
    const { token: jwtToken, user: userInfo } = res.data
    setAuth(jwtToken, userInfo)
  }

  // 注册
  async function register(username, password) {
    await registerApi(username, password)
  }

  // 获取当前用户信息
  async function fetchUser() {
    try {
      const res = await getCurrentUser()
      user.value = res.data
      localStorage.setItem('user', JSON.stringify(res.data))
    } catch {
      logout()
    }
  }

  // 登出
  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  }

  return {
    token,
    user,
    isLoggedIn,
    isAdmin,
    isManager,
    isAnalyst,
    setAuth,
    login,
    register,
    fetchUser,
    logout
  }
})
