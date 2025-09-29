package auto.bangumi.common.model.parser;

import lombok.*;

/**
 * Episode
 *
 * @author sakura
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Episode {
    private String name;
    private int season;
    private String seasonRaw;
    private String episode;
    private String sub;
    private String dpi;
    private String source;
    private String group;
}
