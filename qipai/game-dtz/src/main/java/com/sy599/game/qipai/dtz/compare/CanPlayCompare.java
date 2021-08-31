package com.sy599.game.qipai.dtz.compare;

import com.sy599.game.qipai.dtz.bean.CardPair;
import com.sy599.game.qipai.dtz.bean.DtzModel;

/**
 * 手牌中是否有打得起
 * @author ran
 *
 */
public interface CanPlayCompare {
	/**
	 * 比较器 各个牌型
	 * @param from 手牌统计
	 * @param own 比较出的牌型
	 * @param param 
	 * @return
	 */
	int compareTo(DtzModel model, CardPair cardPair, Object... param);
}
