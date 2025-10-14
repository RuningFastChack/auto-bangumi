package auto.bangumi.qBittorrent.constant;

/**
 * QBittorrent API
 *
 * @author 查查
 * @since 2025/9/27
 */
public interface QBittorrentPathConstant {

    ///登录接口
    String LOGIN = "/api/v2/auth/login";

    //region 分类

    /**
     * 获取所有分类
     */
    String CATEGORIES_GET_ALL = "/api/v2/torrents/categories";

    /**
     * 新增分类
     */
    String CATEGORIES_ADD = "/api/v2/torrents/createCategory";

    /**
     * 编辑分类
     */
    String CATEGORIES_EDIT = "/api/v2/torrents/editCategory";

    /**
     * 删除分类
     */
    String CATEGORIES_REMOVE = "/api/v2/torrents/removeCategories";

    //endregion

    //region torrent

    /**
     * 种子列表
     */
    String TORRENTS_LIST = "/api/v2/torrents/info";

    /**
     * 暂停种子
     */
    String TORRENTS_PAUSE = "/api/v2/torrents/pause";

    /**
     * 恢复种子
     */
    String TORRENTS_RESUME = "/api/v2/torrents/resume";

    /**
     * 删除种子
     */
    String TORRENTS_DELETE = "/api/v2/torrents/delete";

    /**
     * 添加种子
     */
    String TORRENTS_ADD = "/api/v2/torrents/add";

    /**
     * 设置种子分组
     */
    String TORRENTS_SET_CATEGORY = "/api/v2/torrents/setCategory";

    //endregion

    //region FILE

    String FILE_RENAME = "/api/v2/torrents/renameFile";

    //endregion

}
