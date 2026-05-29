import request from '@/utils/request';

/**
 * 获取文章列表
 * @returns {Promise}
 */
export function getArticles() {
  return request.get('/api/articles');
}

/**
 * 获取文章详情
 * @param {number|string} id - 文章ID
 * @returns {Promise}
 */
export function getArticleDetail(id) {
  return request.get(`/api/articles/${id}`);
}

/**
 * 创建/更新文章
 * @param {object} data - 文章数据
 * @returns {Promise}
 */
export function saveArticle(data) {
  return request.post('/api/articles', data);
}

/**
 * 更新文章 (管理端)
 * @param {string|number} id - 文章ID
 * @param {object} data - 文章数据
 * @returns {Promise}
 */
export function updateArticle(id, data) {
  return request.put(`/api/articles/${id}`, data);
}

/**
 * 删除文章
 * @param {number|string} id - 文章ID
 * @returns {Promise}
 */
export function deleteArticle(id) {
  return request.delete(`/api/articles/${id}`);
}

/**
 * 点赞文章
 * @param {number|string} id - 文章ID
 * @returns {Promise}
 */
export function likeArticle(id) {
  return request.post(`/api/articles/${id}/like`);
}

/**
 * 获取文章的评论列表
 * @param {number|string} id - 文章ID
 * @returns {Promise}
 */
export function getComments(id) {
  return request.get(`/api/comments/article/${id}`);
}

/**
 * 发表评论
 * @param {object} data - { articleId, content }
 * @returns {Promise}
 */
export function postComment(data) {
  return request.post('/api/comments', data);
}

/**
 * 获取我的收藏列表
 * @returns {Promise}
 */
export function getMyFavorites() {
  return request.get('/api/favorites/me');
}

/**
 * 检查文章是否已收藏
 * @param {number|string} articleId - 文章ID
 * @returns {Promise}
 */
export function checkFavoriteStatus(articleId) {
  return request.get(`/api/favorites/status/${articleId}`);
}

/**
 * 切换收藏状态
 * @param {number|string} articleId - 文章ID
 * @returns {Promise}
 */
export function toggleFavorite(articleId) {
  return request.post('/api/favorites/toggle', { articleId });
}
