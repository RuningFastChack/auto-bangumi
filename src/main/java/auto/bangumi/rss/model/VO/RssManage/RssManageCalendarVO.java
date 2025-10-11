package auto.bangumi.rss.model.VO.RssManage;

import auto.bangumi.rss.model.entity.RssManage;
import com.alibaba.fastjson.JSON;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 配置
     */
    private RssManageConfigVO config;

    public static RssManageCalendarVO copy(RssManage rssManage){
        return RssManageCalendarVO.builder()
                .id(rssManage.getId())
                .officialTitle(rssManage.getOfficialTitle())
                .season(rssManage.getSeason())
                .updateWeek(rssManage.getUpdateWeek())
                .sendDate(rssManage.getSendDate())
                .posterLink(rssManage.getPosterLink())
                .config(StringUtils.isNotBlank(rssManage.getConfig()) ? JSON.parseObject(rssManage.getConfig(), RssManageConfigVO.class) : new RssManageConfigVO())
                .build();
    }
}
