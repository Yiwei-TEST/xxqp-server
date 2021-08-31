package com.sy599.game.qipai.xxgaohuzi.command.action;

import com.sy599.game.msg.serverPacket.PlayCardReqMsg;
import com.sy599.game.qipai.xxgaohuzi.bean.PaohzDisAction;
import com.sy599.game.qipai.xxgaohuzi.bean.XxGaohuziPlayer;
import com.sy599.game.qipai.xxgaohuzi.bean.XxGaohuziTable;
import com.sy599.game.qipai.xxgaohuzi.command.AbsCodeCommandExecutor;
import com.sy599.game.qipai.xxgaohuzi.constant.PaohzCard;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.LogUtil;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Guang.OuYang
 * @date 2019/9/2-16:58
 */
public class GhzNotFindAction extends AbsCodeCommandExecutor<XxGaohuziTable, XxGaohuziPlayer> {
    @Override
    public Integer actionCode() {
        return -1;  //缺省操作
    }

    @Override
    public GlobalCommonIndex globalCommonIndex() {
        return GlobalCommonIndex.COMMAND_INDEX;
    }

    @Override
    public void execute(XxGaohuziTable table, XxGaohuziPlayer player, CarryMessage carryMessage) {
          LogUtil.errorLog.error("GhzNotFindAction {},{},{},", table!=null?table.getId():"null", player != null?player.getUserId():"null", carryMessage);
    }

}
