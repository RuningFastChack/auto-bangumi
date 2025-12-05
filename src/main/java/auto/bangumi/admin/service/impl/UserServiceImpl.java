package auto.bangumi.admin.service.impl;

import auto.bangumi.admin.mapper.UserMapper;
import auto.bangumi.admin.model.UserConfig;
import auto.bangumi.admin.model.dto.LoginDTO;
import auto.bangumi.admin.model.entity.User;
import auto.bangumi.admin.model.vo.UserVO;
import auto.bangumi.admin.service.IUserService;
import auto.bangumi.common.authentication.utils.LoginUtils;
import auto.bangumi.common.enums.CommonResponseEnum;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.crypto.digest.BCrypt;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    /**
     * 查询用户
     *
     * @param id
     * @return
     */
    @Override
    public UserVO findUserDetailById(Integer id) {
        User user = baseMapper.selectById(id);
        CommonResponseEnum.BAD_USERNAME_OR_PASSWORD.assertNull(user);
        UserConfig parsed = JSON.parseObject(user.getConfig(), UserConfig.class);
        UserVO build = UserVO.builder().build();
        BeanUtil.copyProperties(user, build, CopyOptions.create().setIgnoreProperties("config"));
        build.setConfig(parsed);
        return build;
    }

    /**
     * 获取用户配置
     *
     * @return
     */
    @Override
    public UserConfig findUserConfig() {
        List<User> selected = baseMapper.selectList(new LambdaQueryWrapper<>());
        String config = selected.get(0).getConfig();
        return StringUtils.isNotBlank(config) ? JSON.parseObject(config, UserConfig.class) : UserConfig.builder().build();
    }

    /**
     * 修改登录账号
     *
     * @param dto
     */
    @Override
    public void updateLoginInfo(LoginDTO dto) {
        User build = User.builder()
                .id(1)
                .username(dto.getUsername())
                .password(BCrypt.hashpw(dto.getUsername() + dto.getPassword(), BCrypt.gensalt(10)))
                .build();
        baseMapper.updateById(build);
    }

    /**
     * 修改配置
     *
     * @param config
     */
    @Override
    public void updateUserConfig(UserConfig config) {
        Integer userId = Objects.requireNonNull(LoginUtils.getLoginUser()).getUser().getId();
        User build = User.builder()
                .id(userId)
                .config(JSON.toJSONString(config))
                .build();
        baseMapper.updateById(build);
    }

    /**
     * 重置账户
     */
    @Override
    public void reloadUserInfo() {
        User admin = User.builder()
                .id(1)
                .username("admin")
                .password(BCrypt.hashpw("adminadminadmin", BCrypt.gensalt(10)))
                .build();
        boolean reloaded = baseMapper.insertOrUpdate(admin);
        if (reloaded) {
            log.info("重置账号成功");
        }
    }
}
