/**
 * 判断客户端
 */
export const isMobile = () => {
  const ua = navigator.userAgent;
  const width = window.innerWidth;
  const isMobileUA = /Mobi|Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(ua);
  const isMobileWidth = width <= 768;
  return isMobileUA || isMobileWidth;
};

export function isLocalEnv() {
  return import.meta.env.MODE === 'development' || import.meta.env.MODE === 'local' || import.meta.env.MODE === 'dev';
}

export const isEmpty = (value: any): boolean => {
  if (value == null) return true; // null 或 undefined
  if (typeof value === 'string') return value.trim().length === 0;
  if (Array.isArray(value)) return value.length === 0;
  if (value instanceof Map || value instanceof Set) return value.size === 0;
  if (typeof value === 'object') return Object.keys(value).length === 0;
  return false;
};

export const deepCopy = <T>(value: T): T => {
  return JSON.parse(JSON.stringify(value));
};

export const formatSpeed = (bytesPerSecond: number, decimals: number = 2): string => {
  if (isEmpty(bytesPerSecond)) {
    return '0 B/s';
  }
  if (bytesPerSecond === 0) return '0 B/s';

  const k = 1024;
  const sizes = ['B/s', 'KB/s', 'MB/s', 'GB/s', 'TB/s'];
  const i = Math.floor(Math.log(bytesPerSecond) / Math.log(k));

  return parseFloat((bytesPerSecond / Math.pow(k, i)).toFixed(decimals)) + ' ' + sizes[i];
}

export const formatMilliseconds = (milliseconds: number): string => {
  if (milliseconds <= 0) return '0秒';
  const seconds = Math.floor(milliseconds / 1000);
  const days = Math.floor(seconds / (24 * 60 * 60));
  const hours = Math.floor((seconds % (24 * 60 * 60)) / (60 * 60));
  const minutes = Math.floor((seconds % (60 * 60)) / 60);
  const remainingSeconds = seconds % 60;

  const parts = [];

  if (days > 0) {
    parts.push(`${days}天`);
  }
  if (hours > 0) {
    parts.push(`${hours}小时`);
  }
  if (minutes > 0) {
    parts.push(`${minutes}分钟`);
  }
  if (remainingSeconds > 0 || parts.length === 0) {
    parts.push(`${remainingSeconds}秒`);
  }

  return parts.join('');
}

export const formatTimestamp = (timestamp: number): string => {
  if (!timestamp || timestamp <= 0) return '-';

  const date = new Date(timestamp * 1000); // 转换为毫秒

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}
