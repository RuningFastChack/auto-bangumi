package auto.bangumi.qBittorrent.service;

import auto.bangumi.qBittorrent.model.Request.CategoriesRequest;
import auto.bangumi.qBittorrent.model.Response.CategoriesListResponse;

import java.util.List;

/**
 * 分类服务
 *
 * @author 查查
 * @since 2025/10/3
 */
public interface ICategoriesService {

    /**
     * 查询所有分类
     *
     * @return
     */
    List<CategoriesListResponse> findCategoriesList();

    /**
     * 修改
     */
    Boolean updateCategories(CategoriesRequest request);

    /**
     * 新增
     */
    Boolean addCategories(CategoriesRequest request);

    /**
     * 删除
     */
    Boolean removeCategories(String categories);
}
