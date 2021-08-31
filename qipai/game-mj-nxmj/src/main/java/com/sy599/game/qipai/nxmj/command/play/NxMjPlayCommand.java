package com.sy599.game.qipai.nxmj.command.play;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.qipai.nxmj.bean.NxMjDisAction;
import com.sy599.game.qipai.nxmj.bean.NxMjPlayer;
import com.sy599.game.qipai.nxmj.bean.NxMjTable;
import com.sy599.game.qipai.nxmj.rule.NxMj;
import com.sy599.game.qipai.nxmj.rule.NxMjHelper;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class NxMjPlayCommand extends BaseCommand {


    @Override
	public void setMsgTypeMap() {

	}

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		NxMjTable table = player.getPlayingTable(NxMjTable.class);
		if (table == null  || !(player instanceof NxMjPlayer)) {
			return;
		}
		NxMjPlayer csplayer = (NxMjPlayer) player;
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

		if (action == NxMjDisAction.action_pass) {
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
					if (!DataMapUtil.compareVal(list, majiangIds)) {
						return;
					}

					majiangIds.clear();

				}
			}
		}
		if(action == NxMjDisAction.action_xiaohu) {
			int xiaoHuType = majiangIds.remove((int) 0);
			List<NxMj> majiangs = new ArrayList<>(csplayer.showXiaoHuMajiangs(xiaoHuType,false));
			//List<CsMj> majiangs = new ArrayList<>(csplayer.getHandMajiang());
					//CsMjHelper.toMajiang(majiangIds);
			table.huXiaoHu(csplayer, majiangs, xiaoHuType, action);
		}if(action == NxMjDisAction.action_baoting) {
			table.baoting(csplayer, null, 0, action);
		} else {
			List<NxMj> majiangs = NxMjHelper.toMajiang(majiangIds);
			majiangs.addAll(NxMjHelper.toMajiang(playCard.getExtCardIdsList()));
			table.playCommand(csplayer, majiangs, action);
		}
		NxMjPlayer player0 = (NxMjPlayer) player;
		player0.setAutoPlay(false,false);
		player0.setCheckAutoPlay(false);
	}

}
