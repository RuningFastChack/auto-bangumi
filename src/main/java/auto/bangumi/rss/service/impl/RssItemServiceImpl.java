package auto.bangumi.rss.service.impl;

import auto.bangumi.common.enums.RssResponseEnum;
import auto.bangumi.common.model.dto.PageResult;
import auto.bangumi.common.utils.PageUtils;
import auto.bangumi.rss.mapper.RssItemMapper;
import auto.bangumi.rss.model.DTO.RssItem.RssItemDTO;
import auto.bangumi.rss.model.DTO.RssItem.RssItemListDTO;
import auto.bangumi.rss.model.VO.RssItem.RssItemListVO;
import auto.bangumi.rss.model.entity.RssItem;
import auto.bangumi.rss.service.IRssItemService;
import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class RssItemServiceImpl extends ServiceImpl<RssItemMapper, RssItem> implements IRssItemService {

    /**
     * 查询记录
     *
     * @param dto
     * @return
     */
    @Override
    public PageResult<RssItemListVO> findRssItemPage(RssItemListDTO dto) {
        Page<RssItem> selectedPage = baseMapper.selectPage(PageUtils.getPage(dto), new LambdaQueryWrapper<RssItem>()
                .eq(RssItem::getRssManageId, dto.getRssManageId())
                .like(StringUtils.isNotBlank(dto.getName()), RssItem::getName, dto.getName())
                .like(StringUtils.isNotBlank(dto.getTorrentName()), RssItem::getTorrentName, dto.getTorrentName())
                .like(StringUtils.isNotBlank(dto.getTranslationGroup()), RssItem::getTranslationGroup, dto.getTranslationGroup())
                .eq(StringUtils.isNotBlank(dto.getDownloaded()), RssItem::getDownloaded, dto.getDownloaded())
                .eq(StringUtils.isNotBlank(dto.getPushed()), RssItem::getPushed, dto.getPushed())
                .orderByDesc(RssItem::getEpisodeNum));
        List<RssItem> selectedList = Optional.ofNullable(selectedPage.getRecords()).orElse(new ArrayList<>());
        return PageUtils.getPageResult(dto.getPage(), dto.getLimit(), BeanUtil.copyToList(selectedList, RssItemListVO.class), (int) selectedPage.getTotal());
    }
    /**
     * 保存记录
     *
     * @param saveBatchList
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<RssItemDTO> saveBatchRssItemList(List<RssItemDTO> saveBatchList) {
        if (Objects.isNull(saveBatchList) || saveBatchList.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> codes = saveBatchList.stream()
                .map(RssItemDTO::getTorrentCode)
                .filter(Objects::nonNull)
                .toList();

        List<String> existCodes = Optional.ofNullable(baseMapper.selectList(
                        new LambdaQueryWrapper<RssItem>().select(RssItem::getTorrentCode)
                                .in(RssItem::getTorrentCode, codes)))
                .orElse(new ArrayList<>())
                .stream().map(RssItem::getTorrentCode).toList();
        List<RssItemDTO> needSaveList = saveBatchList.stream().filter(item -> !existCodes.contains(item.getTorrentCode())).toList();
        if (needSaveList.isEmpty()) {
            return Collections.emptyList();
        }
        baseMapper.saveBatchList(needSaveList);
        return needSaveList;
    }

    /**
     * 修改
     *
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRssItemById(RssItemDTO dto) {
        RssItem selectedOne = baseMapper.selectById(dto.getId());
        RssResponseEnum.RSS_ITEM_EXISTS.assertNull(selectedOne);
        RssItem saveInfo = RssItem.builder().build();
        BeanUtil.copyProperties(dto, saveInfo);
        int updated = baseMapper.updateById(saveInfo);
        log.info("RSS Item Manage 编辑{}:{}", updated > 0 ? "成功" : "失败", JSON.toJSONString(saveInfo));

    }
}
