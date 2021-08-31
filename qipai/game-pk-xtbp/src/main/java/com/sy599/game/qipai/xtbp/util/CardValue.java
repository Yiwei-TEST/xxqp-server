package com.sy599.game.qipai.xtbp.util;

import java.util.Objects;

public class CardValue implements Comparable<CardValue> {
	private final int card;
	private final int value;
	// 方片 1 梅花2 红桃3 黑桃4  5王
	private final int color;

	public CardValue(int card) {
		this.card = card;
		this.value = CardUtils.loadCardValue(card);
		this.color = CardUtils.loadCardColor(card);
	}

	public CardValue(int card, int value, int color) {
		this.card = card;
		this.value = value;
		this.color = color;
	}

	public int getCard() {
		return card;
	}

	public int getValue() {
		return value;
	}

	public int getColor() {
		return color;
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
