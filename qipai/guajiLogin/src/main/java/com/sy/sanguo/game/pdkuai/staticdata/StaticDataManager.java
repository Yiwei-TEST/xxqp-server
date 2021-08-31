package com.sy.sanguo.game.pdkuai.staticdata;

//import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.*;
import com.sy.sanguo.common.util.user.GameUtil;
import com.sy.sanguo.game.bean.Server;
import com.sy.sanguo.game.dao.ServerDaoImpl;
import com.sy.sanguo.game.msg.ActivityMsg;
//import com.sy.sanguo.game.msg.MonitorMsg;
import com.sy.sanguo.game.pdkuai.staticdata.bean.*;
import com.sy.sanguo.game.pdkuai.util.DataMapUtil;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
//import com.sy.sanguo.game.service.SysInfManager;
import com.sy.sanguo.game.service.SysInfManager;
import com.sy599.sanguo.util.TimeUtil;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.*;

public class StaticDataManager {
    private static StaticDataManager _inst = new StaticDataManager();
    public static Map<Integer, RankEvent> rankEventMap = new HashMap<Integer, RankEvent>();
    public static List<Integer> rankEventDayList = new ArrayList<>();
    public static boolean rankEventAlwaysOpen = false;
    /*** 排行榜开始时间 */
    public static int rankEventStartDay = 0;
    /*** 排行榜结束时间 */
    public static int rankEventEndDay = 0;
    public static Map<Integer, List<ActivityBean>> activityCsvMap = new HashMap<>();
    private static List<ActivityCsvInfo> activityList = new ArrayList<>();
//    private static Map<Integer, GradedConfig> gradedUrlMap = new HashMap<Integer, GradedConfig>();
//    private static Map<Integer, PortConfig> serverPortMap = new HashMap<Integer, PortConfig>();

    public static StaticDataManager getInstance() {
        return _inst;
    }

    /**
     * csv文件名称
     */
    private static String csv_path = "";

    public void init() {
        csv_path = getPath();
        loadRankEvent();
        loadActivityData();
//        loadGradedUrl();
//        loadPortConfig();
//        saveServerConfig();
//        sendDataToServer();

        SysInfManager.initServer();
    }

//    private void saveServerConfig() {
//        List<Server> servers = SysInfManager.getInstance().getServers();
//        if (servers != null && !servers.isEmpty()) {
//            ServerDaoImpl.getInstance().clearServerConfig();
//            Map<String, Object> map = null;
//            for (Server server : servers) {
//                // serverConfig = new ServerConfig();
//                // serverConfig.setId(server.getId());
//                // serverConfig.setName(server.getName());
//                // serverConfig.setHost(server.getHost());
//                // serverConfig.setChathost(server.getChathost());
//                // serverConfig.setIntranet(server.getIntranet());
//                // serverConfig.setGameType(StringUtil.implode(server.getGameType()));
//                // serverConfig.setMatchType(StringUtil.implode(server.getMatchType()));
//                // lists.add(serverConfig);
//
//                map = new HashMap<String, Object>();
//                map.put("id", server.getId());
//                map.put("name", server.getName());
//                map.put("host", server.getHost());
//
//                if (!StringUtils.isBlank(server.getChathost())) {
//                    map.put("chathost", server.getChathost());
//                }
//
//                if (!StringUtils.isBlank(server.getIntranet())) {
//                    map.put("intranet", server.getIntranet());
//                }
//
//                if (server.getGameType().size() > 0) {
//                    map.put("gameType", StringUtil.implode(server.getGameType()));
//                }
//
//                if (server.getMatchType().size() > 0) {
//                    map.put("matchType", StringUtil.implode(server.getMatchType()));
//                }
//
//                map.put("onlineCount", 0);
////                String extend = "";
////                if (gradedUrlMap.containsKey(server.getId())) {
////                    StringBuffer sb = new StringBuffer();
////                    GradedConfig config = gradedUrlMap.get(server.getId());
////                    sb.append(config.getUrl1()).append(";");
////                    sb.append(config.getUrl2()).append(";");
////                    sb.append(config.getUrl3()).append(";");
////                    extend += sb.toString();
////
////                }
////                extend += "_";
////                if (serverPortMap.containsKey(server.getId())) {
////                    PortConfig config = serverPortMap.get(server.getId());
////                    if (config.getPortList() != null) {
////                        extend += StringUtil.implode(config.getPortList());
////                    }
////                }
//
////                extend += "_";
////                if (!StringUtils.isBlank(extend)) {
////                    map.put("extend", extend);
////                } else {
////                    map.put("extend", "");
////                }
//
//                map.put("serverType",server.getServerType());
//
//                ServerDaoImpl.getInstance().updateServerConfig(map);
//            }
//
//        }
//    }

//    private void sendDataToServer() {
//        List<Server> servers = SysInfManager.getInstance().getServers();
//        if (servers != null) {
//            GameUtil.pushInfoToServer(2, JacksonUtil.writeValueAsString(servers));
//        }
//    }

    private void loadActivityData() {
        List<String[]> list = readCSV("activity.csv", false);
        activityList.clear();
        for (String[] values : list) {
            int i = 0;
            ActivityCsvInfo bean = new ActivityCsvInfo();
            bean.setId(getIntValue(values, i++));
            bean.setType(getIntValue(values, i++));
            try {
                bean.setStartTime(TimeUtil.ParseTime(getValue(values, i++)));
                bean.setEndTime(TimeUtil.ParseTime(getValue(values, i++)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            bean.setGoodLuckHb(getValue(values, i++));
            bean.setNewYearHb(getValue(values, i++));
            bean.setFuDai(getValue(values, i++));
            bean.setMinExchange(getIntValue(values, i++));
            bean.setRankingList(getValue(values, i++));
            bean.setShowContent(getValue(values, i++));
            activityList.add(bean);
        }

        activityCsvMap.clear();
        for (ActivityCsvInfo info : activityList) {
            ActivityBean bean = new ActivityBean();
            bean.load(info);
            List<ActivityBean> beanlist;
            if (activityCsvMap.containsKey(info.getType())) {
                beanlist = activityCsvMap.get(info.getType());
            } else {
                beanlist = new ArrayList<>();
                activityCsvMap.put(info.getType(), beanlist);
            }
            beanlist.add(bean);
            if (info.getType() == 7) {
                System.out.println("");
            }
        }

        if (activityList != null) {
            GameUtil.pushInfoToServer(1, JacksonUtil.writeValueAsString(activityList));
        }
    }

//    /**
//     * 随机端口
//     *
//     * @param server
//     * @param url
//     * @return
//     */
//    public static String getRandomPort(Server server, String url) {
//        PortConfig config = serverPortMap.get(server.getId());
//        if (config == null || config.getPortList().isEmpty()) {
//            return url;
//        }
//
//        int index = MathUtil.mt_rand(0, config.getPortList().size() - 1);
//        int port = config.getPortList().get(index);
//        url = url.replace(server.getPort() + "", port + "");
//        return url;
//    }

//    public static String getGradedChatUrl(Server server, long totalCount) {
//        int gradedLevel = 1;
//        if (totalCount > 50) {
//            //打的局数50局
//            gradedLevel = 2;
//        } else if (totalCount > 300) {
//            //打的局数300局
//            gradedLevel = 3;
//        }
//
//        String url = "";
//        if (gradedUrlMap.containsKey(server.getId())) {
//            GradedConfig config = gradedUrlMap.get(server.getId());
//            if (gradedLevel == 3) {
//                url = config.getUrl3();
//
//            } else if (gradedLevel == 2) {
//                url = config.getUrl2();
//
//            } else if (gradedLevel == 1) {
//                url = config.getUrl1();
//
//            }
//        }
//        if (StringUtils.isBlank(url)) {
//            url = server.getChathost();
//        }
//
//        url = getRandomPort(server, url);
//        return url;
//
//    }

//    public static void getMonitor() {
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("type", 1 + "");
//        params.put("funcType", 1 + "");
//        for (Server server : SysInfManager.getInstance().getServers()) {
//            String post = GameUtil.send(server.getId(), params);
//
//            if (post==null){
//                continue;
//            }
//
//            JsonWrapper wrapper = new JsonWrapper(post);
//            int code = wrapper.getInt("code", -1);
//            if (code == 0) {
//                String extra = wrapper.getString("extra");
//                GameBackLogger.SYS_LOG.info("getMonitor=== serverId:"+server.getId()+",params:"+params+",result:"+post+",extra="+extra);
//                MonitorMsg msg = JacksonUtil.readValue(extra, MonitorMsg.class);
//                if (msg!=null){
//                    SysInfManager.getInstance().refreshMonitor(msg);
//
//                    Map<String, Object> update = new HashMap<>();
//                    update.put("onlineCount", msg.getOnlineCount());
////                GameBackLogger.SYS_LOG.error("getMonitor :" + server.getId() + "-->" + msg.getOnlineCount());
//                    ServerDaoImpl.getInstance().updateServerByMap(msg.getServerId(), update);
//                }
//            } else {
//                GameBackLogger.SYS_LOG.error("getMonitor err:" + post);
//            }
//        }
//
//        monitorToServer();
//        spareIpToServer();
//    }

//    public static void monitorToServer() {
//        Map<Integer, MonitorMsg> monitorMap = SysInfManager.getInstance().getMonitorMap();
//        if (monitorMap != null) {
//            GameUtil.pushInfoToServer(3, JacksonUtil.writeValueAsString(monitorMap));
//        }
//
//    }

//    public static void spareIpToServer() {
//
//        Map<Integer, String> spareIpMap = SysInfManager.getInstance().getSpareIpMap();
//        if (spareIpMap != null) {
//            GameUtil.pushInfoToServer(4, JacksonUtil.writeValueAsString(spareIpMap));
//        }
//
//    }

    /**
     * 获取一个活动
     *
     * @param type
     * @return
     */
    public static ActivityBean getActivityBean(int type) {
        List<ActivityBean> list = activityCsvMap.get(type);
        if (list == null || list.isEmpty()) {
            return null;
        }

        long now = TimeUtil.currentTimeMillis();
        Iterator<ActivityBean> iterator = list.iterator();
        while (iterator.hasNext()) {
            ActivityBean activityBean = iterator.next();
            if (activityBean.getEndTime() < now) {
                if (list.size() > 1) {
                    iterator.remove();
                }
                continue;
            }

            if (activityBean.getStartTime() <= now && activityBean.getEndTime() >= now) {
                return activityBean;
            }
        }

        return null;
    }

    /**
     * res to client
     *
     * @return
     */
    public static List<ActivityMsg> getActivityMsgs() {
        List<ActivityMsg> list = new ArrayList<>();
        if (activityCsvMap == null || activityCsvMap.isEmpty()) {
            return list;
        }
        for (int type : activityCsvMap.keySet()) {
            ActivityBean bean = getActivityBean(type);
            if (bean != null) {
                ActivityMsg msg = bean.buildActivityMsg();
                msg.setIsOpen(1);
                list.add(msg);

            } else {
                ActivityBean noOPenBean = getNoOpenActivity(type);
                if (noOPenBean != null) {
                    ActivityMsg msg = noOPenBean.buildActivityMsg();
                    list.add(msg);
                }
            }
        }
        return list;
    }

    public static ActivityBean getNoOpenActivity(int type) {
        List<ActivityBean> list = activityCsvMap.get(type);
        if (list == null || list.isEmpty()) {
            return null;
        }

        long now = TimeUtil.currentTimeMillis();
        Iterator<ActivityBean> iterator = list.iterator();
        while (iterator.hasNext()) {
            ActivityBean activityBean = iterator.next();
            if (activityBean.getEndTime() < now) {
                if (list.size() > 1) {
                    iterator.remove();
                }
                continue;
            }

            if (activityBean.getStartTime() >= now && activityBean.getEndTime() >= now) {
                return activityBean;
            }
        }
        return null;
    }

    /**
     * @param type
     * @return
     */
    public static ActivityBean getSingleActivityBaseInfo(int type) {

        List<ActivityBean> list = activityCsvMap.get(type);
        if (list == null || list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }

    public static int getShowRankEndTime() {
        if (rankEventEndDay != 0 && rankEventStartDay != 0) {
            int endDay = TimeUtil.getDay(rankEventEndDay, 6);
            return endDay;
        }
        return 0;
    }

    public static boolean isShowRankActivity() {
        int toDay = TimeUtil.getSimpleToDay();
        if (rankEventEndDay != 0 && rankEventStartDay != 0) {
            int endDay = TimeUtil.getDay(rankEventEndDay, 6);
            if (toDay >= rankEventStartDay && toDay <= endDay) {
                return true;
            }
        }

        return false;
    }

    public static RankEvent getRankEvent(int day) {
        if (!rankEventAlwaysOpen) {
            return rankEventMap.get(day);

        } else {
            if (day==8) {
                return rankEventMap.get(day);
            } else {
                Calendar calendar = TimeUtil.parseTimeInCalendar(day + "");
                int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                if (week == 0) {
                    week = 7;
                }
                return rankEventMap.get(week);
            }
        }
    }

    public static List<Integer> getRankEventDayList() {
        if (!rankEventAlwaysOpen) {
            return rankEventDayList;
        } else {
            //
            // 如果今天是结束时间
            // 当天往前推6天 是否大于开始时间
            // 如果大于开始时间 以当天-6天为开始时间 今天为结束时间
            //
            List<Integer> daylist = new ArrayList<>();
            int toDay = TimeUtil.getSimpleToDay();
            if (toDay >= rankEventEndDay) {
                daylist = TimeUtil.getDayList(TimeUtil.parseTimeInDate(rankEventEndDay + ""), 7, true);

            } else {
                int beFore = TimeUtil.getDay(toDay, -6);
                if (beFore < rankEventStartDay) {
                    daylist = TimeUtil.getDayList(TimeUtil.parseTimeInDate(rankEventStartDay + ""), 7, false);

                } else {
                    daylist = TimeUtil.getDayList(TimeUtil.now(), 7, true);
                }

            }
            return daylist;
        }

    }

    private void loadRankEvent() {
        List<String[]> list = readCSV("rankEvent.csv", false);
        for (String[] values : list) {
            int i = 0;
            RankEvent bean = new RankEvent();
            bean.setDay(getIntValue(values, i++));
            if (!rankEventAlwaysOpen && bean.getDay() < 10) {
                rankEventAlwaysOpen = true;
            }
            bean.setEvents(StringUtil.explodeToIntList(getValue(values, i++)));
            bean.setRankCount(getIntValue(values, i++));
            bean.setAwardMap(DataMapUtil.implode(getValue(values, i++)));
            if (rankEventAlwaysOpen) {
                int time = getIntValue(values, i++);
                if (time != 0) {
                    if (rankEventStartDay == 0) {
                        rankEventStartDay = time;
                    } else if (rankEventEndDay == 0) {
                        rankEventEndDay = time;
                    }
                }
            }

            rankEventMap.put(bean.getDay(), bean);
            rankEventDayList.add(bean.getDay());
        }
        if (!rankEventAlwaysOpen && !rankEventDayList.isEmpty()) {
            rankEventStartDay = rankEventDayList.get(0);
            rankEventEndDay = rankEventDayList.get(rankEventDayList.size() - 1);
        }

    }

    public static List<Integer> getFuncList(int index) {
        List<Integer> funcList = new ArrayList<>();
        if (!rankEventAlwaysOpen) {
            for (int eventDay : rankEventDayList) {
                RankEvent event = rankEventMap.get(eventDay);
                funcList.add(event.getEvents().get(index));
            }

        } else {
            List<Integer> list = getRankEventDayList();
            for (int day : list) {
                Calendar calendar = TimeUtil.parseTimeInCalendar(day + "");
                int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                if (week == 0) {
                    week = 7;
                }
                RankEvent event = rankEventMap.get(week);
                funcList.add(event.getEvents().get(index));
            }
            // int week = TimeUtil.curCalendar().get(Calendar.DAY_OF_WEEK) - 1;
            // for (int i = 0; i < 7; i++) {
            // int day = week + i;
            // if (day > 7) {
            // day = day - 7;
            // }
            //
            //
            // }
        }

        return funcList;
    }

    /**
     * getValue读取csv
     *
     * @param values
     * @param index
     * @return
     */
    private static String getValue(String[] values, int index) {
        if (index >= values.length) {
            LogUtil.e("getValue index > lenght-->" + index + ":" + values.toString());
            return "";
        }
        return values[index];
    }

    private static long getLongValue(String[] values, int index) {
        return StringUtil.getLongValue(values, index);
    }

    private static int getIntValue(String[] values, int index) {
        return StringUtil.getIntValue(values, index);
    }

    private String getPath() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getClassLoader().getResource("").getPath());
        sb.append("csv/");
        return sb.toString();
    }

    private boolean isRead(String filename) {
        File file = new File(csv_path + filename);
        if (!file.exists()) {
            LogUtil.e("--------" + filename + "is not exists----------");
            return false;
        }

        return true;
    }

    /**
     * 读取csv文件
     *
     * @param filePath      csv目录下的子文件夹目录名/csv的文件名
     * @param includeHeader list是否包含第一行
     * @return List<String[]> String[]的每个值依次为csv文件每一行从左到右的单元格的值
     */
    private List<String[]> readCSV(String filePath, boolean includeHeader) {
        List<String[]> list = new ArrayList<String[]>();
        if (!isRead(filePath)) {
            return list;
        }
        CsvReader reader = null;
        try {
            reader = new CsvReader(csv_path + filePath, ',', Charset.forName("UTF-8"));
            /** csv的第一行 * */
            reader.readHeaders();
            String[] headers = reader.getHeaders();
            if (includeHeader) {
                // 读取UTF-8格式有bug 需去掉第一个字符的空格
                headers[0] = headers[0].substring(1);
                list.add(headers);
            }
            /** 从第二行开始读 * */
            while (reader.readRecord()) {
                String[] values = reader.getValues();
                if (values.length != 0 && !StringUtils.isBlank(values[0])) {
                    list.add(values);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            /** 关闭reader * */
            if (reader != null) {
                reader.close();
            }
        }
        return list;
    }

    public static List<ActivityCsvInfo> getActivityList() {
        return activityList;
    }
}
