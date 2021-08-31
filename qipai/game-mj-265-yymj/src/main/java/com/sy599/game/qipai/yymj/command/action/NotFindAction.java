package com.sy599.game.qipai.yymj.command.action;

import com.sy599.game.qipai.yymj.bean.YyMjPlayer;
import com.sy599.game.qipai.yymj.bean.YyMjTable;
import com.sy599.game.qipai.yymj.command.AbsCodeCommandExecutor;
import com.sy599.game.util.LogUtil;

/**
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class NotFindAction extends AbsCodeCommandExecutor<YyMjTable, YyMjPlayer> {
    @Override
    public Integer actionCode() {
        return -1;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return AbsCodeCommandExecutor.GlobalCommonIndex.COMMAND_INDEX;
    }

    @Override
    public void execute(YyMjTable table, YyMjPlayer player, CarryMessage carryMessage) {
        LogUtil.errorLog.error("NotFindAction {},{},{},", table != null ? table.getId() : "null", player != null ? player.getUserId() : "null", carryMessage);
    }

}
