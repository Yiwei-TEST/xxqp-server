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

    /*** ????????????????????? **/
    private static final Set<Integer> logCommandSet = new HashSet<>();

    static {
        gcomMap.put(1, new Command(CreateTableCommand.class, true));
        /** ???????????? **/
        gcomMap.put(2, new Command(JoinTableCommand.class, true));
        /** ??????????????? **/
        gcomMap.put(3, new Command(StartNextCommand.class, true));
        /** ?????? **/
        gcomMap.put(4, new Command(ReadyCommand.class, true));
        /** ping **/
        gcomMap.put(5, new Command(PingCommand.class, false));
        // 5 ping
        /** ?????? ???????????? ?????? **/
        gcomMap.put(6, new Command(QuitCommand.class, true));
        // comMap.put(6, LeaveCommand.class);
        /** ?????? **/
        gcomMap.put(7, new Command(DissCommand.class, true));
        /** ???????????? **/
        gcomMap.put(8, new Command(AnswerDissCommand.class, true));
        /** ?????? **/
        gcomMap.put(9, new Command(QuickChatCommand.class, false));
        /** ?????? **/
        gcomMap.put(10, new Command(ConfigCommand.class, false));
        /** ????????? **/
        gcomMap.put(11, new Command(DrawLotteryCommand.class, false));
        /** ?????? **/
        gcomMap.put(12, new Command(MessageCommand.class, false));
        /** ?????? **/
        gcomMap.put(13, new Command(AfkCommand.class, false));
        /** ?????? **/
        gcomMap.put(14, new Command(RecordCommand.class, false));
        /** ??????**/
        gcomMap.put(15, new Command(SignCommand.class, false));
        ///** ??????????????????**/
        //gcomMap.put(16, new Command(SevenSignCommand.class, false));
        /** ??????IP???????????? **/
        gcomMap.put(WebSocketMsgType.req_com_sameip_dissroom, new Command(SameIpDissCommand.class, true));
        /** ???????????? **/
        gcomMap.put(WebSocketMsgType.req_com_daikaitable, new Command(DaikaiCommand.class, true));
        /** ??????????????????**/
        //gcomMap.put(WebSocketMsgType.req_com_dissdaikai, new Command(DissDaikaiCommand.class, true));
        /** ?????????????????? **/
        gcomMap.put(WebSocketMsgType.req_com_getserverid, new Command(GetServerCommand.class, true));
        /** ????????????????????? **/
        gcomMap.put(WebSocketMsgType.req_com_latitudeandlongitude, new Command(LatitudeLongitudeCommand.class, false));
        /** ??????????????????????????????????????????userId???seat?????????id??? **/
        gcomMap.put(WebSocketMsgType.req_com_gotye, new Command(GotyeCommand.class, false));
        /** ????????????????????????**/
        gcomMap.put(WebSocketMsgType.req_com_golduserchange, new Command(GoldUserChangeCommand.class, true));
        /** ???????????? **/
        gcomMap.put(40, new Command(CommonChatCommand.class, false));
        /** ?????????????????????**/
        gcomMap.put(WebSocketMsgType.req_com_goldchat, new Command(GoldChatCommand.class, false));
        /** ????????????**/
        gcomMap.put(WebSocketMsgType.cs_activity, new Command(ActivityCommand.class, false));
        /** ???????????????**/
        gcomMap.put(WebSocketMsgType.req_com_gold_login, new Command(GoldLoginCommand.class, true));
        /** ???????????????**/
        gcomMap.put(WebSocketMsgType.req_com_gold_exchange, new Command(GoldExchangeCommand.class, true));
        /** ???????????????**/
        gcomMap.put(WebSocketMsgType.req_com_group_tables, new Command(GroupCommand.class, true));
        /** ???????????????????????????**/
        gcomMap.put(WebSocketMsgType.req_com_group_table_msg, new Command(GroupTableCommand.class, false));
        /** ???????????????**/
        gcomMap.put(WebSocketMsgType.req_com_group_match, new Command(GroupMatchCommand.class, true));
        /** ???????????????**/
        gcomMap.put(WebSocketMsgType.com_group_change_room, new Command(GroupChangeTableCommand.class, true));
        /** ?????????????????????**/
        gcomMap.put(WebSocketMsgType.com_group_room_fire_player, new Command(GroupTableFirePlayerCommand.class, true));
        /** ?????????????????????**/
        gcomMap.put(WebSocketMsgType.com_group_room_invite, new Command(GroupRoomInvitationCommand.class, false));
        /** ?????????**/
        gcomMap.put(WebSocketMsgType.req_com_match_code, new Command(MatchCommand.class, true));
        /** ???????????????**/
        gcomMap.put(WebSocketMsgType.req_com_match_rank_code, new Command(MatchRankCommand.class, false));
        /** ?????????????????????????????????????????????**/
        gcomMap.put(WebSocketMsgType.req_com_update_latitudeandlongitude, new Command(UpdateLatitudeLongitudeCommand.class, false));
        /** ??????**/
        gcomMap.put(WebSocketMsgType.cs_task, new Command(TaskCommand.class, false));
        /** ?????????**/
        gcomMap.put(WebSocketMsgType.cs_rank, new Command(RankCommand.class, false));
        /** ?????????**/
        gcomMap.put(WebSocketMsgType.cs_kefuhao, new Command(KeFuHaoCommand.class, false));
        /** ??????????????????????????????**/
        gcomMap.put(WebSocketMsgType.cs_mg_playerInfo, new Command(MGPlayerInfoCommand.class, false));
        /** ??????????????????????????????**/
        gcomMap.put(WebSocketMsgType.cs_luck_redbag, new Command(LuckyRedbagCommand.class, false));
        /** ??????????????????????????????????????????????????????????????????????????????????????????????????? **/
        gcomMap.put(WebSocketMsgType.cs_quit_gold_room, new Command(QuitGoldRoomCommand.class, false));
        /** ???????????????????????? **/
        gcomMap.put(WebSocketMsgType.cs_join_bairen_table, new Command(JoinBaiRenTableCommand.class, true));
        /** ???????????????????????????????????? **/
        gcomMap.put(WebSocketMsgType.cs_quit_bairen_table, new Command(QuitBaiRenTableCommand.class, true));
        /** ?????? **/
        gcomMap.put(WebSocketMsgType.com_code_ticket, new Command(TicketCommand.class, false));
        /** ???????????????????????? */
        gcomMap.put(WebSocketMsgType.group_balcony_createdtable, new Command(GroupBalconyCreatedTableCommand.class, true));
        /** ?????????????????????**/
        gcomMap.put(WebSocketMsgType.req_com_group_table_config, new Command(GroupTableConfigCommand.class, true));
        /** ?????????????????????**/
        gcomMap.put(WebSocketMsgType.req_com_group_table_list, new Command(GroupTableListCommand.class, true));
        /** ???????????????????????????????????????????????????????????????**/
        gcomMap.put(WebSocketMsgType.req_code_table_replenish, new Command(ReplenishTableCommand.class, true));
        /** ?????????????????????**/
        gcomMap.put(WebSocketMsgType.req_com_group_table_list_new, new Command(GroupTableListNewCommand.class, true));
        /** ??????????????????**/
        gcomMap.put(WebSocketMsgType.req_code_goldsign, new Command(GoldSignCommand.class, false));
        /** ????????????????????????**/
        gcomMap.put(WebSocketMsgType.res_code_missionabout, new Command(MissionCommand.class, false));
        /*** ???????????????***/
        gcomMap.put(WebSocketMsgType.req_com_gold_room_command, new Command(GoldRoomCommand.class, false));
        /*** Solo??????***/
        gcomMap.put(WebSocketMsgType.req_com_solo_room_command, new Command(SoloRoomCommand.class, false));
        /*** ????????????????????????***/
        gcomMap.put(WebSocketMsgType.req_com_gold_room_activity_command, new Command(GoldRoomAcitivityCommand.class, false));
        /** ??????**/
        gcomMap.put(WebSocketMsgType.res_code_active, new Command(ActiveCommand.class, false));
        /**??????*/
        gcomMap.put(WebSocketMsgType.req_code_xipai, new Command(XipaiCommand.class, false));
        /** Gm?????? **/
        gcomMap.put(WebSocketMsgType.req_code_set_mo, new Command(GmCommand.class, true));
        /**??????*/
        gcomMap.put(WebSocketMsgType.req_code_quanxian, new Command(AuthorityCommand.class, false));
        /** ??????comMsg??????????????????????????????**/
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
        // ????????????
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        int code = req.getCode();
        Command command = gcomMap.get(code);
        Class<? extends BaseCommand> cls = command.getCmd();
        if (cls == null) {
            LogUtil.e("common command not exists:code=" + code);
            return;
        }

        //??????????????????????????????????????????command??????
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
        //????????????????????????????????????????????????????????????????????????????????????????????????table????????????????????????????????????????????????
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
            //???????????????????????????
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
     * ????????????????????????
     *
     * @param message
     * @return
     */
    public Object isGMessage(MessageUnit message, Player player) {
        if (message.getMsgType() == WebSocketMsgType.cs_com) {
            Player player0 = null;
            // ????????????
            ComReq req = (ComReq) this.recognize(ComReq.class, message);

            Class<? extends Player> p = TableManager.getPlayerByCode(req.getCode());
            //??????player????????????????????????
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
