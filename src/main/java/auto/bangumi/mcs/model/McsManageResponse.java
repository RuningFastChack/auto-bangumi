package auto.bangumi.mcs.model;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McsManageResponse<T> {

    /**
     * 状态
     */
    private String status;

    /**
     * 请求完成处理的时间可用于测量延迟。
     */
    private Long time;

    private T data;
}
