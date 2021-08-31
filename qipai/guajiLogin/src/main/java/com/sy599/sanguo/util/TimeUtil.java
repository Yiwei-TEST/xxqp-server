package com.sy599.sanguo.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.sy.mainland.util.CommonUtil;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.apache.commons.lang.time.DateUtils;

import com.sy.sanguo.common.log.GameBackLogger;
import org.apache.commons.lang3.StringUtils;

public final class TimeUtil {
	public static int SENCOND_IN_MINILLS = 1000;
	public static int MIN_IN_MINILLS = 60 * 1000;
	public static int HOUR_IN_MINILLS = 3600 * 1000;
	public static int DAY_IN_MINILLS = 24 * 3600 * 1000;
	// public static long timeDel = 0;
	public static final String def_format = "yyyy-MM-dd HH:mm:ss";
	public static final String def_format_min = "yyyyMMddHHmm";
	public static final String ymd_format = "yyyyMMdd";

	/**
	 * @param date
	 *            (eg:2014-11-10 12:21:00)
	 * @return long
	 * @throws
	 */
	public static String formatTime(Date date) {
		return CommonUtil.dateTimeToString(date,def_format);
	}

	/**
	 * @param date
	 *            (eg:2014-11-10 12:21:00)
	 * @return long
	 * @throws
	 */
	public static String formatTimeMin(Date date) {
		return CommonUtil.dateTimeToString(date,def_format_min);
	}

	public static Date ParseTime(String date) throws ParseException {
		return CommonUtil.stringToDateTime(date,def_format);
	}
	
	public static String getSimpleDay(Date date) {
		return CommonUtil.dateTimeToString(date,ymd_format);
	}

	public static void main(String[] args) {
		System.out.println(getDayList(now(), 7, true));
		System.out.println(getDay(20160928, -7));
	}

	public static int getSimpleToDay() {
		return Integer.parseInt(TimeUtil.getSimpleDay(TimeUtil.now()));
	}

	public static long currentTimeMillis() {
		return System.currentTimeMillis();
	}

	public static Date now() {
		return new Date();
	}

	public static Calendar curCalendar() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(now());
		return cal;
	}

	/**
	 * 指定时间是否在指定时间范围内
	 * @param time 给定时间
	 * @param beginTime 开始时间
	 * @param endTime 结束时间
	 * @return 是否在区间内
	 */
	public static boolean isInTime(Date time, Date beginTime, Date endTime) {
		return time.before(endTime) && time.after(beginTime);
	}

	/**
	 * 获取当天指定时刻的Calendar对象
	 * 
	 * @param time
	 *            格式--20:00
	 * @return Calendar
	 */
	public static Calendar getCale(String time) {
		Calendar cale = TimeUtil.curCalendar();
		return getCale(cale, time);
	}

	/**
	 * 获取指定cale指定时刻的Calendar对象
	 * 
	 * @param cale
	 * @param time
	 * @return Calendar
	 */
	public static Calendar getCale(Calendar cale, String time) {
		SimpleDateFormat formatDay = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat formatHour = new SimpleDateFormat("yyyyMMdd HH:mm");
		StringBuilder sb = new StringBuilder();
		try {
			cale.setTime(formatHour.parse(sb.append(formatDay.format(cale.getTime())).append(" ").append(time).toString()));
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error("cale set time format error");
		}
		return cale;
	}

	/**
	 * 当前时间是否超过当天指定时间点
	 * 
	 * @param time 格式E
	 *            .g. 08:30
	 * @return boolean
	 */
	public static boolean isPass(String time) {
		SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
		String nowTime = sdf.format(now());
		time = time.replace(":", "");
		return Integer.valueOf(time) <= Integer.valueOf(nowTime);
	}

	/**
	 * 计算2个时间相隔的实际小时数
	 */
	public static int apartHours(Date beginDate, Date endDate) {
		return apartHour(beginDate.getTime(), endDate.getTime());
	}

	/**
	 * 计算2个时间相隔的实际小时数
	 */
	public static int apartHour(long beginTime, long endTime) {
		long hours = (endTime - beginTime) / HOUR_IN_MINILLS;
		return (int) hours;
	}

	/**
	 * 计算两个时间之间的天数
	 * 
	 * @param begin
	 * @param end
	 * @return int
	 */
	public static int apartDays(long begin, long end) {
		return apartDays(new Date(begin), new Date(end), true);
	}

	/**
	 * 计算两个时间之间的天数
	 * 
	 * @param begin
	 * @param end
	 * @return int
	 */
	public static int apartDays(int begin, int end) {
		return apartDays(parseTimeInDate(begin + ""), parseTimeInDate(end + ""), true);
	}

	/**
	 * 计算两个时间之间的天数
	 * 
	 * @param beginDate
	 * @param endDate
	 * @return int
	 */
	public static int apartDays(Date beginDate, Date endDate) {
		return apartDays(beginDate, endDate, true);
	}

	/**
	 * 计算两个时间之间的天数
	 * 
	 * @param beginDate
	 * @param endDate
	 * @param ignoreHour
	 *            为true时，只要隔了天，就算没到24小时，也记为1天，为false时则根据相隔24小时才算一天
	 * @return int
	 */
	public static int apartDays(Date beginDate, Date endDate, boolean ignoreHour) {
		long days = 0l;
		DateFormat df = null;
		if (ignoreHour) {
			df = new SimpleDateFormat("yyyy-MM-dd");
		} else {
			df = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		}
		DateFormat tf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		Date t1 = null;
		Date t2 = null;
		try {
			t1 = df.parse(tf.format(beginDate));
			t2 = df.parse(tf.format(endDate));
			days = (t2.getTime() - t1.getTime()) / DAY_IN_MINILLS;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return (int) days;
	}

	public static int getDay(int day, int add) {
		Calendar calendar = parseTimeInCalendar(day + "");
		calendar.add(Calendar.DAY_OF_MONTH, add);
		return Integer.parseInt(getSimpleDay(calendar.getTime()));
	}

	/**
	 * 获取日期 date到dayCount(某某天数)之间的日志
	 * 
	 * @param date
	 * @param dayCount
	 * @param before
	 *            true
	 * @return
	 */
	public static List<Integer> getDayList(Date date, int dayCount, boolean before) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		List<Integer> list = new ArrayList<>();
		list.add(Integer.parseInt(getSimpleDay(calendar.getTime())));
		for (int i = 1; i < dayCount; i++) {
			int add = before ? -1 : 1;
			calendar.add(Calendar.DAY_OF_MONTH, add);
			list.add(Integer.parseInt(getSimpleDay(calendar.getTime())));
		}
		return list;
	}

	public static Calendar parseTimeInCalendar(String timeStr) {
		Date date = parseTimeInDate(timeStr);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}

	/**
	 * @param timeStr
	 *            (eg:2014-11-10 12:21:00)
	 * @return long
	 * @throws
	 */
	public static Date parseTimeInDate(String timeStr) {
		DateFormat df;
		if (timeStr.length() == 8) {
			df = new SimpleDateFormat("yyyyMMdd");

		} else {
			df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
		try {
			return df.parse(timeStr);
		} catch (ParseException e) {

			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 把日期格式为yyyyMMdd 或者 yyyy-MM-dd 以及带上hhmmsss  转化成日期
	 * @param dateFormate yyyyMMdd yyyy-MM-dd
	 * @return
	 */
	public static Date parseDate(String dateFormate, String hhmmsss) throws Exception {
		Date currentDate = new Date();
		SimpleDateFormat sdf0 = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String parseDateStr;
		if (dateFormate != null && dateFormate.length() == 8) {
			Date date = sdf0.parse(dateFormate);
			if (date.getTime() > currentDate.getTime()) {
				parseDateStr = sdf1.format(currentDate) + " " + hhmmsss;
			}else{
				parseDateStr = sdf1.format(date) + " " + hhmmsss;
			}
		}else{
			parseDateStr = sdf1.format(currentDate) + " " + hhmmsss;
		}
		Date date = sdf2.parse(parseDateStr);
		return date;
	}

	/**
	 * 判断两个时间是否为同一天
	 * 
	 * @param aTime
	 * @param bTime
	 * @return boolean
	 * @throws
	 */
	public static boolean isSameDay(long aTime, long bTime) {
		return DateUtils.isSameDay(new Date(aTime), new Date(bTime));
	}

	/**
	 * 今天是星期几，用中文"星期一,星期二····"表示
	 * 
	 * @author taohuiliang
	 * @date 2012-07-06
	 */
	public String getDayOfWeek() {
		int num = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		String[] week = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
		if (num >= 0 && num < week.length) {
			return week[num - 1];
		} else {
			return "";
		}
	}

	/**
	 * 计算2个时间的前后
	 * 
	 * @param DATE1
	 * @param DATE2
	 * @return
	 */
	public static int compare_date(String DATE1, String DATE2) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date dt1 = df.parse(DATE1);
			Date dt2 = df.parse(DATE2);
			if (dt1.getTime() > dt2.getTime()) {
				System.out.println(dt1 + " 在" + dt2 + "前");
				return 1;
			} else if (dt1.getTime() < dt2.getTime()) {
				System.out.println(dt1 + "在" + dt2 + "后");
				return -1;
			} else {
				return 0;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return 0;
	}

	/**
	 * 获取当天已过秒数
	 * 
	 * @param date
	 * @return long
	 */
	public static long currentDaySecond(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int Minute = calendar.get(Calendar.MINUTE);
		long second = calendar.get(Calendar.SECOND);
		return hour * 60 * 60 + Minute * 60 + second;
	}

	/**
	 * 传入的时间是否在当天的一段时间段内
	 * 
	 * @param date
	 *            传放时间
	 * @param startSecond
	 *            开始值 当天的秒数
	 * @param endSecond
	 *            结束值 当天秒数
	 * @return boolean
	 */
	public static boolean isTimeQuantum(Date date, long startSecond, long endSecond) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
		int seconds = calendar.get(Calendar.SECOND);
		int millisecond = calendar.get(Calendar.MILLISECOND);

		long curDateMillisecond = date.getTime() - hour * 60 * 60 * 1000 - minutes * 60 * 1000 - seconds * 1000 - millisecond;

		Date startDate = new Date(curDateMillisecond + startSecond * 1000);
		Date endDate = new Date(curDateMillisecond + endSecond * 1000);

		return date.getTime() >= startDate.getTime() && date.getTime() < endDate.getTime();
	}

	/**
	 * 两时间相差的秒数(如果相差毫秒有余数作为1秒计算)
	 * 
	 * @param date1
	 * @param date2
	 * @return long
	 * @throws
	 */
	public static long intervalSecond(Date date1, Date date2) {
		long millisecond = date1.getTime() - date2.getTime();
		return (millisecond) % 1000 == 0 ? millisecond / 1000 : millisecond / 1000 + 1;
	}

	/**
	 * 一段时间内通过每天定点时间的次数
	 * 
	 * @param startDate
	 *            开始时间
	 * @param endDate
	 *            结束时间
	 * @param pointSecond
	 *            每天定点秒数
	 * @return long
	 * @throws
	 */
	public static long passCount(Date startDate, Date endDate, long pointSecond) {
		long startDaySecond = TimeUtil.currentDaySecond(startDate);
		long endDaySecond = TimeUtil.currentDaySecond(endDate);

		long differSecond = (endDate.getTime() - startDate.getTime()) / 1000;

		// 两时间相隔天数
		long count = differSecond / (24 * 3600);

		if (count == 0) {
			boolean bool = isSameDay(startDate.getTime(), endDate.getTime());
			if (bool) {
				if (startDaySecond < pointSecond && endDaySecond >= pointSecond) {
					count = 1;
				}
			} else {
				if (startDaySecond < pointSecond) {
					count = 1;
				}
				if (endDaySecond >= pointSecond) {
					count = 1;
				}
			}

		} else {
			if (startDaySecond >= pointSecond) {
				count = count - 1;
			}
			if (endDaySecond >= pointSecond) {
				count = count + 1;
			}
		}
		return count;

	}

    public static String getStartOfDay(int dayOffset) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DAY_OF_YEAR, dayOffset);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 1);
        return sdf.format(start.getTime());
    }

    public static String getEndOfDay(int dayOffset) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar end = Calendar.getInstance();
        end.add(Calendar.DAY_OF_YEAR, dayOffset);
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        return sdf.format(end.getTime());
    }

    public static long getDateyyyyMMdd(int dayOffset) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DAY_OF_YEAR, dayOffset);
        return Long.valueOf(sdf.format(start.getTime()));
    }

    public static int getDayOffset(String dateType){
	    //默认今天
        int dayOffset = 0;
        if ("2".equals(dateType)) {
            //昨天
            dayOffset = -1;
        } else if ("3".equals(dateType)) {
            //前天
            dayOffset = -2;
        }
        return dayOffset;
    }

    /**
     * 检查日期字符串格式是否是：yyyy-MM-dd HH:mm:ss
     *
     * @param dateStr
     * @return
     */
    public static boolean checkDateFormat(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(def_format);
        try {
            sdf.parse(dateStr);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * date比当前时间小minute分钟
     *
     * @param date
     * @param minute
     * @return
     */
    public static boolean isBeforeMinute(Date date, int minute) {
        if (date == null) {
            return false;
        }
        return System.currentTimeMillis() - date.getTime() > minute * 60 * 1000;
    }

    /**
     * ：yyyy-MM-dd HH:mm:ss 转 yyyyMMdd
     *
     * @param dateStr yyyy-MM-dd HH:mm:ss
     * @return yyyyMMdd
     */
    public static String getDataDate(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return "";
        }
        String res = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(def_format);
            Date parse = sdf.parse(dateStr);
            SimpleDateFormat sdf1 = new SimpleDateFormat(ymd_format);
            res = sdf1.format(parse);
        } catch (Exception e) {
            LogUtil.e("getDataDate|error|" + dateStr, e);
        }
        return res;
    }


    /**
     * 检查日期字符串格式是否是：yyyyMMdd
     *
     * @param dateStr
     * @return
     */
    public static boolean checkDateFormatYMD(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(ymd_format);
        try {
            sdf.parse(dateStr);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

	/**
	 * 获取Calendar对象<BR/>
	 * 返回当天00:00:00的Calendar
	 *
	 * @param xxDays 天数偏移量
	 * @return
	 */
	public static Calendar xxDaysCalendar_00(int xxDays) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, xxDays);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal;
	}

	/**
	 * 格式化日期<BR/>
	 * 返回该天的00:00:00<BR/>
	 * 格式为 yyyy-MM-dd HH:mm:ss
	 *
	 * @param xxDays 天数偏移量
	 * @return
	 */
	public static String formatXxDays_00(int xxDays) {
		return formatXxDays_00(xxDays, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 格式化日期<BR/>
	 * 返回该天的00:00:00<BR/>
	 * 格式为 yyyy-MM-dd HH:mm:ss
	 *
	 * @param xxDays    天数偏移量
	 * @param formatStr 格式 默认为yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String formatXxDays_00(int xxDays, String formatStr) {
		SimpleDateFormat sdf;
		try {
			sdf = new SimpleDateFormat(formatStr);
		} catch (Exception e) {
			LogUtil.e("formatXxDays_00|error|" + formatStr, e);
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
		Calendar cal = xxDaysCalendar_00(xxDays);
		return sdf.format(cal.getTime());
	}

}
