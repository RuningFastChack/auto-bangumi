package auto.bangumi.mcs.model.Request;

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
 * 解压文件
 *
 * @author 查查
 * @since 2025/9/23
 */
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FileExtractRequest {

    /**
     * 1 压缩 2 解压
     */
    private String type;

    /**
     * 压缩 utf-8 解压 utf-8, gbk, big5
     */
    @NotBlank(message = "不能为空", groups = Update.class)
    private String code;

    /**
     * zip 文件路径
     */
    @NotBlank(message = "不能为空", groups = Update.class)
    private String source;

    /**
     * zip 文件路径
     */
    @NotBlank(message = "不能为空", groups = Update.class)
    private String targets;

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("type", this.getType());
        result.put("code", this.getCode());
        result.put("source", this.getSource());
        result.put("targets", this.getTargets());
        return result;
    }
}
