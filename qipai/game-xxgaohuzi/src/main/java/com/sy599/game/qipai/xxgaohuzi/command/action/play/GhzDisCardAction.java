package com.sy599.game.qipai.xxgaohuzi.command.action.play;

import com.sy599.game.msg.serverPacket.PlayCardReqMsg;
import com.sy599.game.qipai.xxgaohuzi.bean.PaohzDisAction;
import com.sy599.game.qipai.xxgaohuzi.bean.XxGaohuziPlayer;
import com.sy599.game.qipai.xxgaohuzi.bean.XxGaohuziTable;
import com.sy599.game.qipai.xxgaohuzi.command.AbsCodeCommandExecutor;
import com.sy599.game.qipai.xxgaohuzi.constant.PaohzCard;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.LogUtil;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;

/**
 * 出牌操作, 与服务器指令做比较
 *
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class GhzDisCardAction extends AbsCodeCommandExecutor<XxGaohuziTable, XxGaohuziPlayer> {
    @Override
    public Integer actionCode() {
        return -1;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.PLAY_INDEX;
    }

    @Override
    public void execute(XxGaohuziTable table, XxGaohuziPlayer player, CarryMessage carryMessage) {
        PlayCardReqMsg.PlayCardReq playCard = carryMessage.parseFrom(PlayCardReqMsg.PlayCardReq.class);
        //指令
        table.play(player, new ArrayList<>(playCard.getCardIdsList()), playCard.getCardType());
    }

}
