import request from '@/utils/request';

// --- 提货点管理 (Delivery Locations) ---
export function getDeliveryLocations() {
  return request.get('/api/delivery-locations');
}
export function getActiveDeliveryLocations() {
  return request.get('/api/delivery-locations/active');
}
export function createDeliveryLocation(data) {
  return request.post('/api/delivery-locations', data, {
    headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
  });
}
export function updateDeliveryLocation(id, data) {
  return request.put(`/api/delivery-locations/${id}`, data, {
    headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
  });
}
export function deleteDeliveryLocation(id) {
  return request.delete(`/api/delivery-locations/${id}`, {
    headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
  });
}

// --- 团购活动管理 (Campaigns) ---
export function getCampaigns() {
  return request.get('/api/campaigns');
}
export function getCampaignById(id) {
  return request.get(`/api/campaigns/${id}`);
}
export function createCampaign(data) {
  return request.post('/api/campaigns', data, {
    headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
  });
}
export function updateCampaign(id, data) {
  return request.put(`/api/campaigns/${id}`, data, {
    headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
  });
}
export function updateCampaignStatus(id, status) {
  return request.put(`/api/campaigns/${id}/status?status=${status}`, {}, {
    headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
  });
}
export function deleteCampaign(id) {
  return request.delete(`/api/campaigns/${id}`, {
    headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
  });
}

// --- 跟团订单管理 (Campaign Orders) ---
export function createCampaignOrder(campaignId, data) {
  return request.post(`/api/campaigns/${campaignId}/orders`, data, {
    headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
  });
}
export function getCampaignOrders(campaignId) {
  return request.get(`/api/campaigns/${campaignId}/orders`, {
    headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
  });
}
export function updateCampaignOrderStatus(orderId, status) {
  return request.put(`/api/campaigns/orders/${orderId}/status?status=${status}`, {}, {
    headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
  });
}

export function getMyCampaignOrders() {
  return request.get(`/api/campaigns/orders/my`, {
    headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
  });
}
