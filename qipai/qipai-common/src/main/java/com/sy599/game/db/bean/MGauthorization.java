package com.sy599.game.db.bean;

import java.util.Date;

public class MGauthorization {
    private String unionId;

    private String pf;

    private Date createTime;

    public MGauthorization() {
    }

    public MGauthorization(String unionId, String pf, Date createTime) {
        this.unionId = unionId;
        this.pf = pf;
        this.createTime = createTime;
    }

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    public String getPf() {
        return pf;
    }

    public void setPf(String pf) {
        this.pf = pf;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
