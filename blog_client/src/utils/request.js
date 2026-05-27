import axios from 'axios';

// Configure default axios instance
axios.defaults.timeout = 10000;

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
