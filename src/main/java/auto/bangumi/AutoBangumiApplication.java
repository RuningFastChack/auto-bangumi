package auto.bangumi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AutoBangumiApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutoBangumiApplication.class, args);
    }
}
