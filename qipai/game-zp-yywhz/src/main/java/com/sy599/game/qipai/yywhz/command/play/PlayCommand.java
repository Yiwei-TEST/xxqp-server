package com.sy599.game.qipai.yywhz.command.play;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.qipai.yywhz.bean.WaihuziPlayer;
import com.sy599.game.qipai.yywhz.bean.WaihuziTable;
import com.sy599.game.qipai.yywhz.bean.WaihzDisAction;
import com.sy599.game.qipai.yywhz.constant.GuihzCard;
import com.sy599.game.qipai.yywhz.tool.GuihuziTool;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayCommand extends BaseCommand {
	@Override
	public void execute(Player player0, MessageUnit message) throws Exception {
		WaihuziPlayer player = (WaihuziPlayer) player0;
		WaihuziTable table = player.getPlayingTable(WaihuziTable.class);
		if (table == null)
			return;
		int canPlay = table.isCanPlay();
		if (canPlay != 0) {// 该牌局是否能打牌
			if (canPlay == 1) {
				player.writeErrMsg(LangHelp.getMsg(LangMsg.code_6));
				return;
			}
		}
		if (table.getState() != table_state.play) {// 检查桌子的状态
			return;
		}
		PlayCardReq playCard = (PlayCardReq) recognize(PlayCardReq.class, message);
		int action = playCard.getCardType();
		List<Integer> cards = playCard.getCardIdsList();// 牌参数
		if (action == WaihzDisAction.action_pass) {// 客户端过的操作
			List<Integer> passCards = new ArrayList<>();
			if (!cards.isEmpty()) {// 摸的牌
				cards = new ArrayList<>(cards);
			}
			// 获得所有的操作集合
			Map<Integer, List<Integer>> actionMap = table.getActionSeatMap();
			if (actionMap != null && actionMap.containsKey(player.getSeat())) {
				List<Integer> list = actionMap.get(player.getSeat());
				if (!cards.isEmpty()&& cards.get(4)!=1) {
					// 得到第一优先顺序的操作
					int nowId = cards.remove((int) 0);
					if(nowId > 0)
						passCards.add(nowId);
					List<GuihzCard> nowDisCards = table.getNowDisCardIds();
					if (list != null && !list.isEmpty() && list.get(4) != 1) {// 客户端执行过的操作日志  会记录所有除去吃过的点击操作
						LogUtil.monitor_i(player.getName() + "执行过的操作日志-----牌桌ID:" + table.getId() + " 过掉的牌:" + GuihuziTool.toGhzCards(passCards) + " 过掉的操作：" + WaihzDisAction.getDisActionName(list));
					}
					if (nowDisCards != null && !nowDisCards.isEmpty() && list.get(0) != 1) {
						if (nowDisCards.get(0).getId() != nowId && nowDisCards.get(0).getId() > 0) {
							return;
						}
					}
					if (!DataMapUtil.compareVal(list, cards)) {// 比较动作
						return;
					}
				} else {
					if (list != null && !list.isEmpty() && list.get(5) != 1) {// 客户端执行过的操作日志  会记录所有除去吃过的点击操作
						LogUtil.monitor_i(player.getName() + "执行过的操作日志-----牌桌ID:" + table.getId() + " 过掉的牌:" + passCards + " 过掉的操作：" + WaihzDisAction.getDisActionName(list));
					}
				}
			} else {
				LogUtil.monitor_i(player.getName() + "执行过的操作时 当前已不存在可做操作-----牌桌ID:" + table.getId() + " 过掉的牌:" + cards);
			}
			if (!cards.isEmpty()) {// 是否已经摸牌
				cards.clear();
			}
			table.play(player, passCards, action);
			return;
		}
		
		if(action != 9){
			player.setAutoPlay(false,table);
			player.setLastOperateTime(System.currentTimeMillis());
		}
		
		if (action == WaihzDisAction.action_liu)
			LogUtil.msgLog.info(player.getName() + "执行溜的操作，溜的牌：" + cards);
		table.play(player, cards, action);
	}

	@Override
	public void setMsgTypeMap() {
	}
}
