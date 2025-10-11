package auto.bangumi.rss.model.VO.RssManage;

import lombok.*;

/**
 * RssManage配置
 *
 * @author sakura
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RssManageConfigVO {

    /**
     * 最新一集
     */
    private String latestEpisode;

    /**
     * 总集数
     */
    private String totalEpisode;
}
