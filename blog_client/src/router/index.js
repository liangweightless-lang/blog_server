import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/home/Home.vue'
import CreateArticle from '../views/article/CreateArticle.vue'
import Store from '../views/product/Store.vue'
import ArticleDetail from '../views/article/ArticleDetail.vue'
import AdminDashboard from '../views/admin/AdminDashboard.vue'
import UserProfile from '../views/user/UserProfile.vue'
import GroupDetail from '../views/product/GroupDetail.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { hideHeaderMobile: true }
  },
  {
    path: '/create',
    name: 'CreateArticle',
    component: CreateArticle
  },
  {
    path: '/store',
    name: 'Store',
    component: Store
  },
  {
    path: '/product/group/:id',
    name: 'GroupDetail',
    component: GroupDetail,
    meta: { hideHeaderMobile: true }
  },
  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: ArticleDetail,
    meta: { hideHeaderMobile: true }
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboard
  },
  {
    path: '/profile',
    name: 'UserProfile',
    component: UserProfile,
    meta: { hideHeaderMobile: true }
  }
]

const router = new VueRouter({
  mode: 'history',
  base: '/',
  routes
})

export default router
