package auto.bangumi.mcs.model.Request;

import auto.bangumi.common.model.dto.PageQuery;
import auto.bangumi.common.valid.Query;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取文库列表
 *
 * @author 查查
 * @since 2025/9/23
 */
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FileManageListRequest extends PageQuery {

    /**
     * 文件（名称或目录）路径
     */
    @NotBlank(message = "不能为空", groups = Query.class)
    private String target;

    private String filename;

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("target", this.getTarget());
        result.put("file_name", this.getFilename());
        result.put("page", this.getPage());
        result.put("page_size", this.getLimit());
        return result;
    }
}
