package auto.bangumi.rss.service;

import auto.bangumi.common.model.dto.PageResult;
import auto.bangumi.rss.model.DTO.RssManage.RssManageDTO;
import auto.bangumi.rss.model.DTO.RssManage.RssManageListDTO;
import auto.bangumi.rss.model.VO.RssManage.RssManageCalendarVO;
import auto.bangumi.rss.model.VO.RssManage.RssManageListVO;
import auto.bangumi.rss.model.VO.RssManage.RssManageVO;
import auto.bangumi.rss.model.entity.RssManage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.HashMap;
import java.util.List;

public interface IRssManageService extends IService<RssManage> {

    /**
     * 分页
     *
     * @param dto
     * @return
     */
    PageResult<RssManageListVO> findRssManagePage(RssManageListDTO dto);

    /**
     * 日历模式
     *
     * @return
     */
    HashMap<Integer, List<RssManageCalendarVO>> findRssManageCalendar();

    /**
     * 需要更新的番剧
     *
     * @return
     */
    List<RssManageVO> findRequiredUpdateRssManage();

    /**
     * 详情
     *
     * @param id
     * @return
     */
    RssManageVO findRssManageDetailById(Integer id);

    /**
     * 创建
     *
     * @param dto
     */
    void createRssManage(RssManageDTO dto);

    /**
     * 编辑
     *
     * @param dto
     */
    void updateRssManage(RssManageDTO dto);

    /**
     * 删除
     *
     * @param id
     */
    void removeRssManage(Integer id);
}
