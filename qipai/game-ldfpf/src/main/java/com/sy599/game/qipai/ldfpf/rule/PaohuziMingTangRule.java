package com.sy599.game.qipai.ldfpf.rule;


import com.sy599.game.qipai.ldfpf.been.CardTypeHuxi;
import com.sy599.game.qipai.ldfpf.been.LdfpfPlayer;
import com.sy599.game.qipai.ldfpf.been.LdfpfTable;
import com.sy599.game.qipai.ldfpf.been.PaohzDisAction;
import com.sy599.game.qipai.ldfpf.constant.PaohzCard;
import com.sy599.game.qipai.ldfpf.tool.PaohuziHuLack;
import com.sy599.game.qipai.ldfpf.tool.PaohuziTool;
import com.sy599.game.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class PaohuziMingTangRule {

	public static final int LOUDI_MINGTANG_TIANHU = 1;//天胡 100
	public static final int LOUDI_MINGTANG_DIHU = 2;//地胡 100
	public static final int LOUDI_MINGTANG_ZIMO = 3;//自摸 *2
	public static final int LOUDI_MINGTANG_YIDIANZHU = 4;//胡牌时只有一张红字，一点朱  X2
	public static final int LOUDI_MINGTANG_XIAOHONGHU = 5;//胡牌时有10-12张红字，小红胡  X2
	public static final int LOUDI_MINGTANG_DAHONGHU = 6;//胡牌时有13张红字或以上,大红胡 +100
	public static final int LOUDI_MINGTANG_WUHU = 7;//胡牌时全是黑字，乌胡 +100
	public static final int LOUDI_MINGTANG_YIKUAIBIAN = 8;//一块匾 手牌和下坎牌有且只有一个门子的红字 X2
	public static final int LOUDI_MINGTANG_HAIDILAO = 9;//海底捞  X2
	public static final int LOUDI_MINGTANG_20KA = 10;//20卡 正好胡20息 X2
	public static final int LOUDI_MINGTANG_30KA = 11;//30卡 正好胡30息 +100
	public static final int LOUDI_MINGTANG_PIAOHU =12; //飘胡 胡牌时为0息 胡30息
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
	public static List<Integer> calcMingTang(LdfpfPlayer player, PaohuziHuLack hu){
		List<Integer> mtList = new ArrayList<>();
		List<PaohzCard> allCards = new ArrayList<>();
		for (CardTypeHuxi type : player.getCardTypes()) {
			allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
		}
		if (hu != null) {
		    if(hu.getPhzHuCards() != null) {
                for (CardTypeHuxi type : hu.getPhzHuCards()) {
                    allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
                }
            }
            // 跑胡时，将跑的牌放入牌队中计算明堂
            if(hu.getPaoHuCards() != null && hu.getPaoHuCards().size() > 0) {
                for(PaohzCard card : hu.getPaoHuCards()){
                    if(!allCards.contains(card)){
                        allCards.add(card);
                    }
                }
            }
		}
		LdfpfTable table = player.getPlayingTable(LdfpfTable.class);

		boolean isTianHu= player.getPlayingTable(LdfpfTable.class).getFirstCard();
		boolean isSelfMo = player.getSeat() == table.getMoSeat()&&table.isMoFlag();
		if (table.getFirstCard() && player.getSeat() != table.getLastWinSeat()){
			mtList.add(LOUDI_MINGTANG_DIHU);
		}else if (isTianHu && player.getSeat() == table.getLastWinSeat()){
			mtList.add(LOUDI_MINGTANG_TIANHU);
		}else if (isSelfMo && !isTianHu && !mtList.contains(LOUDI_MINGTANG_DIHU)){
			mtList.add(LOUDI_MINGTANG_ZIMO);
		}
		//获取剩余底牌
		List<PaohzCard> leftCards = player.getPlayingTable(LdfpfTable.class).getLeftCards();
		if (leftCards.size()==0){
			mtList.add(LOUDI_MINGTANG_HAIDILAO);
		}
		//判定红黑堂
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
		}else if(redCardCount>=2&&redCardCount<=4){
			//判断是否为一块匾
			if (isYiKuaiBian(redCardCount,player,hu)){
				mtList.add(LOUDI_MINGTANG_YIKUAIBIAN);
			}
		}
		//判断20,30卡
		int totalHu = player.getOutHuxi()+player.getZaiHuxi()+hu.getHuxi();
		if (totalHu==30){
			mtList.add(LOUDI_MINGTANG_30KA);
		} else if (totalHu==20){
			mtList.add(LOUDI_MINGTANG_20KA);
		} else if (totalHu==0&&player.getPlayingTable(LdfpfTable.class).isPiaoHu()){
			mtList.add(LOUDI_MINGTANG_PIAOHU);
		}
		return mtList;
	}

	private  static boolean isYiKuaiBian(int redCardCount,LdfpfPlayer player,PaohuziHuLack hu){
		int count=0;
		List<CardTypeHuxi> pCards = player.getCardTypes();
		List<CardTypeHuxi> huCards = hu.getPhzHuCards();
		switch (redCardCount){
			case 2://只可能为手牌剩下的一对
				if(huCards==null||huCards.isEmpty())
					return false;
				for (CardTypeHuxi type : hu.getPhzHuCards()){
					if (type.getCardIds().size()!=2)
						continue;
					List<PaohzCard> handCards = PaohuziTool.toPhzCards(type.getCardIds());
					for (int i = 0; i < handCards.size(); i++) {
						if (PaohuziTool.c2710List.contains(handCards.get(i).getPai())) {
							count++;
						}
					}
					if(count!=0&&count!=2)
						return false;
					if (count==2)
						return true;
				}
				break;
			case 3:
				if(pCards!=null&&!pCards.isEmpty()){
					for (CardTypeHuxi type : pCards) {
						if (type.getCardIds().size()!=3)
							continue;
						List<PaohzCard> disCards = PaohuziTool.toPhzCards(type.getCardIds());
						for (int i = 0; i < disCards.size(); i++) {
							if (PaohuziTool.c2710List.contains(disCards.get(i).getPai())) {
								count++;
							}
						}
						if(count!=0&&count!=3)
							return false;
						if (count==3)
							return true;
					}
				}

				for (CardTypeHuxi type : huCards){
					if (type.getCardIds().size()!=3)
						continue;
					List<PaohzCard> handCards = PaohuziTool.toPhzCards(type.getCardIds());
					for (int i = 0; i < handCards.size(); i++) {
						if (PaohuziTool.c2710List.contains(handCards.get(i).getPai())) {
							count++;
						}
					}
					if(count!=0&&count!=3)
						return false;
					if (count==3)
						return true;
				}
				break;
			case 4://只可能为提和跑
				if (pCards==null||pCards.isEmpty())
					return false;
				for (CardTypeHuxi type : pCards) {
					if(type.getAction()!= PaohzDisAction.action_ti&&type.getAction()!=PaohzDisAction.action_pao)
						continue;
					List<PaohzCard> paohzCards = PaohuziTool.toPhzCards(type.getCardIds());
					for (int i = 0; i < paohzCards.size(); i++) {
						if (PaohuziTool.c2710List.contains(paohzCards.get(i).getPai())) {
							count++;
						}
					}
				}
				if (count==4)
					return true;
				break;
		}
		return false;
	}


	/**
	 *
	 * @param allHuxi
	 * @param mt 可胡特殊牌型List
	 * @param capping 胡息封顶 通常为200,400，怎除自摸外，最多只能胡100,200,只有自摸可到200,400
	 * @return
	 */
	public static int calcMingTangFen(int allHuxi, List<Integer> mt,Integer capping,boolean boomFlag){
		int huxi = allHuxi;
		boolean huXi100=false;
		if(mt == null || mt.isEmpty()){
			return huxi;
		}else{
			//天湖地胡按100息算，其他名堂先加后乘
			for(int mtId : mt){
				switch(mtId){
					case LOUDI_MINGTANG_TIANHU:
					case LOUDI_MINGTANG_DIHU:
						return 100;
					case LOUDI_MINGTANG_PIAOHU:
						huxi= 30;
						break;

				}
			}
			for(int mtId : mt){
				switch(mtId){
					case LOUDI_MINGTANG_DAHONGHU:
					case LOUDI_MINGTANG_WUHU:
					case LOUDI_MINGTANG_30KA:
						if (huXi100){
							huxi+=100;
						}else {
							huxi=100;
						}
						huXi100=true;
						break;
				}
			}
			for(int mtId : mt){
				switch(mtId){
					case LOUDI_MINGTANG_YIDIANZHU:
					case LOUDI_MINGTANG_XIAOHONGHU:
					case LOUDI_MINGTANG_YIKUAIBIAN:
					case LOUDI_MINGTANG_HAIDILAO:
					case LOUDI_MINGTANG_20KA:
					case LOUDI_MINGTANG_ZIMO:
						huxi*=2;
						break;
				}
			}
//			if (capping>0&&huxi>=capping){
//				huxi=capping;
//			}
			if(boomFlag&&huxi>=100){
				//放炮最多胡100
				huxi=100;
			}

		}
		return huxi;
	}
}
