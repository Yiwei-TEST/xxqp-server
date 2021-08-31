package com.sy599.game.message;

import com.sy599.game.character.Player;
import com.sy599.game.db.dao.NoticeDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.msg.serverPacket.MessageResMsg;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.Date;

public class MessageCommand extends BaseCommand {

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {

		ComReq req = (ComReq) this.recognize(ComReq.class, message);
		int command = req.getParams(0);

		if (command == 1) {
			//如果指令为1，推送消息到前台
			MessageResMsg.NoticelistRes res = player.getMyMessage().buildMessageListRes();
			if (res != null) {
				player.writeSocket(res);
			}
			Date date = NoticeDao.getInstance().selectNewNoticeTime();
			if (date != null && date.getTime() > player.getMyExtend().getNoticeTime()) {
				player.getMyExtend().setNoticeTime(date.getTime());

			}

		} else if (command == 2) {

		}

	}

	@Override
	public void setMsgTypeMap() {

	}

}
