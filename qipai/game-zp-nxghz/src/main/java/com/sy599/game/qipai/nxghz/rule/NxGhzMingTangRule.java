package com.sy599.game.qipai.nxghz.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sy599.game.qipai.nxghz.bean.NxGhzCardTypeHuxi;
import com.sy599.game.qipai.nxghz.bean.NxGhzDisAction;
import com.sy599.game.qipai.nxghz.bean.NxGhzPlayer;
import com.sy599.game.qipai.nxghz.bean.NxGhzTable;
import com.sy599.game.qipai.nxghz.constant.NxGhzCard;
import com.sy599.game.qipai.nxghz.tool.NxGhzHuLack;
import com.sy599.game.qipai.nxghz.tool.NxGhzTool;
import com.sy599.game.util.LogUtil;

/**
 * 鬼胡子大胡规则（名堂）
 * 0项项息 1无息平 2对子胡 3黑胡 4黑对子胡 5一点朱 6十三火 8，全球人  9十对 10大字胡 11小字胡 12海底 13天胡 14报听 15背靠背 16手牵手
 */
public class NxGhzMingTangRule {
/**
 * 13 天胡：庄家起手满足5硬息七方门子直接胡牌
   12 海底胡：胡最后一张摸起的牌
	2    对子息：全为碰、坎、歪、溜、对子（类似麻将碰碰胡）
    1 多息：胡牌有6个硬息
   10 大字胡：全大字胡牌
   11 小字胡：全小字胡牌
	6   十三红：有13个红字，80息，每多一红加10息
	3   全黑胡：没有红字胡牌
	0  行行息：每组牌都有息
	5  一点红：只有一个红字胡牌
	4  乌对胡：碰碰胡且没有红字
	9  十对：有十组对子，不用成牌不用满足5息
   14 报听(庄家为打出第一张后听牌)，没打出过手牌情况下胡牌。可溜，歪后立即胡和歪后溜不算动张，仍为报听
	8   全求人：手里剩一张后胡牌
	15 背靠背
	16 手牵手

 */
    public static final int xiangXiangXi = 0;
    public static final int duoxi = 1;
    public static final int duiZiHu = 2;
    public static final int heiHu = 3;
    public static final int heiDuiZiHu = 4;
    public static final int yiDianZhu = 5;
    public static final int shiSanHuo = 6;
//    public static final int shiSiHuo = 7;
//    public static final int shiWuHuo = 8;
    public static final int quanqiuren = 8;
    public static final int shidui = 9;
    public static final int daZiHu = 10;
    public static final int xiaoZiHu = 11;
    public static final int haiDi = 12;
    public static final int tianHu = 13;
    public static final int baoTing = 14;
    public static final int beikaobei = 15;
    public static final int shouqianshou = 16;
    

    /**
     * 大胡判断（名堂）
     * 0项项息 1无息平 2对子胡 3黑胡 4黑对子胡 5一点朱 6十三火 7十四火 8十五火 9九对半 10大字胡 11小字胡 12海底 13天胡 14报听 15内圆 16外圆 17吊吊手
     *
     * @return
     */
    public static Map<Integer, Integer> calcMingTang(NxGhzTable nxGhzTable,List<Integer> mtList, NxGhzPlayer player, NxGhzHuLack hu, List<NxGhzCard> allCards, List<NxGhzCardTypeHuxi> allHuxiCards, boolean isBegin,boolean isWeiHouHu) {
        List<NxGhzCardTypeHuxi> copyAllHuxiCards = new ArrayList<>(allHuxiCards);
        NxGhzTable table = player.getPlayingTable(NxGhzTable.class);
        boolean duizihu = false;
        List<NxGhzCard> redCardList = NxGhzTool.findRedGhzs(allCards);
        int redCardCount = redCardList.size();
        boolean isWuxi = (hu.getHuxi() >= 5) ? true : false;
        List<NxGhzCard> copyHandPais = new ArrayList<>(player.getHandGhzs());
        if (isWuxi) {// 满足五息胡法规则
            if (hu.getHuxi() == 7) {// 0项项息
                if (NxGhzTool.contains2710(allHuxiCards)) {// 包括2710才是项项息
                    mtList.add(xiangXiangXi);
                }
                boolean allDuizi = true;
                for (NxGhzCardTypeHuxi cardHuxi : copyAllHuxiCards) {
                    if (!NxGhzTool.isSameCard(NxGhzTool.toGhzCards(cardHuxi.getCardIds()))) {
                        allDuizi = false;
                        break;
                    }
                }
                if (allDuizi) {// 2对子胡
                    mtList.add(duiZiHu);
                    duizihu = true;
                }
            }else if (hu.getHuxi() == 6) {// 多息
            	mtList.add(duoxi);
            }
            if (redCardCount == 0) {// 3黑胡
                if (duizihu) {
                    mtList.remove((Integer) 2);
                    mtList.add(heiDuiZiHu);
                } else
                    mtList.add(heiHu);
            } else {
                if (redCardCount == 1) {// 5一点朱
                    mtList.add(yiDianZhu);
                } else if (redCardCount >= 13) {// 6十三火
                    mtList.add(shiSanHuo);
                    if(redCardCount > 13){
                    	mtList.add(100+redCardCount-13);
                    }
                }
            }
            List<NxGhzCard> allSmallCards = NxGhzTool.findSmallGhzs(allCards);// 找出所有小字
            if (allSmallCards.size() == 0) {
                mtList.add(daZiHu);// 10大字胡
            }
            if (allSmallCards != null && allSmallCards.size() == allCards.size()) {
                mtList.add(xiaoZiHu);// 11小字胡
            }
//            if (table.isBaoTingHuSwitchTemp()) {
//                // 没有出过牌的算报听胡
//                if (!player.isDisCardForBaoTingHu()) {
//                    mtList.add(baoTing); // 14报听
//                }
//            } else {
                if ((copyHandPais.size() + player.getFirstLiu() * 4) >= 19) {// 起手牌19张或者20张
                    mtList.add(baoTing); // 14报听
                }else if(isWeiHouHu && (copyHandPais.size() + player.getFirstLiu() * 4) >= 16){
                	mtList.add(baoTing); // 14报听
                }
//            }
            if (copyHandPais.size() <= 2) {
                mtList.add(quanqiuren); // 吊吊手
            }
            List<Integer> tingpais = player.getTingpais();
            if( tingpais != null && tingpais.size() ==2){
            	if(nxGhzTable.isShouqianShou()){
            		int i =  tingpais.get(0) - tingpais.get(1);
            		if(i == 1 || i == -1){
            			mtList.add(shouqianshou); //手牵手
            		}
            	}
            	if(nxGhzTable.isBeikaobei()){
            		int v1 = tingpais.get(0);
            		int v2 = tingpais.get(1);
            		if(v1!=v2 && v1%100 == v2%100){
            			mtList.add(beikaobei); //背靠背
            		}
            	}
            }
            
        } else {// 无息平 九对半
            NxGhzCardIndexArr arr = NxGhzTool.getGuihzCardIndexArr(allCards);
            if (arr.getDuiziNum() == 10) {
                mtList.add(shidui);// 十对
            }
            
            List<NxGhzCard> allSmallCards = NxGhzTool.findSmallGhzs(allCards);// 找出所有小字
            if (allSmallCards.size() == 0) {
                mtList.add(daZiHu);// 10大字胡
            }
            if (allSmallCards != null && allSmallCards.size() == allCards.size()) {
                mtList.add(xiaoZiHu);// 11小字胡
            }
            if ((copyHandPais.size() + player.getFirstLiu() * 4) >= 19) {// 起手牌19张或者20张
                mtList.add(baoTing); // 14报听
            }else if(isWeiHouHu && (copyHandPais.size() + player.getFirstLiu() * 4) >= 16){
            	mtList.add(baoTing); // 14报听
            }
            
//            if (copyHandPais.size() >= 19) {// 起手牌19张或者20张
//                if (table.isWuxiping() && hu.getHuxi() == 0 && hu.isHasWupingXi()) {// 1无息平
//                    mtList.add(wuXiPing);
//                }
//            }
        }
        boolean haidi = (table.getLeftCards().size() == 0) ? true : false;
        if (haidi == true) {
            mtList.add(haiDi);// 12海底
        }
        if (isBegin == true) {
            mtList.add(tianHu);// 13天胡
            if(mtList.contains(baoTing)){
            	mtList.remove((Integer) baoTing);
            }
//            if (mtList.contains(jiuDuiBan)) {
//                mtList.remove((Integer) tianHu);
//            }
        }
//        boolean jiuduibanHu = mtList.contains(jiuDuiBan);
//        boolean wuxipingHu = mtList.contains(wuXiPing);
//        Map<Integer, Integer> yuanMap = getYuanInfos(allCards, copyAllHuxiCards, copyHandPais, hu.getCheckCard(), wuxipingHu || jiuduibanHu);
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
    public static Map<Integer, Integer> getYuanInfos(List<NxGhzCard> allCards, List<NxGhzCardTypeHuxi> allHuxiCards, List<NxGhzCard> handCards, NxGhzCard huCard, boolean wuxipingOrJiuduiBanHu) {
        List<NxGhzCardTypeHuxi> copyAllHuxiCards = new ArrayList<>(allHuxiCards);
        Map<Integer, Integer> yuanMap = new HashMap<>();// 内圆1 外圆2 内圆外圆个数
        yuanMap.put(1, 0);
        yuanMap.put(2, 0);
        if (!wuxipingOrJiuduiBanHu) {// 无息平 不进行内圆 外圆计算
            Map<Integer, List<Integer>> card4Map = new HashMap<>();// 从胡牌牌组中查看四张的牌
            NxGhzCardIndexArr valArr = NxGhzTool.getMax(allCards);
            NxGhzIndex index3 = valArr.getPaohzCardIndex(3);// 四张的牌
            if (index3 != null) {
                for (int val : index3.getPaohzValMap().keySet()) {
                    List<NxGhzCard> cards = index3.getPaohzValMap().get(val);
                    List<Integer> ids = NxGhzTool.toGhzCardIds(cards);
                    card4Map.put(val, ids);
                }
            }
            if (!card4Map.isEmpty()) {// 有四张的牌
                List<Integer> filterCards = new ArrayList<>();
                filterCards.addAll(NxGhzTool.toGhzCardIds(handCards));// 手牌 加上 偎 溜 吃门子的牌
                for (NxGhzCardTypeHuxi huxiCard : copyAllHuxiCards) {
                    if (huxiCard.getAction() == NxGhzDisAction.action_chi || huxiCard.getAction() == NxGhzDisAction.action_shunChi) {
                        filterCards.addAll(huxiCard.getCardIds().subList(1, huxiCard.getCardIds().size()));
                    } else if (huxiCard.getAction() == NxGhzDisAction.action_wei || huxiCard.getAction() == NxGhzDisAction.action_liu || huxiCard.getAction() == NxGhzDisAction.action_weiHouLiu) {
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
    public static boolean hasWaiYuan(List<NxGhzCard> allCards, List<NxGhzCardTypeHuxi> allHuxiCards, List<NxGhzCard> handCards, NxGhzCard huCard) {
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
    public static boolean hasDahu(List<NxGhzCard> handCards, List<NxGhzCard> allCards, boolean isHaiDi, boolean canDiaoDiaoShou) {
        if (isHaiDi) { // 12海底
            return true;
        }
        if (canDiaoDiaoShou && handCards.size() <= 2) {// 17吊吊手
            return true;
        }
        List<NxGhzCard> redCardList = NxGhzTool.findRedGhzs(allCards);
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
        List<NxGhzCard> allSmallCards = NxGhzTool.findSmallGhzs(allCards);// 找出所有小字
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
