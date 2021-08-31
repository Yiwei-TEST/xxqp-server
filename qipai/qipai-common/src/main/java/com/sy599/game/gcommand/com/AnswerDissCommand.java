package com.sy599.game.gcommand.com;

import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.msg.serverPacket.ComMsg.ComRes;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.SendMsgUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

/**
 * 应答解散
 *
 * @author lc
 */
public class AnswerDissCommand extends BaseCommand {

    @Override
    public void setMsgTypeMap() {
    }

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        BaseTable table = player.getPlayingTable();
        if (table == null) {
            return;
        }
        if (table.isGoldRoom()) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_43));
            return;
        }
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        int answer = req.getParams(0);
        LogUtil.msgLog.info("AnswerDissCommand|" + table.getId() + "|" + table.getPlayBureau() + "|" + table.getPlayType() + "|" + player.getUserId() + "|" + player.getSeat() + "|" + answer);
        synchronized (table) {
            table.answerDiss(player.getSeat(), answer);
            if (answer == 1) {
                table.checkDiss(player);
            } else {
                ComRes.Builder com = SendMsgUtil.buildComRes(WebSocketMsgType.res_code_nodisstable, player.getUserId() + "", player.getName(), table.getOnTablePlayerNum());
                for (Player seat : table.getSeatMap().values()) {
                    seat.writeSocket(com.build());
                }
                table.clearAnswerDiss();
            }
        }
    }


}
