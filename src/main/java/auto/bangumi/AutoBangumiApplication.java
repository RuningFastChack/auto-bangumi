package auto.bangumi;

import auto.bangumi.common.utils.DateUtil;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AutoBangumiApplication {
    @Getter
    private static String name;
    @Getter
    private static String version;
    @Getter
    private static String buildTimeStatic;

    @Value("${app.name}")
    private String appName;
    @Value("${app.version}")
    private String appVersion;
    @Value("${app.build-time}")
    private String buildTime;

    private static void setAppInfo(String name, String version, String buildTime) {
        AutoBangumiApplication.name = name;
        AutoBangumiApplication.version = version;
        AutoBangumiApplication.buildTimeStatic = DateUtil.getDateFormat(DateUtil.parseDate(buildTime), DateUtil.TIME_SPLIT_PATTERN);
    }

    @PostConstruct
    public void init() {
        setAppInfo(appName, appVersion, buildTime);
    }

    public static void main(String[] args) {
        SpringApplication.run(AutoBangumiApplication.class, args);
    }
}
