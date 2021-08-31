package com.sy599.game.qipai.hbgzp.tool;

import java.util.List;

import com.sy599.game.character.Player;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayMajiangRes;
import com.sy599.game.qipai.hbgzp.bean.HbgzpPlayer;
import com.sy599.game.qipai.hbgzp.rule.Hbgzp;

public class HbgzpResTool {
	public static void buildPlayRes(PlayMajiangRes.Builder builder, HbgzpPlayer player, int action, List<Hbgzp> majiangs) {
		List<Integer> list = HbgzpHelper.toMajiangIds(majiangs);
		builder.addAllMajiangIds(list);
		builder.setAction(action);
		builder.setUserId(player.getUserId() + "");
		builder.setSeat(player.getSeat());
		builder.addXiaohu(player.getOutCardHuxi());
	}


	/**
	 * 出牌
	 * 
	 * @param player
	 * @param majiang
	 * @return
	 */
	public static PlayMajiangRes.Builder buildDisMajiangRes(Player player, Hbgzp majiang) {
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		builder.setAction(0);
		builder.setUserId(player.getUserId() + "");
		builder.setSeat(player.getSeat());
		builder.addMajiangIds(majiang.getId());
		return builder;
	}
}
