package com.sy599.game.qipai.symj.command.com;

import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.symj.bean.SyMjHu;
import com.sy599.game.qipai.symj.bean.SyMjPlayer;
import com.sy599.game.qipai.symj.bean.SyMjTable;
import com.sy599.game.qipai.symj.constant.SyMjConstants;
import com.sy599.game.qipai.symj.rule.SyMj;
import com.sy599.game.qipai.symj.tool.SyMjTool;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.List;

public class SyMjComCommand extends BaseCommand<SyMjPlayer> {

    @Override
    public void execute(SyMjPlayer player, MessageUnit message) throws Exception {
        SyMjTable table = player.getPlayingTable(SyMjTable.class);
        if (table == null) {
            return;
        }
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        synchronized (table) {
            switch (req.getCode()) {
                case WebSocketMsgType.req_code_chui:
                    chui(table, player, req.getParams(0));
                    break;
                case WebSocketMsgType.req_code_mj_ting:
                    ting(table, player);
                    break;
                case WebSocketMsgType.req_code_tuoguan:
                    boolean autoPlay = req.getParamsCount() > 0 && req.getParams(0) == 1;
                    player.setAutoPlay(autoPlay, true);
                    LogUtil.msg("SyMjComCommand|setAutoPlay|" + player.getPlayingTableId() + "|" + player.getSeat() + "|" + player.getUserId() + "|" + (autoPlay ? 1 : 0) + "|" + 1);
                    break;
            }
        }
    }

    private void chui(SyMjTable table, SyMjPlayer player, int chuiVal) {
        if (table.getTableStatus() != SyMjConstants.TABLE_STATUS_CHUI) {
            return;
        }
        if (player.getChui() == -1 && (chuiVal == 0 || chuiVal == 1)) {
            player.setChui(chuiVal);
        } else {
            return;
        }
        StringBuilder sb = new StringBuilder("SyMj");
        sb.append("|").append(table.getId());
        sb.append("|").append(table.getPlayBureau());
        sb.append("|").append(player.getUserId());
        sb.append("|").append(player.getSeat());
        sb.append("|").append(player.isAutoPlay() ? 1 : 0);
        sb.append("|").append("chui");
        sb.append("|").append(chuiVal);

        ComMsg.ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_chui, player.getSeat(), chuiVal);
        table.broadMsg(build.build());
        table.broadMsgRoomPlayer(build.build());
        table.checkDeal(player.getUserId());
    }

    private void ting(SyMjTable table, SyMjPlayer player) {
        if (table.getState() != table_state.play) {
            return;
        }
        List<SyMj> majiangIds = player.getHandMajiang();
        if (majiangIds.size() % 3 != 1) {
            return;
        }
        List<Integer> huVal = new ArrayList<>();
        List<SyMj> majiangPai = SyMjConstants.getMajiangPai(table.getDaiFeng());
        for (SyMj majiang : majiangPai) {
            SyMjHu majiangHu = SyMjTool.isHu(player, majiang);
            if (majiangHu.isHu()) {
                huVal.add(majiang.getVal());
            }
        }
        ComMsg.ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_mj_ting, huVal);
        player.writeSocket(build.build());
    }

    @Override
    public void setMsgTypeMap() {
    }
}
