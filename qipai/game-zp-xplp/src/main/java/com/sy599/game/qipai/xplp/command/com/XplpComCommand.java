package com.sy599.game.qipai.xplp.command.com;

import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.xplp.bean.XplpPlayer;
import com.sy599.game.qipai.xplp.bean.XplpTable;
import com.sy599.game.qipai.xplp.constant.XplpConstants;
import com.sy599.game.qipai.xplp.rule.XpLp;
import com.sy599.game.qipai.xplp.tool.XplpTool;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.List;

public class XplpComCommand extends BaseCommand<XplpPlayer> {

    @Override
    public void execute(XplpPlayer player, MessageUnit message) throws Exception {
        XplpTable table = player.getPlayingTable(XplpTable.class);
        if (table == null) {
            return;
        }
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        synchronized (table) {
            switch (req.getCode()) {
                case WebSocketMsgType.req_code_mj_ting:
                    ting(table, player);
                    break;
                case WebSocketMsgType.req_code_tuoguan:
                    boolean autoPlay = req.getParamsCount() > 0 && req.getParams(0) == 1;
                    player.setAutoPlay(autoPlay, true);
                    LogUtil.msg("ZzMjComCommand|setAutoPlay|" + player.getPlayingTableId() + "|" + player.getSeat() + "|" + player.getUserId() + "|" + (autoPlay ? 1 : 0) + "|" + 1);
                    break;
                case WebSocketMsgType.req_code_zzmj_piaofen:
                    player.setAutoPlay(false, true);
                    table.piaoFen(player, req.getParams(0));
                    break;
            }
        }
    }

    private void ting(XplpTable table, XplpPlayer player) {
        if (table.getState() != table_state.play) {
            return;
        }
        List<XpLp> majiangIds = player.getHandMajiang();
        if (majiangIds.size() % 3 != 1) {
            return;
        }
        List<Integer> huVal = new ArrayList<>();
        for (XpLp majiang : XpLp.fullMj) {
            if(table.isNohua() && majiang.getId() > 200){
            	continue;
            }
        	List<XpLp> copy = new ArrayList<>(majiangIds);
            copy.add(majiang);
            if (XplpTool.isPingHu(copy)) {
                huVal.add(majiang.getVal());
            }
        }
        ComMsg.ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_mj_ting, huVal);
        player.writeSocket(build.build());
    }

    @Override
    public void setMsgTypeMap() {
        // TODO Auto-generated method stub

    }

}
