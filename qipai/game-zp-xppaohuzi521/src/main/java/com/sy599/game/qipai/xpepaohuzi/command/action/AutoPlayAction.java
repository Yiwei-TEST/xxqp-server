package com.sy599.game.qipai.xpepaohuzi.command.action;

import com.sy599.game.qipai.xpepaohuzi.bean.XPPaohuziPlayer;
import com.sy599.game.qipai.xpepaohuzi.bean.XPPaohuziTable;
import com.sy599.game.qipai.xpepaohuzi.command.AbsCodeCommandExecutor;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

/**
 * 托管
 *
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class AutoPlayAction extends AbsCodeCommandExecutor<XPPaohuziTable, XPPaohuziPlayer> {

    @Override
    public Integer actionCode() {
        return WebSocketMsgType.REQ_DTZ_AUTOPLAY;
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.COMMAND_INDEX;
    }

    @Override
    public void execute(XPPaohuziTable table, XPPaohuziPlayer player, CarryMessage carryMessage) {
//        player.setAutoPlay(!player.isAutoPlay(), table);
        player.setAutoPlay(false, table);
        player.setLastOperateTime(System.currentTimeMillis());
        LogUtil.msg("房间号=" + table.getId() + ",玩家=" + player.getUserId() + "[" + player.getName() + "]座位=" + player.getSeat() + "手动取消托管=" + player.isAutoPlay());
    }
}

