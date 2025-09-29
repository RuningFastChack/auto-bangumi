package auto.bangumi.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CopyOnWriteArrayList;

@Data
@Configuration
@ConfigurationProperties(prefix = "auto-bangumi.cors")
public class AutoBangumiProperties {

    private CopyOnWriteArrayList<String> allowedOrigins;
}
