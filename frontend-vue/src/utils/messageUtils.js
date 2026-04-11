/**
 * 消息提示工具函数
 * 封装 Element Plus 的消息提示组件，提供统一的提示样式
 *
 * @author 陈海波
 * @since 2026-04-11
 */

import { ElMessage, ElMessageBox } from 'element-plus'

/**
 * 成功消息提示
 *
 * @param {string} message - 提示消息
 */
export function showSuccess(message) {
  ElMessage.success(message)
}

/**
 * 错误消息提示
 *
 * @param {string} message - 提示消息
 */
export function showError(message) {
  ElMessage.error(message)
}

/**
 * 警告消息提示
 *
 * @param {string} message - 提示消息
 */
export function showWarning(message) {
  ElMessage.warning(message)
}

/**
 * 信息消息提示
 *
 * @param {string} message - 提示消息
 */
export function showInfo(message) {
  ElMessage.info(message)
}

/**
 * 确认对话框
 *
 * @param {string} message - 确认消息
 * @param {string} title - 对话框标题
 * @param {Object} options - 配置选项
 * @returns {Promise<void>}
 */
export function showConfirm(message, title = '提示', options = {}) {
  return ElMessageBox.confirm(message, title, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    ...options
  })
}

/**
 * 提示对话框（只有确认按钮）
 *
 * @param {string} message - 提示消息
 * @param {string} title - 对话框标题
 * @param {Object} options - 配置选项
 * @returns {Promise<void>}
 */
export function showAlert(message, title = '提示', options = {}) {
  return ElMessageBox.alert(message, title, {
    confirmButtonText: '确定',
    ...options
  })
}