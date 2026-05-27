import { createApp } from 'vue'
import '@arco-design/web-vue/dist/arco.css'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import './utils/request'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)
app.mount('#app')
