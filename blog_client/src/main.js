import { createApp } from 'vue'
import '@arco-design/web-vue/dist/arco.css'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import './utils/request'

const app = createApp(App)
const pinia = createPinia()

import { formatTime, formatDate } from './utils/date'
app.config.globalProperties.$formatTime = formatTime
app.config.globalProperties.$formatDate = formatDate

app.use(pinia)
app.use(router)

import { Message } from '@arco-design/web-vue';


app.mount('#app')
