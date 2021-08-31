package com.sy599.game.gcommand.play;

import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.List;
import java.util.Map;

public class GmCommand extends BaseCommand {
    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        setNextMoPai(player, req.getParamsList());
    }

    public static void setNextMoPai(Player player, List<Integer> intParamList){
        BaseTable table = player.getPlayingTable();
        if (table == null) {
            return;
        }
        if (!table.isGroupRoom()) {
            return;
        }
        Map<Long, Player> seatPlayers = table.getPlayerMap();
        if(!seatPlayers.containsKey(player.getUserId())) {
            return;
        }
        int paiId = intParamList.get(0);

        if(table.setZpMap(player.getUserId(), paiId)) {
            LogUtil.msg(table.getId() + "房间,玩家" + player.getUserId() + "摸牌做牌：" + paiId);
        }
    }
}
