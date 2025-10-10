package auto.bangumi.common.DBInitializer;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.*;

/**
 * 数据库初始化
 *
 * @author sakura
 */
@Slf4j
@Component
public class DatabaseInitializer {

    private final DataSource dataSource;

    private final JdbcTemplate jdbcTemplate;

    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @Autowired
    public DatabaseInitializer(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String VERSION_TABLE = "schema_version";

    @PostConstruct
    public void init() {
        createVersionTableIfNeeded();
        try {
            applyMigrations();
        } catch (Exception e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * 创建版本控制表
     */
    private void createVersionTableIfNeeded() {
        jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS schema_version (
                        version TEXT PRIMARY KEY,
                        filename TEXT,
                        executed_at TEXT DEFAULT (datetime('now','localtime'))
                    )
                """);
    }

    private void applyMigrations() throws Exception {
        Resource[] resources = resolver.getResources("classpath:db/*.sql");

        // 已执行版本集合
        Set<String> executedVersions = new HashSet<>(jdbcTemplate.queryForList(
                "SELECT version FROM " + VERSION_TABLE, String.class));

        // 过滤、按前缀数字排序（符合 001_xxx.sql 规则）
        List<Resource> files = Arrays.stream(resources)
                .filter(r -> {
                    String name = r.getFilename();
                    return name != null && name.matches("^\\d+_.*\\.sql$");
                })
                .sorted(Comparator.comparingInt(r -> Integer.parseInt(Objects.requireNonNull(r.getFilename()).split("_")[0])))
                .toList();

        // 使用事务模板保证单个脚本执行的原子性（如果脚本失败则回滚）
        DataSourceTransactionManager txManager = new DataSourceTransactionManager(dataSource);
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);

        for (Resource resource : files) {
            String filename = Objects.requireNonNull(resource.getFilename());
            String version = filename.split("_")[0];

            if (executedVersions.contains(version))
                continue; // 跳过已执行

            log.info("▶ 执行: {}", filename);

            // 使用 ResourceDatabasePopulator 来正确解析并执行多条 SQL（支持注释、多行、事务语句等）
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.setSqlScriptEncoding("UTF-8");
            populator.setContinueOnError(false); // 出错时抛异常，触发回滚
            populator.addScript(resource);

            try {
                // 在事务中执行脚本：若脚本内部自己有 BEGIN/COMMIT，注意不要双重包裹（建议脚本不要包含事务语句）
                txTemplate.execute(status -> {
                    DatabasePopulatorUtils.execute(populator, dataSource);
                    return null;
                });

                // 成功后记录版本
                jdbcTemplate.update("INSERT INTO " + VERSION_TABLE + " (version, filename) VALUES (?, ?)",
                        version, filename);
                log.info("✔ 成功: {}", filename);
            } catch (Exception ex) {
                // 记录日志并抛出，启动失败时便能看到是哪个脚本失败
                log.error("✖ 失败: {}", filename);
                throw ex;
            }
        }
    }
}
