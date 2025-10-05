package auto.bangumi.qBittorrent.service.impl;

import auto.bangumi.common.enums.QBResponseEnum;
import auto.bangumi.common.exception.common.BusinessException;
import auto.bangumi.qBittorrent.constant.QBittorrentConstant;
import auto.bangumi.qBittorrent.constant.QBittorrentPathConstant;
import auto.bangumi.qBittorrent.model.Request.TorrentsInfoAddRequest;
import auto.bangumi.qBittorrent.model.Request.TorrentsInfoListRequest;
import auto.bangumi.qBittorrent.model.Response.TorrentsInfoListResponse;
import auto.bangumi.qBittorrent.service.ITorrentsService;
import auto.bangumi.qBittorrent.utils.QBHttpUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 种子服务
 *
 * @author 查查
 * @since 2025/10/4
 */
@Slf4j
@Service
public class TorrentsServiceImpl implements ITorrentsService {
    /**
     * 分页
     *
     * @param request
     * @return
     */
    @Override
    public List<TorrentsInfoListResponse> findTorrentsPage(TorrentsInfoListRequest request) {
        String sendGet = QBHttpUtil.sendGet(QBittorrentPathConstant.TORRENTS_LIST, request.toMap());
        if (StringUtils.isBlank(sendGet)) {
            return Collections.emptyList();
        }
        return Optional.ofNullable(JSON.parseArray(sendGet, TorrentsInfoListResponse.class)).orElse(new ArrayList<>());
    }

    /**
     * 新增
     *
     * @param request
     * @return
     */
    @Override
    public Boolean addTorrent(TorrentsInfoAddRequest request) {
        try {
            Map<String, Object> body = request.toMap();

            HttpResponse response = QBHttpUtil.sendJSONPost(QBittorrentPathConstant.TORRENTS_ADD, new HashMap<>(), body);

            QBResponseEnum.TORRENT_OPERATE_ERROR.assertNull(response);

            int status = response.getStatusLine().getStatusCode();

            String message = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            QBResponseEnum.TORRENT_OPERATE_ERROR.message(message).assertFalse(QBittorrentConstant.success.equals(status));
            return QBittorrentConstant.success.equals(status);
        } catch (Exception e) {
            log.error("新增种子异常：{}", e.getMessage());
            throw new BusinessException(QBResponseEnum.TORRENT_OPERATE_ERROR, new Object[]{request}, e.getMessage());
        }
    }

    /**
     * 暂停
     *
     * @param torrents
     * @return
     */
    @Override
    public Boolean pauseTorrent(List<String> torrents) {
        if (torrents.isEmpty()) {
            return false;
        }
        try {
            Map<String, Object> query = Map.of("hashes", String.join("|", torrents));
            HttpResponse response = QBHttpUtil.sendJSONPost(QBittorrentPathConstant.TORRENTS_PAUSE, query, new HashMap<>());
            QBResponseEnum.TORRENT_OPERATE_ERROR.assertNull(response);
            int status = response.getStatusLine().getStatusCode();
            String message = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            QBResponseEnum.TORRENT_OPERATE_ERROR.message(message).assertFalse(QBittorrentConstant.success.equals(status));
            return QBittorrentConstant.success.equals(status);
        } catch (Exception e) {
            log.error("暂停种子异常：{}", e.getMessage());
            throw new BusinessException(QBResponseEnum.TORRENT_OPERATE_ERROR, new Object[]{torrents}, e.getMessage());
        }
    }

    /**
     * 恢复
     *
     * @param torrents
     * @return
     */
    @Override
    public Boolean resumeTorrent(List<String> torrents) {
        if (torrents.isEmpty()) {
            return false;
        }
        try {
            Map<String, Object> query = Map.of("hashes", String.join("|", torrents));
            HttpResponse response = QBHttpUtil.sendJSONPost(QBittorrentPathConstant.TORRENTS_RESUME, query, new HashMap<>());
            QBResponseEnum.TORRENT_OPERATE_ERROR.assertNull(response);
            int status = response.getStatusLine().getStatusCode();
            String message = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            QBResponseEnum.TORRENT_OPERATE_ERROR.message(message).assertFalse(QBittorrentConstant.success.equals(status));
            return QBittorrentConstant.success.equals(status);
        } catch (Exception e) {
            log.error("恢复种子异常：{}", e.getMessage());
            throw new BusinessException(QBResponseEnum.TORRENT_OPERATE_ERROR, new Object[]{torrents}, e.getMessage());
        }
    }

    /**
     * 删除
     *
     * @param torrents
     * @return
     */
    @Override
    public Boolean deleteTorrent(List<String> torrents) {
        if (torrents.isEmpty()) {
            return false;
        }
        try {
            Map<String, Object> query = Map.of("hashes", String.join("|", torrents));
            HttpResponse response = QBHttpUtil.sendJSONPost(QBittorrentPathConstant.TORRENTS_DELETE, query, new HashMap<>());
            QBResponseEnum.TORRENT_OPERATE_ERROR.assertNull(response);
            int status = response.getStatusLine().getStatusCode();
            String message = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            QBResponseEnum.TORRENT_OPERATE_ERROR.message(message).assertFalse(QBittorrentConstant.success.equals(status));
            return QBittorrentConstant.success.equals(status);
        } catch (Exception e) {
            log.error("删除种子异常：{}", e.getMessage());
            throw new BusinessException(QBResponseEnum.TORRENT_OPERATE_ERROR, new Object[]{torrents}, e.getMessage());
        }
    }
}
