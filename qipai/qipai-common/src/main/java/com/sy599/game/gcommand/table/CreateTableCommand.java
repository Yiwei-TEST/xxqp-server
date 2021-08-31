package com.sy599.game.gcommand.table;

import java.util.*;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.TableManager;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.msg.serverPacket.TableRes.CreateTableRes;
import com.sy599.game.websocket.constant.WebSocketMsgType;

public class CreateTableCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        if (player.getMyExtend().isGroupMatch()) {
            player.writeErrMsg(LangMsg.code_257);
            return;
        }

        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        List<Integer> params = req.getParamsList();
        List<String> strParams = req.getStrParamsList();

        StringBuilder sb = new StringBuilder("createTable1|CreateTableCommand|1");
        sb.append("|").append(player.getUserId());
        sb.append("|").append(params);
        sb.append("|").append(strParams);
        LogUtil.monitorLog.info(sb.toString());
        int optType = !req.hasOptType() ? 0 : req.getOptType();
        if (optType == 0) {
            TableManager.getInstance().createTable(player, params, strParams, 0, 0, null);
        } else if (optType == 1) {
            // solo房间
            TableManager.getInstance().createSoloRoom(player, strParams);
        }
    }

    @Override
    public void setMsgTypeMap() {
        msgTypeMap.put(CreateTableRes.class, WebSocketMsgType.sc_createtable);
    }

}
