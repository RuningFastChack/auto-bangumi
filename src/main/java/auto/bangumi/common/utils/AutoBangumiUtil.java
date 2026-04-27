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

/**
 * AutoBangumiUtil
 *
 * @author sakura
 */
@Slf4j
public abstract class AutoBangumiUtil {

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
