package auto.bangumi.admin.model.dto;

import auto.bangumi.common.valid.Query;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * LoginDTO
 *
 * @author sakura
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {

    @NotBlank(message = "用户名不能为空", groups = Query.class)
    private String username;

    @NotBlank(message = "密码不能为空", groups = Query.class)
    private String password;
}
