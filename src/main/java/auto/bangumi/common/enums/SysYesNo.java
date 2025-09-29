package auto.bangumi.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * SysYesNo
 *
 * @author 查查
 * @since 2025/9/9
 */
@Getter
@AllArgsConstructor
public enum SysYesNo {

    YSE("1", "是"),
    NO("0", "否");

    private final String code;

    private final String name;

}
