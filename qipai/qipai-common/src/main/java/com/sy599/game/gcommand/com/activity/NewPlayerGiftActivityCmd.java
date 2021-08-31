package com.sy599.game.gcommand.com.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.RedBagSourceType;
import com.sy599.game.db.bean.RedBagInfo;
import com.sy599.game.db.bean.RedBagReceiveRecord;
import com.sy599.game.db.bean.activityRecord.UserActivityRecord;
import com.sy599.game.db.dao.RedBagInfoDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.dao.group.GroupDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.gcommand.com.ActivityCommand;
import com.sy599.game.manager.RedBagManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.ComMsg.ComReq;
import com.sy599.game.staticdata.bean.ActivityConfig;
import com.sy599.game.staticdata.bean.NewPlayerGiftActivityConfig;
import com.sy599.game.staticdata.model.NewPlayerGiftActivityBean;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang.time.DateUtils;

/**
 * 新人有礼活动
 */
public class NewPlayerGiftActivityCmd extends BaseCommand
{
    @Override
    public void execute(Player player, MessageUnit message) throws Exception
    {
        ComReq req = (ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<Integer> reqParams = req.getParamsList();   //请求参数集
        int requestType = reqParams.get(0);    //0 打开 1领取

        NewPlayerGiftActivityConfig activityConfig = (NewPlayerGiftActivityConfig) ActivityConfig.getActivityConfigInfo(ActivityConfig.activity_new_player_gift);
        UserActivityRecord userActivityRecord = player.getMyActivity().getUserActivityRecord();
        int activityDay = activityConfig.getActivityDay();  //活动时间

        int newPGLiv = getPlayerLiveness(player, activityConfig); //获取玩家活跃度

        List<NewPlayerGiftActivityBean> rewardDiam = activityConfig.getRewardDiam();  //奖励配置
        List<Integer> newPlayerGiftRecord = userActivityRecord.getNewPlayerGiftRecord();  //奖励领取记录
        JSONObject userJsonInfo = new JSONObject();
        if(requestType == 1)
        {
            int rewardIndex = reqParams.get(1);  //要领取的奖励索引
            userJsonInfo.put("rewardIndex", rewardIndex);
            if(rewardIndex >= rewardDiam.size() || rewardIndex < 0)  //参数错误
            {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
                return;
            }
            if(newPlayerGiftRecord.contains(rewardIndex))  //已领取
            {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_219));
                return;
            }
            NewPlayerGiftActivityBean rewardBean  = rewardDiam.get(rewardIndex); //要领取的奖励
            if(newPGLiv < rewardBean.getLiveness())  //条件不足
            {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_220));
                return;
            }
            LogUtil.msgLog.info(player.getName() + "玩家领取新人有礼奖励*类型" + rewardBean.getRewardType() + " rewardIndex" + rewardIndex+" 奖励" + rewardBean.getReward());

            int redbagType = rewardBean.getRewardType();  //红包类型
            int redbag = rewardBean.getReward();   //红包金额
            if(redbagType == 1)  //1钻石
            {
                player.changeCards(redbag, 0, true, -1, CardSourceType.activity_newPlayerGift);  //领取奖励
                player.writeComMessage(WebSocketMsgType.res_code_com, "恭喜您获得：钻石x" + redbag);
                LogUtil.msgLog.info(player.getName() + "恭喜您获得：钻石x" + redbag);
            }
            else if(redbagType == 2)  //2现金
            {
                RedBagManager.getInstance().addRedBagReceiveRecord(new RedBagReceiveRecord(player.getName(), redbag));  //记录跑马灯
            }
            userJsonInfo.put("rewardType",redbagType);
            userJsonInfo.put("reward",redbag);
            RedBagInfo redBagInfo = new RedBagInfo(player.getUserId(), redbagType, redbag, new Date(), null, RedBagSourceType.newPlayerGift_redbag);
            RedBagInfoDao.getInstance().saveRedBagInfo(redBagInfo);
            userActivityRecord.addNewPlayerGiftRecord(rewardIndex);   //添加奖励领取记录
            player.getMyActivity().updateActivityRecord(userActivityRecord, true);  //更新玩家活动记录信息
        }

        List<Integer> recordState = new ArrayList<>();
        int i = 0;
        for(NewPlayerGiftActivityBean bean : rewardDiam)
        {
            if(newPGLiv < bean.getLiveness())
            {
                recordState.add(0);  //不能领
            }
            else if(newPlayerGiftRecord.contains(i))
            {
                recordState.add(1);  //已领取
            }
            else
            {
                recordState.add(2);  //可领取
            }
            i++;
        }

        Date reginTime = player.getReginTime();  //玩家注册时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd");
        String todayZeroTimeStr = dataFormat.format(reginTime) + " 00:00:00";
        long reginZeroTime = sdf.parse(todayZeroTimeStr).getTime();  //注册当天零时
        long countdownLong = (reginZeroTime + activityDay * DateUtils.MILLIS_PER_DAY) - System.currentTimeMillis(); //注册时间 + 活动时间 - 现在时间
        String activeResTime = "";
        int day = (int) (countdownLong / DateUtils.MILLIS_PER_DAY);
        int h = (int) ((countdownLong % DateUtils.MILLIS_PER_DAY) /  DateUtils.MILLIS_PER_HOUR);
        int m = (int) (((countdownLong % DateUtils.MILLIS_PER_DAY) %  DateUtils.MILLIS_PER_HOUR) / DateUtils.MILLIS_PER_MINUTE);
        activeResTime += day+"天"+h+"小时"+m+"分钟";

        Map<Integer,Integer> conditions = activityConfig.getReachConditions();  //活动任务要求配置
        Map<String, Integer> needNum = new HashMap<>();
        for(Map.Entry<Integer, Integer> num : conditions.entrySet())
        {
            needNum.put(num.getKey()+"",num.getValue());
        }
        Map<Integer,Long> newPGTNum = userActivityRecord.getNewPlayerGiftTasksIndex(0); //任务完成次数表
        Map<String, Long> PGTNum = new HashMap<>();
       for(Map.Entry<Integer,Long> num : newPGTNum.entrySet())
        {
            if(num.getKey() == NewPlayerGiftActivityConfig.type_game_count && (num.getValue() > activityConfig.getReachConditions().get(NewPlayerGiftActivityConfig.type_game_count)))
            {
                long maxCount = activityConfig.getReachConditions().get(NewPlayerGiftActivityConfig.type_game_count);
                PGTNum.put(num.getKey()+"",maxCount);
            }
            else if(num.getKey() == NewPlayerGiftActivityConfig.type_big_win && (num.getValue() > activityConfig.getReachConditions().get(NewPlayerGiftActivityConfig.type_big_win)))
            {
                long maxCount = activityConfig.getReachConditions().get(NewPlayerGiftActivityConfig.type_big_win);
                PGTNum.put(num.getKey()+"",maxCount);
            }
            else
            {
                PGTNum.put(num.getKey()+"",num.getValue());
            }
        }
        Map<Integer,Integer> livenessReward = activityConfig.getLivenessReward();  //活动任务要求配置
        Map<String, Integer> livReward = new HashMap<>();
        for(Map.Entry<Integer, Integer> num : livenessReward.entrySet())
        {
            livReward.put(num.getKey()+"",num.getValue());
        }
        userJsonInfo.put("livReward", livReward); //活跃度奖励配置
        userJsonInfo.put("needNum", needNum);  //任务需要完成次数
        userJsonInfo.put("newPGTNum",PGTNum);  //任务完成次数表
        userJsonInfo.put("newPGLiv",newPGLiv);  //活跃度
        userJsonInfo.put("activeResTime", activeResTime);   //活动剩余时间
        userJsonInfo.put("rewardDiam",rewardDiam);  //奖励配置
        userJsonInfo.put("recordState",recordState); //奖励状态集
        userJsonInfo.put("requestType",requestType);  //操作类型
        ActivityCommand.sendActivityInfo(player, activityConfig, userJsonInfo);  //发送 回归礼包 活动具体消息

    }

    //获取玩家活跃度
    private int getPlayerLiveness(Player player,NewPlayerGiftActivityConfig activityConfig) throws Exception
    {
        UserActivityRecord userActivityRecord = player.getMyActivity().getUserActivityRecord();
        Map<Integer,Long> newPGTNum = userActivityRecord.getNewPlayerGiftTasksIndex(0); //任务完成次数表
        Map<Integer,Long> newPGTTime = userActivityRecord.getNewPlayerGiftTasksIndex(2);  //活跃度领取时间表
        Map<Integer,Integer> conditions = activityConfig.getReachConditions();  //活动任务要求配置
        Map<Integer,Integer> livReward = activityConfig.getLivenessReward();  //任务活跃度奖励配置
        int newPGLiv = userActivityRecord.getNewPlayerGifLiv();  //玩家当前活跃度

        int isShared = ShareActivityCmd.isShared(player, "");
        if(isShared != 0) //每日分享完成
        {
            newPGTNum.put(NewPlayerGiftActivityConfig.type_daily_share, 1L);
            userActivityRecord.putNewPlayerGiftTasks(NewPlayerGiftActivityConfig.type_daily_share,0,1L);  //更新任务完成次数
            userActivityRecord.putNewPlayerGiftTasks(NewPlayerGiftActivityConfig.type_daily_share,1, System.currentTimeMillis());  //更新任务完成时间
        }else{
            newPGTNum.put(NewPlayerGiftActivityConfig.type_daily_share, 0L);
            userActivityRecord.putNewPlayerGiftTasks(NewPlayerGiftActivityConfig.type_daily_share,0,0L);  //重置分享任务完成次数
        }

        boolean mallTopUp = UserDao.getInstance().isFirstPay(player.getUserId(),1,9);  //是否第没有充值
        if(!mallTopUp)
        {
            newPGTNum.put(NewPlayerGiftActivityConfig.type_mall_topUp, 1L);
            userActivityRecord.putNewPlayerGiftTasks(NewPlayerGiftActivityConfig.type_mall_topUp,0,1L);
            userActivityRecord.putNewPlayerGiftTasks(NewPlayerGiftActivityConfig.type_mall_topUp,1, System.currentTimeMillis());
        }

        int joinFC = GroupDao.getInstance().loadGroupCount(player.getUserId());  //加入亲友圈数量
        if(joinFC >= 1)
        {
            newPGTNum.put(NewPlayerGiftActivityConfig.type_join_fc, 1L);
            userActivityRecord.putNewPlayerGiftTasks(NewPlayerGiftActivityConfig.type_join_fc,0,1L);
            userActivityRecord.putNewPlayerGiftTasks(NewPlayerGiftActivityConfig.type_join_fc,1, System.currentTimeMillis());
        }
        player.getMyActivity().updateActivityRecord(userActivityRecord, true);

        for(int taskId : conditions.keySet())  //活动任务要求配置【领取活跃度】
        {
            if(newPGTNum.containsKey(taskId) && newPGTNum.get(taskId) >= conditions.get(taskId))  //任务完成
            {
                if(taskId == NewPlayerGiftActivityConfig.type_game_count || taskId == NewPlayerGiftActivityConfig.type_daily_share)
                {
                    if(newPGTTime.get(taskId) == 0 || !TimeUtil.isSameDay(newPGTTime.get(taskId), System.currentTimeMillis()))  //该任务活跃度领取时间不是今天
                    {
                        newPGLiv += livReward.get(taskId);
                        userActivityRecord.putNewPlayerGiftTasks(taskId,2, System.currentTimeMillis());  //更新领取活跃度的时间
                    }
                }
                else
                {
                    if(newPGTTime.get(taskId) == 0)  //该任务活跃度领取时间为0
                    {
                        newPGLiv += livReward.get(taskId);  //领取活跃度
                        userActivityRecord.putNewPlayerGiftTasks(taskId,2, System.currentTimeMillis()); //更新领取活跃度的时间
                    }
                }
            }
        }
        userActivityRecord.setNewPlayerGifLiv(newPGLiv);  //更新玩家活跃度
        player.getMyActivity().updateActivityRecord(userActivityRecord, true);  //更新玩家活动记录信息
        return newPGLiv;
    }

    //每日牌局任务
    public static void updateMatchNum(Player player)
    {
        if(ActivityConfig.isActivityOpen(player, ActivityConfig.activity_new_player_gift))  //新人礼包奖活动开启
        {
            UserActivityRecord userActivityRecord = player.getMyActivity().getUserActivityRecord();
            Long dayBureau = userActivityRecord.getNewPlayerGiftTasksLongArray(NewPlayerGiftActivityConfig.type_game_count)[0]; //最新完成大局次数
            Long time = userActivityRecord.getNewPlayerGiftTasksLongArray(NewPlayerGiftActivityConfig.type_game_count)[1];  //最新完成大局时间
            if(TimeUtil.isSameDay(time, System.currentTimeMillis()))
            {
                userActivityRecord.putNewPlayerGiftTasks(NewPlayerGiftActivityConfig.type_game_count,0,dayBureau+1);
            }
            else
            {
                userActivityRecord.putNewPlayerGiftTasks(NewPlayerGiftActivityConfig.type_game_count,0,1L);  //重置新一天大局数
            }
            userActivityRecord.putNewPlayerGiftTasks(NewPlayerGiftActivityConfig.type_game_count, 1, System.currentTimeMillis());  //更新任务完成时间
            player.getMyActivity().updateActivityRecord(userActivityRecord, true);  //更新玩家活动记录信息
        }
    }

    //大赢家次数任务
    public static void updateBigWinNum(Player player)
    {
        if(!player.isRobot() && ActivityConfig.isActivityOpen(player, ActivityConfig.activity_new_player_gift))  //新人礼包奖活动开启
        {
            UserActivityRecord userActivityRecord = player.getMyActivity().getUserActivityRecord();
            long bigWinCount = userActivityRecord.getNewPlayerGiftTasksLongArray(NewPlayerGiftActivityConfig.type_big_win)[0]; //大赢家次数
            userActivityRecord.putNewPlayerGiftTasks(NewPlayerGiftActivityConfig.type_big_win, 0,bigWinCount+1);  //更新大赢家次数
            userActivityRecord.putNewPlayerGiftTasks(NewPlayerGiftActivityConfig.type_big_win,1, System.currentTimeMillis());
            player.getMyActivity().updateActivityRecord(userActivityRecord, true);  //更新玩家活动记录信息
        }
    }

    //判断是否开启活动【新注册玩家开启】
    public static boolean isNewRegPlayer(Player player)
    {
        if(player == null)
        {
            return false;
        }
        try{
            NewPlayerGiftActivityConfig activityConfig = (NewPlayerGiftActivityConfig) ActivityConfig.getActivityConfigInfo(ActivityConfig.activity_new_player_gift);
            UserActivityRecord userActivityRecord = player.getMyActivity().getUserActivityRecord();
            Map<Integer,Long> newPGTTime = userActivityRecord.getNewPlayerGiftTasksIndex(1); //任务完成时间表
            Date startTime = activityConfig.getStartTime();  //活动开启时间
            Date reginTime = player.getReginTime();  //玩家注册时间
            int activityDay = activityConfig.getActivityDay();  //活动天数
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd");
            String todayZeroTimeStr = dataFormat.format(reginTime) + " 00:00:00";
            long reginZeroTime = sdf.parse(todayZeroTimeStr).getTime();   //注册当天零时时间

            //注册时间  >= 活动开启时间 且 当前时间在活动时间内
            if(reginTime.getTime() >= startTime.getTime() && (System.currentTimeMillis() - reginZeroTime <= activityDay * DateUtils.MILLIS_PER_DAY))
            {
                long gameTime = newPGTTime.get(NewPlayerGiftActivityConfig.type_game_count);  //最新每日牌局完成时间
                long shareTime = newPGTTime.get(NewPlayerGiftActivityConfig.type_daily_share);  //最新每日首次分享完成时间
                if(!TimeUtil.isSameDay(gameTime, System.currentTimeMillis()))
                {
                    userActivityRecord.putNewPlayerGiftTasks(NewPlayerGiftActivityConfig.type_game_count,0,0L);  //清空完成牌局
                }
                if(!TimeUtil.isSameDay(shareTime, System.currentTimeMillis()))
                {
                    userActivityRecord.putNewPlayerGiftTasks(NewPlayerGiftActivityConfig.type_daily_share,0,0L);  //清空每日分享
                }
                player.getMyActivity().updateActivityRecord(userActivityRecord, true);  //更新玩家活动记录信息
                return true;
            }
        }
        catch (Exception e)
        {
            LogUtil.errorLog.info(e.getMessage(), e);
        }
        return false;
    }

}
