package com.sy.sanguo.game.constants;

import com.sy.sanguo.game.bean.group.GroupUser;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupUserTree {

    private Long keyId;
    private Long userId;
    private Integer userRole;
    private Long promoterId = 0L;
    private Integer promoterLevel = 0;
    private Long promoterId1 = 0L;
    private Long promoterId2 = 0L;
    private Long promoterId3 = 0L;
    private Long promoterId4 = 0L;
    private Long promoterId5 = 0L;
    private Long promoterId6 = 0L;
    private Long promoterId7 = 0L;
    private Long promoterId8 = 0L;
    private Long promoterId9 = 0L;
    private Long promoterId10 = 0L;

    private GroupUserTree pre;
    private List<GroupUserTree> nextList;

    public GroupUserTree(GroupUser gu) {
        BeanUtils.copyProperties(gu, this);
    }

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getUserRole() {
        return userRole;
    }

    public void setUserRole(Integer userRole) {
        this.userRole = userRole;
    }

    public Long getPromoterId() {
        return promoterId;
    }

    public void setPromoterId(Long promoterId) {
        this.promoterId = promoterId;
    }

    public Integer getPromoterLevel() {
        return promoterLevel;
    }

    public void setPromoterLevel(Integer promoterLevel) {
        this.promoterLevel = promoterLevel;
    }

    public Long getPromoterId1() {
        return promoterId1;
    }

    public void setPromoterId1(Long promoterId1) {
        this.promoterId1 = promoterId1;
    }

    public Long getPromoterId2() {
        return promoterId2;
    }

    public void setPromoterId2(Long promoterId2) {
        this.promoterId2 = promoterId2;
    }

    public Long getPromoterId3() {
        return promoterId3;
    }

    public void setPromoterId3(Long promoterId3) {
        this.promoterId3 = promoterId3;
    }

    public Long getPromoterId4() {
        return promoterId4;
    }

    public void setPromoterId4(Long promoterId4) {
        this.promoterId4 = promoterId4;
    }

    public Long getPromoterId5() {
        return promoterId5;
    }

    public void setPromoterId5(Long promoterId5) {
        this.promoterId5 = promoterId5;
    }

    public Long getPromoterId6() {
        return promoterId6;
    }

    public void setPromoterId6(Long promoterId6) {
        this.promoterId6 = promoterId6;
    }

    public Long getPromoterId7() {
        return promoterId7;
    }

    public void setPromoterId7(Long promoterId7) {
        this.promoterId7 = promoterId7;
    }

    public Long getPromoterId8() {
        return promoterId8;
    }

    public void setPromoterId8(Long promoterId8) {
        this.promoterId8 = promoterId8;
    }

    public Long getPromoterId9() {
        return promoterId9;
    }

    public void setPromoterId9(Long promoterId9) {
        this.promoterId9 = promoterId9;
    }

    public Long getPromoterId10() {
        return promoterId10;
    }

    public void setPromoterId10(Long promoterId10) {
        this.promoterId10 = promoterId10;
    }

    public GroupUserTree getPre() {
        return pre;
    }

    public void setPre(GroupUserTree pre) {
        this.pre = pre;
    }

    public List<GroupUserTree> getNextList() {
        return nextList;
    }

    public void setNextList(List<GroupUserTree> nextList) {
        this.nextList = nextList;
    }

    public void addNextList(GroupUserTree next) {
        if(nextList == null){
            nextList = new ArrayList<>();
        }
        this.nextList.add(next);
    }

    public HashMap<String, Object> genUpdateSql() {
        HashMap<String, Object> sql = new HashMap<>();
        sql.put("keyId", this.getKeyId());
        sql.put("promoterLevel", this.getPromoterLevel());
        sql.put("promoterId", this.getPromoterId());
        sql.put("promoterId1", this.getPromoterId1());
        sql.put("promoterId2", this.getPromoterId2());
        sql.put("promoterId3", this.getPromoterId3());
        sql.put("promoterId4", this.getPromoterId4());
        sql.put("promoterId5", this.getPromoterId5());
        sql.put("promoterId6", this.getPromoterId6());
        sql.put("promoterId7", this.getPromoterId7());
        sql.put("promoterId8", this.getPromoterId8());
        sql.put("promoterId9", this.getPromoterId9());
        sql.put("promoterId10", this.getPromoterId10());
        return sql;
    }

    /**
     * 设置promoterId
     * 将相应level的上级id设置成指定的id
     * @param promoterLevel
     * @param promoterId
     */
    public  void setPromoterId(int promoterLevel, long promoterId) {
        switch (promoterLevel) {
            case 1:
                this.setPromoterId1(promoterId);
                break;
            case 2:
                this.setPromoterId2(promoterId);
                break;
            case 3:
                this.setPromoterId3(promoterId);
                break;
            case 4:
                this.setPromoterId4(promoterId);
                break;
            case 5:
                this.setPromoterId5(promoterId);
                break;
            case 6:
                this.setPromoterId6(promoterId);
                break;
            case 7:
                this.setPromoterId7(promoterId);
                break;
            case 8:
                this.setPromoterId8(promoterId);
                break;
            case 9:
                this.setPromoterId9(promoterId);
                break;
            case 10:
                this.setPromoterId10(promoterId);
                break;
        }
    }
}
