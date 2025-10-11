package auto.bangumi.rss.service;

import java.util.List;

/**
 * 统一整合接口
 *
 * @author 查查
 * @since 2025/9/11
 */
public interface IUnifiedRssService {

    /**
     * 刷新海报
     *
     * @param rssManageIds
     */
    void refreshPoster(List<Integer> rssManageIds);

    /**
     * 轮询 - RSS订阅刷新
     */
    void pollingRssManage();

    /**
     * 轮询 - 检查已推送的订阅，判断是否下载完成
     */
    void pollingCheckRssItem();

    /**
     * 轮询 - 检查番剧是否完结
     */
    void pollingCheckRssManageComplete();

    /**
     * 轮询 - 推送最新的番剧
     *       最新是无法最新了，只能做 T - 1
     * @param isWeek 需要日期限制
     */
    void pollingLastRssItem(Boolean isWeek);

    /**
     * 刷新指定订阅
     *
     * @param rssManageIds
     */
    void refreshRssManageByIds(List<Integer> rssManageIds);

    /**
     * 推送指定订阅
     *
     * @param torrentCodes
     */
    void pushRssItemToDownLoad(List<String> torrentCodes);
}
