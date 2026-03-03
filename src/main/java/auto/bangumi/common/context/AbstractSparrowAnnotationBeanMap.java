package auto.bangumi.common.context;

import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * 根据注解来封装需要的Bean
 *
 * @author sakura
 * @version 1.0
 * @since 2026/03/03
 */
public abstract class AbstractSparrowAnnotationBeanMap<A extends Annotation, B> extends AbstractSparrowContext {

    /**
     * 需要缓存的注解类型
     *
     * @return 注解类型
     */
    public abstract Class<A> getAnnotation();

    /**
     * 暴露给实现类去操作刷新earlyBeans的接口
     *
     * @param annotationBeanMap 注解Bean映射
     */
    public abstract void refresh(Map<A, B> annotationBeanMap);

    @Override
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() {
        Map<A, B> annotationBeanMap = new HashMap<>();
        Map<String, Object> beanMap = getBeanMapByAnnotation(getAnnotation());
        beanMap.values().forEach(bean -> {
            A annotation = AnnotationUtils.findAnnotation(bean.getClass(), getAnnotation());
            annotationBeanMap.put(annotation, (B) bean);
        });
        refresh(annotationBeanMap);
    }
}
