import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/home/Home.vue'),
    meta: { hideHeaderMobile: true }
  },
  {
    path: '/create',
    name: 'CreateArticle',
    component: () => import('../views/article/CreateArticle.vue'),
    meta: { hideBottomNav: true }
  },
  {
    path: '/store',
    name: 'Store',
    component: () => import('../views/product/Store.vue')
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('../views/product/ProductDetail.vue'),
    meta: { hideHeaderMobile: true, hideBottomNav: true }
  },
  {
    path: '/product/group/:id',
    name: 'GroupDetail',
    component: () => import('../views/product/GroupDetail.vue'),
    meta: { hideHeaderMobile: true, hideBottomNav: true }
  },
  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: () => import('../views/article/ArticleDetail.vue'),
    meta: { hideHeaderMobile: true, hideBottomNav: true }
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('../views/admin/AdminDashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'UserProfile',
    component: () => import('../views/user/UserProfile.vue'),
    meta: { hideHeaderMobile: true, requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory('/'),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  if (to.meta.requiresAuth && !token) {
    window.dispatchEvent(new CustomEvent('open-login'));
    next('/');
  } else {
    next();
  }
});

// 处理线上版本更新导致的按需加载失败问题
router.onError((error, to) => {
  if (error.message.includes('Failed to fetch dynamically imported module') || error.message.includes('Loading chunk')) {
    // 检测到 JS 文件失效时，自动重新加载页面以获取最新的 index.html 和 JS 资源
    window.location.href = to.fullPath;
  }
});

export default router
