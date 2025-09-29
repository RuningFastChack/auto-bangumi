package auto.bangumi.rss.model.DTO.RssManage;

import auto.bangumi.common.model.dto.PageQuery;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RssManageListDTO extends PageQuery {

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
    private String sendDateForm;

    private String sendDateTo;
}