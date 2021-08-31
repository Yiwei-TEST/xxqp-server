package com.sy599.game.jjs.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.HttpUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.character.Player;
import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.dao.TableCheckDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.db.enums.DbEnum;
import com.sy599.game.db.enums.UserMessageEnum;
import com.sy599.game.jjs.bean.MatchBean;
import com.sy599.game.jjs.bean.MatchUser;
import com.sy599.game.jjs.dao.MatchDao;
import com.sy599.game.manager.MarqueeManager;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.message.MessageUtil;
import com.sy599.game.util.Constants;
import com.sy599.game.util.GoldRoomUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 晋级赛工具类
 */
public final class JjsUtil {

    /**
     * 除已报名的人外可以看到该比赛场当前人数的玩家集 比赛场id__没加入比赛场但可见比赛场局数的玩家
     */
//    private static final  Map<Long, List<Player>> playerMap = new ConcurrentHashMap<>();
    /**
     * 检测是否有比赛场的标识
     */
    private static int matchMark = -1;

//    public static List<Player> getPlayersByMatchId(long matchId){
//        return playerMap.get(matchId);
//    }
//
//    public static void addPlayerByMatchId(long matchId, Player player){
//        List<Player> players = playerMap.get(matchId);
//        if(players == null){
//            players = new ArrayList<Player>();
//        }
//        if(!players.contains(player)){
//            players.add(player);
//        }
//        playerMap.put(matchId, players);
//    }
//
//    public static void removePlayerByMatchId(long matchId, Player player){
//        List<Player> players = playerMap.get(matchId);
//        if(players!= null && players.contains(player)){
//            players.remove(player);
//            playerMap.put(matchId, players);
//        }
//    }
//
//    public static Map<Long, List<Player>> getPlayerMap() {
//        return playerMap;
//    }

    public static class Msg {
        private final long time;
        private final String startDate;
        private final String endDate;

        Msg(long time, String startDate, String endDate) {
            this.time = time;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public long getTime() {
            return time;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
        }
    }

    private static final Map<Long, MatchBean> JJS_MATCH_MAP = new ConcurrentHashMap<>();

    /**
     * 是否开启（大于等于0表示开启）
     *
     * @param timeConfig <br/>eg:<br/>yyyy-MM-dd HH:mm:ss_yyyy-MM-dd HH:mm:ss/20;
     *                   yyyy-MM-dd HH:mm:ss_HH:mm:ss/5,HH:mm:ss_HH:mm:ss/30;
     *                   dd HH:mm:ss_HH:mm:ss,HH:mm:ss_HH:mm:ss/20;
     *                   E HH:mm:ss_HH:mm:ss/10,HH:mm:ss_HH:mm:ss;
     * @return (ms)
     */
    public static Msg isOpen(String timeConfig) {
        return isOpen(new Date(), timeConfig);
    }

    /**
     * 是否开启（大于等于0表示开启）
     *
     * @param date
     * @param timeConfig <br/>eg:<br/>yyyy-MM-dd HH:mm:ss_yyyy-MM-dd HH:mm:ss/20;
     *                   yyyy-MM-dd HH:mm:ss_HH:mm:ss/5,HH:mm:ss_HH:mm:ss/30;
     *                   dd HH:mm:ss_HH:mm:ss,HH:mm:ss_HH:mm:ss/20;
     *                   E HH:mm:ss_HH:mm:ss/10,HH:mm:ss_HH:mm:ss;
     * @return (ms)
     */
    public static Msg isOpen(Date date, String timeConfig) {
        if (StringUtils.isNotBlank(timeConfig)) {
            Date startDate = null;
            Date endDate = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                String[] tcs = timeConfig.split(";");
                for (String tc : tcs) {
                    if (StringUtils.isNotBlank(tc)) {
                        if (tc.indexOf(" ") != tc.lastIndexOf(" ")) {
                            String[] se = tc.split("_");
                            if (se.length == 2 && se[0].length() >= 19 && se[1].length() >= 19) {
                                Date tempDate = sdf.parse(se[0].length() == 19 ? se[0] : se[0].substring(0, 19));
                                if (date.after(tempDate)
                                        && date.before(sdf.parse(se[1].length() == 19 ? se[1] : se[1].substring(0, 19)))) {
                                    return new Msg(se[1].length() <= 20 ? 0L : Long.parseLong(se[1].substring(20)) * 60 * 1000, null,null);
                                } else {
                                    if (date.before(tempDate)) {
                                        if (startDate == null || startDate.getTime() > tempDate.getTime()) {
                                            startDate = tempDate;
                                            endDate = sdf.parse(se[1].length() == 19 ? se[1] : se[1].substring(0, 19));
                                        }
                                    }
                                }
                            }
                        } else {
                            String[] se = tc.split(" ");
                            if (se.length == 2 && se[0].length() <= 10 && se[1].length() >= 17) {
                                switch (se[0].length()) {
                                    case 10:
                                        String[] se1 = se[1].split(",");
                                        for (String s : se1) {
                                            if (StringUtils.isNotBlank(s)) {
                                                String[] se2 = s.split("_");
                                                if (se2.length == 2 && se2[0].length() >= 8 && se2[1].length() >= 8) {
                                                    Date tempDate = sdf.parse(se[0] + " " + (se2[0].length() == 8 ? se2[0] : se2[0].substring(0, 8)));
                                                    if (date.after(tempDate)
                                                            && date.before(sdf.parse(se[0] + " " + (se2[1].length() == 8 ? se2[1] : se2[1].substring(0, 8))))) {
                                                        return new Msg(se2[1].length() <= 9 ? 0L : Long.parseLong(se2[1].substring(9)) * 60 * 1000, null,null);
                                                    } else {
                                                        if (date.before(tempDate)) {
                                                            if (startDate == null || startDate.getTime() > tempDate.getTime()) {
                                                                startDate = tempDate;
                                                                endDate = sdf.parse(se[0] + " " + (se2[1].length() == 8 ? se2[1] : se2[1].substring(0, 8)));
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        break;

                                    case 2:
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTime(date);
                                        if (calendar.get(Calendar.DAY_OF_MONTH) == Integer.parseInt(se[0].startsWith("0") ? se[0].substring(1) : se[0])) {
                                            String temp = CommonUtil.dateTimeToString(calendar.getTime(), "yyyy-MM-dd");
                                            se1 = se[1].split(",");
                                            for (String s : se1) {
                                                if (StringUtils.isNotBlank(s)) {
                                                    String[] se2 = s.split("_");
                                                    if (se2.length == 2 && se2[0].length() >= 8 && se2[1].length() >= 8) {
                                                        Date tempDate = sdf.parse(temp + " " + (se2[0].length() == 8 ? se2[0] : se2[0].substring(0, 8)));
                                                        if (date.after(tempDate)
                                                                && date.before(sdf.parse(temp + " " + (se2[1].length() == 8 ? se2[1] : se2[1].substring(0, 8))))) {
                                                            return new Msg(se2[1].length() <= 9 ? 0L : Long.parseLong(se2[1].substring(9)) * 60 * 1000, null,null);
                                                        } else {
                                                            if (date.before(tempDate)) {
                                                                if (startDate == null || startDate.getTime() > tempDate.getTime()) {
                                                                    startDate = tempDate;
                                                                    endDate = sdf.parse(temp + " " + (se2[1].length() == 8 ? se2[1] : se2[1].substring(0, 8)));
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            calendar = Calendar.getInstance();
                                            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(se[0].startsWith("0") ? se[0].substring(1) : se[0]));
                                            calendar.add(Calendar.MONTH, 1);
                                            se1 = se[1].split(",");
                                            Date tempDate = null;
                                            SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy-MM-dd");
                                            for (String s : se1) {
                                                if (StringUtils.isNotBlank(s)) {
                                                    String[] se2 = s.split("_");
                                                    if (se2.length == 2 && se2[0].length() >= 8 && se2[1].length() >= 8) {
                                                        tempDate = sdf.parse(sdf0.format(calendar.getTime()) + " " + (se2[0].length() == 8 ? se2[0] : se2[0].substring(0, 8)));
                                                        endDate = sdf.parse(sdf0.format(calendar.getTime()) + " " + (se2[1].length() == 8 ? se2[1] : se2[1].substring(0, 8)));
                                                        break;
                                                    }
                                                }
                                            }

                                            if (tempDate != null && (startDate == null || startDate.getTime() > tempDate.getTime())) {
                                                startDate = tempDate;
                                            }

                                        }
                                        break;

                                    case 1:
                                        calendar = Calendar.getInstance();
                                        calendar.setTime(date);

                                        if ("0".equals(se[0])){
                                            String temp = CommonUtil.dateTimeToString(calendar.getTime(), "yyyy-MM-dd");
                                            se1 = se[1].split(",");
                                            String tempTime = null;
                                            for (String s : se1) {
                                                if (StringUtils.isNotBlank(s)) {
                                                    String[] se2 = s.split("_");
                                                    if (se2.length == 2 && se2[0].length() >= 8 && se2[1].length() >= 8) {
                                                        tempTime = (se2[0].length() == 8 ? se2[0] : se2[0].substring(0, 8));
                                                        Date tempDate = sdf.parse(temp + " " + tempTime);
                                                        if (date.after(tempDate)
                                                                && date.before(sdf.parse(temp + " " + (se2[1].length() == 8 ? se2[1] : se2[1].substring(0, 8))))) {
                                                            return new Msg(se2[1].length() <= 9 ? 0L : Long.parseLong(se2[1].substring(9)) * 60 * 1000, null,null);
                                                        } else {
                                                            if (date.before(tempDate)) {
                                                                if (startDate == null || startDate.getTime() > tempDate.getTime()) {
                                                                    startDate = tempDate;
                                                                    endDate = sdf.parse(temp + " " + (se2[1].length() == 8 ? se2[1] : se2[1].substring(0, 8)));
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            if (startDate == null && tempTime!=null){
                                                Calendar cal = Calendar.getInstance();
                                                cal.add(Calendar.DAY_OF_YEAR,1);
                                                startDate = sdf.parse(CommonUtil.dateTimeToString(cal.getTime(), "yyyy-MM-dd")+" "+tempTime);
                                            }
                                        }else if (calendar.get(Calendar.DAY_OF_WEEK) == Integer.parseInt(se[0])) {
                                            String temp = CommonUtil.dateTimeToString(calendar.getTime(), "yyyy-MM-dd");
                                            se1 = se[1].split(",");
                                            for (String s : se1) {
                                                if (StringUtils.isNotBlank(s)) {
                                                    String[] se2 = s.split("_");
                                                    if (se2.length == 2 && se2[0].length() >= 8 && se2[1].length() >= 8) {
                                                        Date tempDate = sdf.parse(temp + " " + (se2[0].length() == 8 ? se2[0] : se2[0].substring(0, 8)));
                                                        if (date.after(tempDate)
                                                                && date.before(sdf.parse(temp + " " + (se2[1].length() == 8 ? se2[1] : se2[1].substring(0, 8))))) {
                                                            return new Msg(se2[1].length() <= 9 ? 0L : Long.parseLong(se2[1].substring(9)) * 60 * 1000, null,null);
                                                        } else {
                                                            if (date.before(tempDate)) {
                                                                if (startDate == null || startDate.getTime() > tempDate.getTime()) {
                                                                    startDate = tempDate;
                                                                    endDate = sdf.parse(temp + " " + (se2[1].length() == 8 ? se2[1] : se2[1].substring(0, 8)));
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            calendar = Calendar.getInstance();
                                            calendar.set(Calendar.DAY_OF_WEEK, Integer.parseInt(se[0]));
                                            calendar.add(Calendar.WEEK_OF_YEAR, 1);
                                            se1 = se[1].split(",");
                                            Date tempDate = null;
                                            SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy-MM-dd");
                                            for (String s : se1) {
                                                if (StringUtils.isNotBlank(s)) {
                                                    String[] se2 = s.split("_");
                                                    if (se2.length == 2 && se2[0].length() >= 8 && se2[1].length() >= 8) {
                                                        tempDate = sdf.parse(sdf0.format(calendar.getTime()) + " " + (se2[0].length() == 8 ? se2[0] : se2[0].substring(0, 8)));
                                                        break;
                                                    }
                                                }
                                            }

                                            if (tempDate != null && (startDate == null || startDate.getTime() > tempDate.getTime())) {
                                                startDate = tempDate;
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception timeConfig is error:" + e.getMessage(), e);
//                e.printStackTrace();
            }

            return new Msg(-1, startDate == null ? null : sdf.format(startDate),endDate == null ? null : sdf.format(endDate));
        }
        return new Msg(-1, null,null);
    }

    /**
     * 是否已经结束
     *
     * @param matchBean
     * @return
     */
    public static boolean isOver(MatchBean matchBean) {
        return "2".equals(matchBean.getCurrentState()) || "3".equals(matchBean.getCurrentState());
    }

    public static MatchBean loadMatch(Long matchId) {
        return matchId == null ? null : JJS_MATCH_MAP.get(matchId);
    }

    public static MatchBean loadMatch(GoldRoom goldRoom) {
        String modeId = goldRoom.getModeId();
        return modeId.startsWith("match") ? JJS_MATCH_MAP.get(Long.valueOf(modeId.substring(5))) : null;
    }

    /**
     * 获取当前轮数的局数
     *
     * @param matchBean
     * @return
     */
    public static int loadMatchCurrentGameCount(MatchBean matchBean) {
        String matchRule = matchBean.getMatchRule();
        String currentState = matchBean.getCurrentState();
        if (currentState.startsWith("1_")) {
            String currentNo = currentState.substring(2);
            if (Integer.parseInt(currentNo) > 0) {
                String[] rules = matchRule.contains(";") ? matchRule.split("\\;")[1].split(",") : matchRule.split(",");
                //rule: 轮数_局数_晋级人数
                for (String rule : rules) {
                    String[] msgs = rule.split("\\_");
                    if (msgs.length >= 3 && currentNo.equals(msgs[0])) {
                        return Integer.parseInt(msgs[1]);
                    }
                }
            } else {
                return 1;
            }
        }
        return 1;
    }

    /**
     * 获取当前轮数的局数
     *
     * @param matchId
     * @return
     */
    public static int loadMatchCurrentGameCount(Long matchId) {
        MatchBean matchBean = loadMatch(matchId);
        if (matchBean != null) {
            return loadMatchCurrentGameCount(matchBean);
        }
        return 1;
    }

    /**
     * 获取该场的轮数
     *
     * @param matchBean
     * @return
     */
    public static int loadMatchCurrentGameNo(MatchBean matchBean) {
        String currentState = matchBean.getCurrentState();
        if (currentState.startsWith("1_")) {
            String currentNo = currentState.substring(2);
            return Integer.parseInt(currentNo);
        }
        return 0;
    }

    /**
     * 获取该场的轮数
     *
     * @param matchId
     * @return
     */
    public static int loadMatchCurrentGameNo(Long matchId) {
        MatchBean matchBean = loadMatch(matchId);
        if (matchBean != null) {
            return loadMatchCurrentGameNo(matchBean);
        }
        return 0;
    }

    /**
     * 获取本轮晋级人数
     *
     * @param matchBean
     * @return
     */
    public static int loadMatchCurrentGameWinCount(MatchBean matchBean, String num) {
        String matchRule = matchBean.getMatchRule();
        String currentState = matchBean.getCurrentState();
        if (currentState.startsWith("1_")) {
            String currentNo = StringUtils.isBlank(num) ? currentState.substring(2) : num;
            if (Integer.parseInt(currentNo) > 0) {
                String[] rules = matchRule.contains(";") ? matchRule.split("\\;")[1].split(",") : matchRule.split(",");
                //rule: 轮数_局数_晋级人数
                for (String rule : rules) {
                    String[] msgs = rule.split("\\_");
                    if (msgs.length >= 3 && currentNo.equals(msgs[0])) {
                        return Integer.parseInt(msgs[2]);
                    }
                }
            } else {
                String[] rules = matchRule.contains(";") ? matchRule.split("\\;")[0].split(",") : matchRule.split(",");
                for (String rule : rules) {
                    String[] msgs = rule.split("\\_");
                    if (msgs.length >= 2) {
                        return Integer.parseInt(msgs[1]);
                    }
                }
            }
        }
        return 0;
    }

    public static int loadMaxRestUserCount(MatchBean matchBean, String num) {
        String matchRule = matchBean.getMatchRule();
        String currentState = matchBean.getCurrentState();
        if (currentState.startsWith("1_")) {
            String currentNo = StringUtils.isBlank(num) ? currentState.substring(2) : num;
            if (Integer.parseInt(currentNo) > 0) {
                String[] rules = matchRule.contains(";") ? matchRule.split("\\;")[1].split(",") : matchRule.split(",");
                //rule: 轮数_局数_晋级人数
                for (String rule : rules) {
                    String[] msgs = rule.split("\\_");
                    if (msgs.length >= 3 && currentNo.equals(msgs[0])) {
                        return Integer.parseInt(msgs[2]);
                    }
                }
            } else {
                String[] rules = matchRule.contains(";") ? matchRule.split("\\;")[0].split(",") : matchRule.split(",");
                for (String rule : rules) {
                    String[] msgs = rule.split("\\_");
                    if (msgs.length >= 2) {
                        return Integer.parseInt(msgs[0]);
                    }
                }
            }
        }
        return 0;
    }

    /**
     * 获取本轮晋级人数
     *
     * @param matchId
     * @return
     */
    public static int loadMatchCurrentGameWinCount(Long matchId, String num) {
        MatchBean matchBean = loadMatch(matchId);
        if (matchBean != null) {
            return loadMatchCurrentGameWinCount(matchBean, num);
        }
        return 0;
    }

    /**
     * 比赛场淘汰分
     * @param matchBean  比赛场配置
     * @param matchRatio  倍率
     * @return
     */
    public static int loadMinScore(MatchBean matchBean, long matchRatio) {
        String temp = matchBean.loadExtFieldVal("loseRule1");
        int minScore = (int) (temp.startsWith("score_ratio") ? (matchRatio * Integer.parseInt(temp.substring(11)) / 100) : NumberUtils.toInt(temp, 0));
        return minScore;
    }

    /**
     * 是否有下一轮
     *
     * @param matchBean
     * @return
     */
    public static boolean hasNext(MatchBean matchBean) {
        String matchRule = matchBean.getMatchRule();
        String currentState = matchBean.getCurrentState();
        if (currentState.startsWith("1_")) {
            String currentNo = currentState.substring(2);
            if ("0".equals(currentNo)) {
                return true;
            }
            if (Integer.parseInt(currentNo) > 0) {
                String[] rules = matchRule.contains(";") ? matchRule.split("\\;")[1].split(",") : matchRule.split(",");
                //rule: 轮数_局数_晋级人数
                for (String rule : rules) {
                    String[] msgs = rule.split("\\_");
                    if (msgs.length >= 3 && Integer.parseInt(msgs[0]) > Integer.parseInt(currentNo)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 是否有下一轮
     *
     * @param matchId
     * @return
     */
    public static boolean hasNext(Long matchId) {
        MatchBean matchBean = loadMatch(matchId);
        if (matchBean != null) {
            return hasNext(matchBean);
        }
        return false;
    }

    public static int loadMatchRatio(MatchBean matchBean) {
        if (matchBean != null) {
            if (loadMatchCurrentGameNo(matchBean) > 0) {
                return matchBean.loadExtFieldIntVal("ratio2");
            } else {
                long time = System.currentTimeMillis();
                long time0 = matchBean.getStartTime().longValue();
                int t = (int) ((time - time0) / 1000 / matchBean.loadExtFieldIntVal("addTime1"));

                long ratio = matchBean.loadExtFieldIntVal("ratio1");
                if (t > 0) {
                    int addRatio1 = matchBean.loadExtFieldIntVal("addRatio1");
                    for (; t > 0; t--) {
                        ratio = ratio * addRatio1 / 100;
                    }
                }
                return (int) ratio;
            }
        }
        return 1;
    }

    public static int loadMatchRatio(Long matchId) {
        return loadMatchRatio(loadMatch(matchId));
    }

    private static void checkMarqueeMessage(MatchBean matchBean){
        String str = matchBean.loadExtFieldVal("marquee");
        if (StringUtils.isNotBlank(str)){
            matchBean.sort();
            try {
                JSONObject json = JSONObject.parseObject(str);
                int msgType = json.getIntValue("type");
                int round = json.getIntValue("round");
                if (round <= 0) {
                    round = 1;
                }
                int rank = json.getIntValue("rank");
                StringBuilder strBuilder = new StringBuilder();
                List<String> userMsgs = matchBean.loadUserMsgs();
                for (int i = 0, len = Math.min(rank, userMsgs.size()); i < len; i++) {
                    //userId，轮数，分数，排名
                    String[] msgs = userMsgs.get(i).split(",");
                    strBuilder.append("恭喜 ");
                    Long userId = Long.valueOf(msgs[0]);
                    Player player = PlayerManager.getInstance().getPlayer(userId);
                    if (player != null) {
                        strBuilder.append(player.getRawName());
                    } else {
                        Map<String, Object> user = UserDao.getInstance().loadUserBase(msgs[0]);
                        strBuilder.append(user!=null?user.get("userName"):msgs[0]);
                    }
                    strBuilder.append(" 在").append(matchBean.getMatchName()).append("中获得第").append(i+1).append("名");

                    String award = matchBean.loadAward(i+1);
                    if (player != null && StringUtils.isNotBlank(award)) {
                        String[] temps = award.split("\\,|\\+|\\;");
                        StringBuilder strBuilder0 = new StringBuilder();
                        for (String temp : temps) {
                            int num = NumberUtils.toInt(temp.replaceAll("\\D", ""), 1);
                            if (num>0) {
                                if (strBuilder0.length()==0){
                                    strBuilder0.append("，奖励");
                                }else{
                                    strBuilder0.append("，");
                                }
                                strBuilder0.append(num).append(temp.replaceAll("\\d|\\;|\\,|\\||\\*|\\_", ""));
                            }
                        }
                        if (strBuilder0.length()>0){
                            strBuilder.append(strBuilder0.toString());
                        }
                    }
                    strBuilder.append("! ");
                }

                final String content = strBuilder.toString();
                if (content.length()>0){
                    Map<String,String> map = new ConcurrentHashMap<>();
                    map.put("type","marquee");
                    map.put("userId","ALL");
                    map.put("message",content);
                    map.put("msgType",String.valueOf(msgType));
                    map.put("round",String.valueOf(round));
                    for (Map.Entry<Integer,Server> kv : ServerManager.loadAllServers().entrySet()){
                        if (kv.getKey().intValue()==GameServerConfig.SERVER_ID){
                            MarqueeManager.getInstance().sendMarquee(content,round,msgType,null);
                        }else{
                           marqueeNotice(map,kv.getValue());
                        }
                    }
                }

            }catch (Exception e){
                LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
            }
        }
    }

    private static void marqueeNotice(final Map<String,String> map,final Server server){
        TaskExecutor.EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    HttpUtil.getUrlReturnValue(ServerManager.loadRootUrl(server)+"/online/notice.do","UTF-8","POST",map,2);
                }catch (Exception e){
                }
            }
        });
    }

    public static void overMatch(MatchBean matchBean) {
        JJS_MATCH_MAP.remove(matchBean.getKeyId());
        LogUtil.msgLog.info("over match: matchId={},msg={}", matchBean.getKeyId(), JSON.toJSONString(matchBean));

        checkMarqueeMessage(matchBean);

        matchBean.clear();

        MatchDao.getInstance().deleteMatch(matchBean.getKeyId());

        if ("3".equals(matchBean.getMatchProperty())) {
            try {
                String temp = ResourcesConfigsUtil.loadServerPropertyValue("jjs_activity_time_config_" + matchBean.getMatchType());  //配置的比赛场时间
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = sdf.parse(matchBean.loadExtFieldVal("se").split(",")[1]);  //比赛场结束报名时间
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.MINUTE, 1);
                date = calendar.getTime();   //比赛场结束报名时间加上一分钟后的时间

                JjsUtil.Msg checkMsg = JjsUtil.isOpen(date, temp);
                boolean bl = false;
                if (checkMsg != null && checkMsg.getStartDate() != null) {
                    JSONObject json = JSONObject.parseObject(matchBean.getMatchExt());
                    String endDate = checkMsg.getEndDate();
                    if (endDate == null) {
                        Date tmpDate = sdf.parse(checkMsg.getStartDate());
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(tmpDate);
                        cal.add(Calendar.MINUTE, 40);
                        endDate = sdf.format(cal.getTime());
                    }
                    json.put("se", checkMsg.getStartDate() + "," + endDate);
                    matchBean.setMatchExt(json.toString());
                }else{
                    bl = true;
                }

                MatchBean copy = matchBean.copy();
                if (bl){
                    copy.setCurrentState("-1");
                }
                Server server = ServerManager.loadServer(copy.loadGameType(), JjsUtil.loadMatchServerType(copy.loadGameType()));
                copy.setServerId(server.getId());
                MatchDao.getInstance().save(copy);
            }catch (Exception e){
                LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
            }
        }
    }

    public static void putMatch(MatchBean matchBean) {
        JJS_MATCH_MAP.put(matchBean.getKeyId(), matchBean);

        LogUtil.msgLog.info("add match: matchId={},currentState={},restTableCount={}", matchBean.getKeyId(), matchBean.getCurrentState(), matchBean.getRestTable());
    }

    public static void sendMatchUserCount(MatchBean matchBean, Player player0) {
        String matchId = matchBean.getKeyId().toString();
        Object[] objs = new Object[]{4, matchBean.getMaxCount(), matchBean.getCurrentCount(), matchBean.getMaxCount() - matchBean.getCurrentCount(), matchBean.getMatchType(), matchBean.getKeyId().toString()};
        for (Map.Entry<Long, Player> kv : PlayerManager.playerMap.entrySet()) {
            Player player = kv.getValue();
            if (player.getIsOnline() == 1 && matchId.equals(player.getMatchId()) && player != player0) {
                player.writeComMessage(WebSocketMsgType.req_com_match_msg_code, objs);
            }
        }

//        for(Player player : getPlayersByMatchId(matchBean.getKeyId())){   //没报名但能看到报名人数
//            if (player.getIsOnline() == 1 && !matchId.equals(player.getMatchId())) {
//                player.writeComMessage(WebSocketMsgType.req_com_match_msg_code, objs);
//            }
//        }
    }

    public static boolean canRelive(Player player,MatchBean matchBean){
        String[] reliveMsgs = matchBean.loadReliveMsg();
        MatchUser matchUser = MatchDao.getInstance().selectOneMatchUser(matchBean.getKeyId().toString(),String.valueOf(player.getUserId()));
        if (reliveMsgs == null || reliveMsgs.length == 0) {
            return false;
        } else if (reliveMsgs.length <= matchUser.getReliveCount().intValue()) {
            return false;
        }else{
            return true;
        }
    }

    public static void loadMatchData() {
        try {
            if (TableCheckDao.getInstance().checkTableExists(DbEnum.LOGIN,"t_match")) {
                List<MatchBean> list = MatchDao.getInstance().selectPlayingMatchBeans(GameServerConfig.SERVER_ID);
                if (list != null && list.size() > 0) {
                    for (MatchBean matchBean : list) {
                        if (matchBean.getCurrentState().startsWith("1_")) {
                            matchBean.first(false);
                            matchBean.initTotalTable(matchBean.getRestTable().intValue());
                            JjsUtil.putMatch(matchBean);
                            List<HashMap<String, Object>> userList = MatchDao.getInstance().selectMatchUsers(matchBean.getKeyId().toString(), null);
                            int count0 = 0;
                            List<Long> userIds = new ArrayList<>();
                            if (userList != null) {
                                for (HashMap<String, Object> map : userList) {
                                    Long userId = CommonUtil.object2Long(map.get("userId"));
                                    Integer currentNo = CommonUtil.object2Int(map.get("currentNo"));
                                    Integer currentScore = CommonUtil.object2Int(map.get("currentScore"));
                                    Integer userRank = CommonUtil.object2Int(map.get("userRank"));
                                    matchBean.addUserMsg(userId.longValue(), currentNo.intValue(), currentScore.intValue(), userRank.intValue(), false);
                                    if (userRank.intValue() <= 0) {
                                        count0++;

                                        if ("-1".equals(map.get("currentState"))){
                                            userIds.add(userId);
                                        }
                                    }
                                }
                            }
                            matchBean.sort();
                            matchBean.initRestUserCount(count0);

                            Server server = ServerManager.loadServer(matchBean.getServerId());

                            for (Long userId : userIds){
                                if(MatchDao.getInstance().updateMatchUserState(matchBean.getKeyId(),userId,"1")>0) {
                                    Player player = PlayerManager.getInstance().getPlayer(userId);
                                    if (player == null) {
                                        player = PlayerManager.getInstance().loadPlayer(userId, matchBean.loadGameType());
                                    }
                                    GoldRoomUtil.joinGoldRoom(player, matchBean.loadGameType(), server.getServerType(), null, matchBean.getMatchType(), matchBean, false, null);
                                }
                            }

                            if ("1_0".equals(matchBean.getCurrentState())){
                                if (count0 <= loadMaxRestUserCount(matchBean,"0")) {
                                    doMatch0(matchBean,0,matchBean.loadGameType());
                                }
                            }

                            LogUtil.msgLog.info("load match msg from db: matchId={},currentNo={},rest tables count ={}", matchBean.getKeyId(), matchBean.getCurrentState(), matchBean.getRestTable().intValue());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("loadMatchData Exception:" + e.getMessage(), e);
        }
    }

    /**
     * 推送排名
     * @param matchBean
     */
    public static void sendRank(MatchBean matchBean){
        List<String> list = matchBean.loadAliveUserIds();
        int aliveCount = list.size();
        int gameNo = JjsUtil.loadMatchCurrentGameNo(matchBean);
        if (aliveCount>0){
            for (int i=0;i<aliveCount;i++){
                Player player = PlayerManager.getInstance().getPlayer(Long.valueOf(list.get(i)));
                if(player!=null) {
                    player.writeComMessage(WebSocketMsgType.req_com_match_msg_code, 5, gameNo, i+1, aliveCount);
                }
            }
        }
    }


    private static void startMatch0(final MatchBean matchBean) {
        try {
            synchronized (matchBean) {
                if (matchBean.getCurrentState().startsWith("1_") && matchBean.getTableCount().intValue() > 0) {
                    matchBean.sort(true);
                    int currentNo = Integer.parseInt(matchBean.getCurrentState().substring(2));
                    List<HashMap<String, Object>> userList = MatchDao.getInstance().selectMatchUsers(matchBean.getKeyId().toString(), String.valueOf(currentNo));
                    if (userList != null && userList.size() >= matchBean.getTableCount().intValue()) {
                        int count0 = userList.size();
                        Collections.shuffle(userList, new SecureRandom());
                        int tableCount = 0;
                        int userCount = 0;
                        int tableUserCount = matchBean.getTableCount();

                        Server server = ServerManager.loadServer(matchBean.getServerId());
//                            List<Integer> intsList = matchBean.loadTableInts();
                        int playType = matchBean.loadGameType();

                        while (count0 > 0) {
                            if (count0 < tableUserCount) {
                                break;
                            } else {
                                GoldRoom goldRoom = null;
                                for (int i = 0; i < tableUserCount; i++) {
                                    HashMap<String, Object> userMap = userList.get(userCount++);
                                    Long userId = CommonUtil.object2Long(userMap.get("userId"));
                                    Player player = PlayerManager.getInstance().getPlayer(userId);

                                    if (player == null) {
                                        player = PlayerManager.getInstance().loadPlayer(userId, playType);
                                    }

                                    count0--;

                                    int userScore;
                                    if (currentNo == 0) {
                                        userScore = matchBean.loadExtFieldIntVal("score1");
                                    } else if (currentNo == 1) {
                                        String score1 = matchBean.loadExtFieldVal("score2");
                                        if (score1.startsWith("score1_")) {
                                            userScore = (Integer.parseInt(score1.substring(7)) * matchBean.loadUserScore(userId.longValue()) / 100);
                                        } else {
                                            userScore = NumberUtils.toInt(score1, 0);
                                        }
                                    } else {
                                        userScore = matchBean.loadUserScore(userId.longValue());
                                    }

                                    HashMap<String, Object> map0 = new HashMap<>();
                                    map0.put("currentState", "1");
                                    map0.put("currentNo", currentNo);
                                    map0.put("currentScore", userScore);
                                    MatchDao.getInstance().updateMatchUser(matchBean.getKeyId(), userId, map0);
                                    matchBean.addUserMsg(userId.longValue(), currentNo, userScore, 0, false);
                                    goldRoom = GoldRoomUtil.joinGoldRoom(player, playType, server.getServerType(), null, "1", matchBean, i == 0, goldRoom);
                                }
                                tableCount++;
                            }
                        }

                        matchBean.initTotalTable(tableCount);
                        MatchDao.getInstance().updateRestTable(matchBean.getKeyId(), tableCount, true);
                        matchBean.initRestUserCount(userList.size());//userCount
                        if (count0 > 0) {
//                                    MatchDao.getInstance().updateUserCount(matchBean.getKeyId().toString(), userCount);
                            GoldRoom goldRoom = null;
                            for (int i = 0; i < count0; i++) {
                                HashMap<String, Object> userMap = userList.get(userCount + i);
                                Long userId = CommonUtil.object2Long(userMap.get("userId"));

                                LogUtil.msgLog.info("jjs out user:matchId={},userId={}"
                                        , matchBean.getKeyId(), userId);

//                                    MatchDao.getInstance().deleteMatchUser(matchBean.getKeyId().toString(), userId.toString());
//                                        HashMap<String, Object> map0 = new HashMap<>();
//                                        map0.put("currentState", "4");
//                                        MatchDao.getInstance().updateMatchUser(matchBean.getKeyId(), userId, map0);
//
                                Player player = PlayerManager.getInstance().getPlayer(userId);
                                if (player == null) {
                                    player = PlayerManager.getInstance().loadPlayer0(userId, playType);
                                }
//
//                                        player.quitMatch();
//                                        player.writeComMessage(WebSocketMsgType.req_com_match_code, matchBean.getMatchType(), "2", 2);

                                int userScore;
                                if (currentNo == 0) {
                                    userScore = matchBean.loadExtFieldIntVal("score1");
                                } else if (currentNo == 1) {
                                    String score1 = matchBean.loadExtFieldVal("score2");
                                    if (score1.startsWith("score1_")) {
                                        userScore = (Integer.parseInt(score1.substring(7)) * matchBean.loadUserScore(userId.longValue()) / 100);
                                    } else {
                                        userScore = NumberUtils.toInt(score1, 0);
                                    }
                                } else {
                                    userScore = matchBean.loadUserScore(userId.longValue());
                                }

                                HashMap<String, Object> map0 = new HashMap<>();
                                map0.put("currentState", "1");
                                map0.put("currentNo", currentNo);
                                map0.put("currentScore", userScore);
                                MatchDao.getInstance().updateMatchUser(matchBean.getKeyId(), userId, map0);
                                matchBean.addUserMsg(userId.longValue(), currentNo, userScore, 0, false);
                                goldRoom = GoldRoomUtil.joinGoldRoom(player, playType, server.getServerType(), null, "1", matchBean, i == 0, goldRoom);
                            }
                        }

                        LogUtil.msgLog.info("jjs start:matchId={}:{},userCount={},tableCount={},restUserCount={}"
                                , matchBean.getKeyId(), matchBean.getCurrentState(), userCount, tableCount, count0);

                        //推送排名
                        List<String> list = matchBean.loadAliveUserIds();
                        int aliveCount = list.size();
                        int gameNo = JjsUtil.loadMatchCurrentGameNo(matchBean);
                        if (aliveCount > 0) {
                            for (int i = 0; i < aliveCount; i++) {
                                Player player = PlayerManager.getInstance().getPlayer(Long.valueOf(list.get(i)));
                                if (player != null) {
                                    player.writeComMessage(WebSocketMsgType.req_com_match_msg_code, 5, gameNo, i + 1, aliveCount);
                                }
                            }
                        }
                    } else {
                        LogUtil.errorLog.error("match data error:user error:{}", JSON.toJSONString(matchBean));
                    }
                } else {
                    LogUtil.errorLog.error("match data error:match error:{}", JSON.toJSONString(matchBean));
                }
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("startMatch Exception:" + e.getMessage(), e);
        } finally {
            matchBean.first(false);
        }
    }

    /**
     * 比赛场开局
     *
     * @param matchBean
     */
    public static void startMatch(final MatchBean matchBean) {
        int delay = matchBean.loadExtFieldIntVal("delaySn");
        if (delay==0){
            delay=5;
        }

        if (matchBean.first()||delay<=0){
            TaskExecutor.EXECUTOR_SERVICE.execute(new Runnable() {
                @Override
                public void run() {
                    startMatch0(matchBean);
                }
            });
        }else{
            TaskExecutor.delayExecutor.schedule(new Runnable() {
                @Override
                public void run() {
                    startMatch0(matchBean);
                }
            }, delay, TimeUnit.SECONDS);
        }
    }

    public static String awardPlayer(MatchBean matchBean,Player player, String award,int rank,boolean sendAward) {
        String awardState = "0";
        if (player != null && StringUtils.isNotBlank(award)) {
            String[] temps = award.split("\\,|\\+|\\;");
            if (sendAward){
                StringBuilder strBuilder = new StringBuilder();
                for (String temp : temps) {
                    int num = NumberUtils.toInt(temp.replaceAll("\\D", ""), 1);
                    if (num>0) {
                        if (temp.contains("积分")) {
                            player.addJiFen(num, 2);
                            if ("0".equals(awardState)){
                                awardState="2";
                            }
                        } else if (temp.contains("金币")) {
                            player.changeGold(num, 0, false, 0, true);
                            if ("0".equals(awardState)){
                                awardState="2";
                            }
                        } else if (temp.contains("钻石")) {
                            player.changeCards(num, 0, true, 0, false, CardSourceType.match_award);
                            if ("0".equals(awardState)){
                                awardState="2";
                            }
                        } else if (temp.contains("电话")||temp.contains("手机")||temp.contains("话费")) {
                            awardState="1";
                        } else{
                            awardState="1";
                        }

                        if (strBuilder.length()==0){
                            strBuilder.append("，获得").append(num).append(temp.replaceAll("\\d|\\;|\\,|\\||\\*|\\_", ""));
                        }else{
                            strBuilder.append("，").append(num).append(temp.replaceAll("\\d|\\;|\\,|\\||\\*|\\_", ""));
                        }
                    }
                }
                if (strBuilder.length()>0){
                    strBuilder.insert(0,rank);
                    strBuilder.insert(0,"中排名第");
                    strBuilder.insert(0,matchBean.getMatchName());
                    strBuilder.insert(0,"恭喜您在");
                    MessageUtil.sendMessage(UserMessageEnum.TYPE3,player,strBuilder.toString(),award);
                }
            }else{
                for (String temp : temps) {
                    int num = NumberUtils.toInt(temp.replaceAll("\\D", ""), 1);
                    if (num>0) {
                        if (temp.contains("积分")) {
                            if ("0".equals(awardState)){
                                awardState="2";
                            }
                        } else if (temp.contains("金币")) {
                            if ("0".equals(awardState)){
                                awardState="2";
                            }
                        } else if (temp.contains("钻石")) {
                            if ("0".equals(awardState)){
                                awardState="2";
                            }
                        } else if (temp.contains("电话")||temp.contains("手机")||temp.contains("话费")) {
                            awardState="1";
                        } else{
                            awardState="1";
                        }
                    }
                }
            }
        }

        return awardState;
    }

    /**
     * 处理比赛场相关数据
     */
    public static void doMatch(Collection<Player> players,long matchRatio, MatchBean matchBean) {
        if (matchBean != null) {
            synchronized (matchBean) {
                boolean sendRank = false;
                try {
                    int currentNo = JjsUtil.loadMatchCurrentGameNo(matchBean);
//            for (Map.Entry<Long,Player> kv : getPlayerMap().entrySet()){
//                matchBean.addUserMsg(kv.getValue().getUserId(),currentNo,kv.getValue().loadScore(),true);
//            }
                    matchBean.sort();

                    if (currentNo > 0) {  //复赛
                        int rest = matchBean.add(-1);
                        LogUtil.msgLog.info("current match msg: matchId={},currentNo={},rest tables count ={}", matchBean.getKeyId(), currentNo, rest);

//                        MatchDao.getInstance().updateRestTable(matchBean.getKeyId(), 0, false);
                        MatchDao.getInstance().updateRestTable(matchBean.getKeyId(), rest>=0?rest:0, true);

                        int userCount = JjsUtil.loadMatchCurrentGameWinCount(matchBean, null);

                        if ("win_lose_score".equals(matchBean.loadExtFieldVal("loseRule2")) && matchBean.loadRestUserCount() > userCount) {
                            int tmpScore1 = 0, tmpScore2 = 0, tmpCount1 = 0, tmpCount2 = 0;
                            Player tmpUser1 = null, tmpUser2 = null;
                            for (Player mPlayer : players) {
                                if (tmpUser1 == null) {
                                    tmpUser1 = mPlayer;
                                    tmpUser2 = mPlayer;
                                    tmpScore1 = matchBean.loadUserScore(tmpUser1.getUserId());
                                    tmpScore2 = tmpScore1;
                                    tmpCount1 = 1;
                                    tmpCount2 = 1;
                                } else {
                                    int tempScore = matchBean.loadUserScore(mPlayer.getUserId());
                                    if (tempScore > tmpScore1) {
                                        tmpScore1 = tempScore;
                                        tmpUser1 = mPlayer;
                                        tmpCount1 = 1;
                                    } else if (tempScore == tmpScore1) {
                                        tmpCount1 += 1;
                                    }

                                    if (tempScore < tmpScore2) {
                                        tmpScore2 = tempScore;
                                        tmpUser2 = mPlayer;
                                        tmpCount2 = 1;
                                    } else if (tempScore == tmpScore2) {
                                        tmpCount2 += 1;
                                    }
                                }
                            }

                            if (tmpUser1 != tmpUser2) {
                                boolean bl = true;

                                if (tmpCount1 == 1) {
                                    Player player = tmpUser1;
                                    //第一名晋级
                                    matchBean.addUserMsg(player.getUserId(), currentNo, matchBean.loadUserScore(player.getUserId()), -1, false);
                                    LogUtil.msgLog.info(player.getName()+"晋级");
                                    LogUtil.msgLog.info("match rank0:userId={},matchId={},currentNo={}"
                                            , player.getUserId(), matchBean.getKeyId(), currentNo);

                                    int tempC;
                                    if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("xiaogan_match_quit"))){
                                        if (currentNo < 1){
                                            tempC=matchBean.getMinCount().intValue();
                                        }else{
                                            tempC=JjsUtil.loadMatchCurrentGameWinCount(matchBean, String.valueOf(currentNo-1));
                                        }
                                    }else{
                                        if (currentNo <= 1){
                                            tempC=matchBean.getMinCount().intValue();
                                        }else{
                                            tempC=JjsUtil.loadMatchCurrentGameWinCount(matchBean, String.valueOf(currentNo-1));
                                        }
                                    }

                                    //除每桌第一名外，其他玩家淘汰
                                    if (tempC/matchBean.getTableCount().intValue()>=userCount){
                                        bl = false;
                                        sendRank = true;
                                        TreeMap<Integer,Player> lossMap = new TreeMap<>();
                                        for (Player mPlayer : players) {
                                            if (mPlayer.getUserId()!=player.getUserId()) {
                                                lossMap.put(matchBean.loadUserScore(mPlayer.getUserId()),mPlayer);
                                            }
                                        }

                                        for (Map.Entry<Integer,Player> kv:lossMap.entrySet()){
                                            Player lossPlayer = kv.getValue();
                                            int myRank = matchBean.addRestUserCount(-1) + 1;
                                            int myScore = matchBean.loadUserScore(lossPlayer.getUserId());

                                            int[] rank = matchBean.loadUserRank(currentNo, String.valueOf(lossPlayer.getUserId()));
                                            boolean hasRank = matchBean.hasRank(lossPlayer.getUserId());
                                            matchBean.addUserMsg(lossPlayer.getUserId(), currentNo, myScore, myRank, false);
//                                            player.writeErrMsg(player.getName()+ myRank + "名，淘汰");
                                            LogUtil.msgLog.info(player.getName()+ myRank + "名，淘汰");
                                            String award = matchBean.loadAward(myRank);

                                            String awardState = awardPlayer(matchBean,lossPlayer, award,myRank,!hasRank);
                                            if (!hasRank) {
                                                lossPlayer.writeComMessage(WebSocketMsgType.req_com_match_msg_code, 0, currentNo, myRank, rank[1], award);
                                            }

                                            lossPlayer.quitMatch();

                                            LogUtil.msgLog.info("match rank1:userId={},matchId={},currentNo={},score={},rank={}:{}/{},award={}"
                                                    , lossPlayer.getUserId(), matchBean.getKeyId(), currentNo, myScore, myRank, myRank, rank[1], award);

                                            HashMap<String, Object> map0 = new HashMap<>();
                                            map0.put("currentState", "4");
                                            map0.put("currentScore", myScore);
                                            map0.put("userRank", myRank);
                                            if (StringUtils.isNotBlank(award)) {
                                                map0.put("userAward", award);
                                                map0.put("awardState", awardState);
                                            }

                                            MatchDao.getInstance().updateMatchUser(matchBean.getKeyId(), lossPlayer.getUserId(), map0);
                                        }
                                    }
                                }

                                if (bl && tmpCount2 == 1) {
                                    sendRank = true;
                                    //最后一名淘汰
                                    int myRank = matchBean.addRestUserCount(-1) + 1;
                                    int myScore = matchBean.loadUserScore(tmpUser2.getUserId());

                                    Player player = tmpUser2;

                                    int[] rank = matchBean.loadUserRank(currentNo, String.valueOf(player.getUserId()));
                                    boolean hasRank = matchBean.hasRank(player.getUserId());
                                    matchBean.addUserMsg(player.getUserId(), currentNo, myScore, myRank, false);
//                                    player.writeErrMsg(player.getName()+"第" + myRank + "名，淘汰");
                                    LogUtil.msgLog.info(player.getName()+"第" + myRank + "名，淘汰");
                                    String award = matchBean.loadAward(myRank);

                                    String awardState = awardPlayer(matchBean,player, award,myRank,!hasRank);
                                    if (!hasRank) {
                                        player.writeComMessage(WebSocketMsgType.req_com_match_msg_code, 0, currentNo, myRank, rank[1], award);
                                    }

                                    player.quitMatch();

                                    LogUtil.msgLog.info("match rank1:userId={},matchId={},currentNo={},score={},rank={}:{}/{},award={}"
                                            , player.getUserId(), matchBean.getKeyId(), currentNo, myScore, myRank, myRank, rank[1], award);

                                    HashMap<String, Object> map0 = new HashMap<>();
                                    map0.put("currentState", "4");
                                    map0.put("currentScore", myScore);
                                    map0.put("userRank", myRank);
                                    if (StringUtils.isNotBlank(award)) {
                                        map0.put("userAward", award);
                                        map0.put("awardState", awardState);
                                    }

                                    MatchDao.getInstance().updateMatchUser(matchBean.getKeyId(), player.getUserId(), map0);
                                }

                                if (tmpCount1 == 1 || tmpCount2 == 1)
                                    matchBean.sort();
                            }
                        }

                        if (rest <= 0) {
                            if (userCount > 0 && JjsUtil.hasNext(matchBean)) {
                                int currentNo0 = currentNo;
                                currentNo = currentNo + 1;
                                MatchDao.getInstance().updateMatch(matchBean.getKeyId().toString(), currentNo);
                                matchBean.setCurrentState("1_" + currentNo);

                                List<String>[] lists = matchBean.loadUserMsgs(userCount);

                                int num = 0;
                                for (String str : lists[0]) {
                                    num++;
                                    String[] strs = str.split(",");
                                    long userId = Long.parseLong(strs[0]);
                                    int[] rank = matchBean.loadUserRank(currentNo0, strs[0]);
                                    Player player = PlayerManager.getInstance().getPlayer(userId);
                                    if (player != null) {
//                                        player.writeErrMsg(player.getName()+"_第" + num + "名，晋级");
                                        LogUtil.msgLog.info(player.getName()+"_第" + num + "名，晋级");
                                        player.writeComMessage(WebSocketMsgType.req_com_match_msg_code, 1, currentNo0, rank[0], rank[1]);
                                    }

                                    LogUtil.msgLog.info("match rank:userId={},matchId={},currentNo={},score={},rank={}:{}/{}"
                                            , strs[0], matchBean.getKeyId(), currentNo0, strs[2], num, rank[0], rank[1]);

                                    HashMap<String, Object> map0 = new HashMap<>();
                                    map0.put("currentState", "1");
                                    map0.put("currentNo", currentNo);
                                    MatchDao.getInstance().updateMatchUser(matchBean.getKeyId(), userId, map0);
                                }

                                for (String str : lists[1]) {
                                    num++;
                                    String[] strs = str.split(",");
                                    if (currentNo0 == Integer.parseInt(strs[1])) {
                                        sendRank = true;
                                        long userId = Long.parseLong(strs[0]);
                                        int[] rank = matchBean.loadUserRank(currentNo0, strs[0]);

                                        Player player = PlayerManager.getInstance().getPlayer(userId);
                                        String award = matchBean.loadAward(rank[0]);
                                        if (player != null) {
                                            LogUtil.msgLog.info(player.getName()+"_第" + num + "名，淘汰");
//                                            System.err.println(player.getName()+"_第" + num + "名，淘汰");
                                            if (Integer.parseInt(strs[3]) <= 0)
                                                player.writeComMessage(WebSocketMsgType.req_com_match_msg_code, 0, currentNo0, rank[0], rank[1], award);
                                        } else {
                                            player = PlayerManager.getInstance().loadPlayer0(userId, matchBean.loadGameType());
                                        }
                                        player.quitMatch();

                                        String awardState = awardPlayer(matchBean,player, award,rank[0],Integer.parseInt(strs[3])<=0);

                                        LogUtil.msgLog.info("match rank:userId={},matchId={},currentNo={},score={},rank={}:{}/{},award={}"
                                                , strs[0], matchBean.getKeyId(), currentNo, strs[2], num, rank[0], rank[1], award);

                                        HashMap<String, Object> map0 = new HashMap<>();
                                        map0.put("currentState", "4");
                                        map0.put("currentScore", strs[2]);
                                        map0.put("userRank", num);
                                        if (StringUtils.isNotBlank(award)) {
                                            map0.put("userAward", award);
                                            map0.put("awardState", awardState);
                                        }
                                        matchBean.addUserMsg(userId, currentNo0, Integer.parseInt(strs[2]), num, false);
                                        MatchDao.getInstance().updateMatchUser(matchBean.getKeyId(), userId, map0);
                                    }
                                }
                                // todo
                                // 发送名次信息

                                JjsUtil.startMatch(matchBean);
                            } else {
                                //比赛场结束
                                // todo
                                List<String>[] lists = matchBean.loadUserMsgs(0);

                                int num = 0;
                                for (String str : lists[0]) {
                                    num++;
                                    String[] strs = str.split(",");
                                    if (currentNo == Integer.parseInt(strs[1])) {
                                        long userId = Long.parseLong(strs[0]);

                                        Player player = PlayerManager.getInstance().getPlayer(userId);
                                        int[] rank = matchBean.loadUserRank(currentNo, strs[0]);
                                        String award = matchBean.loadAward(rank[0]);
                                        if (player != null) {
//                                            player.writeErrMsg("第" + num + "名");
//                                            System.err.println(player.getName()+"_第" + num + "名");
                                            LogUtil.msgLog.info(player.getName()+"_第" + num + "名");
                                            if (Integer.parseInt(strs[3]) <= 0)
                                                player.writeComMessage(WebSocketMsgType.req_com_match_msg_code, 2, currentNo, rank[0], rank[1], award);
                                        } else {
                                            player = PlayerManager.getInstance().loadPlayer0(userId, matchBean.loadGameType());
                                        }
                                        player.quitMatch();

                                        String awardState = awardPlayer(matchBean,player, award,rank[0],Integer.parseInt(strs[3])<=0);

                                        LogUtil.msgLog.info("match rank:userId={},matchId={},currentNo={},score={},rank={}:{}/{},award={}"
                                                , strs[0], matchBean.getKeyId(), currentNo, strs[2], num, rank[0], rank[1], award);

                                        HashMap<String, Object> map0 = new HashMap<>();
                                        map0.put("currentState", "2");
                                        map0.put("currentScore", strs[2]);
                                        map0.put("userRank", num);
                                        if (StringUtils.isNotBlank(award)) {
                                            map0.put("userAward", award);
                                            map0.put("awardState", awardState);
                                        }
                                        matchBean.addUserMsg(userId, currentNo, Integer.parseInt(strs[2]), num, false);
                                        MatchDao.getInstance().updateMatchUser(matchBean.getKeyId(), userId, map0);
                                    }
                                }

                                JjsUtil.overMatch(matchBean);
//                        MatchDao.getInstance().updateMatch(matchBean.getKeyId().toString(), -1);
                                MatchDao.getInstance().updateMatchUsers(matchBean.getKeyId(), "2");
                            }
                        } else {
                            for (String str : matchBean.loadAliveUserIds()) {
                                long userId = Long.parseLong(str.split(",")[0]);
                                Player player = PlayerManager.getInstance().getPlayer(userId);
                                if (player != null && player.getPlayingTableId() == 0L) {
//                                   System.err.println("还剩下" + rest + "桌");
                                   LogUtil.msgLog.info("还剩下" + rest + "桌");
                                    player.writeComMessage(WebSocketMsgType.req_com_match_msg_code, 3, currentNo, rest,matchBean.loadGameType());
                                }
                            }
                        }
                    } else {   //预赛
                        int restUserCount1 = matchBean.loadRestUserCount();
                        int maxRestUserCount = JjsUtil.loadMaxRestUserCount(matchBean, null);
                        //
                        if (restUserCount1 >= maxRestUserCount) {
                            List<Long> loserList;
                            if (restUserCount1 > maxRestUserCount) {

                                loserList = new ArrayList<>();
                                int minScore = loadMinScore(matchBean, matchRatio);  //获取淘汰分

                                if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("xiaogan_match_quit"))){  //小甘比赛场淘汰规则
                                    Map<Integer, List<Player>> map = new TreeMap<>();
                                    for (Player mPlayer : players) {
                                        int point = matchBean.loadUserScore(mPlayer.getUserId());
                                        List<Player> playerList ;
                                        if(map.get(point) != null && map.get(point).size() > 0){
                                            playerList = map.get(point);
                                            int index = 0;
                                            int mRank = matchBean.loadUserRank(0, String.valueOf(mPlayer.getUserId()))[0];  //当前玩家名次
                                            for(Player player : playerList){
                                                int rank = matchBean.loadUserRank(0, String.valueOf(player.getUserId()))[0];  //玩家名次
                                                if(mRank > rank){  //当前玩家名次 > 玩家名次
                                                    index = playerList.indexOf(player)+1;
                                                }
                                            }

                                            playerList.add(index, mPlayer);
                                        }else{
                                            playerList = new ArrayList<>(4);
                                            playerList.add(mPlayer);
                                        }
                                        map.put(point, playerList);
                                    }

                                    for (Map.Entry<Integer, List<Player>> kv : map.entrySet()) {
                                        if (kv.getKey().intValue() < minScore) {
                                            for(Player player :kv.getValue()) {
                                                restUserCount1 = matchBean.addRestUserCount(-1);
                                                if (restUserCount1 >= maxRestUserCount) {
                                                    sendRank = true;
                                                    //淘汰
                                                    loserList.add(player.getUserId());

                                                    int num = restUserCount1 + 1;
                                                    LogUtil.msgLog.info(player.getUserId()+"___第" + num + "名，淘汰");
                                                    int score00 = matchBean.loadUserScore(player.getUserId());

                                                    int[] rank = matchBean.loadUserRank(0, String.valueOf(player.getUserId()));
                                                    String award = matchBean.loadAward(rank[0]);
                                                    LogUtil.msgLog.info("match rank:userId={},matchId={},currentNo={},score={},rank={}:{}/{},award={}"
                                                            , player.getUserId(), matchBean.getKeyId(), currentNo, score00, num, rank[0], rank[1], award);

                                                    boolean hasNotRank = !matchBean.hasRank(player.getUserId());
                                                    if (hasNotRank) {
                                                        if (canRelive(player,matchBean)&&StringUtils.isBlank(award)){
                                                            player.writeComMessage(WebSocketMsgType.req_com_match_msg_code,  7, num, rank[1]);
                                                        }else{
                                                            player.writeComMessage(WebSocketMsgType.req_com_match_msg_code, 0, 0, num, rank[1], award);
                                                        }
                                                    }

                                                    String awardState = awardPlayer(matchBean,player, award,rank[0],hasNotRank);

                                                    player.quitMatch();

                                                    HashMap<String, Object> map0 = new HashMap<>();
                                                    map0.put("currentState", "4");
                                                    map0.put("currentScore", score00);
                                                    map0.put("userRank", num);
                                                    if (StringUtils.isNotBlank(award)) {
                                                        map0.put("userAward", award);
                                                        map0.put("awardState", awardState);
                                                    }
                                                    matchBean.addUserMsg(player.getUserId(), 0, score00, num, false);
                                                    MatchDao.getInstance().updateMatchUser(matchBean.getKeyId(), player.getUserId(), map0);
                                                } else {
                                                    matchBean.addRestUserCount(1);
                                                    break;
                                                }
                                            }
                                        } else {
                                            break;
                                        }
                                    }
                                }else{
                                    Map<Integer, Player> map = new TreeMap<>();
                                    for (Player mPlayer : players) {
                                        map.put(matchBean.loadUserScore(mPlayer.getUserId()), mPlayer);
                                    }

                                    for (Map.Entry<Integer, Player> kv : map.entrySet()) {
                                        if (kv.getKey().intValue() < minScore) {
                                            restUserCount1 = matchBean.addRestUserCount(-1);
                                            if (restUserCount1 >= maxRestUserCount) {
                                                sendRank = true;
                                                //淘汰
                                                loserList.add(kv.getValue().getUserId());

                                                Player player = kv.getValue();
                                                int num = restUserCount1 + 1;
//                                                System.err.println("第" + num + "名，淘汰");
                                                LogUtil.msgLog.info("第" + num + "名，淘汰");
                                                int score00 = matchBean.loadUserScore(player.getUserId());

                                                int[] rank = matchBean.loadUserRank(0, String.valueOf(player.getUserId()));
                                                String award = matchBean.loadAward(rank[0]);
                                                LogUtil.msgLog.info("match rank:userId={},matchId={},currentNo={},score={},rank={}:{}/{},award={}"
                                                        , player.getUserId(), matchBean.getKeyId(), currentNo, score00, num, rank[0], rank[1], award);

                                                boolean hasNotRank = !matchBean.hasRank(player.getUserId());
                                                if (hasNotRank) {
                                                    if (canRelive(player,matchBean)&&StringUtils.isBlank(award)){
                                                        player.writeComMessage(WebSocketMsgType.req_com_match_msg_code,  7, rank[0], rank[1]);
                                                    }else{
                                                        player.writeComMessage(WebSocketMsgType.req_com_match_msg_code, 0, 0, rank[0], rank[1], award);
                                                    }
                                                }

                                                String awardState = awardPlayer(matchBean,player, award,rank[0],hasNotRank);

                                                player.quitMatch();

                                                HashMap<String, Object> map0 = new HashMap<>();
                                                map0.put("currentState", "4");
                                                map0.put("currentScore", score00);
                                                map0.put("userRank", num);
                                                if (StringUtils.isNotBlank(award)) {
                                                    map0.put("userAward", award);
                                                    map0.put("awardState", awardState);
                                                }
                                                matchBean.addUserMsg(player.getUserId(), 0, score00, num, false);
                                                MatchDao.getInstance().updateMatchUser(matchBean.getKeyId(), player.getUserId(), map0);
                                            } else {
                                                matchBean.addRestUserCount(1);
                                                break;
                                            }
                                        } else {
                                            break;
                                        }
                                    }
                                }
                            } else {
                                loserList = null;
                            }

                            LogUtil.msgLog.info("current match msg:matchId={},matchRatio={},restUserCount1={},maxRestUserCount={}"
                                    , matchBean.getKeyId(), matchRatio, restUserCount1, maxRestUserCount);

                            if (restUserCount1 <= maxRestUserCount) {
                                if (sendRank){
                                    doMatch0(matchBean, currentNo, matchBean.loadGameType());
                                }else {
                                    sendRank = doMatch0(matchBean, currentNo, matchBean.loadGameType());
                                }
                            } else {
                                if (loserList != null) {
                                    Server server = ServerManager.loadServer(matchBean.getServerId());
                                    for (Player mPlayer : players) {
                                        if (!loserList.contains(mPlayer.getUserId())) {
                                            joinMatch(mPlayer, server, matchBean);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                }finally {
                    if (sendRank){
                        sendRank(matchBean);
                    }
                }
            }
        }
    }

    /**
     * 预赛进复赛
     * @param matchBean
     * @throws Exception
     */
    private static boolean doMatch0(MatchBean matchBean,int currentNo,int playType) throws Exception{
        //todo 没开桌的房间解散、开桌的房间等待打完游戏
        synchronized (Constants.GOLD_LOCK) {
            List<HashMap<String, Object>> goldRooms = GoldRoomDao.getInstance().loadRooms("match" + matchBean.getKeyId().toString(), "0", "1");
            if (goldRooms != null && goldRooms.size() > 0) {
                int tempCount = 0;
                List<String> roomList = new ArrayList<>();
                for (HashMap<String, Object> tempMap : goldRooms) {
                    String keyId = String.valueOf(tempMap.get("keyId"));
                    String state0 = String.valueOf(tempMap.get("currentState"));
                    if ("0".equals(state0)) {
                        roomList.add(keyId);
                        GoldRoomDao.getInstance().updateGoldRoom(Long.parseLong(keyId), 0, "3");
                        GoldRoomDao.getInstance().deleteGoldRoomUser(Long.parseLong(keyId), 0L);
                    } else if ("1".equals(state0)) {
                        tempCount++;
                    }
                }

                if (roomList.size() > 0) {
                    List<HashMap<String, Object>> goldRoomUsers = GoldRoomDao.getInstance().loadRoomUsers(roomList);
                    if (goldRoomUsers != null && goldRoomUsers.size() > 0) {
                        for (HashMap<String, Object> tempMap : goldRoomUsers) {
                            Long userId = CommonUtil.object2Long(tempMap.get("userId"));
                            Player player = PlayerManager.getInstance().getPlayer(userId);
                            if (player != null) {
                                player.setPlayingTableId(0);
                                player.saveBaseInfo();
                            } else {
                                player = PlayerManager.getInstance().loadPlayer(userId, playType);
                                if (player != null) {
                                    player.setPlayingTableId(0);
                                    player.saveBaseInfo();
                                }
                            }
                        }
                    }
                }

                if (tempCount > 0) {
                    matchBean.setRestTable(tempCount);
                    for (String str : matchBean.loadUserMsgs(0)[0]) {
                        String[] msgs = str.split(",");
                        if ("0".equals(msgs[3])) {
                            long userId = Long.parseLong(msgs[0]);

                            Player player = PlayerManager.getInstance().getPlayer(userId);
                            if (player != null && player.getPlayingTableId() == 0L) {
//                                                        player.writeErrMsg("还剩下" + tempCount + "桌，请等待");

                                player.writeComMessage(WebSocketMsgType.req_com_match_msg_code, 3, currentNo, tempCount,matchBean.loadGameType());
                            }
                        }
                    }
                    return false;
                }
            }
        }

        MatchDao.getInstance().updateMatch(matchBean.getKeyId().toString(), 1);

        List<String>[] lists = matchBean.loadUserMsgs(JjsUtil.loadMatchCurrentGameWinCount(matchBean, null));

        matchBean.setCurrentState("1_1");

        int num = 0;
        for (String str : lists[0]) {
            num++;
            String[] strs = str.split(",");
            long userId = Long.parseLong(strs[0]);
            int[] rank = matchBean.loadUserRank(0, strs[0]);

            Player player = PlayerManager.getInstance().getPlayer(userId);
            if (player != null) {
//                                        player.writeErrMsg("第" + num + "名，晋级");
                player.writeComMessage(WebSocketMsgType.req_com_match_msg_code, 1, 0, rank[0], rank[1]);
            }

            LogUtil.msgLog.info("match rank:userId={},matchId={},currentNo={},score={},rank={}:{}/{}"
                    , userId, matchBean.getKeyId(), currentNo, strs[2], num, rank[0], rank[1]);

            HashMap<String, Object> map0 = new HashMap<>();
            map0.put("currentState", "1");
            map0.put("currentNo", "1");
            MatchDao.getInstance().updateMatchUser(matchBean.getKeyId(), userId, map0);
        }

        boolean sendRank = false;
        for (String str : lists[1]) {
            num++;
            String[] strs = str.split(",");
            if (0 == Integer.parseInt(strs[1])) {
                sendRank = true;
                long userId = Long.parseLong(strs[0]);
                int[] rank = matchBean.loadUserRank(0, strs[0]);
                String award = matchBean.loadAward(rank[0]);
                Player player = PlayerManager.getInstance().getPlayer(userId);
                if (player != null) {
//                                            player.writeErrMsg("第" + num + "名，淘汰");

                    if (Integer.parseInt(strs[3]) <= 0)
                        player.writeComMessage(WebSocketMsgType.req_com_match_msg_code, 0, 0, rank[0], rank[1], award);
                } else {
                    player = PlayerManager.getInstance().loadPlayer0(userId, playType);
                }
                player.quitMatch();

                String awardState = awardPlayer(matchBean,player, award,rank[0],true);

                HashMap<String, Object> map0 = new HashMap<>();
                map0.put("currentState", "4");
                map0.put("currentScore", strs[2]);
                map0.put("userRank", num);
                if (StringUtils.isNotBlank(award)) {
                    map0.put("userAward", award);
                    map0.put("awardState", awardState);
                }
                matchBean.addUserMsg(userId, 0, Integer.parseInt(strs[2]), num, false);

                LogUtil.msgLog.info("match rank:userId={},matchId={},currentNo={},score={},rank={}:{}/{},award={}"
                        , userId, matchBean.getKeyId(), currentNo, strs[2], num, rank[0], rank[1], award);

                MatchDao.getInstance().updateMatchUser(matchBean.getKeyId(), userId, map0);
            }
        }
        // todo
        // 发送名次信息

        JjsUtil.startMatch(matchBean);

        return sendRank;
    }

    private static void joinMatch(final Player player, final Server server, final MatchBean matchBean) {
        //等待加入房间
        if (MatchDao.getInstance().updateMatchUserState(matchBean.getKeyId(),player.getUserId(),"-1")>0) {
            int delay = matchBean.loadExtFieldIntVal("delayJr");
            if (delay==0){
                delay=5;
            }
            if (delay>0) {
                TaskExecutor.delayExecutor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            synchronized (matchBean) {
                                if ((!matchBean.hasRank(player.getUserId())) && MatchDao.getInstance().updateMatchUserState(matchBean.getKeyId(), player.getUserId(), "1") > 0) {
                                    GoldRoomUtil.joinGoldRoom(player, matchBean.loadGameType(), server.getServerType(), null, matchBean.getMatchType(), matchBean, false, null);
                                }
                            }
                        } catch (Exception e) {
                            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                        }
                    }
                }, delay, TimeUnit.SECONDS);
            }else{
                try {
                    if ((!matchBean.hasRank(player.getUserId())) && MatchDao.getInstance().updateMatchUserState(matchBean.getKeyId(), player.getUserId(), "1") > 0) {
                        GoldRoomUtil.joinGoldRoom(player, matchBean.loadGameType(), server.getServerType(), null, matchBean.getMatchType(), matchBean, false, null);
                    }
                } catch (Exception e) {
                    LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                }
            }
        }
    }

    public static void startMatchAtFixedRate() {
        try {
            List<MatchBean> list = MatchDao.getInstance().selectMatchBeans(GameServerConfig.SERVER_ID, "2", "0");
            if (list != null && list.size() > 0) {
                for (MatchBean mb : list) {
                    boolean isOk = false;
                    int time1 = mb.loadExtFieldIntVal("t0");
                    String timeStr = mb.loadExtFieldVal("t1");
                    if (StringUtils.isNotBlank(timeStr) && time1 > 0) {
                        Calendar cal = Calendar.getInstance();
                        String[] times = timeStr.split(",");
                        int h = cal.get(Calendar.HOUR_OF_DAY);
                        int m = cal.get(Calendar.MINUTE);

                        for (String t : times) {
                            String[] hm = t.split("\\:");
                            if (hm.length == 2) {
                                int tmpH = Integer.parseInt(hm[0]);
                                int tmpM = Integer.parseInt(hm[1]);
                                if (tmpH == h && tmpM == m) {
                                    isOk = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (isOk) {
                        LogUtil.msgLog.info("check time match:{}", JSON.toJSONString(mb));
                        if (mb.getMinCount().intValue() <= mb.getCurrentCount().intValue()) {
                            //开始
                            long startTime = System.currentTimeMillis();
                            mb.setStartTime(startTime);
                            if (MatchDao.getInstance().updateMatchForStarted(mb) > 0) {
                                // todo 开始晋级赛
                                MatchBean matchBean = MatchDao.getInstance().selectOne(mb.getKeyId().toString());
                                JjsUtil.putMatch(matchBean);
                                JjsUtil.startMatch(matchBean);
                            }
                        } else {
                            //解散
                            int cardPay = JjsUtil.loadCardPay(mb);
                            MatchDao.getInstance().deleteMatch(mb.getKeyId());
                            List<HashMap<String, Object>> userList = MatchDao.getInstance().selectMatchUsers(mb.getKeyId().toString(), "0");
                            for (HashMap<String, Object> userMap : userList) {
                                Long userId = CommonUtil.object2Long(userMap.get("userId"));
                                Player player1 = PlayerManager.getInstance().getPlayer(userId);
                                if (player1 == null) {
                                    player1 = PlayerManager.getInstance().loadPlayer0(userId, mb.loadGameType());
                                }
                                if (player1 != null) {
                                    if (cardPay>0){
                                        player1.changeCards(cardPay,0,true,true,CardSourceType.match_fee_card_refund);
                                    }
                                    player1.quitMatch();
                                    player1.writeErrMsgs(WebSocketMsgType.req_com_match_code, "报名人数不足，比赛已取消");
                                }
                            }
                            MatchDao.getInstance().dissMatchUsers(mb.getKeyId(), "3");
                        }

                        MatchBean copy = mb.copy();
                        Server server = ServerManager.loadServer(copy.loadGameType(), JjsUtil.loadMatchServerType(copy.loadGameType()));
                        copy.setServerId(server.getId());
                        MatchDao.getInstance().save(copy);
                    }
                }
            }

            //固定时间开一场
            list = MatchDao.getInstance().selectMatchBeans(GameServerConfig.SERVER_ID, "3", "0");
            if (list != null && list.size() > 0) {
                SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                for (MatchBean mb : list) {
                    String se = mb.loadExtFieldVal("se");//开始时间,结束时间
                    String[] seDate = se.split(",");
//                    Date sDate = sdf0.parse(seDate[0]);
                    Date eDate = sdf0.parse(seDate[1]);

                    boolean canDiss = false;
                    if (eDate.getTime()<date.getTime()) {
                        canDiss = true;
                    }

                    if (canDiss && MatchDao.getInstance().deleteMatch(mb.getKeyId())>0/**MatchDao.getInstance().updateMatch(mb.getKeyId().toString(), "0", "3") > 0**/) {
                        //解散
                        int cardPay = JjsUtil.loadCardPay(mb);
                        List<HashMap<String, Object>> userList = MatchDao.getInstance().selectMatchUsers(mb.getKeyId().toString(), "0");
                        for (HashMap<String, Object> userMap : userList) {
                            Long userId = CommonUtil.object2Long(userMap.get("userId"));
                            Player player1 = PlayerManager.getInstance().getPlayer(userId);
                            if (player1 == null) {
                                player1 = PlayerManager.getInstance().loadPlayer0(userId, mb.loadGameType());
                            }
                            if (player1 != null) {
                                if (cardPay>0){
                                    player1.changeCards(cardPay,0,true,true,CardSourceType.match_fee_card_refund);
                                }
                                player1.quitMatch();
                                player1.writeErrMsgs(WebSocketMsgType.req_com_match_code, "报名人数不足，比赛已取消");
                            }
                        }
                        MatchDao.getInstance().dissMatchUsers(mb.getKeyId(), "3");

                        String temp = ResourcesConfigsUtil.loadServerPropertyValue("jjs_activity_time_config_" + mb.getMatchType());
                        JjsUtil.Msg checkMsg = JjsUtil.isOpen(date,temp);

                        boolean bl = false;
                        if (checkMsg!=null&&checkMsg.getStartDate()!=null){
                            JSONObject json = JSONObject.parseObject(mb.getMatchExt());
                            String endDate = checkMsg.getEndDate();
                            if (endDate==null){
                                Date tmpDate = sdf0.parse(checkMsg.getStartDate());
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(tmpDate);
                                cal.add(Calendar.MINUTE,40);
                                endDate = sdf0.format(cal.getTime());
                            }
                            json.put("se",checkMsg.getStartDate()+","+endDate);
                            mb.setMatchExt(json.toString());
                        }else{
                            bl = true;
                        }

                        MatchBean copy = mb.copy();
                        if (bl){
                            copy.setCurrentState("-1");
                        }
                        Server server = ServerManager.loadServer(copy.loadGameType(), JjsUtil.loadMatchServerType(copy.loadGameType()));
                        copy.setServerId(server.getId());
                        MatchDao.getInstance().save(copy);
                    }
                }
            }
        }catch (Exception e){
            LogUtil.errorLog.error("Exception:"+e.getMessage(),e);
        }
    }

    /**
     * 获取报名费--钻石
     * @param matchBean
     * @return
     */
    public static int loadCardPay(MatchBean matchBean){
        String pay= matchBean.getMatchPay();
        if (pay.contains("card")) {
            String[] pays = pay.split(",");
            for (String tempPay : pays) {
                if (tempPay.startsWith("card")) {
                    return Integer.parseInt(tempPay.substring(4));
                }
            }
        }
        return 0;
    }

    /**
     * 获取比赛场的服类型
     * @param playType
     * @return
     */
    public static int loadMatchServerType(int playType){
        Integer serverType = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","match_server_type"+playType);
        if (serverType == null){
            serverType = ResourcesConfigsUtil.loadIntegerValue("ServerConfig","match_server_type");
            if (serverType == null){
                return 1;
            }else{
                return serverType.intValue();
            }
        }else{
            return serverType.intValue();
        }
    }

    /**
     * 是否开启了比赛场
     * @return
     */
    public static boolean hasMatch(){
        if (matchMark == -1){
            matchMark = TableCheckDao.getInstance().checkTableExists0(DbEnum.LOGIN,"t_match")?1:0;
        }
        return matchMark >= 1;
    }
}
