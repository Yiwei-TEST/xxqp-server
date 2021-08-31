package com.sy599.game.qipai.daozmj.command.com;

import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.daozmj.bean.DaozMjPlayer;
import com.sy599.game.qipai.daozmj.bean.DaozMjTable;
import com.sy599.game.qipai.daozmj.constant.DaozMjConstants;
import com.sy599.game.qipai.daozmj.rule.DzMj;
import com.sy599.game.qipai.daozmj.tool.DaozMjTool;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.List;

public class DaozMjComCommand extends BaseCommand<DaozMjPlayer> {

    @Override
    public void execute(DaozMjPlayer player, MessageUnit message) throws Exception {
        DaozMjTable table = player.getPlayingTable(DaozMjTable.class);
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
                    LogUtil.msg("DaozMjComCommand|setAutoPlay|" + player.getPlayingTableId() + "|" + player.getSeat() + "|" + player.getUserId() + "|" + (autoPlay ? 1 : 0) + "|" + 1);
                    break;
                case WebSocketMsgType.req_code_piao_fen:
                    if (table.getWaiPiao() > 0) {
                        int paoFen = req.getParams(0);
                        player.setWaiPiaoPoint(paoFen <= 0 ? 0 : paoFen);
                        ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_piao_fen, player.getSeat(), player.getWaiPiaoPoint());
                        table.broadMsgToAll(com.build());
                        table.checkDeal();
                    }
            }
        }
    }

    private void ting(DaozMjTable table, DaozMjPlayer player) {
        if (table.getState() != table_state.play) {
            return;
        }
        List<DzMj> majiangIds = player.getHandMajiang();
        if (majiangIds.size() % 3 != 1) {
            return;
        }
        List<Integer> huVal = new ArrayList<>();
        List<DzMj> majiangPai = DaozMjConstants.getMajiangPai();
        for (DzMj majiang : majiangPai) {
            List<DzMj> copy = new ArrayList<>(majiangIds);
            copy.add(majiang);
            if (DaozMjTool.isHu(copy, table)) {
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
