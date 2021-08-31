package com.sy599.game.qipai.xpepaohuzi.command.action;

import com.sy599.game.qipai.xpepaohuzi.bean.XPPaohuziPlayer;
import com.sy599.game.qipai.xpepaohuzi.bean.XPPaohuziTable;
import com.sy599.game.qipai.xpepaohuzi.command.AbsCodeCommandExecutor;
import com.sy599.game.util.LogUtil;

/**
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class NotFindAction extends AbsCodeCommandExecutor<XPPaohuziTable, XPPaohuziPlayer> {
    @Override
    public Integer actionCode() {
        return -1;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.COMMAND_INDEX;
    }

    @Override
    public void execute(XPPaohuziTable table, XPPaohuziPlayer player, CarryMessage carryMessage) {
          LogUtil.errorLog.error("GhzNotFindAction {},{},{},", table!=null?table.getId():"null", player != null?player.getUserId():"null", carryMessage);
    }

}
