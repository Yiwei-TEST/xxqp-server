package com.sy599.game.gcommand;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.bairen.JoinBaiRenTableCommand;
import com.sy599.game.gcommand.bairen.QuitBaiRenTableCommand;
import com.sy599.game.gcommand.com.*;
import com.sy599.game.gcommand.gold.GoldChatCommand;
import com.sy599.game.gcommand.gold.GoldExchangeCommand;
import com.sy599.game.gcommand.gold.GoldLoginCommand;
import com.sy599.game.gcommand.gold.GoldUserChangeCommand;
import com.sy599.game.gcommand.group.*;
import com.sy599.game.gcommand.match.MatchCommand;
import com.sy599.game.gcommand.match.MatchRankCommand;
import com.sy599.game.gcommand.play.GmCommand;
import com.sy599.game.gcommand.play.ReadyCommand;
import com.sy599.game.gcommand.play.StartNextCommand;
import com.sy599.game.gcommand.table.CreateTableCommand;
import com.sy599.game.gcommand.table.JoinTableCommand;
import com.sy599.game.gcommand.table.ReplenishTableCommand;
import com.sy599.game.gcommand.ticket.TicketCommand;
import com.sy599.game.manager.PlayerManager;
import com.sy599.game.manager.TableManager;
import com.sy599.game.message.MessageCommand;
import com.sy599.game.msg.Command;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.msg.serverPacket.ComMsg.PingRes;
import com.sy599.game.msg.serverPacket.MessageResMsg.NoticelistRes;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.msg.serverPacket.TableRes.DealInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.JoinTableRes;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GeneralCommand extends BaseCommand {
    private final static GeneralCommand processor = new GeneralCommand();
    private static final Map<Integer, Command> gcomMap = new HashMap<>();

    /*** 记录日志的命令 **/
    private static final Set<Integer> logCommandSet = new HashSet<>();

    static {
        gcomMap.put(1, new Command(CreateTableCommand.class, true));
        /** 加入房间 **/
        gcomMap.put(2, new Command(JoinTableCommand.class, true));
        /** 开始下一局 **/
        gcomMap.put(3, new Command(StartNextCommand.class, true));
        /** 准备 **/
        gcomMap.put(4, new Command(ReadyCommand.class, true));
        /** ping **/
        gcomMap.put(5, new Command(PingCommand.class, false));
        // 5 ping
        /** 离开 接口改成 退出 **/
        gcomMap.put(6, new Command(QuitCommand.class, true));
        // comMap.put(6, LeaveCommand.class);
        /** 解散 **/
        gcomMap.put(7, new Command(DissCommand.class, true));
        /** 应答解散 **/
        gcomMap.put(8, new Command(AnswerDissCommand.class, true));
        /** 聊天 **/
        gcomMap.put(9, new Command(QuickChatCommand.class, false));
        /** 设置 **/
        gcomMap.put(10, new Command(ConfigCommand.class, false));
        /** 大转盘 **/
        gcomMap.put(11, new Command(DrawLotteryCommand.class, false));
        /** 消息 **/
        gcomMap.put(12, new Command(MessageCommand.class, false));
        /** 暂离 **/
        gcomMap.put(13, new Command(AfkCommand.class, false));
        /** 战绩 **/
        gcomMap.put(14, new Command(RecordCommand.class, false));
        /** 签到**/
        gcomMap.put(15, new Command(SignCommand.class, false));
        ///** 芒果七日签到**/
        //gcomMap.put(16, new Command(SevenSignCommand.class, false));
        /** 相同IP解散房间 **/
        gcomMap.put(WebSocketMsgType.req_com_sameip_dissroom, new Command(SameIpDissCommand.class, true));
        /** 代开房间 **/
        gcomMap.put(WebSocketMsgType.req_com_daikaitable, new Command(DaikaiCommand.class, true));
        /** 解散代开房间**/
        //gcomMap.put(WebSocketMsgType.req_com_dissdaikai, new Command(DissDaikaiCommand.class, true));
        /** 取最佳服务器 **/
        gcomMap.put(WebSocketMsgType.req_com_getserverid, new Command(GetServerCommand.class, true));
        /** 更新玩家经纬度 **/
        gcomMap.put(WebSocketMsgType.req_com_latitudeandlongitude, new Command(LatitudeLongitudeCommand.class, false));
        /** 向其他玩家推送当前玩家信息（userId、seat、语音id） **/
        gcomMap.put(WebSocketMsgType.req_com_gotye, new Command(GotyeCommand.class, false));
        /** 修改金币玩家信息**/
        gcomMap.put(WebSocketMsgType.req_com_golduserchange, new Command(GoldUserChangeCommand.class, true));
        /** 普通聊天 **/
        gcomMap.put(40, new Command(CommonChatCommand.class, false));
        /** 金币场聊天系统**/
        gcomMap.put(WebSocketMsgType.req_com_goldchat, new Command(GoldChatCommand.class, false));
        /** 精彩活动**/
        gcomMap.put(WebSocketMsgType.cs_activity, new Command(ActivityCommand.class, false));
        /** 登陆金币场**/
        gcomMap.put(WebSocketMsgType.req_com_gold_login, new Command(GoldLoginCommand.class, true));
        /** 金币场兑换**/
        gcomMap.put(WebSocketMsgType.req_com_gold_exchange, new Command(GoldExchangeCommand.class, true));
        /** 俱乐部牌桌**/
        gcomMap.put(WebSocketMsgType.req_com_group_tables, new Command(GroupCommand.class, true));
        /** 俱乐部牌桌详细信息**/
        gcomMap.put(WebSocketMsgType.req_com_group_table_msg, new Command(GroupTableCommand.class, false));
        /** 俱乐部匹配**/
        gcomMap.put(WebSocketMsgType.req_com_group_match, new Command(GroupMatchCommand.class, true));
        /** 俱乐部换桌**/
        gcomMap.put(WebSocketMsgType.com_group_change_room, new Command(GroupChangeTableCommand.class, true));
        /** 俱乐部房间踢人**/
        gcomMap.put(WebSocketMsgType.com_group_room_fire_player, new Command(GroupTableFirePlayerCommand.class, true));
        /** 俱乐部房间邀请**/
        gcomMap.put(WebSocketMsgType.com_group_room_invite, new Command(GroupRoomInvitationCommand.class, false));
        /** 晋级赛**/
        gcomMap.put(WebSocketMsgType.req_com_match_code, new Command(MatchCommand.class, true));
        /** 晋级赛排名**/
        gcomMap.put(WebSocketMsgType.req_com_match_rank_code, new Command(MatchRankCommand.class, false));
        /** 服务器转发通知客户端更新经纬度**/
        gcomMap.put(WebSocketMsgType.req_com_update_latitudeandlongitude, new Command(UpdateLatitudeLongitudeCommand.class, false));
        /** 任务**/
        gcomMap.put(WebSocketMsgType.cs_task, new Command(TaskCommand.class, false));
        /** 排行榜**/
        gcomMap.put(WebSocketMsgType.cs_rank, new Command(RankCommand.class, false));
        /** 客服号**/
        gcomMap.put(WebSocketMsgType.cs_kefuhao, new Command(KeFuHaoCommand.class, false));
        /** 芒果玩家用户信息获取**/
        gcomMap.put(WebSocketMsgType.cs_mg_playerInfo, new Command(MGPlayerInfoCommand.class, false));
        /** 王者千分幸运转盘活动**/
        gcomMap.put(WebSocketMsgType.cs_luck_redbag, new Command(LuckyRedbagCommand.class, false));
        /** 低限制条件的退出金币场房间（每小局结束后，可退出，无需房间大结算） **/
        gcomMap.put(WebSocketMsgType.cs_quit_gold_room, new Command(QuitGoldRoomCommand.class, false));
        /** 加入百人玩法房间 **/
        gcomMap.put(WebSocketMsgType.cs_join_bairen_table, new Command(JoinBaiRenTableCommand.class, true));
        /** 玩家主动退出百人玩法房间 **/
        gcomMap.put(WebSocketMsgType.cs_quit_bairen_table, new Command(QuitBaiRenTableCommand.class, true));
        /** 礼券 **/
        gcomMap.put(WebSocketMsgType.com_code_ticket, new Command(TicketCommand.class, false));
        /** 俱乐部多玩法创房 */
        gcomMap.put(WebSocketMsgType.group_balcony_createdtable, new Command(GroupBalconyCreatedTableCommand.class, true));
        /** 俱乐部配置信息**/
        gcomMap.put(WebSocketMsgType.req_com_group_table_config, new Command(GroupTableConfigCommand.class, true));
        /** 俱乐部房间列表**/
        gcomMap.put(WebSocketMsgType.req_com_group_table_list, new Command(GroupTableListCommand.class, true));
        /** 房间设置信息补充，主要用于部分局内选项设置**/
        gcomMap.put(WebSocketMsgType.req_code_table_replenish, new Command(ReplenishTableCommand.class, true));
        /** 俱乐部房间列表**/
        gcomMap.put(WebSocketMsgType.req_com_group_table_list_new, new Command(GroupTableListNewCommand.class, true));
        /** 七天金币签到**/
        gcomMap.put(WebSocketMsgType.req_code_goldsign, new Command(GoldSignCommand.class, false));
        /** 金币任务相关消息**/
        gcomMap.put(WebSocketMsgType.res_code_missionabout, new Command(MissionCommand.class, false));
        /*** 金币场相关***/
        gcomMap.put(WebSocketMsgType.req_com_gold_room_command, new Command(GoldRoomCommand.class, false));
        /*** Solo相关***/
        gcomMap.put(WebSocketMsgType.req_com_solo_room_command, new Command(SoloRoomCommand.class, false));
        /*** 端午粽子排行活动***/
        gcomMap.put(WebSocketMsgType.req_com_gold_room_activity_command, new Command(GoldRoomAcitivityCommand.class, false));
        /** 活动**/
        gcomMap.put(WebSocketMsgType.res_code_active, new Command(ActiveCommand.class, false));
        /**洗牌*/
        gcomMap.put(WebSocketMsgType.req_code_xipai, new Command(XipaiCommand.class, false));
        /** Gm操作 **/
        gcomMap.put(WebSocketMsgType.req_code_set_mo, new Command(GmCommand.class, true));
        /**权限*/
        gcomMap.put(WebSocketMsgType.req_code_quanxian, new Command(AuthorityCommand.class, false));
        /** 其他comMsg，一些松散的接口访问**/
        gcomMap.put(WebSocketMsgType.req_code_comMsg, new Command(ComMsgCommand.class, false));

        logCommandSet.add(1);
        logCommandSet.add(2);
        logCommandSet.add(3);
        logCommandSet.add(4);
        logCommandSet.add(6);
        logCommandSet.add(7);
        logCommandSet.add(8);
        logCommandSet.add(25);
    }

    public static GeneralCommand getInstance() {
        return processor;
    }

    public void execute(ChannelHandlerContext ctx, Player player, MessageUnit message) throws Exception {
        if (message.getMsgType() != WebSocketMsgType.cs_com) {
            return;
        }
        // 通用消息
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        int code = req.getCode();
        Command command = gcomMap.get(code);
        Class<? extends BaseCommand> cls = command.getCmd();
        if (cls == null) {
            LogUtil.e("common command not exists:code=" + code);
            return;
        }

        //记录创房、加入、解散、代开房command信息
        if (player != null && logCommandSet.contains(code)) {
            StringBuilder sb = new StringBuilder("GeneralCommand");
            sb.append("|").append(code);
            sb.append("|").append(message.getMsgType());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getPlayingTableId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(req.getParamsList());
            sb.append("|").append(req.getStrParamsList());
            sb.append("|").append(player.getClass().getSimpleName());
            sb.append("|").append(cls.getSimpleName());
            LogUtil.msgLog.info(sb.toString());
        }

		//System.err.println("GeneralCommand request code:" + code + " cls:" + cls);


		boolean playerIsNotNull = player != null;
        //创建房间前会先发送消息确认通信服务器地址，之后创房才会访问到对应table，重连、加入退出房间走的也是此处
        if (command.isWait() && playerIsNotNull) {
            if (GameUtil.USER_COMMAND_MAP.put(player.getUserId(), cls.getSimpleName()) == null) {
                try {
                    BaseCommand action = ObjectUtil.newInstance(cls);
                    action.setCtx(ctx);
                    action.setPlayer(player);
                    action.execute(player, message);
                } catch (Throwable t) {
                    throw t;
                } finally {
                    GameUtil.USER_COMMAND_MAP.remove(player.getUserId());
                }
            }
        } else {
            //心跳消息走的是此处
            BaseCommand action = ObjectUtil.newInstance(cls);
            if (playerIsNotNull || action.allowPlayerIsNull()) {
                action.setCtx(ctx);
                action.setPlayer(player);
                action.execute(player, message);
            }
        }
    }

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ChannelHandlerContext ctx = getCtx();
        if (ctx == null && player != null) {
            ctx = player.getMyWebSocket().getCtx();
        }
        execute(ctx, player, message);
    }

    /**
     * 判断是否通用消息
     *
     * @param message
     * @return
     */
    public Object isGMessage(MessageUnit message, Player player) {
        if (message.getMsgType() == WebSocketMsgType.cs_com) {
            Player player0 = null;
            // 通用消息
            ComReq req = (ComReq) this.recognize(ComReq.class, message);

            Class<? extends Player> p = TableManager.getPlayerByCode(req.getCode());
            //转换player为对应玩法继承类
            if (p != null && player != null) {
                player0 = PlayerManager.getInstance().changePlayer(player, p);
            }

            if (!gcomMap.containsKey(req.getCode())) {
                return player0 == null ? false : new Object[]{false, player0};
            }
            return player0 == null ? true : new Object[]{true, player0};
        }
        return false;
    }

    @Override
    public void setMsgTypeMap() {
        msgTypeMap.put(CreateTableRes.class, WebSocketMsgType.sc_createtable);
        msgTypeMap.put(JoinTableRes.class, WebSocketMsgType.sc_jointable);
        msgTypeMap.put(DealInfoRes.class, WebSocketMsgType.sc_dealcards);
        msgTypeMap.put(PingRes.class, WebSocketMsgType.sc_ping);
        msgTypeMap.put(NoticelistRes.class, WebSocketMsgType.sc_message);
    }
}
