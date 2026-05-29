import request from '@/utils/request';

export function getProductCategories() {
  return request.get('/api/product-categories');
}

export function createProductCategory(data) {
  return request.post('/api/product-categories', data, {
    headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
  });
}

export function updateProductCategory(id, data) {
  return request.put(`/api/product-categories/${id}`, data, {
    headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
  });
}

export function deleteProductCategory(id) {
  return request.delete(`/api/product-categories/${id}`, {
    headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
  });
}

export function sortProductCategories(categories) {
  return request.post('/api/product-categories/sort', categories, {
    headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
  });
}
