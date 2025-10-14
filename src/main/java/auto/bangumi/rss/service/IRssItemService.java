package auto.bangumi.rss.service;

import auto.bangumi.common.model.dto.PageResult;
import auto.bangumi.rss.model.DTO.RssItem.RssItemDTO;
import auto.bangumi.rss.model.DTO.RssItem.RssItemListDTO;
import auto.bangumi.rss.model.VO.RssItem.RssItemListVO;
import auto.bangumi.rss.model.entity.RssItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IRssItemService extends IService<RssItem> {

    /**
     * 查询记录
     *
     * @param dto
     * @return
     */
    PageResult<RssItemListVO> findRssItemPage(RssItemListDTO dto);

    /**
     * 保存记录
     *
     * @param saveBatchList
     * @return
     */
    List<RssItemDTO> saveBatchRssItemList(List<RssItemDTO> saveBatchList);

    /**
     * 修改
     *
     * @param dto
     */
    void updateRssItemById(RssItemDTO dto);

    /**
     * 删除
     *
     * @param torrentCodes
     */
    void removeRssItemByTorrentCodes(List<String> torrentCodes);
}
