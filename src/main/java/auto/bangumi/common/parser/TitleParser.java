package auto.bangumi.common.parser;

import auto.bangumi.common.model.parser.ParsedTitle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TitleParser
 *
 * @author sakura
 */
public abstract class TitleParser {
    // 匹配 "第2部分" / "第二季" / "第2季"
    private static final Pattern SEASON_PATTERN =
            Pattern.compile("第([0-9一二三四五六七八九十]+)(季|部分)");

    /**
     * 将中文数字转为阿拉伯数字
     */
    private static int chineseToNumber(String chinese) {
        switch (chinese) {
            case "一":
                return 1;
            case "二":
                return 2;
            case "三":
                return 3;
            case "四":
                return 4;
            case "五":
                return 5;
            case "六":
                return 6;
            case "七":
                return 7;
            case "八":
                return 8;
            case "九":
                return 9;
            case "十":
                return 10;
            default:
                try {
                    return Integer.parseInt(chinese);
                } catch (NumberFormatException e) {
                    return 1; // 默认第1季
                }
        }
    }

    /**
     * 提取动漫标题和季数
     */
    public static ParsedTitle parseTitle(String title) {
        String name = title;
        int season = 1;

        Matcher m = SEASON_PATTERN.matcher(title);
        if (m.find()) {
            String num = m.group(1);
            season = chineseToNumber(num);
            // 去掉季数描述部分
            name = title.substring(0, m.start()).trim();
        }

        // 去掉前缀 "Mikan Project - "
        name = name.replaceFirst("^.*?-\\s*", "").trim();

        return new ParsedTitle(name, season);
    }
}
