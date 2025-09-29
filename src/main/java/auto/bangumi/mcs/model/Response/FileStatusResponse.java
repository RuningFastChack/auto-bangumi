package auto.bangumi.mcs.model.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 获取文件状态
 *
 * @author 查查
 * @since 2025/9/23
 */
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FileStatusResponse {

    private Integer instanceFileTask;

    private Integer globalFileTask;

    private String platform;

    private Boolean isGlobalInstance;

    private List<String> disks;
}
