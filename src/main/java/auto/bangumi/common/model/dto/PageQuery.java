package auto.bangumi.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * PageQuery
 * 工具类来源sz-admin 官方文档：https://szadmin.cn
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PageQuery {

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 条数
     */
    private Integer limit = 10;
}
