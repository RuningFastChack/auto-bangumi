package auto.bangumi.common.model.dto;

import lombok.*;

import java.util.List;

/**
 * PageResult
 * 工具类来源sz-admin 官方文档：https://szadmin.cn
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 当前页
     */
    private long current;

    /**
     * 每页条数
     */
    private long limit;

    /**
     * 总条数
     */
    private long total;

    /**
     * 结果集
     */
    private List<T> rows;
}
