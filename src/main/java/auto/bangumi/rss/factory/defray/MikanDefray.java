package auto.bangumi.rss.factory.defray;

import auto.bangumi.admin.model.UserConfig;
import auto.bangumi.common.enums.RssTypeEnum;
import auto.bangumi.common.model.RssFeed;
import auto.bangumi.common.model.parser.Episode;
import auto.bangumi.common.model.parser.PosterDTO;
import auto.bangumi.common.parser.RawParser;
import auto.bangumi.common.utils.AutoBangumiUtil;
import auto.bangumi.common.utils.ConfigCatch;
import auto.bangumi.common.utils.HttpClientUtil;
import auto.bangumi.rss.factory.annotation.RssMethod;
import auto.bangumi.rss.factory.service.RssAnalysisService;
import auto.bangumi.rss.factory.utils.MikanUtil;
import auto.bangumi.rss.model.AnalysisResult;
import auto.bangumi.rss.model.DTO.RssManage.RssManageConfigDTO;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Objects;

/**
 * Mikan订阅分析服务实现
 *
 * @author sakura
 * @version 1.0
 * @since 2026/04/27
 */
@Slf4j
@Component
@RssMethod(method = RssTypeEnum.Mikan)
public class MikanDefray implements RssAnalysisService {
    @Resource
    private ConfigCatch configCatch;

    /**
     * 获取海报下载路径
     *
     */
    @Override
    public PosterDTO getPosterInfo(String rssPath) {

        String bangumiId = MikanUtil.extractBangumiId(rssPath);
        String baseURL = MikanUtil.baseURL(rssPath);

        if (StringUtils.isBlank(bangumiId) || StringUtils.isBlank(baseURL)) {
            log.error("Mikan 获取海报下载路径失败。RSS：{}", rssPath);
            return null;
        }

        String homePage = StrUtil.format("{}/Home/Bangumi/{}", baseURL, bangumiId);

        try {
            Document doc = Jsoup.connect(homePage)
                    .userAgent("Mozilla/5.0")
                    .get();

            Element poster = doc.selectFirst(".bangumi-poster");

            if (poster != null) {
                String style = poster.attr("style");
                if (!style.isBlank()) {
                    String imagePath = style.replaceAll(".*url\\(['\"]?(.*?)['\"]?\\).*", "$1");
                    if (StringUtils.isNotBlank(imagePath)) {
                        return PosterDTO.builder()
                                .downLoadPath(baseURL + imagePath)
                                .posterLink(StrUtil.format("/images/{}.png", bangumiId))
                                .posterName(StrUtil.format("{}.png", bangumiId)) // PNG 格式
                                .build();
                    }
                }
            }
            return null;
        } catch (IOException e) {
            log.error("Mikan 获取海报下载路径异常。RSS：{}，原因：{}", rssPath, e.getMessage());
            return null;
        }
    }

    /**
     * 获取总集数
     *
     */
    @Override
    public Integer getTotalEpisodeCount(String rssPath) {
        String bangumiId = MikanUtil.extractBangumiId(rssPath);
        String baseURL = MikanUtil.baseURL(rssPath);

        if (StringUtils.isBlank(bangumiId) || StringUtils.isBlank(baseURL)) {
            log.error("Mikan 获取总集数失败。RSS：{}", rssPath);
            return 0;
        }

        String homePage = StrUtil.format("{}/Home/Bangumi/{}", baseURL, bangumiId);

        try {
            Document doc = Jsoup.connect(homePage)
                    .userAgent("Mozilla/5.0")
                    .get();

            String totalEpisode = doc.select("p.bangumi-info:contains(总集数)").text();
            String total = MikanUtil.parseTotalEpisode(totalEpisode);

            if (StringUtils.isBlank(total)) {
                return 0;
            }
            return Integer.valueOf(total);
        } catch (IOException e) {
            log.error("Mikan 获取总集数失败，解析页面错误。RSS：{}，原因：{}", rssPath, e.getMessage());
            return 0;
        }
    }

    /**
     * 解析RSS基础信息
     *
     */
    @Override
    public AnalysisResult analysisRss(String rssPath) {
        AnalysisResult result = AnalysisResult.builder().build();

        String bangumiId = MikanUtil.extractBangumiId(rssPath);

        String subGroupId = MikanUtil.extractSubGroupId(rssPath);

        result.setSubGroupId(subGroupId);

        String baseURL = MikanUtil.baseURL(rssPath);

        if (StringUtils.isBlank(bangumiId) || StringUtils.isBlank(baseURL)) {
            log.error("Mikan 解析RSS失败。RSS：{}", rssPath);
            return result;
        }

        String homePage = StrUtil.format("{}/Home/Bangumi/{}", baseURL, bangumiId);

        //region 通过页面解析
        try {
            Document doc = Jsoup.connect(homePage)
                    .userAgent("Mozilla/5.0")
                    .get();

            String weekText = doc.select("p.bangumi-info:contains(放送日期)").text();
            int week = AutoBangumiUtil.parseWeek(weekText);
            result.setUpdateWeek(week);

            String totalEpisode = doc.select("p.bangumi-info:contains(总集数)").text();
            String total = MikanUtil.parseTotalEpisode(totalEpisode);
            result.setConfig(RssManageConfigDTO.builder().totalEpisode(total).latestEpisode("0").build());

            String title = doc.select("p.bangumi-title").text();
            result.setTitle(title);

            String startText = doc.select("p.bangumi-info:contains(放送开始)").text();
            String startDate = MikanUtil.parseDate(startText);
            result.setSendData(startDate);
            result.setPosterLink(StrUtil.format("/images/{}.png", bangumiId));
        } catch (IOException e) {
            log.error("Mikan 解析RSS失败，解析页面错误。RSS：{}，原因：{}", rssPath, e.getMessage());
        }

        //endregion

        // region 通过XML解析
        try {
            String sendGet = HttpClientUtil.sendGet(rssPath);
            if (StringUtils.isNotBlank(sendGet)) {
                JAXBContext context = JAXBContext.newInstance(RssFeed.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                RssFeed rssFeed = (RssFeed) unmarshaller.unmarshal(new StringReader(sendGet));
                List<RssFeed.Item> itemList = rssFeed.getChannel().getItems();

                if (Objects.nonNull(itemList) && !itemList.isEmpty()) {
                    RssFeed.Item item = itemList.get(0);
                    String xmlLink = item.getLink();

                    Document doc = Jsoup.connect(xmlLink)
                            .userAgent("Mozilla/5.0")
                            .get();

                    String translationGroup = doc.select("p.bangumi-info:contains(字幕组)").text();
                    result.setTranslationGroup(translationGroup.replace("字幕组：", ""));

                }
                String title = rssFeed.getChannel().getItems().get(0).getTitle();
                Episode parse = RawParser.parse(title);
                result.setSeason(String.valueOf(parse.getSeason()));
                result.setTitleEn(StringUtils.isNotBlank(parse.getNameEn()) ? parse.getNameEn() : result.getTitle());
                result.setTitleJp(StringUtils.isNotBlank(parse.getNameJp()) ? parse.getNameEn() : result.getTitle());

                UserConfig.GeneralSetting setting = configCatch.findConfig().getGeneralSetting();
                String savePathRule = setting.getSavePathRule();
                if (StringUtils.isNotBlank(savePathRule)) {
                    String savePath = savePathRule
                            .replace("{officialTitle}", result.getTitle())
                            .replace("{officialTitleEn}", result.getTitleEn())
                            .replace("{officialTitleJp}", result.getTitleJp())
                            .replace("{season}", result.getSeason());
                    result.setSavePath(savePath);
                }

            } else {
                log.error("Mikan 解析RSS失败，解析XML为空。RSS：{}", rssPath);
            }
        } catch (Exception e) {
            log.error("Mikan 解析RSS失败，解析XML异常。RSS：{}，原因：{}", rssPath, e.getMessage());
        }
        // endregion

        return result;
    }

    /**
     * 解析RSS订阅
     *
     * @param rssPath RSS订阅路径
     */
    @Override
    public RssFeed parseRssFeed(String rssPath) {
        try {
            String sendGet = HttpClientUtil.sendGet(rssPath);
            if (StringUtils.isNotBlank(sendGet)) {
                JAXBContext context = JAXBContext.newInstance(RssFeed.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                return (RssFeed) unmarshaller.unmarshal(new StringReader(sendGet));
            } else {
                log.error("Mikan 解析RSS失败，解析XML为空。RSS：{}", rssPath);
            }
            return null;
        } catch (Exception e) {
            log.error("Mikan 解析RSS失败，解析XML异常。RSS：{}，原因：{}", rssPath, e.getMessage());
            return null;
        }
    }
}
