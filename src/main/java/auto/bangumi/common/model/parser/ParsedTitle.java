package auto.bangumi.common.model.parser;

import lombok.*;

/**
 * ParsedTitle
 *
 * @author sakura
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedTitle {
    private String title;
    private Integer season;
}
