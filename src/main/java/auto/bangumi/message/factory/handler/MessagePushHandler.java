package auto.bangumi.message.factory.handler;

import auto.bangumi.admin.model.UserConfig;
import auto.bangumi.common.context.AbstractSparrowAnnotationBeanMap;
import auto.bangumi.common.utils.ConfigCatch;
import auto.bangumi.common.utils.SpringContextUtil;
import auto.bangumi.message.enums.MessagePushType;
import auto.bangumi.message.factory.annotation.MessagePushMethod;
import auto.bangumi.message.factory.service.MessagePusher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Message push handler.
 *
 * @author sakura
 */
@Slf4j
@Component
public class MessagePushHandler extends AbstractSparrowAnnotationBeanMap<MessagePushMethod, MessagePusher> {

    private static final Map<MessagePushType, MessagePusher> PUSHER_MAP = new HashMap<>();

    public static void pushDownloadCompleted(String episodeName) {
        String name = StringUtils.defaultIfBlank(episodeName, "番剧");
        String message = String.format("📥 番剧下载完成%n%s ", name);
        pushMessage(message, true);
    }

    public static boolean pushTestMessage() {
        return pushMessage("🔔 Auto Bangumi 测试推送\n消息推送配置已生效。", false);
    }

    private static boolean pushMessage(String message, boolean requireEnabled) {
        UserConfig.MessageConfig messageConfig = getMessageConfig();
        if (requireEnabled && !isEnabled(messageConfig)) {
            return false;
        }

        MessagePushType pushType = getPushType(messageConfig);
        MessagePusher pusher = PUSHER_MAP.get(pushType);
        if (pusher == null) {
            log.warn("Message pusher not found: {}", pushType);
            return false;
        }

        try {
            return pusher.push(message);
        } catch (Exception e) {
            log.warn("Push message failed: {}", e.getMessage(), e);
            return false;
        }
    }

    private static boolean isEnabled(UserConfig.MessageConfig messageConfig) {
        return messageConfig != null && Boolean.TRUE.equals(messageConfig.getEnabled());
    }

    private static MessagePushType getPushType(UserConfig.MessageConfig messageConfig) {
        if (messageConfig == null || messageConfig.getPushType() == null) {
            return MessagePushType.OPEN_CLAW;
        }
        return messageConfig.getPushType();
    }

    private static UserConfig.MessageConfig getMessageConfig() {
        try {
            ConfigCatch configCatch = SpringContextUtil.getBean(ConfigCatch.class);
            UserConfig config = configCatch.findConfig();
            return Objects.nonNull(config) ? config.getMessageConfig() : null;
        } catch (Exception e) {
            log.debug("Get message config failed: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Class<MessagePushMethod> getAnnotation() {
        return MessagePushMethod.class;
    }

    @Override
    public void refresh(Map<MessagePushMethod, MessagePusher> annotationBeanMap) {
        annotationBeanMap.forEach((annotation, pusher) -> {
            MessagePushType type = annotation.type();
            log.info("Register message pusher: {} -> {}", type, pusher.getClass().getSimpleName());
            PUSHER_MAP.put(type, pusher);
        });
    }
}
