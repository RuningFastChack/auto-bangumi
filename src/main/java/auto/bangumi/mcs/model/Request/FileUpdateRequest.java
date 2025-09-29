package auto.bangumi.mcs.model.Request;

import auto.bangumi.common.valid.Query;
import auto.bangumi.common.valid.Update;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * 编辑或查询文件
 *
 * @author 查查
 * @since 2025/9/23
 */
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FileUpdateRequest {

    @NotBlank(message = "不能为空", groups = {Query.class, Update.class})
    private String target;

    @NotBlank(message = "不能为空", groups = Update.class)
    private String text;


    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("target", this.getTarget());
        result.put("text", this.getText());
        return result;
    }
}
