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
