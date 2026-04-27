package auto.bangumi.rss.service;

import auto.bangumi.common.model.dto.PageResult;
import auto.bangumi.rss.model.DTO.RssItem.RssItemDTO;
import auto.bangumi.rss.model.DTO.RssItem.RssItemListDTO;
import auto.bangumi.rss.model.VO.RssItem.RssItemListVO;
import auto.bangumi.rss.model.entity.RssItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * RSS剧集
 */
public interface IRssItemService extends IService<RssItem> {

    /**
     * 查询记录
     *
     * @param dto 查询参数
     * @return 分页结果
     */
    PageResult<RssItemListVO> findRssItemPage(RssItemListDTO dto);

    /**
     * 保存记录
     *
     * @param saveBatchList 保存参数
     */
    void saveBatchRssItemList(List<RssItemDTO> saveBatchList);

    /**
     * 修改
     *
     * @param dto 更新参数
     */
    void updateRssItemById(RssItemDTO dto);

    /**
     * 删除
     *
     * @param torrentCodes 删除参数
     */
    void removeRssItemByTorrentCodes(List<String> torrentCodes);
}
