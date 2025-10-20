package auto.bangumi.rss.service;

import auto.bangumi.admin.model.UserConfig;
import auto.bangumi.common.model.RssFeed;
import auto.bangumi.common.model.parser.Episode;
import auto.bangumi.common.parser.RawParser;
import auto.bangumi.common.utils.AsyncManager;
import auto.bangumi.common.utils.AutoBangumiUtil;
import auto.bangumi.common.utils.ConfigCatch;
import auto.bangumi.common.utils.HttpClientUtil;
import auto.bangumi.rss.model.AnalysisResult;
import auto.bangumi.rss.model.DTO.RssManage.RssManageConfigDTO;
import cn.hutool.core.util.StrUtil;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Objects;
import java.util.TimerTask;

/**
 * 解析API
 *
 * @author 查查
 * @since 2025/9/11
 */
@Slf4j
@Service
public class AnalysisApi {

    /**
     * 解析RSS - Mikan类型
     *
     * @param rss
     * @param downImage 是否下载图片
     * @return
     */
    public AnalysisResult analysisMikan(String rss, Boolean downImage) {
        AnalysisResult result = AnalysisResult.builder().build();

        String bangumiId = AutoBangumiUtil.extractBangumiId(rss);

        String subGroupId = AutoBangumiUtil.extractSubGroupId(rss);

        result.setSubGroupId(subGroupId);

        String baseURL = AutoBangumiUtil.baseURL(rss);

        if (StringUtils.isBlank(bangumiId) || StringUtils.isBlank(baseURL)) {
            log.error("Mikan 解析RSS失败");
            return result;
        }

        //region 通过页面解析
        String homePage = StrUtil.format("{}/Home/Bangumi/{}", baseURL, bangumiId);

        try {
            Document doc = Jsoup.connect(homePage)
                    .userAgent("Mozilla/5.0")
                    .get();

            String weekText = doc.select("p.bangumi-info:contains(放送日期)").text();
            int week = AutoBangumiUtil.parseWeek(weekText);
            result.setUpdateWeek(week);

            String totalEpisode = doc.select("p.bangumi-info:contains(总集数)").text();
            String total = AutoBangumiUtil.parseTotalEpisode(totalEpisode);
            result.setConfig(RssManageConfigDTO.builder().totalEpisode(total).latestEpisode("0").build());

            String title = doc.select("p.bangumi-title").text();
            result.setTitle(title);

            String startText = doc.select("p.bangumi-info:contains(放送开始)").text();
            String startDate = AutoBangumiUtil.parseDate(startText);
            result.setSendData(startDate);
            result.setPosterLink(StrUtil.format("/images/{}.png", bangumiId));

            if (downImage){
                Element poster = doc.selectFirst(".bangumi-poster");
                AsyncManager.me().execute(new TimerTask() {
                    @Override
                    public void run() {
                        String imagePath = null;
                        if (poster != null) {
                            String style = poster.attr("style");
                            if (!style.isBlank()) {
                                imagePath = style.replaceAll(".*url\\(['\"]?(.*?)['\"]?\\).*", "$1");
                            }
                        }
                        if (imagePath != null && !imagePath.isBlank()) {
                            String fullImgUrl = baseURL + imagePath;
                            try {
                                // 下载图片
                                AutoBangumiUtil.downloadImage(fullImgUrl, bangumiId + ".png"); // PNG 格式
                                log.info("Mikan 解析RSS成功 图片下载成功：{}", fullImgUrl);
                            } catch (Exception e) {
                                log.error("Mikan 解析RSS失败 下载图片失败：{}，原因：{}", fullImgUrl, e.getMessage(), e);
                            }
                        } else {
                            log.error("Mikan 解析RSS失败 未找到封面图片，跳过下载。");
                        }
                    }
                });
            }
        } catch (IOException e) {
            log.error("Mikan 解析RSS失败 页面错误");
        }

        //endregion

        //region 通过XML解析

        try {
            String sendGet = HttpClientUtil.sendGet(rss);
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

            UserConfig.GeneralSetting setting = ConfigCatch.findConfig().getGeneralSetting();
            String savePathRule = setting.getSavePathRule();
            if (StringUtils.isNotBlank(savePathRule)) {
                String savePath = savePathRule
                        .replace("{officialTitle}", result.getTitle())
                        .replace("{officialTitleEn}", result.getTitleEn())
                        .replace("{officialTitleJp}", result.getTitleJp())
                        .replace("{season}", result.getSeason());
                result.setSavePath(savePath);
            }

        } catch (Exception e) {
            log.error("Mikan 解析RSS失败，解析XML异常。RSS：{}，原因：{}", rss, e.getMessage(), e);
        }

        //endregion

        return result;
    }
}
