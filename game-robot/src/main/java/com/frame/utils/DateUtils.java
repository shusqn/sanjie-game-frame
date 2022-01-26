package com.frame.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * TODO
 * @author Sanjie
 * @date 2020-02-24 11:18
 * @version 1.0
 */
@Slf4j
public class DateUtils {
    public static final String TIME_FORMAT_TO_YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 14-12-4 下午7:24
     *
     * @param time
     * @return
     */
    public static String fromTimeToStr(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return sdf.format(calendar.getTime());
    }

    /**
     * 2014-12-04 19:24:29
     *
     * @param time
     * @return
     */
    public static String fromTimeToStandardStr(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return sdf.format(calendar.getTime());
    }

    public static String fromTimeToFromatStr(long time, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return sdf.format(calendar.getTime());
    }

    /**
     * MM-DD-HH-mm，表示在MM月DD天HH时mm分
     *
     * @param timeStr
     * @return
     */
    public static long parseTime(String timeStr) {
        String[] timeArr = StringUtils.split(timeStr, "-");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, Integer.parseInt(timeArr[0]) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(timeArr[1]));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArr[2]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeArr[3]));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * D-HH-MM，表示在每周第D天HH时MM分
     *
     * @param timeStr
     * @return
     */
    public static long parseWeekTime(String timeStr) {
        String[] timeArr = StringUtils.split(timeStr, "-");
        Calendar calendar = Calendar.getInstance();
        int week = Integer.parseInt(timeArr[0]);
        int currWeek = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.set(Calendar.DAY_OF_WEEK, week);
        if (currWeek > week) {
            calendar.add(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArr[1]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeArr[2]));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取某个时间戳的本周几： 周1 ~ 周日：1 ~ 7
     *
     * @param millisTime
     * @param dayOfWeekNum
     * @return
     */
    public static long getThisWeekNumTime(Long millisTime, int dayOfWeekNum) {
        dayOfWeekNum = (dayOfWeekNum % 7) + 1;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millisTime);
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeekNum);
//        calendar.set(Calendar.WEEK_OF_MONTH, dayOfWeekNum);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取某个时间戳的下一个周几： 周1 ~ 周日：1 ~ 7
     *
     * @param millisTime
     * @param dayOfWeekNum
     * @return
     */
    public static long getNextWeekNumTime(Long millisTime, int dayOfWeekNum) {
        dayOfWeekNum = (dayOfWeekNum % 7) + 1;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millisTime);
        int currWeek = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeekNum);
        if (currWeek >= dayOfWeekNum) {
            calendar.add(Calendar.WEEK_OF_MONTH, 1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取某个时间戳的上一个周几： 周1 ~ 周日：1 ~ 7
     *
     * @param millisTime
     * @param dayOfWeekNum
     * @return
     */
    public static long getLastWeekNumTime(Long millisTime, int dayOfWeekNum) {
        dayOfWeekNum = (dayOfWeekNum % 7) + 1;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millisTime);
        int currWeek = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeekNum);
        if (currWeek <= dayOfWeekNum) {
            calendar.add(Calendar.WEEK_OF_MONTH, -1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long parseFullTimeStr(String fullTimeStr, String format) {
        try {
            return parseFullTimeStrWithException(fullTimeStr, format);
        } catch (ParseException e) {
            log.error("", e);
            return 0;
        }
    }

    public static long parseFullTimeStrWithException(String fullTimeStr, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.parse(fullTimeStr).getTime();
    }

    //获得本周一0点时间
    public static long getTimesWeekmorning(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return cal.getTimeInMillis();
    }
    
    /**
     * 获取第二天刷新时间戳 8点
     * @return
     */
    public static long getNextDayTimeMillisByHour(int hour) { 
        Calendar cal = Calendar.getInstance();
        
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        return cal.getTime().getTime();
    }

    /**
     * 下一天的0点整
     */
    public static Date getNextZero() {
        return getZero(1);
    }

    /**
     * 今天的0点整
     */
    public static Date getTodayZero() {
        return getZero(0);
    }

    /**
     * 相对今天N天的0点整
     */
    public static Date getZero(int dayOfYear) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + dayOfYear);
        return zero(cal);
    }

    public static Date getZero(Date date, int plusDay) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + plusDay);
        return zero(cal);
    }

    public static Date zero(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getZero(long timestamp) {
        return getHourBegin(timestamp, 0);
    }

    public static Date getHourBegin(long timestamp, int hour) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);

        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 下一周的周N 0点
     *
     * @param timestamp
     * @param dayOfWeek 1:周一, 2:周二, ... , 7:周日
     * @return
     */
    public static Date nextWeekDayZero(long timestamp, int dayOfWeek) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.add(Calendar.WEEK_OF_YEAR, 1);

        int day = (dayOfWeek + 1) == 8 ? 1 : (dayOfWeek + 1);
        cal.set(Calendar.DAY_OF_WEEK, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date nextMonthDayZero(long timestamp, int plusMonth, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.add(Calendar.MONTH, plusMonth);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static void main(String[] args) {
        Calendar cal = Calendar.getInstance();
        Date date =  DateUtils.nextWeekDayZero(System.currentTimeMillis(), 1);
        System.err.println(cal.get(Calendar.DAY_OF_WEEK));
        System.err.println(DateFormatUtils.format(new Date()));
        System.err.println(DateFormatUtils.format(date));
    }


    /**
     * 获取今天的 hour 时 minute 分 0 秒 0 毫秒 所对应的 Date
     *
     * @param hour
     * @param minute
     * @return
     */
    public static Date getDate(int hour, int minute) {
        LocalTime localTime = LocalTime.of(hour, minute);
        return convertLDTToDate(localTime.atDate(LocalDate.now()));
    }

    //LocalDateTime转换为Date
    public static Date convertLDTToDate(LocalDateTime time) {
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }

    //获取指定时间的指定格式
    public static String formatTime(LocalDateTime time, String pattern) {
        return time.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(long millis, String pattern) {
        return formatTime(LocalDateTime.ofInstant(new Date(millis).toInstant(), ZoneId.systemDefault()), pattern);
    }

    public static boolean isSameDay(Date date1, Date date2) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        Calendar now = Calendar.getInstance();
        now.setTime(date2);

        return cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isToday(Date date) {
        return isSameDay(date, new Date());
    }

    public static long plusMinutes(long timeMillis, long timeMinutes) {
        return timeMillis + timeMinutes * 60 * 1000;
    }

    public static long plusSeconds(long fromTimeMillis, long timeSeconds) {
        return fromTimeMillis + timeSeconds * 1000;
    }

    public static long plusSeconds(long timeSeconds) {
        return plusSeconds(System.currentTimeMillis(),timeSeconds);
    }

    public static Date minusSeconds(long time, long reduceSeconds) {
        return new Date(Math.abs(time - reduceSeconds * 1000));
    }
}
