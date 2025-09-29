package auto.bangumi.common.authentication.configs;

import auto.bangumi.common.authentication.properties.WhitelistProperties;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;

/**
 * SaTokenConfig
 *
 * @author sakura
 */
@Slf4j
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Resource
    private WhitelistProperties whitelistProperties;


    @Bean
    public StpLogic getStpLogicJwt() {
        // Sa-Token 整合 jwt (简单模式)
        return new StpLogicJwtForSimple();
    }

    /**
     * 注册 Sa-Token 路由拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
                .addInterceptor(new SaInterceptor(handler -> SaRouter.match("/**", r -> StpUtil.checkLogin())))
                .addPathPatterns("/**")
                .excludePathPatterns(new ArrayList<>(whitelistProperties.getWhitelist()));
    }
}
