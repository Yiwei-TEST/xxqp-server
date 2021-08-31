package com.sy599.game.gcommand.com;

import com.alibaba.fastjson.JSON;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.SoloRoomConfig;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.SoloRoomUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.List;

public class SoloRoomCommand extends BaseCommand {

    @Override
    public void setMsgTypeMap() {

    }

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);

        int optType = req.hasOptType() ? req.getOptType() : 0; // 0:玩法信息
        if (optType == 0) {
            getSoloRoomConfigList(player, req);
        }

    }

    private void getSoloRoomConfigList(Player player, ComMsg.ComReq req) {
        int intsCount = req.getParamsCount();
        if (intsCount == 0) {
            player.writeErrMsg(LangMsg.code_3);
            return;
        }
        List<Integer> intParams = req.getParamsList();
        int soloType = intParams.get(0);
        List<SoloRoomConfig> list = SoloRoomUtil.getSoloRoomConfigList(soloType);
        player.writeComMessage(WebSocketMsgType.req_code_solo_room_config_list, JSON.toJSONString(list));
    }
}
