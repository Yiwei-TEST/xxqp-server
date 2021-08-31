package com.sy599.game.gcommand.match;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CommonUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.UserShare;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.UserShareDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.jjs.bean.MatchBean;
import com.sy599.game.jjs.bean.MatchUser;
import com.sy599.game.jjs.dao.MatchDao;
import com.sy599.game.jjs.util.JjsUtil;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public class MatchCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {

        if (player.getMyExtend().isGroupMatch()) {
            player.writeErrMsg(LangMsg.code_257);
            return;
        }

        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<Integer> intsList = req.getParamsList();
        List<String> strsList = req.getStrParamsList();
        int intSize = intsList != null ? intsList.size() : 0;
        int strSize = strsList != null ? strsList.size() : 0;

        int type = intSize >= 1 ? intsList.get(0) : -1;
        String gameCode = strSize >= 1 ? strsList.get(0) : null;

        if (StringUtils.isBlank(gameCode)) {
            LogUtil.msgLog.info("match userId={},ints={},strs={},gameCode={}", player.getUserId(), intsList, strsList, gameCode);
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_225));
            return;
        }

        //加载可报名的场次
        if (type == 0) {
            player.loadGoldPlayer(true);
            int matchProperty = intSize >= 2 ? intsList.get(1) : -1;
            JSONObject jsonObject = new JSONObject();
            for (String gc : gameCode.split(",")) {
                if (StringUtils.isNotBlank(gc)) {
                    JSONArray jsonArray = new JSONArray();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String configs = ResourcesConfigsUtil.loadServerPropertyValue(gc);
                    if (StringUtils.isNotBlank(configs)) {
                        for (String config : configs.split(",")) {
                            if (StringUtils.isNotBlank(config)) {
                                String temp = ResourcesConfigsUtil.loadServerPropertyValue("jjs_activity_time_config_" + config);
                                JjsUtil.Msg checkMsg = JjsUtil.isOpen(temp);
                                MatchBean matchBean = MatchDao.getInstance().selectOne(config, matchProperty == 3 ? null : "0");
                                if (matchBean != null) {
                                    JSONObject json = (JSONObject) JSONObject.toJSON(matchBean);
                                    Calendar cal = Calendar.getInstance();
                                    Date date = cal.getTime();
                                    json.put("currentTime", date.getTime());

                                    if ("1".equals(matchBean.getCurrentState()) || matchBean.getCurrentState().startsWith("1_")) {
                                        json.put("open", 2);
                                    } else if (!"0".equals(matchBean.getCurrentState())) {
                                        continue;
                                    } else if ("3".equals(matchBean.getMatchProperty())) {
                                        String[] seDate = matchBean.loadExtFieldVal("se").split(",");
                                        if (date.after(sdf.parse(seDate[0])) && date.before(sdf.parse(seDate[1]))) {
                                            json.put("open", 1);
                                            if ("share".equals(matchBean.getMatchPay())) {
                                                String ymd = CommonUtil.dateTimeToString(date, "yyyy-MM-dd");
                                                json.put("shared", UserShareDao.getInstance().countUserShare(player.getUserId(), ymd + " 00:00:00", ymd + " 23:59:59", "match" + matchBean.getKeyId()));
                                            }
                                        } else if (date.before(sdf.parse(seDate[0]))) {
                                            json.put("open", 0);
                                            json.put("nextTime", seDate[0]);

                                            long t = sdf.parse(seDate[0]).getTime();
                                            json.put("matchTime", t);
                                            long t0 = System.currentTimeMillis();
                                            long rest = (t - t0) / 1000;
                                            if (rest >= 0 && rest <= matchBean.loadExtFieldIntVal("t0") * 60) {
                                                json.put("restTime", rest > 0 ? rest : 1);
                                            }
                                        } else {
                                            continue;
                                        }
                                    } else if (checkMsg.getTime() >= 0L) {
                                        if ("1".equals(matchBean.getMatchProperty())) {
                                            json.put("open", 1);
                                            if ("share".equals(matchBean.getMatchPay())) {
                                                String ymd = CommonUtil.dateTimeToString(date, "yyyy-MM-dd");
                                                json.put("shared", UserShareDao.getInstance().countUserShare(player.getUserId(), ymd + " 00:00:00", ymd + " 23:59:59", "match" + matchBean.getKeyId()));
                                            }
                                        } else if ("2".equals(matchBean.getMatchProperty())) {
                                            int time1 = matchBean.loadExtFieldIntVal("t0");
                                            String timeStr = matchBean.loadExtFieldVal("t1");
                                            if (StringUtils.isNotBlank(timeStr) && time1 > 0) {
                                                String ymd = CommonUtil.dateTimeToString(date, "yyyy-MM-dd");
                                                String[] times = timeStr.split(",");
                                                int h = cal.get(Calendar.HOUR_OF_DAY);
                                                int m = cal.get(Calendar.MINUTE);
                                                int s = cal.get(Calendar.SECOND);

                                                String matchTime = null;
                                                int tmpH = 0, tmpM = 0;
                                                for (String t : times) {
                                                    String[] hm = t.split("\\:");
                                                    if (hm.length == 2) {
                                                        tmpH = Integer.parseInt(hm[0]);
                                                        tmpM = Integer.parseInt(hm[1]);
                                                        if (tmpH > h || (tmpH == h && tmpM > m)) {
                                                            matchTime = t;
                                                            break;
                                                        }
                                                    }
                                                }
                                                if (matchTime == null) {
                                                    json.put("open", 0);
                                                    cal.add(Calendar.DAY_OF_YEAR, 1);
                                                    String tempDate0 = CommonUtil.dateTimeToString(cal.getTime(), "yyyy-MM-dd") + " " + times[0] + ":00";
                                                    json.put("nextTime", tempDate0);
                                                    json.put("matchTime", CommonUtil.stringToDateTime(tempDate0).getTime());
                                                } else {
                                                    json.put("nextTime", matchTime);
                                                    StringBuilder tmpBuilder = new StringBuilder(ymd).append(" ");
                                                    if (tmpH >= 10) {
                                                        tmpBuilder.append(tmpH).append(":");
                                                    } else {
                                                        tmpBuilder.append("0").append(tmpH).append(":");
                                                    }
                                                    if (tmpM >= 10) {
                                                        tmpBuilder.append(tmpM).append(":00");
                                                    } else {
                                                        tmpBuilder.append("0").append(tmpM).append(":00");
                                                    }
                                                    json.put("matchTime", CommonUtil.stringToDateTime(tmpBuilder.toString()).getTime());
                                                    int time2 = ((tmpH - h) * 60 + (tmpM - m)) * 60 - s;
                                                    int time3 = time2 - (time1 * 60);
                                                    if (time3 > 0) {
                                                        json.put("open", 0);
                                                    } else {
                                                        json.put("restTime", time2);
                                                        json.put("open", 1);

                                                        if ("share".equals(matchBean.getMatchPay())) {
                                                            json.put("shared", UserShareDao.getInstance().countUserShare(player.getUserId(), ymd + " 00:00:00", ymd + " 23:59:59", "match" + matchBean.getKeyId()));
                                                        }
                                                    }
                                                }
                                            } else {
                                                json.put("open", 0);
                                            }
                                        } else {
                                            json.put("open", 0);
                                        }
                                    } else {
                                        json.put("open", 0);
                                        if (checkMsg.getStartDate() != null) {
                                            if ("1".equals(matchBean.getMatchProperty())) {
                                                json.put("nextTime", checkMsg.getStartDate());
                                            } else if ("2".equals(matchBean.getMatchProperty())) {
                                                int time1 = matchBean.loadExtFieldIntVal("t0");
                                                String timeStr = matchBean.loadExtFieldVal("t1");
                                                if (StringUtils.isNotBlank(timeStr) && time1 > 0) {
                                                    String ymd = checkMsg.getStartDate().substring(0, 11);
                                                    String[] times = timeStr.split(",");
                                                    String tempDateStr = null;
                                                    for (String time : times) {
                                                        tempDateStr = ymd + time + ":00";
                                                        if (sdf.parse(tempDateStr).after(date)) {
                                                            break;
                                                        } else {
                                                            tempDateStr = null;
                                                        }
                                                    }
                                                    json.put("nextTime", tempDateStr != null ? tempDateStr : checkMsg.getStartDate());
                                                    json.put("matchTime", sdf.parse(tempDateStr != null ? tempDateStr : checkMsg.getStartDate()).getTime());
                                                }
                                            }
                                        }
                                    }

                                    jsonArray.add(json);
                                }
                            }
                        }
                    }

                    Collections.sort(jsonArray, new Comparator<Object>() {
                        @Override
                        public int compare(Object o1, Object o2) {
                            JSONObject json1 = (JSONObject) o1;
                            JSONObject json2 = (JSONObject) o2;

                            int t1 = json1.getIntValue("open");
                            int t2 = json2.getIntValue("open");

                            if (t1 != t2) {
                                if (t1 == 1) {
                                    return -1;
                                } else if (t2 == 1) {
                                    return 1;
                                } else if (t1 == 2) {
                                    return -1;
                                } else if (t2 == 2) {
                                    return 1;
                                } else if (t1 == 0) {
                                    return -1;
                                } else if (t2 == 0) {
                                    return 1;
                                } else if (t1 == 3) {
                                    return -1;
                                } else if (t2 == 3) {
                                    return 1;
                                } else {
                                    return 0;
                                }
                            } else {
                                String p1 = json1.getString("matchProperty");
                                String p2 = json2.getString("matchProperty");
                                if ("1".equals(p1)) {
                                    return 1;
                                }
                                if ("1".equals(p2)) {
                                    return 1;
                                }

                                long m1 = json1.getLongValue("matchTime");
                                long m2 = json2.getLongValue("matchTime");
                                if (m1 <= 0) {
                                    return -1;
                                } else if (m2 <= 0) {
                                    return 1;
                                } else {
                                    try {
                                        if (m1 < m2) {
                                            return -1;
                                        } else if (m1 > m2) {
                                            return 1;
                                        }
                                    } catch (Exception e) {
                                    }
                                }
                            }
                            return 0;
                        }
                    });

                    jsonObject.put(gc, jsonArray);
                }
            }
            jsonObject.put("matchId", player.getMatchId());
            player.writeComMessage(req.getCode(), type, jsonObject.toString(), CommonUtil.objectToString(player.getChannel()),gameCode);
        } else if (type == 1) {//报名
             LogUtil.msgLog.info("player join match:userId={},ints={},strs={}", player.getUserId(), intsList, strsList);

            int gameType = intSize >= 2 ? intsList.get(1) : 0;
            boolean isOpen = JjsUtil.isOpen(ResourcesConfigsUtil.loadServerPropertyValue("jjs_activity_time_config_" + gameCode)).getTime() >= 0;
            if (!isOpen) {
                player.writeErrMsg("比赛未开始，请稍后再来");
                return;
            }
            String matchIdStr = strSize >= 2 ? strsList.get(1) : "0";
            long matchId = NumberUtils.toLong(matchIdStr, 0);
            if (matchId <= 0) {
                LogUtil.msgLog.info("match userId={},ints={},strs={},matchId={}", player.getUserId(), intsList, strsList, matchIdStr);
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_225));
                return;
            }

            MatchUser mu = MatchDao.getInstance().selectPlayingMatchUser(String.valueOf(player.getUserId()));
            if (mu != null){
                MatchBean matchBean = JjsUtil.loadMatch(mu.getMatchId());
                if (matchBean==null){
                    matchBean=MatchDao.getInstance().selectOne(mu.getMatchId().toString());
                }
                if (matchBean!=null){
                    player.joinMatch(matchBean.getKeyId().toString(),true);
                    if (matchBean.getServerId().intValue()==GameServerConfig.SERVER_ID){
                        LogUtil.msgLog.warn("join match warn:matchId={},userId={}",matchBean.getKeyId(),player.getUserId());
                    }else{
                        loadBestServer(player, matchBean.getServerId().intValue(), matchBean);
                    }
                    return;
                }else{
                    MatchDao.getInstance().deleteMatchUser(mu.getMatchId().toString(),mu.getUserId());
                }
            }

            if (player.isPlayingMatch()) {
                if (matchIdStr.equals(player.getMatchId())) {
                    MatchBean matchBean = MatchDao.getInstance().selectOne(matchIdStr);
                    if (matchBean != null) {
                        player.writeComMessage(req.getCode(), type, matchBean.getMaxCount(), matchBean.getCurrentCount(), matchBean.getMaxCount() - matchBean.getCurrentCount(), matchBean.getMatchType(), matchBean.getKeyId().toString(), matchBean.loadAward().toString());
                    } else {
                        LogUtil.msgLog.info("match userId={},ints={},strs={},matchId={} is null", player.getUserId(), intsList, strsList, matchIdStr);
                        player.quitMatch();
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_229));
                    }
                    return;
                }
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_226));
                return;
            }

            if (player.getPlayingTableId() != 0L) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_227));
                return;
            }

            MatchBean matchBean = MatchDao.getInstance().selectOne(matchIdStr);

            if (matchBean == null) {
                LogUtil.msgLog.info("match userId={},ints={},strs={},matchId={} is null", player.getUserId(), intsList, strsList, matchIdStr);
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_229));
                return;
            }

            if ("1".equals(matchBean.getMatchProperty())) {

            } else if ("2".equals(matchBean.getMatchProperty())) {
                boolean bl = true;
                int time1 = matchBean.loadExtFieldIntVal("t0");
                String timeStr = matchBean.loadExtFieldVal("t1");
                if (StringUtils.isNotBlank(timeStr) && time1 > 0) {
                    Calendar cal = Calendar.getInstance();
                    String[] times = timeStr.split(",");
                    int h = cal.get(Calendar.HOUR_OF_DAY);
                    int m = cal.get(Calendar.MINUTE);
                    int s = cal.get(Calendar.SECOND);

                    String matchTime = null;
                    int tmpH = 0, tmpM = 0;
                    for (String t : times) {
                        String[] hm = t.split("\\:");
                        if (hm.length == 2) {
                            tmpH = Integer.parseInt(hm[0]);
                            tmpM = Integer.parseInt(hm[1]);
                            if (tmpH > h || (tmpH == h && tmpM > m)) {
                                matchTime = t;
                                break;
                            }
                        }
                    }
                    if (matchTime != null) {
                        int time2 = ((tmpH - h) * 60 + (tmpM - m)) * 60 - s;
                        int time3 = time2 - (time1 * 60);
                        if (time3 <= 0) {
                            bl = false;
                        }
                    }
                }

                if (bl) {
                    player.writeErrMsg("比赛未开始，请稍后再来");
                    return;
                }
            } else if ("3".equals(matchBean.getMatchProperty())) {
                if ("0".equals(matchBean.getCurrentState())) {
                } else if ("1".equals(matchBean.getCurrentState()) || matchBean.getCurrentState().startsWith("1_")) {
                    player.writeErrMsg("比赛已开始，请等待下一场");
                    return;
                } else if ("2".equals(matchBean.getCurrentState()) || "3".equals(matchBean.getCurrentState())) {
                    player.writeErrMsg("比赛已结束，请等待下一场");
                    return;
                } else {
                    String[] seDate = matchBean.loadExtFieldVal("se").split(",");
                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if (date.after(sdf.parse(seDate[0])) && date.before(sdf.parse(seDate[1]))) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_229));
                        return;
                    } else {
                        player.writeErrMsg("比赛未开始，请稍后再来");
                        return;
                    }
                }
            } else {
                player.writeErrMsg("比赛未开始，请稍后再来");
                return;
            }

            int restCount = matchBean.getMaxCount().intValue() - matchBean.getCurrentCount().intValue();
            int serverId = matchBean.getServerId().intValue();

            if (!"0".equals(matchBean.getCurrentState()) || restCount <= 0) {
                if ("1".equals(matchBean.getMatchProperty())) {
                    synchronized (MatchCommand.class) {
                        MatchBean mb = MatchDao.getInstance().selectOne(matchBean.getMatchType(), "0");
                        if (mb != null && mb.getCurrentCount().intValue() < mb.getMinCount().intValue()) {
                            matchBean = mb;
                        } else {
                            Server server = ServerManager.loadServer(gameType, JjsUtil.loadMatchServerType(gameType));
                            matchBean = matchBean.copy();
                            matchBean.setServerId(server.getId());
                            MatchDao.getInstance().save(matchBean);
                        }
                    }
                    matchId = matchBean.getKeyId();
                    matchIdStr = matchBean.getKeyId().toString();
                    serverId = matchBean.getServerId().intValue();
                } else {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_228));
                    return;
                }
            }

            if (serverId != GameServerConfig.SERVER_ID) {
                loadBestServer(player, serverId, matchBean);
                return;
            }

            String pay = matchBean.getMatchPay();

            int cardPay = 0;

            if (StringUtils.isNotBlank(pay)) {
                if ("0".equals(pay)) {
                    //免费赛
                } else {
                    if (pay.contains("share")) {
                        String currentDate = CommonUtil.dateTimeToString("yyyy-MM-dd");
                        int shareCount = UserShareDao.getInstance().countUserShare(player.getUserId(), currentDate + " 00:00:00", currentDate + " 23:59:59", "match" + matchBean.getKeyId());
                        if (shareCount <= 0) {
                            player.writeErrMsg("请先分享再来报名");
                            return;
                        }
                    }
                    if (pay.contains("card")) {
                        String[] pays = pay.split(",");
                        for (String tempPay : pays) {
                            if (tempPay.startsWith("card")) {
                                cardPay = Integer.parseInt(tempPay.substring(4));
                                break;
                            }
                        }
                        if (player.loadAllCards() < cardPay) {
                            player.writeErrMsg(LangMsg.code_diamond_err);
                            return;
                        } else if (!player.changeCards(0, -cardPay, true, gameType, false, CardSourceType.match_fee_card)) {
                            if (player.loadAllCards() < cardPay) {
                                player.writeErrMsg(LangMsg.code_diamond_err);
                                return;
                            } else if (!player.changeCards(0, -cardPay, true, gameType, false, CardSourceType.match_fee_card)) {
                                player.writeErrMsg(LangMsg.code_225);
                                return;
                            }
                        }
                    }
                }
            }

            boolean joinSuccess = false;

            try {
                MatchUser matchUser = MatchDao.getInstance().selectOneMatchUser(matchIdStr, String.valueOf(player.getUserId()));
                if (matchUser == null) {
                    matchUser = new MatchUser(String.valueOf(player.getUserId()), matchId, matchBean.getMatchType(), matchBean.getMatchRule().contains(";") ? 0 : 1);
                    if (MatchDao.getInstance().save(matchUser) != null) {
                        joinSuccess = true;
                        if (NumberUtils.toInt(ResourcesConfigsUtil.loadServerPropertyValue("test_match"), Integer.MAX_VALUE) <= matchBean.getCurrentCount().intValue() && matchBean.getMaxCount().intValue() > 3) {
                            synchronized (MatchCommand.class) {
                                int rest = matchBean.getMinCount().intValue() - matchBean.getCurrentCount().intValue() - 1;
                                long tempUserId = UserDao.getInstance().getMinId();
                                int max = 500;
                                for (; rest > 0; ) {
                                    max--;
                                    Player player1 = PlayerManager.getInstance().getPlayer(tempUserId);
                                    if (player1 == null) {
                                        player1 = PlayerManager.getInstance().loadPlayer(tempUserId, matchBean.loadGameType());
                                    }

                                    if (player1 != null && player1.getPlayingTableId() == 0L && !player1.isPlayingMatch() && player1.getName().startsWith("test")) {
                                        MatchDao.getInstance().save(new MatchUser(String.valueOf(tempUserId), matchId, matchBean.getMatchType(), matchBean.getMatchRule().contains(";") ? 0 : 1));
                                        player1.joinMatch(matchIdStr);
                                        matchBean.setCurrentCount(matchBean.getCurrentCount() + 1);
                                       // JjsUtil.removePlayerByMatchId(matchId, player);
                                        MatchDao.getInstance().updateUserCount(matchIdStr, true);
                                        rest--;
                                    }
                                    tempUserId++;

                                    if (max <= 0) {
                                        break;
                                    }
                                }
                            }
                        }

                        if (MatchDao.getInstance().updateUserCount(matchIdStr, true) > 0) {
                            player.joinMatch(matchIdStr);
                            matchBean.setCurrentCount(matchBean.getCurrentCount() + 1);
                           // JjsUtil.removePlayerByMatchId(matchId, player);
                            player.writeComMessage(req.getCode(), type, matchBean.getMaxCount(), matchBean.getCurrentCount(), matchBean.getMaxCount() - matchBean.getCurrentCount(), matchBean.getMatchType(), matchBean.getKeyId().toString(), matchBean.loadAward().toString());
                            JjsUtil.sendMatchUserCount(matchBean, player);
                            if ("1".equals(matchBean.getMatchProperty()) || "3".equals(matchBean.getMatchProperty())) {
                                synchronized (MatchCommand.class) {
                                    long startTime = System.currentTimeMillis();
                                    matchBean.setStartTime(startTime);
                                    if (MatchDao.getInstance().updateMatchForStarted(matchBean) > 0) {
                                        // todo 开始晋级赛
                                        matchBean = MatchDao.getInstance().selectOne(matchIdStr);
                                        JjsUtil.putMatch(matchBean);
                                        JjsUtil.startMatch(matchBean);
                                        MatchBean matchBean1;
                                        if ("1".equals(matchBean.getMatchProperty()) && ((matchBean1 = MatchDao.getInstance().selectOne(matchBean.getMatchType(), "0")) == null || matchBean1.getKeyId().longValue() == matchBean.getKeyId().longValue())) {
                                            Server server = ServerManager.loadServer(gameType, JjsUtil.loadMatchServerType(gameType));
                                            MatchBean mb = matchBean.copy();
                                            mb.setServerId(server.getId());
                                            MatchDao.getInstance().save(mb);
                                        }
                                    }
                                }
                            }
                            return;
                        } else {
                            MatchDao.getInstance().deleteMatchUser(matchIdStr, String.valueOf(player.getUserId()));
                            joinSuccess = false;
                            if ("1".equals(matchBean.getMatchProperty())) {
                                synchronized (MatchCommand.class) {
                                    Server server = ServerManager.loadServer(gameType, JjsUtil.loadMatchServerType(gameType));
                                    if (server.getId() == GameServerConfig.SERVER_ID) {
                                        matchBean = matchBean.copy();
                                        matchBean.setServerId(server.getId());
                                        matchBean.setCurrentCount(1);
                                        if (MatchDao.getInstance().save(matchBean) != null) {
                                            restCount = matchBean.getMaxCount().intValue() - matchBean.getCurrentCount().intValue();
                                            matchUser = new MatchUser(String.valueOf(player.getUserId()), matchBean.getKeyId(), matchBean.getMatchType(), matchBean.getMatchRule().contains(";") ? 0 : 1);
                                            if (MatchDao.getInstance().save(matchUser) != null) {
                                                joinSuccess = true;
                                                player.joinMatch(matchBean.getKeyId().toString());
                                                player.writeComMessage(req.getCode(), type, matchBean.getMaxCount(), matchBean.getCurrentCount(), restCount, matchBean.getMatchType(), matchBean.getKeyId().toString(), matchBean.loadAward().toString());
                                                JjsUtil.sendMatchUserCount(matchBean, player);
                                                return;
                                            } else {
                                                MatchDao.getInstance().updateUserCount(matchBean.getKeyId().toString(), false);
                                            }
                                        }
                                    } else {
                                        matchBean = matchBean.copy();
                                        matchBean.setServerId(server.getId());
                                        if (MatchDao.getInstance().save(matchBean) != null) {
                                            loadBestServer(player, serverId, matchBean);
                                            return;
                                        }
                                    }
                                }
                            } else {
                                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_225));
                                return;
                            }
                        }
                    }
                }
            } finally {
                if (!joinSuccess) {
                    if (cardPay > 0) {
                        player.changeCards(cardPay, 0, true, true, CardSourceType.match_fee_card_refund);
                    }
                }
            }
        } else if (type == 2) {//退赛
            long matchId = NumberUtils.toLong(strSize >= 2 ? strsList.get(1) : "0", 0);
            if (matchId <= 0) {
                LogUtil.msgLog.info("match userId={},ints={},strs={},matchId={}", player.getUserId(), intsList, strsList, strsList.get(1));
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_225));
                return;
            }
            String matchIdStr = String.valueOf(matchId);

            MatchBean matchBean = MatchDao.getInstance().selectOne(matchIdStr);

            if (matchBean == null) {
                LogUtil.msgLog.info("match userId={},ints={},strs={},match is null", player.getUserId(), intsList, strsList);
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_225));
                return;
            } else if ("-1".equals(matchBean.getCurrentState())) {
                if (MatchDao.getInstance().deleteMatchUser(matchIdStr, String.valueOf(player.getUserId())) > 0) {
                    MatchDao.getInstance().updateUserCount(matchIdStr, false);
                    player.quitMatch();

                    //退还报名费
                    int cardPay = JjsUtil.loadCardPay(matchBean);
                    if (cardPay > 0) {
                        player.changeCards(cardPay, 0, true, true, CardSourceType.match_fee_card_refund);
                    }

                    matchBean.setCurrentCount(matchBean.getCurrentCount() - 1);
                    player.writeComMessage(req.getCode(), type, matchBean.getMaxCount(), matchBean.getCurrentCount(), matchBean.getMaxCount() - matchBean.getCurrentCount(), matchBean.getMatchType(), "1");
                    JjsUtil.sendMatchUserCount(matchBean, player);
                    return;
                }
                return;
            }else if (!"0".equals(matchBean.getCurrentState())) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_230));
                return;
            }

            if (matchBean.getServerId().intValue() != GameServerConfig.SERVER_ID) {
                loadBestServer(player, matchBean.getServerId().intValue(), matchBean);
                return;
            }

            synchronized (MatchCommand.class) {
                if (JjsUtil.loadMatch(matchBean.getKeyId()) != null) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_230));
                    return;
                } else if (MatchDao.getInstance().deleteMatchUser(matchIdStr, String.valueOf(player.getUserId())) > 0) {
                    MatchDao.getInstance().updateUserCount(matchIdStr, false);
                    player.quitMatch();

                    //退还报名费
                    int cardPay = JjsUtil.loadCardPay(matchBean);
                    if (cardPay > 0) {
                        player.changeCards(cardPay, 0, true, true, CardSourceType.match_fee_card_refund);
                    }

                    matchBean.setCurrentCount(matchBean.getCurrentCount() - 1);
                    player.writeComMessage(req.getCode(), type, matchBean.getMaxCount(), matchBean.getCurrentCount(), matchBean.getMaxCount() - matchBean.getCurrentCount(), matchBean.getMatchType(), "1");
                   // JjsUtil.addPlayerByMatchId(matchId, player);
                    JjsUtil.sendMatchUserCount(matchBean, player);
                    return;
                }
            }

        } else if (type == 3) {//查看比赛信息
            long matchId = NumberUtils.toLong(strSize >= 2 ? strsList.get(1) : "0", 0);
            MatchBean matchBean = MatchDao.getInstance().selectOne(matchId, gameCode);

            if (matchBean != null) {
                player.writeComMessage(req.getCode(), type, matchBean.getMaxCount(), matchBean.getCurrentCount(), matchBean.getMaxCount() - matchBean.getCurrentCount(), matchBean.getMatchType(), matchBean.getKeyId().toString(), JSON.toJSONString(matchBean));
            }
        } else if (type == 4) {//复活
            long matchId = NumberUtils.toLong(strSize >= 2 ? strsList.get(1) : "0", 0);
            MatchBean matchBean = JjsUtil.loadMatch(matchId);
            if (matchBean == null) {
                player.writeErrMsgs(req.getCode(), "该比赛场已结束");
            } else if (JjsUtil.loadMatchCurrentGameNo(matchBean) != 0) {
                player.writeErrMsgs(req.getCode(), "当前场次不支持复活");
            } else {
                synchronized (matchBean) {
                    MatchUser matchUser = MatchDao.getInstance().selectOneMatchUser(matchBean.getKeyId().toString(), String.valueOf(player.getUserId()));
                    if (matchUser == null) {
                        player.writeErrMsgs(req.getCode(), "您未参赛，不能复活");
                        return;
                    } else if (!("3".equals(matchUser.getCurrentState()) || "4".equals(matchUser.getCurrentState()))) {
                        player.writeErrMsgs(req.getCode(), "您未出局，不能复活");
                        return;
                    }

                    String[] reliveMsgs = matchBean.loadReliveMsg();
                    if (reliveMsgs == null || reliveMsgs.length == 0) {
                        player.writeErrMsgs(req.getCode(), "当前赛事不支持复活");
                        return;
                    } else if (reliveMsgs.length <= matchUser.getReliveCount().intValue()) {
                        player.writeErrMsgs(req.getCode(), "您的复活次数已用完！");
                        return;
                    }

                    String reliveMsg = reliveMsgs[matchUser.getReliveCount().intValue()];
                    String[] rms = reliveMsg.split(",");
                    int card = 0;
                    int gold = 0;
                    for (String rm : rms) {
                        if (rm.startsWith("card")) {
                            card = Integer.parseInt(rm.substring(4));
                        } else if (rm.startsWith("gold")) {
                            gold = Integer.parseInt(rm.substring(4));
                        } else if (rm.length() > 0) {
                            player.writeErrMsgs(req.getCode(), "您不满足复活条件，复活失败！");
                            return;
                        }
                    }

                    if (card > 0 && player.loadAllCards() < card) {
                        player.writeErrMsgs(req.getCode(), "您的钻石不足，复活失败！");
                        return;
                    } else if (gold > 0 && player.loadAllGolds() < gold) {
                        player.writeErrMsgs(req.getCode(), "您的金币不足，复活失败！");
                        return;
                    }

                    if (JjsUtil.loadMatchCurrentGameNo(matchBean) != 0) {
                        player.writeErrMsgs(req.getCode(), "当前场次不支持复活");
                        return;
                    }
                    if (matchBean.loadAliveUserIds().size() <= JjsUtil.loadMaxRestUserCount(matchBean, null)) {
                        player.writeErrMsgs(req.getCode(), "即将进入下一场，不支持复活");
                        return;
                    }

                    if (card > 0) {
                        player.changeCards(0, -card, true, matchBean.loadGameType(), CardSourceType.match_recover);
                    }
                    if (gold > 0) {
                        player.changeGold(0, -gold, matchBean.loadGameType());
                    }

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("matchId", matchBean.getKeyId().toString());
                    map.put("userId", String.valueOf(player.getUserId()));
                    map.put("currentState", "1");
                    int currentNo = JjsUtil.loadMatchCurrentGameNo(matchBean);
                    map.put("currentNo", currentNo);
                    int currentScore = JjsUtil.loadMatchRatio(matchBean) * matchBean.loadExtFieldIntVal("aliveRatio");
                    map.put("currentScore", currentScore);
                    map.put("userAward", "");
                    map.put("awardState", "0");
                    map.put("userRank", "0");
                    map.put("addReliveCount", 1);

                    MatchDao.getInstance().updateMatchUser0(matchBean.getKeyId(), player.getUserId(), map);

                    matchBean.resetUserMsg(player.getUserId(), currentNo, currentScore, 0);
                    matchBean.sort();
                    matchBean.addRestUserCount(1);
                    LogUtil.msgLog.info("player relive success:{}", map);

                    GoldRoomUtil.joinGoldRoom(player, matchBean.loadGameType(), ServerManager.loadServer(GameServerConfig.SERVER_ID).getServerType(), null, matchBean.getMatchType(), matchBean, false, null);
                }

                player.writeComMessage(WebSocketMsgType.req_com_match_msg_code, 6, "恭喜您复活成功！");

            }
        } else if (type == 5) {//分享比赛信息
            String matchId = strSize >= 2 ? strsList.get(1) : "0";
            if (CommonUtil.isPureNumber(matchId) && Long.parseLong(matchId) > 0L) {
                UserShare userShare = new UserShare();
                userShare.setDiamond(1);
                userShare.setUserId(player.getUserId());
                userShare.setShareDate(new Date());
                userShare.setExtend("match" + matchId);
                UserShareDao.getInstance().addUserShare(userShare);

                player.writeComMessage(req.getCode(), type, matchId);
            }
        }
//        else if (type == 6){
//
//            LogUtil.msgLog.info("player toView match:userId={},ints={},strs={}", player.getUserId(), intsList, strsList);
//
//            boolean isOpen = JjsUtil.isOpen(ResourcesConfigsUtil.loadServerPropertyValue("jjs_activity_time_config_" + gameCode)).getTime() >= 0;
//            if (!isOpen) {
//                player.writeErrMsg("比赛未开始，请稍后再来");
//                return;
//            }
//            String matchIdStr = strSize >= 2 ? strsList.get(1) : "0";
//            long matchId = NumberUtils.toLong(matchIdStr, 0);
//            if (matchId <= 0) {
//                LogUtil.msgLog.info("match userId={},ints={},strs={},matchId={}", player.getUserId(), intsList, strsList, matchIdStr);
//                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_225));
//                return;
//            }
//
//            if (player.getPlayingTableId() != 0L) {
//                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_227));
//                return;
//            }
//
//            MatchBean matchBean = MatchDao.getInstance().selectOne(matchIdStr);
//
//            if (matchBean == null) {
//                LogUtil.msgLog.info("match userId={},ints={},strs={},matchId={} is null", player.getUserId(), intsList, strsList, matchIdStr);
//                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_229));
//                return;
//            }
//
//            int operation = intsList.get(1);
//            if(operation == 1){
//                int serverId = matchBean.getServerId().intValue();
//                if (serverId != GameServerConfig.SERVER_ID) {
//                    loadBestServer(player, serverId, matchBean);
//                }
//                JjsUtil.addPlayerByMatchId(matchId, player);
//                JjsUtil.sendMatchUserCount(matchBean, player);
//            }else{
//                JjsUtil.removePlayerByMatchId(matchId, player);
//            }
//        }
    }

    @Override
    public void setMsgTypeMap() {
    }


    private static final void loadBestServer(Player player, int serverId, MatchBean matchBean) {
        String[] gameUrls = null;
        boolean loadFromCheckNet = true;
        Server server = ServerManager.loadServer(serverId);

        if (server == null) {
            gameUrls = CheckNetUtil.loadGameUrl(serverId, player.getTotalCount());
            if (gameUrls != null) {
                server = new Server();
                server.setId(serverId);
                server.setChathost(gameUrls[0]);
                loadFromCheckNet = false;
            }
        }

        Map<String, Object> serverMap = new HashMap<>();
        serverMap.put("serverId", server.getId());

        if (loadFromCheckNet) {
            serverMap.put("httpUrl", server.getHost());
            gameUrls = CheckNetUtil.loadGameUrl(server.getId(), player.getTotalCount());
        }

        if (gameUrls == null) {
            serverMap.put("connectHost", server.getChathost());
            serverMap.put("connectHost1", "");
            serverMap.put("connectHost2", "");
        } else {
            serverMap.put("connectHost", StringUtils.isNotBlank(gameUrls[0]) ? gameUrls[0] : server.getChathost());
            serverMap.put("connectHost1", gameUrls[1]);
            serverMap.put("connectHost2", gameUrls[2]);
        }

        Map<String, Object> result = new HashMap<>();

        result.put("server", serverMap);
        result.put("blockIconTime", 0);
        result.put("code", 0);
        // 俱乐部快速加入切服返回桌子号
        result.put("tId", 0);

        String ret = JacksonUtil.writeValueAsString(result);
        LogUtil.msgLog.info("change server for match:userId={},ret={},matchId={},matchType={}", player.getUserId(), ret, matchBean.getKeyId(), matchBean.getMatchType());

        player.writeComMessage(WebSocketMsgType.res_code_getserverid, 1,
                ret, matchBean.getKeyId().toString(), matchBean.getMatchType());

    }

}
