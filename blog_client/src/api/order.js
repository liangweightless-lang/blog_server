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

/**
 * 创建微信支付订单（支持Native扫码/H5自适应）
 * @param {number|string} orderId - 订单ID
 * @returns {Promise}
 */
export function createWechatPay(orderId) {
  return request.post(`/api/pay/wechat/create?orderId=${orderId}`);
}

/**
 * 查询微信支付订单状态（扫码收银台轮询）
 * @param {number|string} orderId - 订单ID
 * @returns {Promise}
 */
export function checkWechatPayStatus(orderId) {
  return request.get(`/api/pay/wechat/query?orderId=${orderId}`);
}

/**
 * 管理员手动确认订单已收款
 * @param {number|string} orderId - 订单ID
 * @returns {Promise}
 */
export function confirmOrderPay(orderId) {
  return request.post(`/api/orders/${orderId}/confirm-pay`);
}


