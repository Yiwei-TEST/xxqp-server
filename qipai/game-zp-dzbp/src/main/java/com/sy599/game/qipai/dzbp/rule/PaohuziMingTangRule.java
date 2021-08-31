package com.sy599.game.qipai.dzbp.rule;

import com.sy599.game.qipai.dzbp.bean.CardTypeHuxi;
import com.sy599.game.qipai.dzbp.bean.DzbpPlayer;
import com.sy599.game.qipai.dzbp.bean.DzbpTable;
import com.sy599.game.qipai.dzbp.constant.PaohzCard;
import com.sy599.game.qipai.dzbp.tool.PaohuziTool;
import com.sy599.game.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class PaohuziMingTangRule {

	public static final int LOUDI_MINGTANG_TIANHU = 1;//天胡 +10
	public static final int LOUDI_MINGTANG_DIHU = 2;//地胡 +10
	public static final int LOUDI_MINGTANG_ZIMO = 3;//自摸 +10
	public static final int LOUDI_MINGTANG_YIDIANZHU = 4;//胡牌时只有一张红字，一点朱  翻倍
	public static final int LOUDI_MINGTANG_XIAOHONGHU = 5;//胡牌时有10-12张红字，小红胡  翻倍
	public static final int LOUDI_MINGTANG_DAHONGHU = 6;//胡牌时有13张红字或以上,大红胡 +60
	public static final int LOUDI_MINGTANG_WUHU = 7;//胡牌时全是黑字，乌胡 +60
	/**
	 * 名堂 红胡（火胡）：即胡牌时手中的红牌大于或等于10张，小于13张（2翻）。 点胡：即胡牌时手中的红牌只有1张（3翻）。
	 * 十三红（甲火胡）：即胡牌时手中的红牌大于或等于13张（4翻）。 黑胡（板胡）：即胡牌时手中没有红牌（5翻）。
	 * 十八小：即胡牌时手中的小牌大于或等于18张（6翻）。 自摸：即胡牌的牌为自己亲手在墩上所摸。（2翻）
	 * 
	 * @return
	 */
	public static List<Integer> calcMingTang(List<PaohzCard> cards) {
		List<PaohzCard> redCardList = PaohuziTool.findRedPhzs(cards);
		int redCardCount = redCardList.size();
		List<Integer> mtList = new ArrayList<>();

		if (redCardCount >= 10 && redCardCount < 13) {
			// 火胡 2翻
			mtList.add(2);
		} else if (redCardCount == 1) {
			// 点胡 3翻
			mtList.add(3);
		} else if (redCardCount >= 13) {
			// 十三红（甲火胡）
			mtList.add(4);
		} else if (redCardCount == 0) {
			// 黑胡
			mtList.add(5);
		}
		if (!mtList.isEmpty()) {
			LogUtil.d_msg("名堂！！！！" + redCardList);
		}

		List<PaohzCard> smallList = PaohuziTool.findSmallPhzs(cards);
		if (smallList.size() >= 18) {
			// 十八小
			mtList.add(6);
			LogUtil.d_msg("名堂！！！！" + redCardList);

		}

		return mtList;

	}
	/**
	 * 名堂
	 * @param player 胡牌玩家
	 * @return
	 */
	public static List<Integer> calcMingTang(DzbpPlayer player){
		List<Integer> mtList = new ArrayList<>();
		List<PaohzCard> allCards = new ArrayList<>();
		for (CardTypeHuxi type : player.getCardTypes()) {
			allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
		}
		if (player.getHu() != null && player.getHu().getPhzHuCards() != null) {
			for (CardTypeHuxi type : player.getHu().getPhzHuCards()) {
				allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
			}
		}
		DzbpTable table = player.getPlayingTable(DzbpTable.class);
		boolean isTianHu = player.isFristDisCard();
		boolean isSelfMo = player.getSeat() == table.getMoSeat();
		if(table.getTianDihu() > 0){
			if (isTianHu && player.getSeat() == table.getLastWinSeat()){
				mtList.add(LOUDI_MINGTANG_TIANHU);
			}
			if (table.getFirstCard() && player.getSeat() != table.getLastWinSeat()){
				mtList.add(LOUDI_MINGTANG_DIHU);
			}
		}
		if (isSelfMo && !isTianHu && !mtList.contains(LOUDI_MINGTANG_DIHU)){
			mtList.add(LOUDI_MINGTANG_ZIMO);
		}
	
		if(table.getIsRedBlack() == 1){
			List<PaohzCard> redCardList = PaohuziTool.findRedPhzs(allCards);
			int redCardCount = redCardList.size();
			if(redCardCount == 1 && table.isBoPi()){
				mtList.add(LOUDI_MINGTANG_YIDIANZHU);
			}else if(redCardCount >= 5 && redCardCount<=6 && table.isBoPi()){
				mtList.add(LOUDI_MINGTANG_XIAOHONGHU);
			}else if(redCardCount >= 7){
				mtList.add(LOUDI_MINGTANG_DAHONGHU);
			}else if(redCardCount == 0){
				mtList.add(LOUDI_MINGTANG_WUHU);
			}
		}
		return mtList;
	}
	public static int calcMingTangFen(DzbpPlayer player, List<Integer> mt){
		if(player == null){
			return 0;
		}
		int fen = 0,huxi = player.getTotalHu();
		DzbpTable table = player.getPlayingTable(DzbpTable.class);
		if(mt == null || mt.isEmpty()){
			fen = huxi;
		}else{
			fen = huxi;
			boolean isMaxFen = false;
			for(int mtId : mt){
				switch(mtId){
					case LOUDI_MINGTANG_YIDIANZHU:
					case LOUDI_MINGTANG_XIAOHONGHU:	
						fen += huxi;
						break;
					case LOUDI_MINGTANG_DAHONGHU:
					case LOUDI_MINGTANG_WUHU:	
						isMaxFen = true;
						fen = 60;
						break;	
				}
			}
			if(mt.contains(LOUDI_MINGTANG_TIANHU) || mt.contains(LOUDI_MINGTANG_DIHU)){
				if(table.getTianDihu() == 1){
					fen += 10;
				}else if(table.getTianDihu() == 2){
					fen *= 2;
				}
			}
			if(mt.contains(LOUDI_MINGTANG_ZIMO)){
				fen += 10;
			}
//			if(!isMaxFen){
//				fen += huxi;
//			}
		}
		return fen;
	}
}
