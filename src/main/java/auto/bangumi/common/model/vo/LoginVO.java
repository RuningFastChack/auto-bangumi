package auto.bangumi.common.model.vo;

import auto.bangumi.admin.model.vo.UserVO;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginVO {

    private String token;

    private Long expire;

    private UserVO user;
}
