package com.sy599.game.db.bean;

import java.io.Serializable;

public class ResourcesConfigs implements Serializable{
    private String msgType;//资源类型
    private String msgKey;//资源key
    private String msgValue;//资源value

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getMsgKey() {
        return msgKey;
    }

    public void setMsgKey(String msgKey) {
        this.msgKey = msgKey;
    }

    public String getMsgValue() {
        return msgValue;
    }

    public void setMsgValue(String msgValue) {
        this.msgValue = msgValue;
    }
}
