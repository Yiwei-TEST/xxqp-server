package com.sy599.game.gcommand.com.activity;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.activity.MyActivity;
import com.sy599.game.character.Player;
import com.sy599.game.common.constant.LangMsg;
import com.sy599.game.common.constant.RedBagSourceType;
import com.sy599.game.db.bean.RedBagInfo;
import com.sy599.game.db.bean.RedBagReceiveRecord;
import com.sy599.game.db.bean.activityRecord.ActivityReward;
import com.sy599.game.db.bean.activityRecord.UserActivityRecord;
import com.sy599.game.db.dao.ActivityDao;
import com.sy599.game.db.dao.RedBagInfoDao;
import com.sy599.game.db.dao.UserDao;
import com.sy599.game.db.enums.CardSourceType;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.gcommand.com.ActivityCommand;
import com.sy599.game.manager.RedBagManager;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.staticdata.bean.ActivityConfig;
import com.sy599.game.staticdata.bean.OldBackGiftActivityConfig;
import com.sy599.game.staticdata.model.OldBackGiftActivityBean;
import com.sy599.game.util.LangHelp;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.TimeUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;
import org.apache.commons.lang.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 老玩家回归礼包
 */
public class OldBackGiftActivityCmd extends BaseCommand
{

    public void execute(Player player, MessageUnit message) throws Exception
    {
        ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
        List<Integer> reqParams = req.getParamsList();   //请求参数集
        int requestType = reqParams.get(0);  //params参数第0位 表示请求的操作类型 0打开 1领取

        //获取回归礼包活动配置信息
        OldBackGiftActivityConfig activityConfig = (OldBackGiftActivityConfig) ActivityConfig.getActivityConfigInfo(ActivityConfig.activity_old_back_gift);
        int dayMax = activityConfig.getDayMax();   //最大有奖登录天数
        List<OldBackGiftActivityBean> rewardDiam= activityConfig.getRewardDiam();  //奖励配置集
        int continuousDay = activityConfig.getContinuousDay();  //活动持续天数

        MyActivity myActivity = player.getMyActivity();  //该玩家的活动
        UserActivityRecord record = myActivity.getUserActivityRecord();  //该玩家活动的记录
        int loginCount = record.getOldBackGifLoginCount();   //活动期间玩家登陆天数
        long lastLoginTime = record.getOldBackGiftLoginTime();  //最新登陆时间
        long time = record.getOldBackGifTime();   //玩家回归时间
        Map<Integer, Long> obgrs = record.getOldBackGifRecord();  //领取表

        if((System.currentTimeMillis() - time) > continuousDay *  DateUtils.MILLIS_PER_DAY){  //当前时间超过回归活动时间
            return;
        }
        //若在活动时间内 且最新登陆时间不是当天
        if((System.currentTimeMillis() - time <= continuousDay *  DateUtils.MILLIS_PER_DAY) && (!TimeUtil.isSameDay(lastLoginTime, System.currentTimeMillis()))) //
        {
            loginCount++;
            record.setOldBackGifLoginCount(loginCount);  //更新登录天数
            record.setOldBackGiftLoginTime(System.currentTimeMillis());  //更新活动期间最新登陆时间
            myActivity.updateActivityRecord(record, true);  //更新玩家活动记录信息
         }

        JSONObject userJsonInfo = new JSONObject();
        boolean recordBoo = false;  //当天是否已领取
        Collection<Long> recordTimes = obgrs.values();
        for(Long recordTime : recordTimes)
        {
            if(TimeUtil.isSameDay(recordTime, System.currentTimeMillis()))
            {
                recordBoo = true;  //当天已领取
            }
        }

        if(requestType == 1)
        {
            int rewardIndex = reqParams.get(1);  //领取奖励索引 从0开始
            if(rewardIndex >= rewardDiam.size() || rewardIndex < 0)
            {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_3));
                return;
            }

            if(obgrs.keySet().contains(rewardIndex))  //已领取
            {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_219));
                return;
            }
            //获取奖励配置表指定索引位置的奖励
            OldBackGiftActivityBean oldBackGiftActivityBean = rewardDiam.get(rewardIndex);

            int index = 0;  //请求领取索引
            for(int i = 0; i < rewardDiam.size(); i++)
            {
                if(!obgrs.keySet().contains(i))
                {
                    index = i;
                    break;
                }
            }

            if(loginCount < oldBackGiftActivityBean.getDayCount() || rewardIndex != index)  //条件不足
            {
                player.writeErrMsg(LangHelp.getMsg(LangMsg.code_220));
                return;
            }

            if(!recordBoo)
            {
                LogUtil.msgLog.info(player.getName() + "玩家领取回归礼包奖励*类型" + oldBackGiftActivityBean.getRewardType() + " rewardIndex" + rewardIndex+" 奖励" + oldBackGiftActivityBean.getReward());
                if(oldBackGiftActivityBean.getRewardType() == 1)  //1钻石
                {
                    player.changeCards(oldBackGiftActivityBean.getReward(), 0, true, -1, CardSourceType.activity_oldBackGift);  //领取奖励
                    userJsonInfo.put("reward",oldBackGiftActivityBean.getReward());
                    userJsonInfo.put("rewardType", oldBackGiftActivityBean.getRewardType());
                    RedBagInfo redBagInfo = new RedBagInfo(player.getUserId(), 1, oldBackGiftActivityBean.getReward(), new Date(), null, RedBagSourceType.oldBackGift_redbag);
                    RedBagInfoDao.getInstance().saveRedBagInfo(redBagInfo);
                }
                else if(oldBackGiftActivityBean.getRewardType() == 2)  //2现金
                {
                    int grade = activityConfig.randomGrade();  //随机红包等级
                    float redbag = activityConfig.getGrades().get(grade)[2];  //对应等级的红包
                    userJsonInfo.put("reward",redbag+"");
                    userJsonInfo.put("rewardType", oldBackGiftActivityBean.getRewardType());
                    RedBagInfo redBagInfo = new RedBagInfo(player.getUserId(), 2, redbag, new Date(), null, RedBagSourceType.oldBackGift_redbag);
                    RedBagInfoDao.getInstance().saveRedBagInfo(redBagInfo);
                    RedBagManager.getInstance().addRedBagReceiveRecord(new RedBagReceiveRecord(player.getName(), redbag));  //记录跑马灯
                }
                record.putOldBackGifRecord(rewardIndex, System.currentTimeMillis());  //添加奖励领取记录
                myActivity.updateActivityRecord(record, true);  //更新玩家活动记录信息
            }
        }

        List<Integer> recordState = new ArrayList<>();  //非当天奖励状态集
        int dayState = 1;  //当天奖励状态
        int i = 0;
        for(OldBackGiftActivityBean bean : rewardDiam)  //遍历奖励配置集
        {
            if(loginCount == bean.getDayCount())  //是当天
            {
                if(record.getOldBackGifRecord().keySet().contains(i)) //已领取
                {
                    dayState = 0;  // 已领取
                }
                else
                {
                    dayState = 1;  //未领取
                }
            }

            if(bean.getDayCount() == dayMax)  //奖励是现金红包
            {
                if (record.getOldBackGifRecord().keySet().contains(i)) //已领取
                {
                    recordState.add(2);  // 红包已领取，待提现
                }
                else
                {
                    recordState.add(1);  //未领取
                }
            }
            else
            {
                if(record.getOldBackGifRecord().keySet().contains(i)) //已领取
                {
                    recordState.add(0);  // 已领取
                }
                else
                {
                    recordState.add(1);  //未领取
                }
            }
            i++;
        }

        long oldBackGifTime = record.getOldBackGifTime();  //回归时间
        long countdownLong = (oldBackGifTime + continuousDay * DateUtils.MILLIS_PER_DAY) - System.currentTimeMillis(); //回归时间 + 活动时间 - 现在时间
        String countdown = "";
        int day = (int) (countdownLong / DateUtils.MILLIS_PER_DAY);
        int h = (int) ((countdownLong % DateUtils.MILLIS_PER_DAY) /  DateUtils.MILLIS_PER_HOUR);
        int m = (int) (((countdownLong % DateUtils.MILLIS_PER_DAY) %  DateUtils.MILLIS_PER_HOUR) / DateUtils.MILLIS_PER_MINUTE);
        countdown += day+"天"+h+"小时"+m+"分钟";

        boolean isVolverPlayer = false;
        if(loginCount != 0 && (System.currentTimeMillis() - time) <= continuousDay *  DateUtils.MILLIS_PER_DAY)
        {
            isVolverPlayer = true;
        }
        if(obgrs.size() == dayMax || recordBoo)
        {
            dayState = 0;
        }

        List<RedBagInfo> redbags = RedBagInfoDao.getInstance().getUserRedBagInfosBySourceType(player.getUserId(), RedBagSourceType.oldBackGift_redbag.getType());
        for(RedBagInfo redInfo : redbags)
        {
            if(redInfo.getRedBagType() == 2 && (redInfo.getReceiveDate().getTime() >time && redInfo.getReceiveDate().getTime()<= System.currentTimeMillis() ))
            {
                float redBagReward = redInfo.getRedbag();
                userJsonInfo.put("redBagReward",redBagReward);
            }
        }
        userJsonInfo.put("oldBackGifTime",oldBackGifTime);  //回归时间
        userJsonInfo.put("isVolverPlayer", isVolverPlayer); //是否是回归玩家
        userJsonInfo.put("loginDay", loginCount);  //活动期间登录天数
        userJsonInfo.put("rewardDiam", rewardDiam);  //奖励配置
        userJsonInfo.put("recordState", recordState);  //非当天奖励状态集
        userJsonInfo.put("dayState", dayState);  //当天奖励状态
        userJsonInfo.put("countdown", countdown);  //活动倒计时
        userJsonInfo.put("requestType", requestType);  //操作方式
        ActivityCommand.sendActivityInfo(player, activityConfig, userJsonInfo);  //发送 回归礼包 活动具体消息

    }

    //15天前登录的用户且打满20小局
    public static boolean isOpenActivity(Player player)
    {
        OldBackGiftActivityConfig activityConfig = (OldBackGiftActivityConfig) ActivityConfig.getActivityConfigInfo(ActivityConfig.activity_old_back_gift);
        if(player == null)
        {
            return false;
        }
        int totalCount = player.getTotalCount();
        int unLofinDay = activityConfig.getUnLofinDay();  //需要注销天数
        Date logoutTime = player.getLogoutTime();
        UserActivityRecord record = player.getMyActivity().getUserActivityRecord();  //该玩家活动的记录
        long lastLoginTime = record.getOldBackGiftLoginTime();  //老玩家最新登陆时间

        long time = record.getOldBackGifTime();   //玩家回归时间
        int continuousDay = activityConfig.getContinuousDay();  //活动持续天数
        int payCount = activityConfig.getPayCount();  //要求对局数

        if(((logoutTime != null && (System.currentTimeMillis() - logoutTime.getTime() >= unLofinDay * DateUtils.MILLIS_PER_DAY)) && totalCount >= payCount) || lastLoginTime > 0)
        {
            if((logoutTime != null && (System.currentTimeMillis() - logoutTime.getTime() >= unLofinDay * DateUtils.MILLIS_PER_DAY)) && totalCount >= payCount)  //刚回归
            {
                try{
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String todayZeroTimeStr = dataFormat.format(new Date()) + " 00:00:00";
                    Date todayZeroTime = sdf.parse(todayZeroTimeStr);   //今天零时时间
                    long todayTime = todayZeroTime.getTime();
                    if(!TimeUtil.isSameDay(time, System.currentTimeMillis()))  //回归时间不是今天
                    {
                        Map<Integer, Long> oldBackGifRecord = new HashMap<>();
                        record.setOldBackGifTime(todayTime); //重置回归时间
                        record.setOldBackGifRecord(oldBackGifRecord);  //重置领奖记录
                    }
                    record.setOldBackGifLoginCount(1);  //重置登陆天数
                    record.setOldBackGiftLoginTime(System.currentTimeMillis());  //重置活动期间最新登陆时间
                    player.getMyActivity().updateActivityRecord(record, true);  //更新玩家活动记录信息
                }
                catch (Exception e)
                {
                    LogUtil.errorLog.info(e.getMessage(), e);
                }
                return true;
            }
            else   //非刚回归
            {
                if((System.currentTimeMillis() - time) > continuousDay *  DateUtils.MILLIS_PER_DAY)
                {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void setMsgTypeMap() {

    }
}
