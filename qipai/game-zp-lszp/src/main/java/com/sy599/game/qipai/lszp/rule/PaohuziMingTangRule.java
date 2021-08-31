package com.sy599.game.qipai.lszp.rule;


import com.sy599.game.qipai.lszp.been.LszpPlayer;
import com.sy599.game.qipai.lszp.been.LszpTable;
import com.sy599.game.qipai.lszp.tool.PaohuziHuLack;

import java.util.ArrayList;
import java.util.List;

public class PaohuziMingTangRule {

	public static final int LOUDI_MINGTANG_ZIMO = 1;//自摸 2番
	public static final int LOUDI_MINGTANG_FANGPAO = 2;//放炮 3番


	/**
	 * 名堂
	 * @param player 胡牌玩家
	 * @return
	 */
	public static List<Integer> calcMingTang(LszpPlayer player, boolean fangPao){
		List<Integer> mtList = new ArrayList<>();
		LszpTable table = player.getPlayingTable(LszpTable.class);
		if(fangPao){
			mtList.add(LOUDI_MINGTANG_FANGPAO);
		}else if(player.getSeat() == table.getMoSeat()){
			mtList.add(LOUDI_MINGTANG_ZIMO);
		}
		return mtList;
	}



	public static int countXiTun(int tun, List<Integer> mt) {
		int lastTun = tun;
		if(mt == null || mt.isEmpty()){
			return lastTun;
		}else{
			int bei=1;
			for(int mtId : mt){
				switch(mtId){
					case LOUDI_MINGTANG_ZIMO:
						bei*=2;
						break;
					case LOUDI_MINGTANG_FANGPAO:
						bei*=3;
						break;
				}
			}
			lastTun*=bei;
		}
		return lastTun;
	}

}
