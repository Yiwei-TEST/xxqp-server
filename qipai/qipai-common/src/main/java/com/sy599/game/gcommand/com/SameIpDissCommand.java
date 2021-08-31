package com.sy599.game.gcommand.com;

import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SameIpDissCommand extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		
		String ip = player.getIp();
		if (StringUtils.isBlank(ip)) {
			player.writeErrMsg(LangHelp.getMsg(LangMsg.code_232));
			return;
		}
		
		BaseTable table = player.getPlayingTable();	
		if (table == null) {
			player.writeErrMsg(LangHelp.getMsg(LangMsg.code_233));
			return;
		}
		
		Map<Long, Player> players = table.getPlayerMap();
		if (players.size() < 2) {
			player.writeErrMsg(LangHelp.getMsg(LangMsg.code_234));
			return;
		}
		
		boolean flag = false;
		Set<String> ipSet = new HashSet<>();
		for (Player temp : players.values()) {
			if (ipSet.contains(temp.getIp())) {
				flag = true;
				break;
			} else {
				ipSet.add(temp.getIp());
			}
		}
		
		if (flag) {
			ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_disstable,String.valueOf(table.calcTableType()), table.getPlayType());
			for (Player temp : players.values()) {
				if (temp != null) {
					temp.writeErrMsg(LangHelp.getMsg(LangMsg.code_235));
					temp.writeSocket(com.build());
				}
			}
            LogUtil.msgLog.info("BaseTable|dissReason|SameIpDissCommand|1|" + table.getId() + "|" + table.getPlayBureau() + "|" + player.getUserId());
			table.diss();
		} else {
			player.writeErrMsg(LangHelp.getMsg(LangMsg.code_236));
		}
	}

	@Override
	public void setMsgTypeMap() {
		
	}

}
