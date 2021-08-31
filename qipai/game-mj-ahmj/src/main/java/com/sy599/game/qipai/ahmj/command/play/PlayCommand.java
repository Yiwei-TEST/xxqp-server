package com.sy599.game.qipai.ahmj.command.play;

import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.qipai.ahmj.bean.AhmjPlayer;
import com.sy599.game.qipai.ahmj.bean.AhmjTable;
import com.sy599.game.qipai.ahmj.bean.MjDisAction;
import com.sy599.game.qipai.ahmj.constant.Ahmj;
import com.sy599.game.qipai.ahmj.tool.MajiangHelper;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayCommand  extends BaseCommand<AhmjPlayer> {
	@Override
	public void execute(AhmjPlayer player, MessageUnit message) throws Exception {
		AhmjTable table = player.getPlayingTable(AhmjTable.class);
		if (table == null) {
			return;
		}

		// 该牌局是否能打牌
		int canPlay = table.isCanPlay();
		if (canPlay != 0) {
			if (canPlay == 1) {
				player.writeErrMsg(LangHelp.getMsg(LangMsg.code_6));
			} else if (canPlay == 2) {
				player.writeErrMsg(LangHelp.getMsg(LangMsg.code_7));
			}
			return;
		}

		if (table.getState() != SharedConstants.table_state.play) {
			return;
		}
		PlayCardReq playCard = (PlayCardReq) recognize(PlayCardReq.class, message);
		int action = playCard.getCardType();
		List<Integer> majiangIds = playCard.getCardIdsList();

		if (action == MjDisAction.action_pass) {
			Map<Integer, List<Integer>> actionMap = table.getActionSeatMap();
			if (actionMap != null && actionMap.containsKey(player.getSeat())) {
				List<Integer> list = actionMap.get(player.getSeat());
				// /////////////////////////////////////////////////////////
				// 检查过
				if (!majiangIds.isEmpty()) {
					// 是否已经摸牌
					majiangIds = new ArrayList<>(majiangIds);
					int playIsMo = majiangIds.remove((int) 0);
					int isAlreadyMo = player.isAlreadyMoMajiang() ? 1 : 0;
					if (playIsMo != isAlreadyMo) {
						player.writeErrMsg("过---手牌数量错误!!");
						return;
					}

					// 比较动作
					if (!DataMapUtil.compareVal(list, majiangIds)) {
						player.writeErrMsg("过---动作错误!!");
						return;
					}

					majiangIds.clear();

				}

				if (list != null && !list.isEmpty() && list.get(0) == 1) {
					LogUtil.monitor_i("------pass日志过掉可以胡的牌麻将:tableId:" + table.getId() + " playType:" + table.getPlayType() + " playBureau:" + table.getPlayBureau() + " userId:" + player.getUserId()
							+ " cards:" + player.getHandPais() + " action:" + list);

				}
			}
		}
		if (majiangIds != null && majiangIds.contains(0)) {
			LogUtil.monitor_i("麻将出牌-->" + table.getId() + " playType:" + table.getPlayType() + " playBureau:" + table.getPlayBureau() + " userId:" + player.getUserId() + " cards:" + majiangIds
					+ " action:" + action);
		}
		List<Ahmj> majiangs = MajiangHelper.toMajiang(majiangIds);
		table.disMajiang(player, majiangs, action);

	}

	@Override
	public void setMsgTypeMap() {

	}

}
