package com.sy599.game.gcommand.login;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy599.game.GameServerConfig;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.CommonPlayer;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.LogConstants;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.executor.task.TenMinuteFixedRateTask;
import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.db.bean.RoomBean;
import com.sy599.game.db.bean.Server;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.bean.gold.GoldRoomMatchPlayer;
import com.sy599.game.db.bean.gold.GoldRoomUser;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.gcommand.com.activity.OldPlayerBackActivityCmd;
import com.sy599.game.gcommand.login.util.LoginDataUtil;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.ServerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.OpenMsg.Open;
import com.sy599.game.util.CompetitionUtil;
import com.sy599.game.util.GameServerUtil;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.GoldRoomUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.Md5CheckUtil;
import com.sy599.game.util.MissionConfigUtil;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.util.StringUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.MyWebSocket;
import com.sy599.game.websocket.WebSocketManager;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.NettyUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class LoginCommand extends BaseCommand {
    private long loginUserId;
    private boolean returnPlayer = false;

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
    }

    public Player login(MessageUnit message, MyWebSocket socket) throws Exception {
        Open openMsg = Open.parseFrom(message.getContent());
        String userId = openMsg.getUserId();
        String t = openMsg.getT();
        String s = openMsg.getS();
        String c = openMsg.getC();
        String v = openMsg.getV();
        int isCrossServer = openMsg.getIsCrossServer();

        boolean loginSuccess = false;
        long startTime = System.currentTimeMillis();

        String fromUrl = openMsg.getFromUrl();
        if (StringUtils.isBlank(fromUrl)) {
            fromUrl = "";
        }
        String ip = NettyUtil.userIpMap.get(socket.getCtx().channel());
        if (StringUtils.isBlank(ip)) {
            ip = NettyUtil.getRemoteAddr(socket.getCtx());
        }
        String channelId = socket.getCtx().channel().id().asShortText();
        if (NumberUtils.toLong(userId, 0) <= 0) {
            StringBuilder sb = new StringBuilder("LoginCommand|error|userId0");
            sb.append("|").append(userId);
            sb.append("|").append(channelId);
            sb.append("|").append(ip);
            sb.append("|").append(v);
            LogUtil.errorLog.error(sb.toString());
            socket.sendComMessage(WebSocketMsgType.res_code_err, 4, "参数错误[userId null]");
            return null;
        }

        // 验证md5
        if (!Md5CheckUtil.checkLoginMd5(userId, t, s)) {
            StringBuilder sb = new StringBuilder("LoginCommand|error|md5");
            sb.append("|").append(userId);
            sb.append("|").append(channelId);
            sb.append("|").append(ip);
            sb.append("|").append(v);
            LogUtil.errorLog.error(sb.toString());
            socket.sendComMessage(WebSocketMsgType.res_code_err, 4, "验证错误，请稍后再试");
            return null;
        }
        loginUserId = Long.parseLong(userId);
        String currentCommand = GameUtil.USER_COMMAND_MAP.get(loginUserId);
        if (currentCommand != null) {
            StringBuilder sb = new StringBuilder("LoginCommand|error|waitCommand");
            sb.append("|").append(userId);
            sb.append("|").append(channelId);
            sb.append("|").append(ip);
            sb.append("|").append(v);
            sb.append("|").append(currentCommand);
            LogUtil.errorLog.error(sb.toString());
            socket.sendComMessage(WebSocketMsgType.res_code_err, 4, "请稍后，系统忙...");
            return null;
        }

        LogUtil.monitorLog.info("LoginCommand|login|start|{}|{}|{}|{}|{}|{}|{}|{}", userId, t, s, c, v, channelId, ip, openMsg.getMsgList());
        long now = TimeUtil.currentTimeMillis();
        player = PlayerManager.getInstance().getPlayer(loginUserId);
        if (player == null) {
            // 需要获取用户数据
            boolean ret = loadPlayer(loginUserId, socket);
            if (returnPlayer) {
                return player;
            }
            if (!ret) {
                LogUtil.e(userId + "获取用户数据错误");
                return null;
            }
        } else {
            // 如果有数据没有保存先保存
            player.saveBaseInfo();
            if (isCrossServer == 1) {
                player.refreshPlayer();
            } else if (player.getSyncTime() == null || (now - player.getSyncTime().getTime() > 20 * SharedConstants.SENCOND_IN_MINILLS)) {
                player.refreshPlayer();
            }
        }

        if (!TenMinuteFixedRateTask.checkBlack(player) || player.isForbidLogin()) {
            StringBuilder sb = new StringBuilder("LoginCommand|error|forbidden");
            sb.append("|").append(userId);
            sb.append("|").append(channelId);
            sb.append("|").append(ip);
            sb.append("|").append(v);
            LogUtil.errorLog.error(sb.toString());
            socket.sendComMessage(WebSocketMsgType.res_code_err, 4, "您已被禁止登陆");
            return null;
        }

        if (StringUtils.isBlank(player.getSessionId()) || c == null) {
            StringBuilder sb = new StringBuilder("LoginCommand|error|sessionCodeNull");
            sb.append("|").append(userId);
            sb.append("|").append(channelId);
            sb.append("|").append(ip);
            sb.append("|").append(v);
            sb.append("|").append(c);
            sb.append("|").append(player.getSessionId());
            LogUtil.errorLog.error(sb.toString());
            socket.accountConflict(player);
            return null;
        }

        // 验证
        if (!c.equals(player.getSessionId())) {
            // session验证不一样，进行过登录登录操作
            String oldSession = player.getSessionId();
            player.refreshPlayer();
            if (!c.equals(player.getSessionId())) {
                StringBuilder sb = new StringBuilder("LoginCommand|error|sessionCodeError");
                sb.append("|").append(userId);
                sb.append("|").append(channelId);
                sb.append("|").append(ip);
                sb.append("|").append(v);
                sb.append("|").append(c);
                sb.append("|").append(oldSession);
                sb.append("|").append(player.getSessionId());
                LogUtil.errorLog.error(sb.toString());
                socket.accountConflict(player);
                return null;
            }
        }

        // 金币场匹配
        GoldRoomMatchPlayer matchPlayer = null;
        JSONObject loginExtendJson = null;
        if (StringUtils.isNotBlank(player.getLoginExtend())) {
            loginExtendJson = JSON.parseObject(player.getLoginExtend());
            if (loginExtendJson.containsKey("grmId")) {
                matchPlayer = GoldRoomDao.getInstance().loadGoldRoomMatchPlayer(player.getUserId());
                if (matchPlayer == null) {
                    loginExtendJson.remove("grmId");
                    Map<String, Object> map = new HashMap<>();
                    map.put("loginExtend", loginExtendJson.toJSONString());
                    player.setLoginExtend(loginExtendJson.toJSONString());
                    UserDao.getInstance().updateUser(String.valueOf(player.getUserId()), map);
                } else {
                    if (GameServerConfig.SERVER_ID != matchPlayer.getServerId()) {
                        LogUtil.e("needGoServer|" + userId + "|" + matchPlayer.getServerId());
                        PlayerManager.getInstance().removePlayer(player);
                        socket.sendComMessage(WebSocketMsgType.res_code_login);
                        GameServerUtil.sendChangeServerCommand(socket, player.getTotalCount(), ServerManager.loadServer(matchPlayer.getServerId()));
                        return player;
                    }
                }
            }
        }

       	//玩家处于比赛场等待界面
		CompetitionUtil.pushBackSign(player, socket);
		//玩家处于比赛场等待晋级界面
        CompetitionUtil.pushPlayCheck(player, socket);

        int cards = (int) player.loadAllCards();
        int golds = (int) player.loadAllGolds();
        long playTableId = 0;
        int playerMark = 0;

        try {
            if (player.getPlayingTableId() != 0) {
                BaseTable table = TableManager.getInstance().getTable(player.getPlayingTableId());
                Player player1;
                if (table != null && (player1 = table.getPlayerMap().get(player.getUserId())) != null) {
                    if (player1 != player) {
                        PlayerManager.getInstance().addPlayer(player1, true);
                        player = player1;
                    }
                } else{
                    RoomBean room = TableDao.getInstance().queryRoom(player.getPlayingTableId());
                    if (room != null && room.getServerId() != GameServerConfig.SERVER_ID) {
                        Server server = ServerManager.loadServer(room.getServerId());
                        if (server == null) {
                            LogUtil.msgLog.info("playingTableId0|5|" + player.getUserId() + "|" + player.getEnterServer() + "|" + player.getPlayingTableId());
                            player.clearTableInfo();
                            player.cleanXipaiData();
                            player.setEnterServer(GameServerConfig.SERVER_ID);
                            player.setPlayingTableId(0);
                            playerMark = 1;
                        } else {
                            StringBuilder sb = new StringBuilder("LoginCommand|error|serverError");
                            sb.append("|").append(userId);
                            sb.append("|").append(channelId);
                            sb.append("|").append(ip);
                            sb.append("|").append(v);
                            sb.append("|").append(player.getEnterServer());
                            sb.append("|").append(player.getPlayingTableId());
                            sb.append("|").append(room.getServerId());
                            sb.append("|").append(GameServerConfig.SERVER_ID);
                            LogUtil.errorLog.error(sb.toString());

                            PlayerManager.getInstance().removePlayer(player);
                            socket.sendComMessage(WebSocketMsgType.res_code_login);
                            GameServerUtil.sendChangeServerCommand(socket, player.getTotalCount(), server);
                            return player;
                        }
                    }
                }
            }

            // 是否弹出签到提示
            String isOut =player.loadGoldSigns();
            // 是否在房间标示
            String isInRoom = "0";
            OldPlayerBackActivityCmd.oldPlayerBackReward(player);

            if (GameServerConfig.SERVER_ID != player.getEnterServer()) {
                player.setEnterServer(GameServerConfig.SERVER_ID);
                playerMark = 1;
                LogUtil.msgLog.info("update player enterServer:userId=" + player.getUserId() + ",enterServer=" + GameServerConfig.SERVER_ID);
            }

            player.setIsOnline(1, false);

            // 需要远程去获取数据(银币等)
            player.setMyWebSocket(socket);
            socket.setPlayer(player);

            Player player1 = PlayerManager.getInstance().addPlayer(player, false);
            if (player1 != player) {
                StringBuilder sb = new StringBuilder("LoginCommand|error|alreadyLogin");
                sb.append("|").append(userId);
                sb.append("|").append(channelId);
                sb.append("|").append(ip);
                sb.append("|").append(v);
                LogUtil.errorLog.error(sb.toString());
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_223));
                return null;
            }

            WebSocketManager.addWebSocket(socket);

            player.initMsgCheckCode();

            String matchType = ResourcesConfigsUtil.loadServerPropertyValue("matchType", "1");

            BaseTable table = player.getPlayingTable();

            if (table != null) {
                playTableId = table.getId();
                isInRoom = "1";
                player.setIsEntryTable(SharedConstants.table_online);
                if (player.getState() == null) {
                    player.changeState(SharedConstants.player_state.entry);
                }
            } else if (GoldRoomUtil.isGoldRoom(player)) {
                GoldRoomUser goldRoomUser = GoldRoomDao.getInstance().loadGoldRoomUser(player.getPlayingTableId(), player.getUserId());
                if (goldRoomUser != null) {
                    GoldRoom goldRoom0 = GoldRoomDao.getInstance().loadGoldRoom(goldRoomUser.getRoomId());
                    if (goldRoom0 != null && "2".equals(matchType)) {
                        BaseTable baseTable = TableManager.getInstance().getTable(goldRoom0.getTableId());
                        if (baseTable != null) {
                            playTableId = player.getPlayingTableId();
                            isInRoom = "1";
                        } else {
                            player.setPlayingTableId(0);
                            playerMark = 1;
                        }
                    } else {
                        if (goldRoom0 == null || goldRoom0.isOver()) {
                            player.setPlayingTableId(0);
                            playerMark = 1;
                        } else if (goldRoom0.isPlaying()) {
                            playTableId = player.getPlayingTableId();
                            isInRoom = "1";
                        } else {
                            playTableId = player.getPlayingTableId();
                            isInRoom = "1";
                        }
                    }
                } else {
                    player.setPlayingTableId(0);
                    playerMark = 1;
                }
            } else {
                player.changeState(SharedConstants.player_state.entry);
                if (player.getMyExtend().getPlayerStateMap().size() > 0) {
                    player.getMyExtend().getPlayerStateMap().clear();
                    player.changeExtend();
                    playerMark = 1;
                }

                if (player.getPlayingTableId() != 0) {
                    LogUtil.msgLog.info("playingTableId0|8|" + player.getUserId() + "|" + player.getEnterServer() + "|" + player.getPlayingTableId());
                    player.setPlayingTableId(0);
                    playerMark = 1;
                }
                player.setSeat(0);
                // 重新登录标示已经退出房间
                player.setIsEntryTable(0);
                playerMark = 1;
            }

            if ("1".equals(isInRoom)) {
                isOut = "0";
            }

            int seat = NumberUtils.toInt(player.getMyExtend().getPlayerStateMap().get("seat"), 0);
            if (seat > 0) {
                player.setSeat(seat);
            }

            int name = player.loadDesignation();

            player.getMyExtend().setVersions(StringUtil.parseVersions(v));
            player.setVersion(v);

            List<String> strMsgs = new ArrayList<>();
            List<Integer> intMsgs = new ArrayList<>();
            strMsgs.add(String.valueOf(playTableId));//0房间号
            strMsgs.add(String.valueOf(player.getConfig()));//1配置
            strMsgs.add(String.valueOf(fromUrl));//2来源
            strMsgs.add("");//3匹配模式的俱乐部ID
            strMsgs.add("");//4芒果段位描述信息
            strMsgs.add(socket.getCtx().channel().id().asShortText());  // 5连接id
            strMsgs.add(String.valueOf(player.loadAllCoin())); // 6金币
            strMsgs.add(String.valueOf(System.currentTimeMillis() / 1000)); // 7服务器时间
            strMsgs.add(String.valueOf(player.getGoldRoomGroupId())); // 8 金币场绑定的亲友圈id
            strMsgs.add(String.valueOf(player.loadAllGolds())); // 9 金币
            strMsgs.add(String.valueOf(player.getHeadimgurl())); // 10 头像
            if(matchPlayer != null) { // 11 匹配信息 playType|matchType|configId
                strMsgs.add(String.valueOf(matchPlayer.getPlayType()) + "|" + String.valueOf(matchPlayer.getMatchType()) + "|" + String.valueOf(matchPlayer.getConfigId() != null ? matchPlayer.getConfigId() : "0"));
            }else{
                strMsgs.add("");
            }

            intMsgs.add(cards);//0房卡或钻石
            intMsgs.add(name);//1官衔
            intMsgs.add((int) player.getPayBindId());//2绑定的邀请码
            intMsgs.add(golds);//3金币
            intMsgs.add(0);//4体力
            intMsgs.add(player.getGoldPlayer().getGrade());//5芒果段位
            intMsgs.add(0);//6幸运红包开关
            intMsgs.add(matchPlayer != null ? 1 : 0);//7是否在比赛场
            intMsgs.add(0);//8俱乐部匹配模式可取消的剩余时间
            intMsgs.add(player.getIsCreateGroup());//9是否可以创亲友圈
            intMsgs.add(player.isSendDiamondsPermission());//10 是否能赠送钻石
            intMsgs.add(player.getIsSpAdmin());//11 是否是超级管理员（可配置权限）
            intMsgs.add((int)player.getPayBindId());//12绑定人
            intMsgs.add(UserDao.getInstance().selectUserIsSalesman(player.getUserId()));//13 是否是业务员
            /**发送登录成功消息**/
            player.writeComMessage(WebSocketMsgType.res_code_login, strMsgs, intMsgs);
            //发送部分客户端牌配置信息
            player.writeSocket(MissionConfigUtil.getSendConfig());
            player.setLoginActionTime(now);

            if (isCrossServer == 0) {
                // 发送签到信息
                player.writeGoldSignInfo(isOut,isInRoom);
                player.checkLogin();
            }
            player.startServerRedCom();

            if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("ip_from_game"))) {
                if (StringUtils.isNotBlank(ip)) {
                    player.setIp(ip);
                }
            }

            player.setIsOnline(1, true);
            playerMark = 2;
            loginSuccess = true;

            if (table != null) {
                table.broadIsOnlineMsg(player, SharedConstants.table_online);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (player != null) {
                if (playerMark > 0) {
                    player.saveBaseInfo(true);
                }
            }

            if (!loginSuccess) {
                WebSocketManager.removeWebSocket(socket.getPlayer());
			}
        }

        socket.setLoginSuccess(true);
        Date date = new Date();
        player.setLoginTime(date);

        LoginDataUtil.loginData(userId, date);

        StringBuilder sb = new StringBuilder("LoginCommand|login|end");
        sb.append("|").append(userId);
        sb.append("|").append(playTableId);
        sb.append("|").append(player.getPayBindId());
        sb.append("|").append(cards);
        sb.append("|").append(golds);
        sb.append("|").append(System.currentTimeMillis() - startTime);
        sb.append("|").append(channelId);
        sb.append("|").append(ip);
        sb.append("|").append(v);
        LogUtil.monitorLog.info(sb.toString());

        player.sendActionLog(LogConstants.reason_login, "");
        return player;
    }

    private boolean loadPlayer(long userId, MyWebSocket socket) throws Exception {
        RegInfo info = UserDao.getInstance().selectUserByUserId(userId);

        if (info == null) {
            LogUtil.e("login err:user is null-->" + userId);
            socket.send(WebSocketMsgType.sc_err_login, "登录错误:userId-" + userId);
            socket.accountConflict(info);
            return false;
        }
        boolean newPlayer = true;

        if (info.getEnterServer() != 0 && info.getPlayingTableId() != 0) {
            BaseTable playIngTable = TableManager.getInstance().getTable(info.getPlayingTableId());
            if (playIngTable == null) {
                if (GameServerConfig.SERVER_ID == info.getEnterServer()) {
                    LogUtil.msgLog.info("playingTableId0|1|" + info.getUserId() + "|" + info.getEnterServer() + "|" + info.getPlayingTableId());
                    info.setPlayingTableId(0);
                    Map<String, Object> paramMap = new HashMap<String, Object>();
                    paramMap.put("playingTableId", 0);
                    UserDao.getInstance().save(info.getFlatId(), info.getPf(), info.getUserId(), paramMap);
                } else {
                    Server server = ServerManager.loadServer(info.getEnterServer());
                    if (server == null) {
                        LogUtil.msgLog.info("playingTableId0|2|" + info.getUserId() + "|" + info.getEnterServer() + "|" + info.getPlayingTableId());
                        if (player == null) {
                            player = new CommonPlayer();
                            player.loadFromDB(info);
                            newPlayer = false;
                        }
                        player.clearTableInfo();
                        player.cleanXipaiData();
                        player.setEnterServer(GameServerConfig.SERVER_ID);
                        player.setPlayingTableId(0);
                        player.saveBaseInfo();
                    } else {
                        LogUtil.e("login loadPlayer err-->" + info.getUserId() + "-登录错误:应进入-" + info.getEnterServer() + "-房间号:" + info.getPlayingTableId());
                        if (player == null) {
                            player = new CommonPlayer();
                            player.loadFromDB(info);
                        }
                        PlayerManager.getInstance().removePlayer(player);
                        socket.sendComMessage(WebSocketMsgType.res_code_login);
                        returnPlayer = true;
                        GameServerUtil.sendChangeServerCommand(socket, player.getTotalCount(), server);
                        return false;
                    }
                }
                if (newPlayer) {
                    player = new CommonPlayer();
                    player.loadFromDB(info);
                    newPlayer = false;
                }
            } else {
                player = playIngTable.getPlayer(info.getUserId(), playIngTable.getPlayerClass());
                if (player == null) {
                    player = playIngTable.getRoomPlayerMap().get(info.getUserId());
                    if (player == null) {
                        player = ObjectUtil.newInstance(playIngTable.getPlayerClass());
                        player.loadFromDB(info);
                        playIngTable.initPlayers(info.getUserId(), player);
                    } else {
                        if (player.getMyExtend().getPlayerStateMap().containsKey("seat")) {
                            player.changeState(SharedConstants.player_state.ready);
                        } else {
                            player.changeState(SharedConstants.player_state.entry);
                        }
                    }
                } else {
                    if (playIngTable.getState() == SharedConstants.table_state.play) {
                        player.changeState(SharedConstants.player_state.play);
                    }
                }
                newPlayer = false;
            }
        }

        if (newPlayer) {
            player = new CommonPlayer();
            player.loadFromDB(info);
//            player.loadGoldPlayer(info);
        }

        return true;
    }

    @Override
    public void setMsgTypeMap() {
        msgTypeMap.put(ComRes.class, WebSocketMsgType.sc_com);
    }

    public long getLoginUserId() {
        return loginUserId;
    }

}
