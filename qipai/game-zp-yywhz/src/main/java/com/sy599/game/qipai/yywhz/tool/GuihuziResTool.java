package com.sy599.game.qipai.yywhz.tool;

import com.sy599.game.character.Player;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayPaohuziRes;
import com.sy599.game.qipai.yywhz.constant.GuihzCard;

import java.util.List;

public class GuihuziResTool {
	public static void buildPlayRes(PlayPaohuziRes.Builder builder, Player player, int action, List<GuihzCard> majiangs) {
		List<Integer> list = GuihuziTool.toGhzCardIds(majiangs);
		builder.addAllPhzIds(list);
		builder.setAction(action);
		builder.setUserId(player.getUserId() + "");
		builder.setSeat(player.getSeat());
	}
}
