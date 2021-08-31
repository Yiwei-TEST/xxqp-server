package com.sy599.game.qipai.sandh.command.play;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayCardRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingInfoRes;
import com.sy599.game.qipai.sandh.bean.SandhPlayer;
import com.sy599.game.qipai.sandh.bean.SandhTable;
import com.sy599.game.qipai.sandh.constant.SandhConstants;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class PlayCommand extends BaseCommand<SandhPlayer> {
    @Override
    public void execute(SandhPlayer player, MessageUnit message) throws Exception {
        SandhTable table = player.getPlayingTable(SandhTable.class);
        if (table == null) {
            return;
        }
        if (table.getState() != table_state.play) {
            return;
        }

        if (player.getSeat() != table.getNowDisCardSeat()) {
            // 没轮到出牌
            return;
        }

        PlayCardReq playCard = (PlayCardReq) recognize(PlayCardReq.class, message);

        List<Integer> cards = new ArrayList<>(playCard.getCardIdsList());

        List<Integer> disCardIds = table.getNowDisCardIds();
        int action =0;
        if ( playCard.getCardType() == SandhConstants.REQ_MAIPAI) {//
        	action = SandhConstants.REQ_MAIPAI;
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
