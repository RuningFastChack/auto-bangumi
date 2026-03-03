package auto.bangumi.player.handler;

import auto.bangumi.common.context.AbstractSparrowAnnotationBeanMap;
import auto.bangumi.common.enums.PlayerEnums;
import auto.bangumi.player.annotation.PlayerMethod;
import auto.bangumi.player.service.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 播放器处理类
 *
 * @author sakura
 * @version 1.0
 * @since 2026/03/03
 */
@Slf4j
@Component
public class PlayerHandler extends AbstractSparrowAnnotationBeanMap<PlayerMethod, PlayerService> {
    private static final Map<PlayerEnums, PlayerService> MAP = new HashMap<>();

    /**
     * 需要缓存的注解类型
     *
     * @return 注解类型
     */
    @Override
    public Class<PlayerMethod> getAnnotation() {
        return PlayerMethod.class;
    }

    /**
     * 暴露给实现类去操作刷新earlyBeans的接口
     *
     * @param annotationBeanMap 注解Bean映射
     */
    @Override
    public void refresh(Map<PlayerMethod, PlayerService> annotationBeanMap) {
        annotationBeanMap.forEach((pay, payment) -> MAP.put(pay.method(), payment));
    }

    /**
     * 根据播放器枚举获取播放器服务
     * @param playerEnums 播放器枚举
     * @return 播放器服务
     */
    public PlayerService getPlayerService(PlayerEnums playerEnums) {
        return MAP.get(playerEnums);
    }
}