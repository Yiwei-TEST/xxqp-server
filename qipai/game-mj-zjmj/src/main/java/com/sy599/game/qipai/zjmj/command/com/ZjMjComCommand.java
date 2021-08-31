package com.sy599.game.qipai.zjmj.command.com;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.zjmj.bean.ZjMjPlayer;
import com.sy599.game.qipai.zjmj.bean.ZjMjTable;
import com.sy599.game.qipai.zjmj.constant.ZjMjConstants;
import com.sy599.game.qipai.zjmj.rule.ZjMj;
import com.sy599.game.qipai.zjmj.tool.ZjMjTool;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class ZjMjComCommand extends BaseCommand<ZjMjPlayer> {

    @Override
    public void execute(ZjMjPlayer player, MessageUnit message) throws Exception {
        ZjMjTable table = player.getPlayingTable(ZjMjTable.class);
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

    private void ting(ZjMjTable table, ZjMjPlayer player) {
        if (table.getState() != table_state.play) {
            return;
        }
        List<ZjMj> majiangIds = player.getHandMajiang();
        if (majiangIds.size() % 3 != 1) {
            return;
        }
        List<Integer> huVal = new ArrayList<>();
        List<ZjMj> majiangPai = ZjMjConstants.getMajiangPai();
        for (ZjMj majiang : majiangPai) {
            List<ZjMj> copy = new ArrayList<>(majiangIds);
            copy.add(majiang);
            if (ZjMjTool.isHu(copy, table,player.getPeng(),player.getGang())) {
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
