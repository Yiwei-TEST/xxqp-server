package com.sy599.game.qipai.cdtlj.command.play;

import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayCardRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingInfoRes;
import com.sy599.game.qipai.cdtlj.bean.CdtljPlayer;
import com.sy599.game.qipai.cdtlj.bean.CdtljTable;
import com.sy599.game.qipai.cdtlj.constant.CdtljConstants;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.List;

public class PlayCommand extends BaseCommand<CdtljPlayer> {
    @Override
    public void execute(CdtljPlayer player, MessageUnit message) throws Exception {
        CdtljTable table = player.getPlayingTable(CdtljTable.class);
        if (table == null) {
            return;
        }
//        if (table.getState() != table_state.play) {
//            return;
//        }

        if (player.getSeat() != table.getNowDisCardSeat()) {
            // 没轮到出牌
            return;
        }

        PlayCardReq playCard = (PlayCardReq) recognize(PlayCardReq.class, message);

        List<Integer> cards = new ArrayList<>(playCard.getCardIdsList());

        List<Integer> disCardIds = table.getNowDisCardIds();
        int action =0;
        if ( playCard.getCardType() == CdtljConstants.REQ_MAIPAI) {//
        	action = CdtljConstants.REQ_MAIPAI;
        }

        if (!player.isAutoPlay()) {
            player.setAutoPlay(false, table);
            player.setLastOperateTime(System.currentTimeMillis());
        }

        table.playCommand(player,action, cards);
    }

    @Override
    public void setMsgTypeMap() {
        msgTypeMap.put(PlayCardRes.class, WebSocketMsgType.sc_playcardres);
        msgTypeMap.put(ClosingInfoRes.class, WebSocketMsgType.sc_closinginfores);
    }

}
