/**
 * 成功状态
 * @type {string}
 */
export const CODE_SUCCESS: string = '0000';

/**
 * 无效Token
 * @type {string}
 */
export const CODE_TOKEN_FAIL: string = 'A105';

import { message } from 'ant-design-vue';

/**
 * @description: 校验网络请求状态码
 * @param {Number} status
 * @param msg
 * @return void
 */
export const checkStatus = (status: number, msg?: string) => {
  let text = msg || '请求失败！';
  switch (status) {
    case 400:
      text = msg || '请求失败！请您稍后重试';
      break;
    case 401:
      text = msg || '登录失效！请您重新登录';
      break;
    case 404:
      text = msg || '你所访问的资源不存在！';
      break;
    case 405:
      text = msg || '请求方式错误！请您稍后重试';
      break;
    case 408:
      text = msg || '请求超时！请您稍后重试';
      break;
    case 422:
      text = msg || '请求参数异常！';
      break;
    case 500:
      text = msg || '服务异常！';
      break;
    case 502:
      text = msg || '网关错误！';
      break;
    case 503:
      text = msg || '服务不可用！';
      break;
    case 504:
      text = msg || '网关超时！';
      break;
    default:
      text = msg || '请求失败！';
      break;
  }
  message.error(text);
};
