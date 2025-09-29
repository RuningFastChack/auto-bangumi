package auto.bangumi.common.model;

import lombok.*;

/**
 * 种子信息
 *
 * @author sakura
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TorrentInfo {

    private String hash;

    private String state;

    private String content_path;
}
