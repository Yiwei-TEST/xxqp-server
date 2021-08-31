package com.sy599.game.qipai.zzzp.rule;


import com.sy599.game.qipai.zzzp.been.CardTypeHuxi;
import com.sy599.game.qipai.zzzp.been.ZzzpPlayer;
import com.sy599.game.qipai.zzzp.been.ZzzpTable;
import com.sy599.game.qipai.zzzp.constant.PaohzCard;
import com.sy599.game.qipai.zzzp.tool.PaohuziHuLack;
import com.sy599.game.qipai.zzzp.tool.PaohuziTool;

import java.util.ArrayList;
import java.util.List;

public class PaohuziMingTangRule {

	public static final int LOUDI_MINGTANG_ZIMO = 1;//自摸 2番
	public static final int LOUDI_MINGTANG_MAOHU =2; //毛胡 胡牌时为0息 胡15息
	public static final int LOUDI_MINGTANG_YIDIANZHU = 3;//胡牌时只有一张红字，一点朱  4番
	public static final int LOUDI_MINGTANG_XIAOHONGHU = 4;//胡牌时有10-12张红字，小红胡  3番
	public static final int LOUDI_MINGTANG_DAHONGHU = 5;//胡牌时有13张红字或以上,大红胡 5翻
	public static final int LOUDI_MINGTANG_WUHU = 6;//胡牌时全是黑字，乌胡 5翻
	public static final int LOUDI_MINGTANG_FANGPAO = 7;//放炮胡牌，胡息 多少人多少番


	/**
	 * 名堂
	 * @param player 胡牌玩家
	 * @return
	 */
	public static List<Integer> calcMingTang(ZzzpPlayer player, PaohuziHuLack hu,boolean fangPao){
		List<Integer> mtList = new ArrayList<>();
		List<PaohzCard> allCards = new ArrayList<>();
		ZzzpTable table = player.getPlayingTable(ZzzpTable.class);
		if(fangPao){
			mtList.add(LOUDI_MINGTANG_FANGPAO);
		}else if(player.getSeat() == table.getMoSeat()){
			mtList.add(LOUDI_MINGTANG_ZIMO);
		}
		if(table.getMingTang()>1){
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
			if(redCardCount == 1){
				mtList.add(LOUDI_MINGTANG_YIDIANZHU);
			}else if(redCardCount >= 10 && redCardCount<=12){
				mtList.add(LOUDI_MINGTANG_XIAOHONGHU);
			}else if(redCardCount >= 13){
				mtList.add(LOUDI_MINGTANG_DAHONGHU);
			}else if(redCardCount == 0){
				mtList.add(LOUDI_MINGTANG_WUHU);
			}
		}
		int allHuxi = player.getOutHuxi()+player.getZaiHuxi()+hu.getHuxi();
		if(table.getMaoHu()==1&&allHuxi==0)
			mtList.add(LOUDI_MINGTANG_MAOHU);
		return mtList;
	}



	public static int countXiTun(int tun, List<Integer> mt,Integer mingTang ,int playerCount) {
		int lastTun = tun;
		if(mt == null || mt.isEmpty()){
			return lastTun;
		}else{
			int bei=1;
			for(int mtId : mt){
				if(mingTang==1){
					switch(mtId){
						case LOUDI_MINGTANG_ZIMO:
							bei*=2;
							break;
						case LOUDI_MINGTANG_FANGPAO:
							bei*=playerCount;
							break;
					}
				}
				if(mingTang==2){
					switch(mtId){
						case LOUDI_MINGTANG_ZIMO:
							bei*=2;
							break;
						case LOUDI_MINGTANG_DAHONGHU:
						case LOUDI_MINGTANG_WUHU:
							bei*=5;
							break;
						case LOUDI_MINGTANG_YIDIANZHU:
							bei*=4;
							break;
						case LOUDI_MINGTANG_XIAOHONGHU:
							bei*=3;
							break;
						case LOUDI_MINGTANG_FANGPAO:
							bei*=playerCount;
							break;
					}
				}else if(mingTang==3){
					switch(mtId){
						case LOUDI_MINGTANG_ZIMO:
						case LOUDI_MINGTANG_DAHONGHU:
						case LOUDI_MINGTANG_WUHU:
						case LOUDI_MINGTANG_YIDIANZHU:
						case LOUDI_MINGTANG_XIAOHONGHU:
							bei*=2;
							break;
						case LOUDI_MINGTANG_FANGPAO:
							bei*=playerCount;
							break;
					}
				}
			}
			lastTun*=bei;
		}
		return lastTun;
	}

}
