package com.sy599.game.gcommand.com;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.enums.SourceType;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.util.LangHelp;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.List;

public class MissionCommand extends BaseCommand {
    private static final int brokeShare=1;
    private static final int getMissionMsg=2;
    private static final int complete=3;
    private static final int brokeAward=4;
    private static final int deskShare=5;
    private static final int videoAward=6;
    private static final int getVideoAwardMsg=7;
    private static final int brokeAwardVideo=8;

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        List<Integer> lists = req.getParamsList();
        if (lists == null || lists.size() == 0) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
            return;
        }
        // 获得传递过来的操作指令
        int command = req.getParams(0);
        switch (command){
            case getMissionMsg:
                player.getMissionMsg();
                break;
            case complete:
                player.receiveMissionAward(req.getParams(1));
                break;
            case brokeShare:
                player.brokeShare();
                break;
            case brokeAward:
                player.brokeAward(SourceType.broke_award);
                break;
            case deskShare:
                player.deskShare();
                break;
            case videoAward:
                player.videoAward();
                break;
            case getVideoAwardMsg:
                player.getVideoAwardMsg();
                break;
            case brokeAwardVideo:
                player.brokeAward(SourceType.video_broke);
                break;
        }
    }

    @Override
    public void setMsgTypeMap() {

    }

}
