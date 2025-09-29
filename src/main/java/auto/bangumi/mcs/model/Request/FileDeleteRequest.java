package auto.bangumi.mcs.model.Request;


import auto.bangumi.common.valid.Delete;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 删除文件
 *
 * @author 查查
 * @since 2025/9/23
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDeleteRequest {

    @NotNull(message = "不能为空", groups = Delete.class)
    private List<String> targets;

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("targets", this.getTargets());
        return result;
    }
}
