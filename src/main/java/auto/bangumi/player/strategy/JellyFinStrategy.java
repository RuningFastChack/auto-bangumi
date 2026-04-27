package auto.bangumi.player.strategy;

import auto.bangumi.admin.model.UserConfig;
import auto.bangumi.common.enums.PlayerEnums;
import auto.bangumi.common.utils.ConfigCatch;
import auto.bangumi.common.utils.HttpClientUtil;
import auto.bangumi.player.annotation.PlayerMethod;
import auto.bangumi.player.service.PlayerService;
import auto.bangumi.rss.model.entity.RssItem;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * JellyFin播放器策略
 *
 * @author sakura
 * @version 1.0
 * @since 2026/03/03
 */
@Slf4j
@Component
@PlayerMethod(method = PlayerEnums.JellyFin)
public class JellyFinStrategy implements PlayerService {

    private final ConfigCatch configCatch;

    public JellyFinStrategy(ConfigCatch configCatch) {
        this.configCatch = configCatch;
    }

    @Override
    public String playVideo(RssItem item) {
        UserConfig.PlayerSetting playerSetting = getPlayerSetting();
        if (playerSetting == null) {
            log.error("JellyFin播放器配置缺失");
            return null;
        }

        String baseUrl = playerSetting.getUrl();
        String apiKey = playerSetting.getAppKey();
        String userId = playerSetting.getUserId();
        String basePath = playerSetting.getBasePath();

        if (StringUtils.isAnyBlank(baseUrl, apiKey, userId)) {
            log.error("JellyFin配置不完整: url={}, apiKey={}, userId={}", baseUrl, apiKey, userId);
            return null;
        }

        // 尝试根据保存路径查找媒体项
        String itemId = findItemIdByPath(item.getSavePath(), item.getName(), baseUrl, apiKey, userId, basePath);
        if (itemId == null) {
            log.warn("未找到对应的JellyFin媒体项: savePath={}, name={}", item.getSavePath(), item.getName());
            // 返回基础的JellyFin URL
            return baseUrl + "/web/index.html";
        }

        // 构造播放URL
        String playUrl = StrUtil.format("{}/web/index.html#!/details?id={}", baseUrl, itemId);
        log.info("生成JellyFin播放URL: {}", playUrl);
        return playUrl;
    }

    @Override
    public boolean selectSubtitle(RssItem item, String subtitleTrackId) {
        UserConfig.PlayerSetting playerSetting = getPlayerSetting();
        if (playerSetting == null) {
            log.error("JellyFin播放器配置缺失");
            return false;
        }

        String baseUrl = playerSetting.getUrl();
        String apiKey = playerSetting.getAppKey();
        String userId = playerSetting.getUserId();
        String basePath = playerSetting.getBasePath();

        if (StringUtils.isAnyBlank(baseUrl, apiKey, userId)) {
            log.error("JellyFin配置不完整");
            return false;
        }

        // 查找媒体项ID
        String itemId = findItemIdByPath(item.getSavePath(), item.getName(), baseUrl, apiKey, userId, basePath);
        if (itemId == null) {
            log.warn("未找到对应的JellyFin媒体项，无法选择字幕");
            return false;
        }

        // 获取当前用户的活跃会话ID
        String sessionId = getActiveSessionId(baseUrl, apiKey, userId);
        if (sessionId == null) {
            log.warn("未找到当前用户的活跃播放会话，无法切换字幕");
            return false;
        }

        // 调用JellyFin API设置字幕轨道
        try {
            String path = StrUtil.format("/Sessions/{}/Playing/SetSubtitleStream", sessionId);
            Map<String, Object> headers = new HashMap<>();
            headers.put("X-Emby-Token", apiKey);
            Map<String, Object> query = new HashMap<>();
            query.put("subtitleStreamIndex", subtitleTrackId);
            // 发送表单POST请求
            HttpResponse response = HttpClientUtil.sendFormPost(baseUrl, path, headers, query, null);
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                log.info("字幕轨道设置成功: itemId={}, subtitleTrackId={}", itemId, subtitleTrackId);
                return true;
            } else {
                log.error("字幕轨道设置API返回失败响应");
                return false;
            }
        } catch (Exception e) {
            log.error("字幕轨道设置API调用异常", e);
            return false;
        }
    }

    private String getActiveSessionId(String baseUrl, String apiKey, String userId) {
        try {
            String apiUrl = StrUtil.format("{}/Sessions", baseUrl);
            Map<String, Object> headers = new HashMap<>();
            headers.put("X-Emby-Token", apiKey);
            String response = HttpClientUtil.sendGet(apiUrl, null, headers);
            if (StringUtils.isNotBlank(response)) {
                JSONArray sessions = JSON.parseArray(response);
                for (int i = 0; i < sessions.size(); i++) {
                    JSONObject session = sessions.getJSONObject(i);
                    String sessionUserId = session.getString("UserId");
                    if (userId.equals(sessionUserId)) {
                        // 检查是否正在播放
                        JSONObject nowPlayingItem = session.getJSONObject("NowPlayingItem");
                        if (nowPlayingItem != null) {
                            return session.getString("Id");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取活跃会话ID异常", e);
        }
        return null;
    }

    private UserConfig.PlayerSetting getPlayerSetting() {
        UserConfig userConfig = configCatch.findConfig();
        if (userConfig == null || userConfig.getPlayerSetting() == null) {
            return null;
        }
        return userConfig.getPlayerSetting();
    }

    private String findItemIdByPath(String savePath, String name, String baseUrl, String apiKey, String userId, String basePath) {
        // 调用JellyFin API搜索媒体项
        // 使用 /Users/{UserId}/Items 端点，支持递归搜索和路径过滤
        String apiUrl = StrUtil.format("{}/Users/{}/Items", baseUrl, userId);
        Map<String, Object> params = new HashMap<>();
        params.put("recursive", true);
        params.put("includeItemTypes", "Episode");
        params.put("searchTerm", name); // 使用名称搜索
        // 可以添加路径过滤，但JellyFin API可能不支持直接路径匹配

        Map<String, Object> headers = new HashMap<>();
        headers.put("X-Emby-Token", apiKey);

        // 构建绝对路径用于匹配
        String absolutePath = savePath;
        if (StringUtils.isNotBlank(basePath)) {
            // 简单拼接，确保路径分隔符正确
            if (basePath.endsWith("/") && savePath.startsWith("/")) {
                absolutePath = basePath + savePath.substring(1);
            } else if (!basePath.endsWith("/") && !savePath.startsWith("/")) {
                absolutePath = basePath + "/" + savePath;
            } else {
                absolutePath = basePath + savePath;
            }
        }

        try {
            String response = HttpClientUtil.sendGet(apiUrl, params, headers);
            if (StringUtils.isNotBlank(response)) {
                JSONObject jsonObject = JSON.parseObject(response);
                JSONArray items = jsonObject.getJSONArray("Items");
                if (items != null && !items.isEmpty()) {
                    // 遍历匹配路径或名称
                    for (int i = 0; i < items.size(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        String itemName = item.getString("Name");
                        String itemPath = item.getString("Path");
                        // 简单匹配：名称包含或路径包含绝对路径
                        if (StringUtils.containsIgnoreCase(itemName, name) ||
                                (itemPath != null && itemPath.contains(absolutePath))) {
                            return item.getString("Id");
                        }
                    }
                    // 如果没有匹配，返回第一个项目的ID
                    JSONObject first = items.getJSONObject(0);
                    log.warn("未找到精确匹配的媒体项，返回第一个项目: {}", first.getString("Name"));
                    return first.getString("Id");
                }
            } else {
                log.error("JellyFin搜索API返回空响应");
            }
        } catch (Exception e) {
            log.error("JellyFin搜索API调用异常", e);
        }
        return null;
    }
}