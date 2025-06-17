// 新建axios配置文件
import axios from 'axios'

axios.interceptors.request.use(config => {
  console.log('正在发送请求:', config);
  return config;
});


const instance = axios.create({
  baseURL: '/api',
  timeout: 10000
})

export default instance