package com.sy599.game.qipai.hbgzp.command.com;

import java.util.ArrayList;
import java.util.List;

import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.hbgzp.bean.HbgzpPlayer;
import com.sy599.game.qipai.hbgzp.bean.HbgzpTable;
import com.sy599.game.qipai.hbgzp.constant.HbgzpConstants;
import com.sy599.game.qipai.hbgzp.rule.Hbgzp;
import com.sy599.game.qipai.hbgzp.tool.HbgzpHuLack;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class HbgzpComCommand extends BaseCommand<HbgzpPlayer> {

    @Override
    public void execute(HbgzpPlayer player, MessageUnit message) throws Exception {
        HbgzpTable table = player.getPlayingTable(HbgzpTable.class);
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
                    LogUtil.msg("HbgzpComCommand|setAutoPlay|" + player.getPlayingTableId() + "|" + player.getSeat() + "|" + player.getUserId() + "|" + (autoPlay ? 1 : 0) + "|" + 1);
                    break;
                case WebSocketMsgType.req_code_hbgzp_piaofen:
                    player.setAutoPlay(false, true);
                    table.piaoFen(player, req.getParams(0));
                    break;
            }
        }
    }

    private void chui(HbgzpTable table, HbgzpPlayer player, int chuiVal) {
        if (table.getTableStatus() != HbgzpConstants.TABLE_STATUS_CHUI) {
            return;
        }
        if (player.getChui() == -1 && (chuiVal == 0 || chuiVal == 1)) {
            player.setChui(chuiVal);
        } else {
            return;
        }
        StringBuilder sb = new StringBuilder("Hbgzp");
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

    private void ting(HbgzpTable table, HbgzpPlayer player) {
        if (table.getState() != table_state.play) {
            return;
        }
        List<Hbgzp> majiangIds = player.getHandMajiang();
        if (majiangIds.size() % 3 != 1) {
            return;
        }
        List<Integer> huVal = new ArrayList<>();
        List<Hbgzp> majiangPai = HbgzpConstants.getMajiangPai();
        for (Hbgzp majiang : majiangPai) {
            
        	HbgzpHuLack lack = player.checkHuNew(majiang, true);
    		if (lack.isHu()) {
    			huVal.add(majiang.getVal());
    		}
        	
//        	HbgzpHu majiangHu = HbgzpTool.isHu(player, majiang);
//            if (majiangHu.isHu()) {
//                huVal.add(majiang.getVal());
//            }
        }
        ComMsg.ComRes.Builder build = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_mj_ting, huVal);
        player.writeSocket(build.build());
    }

    @Override
    public void setMsgTypeMap() {
    }
}
