package com.sy599.game.qipai.zhengzmj.command.com;

import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.zhengzmj.bean.ZhengzMjPlayer;
import com.sy599.game.qipai.zhengzmj.bean.ZhengzMjTable;
import com.sy599.game.qipai.zhengzmj.constant.ZhengzMjConstants;
import com.sy599.game.qipai.zhengzmj.rule.ZhengzMj;
import com.sy599.game.qipai.zhengzmj.tool.ZhengzMjTool;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.List;

public class ZhengzMjComCommand extends BaseCommand<ZhengzMjPlayer> {

    @Override
    public void execute(ZhengzMjPlayer player, MessageUnit message) throws Exception {
        ZhengzMjTable table = player.getPlayingTable(ZhengzMjTable.class);
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
                    LogUtil.msg("ZhengzMjComCommand|setAutoPlay|" + player.getPlayingTableId() + "|" + player.getSeat() + "|" + player.getUserId() + "|" + (autoPlay ? 1 : 0) + "|" + 1);
                    break;
                case WebSocketMsgType.req_code_piao_fen:
                    if (table.getKePiao() > 5) {
                        int paoFen = req.getParams(0);
                        player.setPiaoPoint(paoFen <= 0 ? 0 : paoFen);
                        ComMsg.ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_piao_fen, player.getSeat(), player.getPiaoPoint());
                        table.sendPiaoPoint(player, paoFen <= 0 ? 0 : paoFen);
                        table.broadMsgToAll(com.build());
                        table.checkDeal();
                    }
            }
        }
    }

    private void ting(ZhengzMjTable table, ZhengzMjPlayer player) {
        if (table.getState() != table_state.play) {
            return;
        }
        List<ZhengzMj> majiangIds = player.getHandMajiang();
        if (majiangIds.size() % 3 != 1) {
            return;
        }
        List<Integer> huVal = new ArrayList<>();
        List<ZhengzMj> majiangPai = ZhengzMjConstants.getMajiangPai();
        for (ZhengzMj majiang : majiangPai) {
            List<ZhengzMj> copy = new ArrayList<>(majiangIds);
            copy.add(majiang);
            if (ZhengzMjTool.isHu(copy, table)) {
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
