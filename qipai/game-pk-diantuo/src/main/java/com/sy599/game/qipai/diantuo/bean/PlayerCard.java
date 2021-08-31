package com.sy599.game.qipai.diantuo.bean;

import com.alibaba.fastjson.JSON;

import java.util.List;

//用于回放功能，玩家对应出牌
public class PlayerCard {
    String name;
    List<Integer> cards;

    public PlayerCard() {
    }

    public PlayerCard(String name, List<Integer> cards) {
        this.name = name;
        this.cards = cards;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getCards() {
        return cards;
    }

    public void setCards(List<Integer> cards) {
        this.cards = cards;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
