package com.sy599.game.gcommand.play;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.TableRes;
import com.sy599.game.util.GameUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class ReadyCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        BaseTable table = player.getPlayingTable();
        if (table == null || table.isGoldRoom()) {
            return;
        }
        if (player.getState() != player_state.entry && player.getState() != player_state.over) {
            return;
        }
        boolean readyResult = false;
        long currentUserId = player.getUserId();
        try {
            synchronized (table) {
                if (player.getState() != player_state.entry && player.getState() != player_state.over) {
                    return;
                }
                if (table.ready(player) && "1".equals(table.getRoomModeMap().get("2"))) {
                    if (table.getPlayBureau() >= table.getTotalBureau()) {
                        if (table.getState() != SharedConstants.table_state.ready) {
                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_44));
                            return;
                        }
                        int count = table.getPlayerMap().size();
                        for (Player player1 : table.getPlayerMap().values()) {
                            if (player1.getState() == player_state.ready) {
                                count--;
                            }
                        }
                        if (count <= 0) {
                            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_44));
                            return;
                        }
                    }
                    boolean isObserverJoin = false;
                    if (!table.isCanJoin(player)) {
                        if (player.getState() == player_state.ready && table.getPlayerMap().containsKey(player.getUserId())) {

                            // 检查信用分
                            if (!table.checkCanStartNext()) {
                                return;
                            }

                            //给房间里的所有玩家传回玩家的准备状态
                            ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_state, player.getSeat(), SharedConstants.state_player_ready);
                            GeneratedMessage msg2 = com.build();
                            for (Player tableplayer : table.getSeatMap().values()) {
                                tableplayer.writeSocket(msg2);
                            }
                            for (Player player0 : table.getRoomPlayerMap().values()) {
                                player0.writeSocket(msg2);
                            }

                            table.ready();
                            table.checkDeal();
                            table.startNext();
                        }
                        return;
                    }

                    int rest = table.getMaxPlayerCount() - table.getSeatMap().size();
                    if (rest > 0) {
                        for (Player player0 : table.getRoomPlayerMap().values()) {
                            if ("0".equals(player0.getMyExtend().getPlayerStateMap().get("2"))) {
                                rest--;
                            }
                        }
                    }

                    if (rest > 0) {
                        if ("1".equals(table.getRoomModeMap().get("2"))) {
                            if (player.getSeat() <= 0) {
                                int seat = table.randomSeat();
                                if (seat > 0) {
                                    readyResult = true;
                                    LogUtil.msgLog.info("observer ready table:tableId=" + table.getId() + ",master=" + table.getMasterId() + ",userId=" + player.getUserId() + ",seat=" + seat);
                                    player.setSeat(seat);
                                    player.getMyExtend().getPlayerStateMap().put("2", "0");
                                    player.getMyExtend().getPlayerStateMap().remove("1");
                                    player.setIsEntryTable(SharedConstants.table_online);
                                    player.getMyExtend().getPlayerStateMap().put("seat", String.valueOf(seat));
                                    player.changeExtend();
                                    ComRes.Builder comRes = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_code_observer_state);
                                    player.writeSocket(comRes.build());
                                    isObserverJoin = true;
                                } else {
                                    player.writeErrMsg(LangHelp.getMsg(LangMsg.code_5, table.getId()));
                                    return;
                                }
                            } else {
//                                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_4, table.getId()));
//                                return;
                            }
                        }
                    } else {
                        player.writeErrMsg(LangHelp.getMsg(LangMsg.code_5, table.getId()));
                        return;
                    }

                    if (!isObserverJoin || table.isCanReady()) {
                        // 加入牌桌
                        if (!table.joinPlayer(player)) {
                            player.getMyExtend().getPlayerStateMap().remove("seat");
                            player.getMyExtend().getPlayerStateMap().remove("2");
                            player.changeExtend();
                            LogUtil.e("ready command join player fail-->userId:" + player.getUserId() + ",tableId:" + table.getId());
                            return;
                        } else {
                            readyResult = true;
                            player.getMyExtend().getPlayerStateMap().remove("2");
                            player.changeExtend();
                        }
                    }
                }

                if (player.getSeat() <= 0) {
                    return;
                }

                if (readyResult) {
                    TableRes.JoinTableRes.Builder joinRes = TableRes.JoinTableRes.newBuilder();
                    joinRes.setPlayer(player.buildPlayInTableInfo());
                    //玩法
                    joinRes.setWanfa(table.getPlayType());
                    GeneratedMessage msg1 = joinRes.build();
                    for (Player tablePlayer : table.getSeatMap().values()) {
                        //如果是要加入的人，则推送创建房间消息,如果是其他人，推送加入房间消息
                        if (tablePlayer.getUserId() != currentUserId) {
                            tablePlayer.writeSocket(msg1);
                        }
                    }

                    for (Player player0 : table.getRoomPlayerMap().values()) {
                        if (player0.getUserId() != currentUserId) {
                            player0.writeSocket(msg1);
                        }
                    }
                    player.writeSocket(table.buildCreateTableRes(player.getUserId()));
                }

                if (readyResult || table.getPlayerMap().containsKey(player.getUserId())) {

                    // 检查信用分
                    if (!table.checkCanStartNext()) {
                        return;
                    }

                    readyResult = true;
                    //给房间里的所有玩家传回玩家的准备状态
                    ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_state, player.getSeat(), SharedConstants.state_player_ready);
                    GeneratedMessage msg2 = com.build();
                    for (Player tableplayer : table.getSeatMap().values()) {
                        tableplayer.writeSocket(msg2);
                    }
                    for (Player player0 : table.getRoomPlayerMap().values()) {
                        if ((GameUtil.isPlayTenthirty(table.getPlayType()) || GameUtil.isPlayThreeMonkeys(table.getPlayType())) && player0.getUserId() != currentUserId) {
                            player0.writeSocket(table.buildCreateTableRes(player0.getUserId()));
                        }
                        player0.writeSocket(msg2);
                    }
                    if (table.isTest()) {
                        for (Player tableplayer : table.getSeatMap().values()) {
                            if (tableplayer.isRobot())
                                table.ready(tableplayer);
                        }
                    }
                    // 检查所有人是否都准备完毕,如果准备完毕,改变牌桌状态并开始发牌
                    if (table.getState() != SharedConstants.table_state.play) {
                        table.ready();
                        table.checkDeal();
                        //检查起手牌是否需要自动操作
                        table.startNext();
                    }
                }
            }

        } catch (Throwable t) {
            LogUtil.errorLog.error("Throwable:" + t.getMessage(), t);
        } finally {
            if (readyResult) {
                for (Player player1 : table.getPlayerMap().values()) {
                    if (player1.getIsOnline() == 0) {
                        table.broadIsOnlineMsg(player1, SharedConstants.table_offline);
                    }
                }
            } else {
                player.changeState(player_state.entry);
            }
        }
    }

    @Override
    public void setMsgTypeMap() {

    }

}
