package auto.bangumi.qBittorrent.service;

import auto.bangumi.qBittorrent.model.Request.TorrentsInfoAddRequest;
import auto.bangumi.qBittorrent.model.Request.TorrentsInfoListRequest;
import auto.bangumi.qBittorrent.model.Response.TorrentsInfoListResponse;

import java.util.List;

/**
 * 种子服务
 *
 * @author 查查
 * @since 2025/10/4
 */
public interface ITorrentsService {

    /**
     * 分页
     *
     * @param request
     * @return
     */
    List<TorrentsInfoListResponse> findTorrentsPage(TorrentsInfoListRequest request);

    /**
     * 新增
     *
     * @param request
     * @return
     */
    Boolean addTorrent(TorrentsInfoAddRequest request);

    /**
     * 暂停
     *
     * @param torrents
     * @return
     */
    Boolean pauseTorrent(List<String> torrents);

    /**
     * 恢复
     *
     * @param torrents
     * @return
     */
    Boolean resumeTorrent(List<String> torrents);

    /**
     * 删除
     *
     * @param torrents
     * @return
     */
    Boolean deleteTorrent(List<String> torrents);
}
