package com.sy599.game.qipai.penghuzi.tool;

import com.sy599.game.character.Player;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayPaohuziRes;
import com.sy599.game.qipai.penghuzi.constant.PenghzCard;

import java.util.List;

public class PenghuziResTool {
	public static void buildPlayRes(PlayPaohuziRes.Builder builder, Player player, int action, List<PenghzCard> majiangs) {
		if(player==null){
			return;
		}
		List<Integer> list = PenghuziTool.toPhzCardIds(majiangs);
		builder.addAllPhzIds(list);
		builder.setAction(action);
		builder.setUserId(player.getUserId() + "");
		builder.setSeat(player.getSeat());
	}

}
