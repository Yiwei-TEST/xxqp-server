package com.sy599.game.qipai.yjghz.rule;

import com.sy599.game.qipai.yjghz.bean.YjGhzCardTypeHuxi;
import com.sy599.game.qipai.yjghz.bean.YjGhzDisAction;
import com.sy599.game.qipai.yjghz.bean.YjGhzPlayer;
import com.sy599.game.qipai.yjghz.bean.YjGhzTable;
import com.sy599.game.qipai.yjghz.constant.YjGhzCard;
import com.sy599.game.qipai.yjghz.tool.YjGhzHuLack;
import com.sy599.game.qipai.yjghz.tool.YjGhzTool;
import com.sy599.game.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 鬼胡子大胡规则（名堂）
 * 0项项息 1无息平 2对子胡 3黑胡 4黑对子胡 5一点朱 6十三火 7十四火 8十五火 9九对半 10大字胡 11小字胡 12海底 13天胡 14报听 15内圆 16外圆
 */
public class YjGhzMingTangRule {

    public static final int xiangXiangXi = 0;
    public static final int wuXiPing = 1;
    public static final int duiZiHu = 2;
    public static final int heiHu = 3;
    public static final int heiDuiZiHu = 4;
    public static final int yiDianZhu = 5;
    public static final int shiSanHuo = 6;
    public static final int shiSiHuo = 7;
    public static final int shiWuHuo = 8;
    public static final int jiuDuiBan = 9;
    public static final int daZiHu = 10;
    public static final int xiaoZiHu = 11;
    public static final int haiDi = 12;
    public static final int tianHu = 13;
    public static final int baoTing = 14;
    public static final int neiYuan = 15;
    public static final int waiYuan = 16;
    public static final int diaoDiaoShou = 17;

    /**
     * 大胡判断（名堂）
     * 0项项息 1无息平 2对子胡 3黑胡 4黑对子胡 5一点朱 6十三火 7十四火 8十五火 9九对半 10大字胡 11小字胡 12海底 13天胡 14报听 15内圆 16外圆 17吊吊手
     *
     * @return
     */
    public static Map<Integer, Integer> calcMingTang(List<Integer> mtList, YjGhzPlayer player, YjGhzHuLack hu, List<YjGhzCard> allCards, List<YjGhzCardTypeHuxi> allHuxiCards, boolean isBegin) {
        List<YjGhzCardTypeHuxi> copyAllHuxiCards = new ArrayList<>(allHuxiCards);
        YjGhzTable table = player.getPlayingTable(YjGhzTable.class);
        boolean duizihu = false;
        List<YjGhzCard> redCardList = YjGhzTool.findRedGhzs(allCards);
        int redCardCount = redCardList.size();
        boolean isWuxi = (hu.getHuxi() >= 5) ? true : false;
        List<YjGhzCard> copyHandPais = new ArrayList<>(player.getHandGhzs());
        if (isWuxi) {// 满足五息胡法规则
            if (hu.getHuxi() == 7) {// 0项项息
                if (YjGhzTool.contains2710(allHuxiCards)) {// 包括2710才是项项息
                    mtList.add(xiangXiangXi);
                }
                boolean allDuizi = true;
                for (YjGhzCardTypeHuxi cardHuxi : copyAllHuxiCards) {
                    if (!YjGhzTool.isSameCard(YjGhzTool.toGhzCards(cardHuxi.getCardIds()))) {
                        allDuizi = false;
                        break;
                    }
                }
                if (allDuizi) {// 2对子胡
                    mtList.add(duiZiHu);
                    duizihu = true;
                }
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
                } else if (redCardCount == 13) {// 6十三火
                    mtList.add(shiSanHuo);
                } else if (redCardCount == 14) {// 7十四火
                    mtList.add(shiSiHuo);
                } else if (redCardCount >= 15) {// 8十五火
                    mtList.add(shiWuHuo);
                }
            }
            List<YjGhzCard> allSmallCards = YjGhzTool.findSmallGhzs(allCards);// 找出所有小字
            if (allSmallCards.size() == 0) {
                mtList.add(daZiHu);// 10大字胡
            }
            if (allSmallCards != null && allSmallCards.size() == allCards.size()) {
                mtList.add(xiaoZiHu);// 11小字胡
            }
            if (table.isBaoTingHuSwitchTemp()) {
                // 没有出过牌的算报听胡
                if (!player.isDisCardForBaoTingHu()) {
                    mtList.add(baoTing); // 14报听
                }
            } else {
                if ((copyHandPais.size() + player.getFirstLiu() * 4) >= 19) {// 起手牌19张或者20张
                    mtList.add(baoTing); // 14报听
                }
            }
            if (table.isDiaodiaoshou() == true && copyHandPais.size() <= 2) {
                mtList.add(diaoDiaoShou); // 吊吊手
            }
        } else {// 无息平 九对半
            YjGhzCardIndexArr arr = YjGhzTool.getGuihzCardIndexArr(copyHandPais);
            if (copyHandPais.size() >= 19) {// 起手牌19张或者20张
                if (isBegin == true && arr.getDuiziNum() >= 9) {// 九对半  并且对子数为9
                    mtList.add(jiuDuiBan);// 9九对半
                }
                if (table.isWuxiping() && hu.getHuxi() == 0 && hu.isHasWupingXi()) {// 1无息平
                    mtList.add(wuXiPing);
                }
            }
        }
        boolean haidi = (table.getLeftCards().size() == 0) ? true : false;
        if (haidi == true) {
            mtList.add(haiDi);// 12海底
        }
        if (isBegin == true) {
            mtList.add(tianHu);// 13天胡
            if (mtList.contains(jiuDuiBan)) {
                mtList.remove((Integer) tianHu);
            }
        }
        boolean jiuduibanHu = mtList.contains(jiuDuiBan);
        boolean wuxipingHu = mtList.contains(wuXiPing);
        Map<Integer, Integer> yuanMap = getYuanInfos(allCards, copyAllHuxiCards, copyHandPais, hu.getCheckCard(), wuxipingHu || jiuduibanHu);
        if (!mtList.isEmpty()) {
            StringBuilder sb = new StringBuilder("YjGhz");
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
    public static Map<Integer, Integer> getYuanInfos(List<YjGhzCard> allCards, List<YjGhzCardTypeHuxi> allHuxiCards, List<YjGhzCard> handCards, YjGhzCard huCard, boolean wuxipingOrJiuduiBanHu) {
        List<YjGhzCardTypeHuxi> copyAllHuxiCards = new ArrayList<>(allHuxiCards);
        Map<Integer, Integer> yuanMap = new HashMap<>();// 内圆1 外圆2 内圆外圆个数
        yuanMap.put(1, 0);
        yuanMap.put(2, 0);
        if (!wuxipingOrJiuduiBanHu) {// 无息平 不进行内圆 外圆计算
            Map<Integer, List<Integer>> card4Map = new HashMap<>();// 从胡牌牌组中查看四张的牌
            YjGhzCardIndexArr valArr = YjGhzTool.getMax(allCards);
            YjGhzIndex index3 = valArr.getPaohzCardIndex(3);// 四张的牌
            if (index3 != null) {
                for (int val : index3.getPaohzValMap().keySet()) {
                    List<YjGhzCard> cards = index3.getPaohzValMap().get(val);
                    List<Integer> ids = YjGhzTool.toGhzCardIds(cards);
                    card4Map.put(val, ids);
                }
            }
            if (!card4Map.isEmpty()) {// 有四张的牌
                List<Integer> filterCards = new ArrayList<>();
                filterCards.addAll(YjGhzTool.toGhzCardIds(handCards));// 手牌 加上 偎 溜 吃门子的牌
                for (YjGhzCardTypeHuxi huxiCard : copyAllHuxiCards) {
                    if (huxiCard.getAction() == YjGhzDisAction.action_chi || huxiCard.getAction() == YjGhzDisAction.action_shunChi) {
                        filterCards.addAll(huxiCard.getCardIds().subList(1, huxiCard.getCardIds().size()));
                    } else if (huxiCard.getAction() == YjGhzDisAction.action_wei || huxiCard.getAction() == YjGhzDisAction.action_liu || huxiCard.getAction() == YjGhzDisAction.action_weiHouLiu) {
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
    public static boolean hasWaiYuan(List<YjGhzCard> allCards, List<YjGhzCardTypeHuxi> allHuxiCards, List<YjGhzCard> handCards, YjGhzCard huCard) {
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
    public static boolean hasDahu(List<YjGhzCard> handCards, List<YjGhzCard> allCards, boolean isHaiDi, boolean canDiaoDiaoShou) {
        if (isHaiDi) { // 12海底
            return true;
        }
        if (canDiaoDiaoShou && handCards.size() <= 2) {// 17吊吊手
            return true;
        }
        List<YjGhzCard> redCardList = YjGhzTool.findRedGhzs(allCards);
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
        List<YjGhzCard> allSmallCards = YjGhzTool.findSmallGhzs(allCards);// 找出所有小字
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
        if (yuanMap.get(1) > 0) {
            sb.append("内圆 x" + yuanMap.get(1) + " ");
        }
        if (yuanMap.get(2) > 0) {
            sb.append("外圆 x" + yuanMap.get(2) + " ");
        }
        return sb.toString();
    }
}
