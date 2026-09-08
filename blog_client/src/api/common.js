import request from '@/utils/request';
import { getApiBaseUrl } from '@/utils/image';

/**
 * 获取首页配置参数
 * @returns {Promise}
 */
export function getHomeConfig() {
  return request.get('/api/home/config');
}

/**
 * 获取全局通用的文件上传完整 Action URL
 * 智能支持 Web 端、测试环境与 Android App 壳内调用，解决 App 相对路径 404 问题
 */
export function getUploadUrl() {
  return `${getApiBaseUrl()}/api/common/upload`;
}

/**
 * 获取上传接口需要的安全认证 Header
 */
export function getUploadHeaders() {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

/**
 * 统一文件上传 API (基于通用 request 实例，自动注入 baseURL 与 Token)
 * @param {File|Blob} file 
 * @returns {Promise}
 */
export function uploadFile(file) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post('/api/common/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

// ================= 管理端接口 ================= //

/**
 * 更新首页配置（管理端）
 * @param {object} data - 配置数据
 * @returns {Promise}
 */
export function updateHomeConfig(data) {
  return request.post('/api/home/config', data);
}
