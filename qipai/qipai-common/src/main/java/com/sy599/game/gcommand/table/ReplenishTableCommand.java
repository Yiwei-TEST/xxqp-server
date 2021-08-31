package com.sy599.game.gcommand.table;

import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.List;


public class ReplenishTableCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        if (player.getMyExtend().isGroupMatch()){
            player.writeErrMsg(LangMsg.code_257);
            return;
        }

        //判断玩家是否在打比赛场
        if (player.isMatching() || player.isPlayingMatch()) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_221));
            return;
        }

        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        List<Integer> intParamList=req.getParamsList();
        BaseTable table = TableManager.getInstance().getTable(player.getPlayingTableId());
        if(table!=null){
            table.setReplenishParams(player,intParamList,req.getStrParamsList());
            player.writeComMessage(WebSocketMsgType.req_code_daniao_return);
            LogUtil.msgLog.info("ReplenishTableCommand|" + table.getId() + "|" + player.getUserId() + "|" + intParamList);
        }
//        Map<Integer, Player> seatMap = table.getSeatMap();
//        for (Map.Entry<Integer,Player> entry:seatMap.entrySet()){
//            //传code+座位号+已打鸟
//            entry.getValue().writeComMessage(WebSocketMsgType.req_code_daniao_seat,entry.getKey(),1);
//        }
    }
}
