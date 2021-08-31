package com.sy599.game.qipai.yywhz.rule;

import com.sy599.game.msg.serverPacket.TablePhzResMsg.PhzHuCards;
import com.sy599.game.qipai.yywhz.bean.CardTypeHuxi;
import com.sy599.game.qipai.yywhz.bean.WaihuziPlayer;
import com.sy599.game.qipai.yywhz.bean.WaihuziTable;
import com.sy599.game.qipai.yywhz.bean.WaihzDisAction;
import com.sy599.game.qipai.yywhz.constant.GuihzCard;
import com.sy599.game.qipai.yywhz.tool.GuihuziHuLack;
import com.sy599.game.qipai.yywhz.tool.GuihuziTool;
import com.sy599.game.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuping
 * 鬼胡子大胡规则（名堂）
 * 0项项息 1无息平 2对子胡 3黑胡 4黑对子胡 5一点朱 6十三火 7十四火 8十五火 9九对半 10大字胡 11小字胡 12海底 13天胡 14报听 15内圆 16外圆
 * 项项息    加50息 胡牌时有七息牌，且其中包含二七十或贰柒拾的息
 * 无息平    加50息 起手牌全可组成一句话的顺子牌，听牌无对
 * 对子胡    加100息 胡牌时有七息牌，且所有牌都不为顺子和二七十、贰柒拾
 * 黑胡      加100息 胡牌时所有牌都为黑色
 * 黑对子胡  加200息 胡牌时所有牌都为黑色且为七息非二七十牌
 * 一点朱    加50息 胡牌时只有一张红牌 (包括下的牌)
 * 十三火    加50息 胡牌时有十三张红牌
 * 十四火    加100息 胡牌时有十四张红牌
 * 十五火    加200息 胡牌时有十五张红牌
 * 九对半   加200息 起手有九个对子牌，庄家20张牌打出一张即胡牌，闲家直接胡
 * 大字胡   加100息 胡牌时全是大字牌
 * 小字胡   加100息  胡牌时全是小字牌
 * 海底      息数乘以4 海底最后一张牌胡牌为海底门子
 * 天胡      息数乘以4 起手直接胡牌为天胡
 * 报听     息数乘以4 起手牌庄家打出一张牌或闲家直接听牌
 * 内圆     息数乘以4 胡牌时有溜牌（自己手里或牌里有4张同牌，可以是偎加一句话或坎加一句话）
 * 外圆     息数乘以2 胡牌时有飘牌（自己手里有三张同牌，而另一张牌不是自己摸得牌而得到的）
 */
public class GuihuziMingTangRule {

	/**
	 * 大胡判断（名堂）
	 * 0项项息 1一九对胡 2对子胡 3黑胡 4黑对子胡 5一点朱 6十三火 7十四火 8十五火 9零对或10对 10大字胡 11小字胡 12海底 13天胡 14报听 15内圆 16外圆 17吊吊手
	 * @return
	 */
	public static Map<Integer, Integer> calcMingTang(List<Integer> mtList, WaihuziPlayer player, GuihuziHuLack hu, List<GuihzCard> allCards, List<CardTypeHuxi> allHuxiCards, boolean isBegin) {
		List<CardTypeHuxi> copyAllHuxiCards = new ArrayList<>(allHuxiCards);
		WaihuziTable table = player.getPlayingTable(WaihuziTable.class);
		//boolean duizihu = false;
		List<GuihzCard> redCardList = GuihuziTool.findRedGhzs(allCards);
		int redCardCount = redCardList.size();
		boolean isWuxi = (hu.getHuxi() >= 7) ? true : false;
		List<GuihzCard> copyHandPais = new ArrayList<>(player.getHandGhzs());
		if(isWuxi) {// 满足五息胡法规则
				boolean allDuizi = true;
				for(CardTypeHuxi cardHuxi : copyAllHuxiCards) {
					if(!GuihuziTool.isSameCard(GuihuziTool.toGhzCards(cardHuxi.getCardIds()))) {
						allDuizi = false;
						break;
					}
				}
				if(allDuizi) {// 2对子胡
					mtList.add(2);
				}
				
				

				List<PhzHuCards> huCards = player.buildGhzHuCards();
				boolean huxi= true;
				for(PhzHuCards card: huCards ) {
					if(card.getHuxi()==0){
						huxi=false;
						break;
					}
				}
			if (huxi) {
				if (!mtList.contains(2))
					mtList.add(0);
			}
				
				
//			}

			if(player.getFirstLiu()==0) {// 起手牌19张或者20张
				mtList.add(12); // 14报听
			}
			if(copyHandPais.size() <= 1) {
				mtList.add(13); // 
			}
		} else {
			
			// 无息平 九对半
			GuihzCardIndexArr arr = GuihuziTool.getGuihzCardIndexArr(copyHandPais);
			if(copyHandPais.size() >= 19) {// 起手牌19张或者20张
				if(isBegin == true ) {// 九对半  并且对子数为9
					if(arr.getDuiziNum() == 10 ||arr.getDuiziNum() == 0) {
						mtList.add(6);// 9九对半
					}else if(arr.getDuiziNum() == 9 ||arr.getDuiziNum() == 1) {
						mtList.add(1);
					}
					
					if(arr.getDuizis().size()==1 &&arr.getDuiziNum()==2) {
						mtList.add(1);
					}
				}

			}
		}
		
		if(redCardCount == 0) {// 3黑胡
				mtList.add(3);
		} else {
			if(redCardCount == 1) {// 5一点朱
				mtList.add(4);
			} else if(redCardCount >= 13) {// 6十三火
				mtList.add(5);
				mtList.add(100+redCardCount-13);
			}
		}
		
		List<GuihzCard> allSmallCards = GuihuziTool.findSmallGhzs(allCards);// 找出所有小字
		if(allSmallCards.size() == 0) {
			mtList.add(7);// 10大字胡
		}
		if(allSmallCards != null && allSmallCards.size() == allCards.size()) {
			mtList.add(8);// 11小字胡
		}
		boolean haidi = (table.getLeftCards().size() == 0) ? true : false;
		if(haidi == true) {
			mtList.add(9);// 12海底
		}
		if(isBegin == true&&(!mtList.contains(6)&&!mtList.contains(1))) {
			mtList.add(10);// 13天胡
//			if(!mtList.contains(12)){
//				
//			}
			mtList.remove((Integer)12);
//			mtList.remove((Integer)6);
//			mtList.remove((Integer)1);
//			if(mtList.contains(9)) 
//				mtList.remove((Integer)13);
		}
		
		boolean jiuduibanHu = mtList.contains(9);
		boolean wuxipingHu = mtList.contains(1);
		Map<Integer, Integer> yuanMap = getYuanInfos(allCards, copyAllHuxiCards, copyHandPais, hu.getCheckCard(), table.isSelfMo(player));
		if (!mtList.isEmpty()) {
			LogUtil.d_msg("名堂！！！！" + mtList);
		}
		if(yuanMap.get(1) > 0) {
			LogUtil.d_msg("内圆个数" + yuanMap.get(1));
		}
		if(yuanMap.get(2) > 0) {
			LogUtil.d_msg("外圆个数" + yuanMap.get(2));
		}
		return yuanMap;
	}
	
	
	/**
	 * 计算内圆、外圆个数    内圆1 外圆2
	 * @param allCards
	 * @param allHuxiCards
	 * @return
	 */
	public static Map<Integer, Integer> getYuanInfos(List<GuihzCard> allCards, List<CardTypeHuxi> allHuxiCards, List<GuihzCard> handCards, GuihzCard huCard, boolean zimo) {
		List<CardTypeHuxi> copyAllHuxiCards = new ArrayList<>(allHuxiCards);
		Map<Integer, Integer> yuanMap = new HashMap<>();// 内圆1 外圆2 内圆外圆个数
		yuanMap.put(1, 0);
		yuanMap.put(2, 0);
		yuanMap.put(3, 0);
		yuanMap.put(4, 0);
		// 无息平 不进行内圆 外圆计算
			Map<Integer, List<Integer>> card4Map = new HashMap<>();// 从胡牌牌组中查看四张的牌
			GuihzCardIndexArr valArr = GuihuziTool.getMax(allCards);
			GuihuziIndex index3 = valArr.getPaohzCardIndex(3);// 四张的牌
			if (index3 != null) {
				for (int val : index3.getPaohzValMap().keySet()) {
					List<GuihzCard> cards = index3.getPaohzValMap().get(val);
					List<Integer> ids = GuihuziTool.toGhzCardIds(cards);
					card4Map.put(val, ids);
				}
			}
			if(!card4Map.isEmpty()) {// 有四张的牌
				List<Integer> filterCards = new ArrayList<>();
				List<Integer> filterCards2 = new ArrayList<>();
				filterCards.addAll(GuihuziTool.toGhzCardIds(handCards));// 手牌 加上 偎 溜 吃门子的牌
				for(CardTypeHuxi huxiCard : copyAllHuxiCards) {
					if(huxiCard.getAction() == WaihzDisAction.action_chi || huxiCard.getAction() == WaihzDisAction.action_shunChi ||huxiCard.getAction() == WaihzDisAction.action_peng) {
						filterCards.addAll(huxiCard.getCardIds().subList(1, huxiCard.getCardIds().size()));
					} else if( huxiCard.getAction() == WaihzDisAction.action_liu || huxiCard.getAction() == WaihzDisAction.action_qishouLiu ||huxiCard.getAction()==WaihzDisAction.action_shun ) {
						if(!zimo&& huxiCard.getAction()==WaihzDisAction.action_shun&& (huCard!=null &&huCard.getVal()==GuihzCard.getPaohzCard(huxiCard.getCardIds().get(0)).getVal())) {
							filterCards.addAll(huxiCard.getCardIds().subList(1, huxiCard.getCardIds().size()));
						}else {
							filterCards.addAll(huxiCard.getCardIds());
						}
					}else if(huxiCard.getAction() == WaihzDisAction.action_wei|| huxiCard.getAction() == WaihzDisAction.action_weiHouLiu){
						filterCards.addAll(huxiCard.getCardIds());
						filterCards2.addAll(huxiCard.getCardIds());
					}
				}
				
				
				for(int val : card4Map.keySet()) {
					List<Integer> card4s = card4Map.get(val);
					int num = 0;
					for(int cardId : card4s) {
						if(filterCards.contains(new Integer(cardId))) {
							num ++;
						}
					}
					if(num == 4) {// 内圆
						if(filterCards2.contains(card4s.get(0))) {//内豪
							yuanMap.put(1, yuanMap.get(1) + 1);
						}else{
							yuanMap.put(2, yuanMap.get(2) + 1);//溜豪
						}
						
					} else if(num<=1){// 散豪
						yuanMap.put(3, yuanMap.get(3) + 1);
					}else if(num>=2) {//外豪
						yuanMap.put(4, yuanMap.get(4) + 1);
					}
				}
			}
		return yuanMap;
	}
	
	/**
	 * 是否有外圆
	 * @param allCards
	 * @param allHuxiCards
	 * @param handCards
	 * @param huCard
	 * @return
	 */
	public static boolean hasWaiYuan(List<GuihzCard> allCards, List<CardTypeHuxi> allHuxiCards, List<GuihzCard> handCards, GuihzCard huCard) {
		Map<Integer, Integer> yuanMap = getYuanInfos(allCards, allHuxiCards, handCards, huCard, false);
		
		if(yuanMap.get(2) > 0 || yuanMap.get(1) > 0 || yuanMap.get(3)>0|| yuanMap.get(4)>0) {
			return true;
		} else
			return false;
	}
	
	/**
	 * 是否有小字胡 大字胡 一点朱 火胡 黑胡 海底只需满足五息牌 不需要满足有坎或者有偎
	 * @param allCards
	 * @return
	 */
	public static boolean hasDahu(List<GuihzCard> handCards, WaihuziTable table, List<GuihzCard> allCards) {
		boolean haidi = (table.getLeftCards().size() == 0) ? true : false;
		if(haidi) // 12海底
			return true;
		List<GuihzCard> redCardList = GuihuziTool.findRedGhzs(allCards);
		int redCardCount = redCardList.size();
		if(redCardCount == 0) {// 3黑胡
			return true;
		} else {
			if(redCardCount == 1) {// 5一点朱
				return true;
			} else if(redCardCount>= 13) {// 6十三火
				return true;
			} 
		}
		List<GuihzCard> allSmallCards = GuihuziTool.findSmallGhzs(allCards);// 找出所有小字
		if(allSmallCards.size() == 0) {// 大字胡
			return true;
		}
		if(allSmallCards != null && allSmallCards.size() == allCards.size()) {// 小字胡
			return true;
		}
		return false;
	}
	
	/**
	 * @param dahus 0项项息 1无息平 2对子胡 3黑胡 4黑对子胡 5一点朱 6十三火 7十四火 8十五火 9九对半 10大字胡 11小字胡 12海底 13天胡 14报听 15内圆 16外圆 17吊吊手
	 * @return
	 */
	public static String getDahuNames(List<Integer> dahus, Map<Integer, Integer> yuanMap) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < dahus.size(); i++) {
			int disAction = dahus.get(i);
			switch(disAction) {//0溜  1漂 2偎 3胡 4 碰 5吃
			case 0:
				sb.append("项项息 ");
				break;
			case 1:
				sb.append("无息平 ");
				break;
			case 2:
				sb.append("对子胡 ");
				break;
			case 3:
				sb.append("黑胡 ");
				break;
			case 4:
				sb.append("黑对子胡 ");
				break;
			case 5:
				sb.append("一点朱 ");
				break;
			case 6:
				sb.append("十三火 ");
				break;
			case 7:
				sb.append("十四火 ");
				break;
			case 8:
				sb.append("十五火 ");
				break;
			case 9:
				sb.append("九对半 ");
				break;
			case 10:
				sb.append("大字胡 ");
				break;
			case 11:
				sb.append("小字胡 ");
				break;
			case 12:
				sb.append("海底 ");
				break;
			case 13:
				sb.append("天胡 ");
				break;
			case 14:
				sb.append("报听 ");
				break;
			case 15:
				sb.append("内圆 ");
				break;
			case 16:
				sb.append("外圆 ");
				break;
			case 17:
				sb.append("吊吊手");
				break;
			default:
				break;
			}
		}
		if(yuanMap.get(1) > 0) {
			sb.append("内圆 x" + yuanMap.get(1) + " ");
		}
		if(yuanMap.get(2) > 0) {
			sb.append("外圆 x" + yuanMap.get(2) + " ");
		}
		return sb.toString();
	}
}
