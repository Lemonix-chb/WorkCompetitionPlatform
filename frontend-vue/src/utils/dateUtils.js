/**
 * 日期时间格式化工具函数
 *
 * @author 陈海波
 * @since 2026-04-11
 */

/**
 * 格式化日期（不含时间）
 *
 * @param {string|Date} dateStr - 日期字符串或 Date 对象
 * @returns {string} 格式化后的日期（如：2026-04-11）
 */
export function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

/**
 * 格式化日期时间
 *
 * @param {string|Date} dateStr - 日期字符串或 Date 对象
 * @returns {string} 格式化后的日期时间（如：2026/04/11 15:30:45）
 */
export function formatDateTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

/**
 * 格式化相对时间（多久之前）
 *
 * @param {string|Date} dateStr - 日期字符串或 Date 对象
 * @returns {string} 相对时间描述（如：3天前、刚刚）
 */
export function formatRelativeTime(dateStr) {
  if (!dateStr) return ''

  const now = new Date()
  const date = new Date(dateStr)
  const diffMs = now - date
  const diffSeconds = Math.floor(diffMs / 1000)
  const diffMinutes = Math.floor(diffSeconds / 60)
  const diffHours = Math.floor(diffMinutes / 60)
  const diffDays = Math.floor(diffHours / 24)

  if (diffSeconds < 60) {
    return '刚刚'
  }
  if (diffMinutes < 60) {
    return `${diffMinutes}分钟前`
  }
  if (diffHours < 24) {
    return `${diffHours}小时前`
  }
  if (diffDays < 7) {
    return `${diffDays}天前`
  }

  // 超过7天，显示具体日期
  return formatDate(dateStr)
}