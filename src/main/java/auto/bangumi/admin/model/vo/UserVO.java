package auto.bangumi.admin.model.vo;

import auto.bangumi.admin.model.UserConfig;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 用户姓名
     */
    private String username;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 配置
     */
    private UserConfig config;
}
