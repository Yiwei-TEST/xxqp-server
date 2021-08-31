package com.sy599.game.gcommand.login.util;

import com.sy.mainland.util.CommonUtil;
import com.sy599.game.db.bean.DataStatistics;
import com.sy599.game.db.dao.DataStatisticsDao;
import com.sy599.game.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class LoginDataUtil {

    /**
     * 登入调用
     * @param userId
     * @param date
     */
    public static void loginData(String userId,Date date){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            String ymd = sdf.format(date);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            if (hour == 24) {
                hour = 0;
            }
            String dateStr = sdfFull.format(date);

            Map<String, Object> map = new HashMap<>(64);
            for (int i = 0; i < 24; i++) {
                map.put(new StringBuilder(12).append("loginCount").append(i).toString(), i == hour ? 1 : 0);
            }
            map.put("userId", userId);
            map.put("currentDate", ymd);
            map.put("firstLoginTime", dateStr);
            map.put("lastLoginTime", dateStr);
            map.put("loginTotalCount", 1);
            map.put("loginTotalTime", 0);
            DataStatisticsDao.getInstance().saveOrUpdateLoginData(map);

            String login = sdf.format(System.currentTimeMillis());
            int logintime = Integer.parseInt(login);
            int userIdInt = Integer.parseInt(userId);

            Map<String, Object> loginType = new HashMap<>();
            loginType.put("userId", userIdInt);
            loginType.put("logintime", logintime);
            loginType.put("type", 2);
            DataStatisticsDao.getInstance().saveSystemUserCountlogin(loginType);

        }catch (Exception e){
            LogUtil.errorLog.error("LoginData Exception:"+e.getMessage(),e);
        }
    }

    /**
     * 登出调用
     * @param userId
     * @param loginDate
     * @param logoutDate
     */
    public static void logOutData(String userId,Date loginDate,Date logoutDate){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = logoutDate;
            String ymd = sdf.format(date);
            String dateStr = sdfFull.format(date);

            String loginYmd = sdf.format(loginDate);

            if (ymd.equals(loginYmd)) {
                Map<String, Object> map = new HashMap<>(8);
                map.put("userId", userId);
                map.put("currentDate", ymd);
                map.put("lastLogoutTime", dateStr);
                long seconds = (date.getTime() - loginDate.getTime()) / 1000;
                map.put("loginTotalTime", seconds > 0 ? seconds : 0);

                DataStatisticsDao.getInstance().updateLoginData(map);
            } else {
                Date date0 = sdfFull.parse(CommonUtil.dateTimeToString(date, "yyyy-MM-dd") + " 00:00:00");

                Map<String, Object> map0 = new HashMap<>(8);
                map0.put("userId", userId);
                map0.put("currentDate", sdf.format(loginDate));
                map0.put("lastLogoutTime", dateStr);
                long seconds = (date0.getTime() - loginDate.getTime()) / 1000;
                map0.put("loginTotalTime", seconds > 0 ? seconds : 0);

                DataStatisticsDao.getInstance().updateLoginData(map0);

                Map<String, Object> map = new HashMap<>(64);
                for (int i = 0; i < 24; i++) {
                    map.put(new StringBuilder(12).append("loginCount").append(i).toString(), 0);
                }
                map.put("userId", userId);
                map.put("currentDate", ymd);
                String tempDate = sdfFull.format(loginDate);
                map.put("firstLoginTime", tempDate);
                map.put("lastLoginTime", tempDate);
                map.put("lastLogoutTime", dateStr);
                map.put("loginTotalCount", 0);
                seconds = (date.getTime() - date0.getTime()) / 1000;
                map.put("loginTotalTime", seconds > 0 ? seconds : 0);

                DataStatisticsDao.getInstance().saveOrUpdateLoginData(map);
            }
        }catch (Exception e){
            LogUtil.errorLog.error("LoginOut Exception:"+e.getMessage(),e);
        }
    }

}
