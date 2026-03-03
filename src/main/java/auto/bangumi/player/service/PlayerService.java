package auto.bangumi.player.service;

import auto.bangumi.rss.model.entity.RssItem;

/**
 * 播放器服务
 *
 * @author sakura
 * @version 1.0
 * @since 2026/03/03
 */
public interface PlayerService {

    /**
     * 播放视频
     * @param item 剧集信息
     * @return 播放URL
     */
    String playVideo(RssItem item);

    /**
     * 选择字幕
     * @param item 剧集信息
     * @param subtitleTrackId 字幕轨道ID
     * @return 是否成功
     */
    boolean selectSubtitle(RssItem item, String subtitleTrackId);
}