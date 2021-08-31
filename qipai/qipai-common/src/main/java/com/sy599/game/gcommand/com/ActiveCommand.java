package com.sy599.game.gcommand.com;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.LangHelp;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.List;

public class ActiveCommand extends BaseCommand {
    private static final int getActiveLZ=1;
    private static final int recevieActiveLZ=2;
    private static final int getActiveQueQiao=3;
    private static final int invite=4;
    private static final int allowInvite=5;
    private static final int getInviteMsg=6;
    private static final int recevieActiveQueQiao=7;

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<Integer> lists = req.getParamsList();
        if (lists == null || lists.size() == 0) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return;
        }
        // 获得传递过来的操作指令
        int command = req.getParams(0);
        switch (command){
            case getActiveLZ:
//                player.getActivityLZ();活动已过期
                break;
            case recevieActiveLZ:
//                player.receiveActiveLZ(req.getParams(1));活动已过期
                break;
            case getActiveQueQiao:
                player.getActivityQueQiaoMsg();
                break;
            case invite:
                player.getAqq().invitePeople(Long.parseLong(req.getStrParams(0)));
                break;
            case allowInvite:
                player.getAqq().sendAllowInvite(Long.parseLong(req.getStrParams(0)),player);

                break;
            case getInviteMsg:
                player.getActivityQueQiaoInviteBoard();
                break;
            case recevieActiveQueQiao:
                player.receiveActiveQueQiao(req.getParams(1),req.getParams(2)==1?"video":"normal");
                break;
        }
    }

    @Override
    public void setMsgTypeMap() {

    }

}