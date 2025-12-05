package auto.bangumi.common.utils;

import auto.bangumi.admin.model.UserConfig;
import auto.bangumi.admin.model.entity.User;
import auto.bangumi.admin.service.IUserService;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * ConfigCatch
 *
 * @author sakura
 */
@Slf4j
@Component
public class ConfigCatch {

    @Resource
    private IUserService iUserService;

    /**
     * 查询
     *
     * @return
     */
    public UserConfig findConfig() {
        User selected = iUserService.list().stream().findFirst().orElse(new User());
        boolean notBlank = StringUtils.isNotBlank(selected.getConfig());
        return notBlank ?
                JSON.parseObject(selected.getConfig(), UserConfig.class)
                : new UserConfig();
    }
}
