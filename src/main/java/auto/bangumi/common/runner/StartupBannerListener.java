package auto.bangumi.common.runner;

import auto.bangumi.AutoBangumiApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * StartupBannerListener
 *
 * @author sakura
 * @version 1.0
 * @since 2026/04/27
 */
@Slf4j
@Component
public class StartupBannerListener {
    private final ApplicationContext context;

    public StartupBannerListener(ApplicationContext context) {
        this.context = context;
    }

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {

        long time = event.getTimeTaken().toMillis();

        String springBootVersion = SpringBootVersion.getVersion();

        // 获取当前 active profile
        String[] profiles = context.getEnvironment().getActiveProfiles();
        String activeProfiles = (profiles.length == 0) ? "default" : String.join(", ", profiles);

        String template = """
                
                 █████╗ ██╗   ██╗████████╗ ██████╗ ██████╗  █████╗ ███╗   ██╗ ██████╗ ██╗   ██╗███╗   ███╗██╗
                ██╔══██╗██║   ██║╚══██╔══╝██╔═══██╗██╔══██╗██╔══██╗████╗  ██║██╔════╝ ██║   ██║████╗ ████║██║
                ███████║██║   ██║   ██║   ██║   ██║██████╔╝███████║██╔██╗ ██║██║  ███╗██║   ██║██╔████╔██║██║
                ██╔══██║██║   ██║   ██║   ██║   ██║██╔══██╗██╔══██║██║╚██╗██║██║   ██║██║   ██║██║╚██╔╝██║██║
                ██║  ██║╚██████╔╝   ██║   ╚██████╔╝██████╔╝██║  ██║██║ ╚████║╚██████╔╝╚██████╔╝██║ ╚═╝ ██║███████╗
                ╚═╝  ╚═╝ ╚═════╝    ╚═╝    ╚═════╝ ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═══╝ ╚═════╝  ╚═════╝ ╚═╝     ╚═╝╚══════╝
                 ----------------------------------------------------
                 Application : %s
                 Version     : v%s
                 Build Time  : %s
                 Spring Boot : %s
                 Active Profiles: %s
                 Startup Time: %d ms
                 Server Port : %s
                 ----------------------------------------------------
                """;

        String banner = String.format(
                template,
                AutoBangumiApplication.getName(),
                AutoBangumiApplication.getVersion(),
                AutoBangumiApplication.getBuildTimeStatic(),
                springBootVersion,
                activeProfiles,
                time,
                context.getEnvironment().getProperty("server.port")
        );

        System.out.println(banner);
    }
}
