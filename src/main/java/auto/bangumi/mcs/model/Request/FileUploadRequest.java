package auto.bangumi.mcs.model.Request;

import auto.bangumi.common.valid.Query;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 上传文件
 *
 * @author 查查
 * @since 2025/9/23
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRequest {

    @NotBlank(message = "不能为空", groups = Query.class)
    private String uploadDir;

    @NotBlank(message = "不能为空", groups = Query.class)
    private String filename;

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("upload_dir", this.getUploadDir());
        result.put("file_name", this.getFilename());
        return result;
    }
}
