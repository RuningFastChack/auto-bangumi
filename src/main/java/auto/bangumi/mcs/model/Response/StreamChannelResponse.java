package auto.bangumi.mcs.model.Response;

import auto.bangumi.mcs.model.McsManageSecurityResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 设置终端流通道
 *
 * @author 查查
 * @since 2025/9/23
 */
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StreamChannelResponse extends McsManageSecurityResponse {

    private String prefix;
}
