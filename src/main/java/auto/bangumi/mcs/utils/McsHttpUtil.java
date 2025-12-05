package auto.bangumi.mcs.utils;

import auto.bangumi.admin.model.UserConfig;
import auto.bangumi.common.enums.McsResponseEnum;
import auto.bangumi.common.utils.ConfigCatch;
import auto.bangumi.common.utils.HttpClientUtil;
import auto.bangumi.common.utils.SpringContextUtil;
import auto.bangumi.mcs.constant.McsManageConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;

import java.util.Map;

/**
 * McsHttpUtil
 *
 * @author 查查
 * @since 2025/9/23
 */
@Slf4j
public abstract class McsHttpUtil {

    private static String DAEMON_ID = "";

    private static String INSTANCE_ID = "";

    private static String API_KEY = "";

    private static String URL = "";

    static {
        UserConfig.McsManageSetting mcsManageSetting = SpringContextUtil.getBean(ConfigCatch.class).findConfig().getMcsManageSetting();
        DAEMON_ID = mcsManageSetting.getDaemonId();
        INSTANCE_ID = mcsManageSetting.getInstanceId();
        API_KEY = mcsManageSetting.getMcsManageKey();
        URL = mcsManageSetting.getUrl();
        McsResponseEnum.VALID_ERROR.assertTrue(StringUtils.isAnyBlank(DAEMON_ID, INSTANCE_ID, API_KEY, URL));
    }

    public static String sendGet(String path, Map<String, Object> params) {
        params.put(McsManageConstant.API_KEY, API_KEY);
        params.put(McsManageConstant.DAEMON_ID, DAEMON_ID);
        params.put(McsManageConstant.INSTANCE_ID, INSTANCE_ID);
        return HttpClientUtil.sendGet(URL + path, params, McsManageConstant.HEADERS);
    }

    /**
     * POST请求 - json提交
     */
    public static HttpResponse sendJSONPost(String path, Map<String, Object> query, Map<String, Object> body) {
        query.put(McsManageConstant.API_KEY, API_KEY);
        query.put(McsManageConstant.DAEMON_ID, DAEMON_ID);
        query.put(McsManageConstant.INSTANCE_ID, INSTANCE_ID);
        return HttpClientUtil.sendJsonPost(URL, path, McsManageConstant.HEADERS, query, body);
    }

    /**
     * PUT请求 - form提交
     */
    public static HttpResponse sendJSONPut(String path, Map<String, Object> query, Map<String, Object> body) {
        query.put(McsManageConstant.API_KEY, API_KEY);
        query.put(McsManageConstant.DAEMON_ID, DAEMON_ID);
        query.put(McsManageConstant.INSTANCE_ID, INSTANCE_ID);
        return HttpClientUtil.sendJsonPut(URL, path, McsManageConstant.HEADERS, query, body);
    }

    /**
     * DELETE请求 - form提交
     */
    public static HttpResponse sendDelete(String path, Map<String, Object> query, Map<String, Object> body) {
        query.put(McsManageConstant.API_KEY, API_KEY);
        query.put(McsManageConstant.DAEMON_ID, DAEMON_ID);
        query.put(McsManageConstant.INSTANCE_ID, INSTANCE_ID);
        return HttpClientUtil.sendJsonDelete(URL, path, McsManageConstant.HEADERS, query, body);
    }
}
