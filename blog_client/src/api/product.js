import request from '@/utils/request';

/**
 * 获取商品列表
 * @returns {Promise}
 */
export function getProducts() {
  return request.get('/api/products');
}

/**
 * 获取商品详情
 * @param {number|string} id - 商品ID
 * @returns {Promise}
 */
export function getProductDetail(id) {
  return request.get(`/api/products/${id}`);
}

/**
 * 创建/更新商品 (管理端)
 * @param {object} data - 商品数据
 * @returns {Promise}
 */
export function saveProduct(data) {
  return request.post('/api/products', data);
}

/**
 * 更新商品 (管理端)
 * @param {string|number} id - 商品 ID
 * @param {object} data - 商品数据
 * @returns {Promise}
 */
export function updateProduct(id, data) {
  return request.put(`/api/products/${id}`, data);
}

/**
 * 删除商品 (管理端)
 * @param {string|number} id - 商品 ID
 * @returns {Promise}
 */
export function deleteProduct(id) {
  return request.delete(`/api/products/${id}`);
}

/**
 * 获取所有拼团 (管理端)
 * @returns {Promise}
 */
export function getGroups() {
  return request.get('/api/groups');
}

/**
 * 获取活跃拼团列表
 * @returns {Promise}
 */
export function getActiveGroups() {
  return request.get('/api/groups/active');
}

/**
 * 获取当前用户的拼团记录
 * @returns {Promise}
 */
export function getMyGroups() {
  return request.get('/api/groups/me');
}

/**
 * 获取拼团详情
 * @param {number|string} id - 拼团ID
 * @returns {Promise}
 */
export function getGroupDetail(id) {
  return request.get(`/api/groups/${id}`);
}

/**
 * 获取拼团成员列表
 * @param {number|string} id - 拼团ID
 * @returns {Promise}
 */
export function getGroupMembers(id) {
  return request.get(`/api/groups/${id}/members`);
}

/**
 * 发起拼团
 * @param {object} data - { productId, address }
 * @returns {Promise}
 */
export function startGroup(data) {
  return request.post('/api/groups/start', data);
}

/**
 * 加入拼团
 * @param {number|string} groupId - 拼团ID
 * @param {object} data - { address }
 * @returns {Promise}
 */
export function joinGroup(groupId, data) {
  return request.post(`/api/groups/${groupId}/join`, data);
}

/**
 * 强制成团 (管理端)
 * @param {number|string} groupId - 拼团ID
 * @returns {Promise}
 */
export function forceGroupSuccess(groupId) {
  return request.post(`/api/groups/${groupId}/force-success`);
}

/**
 * 强制失败/解散 (管理端)
 * @param {number|string} groupId - 拼团ID
 * @returns {Promise}
 */
export function forceGroupFail(groupId) {
  return request.post(`/api/groups/${groupId}/force-fail`);
}
