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
import auto.bangumi.rss.model.entity.RssManage;
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
     * @param dto
     * @return
     */
    @GetMapping("page")
    public ApiPageResult<PageResult<RssManageListVO>> findRssManagePage(RssManageListDTO dto) {
        return ApiPageResult.success(iRssManageService.findRssManagePage(dto));
    }

    /**
     * 日历
     *
     * @return
     */
    @GetMapping("calendar")
    public ApiResult<HashMap<Integer, List<RssManageCalendarVO>>> findRssManageCalendar() {
        return ApiResult.success(iRssManageService.findRssManageCalendar());
    }

    /**
     * 详情
     *
     * @param id
     * @return
     */
    @GetMapping
    public ApiResult<RssManageVO> findRssManageDetail(@NotNull(message = "不能为空") Integer id) {
        return ApiResult.success(iRssManageService.findRssManageDetailById(id));
    }

    /**
     * 创建
     *
     * @param dto
     */
    @PostMapping
    public ApiResult<Void> createRssManage(@RequestBody @Validated(Add.class) RssManageDTO dto) {
        iRssManageService.createRssManage(dto);
        return ApiResult.success();
    }

    /**
     * 编辑
     *
     * @param dto
     */
    @PutMapping
    public ApiResult<Void> updateRssManage(@RequestBody @Validated(Update.class) RssManageDTO dto) {
        iRssManageService.updateRssManage(dto);
        return ApiResult.success();
    }

    /**
     * 修改状态
     *
     * @param dto
     * @return
     */
    @PutMapping("change")
    public ApiResult<Void> changeRssManage(@RequestBody RssManageDTO dto) {
        if (Objects.isNull(dto.getId())) {
            return ApiResult.error(CommonResponseEnum.VALID_ERROR);
        }
        iRssManageService.updateById(RssManage.builder().id(dto.getId()).status(dto.getStatus()).complete(dto.getComplete()).build());
        return ApiResult.success();
    }

    /**
     * 删除
     *
     * @param params
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
     * @param rssManageIds
     * @return
     */
    @PutMapping("refresh/poster")
    public ApiResult<Void> refreshPoster(@RequestBody List<Integer> rssManageIds) {
        iUnifiedRssService.refreshPoster(rssManageIds);
        return ApiResult.success();
    }

    /**
     * 刷新订阅
     *
     * @param rssManageIds
     * @return
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
