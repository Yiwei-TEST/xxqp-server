package com.sy599.game.qipai.yiyangwhz.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sy599.game.qipai.yiyangwhz.bean.YyWhzCardTypeHuxi;
import com.sy599.game.qipai.yiyangwhz.bean.YyWhzDisAction;
import com.sy599.game.qipai.yiyangwhz.bean.YyWhzPlayer;
import com.sy599.game.qipai.yiyangwhz.bean.YyWhzTable;
import com.sy599.game.qipai.yiyangwhz.constant.YyWhzCard;
import com.sy599.game.qipai.yiyangwhz.tool.YyWhzHuLack;
import com.sy599.game.qipai.yiyangwhz.tool.YyWhzTool;
import com.sy599.game.util.LogUtil;

/**
 * 鬼胡子大胡规则（名堂）
 * 0项项息 1无息平 2对子胡 3黑胡 4黑对子胡 5一点朱 6十三火 8，全球人  9十对 10大字胡 11小字胡 12海底 13天胡 14报听 15背靠背 16手牵手
 */
public class YyWhzMingTangRule {
/**
 * 1 对子息：对子息：胡牌全部为碰或歪或溜或飘的牌，最后计分结算分数时x8
 * 2 大字胡：胡牌时全部为大字，最后计分结算分数时x8
 * 3 小子胡：胡牌时全部为小字，最后计分结算分数时x8
 * 4 火火翻：胡牌时有10个红字，最后计分结算分数时x2；11个红字，最后计分结算分数时x4；12个红字，最后计分结算分数时x8；以此类推，多一个红字翻一翻。
 * 5 行行息：没有顺子，句句是息，最后计分结算分数时x4
 * 6 黑胡子：胡牌时没有一个红字，最后计分结算分数时x8
 * 7 黑对子胡：胡牌时全部为碰或歪或溜，且没有一个红字，胡牌算对子息x黑胡子（8x4=32）。
 * 8 天地胡：庄家起手胡牌，算天胡；闲家胡庄家打出的第一张牌，算地胡；天地胡胡牌最后计算分数时x4.
 * 9 一点红：胡牌时，手上只有一个红字，最后计算分数x4.
 * 10 海底捞：海底捞胡牌后x4（勾选）
 * 11 报听：庄家起手打出一张牌后听牌，闲家起手听牌，胡牌后计算分数时x4.（飘了不算报听）
 * 12 神腰：玩家胡牌后，如果胡牌的那张牌的下一张翻开后和第一局牌的亮牌都是同为单数或者双数，最后计分结算分数时x2。海底时海底牌为神牌。
 * 13 单调胡牌：手里剩一张后胡牌（客户端不管这个）
 * 14 单漂：结算时只有一组红色的牌，其他全为黑，*2倍；
   15 双漂：结算时只有两组红色的牌，其他全为黑，*2倍；
   16 印：只有1组红色的牌，另外还有1个红色的牌，其他均为黑，*2倍；
   17 花胡子：七项牌，每一项中都要带“二七十”或“贰柒拾”任意一张或多张，最后计算分数*64
 */
	public static final int duiZiHu = 1;
	public static final int daZiHu = 2;
	public static final int xiaoZiHu = 3;
	public static final int shiSanHuo = 4;
    public static final int xiangXiangXi = 5;
    public static final int heiHu = 6;
    public static final int heiDuiZiHu = 7;
    public static final int tianHu = 8;
    public static final int yiDianZhu = 9;
    public static final int haiDi = 10;
    public static final int baoTing = 11;
    public static final int shenyao = 12;
    public static final int quanqiuren = 13;
    public static final int danpiao = 14;
    public static final int shuangpiao = 15;
    public static final int yin = 16;
    public static final int huahuzi = 17;
    

    /**
     * 大胡判断（名堂）
     * @return
     */
    public static Map<Integer, Integer> calcMingTang(YyWhzTable nxGhzTable,List<Integer> mtList, YyWhzPlayer player, YyWhzHuLack hu, List<YyWhzCard> allCards, List<YyWhzCardTypeHuxi> allHuxiCards,
    		boolean isBegin,boolean isWeiHouHu,YyWhzCard nowDisCard) {
        List<YyWhzCardTypeHuxi> copyAllHuxiCards = new ArrayList<>(allHuxiCards);
        YyWhzTable table = player.getPlayingTable(YyWhzTable.class);
        boolean duizihu = false;
        List<YyWhzCard> redCardList = YyWhzTool.findRedGhzs(allCards);
        int redCardCount = redCardList.size();
        List<YyWhzCard> copyHandPais = new ArrayList<>(player.getHandGhzs());
        
    	boolean allDuizi = true;
        boolean allXXx = true;
        int hongjuNum = 0;//全红牌型数量
        int hongpaiJu = 0;//全红牌型外带红牌牌型的数量
        int otherHongNum = 0;//全红牌型外的红字数量
        for (YyWhzCardTypeHuxi cardHuxi : copyAllHuxiCards) {
            List<YyWhzCard> cardList = YyWhzTool.toGhzCards(cardHuxi.getCardIds());
        	if (!YyWhzTool.isSameCard(cardList)) {
                allDuizi = false;
            }
            if(cardHuxi.getHux() <= 0){
            	allXXx = false;
            }
            boolean isAllHong = true;
            boolean isHong = false;
            int hongNum = 0;
            for (YyWhzCard card:cardList) {
				if(card.isHong()){
					isHong = true;
					hongNum++;
				}else{
					isAllHong = false;
				}
			}
            if(isAllHong){
            	hongjuNum += isAllHong?1:0;
            }else{
            	hongpaiJu += isHong?1:0;
            	otherHongNum += hongNum;
            	}
        }
        
        if(table.isMingtang()){
        	 if(hongjuNum == 1){
             	if(otherHongNum == 0){
             		mtList.add(danpiao);
             	}else if(otherHongNum== 1){
             		mtList.add(yin);
             	}
             }else if(hongjuNum == 2&& otherHongNum ==0){
             	mtList.add(shuangpiao);
             }
        	 if(hongjuNum + hongpaiJu == 7){
        		 mtList.add(huahuzi);
        	 }
        }
        
    	if (allDuizi) {// 对子胡
            mtList.add(duiZiHu);
            duizihu = true;
        }else if(allXXx){
        	mtList.add(xiangXiangXi);//行行息   对子息和行行息不能同时出现
        }
    
        if (redCardCount == 0) {//黑胡
//            if (duizihu) {
//                mtList.remove((Integer)duiZiHu);
//                mtList.add(heiDuiZiHu);
//            } else{
            	mtList.add(heiHu);
//            }
        } else {
            if (redCardCount == 1) {//一点红
                mtList.add(yiDianZhu);
            } else if (redCardCount >= 10) {//火火翻
                mtList.add(shiSanHuo);
                if(redCardCount > 10){
                	mtList.add(100+redCardCount-10);
                }
            }
        }
    	 boolean haidi = (table.getLeftCards().size() == 0) ? true : false;
         if (haidi == true) {
             mtList.add(haiDi);//海底
         }
    
        
        if(table.isDaxiaozihu()){
        	List<YyWhzCard> allSmallCards = YyWhzTool.findSmallGhzs(allCards);// 找出所有小字
            if (allSmallCards.size() == 0) {
                mtList.add(daZiHu);// 大字胡
            }
            if (allSmallCards != null && allSmallCards.size() == allCards.size()) {
                mtList.add(xiaoZiHu);// 小字胡
            }
        }
//        if (copyHandPais.size() == 1 && nowDisCard != null) {
//        	YyWhzCard handCard = copyHandPais.get(0);
//        	if(handCard.getVal() == nowDisCard.getVal()){
//        		mtList.add(quanqiuren); //单调胡牌
//        	}
//        }
//        
        if(table.isTianhuBaoting()){
        	if ((copyHandPais.size() + player.getFirstLiu() * 4) >= 19) {// 起手牌19张或者20张
                mtList.add(baoTing); // 报听
            }else if(isWeiHouHu && (copyHandPais.size() + player.getFirstLiu() * 4) >= 16){
            	mtList.add(baoTing); // 报听
            }
        	
        	if (isBegin == true) {
                 mtList.add(tianHu);//天胡
                 if(mtList.contains(baoTing)){
                 	mtList.remove((Integer) baoTing);
                 }
             } 
//        	 else if(){//闲家胡庄家打出的第一张牌
//            	 mtList.add(tianHu);// 13天胡
//               }
        }
       
        Map<Integer, Integer> yuanMap = getYuanInfos(allCards, copyAllHuxiCards, copyHandPais, hu.getCheckCard(), false);
        if (!mtList.isEmpty()) {
            StringBuilder sb = new StringBuilder("NxGhz");
            sb.append("|").append(table.getId());
            sb.append("|").append(table.getPlayBureau());
            sb.append("|").append(player.getUserId());
            sb.append("|").append(player.getSeat());
            sb.append("|").append(player.isAutoPlay() ? 1 : 0);
            sb.append("|").append("mingTang");
            sb.append("|").append(mtList);
            LogUtil.msgLog.info(sb.toString());
        }
        if (yuanMap.get(1) > 0) {
            LogUtil.d_msg("内圆个数" + yuanMap.get(1));
        }
        if (yuanMap.get(2) > 0) {
            LogUtil.d_msg("外圆个数" + yuanMap.get(2));
        }
        return yuanMap;
    }


    /**
     * 计算内圆、外圆个数    内圆1 外圆2
     *
     * @param allCards
     * @param allHuxiCards
     * @return
     */
    public static Map<Integer, Integer> getYuanInfos(List<YyWhzCard> allCards, List<YyWhzCardTypeHuxi> allHuxiCards, List<YyWhzCard> handCards, YyWhzCard huCard, boolean wuxipingOrJiuduiBanHu) {
        List<YyWhzCardTypeHuxi> copyAllHuxiCards = new ArrayList<>(allHuxiCards);
        Map<Integer, Integer> yuanMap = new HashMap<>();// 内圆1 外圆2 内圆外圆个数
        yuanMap.put(1, 0);
        yuanMap.put(2, 0);
        if (!wuxipingOrJiuduiBanHu) {// 无息平 不进行内圆 外圆计算
            Map<Integer, List<Integer>> card4Map = new HashMap<>();// 从胡牌牌组中查看四张的牌
            YyWhzCardIndexArr valArr = YyWhzTool.getMax(allCards);
            YyWhzIndex index3 = valArr.getPaohzCardIndex(3);// 四张的牌
            if (index3 != null) {
                for (int val : index3.getPaohzValMap().keySet()) {
                    List<YyWhzCard> cards = index3.getPaohzValMap().get(val);
                    List<Integer> ids = YyWhzTool.toGhzCardIds(cards);
                    card4Map.put(val, ids);
                }
            }
            if (!card4Map.isEmpty()) {// 有四张的牌
                List<Integer> filterCards = new ArrayList<>();
                filterCards.addAll(YyWhzTool.toGhzCardIds(handCards));// 手牌 加上 偎 溜 吃门子的牌
                for (YyWhzCardTypeHuxi huxiCard : copyAllHuxiCards) {
                    if (huxiCard.getAction() == YyWhzDisAction.action_chi || huxiCard.getAction() == YyWhzDisAction.action_shunChi) {
                        filterCards.addAll(huxiCard.getCardIds().subList(1, huxiCard.getCardIds().size()));
                    } else if (huxiCard.getAction() == YyWhzDisAction.action_wei || huxiCard.getAction() == YyWhzDisAction.action_liu || huxiCard.getAction() == YyWhzDisAction.action_weiHouLiu) {
                        filterCards.addAll(huxiCard.getCardIds());
                    }
                }
                for (int val : card4Map.keySet()) {
                    List<Integer> card4s = card4Map.get(val);
                    int num = 0;
                    for (int cardId : card4s) {
                        if (filterCards.contains(new Integer(cardId))) {
                            num++;
                        }
                    }
                    if (num == 4) {// 内圆
                        yuanMap.put(1, yuanMap.get(1) + 1);
                    } else {// 外圆
                        yuanMap.put(2, yuanMap.get(2) + 1);
                    }
                }
            }
        }
        return yuanMap;
    }

    /**
     * 是否有外圆
     *
     * @param allCards
     * @param allHuxiCards
     * @param handCards
     * @param huCard
     * @return
     */
    public static boolean hasWaiYuan(List<YyWhzCard> allCards, List<YyWhzCardTypeHuxi> allHuxiCards, List<YyWhzCard> handCards, YyWhzCard huCard) {
        Map<Integer, Integer> yuanMap = getYuanInfos(allCards, allHuxiCards, handCards, huCard, false);
        if (yuanMap.get(2) > 0 || yuanMap.get(1) > 0) {
            return true;
        } else
            return false;
    }

    /**
     * 是否有小字胡 大字胡 一点朱 火胡 黑胡 海底只需满足五息牌 不需要满足有坎或者有偎
     *
     * @param allCards
     * @return
     */
    public static boolean hasDahu(List<YyWhzCard> handCards, List<YyWhzCard> allCards, boolean isHaiDi, boolean canDiaoDiaoShou) {
        if (isHaiDi) { // 12海底
            return true;
        }
        if (canDiaoDiaoShou && handCards.size() <= 2) {// 17吊吊手
            return true;
        }
        List<YyWhzCard> redCardList = YyWhzTool.findRedGhzs(allCards);
        int redCardCount = redCardList.size();
        if (redCardCount == 0) {// 3黑胡
            return true;
        } else {
            if (redCardCount == 1) {// 5一点朱
                return true;
            } else if (redCardCount == 13) {// 6十三火
                return true;
            } else if (redCardCount == 14) {// 7十四火
                return true;
            } else if (redCardCount >= 15) {// 8十五火
                return true;
            }
        }
        List<YyWhzCard> allSmallCards = YyWhzTool.findSmallGhzs(allCards);// 找出所有小字
        if (allSmallCards.size() == 0) {// 大字胡
            return true;
        }
        if (allSmallCards != null && allSmallCards.size() == allCards.size()) {// 小字胡
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
        for (int i = 0; i < dahus.size(); i++) {
            int disAction = dahus.get(i);
            switch (disAction) {//0溜  1漂 2偎 3胡 4 碰 5吃
                case 0:
                    sb.append("行行息");
                    break;
                case 1:
                    sb.append("多息");
                    break;
                case 2:
                    sb.append("对子息 ");
                    break;
                case 3:
                    sb.append("全黑胡 ");
                    break;
                case 4:
                    sb.append("乌对胡");
                    break;
                case 5:
                    sb.append("一点红");
                    break;
                case 6:
                    sb.append("十三红");
                    break;
                case 8:
                    sb.append("全求人");
                    break;
                case 9:
                    sb.append("十对");
                    break;
                case 10:
                    sb.append("大字胡 ");
                    break;
                case 11:
                    sb.append("小字胡 ");
                    break;
                case 12:
                    sb.append("海底胡 ");
                    break;
                case 13:
                    sb.append("天胡 ");
                    break;
                case 14:
                    sb.append("报听 ");
                    break;
                case 15:
                    sb.append("背靠背");
                    break;
                case 16:
                    sb.append("手牵手");
                    break;
                default:
                    break;
            }
        }
        if (yuanMap.get(1) > 0) {
            sb.append("内豪x" + yuanMap.get(1) + " ");
        }
        if (yuanMap.get(2) > 0) {
            sb.append("外豪x" + yuanMap.get(2) + " ");
        }
        return sb.toString();
    }
}
