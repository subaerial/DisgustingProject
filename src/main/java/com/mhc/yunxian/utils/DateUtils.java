
package com.mhc.yunxian.utils;

import com.mhc.yunxian.bean.EditDragonResponse;
import com.mhc.yunxian.constants.RespStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author Meizu
 * @ClassName: DateUtils
 * @Description: TODO
 * @date 2015年9月15日 上午11:07:06
 */
public class DateUtils {

    /**
     * 获取1970-1-1至今的秒数
     *
     * @return
     */
    public static final int getTime() {
        Date now = new Date();
        return Integer.parseInt(String.valueOf(now.getTime() / 1000));
    }

    /**
     * 默认的日期格式
     */
    public static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    public static String SHORT_DATE_FORMAT = "yyyyMMdd";

    public static String LONG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String LONG_DATE_FORMAT_MINUTE = "yyyy-MM-dd HH:mm";

    public static String TIME_FORMAT = "HH:mm";

    public static String LONG_DATE_FORMAT_SSS = "yyyy-MM-dd HH:mm:ss.SSS";// 精确到毫秒

    public static String MONTH_DAY = "MM-dd";
    public static String MONTH_DAY_FAST_ARRIVE = "MM月dd日";

    public final static String RDO_FORMAT = "yyyyMMddHHmmss";

    private DateUtils() {
    }

    /**
     * String转换时间戳
     *
     * @param time
     * @return
     */
    public static final Long timeToTimeStamp(String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = simpleDateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return date.getTime();
    }

    /**
     * 得到当前日期的前后日期 +为后 -为前
     *
     * @param day_i
     * @return
     */
    public static final String getBefDateString(Date date, int day_i, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            //Date date = SDF.parse(currentDate);
            Calendar day = Calendar.getInstance();
            day.setTime(date);
            day.add(Calendar.DATE, day_i);
            return sdf.format(day.getTime());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 取得当前日期
     *
     * @return Date 当前日期
     */
    public static Date getCurrentDate() {
        return new Date(System.currentTimeMillis());
    }

    /**
     * 返回当前日期对应的默认格式的字符串
     *
     * @return String 当前日期对应的字符串
     */
    public static String getCurrentStringDate() {
        return convertDate2String(getCurrentDate(), DEFAULT_DATE_FORMAT);
    }

    /**
     * 返回当前日期对应的指定格式的字符串
     *
     * @param dateFormat - 日期格式
     * @return String 当前日期对应的字符串
     */
    public static String getCurrentStringDate(String dateFormat) {
        return convertDate2String(getCurrentDate(), dateFormat);
    }

    /**
     * 将日期转换成指定格式的字符串
     *
     * @param date       - 要转换的日期
     * @param dateFormat - 日期格式
     * @return String 日期对应的字符串
     */
    public static String convertDate2String(Date date, String dateFormat) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = null;
        if (StringUtils.isNotBlank(dateFormat)) {
            try {
                sdf = new SimpleDateFormat(dateFormat, Locale.US);
            } catch (Exception e) {
                e.printStackTrace();
                sdf = new SimpleDateFormat(LONG_DATE_FORMAT_MINUTE, Locale.US);
            }
        } else {
            sdf = new SimpleDateFormat(LONG_DATE_FORMAT_MINUTE, Locale.US);
        }
        return sdf.format(date);
    }

    /**
     * 将日期转换成指定格式的字符串
     *
     * @param date - 要转换的日期
     * @param //dateFormat - 日期格式
     * @return String 日期对应的字符串
     */
//    public static String convertDate2String(Date date) {
//        if (date == null) {
//            return null;
//        }
//        SimpleDateFormat SDF = null;
//        SDF = new SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.US);
//        return SDF.format(date);
//    }

    /**
     * 将日期转换成长格式的字符串
     *
     * @param date - 要转换的日期
     * @return String 日期对应的字符串
     */
    public static String convertDate2LongString(Date date) {
        return convertDate2String(date, LONG_DATE_FORMAT);
    }

    /**
     * 将字符串转换成日期
     *
     * @param stringDate - 要转换的字符串格式的日期
     * @return Date 字符串对应的日期
     */
    public static Date convertString2Date(String stringDate) {
        return convertString2Date(stringDate, LONG_DATE_FORMAT_MINUTE);
    }

    /**
     * 将字符串转换成日期
     *
     * @param stringDate - 要转换的字符串格式的日期
     * @param dateFormat - 要转换的字符串对应的日期格式
     * @return Date 字符串对应的日期
     */
    public static Date convertString2Date(String stringDate, String dateFormat) {
        if (StringUtils.isEmpty(stringDate)) {
            return null;
        }
        SimpleDateFormat sdf = null;
        if (StringUtils.isNotBlank(dateFormat)) {
            try {
                sdf = new SimpleDateFormat(dateFormat, Locale.US);
            } catch (Exception e) {
                e.printStackTrace();
                sdf = new SimpleDateFormat(LONG_DATE_FORMAT_MINUTE, Locale.US);
            }
        } else {
            sdf = new SimpleDateFormat(LONG_DATE_FORMAT_MINUTE, Locale.US);
        }
        try {
            return sdf.parse(stringDate);
        } catch (ParseException pe) {
            pe.printStackTrace();
            // TODO
            return new Date(System.currentTimeMillis());
        }
    }

    public static Date convertString2Date2(String stringDate) {
        return convertString2Date2(stringDate, LONG_DATE_FORMAT);
    }

    public static Date convertString2Date2(String stringDate, String dateFormat) {
        if (StringUtils.isEmpty(stringDate)) {
            return null;
        }
        SimpleDateFormat sdf = null;
        if (StringUtils.isNotBlank(dateFormat)) {
            try {
                sdf = new SimpleDateFormat(dateFormat, Locale.US);
            } catch (Exception e) {
                e.printStackTrace();
                sdf = new SimpleDateFormat(LONG_DATE_FORMAT, Locale.US);
            }
        } else {
            sdf = new SimpleDateFormat(LONG_DATE_FORMAT, Locale.US);
        }
        try {
            return sdf.parse(stringDate);
        } catch (ParseException pe) {
            pe.printStackTrace();
            // TODO
            return new Date(System.currentTimeMillis());
        }
    }

    /**
     * 离下个月还有几天
     */
    public static int getDaysToMonthBottom() {
        Calendar c = Calendar.getInstance();
        int d = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        int now = c.get(Calendar.DAY_OF_MONTH);
        return (d - now) + 1;
    }

    @Deprecated
    public static String getDate(Date date) {
        return convertDate2String(date, DEFAULT_DATE_FORMAT);
    }

    @Deprecated
    public static String getDate(long dateTime) {
        return getDate(new Date(dateTime));
    }

    public static String format(Date date) {
        return FastDateFormat.getInstance(LONG_DATE_FORMAT).format(date);
    }

    //
    public static String convertDate2LongString2(Date date) {
        return convertDate2String(date, LONG_DATE_FORMAT_MINUTE);
    }

    /**
     * 时间戳转换成日期格式字符串,格式化小时
     *
     * @param seconds 精确到毫秒
     * @return
     */
    public static String timeStamp2Date(Long seconds) {
        if (seconds == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:00:00");
        return sdf.format(new Date(seconds));
    }
}

