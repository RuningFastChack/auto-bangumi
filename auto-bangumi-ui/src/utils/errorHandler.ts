import { notification } from 'ant-design-vue';
import { t } from '@/config/lang/i18n.ts';

/**
 * @description 全局代码错误捕捉
 */
const errorHandler = (error: any): void => {
  // 过滤 HTTP 请求错误
  if (!error || error.status || error.status === 0) return;

  const errorMap: { [key: string]: string } = {
    InternalError: t('TXT_CODE_ERROR_HANDLER_INTERNAL_ERROR'),
    ReferenceError: t('TXT_CODE_ERROR_HANDLER_REFERENCE_ERROR'),
    TypeError: t('TXT_CODE_ERROR_HANDLER_TYPE_ERROR'),
    RangeError: t('TXT_CODE_ERROR_HANDLER_RANGE_ERROR'),
    SyntaxError: t('TXT_CODE_ERROR_HANDLER_SYNTAX_ERROR'),
    EvalError: t('TXT_CODE_ERROR_HANDLER_EVAL_ERROR'),
    URIError: t('TXT_CODE_ERROR_HANDLER_URI_ERROR')
  };

  const errorName = errorMap[error.name] || t('TXT_CODE_ERROR_HANDLER_UNKNOWN_ERROR');

  notification.error({
    message: errorName,
    description: error.message || String(error),
    duration: 3
  });
};

export default errorHandler;
