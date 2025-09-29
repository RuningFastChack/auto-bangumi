package auto.bangumi.common.utils;

import auto.bangumi.common.enums.CommonResponseEnum;
import auto.bangumi.common.exception.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.util.Strings;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 时间工具类
 *
 * @author JinJun
 */
@Slf4j
public abstract class DateUtil {

    public static final String YYYY = "yyyy";

    public static final String MM = "MM";

    public static final String FULL_TIME_PATTERN = "yyyyMMddHHmmss";

    public static final String CHINESE_TIME_YEAR_MONTH_DAY = "yyyy年MM月dd日";

    public static final String TIME_SPLIT_PATTERN = "yyyy-MM-dd";

    public static final String CHINESE_TIME_YEAR_MONTH_DAY_EN = "yyyyMMdd";

    public static final String CHINESE_FULL_NOT_MM = "yyyy年MM月dd日 HH时mm分";

    public static final String FULL_TIME_SPLIT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final String CST_TIME_PATTERN = "EEE MMM dd HH:mm:ss zzz yyyy";

    public static Date getNowDate() {
        return Calendar.getInstance().getTime();
    }

    public static String getNowDate(String params) {
        return formatDateTime(null, params);
    }

    public static String getNowDateStr() {
        return getNowDate(null);
    }

    /**
     * 强制解析日期
     *
     * @param inputDate 要解析的字符串
     * @return 解析出来的日期，如果没有匹配的返回null
     */
    public static Date parseDate(String inputDate) {
        String[] possiblePatterns =
                {
                        "yyyy-MM-dd HH:mm:ss",
                        "yyyy-MM-dd HH:mm",
                        "yyyy-MM-dd",
                        "yyyyMMdd",
                        "yyyy/MM/dd",
                        "yyyy年MM月dd日",
                        "yyyy MM dd",
                        "yyyy/M/d",
                        "yyyy-M-d",
                };
        SimpleDateFormat df = new SimpleDateFormat();
        for (String pattern : possiblePatterns) {
            df.applyPattern(pattern);
            df.setLenient(false);//设置解析日期格式是否严格解析日期
            ParsePosition pos = new ParsePosition(0);
            Date date = df.parse(inputDate, pos);
            if (date != null) {
                return date;
            }
        }
        if (inputDate.matches("^\\d{4}[-年/ ]?02[-月/ ]?29.*$")) {
            inputDate = inputDate.replaceFirst("02([-月/ ]?)29", "02$128");
            return parseDate(inputDate); // 再次尝试
        }
        return null;
    }

    public static Date parseDateFormat(String date) {
        try {
            if (StringUtils.isBlank(date)) {
                return null;
            }
            return DateUtils.parseDate(date);
        } catch (Exception e) {
            log.error("时间转换失败:{}", e.getMessage());
            throw new BusinessException(CommonResponseEnum.VALID_ERROR, new Object[]{date}, "时间转换失败," + e.getMessage());
        }
    }

    public static Date parseDateFormat(String date, String dateFormType) {
        try {
            if (StringUtils.isBlank(date)) {
                return null;
            }
            return DateUtils.parseDate(date, dateFormType);
        } catch (Exception e) {
            throw new BusinessException(CommonResponseEnum.VALID_ERROR, new Object[]{date, dateFormType}, "时间转换失败," + e.getMessage());
        }
    }

    public static String formatDateTime(Date date, String params) {
        SimpleDateFormat format = new SimpleDateFormat(StringUtils.isBlank(params) ? FULL_TIME_SPLIT_PATTERN : params);
        return format.format(Objects.isNull(date) ? getNowDate() : date);
    }

    public static String formatFullTime(LocalDateTime localDateTime) {
        return formatFullTime(localDateTime, FULL_TIME_PATTERN);
    }

    public static String formatFullTime(LocalDateTime localDateTime, String pattern) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return localDateTime.format(dateTimeFormatter);
    }

    public static String getDateFormat(Date date) {
        return getDateFormat(date, FULL_TIME_SPLIT_PATTERN);
    }

    public static String getDateFormat(Date date, String dateFormatType) {
        if (Objects.isNull(date)) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatType, Locale.CHINA);
        return simpleDateFormat.format(date);
    }

    public static String formatCstTime(String date, String format) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CST_TIME_PATTERN, Locale.US);
        Date usDate = simpleDateFormat.parse(date);
        return DateUtil.getDateFormat(usDate, format);
    }

    public static String formatInstant(Instant instant, String format) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return localDateTime.format(DateTimeFormatter.ofPattern(format));
    }

    public static String formatUtcTime(String utcTime) {
        try {
            String t = StringUtils.replace(utcTime, "T", " ");
            String z = StringUtils.replace(t, "Z", Strings.EMPTY);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FULL_TIME_SPLIT_PATTERN);
            LocalDateTime localDateTime = LocalDateTime.parse(z, formatter);
            LocalDateTime now = localDateTime.plusHours(8);
            return formatFullTime(now, FULL_TIME_SPLIT_PATTERN);
        } catch (Exception ignore) {
            return Strings.EMPTY;
        }
    }

    /**
     * 计算两天相隔天数
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static Double daysBetween(Date startTime, Date endTime) {
        if (endTime.after(startTime)) {
            long diff = Math.abs((endTime.getTime() - startTime.getTime()));
            double result = 0.0;
            double hour = Math.floor((double) diff / (1000 * 60 * 60));
            double day = Math.floor((double) diff / (1000 * 60 * 60 * 24));
            result = day;
            if (day > 0) {
                //去掉天数部分，仅留小时数
                hour -= day * 24;
            }
            if (hour >= 12) {
                result += 1;
            } else {
                result += 0.5;
            }
            if (result == 0) {
                return 1.0;
            }
            return result;
        }
        return 0.0;
    }

    /**
     * 计算两天相隔天数 - 只算日期 不包含当天
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static Long daysBetweenOnlyDay(Date startTime, Date endTime) {
        // 转为 LocalDate，只保留年月日
        LocalDate startDate = startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = endTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * 获取日期时间段内的所有日期
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<String> getDays(String startTime, String endTime) {

        // 返回的日期集合
        List<String> days = new ArrayList<String>();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = dateFormat.parse(startTime);
            Date end = dateFormat.parse(endTime);

            Calendar tempStart = Calendar.getInstance();
            tempStart.setTime(start);

            Calendar tempEnd = Calendar.getInstance();
            tempEnd.setTime(end);
            tempEnd.add(Calendar.DATE, +1);// 日期加1(包含结束)
            while (tempStart.before(tempEnd)) {
                days.add(dateFormat.format(tempStart.getTime()));
                tempStart.add(Calendar.DAY_OF_YEAR, 1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return days;
    }

    /**
     * 时间转换年份 将会减少一年 同年则返回一年
     *
     * @param startDate 2010-01-01
     * @param endDate   2015-01-01
     * @return 2010 2011 2012 2013 2014
     */
    public static List<String> calculateFeeYears(Date startDate, Date endDate) {
        LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if (start.getYear() == end.getYear()) {
            return Collections.singletonList(String.valueOf(start.getYear()));
        }

        return IntStream.range(start.getYear(), end.getYear())
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * 根据两个日期返回差距的年、月、日（只返回最高单位差）
     * 若年差大于0，则月、日均为0；若月差大于0，则日为0。
     *
     * @param startDate 起始日期
     * @param endDate   结束日期
     * @return Map，键为"year"、"month"、"day"
     */
    public static Map<String, Integer> getDiff(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate/endDate 不能为空");
        }

        LocalDate s = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate e = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (s.isAfter(e)) {
            LocalDate tmp = s;
            s = e;
            e = tmp;
        }

        Map<String, Integer> diff = new HashMap<>();
        diff.put("year", 0);
        diff.put("month", 0); // 始终返回 0
        diff.put("day", 0);

        // 包含结束日
        long totalDaysInclusive = ChronoUnit.DAYS.between(s, e) + 1;

        if (s.getYear() == e.getYear()) {
            // 同一年 → 天数
            diff.put("day", (int) totalDaysInclusive);
            return diff;
        }

        // 跨年 → 计算当前年份剩余天数 + 后续年份天数
        LocalDate endOfStartYear = LocalDate.of(s.getYear(), 12, 31);
        long daysRemainingInStartYear = ChronoUnit.DAYS.between(s, endOfStartYear) + 1;

        if (totalDaysInclusive <= daysRemainingInStartYear) {
            diff.put("day", (int) totalDaysInclusive);
        } else {
            // 判断跨几年
            int years = 1;
            LocalDate temp = s.plusYears(1);
            while (!temp.isAfter(e)) {
                years++;
                temp = temp.plusYears(1);
            }
            diff.put("year", years);
        }

        return diff;
    }
}
