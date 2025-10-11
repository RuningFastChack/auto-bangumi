import { t } from '@/config/lang/i18n.ts';
import { message } from 'ant-design-vue';

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
      text = msg || t('TXT_CODE_RESPONSE_ERROR_400');
      break;
    case 401:
      text = msg || t('TXT_CODE_RESPONSE_ERROR_401');
      break;
    case 404:
      text = msg || t('TXT_CODE_RESPONSE_ERROR_404');
      break;
    case 405:
      text = msg || t('TXT_CODE_RESPONSE_ERROR_405');
      break;
    case 408:
      text = msg || t('TXT_CODE_RESPONSE_ERROR_408');
      break;
    case 422:
      text = msg || t('TXT_CODE_RESPONSE_ERROR_422');
      break;
    case 500:
      text = msg || t('TXT_CODE_RESPONSE_ERROR_500');
      break;
    case 502:
      text = msg || t('TXT_CODE_RESPONSE_ERROR_502');
      break;
    case 503:
      text = msg || t('TXT_CODE_RESPONSE_ERROR_503');
      break;
    case 504:
      text = msg || t('TXT_CODE_RESPONSE_ERROR_504');
      break;
    default:
      text = msg || t('TXT_CODE_RESPONSE_ERROR_OTHER');
      break;
  }
  message.error(text);
};
