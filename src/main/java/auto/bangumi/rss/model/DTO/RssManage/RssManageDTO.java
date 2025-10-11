package auto.bangumi.rss.model.DTO.RssManage;

import auto.bangumi.common.valid.Add;
import auto.bangumi.common.valid.Update;
import auto.bangumi.rss.model.Rss;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

/**
 * RssManageDTO
 *
 * @author 查查
 * @since 2025/9/11
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RssManageDTO {

    /**
     * 主键
     */
    @NotNull(message = "不能为空", groups = Update.class)
    private Integer id;

    /**
     * 动画标题
     */
    @NotBlank(message = "不能为空", groups = {Add.class, Update.class})
    private String officialTitle;

    /**
     * 季度
     */
    @NotBlank(message = "不能为空", groups = {Add.class, Update.class})
    private String season;

    /**
     * 是否启用
     */
    @NotBlank(message = "不能为空", groups = {Add.class, Update.class})
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
    @NotBlank(message = "不能为空", groups = {Add.class, Update.class})
    private String complete;

    /**
     * 更新星期 1 2 3 4 5 6 7
     */
    @NotNull(message = "不能为空", groups = {Add.class, Update.class})
    private Integer updateWeek;

    /**
     * 发布日期 yyyy-MM-dd
     */
    @NotBlank(message = "不能为空", groups = {Add.class,Update.class})
    private String sendDate;

    /**
     * 改动画的订阅合集
     */
    @Size(min = 1, message = "至少需要一条订阅", groups = {Add.class, Update.class})
    @NotNull(message = "不能为空", groups = {Add.class, Update.class})
    private List<Rss> rssList;

    private RssManageConfigDTO config;
}
