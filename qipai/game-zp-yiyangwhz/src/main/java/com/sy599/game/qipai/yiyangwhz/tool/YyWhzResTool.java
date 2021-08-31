package com.sy599.game.qipai.yiyangwhz.tool;

import com.sy599.game.character.Player;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayPaohuziRes;
import com.sy599.game.qipai.yiyangwhz.constant.YyWhzCard;

import java.util.List;

public class YyWhzResTool {
    public static void buildPlayRes(PlayPaohuziRes.Builder builder, Player player, int action, List<YyWhzCard> majiangs) {
        List<Integer> list = YyWhzTool.toGhzCardIds(majiangs);
        builder.addAllPhzIds(list);
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
    }
}
