package com.sy.sanguo.game.pdkuai.db.dao;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import com.sy.sanguo.game.bean.GoldUserInfo;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.dao.UserDao;
import com.sy.sanguo.game.pdkuai.game.FirstMythAction;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.apache.commons.beanutils.converters.DoubleConverter;
import org.apache.commons.lang3.StringUtils;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.game.bean.FirstMyth;
import com.sy.sanguo.game.msg.FirstMythMsg;
import com.sy.sanguo.game.pdkuai.staticdata.StaticDataManager;
import com.sy.sanguo.game.pdkuai.staticdata.bean.RankEvent;
import com.sy599.sanguo.util.TimeUtil;

public class FirstMythDao extends BaseDao {
    private Map<Integer, Map<Integer, List<Map<String, Object>>>> rankings = new HashMap<>();
    private Map<Integer, Map<Integer, Long>> time = new HashMap<>();
    private static FirstMythDao _inst = new FirstMythDao();
    public static int refresh = 0;

    public static FirstMythDao getInstance() {
        return _inst;
    }

    public FirstMythMsg rankingList(long userId, int day, int viewIndex, int week) {
        if (week == 0) {
            return rankingList(userId, day, viewIndex);
        } else {
            return weekRankingList(userId, day, viewIndex, week);
        }
    }

    /**
     * 周排行榜
     */
    private FirstMythMsg weekRankingList(long userId, int day, int viewIndex, int week) {
        FirstMythMsg msg = new FirstMythMsg();
        RankEvent rankEvent = StaticDataManager.getRankEvent(day);
        if (rankEvent == null) {
            GameBackLogger.SYS_LOG.error("rankingList  err :rankEvent is null-->" + day);
            return msg;
        }

        if (viewIndex >= rankEvent.getEvents().size()) {
            viewIndex = 0;
        }

        try {
            int startDay = FirstMythAction.getStartDay(day, week);
            int endDay = FirstMythAction.getEndDay(day, week);
            if (startDay < StaticDataManager.rankEventStartDay) {
                return msg;
            }
            if (endDay > StaticDataManager.rankEventEndDay) {
                endDay = StaticDataManager.rankEventEndDay;
            }
            int func = rankEvent.getEvents().get(viewIndex);
            msg.setFunc(func);
            msg.setRankCount(rankEvent.getRankCount());
            if (!isTimeOut(startDay, func)) {
                if (rankings.containsKey(startDay)) {
                    if (rankings.get(startDay).containsKey(func)) {
                        List<Map<String, Object>> rankMsg = rankings.get(startDay).get(func);
                        if (rankMsg != null && !rankMsg.isEmpty()) {
                            msg.setList(rankings.get(startDay).get(func));
                            for (Map<String, Object> map : rankMsg) {
                                long uid = (Long) map.get("userId");
                                if (uid == userId) {
                                    msg.setSelf(map);
                                    buildIsGetAward(userId, day, week, msg, func);
                                    return msg;
                                }
                            }
                            buildMySelf(userId, msg, startDay, endDay, func);
                            return msg;
                        }
                    }
                }
            }

            // 得到排行榜
            List<FirstMyth> rank = getWeekFirstMythList(func, rankEvent, startDay, endDay);
            int j = 1;
            List<Map<String, Object>> list = new ArrayList<>();
            if (rank != null && !rank.isEmpty()) {
                for (FirstMyth bean : rank) {
                    if (j > StaticDataManager.getRankEvent(8).getRankCount()) {
                        break;
                    }
                    Map<String, Object> msgMap = buildFirstMythMap(bean, j, func, rankEvent);
                    list.add(msgMap);
                    j++;
                }
            }

            // 得到自己的排名
            buildMyInfo(userId, day, week, msg, rankEvent, startDay, endDay, func);

            Map<Integer, List<Map<String, Object>>> dataMap;
            Map<Integer, Long> timeMap;
            if (rankings.containsKey(startDay) && time.containsKey(startDay)) {
                dataMap = rankings.get(startDay);
                timeMap = time.get(startDay);
            } else {
                dataMap = new HashMap<>();
                timeMap = new HashMap<>();
                rankings.put(startDay, dataMap);
                time.put(startDay, timeMap);
            }

            dataMap.put(func, list);
            timeMap.put(func, TimeUtil.currentTimeMillis());
            // 返回
            msg.setList(list);
            return msg;
        } catch (Exception e) {
            GameBackLogger.SYS_LOG.error("weekRankingList error", e);
            return msg;
        }
    }

    private void buildMyInfo(long userId, int day, int week, FirstMythMsg msg, RankEvent rankEvent, int startDay, int endDay, int func) {
        Map myRank = getWeekSelfRanking(func, rankEvent, startDay, endDay, userId);
        if (myRank != null) {
            // 榜上
            buildMyRank(myRank, userId, func, rankEvent, msg);
            buildIsGetAward(userId, day, week, msg, func);
        } else {
            // 榜下
            buildMySelf(userId, msg, startDay, endDay, func);
        }
    }

    private Map<String, Object> buildMyRank(Map myRank, long userId, int func, RankEvent rankEvent, FirstMythMsg msg) {
        Map<String, Object> msgMap = new HashMap<>();
        int rank = ((Double) myRank.get("rank")).intValue();
        msgMap.put("rank", rank);
        msgMap.put("userId", userId);
        msgMap.put("name", myRank.get("userName"));
        msgMap.put("val", myRank.get("record" + func));
        if (rank > 0 && rankEvent != null) {
            msgMap.put("award", rankEvent.getAward(rank));
        } else {
            msgMap.put("award", 0);
        }
        RegInfo user = getUser(userId);
        if (user != null) {
            msgMap.put("icon", user.getHeadimgurl());
        }
        msg.setSelf(msgMap);
        return msgMap;
    }

    /**
     * 获得自己的周排行
     */
    public Map getWeekSelfRanking(int func, RankEvent rankEvent, int startDay, int endDay, long userId) {
        String record = "record" + func;
        Map rank;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT rank, userName," + record + " FROM (SELECT @rank := @rank + 1 AS rank,t.* FROM ( select userId,\n" +
                "        userName, SUM(record1) record1,SUM(record2) record2, SUM(record3) record3,SUM(record4) record4,\n" +
                "        SUM(record5) record5,SUM(record6) record6, SUM(record7) record7,SUM(record8) record8,SUM(record9)\n" +
                "        record9,SUM(record10) record10,SUM(record11) record11,SUM(record12) record12,SUM(record13)\n" +
                "        record13,SUM(record14) record14\n" +
                "        FROM user_firstmyth WHERE recordDate BETWEEN " + startDay + " AND " + endDay + " GROUP BY userId HAVING " + record + " !=0\n" +
                "        ORDER BY " + record + " desc limit " + rankEvent.getRankCount() + ") t,(SELECT @rank := 0) r) a\n" +
                "        WHERE userId = " + userId);
        String sql = sb.toString();
        try {
            rank = (Map) getSql().queryForObject("firstmyth.getWeekSelfRanking", sql);
        } catch (SQLException e) {
            GameBackLogger.SYS_LOG.error("rankingList err ", e);
            return null;
        }
        return rank;
    }

    /**
     * 自己榜下构建信息
     */
    private void buildMySelf(long userId, FirstMythMsg msg, int startDay, int endDay, int func) {
        FirstMyth self = getWeekFirstMyth(userId, startDay, endDay);
        if (self != null) {
            // 有纪录
            Map<String, Object> selfMap = buildFirstMythMap(self, 0, func, null);
            msg.setSelf(selfMap);
            msg.getSelf().put("isGetAward", 0);
        } else {
            // 无记录，新玩家
            buildBaseFirstMythMap(userId, msg);
        }
    }

    private void buildIsGetAward(long userId, int day, int week, FirstMythMsg msg, int func) {
        if (week == 2) {
            Map awardBean = FirstMythDao.getInstance().getAwardRecord(userId, FirstMythAction.getStartDay(day, 1), FirstMythAction.getEndDay(day, 1));
            int isGetAward = 1;
            if (awardBean != null && !StringUtils.isBlank((String) awardBean.get("rewardRecord")) && ((String) awardBean.get("rewardRecord")).contains(func + "")) {
                isGetAward = 2;
            }
            msg.getSelf().put("isGetAward", isGetAward);
        } else {
            msg.getSelf().put("isGetAward", 0);
        }
    }

    private Map<String, Object> buildBaseFirstMythMap(long userId, FirstMythMsg msg) {
        RegInfo myself = getUser(userId);
        if (myself != null) {
            Map<String, Object> msgMap = new HashMap<>();
            msgMap.put("rank", 0);
            msgMap.put("userId", userId);
            msgMap.put("name", myself.getName());
            msgMap.put("val", 0);
            msgMap.put("award", 0);
            msgMap.put("icon", myself.getHeadimgurl());
            msgMap.put("isGetAward", 0);
            msg.setSelf(msgMap);
            return msgMap;
        } else {
            GameBackLogger.SYS_LOG.error("weekRankingList  err :user is null-->id:" + userId);
            return null;
        }
    }

    public List<FirstMyth> getWeekFirstMythList(int func, RankEvent rankEvent, int startDay, int endDay) {
        String record = "record";
        List<FirstMyth> rank;
        Map<String, Object> sqlMap = new HashMap<>();
        sqlMap.put("record", record + func);
        sqlMap.put("sort", "desc");
        sqlMap.put("limit", rankEvent.getRankCount());
        sqlMap.put("startDay", startDay);
        sqlMap.put("endDay", endDay);
        try {
            rank = (List<FirstMyth>) getSql().queryForList("firstmyth.rankingWeekList", sqlMap);
        } catch (SQLException e) {
            rank = new ArrayList<FirstMyth>();
            GameBackLogger.SYS_LOG.error("rankingList err ", e);
        }
        return rank;
    }

    private FirstMyth getWeekFirstMyth(long userId, int startDay, int endDay) {
        FirstMyth bean = null;
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("startDay", startDay);
            map.put("endDay", endDay);
            bean = (FirstMyth) getSql().queryForObject("firstmyth.getSelfWeekRanking", map);
        } catch (SQLException e) {
            GameBackLogger.SYS_LOG.error("getFirstMyth", e);
        }
        return bean;
    }

    /**
     * @param day       日期 例：20170730
     * @param viewIndex 0跑得快(斗地主) 1麻将
     * @return 返回指定日期的排行榜
     */
    public FirstMythMsg rankingList(long userId, int day, int viewIndex) {
        FirstMythMsg msg = new FirstMythMsg();
        RankEvent rankEvent = StaticDataManager.getRankEvent(day);
        if (rankEvent == null) {
            GameBackLogger.SYS_LOG.error("rankingList  err :rankEvent is null-->" + day);
            return msg;
        }
        boolean isToDay = Integer.parseInt(TimeUtil.getSimpleDay(TimeUtil.now())) == day;

        if (viewIndex >= rankEvent.getEvents().size()) {
            viewIndex = 0;
        }
        int func = rankEvent.getEvents().get(viewIndex);

        msg.setFunc(func);
        msg.setRankCount(rankEvent.getRankCount());
        FirstMyth self = getFirstMyth(day, userId);
        if (self != null) {
            Map<String, Object> selfMap = buildFirstMythMap(self, 0, func, null);
            msg.setSelf(selfMap);
        } else {
            RegInfo myself = getUser(userId);
            if (myself != null) {
                Map msgMap = buildBaseFirstMythMap(userId, msg);
                msg.setSelf(msgMap);
            } else {
                GameBackLogger.SYS_LOG.error("rankingList  err :user is null-->id:" + userId);
                return msg;
            }
        }

        List<FirstMyth> rank;


        // 得到排行榜
        rank = getFirstMythList(func, day, rankEvent);
        int j = 1;
        List<Map<String, Object>> list = new ArrayList<>();
        for (FirstMyth bean : rank) {
            Map<String, Object> msgMap = buildFirstMythMap(bean, j, func, rankEvent);
            int val = (int) msgMap.get("val");
            if (val == 0) {
                continue;
            }
            if (func == 1 || func == 8) {
                if (val <= 0) {
                    continue;
                }
            } else if (func == 3 || func == 10) {
                if (val >= 0) {
                    continue;
                }
            }

            if (bean.getUserId() == userId) {
                msg.getSelf().put("rank", j);
                msg.getSelf().put("award", rankEvent.getAward(j));
                // 在50名以内
                if (!isToDay) {
                    // 今天的排名先不能领，只能领昨天的
                    int isGetAward = 1;
                    if (!StringUtils.isBlank(bean.getRewardRecord()) && bean.getRewardRecord().contains(func + "")) {
                        isGetAward = 2;

                    }

                    msg.getSelf().put("isGetAward", isGetAward);
                } else {
                    msg.getSelf().put("isGetAward", 0);

                }
            }
            list.add(msgMap);
            j++;
        }
        // 十分钟刷新一次排行榜
        if (!isTimeOut(day, func)) {
            if (rankings.containsKey(day)) {
                if (rankings.get(day).containsKey(func)) {
                    msg.setList(rankings.get(day).get(func));
                    return msg;
                }
            }
        }
        Map<Integer, List<Map<String, Object>>> dataMap = null;
        Map<Integer, Long> timeMap = null;
        if (rankings.containsKey(day)) {
            dataMap = rankings.get(day);
            timeMap = time.get(day);
        } else {
            dataMap = new HashMap<>();
            timeMap = new HashMap<>();
            rankings.put(day, dataMap);
            time.put(day, timeMap);
        }

        dataMap.put(func, list);
        timeMap.put(func, TimeUtil.currentTimeMillis());
        // 返回
        msg.setList(list);
        return msg;
    }

    public int updateFirstMyth(long userId, int day, String rewardRecord) {
        Map<String, Object> sqlMap = new HashMap<>();
        try {
            sqlMap.put("rewardRecord", rewardRecord);
            sqlMap.put("userId", userId);
            sqlMap.put("day", day);
            return getSql().update("firstmyth.updateAward", sqlMap);
        } catch (SQLException e) {
            GameBackLogger.SYS_LOG.error("updateFirstMyth ", e);
        }
        return 0;
    }

    public int insertFirstMyth(RegInfo user, int day) {
        Map<String, Object> sqlMap = new HashMap<>();
        try {
            sqlMap.put("userId", user.getUserId());
            sqlMap.put("userName", user.getName());
            sqlMap.put("day", day);
            getSql().insert("firstmyth.insertAward", sqlMap);
            return 1;
        } catch (SQLException e) {
            GameBackLogger.SYS_LOG.error("insertFirstMyth error", e);
        }
        return 0;
    }

    public List<FirstMyth> getFirstMythList(int func, int day, RankEvent rankEvent) {
        String record = "record";
        List<FirstMyth> rank = new ArrayList<FirstMyth>();
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("record", record + func);
        sqlMap.put("sort", "desc");
        sqlMap.put("yestedayDate", day);
        sqlMap.put("limit", rankEvent.getRankCount());
        if (func == 3) {
            sqlMap.put("record", record + 1);
            sqlMap.put("sort", "asc");
        } else if (func == 10) {
            sqlMap.put("record", record + 8);
            sqlMap.put("sort", "asc");
        }

        try {
            rank = (List<FirstMyth>) getSql().queryForList("firstmyth.rankingList", sqlMap);
        } catch (SQLException e) {
            rank = new ArrayList<FirstMyth>();
            GameBackLogger.SYS_LOG.error("rankingList err ", e);
        }
        return rank;

    }

    public FirstMyth getFirstMyth(int day, long userId) {
        FirstMyth bean = null;
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("day", day);
            map.put("userId", userId);
            bean = (FirstMyth) getSql().queryForObject("firstmyth.getSelfRanking", map);
        } catch (SQLException e) {
            GameBackLogger.SYS_LOG.error("getFirstMyth", e);
        }
        return bean;
    }

    public Map<String, Object> buildFirstMythMap(FirstMyth bean, int rank, int view, RankEvent rankEvent) {
        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("rank", rank);
        msgMap.put("userId", bean.getUserId());
        msgMap.put("name", bean.getUserName());
        if (view == 3) {
            view = 1;
        } else if (view == 10) {
            view = 8;
        }
        msgMap.put("val", getRecordValue(bean, view));
        if (rank > 0 && rankEvent != null) {
            msgMap.put("award", rankEvent.getAward(rank));
        } else {
            msgMap.put("award", 0);
        }
        RegInfo user = getUser(bean.getUserId());
        if (user != null) {
            msgMap.put("icon", user.getHeadimgurl());
        }
        return msgMap;
    }

    private RegInfo getUser(long userId) {
        RegInfo user = null;
        try {
            user = (RegInfo) getSql().queryForObject("user.getUserById", userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    private boolean isTimeOut(int day, int view) {
        if (!time.containsKey(day)) {
            return true;
        }

        if (!time.get(day).containsKey(view)) {
            return true;
        }

        long refreshTime = time.get(day).get(view);
        return TimeUtil.currentTimeMillis() - refreshTime > TimeUtil.MIN_IN_MINILLS * 10;
        //        return true;
    }

    public Object getRecordValue(FirstMyth bean, int record) {
        try {
            Method method = bean.getClass().getMethod("getRecord" + record);
            return method.invoke(bean);
        } catch (Exception e) {
            GameBackLogger.SYS_LOG.error("getRecordValue err ", e);
        }
        return null;
    }

    public Map getAwardRecord(long userId, int startDay, int endDay) {
        Map result = null;
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("startDay", startDay);
            map.put("endDay", endDay);
            result = (Map) getSql().queryForObject("firstmyth.getLastWeekAwardRecord", map);
        } catch (SQLException e) {
            GameBackLogger.SYS_LOG.error("getFirstMyth", e);
        }
        return result;
    }

    /**
     * 检查排行榜刷新
     */
    public void checkRefresh() {
        Calendar ca = Calendar.getInstance();
        if (ca.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && ca.get(Calendar.HOUR_OF_DAY) == 0 && ca.get(Calendar.MINUTE) == 0) {
            time.clear();
            rankings.clear();
//            refresh=1;
        }
//        refresh=0;
    }

    /**
     * 财富榜
     */
    public FirstMythMsg wealthRankingList(long userId, int day) {
        int startDay = StaticDataManager.rankEventStartDay;
        FirstMythMsg msg = new FirstMythMsg();
        RankEvent rankEvent = StaticDataManager.getRankEvent(startDay);
        if (rankEvent == null) {
            GameBackLogger.SYS_LOG.error("rankingList  err :rankEvent is null-->startDay:" + startDay);
            return msg;
        }
        boolean isToDay = Integer.parseInt(TimeUtil.getSimpleDay(TimeUtil.now())) == day;
        int func = rankEvent.getEvents().get(0);
        msg.setFunc(func);
        msg.setRankCount(rankEvent.getRankCount());
        FirstMyth self = getGoldFirstMyth(day, userId);
        if (self != null) {
            Map<String, Object> selfMap = buildFirstMythMap(self, 0, func, null);
            msg.setSelf(selfMap);
        } else {
            GoldUserInfo myself = getGoldUser(userId);
            if (myself == null) {
                Map msgMap = buildBaseFirstMythMap(userId, msg);
                msg.setSelf(msgMap);
            } else {
                buildBaseGoldFirstMythMap(myself, msg);
            }
        }

        List<FirstMyth> rank = null;

        // 得到排行榜
        List<Map<String, Object>> rankList = getWealthFirstMythList(func, day, rankEvent);
        int j = 1;
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> bean : rankList) {
            Map<String, Object> msgMap = buildWealthFirstMythMap(bean, j, rankEvent);
            String val = (String)msgMap.get("val");
            if ("0".equals(val)) {
                continue;
            }
            long uid = (long)bean.get("userId");
            if (uid == userId) {
                msg.getSelf().put("rank", j);
                msg.getSelf().put("award", rankEvent.getAward(j));
                // 在50名以内
                if (!isToDay) {
                    // 今天的排名先不能领，只能领昨天的
                    int isGetAward = 1;
                    if (!StringUtils.isBlank(self.getRewardRecord()) && self.getRewardRecord().contains(func + "")) {
                        isGetAward = 2;
                    }
                    msg.getSelf().put("isGetAward", isGetAward);
                } else {
                    msg.getSelf().put("isGetAward", 0);

                }
            }
            list.add(msgMap);
            j++;
        }
        // 十分钟刷新一次排行榜
        if (!isTimeOut(day, func)) {
            if (rankings.containsKey(day)) {
                if (rankings.get(day).containsKey(func)) {
                    msg.setList(rankings.get(day).get(func));
                    return msg;
                }
            }
        }
        Map<Integer, List<Map<String, Object>>> dataMap = null;
        Map<Integer, Long> timeMap = null;
        if (rankings.containsKey(day)) {
            dataMap = rankings.get(day);
            timeMap = time.get(day);
        } else {
            dataMap = new HashMap<>();
            timeMap = new HashMap<>();
            rankings.put(day, dataMap);
            time.put(day, timeMap);
        }

        dataMap.put(func, list);
        timeMap.put(func, TimeUtil.currentTimeMillis());
        // 返回
        msg.setList(list);
        return msg;
    }

    /**
     * 金币场玩家基本排行榜信息构建
     */
    private Map buildBaseGoldFirstMythMap(GoldUserInfo myself, FirstMythMsg msg) {
        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("rank", 0);
        msgMap.put("userId", myself.getUserId());
        msgMap.put("name", myself.getUserNickName());
        msgMap.put("val", myself.getFreeGold()+myself.getGold());
        msgMap.put("award", 0);
        msgMap.put("icon", myself.getHeadimgurl());
        msgMap.put("isGetAward", 0);
        msg.setSelf(msgMap);
        return msgMap;
    }

    /**
     * 获得金币玩家身份
     */
    private GoldUserInfo getGoldUser(long userId) {
        try {
            return (GoldUserInfo) getSql().queryForObject("gold.selectGoldUserByUserId", userId);
        } catch (SQLException e) {
            LogUtil.e("getGoldUser err-->" + e);
        }
        return null;
    }

    /**
     * 获得金币场玩家封神记录
     */
    private FirstMyth getGoldFirstMyth(int day, long userId) {
        FirstMyth bean = null;
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("day", day);
            map.put("userId", userId);
            bean = (FirstMyth) getSql().queryForObject("gold_firstmyth.getSelfRanking", map);
        } catch (SQLException e) {
            GameBackLogger.SYS_LOG.error("getGoldFirstMyth", e);
        }
        return bean;
    }

    /**
     * 金币场财富排行榜信息构建
     */
    private Map<String, Object> buildWealthFirstMythMap(Map<String, Object> bean, int rank, RankEvent rankEvent) {
        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("rank", rank);
        msgMap.put("userId", bean.get("userId"));
        msgMap.put("name", bean.get("userNickname"));
        msgMap.put("val", String.valueOf(bean.get("allGold")));
        if (rank > 0 && rankEvent != null) {
            msgMap.put("award", rankEvent.getAward(rank));
        } else {
            msgMap.put("award", 0);
        }
        msgMap.put("icon", bean.get("headimgurl"));
        msgMap.put("signature", bean.get("signature"));
        return msgMap;
    }

    public static String buildGold(long gold) {
        if (gold >= 100000000){
            double number = new BigDecimal((float)gold/100000000).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            return number + "亿";
        } else if (gold >= 10000) {
            double  number = new BigDecimal((float)gold/10000).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            return number + "万";
        }
        return String.valueOf(gold);
    }

    /**
     * 得到金币场财富排行榜
     */
    private List<Map<String, Object>> getWealthFirstMythList(int func, int day, RankEvent rankEvent) {
        List<Map<String, Object>> result;
        Map<String, Object> sqlMap = new HashMap<>();
        sqlMap.put("sort", "desc");
        sqlMap.put("limit", rankEvent.getRankCount());
        try {
            result = (List<Map<String, Object>>) getSql().queryForList("gold_firstmyth.getWealthFirstMythList", sqlMap);
        } catch (SQLException e) {
            result = new ArrayList<>();
            GameBackLogger.SYS_LOG.error("rankingList err ", e);
        }
        return result;
    }

    private FirstMyth getMyWealthFirstMyth(long userId) {
        FirstMyth bean = null;
        try {
            bean = (FirstMyth) getSql().queryForObject("gold_firstmyth.getSelfWealthRanking", userId);
        } catch (SQLException e) {
            GameBackLogger.SYS_LOG.error("getFirstMyth", e);
        }
        return bean;
    }
}
