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

    private static final String API_URL = "https://api.deepseek.com/chat/completions";

    private static final String DEFAULT_MODEL = "deepseek-v4-flash";

    private static final int TIMEOUT_MS = 60000;

    /**
     * System prompt 作为系统角色固定指令
     */
    private static String buildSystemPrompt() {
        return """
                你是一个动漫种子标题解析专家。你的任务是从番剧标题中提取结构化信息。
                必须严格遵守以下规则：
                
                1. 仅返回合法的 JSON 对象，不要包含任何其他文字、代码块标记或解释
                2. 字段说明：
                   - episode: 集数（字符串），例如 "05"、"1"、"12.5"，未识别到则返回 "0"
                   - season: 季度（整数），未识别到则返回 1
                   - nameEn: 英文标题，未识别到则返回空字符串
                   - nameJp: 日文标题，未识别到则返回空字符串
                   - nameZh: 中文标题，未识别到则返回空字符串
                   - sub: 字幕类型（简/繁/日等），未识别到则返回空字符串
                   - dpi: 分辨率，例如 "1080"、"720"、"2160"，未识别到则返回空字符串
                   - source: 片源，例如 "Web"、"Baha"、"Bilibili"、"AT-X"，未识别到则返回空字符串
                   - group: 字幕组/发布组名称，未识别到则返回空字符串
                
                3. 如果标题包含明确的季数信息（如 S1、第二季、Season 2 等）请正确提取，否则默认为 1
                4. 集数可能是整数（如 1、05）或小数（如 12.5），请原样返回
                """;
    }

    private static String buildUserPrompt(String title) {
        return "请解析以下番剧标题：\n" + title;
    }

    private static String buildRequestBody(String model, String userPrompt) {
        JSONObject body = new JSONObject();
        body.put("model", model);

        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", buildSystemPrompt());

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);

        body.put("messages", new JSONObject[]{systemMsg, userMsg});

        JSONObject thinking = new JSONObject();
        thinking.put("type", "disabled");
        body.put("thinking", thinking);

        // 强制 JSON 输出格式
        body.put("response_format", JSON.parseObject("{\"type\":\"json_object\"}"));
        body.put("stream", false);
        return body.toJSONString();
    }

    private static String callApi(String apiKey, String requestBody) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(API_URL).toURL().openConnection();
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

        String model = StringUtils.defaultIfBlank(setting.getModel(), DEFAULT_MODEL);

        try {
            String userPrompt = buildUserPrompt(rawTitle);
            String requestBody = buildRequestBody(model, userPrompt);
            String responseJson = callApi(setting.getApiKey(), requestBody);
            Episode result = parseResponse(responseJson, rawTitle);

            if (result != null && StringUtils.isNotBlank(result.getEpisode())) {
                log.debug("DeepSeek 解析标题成功: {} -> episode={}, season={}", rawTitle, result.getEpisode(), result.getSeason());
                return result;
            }
        } catch (Exception e) {
            log.warn("DeepSeek 解析标题失败: {}, 原因: {}", rawTitle, e.getMessage());
        }

        return null;
    }
}
