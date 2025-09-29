package auto.bangumi.rss.mapper;

import auto.bangumi.rss.model.DTO.RssItem.RssItemDTO;
import auto.bangumi.rss.model.entity.RssItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RssItemMapper extends BaseMapper<RssItem> {

    /**
     * 批量保存
     *
     * @param list
     */
    void saveBatchList(@Param("list") List<RssItemDTO> list);
}