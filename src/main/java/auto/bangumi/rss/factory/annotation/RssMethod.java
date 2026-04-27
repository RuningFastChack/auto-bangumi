package auto.bangumi.rss.factory.annotation;

import auto.bangumi.common.enums.RssTypeEnum;

import java.lang.annotation.*;

/**
 * RSS订阅类型
 *
 * @author sakura
 * @version 1.0
 * @since 2026/04/27
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RssMethod {
    RssTypeEnum method();
}
