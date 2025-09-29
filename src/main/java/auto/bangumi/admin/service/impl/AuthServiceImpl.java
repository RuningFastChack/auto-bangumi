package auto.bangumi.admin.service.impl;

import auto.bangumi.admin.model.UserConfig;
import auto.bangumi.admin.model.dto.LoginDTO;
import auto.bangumi.admin.model.entity.User;
import auto.bangumi.admin.model.vo.UserVO;
import auto.bangumi.admin.service.IAuthService;
import auto.bangumi.admin.service.IUserService;
import auto.bangumi.common.authentication.utils.LoginUtils;
import auto.bangumi.common.enums.CommonResponseEnum;
import auto.bangumi.common.model.vo.LoginVO;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.crypto.digest.BCrypt;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * AuthServiceImpl
 *
 * @author sakura
 */
@Slf4j
@Service
public class AuthServiceImpl implements IAuthService {
    @Resource
    private IUserService iUserService;

    @Override
    public LoginVO Login(LoginDTO dto) {

        User selectedOne = iUserService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));

        CommonResponseEnum.BAD_USERNAME_OR_PASSWORD.assertNull(selectedOne);

        boolean checkpwed = BCrypt.checkpw(dto.getUsername() + dto.getPassword(), selectedOne.getPassword());

        CommonResponseEnum.BAD_USERNAME_OR_PASSWORD.assertFalse(checkpwed);

        UserVO build = UserVO.builder().build();

        BeanUtil.copyProperties(selectedOne, build, CopyOptions.create().setIgnoreProperties("config"));

        build.setConfig(StringUtils.isNotBlank(selectedOne.getConfig()) ?
                JSON.parseObject(selectedOne.getConfig(), UserConfig.class) : new UserConfig());

        SaLoginParameter model = new SaLoginParameter();

        model.setTimeout(604800);

        model.setActiveTimeout(86400);

        LoginVO loginVo = LoginVO.builder().user(build).build();

        LoginUtils.performLogin(loginVo, model, Map.of("userId", build.getId()));

        loginVo.setToken(StpUtil.getTokenValue());

        loginVo.setExpire(StpUtil.getTokenTimeout());

        return loginVo;
    }

    @Override
    public void kickOut(Long userId) {
        StpUtil.logout(userId);
    }
}
