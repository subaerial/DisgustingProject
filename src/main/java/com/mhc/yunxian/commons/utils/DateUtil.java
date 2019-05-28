package com.mhc.yunxian.commons.utils;


import com.mhc.yunxian.utils.JsonUtils;
import org.springframework.util.Assert;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Alin
 *         日期工具类
 */
public class DateUtil {

	public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static final SimpleDateFormat SDF_YMD = new SimpleDateFormat("yyyy-MM-dd");

	public static String formatDate(Date date) {
		if (date == null) {
			return "";
		}
		return SDF.format(date);
	}

	public static String formatDateYMD(Date date) {
		if (date == null) {
			return "";
		}
		return SDF_YMD.format(date);
	}

	public static String xDaysLater(Integer x) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, x);
		return SDF_YMD.format(cal.getTime());
	}

	/**
	 * 获得当天0点时间
	 */

	public static Date getTimesMorning() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}


	/**
	 * 获得当天24点时间
	 *
	 * @return
	 */
	public static Date getTimesNight() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 24);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/**
	 * 获取昨天的开始时间
	 *
	 * @return
	 */
	public static Date getTimesMorningOfYesterday() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getTimesMorning());
		cal.add(Calendar.DAY_OF_MONTH, -1);
		return cal.getTime();
	}

	/**
	 * 获取昨天的结束时间
	 *
	 * @return
	 */
	public static Date getTimesNightOfYesterDay() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getTimesNight());
		cal.add(Calendar.DAY_OF_MONTH, -1);
		return cal.getTime();
	}

	/**
	 * 获取明天的开始时间
	 *
	 * @return
	 */
	public static Date getTimesMorningOfTomorrow() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getTimesMorning());
		cal.add(Calendar.DAY_OF_MONTH, 1);

		return cal.getTime();
	}

	/**
	 * 获取明天的结束时间
	 *
	 * @return
	 */
	public static Date getTimesNightOfTomorrow() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getTimesNight());
		cal.add(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}

	/**
	 * 获得本周一0点时间
	 *
	 * @return
	 */
	public static Date getTimesWeekMorning() {
		Calendar cal = Calendar.getInstance();
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		return cal.getTime();
	}
	/**
	 * 获得上周一0点时间
	 *
	 * @return
	 */
	public static Date getLastWeekMondayMorning() {
		Date weekMorning = DateUtil.getTimesWeekMorning();
		Calendar cal = Calendar.getInstance();
		cal.setTime(weekMorning);
		cal.add(Calendar.DAY_OF_WEEK, -7);
		return cal.getTime();
	}

	/**
	 * 获得下周一0点时间
	 *
	 * @return
	 */
	public static Date getNextWeekMondayMorning() {
		Date weekMorning = DateUtil.getTimesWeekMorning();
		Calendar cal = Calendar.getInstance();
		cal.setTime(weekMorning);
		cal.add(Calendar.DAY_OF_WEEK, 7);
		return cal.getTime();
	}

	/**
	 * 获得本周日24点时间
	 *
	 * @return
	 */
	public static Date getTimesWeekNight() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getTimesWeekMorning());
		cal.add(Calendar.DAY_OF_WEEK, 7);
		return cal.getTime();
	}


	/**
	 * 获得本月第一天0点时间
	 *
	 * @return
	 */
	public static Date getTimesMonthMorning() {
		Calendar cal = Calendar.getInstance();
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
		return cal.getTime();
	}


	/**
	 * 获得本月最后一天24点时间
	 *
	 * @return
	 */
	public static Date getTimesMonthNight() {
		Calendar cal = Calendar.getInstance();
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.HOUR_OF_DAY, 24);
		return cal.getTime();
	}

	/**
	 * 计算当前时间后一天的开始时间
	 *
	 * @param date
	 * @param day
	 * @return
	 */
	public static Date getTimesStartOfTomorrow(Date date, Integer day) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}


	/**
	 * 计算当前时间后一天的结束时间
	 *
	 * @param date
	 * @param day
	 * @return
	 */
	public static Date getTimesEndOfTomorrow(Date date, Integer day) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}


	/**
	 * 距离今天相差天数
	 *
	 * @param date
	 * @return
	 */
	public static Long getBetweenDay(Date date) {
		if (date == null || date.after(new Date())) {
			return 0l;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		LocalDate localDate1 = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
		LocalDate localDate2 = LocalDate.now();

		return localDate2.toEpochDay() - localDate1.toEpochDay();
	}


	/**
	 * 当前日期
	 */
	public static String getYearAndDay(Date date) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
		return simpleDateFormat.format(date);
	}


	/**
	 * 设置时分秒
	 */
	public static Date setYMD(Integer hour, Integer minute, Integer second) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.SECOND, minute);
		cal.set(Calendar.MINUTE, second);
		return cal.getTime();
	}


	/**
	 * 任务相关
	 */
	public static Integer getWeekCycleNum(Date time) {
		Assert.notNull(time, "日期不能为空");

		try {
			return Integer.parseInt(String.valueOf(getCalendar(time).get(Calendar.YEAR)) + getWeekOfYear(time));
		} catch (Exception e) {
		}

		return null;
	}


	private static Calendar getCalendar(Date time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(time);
		return calendar;
	}


	public static Integer getWeekOfYear(Date time) {
		Assert.notNull(time, "日期不能为空");

		Calendar calendar = getCalendar(time);
		calendar.setFirstDayOfWeek(Calendar.MONDAY);

		return calendar.get(Calendar.WEEK_OF_YEAR);
	}

	public static void main(String args[]){
		Date timesMorning = DateUtil.getTimesWeekMorning();
		String format = SDF.format(timesMorning);
		System.out.println(format);
		Date lastWeekMorning = DateUtil.getLastWeekMondayMorning();
		String lastWeekMorningStr = SDF.format(lastWeekMorning);
		System.out.println(lastWeekMorningStr);
		Date nextWeekMondayMorning = DateUtil.getNextWeekMondayMorning();
		String nextWeekMondayMorningStr = SDF.format(nextWeekMondayMorning);
		System.out.println(nextWeekMondayMorningStr);
		Calendar instance = Calendar.getInstance();
		instance.set(instance.get(Calendar.YEAR), instance.get(Calendar.MONDAY), instance.get(Calendar.DAY_OF_MONTH), 21, 21, 0);
		instance.add(Calendar.DAY_OF_MONTH,-1);
		Date date = instance.getTime();
		System.out.println(SDF.format(date));
		System.out.println("-------------------------------------");
		Integer[] dayOfWeek=new Integer[]{1,4,6};
		// 当天时间
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		System.out.println("今天是周"+SDF.format(cal.getTime())+"周"+cal.get(Calendar.DAY_OF_WEEK));
		List<Date> dateList=new ArrayList<>();
		/*for (Integer i:dayOfWeek){
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.DAY_OF_WEEK,i);
			calendar.set(Calendar.HOUR_OF_DAY,12);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND,0);
			dateList.add(calendar.getTime());
			System.out.println("----------------calendar---------------------"+ SDF.format(calendar.getTime())+"周"+calendar.get(Calendar.DAY_OF_WEEK));

		}*/
		System.out.println("----------------dateList---------------------"+ JsonUtils.toJson(dateList));
	}
}
