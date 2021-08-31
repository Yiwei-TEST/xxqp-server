package com.sy599.game.db.bean.task;

import com.sy599.game.msg.serverPacket.TaskMsg;
import com.sy599.game.staticdata.bean.ActivityConfigInfo;
import com.sy599.game.staticdata.bean.TaskConfig;

public class TaskInfo implements Comparable<TaskInfo>{

    private int taskId;

    private int taskType;

    private String param;

    private String rewardParam;

    private String taskDesc;

    private String rewardDesc;

    private int state;

    private int process;

    public TaskInfo(int taskId, int taskType, String param, String rewardParam, String taskDesc, String rewardDesc, int state, int process) {
        this.taskId = taskId;
        this.taskType = taskType;
        this.param = param;
        this.rewardParam = rewardParam;
        this.taskDesc = taskDesc;
        this.rewardDesc = rewardDesc;
        this.state = state;
        this.process = process;
    }

    public TaskMsg.TaskInfo.Builder getTaskInfoBuilder() {
        TaskMsg.TaskInfo.Builder info = TaskMsg.TaskInfo.newBuilder();
        info.setTaskId(taskId);
        info.setTaskType(taskType);
        info.setParam(param);
        info.setRewardParam(rewardParam);
        info.setTaskDesc(taskDesc);
        info.setRewardDesc(rewardDesc);
        info.setState(state);
        info.setProcess(process);
        return info;
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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getProcess() {
        return process;
    }

    public void setProcess(int process) {
        this.process = process;
    }

    @Override
    public int compareTo(TaskInfo o) {
        if(this.state > o.state)
            return 1;
        else {
            if(this.taskId < o.taskId)
                return 1;
            else
                return -1;
        }
    }
}
