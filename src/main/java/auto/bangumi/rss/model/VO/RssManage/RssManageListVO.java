package auto.bangumi.rss.model.VO.RssManage;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RssManageListVO {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 动画标题
     */
    private String officialTitle;

    /**
     * 季度
     */
    private String season;

    /**
     * 推送的最新剧集
     */
    private String lastEpisodeNum;

    /**
     * 是否启用
     */
    private String status;

    /**
     * 保存路径
     */
    private String savePath;

    /**
     * 是否完结 0 否 1 是
     */
    private String complete;

    /**
     * 更新星期 1 2 3 4 5 6 7
     */
    private Integer updateWeek;

    /**
     * 发布日期 yyyy-MM-dd
     */
    private String sendDate;
}
