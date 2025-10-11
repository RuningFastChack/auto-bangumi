package auto.bangumi.qBittorrent.controller;

import auto.bangumi.common.model.dto.ApiResult;
import auto.bangumi.common.valid.Add;
import auto.bangumi.common.valid.Delete;
import auto.bangumi.qBittorrent.model.Request.TorrentsInfoAddRequest;
import auto.bangumi.qBittorrent.model.Request.TorrentsInfoDeleteRequest;
import auto.bangumi.qBittorrent.model.Request.TorrentsInfoListRequest;
import auto.bangumi.qBittorrent.model.Response.TorrentsInfoListResponse;
import auto.bangumi.qBittorrent.service.ITorrentsService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("v1/qb/torrents")
public class TorrentsController {
    @Resource
    private ITorrentsService iTorrentsService;

    /**
     * 分页查询种子列表
     */
    @GetMapping("list")
    public ApiResult<List<TorrentsInfoListResponse>> findTorrentsPage(TorrentsInfoListRequest request) {
        return ApiResult.success(iTorrentsService.findTorrentsPage(request));
    }

    /**
     * 新增种子
     */
    @PostMapping
    public ApiResult<Boolean> addTorrent(@RequestBody @Validated(Add.class) TorrentsInfoAddRequest request) {
        return ApiResult.success(iTorrentsService.addTorrent(request));
    }

    /**
     * 暂停种子
     */
    @PutMapping("pause")
    public ApiResult<Boolean> pauseTorrent(@RequestBody @NotNull(message = "不能为空") List<String> torrents) {
        return ApiResult.success(iTorrentsService.pauseTorrent(torrents));
    }

    /**
     * 恢复种子
     */
    @PutMapping("resume")
    public ApiResult<Boolean> resumeTorrent(@RequestBody @NotNull(message = "不能为空") List<String> torrents) {
        return ApiResult.success(iTorrentsService.resumeTorrent(torrents));
    }

    /**
     * 删除种子
     */
    @DeleteMapping
    public ApiResult<Boolean> deleteTorrent(@RequestBody @Validated(Delete.class) TorrentsInfoDeleteRequest request) {
        return ApiResult.success(iTorrentsService.deleteTorrent(request));
    }
}
