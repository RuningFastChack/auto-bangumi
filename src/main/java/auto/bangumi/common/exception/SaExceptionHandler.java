package auto.bangumi.common.exception;

import auto.bangumi.common.enums.CommonResponseEnum;
import auto.bangumi.common.model.dto.ApiResult;
import cn.dev33.satoken.exception.NotLoginException;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * SaExceptionHandler
 *
 * @author 查查
 * @since 2025/9/13
 */
@Order(Integer.MIN_VALUE)
@RestControllerAdvice
public class SaExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public ApiResult<Void> handlerNotLoginException(NotLoginException e) {
        String message;
        if (e.getType().equals(NotLoginException.NOT_TOKEN)) {
            message = "未能读取到有效 token";
        } else if (e.getType().equals(NotLoginException.INVALID_TOKEN) || e.getType().equals(NotLoginException.TOKEN_FREEZE)) {
            message = "您的登录信息已过期，请重新登录以继续访问。";
        } else if (e.getType().equals(NotLoginException.TOKEN_TIMEOUT)) {
            message = "token 已过期";
        } else if (e.getType().equals(NotLoginException.BE_REPLACED)) {
            message = "token 已被顶下线";
        } else if (e.getType().equals(NotLoginException.KICK_OUT)) {
            message = "token 已被踢下线";
        } else if (e.getType().equals(NotLoginException.NO_PREFIX)) {
            message = "未按照指定前缀提交 token";
        } else {
            message = "当前会话未登录";
        }
        return new ApiResult<>(CommonResponseEnum.INVALID_TOKEN.getCodePrefixEnum().getPrefix() + CommonResponseEnum.INVALID_TOKEN.getCode(), message);
    }
}
