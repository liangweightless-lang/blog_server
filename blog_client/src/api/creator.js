import request from '@/utils/request';

/**
 * 提交成为小柴包酱申请
 */
export function applyCreator(data) {
  return request.post('/api/creator/apply', data);
}

/**
 * 查询我的小柴包酱申请状态
 */
export function getMyCreatorStatus() {
  return request.get('/api/creator/my-status');
}

/**
 * 管理员获取小柴包酱申请列表
 */
export function getCreatorApplications() {
  return request.get('/api/creator/admin/list');
}

/**
 * 管理员审核通过
 */
export function approveCreatorApplication(id) {
  return request.post(`/api/creator/admin/${id}/approve`);
}

/**
 * 管理员驳回申请
 */
export function rejectCreatorApplication(id, reason) {
  return request.post(`/api/creator/admin/${id}/reject`, { reason });
}
