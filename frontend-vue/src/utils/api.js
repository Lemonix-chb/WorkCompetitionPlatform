/**
 * Axios API 客户端配置
 * 提供统一的 HTTP 请求处理，自动注入 JWT token，统一错误处理
 *
 * @author 陈海波
 * @since 2026-04-11
 */

import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：自动注入 JWT token
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求配置错误', error)
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理响应和错误
api.interceptors.response.use(
  response => {
    const { data } = response

    // 项目约定的响应格式：{ code: 200, message: '', data: ... }
    if (data.code === 200) {
      return data
    }

    // 业务错误
    const errorMsg = data.message || '操作失败'
    ElMessage.error(errorMsg)
    return Promise.reject(new Error(errorMsg))
  },
  error => {
    // HTTP 错误处理
    if (error.response) {
      const { status, data } = error.response

      switch (status) {
        case 401:
          // 未授权，token 过期或无效
          ElMessage.error('登录已过期，请重新登录')
          localStorage.removeItem('token')
          localStorage.removeItem('userRole')
          localStorage.removeItem('userId')
          router.push('/login')
          break

        case 403:
          // 无权限
          ElMessage.error(data?.message || '没有权限访问')
          break

        case 404:
          // 资源不存在
          ElMessage.error(data?.message || '请求的资源不存在')
          break

        case 500:
          // 服务器错误
          ElMessage.error(data?.message || '服务器内部错误')
          break

        default:
          ElMessage.error(data?.message || `请求失败 (${status})`)
      }
    } else if (error.request) {
      // 请求已发出但无响应（网络错误）
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      // 请求配置错误
      ElMessage.error(error.message || '请求失败')
    }

    console.error('API 错误', error)
    return Promise.reject(error)
  }
)

/**
 * GET 请求
 *
 * @param {string} url - 请求路径
 * @param {Object} params - 查询参数
 * @returns {Promise<Object>} 响应数据
 */
export const get = (url, params = {}) => {
  return api.get(url, { params })
}

/**
 * POST 请求
 *
 * @param {string} url - 请求路径
 * @param {Object} data - 请求数据
 * @returns {Promise<Object>} 响应数据
 */
export const post = (url, data = {}) => {
  return api.post(url, data)
}

/**
 * PUT 请求
 *
 * @param {string} url - 请求路径
 * @param {Object} data - 请求数据
 * @returns {Promise<Object>} 响应数据
 */
export const put = (url, data = {}) => {
  return api.put(url, data)
}

/**
 * DELETE 请求
 *
 * @param {string} url - 请求路径
 * @returns {Promise<Object>} 响应数据
 */
export const del = (url) => {
  return api.delete(url)
}

/**
 * POST 表单数据（application/x-www-form-urlencoded）
 *
 * @param {string} url - 请求路径
 * @param {Object} params - 表单参数
 * @returns {Promise<Object>} 响应数据
 */
export const postForm = (url, params = {}) => {
  return api.post(url, null, {
    params,
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
  })
}

/**
 * 文件上传
 *
 * @param {string} url - 请求路径
 * @param {File} file - 上传文件
 * @param {Object} extraData - 额外的表单数据
 * @returns {Promise<Object>} 响应数据
 */
export const uploadFile = (url, file, extraData = {}) => {
  const formData = new FormData()
  formData.append('file', file)

  Object.keys(extraData).forEach(key => {
    formData.append(key, extraData[key])
  })

  return api.post(url, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export default api