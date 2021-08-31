package com.sy599.game.qipai.yjghz.tool;

import com.sy599.game.character.Player;
import com.sy599.game.msg.serverPacket.PlayCardResMsg.PlayPaohuziRes;
import com.sy599.game.qipai.yjghz.constant.YjGhzCard;

import java.util.List;

public class YjGhzResTool {
    public static void buildPlayRes(PlayPaohuziRes.Builder builder, Player player, int action, List<YjGhzCard> majiangs) {
        List<Integer> list = YjGhzTool.toGhzCardIds(majiangs);
        builder.addAllPhzIds(list);
        builder.setAction(action);
        builder.setUserId(player.getUserId() + "");
        builder.setSeat(player.getSeat());
    }
}
