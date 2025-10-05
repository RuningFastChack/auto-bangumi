package auto.bangumi.qBittorrent.model.Request;

import auto.bangumi.common.valid.Add;
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
 * 分类设置
 *
 * @author 查查
 * @since 2025/10/3
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriesRequest {

    /**
     * 分类名称
     */
    @NotBlank(message = "不能为空",groups = {Update.class, Add.class})
    private String category;

    /**
     * 保存路径
     */
    private String savePath;

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("category", this.getCategory());
        result.put("savePath", this.getSavePath());
        return result;
    }
}
