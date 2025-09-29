package auto.bangumi.mcs.constant;

import java.util.Map;

/**
 * McsManage常量
 *
 * @author 查查
 * @since 2025/9/21
 */
public interface McsManageConstant {

    String SUCCESS = "200";

    String API_KEY = "apikey";

    String DAEMON_ID = "daemonId";

    String INSTANCE_ID = "uuid";

    Map<String, Object> HEADERS = Map.of("Content-Type", "application/json; charset=utf-8", "X-Requested-With", "XMLHttpRequest");
}
