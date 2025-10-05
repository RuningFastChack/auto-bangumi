package auto.bangumi.qBittorrent.model.Request;

import auto.bangumi.common.valid.Add;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 添加种子
 *
 * @author 查查
 * @since 2025/10/4
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TorrentsInfoAddRequest {

    /**
     * 多行 URL 列表（每行一个 URL），用于通过 URL 下载 .torrent 文件。
     * 对应 API 参数名："urls"
     */
    @NotNull(message = "不能为空", groups = Add.class)
    private List<String> urls;


    /**
     * 保存路径（下载到本地的目标目录）。
     * 对应 API 参数名："savepath"
     */
    private String savePath;


    /**
     * 请求下载 .torrent 时使用的 Cookie（如果服务器需要认证）。
     * 对应 API 参数名："cookie"
     */
    private String cookie;


    /**
     * 种子分类（例如："movies"、"tv" 等）。
     * 对应 API 参数名："category"
     */
    private String category;


    /**
     * 标签，多个标签以逗号分隔（例如："tag1,tag2"）。
     * 对应 API 参数名："tags"
     */
    private List<String> tags;


    /**
     * 是否跳过哈希校验。可选值："true" 或 "false"（默认 false）。
     * 对应 API 参数名："skip_checking"
     */
    private Boolean skipChecking;


    /**
     * 是否以暂停状态添加。可选值："true" 或 "false"（默认 false）。
     * 对应 API 参数名："paused"
     */
    private Boolean paused;


    /**
     * 是否创建根文件夹。可选值："true"、"false" 或 "unset"（默认 unset）。
     * 对应 API 参数名："root_folder"
     */
    private String rootFolder;


    /**
     * 重命名种子（通常用于上传时修改显示名）。
     * 对应 API 参数名："rename"
     */
    private String rename;


    /**
     * 上传速度限制（字节/秒）。对应 API 参数名："upLimit"
     */
    private Double upLimit;


    /**
     * 下载速度限制（字节/秒）。对应 API 参数名："dlLimit"
     */
    private Double dlLimit;

    /**
     * 设置种子下载时间限制，单位为分钟
     */
    private Double seedingTimeLimit;

    /**
     * 分享率限制（自 2.8.1 起支持）。对应 API 参数名："ratioLimit"
     */
    private Double ratioLimit;

    /**
     * 是否应使用自动 Torrent 管理
     */
    private Boolean autoTMM;

    /**
     * 启用顺序下载
     */
    private Boolean sequentialDownload;

    /**
     * 优先下载第一个最后一个片段
     */
    private Boolean firstLastPiecePrio;

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("urls", String.join("\n", this.getUrls()));
        result.put("savepath", this.getSavePath());
        result.put("cookie", this.getCookie());
        result.put("category", this.getCategory());
        result.put("tags", String.join(",", this.getTags()));
        result.put("skip_checking", this.getSkipChecking());
        result.put("paused", this.getPaused());
        result.put("root_folder", this.getRootFolder());
        result.put("rename", this.getRename());
        result.put("upLimit", this.getUpLimit());
        result.put("dlLimit", this.getDlLimit());
        result.put("ratioLimit", this.getRatioLimit());
        result.put("seedingTimeLimit", this.getSeedingTimeLimit());
        result.put("autoTMM", this.getAutoTMM());
        result.put("sequentialDownload", this.getSequentialDownload());
        result.put("firstLastPiecePrio", this.getFirstLastPiecePrio());
        return result;
    }
}
