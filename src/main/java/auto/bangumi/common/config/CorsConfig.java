package auto.bangumi.common.config;

import auto.bangumi.common.properties.AutoBangumiProperties;
import jakarta.annotation.Resource;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * CorsConfig
 *
 * @author 查查
 * @since 2025/9/13
 */
@Configuration
public class CorsConfig {
    @Resource
    private AutoBangumiProperties autoBangumiProperties;

    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildConfig());
        return new CorsFilter(source);
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterFilterRegistrationBean() {
        FilterRegistrationBean<CorsFilter> registrationBean = new FilterRegistrationBean<>(corsFilter());
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }

    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 在生产环境中，推荐将 origin 设置为准确的域名，而不是使用 *
        CopyOnWriteArrayList<String> allowedOrigins = autoBangumiProperties.getAllowedOrigins();
        if (allowedOrigins.contains("*")) {
            corsConfiguration.addAllowedOrigin(CorsConfiguration.ALL);
        } else {
            for (String origin : allowedOrigins) {
                corsConfiguration.addAllowedOrigin(origin);
            }
        }
        corsConfiguration.addAllowedHeader(CorsConfiguration.ALL);
        corsConfiguration.addAllowedMethod(CorsConfiguration.ALL);
        corsConfiguration.addExposedHeader(CorsConfiguration.ALL);
        corsConfiguration.setMaxAge(3600L);
        return corsConfiguration;
    }
}
