package com.sy599.game.qipai.pdkuai.command.play;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.qipai.pdkuai.bean.PdkPlayer;
import com.sy599.game.qipai.pdkuai.bean.PdkTable;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class CutCardCommand extends BaseCommand<PdkPlayer> {

    @Override
    public void execute(PdkPlayer player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        PdkTable table = player.getPlayingTable(PdkTable.class);
        if (table == null) {
            return;
        }
        synchronized (table) {
            switch (req.getCode()) {
                case 131:
                    player.setAutoPlay(false, table);
                    player.setLastOperateTime(System.currentTimeMillis());
                    break;
                case WebSocketMsgType.req_com_enter_cutcard:
                    enterCutCard(table, player);
                    break;
                case WebSocketMsgType.req_com_cutcard:
                    cutCard(table, player, req);
                    break;
                case WebSocketMsgType.req_code_pdk_daniao: {
                    int niaoFen = req.getParams(0); // 1 打鸟，0不打鸟
                    niaoFen = niaoFen == 1 ? table.getDaNiaoFen() : 0;
                    player.setAutoPlay(false, table);
                    player.setLastOperateTime(System.currentTimeMillis());
                    table.daNiao(player, niaoFen);
                    break;
                }
                case WebSocketMsgType.req_code_pdk_piaofen:
                    player.setAutoPlay(false, table);
                    player.setLastOperateTime(System.currentTimeMillis());
                    table.piaoFen(player, req.getParams(0));
                    break;
                case WebSocketMsgType.req_code_first_xipai:
                    player.setAutoPlay(false, table);
                    player.setLastOperateTime(System.currentTimeMillis());
                    if(!table.isFirstXipai()){
                        player.writeErrMsg(LangMsg.code_265);
                        return;
                    }
                    if(!table.checkXipaiCreditOnStartNext(player)){
                        player.writeErrMsg(LangMsg.code_266);
                        return;
                    }
                    table.handleFirstXipai(player, req.getParams(0));
                break;
            }
        }
    }

    private void enterCutCard(PdkTable table, PdkPlayer player) {
        boolean allReady = true;
        for (Player tableplayer : table.getSeatMap().values()) {
            if (tableplayer.getUserId() == player.getUserId()) {
                continue;
            }
            if (tableplayer.getState() != player_state.ready) {
                allReady = false;
                break;
            }
        }

        if (allReady) {

            ComRes.Builder builder = null;

            for (Player tableplayer : table.getSeatMap().values()) {
                if (tableplayer.getUserId() == player.getUserId()) {
                    builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_code_cutcard, -1, 0);
                } else {
                    builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_code_cutcard, -1, 1);
                }
                tableplayer.writeSocket(builder.build());
            }

        } else {
            player.writeErrMsg(LangMsg.code_210);
        }
    }

    private void cutCard(PdkTable table, PdkPlayer player, ComReq req) {
        int progress = req.getParams(0);
        ComRes.Builder builder = null;

        for (Player tableplayer : table.getSeatMap().values()) {
            if (tableplayer.getUserId() == player.getUserId()) {
                builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_code_cutcard, progress, 0);
            } else {
                builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_com_code_cutcard, progress, 1);
            }
            tableplayer.writeSocket(builder.build());
        }
    }

    @Override
    public void setMsgTypeMap() {

    }



}
