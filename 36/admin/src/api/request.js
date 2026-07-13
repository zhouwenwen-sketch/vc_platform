import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/** 本地后端地址（开发走 Vite 代理，生产构建直连本机 8080） */
const LOCAL_BACKEND = 'http://localhost:8080'

export const API_BASE = import.meta.env.DEV ? '' : LOCAL_BACKEND

/** 图片/上传资源预览根地址（开发走代理用相对路径，构建后直连本机后端） */
export const MEDIA_BASE = import.meta.env.DEV ? '' : LOCAL_BACKEND

const request = axios.create({
  baseURL: import.meta.env.DEV ? '/api' : `${API_BASE}/api`,
  timeout: 30000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('admin_token')
        router.replace('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络错误'
    ElMessage.error(message)
    if (error.response?.status === 401) {
      localStorage.removeItem('admin_token')
      router.replace('/login')
    }
    return Promise.reject(error)
  }
)

export default request
