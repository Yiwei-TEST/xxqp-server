package com.sy599.game.gcommand.com;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.RoomBean;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.SoloRoomConfig;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.bean.gold.GoldRoomConfig;
import com.sy599.game.db.bean.group.GroupTable;
import com.sy599.game.db.bean.group.GroupTableConfig;
import com.sy599.game.db.bean.group.GroupUser;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.gcommand.com.competition.CompetitionServerCommand;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.util.CheckNetUtil;
import com.sy599.game.util.GoldRoomUtil;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.util.SoloRoomUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.SslUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class GetServerCommand extends BaseCommand {

    @Override
    public void setMsgTypeMap() {

    }

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);

        int optType = req.hasOptType() ? req.getOptType() : 0; // 0:旧接口相关，1：新金币场
        if (optType == 0) {
            process(player, req, false);
        } else if (optType == 1) {
            processGoldRoom(player, req);
        } else if (optType == 2) {
            processSoloRoom(player, req);
        } else if (optType == 3) {
            CompetitionServerCommand.process(player, req);
        } else if (optType == 4) {
            processGoldRoomMatch(player, req);
        } else {
            player.writeErrMsg(LangMsg.code_3);
        }
    }

    private void process(Player player, ComReq req, boolean isCallback) throws Exception {
        List<Integer> params = req.getParamsList();
        List<String> strParams = req.getStrParamsList();

        int gameType = StringUtil.getIntValue(params, 0, 0);
        //游戏服类型0练习场1普通场2金币场3h5金币场4现金红包瓜分活动5比赛场
        int serverType = StringUtil.getIntValue(params, 1, 1);
        long totalCount = StringUtil.getIntValue(params, 2, 0);
        int appType = StringUtil.getIntValue(params, 3, 0);//0安装包,1h5


        String tableIdStr = strParams.get(0);
        long tableId = Long.parseLong(tableIdStr);
        long modeId = strParams.size() > 1 ? NumberUtils.toLong(strParams.get(1), -1) : 0;
        int gId = strParams.size() > 2 ? NumberUtils.toInt(strParams.get(2), -1) : 0;
        String wanfaIds = strParams.size() > 3 ? strParams.get(3) : "";

        long userId = player.getUserId();

        if (userId != 0) {
            RegInfo info = UserDao.getInstance().selectUserByUserId(userId);
            if (info != null) {
                totalCount = (-info.getUsedCards() + info.getCards()) / 150 + info.getTotalCount();
                if (info.getPlayingTableId() != 0 && info.getPlayState() == 1) {
                    //如果正在开局，就不能切服
                    tableId = info.getPlayingTableId();
                }
            }
        }

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("|").append(gameType);
        strBuilder.append("|").append(tableIdStr);
        strBuilder.append("|").append(tableId);
        strBuilder.append("|").append(userId);
        strBuilder.append("|").append(modeId);
        strBuilder.append("|").append(serverType);
        strBuilder.append("|").append(gId);
        strBuilder.append("|").append(wanfaIds);
        LogUtil.msgLog.info("GetServerCommand|0|start|" + strBuilder.toString());

        boolean loadFromCheckNet = true;
        Map<String, Object> result = new HashMap<String, Object>();
        Server server = null;
        int serverId = 0;
        String matchType = "";
        String[] gameUrls = null;
        if (tableId == 1 && modeId > 0) {
            // ------------无房号金币场-------------
            matchType = ResourcesConfigsUtil.loadServerPropertyValue("matchType", "1");
            serverType = NumberUtils.toInt(appType == 1 ? ResourcesConfigsUtil.loadServerPropertyValue("gold_h5_server_type") : ResourcesConfigsUtil.loadServerPropertyValue("gold_server_type"), serverType);
            loadFromCheckNet = false;
            GoldRoom goldRoom = GoldRoomDao.getInstance().loadCanJoinGoldRoom(String.valueOf(modeId));
            if ("2".equals(matchType) && goldRoom == null) {
                goldRoom = GoldRoomDao.getInstance().loadCanJoinGoldRoom(String.valueOf(modeId), "1");
            }
            if (goldRoom != null) {
                server = ServerManager.loadServer(goldRoom.getServerId());
            }
            if (server == null) {
                server = ServerManager.loadServer(gameType, serverType);
            }
            if (server != null) {
                serverId = server.getId();
                gameUrls = CheckNetUtil.loadGameUrl(serverId, totalCount);
                if (gameUrls != null) {
                    server = new Server();
                    server.setId(serverId);
                    if (gameUrls[0].startsWith("ws:")) {
                        server.setChathost(gameUrls[0]);
                    } else if (gameUrls[0].startsWith("wss:")) {
                        server.setWssUri(gameUrls[0]);
                    }
                }
            }
        } else if (tableId <= 0 && modeId > 0) {
            // ---------军团、俱乐部、亲友圈房-----------
            try {
                if (gId > 0) {
                    // 俱乐部快速加入
                    GroupTable groupTable = null;
                    GroupTableConfig gtc = GroupDao.getInstance().loadGroupTableConfig(modeId);
                    if (gtc == null) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_48));
                        return;
                    }
                    try {
                        groupTable = GroupDao.getInstance().loadRandomSameModelTable(modeId, gId);
                    } catch (Throwable th) {
                        LogUtil.e("get server err-->userId:" + player.getUserId() + ",modeId:" + modeId);
                    }
                    if (groupTable != null) {
                        serverId = NumberUtils.toInt(groupTable.getServerId());
                        tableId = groupTable.getTableId();
                        gameType = getGameType(groupTable.getTableMsg());
                        server = ServerManager.loadServer(serverId);
                        if (server == null) {
                            LogUtil.e("get server err-->serverId:" + serverId + ",tableId:" + groupTable.getServerId());
                            tableId = 0;
                            gameUrls = CheckNetUtil.loadGameUrl(serverId, totalCount);
                            if (gameUrls != null) {
                                server = new Server();
                                server.setId(serverId);
                                if (gameUrls[0].startsWith("ws:")) {
                                    server.setChathost(gameUrls[0]);
                                } else if (gameUrls[0].startsWith("wss:")) {
                                    server.setWssUri(gameUrls[0]);
                                }
                                loadFromCheckNet = false;
                            }
                        }
                    }
                    strBuilder.append(",clubTableId=").append(tableId);
                } else {
                    GroupTable groupTable = GroupDao.getInstance().loadRandomGroupTable(modeId);

                    if (groupTable != null) {
                        tableId = groupTable.getTableId();
                        strBuilder.append(",groupTableId=").append(tableId);
                    }

                    if (gameType <= 0) {
                        GroupTableConfig groupTableConfig = GroupDao.getInstance().loadGroupTableConfig(modeId);
                        if (groupTableConfig != null) {
                            gameType = groupTableConfig.getGameType();
                            strBuilder.append(",groupGameType=").append(gameType);
                        }
                    }
                }

                // -----------退出当前的房间-------------
                BaseTable table2 = player.getPlayingTable();
                if (table2 != null && tableId != table2.getId()) {
                    LogUtil.msgLog.info("GetServerCommand|quitOldTable|" + table2.getId() + "|" + player.getUserId() + "|" + player.getSeat() + "|" + tableId);
                    if (!quitOldTable(table2, player)) {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_230));
                        return;
                    }
                }
            } catch (Exception e) {
                LogUtil.e("Exception:" + e.getMessage(), e);
            }
        } else if (tableId > 0 && GoldRoomUtil.isNotGoldRoom(tableId)) {

            boolean doQuickJoin = false;
            GroupTable gt = GroupDao.getInstance().loadGroupTable(userId, tableId);
            if (gt != null) {
                if (gt.getMaxCount() <= gt.getCurrentCount()) {
                    if(isCallback) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_5));
                    return;
                    } else {
                        doQuickJoin = true;
                    }
                }
            } else if (!isCallback){
                doQuickJoin = true;
            }
            //若指定桌子人满了或者已开局 走一遍快速加入的逻辑
            if( doQuickJoin ) {
                ComReq.Builder newRep = ComReq.newBuilder();
                newRep = req.toBuilder().clone();
                newRep.setStrParams(0, "0");
                process(player, newRep.build(), true);
                return;
            }

            // -----------退出当前的房间-------------
            BaseTable table2 = player.getPlayingTable();
            if (table2 != null && tableId != table2.getId()) {
                LogUtil.msgLog.info("GetServerCommand|quitOldTable|" + table2.getId() + "|" + player.getUserId() + "|" + player.getSeat() + "|" + tableId);
                if (!quitOldTable(table2, player)) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_230));
                    return;
                }
            }

            RoomBean room = TableDao.getInstance().queryRoom(tableId);
            if (room == null) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_1, tableId));
                return;
            }

            serverId = room.getServerId();
            gameType = room.getType();
            if (serverId > 0) {
                server = ServerManager.loadServer(serverId);
                if (server == null) {
                    gameUrls = CheckNetUtil.loadGameUrl(serverId, totalCount);
                    if (gameUrls != null) {
                        server = new Server();
                        server.setId(serverId);
                        if (gameUrls[0].startsWith("ws:")) {
                            server.setChathost(gameUrls[0]);
                        } else if (gameUrls[0].startsWith("wss:")) {
                            server.setWssUri(gameUrls[0]);
                        }
                        loadFromCheckNet = false;
                    }
                }
            } else if (room.getUsed() == 0) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_1, tableId));
                return;
            }
            if (server == null) {
                result.put("code", -1);
                player.writeComMessage(WebSocketMsgType.res_code_getserverid, JacksonUtil.writeValueAsString(result));
                return;
            }

        } else if (GoldRoomUtil.isGoldRoom(tableId)) {
            // ---------金币场-----------
            serverType = NumberUtils.toInt(ResourcesConfigsUtil.loadServerPropertyValue("gold_server_type"), serverType);
            loadFromCheckNet = false;
            GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(tableId);
            if (goldRoom == null || goldRoom.isOver()) {
                goldRoom = GoldRoomDao.getInstance().loadCanJoinGoldRoom(String.valueOf(modeId));
            }

            if (goldRoom != null) {
                server = ServerManager.loadServer(goldRoom.getServerId());
            }
            if (server == null) {
                server = ServerManager.loadServer(gameType, serverType);
            }
            if (server != null) {
                serverId = server.getId();
                gameUrls = CheckNetUtil.loadGameUrl(serverId, totalCount);
                if (gameUrls != null) {
                    server = new Server();
                    server.setId(serverId);
                    if (gameUrls[0].startsWith("ws:")) {
                        server.setChathost(gameUrls[0]);
                    } else if (gameUrls[0].startsWith("wss:")) {
                        server.setWssUri(gameUrls[0]);
                    }
                }
            }
        } else {
            // -----------退出当前的房间-------------
            BaseTable table2 = player.getPlayingTable();
            if (table2 != null && tableId != table2.getId()) {
                LogUtil.msgLog.info("GetServerCommand|quitOldTable|" + table2.getId() + "|" + player.getUserId() + "|" + player.getSeat() + "|" + tableId);
                if (!quitOldTable(table2, player)) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_230));
                    return;
                }
            }
        }

        if (gameType == 0 && serverType == 4) {
            // ----------------serverType为4：现金红包瓜分活动------------
            server = ServerManager.loadServer(gameType, serverType);
        } else if (gameType != 0 && tableId == 0) {
            // ---------------创建房间的时候--------------------------
            server = ServerManager.loadServer(gameType, serverType);
        }

        if (server == null) {
            LogUtil.errorLog.error("get server error:player=" + player.getUserId() + ",strs=" + strParams + ",ints=" + params);
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return;
        }

        if (!StringUtils.isBlank(wanfaIds) && gameType > 0) {
            boolean check = false;
            String[] wanfas = wanfaIds.split(",");
            for (String wanfa : wanfas) {
                if (wanfa.equals(String.valueOf(gameType))) {
                    check = true;
                    break;
                }
            }
            if (!check) {
                LogUtil.errorLog.error("get server error:player=" + player.getUserId() + ",strs=" + strParams + ",ints=" + params + ",gameType:" + gameType);
                if (tableId != 0) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_212));
                } else {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_58));
                }
                return;
            }
        }

        Map<String, Object> serverMap = new HashMap<>();
        serverMap.put("serverId", server.getId());

        boolean useSsl = SslUtil.hasSslHandler(player.getMyWebSocket().getCtx());

        if (loadFromCheckNet) {
            serverMap.put("httpUrl", useSsl ? server.getHttpsUri() : server.getHost());
            gameUrls = CheckNetUtil.loadGameUrl(server.getId(), totalCount);
        }

        if (gameUrls == null) {
            serverMap.put("connectHost", useSsl ? server.getWssUri() : server.getChathost());
            serverMap.put("connectHost1", "");
            serverMap.put("connectHost2", "");
        } else {
            String url0;
            if (useSsl) {
                url0 = (StringUtils.isNotBlank(gameUrls[0]) && gameUrls[0].startsWith("wss:")) ? gameUrls[0] : server.getWssUri();
            } else {
                url0 = (StringUtils.isNotBlank(gameUrls[0]) && gameUrls[0].startsWith("ws:")) ? gameUrls[0] : server.getChathost();
            }

            serverMap.put("connectHost", url0);
            serverMap.put("connectHost1", gameUrls[1]);
            serverMap.put("connectHost2", gameUrls[2]);
        }

        result.put("server", serverMap);
        result.put("blockIconTime", 0);
        result.put("goldRoomId", 0);
        result.put("code", 0);
        // 俱乐部快速加入切服返回桌子号
        result.put("tId", (modeId > 0 && gId > 0) ? tableId : 0);

        strBuilder.append("|").append(result);
        LogUtil.msgLog.info("GetServerCommand|0|end|" + strBuilder.toString());
        player.writeComMessage(WebSocketMsgType.res_code_getserverid, JacksonUtil.writeValueAsString(result));
    }

    private int getGameType(String tableMsg) {
        try {
            int gameType = 0;
            JSONObject jsonObject = JSONObject.parseObject(tableMsg);
            String ints = jsonObject.getString("ints");
            if (!StringUtils.isBlank(ints)) {
                gameType = NumberUtils.toInt(ints.split(",")[1]);
            }
            return gameType;
        } catch (Exception e) {
            LogUtil.e("getGameType err-->tableMsg:" + tableMsg);
            return -1;
        }
    }

    private boolean quitOldTable(BaseTable table, Player player) {
        synchronized (table) {
            if (!table.canQuit(player) || !table.quitPlayer(player)) {
                return false;
            }
            table.onPlayerQuitSuccess(player);
            table.updateRoomPlayers();
        }
        return true;
    }

    /**
     * 新金币场匹配模式
     *
     * @param player
     * @param req
     */
    private void processGoldRoom(Player player, ComReq req) {
        try {
            if (req.getStrParamsCount() <= 0) {
                player.writeErrMsg(LangMsg.code_3);
                return;
            }
            List<String> strParams = req.getStrParamsList();

            long userId = player.getUserId();
            Long configId = Long.valueOf(strParams.get(0));
            StringBuilder sb = new StringBuilder("GetServerCommand|GoldRoom|start");
            sb.append("|").append(player.getUserId());
            sb.append("|").append(strParams);
            LogUtil.msgLog.info(sb.toString());
            if (configId <= 0) {
                player.writeErrMsg(LangMsg.code_3);
                return;
            }
            GoldRoomConfig config = GoldRoomUtil.getGoldRoomConfig(configId);
            if (config == null) {
                player.writeErrMsg(LangMsg.getMsg(LangMsg.code_254, configId));
                return;
            }
            if (player.loadAllGolds() < config.getJoinLimit()) {
//                player.writeErrMsg(LangMsg.code_256);
                player.sendBrokeAward(config.getJoinLimit());
                return;
            }
            long groupId = player.getGoldRoomGroupId();
            if (groupId > 0) {
                GroupUser gu = GroupDao.getInstance().loadGroupUser(userId, groupId);
                if (gu == null) {
                    player.writeErrMsg(LangMsg.code_253);
                    return;
                }
            }
            Server server = null;
            String groupIdLimit = GoldRoomUtil.getGroupIdLimit(groupId);
            GoldRoom goldRoom;
            if (groupId > 0 && StringUtils.isNotBlank(groupIdLimit)) {
                // 亲友圈限制匹配
                goldRoom = GoldRoomDao.getInstance().randomGoldRoomByGroupLimit(configId, groupIdLimit);
            } else {
                goldRoom = GoldRoomDao.getInstance().randomGoldRoom(configId);
            }
            if (goldRoom != null) {
                server = ServerManager.loadServer(goldRoom.getServerId());
            }
            if (server == null) {
                server = ServerManager.loadServer(config.getPlayType(), Server.SERVER_TYPE_GOLD_ROOM);
            }
            if (server == null) {
                player.writeErrMsg(LangMsg.code_0);
                LogUtil.errorLog.error("processGoldRoom|fail|" + player.getUserId() + "|" + strParams);
                return;
            }
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> serverMap = new HashMap<>();
            serverMap.put("serverId", server.getId());
            serverMap.put("connectHost", server.getChathost());
            result.put("server", serverMap);
            if (goldRoom != null) {
                result.put("goldRoomId", goldRoom.getKeyId());
            } else {
                result.put("goldRoomId", 0);
            }
            result.put("code", 0);
            player.writeComMessage(WebSocketMsgType.res_code_getserverid, JacksonUtil.writeValueAsString(result));

            sb = new StringBuilder("GetServerCommand|GoldRoom|end");
            sb.append("|").append(player.getUserId());
            sb.append("|").append(result);
            LogUtil.msgLog.info(sb.toString());
        } catch (Exception e) {
            LogUtil.errorLog.error("processGoldRoom|error|" + e.getMessage(), e);
        }
    }

    /**
     * 金币场挑战赛
     *
     * @param player
     * @param req
     */
    private void processSoloRoom(Player player, ComReq req) {
        try {
            if (req.getStrParamsCount() <= 0) {
                player.writeErrMsg(LangMsg.code_3);
                return;
            }
            List<String> strParams = req.getStrParamsList();
            Long configId = Long.valueOf(strParams.get(0));
            StringBuilder sb = new StringBuilder("GetServerCommand|SoloRoom|start");
            sb.append("|").append(player.getUserId());
            sb.append("|").append(strParams);
            LogUtil.msgLog.info(sb.toString());
            if (configId <= 0) {
                player.writeErrMsg(LangMsg.code_3);
                return;
            }
            SoloRoomConfig config = SoloRoomUtil.getSoloRoomConfig(configId);
            if (config == null) {
                player.writeErrMsg("挑战：玩法不存在或暂未开放");
                return;
            }
            Server server = ServerManager.loadServer(config.getPlayType(), Server.SERVER_TYPE_GOLD_ROOM);
            if (server == null) {
                player.writeErrMsg(LangMsg.code_0);
                LogUtil.errorLog.error("processSoloRoom|fail|" + player.getUserId() + "|" + strParams);
                return;
            }
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> serverMap = new HashMap<>();
            serverMap.put("serverId", server.getId());
            serverMap.put("connectHost", server.getChathost());
            result.put("server", serverMap);
            result.put("code", 0);
            player.writeComMessage(WebSocketMsgType.res_code_getserverid, JacksonUtil.writeValueAsString(result));

            sb = new StringBuilder("GetServerCommand|SoloRoom|end");
            sb.append("|").append(player.getUserId());
            sb.append("|").append(result);
            LogUtil.msgLog.info(sb.toString());
        } catch (Exception e) {
            LogUtil.errorLog.error("processSoloRoom|error|" + e.getMessage(), e);
        }
    }

    /**
     * 金币场新匹配模式
     *
     * @param player
     * @param req
     */
    private void processGoldRoomMatch(Player player, ComReq req) {
        try {
            if (req.getParamsCount() <= 0) {
                player.writeErrMsg(LangMsg.code_3);
                return;
            }
            int playType = req.getParams(0);
            Server server = ServerManager.loadServer(playType, Server.SERVER_TYPE_GOLD_ROOM, true);
            if (server == null) {
                player.writeErrMsg(LangMsg.code_0);
                LogUtil.errorLog.error("processGoldRoomMatch|fail|" + player.getUserId() + "|" + playType);
                return;
            }
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> serverMap = new HashMap<>();
            serverMap.put("serverId", server.getId());
            serverMap.put("connectHost", server.getChathost());
            result.put("server", serverMap);
            result.put("goldRoomId", 0);
            result.put("code", 0);
            player.writeComMessage(WebSocketMsgType.res_code_getserverid, JacksonUtil.writeValueAsString(result));

            StringBuilder sb = new StringBuilder("GetServerCommand|GoldRoom|end");
            sb.append("|").append(player.getUserId());
            sb.append("|").append(result);
            LogUtil.msgLog.info(sb.toString());
        } catch (Exception e) {
            LogUtil.errorLog.error("processGoldRoom|error|" + e.getMessage(), e);
        }
    }

}
