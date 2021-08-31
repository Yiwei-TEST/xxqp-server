package com.sy599.game.qipai.glphz.rule;


import java.util.ArrayList;
import java.util.List;

import com.sy599.game.qipai.glphz.bean.CardTypeHuxi;
import com.sy599.game.qipai.glphz.bean.GlphzPlayer;
import com.sy599.game.qipai.glphz.bean.GlphzTable;
import com.sy599.game.qipai.glphz.constant.PaohzCard;
import com.sy599.game.qipai.glphz.tool.PaohuziHuLack;
import com.sy599.game.qipai.glphz.tool.PaohuziTool;

public class PaohuziMingTangRule {

	public static final int LOUDI_MINGTANG_ZIMO = 1;//自摸         2番
	public static final int LOUDI_MINGTANG_XIAOHONG = 2;//小红胡   2番 胡牌时红牌数量为10-12张胡牌时
	public static final int LOUDI_MINGTANG_DAHONG = 3;//大红胡     4番 胡牌时红牌大于等于13张
	public static final int LOUDI_MINGTANG_YIDIANHONG = 4;//一点红 3番 胡牌时有且仅只有一张红牌
	public static final int LOUDI_MINGTANG_HEIHU = 5;//黑胡        5番 胡牌时全部为黑色牌
	public static final int LOUDI_MINGTANG_DIHU = 6;//地胡         2番
	public static final int LOUDI_MINGTANG_TIANHU = 7;//天胡       2番
	public static final int LOUDI_MINGTANG_PIAOHU = 8;//飘胡       15息
	public static final int LOUDI_MINGTANG_HAIDI = 9;//海底胡      2番
	public static final int LOUDI_MINGTANG_SHIHONG = 10;//十红     3番 每多一红，+3息
	public static final int LOUDI_MINGTANG_FANGPAO = 11;//放炮     番数看勾选项



	/**
	 * 名堂
	 * @param player 胡牌玩家
	 * @return
	 */
	public static List<Integer> calcMingTang(GlphzPlayer player, PaohuziHuLack hu,GlphzTable table){
		List<Integer> mtList = new ArrayList<>();
		List<PaohzCard> allCards = new ArrayList<>();
//		if(table.getHongHei()==1){
//			for (CardTypeHuxi type : player.getCardTypes()) {
//				allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
//			}
//			if (hu != null && hu.getPhzHuCards() != null) {
//				for (CardTypeHuxi type : hu.getPhzHuCards()) {
//					allCards.addAll(PaohuziTool.toPhzCards(type.getCardIds()));
//				}
//			}
//			List<PaohzCard> redCardList = PaohuziTool.findRedPhzs(allCards);
//			int redCardCount = redCardList.size();
//			if(redCardCount == 1){
//				mtList.add(LOUDI_MINGTANG_YIDIANHONG);
//			}else if(redCardCount == 0){
//				mtList.add(LOUDI_MINGTANG_HEIHU);
//			}
//
//			if(table.getShiHong3Bei()==1){
//				if(redCardCount >= 13){
//					mtList.add(LOUDI_MINGTANG_DAHONG);
//				}else if(redCardCount >= 10){
//					mtList.add(LOUDI_MINGTANG_XIAOHONG);
//				}
//			}else {
//				if(redCardCount >= 10){
//					mtList.add(LOUDI_MINGTANG_SHIHONG);
//				}
//			}
//		}
		int totalHu = player.getOutHuxi()+player.getZaiHuxi()+hu.getHuxi();
		if (totalHu==0&&table.getPiaoHu()==1){
			mtList.add(LOUDI_MINGTANG_PIAOHU);
		}
//		if(table.getLeftCards().size()==0&&table.getHaiDiHu()==1)
//			mtList.add(LOUDI_MINGTANG_HAIDI);

		if(table.getDisOrMo()==2&&player.getSeat() == table.getMoSeat()&&table.getZiMo()==1){
			mtList.add(LOUDI_MINGTANG_ZIMO);
		}
//		if(table.getDisNum()==0&&table.getMoNum()==0){
//			if(table.getTianHu()==1){
//				mtList.add(LOUDI_MINGTANG_TIANHU);
//			}
//			if(table.getZiMo()==1)
//				mtList.add(LOUDI_MINGTANG_ZIMO);
//		}
//		if(table.getDisOrMo()==1){
//			if(table.getDisNum()==1&&table.getMoNum()==0&&player.getDisNum()==0&&table.getDiHu()==1) {
//				mtList.add(LOUDI_MINGTANG_DIHU);
//			}
//			mtList.add(LOUDI_MINGTANG_FANGPAO);
//		}
		return mtList;
	}

	public static int countXiTun(int tun, List<Integer> mts,GlphzTable table) {
		if(mts==null||mts.size()==0)
			return tun;
		int fan=1;
		for (Integer mt:mts) {
			switch (mt){
				case LOUDI_MINGTANG_TIANHU:
				case LOUDI_MINGTANG_DIHU:
				case LOUDI_MINGTANG_HAIDI:
					fan*=2;
					break;
				case LOUDI_MINGTANG_XIAOHONG:
				case LOUDI_MINGTANG_SHIHONG:
					fan*=3;
					break;
				case LOUDI_MINGTANG_DAHONG:
				case LOUDI_MINGTANG_HEIHU:
					fan*=5;
					break;
//				case LOUDI_MINGTANG_YIDIANHONG:
//					if(table.getYiDianHong3Bei()==1)
//						fan*=3;
//					else
//						fan*=4;
//					break;
			}
		}
		if(fan<1)
			return tun;
		return tun*fan;
	}

}
