package auto.bangumi.common.model.dto;

import auto.bangumi.common.enums.CommonResponseEnum;
import auto.bangumi.common.utils.PageUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局通用返回结构
 * 工具类来源sz-admin 官方文档：https://szadmin.cn
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class ApiPageResult<T> extends ApiResult<T> {


    public static <T> ApiResult<PageResult<T>> success(List<T> data) {
        ApiResult<PageResult<T>> apiResult = new ApiPageResult<>();
        apiResult.data = (data != null) ? PageUtils.getPageResult(data) : PageUtils.getPageResult(new ArrayList<>());
        return apiResult;
    }

    public static <T> ApiResult<PageResult<T>> success(Page<T> page) {
        return success(PageUtils.getPageResult(page));
    }

    public static <T> ApiResult<PageResult<T>> success(List<T> data, Object param) {
        ApiResult<PageResult<T>> apiResult = new ApiPageResult<>();
        apiResult.data = (data != null) ? PageUtils.getPageResult(data) : PageUtils.getPageResult(new ArrayList<>());
        apiResult.setParam(param);
        return apiResult;
    }

    public static <T> ApiPageResult<PageResult<T>> success(PageResult<T> data) {
        ApiPageResult<PageResult<T>> apiResult = new ApiPageResult<>();
        apiResult.data = (data != null) ? data : PageUtils.getPageResult(new ArrayList<>());
        return apiResult;
    }

    public static <T> ApiPageResult<T> error(CommonResponseEnum responseEnum) {
        ApiPageResult<T> apiResult = new ApiPageResult<>();
        apiResult.setCode(getResponseCode(responseEnum));
        apiResult.setMessage(responseEnum.getMessage());
        return apiResult;
    }
}
