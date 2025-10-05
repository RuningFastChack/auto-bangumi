package auto.bangumi.qBittorrent.model.Request;

import auto.bangumi.qBittorrent.enums.TorrentsStatusEnum;
import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * TorrentsInfoListRequest
 *
 * @author sakura
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TorrentsInfoListRequest {
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
    private List<String> hashes;

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("filter", this.getFilter());
        result.put("category", this.getCategory());
        result.put("tag", this.getTag());
        result.put("sort", this.getSort());
        result.put("reverse", this.getReverse());
        if (Objects.nonNull(this.getHashes()) && !this.getHashes().isEmpty()) {
            result.put("hashes", String.join("|", this.getHashes()));
        }
        return result;
    }
}
