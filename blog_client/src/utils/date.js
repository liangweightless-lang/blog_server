/**
 * 格式化时间字符串
 * @param {string|Date} timeStr - 待格式化的日期字符串或 Date 对象
 * @param {boolean} includeTime - 是否包含时间部分 (HH:mm)
 * @returns {string} 格式化后的日期字符串 (例如: "YYYY-MM-DD HH:mm")
 */
export function formatTime(timeStr, includeTime = true) {
  if (!timeStr) return '';
  
  const d = new Date(timeStr);
  if (isNaN(d.getTime())) return ''; // 检查日期是否有效
  
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  
  const datePart = `${year}-${month}-${day}`;
  
  if (!includeTime) {
    return datePart;
  }
  
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  
  return `${datePart} ${hours}:${minutes}`;
}

/**
 * 仅格式化日期
 * @param {string|Date} dateStr - 待格式化的日期字符串或 Date 对象
 * @returns {string} 格式化后的日期字符串 (例如: "YYYY-MM-DD")
 */
export function formatDate(dateStr) {
  return formatTime(dateStr, false);
}
