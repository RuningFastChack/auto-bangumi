package auto.bangumi.common.enums;

/**
 * MessageResponseEnum
 *
 * @author sakura
 * @version 1.0
 * @since 2026/06/29
 */
public enum MessageResponseEnum implements ResponseEnumTemplate<MessageResponseEnum> {

    VALID_ERROR(5000, "参数缺失"),

    PUSHER_NOT_FOUND(5001, "消息推送策略不存在"),

    MESSAGE_CONFIG_ERROR(5002, "消息推送配置异常"),

    OPEN_CLAW_CONFIG_ERROR(5100, "OpenClaw消息推送配置缺失"),

    OPEN_CLAW_DELIVERY_ERROR(5101, "OpenClaw Delivery JSON格式错误"),

    OPEN_CLAW_REQUEST_ERROR(5102, "OpenClaw消息推送请求失败"),

    SERVER_CHAN_CONFIG_ERROR(5200, "Server酱消息推送配置缺失"),

    SERVER_CHAN_REQUEST_ERROR(5201, "Server酱消息推送请求失败"),

    SERVER_CHAN_RESPONSE_ERROR(5202, "Server酱消息推送响应异常"),

    ;

    /**
     * 返回码
     */
    private final int code;

    /**
     * 返回消息
     */
    private final String message;

    MessageResponseEnum(int code, String message) {
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
        return ErrorPrefixEnum.MESSAGE;
    }
}
