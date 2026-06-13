package auto.bangumi.common.constant;

import java.util.regex.Pattern;

/**
 * 常量
 *
 * @author sakura
 */
public interface AutoBangumiConstant {

    /**
     * 分组类型
     */
    String TORRENT_CATEGORY = "AutoBangumi";

    /**
     * 归档分组
     */
    String TORRENT_CATEGORY_ARCHIVE = "AutoBangumiArchive";

    /**
     * 做种时长，做个好心人吧共享一下
     */
    Integer SENDING_TIME_LIMIT = 1800;

    Pattern EPISODE_INTEGER = Pattern.compile("\\d+");

    Pattern EPISODE_DOUBLE = Pattern.compile("\\d+\\.\\d+");
}
