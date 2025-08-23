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

// 添加上传图片获取地址的全局属性
// 只用来上传文件
// 只用来上传文件
// 只用来上传文件
// 只用来上传文件
app.config.globalProperties.$uploadAction = (path = '/api/auth/common/upload') => {
   const url = "https://xsidework.com";
   const b =  `${url}${path}`;
   console.log("上传图片=====",b);
   return b;
}

app.use(router)

app.mount('#app')
