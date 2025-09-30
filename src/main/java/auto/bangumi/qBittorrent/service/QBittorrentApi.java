package auto.bangumi.qBittorrent.service;

import auto.bangumi.admin.model.UserConfig;
import auto.bangumi.common.constant.AutoBangumiConstant;
import auto.bangumi.common.model.TorrentInfo;
import auto.bangumi.common.utils.ConfigCatch;
import auto.bangumi.common.utils.HttpClientUtil;
import auto.bangumi.qBittorrent.constant.QBittorrentPathConstant;
import auto.bangumi.rss.model.DTO.RssItem.RssItemDTO;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.springframework.stereotype.Component;

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
    private static String SID = "";
    private static String URL = "";
    private static Date EXPIRE_TIME = null;
    // 有效时长
    private static final long EXPIRE_DURATION = 30 * 60 * 1000;

    static {
        UserConfig.DownLoadSetting downLoadSetting = ConfigCatch.findConfig().getDownLoadSetting();
        URL = downLoadSetting.getUrl();
        LoginQBittorrent(downLoadSetting.getUsername(), downLoadSetting.getPassword());
    }

    /**
     * 登录账号
     */
    private static void LoginQBittorrent(String username, String password) {
        if (SID == null || SID.isEmpty() || EXPIRE_TIME == null || new Date().after(EXPIRE_TIME)) {
            HttpResponse httpResponse = HttpClientUtil.sendFormPost(URL, QBittorrentPathConstant.LOGIN,
                    Map.of("Referer", StrUtil.format("{}{}", URL, QBittorrentPathConstant.LOGIN)),
                    new HashMap<>(),
                    Map.of("username", username, "password", password));
            if (httpResponse != null) {
                int status = httpResponse.getStatusLine().getStatusCode();
                if (status == 200) {
                    for (Header header : httpResponse.getAllHeaders()) {
                        for (HeaderElement element : header.getElements()) {
                            boolean HAS_SID = element.getName().equals("SID");
                            if (HAS_SID) {
                                SID = element.getValue();
                            }
                        }
                    }
                    // 登录成功后刷新过期时间
                    EXPIRE_TIME = new Date(System.currentTimeMillis() + EXPIRE_DURATION);
                } else {
                    log.error("QBittorrent登录失败，状态码：{}", status);
                }
            }
        }
    }

    /**
     * 创建分组
     */
    public static void CreateCategory() {
        if (StringUtils.isBlank(SID)) {
            log.error("QBittorrent登录失败");
            return;
        }
        HttpClientUtil.sendFormPost(
                URL,
                QBittorrentPathConstant.OPERATE_TORRENTS_CREATE_CATEGORY,
                Map.of("Cookie", StrUtil.format("SID={}", SID)),
                new HashMap<>(),
                Map.of("category", AutoBangumiConstant.TORRENT_CATEGORY));
    }

    /**
     * 推送种子
     *
     * @param itemDTO 需要推送的数据
     * @return 推送结果
     */
    public static Boolean CreateTorrents(RssItemDTO itemDTO) {
        if (StringUtils.isBlank(SID)) {
            log.error("QBittorrent登录失败");
            return false;
        }

        UserConfig.DownLoadSetting downLoadSetting = ConfigCatch.findConfig().getDownLoadSetting();

        try {
            HttpResponse httpResponse = HttpClientUtil.sendFormPost(URL,
                    QBittorrentPathConstant.OPERATE_TORRENTS_ADD,
                    Map.of("Cookie", StrUtil.format("SID={}", SID)),
                    new HashMap<>(),
                    Map.of(
                            "urls", itemDTO.getUrl(),
                            "savepath", StrUtil.format("{}{}",
                                    StringUtils.isNotBlank(downLoadSetting.getSavePath()) ? downLoadSetting.getSavePath() : "",
                                    itemDTO.getSavePath()),
                            "category", AutoBangumiConstant.TORRENT_CATEGORY,
                            "rename", itemDTO.getTorrentName(),
                            "seedingTimeLimit", AutoBangumiConstant.SENDING_TIME_LIMIT
                    ));
            if (httpResponse != null) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    log.info("Torrent 推送成功 {}", itemDTO.getTorrentName());
                    return true;
                } else if (statusCode == 405) {
                    log.error("Torrent 推送失败 {} {}", itemDTO.getTorrentName(), "Torrent 文件无效");
                } else {
                    log.error("Torrent 推送失败 状态码：{} {} {}", statusCode, itemDTO.getTorrentName(), "What is this？");
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
        if (StringUtils.isBlank(SID)) {
            log.error("QBittorrent登录失败");
            return;
        }

        List<TorrentInfo> torrentInfos = FindTorrentList(Collections.singletonList(itemDTO.getTorrentCode()));
        if (torrentInfos.isEmpty()) {
            log.error("文件重命名失败 {} {}", itemDTO.getTorrentName(), "无效TorrentCode");
            return;
        }

        TorrentInfo torrentInfo = torrentInfos.get(0);

        String absPath = torrentInfo.getContent_path();
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
            HttpResponse httpResponse = HttpClientUtil.sendFormPost(URL,
                    QBittorrentPathConstant.OPERATE_TORRENTS_RENAME_FILE,
                    Map.of("Cookie", StrUtil.format("SID={}", SID)),
                    new HashMap<>(),
                    body);
            if (httpResponse != null) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    log.info("文件重命名成功。{} >> {}", itemDTO.getTorrentName(), itemDTO.getName());
                } else if (statusCode == 409) {
                    log.error("文件重命名失败，番剧：{}，原因：{}", itemDTO.getTorrentName(), "无效newPath或oldPath，或newPath已被使用");
                } else if (statusCode == 400) {
                    log.error("文件重命名失败，番剧：{}，原因：{}", itemDTO.getTorrentName(), "缺少newPath参数");
                } else {
                    log.error("文件重命名失败，状态码：{}，番剧：{}，原因：{}", statusCode, itemDTO.getTorrentName(), "What is this？");
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
        return Optional.of(FindTorrentList(torrentCodes).stream().filter(item -> isDownloadCompleted(item.getState())).map(TorrentInfo::getHash)
                .toList()).orElse(new ArrayList<>());
    }

    /**
     * 查询种子
     *
     * @param torrentCodes
     * @return 列表信息
     */
    public static List<TorrentInfo> FindTorrentList(List<String> torrentCodes) {
        if (StringUtils.isBlank(SID)) {
            log.error("QBittorrent登录失败");
            return Collections.emptyList();
        }
        String hashes = String.join("|", torrentCodes);
        String sendGet = HttpClientUtil.sendGet(
                StrUtil.format("{}{}?hashes={}", URL, QBittorrentPathConstant.GET_TORRENTS_LIST, hashes),
                new HashMap<>(),
                Map.of("Cookie", StrUtil.format("SID={}", SID))
        );

        if (StringUtils.isBlank(sendGet)) {
            return Collections.emptyList();
        }

        return Optional.ofNullable(JSON.parseArray(sendGet, TorrentInfo.class)).orElse(new ArrayList<>());
    }

    /**
     * 删除种子
     *
     * @param torrentCodes
     * @return
     */
    public static Boolean RemoveTorrents(List<String> torrentCodes) {
        if (StringUtils.isBlank(SID)) {
            log.error("QBittorrent登录失败");
            return false;
        }
        if (Objects.isNull(torrentCodes) || torrentCodes.isEmpty()) {
            return false;
        }
        String hashes = String.join("|", torrentCodes);
        HttpResponse httpResponse = HttpClientUtil.sendFormPost(
                URL,
                QBittorrentPathConstant.OPERATE_TORRENTS_DELETE,
                Map.of("Cookie", StrUtil.format("SID={}", SID)),
                new HashMap<>(),
                Map.of("hashes", hashes, "deleteFiles", "false")
        );
        if (httpResponse != null) {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                return true;
            }
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
