package com.sy599.game.webservice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.IpUtil;
import com.sy.mainland.util.OutputUtil;
import com.sy.mainland.util.UrlParamUtil;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.group.GroupTable;
import com.sy599.game.db.bean.group.GroupTableConfig;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupServlet extends HttpServlet {

    private final static String APP_KEY = "qweh#$*(_~)lpslot;589*/-+.-8&^%$#@!";

    private static final long serialVersionUID = 1L;

    public GroupServlet() {
    }

    public void init() throws ServletException {
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> paramsMap = UrlParamUtil.getParameters(request);
        String type = paramsMap.get("type");
        String timestamp = paramsMap.get("timestamp");
        String sign = paramsMap.get("sign");

        String ip = IpUtil.getIpAddr(request);

        LogUtil.msgLog.info("GroupServlet:ip={},type={},paramsMap={}", ip, type, paramsMap);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "-1");
        try {
            if (IpUtil.isIntranet(ip) || (NumberUtils.isDigits(timestamp) && (Math.abs(System.currentTimeMillis() - Long.parseLong(timestamp)) <= 5 * 60 * 1000) && MD5Util.getMD5String(APP_KEY + type + timestamp).equalsIgnoreCase(sign))) {
                if ("table".equals(type)) {
                    long tableId = NumberUtils.toLong(paramsMap.get("tableId"), 0);
                    String tableKey = paramsMap.get("tableKey");
                    BaseTable table = TableManager.getInstance().getTable(tableId);
                    String[] ts;
                    if (table != null && table.isGroupRoom() && (ts = table.getServerKey().split("_")).length >= 2 && ts[1].equals(tableKey)) {
                        GroupTable groupTable = GroupDao.getInstance().loadGroupTableByKeyId(tableKey);
                        jsonObject.put("dealCount", groupTable != null ? groupTable.getDealCount() : table.getPlayedBureau());
                        jsonObject.put("playBureau", table.getPlayBureau());
                        List<String> retList = new ArrayList<>(table.getPlayerCount());
                        Map<Long, Player> players = table.getPlayerMap();
                        for (Player player1 : players.values()) {
                            JSONObject json = new JSONObject();
                            json.put("userId", player1.getUserId());
                            json.put("online", player1.getIsOnline());
                            json.put("name", player1.getRawName());
                            json.put("headimgurl", player1.getHeadimgurl());
                            json.put("score", player1.loadAggregateScore());
                            json.put("tzScore", player1.loadTzScore());
                            retList.add(json.toString());
                        }
                        jsonObject.put("msg", JSON.toJSONString(retList));
                        jsonObject.put("code", "0");
                    }
                } else if ("notice".equals(type)) {
                    jsonObject.put("code", "0");

                    String checkRoom = paramsMap.get("checkRoom");
                    String userIds = paramsMap.get("userId");
                    if(StringUtils.isBlank(userIds)){
                        jsonObject.put("message", 0);
                    }else if (CommonUtil.isPureNumber(userIds)){
                        Player player = PlayerManager.getInstance().getPlayer(Long.valueOf(userIds));
                        if (player != null&&((!"1".equals(checkRoom))||GroupRoomUtil.canJoinInviteTable(player))) {
                            ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(Integer.parseInt(paramsMap.get("code")), JSON.parseArray(paramsMap.get("ints"), Integer.class), JSON.parseArray(paramsMap.get("strs"), String.class));
                            player.writeSocket(res.build());
                        }
                        jsonObject.put("message", player == null ? 0 : 1);
                    }else{
                        String[] uids = userIds.split(",");
                        int count = 0;
                        for (String uid:uids){
                            if (CommonUtil.isPureNumber(uid)){
                                Player player = PlayerManager.getInstance().getPlayer(Long.valueOf(uid));
                                if (player != null&&((!"1".equals(checkRoom))||GroupRoomUtil.canJoinInviteTable(player))) {
                                    ComMsg.ComRes.Builder res = SendMsgUtil.buildComRes(Integer.parseInt(paramsMap.get("code")), JSON.parseArray(paramsMap.get("ints"), Integer.class), JSON.parseArray(paramsMap.get("strs"), String.class));
                                    player.writeSocket(res.build());

                                    count++;
                                }
                            }
                        }
                        jsonObject.put("message", count);
                    }
                } else if ("match".equals(type)) {
                    String config = paramsMap.get("config");
                    String users = paramsMap.get("users");
                    if (StringUtils.isNotBlank(config) && StringUtils.isNotBlank(users)) {
                        GroupTableConfig tableConfig = JSON.parseObject(config, GroupTableConfig.class);
                        String[] userIds = users.split("_");
                        Player player = null;
                        for (String uid : userIds) {
                            player = PlayerManager.getInstance().getPlayer(Long.valueOf(uid));
                            if (player != null) {
                                break;
                            }
                        }
                        if (player == null) {
                            player = PlayerManager.getInstance().loadPlayer(Long.parseLong(userIds[new SecureRandom().nextInt(userIds.length)]), tableConfig.getGameType());
                        }
                        Map<String, Object> map = new HashMap<>();
                        map.put("groupMatchUsers", users);
                        List<Integer> integerList;
                        if (tableConfig.getModeMsg().startsWith("{")) {
                            integerList = GameConfigUtil.string2IntList(JSON.parseObject(tableConfig.getModeMsg()).getString("ints"), ",");
                        } else {
                            integerList = GameConfigUtil.string2IntList(tableConfig.getModeMsg(), ",");
                        }

                        List<String> stringList = new ArrayList<>(4);
                        stringList.add(tableConfig.getGroupId().toString());
                        stringList.add("1");
                        stringList.add("1");
                        stringList.add(tableConfig.getKeyId().toString());
                        jsonObject.put("master", player.getUserId());

                        StringBuilder sb = new StringBuilder("createTable1|GroupServlet|1");
                        sb.append("|").append(player.getUserId());
                        sb.append("|").append(integerList);
                        sb.append("|").append(stringList);
                        sb.append("|").append(map);
                        LogUtil.monitorLog.info(sb.toString());

                        BaseTable table = TableManager.getInstance().createTable(player, integerList, stringList, 0, 0, true, map);
                        jsonObject.put("code", "0");
                        long tableId = table == null ? 0 : table.getId();
                        jsonObject.put("tableId", tableId);
                        Object msg = map.get("errorMsg");
                        jsonObject.put("message", tableId > 0L && msg == null ? "匹配成功" : msg);
                    }
                } else if ("server".equals(type)) {
                    int serverId = Integer.parseInt(paramsMap.get("serverId"));
                    long userId = Long.parseLong(paramsMap.get("userId"));
                    Player player1 = PlayerManager.getInstance().getPlayer(userId);
                    if (player1 != null) {
                        long tableId = NumberUtils.toLong(paramsMap.get("tableId"),0);
                        if (tableId>0L){
                            player1.setPlayingTableId(tableId);
                            player1.setEnterServer(serverId);
                        }
                        Server server = ServerManager.loadServer(serverId);
                        if (server != null) {
                            GameServerUtil.sendChangeServerCommand(player1.getMyWebSocket(), player1.getTotalCount(), server);
                        }
                        PlayerManager.getInstance().removePlayer(player1);
                    }
                    jsonObject.put("code", "0");
                } else if ("groupRoomFire".equals(type)) {
                    String groupId = paramsMap.get("groupId");
                    String tableKey = paramsMap.get("tableKey");
                    String playerId = paramsMap.get("playerId");

                    Player player1 = PlayerManager.getInstance().getPlayer(Long.valueOf(playerId));
                    if (player1==null){
                        jsonObject.put("code", "1");
                        jsonObject.put("msg", "操作失败:该玩家不在该房间中");
                        return;
                    }
                    BaseTable table = player1.getPlayingTable();
                    if (table==null||!table.isGroupRoom()||!tableKey.equals(table.loadGroupTableKeyId())){
                        jsonObject.put("code", "1");
                        jsonObject.put("msg", "操作失败:该玩家不在该房间中");
                        return;
                    }
                    if (!groupId.equals(table.loadGroupId())){
                        jsonObject.put("code", "1");
                        jsonObject.put("msg", "操作失败:该房间不在此亲友圈中");
                        return;
                    }

                    boolean success = false;
                    synchronized (table){
                        if (table.getState() != SharedConstants.table_state.ready || table.getPlayedBureau()>0||table.getPlayBureau()>1){
                            jsonObject.put("code", "1");
                            jsonObject.put("msg", "牌局已开始，不能踢除该玩家");
                            return;
                        }
                        if (table.canQuit(player1)&&table.quitPlayer(player1)){
                            table.onPlayerQuitSuccess(player1);
                            table.updateRoomPlayers();
                            success = true;
                            player1.writeErrMsg(LangMsg.code_269,table.getId());
                        }
                    }

                    if (success){
                        jsonObject.put("code", "0");
                    }else{
                        jsonObject.put("code", "1");
                        jsonObject.put("msg", "操作失败:不能把该玩家从该房间中踢除");
                    }
                }else if("progress".equals(type)){
                    String tableIds = paramsMap.get("tableIds");
                    if (StringUtils.isNotBlank(tableIds)){
                        String[] ids = tableIds.split(",");
                        JSONArray jsonArray = new JSONArray(ids.length);
                        for (String id : ids){
                            if (StringUtils.isNotBlank(id)) {
                                JSONObject tableMap = new JSONObject();
                                BaseTable table = TableManager.getInstance().getTable(Long.valueOf(id));
                                if (table != null) {
                                    if("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("group_table_current_bureau")) && table.isGroupRoom()){  //俱乐部房间实时局数显示
                                        int dealCount = GroupDao.getInstance().selectGroupTableDealCount(Long.parseLong(table.loadGroupTableKeyId()));
                                        tableMap.put("progress0", dealCount);
                                    }else{
                                        tableMap.put("progress0",table.loadOverCurrentValue());
                                    }
                                    tableMap.put("progress1",table.loadOverValue());
                                    tableMap.put("tableId", id);
                                    jsonArray.add(tableMap);
                                }
                            }
                        }
                        jsonObject.put("datas", jsonArray);
                    }
                } else {
                    jsonObject.put("msg", "not support");
                }
            } else {
                jsonObject.put("msg", "ip or sign invalid");
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        } finally {
            OutputUtil.output(jsonObject.toString(),request,response,null,false);
        }
    }

}
