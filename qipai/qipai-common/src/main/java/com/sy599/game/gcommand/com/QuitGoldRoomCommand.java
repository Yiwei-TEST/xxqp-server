package com.sy599.game.gcommand.com;

import com.google.protobuf.GeneratedMessage;
import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.db.bean.gold.GoldRoom;
import com.sy599.game.db.bean.gold.GoldRoomUser;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.*;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class QuitGoldRoomCommand extends BaseCommand {
    @Override
    public void execute(Player player, MessageUnit message) throws Exception {

        if (GoldRoomUtil.isGoldRoom(player.getPlayingTableId())) {
            synchronized (Constants.GOLD_LOCK) {
                GoldRoomUser goldRoomUser = GoldRoomDao.getInstance().loadGoldRoomUser(player.getPlayingTableId(), player.getUserId());
                if (goldRoomUser != null) {
                    GoldRoom goldRoom = GoldRoomDao.getInstance().loadGoldRoom(goldRoomUser.getRoomId());
                    if (goldRoom != null) {
                        BaseTable baseTable = TableManager.getInstance().getTable(player.getPlayingTableId());
                        if (baseTable != null && baseTable.isAutoKickMinGoldRoom()) {
                            if (baseTable.canQuit(player)) {
                                boolean quit = baseTable.quitPlayer(player);
                                if (quit) {
                                    baseTable.onPlayerQuitSuccess(player);
                                    String currentState = baseTable.getPlayerMap().size() > 1 ? "1" : "2";
                                    GoldRoomDao.getInstance().deleteGoldRoomUser(baseTable.getId(), player.getUserId());
                                    GoldRoomDao.getInstance().updateGoldRoom(baseTable.getId(), -1, currentState);
                                    player.setPlayingTableId(0);
                                    player.saveBaseInfo();
                                }
                            } else {
                                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_10));
                                return;
                            }
                        } else if (baseTable == null) {//匹配阶段退出
                            GoldRoomDao.getInstance().deleteGoldRoomUser(goldRoom.getKeyId(), player.getUserId());
                            GoldRoomDao.getInstance().updateGoldRoom(goldRoom.getKeyId(), -1, "0");
                            player.setPlayingTableId(0);
                            player.saveBaseInfo();
                        }
                    } else {
                        player.setPlayingTableId(0);
                        player.saveBaseInfo();
                    }
                } else {
                    player.setPlayingTableId(0);
                    player.saveBaseInfo();
                }
                ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tablequit, String.valueOf(player.getUserId()),String.valueOf(BaseTable.TABLE_TYPE_GOLD));
                player.writeSocket(com.build());
            }
            return;
        }
    }

    @Override
    public void setMsgTypeMap() {

    }

}
