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
                log.info("DeepSeek 解析标题成功：{} -> title:{}, episode:{}, season:{}",
                        rawTitle, result.getName(), result.getEpisode(), result.getSeason());
                return result;
            }
        } catch (Exception e) {
            log.warn("DeepSeek 解析标题失败: {}, 原因: {}", rawTitle, e.getMessage());
        }

        return null;
    }
}
