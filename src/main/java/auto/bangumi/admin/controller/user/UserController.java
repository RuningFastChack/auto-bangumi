package auto.bangumi.admin.controller.user;

import auto.bangumi.admin.model.UserConfig;
import auto.bangumi.admin.model.dto.LoginDTO;
import auto.bangumi.admin.service.IUserService;
import auto.bangumi.common.model.dto.ApiResult;
import auto.bangumi.common.valid.Query;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("v1/user")
public class UserController {

    @Resource
    private IUserService iUserService;

    @GetMapping("config")
    public ApiResult<UserConfig> findUserConfig() {
        return ApiResult.success(iUserService.findUserConfig());
    }

    @PutMapping
    public ApiResult<Void> updateConfig(@RequestBody UserConfig config) {
        iUserService.updateUserConfig(config);
        return ApiResult.success();
    }

    @PostMapping("update/login/info")
    public ApiResult<Void> updateLoginInfo(@RequestBody @Validated(Query.class) LoginDTO dto) {
        iUserService.updateLoginInfo(dto);
        return ApiResult.success();
    }
}
