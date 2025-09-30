package auto.bangumi.qBittorrent.model.Request;

import auto.bangumi.common.model.dto.PageQuery;
import auto.bangumi.qBittorrent.enums.TorrentsStatusEnum;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * TorrentsInfoListRequest
 *
 * @author sakura
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TorrentsInfoListRequest extends PageQuery {
    /**
     * 过滤状态
     * all, downloading, seeding, completed, paused,
     * active, inactive, resumed, stalled,
     * stalled_uploading, stalled_downloading, errored
     */
    private TorrentsStatusEnum filter;
    /**
     * 分类 (URL 编码后传输)
     * 空字符串: 无分类
     * null: 任意分类
     */
    private String category;
    /**
     * 标签 (URL 编码后传输)
     * 空字符串: 无标签
     * null: 任意标签
     */
    private String tag;
    /**
     * 排序字段
     * 可用字段：torrent JSON 返回中的任何 key
     */
    private String sort;
    /**
     * 是否反向排序
     * 默认 false
     */
    private String reverse;
    /**
     * 指定种子 Hash
     * 多个 hash 用 | 分隔
     */
    private String hashes;
}
