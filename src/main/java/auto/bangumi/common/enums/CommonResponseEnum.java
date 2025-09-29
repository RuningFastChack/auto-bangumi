package auto.bangumi.common.enums;

/**
 * 异常枚举类
 */
public enum CommonResponseEnum implements ResponseEnumTemplate<CommonResponseEnum> {

    // @formatter:off
    VALID_ERROR(100, "参数校验异常"),

    BAD_USERNAME_OR_PASSWORD(101, "账户不存在或密码错误"),

    INVALID_TOKEN(105, "无效Token"),

    INVALID_USER(106, "无效用户"),

    DEBOUNCE(110, "您的请求过于频繁，请稍后再试！"),

    INVALID_ID(1000, "无效ID"),

    EXISTS(1001, "已存在"),

    NOT_EXISTS(1002, "不存在"),

    UNKNOWN(9999, "未知异常"),
    ;
    // @formatter:on

    /**
     * 返回码
     */
    private final int code;

    /**
     * 返回消息
     */
    private final String message;

    CommonResponseEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public ErrorPrefixEnum getCodePrefixEnum() {
        return ErrorPrefixEnum.ADMIN;
    }
}