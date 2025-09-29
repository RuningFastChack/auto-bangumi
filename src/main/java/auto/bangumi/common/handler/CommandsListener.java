package auto.bangumi.common.handler;

import auto.bangumi.admin.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class CommandsListener implements CommandLineRunner {
    @Resource
    private IUserService iUserService;

    @Override
    public void run(String... args) throws Exception {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if ("reload".equals(line)) {
                    iUserService.reloadUserInfo();
                }
            }
        }, "Console-Command-Thread").start();
    }
}
