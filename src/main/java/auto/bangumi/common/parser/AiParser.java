package auto.bangumi.common.parser;

import auto.bangumi.common.model.parser.Episode;
import auto.bangumi.rss.factory.handler.TitleParserHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * AI 标题解析门面类
 * 委托 {@link TitleParserHandler} 根据配置选择对应的 AI 模型策略，
 * 失败时自动回退到 {@link RawParser}
 *
 * @author sakura
 */
@Slf4j
public abstract class AiParser {

    /**
     * 解析标题
     *
     * @param rawTitle 原始标题
     * @return 解析结果
     */
    public static Episode parse(String rawTitle) {
        return TitleParserHandler.parse(rawTitle);
    }
}
