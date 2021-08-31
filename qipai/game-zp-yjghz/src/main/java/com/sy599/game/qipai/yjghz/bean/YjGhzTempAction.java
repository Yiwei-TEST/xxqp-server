package com.sy599.game.qipai.yjghz.bean;

import com.sy599.game.qipai.yjghz.constant.YjGhzCard;
import com.sy599.game.qipai.yjghz.tool.YjGhzTool;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuping 鬼胡子临时操作
 */
public class YjGhzTempAction {

    private int seat;// 玩家位置
    private int action;// 玩家所做的操作
    private List<YjGhzCard> cardList;// 操作对应的牌
    private YjGhzCard nowDisCard;// 当前打出的牌

    public YjGhzTempAction() {
    }

    public YjGhzTempAction(int seat, int action, List<YjGhzCard> cardList, YjGhzCard nowDisCard) {
        this.seat = seat;
        this.action = action;
        if (cardList == null)
            this.cardList = new ArrayList<>();
        else
            this.cardList = cardList;
        this.nowDisCard = nowDisCard;
    }

    public int getSeat() {
        return seat;
    }

    public void setSeat(int seat) {
        this.seat = seat;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public List<YjGhzCard> getCardList() {
        return cardList;
    }

    public void setCardList(List<YjGhzCard> cardList) {
        this.cardList = cardList;
    }

    public YjGhzCard getNowDisCard() {
        return nowDisCard;
    }

    public void setNowDisCard(YjGhzCard nowDisCard) {
        this.nowDisCard = nowDisCard;
    }

    public void initData(String data) {
        JsonWrapper wrapper = new JsonWrapper(data);
        seat = wrapper.getInt("1", 0);
        action = wrapper.getInt("2", 0);
        String cardStr = wrapper.getString("3");
        cardList = YjGhzTool.explodeGhz(cardStr, ",");
        int nowDisCardId = wrapper.getInt("4", 0);
        nowDisCard = YjGhzCard.getPaohzCard(nowDisCardId);
    }

    public String buildData() {
        JsonWrapper wrapper = new JsonWrapper("");
        wrapper.putInt(1, seat);
        wrapper.putInt(2, action);
        wrapper.putString(3, StringUtil.implode(YjGhzTool.toGhzCardIds(cardList), ","));
        wrapper.putInt(4, (nowDisCard != null) ? nowDisCard.getId() : 0);
        return wrapper.toString();
    }
}
