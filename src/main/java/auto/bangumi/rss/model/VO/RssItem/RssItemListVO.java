package auto.bangumi.rss.model.VO.RssItem;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RssItemListVO {

    /**
     * 主键
     */
    private Integer id;

    /**
     * torrent的哈希值
     */
    private String torrentCode;

    /**
     * 剧集数
     */
    private String episodeNum;

    /**
     * RssManage的ID
     */
    private Integer rssManageId;

    /**
     * 字幕组
     */
    private String translationGroup;

    /**
     * 保存路径
     */
    private String savePath;

    /**
     * torrent的原始标题
     */
    private String torrentName;

    /**
     * 剧集名称 xxx S00E00
     */
    private String name;

    /**
     * 是否下载完成
     */
    private String downloaded;

    /**
     * 是否启用
     */
    private String status;

    /**
     * 是否推送
     */
    private String pushed;
}
