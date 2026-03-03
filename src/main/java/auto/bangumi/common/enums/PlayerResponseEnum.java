package auto.bangumi.common.enums;

/**
 * PlayerResponseEnum
 *
 * @author sakura
 * @version 1.0
 * @since 2026/03/03
 */
public enum PlayerResponseEnum implements ResponseEnumTemplate<PlayerResponseEnum> {

    VALID_ERROR(5000, "参数缺失"),
    UNKNOWN_PLAYER(5001, "未知播放器策略"),
    UNKNOWN_ERROR(5999, "未知错误"),

    ;

    /**
     * 返回码
     */
    private final int code;

    /**
     * 返回消息
     */
    private final String message;

    PlayerResponseEnum(int code, String message) {
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
        return ErrorPrefixEnum.PR;
    }
}
