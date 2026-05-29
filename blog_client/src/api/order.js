import request from '@/utils/request';

/**
 * 获取全部订单 (管理端)
 * @returns {Promise}
 */
export function getOrderDetail(id) {
  return request.get(`/api/orders/${id}`);
}

/**
 * 创建支付宝支付表单
 * @param {string|number} orderId - 订单 ID
 * @returns {Promise}
 */
export function createAlipayOrder(orderId) {
  return request.post(`/api/pay/alipay/create?orderId=${orderId}`);
}

// ================= 管理端接口 ================= //

/**
 * 获取所有订单列表（管理端）
 * @returns {Promise}
 */
export function getOrdersAdmin() {
  return request.get('/api/orders');
}

/**
 * 获取当前用户的订单
 * @returns {Promise}
 */
export function getMyOrders() {
  return request.get('/api/orders/me');
}

/**
 * 创建订单
 * @param {object} data - 订单数据
 * @returns {Promise}
 */
export function createOrder(data) {
  return request.post('/api/orders/create', data);
}

/**
 * 积分兑换商品
 * @param {object} data - { productId, address }
 * @returns {Promise}
 */
export function redeemOrder(data) {
  return request.post('/api/orders/redeem', data);
}

/**
 * 订单发货 (管理端)
 * @param {number|string} id - 订单ID
 * @returns {Promise}
 */
export function shipOrder(id) {
  return request.post(`/api/orders/${id}/ship`);
}

/**
 * 创建支付宝支付订单
 * @param {number|string} orderId - 订单ID
 * @returns {Promise}
 */
export function createAlipay(orderId) {
  return request.post(`/api/pay/alipay/create?orderId=${orderId}`);
}
