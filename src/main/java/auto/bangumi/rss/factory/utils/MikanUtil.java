package auto.bangumi.rss.factory.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mikan RSS解析工具类
 *
 * @author sakura
 * @version 1.0
 * @since 2026/04/27
 */
@Slf4j
public abstract class MikanUtil {

    private static final Pattern BANGUMI_ID_PATTERN = Pattern.compile("(?:[?&])bangumiId=(\\d+)");
    private static final Pattern BANGUMI_ID_PATTERN_SUB_GROUP_ID = Pattern.compile("(?:[?&])subgroupid=(\\d+)");
    private static final Pattern LAST_PATH_SEGMENT = Pattern.compile(".*/([^/]+)$");

    /**
     * 获取 URL 的 base，例如
     * <p>https://mikanime.tv</p>
     */
    public static String baseURL(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();     // http / https
            String host = uri.getHost();         // mikanime.tv
            int port = uri.getPort();            // 端口（-1 表示默认端口）
            if (port == -1) {
                return scheme + "://" + host;
            } else {
                return scheme + "://" + host + ":" + port;
            }
        } catch (Exception e) {
            log.error("路径解析失败,{}", e.getMessage());
            return "";
        }
    }

    /**
     * 获取torrent的值 PS 应该是唯一吧
     */
    public static String extractEpisodeId(String url) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath();
            Matcher m = LAST_PATH_SEGMENT.matcher(path);
            return m.find() ? m.group(1) : null;
        } catch (Exception e) {
            log.error("路径解析失败,{}", e.getMessage());
            return "";
        }
    }

    /**
     * 提取 bangumiId 参数
     */
    public static String extractBangumiId(String url) {
        Matcher matcher = BANGUMI_ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * 提取 SubGroupId 参数
     */
    public static String extractSubGroupId(String url) {
        Matcher matcher = BANGUMI_ID_PATTERN_SUB_GROUP_ID.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "0";
    }

    /**
     * 解析放送开始日期：3/31/2025 -> 2025-03-31
     */
    public static String parseDate(String text) {
        String raw = text.replace("放送开始：", "").trim();
        DateTimeFormatter inputFmt = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.ENGLISH);
        DateTimeFormatter outputFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(raw, inputFmt);
        return date.format(outputFmt);
    }

    /**
     * 解析总集数
     */
    public static String parseTotalEpisode(String text) {
        if (StringUtils.isBlank(text)) {
            return "0";
        }
        String raw = text.replace("总集数：", "").trim();
        return StringUtils.isBlank(raw) ? "0" : raw;
    }

}
