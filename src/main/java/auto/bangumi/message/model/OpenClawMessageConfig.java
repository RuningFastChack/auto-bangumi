package auto.bangumi.message.model;

import auto.bangumi.message.enums.OpenClawAuthType;
import lombok.*;

/**
 * OpenClawMessageConfig
 *
 * @author sakura
 * @version 1.0
 * @since 2026/06/27
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OpenClawMessageConfig {

    private String gatewayUrl;

    private OpenClawAuthType authType;

    private String token;

    private String password;

    private String sessionKey;

    private Object delivery;
}
