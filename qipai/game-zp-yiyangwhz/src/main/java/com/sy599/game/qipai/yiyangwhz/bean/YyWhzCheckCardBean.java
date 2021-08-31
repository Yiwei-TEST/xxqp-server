package com.sy599.game.qipai.yiyangwhz.bean;

import com.sy599.game.qipai.yiyangwhz.constant.YyWhzCard;
import com.sy599.game.qipai.yiyangwhz.tool.YyWhzHuLack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author liuping
 * 鬼胡子摸牌或出牌可以做的操作  溜  > 漂 > 偎 ＞ 胡 ＞ 碰 ＞ 吃
 */
public class YyWhzCheckCardBean {

    public static final int index_liu = 0;
    public static final int index_piao = 1;
    public static final int index_wei = 2;
    public static final int index_hu = 3;
    public static final int index_peng = 4;
    public static final int index_chi = 5;
    public static final int index_tianHu = 6;


    // 当前seat
    private int seat;
    /**
     * 溜
     */
    private boolean liu;
    /**
     * 漂
     */
    private boolean piao;
    /**
     * 偎
     */
    private boolean wei;
    /**
     * 胡
     */
    private boolean hu;
    /**
     * 碰
     */
    private boolean peng;
    /**
     * 吃
     */
    private boolean chi;
    /**
     * 天胡或九对半胡
     */
    private boolean tianHu;
    /**
     * 出的牌或者是摸的牌
     */
    private YyWhzCard disCard;
    /**
     * 可做的操作列表
     */
    private List<Integer> actionList;
    /**
     * 是否正在摸牌
     */
    private boolean isMoPaiIng;
    /**
     * 胡牌时相关信息
     */
    private YyWhzHuLack lack;
    /**
     * 过掉胡
     */
    private boolean isPassHu;

    public YyWhzCheckCardBean() {
    }

    public YyWhzCheckCardBean(int seat, YyWhzCard disCard) {
        this.seat = seat;
        this.disCard = disCard;
    }

    public List<Integer> getActionList() {
        return actionList;
    }

    public void setActionList(List<Integer> actionList) {
        this.actionList = actionList;
    }

    public YyWhzCard getDisCard() {
        return disCard;
    }

    public void setDisCard(YyWhzCard disCard) {
        this.disCard = disCard;
    }

    public int getSeat() {
        return seat;
    }

    public void setSeat(int seat) {
        this.seat = seat;
    }

    public boolean isLiu() {
        return liu;
    }

    public void setLiu(boolean liu) {
        this.liu = liu;
    }

    public boolean isPiao() {
        return piao;
    }

    public void setPiao(boolean piao) {
        this.piao = piao;
    }

    public boolean isWei() {
        return wei;
    }

    public void setWei(boolean wei) {
        this.wei = wei;
    }

    public boolean isHu() {
        return hu;
    }

    public void setHu(boolean hu) {
        this.hu = hu;
    }

    public boolean isPeng() {
        return peng;
    }

    public void setPeng(boolean peng) {
        this.peng = peng;
    }

    public boolean isChi() {
        return chi;
    }

    public void setChi(boolean chi) {
        this.chi = chi;
    }

    public boolean isTianHu() {
        return tianHu;
    }

    public void setTianHu(boolean tianHu) {
        this.tianHu = tianHu;
    }

    /**
     * 0溜  1漂 2偎 3胡 4 碰 5吃 6天胡(九对半胡)
     *
     * @return
     */
    public List<Integer> buildActionList() {
        int[] arr = new int[7];
        if (liu) {
            arr[index_liu] = 1;
        }
        if (piao) {
            arr[index_piao] = 1;
        }
        if (wei) {
            arr[index_wei] = 1;
        }
        if (hu) {
            arr[index_hu] = 1;
        }
        if (peng) {
            arr[index_peng] = 1;
        }
        if (chi) {
            arr[index_chi] = 1;
        }
        if (tianHu) {
            arr[index_tianHu] = 1;
        }
        List<Integer> list = new ArrayList<>();
        for (int val : arr) {
            list.add(val);
        }
        if (list.contains(1)) {
            actionList = list;
        } else {
            actionList = Collections.emptyList();
        }
        return actionList;
    }

    public boolean isMoPaiIng() {
        return isMoPaiIng;
    }

    public void setMoPaiIng(boolean isMoPaiIng) {
        this.isMoPaiIng = isMoPaiIng;
    }

    public YyWhzHuLack getLack() {
        return lack;
    }

    public void setLack(YyWhzHuLack lack) {
        this.lack = lack;
    }

    public boolean isPassHu() {
        return isPassHu;
    }

    public void setPassHu(boolean isPassHu) {
        this.isPassHu = isPassHu;
    }

    public static String actionListToString(List<Integer> actionList) {
        if (actionList == null || actionList.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < actionList.size(); i++) {
            if (actionList.get(i) == 1) {
                switch (i) {
                    case index_liu:
                        sb.append("liu").append(",");
                        break;
                    case index_piao:
                        sb.append("piao").append(",");
                        break;
                    case index_wei:
                        sb.append("wei").append(",");
                        break;
                    case index_hu:
                        sb.append("hu").append(",");
                        break;
                    case index_peng:
                        sb.append("peng").append(",");
                        break;
                    case index_chi:
                        sb.append("chi").append(",");
                        break;
                    case index_tianHu:
                        sb.append("tianHu").append(",");
                        break;
                    default:
                        sb.append("未知").append(i).append(",");
                }
            }

        }
        sb.append("]");
        return sb.toString();
    }

    private static boolean hasAction(List<Integer> actionList, int actionIndex) {
        return actionList != null && actionList.size() > actionIndex && actionList.get(actionIndex) == 1;
    }

    public static boolean hasLiu(List<Integer> actionList) {
        return hasAction(actionList, index_liu);
    }
    public static boolean hasWei(List<Integer> actionList) {
    	return hasAction(actionList, index_wei);
    }

    public static boolean hasHu(List<Integer> actionList) {
        return hasAction(actionList, index_hu);
    }

    public static boolean hasChi(List<Integer> actionList) {
        return hasAction(actionList, index_chi);
    }


}
