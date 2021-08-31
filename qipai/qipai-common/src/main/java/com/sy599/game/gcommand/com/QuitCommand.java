package com.sy599.game.gcommand.com;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.bean.gold.GoldRoomUser;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

/**
 * 退出
 *
 * @author lc
 */
public class QuitCommand extends BaseCommand {
    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        if (player.getPlayingTableId() <= 0) {
            return;
        }
        BaseTable table = player.getPlayingTable();
        if (table == null) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_1));
            return;
        }
        if (player.isPlayingMatch() || table.isCompetitionRoom()) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_221));
            return;
        }
        if (table.isGoldRoom()) {
            quitGoldRoom(player);
            return;
        }


        synchronized (table) {
            if (table.getPlayerMap().containsKey(player.getUserId()) && player.getMyExtend().getPlayerStateMap().get("2") != null) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_9, table.getPlayBureau()));
                return;
            }

            String pre = player.getMyExtend().getPlayerStateMap().get("1");
            int groupId = 0;
            if (table.isGroupRoom()) {
                groupId = Integer.parseInt(table.loadGroupId());
            }
            if ("1".equals(pre)) {
                LogUtil.msgLog.info("playingTableId0|4|" + player.getUserId() + "|" + player.getEnterServer() + "|" + player.getPlayingTableId());
                table.getRoomPlayerMap().remove(player.getUserId());
                player.clearTableInfo();
                player.cleanXipaiData();
                player.getMyExtend().getPlayerStateMap().clear();
                player.changeExtend();
                ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tablequit, String.valueOf(player.getUserId()),String.valueOf(table.calcTableType()), table.getPlayType(), 1, 0, groupId, groupId);
                player.writeSocket(com.build());
                return;
            } else if ("0".equals(pre)) {
                ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tablequit, String.valueOf(player.getUserId()),String.valueOf(table.calcTableType()), table.getPlayType(), 1, 0, groupId, groupId);
                player.writeSocket(com.build());
                return;
            }
            if (!table.getPlayerMap().containsKey(player.getUserId()) && !table.getRoomPlayerMap().containsKey(player.getUserId())) {
                ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tablequit, String.valueOf(player.getUserId()),String.valueOf(table.calcTableType()), table.getPlayType());

                GeneratedMessage msg = com.build();
                player.writeSocket(msg);
                return;
            }
            if (table.isCompetition()) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_20));
                return;
            }
            if (table.getPlayBureau() != 1) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_9, table.getPlayBureau()));
                return;
            }
            if (table.getState() != table_state.ready || !table.canQuit(player)) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_10));
                return;
            }
            if (table.quitPlayer(player)) {
                table.onPlayerQuitSuccess(player);
                // 修改代开房间玩家的信息记录
                table.updateDaikaiTablePlayer();
                // 修改room表
                table.updateRoomPlayers();
            }
        }

    }

    private void quitGoldRoom(Player player) throws Exception {
        BaseTable table = player.getPlayingTable();
        GoldRoom goldRoom = table.getGoldRoom();
        if (goldRoom != null) {
            synchronized (goldRoom) {
                if (!goldRoom.isNotStart()) {
                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_10));
                    return;
                }
                GoldRoomUser goldRoomUser = table.getGoldRoomUser(player.getUserId());
                if (goldRoomUser != null) {
                    int ret = GoldRoomDao.getInstance().deleteGoldRoomUser(goldRoomUser.getRoomId(), player.getUserId());
                    if (ret > 0) {
                        ret = GoldRoomDao.getInstance().addGoldRoomPlayerCount(goldRoom.getKeyId(), -1);
                        if (ret > 0) {
                            player.setPlayingTableId(0);
                            player.saveBaseInfo();

                            table.getGoldRoomUserMap().remove(player.getUserId());
                            goldRoom.setCurrentCount(goldRoom.getCurrentCount() - 1);

                            StringBuilder sb = new StringBuilder("GoldRoom|quit");
                            sb.append("|").append(goldRoom.getKeyId());
                            sb.append("|").append(goldRoom.getModeId());
                            sb.append("|").append(player.getUserId());
                            LogUtil.msgLog.info(sb.toString());

                            ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tablequit, String.valueOf(player.getUserId()),String.valueOf(table.calcTableType()));
                            player.writeSocket(com.build());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setMsgTypeMap() {

    }

}
