package auto.bangumi.admin.service;

import auto.bangumi.admin.model.UserConfig;
import auto.bangumi.admin.model.dto.LoginDTO;
import auto.bangumi.admin.model.entity.User;
import auto.bangumi.admin.model.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IUserService extends IService<User> {

    /**
     * 查询用户
     *
     * @param id
     * @return
     */
    UserVO findUserDetailById(Integer id);

    /**
     * 获取用户配置
     *
     * @return
     */
    UserConfig findUserConfig();

    /**
     * 修改登录账号
     *
     * @param dto
     */
    void updateLoginInfo(LoginDTO dto);

    /**
     * 修改配置
     *
     * @param config
     */
    void updateUserConfig(UserConfig config);

    /**
     * 重置账户
     */
    void reloadUserInfo();
}
