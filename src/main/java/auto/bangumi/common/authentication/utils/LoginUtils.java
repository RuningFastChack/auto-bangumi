package auto.bangumi.common.authentication.utils;

import auto.bangumi.common.model.vo.LoginVO;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * LoginUtils
 *
 * @author sakura
 */
@Slf4j
public class LoginUtils {

    private LoginUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static final String USER_KEY = "loginUser";

    public static void performLogin(LoginVO loginUser, SaLoginParameter model, Map<String, Object> extraData) {
        model = ObjectUtil.defaultIfNull(model, new SaLoginParameter());
        model.setExtraData(extraData);
        // 登录，生成token
        StpUtil.login(loginUser.getUser().getId(), model);
        StpUtil.getTokenSession().set(USER_KEY, loginUser);
        // 使用全局配置而不使用model独立配置时间的问题
        StpUtil.getTokenSession().updateTimeout(model.getTimeout());
        StpUtil.getSession().updateTimeout(model.getTimeout());
    }

    public static void performMiniLogin(Object userId, Object loginUser, SaLoginParameter model, Map<String, Object> extraData) {
        model = ObjectUtil.defaultIfNull(model, new SaLoginParameter());
        model.setExtraData(extraData);
        // 登录，生成token
        StpUtil.login(userId, model);
        StpUtil.getTokenSession().set(USER_KEY, loginUser);
        // 使用全局配置而不使用model独立配置时间的问题
        StpUtil.getTokenSession().updateTimeout(model.getTimeout());
        StpUtil.getSession().updateTimeout(model.getTimeout());
    }

    /**
     * 获取用户
     */
    public static LoginVO getLoginUser() {
        SaSession session = StpUtil.getTokenSession();
        if (ObjectUtil.isNull(session)) {
            return null;
        }
        return (LoginVO) session.get(USER_KEY);
    }

    /**
     * 根据token获取用户信息
     *
     * @param token token
     * @return 用户信息
     */
    public static LoginVO getLoginUser(String token) {
        SaSession session = StpUtil.getTokenSessionByToken(token);
        if (ObjectUtil.isNull(session)) {
            return null;
        }
        return (LoginVO) session.get(USER_KEY);
    }
}
