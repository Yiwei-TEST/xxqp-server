package com.sy599.game.qipai.yzlc.command.action;

import com.sy599.game.qipai.yzlc.command.AbsCodeCommandExecutor;
import com.sy599.game.qipai.yzlc.bean.YzLcPaohuziPlayer;
import com.sy599.game.qipai.yzlc.bean.YzLcPaohuziTable;
import com.sy599.game.util.LogUtil;

/**
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class NotFindAction extends AbsCodeCommandExecutor<YzLcPaohuziTable, YzLcPaohuziPlayer> {
    @Override
    public Integer actionCode() {
        return -1;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.COMMAND_INDEX;
    }

    @Override
    public void execute(YzLcPaohuziTable table, YzLcPaohuziPlayer player, CarryMessage carryMessage) {
          LogUtil.errorLog.error("GhzNotFindAction {},{},{},", table!=null?table.getId():"null", player != null?player.getUserId():"null", carryMessage);
    }

}
