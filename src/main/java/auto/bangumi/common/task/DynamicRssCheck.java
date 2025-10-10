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
 * 检查RSS下载情况、检查番剧是否完结
 *
 * @author 查查
 * @since 2025/9/20
 */
@Configuration
@EnableScheduling
public class DynamicRssCheck implements SchedulingConfigurer {

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
                        iUnifiedRssService.pollingCheckRssItem();
                    }

                    //检查番剧是否完结
                    iUnifiedRssService.pollingCheckRssManageComplete();
                },
                // 动态触发器
                triggerContext -> {
                    // 默认 10 分钟
                    long interval = 600L;
                    Instant instant = triggerContext.lastCompletion();
                    return (instant == null ? Instant.now() : instant.plusSeconds(interval));
                }
        );
    }
}
