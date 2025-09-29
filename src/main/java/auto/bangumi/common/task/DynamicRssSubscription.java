package auto.bangumi.common.task;

import auto.bangumi.admin.model.UserConfig;
import auto.bangumi.common.utils.ConfigCatch;
import auto.bangumi.rss.service.IUnifiedRssService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Instant;
import java.util.Objects;

/**
 * RSS订阅刷新
 */
@Configuration
@EnableScheduling
public class DynamicRssSubscription implements SchedulingConfigurer {

    @Resource
    private IUnifiedRssService iUnifiedRssService;

    @Value("${spring.profiles.active}")
    private String active;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(
                // 要执行的任务
                () -> {
                    if (Objects.equals(active, "dev")) {
                        return;
                    }

                    UserConfig config = ConfigCatch.findConfig();

                    if (Objects.isNull(config)) {
                        return;
                    }

                    if (Objects.nonNull(config.getGeneralSetting()) && config.getGeneralSetting().getEnable()) {
                        iUnifiedRssService.pollingRssManage();
                    }
                },
                // 动态触发器
                triggerContext -> {
                    UserConfig config = ConfigCatch.findConfig();
                    // 默认 10 分钟
                    long interval = 600L;
                    if (Objects.nonNull(config) &&
                            Objects.nonNull(config.getGeneralSetting()) &&
                            config.getGeneralSetting().getRssTimeOut() != null) {
                        interval = config.getGeneralSetting().getRssTimeOut();
                    }
                    Instant instant = triggerContext.lastCompletion();
                    return (instant == null ? Instant.now() : instant.plusSeconds(interval));
                }
        );
    }
}
