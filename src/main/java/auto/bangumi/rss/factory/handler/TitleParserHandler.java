package auto.bangumi.rss.factory.handler;

import auto.bangumi.admin.model.UserConfig;
import auto.bangumi.common.context.AbstractSparrowAnnotationBeanMap;
import auto.bangumi.common.enums.AiModelEnum;
import auto.bangumi.common.model.parser.Episode;
import auto.bangumi.common.parser.RawParser;
import auto.bangumi.common.utils.ConfigCatch;
import auto.bangumi.common.utils.SpringContextUtil;
import auto.bangumi.rss.factory.annotation.ParserMethod;
import auto.bangumi.rss.factory.service.TitleParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 标题解析器处理器
 * 自动注册所有 {@link ParserMethod} 注解的策略实现，
 * 根据配置选择 AI 模型解析，失败时回退到 {@link RawParser}
 *
 * @author sakura
 */
@Slf4j
@Component
public class TitleParserHandler extends AbstractSparrowAnnotationBeanMap<ParserMethod, TitleParser> {

    private static final Map<AiModelEnum, TitleParser> PARSER_MAP = new HashMap<>();

    /**
     * 解析标题
     * 步骤：
     * 1. 检查 AI 解析是否启用，未启用则直接走 RawParser
     * 2. 根据配置选择对应的 AI 模型策略进行解析
     * 3. AI 解析失败时回退到 RawParser
     *
     * @param rawTitle 原始标题
     * @return 解析结果
     */
    public static Episode parse(String rawTitle) {
        UserConfig.AiParseSetting aiSetting = getAiSetting();

        // 步骤 1: AI 未配置或未启用，直接走原始解析
        if (!isAiEnabled(aiSetting)) {
            return RawParser.parse(rawTitle);
        }

        // 步骤 2: 根据配置选择 AI 模型
        AiModelEnum provider = getProvider(aiSetting);
        TitleParser parser = PARSER_MAP.get(provider);

        if (parser == null) {
            log.warn("未找到 AI 模型 [{}] 的解析器，回退到正则解析", provider);
            return RawParser.parse(rawTitle);
        }

        // 步骤 3: 尝试 AI 解析，失败则回退
        try {
            Episode result = parser.parse(rawTitle);
            if (result != null && Objects.nonNull(result.getEpisode()) && !"0".equals(result.getEpisode())) {
                log.debug("AI 解析标题成功: {} -> episode={}, season={}", rawTitle, result.getEpisode(), result.getSeason());
                return result;
            }
        } catch (Exception e) {
            log.warn("AI 解析标题失败，回退到正则解析。title: {}，原因: {}", rawTitle, e.getMessage());
        }

        return RawParser.parse(rawTitle);
    }

    /**
     * 判断 AI 解析是否启用
     * 本地模式（配置了 baseUrl）不需要 API Key，云端模式需要 API Key
     */
    private static boolean isAiEnabled(UserConfig.AiParseSetting setting) {
        if (setting == null || !Boolean.TRUE.equals(setting.getEnabled())) {
            return false;
        }
        // 配置了自定义地址（本地模型）不需要 API Key
        if (StringUtilsNoneBlank(setting.getBaseUrl())) {
            return true;
        }
        // 云端模式需要 API Key
        return StringUtilsNoneBlank(setting.getApiKey());
    }

    /**
     * 获取配置的 AI 模型供应商
     */
    private static AiModelEnum getProvider(UserConfig.AiParseSetting setting) {
        if (setting == null) {
            return null;
        }
        return setting.getProvider() != null ? setting.getProvider() : AiModelEnum.DEEPSEEK;
    }

    private static boolean StringUtilsNoneBlank(String... strings) {
        for (String str : strings) {
            if (org.apache.commons.lang3.StringUtils.isBlank(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取 AI 解析配置
     */
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
    public Class<ParserMethod> getAnnotation() {
        return ParserMethod.class;
    }

    @Override
    public void refresh(Map<ParserMethod, TitleParser> annotationBeanMap) {
        annotationBeanMap.forEach((annotation, parser) -> {
            AiModelEnum model = annotation.model();
            log.info("注册标题解析器: {} -> {}", model, parser.getClass().getSimpleName());
            PARSER_MAP.put(model, parser);
        });
    }
}
