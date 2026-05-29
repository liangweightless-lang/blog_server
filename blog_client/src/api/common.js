import request from '@/utils/request';

/**
 * 获取首页配置参数
 * @returns {Promise}
 */
export function getHomeConfig() {
  return request.get('/api/home/config');
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
