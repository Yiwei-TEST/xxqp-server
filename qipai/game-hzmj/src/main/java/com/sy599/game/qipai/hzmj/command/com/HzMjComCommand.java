package com.sy599.game.qipai.hzmj.command.com;

import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.hzmj.bean.HzMjPlayer;
import com.sy599.game.qipai.hzmj.bean.HzMjTable;
import com.sy599.game.qipai.hzmj.constant.HzMjConstants;
import com.sy599.game.qipai.hzmj.rule.HzMj;
import com.sy599.game.qipai.hzmj.tool.HzMjTool;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.List;

public class HzMjComCommand extends BaseCommand<HzMjPlayer> {

    @Override
    public void execute(HzMjPlayer player, MessageUnit message) throws Exception {
        HzMjTable table = player.getPlayingTable(HzMjTable.class);
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
                    LogUtil.msg("HzMjComCommand|setAutoPlay|" + player.getPlayingTableId() + "|" + player.getSeat() + "|" + player.getUserId() + "|" + (autoPlay ? 1 : 0) + "|" + 1);
                    break;
                case WebSocketMsgType.req_code_piao_fen:
                    if (table.getKePiao() > 0) {
                        int paoFen = req.getParams(0);
                        player.setPiaoPoint(paoFen <= 0 ? 0 : paoFen);
                        ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_piao_fen, player.getSeat(), player.getPiaoPoint());
                        table.broadMsgToAll(com.build());
                        table.checkDeal();
                    }
            }
        }
    }

    private void ting(HzMjTable table, HzMjPlayer player) {
        if (table.getState() != table_state.play) {
            return;
        }
        List<HzMj> majiangIds = player.getHandMajiang();
        if (majiangIds.size() % 3 != 1) {
            return;
        }
        List<Integer> huVal = new ArrayList<>();
        List<HzMj> majiangPai = HzMjConstants.getMajiangPai();
        for (HzMj majiang : majiangPai) {
            List<HzMj> copy = new ArrayList<>(majiangIds);
            copy.add(majiang);
            if (HzMjTool.isHu(copy, table)) {
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
