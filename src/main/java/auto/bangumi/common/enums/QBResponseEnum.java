package auto.bangumi.common.enums;

/**
 * QBResponseEnum
 *
 * @author 查查
 * @since 2025/10/4
 */
public enum QBResponseEnum implements ResponseEnumTemplate<QBResponseEnum> {

    VALID_ERROR(5000, "参数缺失"),

    QB_REQUEST_ERROR(5001, "请求失败"),

    CATEGORIES_OPERATE_ERROR(5100,"QB分类异常"),

    TORRENT_OPERATE_ERROR(5200,"种子异常")

    ;

    /**
     * 返回码
     */
    private final int code;

    /**
     * 返回消息
     */
    private final String message;

    QBResponseEnum(int code, String message) {
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
        return ErrorPrefixEnum.QB;
    }
}
