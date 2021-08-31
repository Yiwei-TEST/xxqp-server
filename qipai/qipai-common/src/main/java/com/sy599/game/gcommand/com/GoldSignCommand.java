package com.sy599.game.gcommand.com;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.enums.SourceType;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class GoldSignCommand extends BaseCommand {
    private final static int sign=3;
    private final static int getSignMsg=4;
    private final static int videoSign=5;

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
            case sign:
                player.sign(req.getParams(1), SourceType.sign);
                break;
            case getSignMsg://手动获取签到信息
                player.writeGoldSignInfo();
                break;
            case videoSign:
                player.videoSign();
                break;
        }
    }

    @Override
    public void setMsgTypeMap() {

    }

}
