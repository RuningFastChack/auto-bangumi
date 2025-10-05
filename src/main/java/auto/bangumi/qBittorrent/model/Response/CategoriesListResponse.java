package auto.bangumi.qBittorrent.model.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 获取分类响应
 *
 * @author 查查
 * @since 2025/10/3
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriesListResponse {

    /**
     * 名称
     */
    @JsonProperty("name")
    private String name;

    /**
     * 保存路径
     */
    @JsonProperty("savePath")
    private String savePath;
}
