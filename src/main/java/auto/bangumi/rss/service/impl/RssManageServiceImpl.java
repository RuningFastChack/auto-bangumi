package auto.bangumi.rss.service.impl;

import auto.bangumi.common.enums.RssResponseEnum;
import auto.bangumi.common.enums.SysYesNo;
import auto.bangumi.common.model.dto.PageResult;
import auto.bangumi.common.utils.DateUtil;
import auto.bangumi.common.utils.PageUtils;
import auto.bangumi.qBittorrent.service.QBittorrentApi;
import auto.bangumi.rss.mapper.RssItemMapper;
import auto.bangumi.rss.mapper.RssManageMapper;
import auto.bangumi.rss.model.DTO.RssManage.RssManageDTO;
import auto.bangumi.rss.model.DTO.RssManage.RssManageListDTO;
import auto.bangumi.rss.model.Rss;
import auto.bangumi.rss.model.VO.RssManage.RssManageCalendarVO;
import auto.bangumi.rss.model.VO.RssManage.RssManageListVO;
import auto.bangumi.rss.model.VO.RssManage.RssManageVO;
import auto.bangumi.rss.model.entity.RssItem;
import auto.bangumi.rss.model.entity.RssManage;
import auto.bangumi.rss.service.IRssManageService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RSS管理
 */
@Slf4j
@Service
public class RssManageServiceImpl extends ServiceImpl<RssManageMapper, RssManage> implements IRssManageService {

    @Resource
    private RssItemMapper rssItemMapper;
    @Resource
    private QBittorrentApi qBittorrentApi;

    /**
     * 分页
     *
     * @param dto 分页查询参数
     * @return 分页结果
     */
    @Override
    public PageResult<RssManageListVO> findRssManagePage(RssManageListDTO dto) {
        Page<RssManage> selectedPage = baseMapper.selectPage(PageUtils.getPage(dto), new LambdaQueryWrapper<RssManage>()
                .and(StringUtils.isNotBlank(dto.getOfficialTitle()),
                        item -> item.like(StringUtils.isNotBlank(dto.getOfficialTitle()), RssManage::getOfficialTitle, dto.getOfficialTitle()).or()
                                .like(StringUtils.isNotBlank(dto.getOfficialTitle()), RssManage::getOfficialTitleEn, dto.getOfficialTitle()).or()
                                .like(StringUtils.isNotBlank(dto.getOfficialTitle()), RssManage::getOfficialTitleJp, dto.getOfficialTitle()))
                .eq(StringUtils.isNotBlank(dto.getSeason()), RssManage::getSeason, dto.getSeason())
                .eq(StringUtils.isNotBlank(dto.getStatus()), RssManage::getStatus, dto.getStatus())
                .eq(StringUtils.isNotBlank(dto.getComplete()), RssManage::getComplete, dto.getComplete())
                .eq(Objects.nonNull(dto.getUpdateWeek()), RssManage::getUpdateWeek, dto.getUpdateWeek())
                .ge(StringUtils.isNotBlank(dto.getSendDateForm()), RssManage::getSendDate, dto.getSendDateForm())
                .le(StringUtils.isNotBlank(dto.getSendDateTo()), RssManage::getSendDate, dto.getSendDateTo())
                .orderByDesc(RssManage::getSendDate, RssManage::getStatus, RssManage::getUpdateWeek));
        List<RssManage> rssManages = Optional.ofNullable(selectedPage.getRecords()).orElse(new ArrayList<>());
        ArrayList<RssManageListVO> result = new ArrayList<>(rssManages.size());
        for (RssManage rssManage : rssManages) {
            RssManageListVO copy = RssManageListVO.copy(rssManage);
            List<Rss> rssList = StringUtils.isNotBlank(rssManage.getRssList()) ? JSON.parseArray(rssManage.getRssList(), Rss.class) : new ArrayList<>();
            copy.setTranslationGroupList(rssList.stream().filter(item -> item.getStatus().equals(SysYesNo.YES.getCode())).map(Rss::getTranslationGroup).collect(Collectors.toList()));
            result.add(copy);
        }
        return PageUtils.getPageResult(dto.getPage(), dto.getLimit(), result, (int) selectedPage.getTotal());
    }

    /**
     * 日历模式
     *
     * @return 日历结果
     */
    @Override
    public HashMap<Integer, List<RssManageCalendarVO>> findRssManageCalendar() {
        List<RssManage> selectedList = Optional.ofNullable(baseMapper.selectList(new LambdaQueryWrapper<RssManage>()
                .eq(RssManage::getStatus, SysYesNo.YES.getCode())
                .eq(RssManage::getComplete, SysYesNo.NO.getCode())
                .orderByDesc(RssManage::getSendDate))).orElse(new ArrayList<>());
        HashMap<Integer, List<RssManageCalendarVO>> calendarMap = new HashMap<>();

        for (RssManage rssManage : selectedList) {
            Integer week = rssManage.getUpdateWeek();
            RssManageCalendarVO result = RssManageCalendarVO.copy(rssManage);
            List<Rss> rssList = StringUtils.isNotBlank(rssManage.getRssList()) ? JSON.parseArray(rssManage.getRssList(), Rss.class) : new ArrayList<>();
            result.setTranslationGroupList(rssList.stream().filter(item -> item.getStatus().equals(SysYesNo.YES.getCode())).map(Rss::getTranslationGroup).collect(Collectors.toList()));
            calendarMap.computeIfAbsent(week, k -> new ArrayList<>()).add(result);
        }
        return calendarMap;
    }

    /**
     * 需要更新的番剧
     * 需要更新的番剧，仅查询启用且未完结的番剧
     *
     * @return 需要更新的番剧列表
     */
    @Override
    public List<RssManageVO> findRequiredUpdateRssManage() {
        return Optional.ofNullable(baseMapper.selectList(new LambdaQueryWrapper<RssManage>()
                        .eq(RssManage::getStatus, SysYesNo.YES.getCode())
                        .eq(RssManage::getComplete, SysYesNo.NO.getCode())
                )).orElse(new ArrayList<>())
                .stream().map(RssManageVO::copy).toList();
    }

    /**
     * 详情
     *
     * @param id 主键
     * @return 详情
     */
    @Override
    public RssManageVO findRssManageDetailById(Integer id) {
        RssManage selectedOne = baseMapper.selectById(id);
        RssResponseEnum.RSS_MANAGE_EXISTS.assertNull(selectedOne);
        return RssManageVO.copy(selectedOne);
    }

    /**
     * 创建
     *
     * @param dto 创建参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRssManage(RssManageDTO dto) {
        String savePath = StrUtil.format("/{}/Season {}",
                dto.getOfficialTitle(),
                dto.getSeason());

        List<Rss> rssList = dto.getRssList();
        List<Rss> uniqueRssList = new ArrayList<>(rssList.stream()
                .filter(Objects::nonNull) // 过滤掉 null 元素
                .collect(Collectors.toMap(
                        Rss::getRss, // 根据 rss 字段作为 key
                        rss -> rss,  // 保留整个 Rss 对象
                        (existing, replacement) -> existing // 如果有重复的 rss，保留第一个
                ))
                .values()).stream().sorted(Comparator.comparingInt(r -> r.getSort() != null ? r.getSort() : 0)).toList();

        RssManage saveInfo = RssManage.builder().build();

        BeanUtil.copyProperties(dto, saveInfo, CopyOptions.create().setIgnoreProperties("filter", "rssList"));

        saveInfo.setSavePath(StringUtils.isBlank(dto.getSavePath()) ? savePath : dto.getSavePath());

        saveInfo.setFilter(Objects.nonNull(dto.getFilter()) && !dto.getFilter().isEmpty() ? String.join(",", dto.getFilter()) : "");

        saveInfo.setSendDate(StringUtils.isNotBlank(dto.getSendDate()) ? dto.getSendDate() : DateUtil.getNowDate(DateUtil.TIME_SPLIT_PATTERN));

        saveInfo.setRssList(!uniqueRssList.isEmpty() ? JSON.toJSONString(uniqueRssList) : JSON.toJSONString(new ArrayList<>()));

        saveInfo.setConfig(JSON.toJSONString(dto.getConfig()));

        int insert = baseMapper.insert(saveInfo);
        log.info("RSS Manage 保存{}:{}", insert > 0 ? "成功" : "失败", JSON.toJSONString(saveInfo));
    }

    /**
     * 编辑
     *
     * @param dto 更新参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRssManage(RssManageDTO dto) {
        RssManage selectedOne = baseMapper.selectById(dto.getId());
        RssResponseEnum.RSS_MANAGE_EXISTS.assertNull(selectedOne);

        List<Rss> rssList = dto.getRssList();

        List<Rss> uniqueRssList = new ArrayList<>(rssList.stream()
                .filter(Objects::nonNull) // 过滤掉 null 元素
                .collect(Collectors.toMap(
                        Rss::getRss, // 根据 rss 字段作为 key
                        rss -> rss,  // 保留整个 Rss 对象
                        (existing, replacement) -> existing // 如果有重复的 rss，保留第一个
                ))
                .values()).stream().sorted(Comparator.comparingInt(r -> r.getSort() != null ? r.getSort() : 0)).toList();

        RssManage saveInfo = RssManage.builder().build();
        BeanUtil.copyProperties(dto, saveInfo, CopyOptions.create().setIgnoreProperties("filter", "rssList", "config"));
        saveInfo.setFilter(Objects.nonNull(dto.getFilter()) && !dto.getFilter().isEmpty() ? String.join(",", dto.getFilter()) : "");
        saveInfo.setRssList(!uniqueRssList.isEmpty() ? JSON.toJSONString(uniqueRssList) : JSON.toJSONString(new ArrayList<>()));
        saveInfo.setConfig(Objects.nonNull(dto.getConfig()) ? JSON.toJSONString(dto.getConfig()) : JSON.toJSONString(new RssManageDTO()));

        int updated = baseMapper.updateById(saveInfo);
        log.info("RSS Manage 编辑{}:{}", updated > 0 ? "成功" : "失败", JSON.toJSONString(saveInfo));

        //同步修改订阅记录的启用状态
        Map<String, List<Rss>> rssGroupByStatus = uniqueRssList.stream().collect(Collectors.groupingBy(Rss::getStatus));
        for (Map.Entry<String, List<Rss>> entry : rssGroupByStatus.entrySet()) {
            List<Rss> rssListCopy = entry.getValue();
            if (!rssListCopy.isEmpty()) {
                List<String> subGroupIds = rssListCopy.stream().map(Rss::getSubGroupId).toList();
                rssItemMapper.update(new LambdaUpdateWrapper<RssItem>()
                        .set(RssItem::getStatus, entry.getKey())
                        .in(RssItem::getSubGroupId, subGroupIds));
            }
        }
    }

    /**
     * 更新状态
     * 更改状态时，是否启用状态status会和RssList以及RssItem进行联动更新。
     * 仅适合禁用状态，因为我不想启动的是将所有Rss订阅都启动。
     *
     * @param dto 更新参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRssManageStatusOrComplete(RssManageDTO dto) {
        Integer id = dto.getId();
        String status = dto.getStatus();
        String complete = dto.getComplete();

        RssManage selectedOne = baseMapper.selectById(id);
        RssResponseEnum.RSS_MANAGE_EXISTS.assertNull(selectedOne);
        RssManage updateInfo = RssManage.builder()
                .id(id)
                .status(status)
                .complete(complete)
                .build();

        if (SysYesNo.NO.getCode().equals(status)) {
            String rssList = selectedOne.getRssList();
            List<Rss> rssListCopy = JSON.parseArray(rssList, Rss.class);
            if (!rssListCopy.isEmpty()) {
                for (Rss rss : rssListCopy) {
                    rss.setStatus(SysYesNo.NO.getCode());
                }
            }
            updateInfo.setRssList(JSON.toJSONString(rssListCopy));
            rssItemMapper.update(new LambdaUpdateWrapper<RssItem>()
                    .set(RssItem::getStatus, SysYesNo.NO.getCode())
                    .eq(RssItem::getRssManageId, id)
            );
        }
        baseMapper.updateById(updateInfo);
        log.info("RSS Manage 更新状态{}:{}", id > 0 ? "成功" : "失败", JSON.toJSONString(updateInfo));
    }

    /**
     * 删除
     *
     * @param id 主键
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRssManage(Integer id) {

        RssManage rssManage = baseMapper.selectById(id);

        RssResponseEnum.RSS_MANAGE_EXISTS.assertNull(rssManage);

        boolean removeRssTorrent = true;
        List<String> torrentCodes = Optional.ofNullable(rssItemMapper.selectList(new LambdaQueryWrapper<RssItem>()
                        .eq(RssItem::getRssManageId, id)
                        .eq(RssItem::getPushed, SysYesNo.YES.getCode())))
                .orElse(new ArrayList<>()).stream().map(RssItem::getTorrentCode).toList();
        if (!torrentCodes.isEmpty()) {
            removeRssTorrent = qBittorrentApi.RemoveTorrents(torrentCodes);
        }
        int deleted = baseMapper.deleteById(rssManage.getId());
        log.info("RSS Manage 删除订阅{} 番剧:{} 季度:{} 删除种子:{}",
                deleted > 0 ? "成功" : "失败",
                rssManage.getOfficialTitle(),
                rssManage.getSeason(),
                removeRssTorrent ? "成功" : "失败"
        );
        rssItemMapper.delete(new LambdaQueryWrapper<RssItem>().eq(RssItem::getRssManageId, rssManage.getId()));
    }
}
