package auto.bangumi.admin.controller.auth;

import auto.bangumi.admin.model.dto.LoginDTO;
import auto.bangumi.admin.service.IAuthService;
import auto.bangumi.common.model.dto.ApiResult;
import auto.bangumi.common.model.vo.LoginVO;
import auto.bangumi.common.valid.Query;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("v1/auth")
public class LoginController {

    @Resource
    private IAuthService iAuthService;

    @SaIgnore
    @PostMapping("login")
    public ApiResult<LoginVO> Login(@RequestBody @Validated(Query.class) LoginDTO dto) {
        return ApiResult.success(iAuthService.Login(dto));
    }

    @PostMapping("logout")
    public ApiResult<Void> LogOut() {
        StpUtil.getTokenSession().logout();
        StpUtil.logout();
        return ApiResult.success();
    }
}
