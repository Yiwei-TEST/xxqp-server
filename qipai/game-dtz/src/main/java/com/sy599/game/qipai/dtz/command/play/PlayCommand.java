package com.sy599.game.qipai.dtz.command.play;

import com.sy599.game.character.Player;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayCardRes;
import com.sy599.game.msg.serverPacket.TableRes.ClosingInfoRes;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.dtz.bean.DtzPlayer;
import com.sy599.game.qipai.dtz.bean.DtzTable;
import com.sy599.game.qipai.dtz.rule.CardTypeDtz;
import com.sy599.game.qipai.dtz.tool.CardTypeToolDtz;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.ArrayList;
import java.util.List;

/**
 * 玩家出牌
 * @author zhouhj
 *
 */
public class PlayCommand extends BaseCommand {
	@Override
	public void execute(Player player0, MessageUnit message) throws Exception {
		//检查对象是否存在
		DtzPlayer player=(DtzPlayer)player0;
		if(player==null)return;
		DtzTable table = player.getPlayingTable(DtzTable.class);
		if(table==null)return;
		PlayCardReq playCard = (PlayCardReq)recognize(PlayCardReq.class, message);
		if(playCard==null)return;
		//检查出的牌
		List<Integer> cards = new ArrayList<>(playCard.getCardIdsList());
		if(cards.size() == 0)return;
		CardTypeDtz cardType = CardTypeToolDtz.toPokerType(cards, table);
		if(cardType.getType() == 0){
			return;
		}
		if(player.getAutoPlay() == 1){
			player.setAutoPlay(0);
			ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.RES_DTZ_AUTOPLAY, player.getUserId(), player.getSeat(), player.getAutoPlay());
			table.broadMsg(build.build());
		}
		table.playCommand(player, cards, cardType);
        player.setAutoPlayCheckedTimeAdded(false);
	}

	@Override
	public void setMsgTypeMap() {
		msgTypeMap.put(PlayCardRes.class, WebSocketMsgType.sc_playcardres);
		msgTypeMap.put(ClosingInfoRes.class, WebSocketMsgType.sc_closinginfores);
	}

}
