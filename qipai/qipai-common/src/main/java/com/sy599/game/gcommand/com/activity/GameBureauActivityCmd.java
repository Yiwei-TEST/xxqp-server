package com.sy599.game.gcommand.com.activity;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.RedBagSourceType;
import com.sy599.game.db.bean.RedBagInfo;
import com.sy599.game.db.bean.UserGameBureau;
import com.sy599.game.db.dao.RedBagInfoDao;
import com.sy599.game.db.dao.UserGameBureauDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.gcommand.com.ActivityCommand;
import com.sy599.game.staticdata.bean.ActivityConfig;
import com.sy599.game.staticdata.bean.GameBureauActivityConfig;
import com.sy599.game.staticdata.model.RewardBean;
import com.sy599.game.util.LogUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 玩家局数统计活动
 */
public class GameBureauActivityCmd extends BaseCommand {

    @Override
    public void execute(Player player, MessageUnit message) throws Exception {
        GameBureauActivityConfig config = (GameBureauActivityConfig) ActivityConfig.getActivityConfigInfo(ActivityConfig.activity_game_bureau);
        List<RewardBean> rewardBeans = config.getRewardBeans();

        UserGameBureau userData = UserGameBureauDao.getInstance().loadOneUserGameBureau(player.getUserId());
        int finishCount = userData != null ? userData.getNumber() : 0;

        List<RedBagInfo> redBagList = RedBagInfoDao.getInstance().getUserRedBagInfosBySourceType(player.getUserId(), RedBagSourceType.gameBureauStaticActivity.getType());
        List<Float> givedList = new ArrayList<>();
        List<Float> receivedList = new ArrayList<>();
        if (redBagList != null && redBagList.size() > 0) {
            for (RedBagInfo redBag : redBagList) {
                givedList.add(redBag.getRedbag());
                if (redBag.getDrawDate() != null) {
                    receivedList.add(redBag.getRedbag());
                }
            }
        }
        List<Float> receivedCopy = new ArrayList<>(receivedList);
        List<RewardBean> dataList = new ArrayList<>();
        for (RewardBean rewardBean : rewardBeans) {
            RewardBean data = new RewardBean(rewardBean.getData(), rewardBean.getType(), rewardBean.getValue());
            if (finishCount >= rewardBean.getData()) {
                //达到要求
                if (rewardBean.getType() == 2) {
                    //红包
                    if (givedList.contains(rewardBean.getValue())) {
                        //已发放红包
                        if (receivedCopy.contains(rewardBean.getValue())) {
                            data.setStatus(2);
                            receivedCopy.remove(rewardBean.getValue());
                        } else {
                            data.setStatus(1);
                        }
                    } else {
                        //给玩家发红包奖励
                        data.setStatus(1);
                        RedBagInfo redBagInfo = new RedBagInfo(player.getUserId(), rewardBean.getType(), rewardBean.getValue(), new Date(), null, RedBagSourceType.gameBureauStaticActivity);
                        RedBagInfoDao.getInstance().saveRedBagInfo(redBagInfo);
                        LogUtil.msg("GameBureauActivityCmd|redBag|" + player.getUserId() + "|" + rewardBean.getData() + "|" + rewardBean.getType() + "|" + rewardBean.getValue());
                    }
                } else {
                    data.setStatus(1);
                }
            }
            data.setData(data.getData() / 10);
            dataList.add(data);
        }

        JSONObject json = new JSONObject();
        json.put("id", ActivityConfig.activity_game_bureau);
        json.put("startTime", config.getStartTime().getTime());
        json.put("endTime", config.getEndTime().getTime());
        json.put("activeEndTime", config.getActiveEndDate().getTime());
        json.put("fuCount", finishCount / 10);  //福数
        json.put("finishCount", finishCount); //局数
        json.put("dataList", dataList);
        ActivityCommand.sendActivityInfo(player, config, json);
    }

    @Override
    public void setMsgTypeMap() {
    }
}
