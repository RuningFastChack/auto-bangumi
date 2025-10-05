package auto.bangumi.qBittorrent.controller;

import auto.bangumi.common.model.dto.ApiResult;
import auto.bangumi.common.valid.Add;
import auto.bangumi.common.valid.Update;
import auto.bangumi.qBittorrent.model.Request.CategoriesRequest;
import auto.bangumi.qBittorrent.model.Response.CategoriesListResponse;
import auto.bangumi.qBittorrent.service.ICategoriesService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("v1/qb/categories")
public class CategoriesController {
    @Resource
    private ICategoriesService iCategoriesService;

    /**
     * 查询所有分类
     */
    @GetMapping("list")
    public ApiResult<List<CategoriesListResponse>> findCategoriesList() {
        return ApiResult.success(iCategoriesService.findCategoriesList());
    }

    /**
     * 新增分类
     */
    @PostMapping
    public ApiResult<Boolean> addCategories(@RequestBody @Validated(Add.class) CategoriesRequest request) {
        return ApiResult.success(iCategoriesService.addCategories(request));
    }

    /**
     * 修改分类
     */
    @PutMapping
    public ApiResult<Boolean> updateCategories(@RequestBody @Validated(Update.class) CategoriesRequest request) {
        return ApiResult.success(iCategoriesService.updateCategories(request));
    }

    /**
     * 删除分类
     */
    @DeleteMapping("{categories}")
    public ApiResult<Boolean> removeCategories(@PathVariable("categories") @NotBlank(message = "不能为空") String categories) {
        return ApiResult.success(iCategoriesService.removeCategories(categories));
    }
}
