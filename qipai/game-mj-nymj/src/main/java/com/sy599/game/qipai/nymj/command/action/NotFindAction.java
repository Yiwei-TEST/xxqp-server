package com.sy599.game.qipai.nymj.command.action;

import com.sy599.game.qipai.nymj.bean.NyMjPlayer;
import com.sy599.game.qipai.nymj.bean.NyMjTable;
import com.sy599.game.qipai.nymj.command.AbsCodeCommandExecutor;
import com.sy599.game.util.LogUtil;

/**
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class NotFindAction extends AbsCodeCommandExecutor<NyMjTable, NyMjPlayer> {
    @Override
    public Integer actionCode() {
        return -1;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return AbsCodeCommandExecutor.GlobalCommonIndex.COMMAND_INDEX;
    }

    @Override
    public void execute(NyMjTable table, NyMjPlayer player, CarryMessage carryMessage) {
        LogUtil.errorLog.error("NotFindAction {},{},{},", table != null ? table.getId() : "null", player != null ? player.getUserId() : "null", carryMessage);
    }

}
