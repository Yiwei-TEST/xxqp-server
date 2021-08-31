package com.sy599.game.qipai.zzmj.tool;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.character.Player;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayMajiangRes;
import com.sy599.game.qipai.zzmj.rule.ZzMj;

public class ZzMjResTool {
    public static void buildPlayRes(PlayMajiangRes.Builder builder, Player player, int action, List<ZzMj> majiangs) {
        List<Integer> list = ZzMjHelper.toMajiangIds(majiangs);
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
    public static PlayMajiangRes.Builder buildDisMajiangRes(Player player, ZzMj majiang) {
        PlayMajiangRes.Builder builder = PlayMajiangRes.newBuilder();
        builder.setAction(0);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
        builder.addMajiangIds(majiang.getId());
        return builder;
    }
}
