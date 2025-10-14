package auto.bangumi.rss.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "rss_item")
public class RssItem {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * torrent的哈希值
     */
    @TableField(value = "torrent_code")
    private String torrentCode;

    /**
     * 剧集数
     */
    @TableField(value = "episode_num")
    private String episodeNum;

    /**
     * RssManage的ID
     */
    @TableField(value = "rss_manage_id")
    private Integer rssManageId;

    /**
     * 字幕组
     */
    @TableField(value = "translation_group")
    private String translationGroup;

    /**
     * 保存路径
     */
    @TableField(value = "save_path")
    private String savePath;

    /**
     * 番剧 - 字幕组ID？
     */
    @TableField(value = "sub_group_id")
    private String subGroupId;

    /**
     * torrent的原始标题
     */
    @TableField(value = "torrent_name")
    private String torrentName;

    /**
     * 剧集名称 xxx S00E00
     */
    @TableField(value = "name")
    private String name;

    /**
     * 磁力链
     */
    @TableField(value = "url")
    private String url;

    /**
     * 解析出来的Link
     */
    @TableField(value = "homepage")
    private String homepage;

    /**
     * 是否下载完成
     */
    @TableField(value = "downloaded")
    private String downloaded;

    /**
     * 是否推送
     */
    @TableField(value = "pushed")
    private String pushed;

    /**
     * 是否启用
     */
    @TableField(value = "status")
    private String status;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Boolean deleted;
}