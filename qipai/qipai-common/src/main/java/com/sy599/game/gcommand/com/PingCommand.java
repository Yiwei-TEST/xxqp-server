package com.sy599.game.gcommand.com;

import com.sy599.game.character.Player;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg.PingRes;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;

public class PingCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
//		ComReq req = (ComReq) this.recognize(ComReq.class, message);

        PingRes.Builder res = PingRes.newBuilder();
        res.setT((int) (player.loadAllCards()));

        String version = ResourcesConfigsUtil.loadServerPropertyValue("version", "");
        res.setV(version);

        player.writeSocket(res.build());

    }

    @Override
    public void setMsgTypeMap() {

    }

}
