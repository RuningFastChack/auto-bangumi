package auto.bangumi.qBittorrent.model.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * TorrentsInfoListResponse
 *
 * @author sakura
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TorrentsInfoListResponse {

    /**
     * 种子添加到客户端的时间（Unix 时间戳）
     */
    @JsonProperty("added_on")
    private Long addedOn;

    /**
     * 剩余下载的数据量（字节）
     */
    @JsonProperty("amount_left")
    private Long amountLeft;

    /**
     * 是否由自动种子管理管理
     */
    @JsonProperty("auto_tmm")
    private Boolean autoTmm;

    /**
     * 文件片段的可用百分比
     */
    @JsonProperty("availability")
    private Double availability;

    /**
     * 种子分类
     */
    @JsonProperty("category")
    private String category;

    /**
     * 已完成传输的数据量（字节）
     */
    @JsonProperty("completed")
    private Long completed;

    /**
     * 种子完成时间（Unix 时间戳）
     */
    @JsonProperty("completion_on")
    private Long completionOn;

    /**
     * 种子内容绝对路径（多文件为根路径，单文件为文件路径）
     */
    @JsonProperty("content_path")
    private String contentPath;

    /**
     * 种子下载速度限制（字节/秒），-1 表示无限制
     */
    @JsonProperty("dl_limit")
    private Long dlLimit;

    /**
     * 种子当前下载速度（字节/秒）
     */
    @JsonProperty("dlspeed")
    private Long dlSpeed;

    /**
     * 已下载的数据量
     */
    @JsonProperty("downloaded")
    private Long downloaded;

    /**
     * 本次会话已下载的数据量
     */
    @JsonProperty("downloaded_session")
    private Long downloadedSession;

    /**
     * 剩余时间（秒）
     */
    @JsonProperty("eta")
    private Long eta;

    /**
     * 是否优先下载首尾片段
     */
    @JsonProperty("f_l_piece_prio")
    private Boolean firstLastPiecePrio;

    /**
     * 是否启用强制启动
     */
    @JsonProperty("force_start")
    private Boolean forceStart;

    /**
     * 种子哈希
     */
    @JsonProperty("hash")
    private String hash;

    /**
     * 是否来自私有 tracker（5.0.0 新增）
     */
    @JsonProperty("isPrivate")
    private Boolean isPrivate;

    /**
     * 最后下载/上传块的时间（Unix 时间戳）
     */
    @JsonProperty("last_activity")
    private Long lastActivity;

    /**
     * 种子对应的 Magnet URI
     */
    @JsonProperty("magnet_uri")
    private String magnetUri;

    /**
     * 最大分享率，达到后停止做种/上传
     */
    @JsonProperty("max_ratio")
    private Double maxRatio;

    /**
     * 最大做种时间（秒），达到后停止做种
     */
    @JsonProperty("max_seeding_time")
    private Long maxSeedingTime;

    /**
     * 种子名称
     */
    @JsonProperty("name")
    private String name;

    /**
     * swarm 中的种子数量
     */
    @JsonProperty("num_complete")
    private Long numComplete;

    /**
     * swarm 中的下载者数量
     */
    @JsonProperty("num_incomplete")
    private Long numIncomplete;

    /**
     * 当前连接的下载者数量
     */
    @JsonProperty("num_leechs")
    private Long numLeechs;

    /**
     * 当前连接的种子数量
     */
    @JsonProperty("num_seeds")
    private Long numSeeds;

    /**
     * 种子优先级，-1 表示排队禁用或种子模式
     */
    @JsonProperty("priority")
    private Integer priority;

    /**
     * 种子进度（百分比/100）
     */
    @JsonProperty("progress")
    private Double progress;

    /**
     * 种子分享率，最大值 9999
     */
    @JsonProperty("ratio")
    private Double ratio;

    /**
     * TODO（与 max_ratio 的区别）
     */
    @JsonProperty("ratio_limit")
    private Double ratioLimit;

    /**
     * 种子数据存储路径
     */
    @JsonProperty("save_path")
    private String savePath;

    /**
     * 种子完成后的活跃做种时间（秒）
     */
    @JsonProperty("seeding_time")
    private Long seedingTime;

    /**
     * TODO（与 max_seeding_time 的区别，单种子设置）
     */
    @JsonProperty("seeding_time_limit")
    private Long seedingTimeLimit;

    /**
     * 种子最后一次完成时间（Unix 时间戳）
     */
    @JsonProperty("seen_complete")
    private Long seenComplete;

    /**
     * 是否启用顺序下载
     */
    @JsonProperty("seq_dl")
    private Boolean sequentialDownload;

    /**
     * 已选文件总大小（字节）
     */
    @JsonProperty("size")
    private Long size;

    /**
     * 种子状态
     */
    @JsonProperty("state")
    private String state;

    /**
     * 是否启用超级做种
     */
    @JsonProperty("super_seeding")
    private Boolean superSeeding;

    /**
     * 种子标签列表（逗号分隔）
     */
    @JsonProperty("tags")
    private String tags;

    /**
     * 种子总活跃时间（秒）
     */
    @JsonProperty("time_active")
    private Long timeActive;

    /**
     * 种子所有文件总大小（包括未选择的）
     */
    @JsonProperty("total_size")
    private Long totalSize;

    /**
     * 第一个工作状态正常的 tracker，若无返回空
     */
    @JsonProperty("tracker")
    private String tracker;

    /**
     * 种子上传速度限制（字节/秒），-1 表示无限制
     */
    @JsonProperty("up_limit")
    private Long upLimit;

    /**
     * 已上传的数据量
     */
    @JsonProperty("uploaded")
    private Long uploaded;

    /**
     * 本次会话已上传的数据量
     */
    @JsonProperty("uploaded_session")
    private Long uploadedSession;

    /**
     * 种子当前上传速度（字节/秒）
     */
    @JsonProperty("upspeed")
    private Long upSpeed;
}
