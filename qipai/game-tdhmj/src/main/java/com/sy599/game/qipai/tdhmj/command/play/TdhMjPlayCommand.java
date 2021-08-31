package com.sy599.game.qipai.tdhmj.command.play;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.PlayCardReqMsg.PlayCardReq;
import com.sy599.game.qipai.tdhmj.bean.TdhMjDisAction;
import com.sy599.game.qipai.tdhmj.bean.TdhMjPlayer;
import com.sy599.game.qipai.tdhmj.bean.TdhMjTable;
import com.sy599.game.qipai.tdhmj.rule.TdhMj;
import com.sy599.game.qipai.tdhmj.rule.TdhMjHelper;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class TdhMjPlayCommand extends BaseCommand {


    @Override
	public void setMsgTypeMap() {

	}

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		TdhMjTable table = player.getPlayingTable(TdhMjTable.class);
		if (table == null  || !(player instanceof TdhMjPlayer)) {
			return;
		}
		TdhMjPlayer csplayer = (TdhMjPlayer) player;
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

		if (action == TdhMjDisAction.action_pass) {
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
			List<TdhMj> majiangs = TdhMjHelper.toMajiang(majiangIds);
			majiangs.addAll(TdhMjHelper.toMajiang(playCard.getExtCardIdsList()));
			table.playCommand(csplayer, majiangs, action);
		TdhMjPlayer player0 = (TdhMjPlayer) player;
		player0.setAutoPlay(false,false);
		player0.setCheckAutoPlay(false);
		if(player0.getAutoMD()==1){
			player0.setAutoMD(0);
			ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.req_code_TDH_AUTO_MD, player.getSeat(), player0.getAutoMD());
			player.writeSocket(com.build());
			LogUtil.msg("TdhMjPlayCommand|setAutoMD|" +table.getId()+"|"+player.getSeat()+"|"+player.getName()+"|"+player0.getAutoMD());
		}
	}

}
