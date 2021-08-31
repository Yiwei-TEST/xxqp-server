package com.sy599.game.gcommand.gold;

import com.sy599.game.character.GoldPlayer;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.dao.gold.GoldDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.util.*;
import com.sy599.game.util.gold.constants.GoldConstans;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class GoldUserChangeCommand extends BaseCommand {
    @Override
    public void setMsgTypeMap() {

    }

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        if (!GoldConstans.isGoldSiteOpen()) {
            player.writeErrMsg(LangHelp.getMsg(LangMsg.code_901));
            return;
        }

        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        GoldPlayer goldPlayer = player.getGoldPlayer();
        if (goldPlayer == null) {
            LogUtil.e("changeGoldUserInfo err-->goldUser is not exist-->userId" + player.getUserId());
            return;
        }
        // 昵称
        String nickName = req.getStrParams(0);
        // 签名
        String signature = req.getStrParams(1);
        long userId = player.getUserId();
        Map<String, Object> modify = new HashMap<>();
        if (!StringUtils.isBlank(nickName)) {
            nickName =  KeyWordsFilter.getInstance().filt(nickName);
            if (StringUtil.isBlank(nickName) || nickName.contains("*")) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_908, nickName));
                return;
            }
            nickName = nickName.replaceAll("\\,|\\;|\\_|\\||\\*","");
            modify.put("nickName", nickName);
        }
        if (!StringUtils.isBlank(signature)) {
            signature = KeyWordsFilter.getInstance().filt(signature);
            modify.put("signature", signature);
        }
        modify.put("userId", (int)userId);
        int res = GoldDao.getInstance().updateGoldUser(modify);
        LogUtil.msg("changeGoldUserInfo-->userId:"+userId+",modify:"+JacksonUtil.writeValueAsString(modify)+",result:"+res);
        player.writeComMessage(WebSocketMsgType.res_com_gold_golduserchange, res, JacksonUtil.writeValueAsString(modify));
        if (res == 1) {
            goldPlayer.setUserNickName(nickName);
            goldPlayer.setSignature(signature);
        }
    }
}
