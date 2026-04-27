package auto.bangumi.rss.controller.v1;

import auto.bangumi.common.enums.CommonResponseEnum;
import auto.bangumi.common.model.dto.ApiPageResult;
import auto.bangumi.common.model.dto.ApiResult;
import auto.bangumi.common.model.dto.PageResult;
import auto.bangumi.common.valid.Add;
import auto.bangumi.common.valid.Update;
import auto.bangumi.rss.model.DTO.RssManage.RssManageDTO;
import auto.bangumi.rss.model.DTO.RssManage.RssManageListDTO;
import auto.bangumi.rss.model.VO.RssManage.RssManageCalendarVO;
import auto.bangumi.rss.model.VO.RssManage.RssManageListVO;
import auto.bangumi.rss.model.VO.RssManage.RssManageVO;
import auto.bangumi.rss.service.IRssManageService;
import auto.bangumi.rss.service.IUnifiedRssService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Slf4j
@Validated
@RestController
@RequestMapping("v1/rss/manage")
public class RssManageController {

    @Resource
    private IRssManageService iRssManageService;

    @Resource
    private IUnifiedRssService iUnifiedRssService;

    /**
     * 分页
     *
     * @param dto 分页查询参数
     * @return 分页结果
     */
    @GetMapping("page")
    public ApiPageResult<PageResult<RssManageListVO>> findRssManagePage(RssManageListDTO dto) {
        return ApiPageResult.success(iRssManageService.findRssManagePage(dto));
    }

    /**
     * 日历
     *
     * @return 日历结果
     */
    @GetMapping("calendar")
    public ApiResult<HashMap<Integer, List<RssManageCalendarVO>>> findRssManageCalendar() {
        return ApiResult.success(iRssManageService.findRssManageCalendar());
    }

    /**
     * 详情
     *
     * @param id 主键
     */
    @GetMapping
    public ApiResult<RssManageVO> findRssManageDetail(@NotNull(message = "不能为空") Integer id) {
        return ApiResult.success(iRssManageService.findRssManageDetailById(id));
    }

    /**
     * 创建
     *
     * @param dto 创建参数
     */
    @PostMapping
    public ApiResult<Void> createRssManage(@RequestBody @Validated(Add.class) RssManageDTO dto) {
        iRssManageService.createRssManage(dto);
        return ApiResult.success();
    }

    /**
     * 编辑
     *
     * @param dto 更新参数
     */
    @PutMapping
    public ApiResult<Void> updateRssManage(@RequestBody @Validated(Update.class) RssManageDTO dto) {
        iRssManageService.updateRssManage(dto);
        return ApiResult.success();
    }

    /**
     * 修改状态
     *
     * @param dto 更新参数
     */
    @PutMapping("change")
    public ApiResult<Void> changeRssManage(@RequestBody RssManageDTO dto) {
        if (Objects.isNull(dto.getId())) {
            return ApiResult.error(CommonResponseEnum.VALID_ERROR);
        }
        iRssManageService.updateRssManageStatus(dto.getId(), dto.getStatus());
        return ApiResult.success();
    }

    /**
     * 删除
     *
     * @param params 删除参数
     */
    @DeleteMapping
    public ApiResult<Void> removeRssManage(@RequestBody HashMap<String,Integer> params) {
        CommonResponseEnum.VALID_ERROR.assertNull(params.get("id"));
        Integer id = params.get("id");
        iRssManageService.removeRssManage(id);
        return ApiResult.success();
    }

    /**
     * 刷新海报
     *
     * @param rssManageIds 刷新参数
     */
    @PutMapping("refresh/poster")
    public ApiResult<Void> refreshPoster(@RequestBody List<Integer> rssManageIds) {
        iUnifiedRssService.refreshPoster(rssManageIds);
        return ApiResult.success();
    }

    /**
     * 刷新订阅
     *
     * @param rssManageIds 刷新参数
     */
    @PutMapping("refresh/manage")
    public ApiResult<Void> refreshRssManageByIds(@RequestBody @NotNull(message = "不能为空") List<Integer> rssManageIds) {
        if (rssManageIds.isEmpty()) {
            ApiResult<Void> error = ApiResult.error(CommonResponseEnum.VALID_ERROR);
            error.setMessage("请选择需要刷新的订阅");
            return error;
        }
        iUnifiedRssService.refreshRssManageByIds(rssManageIds);
        return ApiResult.success();
    }
}
