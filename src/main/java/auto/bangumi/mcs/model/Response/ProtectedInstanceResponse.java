package auto.bangumi.mcs.model.Response;

import lombok.*;

/**
 * 启动实例 停止实例 重启实例
 *
 * @author 查查
 * @since 2025/9/21
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtectedInstanceResponse {
    private String instanceUuid;
}
