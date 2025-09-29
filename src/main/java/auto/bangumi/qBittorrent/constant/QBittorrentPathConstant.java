package auto.bangumi.qBittorrent.constant;

/**
 * QBittorrent API
 *
 * @author 查查
 * @since 2025/9/27
 */
public interface QBittorrentPathConstant {

    /**
     * 登录接口
     */
    String LOGIN = "/api/v2/auth/login";

    /**
     * 获取API版本号
     */
    String GET_API_VERSION = "/api/v2/app/webapiVersion";

    /**
     * 获取种子列表
     */
    String GET_TORRENTS_LIST = "/api/v2/torrents/info";

    /**
     * 操作 - 删除种子
     */
    String OPERATE_TORRENTS_DELETE = "/api/v2/torrents/delete";

    /**
     * 操作 - 创建分组
     */
    String OPERATE_TORRENTS_CREATE_CATEGORY = "/api/v2/torrents/createCategory";

    /**
     * 添加种子
     */
    String OPERATE_TORRENTS_ADD = "/api/v2/torrents/add";

    /**
     * 文件重命名
     */
    String OPERATE_TORRENTS_RENAME_FILE = "/api/v2/torrents/renameFile";
}
