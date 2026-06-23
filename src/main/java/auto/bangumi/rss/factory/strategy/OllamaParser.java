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
 * Ollama 本地 AI 标题解析器
 *
 * @author sakura
 */
@Slf4j
@Component
@ParserMethod(model = AiModelEnum.OLLAMA)
public class OllamaParser implements TitleParser {

    /**
     * 默认模型名称，使用微调后的 bangumi-parser 模型
     * 该模型已内置 30 个 Few-shot 示例，可直接从番剧标题提取结构化信息
     */
    private static final String DEFAULT_MODEL = "bangumi-parser";

    private static final int TIMEOUT_MS = 120000;

    private static String getApiUrl(UserConfig.AiParseSetting setting) {
        String base = setting.getBaseUrl().replaceAll("/+$", "");
        return base + "/chat/completions";
    }

    /**
     * System prompt 作为系统角色固定指令
     */
    private static String buildSystemPrompt() {
        return """
                你是一个番剧标题结构化解析器。
                
                任务：
                从标题中提取字段并返回 JSON。
                
                严格要求：
                
                1. 只允许输出 JSON
                2. 不允许输出 markdown
                3. 不允许输出解释
                4. 不允许输出思考过程
                5. 不允许输出代码块
                
                返回格式：
                
                {"episode":"","season":1,"nameEn":"","nameJp":"","nameZh":"","sub":"","dpi":"","source":"","group":""}
                
                规则：
                
                - episode 剧集编号, 默认为1, 识别不了返回NaN
                - season 季数, 默认为1, 识别不了返回NaN
                - nameEn 英文名称, 若标题中没有英文名称, 则默认为空
                - nameJp 日文名称, 若标题中没有日文名称, 则默认为空
                - nameZh 中文名称, 若标题中没有中文名称, 则默认为空
                - sub 字幕, 识别：Pattern.compile("[简繁日字幕]|CH|BIG5|GB")
                - dpi 分辨率, 仅返回数字, 若标题中没有分辨率, 则默认为空 Pattern.compile("1080|720|2160|4K")
                - source 来源, 识别：Pattern.compile("B-Global|[Bb]aha|[Bb]ilibili|AT-X|Web")
                - group 字幕组, 若标题中没有字幕组, 则默认为空
                
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

        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", buildSystemPrompt());

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);

        body.put("messages", new JSONObject[]{systemMsg, userMsg});
        body.put("stream", false);
        body.put("temperature", 0);
        body.put("top_p", 0.9);
        body.put("think", false);
        body.put("format", "json");

        JSONObject responseFormat = new JSONObject();
        responseFormat.put("type", "json_object");
        body.put("response_format", responseFormat);

        // 禁用推理模型的思考链，避免超长等待（如 DeepSeek-R1）
        JSONObject thinking = new JSONObject();
        thinking.put("type", "disabled");
        body.put("thinking", thinking);

        return body.toJSONString();
    }

    private static String callApi(String apiUrl, String requestBody) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(apiUrl).toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestProperty("Content-Type", "application/json");
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
            throw new RuntimeException("Ollama API 返回错误码: " + responseCode + ", body: " + errorBody);
        }

        return new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static String extractJsonObject(String content) {
        if (StringUtils.isBlank(content)) {
            return content;
        }
        String trimmed = content.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }

    private static String cleanResultText(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        return value.replace("\r", "").replace("\n", "").trim();
    }

    private static Episode parseResponse(String responseJson, String rawTitle) {
        try {
            JSONObject root = JSON.parseObject(responseJson);
            String content = root.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            if (StringUtils.isBlank(content)) {
                log.warn("Ollama 返回内容为空，title: {}", rawTitle);
                return null;
            }

            JSONObject result = JSON.parseObject(extractJsonObject(content));

            Episode episode = Episode.builder()
                    .name(cleanResultText(result.getString("nameZh")))
                    .nameEn(cleanResultText(result.getString("nameEn")))
                    .nameJp(cleanResultText(result.getString("nameJp")))
                    .season(result.getIntValue("season"))
                    .episode(cleanResultText(result.getString("episode")))
                    .sub(cleanResultText(result.getString("sub")))
                    .dpi(cleanResultText(result.getString("dpi")))
                    .source(cleanResultText(result.getString("source")))
                    .group(cleanResultText(result.getString("group")))
                    .build();

            if (episode.getSeason() <= 0) {
                episode.setSeason(1);
            }
            if (StringUtils.isBlank(episode.getEpisode())) {
                episode.setEpisode("0");
            }

            return episode;
        } catch (Exception e) {
            log.warn("Ollama 响应解析失败，title: {}, 原因: {}", rawTitle, e.getMessage());
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
        if (setting == null || StringUtils.isBlank(setting.getBaseUrl())) {
            log.warn("Ollama 未配置 API 地址");
            return null;
        }

        String apiUrl = getApiUrl(setting);
        String model = StringUtils.defaultIfBlank(setting.getModel(), DEFAULT_MODEL);

        try {
            String userPrompt = buildUserPrompt(rawTitle);
            String requestBody = buildRequestBody(model, userPrompt);
            String responseJson = callApi(apiUrl, requestBody);
            Episode result = parseResponse(responseJson, rawTitle);

            if (result != null && StringUtils.isNotBlank(result.getEpisode())) {
                log.info("Ollama 解析标题成功: {} -> episode={}, season={}", rawTitle, result.getEpisode(), result.getSeason());
                return result;
            }
        } catch (Exception e) {
            log.warn("Ollama 解析标题失败: {}, 原因: {}", rawTitle, e.getMessage());
        }

        return null;
    }
}
