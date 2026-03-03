package auto.bangumi.common.context;

import jakarta.annotation.Nonnull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 操作spring容器的Bean
 *
 * @author sakura
 * @version 1.0
 * @since 2026/03/03
 */
public abstract class AbstractSparrowContext implements ApplicationContextAware, InitializingBean {
    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext context) throws BeansException {
        this.applicationContext = context;
    }

    /**
     * 获取spring容器中的对象
     *
     * @param targetClz 目标类
     * @param <T>       目标类类型
     * @return 目标类实例
     */
    @SuppressWarnings("unchecked")
    protected <T> T getBean(Class<T> targetClz) {
        T beanInstance = null;
        //byType
        try {
            beanInstance = applicationContext.getBean(targetClz);
        } catch (Exception ignored) {

        }
        //byName
        if (beanInstance == null) {
            String simpleName = targetClz.getSimpleName();
            simpleName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
            beanInstance = (T) applicationContext.getBean(simpleName);
        }
        return beanInstance;
    }

    /**
     * 获取注解的类
     *
     * @param annotationClz 注解类
     * @return 注解类的所有Bean
     */
    protected Map<String, Object> getBeanMapByAnnotation(Class<? extends Annotation> annotationClz) {
        return applicationContext.getBeansWithAnnotation(annotationClz);
    }
}
