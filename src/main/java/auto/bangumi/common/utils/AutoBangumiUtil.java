package auto.bangumi.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AutoBangumiUtil
 *
 * @author sakura
 */
@Slf4j
public abstract class AutoBangumiUtil {

    private static final Pattern BANGUMI_ID_PATTERN = Pattern.compile("(?:[?&])bangumiId=(\\d+)");
    private static final Pattern BANGUMI_ID_PATTERN_SUB_GROUP_ID = Pattern.compile("(?:[?&])subgroupid=(\\d+)");
    private static final Pattern LAST_PATH_SEGMENT = Pattern.compile(".*/([^/]+)$");

    /**
     * 获取 URL 的 base，例如 https://mikanime.tv
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
     * 获取torrent的值 PS 应该是唯一吧
     *
     * @param url
     * @return
     * @throws Exception
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
     * 解析放送日期：星期一 -> 1, 星期二 -> 2 ... 星期日 -> 7
     */
    public static int parseWeek(String text) {
        if (text.contains("星期一"))
            return 1;
        if (text.contains("星期二"))
            return 2;
        if (text.contains("星期三"))
            return 3;
        if (text.contains("星期四"))
            return 4;
        if (text.contains("星期五"))
            return 5;
        if (text.contains("星期六"))
            return 6;
        if (text.contains("星期日") || text.contains("星期天"))
            return 7;
        return 0;
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
     * 下载图片到项目根目录下的 images 文件夹
     */
    public static void downloadImage(String imgUrl, String fileName) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imgUrl))
                .build();

        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        String projectDir = System.getProperty("user.dir");

        File folder = new File(projectDir, "images");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        // 强制保存为 PNG
        if (!fileName.toLowerCase().endsWith(".png")) {
            fileName = fileName.replaceAll("\\..*$", "") + ".png";
        }

        File outputFile = new File(folder, fileName);

        try (InputStream in = response.body(); FileOutputStream out = new FileOutputStream(outputFile)) {
            in.transferTo(out);
        }

        log.debug("图片已保存：{}", outputFile.getAbsolutePath());
    }
}
