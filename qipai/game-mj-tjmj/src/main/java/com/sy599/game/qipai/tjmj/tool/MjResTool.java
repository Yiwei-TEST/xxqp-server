package com.sy599.game.qipai.tjmj.tool;

import com.sy599.game.character.Player;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayMajiangRes;
import com.sy599.game.qipai.tjmj.rule.Mj;
import com.sy599.game.qipai.tjmj.rule.MjHelper;

import java.util.ArrayList;
import java.util.List;

public class MjResTool {
	public static void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<Mj> majiangs) {
		List<Integer> list = MjHelper.toMajiangIds(majiangs);
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
	public static PlayMajiangRes.Builder buildDisMajiangRes(Player player, Mj majiang) {
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		builder.setAction(0);
		builder.setUserId(player.getUserId() + "");
		builder.setSeat(player.getSeat());
		builder.addMajiangIds(majiang.getId());
		return builder;
	}

	public static PlayMajiangRes.Builder buildPlayRes(Player player, int action, List<Mj> majiangs) {
		PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
		List<Integer> list = MjHelper.toMajiangIds(majiangs);
		builder.addAllMajiangIds(list);
		builder.setAction(action);
		builder.setUserId(player.getUserId() + "");
		builder.setSeat(player.getSeat());
		return builder;
	}
}
