package auto.bangumi.qBittorrent.utils;

import auto.bangumi.admin.model.UserConfig;
import auto.bangumi.common.utils.ConfigCatch;
import auto.bangumi.common.utils.HttpClientUtil;
import auto.bangumi.qBittorrent.constant.QBittorrentPathConstant;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * QB请求
 *
 * @author 查查
 * @since 2025/10/4
 */
@Slf4j
public abstract class QBHttpUtil {

    private static String SID = "";
    private static String URL = "";

    static {
        UserConfig.DownLoadSetting downLoadSetting = ConfigCatch.findConfig().getDownLoadSetting();
        URL = downLoadSetting.getUrl();
        LoginQBittorrent(downLoadSetting.getUsername(), downLoadSetting.getPassword());
    }

    /**
     * 登录账号
     */
    private static void LoginQBittorrent(String username, String password) {
        HttpResponse httpResponse = HttpClientUtil.sendFormPost(URL, QBittorrentPathConstant.LOGIN,
                Map.of("Referer", StrUtil.format("{}{}", URL, QBittorrentPathConstant.LOGIN)),
                new HashMap<>(),
                Map.of("username", username, "password", password));
        if (httpResponse != null) {
            int status = httpResponse.getStatusLine().getStatusCode();
            if (status == 200) {
                for (Header header : httpResponse.getAllHeaders()) {
                    for (HeaderElement element : header.getElements()) {
                        boolean HAS_SID = element.getName().equals("SID");
                        if (HAS_SID) {
                            SID = element.getValue();
                        }
                    }
                }
            } else {
                log.error("QBittorrent登录失败，状态码：{}", status);
            }
        }
    }

    public static String sendGet(String path, Map<String, Object> params) {
        return HttpClientUtil.sendGet(URL + path, params, Map.of("Cookie", StrUtil.format("SID={}", SID)));
    }

    /**
     * POST请求 - json提交
     */
    public static HttpResponse sendJSONPost(String path, Map<String, Object> query, Map<String, Object> body) {
        return HttpClientUtil.sendFormPost(URL, path, Map.of("Cookie", StrUtil.format("SID={}", SID)), query, body);
    }

    /**
     * PUT请求 - form提交
     */
    public static HttpResponse sendJSONPut(String path, Map<String, Object> query, Map<String, Object> body) {
        return HttpClientUtil.sendFormPut(URL, path, Map.of("Cookie", StrUtil.format("SID={}", SID)), query, body);
    }

    /**
     * DELETE请求 - form提交
     */
    public static HttpResponse sendDelete(String path, Map<String, Object> query, Map<String, Object> body) {
        return HttpClientUtil.sendFormDelete(URL, path, Map.of("Cookie", StrUtil.format("SID={}", SID)), query, body);
    }

}
