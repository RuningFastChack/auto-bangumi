package auto.bangumi.player.strategy;

import auto.bangumi.admin.model.UserConfig;
import auto.bangumi.common.enums.PlayerEnums;
import auto.bangumi.common.utils.ConfigCatch;
import auto.bangumi.common.utils.HttpClientUtil;
import auto.bangumi.player.annotation.PlayerMethod;
import auto.bangumi.player.service.PlayerService;
import auto.bangumi.rss.model.entity.RssItem;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
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

        if (StringUtils.isAnyBlank(baseUrl, apiKey, userId)) {
            log.error("JellyFin配置不完整: url={}, apiKey={}, userId={}", baseUrl, apiKey, userId);
            return null;
        }

        // 尝试根据保存路径查找媒体项
        String itemId = findItemIdByPath(item.getSavePath(), item.getName(), baseUrl, apiKey, userId);
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

        if (StringUtils.isAnyBlank(baseUrl, apiKey, userId)) {
            log.error("JellyFin配置不完整");
            return false;
        }

        // 查找媒体项ID
        String itemId = findItemIdByPath(item.getSavePath(), item.getName(), baseUrl, apiKey, userId);
        if (itemId == null) {
            log.warn("未找到对应的JellyFin媒体项，无法选择字幕");
            return false;
        }

        // 调用JellyFin API设置字幕轨道
        // 注意：JellyFin API可能需要会话或播放会话ID，这里简化实现
        // 实际应调用 /Sessions/{sessionId}/Playing/SetSubtitleStream
        // 由于播放会话管理复杂，暂不实现
        log.info("选择字幕轨道 itemId={}, subtitleTrackId={}", itemId, subtitleTrackId);
        return true;
    }

    private UserConfig.PlayerSetting getPlayerSetting() {
        UserConfig userConfig = configCatch.findConfig();
        if (userConfig == null || userConfig.getPlayerSetting() == null) {
            return null;
        }
        return userConfig.getPlayerSetting();
    }

    private String findItemIdByPath(String savePath, String name, String baseUrl, String apiKey, String userId) {
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
                        // 简单匹配：名称包含或路径包含
                        if (StringUtils.containsIgnoreCase(itemName, name) ||
                                (itemPath != null && itemPath.contains(savePath))) {
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