package auto.bangumi.admin.service;

import auto.bangumi.admin.model.dto.LoginDTO;
import auto.bangumi.admin.model.vo.UserVO;
import auto.bangumi.common.model.vo.LoginVO;

/**
 * 登录服务
 *
 * @author sakura
 */
public interface IAuthService {

    LoginVO Login(LoginDTO dto);

    void kickOut(Long userId);
}
