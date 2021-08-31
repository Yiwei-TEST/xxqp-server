package com.sy599.game.staticdata.bean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务系统配置
 */
public class TaskConfig {

    private static Map<Integer, Map<Integer, TaskConfigInfo>> taskTypeConfigMap = new ConcurrentHashMap<>();

    private static Map<Integer, TaskConfigInfo> taskConfigMap = new ConcurrentHashMap<>();

    /*------任务类型配置------*/
    /**
     * 所有任务
     */
    public static int task_all = 0;
    /**
     * 局数任务
     */
    public static int task_bureau = 1;
    /**
     * 财富任务
     */
    public static int task_fortune = 2;
    /**
     * 日常任务
     */
    public static int task_daily = 3;

    /*------日常任务ID标识----*/
    /**
     * 进行5局游戏
    */
    public static int task_daily_five_gold_game = 3001;
    /**
     * 进行10局游戏
     */
    public static int task_daily_ten_gold_game = 3002;
    /**
     * 完成连胜3把
     */
    public static int task_daily_3_win_streak = 3003;
    /**
     * 参加1场比赛
     */
    public static int task_daily_one_match = 3004;

    /*------任务状态标识------*/
    /**
     * 未达成0
     */
    public static int task_state_unReach = 0;
    /**
     * 可领取1
     */
    public static int task_state_canReceive = 1;
    /**
     * 已领取2
     */
    public static int task_state_received = 2;


    public static Map<Integer, TaskConfigInfo> getTaskConfigInfosByType(int taskType) {
        return taskTypeConfigMap.get(taskType);
    }

    public TaskConfigInfo getTaskConfigInfo(int taskId) {
        return taskConfigMap.get(taskId);
    }

    public static void initTaskConfigInfos(Map<Integer, TaskConfigInfo> map) {
        taskConfigMap = map;
        Map<Integer, Map<Integer, TaskConfigInfo>> typeMap = new ConcurrentHashMap<>();
        for(TaskConfigInfo info : map.values()) {
            if(typeMap.containsKey(info.getTaskType())) {
                Map<Integer, TaskConfigInfo> tempMap = typeMap.get(info.getTaskType());
                tempMap.put(info.getTaskId(), info);
            } else {
                Map<Integer, TaskConfigInfo> tempMap = new HashMap<>();
                tempMap.put(info.getTaskId(), info);
                typeMap.put(info.getTaskType(), tempMap);
            }
        }
        taskTypeConfigMap = typeMap;
    }

    public static Map<Integer, Map<Integer, TaskConfigInfo>> getTaskTypeConfigMap() {
        return taskTypeConfigMap;
    }

    public static Map<Integer, TaskConfigInfo> getTaskConfigMap() {
        return taskConfigMap;
    }
}
