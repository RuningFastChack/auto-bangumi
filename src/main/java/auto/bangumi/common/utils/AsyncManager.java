package auto.bangumi.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * AsyncManager
 *
 * @author sakura
 */
@Slf4j
public class AsyncManager {
    private static final AsyncManager INSTANCE = new AsyncManager();

    private final ThreadPoolExecutor executor;

    private AsyncManager() {
        int core = Runtime.getRuntime().availableProcessors();
        this.executor = new ThreadPoolExecutor(
                core,
                core * 2,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                BasicThreadFactory.builder().namingPattern("async-task-%d").build(),
                new ThreadPoolExecutor.CallerRunsPolicy() // 兜底策略，防止任务丢失
        );
    }

    public static AsyncManager me() {
        return INSTANCE;
    }

    public void execute(Runnable task) {
        executor.execute(task);
    }

    public void shutdown() {
        log.info("AsyncManager shutdown...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
