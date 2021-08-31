package com.sy599.game.common.bean;

import com.sy599.game.character.Player;
import com.sy599.game.db.enums.SourceType;

public class Consume {

    private Player player;

    /*** 是否成功**/
    private boolean isOK;

    /*** 免费房卡 (正数：增加免费钻，不能设置负数)**/
    private long freeValue;

    /*** 收费房卡 (正数：增加充值钻，负数：消耗房卡，会优化消耗免费房卡，)**/
    private long value;

    /*** 是否推送给前台**/
    private boolean isWrite;

    /*** 消耗的玩法**/
    private int playType;

    /*** 是否记录**/
    private boolean isRecord;

    /*** 钻石来源**/
    private SourceType sourceType;

    /*** 实际消耗免费钻**/
    private long freeValue1;

    /*** 实际消耗钻**/
    private long value1;

    /*** 亲友圈id**/
    private long groupId;

    /*** 牌桌id**/
    private long tableId;


    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public boolean isOK() {
        return isOK;
    }

    public void setOK(boolean OK) {
        isOK = OK;
    }

    public long getFreeValue() {
        return freeValue;
    }

    public void setFreeValue(long freeValue) {
        this.freeValue = freeValue;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public boolean isWrite() {
        return isWrite;
    }

    public void setWrite(boolean write) {
        isWrite = write;
    }

    public int getPlayType() {
        return playType;
    }

    public void setPlayType(int playType) {
        this.playType = playType;
    }

    public boolean isRecord() {
        return isRecord;
    }

    public void setRecord(boolean record) {
        isRecord = record;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public long getFreeValue1() {
        return freeValue1;
    }

    public void setFreeValue1(long freeValue1) {
        this.freeValue1 = freeValue1;
    }

    public long getValue1() {
        return value1;
    }

    public void setValue1(long value1) {
        this.value1 = value1;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getTableId() {
        return tableId;
    }

    public void setTableId(long tableId) {
        this.tableId = tableId;
    }
}
