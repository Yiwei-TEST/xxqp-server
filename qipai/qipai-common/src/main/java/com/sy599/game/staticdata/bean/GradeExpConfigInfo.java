package com.sy599.game.staticdata.bean;

/**
 * 芒果跑得快 段位对应积分配置
 */
public class GradeExpConfigInfo {

    private int grade;

    private int needExp;

    private String desc;

    public GradeExpConfigInfo(int grade, int needExp, String desc) {
        this.grade = grade;
        this.needExp = needExp;
        this.desc = desc;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public int getNeedExp() {
        return needExp;
    }

    public void setNeedExp(int needExp) {
        this.needExp = needExp;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
