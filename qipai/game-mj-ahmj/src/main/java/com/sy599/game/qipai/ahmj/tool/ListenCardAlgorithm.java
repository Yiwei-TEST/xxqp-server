package com.sy599.game.qipai.ahmj.tool;

import java.util.List;

import com.sy599.game.base.BaseTable;
import com.sy599.game.character.Player;
import com.sy599.game.qipai.ahmj.constant.Ahmj;

/**
 * 听牌算法接口类
 * 
 * @author Gavin 2019/04/09
 */
public interface ListenCardAlgorithm<T extends BaseTable, P extends Player> {
	/**
	 * 获取玩家的手牌听牌的牌
	 * 
	 * @param table     牌桌对象
	 * @param player    玩家对象
	 * @return 如果可以听牌则返回true,否则返回false
	 */
	List<Ahmj> dealListenCard(T table, P player);
}
