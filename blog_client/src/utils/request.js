import axios from 'axios';

// Configure default axios instance
axios.defaults.timeout = 10000;

// Detect if running inside Capacitor (mobile App)
const isCapacitor = typeof window !== 'undefined' && (window.hasOwnProperty('Capacitor') || window.Capacitor);
if (isCapacitor) {
  // 【本地开发测试】如果在电脑模拟器上测试本地运行的后端：
  // axios.defaults.baseURL = 'http://10.0.2.2:8081';

  // 【云服务器环境】如果是测试部署在云服务器上的后端，请将下方地址修改为您云服务器的真实域名或公网IP（例如 https://api.yourdomain.com）
  axios.defaults.baseURL = 'https://caibread.com';
}

// Request Interceptor
axios.interceptors.request.use(
  (config) => {
    // Add token to headers if it exists
    const token = localStorage.getItem('token');
    if (token) {
      if (!config.headers) {
        config.headers = {};
      }
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor
axios.interceptors.response.use(
  (response) => {
    // Automatically map relative image paths starting with /uploads/ to the full backend URL in Capacitor
    if (axios.defaults.baseURL && response.data) {
      const baseURL = axios.defaults.baseURL.replace(/\/$/, ''); // Remove trailing slash if exists
      
      // Recursive helper to replace relative /uploads/ URLs in the response payload
      const mapAssetUrls = (obj) => {
        if (typeof obj === 'string') {
          // If it's a direct relative path (e.g. /uploads/image.png)
          if (obj.startsWith('/uploads/')) {
            return baseURL + obj;
          }
          // If it's a rich-text HTML or JSON string containing relative upload paths
          if (obj.includes('/uploads/')) {
            // Replace all occurrences of "/uploads/ or '/uploads/ (covers src=, href=, JSON arrays, etc.)
            return obj.replace(/(["'])(\/uploads\/)/g, `$1${baseURL}/uploads/`);
          }
          return obj;
        }
        if (Array.isArray(obj)) {
          return obj.map(item => mapAssetUrls(item));
        }
        if (obj !== null && typeof obj === 'object') {
          for (const key in obj) {
            if (obj.hasOwnProperty(key)) {
              obj[key] = mapAssetUrls(obj[key]);
            }
          }
        }
        return obj;
      };
      
      response.data = mapAssetUrls(response.data);
    }
    return response;
  },
  (error) => {
    // Handle 401 Unauthorized
    if (error.response && error.response.status === 401) {
      // Token is invalid or expired
      localStorage.removeItem('token');
      // Trigger global event to show login dialog
      window.dispatchEvent(new CustomEvent('auth-expired'));
    }
    return Promise.reject(error);
  }
);

export default axios;
