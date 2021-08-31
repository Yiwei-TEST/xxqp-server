package com.sy599.game.qipai.yzwdmj.command.com;

import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.yzwdmj.bean.YzwdmjPlayer;
import com.sy599.game.qipai.yzwdmj.bean.YzwdmjTable;
import com.sy599.game.qipai.yzwdmj.constant.YzwdmjConstants;
import com.sy599.game.qipai.yzwdmj.rule.Yzwdmj;
import com.sy599.game.qipai.yzwdmj.tool.YzwdmjTool;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.List;

public class YzwdmjComCommand extends BaseCommand<YzwdmjPlayer> {

    @Override
    public void execute(YzwdmjPlayer player, MessageUnit message) throws Exception {
        YzwdmjTable table = player.getPlayingTable(YzwdmjTable.class);
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
                    LogUtil.msg("Yzwdmj|setAutoPlay|" + player.getPlayingTableId() + "|" + player.getSeat() + "|" + player.getUserId() + "|" + (autoPlay ? 1 : 0) + "|" + 1);
                    break;
                case WebSocketMsgType.req_code_piaoFen:
                    player.setAutoPlay(false, true);
                    table.piaoFen(player, req.getParams(0));
                    break;
            }
        }
    }

    private void ting(YzwdmjTable table, YzwdmjPlayer player) {
        if (table.getState() != table_state.play) {
            return;
        }
        List<Yzwdmj> majiangIds = player.getHandMajiang();
        if (majiangIds.size() % 3 != 1) {
            return;
        }
        List<Integer> huVal = new ArrayList<>();
        List<Yzwdmj> majiangPai = YzwdmjConstants.getMajiangPai(table.getMaxPlayerCount());
        for (Yzwdmj majiang : majiangPai) {
            List<Yzwdmj> copy = new ArrayList<>(majiangIds);
            copy.add(majiang);
            if (YzwdmjTool.isHu(copy, table.isHu7dui())) {
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
