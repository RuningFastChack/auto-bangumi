package auto.bangumi.rss.model;

import auto.bangumi.rss.model.DTO.RssManage.RssManageConfigDTO;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisResult {

    /**
     * 标题
     */
    private String title;

    /**
     * 标题
     */
    private String titleEn;

    /**
     * 标题
     */
    private String titleJp;

    /**
     * 季度
     */
    private String season;

    /**
     * 保存路径
     */
    private String savePath;

    /**
     * 分组
     */
    private String translationGroup;

    /**
     * 番剧 - 翻译分组？
     */
    private String subGroupId;

    /**
     * 发布日期 yyyy-MM-dd
     */
    private String sendData;

    /**
     * 更新星期 1 2 3 4 5 6 7
     */
    private Integer updateWeek;

    /**
     * 图片路径
     */
    private String posterLink;

    /**
     * 配置
     */
    private RssManageConfigDTO config;
}
