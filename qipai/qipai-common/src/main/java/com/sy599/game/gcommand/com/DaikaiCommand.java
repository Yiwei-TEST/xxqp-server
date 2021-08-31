package com.sy599.game.gcommand.com;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.dao.TableDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.manager.TableManager;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.StringUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.List;

public class DaikaiCommand extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {

        if (player.getMyExtend().isGroupMatch()){
            player.writeErrMsg(LangMsg.code_257);
            return;
        }

        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        List<String> strParams = req.getStrParamsList();// 对应APP传入对应APP上的所有玩法
        List<Integer> wanfaIds = new ArrayList<>();
        if(strParams != null && !strParams.isEmpty() && req.getStrParamsCount() > 0) {
            wanfaIds = StringUtil.explodeToIntList(strParams.get(0));
        }
        int count;
        if(wanfaIds.isEmpty())
            count = TableDao.getInstance().getDaikaiTableCount(player.getUserId());
        else
            count = TableDao.getInstance().getWanfaDaikaiTableCount(player.getUserId(), wanfaIds);
        if (count >= 10) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_211));
            return;
        }
        TableManager.getInstance().daikaiTable(player, req);
    }

    @Override
    public void setMsgTypeMap() {

    }
}
