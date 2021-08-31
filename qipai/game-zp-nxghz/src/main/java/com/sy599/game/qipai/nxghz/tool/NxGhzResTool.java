package com.sy599.game.qipai.nxghz.tool;

import com.sy599.game.character.Player;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayPaohuziRes;
import com.sy599.game.qipai.nxghz.constant.NxGhzCard;

import java.util.List;

public class NxGhzResTool {
    public static void buildPlayRes(PlayPaohuziRes.Builder builder, Player player, int action, List<NxGhzCard> majiangs) {
        List<Integer> list = NxGhzTool.toGhzCardIds(majiangs);
        builder.addAllPhzIds(list);
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
    }
}
