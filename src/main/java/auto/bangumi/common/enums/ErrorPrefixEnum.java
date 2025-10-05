package auto.bangumi.common.enums;

import lombok.Getter;

/**
 * ErrorPrefixEnum - 定义错误前缀的枚举类。
 * 此枚举类用于集中管理和定义应用中的错误前缀，以便统一错误代码格式。
 */
@Getter
public enum ErrorPrefixEnum {

    // @formatter:off
    ADMIN("A", "admin异常"),

    RSS("R", "rss异常"),

    MCS("M", "MCS异常"),

    QB("Q", "QB异常"),

    // ...其他业务模块的异常前缀
    ;
    // @formatter:on

    /**
     * 前缀标识
     */
    private final String prefix;

    /**
     * 描述
     */
    private final String description;

    ErrorPrefixEnum(String prefix, String description) {
        this.prefix = prefix;
        this.description = description;
    }
}
