package com.sy599.game.qipai.qianfen.command.play;

import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayCardRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingInfoRes;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.qianfen.bean.QianfenPlayer;
import com.sy599.game.qipai.qianfen.bean.QianfenTable;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.ArrayList;
import java.util.List;

public class PlayCommand extends BaseCommand<QianfenPlayer> {

	@Override
	public void execute(QianfenPlayer player, MessageUnit message) throws Exception {
		QianfenTable table = player.getPlayingTable(QianfenTable.class);
		if (table == null) {
			return;
		}

		if (table.getState() != table_state.play) {
			return;
		}

		PlayCardReq playCard = (PlayCardReq) recognize(PlayCardReq.class, message);
		List<Integer> cards = new ArrayList<>(playCard.getCardIdsList());
		if(player.getHandPais0().size()!=0&&cards.size()!=0&&player.getHandPais0().size()!=cards.size()){
			player.setAutoPlay(false, table);
			player.setLastOperateTime(System.currentTimeMillis());
		}
		table.playCommand(player, cards);
	}

	@Override
	public void setMsgTypeMap() {
		msgTypeMap.put(PlayCardRes.class, WebSocketMsgType.sc_playcardres);
		msgTypeMap.put(ClosingInfoRes.class, WebSocketMsgType.sc_closinginfores);
	}

}
