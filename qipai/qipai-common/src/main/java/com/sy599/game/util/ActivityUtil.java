package com.sy599.game.util;

import com.sy599.game.db.bean.Activity;
import com.sy599.game.db.bean.gold.GoldAcitivityRankResult;
import com.sy599.game.db.dao.ActivityDao;
import com.sy599.game.db.dao.TableCheckDao;
import com.sy599.game.db.enums.DbEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActivityUtil {
    private static Map<String,List<Activity>> activityMap=new ConcurrentHashMap<>();
    private static Map<String,List<Activity>> allActivityMap=new ConcurrentHashMap<>();
    public static final int themLZ=101;//龙舟活动
    public static final int themZongZi=102;//欢乐龙舟攒粽子
    public static final int themGiftCertificate=103;//金币场礼券活动-长期
    public static final int themGooldRoomWatchAdsReword=104;//结算时输赢白金豆>1000可观看活动奖励 -长期
    public static final int themQueQiao=105;//鹊桥活动
    public static volatile long DuanWu_GoldRoomActivityLastQueryTime = 0l;
    public static volatile List<GoldAcitivityRankResult> DuanWu_GoldRoomActivityRankList = null;
    public static volatile long GoldRoom7xiActivityLastQueryTime = 0l;
    public static volatile List<GoldAcitivityRankResult> GoldRoom7xiActivityRankList = null;


    public static final int them7xi=106;//2020 七夕活动





    public static void init(){
        initActive();
    }

    public static synchronized void initActive(){
        try {
            List<Activity> activity = TableCheckDao.getInstance().checkTableExists(DbEnum.LOGIN,"activity") ?
                    ActivityDao.getInstance().loadActiveConfig(): null;
            if(activity!=null) {
                for (Activity atv:activity) {
                    if(atv.sysUse()){
                        List<Activity> activities = activityMap.get(atv.getThem());
                        if(activities==null){
                            activities=new ArrayList<>();
                            activityMap.put(atv.getThem(),activities);
                        }
                        activities.add(atv);
                    }
                    List<Activity> as = allActivityMap.get(atv.getThem());
                    if(as==null){
                        as=new ArrayList<>();
                        allActivityMap.put(atv.getThem(),as);
                    }
                    as.add(atv);
                }
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
        }
    }

    public static List<Activity> getActivityByThem(int them) {
        List<Activity> activities = activityMap.get(them + "");
        return activities==null?new ArrayList<>():activities;
    }

    public static List<Activity> getAllActivityByThem(int them) {
        List<Activity> activities = allActivityMap.get(them + "");
        return activities==null?new ArrayList<>():activities;
    }

    public static long getDuanWu_GoldRoomActivityLastQueryTime() {
        return DuanWu_GoldRoomActivityLastQueryTime;
    }

    public static void setDuanWu_GoldRoomActivityLastQueryTime(long duanWu_GoldRoomActivityLastQueryTime) {
        DuanWu_GoldRoomActivityLastQueryTime = duanWu_GoldRoomActivityLastQueryTime;
    }

    public static List<GoldAcitivityRankResult> getDuanWu_GoldRoomActivityRankList() {
        return DuanWu_GoldRoomActivityRankList;
    }

    public static void setDuanWu_GoldRoomActivityRankList( List<GoldAcitivityRankResult> duanWu_GoldRoomActivityRankList) {
        DuanWu_GoldRoomActivityRankList = duanWu_GoldRoomActivityRankList;
    }

    public static List<Activity> getQueQiaoActive() {
        List<Activity> activities=new ArrayList<>();
        List<Activity> list1 = activityMap.get(""+themQueQiao);
        if(list1!=null)
            activities.addAll(list1);
        return activities;
    }


    public static long getGoldRoom7xiActivityLastQueryTime() {
        return GoldRoom7xiActivityLastQueryTime;
    }

    public static void setGoldRoom7xiActivityLastQueryTime(long goldRoom7xiActivityLastQueryTime) {
        GoldRoom7xiActivityLastQueryTime = goldRoom7xiActivityLastQueryTime;
    }

    public static List<GoldAcitivityRankResult> getGoldRoom7xiActivityRankList() {
        return GoldRoom7xiActivityRankList;
    }

    public static void setGoldRoom7xiActivityRankList(List<GoldAcitivityRankResult> goldRoom7xiActivityRankList) {
        GoldRoom7xiActivityRankList = goldRoom7xiActivityRankList;
    }
}
