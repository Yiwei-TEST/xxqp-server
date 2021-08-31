package com.sy599.game.qipai.zhz.tool;

import com.sy599.game.character.Player;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayPaohuziRes;
import com.sy599.game.qipai.zhz.constant.PaohzCard;


import java.util.List;

public class PaohuziResTool {
	public static void buildPlayRes(PlayPaohuziRes.Builder builder, Player player, int action, List<PaohzCard> majiangs) {
		List<Integer> list = PaohuziTool.toPhzCardIds(majiangs);
		builder.addAllPhzIds(list);
		builder.setAction(action);
		builder.setUserId(player.getUserId() + "");
		builder.setSeat(player.getSeat());
	}

}
