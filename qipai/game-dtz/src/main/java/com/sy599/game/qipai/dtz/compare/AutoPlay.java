package com.sy599.game.qipai.dtz.compare;

import java.util.List;

import com.sy599.game.qipai.dtz.bean.AutoHandPais;
import com.sy599.game.qipai.dtz.bean.CardPair;
import com.sy599.game.qipai.dtz.bean.DtzTable;

public interface AutoPlay {
	/**
	 * 托管自动出牌
	 * @param autoHandPais
	 * @param outCards
	 * @param table
	 * @return
	 */
	CardPair autoPlay(AutoHandPais autoHandPais, List<Integer> outCards, DtzTable table);
}
