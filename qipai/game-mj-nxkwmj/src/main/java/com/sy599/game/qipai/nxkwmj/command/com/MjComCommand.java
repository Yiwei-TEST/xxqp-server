package com.sy599.game.qipai.nxkwmj.command.com;

import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.qipai.nxkwmj.bean.KwMjPlayer;
import com.sy599.game.qipai.nxkwmj.bean.KwMjTable;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class MjComCommand extends BaseCommand<KwMjPlayer> {

    @Override
    public void execute(KwMjPlayer player, MessageUnit message) throws Exception {
        KwMjTable table = player.getPlayingTable(KwMjTable.class);
        if (table == null) {
            return;
        }
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        synchronized (table) {
            switch (req.getCode()) {
                case WebSocketMsgType.req_code_tuoguan:
                    boolean autoPlay = req.getParamsCount() > 0 && req.getParams(0) == 1;
                    player.setAutoPlay(autoPlay, true);
                    LogUtil.msg("NxkwMjComCommand|setAutoPlay|" + player.getPlayingTableId() + "|" + player.getSeat() + "|" + player.getUserId() + "|" + (autoPlay ? 1 : 0) + "|" + 1);
                    break;
                case WebSocketMsgType.req_code_nxkwmj_fengDong:
                    table.fengDong(player);
                    LogUtil.msg("NxkwMjComCommand|fengDong|" + player.getPlayingTableId() + "|" + player.getSeat() + "|" + player.getUserId());
                    ComMsg.ComRes.Builder builder = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_nxkwmj_fengDong);
                    player.writeSocket(builder.build());
                    break;
                case WebSocketMsgType.req_code_piaoFen:
                    player.setAutoPlay(false, true);
                    table.piaoFen(player, req.getParams(0));
                    break;
                case  WebSocketMsgType.req_com_lastmajiang:
                    player.setAutoPlay(false, true);
                    table.answerHaiDi(player, req.getParams(0));
                    break;
                case  WebSocketMsgType.req_code_nxkwmj_jzkg:
                    player.setAutoPlay(false, true);
                    table.jzkGang(player, req.getParams(0));
                    break;
            }
        }
    }

    @Override
    public void setMsgTypeMap() {
        // TODO Auto-generated method stub
    }

}
