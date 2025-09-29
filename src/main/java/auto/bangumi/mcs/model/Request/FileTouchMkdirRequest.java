package auto.bangumi.mcs.model.Request;

import auto.bangumi.common.valid.Add;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 新建文件 或 文件夹
 *
 * @author 查查
 * @since 2025/9/23
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileTouchMkdirRequest {

    @NotBlank(message = "不能为空", groups = Add.class)
    private String target;

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("target", this.getTarget());
        return result;
    }
}
