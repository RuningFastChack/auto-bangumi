package auto.bangumi.rss.model;

import auto.bangumi.common.enums.RssTypeEnum;
import lombok.*;

/**
 * 链接设置
 *
 * @author 查查
 * @since 2025/9/11
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Rss {

    /**
     * 订阅的链接
     */
    private String rss;

    /**
     * 分组
     */
    private String translationGroup;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否启用
     */
    private String  status;

    /**
     * 链接类型 - Mikan
     */
    private RssTypeEnum type;

    /**
     * 番剧 - 翻译分组？
     */
    private String subGroupId;

    /**
     * 剧集偏移量 +-数量
     * 例如：+1  标识原剧集10变成11
     * 例如：-1  标识原剧集10变成9
     * 0 表示不偏移
     */
    private Integer offset;
}
