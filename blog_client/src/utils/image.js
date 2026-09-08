/**
 * 获取当前运行环境真实的后端基础域名 (Base URL)
 * 智能区分本地开发、测试服务器 (test.caibread.com)、生产服 (caibread.com) 与 App (Capacitor) 离线容器
 */
export function getApiBaseUrl() {
  // 1. 优先使用编译期显式注入的环境变量 (如 .env.test 或 .env.production 中的配置)
  if (import.meta.env?.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL.replace(/\/$/, '');
  }

  // 2. 智能读取当前网页运行环境的真实 Origin (只要不是 localhost / 127.0.0.1 等本地开发调试)
  // 无论是在手机浏览器、还是在加载线上网页的手机 App 中，始终严格遵循当前页面的真实域名 (如 test.caibread.com 或 caibread.com)
  if (typeof window !== 'undefined' && window.location) {
    const { origin, hostname, protocol } = window.location;
    if (origin && protocol.startsWith('http') && !hostname.includes('localhost') && !hostname.includes('127.0.0.1')) {
      return origin.replace(/\/$/, '');
    }
  }

  // 3. 仅在纯原生本地离线打包 (http://localhost 或 capacitor://localhost) 且未配置环境变量时安全回退
  return 'https://caibread.com';
}

/**
 * 全局统一图片地址处理工具
 * 完美解决打包 App 与网页端相对路径/绝对路径不一致、uploads 图片在 App 端无法显示的问题
 */
export function formatImageUrl(url) {
  if (!url || typeof url !== 'string') {
    return '';
  }

  // 1. 如果已是完整的网络链接或 base64，直接返回
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:image')) {
    return url;
  }

  // 2. 处理 /uploads/ 或 uploads/ 相对路径
  if (url.includes('/uploads/') || url.startsWith('uploads/')) {
    // 规范化路径部分为 /uploads/xxx
    const uploadIndex = url.indexOf('/uploads/');
    const relativePath = uploadIndex !== -1 ? url.substring(uploadIndex) : (url.startsWith('/') ? url : `/${url}`);

    const base = getApiBaseUrl();
    return `${base}${relativePath}`;
  }

  return url;
}
