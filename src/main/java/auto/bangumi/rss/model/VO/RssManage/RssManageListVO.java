package auto.bangumi.rss.model.VO.RssManage;

import auto.bangumi.rss.model.Rss;
import auto.bangumi.rss.model.entity.RssManage;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.fastjson.JSON;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;

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

    /**
     * 配置
     */
    private RssManageConfigVO config;

    public static RssManageListVO copy(RssManage rssManage) {
        return RssManageListVO.builder()
                .id(rssManage.getId())
                .officialTitle(rssManage.getOfficialTitle())
                .season(rssManage.getSeason())
                .status(rssManage.getStatus())
                .savePath(rssManage.getSavePath())
                .complete(rssManage.getComplete())
                .updateWeek(rssManage.getUpdateWeek())
                .sendDate(rssManage.getSendDate())
                .config(StringUtils.isNotBlank(rssManage.getConfig()) ? JSON.parseObject(rssManage.getConfig(), RssManageConfigVO.class) : new RssManageConfigVO())
                .build();
    }
}
