package com.sy599.game.qipai.dehmj.command.play;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.qipai.dehmj.bean.DehMjDisAction;
import com.sy599.game.qipai.dehmj.bean.DehMjPlayer;
import com.sy599.game.qipai.dehmj.bean.DehMjTable;
import com.sy599.game.qipai.dehmj.rule.DehMj;
import com.sy599.game.qipai.dehmj.rule.DehMjHelper;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DehMjPlayCommand extends BaseCommand {


    @Override
	public void setMsgTypeMap() {

	}

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		DehMjTable table = player.getPlayingTable(DehMjTable.class);
		if (table == null  || !(player instanceof DehMjPlayer)) {
			return;
		}
		DehMjPlayer csplayer = (DehMjPlayer) player;
		// 该牌局是否能打牌
		int canPlay = table.isCanPlay();
		if (canPlay != 0) {
			if (canPlay == 1) {
				csplayer.writeErrMsg(LangHelp.getMsg(LangMsg.code_6));
			} else if (canPlay == 2) {
				csplayer.writeErrMsg(LangHelp.getMsg(LangMsg.code_7));
			}
			return;
		}

		if (table.getState() != table_state.play) {
			return;
		}
		PlayCardReq playCard = (PlayCardReq) recognize(PlayCardReq.class, message);
		int action = playCard.getCardType();
		List<Integer> cardIdsList = playCard.getCardIdsList();
		//playCard.getCardIdsList()返回的是unmodifiableList
		List<Integer> majiangIds = new ArrayList<>();
		if(cardIdsList != null){
			majiangIds = new ArrayList<>(cardIdsList);
		}

		if (action == DehMjDisAction.action_pass) {
			Map<Integer, List<Integer>> actionMap = table.getActionSeatMap();
			if (actionMap != null && actionMap.containsKey(csplayer.getSeat())) {
				List<Integer> list = actionMap.get(csplayer.getSeat());
				// /////////////////////////////////////////////////////////
				// 检查过
				if (!majiangIds.isEmpty()) {
					// 是否已经摸牌
					majiangIds = new ArrayList<>(majiangIds);
					int playIsMo = majiangIds.remove((int) 0);
					int isAlreadyMo = csplayer.isAlreadyMoMajiang() ? 1 : 0;
					if (playIsMo != isAlreadyMo) {
						return;
					}

					// 比较动作
//					if (!DataMapUtil.compareVal(list, majiangIds)) {
//						return;
//					}

					majiangIds.clear();

				}
			}
		}
		
			List<DehMj> majiangs = DehMjHelper.toMajiang(majiangIds);
			majiangs.addAll(DehMjHelper.toMajiang(playCard.getExtCardIdsList()));
			table.playCommand(csplayer, majiangs, action);
		csplayer.setAutoPlay(false,false);
	}

}
