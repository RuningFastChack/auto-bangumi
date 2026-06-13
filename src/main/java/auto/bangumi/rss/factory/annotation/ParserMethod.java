package auto.bangumi.rss.factory.annotation;

import auto.bangumi.common.enums.AiModelEnum;

import java.lang.annotation.*;

/**
 * 标题解析器注解，标记策略实现对应的 AI 模型
 *
 * @author sakura
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParserMethod {
    AiModelEnum model();
}
