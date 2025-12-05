package auto.bangumi.common.runner;

import auto.bangumi.admin.service.IUserService;
import auto.bangumi.qBittorrent.service.QBittorrentApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AutoBangumiStartedUpRunner implements ApplicationRunner {

    @Resource
    private IUserService iUserService;

    @Resource
    private ConfigurableApplicationContext context;

    @Resource
    private QBittorrentApi qBittorrentApi;

    @Override
    public void run(ApplicationArguments args) {

        initSysCache();

        if (context.isActive()) {
            log.info("   _____          __        __________                                     .__ ");
            log.info("  /  _  \\  __ ___/  |_  ____\\______   \\_____    ____    ____  __ __  _____ |__|");
            log.info(" /  /_\\  \\|  |  \\   __\\/  _ \\|    |  _/\\__  \\  /    \\  / ___\\|  |  \\/     \\|  |");
            log.info("/    |    \\  |  /|  | (  <_> )    |   \\ / __ \\|   |  \\/ /_/  >  |  /  Y Y  \\  |");
            log.info("\\____|__  /____/ |__|  \\____/|______  /(____  /___|  /\\___  /|____/|__|_|  /__|");
            log.info("        \\/                          \\/      \\/     \\//_____/             \\/    ");
            log.info("AutoBangumi启动完毕");
        }

        if (args.containsOption("reload")) {
            log.info("检测到 --reload 参数，执行重置用户逻辑...");
            resetUser();
        }
    }

    private void resetUser() {
        iUserService.reloadUserInfo();
        log.info("用户已重置完成！");
    }

    private void initSysCache() {
        qBittorrentApi.CreateCategory();
        log.info("系统缓存初始化完成");
    }
}
