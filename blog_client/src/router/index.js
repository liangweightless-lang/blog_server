import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import CreateArticle from '../views/CreateArticle.vue'

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
  }
]

const router = new VueRouter({
  mode: 'history',
  base: '/',
  routes
})

export default router
