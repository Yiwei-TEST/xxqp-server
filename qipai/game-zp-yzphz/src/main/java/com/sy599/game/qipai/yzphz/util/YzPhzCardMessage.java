package com.sy599.game.qipai.yzphz.util;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.msg.serverPacket.TablePhzResMsg;

import java.util.Arrays;
import java.util.List;

public final class YzPhzCardMessage {
    private YzPhzHuXiEnums huXiEnum;
    private boolean checkHuxi = true;
    private List<Integer> cards;

    public YzPhzCardMessage(YzPhzHuXiEnums huXiEnum, List<Integer> cards){
        this.huXiEnum=huXiEnum;
        this.cards=cards;
    }

    public YzPhzCardMessage(YzPhzHuXiEnums huXiEnum, Integer... cards){
        this.huXiEnum=huXiEnum;
        this.cards= Arrays.asList(cards);
    }

    public void init(YzPhzHuXiEnums huXiEnum, List<Integer> cards) {
        this.huXiEnum=huXiEnum;
        this.cards=cards;
    }

    public int loadHuxi(){
        return checkHuxi?(YzPhzCardUtils.smallCard(cards.get(0))?huXiEnum.getSmall():huXiEnum.getBig()):0;
    }

    public boolean isCheckHuxi() {
        return checkHuxi;
    }

    public void setCheckHuxi(boolean checkHuxi) {
        this.checkHuxi = checkHuxi;
    }

    @Override
    public String toString() {
        JSONObject json=new JSONObject();
        json.put("action",huXiEnum.name());
        json.put("huxi",loadHuxi());
        json.put("cards",cards.toString());
        return json.toString();
    }

    public TablePhzResMsg.PhzHuCards.Builder build() {
        TablePhzResMsg.PhzHuCards.Builder msg = TablePhzResMsg.PhzHuCards.newBuilder();
        msg.setAction(huXiEnum.getAction()).setHuxi(loadHuxi()).addAllCards(cards);
        return msg;
    }

    public boolean eq(YzPhzCardMessage cm){
        if (cm==this){
            return true;
        }else{
            return cm.cards.size()==this.cards.size()&&this.huXiEnum.getAction()==cm.huXiEnum.getAction()&&cm.cards.containsAll(this.cards);
        }
    }

    public YzPhzHuXiEnums getHuXiEnum() {
        return huXiEnum;
    }

    public List<Integer> getCards() {
        return cards;
    }
}
