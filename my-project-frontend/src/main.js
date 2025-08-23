import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from "axios";

import 'element-plus/theme-chalk/dark/css-vars.css'

//TODO 不要放开
//开发环境
// axios.defaults.baseURL = 'http://localhost:8080'
//生产环境
axios.defaults.baseURL = '/'

const app = createApp(App)

// 只用来做上传图片接口全局
app.config.globalProperties.$uploadAction = (path = '/api/auth/common/upload') => {
   const b =  `${axios.defaults.baseURL}${path}`;
   console.log("上传图片=====",b);
   return b;
}

app.use(router)

app.mount('#app')
