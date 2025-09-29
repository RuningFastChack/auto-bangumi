package auto.bangumi.mcs.model.Request;

import auto.bangumi.common.valid.Update;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 复制文件
 *
 * @author 查查
 * @since 2025/9/23
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileCopyRequest {

    @NotNull(message = "请选择文件", groups = Update.class)
    private List<List<String>> targets;

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("targets", this.getTargets());
        return result;
    }
}
