package auto.bangumi.mcs.model.Request;

import auto.bangumi.common.valid.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * 更改文件权限
 *
 * @author 查查
 * @since 2025/9/23
 */
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FileChangePermissionRequest {

    @NotNull(message = "不能为空", groups = {Update.class})
    private Integer chmod;

    private Boolean deep;

    @NotBlank(message = "不能为空", groups = {Update.class})
    private String target;

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("chmod", this.getChmod());
        result.put("deep", this.getDeep());
        result.put("target", this.getTarget());
        return result;
    }
}
