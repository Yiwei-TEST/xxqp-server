package com.sy599.game.common.bean;

import com.sy599.game.util.MissionConfigUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

public class MissionState {
    private int id;
    private int progressBar;
    private boolean isComplete;
    private boolean isObtain;

    public MissionState() {
    }

    public MissionState(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setProgressBar(int progressBar) {
        this.progressBar = progressBar;
    }

    public int getProgressBar() {
        return progressBar;
    }

    public boolean addProgressBar() {
        int finishNum = MissionConfigUtil.getMissionIdAndConfig().get(id).getFinishNum();
        if(!isObtain&&progressBar<finishNum){
            this.progressBar++;
            if(progressBar>=finishNum){
                isComplete=true;
                return true;
            }
        }
        return false;
    }
    public boolean addSignProgressBar() {
        int finishNum = MissionConfigUtil.getMissionIdAndConfig().get(id).getFinishNum();
        if(!isObtain&&progressBar<finishNum)
            this.progressBar++;
        if(progressBar>=finishNum){
            isObtain=true;
            return true;
        }else
            return false;
    }


    public boolean receiveAward(){
        if(isComplete&&!isObtain){
            isObtain=true;
            return true;
        }else
            return false;
    }

    public boolean isObtain() {
        return isObtain;
    }

    public void setObtain(boolean obtain) {
        isObtain = obtain;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public String toStr(){
        StringBuffer sb = new StringBuffer();
        sb.append(id).append(",");
        sb.append(progressBar).append(",");
        sb.append(isComplete?1:0).append(",");
        sb.append(isObtain?1:0).append(",");
        return sb.toString();
    }

    public void init(String str){
        if (!StringUtils.isBlank(str)) {
            int i = 0;
            String[] values = str.split(",");
            id = StringUtil.getIntValue(values, i++);
            progressBar = StringUtil.getIntValue(values, i++);
            isComplete = StringUtil.getIntValue(values, i++) == 1;
            isObtain = StringUtil.getIntValue(values, i++) == 1;
        }
    }

    public void clear(){
        progressBar=0;
        isObtain=false;
        isComplete=false;
    }
}
