package com.sy599.game.staticdata.bean;

/**
 * 任务配置信息
 */
public class TaskConfigInfo {

    /**
     * 任务ID
     */
    private int taskId;

    /**
     * 任务类型
     */
    private int taskType;

    /**
     * 任务达成条件
     */
    private String param;

    /**
     * 达成任务奖励
     */
    private String rewardParam;

    /**
     * 任务描述
     */
    private String taskDesc;

    /**
     * 任务奖励描述
     */
    private String rewardDesc;

    public TaskConfigInfo(){
    }

    public TaskConfigInfo(int taskId, int taskType, String param, String rewardParam, String taskDesc, String rewardDesc) {
        this.taskId = taskId;
        this.taskType = taskType;
        this.param = param;
        this.rewardParam = rewardParam;
        this.taskDesc = taskDesc;
        this.rewardDesc = rewardDesc;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getTaskType() {
        return taskType;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getRewardParam() {
        return rewardParam;
    }

    public void setRewardParam(String rewardParam) {
        this.rewardParam = rewardParam;
    }

    public String getTaskDesc() {
        return taskDesc;
    }

    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
    }

    public String getRewardDesc() {
        return rewardDesc;
    }

    public void setRewardDesc(String rewardDesc) {
        this.rewardDesc = rewardDesc;
    }
}
