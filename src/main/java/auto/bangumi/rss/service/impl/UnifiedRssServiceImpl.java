package auto.bangumi.rss.service.impl;

import auto.bangumi.admin.model.UserConfig;
import auto.bangumi.common.enums.SysYesNo;
import auto.bangumi.common.model.RssFeed;
import auto.bangumi.common.model.parser.Episode;
import auto.bangumi.common.model.parser.PosterDTO;
import auto.bangumi.common.parser.RawParser;
import auto.bangumi.common.utils.AsyncManager;
import auto.bangumi.common.utils.AutoBangumiUtil;
import auto.bangumi.common.utils.ConfigCatch;
import auto.bangumi.qBittorrent.service.QBittorrentApi;
import auto.bangumi.rss.factory.handler.RssHandler;
import auto.bangumi.rss.factory.utils.MikanUtil;
import auto.bangumi.rss.model.DTO.RssItem.RssItemDTO;
import auto.bangumi.rss.model.Rss;
import auto.bangumi.rss.model.VO.RssManage.RssManageConfigVO;
import auto.bangumi.rss.model.VO.RssManage.RssManageVO;
import auto.bangumi.rss.model.entity.RssItem;
import auto.bangumi.rss.model.entity.RssManage;
import auto.bangumi.rss.service.IRssItemService;
import auto.bangumi.rss.service.IRssManageService;
import auto.bangumi.rss.service.IUnifiedRssService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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
    private ConfigCatch configCatch;
    @Resource
    private QBittorrentApi qBittorrentApi;

    /**
     * 刷新海报
     *
     * @param rssManageIds RssMange主键列表
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
        for (RssManageVO rssManage : selectedList) {
            List<Rss> rssList = rssManage.getRssList();
            if (Objects.nonNull(rssList) && !rssList.isEmpty()) {
                Rss rss = rssList.get(0);
                if (Objects.isNull(rss.getType())) {
                    continue;
                }
                AsyncManager.me().execute(new TimerTask() {
                    @Override
                    public void run() {
                        PosterDTO posterDTO = RssHandler.getPosterInfo(rss.getType(), rss.getRss());
                        if (Objects.nonNull(posterDTO)) {
                            try {
                                // 下载图片
                                AutoBangumiUtil.downloadImage(posterDTO.getDownLoadPath(), posterDTO.getPosterName());
                                log.info("图片下载成功 RSS：{}，海报链接：{}", rss.getRss(), posterDTO.getPosterLink());
                            } catch (Exception e) {
                                log.info("图片下载失败 RSS：{}，原因 ：{}", rss.getRss(), e.getMessage());
                            }
                            RssManage build = RssManage.builder()
                                    .id(rssManage.getId())
                                    .posterLink(posterDTO.getPosterLink())
                                    .build();
                            iRssManageService.updateById(build);
                        }
                    }
                });
            }
        }
    }

    /**
     * 刷新总集数
     *
     * @param rssManageIds RssMange主键列表
     */
    @Override
    public void pollingRefreshRssManageBaseInfo(List<Integer> rssManageIds) {
        List<RssManageVO> selectedList = Optional.ofNullable(
                iRssManageService.list(new LambdaQueryWrapper<RssManage>()
                        .eq(Objects.isNull(rssManageIds) || rssManageIds.isEmpty(), RssManage::getComplete, SysYesNo.NO.getCode())
                        .in(Objects.nonNull(rssManageIds) && !rssManageIds.isEmpty(), RssManage::getId, rssManageIds))
        ).orElse(new ArrayList<>()).stream().map(RssManageVO::copy).toList();
        if (selectedList.isEmpty()) {
            return;
        }

        for (RssManageVO rssManage : selectedList) {
            List<Rss> rssList = rssManage.getRssList();
            if (Objects.nonNull(rssList) && !rssList.isEmpty()) {
                Rss rss = rssList.get(0);
                if (Objects.isNull(rss.getType())) {
                    continue;
                }
                AsyncManager.me().execute(new TimerTask() {
                    @Override
                    public void run() {
                        Integer episodeCount = RssHandler.getTotalEpisodeCount(rss.getType(), rss.getRss());
                        if (Objects.nonNull(episodeCount) && episodeCount > 0) {
                            RssManageConfigVO config = rssManage.getConfig();
                            config.setTotalEpisode(episodeCount.toString());
                            RssManage build = RssManage.builder()
                                    .id(rssManage.getId())
                                    .config(JSON.toJSONString(config))
                                    .build();
                            iRssManageService.updateById(build);
                        }
                    }
                });
            }
        }
    }

    /**
     * 轮询 - RSS订阅刷新
     * 只会刷新RssManage未完结且已启用的订阅
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
                        .eq(RssItem::getPushed, SysYesNo.YES.getCode())
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
        if (!checkCodes.isEmpty()) {
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
                    Long count = iRssItemService.count(new LambdaQueryWrapper<RssItem>()
                            .eq(RssItem::getPushed, SysYesNo.YES.getCode())
                            .eq(RssItem::getDownloaded, SysYesNo.YES.getCode())
                            .eq(RssItem::getRssManageId, rssManage.getId()));
                    Long totalEpisode = Long.valueOf(episode);
                    if (totalEpisode.equals(count)) {
                        RssManage updateInfo = RssManage.builder().id(rssManage.getId())
                                .complete(SysYesNo.YES.getCode())
                                .build();
                        iRssManageService.updateById(updateInfo);
                    }
                }
            });
        }
    }

    /**
     * 检查种子
     *
     * @param torrentCodes 磁力链接列表
     */
    private void checkRssItem(List<String> torrentCodes) {
        List<String> successCodes = qBittorrentApi.CheckTorrentState(torrentCodes);
        if (!successCodes.isEmpty()) {
            iRssItemService.update(new LambdaUpdateWrapper<RssItem>()
                    .set(RssItem::getDownloaded, SysYesNo.YES.getCode())
                    .in(RssItem::getTorrentCode, successCodes));

            List<RssItem> items = iRssItemService.list(new LambdaQueryWrapper<RssItem>().in(RssItem::getTorrentCode, successCodes));
            for (RssItem item : items) {
                AsyncManager.me().execute(new TimerTask() {
                    @Override
                    public void run() {
                        RssItemDTO build = RssItemDTO.builder().build();
                        BeanUtil.copyProperties(item, build);
                        qBittorrentApi.RenameFile(build);
                    }
                });
            }

            //归档种子
            AsyncManager.me().execute(new TimerTask() {
                @Override
                public void run() {
                    qBittorrentApi.ArchiveTorrents(successCodes);
                }
            });
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
                .eq(RssManage::getStatus, SysYesNo.YES.getCode())
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
                        .eq(RssItem::getStatus, SysYesNo.YES.getCode())
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

        Boolean enable = configCatch.findConfig().getFilterSetting().getEnable();

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
                List<RssItemDTO> episodeListValue = episodeList.getValue();
                if (!episodeListValue.isEmpty()) {
                    boolean isAllNotPushed = episodeListValue.stream().allMatch(item -> SysYesNo.NO.getCode().equals(item.getPushed()));
                    if (!isAllNotPushed) {
                        continue;
                    }

                    //剧集根据字幕组分组
                    Map<String, List<RssItemDTO>> episodeGroupByTranslation = episodeListValue.stream().collect(Collectors.groupingBy(RssItemDTO::getSubGroupId));

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
    }

    /**
     * 刷新指定订阅
     *
     * @param rssManageIds RssMange主键列表
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
                                .officialTitleEn(item.getOfficialTitleEn())
                                .officialTitleJp(item.getOfficialTitleJp())
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
                if (SysYesNo.YES.getCode().equals(rss.getStatus())) {
                    try {
                        RssFeed rssFeed = RssHandler.parseRssFeed(rss.getType(), rss.getRss());
                        List<RssFeed.Item> itemList = rssFeed.getChannel().getItems();
                        for (RssFeed.Item item : itemList) {
                            try {
                                String torrentCode = MikanUtil.extractEpisodeId(item.getLink());

                                boolean added = torrentCodeSet.add(torrentCode);

                                if (!added) {
                                    continue;
                                }

                                // 偏移量
                                Integer offset = Objects.nonNull(rss.getOffset()) ? rss.getOffset() : 0;

                                //获取剧集名称
                                String SeriesName = item.getTitle();

                                String episodeNum = null;

                                String episodeNumStr = "ENN";

                                Episode episode = RawParser.parse(SeriesName);

                                if (Objects.nonNull(episode) && StringUtils.isNotBlank(episode.getEpisode())) {
                                    String rawEpisode = episode.getEpisode();
                                    Matcher intMatcher = RawParser.EPISODE_INTEGER.matcher(rawEpisode);
                                    if (intMatcher.matches()) {
                                        Integer value = Integer.parseInt(rawEpisode);
                                        Integer adjusted = value + offset;
                                        adjusted = Math.max(adjusted, 0);
                                        episodeNum = String.valueOf(adjusted);
                                        episodeNumStr = adjusted < 10 ? String.format("E0%d", adjusted) : String.format("E%d", adjusted);
                                    } else {
                                        Matcher isDouble = RawParser.EPISODE_DOUBLE.matcher(episode.getEpisode());
                                        if (isDouble.matches()) {
                                            Double value = Double.parseDouble(rawEpisode);
                                            Double adjusted = value + offset;
                                            episodeNum = String.valueOf(adjusted);
                                            episodeNumStr = StrUtil.format("E{}", adjusted);
                                        }
                                    }
                                }

                                String seasonNumStr = Integer.parseInt(rssManage.getSeason()) <= 9 ?
                                        StrUtil.format("S0{}", rssManage.getSeason()) :
                                        StrUtil.format("S{}", rssManage.getSeason());
                                SeriesName = StrUtil.format("{} {}{}", rssManage.getOfficialTitle(), seasonNumStr, episodeNumStr);

                                UserConfig.GeneralSetting setting = configCatch.findConfig().getGeneralSetting();
                                String episodeTitleRule = setting.getEpisodeTitleRule();
                                if (StringUtils.isNotBlank(episodeTitleRule)) {
                                    SeriesName = episodeTitleRule
                                            .replace("{officialTitle}", rssManage.getOfficialTitle())
                                            .replace("{officialTitleEn}", rssManage.getOfficialTitleEn())
                                            .replace("{officialTitleJp}", rssManage.getOfficialTitleJp())
                                            .replace("{season}", seasonNumStr.replace("S", ""))
                                            .replace("{episode}", episodeNumStr.replace("E", ""))
                                    ;
                                }

                                RssItemDTO build = RssItemDTO.builder()
                                        .torrentCode(torrentCode)
                                        .episodeNum(episodeNum)
                                        .rssManageId(rssManage.getId())
                                        .translationGroup(StringUtils.isBlank(rss.getTranslationGroup()) ? "未知" : rss.getTranslationGroup())
                                        .subGroupId(rss.getSubGroupId())
                                        .savePath(rssManage.getSavePath())
                                        .torrentName(item.getTitle())
                                        .name(SeriesName)
                                        .url(item.getEnclosure().getUrl())
                                        .homepage(item.getLink())
                                        .readed(SysYesNo.NO.getCode())
                                        .status(SysYesNo.YES.getCode())
                                        .downloaded(SysYesNo.NO.getCode())
                                        .pushed(SysYesNo.NO.getCode())
                                        .build();
                                saveBatchList.add(build);
                            } catch (Exception e) {
                                log.error("刷新订阅，解析XML异常。番剧：{}，季度：{}，RSS：{}，原因：{}", rssManage.getOfficialTitle(), rssManage.getSeason(), rss.getRss(), e.getMessage(), e);
                            }

                            if (saveBatchList.size() > 3000) {
                                iRssItemService.saveBatchRssItemList(saveBatchList);
                                saveBatchList.clear();
                            }
                        }
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
     * @param torrentCodes 磁力链接列表
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
     * @param item 磁力链接数据
     */
    private void pushTorrent(RssItemDTO item) {
        Boolean isPush = qBittorrentApi.CreateTorrents(item);
        if (isPush) {
            //推送成功
            iRssItemService.update(new LambdaUpdateWrapper<RssItem>()
                    .eq(RssItem::getTorrentCode, item.getTorrentCode())
                    .set(RssItem::getPushed, SysYesNo.YES.getCode())
            );
            //刷新最新剧集
            RssManageVO rssManage = iRssManageService.findRssManageDetailById(item.getRssManageId());

            if (rssManage != null && rssManage.getConfig() != null) {
                RssManageConfigVO config = rssManage.getConfig();

                RssManage updateInfo = RssManage.builder()
                        .id(rssManage.getId())
                        .build();
                //推送的最新集数
                BigDecimal pushEpisodeNum = new BigDecimal(Optional.ofNullable(item.getEpisodeNum()).orElse("0"));
                //配置的最新集数
                BigDecimal configEpisodeNum = new BigDecimal(Optional.ofNullable(config.getLatestEpisode()).orElse("0"));

                //更新最新集数
                if (pushEpisodeNum.compareTo(configEpisodeNum) > 0) {
                    config.setLatestEpisode(JSON.toJSONString(pushEpisodeNum));
                    updateInfo.setConfig(JSON.toJSONString(config));
                    iRssManageService.updateById(updateInfo);
                }
            }
        }
    }
}
