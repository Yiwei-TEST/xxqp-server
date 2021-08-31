package com.sy599.game.qipai.hylhq.rule;


import com.sy599.game.qipai.hylhq.been.CardTypeHuxi;
import com.sy599.game.qipai.hylhq.been.HylhqPlayer;
import com.sy599.game.qipai.hylhq.been.HylhqTable;
import com.sy599.game.qipai.hylhq.constant.PaohzCard;
import com.sy599.game.qipai.hylhq.tool.PaohuziHuLack;
import com.sy599.game.qipai.hylhq.tool.PaohuziTool;

import java.util.ArrayList;
import java.util.List;

public class PaohuziMingTangRule {

	public static final int LOUDI_MINGTANG_ZIMO = 1;//自摸         2番
	public static final int LOUDI_MINGTANG_XIAOHONG = 2;//小红     2番 胡牌时红牌数量为8-9张胡牌时
	public static final int LOUDI_MINGTANG_DAHONG = 3;//大红       4番 胡牌时红牌大于等于10张
	public static final int LOUDI_MINGTANG_YIDIANHONG = 4;//一点红 3番 胡牌时有且仅只有一张红牌
	public static final int LOUDI_MINGTANG_HEIHU = 5;//黑胡        5番 胡牌时全部为黑色牌
	public static final int LOUDI_MINGTANG_DIHU = 6;//地胡         2番
	public static final int LOUDI_MINGTANG_TIANHU = 7;//天胡       2番



	/**
	 * 名堂
	 * @param player 胡牌玩家
	 * @return
	 */
	public static List<Integer> calcMingTang(HylhqPlayer player, PaohuziHuLack hu,HylhqTable table){
		List<Integer> mtList = new ArrayList<>();
		List<PaohzCard> allCards = new ArrayList<>();
		if(table.getDisNum()==0&&table.getMoNum()==0){
			if(table.getTianHu()==1)
				mtList.add(LOUDI_MINGTANG_TIANHU);
		}else if(table.getDisOrMo()==1&&table.getMoNum()==0&&player.getDisNum()==0){
			if(table.getDisNum()==1)
				mtList.add(LOUDI_MINGTANG_DIHU);
		}else if(table.getDisOrMo()==2&&player.getSeat() == table.getMoSeat()){
			if(table.getZiMo()==1)
				mtList.add(LOUDI_MINGTANG_ZIMO);
		}
		if(table.getHongHei()==1||table.getHongHei2Fan()==1){
			for (CardTypeHuxi type : player.getCardTypes()) {
				allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
			}
			if (hu != null && hu.getPhzHuCards() != null) {
				for (CardTypeHuxi type : hu.getPhzHuCards()) {
					allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
				}
			}
			List<PaohzCard> redCardList = PaohuziTool.findRedPhzs(allCards);
			int redCardCount = redCardList.size();
			if(table.getHongHei2Fan()==1){
				if(redCardCount >= 10){
					mtList.add(LOUDI_MINGTANG_DAHONG);
				}else if(redCardCount == 0){
					mtList.add(LOUDI_MINGTANG_HEIHU);
				}
			}else if(table.getHongHei()==1){
				if(redCardCount == 1){
					mtList.add(LOUDI_MINGTANG_YIDIANHONG);
				}else if(redCardCount >= 10){
					mtList.add(LOUDI_MINGTANG_DAHONG);
				}else if(redCardCount >= 8){
					mtList.add(LOUDI_MINGTANG_XIAOHONG);
				}else if(redCardCount == 0){
					mtList.add(LOUDI_MINGTANG_HEIHU);
				}
			}
		}
		return mtList;
	}



	public static int countXiTun(int tun, List<Integer> mts,boolean hh2f) {
		if(mts==null||mts.size()==0)
			return tun;
		int fan=1;
		for (Integer mt:mts) {
			switch (mt){
				case LOUDI_MINGTANG_ZIMO:
					fan*=2;
					break;
				case LOUDI_MINGTANG_XIAOHONG:
					fan*=2;
					break;
				case LOUDI_MINGTANG_DAHONG:
					if(hh2f)
						fan*=2;
					else
						fan*=4;
					break;
				case LOUDI_MINGTANG_YIDIANHONG:
					fan*=3;
					break;
				case LOUDI_MINGTANG_HEIHU:
					if(hh2f)
						fan*=2;
					else
						fan*=5;
					break;
				case LOUDI_MINGTANG_DIHU:
					fan*=2;
					break;
				case LOUDI_MINGTANG_TIANHU:
					fan*=2;
					break;
			}
		}
		return tun*fan;
	}

}
