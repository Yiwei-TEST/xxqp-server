package com.sy599.game.qipai.qianfen.util;

import java.util.Objects;

public class CardValue implements Comparable<CardValue> {
    private final int card;
    private final int value;

    public CardValue(int card) {
        this.card = card;
        this.value = CardUtils.loadCardValue(card);
    }

    public CardValue(int card, int value) {
        this.card = card;
        this.value = value;
    }

    public int getCard() {
        return card;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "{card=" + card + ",value=" + value + "}";
    }

    @Override
    public int compareTo(CardValue o) {
        return this.value - o.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof CardValue) && card == ((CardValue) obj).card;
    }

}
