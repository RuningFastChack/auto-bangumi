package auto.bangumi.common.shutdown;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AutoBanguiShutDownHook {

    @EventListener(classes = {ContextClosedEvent.class})
    public void onFebsApplicationClosed(@NonNull ApplicationEvent event) {
        log.info("AutoBangumi已关闭，Bye");
    }
}
