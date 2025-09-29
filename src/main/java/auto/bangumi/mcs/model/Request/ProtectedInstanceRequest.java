package auto.bangumi.mcs.model.Request;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 启动实例 停止实例 重启实例 获取输出
 *
 * @author 查查
 * @since 2025/9/21
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtectedInstanceRequest {

    /**
     * 获取的日志大小: 1KB ~ 2048KB 如果未设置，则返回所有日志
     */
    private Integer size;

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("size", this.getSize());
        return result;
    }
}
