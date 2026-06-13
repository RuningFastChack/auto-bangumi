package auto.bangumi.rss.factory.service;

import auto.bangumi.common.model.parser.Episode;

/**
 * 标题解析器接口，各 AI 模型通过此接口实现策略
 *
 * @author sakura
 */
public interface TitleParser {

    /**
     * 解析番剧标题
     *
     * @param rawTitle 原始标题
     * @return 解析结果
     */
    Episode parse(String rawTitle);
}
