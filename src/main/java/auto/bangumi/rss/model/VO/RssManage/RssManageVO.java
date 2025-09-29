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
     * 推送的最新剧集
     */
    private String lastEpisodeNum;

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
     * 复制
     *
     * @param dto
     * @return
     */
    public static RssManageVO copy(RssManage dto) {
        RssManageVO result = RssManageVO.builder().build();
        BeanUtil.copyProperties(dto, result, CopyOptions.create().setIgnoreProperties("filter", "rssList"));
        result.setFilter(StringUtils.isNotBlank(dto.getFilter()) ? Arrays.asList(dto.getFilter().split(",")) : new ArrayList<>());
        result.setRssList(StringUtils.isNotBlank(dto.getRssList()) ? JSON.parseArray(dto.getRssList(), Rss.class) : new ArrayList<>());
        return result;
    }
}
