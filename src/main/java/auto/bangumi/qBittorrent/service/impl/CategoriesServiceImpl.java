package auto.bangumi.qBittorrent.service.impl;

import auto.bangumi.common.enums.QBResponseEnum;
import auto.bangumi.common.exception.common.BusinessException;
import auto.bangumi.qBittorrent.constant.QBittorrentConstant;
import auto.bangumi.qBittorrent.constant.QBittorrentPathConstant;
import auto.bangumi.qBittorrent.model.Request.CategoriesRequest;
import auto.bangumi.qBittorrent.model.Response.CategoriesListResponse;
import auto.bangumi.qBittorrent.service.ICategoriesService;
import auto.bangumi.qBittorrent.utils.QBHttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 分类服务
 *
 * @author 查查
 * @since 2025/10/3
 */
@Slf4j
@Service
public class CategoriesServiceImpl implements ICategoriesService {
    /**
     * 查询所有分类
     *
     * @return
     */
    @Override
    public List<CategoriesListResponse> findCategoriesList() {
        String sendGet = QBHttpUtil.sendGet(QBittorrentPathConstant.CATEGORIES_GET_ALL, new HashMap<>());
        if (StringUtils.isBlank(sendGet)) {
            return new ArrayList<>();
        }
        Map<String, CategoriesListResponse> parsed = JSON.parseObject(sendGet, new TypeReference<>() {
        });

        if (Objects.isNull(parsed)) {
            return new ArrayList<>();
        }

        return new ArrayList<>(parsed.values());
    }

    /**
     * 修改
     *
     * @param request
     */
    @Override
    public Boolean updateCategories(CategoriesRequest request) {
        try {
            Map<String, Object> map = request.toMap();
            HttpResponse httpResponse = QBHttpUtil.sendJSONPost(QBittorrentPathConstant.CATEGORIES_EDIT, new HashMap<>(), map);
            QBResponseEnum.CATEGORIES_OPERATE_ERROR.assertNull(httpResponse);
            int status = httpResponse.getStatusLine().getStatusCode();
            String message = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
            QBResponseEnum.CATEGORIES_OPERATE_ERROR.message(message).assertFalse(QBittorrentConstant.success.equals(status));

            return QBittorrentConstant.success.equals(status);
        } catch (Exception e) {
            log.error("编辑分类异常：{}", e.getMessage());
            throw new BusinessException(QBResponseEnum.CATEGORIES_OPERATE_ERROR, new Object[]{request}, e.getMessage());
        }
    }

    /**
     * 新增
     *
     * @param request
     */
    @Override
    public Boolean addCategories(CategoriesRequest request) {
        try {
            Map<String, Object> map = request.toMap();
            HttpResponse httpResponse = QBHttpUtil.sendJSONPost(QBittorrentPathConstant.CATEGORIES_ADD, new HashMap<>(), map);
            QBResponseEnum.CATEGORIES_OPERATE_ERROR.assertNull(httpResponse);
            int status = httpResponse.getStatusLine().getStatusCode();
            String message = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
            QBResponseEnum.CATEGORIES_OPERATE_ERROR.message(message).assertFalse(QBittorrentConstant.success.equals(status));
            return QBittorrentConstant.success.equals(status);
        } catch (Exception e) {
            log.error("新增分类异常：{}", e.getMessage());
            throw new BusinessException(QBResponseEnum.CATEGORIES_OPERATE_ERROR, new Object[]{request}, e.getMessage());
        }
    }

    /**
     * 删除
     *
     * @param categories
     */
    @Override
    public Boolean removeCategories(String categories) {
        try {
            HttpResponse httpResponse = QBHttpUtil.sendJSONPost(QBittorrentPathConstant.CATEGORIES_REMOVE, new HashMap<>(), Map.of("categories", categories));
            QBResponseEnum.CATEGORIES_OPERATE_ERROR.assertNull(httpResponse);
            int status = httpResponse.getStatusLine().getStatusCode();
            String message = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
            QBResponseEnum.CATEGORIES_OPERATE_ERROR.message(message).assertFalse(QBittorrentConstant.success.equals(status));
            return QBittorrentConstant.success.equals(status);
        } catch (Exception e) {
            log.error("删除分类异常：{}", e.getMessage());
            throw new BusinessException(QBResponseEnum.CATEGORIES_OPERATE_ERROR, new Object[]{categories}, e.getMessage());
        }
    }
}
