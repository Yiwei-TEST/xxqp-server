package com.sy599.game.qipai.dehmj.tool;

import com.sy599.game.character.Player;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayMajiangRes;
import com.sy599.game.qipai.dehmj.rule.DehMj;
import com.sy599.game.qipai.dehmj.rule.DehMjHelper;

import java.util.ArrayList;
import java.util.List;

public class DehMjResTool {
	public static void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<DehMj> majiangs) {
		List<Integer> list = DehMjHelper.toMajiangIds(majiangs);
		builder.addAllMajiangIds(list);
		builder.setAction(action);
		builder.setUserId(player.getUserId() + "");
		builder.setSeat(player.getSeat());
	}

	/**
	 * 小胡
	 * 
	 * @param player
	 * @param xiaohu
	 * @return
	 */
	public static PlayMajiangRes.Builder buildActionRes(Player player, List<Integer> xiaohu) {
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		builder.setAction(0);
		builder.setUserId(player.getUserId() + "");
		builder.setSeat(player.getSeat());
		builder.addAllMajiangIds(new ArrayList<Integer>());
		builder.addAllXiaohu(xiaohu);
		return builder;
	}

	/**
	 * 出牌
	 * 
	 * @param player
	 * @param majiang
	 * @return
	 */
	public static PlayMajiangRes.Builder buildDisMajiangRes(Player player, DehMj majiang) {
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		builder.setAction(0);
		builder.setUserId(player.getUserId() + "");
		builder.setSeat(player.getSeat());
		builder.addMajiangIds(majiang.getId());
		return builder;
	}

	public static PlayMajiangRes.Builder buildPlayRes(Player player, int action, List<DehMj> majiangs) {
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		List<Integer> list = DehMjHelper.toMajiangIds(majiangs);
		builder.addAllMajiangIds(list);
		builder.setAction(action);
		builder.setUserId(player.getUserId() + "");
		builder.setSeat(player.getSeat());
		return builder;
	}
}
