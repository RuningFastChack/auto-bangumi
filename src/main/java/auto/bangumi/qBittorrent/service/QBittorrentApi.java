package auto.bangumi.qBittorrent.service;

import auto.bangumi.admin.model.UserConfig;
import auto.bangumi.common.constant.AutoBangumiConstant;
import auto.bangumi.common.utils.ConfigCatch;
import auto.bangumi.qBittorrent.constant.QBittorrentPathConstant;
import auto.bangumi.qBittorrent.model.Response.TorrentsInfoListResponse;
import auto.bangumi.qBittorrent.utils.QBHttpUtil;
import auto.bangumi.rss.model.DTO.RssItem.RssItemDTO;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * QBittorrentApi
 * 不要指望我做适配
 * @author 查查
 * @since 2025/9/9
 */
@Slf4j
@Component
public abstract class QBittorrentApi {
    /**
     * 创建分组
     */
    public static void CreateCategory() {
        try {
            QBHttpUtil.sendJSONPost(QBittorrentPathConstant.CATEGORIES_ADD, new HashMap<>(),
                    Map.of("category", AutoBangumiConstant.TORRENT_CATEGORY));
        } catch (Exception e) {
            log.error("初始化QB失败，创建分组异常。{}", e.getMessage());
        }
    }

    /**
     * 推送种子
     *
     * @param itemDTO 需要推送的数据
     * @return 推送结果
     */
    public static Boolean CreateTorrents(RssItemDTO itemDTO) {
        try {
            UserConfig.DownLoadSetting downLoadSetting = ConfigCatch.findConfig().getDownLoadSetting();
            HttpResponse response = QBHttpUtil.sendJSONPost(QBittorrentPathConstant.TORRENTS_ADD, new HashMap<>(), Map.of(
                    "urls", itemDTO.getUrl(),
                    "savepath", StrUtil.format("{}{}",
                            StringUtils.isNotBlank(downLoadSetting.getSavePath()) ? downLoadSetting.getSavePath() : "",
                            itemDTO.getSavePath()),
                    "category", AutoBangumiConstant.TORRENT_CATEGORY,
                    "rename", itemDTO.getTorrentName(),
                    "seedingTimeLimit", AutoBangumiConstant.SENDING_TIME_LIMIT
            ));
            if (response != null) {

                String message = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    log.info("Torrent 推送成功：{}", itemDTO.getTorrentName());
                    return true;
                } else if (statusCode == 405) {
                    log.error("Torrent 推送失败：{}，{}，{}", itemDTO.getTorrentName(), "Torrent 文件无效", message);
                } else {
                    log.error("Torrent 推送失败 状态码：{}，{}，{}", statusCode, itemDTO.getTorrentName(), message);
                }
            }
        } catch (Exception e) {
            log.error("推送下载失败，{}", e.getMessage(), e);
        }
        return false;
    }

    /**
     * 修改文件名称
     *
     * @param itemDTO
     * @return
     */
    public static void RenameFile(RssItemDTO itemDTO) {
        List<TorrentsInfoListResponse> torrentInfos = FindTorrentList(Collections.singletonList(itemDTO.getTorrentCode()));
        if (torrentInfos.isEmpty()) {
            log.error("文件重命名失败 {} {}", itemDTO.getTorrentName(), "无效TorrentCode");
            return;
        }

        TorrentsInfoListResponse torrentInfo = torrentInfos.get(0);

        String absPath = torrentInfo.getContentPath();
        if (StringUtils.isBlank(absPath)) {
            log.error("文件重命名失败 {} {}", itemDTO.getTorrentName(), "Content path为空");
            return;
        }

        int dotIndex = absPath.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == absPath.length() - 1) {
            log.error("文件重命名失败 {} {}", itemDTO.getTorrentName(), "文件没有后缀");
            return;
        }
        String oldPath = absPath.substring(absPath.lastIndexOf('/') + 1, dotIndex);
        String ext = absPath.substring(dotIndex);
        Map<String, Object> body = Map.of(
                "hash", itemDTO.getTorrentCode(),
                "oldPath", StrUtil.format("{}{}", oldPath, ext),
                "newPath", StrUtil.format("{}{}", itemDTO.getName(), ext));

        try {
            HttpResponse response = QBHttpUtil.sendJSONPost(QBittorrentPathConstant.FILE_RENAME, new HashMap<>(), body);
            if (response != null) {
                String message = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    log.info("文件重命名成功。{} >> {}", itemDTO.getTorrentName(), itemDTO.getName());
                } else if (statusCode == 409) {
                    log.error("文件重命名失败，番剧：{}，原因：{}", itemDTO.getTorrentName(), "无效newPath或oldPath，或newPath已被使用");
                } else if (statusCode == 400) {
                    log.error("文件重命名失败，番剧：{}，原因：{}", itemDTO.getTorrentName(), "缺少newPath参数");
                } else {
                    log.error("文件重命名失败，状态码：{}，番剧：{}，原因：{}", statusCode, itemDTO.getTorrentName(), message);
                }
            }
        } catch (Exception e) {
            log.error("文件重命名失败，{}", e.getMessage(), e);
        }
    }

    /**
     * 检查种子状态
     *
     * @param torrentCodes
     * @return 返回下载成功的torrent hash
     */
    public static List<String> CheckTorrentState(List<String> torrentCodes) {
        return Optional.of(FindTorrentList(torrentCodes).stream().filter(item -> isDownloadCompleted(item.getState())).map(TorrentsInfoListResponse::getHash)
                .toList()).orElse(new ArrayList<>());
    }

    /**
     * 查询种子
     *
     * @param torrentCodes
     * @return 列表信息
     */
    public static List<TorrentsInfoListResponse> FindTorrentList(List<String> torrentCodes) {
        String sendGet = QBHttpUtil.sendGet(QBittorrentPathConstant.TORRENTS_LIST, Map.of("hashes", String.join("|", torrentCodes)));
        if (StringUtils.isBlank(sendGet)) {
            return Collections.emptyList();
        }
        return Optional.ofNullable(JSON.parseArray(sendGet, TorrentsInfoListResponse.class)).orElse(new ArrayList<>());
    }

    /**
     * 删除种子
     *
     * @param torrentCodes
     * @return
     */
    public static Boolean RemoveTorrents(List<String> torrentCodes) {
        if (Objects.isNull(torrentCodes) || torrentCodes.isEmpty()) {
            return false;
        }
        String hashes = String.join("|", torrentCodes);
        HttpResponse response = QBHttpUtil.sendJSONPost(
                QBittorrentPathConstant.TORRENTS_DELETE,
                new HashMap<>(),
                Map.of("hashes", hashes, "deleteFiles", "false")
        );

        if (response != null) {
            int statusCode = response.getStatusLine().getStatusCode();
            return statusCode == 200;
        }
        return false;
    }

    /**
     * 判断是否下载完成l
     *
     * @param state
     * @return
     */
    private static boolean isDownloadCompleted(String state) {
        return switch (state) {
            case "uploading", "pausedUP", "queuedUP", "stalledUP", "checkingUP", "forcedUP" -> true;
            default -> false;
        };
    }
}
