package auto.bangumi.rss.controller.v1;

import auto.bangumi.common.enums.CommonResponseEnum;
import auto.bangumi.common.model.dto.ApiPageResult;
import auto.bangumi.common.model.dto.ApiResult;
import auto.bangumi.common.model.dto.PageResult;
import auto.bangumi.common.valid.Query;
import auto.bangumi.common.valid.Update;
import auto.bangumi.rss.model.DTO.RssItem.RssItemDTO;
import auto.bangumi.rss.model.DTO.RssItem.RssItemListDTO;
import auto.bangumi.rss.model.VO.RssItem.RssItemListVO;
import auto.bangumi.rss.model.entity.RssItem;
import auto.bangumi.rss.service.IRssItemService;
import auto.bangumi.rss.service.IUnifiedRssService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Slf4j
@Validated
@RestController
@RequestMapping("v1/rss/item")
public class RssItemController {

    @Resource
    private IRssItemService iRssItemService;

    @Resource
    private IUnifiedRssService iUnifiedRssService;

    /**
     * 分页
     *
     * @param dto
     * @return
     */
    @GetMapping("page")
    public ApiPageResult<PageResult<RssItemListVO>> findRssItemPage(@Validated(Query.class) RssItemListDTO dto) {
        return ApiPageResult.success(iRssItemService.findRssItemPage(dto));
    }

    /**
     * 编辑
     *
     * @param dto
     */
    @PutMapping
    public ApiResult<Void> updateRssItemById(@RequestBody @Validated(Update.class) RssItemDTO dto) {
        iRssItemService.updateRssItemById(dto);
        return ApiResult.success();
    }


    /**
     * 修改列表信息
     *
     * @param dto
     * @return
     */
    @PutMapping("update")
    public ApiResult<Void> updateRssItemToList(@RequestBody RssItemDTO dto) {
        if (Objects.isNull(dto.getId())) {
            return ApiResult.error(CommonResponseEnum.VALID_ERROR);
        }
        iRssItemService.updateById(RssItem.builder().id(dto.getId()).name(dto.getName()).downloaded(dto.getDownloaded()).status(dto.getStatus()).pushed(dto.getPushed()).build());
        return ApiResult.success();
    }


    /**
     * 推送指定订阅
     *
     * @param torrentCodes
     * @return
     */
    @PutMapping("push")
    public ApiResult<Void> pushRssItemToDownLoad(@RequestBody @NotNull(message = "不能为空") List<String> torrentCodes) {
        iUnifiedRssService.pushRssItemToDownLoad(torrentCodes);
        return ApiResult.success();
    }

    /**
     * 手动触发可推送番剧
     *
     * @return
     */
    @PostMapping("refresh/last")
    public ApiResult<Void> triggerPushLastRssItem() {
        iUnifiedRssService.pollingLastRssItem(false);
        return ApiResult.success();
    }

    /**
     * 删除
     *
     * @param torrentCodes
     * @return
     */
    @DeleteMapping
    public ApiResult<Void> removeRssItemByTorrentCodes(@RequestBody @NotNull(message = "不能为空") List<String> torrentCodes) {
        iRssItemService.removeRssItemByTorrentCodes(torrentCodes);
        return ApiResult.success();
    }
}
