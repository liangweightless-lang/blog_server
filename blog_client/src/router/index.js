import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import CreateArticle from '../views/CreateArticle.vue'
import Store from '../views/Store.vue'
import ArticleDetail from '../views/ArticleDetail.vue'
import AdminDashboard from '../views/AdminDashboard.vue'
import UserProfile from '../views/UserProfile.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
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
    path: '/article/:id',
    name: 'ArticleDetail',
    component: ArticleDetail
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboard
  },
  {
    path: '/profile',
    name: 'UserProfile',
    component: UserProfile
  }
]

const router = new VueRouter({
  mode: 'history',
  base: '/',
  routes
})

export default router
