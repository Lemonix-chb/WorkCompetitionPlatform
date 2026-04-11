/**
 * 文件附件相关工具函数
 * 提供文件大小格式化、附件类型文本转换、文件下载等功能
 *
 * @author 陈海波
 * @since 2026-04-11
 */

import { ElMessage } from 'element-plus'
import { get } from './api'
import { showError, showSuccess } from './messageUtils'

/**
 * 附件类型枚举映射
 */
const ATTACHMENT_TYPE_TEXT = {
  SOURCE_CODE: '源代码',
  DOCUMENT: '文档',
  PRESENTATION: '演示文稿',
  VIDEO: '视频',
  OTHER: '其他'
}

/**
 * 格式化文件大小
 * 将字节转换为人类可读格式（B、KB、MB、GB）
 *
 * @param {number} bytes - 文件大小（字节）
 * @returns {string} 格式化后的文件大小字符串
 */
export function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

/**
 * 获取附件类型的中文文本
 *
 * @param {string} attachmentType - 附件类型枚举值
 * @returns {string} 中文文本描述
 */
export function getAttachmentTypeText(attachmentType) {
  return ATTACHMENT_TYPE_TEXT[attachmentType] || attachmentType
}

/**
 * 下载文件附件
 * 通过 blob 方式下载文件，自动处理 blob URL 的生命周期
 *
 * 注意：延迟 200ms 清理 blob URL，确保浏览器有时间启动下载
 *
 * @param {Object} attachment - 附件对象，包含 id 和 fileName
 * @returns {Promise<void>}
 */
export async function downloadAttachment(attachment) {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`/api/upload/download/${attachment.id}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })

    if (!response.ok) {
      showError('下载失败')
      return
    }

    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = attachment.fileName
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)

    // 延迟清理 blob URL，确保浏览器有时间启动下载进程
    setTimeout(() => window.URL.revokeObjectURL(url), 200)

    showSuccess('下载成功')
  } catch (error) {
    console.error('文件下载失败', error)
    showError('下载失败')
  }
}

/**
 * 获取作品的附件列表
 *
 * @param {number} workId - 作品 ID
 * @returns {Promise<Array>} 附件列表
 */
export async function fetchWorkAttachments(workId) {
  try {
    const data = await get(`/upload/work/${workId}`)
    return data.data || []
  } catch (error) {
    console.error('获取附件列表失败', error)
    return []
  }
}

/**
 * Vue 3 Composable: 作品附件管理
 * 提供响应式的附件状态和操作方法
 *
 * @returns {Object} 包含 attachments ref 和操作方法
 */
export function useWorkAttachments() {
  const attachments = ref([])

  const loadAttachments = async (workId) => {
    attachments.value = await fetchWorkAttachments(workId)
  }

  const download = async (attachment) => {
    await downloadAttachment(attachment)
  }

  return {
    attachments,
    loadAttachments,
    download,
    formatFileSize,
    getAttachmentTypeText
  }
}

// 需要在使用 composable 的文件中导入 ref
import { ref } from 'vue'