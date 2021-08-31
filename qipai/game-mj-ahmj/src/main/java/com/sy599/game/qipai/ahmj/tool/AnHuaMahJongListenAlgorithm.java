package com.sy599.game.qipai.ahmj.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sy599.game.common.constant.SharedConstants.player_state;
import com.sy599.game.common.constant.SharedConstants.table_state;
import com.sy599.game.qipai.ahmj.bean.AhmjPlayer;
import com.sy599.game.qipai.ahmj.bean.AhmjTable;
import com.sy599.game.qipai.ahmj.bean.AhmjHu;
import com.sy599.game.qipai.ahmj.constant.Ahmj;

/**
 * 安化麻将听牌算法实现类
 * 
 * @author Gavin 2019/04/10
 *
 */
public class AnHuaMahJongListenAlgorithm implements ListenCardAlgorithm<AhmjTable, AhmjPlayer> {

	@Override
	public List<Ahmj> dealListenCard(AhmjTable table, AhmjPlayer player) {
		List<Ahmj> results = new ArrayList<>();
		if (table.getState() == table_state.play && player.getState() == player_state.play) {
			List<Ahmj> handMajiangList = new ArrayList<>(player.getHandMajiang());
			 
			if (!table.getActionSeatMap().containsKey(player.getSeat())) {
				List<Ahmj> gang = player.getaGang();
				List<Ahmj> peng = player.getPeng();
				List<Ahmj> chi = player.getChi();
				List<Ahmj> buzhang = player.getBuzhang();
				List<Ahmj> conditionList = Arrays.asList(Ahmj.mj1, Ahmj.mj2, Ahmj.mj3, Ahmj.mj4,
						Ahmj.mj5, Ahmj.mj6, Ahmj.mj7, Ahmj.mj8, Ahmj.mj9, Ahmj.mj10, Ahmj.mj11,
						Ahmj.mj12, Ahmj.mj13, Ahmj.mj14, Ahmj.mj15, Ahmj.mj16, Ahmj.mj17,
						Ahmj.mj18, Ahmj.mj19, Ahmj.mj20, Ahmj.mj21, Ahmj.mj22, Ahmj.mj23,
						Ahmj.mj24, Ahmj.mj25, Ahmj.mj26, Ahmj.mj27);
				Ahmj kingMahJong = Ahmj.getMajang(table.getFirstId());
				boolean isFourKing = table.isFourWang();

				for (Ahmj majiang : conditionList) {
					List<Ahmj> handMajiangs = new ArrayList<>(handMajiangList);
					handMajiangs.add(majiang);
					AhmjHu hu = AhMajiangTool.isHuAHMajiang(handMajiangs, gang, peng, chi, buzhang, kingMahJong,
							isFourKing, table.getDisCardRound() == 0, table.getDisCardSeat() == player.getSeat(),
							false);
					boolean flag = hu.isHu() || hu.is7Xiaodui() || hu.isDahu() || hu.isPingHu();
					if (flag && !results.contains(majiang)) {
						results.add(majiang);
					}
				}
			}
		}
		return results;
	}

	public static void main(String[] args) {

	}
}
