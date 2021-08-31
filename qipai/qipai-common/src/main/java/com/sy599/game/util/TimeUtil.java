package com.sy599.game.util;

import com.sy.mainland.util.CommonUtil;
import com.sy599.game.common.constant.SharedConstants;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class TimeUtil {

	/**
	 * 获得yyyyMMdd格式的当天日期转换成的int值
	 */
	public static Date getDateByString(String dateStr, String pattern) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.parse(dateStr);
	}


	/**
	 * 获得yyyyMMdd格式的当天日期转换成的int值
	 */
	public static Integer getIntSimpleDay() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return Integer.valueOf(sdf.format(TimeUtil.now()));
	}

	/**
	 * @param date
	 *            (eg:2014-11-10 12:21:00)
	 * @return long
	 * @throws
	 */
	public static String formatTime(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}
	
	/**
	 * @param date
	 *            (eg:2014-11-10 12:21:00)
	 * @return long
	 * @throws
	 */
	public static int formatDayIntTime(Date date) {
		return Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(date));
	}

	public static String formatDayTime(Date date) {
		return new SimpleDateFormat("yyyyMMdd").format(date);
	}

	public static String formatDayTime2(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd").format(date);
	}


	public static long currentTimeMillis() {
		return System.currentTimeMillis();
	}

	public static Date now() {
		return new Date();
	}

	public static String parseTime(long time, String format) {
		if (StringUtils.isBlank(format)) {
			format = "yyyy-MM-dd HH:mm:ss";
		}
		DateFormat df = new SimpleDateFormat(format);
		return df.format(new Date(time));
	}

	/**
	 * @param timeStr
	 *            (eg:2014-11-10 12:21:00)
	 * @return long
	 * @throws
	 */
	public static long parseTimeInMillis(String timeStr) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return df.parse(timeStr).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return 0;
	}

	public static Calendar curCalendar() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(now());
		return cal;
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
			cale.setTime(formatHour.parse(sb.append(new SimpleDateFormat("yyyyMMdd").format(cale.getTime())).append(" ").append(time).toString()));
		} catch (Exception e) {
			LogUtil.msgLog.error("cale set time format error");
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
		if (time.length() > 5) {
			return isPassWithSecond(time);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
		String nowTime = sdf.format(now());
		time = time.replace(":", "");
		return Integer.valueOf(time) <= Integer.valueOf(nowTime);
	}

	/**
	 * 当前时间是否超过当天指定时间点
	 * 
	 * @param time 格式E
	 *            .g. 08:30:55
	 * @return boolean
	 */
	public static boolean isPassWithSecond(String time) {
		SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
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
		long hours = (endTime - beginTime) / SharedConstants.HOUR_IN_MINILLS;
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
			days = (t2.getTime() - t1.getTime()) / SharedConstants.DAY_IN_MINILLS;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return (int) days;
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
	/*
	 * public String getDayOfWeek() { int num =
	 * Calendar.getInstance().get(Calendar.DAY_OF_WEEK); String[] week = {"星期日",
	 * "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"}; if (num >= 1 && num <
	 * week.length) { return week[num-1]; } else { return ""; } }
	 */

	public static int getDayOfWeek() {
		int num = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		int[] week = { 7, 1, 2, 3, 4, 5, 6 };
		return week[num - 1];
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
				System.out.println(dt1 + " 在" + dt2 + "后");
				return 1;
			} else if (dt1.getTime() < dt2.getTime()) {
				System.out.println(dt1 + "在" + dt2 + "前");
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

	/**
	 * 和现在的时间对比相差了具体的时间
	 * 
	 * @param value
	 * @return
	 */
	public static String getSurplusTime(long value) {
		value = value / 1000;
		long time = currentTimeMillis() / 1000;
		long poor = Math.abs(time - value);

		int t = (int) (poor / 86400);
		int s = (int) ((poor - t * 86400) / 3600);
		int f = (int) ((poor - t * 86400 - s * 3600) / 60);
		String str = "";
		if (t > 0) {
			str = t + "天";
		}
		if (s > 0) {
			str += (s + "小时");
		}
		str += (f + "分钟");
		return str;
	}

	/**
	 * 当前时间是否在某个时间段内（8:00-16:00）
	 * @param startTime 开始时间段 比如8:00
	 * @param EndTime 比如16:00
	 * @return
	 */
	public static boolean isInTimeRange(String startTime, String EndTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		String currentDate = sdf.format(new Date());
		try {
			Date startDate = sdf.parse(startTime);
		    Date endDate = sdf.parse(EndTime);//每节结束时间
		    Date currDate = sdf.parse(currentDate);//当前时间
		    if(currDate.after(startDate) && currDate.before(endDate)){
		    	return true;
		    }
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	public static final long object2Long(Object object){
		if (object instanceof Date){
			return ((Date) object).getTime();
		}else{
			return CommonUtil.object2Long(object);
		}
	}

	public static int countTwoDay(Date yesDay,Date nowDay){
		if(yesDay==null)
			return 0;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(yesDay);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		long l1 = calendar.getTimeInMillis();
		calendar.setTime(nowDay);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		long l2 = calendar.getTimeInMillis();
		return (int)((l2-l1)/(24*3600*1000));
	}

	/**
	 * 获取当天0点时间
	 * @return
	 */
	public static Date getNowDayZero(){
		Date nowDay=new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(nowDay);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * 获取当天0点时间
	 * @return
	 */
	public static long getNowDayZeroMS(){
		return getNowDayZero().getTime();
	}

	public static int curHour(){
	    Calendar c = Calendar.getInstance();
	    return c.get(Calendar.HOUR_OF_DAY);
    }

}
