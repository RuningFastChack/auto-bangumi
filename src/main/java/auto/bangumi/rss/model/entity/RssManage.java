package auto.bangumi.rss.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "rss_manage")
public class RssManage {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 动画标题
     */
    @TableField(value = "official_title")
    private String officialTitle;

    /**
     * 动画标题
     */
    @TableField(value = "official_title_en")
    private String officialTitleEn;

    /**
     * 动画标题
     */
    @TableField(value = "official_title_jp")
    private String officialTitleJp;

    /**
     * 季度
     */
    @TableField(value = "season")
    private String season;

    /**
     * 是否启用
     */
    @TableField(value = "status")
    private String status;

    /**
     * 下载规则
     */
    @TableField(value = "filter")
    private String filter;

    /**
     * 图片路径
     */
    @TableField(value = "poster_link")
    private String posterLink;

    /**
     * 保存路径
     */
    @TableField(value = "save_path")
    private String savePath;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Boolean deleted;

    /**
     * 是否完结 0 否 1 是
     */
    @TableField(value = "complete")
    private String complete;

    /**
     * 更新星期 1 2 3 4 5 6 7
     */
    @TableField(value = "update_week")
    private Integer updateWeek;

    /**
     * 发布日期 yyyy-MM-dd
     */
    @TableField(value = "send_date")
    private String sendDate;

    /**
     * 改动画的订阅合集
     */
    @TableField(value = "rss_list")
    private String rssList;

    /**
     * 配置
     */
    @TableField(value = "config")
    private String config;
}