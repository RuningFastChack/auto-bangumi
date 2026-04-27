package auto.bangumi.common.model.parser;

import lombok.*;

/**
 * 海报DTO
 *
 * @author sakura
 * @version 1.0
 * @since 2026/04/27
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PosterDTO {
    /**
     * 下载路径
     */
    private String downLoadPath;

    /**
     * 海报链接
     */
    private String posterLink;

    /**
     * 海报名称
     */
    private String posterName;
}
