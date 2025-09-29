package auto.bangumi.rss.model.VO.RssManage;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RssManageCalendarVO {

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
     * 更新星期 1 2 3 4 5 6 7
     */
    private Integer updateWeek;

    /**
     * 发布日期 yyyy-MM-dd
     */
    private String sendDate;

    /**
     * 图片路径
     */
    private String posterLink;
}
