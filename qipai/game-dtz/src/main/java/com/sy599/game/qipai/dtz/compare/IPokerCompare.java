package com.sy599.game.qipai.dtz.compare;

import com.sy599.game.qipai.dtz.bean.CardPair;

/**
 * 比较牌型的大小
 * @author zhouhj
 *
 */
public interface IPokerCompare {
	/**
	 * 比较器 各个牌型
	 * @param from 上家出牌
	 * @param own 自己出牌
	 * @param param 特殊参数，比如牌的类型（三副牌或者四副牌）
	 * @return
	 */
	int compareTo(CardPair from, CardPair own, int... param);
}
