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

    // 智能获取后端基础域名
    let base = import.meta.env.VITE_API_BASE_URL || '';
    if (!base) {
      const isApp = typeof window !== 'undefined' && (window.Capacitor || !window.location.host || window.location.host.includes('localhost'));
      if (isApp) {
        base = 'https://caibread.com';
      } else if (typeof window !== 'undefined' && window.location.origin) {
        base = window.location.origin;
      }
    }
    return `${base.replace(/\/$/, '')}${relativePath}`;
  }

  return url;
}
