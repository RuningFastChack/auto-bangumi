package auto.bangumi.rss.model.VO.RssManage;

import lombok.*;

/**
 * 查看番剧
 *
 * @author sakura
 * @version 1.0
 * @since 2026/03/02
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RssManageItemVO {
    private String rssManageId;

    private String officialTitle;

    private String officialTitleEn;

    private String officialTitleJp;

}
