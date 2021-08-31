package com.sy599.game.qipai.zzmj.command.com;

import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.zzmj.bean.ZzMjPlayer;
import com.sy599.game.qipai.zzmj.bean.ZzMjTable;
import com.sy599.game.qipai.zzmj.constant.ZzMjConstants;
import com.sy599.game.qipai.zzmj.rule.ZzMj;
import com.sy599.game.qipai.zzmj.tool.ZzMjTool;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.List;

public class ZzMjComCommand extends BaseCommand<ZzMjPlayer> {

    @Override
    public void execute(ZzMjPlayer player, MessageUnit message) throws Exception {
        ZzMjTable table = player.getPlayingTable(ZzMjTable.class);
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

    private void ting(ZzMjTable table, ZzMjPlayer player) {
        if (table.getState() != table_state.play) {
            return;
        }
        List<ZzMj> majiangIds = player.getHandMajiang();
        if (majiangIds.size() % 3 != 1) {
            return;
        }
        List<Integer> huVal = new ArrayList<>();
        List<ZzMj> majiangPai = ZzMjConstants.getMajiangPai(table.getMaxPlayerCount());
        for (ZzMj majiang : majiangPai) {
            List<ZzMj> copy = new ArrayList<>(majiangIds);
            copy.add(majiang);
            if (ZzMjTool.isHu(copy, table.isHu7dui())) {
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
