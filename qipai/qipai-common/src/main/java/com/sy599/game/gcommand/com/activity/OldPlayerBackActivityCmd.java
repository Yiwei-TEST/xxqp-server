package com.sy599.game.gcommand.com.activity;

import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.db.enums.UserMessageEnum;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.message.MessageUtil;
import com.sy599.game.staticdata.bean.ActivityConfig;
import com.sy599.game.staticdata.bean.CommonAcitivityConfig;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang.time.DateUtils;

public class OldPlayerBackActivityCmd extends BaseCommand {

    /**
     * 老玩家回归奖励
     * @param player
     */
    public static void oldPlayerBackReward(Player player) {
        try {
            CommonAcitivityConfig activityConfig = (CommonAcitivityConfig) ActivityConfig.getActivityConfigInfo(ActivityConfig.activity_old_player_back);
            if (activityConfig!=null){
                int unLoginDay = Integer.parseInt(activityConfig.getParams());
                int rewardDiam = Integer.parseInt(activityConfig.getRewards());
                if (ActivityConfig.isActivityOpen(ActivityConfig.activity_old_player_back) && player.getLogoutTime() != null && (System.currentTimeMillis() - player.getLogoutTime().getTime() >= unLoginDay * DateUtils.MILLIS_PER_DAY)) {
                    player.changeCards(rewardDiam, 0, true, CardSourceType.activity_oldPlayerBack);
                    LogUtil.msgLog.info(player.getName() + "登陆获得老玩家回归" + rewardDiam + "钻石奖励！");

                    String msg = LangMsg.getMsg(LangMsg.code_250, unLoginDay, rewardDiam);

                    MessageUtil.sendMessage(true,true, UserMessageEnum.TYPE0,player,msg,null);
                }
            }
        }catch(Exception e) {
            LogUtil.errorLog.info("老玩家回归奖励接口异常：" + e.getMessage(),e);
        }
    }

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {

    }

    @Override
    public void setMsgTypeMap() {
    }
}
