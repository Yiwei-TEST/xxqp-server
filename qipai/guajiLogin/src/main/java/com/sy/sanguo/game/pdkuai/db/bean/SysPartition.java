package com.sy.sanguo.game.pdkuai.db.bean;


public class SysPartition {

    private int keyId;

    private int type;

    private int seq;

    private int isHash;

    private String ids;

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public int getIsHash() {
        return isHash;
    }

    public void setIsHash(int isHash) {
        this.isHash = isHash;
    }

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }
}
