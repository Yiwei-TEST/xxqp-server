package com.sy599.game.db.bean.group;

import java.io.Serializable;

public class LogGroupGoldWin implements Serializable {

    private static final long serialVersionUID = 1L;

    private long keyId;
    private long dataDate;
    private long groupId;
    private long userId;

    private long win;
    private long lose;
    private long selfWin;
    private int tag;


    public LogGroupGoldWin(Long dataDate, long groupId, long userId , long selfWin) {
        this.dataDate = dataDate;
        this.groupId = groupId;
        this.userId = userId;
        this.selfWin = selfWin;
    }

}
