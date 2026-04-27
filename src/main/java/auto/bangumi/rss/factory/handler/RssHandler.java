package auto.bangumi.rss.factory.handler;

import auto.bangumi.common.context.AbstractSparrowAnnotationBeanMap;
import auto.bangumi.common.enums.RssTypeEnum;
import auto.bangumi.common.model.RssFeed;
import auto.bangumi.common.model.parser.PosterDTO;
import auto.bangumi.rss.factory.annotation.RssMethod;
import auto.bangumi.rss.factory.service.RssAnalysisService;
import auto.bangumi.rss.model.AnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * RSS处理类
 *
 * @author sakura
 * @version 1.0
 * @since 2026/04/27
 */
@Slf4j
@Component
public class RssHandler extends AbstractSparrowAnnotationBeanMap<RssMethod, RssAnalysisService> {

    private static final Map<RssTypeEnum, RssAnalysisService> MAP = new HashMap<>();

    /**
     * 获取海报下载路径
     */
    public static PosterDTO getPosterInfo(RssTypeEnum rssTypeEnum, String rssPath) {
        return MAP.get(rssTypeEnum).getPosterInfo(rssPath);
    }

    /**
     * 获取总集数
     */
    public static Integer getTotalEpisodeCount(RssTypeEnum rssTypeEnum, String rssPath) {
        return MAP.get(rssTypeEnum).getTotalEpisodeCount(rssPath);
    }

    /**
     * 解析RSS基础信息
     */
    public static AnalysisResult analysisRss(RssTypeEnum rssTypeEnum, String rssPath) {
        return MAP.get(rssTypeEnum).analysisRss(rssPath);
    }

    /**
     * 解析RSS订阅
     */
    public static RssFeed parseRssFeed(RssTypeEnum rssTypeEnum, String rssPath) {
        return MAP.get(rssTypeEnum).parseRssFeed(rssPath);
    }

    /**
     * 需要缓存的注解类型
     *
     * @return 注解类型
     */
    @Override
    public Class<RssMethod> getAnnotation() {
        return RssMethod.class;
    }

    /**
     * 暴露给实现类去操作刷新earlyBeans的接口
     *
     * @param annotationBeanMap 注解Bean映射
     */
    @Override
    public void refresh(Map<RssMethod, RssAnalysisService> annotationBeanMap) {
        annotationBeanMap.forEach((rssMethod, item) -> MAP.put(rssMethod.method(), item));
    }
}
