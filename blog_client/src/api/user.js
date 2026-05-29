import request from '@/utils/request';

/**
 * 获取图片验证码
 * @returns {Promise}
 */
export function getCaptcha() {
  return request.get('/api/auth/captcha');
}

/**
 * 用户登录/注册等
 * @param {string} endpoint - 登录或注册的端点 (e.g., '/api/auth/login')
 * @param {object} data - 表单数据
 * @returns {Promise}
 */
export function authAction(endpoint, data) {
  return request.post(endpoint, data);
}

/**
 * 获取当前登录用户信息
 * @returns {Promise}
 */
export function getUserInfo() {
  return request.get('/api/users/me');
}

/**
 * 获取所有用户列表（管理端）
 * @returns {Promise}
 */
export function getUserList() {
  return request.get('/api/users');
}

/**
 * 用户签到
 * @returns {Promise}
 */
export function userCheckIn() {
  return request.post('/api/users/checkin');
}

/**
 * 更新用户个人资料
 * @param {object} data - 用户资料数据
 * @returns {Promise}
 */
export function updateUserProfile(data) {
  return request.put('/api/users/profile', data);
}
