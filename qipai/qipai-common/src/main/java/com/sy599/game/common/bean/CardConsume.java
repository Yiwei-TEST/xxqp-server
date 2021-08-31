package com.sy599.game.common.bean;

import com.sy599.game.character.Player;
import com.sy599.game.db.enums.CardSourceType;

public class CardConsume {

    private Player player;

    /*** 是否成功**/
    private boolean isOK;

    /*** 免费房卡 (正数：增加免费钻，不能设置负数)**/
    private long freeCards;

    /*** 收费房卡 (正数：增加充值钻，负数：消耗房卡，会优化消耗免费房卡，)**/
    private long cards;

    /*** 是否推送给前台**/
    private boolean isWrite;

    /*** 消耗的玩法**/
    private int playType;

    /*** 是否记录**/
    private boolean isRecord;

    /*** 钻石来源**/
    private CardSourceType sourceType;

    /*** 实际消耗免费钻**/
    private long freeCards1;

    /*** 实际消耗钻**/
    private long cards1;

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

    public long getFreeCards() {
        return freeCards;
    }

    public void setFreeCards(long freeCards) {
        this.freeCards = freeCards;
    }

    public long getCards() {
        return cards;
    }

    public void setCards(long cards) {
        this.cards = cards;
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

    public CardSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(CardSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public long getFreeCards1() {
        return freeCards1;
    }

    public void setFreeCards1(long freeCards1) {
        this.freeCards1 = freeCards1;
    }

    public long getCards1() {
        return cards1;
    }

    public void setCards1(long cards1) {
        this.cards1 = cards1;
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
