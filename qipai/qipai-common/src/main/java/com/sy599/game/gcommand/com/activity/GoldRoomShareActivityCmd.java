package com.sy599.game.gcommand.com.activity;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.bean.UserShare;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.UserShareDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.db.enums.UserMessageEnum;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.gcommand.com.ActivityCommand;
import com.sy599.game.message.MessageUtil;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.staticdata.bean.ActivityConfig;
import com.sy599.game.staticdata.bean.GoldRoomShareActivityConfig;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * 欢乐金币场局数奖励活动
 */
public class GoldRoomShareActivityCmd extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        ComReq req = (ComReq) this.recognize(ComReq.class, message);
        List<Integer> reqParams = req.getParamsList();
        int reqType = reqParams.get(0);// 0打开 1领取
        GoldRoomShareActivityConfig configInfo = (GoldRoomShareActivityConfig) ActivityConfig.getActivityConfigInfo(ActivityConfig.activity_gold_room_share);
        long userId = player.getUserId();

        JSONObject userJsonObj = new JSONObject();
        Calendar now = Calendar.getInstance();
        int isShared = isGoldRoomShared(player);
        String currentDate = new SimpleDateFormat("yyyyMMdd").format(now.getTime());
        int finishCount = UserDao.getInstance().countUserStatistics(true, String.valueOf(player.getUserId()), "gold", currentDate, null);
        if (reqType == 0) {
            userJsonObj.put("isShared", isShared);
            userJsonObj.put("finishCount", finishCount);
            userJsonObj.put("needCount", configInfo.getFinishCount());
            ActivityCommand.sendActivityInfo(player, configInfo, userJsonObj);
        } else {
            if (isShared == 1) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_219));
                return;
            }
            if (finishCount < configInfo.getFinishCount()) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_220));
                return;
            }
            int diamond = configInfo.calcReward();
            if (diamond <= 0) {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_220));
                return;
            }

            UserShare userShare = new UserShare(userId, UserShare.type_gold_room, now.getTime(), diamond, "diamond");
            UserShareDao.getInstance().addUserShare(userShare);

            player.changeCards(diamond, 0, true, CardSourceType.activity_gold_room_share);

            MessageUtil.sendMessage(UserMessageEnum.TYPE0, player, "您在分享中获得:钻石x" + diamond, null);
            player.writeComMessage(WebSocketMsgType.res_code_com, "分享成功！恭喜您获得：钻石x" + diamond);

            LogUtil.msgLog.info("GoldRoomShareActivityCmd|" + player.getUserId() + "|" + player.getName() + "|" + diamond);
        }
    }

    /**
     * 玩家是否当天已分享
     *
     * @param player
     * @return
     */
    public static int isGoldRoomShared(Player player) {
        Calendar now = Calendar.getInstance();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(now.getTime());
        List<UserShare> list = UserShareDao.getInstance().getUserShare(player.getUserId(), currentDate, UserShare.type_gold_room);
        return (list != null && list.size() > 0) ? 1 : 0;
    }

    @Override
    public void setMsgTypeMap() {
    }
}
