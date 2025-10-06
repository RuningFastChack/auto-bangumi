package auto.bangumi.qBittorrent.model.Response;

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
    private Long addedOn;

    /**
     * 剩余下载的数据量（字节）
     */
    private Long amountLeft;

    /**
     * 是否由自动种子管理管理
     */
    private Boolean autoTmm;

    /**
     * 文件片段的可用百分比
     */
    private Double availability;

    /**
     * 种子分类
     */
    private String category;

    /**
     * 已完成传输的数据量（字节）
     */
    private Long completed;

    /**
     * 种子完成时间（Unix 时间戳）
     */
    private Long completionOn;

    /**
     * 种子内容绝对路径（多文件为根路径，单文件为文件路径）
     */
    private String contentPath;

    /**
     * 种子下载速度限制（字节/秒），-1 表示无限制
     */
    private Long dlLimit;

    /**
     * 种子当前下载速度（字节/秒）
     */
    private Long dlSpeed;

    /**
     * 已下载的数据量
     */
    private Long downloaded;

    /**
     * 本次会话已下载的数据量
     */
    private Long downloadedSession;

    /**
     * 剩余时间（秒）
     */
    private Long eta;

    /**
     * 是否优先下载首尾片段
     */
    private Boolean firstLastPiecePrio;

    /**
     * 是否启用强制启动
     */
    private Boolean forceStart;

    /**
     * 种子哈希
     */
    private String hash;

    /**
     * 是否来自私有 tracker（5.0.0 新增）
     */
    private Boolean isPrivate;

    /**
     * 最后下载/上传块的时间（Unix 时间戳）
     */
    private Long lastActivity;

    /**
     * 种子对应的 Magnet URI
     */
    private String magnetUri;

    /**
     * 最大分享率，达到后停止做种/上传
     */
    private Double maxRatio;

    /**
     * 最大做种时间（秒），达到后停止做种
     */
    private Long maxSeedingTime;

    /**
     * 种子名称
     */
    private String name;

    /**
     * swarm 中的种子数量
     */
    private Long numComplete;

    /**
     * swarm 中的下载者数量
     */
    private Long numIncomplete;

    /**
     * 当前连接的下载者数量
     */
    private Long numLeechs;

    /**
     * 当前连接的种子数量
     */
    private Long numSeeds;

    /**
     * 种子优先级，-1 表示排队禁用或种子模式
     */
    private Integer priority;

    /**
     * 种子进度（百分比/100）
     */
    private Double progress;

    /**
     * 种子分享率，最大值 9999
     */
    private Double ratio;

    /**
     * TODO（与 max_ratio 的区别）
     */
    private Double ratioLimit;

    /**
     * 种子数据存储路径
     */
    private String savePath;

    /**
     * 种子完成后的活跃做种时间（秒）
     */
    private Long seedingTime;

    /**
     * TODO（与 max_seeding_time 的区别，单种子设置）
     */
    private Long seedingTimeLimit;

    /**
     * 种子最后一次完成时间（Unix 时间戳）
     */
    private Long seenComplete;

    /**
     * 是否启用顺序下载
     */
    private Boolean sequentialDownload;

    /**
     * 已选文件总大小（字节）
     */
    private Long size;

    /**
     * 种子状态
     */
    private String state;

    /**
     * 是否启用超级做种
     */
    private Boolean superSeeding;

    /**
     * 种子标签列表（逗号分隔）
     */
    private String tags;

    /**
     * 种子总活跃时间（秒）
     */
    private Long timeActive;

    /**
     * 种子所有文件总大小（包括未选择的）
     */
    private Long totalSize;

    /**
     * 第一个工作状态正常的 tracker，若无返回空
     */
    private String tracker;

    /**
     * 种子上传速度限制（字节/秒），-1 表示无限制
     */
    private Long upLimit;

    /**
     * 已上传的数据量
     */
    private Long uploaded;

    /**
     * 本次会话已上传的数据量
     */
    private Long uploadedSession;

    /**
     * 种子当前上传速度（字节/秒）
     */
    private Long upSpeed;

    public static TorrentsInfoListResponse copy(TorrentsInfoRawResponse raw) {
        if (raw == null) {
            return null;
        }

        return TorrentsInfoListResponse.builder()
                .addedOn(raw.getAdded_on())
                .amountLeft(raw.getAmount_left())
                .autoTmm(raw.getAuto_tmm())
                .availability(raw.getAvailability())
                .category(raw.getCategory())
                .completed(raw.getCompleted())
                .completionOn(raw.getCompletion_on())
                .contentPath(raw.getContent_path())
                .dlLimit(raw.getDl_limit())
                .dlSpeed(raw.getDlspeed())
                .downloaded(raw.getDownloaded())
                .downloadedSession(raw.getDownloaded_session())
                .eta(raw.getEta())
                .firstLastPiecePrio(raw.getF_l_piece_prio())
                .forceStart(raw.getForce_start())
                .hash(raw.getHash())
                .isPrivate(raw.getIsPrivate())
                .lastActivity(raw.getLast_activity())
                .magnetUri(raw.getMagnet_uri())
                .maxRatio(raw.getMax_ratio())
                .maxSeedingTime(raw.getMax_seeding_time())
                .name(raw.getName())
                .numComplete(raw.getNum_complete())
                .numIncomplete(raw.getNum_incomplete())
                .numLeechs(raw.getNum_leechs())
                .numSeeds(raw.getNum_seeds())
                .priority(raw.getPriority())
                .progress(raw.getProgress())
                .ratio(raw.getRatio())
                .ratioLimit(raw.getRatio_limit())
                .savePath(raw.getSave_path())
                .seedingTime(raw.getSeeding_time())
                .seedingTimeLimit(raw.getSeeding_time_limit())
                .seenComplete(raw.getSeen_complete())
                .sequentialDownload(raw.getSeq_dl())
                .size(raw.getSize())
                .state(raw.getState())
                .superSeeding(raw.getSuper_seeding())
                .tags(raw.getTags())
                .timeActive(raw.getTime_active())
                .totalSize(raw.getTotal_size())
                .tracker(raw.getTracker())
                .upLimit(raw.getUp_limit())
                .uploaded(raw.getUploaded())
                .uploadedSession(raw.getUploaded_session())
                .upSpeed(raw.getUpspeed())
                .build();
    }
}
