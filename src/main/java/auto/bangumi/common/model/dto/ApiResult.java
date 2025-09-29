package auto.bangumi.common.model.dto;

import auto.bangumi.common.enums.CommonResponseEnum;
import auto.bangumi.common.exception.common.BusinessExceptionCustomAssert;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

/**
 * ApiResult
 * 工具类来源sz-admin 官方文档：https://szadmin.cn
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
public class ApiResult<T> {

    public String code = "0000";

    public String message = "SUCCESS";

    public T data;

    private Object param;

    public ApiResult() {
    }

    public ApiResult(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static <T> ApiResult<T> success() {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.data = null;
        return apiResult;
    }

    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.data = data;
        return apiResult;
    }

    public static <T> ApiResult<T> success(T data, Object param) {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.data = data;
        if (Objects.nonNull(param)) {
            apiResult.param = param;
        }
        return apiResult;
    }

    public static <T> ApiResult<T> error(CommonResponseEnum responseEnum) {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.code = getResponseCode(responseEnum);
        apiResult.message = responseEnum.getMessage();
        apiResult.data = null;
        return apiResult;
    }

    protected static String getResponseCode(CommonResponseEnum responseEnum) {
        return responseEnum.getCodePrefixEnum().getPrefix() + responseEnum.getCode();
    }

    public static <T> ApiResult<T> error(BusinessExceptionCustomAssert responseEnum) {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.code = getResponseCode(responseEnum);
        apiResult.message = responseEnum.getMessage();
        apiResult.data = null;
        return apiResult;
    }

    protected static String getResponseCode(BusinessExceptionCustomAssert responseEnum) {
        return responseEnum.getCodePrefixEnum().getPrefix() + responseEnum.getCode();
    }

}