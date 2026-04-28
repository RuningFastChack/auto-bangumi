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

/**
 * RSS管理
 */
public interface IRssManageService extends IService<RssManage> {

    /**
     * 分页
     *
     * @param dto 分页查询参数
     * @return 分页结果
     */
    PageResult<RssManageListVO> findRssManagePage(RssManageListDTO dto);

    /**
     * 日历模式
     *
     * @return 日历结果
     */
    HashMap<Integer, List<RssManageCalendarVO>> findRssManageCalendar();

    /**
     * 需要更新的番剧，仅查询启用且未完结的番剧
     *
     * @return 需要更新的番剧列表
     */
    List<RssManageVO> findRequiredUpdateRssManage();

    /**
     * 详情
     *
     * @param id 主键
     * @return 详情
     */
    RssManageVO findRssManageDetailById(Integer id);

    /**
     * 创建
     *
     * @param dto 创建参数
     */
    void createRssManage(RssManageDTO dto);

    /**
     * 编辑
     * 更改状态时，是否启用状态status会和RssList以及RssItem进行联动更新。
     * 仅适合禁用状态，因为我不想启动的是将所有Rss订阅都启动。
     * @param dto 更新参数
     */
    void updateRssManage(RssManageDTO dto);

    /**
     * 更新状态
     * 更改状态时，是否启用状态status会和RssList以及RssItem进行联动更新。
     * 仅适合禁用状态，因为我不想启动的是将所有Rss订阅都启动。
     *
     * @param dto 更新参数
     */
    void updateRssManageStatusOrComplete(RssManageDTO dto);

    /**
     * 删除
     *
     * @param id 主键
     */
    void removeRssManage(Integer id);
}
