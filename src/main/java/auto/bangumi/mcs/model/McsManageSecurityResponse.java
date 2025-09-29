package auto.bangumi.mcs.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * McsManage Security
 *
 * @author 查查
 * @since 2025/9/23
 */
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class McsManageSecurityResponse {
    /**
     * 远程实例 节点 http://localhost:23333
     */
    private String addr;

    private String password;
}
