package auto.bangumi.message.factory.strategy;

import auto.bangumi.admin.model.UserConfig;
import auto.bangumi.common.utils.ConfigCatch;
import auto.bangumi.common.utils.SpringContextUtil;
import auto.bangumi.message.enums.MessagePushType;
import auto.bangumi.message.enums.OpenClawAuthType;
import auto.bangumi.message.factory.annotation.MessagePushMethod;
import auto.bangumi.message.factory.service.MessagePusher;
import auto.bangumi.message.model.OpenClawMessageConfig;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * OpenClaw message pusher.
 *
 * @author sakura
 */
@Slf4j
@Component
@MessagePushMethod(type = MessagePushType.OPEN_CLAW)
public class OpenClawMessagePusher implements MessagePusher {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    @Override
    public boolean push(String message) {
        OpenClawMessageConfig config = getOpenClawConfig();
        if (!isValid(config)) {
            log.warn("OpenClaw message config is incomplete");
            return false;
        }

        Map<String, Object> args = new LinkedHashMap<>();
        args.put("sessionKey", config.getSessionKey());
        args.put("message", message);
        Object delivery = parseDelivery(config.getDelivery());
        if (delivery != null) {
            args.put("delivery", delivery);
        } else {
            log.debug("OpenClaw delivery is empty. Message may stay in the session instead of being delivered to WeChat.");
        }

        Map<String, Object> body = Map.of("tool", "sessions_send", "args", args);

        try {
            String credential = getCredential(config);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getGatewayUrl()))
                    .header("Authorization", buildAuthorization(credential))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(body)))
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() >= 200 && response.statusCode() < 300;
            if (!success) {
                log.warn("OpenClaw message push failed, status: {}, body: {}", response.statusCode(), response.body());
            }
            return success;
        } catch (Exception e) {
            log.warn("OpenClaw message push failed: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean isValid(OpenClawMessageConfig config) {
        return config != null
                && StringUtils.isNotBlank(config.getGatewayUrl())
                && StringUtils.isNotBlank(getCredential(config))
                && StringUtils.isNotBlank(config.getSessionKey());
    }

    private String getCredential(OpenClawMessageConfig config) {
        if (config == null) {
            return null;
        }
        if (OpenClawAuthType.PASSWORD.equals(config.getAuthType())) {
            return config.getPassword();
        }
        if (OpenClawAuthType.TOKEN.equals(config.getAuthType())) {
            return config.getToken();
        }
        return StringUtils.isNotBlank(config.getToken()) ? config.getToken() : config.getPassword();
    }

    private String buildAuthorization(String token) {
        if (hasBearerPrefix(token)) {
            return token;
        }
        return "Bearer " + token;
    }

    private boolean hasBearerPrefix(String value) {
        return value != null && value.regionMatches(true, 0, "Bearer ", 0, "Bearer ".length());
    }

    private OpenClawMessageConfig getOpenClawConfig() {
        try {
            ConfigCatch configCatch = SpringContextUtil.getBean(ConfigCatch.class);
            UserConfig config = configCatch.findConfig();
            UserConfig.MessageConfig messageConfig = Objects.nonNull(config) ? config.getMessageConfig() : null;
            Object rawConfig = Objects.nonNull(messageConfig) ? messageConfig.getConfig() : null;
            if (rawConfig == null) {
                return null;
            }
            if (rawConfig instanceof OpenClawMessageConfig openClawConfig) {
                return openClawConfig;
            }
            OpenClawMessageConfig openClawConfig = JSON.parseObject(JSON.toJSONString(rawConfig), OpenClawMessageConfig.class);
            if (rawConfig instanceof Map<?, ?> rawMap) {
                if (StringUtils.isBlank(openClawConfig.getGatewayUrl())) {
                    openClawConfig.setGatewayUrl(toString(rawMap.get("apiUrl")));
                }
                if (StringUtils.isBlank(openClawConfig.getToken())) {
                    openClawConfig.setToken(formatLegacyToken(toString(rawMap.get("authorization"))));
                }
                if (openClawConfig.getAuthType() == null) {
                    openClawConfig.setAuthType(inferAuthType(openClawConfig));
                }
            }
            return openClawConfig;
        } catch (Exception e) {
            log.debug("Get OpenClaw message config failed: {}", e.getMessage());
            return null;
        }
    }

    private Object parseDelivery(Object delivery) {
        if (delivery == null) {
            return null;
        }
        if (delivery instanceof CharSequence text) {
            String deliveryText = text.toString();
            if (StringUtils.isBlank(deliveryText)) {
                return null;
            }
            try {
                return JSON.parse(deliveryText);
            } catch (Exception e) {
                log.warn("OpenClaw delivery config is not valid JSON: {}", e.getMessage());
                return null;
            }
        }
        return delivery;
    }

    private String toString(Object value) {
        return value == null ? null : value.toString();
    }

    private OpenClawAuthType inferAuthType(OpenClawMessageConfig config) {
        return StringUtils.isNotBlank(config.getPassword()) ? OpenClawAuthType.PASSWORD : OpenClawAuthType.TOKEN;
    }

    private String formatLegacyToken(String authorization) {
        if (StringUtils.isBlank(authorization)) {
            return authorization;
        }
        if (hasBearerPrefix(authorization)) {
            return authorization.substring("Bearer ".length());
        }
        return authorization;
    }
}
