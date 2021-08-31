package com.sy599.game.qipai.lyzp.rule;


import com.sy599.game.qipai.lyzp.been.CardTypeHuxi;
import com.sy599.game.qipai.lyzp.been.LyzpPlayer;
import com.sy599.game.qipai.lyzp.been.LyzpTable;
import com.sy599.game.qipai.lyzp.constant.PaohzCard;
import com.sy599.game.qipai.lyzp.tool.PaohuziHuLack;
import com.sy599.game.qipai.lyzp.tool.PaohuziTool;

import java.util.ArrayList;
import java.util.List;

public class PaohuziMingTangRule {

	public static final int LOUDI_MINGTANG_ZIMO = 1;//自摸 2番
	public static final int LOUDI_MINGTANG_TIANHU = 2;//天湖 2番
	public static final int LOUDI_MINGTANG_JUSHOU = 3;//举手 2番
	public static final int LOUDI_MINGTANG_YIDIANZHU = 4;//胡牌时只有一张红字，一点朱  2番
	public static final int LOUDI_MINGTANG_DAHONGHU = 5;//胡牌时有12张红字或以上,大红胡 2番
	public static final int LOUDI_MINGTANG_HEIHU = 6;//胡牌时全是黑字，黑胡 2番
	public static final int LOUDI_MINGTANG_WUHU =7; //无胡 胡牌时为0息 21息
	public static final int LOUDI_MINGTANG_XIAOKAHU =8; //小卡胡 胡牌时为10息 16息
	public static final int LOUDI_MINGTANG_DAKAHU =9; //大卡胡 胡牌时为20息 24息
	public static final int LOUDI_MINGTANG_DIAOPAO =10; //点炮


	/**
	 * 简单检测吃遍打边红黑堂
	 * @return
	 */
	public static boolean calcSimpleHht(List<PaohzCard> allCards,LyzpTable table,int huxi){
		List<Integer> mtList = new ArrayList<>();
		List<PaohzCard> redCardList = PaohuziTool.findRedPhzs(allCards);
		int redCardCount = redCardList.size();
		if(huxi>=table.getFloorValue()){
			if(redCardCount == 1&&table.getNoYidianZhu()==0){
				mtList.add(LOUDI_MINGTANG_YIDIANZHU);
			}else if(redCardCount > 12){
				mtList.add(LOUDI_MINGTANG_DAHONGHU);
			}else if(redCardCount == 0){
				mtList.add(LOUDI_MINGTANG_HEIHU);
			}
		}else if(huxi==0&&table.getNoWuHu()==0){
			mtList.add(LOUDI_MINGTANG_HEIHU);
		}
		if (mtList.size()>0)
			return true;
		return false;
	}


	/**
	 * 名堂
	 * @param player 胡牌玩家
	 * @return
	 */
	public static List<Integer> calcMingTang(LyzpPlayer player, PaohuziHuLack hu,LyzpTable table){
		List<Integer> mtList = new ArrayList<>();
		List<PaohzCard> allCards = new ArrayList<>();
		if(player.getDisNum()==0&&player.getTiSize()<=1){
			mtList.add(LOUDI_MINGTANG_JUSHOU);
		}
		if((table.getDisOrMo()==2&&table.getMoSeat()==player.getSeat())||table.isTianHu()){
			mtList.add(LOUDI_MINGTANG_ZIMO);
		}
		for (CardTypeHuxi type : player.getCardTypes()) {
			allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
		}
		if (hu != null && hu.getPhzHuCards() != null) {
			for (CardTypeHuxi type : hu.getPhzHuCards()) {
				allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
			}
			// 跑胡时，将跑的牌放入牌队中计算明堂
			if(hu.getPaohuList() != null && hu.getPaohuList().size() > 0) {
				for(PaohzCard card : hu.getPaoHuCards()){
					if(!allCards.contains(card)){
						allCards.add(card);
					}
				}
			}
		}
		List<PaohzCard> redCardList = PaohuziTool.findRedPhzs(allCards);
		int redCardCount = redCardList.size();
		if(redCardCount == 1&&table.getNoYidianZhu()==0){
			mtList.add(LOUDI_MINGTANG_YIDIANZHU);
		}else if(redCardCount > 12){
			mtList.add(LOUDI_MINGTANG_DAHONGHU);
		}else if(redCardCount == 0){
			mtList.add(LOUDI_MINGTANG_HEIHU);
		}

		int allHuxi = player.getOutHuxi()+player.getZaiHuxi()+hu.getHuxi();
		//判断20,30卡
		if (allHuxi==20){
			mtList.add(LOUDI_MINGTANG_DAKAHU);
		} else if (allHuxi==10){
			mtList.add(LOUDI_MINGTANG_XIAOKAHU);
		} else if (allHuxi==0&&table.getNoWuHu()==0){
			mtList.add(LOUDI_MINGTANG_WUHU);
		}
		return mtList;
	}

	public static int countXiTun(int huxi, List<Integer> mt) {
		int bei=0;
		for(int mtId : mt){
			switch(mtId){
				case LOUDI_MINGTANG_WUHU:
					huxi=21;
					break;
				case LOUDI_MINGTANG_XIAOKAHU:
					huxi=16;
					break;
				case LOUDI_MINGTANG_DAKAHU:
					huxi=24;
					break;
			}
			switch(mtId){
				case LOUDI_MINGTANG_TIANHU:
				case LOUDI_MINGTANG_JUSHOU:
				case LOUDI_MINGTANG_YIDIANZHU:
				case LOUDI_MINGTANG_DAHONGHU:
				case LOUDI_MINGTANG_HEIHU:
					bei+=2;
					break;
			}
		}
		if(bei!=0)
			huxi*=bei;
		int tun=0;
		if(huxi>10&&huxi<=15){
			tun=1;
		}else if(huxi>=16&&huxi<=19){
			tun=2;
		}else if(huxi>=21){
			tun=(huxi-21)/3+3;
		}
		return tun;
	}

	public static boolean isHht(List<Integer> mt){
		if(mt.size()==0)
			return false;
		if(mt.contains(LOUDI_MINGTANG_YIDIANZHU)||
				mt.contains(LOUDI_MINGTANG_DAHONGHU)||
				mt.contains(LOUDI_MINGTANG_HEIHU)||
				mt.contains(LOUDI_MINGTANG_WUHU))
			return true;
		return false;
	}

}
