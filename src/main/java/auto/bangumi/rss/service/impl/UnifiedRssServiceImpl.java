package auto.bangumi.rss.service.impl;

import auto.bangumi.common.enums.SysYesNo;
import auto.bangumi.common.model.RssFeed;
import auto.bangumi.common.model.parser.Episode;
import auto.bangumi.common.parser.RawParser;
import auto.bangumi.common.utils.AsyncManager;
import auto.bangumi.common.utils.AutoBangumiUtil;
import auto.bangumi.common.utils.ConfigCatch;
import auto.bangumi.common.utils.HttpClientUtil;
import auto.bangumi.qBittorrent.service.QBittorrentApi;
import auto.bangumi.rss.model.AnalysisResult;
import auto.bangumi.rss.model.DTO.RssItem.RssItemDTO;
import auto.bangumi.rss.model.Rss;
import auto.bangumi.rss.model.VO.RssManage.RssManageConfigVO;
import auto.bangumi.rss.model.VO.RssManage.RssManageVO;
import auto.bangumi.rss.model.entity.RssItem;
import auto.bangumi.rss.model.entity.RssManage;
import auto.bangumi.rss.service.AnalysisApi;
import auto.bangumi.rss.service.IRssItemService;
import auto.bangumi.rss.service.IRssManageService;
import auto.bangumi.rss.service.IUnifiedRssService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * UnifiedRssServiceImpl
 *
 * @author 查查
 * @since 2025/9/11
 */
@Slf4j
@Service
public class UnifiedRssServiceImpl implements IUnifiedRssService {

    @Resource
    private IRssManageService iRssManageService;
    @Resource
    private IRssItemService iRssItemService;
    @Resource
    private AnalysisApi analysisApi;

    /**
     * 刷新海报
     *
     * @param rssManageIds
     */
    @Override
    public void refreshPoster(List<Integer> rssManageIds) {
        List<RssManageVO> selectedList = Optional.ofNullable(
                iRssManageService.list(new LambdaQueryWrapper<RssManage>()
                        .in(Objects.nonNull(rssManageIds) && !rssManageIds.isEmpty(), RssManage::getId, rssManageIds))
        ).orElse(new ArrayList<>()).stream().map(RssManageVO::copy).toList();

        if (selectedList.isEmpty()) {
            return;
        }

        AsyncManager.me().execute(new TimerTask() {
            @Override
            public void run() {
                for (RssManageVO rssManage : selectedList) {
                    List<Rss> rssList = rssManage.getRssList();
                    if (Objects.nonNull(rssList) && !rssList.isEmpty()) {
                        Rss rss = rssList.get(0);
                        switch (rss.getType()) {
                            case Mikan:
                                AnalysisResult mikan = analysisApi.analysisMikan(rss.getRss());

                                RssManage build = RssManage.builder().id(rssManage.getId()).posterLink(mikan.getPosterLink()).build();

                                String episode = mikan.getConfig().getTotalEpisode();
                                if (StringUtils.isNotBlank(episode) && !"0".equals(episode)) {
                                    RssManageConfigVO config = rssManage.getConfig();
                                    config.setTotalEpisode(episode);
                                    build.setConfig(JSON.toJSONString(config));
                                }

                                iRssManageService.updateById(build);
                                break;
                            default:
                                break;
                        }
                    }
                }

            }
        });
    }

    /**
     * 轮询 - RSS订阅刷新
     */
    @Override
    public void pollingRssManage() {
        List<RssManageVO> selectedList = iRssManageService.findRequiredUpdateRssManage();
        if (selectedList.isEmpty()) {
            return;
        }

        List<Integer> rssManageIds = selectedList.stream().map(RssManageVO::getId).distinct().toList();

        refreshRssManageByIds(rssManageIds);

        pollingLastRssItem(false);
    }

    /**
     * 轮询 - 检查已推送的订阅，判断是否下载完成
     */
    @Override
    public void pollingCheckRssItem() {
        List<String> torrentCodes = Optional.ofNullable(iRssItemService.list(new LambdaQueryWrapper<RssItem>()
                        .select(RssItem::getTorrentCode)
                        .eq(RssItem::getPushed, SysYesNo.YSE.getCode())
                        .eq(RssItem::getDownloaded, SysYesNo.NO.getCode())
                        .isNotNull(RssItem::getTorrentCode)
                ))
                .orElse(new ArrayList<>()).stream().map(RssItem::getTorrentCode).toList();

        List<String> checkCodes = new ArrayList<>();

        for (String torrentCode : torrentCodes) {
            checkCodes.add(torrentCode);
            if (checkCodes.size() >= 10) {
                checkRssItem(checkCodes);
                checkCodes.clear();
            }
        }
        if (!checkCodes.isEmpty()){
            checkRssItem(checkCodes);
        }
    }

    /**
     * 轮询 - 检查番剧是否完结
     */
    @Override
    public void pollingCheckRssManageComplete() {
        List<RssManageVO> selectedList = Optional.ofNullable(iRssManageService.list(new LambdaQueryWrapper<RssManage>()
                        .eq(RssManage::getComplete, SysYesNo.NO.getCode()))).orElse(new ArrayList<>())
                .stream().map(RssManageVO::copy).toList();

        if (selectedList.isEmpty()) {
            return;
        }

        for (RssManageVO rssManage : selectedList) {
            String episode = rssManage.getConfig().getTotalEpisode();
            if ("0".equals(episode)) {
                continue;
            }
            AsyncManager.me().execute(new TimerTask() {
                @Override
                public void run() {
                    Long count = iRssItemService.count(new LambdaQueryWrapper<RssItem>().eq(RssItem::getRssManageId, rssManage.getId()));
                    Long totalEpisode = Long.valueOf(episode);
                    if (totalEpisode.equals(count)) {
                        RssManage updateInfo = RssManage.builder().id(rssManage.getId())
                                .complete(SysYesNo.YSE.getCode())
                                .build();
                        iRssManageService.updateById(updateInfo);
                    }
                }
            });
        }
    }

    private void checkRssItem(List<String> torrentCodes){
        List<String> successCodes = QBittorrentApi.CheckTorrentState(torrentCodes);
        if (!successCodes.isEmpty()) {
            iRssItemService.update(new LambdaUpdateWrapper<RssItem>()
                    .set(RssItem::getDownloaded, SysYesNo.YSE.getCode())
                    .in(RssItem::getTorrentCode, successCodes));

            List<RssItem> items = iRssItemService.list(new LambdaQueryWrapper<RssItem>().in(RssItem::getTorrentCode, successCodes));
            for (RssItem item : items) {
                AsyncManager.me().execute(new TimerTask() {
                    @Override
                    public void run() {
                        RssItemDTO build = RssItemDTO.builder().build();
                        BeanUtil.copyProperties(item, build);
                        QBittorrentApi.RenameFile(build);
                    }
                });
            }
        }
    }

    /**
     * 轮询 - 推送最新的番剧
     * 最新是无法最新了，只能做 T - 1
     */
    @Override
    public void pollingLastRssItem(Boolean isWeek) {
        int week = LocalDate.now().getDayOfWeek().getValue() - 1;

        List<RssManageVO> selectedList = Optional.ofNullable(iRssManageService.list(new LambdaQueryWrapper<RssManage>()
                .eq(isWeek, RssManage::getUpdateWeek, week)
                .eq(RssManage::getStatus, SysYesNo.YSE.getCode())
                .eq(RssManage::getComplete, SysYesNo.NO.getCode())
        )).orElse(new ArrayList<>()).stream().map(item ->
                RssManageVO.builder()
                        .id(item.getId())
                        .officialTitle(item.getOfficialTitle())
                        .season(item.getSeason())
                        .filter(StringUtils.isNotBlank(item.getFilter()) ? Arrays.asList(item.getFilter().split(",")) : new ArrayList<>())
                        .rssList(StringUtils.isNotBlank(item.getRssList()) ? JSON.parseArray(item.getRssList(), Rss.class) : new ArrayList<>())
                        .build()
        ).toList();

        if (selectedList.isEmpty()) {
            return;
        }

        List<Integer> rssManageIds = selectedList.stream().map(RssManageVO::getId).toList();

        List<RssItemDTO> rssItemList = Optional.ofNullable(iRssItemService.list(new LambdaQueryWrapper<RssItem>().in(RssItem::getRssManageId, rssManageIds)
                        .orderByAsc(RssItem::getEpisodeNum)))
                .orElse(new ArrayList<>())
                .stream().map(item -> RssItemDTO.builder()
                        .id(item.getId())
                        .torrentCode(item.getTorrentCode())
                        .episodeNum(item.getEpisodeNum())
                        .rssManageId(item.getRssManageId())
                        .translationGroup(item.getTranslationGroup())
                        .savePath(item.getSavePath())
                        .subGroupId(item.getSubGroupId())
                        .torrentName(item.getTorrentName())
                        .name(item.getName())
                        .url(item.getUrl())
                        .homepage(item.getHomepage())
                        .downloaded(item.getDownloaded())
                        .pushed(item.getPushed()).build()).toList();

        Boolean enable = ConfigCatch.findConfig().getFilterSetting().getEnable();

        Map<Integer, List<RssItemDTO>> rssItemGroupByRssManageId = rssItemList.stream().collect(Collectors.groupingBy(RssItemDTO::getRssManageId));

        Map<Integer, RssManageVO> rssManageMap = selectedList.stream().collect(Collectors.toMap(RssManageVO::getId, o -> o));

        for (Map.Entry<Integer, List<RssItemDTO>> entry : rssItemGroupByRssManageId.entrySet()) {
            RssManageVO rssManage = rssManageMap.get(entry.getKey());

            List<String> filters = Optional.ofNullable(rssManage.getFilter()).orElse(new ArrayList<>());

            //排序番剧中的字幕组
            List<String> sortTranslation = rssManage.getRssList().stream()
                    .sorted(Comparator.comparingInt(r -> r.getSort() != null ? r.getSort() : Integer.MAX_VALUE))
                    .map(Rss::getSubGroupId)
                    .toList();

            //根据剧集分组
            TreeMap<String, List<RssItemDTO>> episodeMap = entry.getValue().stream()
                    .collect(Collectors.groupingBy(
                            RssItemDTO::getEpisodeNum,
                            () -> new TreeMap<>(Comparator.comparingDouble(Double::parseDouble)),
                            Collectors.toList()
                    ));

            for (Map.Entry<String, List<RssItemDTO>> episodeList : episodeMap.entrySet()) {
                boolean isAllNotPushed = episodeList.getValue().stream().allMatch(item -> SysYesNo.NO.getCode().equals(item.getPushed()));
                if (!isAllNotPushed) {
                    continue;
                }

                //剧集根据字幕组分组
                Map<String, List<RssItemDTO>> episodeGroupByTranslation = episodeList.getValue().stream().collect(Collectors.groupingBy(RssItemDTO::getSubGroupId));

                episodeLoop:
                for (String translationId : sortTranslation) {
                    List<RssItemDTO> rssItems = episodeGroupByTranslation.getOrDefault(translationId, new ArrayList<>());

                    //谈若改字幕组没有更新。跳过该字幕组。使用下一个字幕组。
                    if (rssItems.isEmpty()) {
                        continue;
                    }

                    //只要有其中一集推送成功，退出当前剧集循环执行下一集。
                    for (RssItemDTO rssItem : rssItems) {
                        boolean shouldPush = false;
                        if (filters.isEmpty()) {
                            shouldPush = true;
                        } else if (enable) {
                            boolean isSkip = filters.stream()
                                    .anyMatch(rule -> Pattern.compile(rule).matcher(rssItem.getTorrentName()).find());
                            shouldPush = !isSkip; // 不跳过才推送
                        } else {
                            shouldPush = true;
                        }

                        if (shouldPush) {
                            pushTorrent(rssItem);
                            break episodeLoop;
                        }
                    }
                }

            }

        }
    }

    /**
     * 刷新指定订阅
     *
     * @param rssManageIds
     */
    @Override
    public void refreshRssManageByIds(List<Integer> rssManageIds) {

        List<RssItemDTO> saveBatchList = new ArrayList<>();

        List<RssManageVO> selectedList = Optional.ofNullable(
                        iRssManageService.list(new LambdaQueryWrapper<RssManage>()
                                .in(RssManage::getId, rssManageIds)
                                .isNotNull(RssManage::getRssList))
                ).orElse(new ArrayList<>())
                .stream().map(item ->
                        RssManageVO.builder()
                                .id(item.getId())
                                .officialTitle(item.getOfficialTitle())
                                .season(item.getSeason())
                                .savePath(item.getSavePath())
                                .rssList(JSON.parseArray(item.getRssList(), Rss.class))
                                .build()
                ).toList();
        if (selectedList.isEmpty()) {
            return;
        }

        Map<Integer, List<RssItem>> rssItemMap = Optional.ofNullable(iRssItemService.list(new LambdaQueryWrapper<RssItem>()
                        .in(RssItem::getRssManageId, rssManageIds)))
                .orElse(new ArrayList<>())
                .stream().collect(Collectors.groupingBy(RssItem::getRssManageId));


        for (RssManageVO rssManage : selectedList) {
            List<Rss> rssList = rssManage.getRssList();
            Set<String> torrentCodeSet = rssItemMap.computeIfAbsent(rssManage.getId(), k -> new ArrayList<>()).stream().map(RssItem::getTorrentCode).collect(Collectors.toSet());
            for (Rss rss : rssList) {
                if (SysYesNo.YSE.getCode().equals(rss.getStatus())) {
                    try {
                        String sendGet = HttpClientUtil.sendGet(rss.getRss());
                        JAXBContext context = JAXBContext.newInstance(RssFeed.class);
                        Unmarshaller unmarshaller = context.createUnmarshaller();
                        RssFeed rssFeed = (RssFeed) unmarshaller.unmarshal(new StringReader(sendGet));
                        List<RssFeed.Item> itemList = rssFeed.getChannel().getItems();

                        for (RssFeed.Item item : itemList) {
                            String torrentCode = AutoBangumiUtil.extractEpisodeId(item.getLink());

                            boolean added = torrentCodeSet.add(torrentCode);

                            if (!added) {
                                continue;
                            }

                            //获取剧集名称
                            String SeriesName = item.getTitle();

                            String episodeNum = String.valueOf(Integer.MAX_VALUE);

                            String episodeNumStr = "E" + Integer.MAX_VALUE;

                            Episode episode = RawParser.parse(SeriesName);

                            if (Objects.nonNull(episode)) {
                                Matcher isInteger = RawParser.EPISODE_INTEGER.matcher(episode.getEpisode());
                                if (isInteger.matches()) {
                                    Integer value = Integer.valueOf(episode.getEpisode());
                                    episodeNum = String.valueOf(value);
                                    episodeNumStr = value < 10 ?
                                            StrUtil.format("E0{}", value) :
                                            StrUtil.format("E{}", value);
                                }
                                Matcher isDouble = RawParser.EPISODE_DOUBLE.matcher(episode.getEpisode());
                                if (isDouble.matches()) {
                                    Double value = Double.valueOf(episode.getEpisode());
                                    episodeNum = String.valueOf(value);
                                    episodeNumStr = StrUtil.format("E{}", value);
                                }
                            }

                            String seasonNumStr = Integer.parseInt(rssManage.getSeason()) <= 9 ?
                                    StrUtil.format("S0{}", rssManage.getSeason()) :
                                    StrUtil.format("S{}", rssManage.getSeason());
                            SeriesName = StrUtil.format("{} {}{}", rssManage.getOfficialTitle(), seasonNumStr, episodeNumStr);
                            RssItemDTO build = RssItemDTO.builder()
                                    .torrentCode(torrentCode)
                                    .episodeNum(String.valueOf(episodeNum))
                                    .rssManageId(rssManage.getId())
                                    .translationGroup(StringUtils.isBlank(rss.getTranslationGroup()) ? "未知" : rss.getTranslationGroup())
                                    .subGroupId(rss.getSubGroupId())
                                    .savePath(rssManage.getSavePath())
                                    .torrentName(item.getTitle())
                                    .name(SeriesName)
                                    .url(item.getEnclosure().getUrl())
                                    .homepage(item.getLink())
                                    .downloaded(SysYesNo.NO.getCode())
                                    .pushed(SysYesNo.NO.getCode())
                                    .build();
                            saveBatchList.add(build);

                            if (saveBatchList.size() > 3000) {
                                iRssItemService.saveBatchRssItemList(saveBatchList);
                                saveBatchList.clear();
                            }
                        }
                    } catch (JAXBException e) {
                        log.error("刷新订阅，解析XML异常。番剧：{}，季度：{}，RSS：{}，原因：{}", rssManage.getOfficialTitle(), rssManage.getSeason(), rss.getRss(), e.getMessage(), e);
                    } catch (Exception e) {
                        log.error("刷新订阅异常。番剧：{}，季度：{}，RSS：{}，原因：{}", rssManage.getOfficialTitle(), rssManage.getSeason(), rss.getRss(), e.getMessage(), e);
                    }
                }
            }
        }
        if (!saveBatchList.isEmpty()) {
            iRssItemService.saveBatchRssItemList(saveBatchList);
        }
    }

    /**
     * 推送指定订阅
     *
     * @param torrentCodes
     */
    @Override
    public void pushRssItemToDownLoad(List<String> torrentCodes) {
        List<RssItem> selectedList = Optional.ofNullable(iRssItemService.list(new LambdaQueryWrapper<RssItem>().in(RssItem::getTorrentCode, torrentCodes)))
                .orElse(new ArrayList<>());
        if (selectedList.isEmpty()) {
            return;
        }

        List<RssItemDTO> copiedToList = BeanUtil.copyToList(selectedList, RssItemDTO.class);

        for (RssItemDTO rssItem : copiedToList) {
            pushTorrent(rssItem);
        }
    }

    /**
     * 推送下载
     *
     * @param item
     */
    private void pushTorrent(RssItemDTO item) {
        //开线程推送，
        AsyncManager.me().execute(new TimerTask() {
            @Override
            public void run() {
                Boolean isPush = QBittorrentApi.CreateTorrents(item);
                if (isPush) {
                    //推送成功
                    iRssItemService.update(new LambdaUpdateWrapper<RssItem>()
                            .eq(RssItem::getTorrentCode, item.getTorrentCode())
                            .set(RssItem::getPushed, SysYesNo.YSE.getCode()));
                    //刷新最新剧集
                    RssManageVO rssManage = iRssManageService.findRssManageDetailById(item.getRssManageId());
                    if (rssManage != null && rssManage.getConfig() != null) {
                        RssManage updateInfo = RssManage.builder().id(rssManage.getId()).build();

                        RssManageConfigVO config = rssManage.getConfig();
                        BigDecimal pushEpisodeNum = new BigDecimal(Optional.ofNullable(item.getEpisodeNum()).orElse("0"));
                        BigDecimal configEpisodeNum = new BigDecimal(Optional.ofNullable(config.getLatestEpisode()).orElse("0"));

                        //更新最新集数
                        if (pushEpisodeNum.compareTo(configEpisodeNum) > 0) {
                            config.setLatestEpisode(item.getEpisodeNum());
                            updateInfo.setConfig(JSON.toJSONString(config));
                        }

                        iRssManageService.updateById(updateInfo);
                    }
                }
            }
        });
    }
}
