package com.sy599.game.gcommand.bairen;

import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.dao.gold.GoldRoomDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

/**
 * 玩家主动退出百人房间
 */
public class QuitBaiRenTableCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        BaseTable baseTable = TableManager.getInstance().getTable(player.getPlayingTableId());
        if (baseTable != null) {
            if (baseTable.canQuit(player)) {// 下注的情况下 不能退出
                boolean quit = baseTable.quitPlayer(player);
                if (quit) {
                    baseTable.onPlayerQuitSuccess(player);
                    ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_tablequit, String.valueOf(player.getUserId()) ,String.valueOf(baseTable.calcTableType()));
                    player.writeSocket(com.build());
                }
            } else {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_252));
                return;
            }
        }
    }

    @Override
    public void setMsgTypeMap() {
    }
}

