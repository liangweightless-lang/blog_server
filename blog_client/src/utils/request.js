import axios from 'axios';
import { Message } from '@arco-design/web-vue';
import { getApiBaseUrl } from './image';

// 1. 创建独立的 Axios 实例，不污染全局
const request = axios.create({
  // 智能区分本地、测试服务器 (test.caibread.com) 与生产服 (caibread.com)，避免 App 错连
  baseURL: getApiBaseUrl(),
  timeout: 10000
});

// 2. 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      if (!config.headers) {
        config.headers = {};
      }
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 3. 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 后端现已直接返回完整的 OSS/CDN 链接，前端直接无损放行
    return response;
  },
  (error) => {
    // 统一的错误拦截处理
    if (error.response) {
      const { status, data } = error.response;
      
      if (status === 401) {
        localStorage.removeItem('token');
        window.dispatchEvent(new CustomEvent('auth-expired'));
      } else {
        // 统一弹出后端的错误提示，避免组件到处写 catch Message.error
        // 注意：某些静默请求可能不需要报错提示，这里为了演示保留全局提示
        if (data && data.message) {
           Message.error(data.message);
        }
      }
    } else {
      Message.error('网络连接异常或请求超时');
    }
    return Promise.reject(error);
  }
);

export default request;
