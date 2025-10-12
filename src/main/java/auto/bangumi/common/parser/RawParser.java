package auto.bangumi.common.parser;

import auto.bangumi.common.model.parser.Episode;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RawParser
 *
 * @author sakura
 */
@Slf4j
public abstract class RawParser {

    public static final Pattern EPISODE_INTEGER = Pattern.compile("\\d+");

    public static final Pattern EPISODE_DOUBLE = Pattern.compile("\\d+\\.\\d+");

    public static final Pattern TITLE_RE = Pattern.compile(
            "(.*|\\[.*])" +
                    "( -? \\d+(?:\\.\\d+)?|\\[\\d+(?:\\.\\d+)?]|\\[\\d+.?[vV]\\d]|第\\d+[话話集]|\\[第?\\d+[话話集]]|\\[\\d+.?END]|[Ee][Pp]?\\d+)" +
                    "(.*)"
    );

    public static final Pattern RESOLUTION_RE = Pattern.compile("1080|720|2160|4K");
    public static final Pattern SOURCE_RE = Pattern.compile("B-Global|[Bb]aha|[Bb]ilibili|AT-X|Web");
    public static final Pattern SUB_RE = Pattern.compile("[简繁日字幕]|CH|BIG5|GB");
    public static final Pattern PREFIX_RE = Pattern.compile("[^\\w\\s\\u4e00-\\u9fff\\u3040-\\u309f\\u30a0-\\u30ff-]");

    private static final Map<String, Integer> CHINESE_NUMBER_MAP = new HashMap<>();


    static {
        CHINESE_NUMBER_MAP.put("一", 1);
        CHINESE_NUMBER_MAP.put("二", 2);
        CHINESE_NUMBER_MAP.put("三", 3);
        CHINESE_NUMBER_MAP.put("四", 4);
        CHINESE_NUMBER_MAP.put("五", 5);
        CHINESE_NUMBER_MAP.put("六", 6);
        CHINESE_NUMBER_MAP.put("七", 7);
        CHINESE_NUMBER_MAP.put("八", 8);
        CHINESE_NUMBER_MAP.put("九", 9);
        CHINESE_NUMBER_MAP.put("十", 10);
    }

    public static Episode parse(String rawTitle) {

        rawTitle = rawTitle.trim().replace("\n", " ");
        String contentTitle = preProcess(rawTitle);
        String group = getGroup(contentTitle);

        Matcher matcher = TITLE_RE.matcher(contentTitle);

        if (!matcher.find()) {
            log.error("Cannot parse title: {}", rawTitle);
            return new Episode();
        }

        String seasonInfo = matcher.group(1).trim();
        String episodeInfo = matcher.group(2).trim();
        String other = matcher.group(3).trim();

        String processedRaw = prefixProcess(seasonInfo, group);
        SeasonResult seasonResult = processSeason(processedRaw);

        NameResult nameResult = processName(seasonResult.name);

        String episode = String.valueOf(Integer.MAX_VALUE);

        Matcher isInteger = EPISODE_INTEGER.matcher(episodeInfo);
        if (isInteger.find()) {
            episode = String.valueOf(Integer.parseInt(isInteger.group()));
        }

        Matcher isDouble = EPISODE_DOUBLE.matcher(episodeInfo);
        if (isDouble.find()) {
            episode = String.valueOf(Double.valueOf(isDouble.group()));
        }


        TagResult tags = findTags(other);

        return Episode.builder()
                .name(nameResult.nameZh)
                .nameJp(nameResult.nameJp)
                .nameEn(nameResult.nameEn)
                .season(seasonResult.season)
                .seasonRaw(seasonResult.seasonRaw)
                .episode(episode)
                .sub(tags.sub)
                .dpi(tags.dpi)
                .source(tags.source)
                .build();
    }

    private static String preProcess(String raw) {
        return raw.replace("【", "[").replace("】", "]");
    }

    private static String getGroup(String name) {
        String[] parts = name.split("\\[|\\]");
        if (parts.length > 1)
            return parts[1];
        return "";
    }

    private static String prefixProcess(String raw, String group) {
        raw = raw.replace(group, "");
        raw = PREFIX_RE.matcher(raw).replaceAll("/");
        String[] parts = raw.split("/");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty())
                sb.append(part).append(" ");
        }
        return sb.toString().trim();
    }

    private static SeasonResult processSeason(String seasonInfo) {
        String seasonRule = "S\\d{1,2}|Season \\d{1,2}|[第].?[季期]";
        String nameSeason = seasonInfo.replaceAll("[\\[\\]]", " ");
        Pattern pattern = Pattern.compile(seasonRule);
        Matcher matcher = pattern.matcher(nameSeason);

        int season = 1;
        String seasonRaw = "";
        while (matcher.find()) {
            seasonRaw = matcher.group();
            String temp = seasonRaw.replaceAll("Season|S|第|季|期", "");
            try {
                season = Integer.parseInt(temp);
            } catch (NumberFormatException e) {
                season = CHINESE_NUMBER_MAP.getOrDefault(temp, 1);
            }
        }

        String name = nameSeason.replaceAll(seasonRule, "").trim();
        return new SeasonResult(name, seasonRaw, season);
    }

    private static NameResult processName(String name) {
        String nameEn = "";
        String nameZh = "";
        String nameJp = "";

        String[] parts = name.split("/|\\s{2}|-\\s{2}");
        for (String part : parts) {
            part = part.trim();
            if (part.matches(".*[\\u0800-\\u4e00]{2,}.*") && nameJp.isEmpty()) {
                nameJp = part;
            } else if (part.matches(".*[\\u4e00-\\u9fa5]{2,}.*") && nameZh.isEmpty()) {
                nameZh = part;
            } else if (part.matches(".*[a-zA-Z]{3,}.*") && nameEn.isEmpty()) {
                nameEn = part;
            }
        }

        return new NameResult(nameEn, nameZh, nameJp);
    }

    private static TagResult findTags(String other) {
        String sub = null;
        String dpi = null;
        String source = null;
        String[] elements = other.replaceAll("[\\[\\]()（）]", " ").split(" ");
        for (String ele : elements) {
            if (ele.isEmpty())
                continue;
            if (SUB_RE.matcher(ele).find())
                sub = ele;
            else if (RESOLUTION_RE.matcher(ele).find())
                dpi = ele;
            else if (SOURCE_RE.matcher(ele).find())
                source = ele;
        }
        return new TagResult(sub, dpi, source);
    }

    private static class SeasonResult {
        String name;
        String seasonRaw;
        int season;

        SeasonResult(String name, String seasonRaw, int season) {
            this.name = name;
            this.seasonRaw = seasonRaw;
            this.season = season;
        }
    }

    private static class NameResult {
        String nameEn;
        String nameZh;
        String nameJp;

        NameResult(String en, String zh, String jp) {
            this.nameEn = en;
            this.nameZh = zh;
            this.nameJp = jp;
        }
    }

    private static class TagResult {
        String sub;
        String dpi;
        String source;

        TagResult(String sub, String dpi, String source) {
            this.sub = sub;
            this.dpi = dpi;
            this.source = source;
        }
    }
}
