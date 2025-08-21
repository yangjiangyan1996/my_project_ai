import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from "axios";

import 'element-plus/theme-chalk/dark/css-vars.css'

//开发环境
// axios.defaults.baseURL = 'http://localhost:8080'
//生产环境
axios.defaults.baseURL = '/api'

const app = createApp(App)

app.use(router)

app.mount('#app')
