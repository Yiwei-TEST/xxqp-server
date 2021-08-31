package com.sy599.game.qipai.hylhq.command.play;

import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.qipai.hylhq.been.HylhqPlayer;
import com.sy599.game.qipai.hylhq.been.HylhqTable;
import com.sy599.game.qipai.hylhq.been.PaohzDisAction;
import com.sy599.game.qipai.hylhq.constant.PaohzCard;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayCommand extends BaseCommand<HylhqPlayer> {
	@Override
	public void execute(HylhqPlayer player, MessageUnit message) throws Exception {
		HylhqTable table = player.getPlayingTable(HylhqTable.class);
		if (table == null) return;

		// 该牌局是否能打牌
		int canPlay = table.isCanPlay();
		if (canPlay != 0) {
			if (canPlay == 1) {
				player.writeErrMsg(LangHelp.getMsg(LangMsg.code_6));
				return;
			}
		}

		// 检查桌子的状态
		if (table.getState() != table_state.play) {
			return;
		}
		PlayCardReq playCard = (PlayCardReq) recognize(PlayCardReq.class, message);
		int action = playCard.getCardType();
		// 牌参数
		List<Integer> cards = playCard.getCardIdsList();

		if (action == PaohzDisAction.action_pass) {
			if (!cards.isEmpty()) {
				// 摸的牌
				cards = new ArrayList<>(cards);
			}

			// 获得所有的操作集合
			Map<Integer, List<Integer>> actionMap = table.getActionSeatMap();
			if (actionMap != null && actionMap.containsKey(player.getSeat())) {
				List<Integer> list = actionMap.get(player.getSeat());
				// /////////////////////////////////////////////////////////
				// 检查过
				if (!cards.isEmpty()) {
					// 得到第一优先顺序的操作
					int nowId = cards.remove((int) 0);
					List<PaohzCard> nowDisCards = table.getNowDisCardIds();
					if (nowDisCards != null && !nowDisCards.isEmpty()) {
						if (nowDisCards.get(0).getId() != nowId && nowDisCards.get(0).getId() > 0) {
							return;
						}
					}

					// 比较动作
					if (!DataMapUtil.compareVal(list, cards)) {
						return;
					}

				}
				if (list != null && !list.isEmpty() && list.get(0) == 1) {
					LogUtil.monitor_i("------pass日志过掉可以胡的牌跑胡子:tableId:" + table.getId() + " playType:" + table.getPlayType() + " playBureau:" + table.getPlayBureau() + " userId:" + player.getUserId()
							+ " cards:" + player.getHandPais() + " action:" + list);

				}
			}
			if (!cards.isEmpty()) {
				// 是否已经摸牌
				cards.clear();
			}
		}
		if(action != PaohzDisAction.action_mo){
			player.setAutoPlay(false,table);
			player.setLastOperateTime(System.currentTimeMillis());
		}
		table.play(player, cards, action);

	}

	@Override
	public void setMsgTypeMap() {

	}

}
