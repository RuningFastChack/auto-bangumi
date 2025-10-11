package auto.bangumi.rss.model.VO.RssManage;

import auto.bangumi.rss.model.Rss;
import auto.bangumi.rss.model.entity.RssManage;
import com.alibaba.fastjson.JSON;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RssManageVO {

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
     * 是否启用
     */
    private String status;

    /**
     * 下载规则
     */
    private List<String> filter;

    /**
     * 图片路径
     */
    private String posterLink;

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

    /**
     * 改动画的订阅合集
     */
    private List<Rss> rssList;

    /**
     * 配置
     */
    private RssManageConfigVO config;

    /**
     * 复制
     *
     * @param rssManage
     * @return
     */
    public static RssManageVO copy(RssManage rssManage) {
        return RssManageVO.builder()
                .id(rssManage.getId())
                .officialTitle(rssManage.getOfficialTitle())
                .season(rssManage.getSeason())
                .status(rssManage.getStatus())
                .filter(StringUtils.isNotBlank(rssManage.getFilter()) ? Arrays.asList(rssManage.getFilter().split(",")) : new ArrayList<>())
                .posterLink(rssManage.getPosterLink())
                .savePath(rssManage.getSavePath())
                .complete(rssManage.getComplete())
                .updateWeek(rssManage.getUpdateWeek())
                .sendDate(rssManage.getSendDate())
                .rssList(StringUtils.isNotBlank(rssManage.getRssList()) ? JSON.parseArray(rssManage.getRssList(), Rss.class) : new ArrayList<>())
                .config(StringUtils.isNotBlank(rssManage.getConfig()) ? JSON.parseObject(rssManage.getConfig(), RssManageConfigVO.class) : new RssManageConfigVO())
                .build();

    }
}
