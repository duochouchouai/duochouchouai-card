import axios from 'axios'
import { useAuthStore } from '@/stores/authStore'

// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 5000
})

// 请求拦截器（添加Token）
request.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}` // JWT鉴权（后端支持）
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器（处理错误，如Token过期）
request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const authStore = useAuthStore()
    // Token过期：清除状态并跳转登录页
    if (error.response?.status === 401) {
      authStore.logout()
      window.location.href = '/#/login'
    }
    return Promise.reject(error)
  }
)

export default request