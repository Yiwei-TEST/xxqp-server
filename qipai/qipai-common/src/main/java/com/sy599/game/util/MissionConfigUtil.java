package com.sy599.game.util;

import com.sy599.game.db.bean.Activity;
import com.sy599.game.db.bean.MissionConfig;
import com.sy599.game.db.bean.SevenSignConfig;
import com.sy599.game.db.dao.ActivityDao;
import com.sy599.game.db.dao.ResourcesConfigsDao;
import com.sy599.game.db.dao.TableCheckDao;
import com.sy599.game.db.enums.DbEnum;
import com.sy599.game.msg.serverPacket.ConfigMsg;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MissionConfigUtil {
    private static final Map<Integer, Integer> SEVEN_DAY_CONFIG = new ConcurrentHashMap<>();
    public static ConfigMsg.ConfigInfo.Builder sendConfig= ConfigMsg.ConfigInfo.newBuilder();
    public static Map<Integer, List<MissionConfig>> tagMissionMap=new TreeMap<>();

    public static void init(){
        initSevenSign();
        initMission();
        addSomeConfig();
    }

    public static ConfigMsg.ConfigInfo getSendConfig() {
        return sendConfig.build();
    }

    public static Map<Integer, List<MissionConfig>> getTagMissionMap() {
        return tagMissionMap;
    }



    public static synchronized void initSevenSign(){
        try {
            List<SevenSignConfig> list = TableCheckDao.getInstance().checkTableExists(DbEnum.LOGIN,"seven_sign_config") ?
                    ResourcesConfigsDao.getInstance().loadSevenSignConfig() : null;
            if(list!=null){
                sendConfig.clearSign();
                for (SevenSignConfig config:list) {
                    ConfigMsg.SevenGoldConfigRes.Builder sevenConfig= ConfigMsg.SevenGoldConfigRes.newBuilder();
                    sevenConfig.setId(config.getId());
                    sevenConfig.setDayNum(config.getDayNum());
                    sevenConfig.setGoldNum(config.getGoldNum());
                    sendConfig.addSign(sevenConfig);
                    SEVEN_DAY_CONFIG.put(config.getDayNum(),config.getGoldNum());
                }
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
    }

    public static Map<Integer, Integer> getSevenDayConfig() {
        return SEVEN_DAY_CONFIG;
    }

    public static Map<Integer,List<Integer>> tagAndId=new HashMap<>();
    public static Map<Integer,List<Integer>> typeAndId=new HashMap<>();
    public static Map<Integer,MissionConfig> missionIdAndConfig=new HashMap<>();
    public static final int tag_dayMission=1;
    public static final int tag_challengeMission=2;
    //龙舟活动 活动已过期
    public static final int tag_activeLZ=101;
    //鹊桥活动
    public static final int tag_activeQueQiao=105;
    public static final List<Integer> show_mission= Arrays.asList(tag_dayMission,tag_challengeMission);



    public static final int type_signMission=1;//签到任务
    public static final int type_playMission=2;//普通金币对局任务
    public static final int type_shareMission=3;//分享任务
    public static final int type_challengeRoomId=4;//挑战赛对局任务

    public static Map<Integer, List<Integer>> getTagAndId() {
        return tagAndId;
    }

    //所有每日刷新的任務
    public static List<Integer> getDayMissionId(){
        List<Integer> dayMissionId = new ArrayList<>();
        if(tagAndId.containsKey(tag_dayMission))
            dayMissionId.addAll(tagAndId.get(tag_dayMission));
        if(tagAndId.containsKey(tag_activeQueQiao))
            dayMissionId.addAll(tagAndId.get(tag_activeQueQiao));//鹊桥活动也是每日重置
        return dayMissionId;
    }

    public static List<Integer> getShowMissionId(){
        List<Integer> showMissionId = new ArrayList<>();
        if(tagAndId.containsKey(tag_dayMission))
            showMissionId.addAll(tagAndId.get(tag_dayMission));
        return showMissionId;
    }

    public static Map<Integer, List<Integer>> getTypeAndId() {
        return typeAndId;
    }

    public static List<Integer> getSignMissionId(){
        List<Integer> list = typeAndId.get(type_signMission);
        if(list==null)
            list=new ArrayList<>();
        return list;
    }

    public static List<Integer> getPlayMissionId(){
        List<Integer> list = typeAndId.get(type_playMission);
        if(list==null)
            list=new ArrayList<>();
        return list;
    }

    public static List<Integer> getShareMissionId(){
        List<Integer> list=typeAndId.get(type_shareMission);
        if(list==null)
            list=new ArrayList<>();
        return list;
    }

    public static List<Integer> getChallengeRoomId(){
        List<Integer> list=typeAndId.get(type_challengeRoomId);
        if(list==null)
            list=new ArrayList<>();
        return list;
    }

    public static List<Integer> getQueQiaoId(){
        List<Integer> list=tagAndId.get(tag_activeQueQiao);
        if(list==null)
            list=new ArrayList<>();
        return list;
    }

    public static Integer queQiaoSpecialId=null;
    public static Integer getQueQiaoSpecialId(){
        if(queQiaoSpecialId!=null)
            return queQiaoSpecialId;
        List<MissionConfig> configs = tagMissionMap.get(tag_activeQueQiao);
        for (MissionConfig mc:configs) {
            if(mc.getExt()!=null&&mc.getExt()!="")
                queQiaoSpecialId=mc.getId();
        }
        return queQiaoSpecialId;
    }

    public static Map<Integer, MissionConfig> getMissionIdAndConfig() {
        return missionIdAndConfig;
    }

    public static synchronized void initMission(){
        try {
            List<MissionConfig> list = TableCheckDao.getInstance().checkTableExists(DbEnum.LOGIN,"mission_config") ?
                    ResourcesConfigsDao.getInstance().loadMissionConfig() : null;
            if(list!=null){
                tagMissionMap.clear();
                for (MissionConfig config:list){
                    addTagId(config);
                    addTypeId(config);
                    missionIdAndConfig.put(config.getId(),config);
                    List<MissionConfig> l = tagMissionMap.get(config.getTag());
                    if (l==null){
                        l = new ArrayList<>();
                        tagMissionMap.put(config.getTag(),l);
                    }
                    l.add(config);
                }
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
    }

    private static void addTagId(MissionConfig config){
        List<Integer> list = tagAndId.get(config.getTag());
        if(list==null){
            list=new ArrayList<>();
            tagAndId.put(config.getTag(),list);
        }
        if(!list.contains(config.getId()))
            list.add(config.getId());
    }

    private static void addTypeId(MissionConfig config){
        if(!isUsingWithActive(config))
            return;
        List<Integer> list = typeAndId.get(config.getType());
        if(list==null){
            list=new ArrayList<>();
            typeAndId.put(config.getType(),list);
        }
        if(!list.contains(config.getId()))
            list.add(config.getId());
    }
    private static boolean isUsingWithActive(MissionConfig config){
        List<Activity> activityByThem = ActivityUtil.getActivityByThem(config.getTag());
        if(activityByThem.size()==0)
            return true;
        if(activityByThem.get(0).isUsing())
            return true;
        return false;
    }


    /**
     * 此配置暂时写死
     */
    public static void addSomeConfig(){
        try {
            ConfigMsg.AloneConfigRes.Builder builder = ConfigMsg.AloneConfigRes.newBuilder();
            builder.setShare(600);//分享领豆
            builder.setBroke(1500);//破产领豆
            builder.setBrokeShare(500);//分享破产领豆
            builder.setBingDingNum(3000);//绑定成功领豆
            builder.setBrokeTigger(1500);//破产触发下限
            builder.setVideoWatchNum(5);//视频奖励次数
            builder.setVideoAwardNum(1000);//视频奖励数量
            sendConfig.setAloneConfig(builder);
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
    }


}
