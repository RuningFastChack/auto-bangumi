package auto.bangumi.common.utils;

import auto.bangumi.admin.model.UserConfig;
import auto.bangumi.admin.model.entity.User;
import auto.bangumi.admin.service.IUserService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * ConfigCatch
 *
 * @author sakura
 */
@Slf4j
public abstract class ConfigCatch {
    private static UserConfig config = new UserConfig();

    /**
     * 查询
     *
     * @return
     */
    public static UserConfig findConfig() {
        return config;
    }

    /**
     * 重载
     */
    public static void reloadConfig() {
        IUserService iUserService = SpringContextUtil.getBean(IUserService.class);
        User selected = iUserService.list().stream().findFirst().orElse(new User());
        if (StringUtils.isNotBlank(selected.getConfig())) {
            config = JSON.parseObject(selected.getConfig(), UserConfig.class);
        }
    }
}
