package auto.bangumi.common.enums;

/**
 * AutoResponseEnum
 *
 * @author 查查
 * @since 2025/9/13
 */
public enum RssResponseEnum implements ResponseEnumTemplate<RssResponseEnum> {

    RSS_ITEM_EXISTS(2102, "订阅剧集不存在"),

    RSS_MANAGE_EXISTS(2202, "订阅管理不存在"),
    ;

    /**
     * 返回码
     */
    private final int code;

    /**
     * 返回消息
     */
    private final String message;

    RssResponseEnum(int code, String message) {
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
        return ErrorPrefixEnum.RSS;
    }
}
