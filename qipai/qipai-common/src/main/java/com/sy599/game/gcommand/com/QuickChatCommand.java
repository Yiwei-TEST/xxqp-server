package com.sy599.game.gcommand.com;

import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.util.KeyWordsFilter;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;

/**
 * 快速语音聊天
 * 
 * @author lc
 * 
 */
public class QuickChatCommand extends BaseCommand {
	private final static String PROPERTY_KEY = "QuickChat";
	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		BaseTable table = player.getPlayingTable();
		if (table == null) {
			return;
		}
		ComReq req = (ComReq) this.recognize(ComReq.class, message);
		int chatId = req.getParams(0);
		String chatStr = "";
		if (req.getStrParamsCount() > 0) {
			chatStr = req.getStrParams(0);
		}

		int toSeat = 0;
		if(req.getParamsCount() > 1) {
			long nowTime = System.currentTimeMillis();
			Long preTime = player.getPropertiesCache().get(PROPERTY_KEY);
			if(preTime!=null && nowTime - preTime.longValue() < 3 * SharedConstants.SENCOND_IN_MINILLS) {
				player.writeErrMsg(LangHelp.getMsg(LangMsg.code_45));
				return;
			}
			player.getPropertiesCache().put(PROPERTY_KEY,nowTime);
			toSeat = req.getParams(1);
		}
		
		if (StringUtils.isNotBlank(chatStr))
			chatStr = KeyWordsFilter.getInstance().filt(chatStr);

		ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_chat, chatId, table.getPlayType(), player.getUserId() + "", chatStr, toSeat);
		for (Player seatPlayer : table.getSeatMap().values()) {
			seatPlayer.writeSocket(com.build());
		}
	}

	@Override
	public void setMsgTypeMap() {

	}

}
