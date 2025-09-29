package auto.bangumi.common.runner;

import auto.bangumi.admin.service.IUserService;
import auto.bangumi.common.utils.ConfigCatch;
import auto.bangumi.qBittorrent.service.QBittorrentApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
@Component
public class AutoBangumiStartedUpRunner implements ApplicationRunner {

    @Resource
    private IUserService iUserService;

    @Resource
    private ConfigurableApplicationContext context;

    @Override
    public void run(ApplicationArguments args) {

        initDB();

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

    private void initDB() {
        try {
            // 获取项目根目录（pom.xml 同级目录）
            String projectDir = System.getProperty("user.dir");

            // 在项目根目录下创建 db 文件夹
            File dbDir = new File(projectDir, "db");
            if (!dbDir.exists()) {
                dbDir.mkdirs(); // 如果db文件夹不存在，创建它
            }

            File dbFile = new File(dbDir, "app.db");
            if (!dbFile.exists()) {
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath())) {
                    log.info("SQLite 数据库文件已创建: {}", dbFile.getAbsolutePath());
                }
            } else {
                log.info("SQLite 数据库文件已存在: {}", dbFile.getAbsolutePath());
            }
        } catch (SQLException e) {
            log.error("SQLite 数据库文件创建失败: {}", e.getMessage(), e);
        }
    }

    private void initSysCache() {
        ConfigCatch.reloadConfig();
        QBittorrentApi.CreateCategory();
        log.info("系统缓存初始化完成");
    }
}
