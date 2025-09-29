package auto.bangumi.rss.model.DTO.RssItem;

import auto.bangumi.common.model.dto.PageQuery;
import auto.bangumi.common.valid.Query;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class RssItemListDTO extends PageQuery {

    /**
     * RssManage的ID
     */
    @NotNull(message = "不能为空", groups = Query.class)
    private Integer rssManageId;

    /**
     * 字幕组
     */
    private String translationGroup;

    /**
     * torrent的原始标题
     */
    private String torrentName;

    /**
     * 剧集名称 xxx S00E00
     */
    private String name;

    /**
     * 是否下载完成
     */
    private String downloaded;

    /**
     * 是否推送
     */
    private String pushed;

}