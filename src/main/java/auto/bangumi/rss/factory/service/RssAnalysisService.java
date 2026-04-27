package auto.bangumi.rss.factory.service;

import auto.bangumi.common.model.RssFeed;
import auto.bangumi.common.model.parser.PosterDTO;
import auto.bangumi.rss.model.AnalysisResult;

/**
 * RSS订阅分析服务
 *
 * @author sakura
 * @version 1.0
 * @since 2026/04/27
 */
public interface RssAnalysisService {

    /**
     * 获取海报下载路径
     */
    PosterDTO getPosterInfo(String rssPath);

    /**
     * 获取总集数
     */
    Integer getTotalEpisodeCount(String rssPath);

    /**
     * 解析RSS基础信息
     */
    AnalysisResult analysisRss(String rssPaths);

    /**
     * 解析RSS订阅
     */
    RssFeed parseRssFeed(String rssPath);
}
