package auto.bangumi.qBittorrent.model.Request;

import auto.bangumi.common.valid.Delete;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 删除种子
 *
 * @author 查查
 * @since 2025/10/4
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TorrentsInfoDeleteRequest {

    @NotNull(message = "不能为空", groups = Delete.class)
    private List<String> hashes;

    private Boolean deleteFiles;

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        if (Objects.isNull(this.getDeleteFiles())) {
            result.put("deleteFiles", false);
        } else {
            result.put("deleteFiles", true);
        }
        if (Objects.nonNull(this.getHashes()) && !this.getHashes().isEmpty()) {
            result.put("hashes", String.join("|", this.getHashes()));
        }
        return result;
    }
}
