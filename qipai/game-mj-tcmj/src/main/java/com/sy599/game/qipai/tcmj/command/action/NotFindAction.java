package com.sy599.game.qipai.tcmj.command.action;

import com.sy599.game.qipai.tcmj.bean.TcMjPlayer;
import com.sy599.game.qipai.tcmj.bean.TcMjTable;
import com.sy599.game.qipai.tcmj.command.AbsCodeCommandExecutor;
import com.sy599.game.util.LogUtil;

/**
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class NotFindAction extends AbsCodeCommandExecutor<TcMjTable, TcMjPlayer> {
    @Override
    public Integer actionCode() {
        return -1;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return AbsCodeCommandExecutor.GlobalCommonIndex.COMMAND_INDEX;
    }

    @Override
    public void execute(TcMjTable table, TcMjPlayer player, CarryMessage carryMessage) {
        LogUtil.errorLog.error("NotFindAction {},{},{},", table != null ? table.getId() : "null", player != null ? player.getUserId() : "null", carryMessage);
    }

}
