package com.sy.sanguo.game.bean.group;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GroupCreditWheel implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long keyId;
    private Integer groupId;
    private Long creditPool;
    private String prize;
    private String rate;
    private Integer nextWin = 1;
    private Integer status = 0;
    private Integer totalPay = 0;
    private Integer biggestPrize = 0;


    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Long getCreditPool() {
        return creditPool;
    }

    public void setCreditPool(Long creditPool) {
        this.creditPool = creditPool;
    }

    public String getPrize() {
        return prize;
    }

    public void setPrize(String prize) {
        this.prize = prize;
        String[] prizes = prize.split(",");
        if(prize.length() >0) {
            for (String p : prizes) {
                Integer value = Integer.valueOf(p);
                if (value > biggestPrize) {
                    biggestPrize = value;
                }
            }
        }
    }

    public Integer getBiggestPrize() {
        return biggestPrize;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public Integer getNextWin() {
        return nextWin;
    }

    public void setNextWin(Integer nextWin) {
        this.nextWin = nextWin;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getTotalPay() {
        return totalPay;
    }

    public void setTotalPay(Integer totalPay) {
        this.totalPay = totalPay;
    }

    public Map<Integer, Long> getDrawMap() {
        Map<Integer, Long> drawMap = new LinkedHashMap<>();
        if(prize.isEmpty() || rate.isEmpty())
            return drawMap;
        String[] prizes = prize.split(",");
        String[] rates = rate.split(",");
        if(prizes.length != rates.length){
            return drawMap;
        }
        for (int i = 0 ; i < prizes.length; i++) {
            drawMap.put(Integer.valueOf(prizes[i]), Long.valueOf(rates[i]));
        }
        return drawMap;
    }

}
