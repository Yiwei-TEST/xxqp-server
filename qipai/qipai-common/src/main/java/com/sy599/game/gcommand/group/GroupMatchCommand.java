package com.sy599.game.gcommand.group;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.HttpUtil;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.group.GroupInfo;
import com.sy599.game.db.bean.group.GroupTableConfig;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.*;
import com.sy599.game.util.constants.GroupConstants;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;

public class GroupMatchCommand extends BaseCommand {
    @Override
    public void execute(Player player, MessageUnit message) throws Exception {

        if (player.isPlayingMatch()) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_240));
            return;
        }

        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<Integer> intsList = req.getParamsList();
        List<String> strsList = req.getStrParamsList();
        int intSize = intsList == null ? 0 : intsList.size();
        int strSize = strsList == null ? 0 : strsList.size();
        if (intSize == 0 || strSize == 0 || !CommonUtil.isPureNumber(strsList.get(0))) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return;
        }
        int type = intsList.get(0).intValue();
        String groupId = strsList.get(0);
        if (type == 1) {//加入

            if (player.getPlayingTableId()>0){
                player.writeErrMsg("您正在"+player.getPlayingTableId()+"牌桌中，不能报名！");
                return;
            }

            GroupUser groupUser = GroupDao.getInstance().loadGroupUser(player.getUserId(), groupId);
            if (groupUser == null) {
                player.writeErrMsg(LangMsg.code_63, groupId);
                return;
            } else if (GroupConstants.isForbidden(groupUser)) {
                player.writeErrMsg(LangMsg.code_258);
                return;
            }
            GroupInfo groupInfo = GroupDao.getInstance().loadGroupInfo(groupId, "0");

            if (groupInfo == null) {
                player.writeErrMsg(LangMsg.code_63, groupId);
                return;
            }

            GroupTableConfig groupTableConfig = GroupDao.getInstance().loadLastGroupTableConfig(groupInfo.getGroupId().longValue(), 0);

            if (groupTableConfig == null) {
                player.writeErrMsg("请联系群主设置游戏玩法");
                return;
            }

            if (loadGroupMatchConfig(groupInfo) <= -1) {
                player.writeErrMsg("该俱乐部未开启匹配模式，请稍后再试");
                return;
            }

            if (groupTableConfig.getPayType().intValue() == 2) {
                player.writeErrMsg("匹配模式不支持房主支付，请联系群主更改支付方式");
                return;
            }

            int playerCount = NumberUtils.toInt(ResourcesConfigsUtil.loadServerPropertyValue("group_match_mode" + groupTableConfig.getGameType(), "3"), 3);
            if (playerCount > 0 && playerCount != groupTableConfig.getPlayerCount().intValue()) {
                player.writeErrMsg("该匹配模式仅支持" + playerCount + "人玩法，请联系群主更改玩法");
                return;
            }

            HashMap<String, Object> map = GroupDao.getInstance().loadGroupMatch(player.getUserId());

            long restTime;
            if (map == null) {
                Long key = GroupDao.getInstance().saveGroupMatch(player.getUserId(), groupId);
                if (key != null && key.longValue() > 0) {
                    player.getMyExtend().setGroupMatch(true);
                    player.saveBaseInfo();
                }
                restTime = GroupRoomUtil.loadGroupMatchMinTimeForCancel();
            }else{
                restTime = GroupRoomUtil.loadGroupMatchMinTimeForCancel() - ((System.currentTimeMillis() - TimeUtil.object2Long(map.get("createdTime")))/1000);
            }

            List<HashMap<String, Object>> userList = GroupDao.getInstance().loadGroupMatchUsers(groupId, 30);
            List<Integer> list = checkGroupMatch(groupInfo, groupTableConfig, userList);

            player.writeComMessage(WebSocketMsgType.req_com_group_match, type, list, (int)restTime, groupId, String.valueOf(player.getUserId()));

            if (list.get(2).intValue() == 0) {
                startGroupMatch(groupInfo, groupTableConfig, String.valueOf(player.getUserId()));
            } else {
                sendMsg(groupInfo, groupTableConfig, userList, String.valueOf(player.getUserId()));
            }
        } else if (type == 2) {//离开

            if (player.getPlayingTableId()>0){
                player.writeErrMsg("您正在"+player.getPlayingTableId()+"牌桌中，不能退赛！");
                return;
            }

            HashMap<String, Object> map = GroupDao.getInstance().loadGroupMatch(player.getUserId());
            if (map == null) {
                player.getMyExtend().setGroupMatch(false);
                player.saveBaseInfo();
                player.writeComMessage(WebSocketMsgType.req_com_group_match, type, groupId);
                return;
            }

            long restTime = GroupRoomUtil.loadGroupMatchMinTimeForCancel() - ((System.currentTimeMillis() - TimeUtil.object2Long(map.get("createdTime")))/1000);
            if (restTime > 0){
                player.writeErrMsg("您正在进行匹配，"+restTime+"秒后可取消");
                return;
            }

            boolean bl = GroupDao.getInstance().lockGroupMatch(groupId);
            if (bl) {
                try {
                    if (GroupDao.getInstance().quitGroupMatch(player.getUserId()) > 0) {
                        player.getMyExtend().setGroupMatch(false);
                        player.saveBaseInfo();
                    }
                    player.writeComMessage(WebSocketMsgType.req_com_group_match, type, groupId);
                } catch (Exception e) {
                    LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                } finally {
                    GroupDao.getInstance().unlockGroupMatch(groupId);
                }

                GroupInfo groupInfo = GroupDao.getInstance().loadGroupInfo(groupId, "0");
                GroupTableConfig groupTableConfig = GroupDao.getInstance().loadLastGroupTableConfig(Long.parseLong(groupId), 0);

                if (groupInfo != null && groupTableConfig != null) {
                    List<HashMap<String, Object>> userList = GroupDao.getInstance().loadGroupMatchUsers(groupId, 30);
                    List<Integer> list = checkGroupMatch(groupInfo, groupTableConfig, userList);
                    if (list.get(2).intValue() == 0) {
                        startGroupMatch(groupInfo, groupTableConfig, String.valueOf(player.getUserId()));
                    } else {
                        sendMsg(groupInfo, groupTableConfig, userList, String.valueOf(player.getUserId()));
                    }
                }
            } else {
                player.writeErrMsg(LangMsg.code_225);
            }
        } else if (type == 3) {//查看
            GroupInfo groupInfo = GroupDao.getInstance().loadGroupInfo(groupId, "0");

            if (groupInfo == null) {
                player.writeErrMsg(LangMsg.code_63, groupId);
                return;
            }

            GroupTableConfig groupTableConfig = GroupDao.getInstance().loadLastGroupTableConfig(groupInfo.getGroupId().longValue(), 0);

            if (groupTableConfig == null) {
                player.writeErrMsg("请联系群主设置游戏玩法");
                return;
            }

            if (loadGroupMatchConfig(groupInfo) <= -1) {
                player.writeErrMsg("该俱乐部未开启匹配模式，请稍后再试");
                return;
            }

            List<HashMap<String, Object>> userList = GroupDao.getInstance().loadGroupMatchUsers(groupId, 30);
            List<Integer> list = checkGroupMatch(groupInfo, groupTableConfig, userList);

            player.writeComMessage(WebSocketMsgType.req_com_group_match, type, groupId, userList == null ? "[]" : JSON.toJSONString(userList), list);
        }
    }

    private static List<Integer> checkGroupMatch(GroupInfo groupInfo, GroupTableConfig groupTableConfig) throws Exception {
        List<Integer> list = new ArrayList<>(3);
        int min = Math.max(loadGroupMatchConfig(groupInfo), groupTableConfig.getPlayerCount());
        list.add(min);
        int current = GroupDao.getInstance().countGroupMatchUsers(groupInfo.getGroupId().toString());
        list.add(current);
        list.add(min > current ? (min - current) : 0);
        return list;
    }

    private static List<Integer> checkGroupMatch(GroupInfo groupInfo, GroupTableConfig groupTableConfig, List<HashMap<String, Object>> userList) throws Exception {
        List<Integer> list = new ArrayList<>(3);
        int min = Math.max(loadGroupMatchConfig(groupInfo), groupTableConfig.getPlayerCount());
        list.add(min);
        int current = userList == null ? 0 : userList.size();
        list.add(current);
        list.add(min > current ? (min - current) : 0);
        return list;
    }

    /**
     * 向报名的人推送消息
     *
     * @param list
     * @param mUserId
     */
    private static void sendMsg(GroupInfo groupInfo, GroupTableConfig groupTableConfig, List<HashMap<String, Object>> list, String mUserId) throws Exception {
        if (list == null || list.size() == 0) {
            return;
        } else {
            List<Integer> list1 = checkGroupMatch(groupInfo, groupTableConfig, list);
            ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(WebSocketMsgType.req_com_group_match, 1, list1, 0, groupInfo.getGroupId().toString(), mUserId);
//            GeneratedMessage msg = res.build();

            for (HashMap<String, Object> map : list) {
                String userId = String.valueOf(map.get("userId"));
                if (!userId.equals(mUserId)) {
                    res.setParams(4,(int)(GroupRoomUtil.loadGroupMatchMinTimeForCancel() - ((System.currentTimeMillis() - TimeUtil.object2Long(map.get("createdTime")))/1000)));
                    Long uid = Long.valueOf(userId);
                    Player player1 = PlayerManager.getInstance().getPlayer(uid);
                    if (player1 != null) {
                        player1.writeSocket(res.build());
                    } else {
                        Server server = ServerManager.loadServer(UserDao.getInstance().getUserServerId(userId));
                        if (server != null && server.getId() != GameServerConfig.SERVER_ID) {
                            Map<String, String> paramsMap = new HashMap<>();
                            paramsMap.put("type", "notice");
                            paramsMap.put("userId", userId);
                            paramsMap.put("code", String.valueOf(res.getCode()));
                            paramsMap.put("ints", JSON.toJSONString(res.getParamsList()));
                            paramsMap.put("strs", JSON.toJSONString(res.getStrParamsList()));
                            HttpUtil.getUrlReturnValue(ServerManager.loadRootUrl(server) + "/group/msg.do", "UTF-8", "POST", paramsMap);
                        }
                    }
                }
            }
        }
    }

    private static void startGroupMatch(GroupInfo groupInfo, GroupTableConfig groupTableConfig, String mUserId) throws Exception {
        int count = loadGroupMatchConfig(groupInfo);
        if (count <= -1) {
            return;
        }
        if (groupTableConfig.getPayType().intValue() == 2) {
            return;
        }

        int playerCount = NumberUtils.toInt(ResourcesConfigsUtil.loadServerPropertyValue("group_match_mode" + groupTableConfig.getGameType(), "3"), 3);
        if (playerCount > 0 && playerCount != groupTableConfig.getPlayerCount().intValue()) {
            return;
        }

        String groupId = groupInfo.getGroupId().toString();
        List<HashMap<String, Object>> list = null;

        boolean isOut = true;
        boolean bl = GroupDao.getInstance().lockGroupMatch(groupId);
        if (bl) {
            try {
                boolean tempBl = false;
                final int minCount = groupTableConfig.getPlayerCount().intValue();

                list = GroupDao.getInstance().loadGroupMatchUsers(groupId, minCount >= count ? minCount : count);
                int totalTableCount = list == null ? 0 : (list.size() / minCount);

                one:
                while (list != null && list.size() >= minCount && totalTableCount > 0) {
                    List<String> userList;
                    //人满即开
                    if (minCount >= count) {
                        tempBl = true;
                        userList = new ArrayList<>(minCount);
                        for (HashMap<String, Object> map : list) {
                            String userId = String.valueOf(map.get("userId"));
                            GroupUser groupUser = GroupDao.getInstance().loadGroupUser(Long.parseLong(userId), groupId);
                            if (GroupConstants.isForbidden(groupUser)) {
                                GroupDao.getInstance().quitGroupMatch(Long.parseLong(userId));
                                if (totalTableCount<=0){
                                    break one;
                                }
                                list = GroupDao.getInstance().loadGroupMatchUsers(groupId, minCount >= count ? minCount : count);
                                continue one;
                            }
                            userList.add(userId);
                        }

                        if (userList.size() != minCount) {
                            if (totalTableCount<=0){
                                break one;
                            }
                            list = GroupDao.getInstance().loadGroupMatchUsers(groupId, minCount >= count ? minCount : count);
                            continue one;
                        }

                        if (isOut && !userList.contains(mUserId)) {
                            isOut = false;
                        }

                    } else if (count == list.size() || tempBl) {
                        tempBl = true;
                        userList = new ArrayList<>(minCount);
                        for (HashMap<String, Object> map : list) {
                            String userId = String.valueOf(map.get("userId"));
                            GroupUser groupUser = GroupDao.getInstance().loadGroupUser(Long.parseLong(userId), groupId);
                            if (GroupConstants.isForbidden(groupUser)) {
                                GroupDao.getInstance().quitGroupMatch(Long.parseLong(userId));
                                if (totalTableCount<=0){
                                    break one;
                                }
                                list = GroupDao.getInstance().loadGroupMatchUsers(groupId, minCount >= count ? minCount : count);
                                continue one;
                            }
                            userList.add(userId);
                        }

                        Collections.shuffle(userList);

                        while (userList.size() > minCount) {
                            userList.remove(0);
                        }

                        if (userList.size() != minCount) {
                            if (totalTableCount<=0){
                                break one;
                            }
                            list = GroupDao.getInstance().loadGroupMatchUsers(groupId, minCount >= count ? minCount : count);
                            continue one;
                        }

                        if (isOut && !userList.contains(mUserId)) {
                            isOut = false;
                        }

                    } else {
                        break one;
                    }

                    Map<String, Integer> userServers = new HashMap<>();
                    List<Player> playerList = new ArrayList<>(userList.size());

                    Server server = ServerManager.loadServer(groupTableConfig.getGameType(), 1);

                    StringBuilder userIdBuilder = new StringBuilder(userList.size() * 11);
                    for (String userId : userList) {
                        userIdBuilder.append("_").append(userId);
                        Player player = PlayerManager.getInstance().getPlayer(Long.valueOf(userId));
                        if (player == null) {
                            if (server.getId() == GameServerConfig.SERVER_ID) {
                                player = PlayerManager.getInstance().loadPlayer(Long.parseLong(userId), groupTableConfig.getGameType());
                                playerList.add(0, player);
                            } else {
                                player = PlayerManager.getInstance().loadPlayer0(Long.parseLong(userId), groupTableConfig.getGameType());
                                playerList.add(player);
                            }
                        } else {
                            playerList.add(0, player);
                        }

                        if (player.getPlayingTableId()>0){
                            player.getMyExtend().setGroupMatch(false);
                            GroupDao.getInstance().quitGroupMatch(player.getUserId());
                            if (totalTableCount<=0){
                                break one;
                            }
                            list = GroupDao.getInstance().loadGroupMatchUsers(groupId, minCount >= count ? minCount : count);
                            continue one;
                        }

                        userServers.put(userId, player.getEnterServer());
                    }

                    totalTableCount--;

                    if(GroupDao.getInstance().quitGroupMatch(userList)!=minCount){
                        if (totalTableCount<=0){
                            break one;
                        }
                        list = GroupDao.getInstance().loadGroupMatchUsers(groupId, minCount >= count ? minCount : count);
                        continue one;
                    }

                    //俱乐部匹配模式开房
                    Map<String, String> paramsMap = new HashMap<>();
                    paramsMap.put("type", "match");
                    paramsMap.put("users", userIdBuilder.substring(1));
                    paramsMap.put("config", JSON.toJSONString(groupTableConfig));

                    String ret;
                    if (server.getId() == GameServerConfig.SERVER_ID) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("groupMatchUsers", paramsMap.get("users"));
                        List<Integer> integerList;
                        if (groupTableConfig.getModeMsg().startsWith("{")) {
                            integerList = GameConfigUtil.string2IntList(JSON.parseObject(groupTableConfig.getModeMsg()).getString("ints"), ",");
                        } else {
                            integerList = GameConfigUtil.string2IntList(groupTableConfig.getModeMsg(), ",");
                        }

                        List<String> stringList = new ArrayList<>(4);
                        stringList.add(groupTableConfig.getGroupId().toString());
                        stringList.add("1");
                        stringList.add("1");
                        stringList.add(groupTableConfig.getKeyId().toString());

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("code", "0");
                        try {
                            Player master = playerList.get(0);
                            jsonObject.put("master", master.getUserId());

                            StringBuilder sb = new StringBuilder("createTable1|GroupMatchCommand|1");
                            sb.append("|").append(master.getUserId());
                            sb.append("|").append(integerList);
                            sb.append("|").append(stringList);
                            sb.append("|").append(map);
                            LogUtil.monitorLog.info(sb.toString());

                            BaseTable table = TableManager.getInstance().createTable(master, integerList, stringList, 0, 0, true, map);

                            long tableId = table == null ? 0 : table.getId();
                            jsonObject.put("tableId", tableId);
                            Object msg = map.get("errorMsg");
                            jsonObject.put("message", tableId > 0L && msg == null ? "匹配成功" : msg);
                        } catch (Exception e1) {
                            LogUtil.errorLog.error("Exception:" + e1.getMessage(), e1);
                        }

                        ret = jsonObject.toString();
                    } else {
                        ret = HttpUtil.getUrlReturnValue(ServerManager.loadRootUrl(server) + "/group/msg.do", "UTF-8", "POST", paramsMap);
                    }

                    LogUtil.msgLog.info("group match create table:params={},ret={}", paramsMap, ret);

                    long tableId = StringUtils.isNotBlank(ret)? JSON.parseObject(ret).getLongValue("tableId"):0L;

                    for (Player player1 : playerList) {
                        player1.getMyExtend().setGroupMatch(false);
                        player1.setPlayingTableId(tableId);
                        player1.saveBaseInfo();
                    }

                    if (tableId > 0L) {
                        //切服
                        for (Map.Entry<String, Integer> kv : userServers.entrySet()) {
                            int s = kv.getValue().intValue();
                            if (server.getId() != s) {
                                //通知玩家切服
                                Server server1 = ServerManager.loadServer(s);
                                if (server1 != null) {
                                    if (s != GameServerConfig.SERVER_ID) {
                                        String ret0 = HttpUtil.getUrlReturnValue(ServerManager.loadRootUrl(server1) + "/group/msg.do?type=server&serverId=" + server.getId() + "&userId=" + kv.getKey()+"&tableId="+tableId, 2);
                                        LogUtil.msgLog.info("group match success:currentServer={},userServer={},tableServer={},userId={},groupId={},tableId={},ret={}", GameServerConfig.SERVER_ID, s, server.getId(), kv.getKey(), groupId, tableId, ret0);
                                    } else {
                                        LogUtil.msgLog.info("group match success:currentServer={},userServer={},tableServer={},userId={},groupId={},tableId={}", GameServerConfig.SERVER_ID, s, server.getId(), kv.getKey(), groupId, tableId);
                                        Player player1 = PlayerManager.getInstance().getPlayer(Long.valueOf(kv.getKey()));
                                        if (player1 != null) {
                                            GameServerUtil.sendChangeServerCommand(player1.getMyWebSocket(), player1.getTotalCount(), server);
                                            PlayerManager.getInstance().removePlayer(player1);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        List<String> stringList = new ArrayList<>(1);
                        JSONObject json = StringUtils.isNotBlank(ret) ? JSON.parseObject(ret) : null;
                        String errorMsg = json != null ? json.getString("message") : null;
//                        long masterId = json != null ? json.getLongValue("master") : null;
                        if (StringUtils.isBlank(errorMsg)) {
                            errorMsg = "匹配失败，请稍后再试";
                        }
                        stringList.add(errorMsg);

                        List<Integer> intList = new ArrayList<>(1);
                        intList.add(WebSocketMsgType.req_com_group_match);
                        //失败通知
                        for (Map.Entry<String, Integer> kv : userServers.entrySet()) {
                            int s = kv.getValue().intValue();

                            if (GameServerConfig.SERVER_ID != s) {
                                Server server1 = ServerManager.loadServer(s);
                                if (server1 != null) {
                                    Map<String, String> paramsMap0 = new HashMap<>();
                                    paramsMap0.put("type", "notice");
                                    paramsMap0.put("userId", kv.getKey());
                                    paramsMap0.put("code", String.valueOf(WebSocketMsgType.res_code_err));
                                    paramsMap0.put("ints", JSON.toJSONString(intList));
                                    paramsMap0.put("strs", JSON.toJSONString(stringList));
                                    String ret0 = HttpUtil.getUrlReturnValue(ServerManager.loadRootUrl(server1) + "/group/msg.do", "UTF-8", "POST", paramsMap0);
                                    LogUtil.msgLog.info("group match fail:notice={},userId={},groupId={},currentServer={},userServer={},ret={}", errorMsg, kv.getKey(), groupId, GameServerConfig.SERVER_ID, s, ret0);
                                }
                            } else {
                                Player player1 = PlayerManager.getInstance().getPlayer(Long.valueOf(kv.getKey()));
                                if (player1 != null) {
                                    LogUtil.msgLog.info("group match fail:notice={},userId={},groupId={},currentServer={},userServer={}", errorMsg, kv.getKey(), groupId, GameServerConfig.SERVER_ID, s);
                                    player1.writeErrMsgs(WebSocketMsgType.req_com_group_match, errorMsg);
                                }
                            }
                        }
                    }

                    if (totalTableCount<=0){
                        Iterator<HashMap<String,Object>> it = list.iterator();
                        int tempCount = userServers.size();
                        while (it.hasNext()&&tempCount>0) {
                            String tempUserId = String.valueOf(it.next().get("userId"));
                            for (Map.Entry<String,Integer> kv : userServers.entrySet()){
                                if (tempUserId.equals(kv.getKey())){
                                    it.remove();
                                    tempCount--;
                                }
                            }
                        }
                        break one;
                    }
                    list = GroupDao.getInstance().loadGroupMatchUsers(groupId, minCount >= count ? minCount : count);
                }
            } catch (Exception e) {
                LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
            } finally {
                GroupDao.getInstance().unlockGroupMatch(groupId);
            }
        }

        sendMsg(groupInfo, groupTableConfig, list, isOut ? mUserId : null);
    }

    public static int loadGroupMatchConfig(GroupInfo groupInfo) {
        JSONObject jsonObject = StringUtils.isBlank(groupInfo.getExtMsg()) ? null : JSONObject.parseObject(groupInfo.getExtMsg());

        if (jsonObject == null) {
            return -1;
        }
        int count = NumberUtils.toInt(jsonObject.getString("match"), -1);
        return count;
    }

    @Override
    public void setMsgTypeMap() {

    }
}
