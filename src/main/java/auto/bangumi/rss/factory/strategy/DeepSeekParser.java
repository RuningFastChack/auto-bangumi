package auto.bangumi.rss.factory.strategy;

import auto.bangumi.admin.model.UserConfig;
import auto.bangumi.common.enums.AiModelEnum;
import auto.bangumi.common.model.parser.Episode;
import auto.bangumi.common.utils.ConfigCatch;
import auto.bangumi.common.utils.SpringContextUtil;
import auto.bangumi.rss.factory.annotation.ParserMethod;
import auto.bangumi.rss.factory.service.TitleParser;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * DeepSeek AI 标题解析器
 *
 * @author sakura
 */
@Slf4j
@Component
@ParserMethod(model = AiModelEnum.DEEPSEEK)
public class DeepSeekParser implements TitleParser {

    private static final String DEFAULT_API_URL = "https://api.deepseek.com/chat/completions";

    private static final String DEFAULT_MODEL = "deepseek-v4-flash";

    private static final int TIMEOUT_MS = 60000;

    /**
     * 根据配置获取 API URL。如果配置了 baseUrl 则拼接 /chat/completions，
     * 否则使用默认的 DeepSeek 云端地址
     */
    private static String getApiUrl(UserConfig.AiParseSetting setting) {
        if (setting != null && StringUtils.isNotBlank(setting.getBaseUrl())) {
            String base = setting.getBaseUrl().replaceAll("/+$", "");
            return base + "/chat/completions";
        }
        return DEFAULT_API_URL;
    }

    /**
     * System prompt 作为系统角色固定指令
     */
    private static String buildSystemPrompt() {
        return """
                你是番剧种子标题解析器。
                
                任务：
                从标题中提取信息并返回 JSON。
                
                严格要求：
                
                1. 只允许返回 JSON
                2. 不允许返回 markdown
                3. 不允许返回解释
                4. 不允许返回注释
                5. 不允许返回代码块
                
                返回格式：
                
                {"episode":"","season":1,"nameEn":"","nameJp":"","nameZh":"","sub":"","dpi":"","source":"","group":""}
                
                规则：
                
                - season 默认 1
                - episode 未识别返回 "0"
                - 保留集数原格式，例如：
                  01
                  11
                  12.5
                
                - dpi 只保留数字：
                  1080P -> 1080
                  720P -> 720
                  2160P -> 2160
                
                - source 常见值：
                  Baha
                  Bilibili
                  Web
                  Netflix
                  Crunchyroll
                  AT-X
                
                - group 为最前面的发布组
                
                - nameEn 为英文标题
                - nameJp 为日文标题
                - nameZh 为中文标题
                
                示例：
                
                输入：
                
                [ANi] 9nine Rulers Crown / 9-nine- 支配者的王冠 - 11 [1080P][Baha][WEB-DL][AAC AVC][CHT][MP4]
                
                输出：
                
                {
                  "episode":"11",
                  "season":1,
                  "nameEn":"9nine Rulers Crown",
                  "nameJp":"",
                  "nameZh":"9-nine- 支配者的王冠",
                  "sub":"CHT",
                  "dpi":"1080",
                  "source":"Baha",
                  "group":"ANi"
                }
                """;
    }

    private static String buildUserPrompt(String title) {
        return "请解析以下番剧标题：\n" + title;
    }

    private static String buildRequestBody(String model, String userPrompt) {
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("stream", false);
        // 结构化抽取推荐
        body.put("temperature", 0);
        JSONObject responseFormat = new JSONObject();
        responseFormat.put("type", "json_object");
        body.put("response_format", responseFormat);
        JSONObject thinking = new JSONObject();
        thinking.put("type", "disabled");
        body.put("thinking", thinking);
        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", buildSystemPrompt());
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        body.put("messages", new Object[]{
                systemMsg,
                userMsg
        });
        return body.toJSONString();
    }

    private static String callApi(String apiUrl, String apiKey, String requestBody) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(apiUrl).toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            String errorBody = new String(conn.getErrorStream() != null
                    ? conn.getErrorStream().readAllBytes()
                    : conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            throw new RuntimeException("DeepSeek API 返回错误码: " + responseCode + ", body: " + errorBody);
        }

        return new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static Episode parseResponse(String responseJson, String rawTitle) {
        try {
            JSONObject root = JSON.parseObject(responseJson);
            String content = root.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            if (StringUtils.isBlank(content)) {
                log.warn("DeepSeek 返回内容为空，title: {}", rawTitle);
                return null;
            }

            JSONObject result = JSON.parseObject(content);

            Episode episode = Episode.builder()
                    .group(result.getString("group"))
                    .name(result.getString("nameZh"))
                    .nameEn(result.getString("nameEn"))
                    .nameJp(result.getString("nameJp"))
                    .season(result.getIntValue("season"))
                    .episode(result.getString("episode"))
                    .sub(result.getString("sub"))
                    .dpi(result.getString("dpi"))
                    .source(result.getString("source"))
                    .build();

            if (episode.getSeason() <= 0) {
                episode.setSeason(1);
            }
            if (StringUtils.isBlank(episode.getEpisode())) {
                episode.setEpisode("0");
            }

            return episode;
        } catch (Exception e) {
            log.warn("DeepSeek 响应解析失败，title: {}, 原因: {}", rawTitle, e.getMessage());
            return null;
        }
    }

    private static UserConfig.AiParseSetting getAiSetting() {
        try {
            ConfigCatch configCatch = SpringContextUtil.getBean(ConfigCatch.class);
            UserConfig config = configCatch.findConfig();
            return Objects.nonNull(config) ? config.getAiParseSetting() : null;
        } catch (Exception e) {
            log.debug("获取 AI 解析配置失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Episode parse(String rawTitle) {
        UserConfig.AiParseSetting setting = getAiSetting();
        if (setting == null || StringUtils.isBlank(setting.getApiKey())) {
            log.warn("DeepSeek 未配置 API Key");
            return null;
        }

        String apiUrl = getApiUrl(setting);
        String model = StringUtils.defaultIfBlank(setting.getModel(), DEFAULT_MODEL);

        try {
            String userPrompt = buildUserPrompt(rawTitle);
            String requestBody = buildRequestBody(model, userPrompt);
            String responseJson = callApi(apiUrl, setting.getApiKey(), requestBody);
            Episode result = parseResponse(responseJson, rawTitle);

            if (result != null && StringUtils.isNotBlank(result.getEpisode())) {
                log.info("DeepSeek 解析标题成功: {} -> episode={}, season={}", rawTitle, result.getEpisode(), result.getSeason());
                return result;
            }
        } catch (Exception e) {
            log.warn("DeepSeek 解析标题失败: {}, 原因: {}", rawTitle, e.getMessage());
        }

        return null;
    }
}
