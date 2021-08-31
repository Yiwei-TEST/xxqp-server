package com.sy599.game.qipai.jhphz281.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import com.sy599.game.common.executor.TaskExecutor;
import com.sy599.game.qipai.jhphz281.bean.JhPhzBase;
import com.sy599.game.util.LogUtil;

public final class JhPhzHuPaiUtils {

    private static final ExecutorService EXECUTOR_SERVICE = TaskExecutor.EXECUTOR_SERVICE;//Executors.newCachedThreadPool();
    private static final Map<String, Integer> BEAT_SWITCH = new ConcurrentHashMap<>();

    public static JhPhzCardResult huPai(final JhPhzHandCards mHandCards, final int currentId, final JhPhzBase phzBase, boolean isSelf, Object user, int xingCard) {
        JhPhzCardResult cardResult = new JhPhzCardResult();
        cardResult.setCurId(currentId);
        cardResult.setPhzBase(phzBase);
        cardResult.setSelf(isSelf);

        final JhPhzHandCards handCards;
        if (mHandCards.WANGS.size() > 0) {
            if (phzBase.loadHuMode() >= 0) {
                if (!isSelf) {
                    return cardResult;
                }
            }
            handCards = mHandCards.copy();
            if (JhPhzCardUtils.wangCard(currentId)) {
                handCards.WANGS.add(currentId);
            }
        } else {
            if (JhPhzCardUtils.wangCard(currentId)) {
                if (phzBase.loadHuMode() >= 0) {
                    if (!isSelf) {
                        return cardResult;
                    }
                }
                handCards = mHandCards.copy();
                handCards.WANGS.add(currentId);
            } else {
                handCards = mHandCards.copy();
            }
        }

        List<Integer> list = new ArrayList<>(handCards.INS);

        if (JhPhzCardUtils.commonCard(currentId)) {
            list.add(currentId);
        }

        int ct = (list.size() + handCards.WANGS.size()) % 3;
        if (handCards.hasFourSameCard()) {
            if (ct == 2 || (list.size() == 3 && handCards.WANGS.size() == 3) || (list.size() == 2 && handCards.WANGS.size() == 4)) {

            } else {
                return cardResult;
            }
        } else {
            if (ct != 0) {
                return cardResult;
            }
        }

        cardResult.setXingCard(xingCard);
        cardResult.setRedCount(mHandCards.loadRedCardCount(true) + (JhPhzCardUtils.redCard(currentId) ? 1 : 0));
        JhPhzCardUtils.sort(list);

        cardResult.setHandCards(handCards);

        long time1 = System.currentTimeMillis();

        List<Integer> wangList = new ArrayList<>(handCards.WANGS);
//        Set<List<Integer>> rests = new HashSet<>();
        Map<Integer, Integer> map = JhPhzCardUtils.loadCardCount(list);
        Set<JhPhzCardMsg> lists = new HashSet<>();
        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
            List<List<Integer>> temps = JhPhzCardUtils.loadYijuhua(list, kv.getKey(), 0, true, false);
            for (List<Integer> t : temps) {
                lists.add(new JhPhzCardMsg(t));
            }
        }

        if (wangList.size() > 0 && list.size() > 1) {
            boolean bl1 = true;
            if (wangList.size() >= 3 && list.size() == 2) {
                bl1 = false;
                lists.clear();
            } else if (list.size() == 3 && map.size() == 2 && (wangList.size() == 3 && JhPhzCardUtils.loadIdByVal(list, JhPhzCardUtils.loadCardVal(currentId)) > 0 || wangList.size() == 2 && handCards.hasFourSameCard())) {
                bl1 = false;
                lists.clear();
            } else if (list.size() == 4 && map.size() == 2 && map.entrySet().iterator().next().getValue().intValue() == 2) {
                if (wangList.size() == 2) {
                    bl1 = false;
                    lists.clear();
                } else if (wangList.size() == 1) {
                    JhPhzCardUtils.sort(list);
                    if (Math.abs(JhPhzCardUtils.loadCardVal(list.get(0)) - JhPhzCardUtils.loadCardVal(list.get(3))) == 100) {
                        bl1 = false;
                        lists.clear();
                    }
                }
            } else if (wangList.size() == 3 && list.size() == 5 && handCards.hasFourSameCard() && JhPhzCardUtils.loadIdByVal(list, JhPhzCardUtils.loadCardVal(currentId)) > 0) {
                if (JhPhzCardUtils.hasSameValCard(list) && JhPhzCardUtils.hasSameValCardIgnoreCase(list)) {
                    boolean bl = true;
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        List<List<Integer>> temps = JhPhzCardUtils.loadYijuhua(list, kv.getKey(), 0, false, false);
                        if (temps.size() > 0) {
                            bl = false;
                            break;
                        }
                    }
                    if (bl) {
                        bl1 = false;
                        lists.clear();
                    }
                }
            } else if (wangList.size() == 4 && list.size() == 5 && JhPhzCardUtils.loadCardCount(list).size() <= 3) {
                bl1 = false;
                lists.clear();
            }

            if (wangList.size() == 4 && list.size() == 4 && handCards.hasFourSameCard() && JhPhzCardUtils.loadIdByVal(list, JhPhzCardUtils.loadCardVal(currentId)) > 0) {
                if (JhPhzCardUtils.hasSameValCard(list)) {
                    boolean bl = true;
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        List<List<Integer>> temps = JhPhzCardUtils.loadYijuhua(list, kv.getKey(), 0, false, false);
                        if (temps.size() > 0) {
                            bl = false;
                            break;
                        }
                    }
                    if (bl) {
                        bl1 = false;
                        lists.clear();
                    }
                }
            }

            if (bl1) {
                boolean bl = true;
                if (list.size() == 2 && JhPhzCardUtils.sameCard(list.get(0), list.get(1))) {
                    bl = false;
                    lists.clear();
                } else if (list.size() == 3) {
                    int idx = list.indexOf(Integer.valueOf(JhPhzCardUtils.loadIdByVal(list, JhPhzCardUtils.loadCardVal(currentId))));
                    if (idx >= 0) {
                        int idx1, idx2;
                        if (idx == 0) {
                            idx1 = 1;
                            idx2 = 2;
                        } else if (idx == 1) {
                            idx1 = 0;
                            idx2 = 2;
                        } else {
                            idx1 = 1;
                            idx2 = 0;
                        }
                        if (JhPhzCardUtils.sameCard(list.get(idx1), list.get(idx2))) {
                            bl = false;
                            lists.clear();
                        }
                    }
                }

                if (bl) {
//                    if(lists.size()==0){
                    Integer wangId = wangList.get(0);
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        List<List<Integer>> temps = JhPhzCardUtils.loadYijuhua(list, kv.getKey(), wangId, true, false);
                        for (List<Integer> t : temps) {
                            lists.add(new JhPhzCardMsg(t));
                        }
                    }
//                    }
                } else {
                    lists.clear();
                }
            }
        }

        final String uuid = UUID.randomUUID().toString();

        BEAT_SWITCH.put(uuid, 1);

        Map<JhPhzLinkCard, Integer> linkCardList = new ConcurrentHashMap<>();
        Map<JhPhzLinkCard, String> filterMap = new ConcurrentHashMap<>();

        int bestFan;
        try {
            if (lists.size() > 0) {
                CountDownLatch countDownLatch = new CountDownLatch(lists.size());
                for (JhPhzCardMsg tmp : lists) {
                    List<Integer> temp = new ArrayList<>(list);
                    temp.removeAll(tmp.getIds());
//                rests.add(tmp);
                    JhPhzLinkCard linkCard = new JhPhzLinkCard();
                    linkCard.setCurrent(tmp);
                    linkCard.setRest(temp);
                    List<Integer> tempWs;
                    if (wangList.size() > 0) {
                        tempWs = new ArrayList<>(wangList);
                        tempWs.removeAll(tmp.getIds());
                    } else {
                        tempWs = wangList;
                    }

                    remove(temp, linkCard, linkCardList, tempWs, currentId, phzBase, cardResult, countDownLatch, filterMap, uuid);
//                rests.addAll();
                }
                try {
                    countDownLatch.await();
                } catch (Exception e) {
                    LogUtil.errorLog.error("Exception:" + e.getMessage(), e);
                }

            } else {
                JhPhzLinkCard linkCard = new JhPhzLinkCard();
                linkCard.setRest(list);

                canHuLinkCard(linkCardList, linkCard, cardResult, phzBase, uuid);
            }
        } catch (Throwable th) {
            LogUtil.errorLog.error("Throwable:" + th.getMessage(), th);
        } finally {
            bestFan = BEAT_SWITCH.remove(uuid);
        }

        long time = System.currentTimeMillis() - time1;

        if (time >= 20L) {
            LogUtil.monitorLog.info("yzphz hupai calc times(ms)=" + time + ",user=" + user + ",currentId=" + currentId);
        }

        cardResult.setCanHu(linkCardList.size() > 0);

        if (cardResult.isCanHu()) {
            boolean isTianHu = phzBase.isTianHu();
            JhPhzLinkCard best = null;
            int currentFans, bestFans = 1;
            JhPhzCardResult bestResult = null;

            for (Map.Entry<JhPhzLinkCard, Integer> lf : linkCardList.entrySet()) {

                if (lf.getValue().intValue() < bestFan) {
                    continue;
                }

                JhPhzLinkCard linkCard = lf.getKey();
                if (linkCard.getHuxi() >= phzBase.loadQihuxi()) {
                    JhPhzCardResult tempCardResult = new JhPhzCardResult();
                    tempCardResult.setRedCount(cardResult.getRedCount());
                    tempCardResult.setPhzBase(phzBase);
                    tempCardResult.setHandCards(mHandCards.copy());
                    tempCardResult.setTotalHuxi(linkCard.getHuxi());
                    tempCardResult.addAll(linkCard.getCardMessageList());
                    tempCardResult.calc();
                    if (tempCardResult.getFans().size() > 0) {
                        linkCard.addFan(tempCardResult.getFans().iterator().next());
                    }

                    if (isTianHu) {
                        if (linkCard.getHuCard() <= 0) {
                            List<Integer> srcs = mHandCards.loadCards();
                            Map<Integer, Integer> map1 = JhPhzCardUtils.loadCardCount(srcs);
                            Map.Entry<Integer, Integer> maxEntry = null;
                            int max = 0;
                            for (Map.Entry<Integer, Integer> kv : map1.entrySet()) {
                                int tempMax = tempCardResult.loadValCount(kv.getKey());
                                if (maxEntry == null) {
                                    maxEntry = kv;
                                    max = tempMax;
                                } else if (tempMax > max) {
                                    maxEntry = kv;
                                    max = tempMax;
                                }
                            }
                            if (maxEntry != null) {
                                linkCard.setHuCard(JhPhzCardUtils.loadIdByVal(srcs, maxEntry.getKey()));
                            }
                        }

                        if (phzBase.loadXingMode() == 0) {
                            xingCard = linkCard.getHuCard();
                        }
                    }

                    int tun1 = phzBase.loadBaseTun();
                    int tun2 = phzBase.loadXingTun(tempCardResult.loadValCount(JhPhzCardUtils.loadCardVal(xingCard)));
                    int tun3 = phzBase.loadCommonTun(tempCardResult.getTotalHuxi(false));
                    tempCardResult.setTotalTun(tun1 + tun2 + tun3);
                    tempCardResult.setHuxiTun(tun1 + tun3);
                    tempCardResult.setXingTun(tun2);
                    linkCard.setTotalTun(tempCardResult.getTotalTun());

                    tempCardResult.addFans(linkCard.getFans());
                    currentFans = loadMaxFanCount(linkCard.getFans());
                    if (best == null) {
                        best = linkCard;
                        bestFans = loadMaxFanCount(best.getFans());
                        bestResult = tempCardResult;
                    } else if (currentFans > bestFans) {
                        best = linkCard;
                        bestFans = currentFans;
                        bestResult = tempCardResult;
                    } else if (currentFans == bestFans) {
                        boolean bl = false;
                        if (best.getFans().contains(JhPhzFanEnums.WANG_ZHA_WANG)) {
                            if (!linkCard.getFans().contains(JhPhzFanEnums.WANG_ZHA_WANG)) {
                                continue;
                            }
                            bl = true;
                        } else if (linkCard.getFans().contains(JhPhzFanEnums.WANG_ZHA_WANG)) {
                            bl = true;
                        } else if (best.getFans().contains(JhPhzFanEnums.WANG_ZHA) || best.getFans().contains(JhPhzFanEnums.WANG_CHUANG_WANG)) {
                            if (!(linkCard.getFans().contains(JhPhzFanEnums.WANG_ZHA) || linkCard.getFans().contains(JhPhzFanEnums.WANG_CHUANG_WANG))) {
                                continue;
                            }
                            bl = true;
                        } else if (linkCard.getFans().contains(JhPhzFanEnums.WANG_ZHA) || linkCard.getFans().contains(JhPhzFanEnums.WANG_CHUANG_WANG)) {
                            bl = true;
                        } else if (best.getFans().contains(JhPhzFanEnums.WANG_CHUANG) || best.getFans().contains(JhPhzFanEnums.WANG_DIAO_WANG)) {
                            if (!(linkCard.getFans().contains(JhPhzFanEnums.WANG_CHUANG) || linkCard.getFans().contains(JhPhzFanEnums.WANG_DIAO_WANG))) {
                                continue;
                            }
                            bl = true;
                        } else if (linkCard.getFans().contains(JhPhzFanEnums.WANG_CHUANG) || linkCard.getFans().contains(JhPhzFanEnums.WANG_DIAO_WANG)) {
                            bl = true;
                        } else if (best.getFans().contains(JhPhzFanEnums.WANG_DIAO)) {
                            if (!linkCard.getFans().contains(JhPhzFanEnums.WANG_DIAO)) {
                                continue;
                            }
                            bl = true;
                        } else if (linkCard.getFans().contains(JhPhzFanEnums.WANG_DIAO)) {
                            bl = true;
                        }

                        if (linkCard.getTotalTun() > best.getTotalTun()) {
                            best = linkCard;
                            bestFans = currentFans;
                            bestResult = tempCardResult;
                        } else if (linkCard.getTotalTun() == best.getTotalTun() && (bl || bestResult.getXingTun() > tempCardResult.getXingTun())) {
                            best = linkCard;
                            bestFans = currentFans;
                            bestResult = tempCardResult;
                        }
                    }
                }
            }

            if (best == null) {
                cardResult.setCanHu(false);
                return cardResult;
            }

            cardResult.getCardMessageList().clear();
            cardResult.addAll(best.getCardMessageList());
            cardResult.setTotalHuxi(bestResult.getTotalHuxi(false));
            cardResult.setCanHu(true);
            cardResult.setTotalTun(best.getTotalTun());
            cardResult.setHuxiTun(bestResult.getHuxiTun());
            cardResult.setXingTun(bestResult.getXingTun());
            cardResult.getFans().clear();
            cardResult.addFans(best.getFans());
            cardResult.getReplaceCards().clear();
            cardResult.getReplaceCards().addAll(bestResult.getReplaceCards());

            if (isTianHu) {
//                if (phzBase.loadXingMode() == 0) {
//                    xingCard = best.getHuCard();
//                }
                cardResult.setHuCard(xingCard);
            }

            cardResult.setXingCard(xingCard);
            if (isSelf) {
                boolean bl = true;
                for (JhPhzFanEnums fan : cardResult.getFans()) {
                    if (fan.name().startsWith("WANG")) {
                        bl = false;
                        cardResult.setSelf(true);
                        break;
                    }
                }
                if (bl) {
                    cardResult.addFan(JhPhzFanEnums.SELF);
                    cardResult.setSelf(true);
                }
            }

            int totalFan = 1;
            for (JhPhzFanEnums fan : cardResult.getFans()) {
                totalFan *= fan.getFan();
                if (fan.name().equals("WANG_DIAO") || fan.name().equals("WANG_DIAO_WANG")) {
                    cardResult.setWangDiao(true);
                } else if (fan.name().equals("WANG_CHUANG_WANG") || fan.name().equals("WANG_CHUANG")) {
                    cardResult.setWangChuang(true);
                } else if (fan.name().equals("WANG_ZHA") || fan.name().equals("WANG_ZHA_WANG")) {
                    cardResult.setWangZha(true);
                }
            }
            cardResult.setTotalFan(totalFan);
            if (phzBase.loadHuMode() == 0) {
                if (handCards.WANGS.size() > 0) {
                    if (!isSelf) {
                        cardResult.setCanHu(false);
                        return cardResult;
                    }

//                    if(cardResult.isWangDiao()||cardResult.isWangChuang()||cardResult.isWangZha()){
//
//                    }else{
//                        cardResult.setCanHu(false);
//                        return cardResult;
//                    }
                }
            } else if (phzBase.loadHuMode() == 1) {
                if (handCards.WANGS.size() > 0) {
                    if (!isSelf) {
                        cardResult.setCanHu(false);
                        return cardResult;
                    }
                }
                if (handCards.WANGS.size() >= 3) {
//                    if (cardResult.isWangDiao() || cardResult.isWangChuang() || cardResult.isWangZha()) {
//
//                    } else {
//                        cardResult.setCanHu(false);
//                        return cardResult;
//                    }
                }
            } else if (phzBase.loadHuMode() == 2) {
                if (handCards.WANGS.size() > 0) {
                    if (!isSelf) {
                        cardResult.setCanHu(false);
                        return cardResult;
                    }
                }
//                switch (handCards.WANGS.size()) {
//                    case 1:
//                        if (totalFan < 2) {
//                            cardResult.setCanHu(false);
//                            return cardResult;
//                        }
//                        break;
//                    case 2:
//                        if (totalFan < 4) {
//                            cardResult.setCanHu(false);
//                            return cardResult;
//                        }
//                        break;
//                    case 3:
//                        if (totalFan < 8) {
//                            cardResult.setCanHu(false);
//                            return cardResult;
//                        }
//                        break;
//                    case 4:
//                        if (totalFan < 16) {
//                            cardResult.setCanHu(false);
//                            return cardResult;
//                        }
//                        break;
//                }
            }
        }

        if (cardResult.isWangZha()) {
            JhPhzCardResult tempCardResult = new JhPhzCardResult();
            tempCardResult.setCurId(currentId);
            tempCardResult.setPhzBase(phzBase);
            tempCardResult.setRedCount(cardResult.getRedCount());
            tempCardResult.setSelf(isSelf);
            tempCardResult.setHandCards(mHandCards.copy());
            tempCardResult.addAll(cardResult.copyCardMessageList());
            JhPhzCardMessage cm1 = null, cm2 = null;
            for (JhPhzCardMessage cm : tempCardResult.getCardMessageList()) {
                if (cm.getHuXiEnum() == JhPhzHuXiEnums.TI && JhPhzCardUtils.countCard(cm.getCards(), 201) >= 3 && JhPhzCardUtils.hasCard(cm.getCards(), JhPhzCardUtils.loadCardVal(currentId))) {
                    cm1 = cm;
                } else if (cm.getHuXiEnum() == JhPhzHuXiEnums.DUI) {
                    cm2 = cm;
                }
            }

            if (cm1 != null && cm2 != null) {
                Integer card = JhPhzCardUtils.loadIdByVal(cm1.getCards(), 201);
                cm1.getCards().remove(card);
                cm1.init(JhPhzHuXiEnums.WEI, new ArrayList<>(cm1.getCards()));
                cm2.getCards().add(card);
                cm2.init(JhPhzHuXiEnums.KAN, new ArrayList<>(cm2.getCards()));

                if (tempCardResult.getTotalHuxi() >= phzBase.loadQihuxi()) {
                    int totalFan = 1;
                    tempCardResult.getFans().add(JhPhzCardUtils.wangCard(currentId) ? JhPhzFanEnums.WANG_CHUANG_WANG : JhPhzFanEnums.WANG_CHUANG);
                    for (JhPhzFanEnums fan : tempCardResult.getFans()) {
                        totalFan *= fan.getFan();
                    }

                    int tun1 = phzBase.loadBaseTun();
                    int tun2 = phzBase.loadXingTun(tempCardResult.loadValCount(JhPhzCardUtils.loadCardVal(xingCard)));
                    int tun3 = phzBase.loadCommonTun(tempCardResult.getTotalHuxi(false));
                    tempCardResult.setTotalTun(tun1 + tun2 + tun3);
                    tempCardResult.setHuxiTun(tun1 + tun3);
                    tempCardResult.setXingTun(tun2);

                    tempCardResult.setTotalFan(totalFan);
                    if (phzBase.loadHuMode() == 0) {
                        tempCardResult.setWangChuang(true);
                        tempCardResult.setCanHu(true);
                        cardResult.setWangChuang(true);
                        cardResult.setChuangCardResult(tempCardResult);
                    } else if (phzBase.loadHuMode() == 1) {
                        tempCardResult.setWangChuang(true);
                        tempCardResult.setCanHu(true);
                        cardResult.setWangChuang(true);
                        cardResult.setChuangCardResult(tempCardResult);
                    } else if (phzBase.loadHuMode() == 2) {
                        switch (handCards.WANGS.size()) {
                            case 1:
                                if (totalFan >= 2) {
                                    tempCardResult.setWangChuang(true);
                                    tempCardResult.setCanHu(true);
                                    cardResult.setWangChuang(true);
                                    cardResult.setChuangCardResult(tempCardResult);
                                }
                                break;
                            case 2:
                                if (totalFan >= 4) {
                                    tempCardResult.setWangChuang(true);
                                    tempCardResult.setCanHu(true);
                                    cardResult.setWangChuang(true);
                                    cardResult.setChuangCardResult(tempCardResult);
                                }
                                break;
                            case 3:
                                if (totalFan >= 8) {
                                    tempCardResult.setWangChuang(true);
                                    tempCardResult.setCanHu(true);
                                    cardResult.setWangChuang(true);
                                    cardResult.setChuangCardResult(tempCardResult);
                                }
                                break;
                            case 4:
                                if (totalFan >= 16) {
                                    tempCardResult.setWangChuang(true);
                                    tempCardResult.setCanHu(true);
                                    cardResult.setWangChuang(true);
                                    cardResult.setChuangCardResult(tempCardResult);
                                }
                                break;
                        }
                    }
                }

                if (cardResult.isWangChuang()) {
                    JhPhzCardResult tempCardResult1 = new JhPhzCardResult();
                    tempCardResult1.setCurId(currentId);
                    tempCardResult1.setPhzBase(phzBase);
                    tempCardResult1.setRedCount(cardResult.getRedCount());
                    tempCardResult1.setSelf(isSelf);
                    tempCardResult1.setHandCards(mHandCards.copy());
                    tempCardResult1.addAll(cardResult.getChuangCardResult().copyCardMessageList());
                    cm1 = null;
                    cm2 = null;
                    for (JhPhzCardMessage cm : tempCardResult1.getCardMessageList()) {
                        if (cm.getHuXiEnum() == JhPhzHuXiEnums.WEI && JhPhzCardUtils.countCard(cm.getCards(), 201) >= 2 && JhPhzCardUtils.hasCard(cm.getCards(), JhPhzCardUtils.loadCardVal(currentId))) {
                            cm1 = cm;
                        } else if (cm.getHuXiEnum() == JhPhzHuXiEnums.DUI) {
                            cm2 = cm;
                        } else {
                            if (cm2 == null || cm2.getHuXiEnum() != JhPhzHuXiEnums.DUI) {
                                if (cm.getHuXiEnum() == JhPhzHuXiEnums.WEI || cm.getHuXiEnum() == JhPhzHuXiEnums.KAN) {
                                    if (cm2 == null) {
                                        cm2 = cm;
                                    } else {
                                        int val = JhPhzCardUtils.loadCardVal(cm2.getCards().get(0));
                                        if (val > 100) {
                                            if (JhPhzCardUtils.redCardByVal(val)) {
                                            } else {
                                                cm2 = cm;
                                            }
                                        } else {
                                            if (JhPhzCardUtils.redCardByVal(val)) {
                                            } else {
                                                cm2 = cm;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (cm1 != null && cm2 != null) {
                        card = JhPhzCardUtils.loadIdByVal(cm1.getCards(), 201);
                        cm1.getCards().remove(card);
                        cm1.init(JhPhzHuXiEnums.DUI, new ArrayList<>(cm1.getCards()));
                        cm2.getCards().add(card);
                        cm2.init(cm2.getHuXiEnum() == JhPhzHuXiEnums.DUI ? JhPhzHuXiEnums.KAN : JhPhzHuXiEnums.TI, new ArrayList<>(cm2.getCards()));

                        if (tempCardResult1.getTotalHuxi() >= phzBase.loadQihuxi()) {
                            int totalFan = 1;
                            tempCardResult1.getFans().add(JhPhzCardUtils.wangCard(currentId) ? JhPhzFanEnums.WANG_DIAO_WANG : JhPhzFanEnums.WANG_DIAO);
                            for (JhPhzFanEnums fan : tempCardResult1.getFans()) {
                                totalFan *= fan.getFan();
                            }

                            int tun1 = phzBase.loadBaseTun();
                            int tun2 = phzBase.loadXingTun(tempCardResult1.loadValCount(JhPhzCardUtils.loadCardVal(xingCard)));
                            int tun3 = phzBase.loadCommonTun(tempCardResult1.getTotalHuxi(false));
                            tempCardResult1.setTotalTun(tun1 + tun2 + tun3);
                            tempCardResult1.setHuxiTun(tun1 + tun3);
                            tempCardResult1.setXingTun(tun2);

                            tempCardResult1.setTotalFan(totalFan);
                            if (phzBase.loadHuMode() == 0) {
                                tempCardResult1.setWangDiao(true);
                                tempCardResult1.setCanHu(true);
                                cardResult.setWangDiao(true);
                                cardResult.setDiaoCardResult(tempCardResult1);
                            } else if (phzBase.loadHuMode() == 1) {
                                tempCardResult1.setWangDiao(true);
                                tempCardResult1.setCanHu(true);
                                cardResult.setWangDiao(true);
                                cardResult.setDiaoCardResult(tempCardResult1);
                            } else if (phzBase.loadHuMode() == 2) {
                                switch (handCards.WANGS.size()) {
                                    case 1:
                                        if (totalFan >= 2) {
                                            tempCardResult1.setWangDiao(true);
                                            tempCardResult1.setCanHu(true);
                                            cardResult.setWangDiao(true);
                                            cardResult.setDiaoCardResult(tempCardResult1);
                                        }
                                        break;
                                    case 2:
                                        if (totalFan >= 4) {
                                            tempCardResult1.setWangDiao(true);
                                            tempCardResult1.setCanHu(true);
                                            cardResult.setWangDiao(true);
                                            cardResult.setDiaoCardResult(tempCardResult1);
                                        }
                                        break;
                                    case 3:
                                        if (totalFan >= 8) {
                                            tempCardResult1.setWangDiao(true);
                                            tempCardResult1.setCanHu(true);
                                            cardResult.setWangDiao(true);
                                            cardResult.setDiaoCardResult(tempCardResult1);
                                        }
                                        break;
                                    case 4:
                                        if (totalFan >= 16) {
                                            tempCardResult1.setWangDiao(true);
                                            tempCardResult1.setCanHu(true);
                                            cardResult.setWangDiao(true);
                                            cardResult.setDiaoCardResult(tempCardResult1);
                                        }
                                        break;
                                }
                            }
                        }
                    }
                } else {
                    card = JhPhzCardUtils.loadIdByVal(cm1.getCards(), 201);
                    cm1.getCards().remove(card);
                    cm1.init(JhPhzHuXiEnums.DUI, new ArrayList<>(cm1.getCards()));
                    cm2.getCards().add(card);
                    cm2.init(JhPhzHuXiEnums.TI, new ArrayList<>(cm2.getCards()));

                    if (tempCardResult.getTotalHuxi() >= phzBase.loadQihuxi()) {
                        int totalFan = 1;
                        tempCardResult.getFans().add(JhPhzCardUtils.wangCard(currentId) ? JhPhzFanEnums.WANG_DIAO_WANG : JhPhzFanEnums.WANG_DIAO);
                        for (JhPhzFanEnums fan : tempCardResult.getFans()) {
                            totalFan *= fan.getFan();
                        }

                        int tun1 = phzBase.loadBaseTun();
                        int tun2 = phzBase.loadXingTun(tempCardResult.loadValCount(JhPhzCardUtils.loadCardVal(xingCard)));
                        int tun3 = phzBase.loadCommonTun(tempCardResult.getTotalHuxi(false));
                        tempCardResult.setTotalTun(tun1 + tun2 + tun3);
                        tempCardResult.setHuxiTun(tun1 + tun3);
                        tempCardResult.setXingTun(tun2);

                        tempCardResult.setTotalFan(totalFan);
                        if (phzBase.loadHuMode() == 0) {
                            tempCardResult.setWangDiao(true);
                            tempCardResult.setCanHu(true);
                            cardResult.setWangDiao(true);
                            cardResult.setDiaoCardResult(tempCardResult);
                        } else if (phzBase.loadHuMode() == 1) {
                            tempCardResult.setWangDiao(true);
                            tempCardResult.setCanHu(true);
                            cardResult.setWangDiao(true);
                            cardResult.setDiaoCardResult(tempCardResult);
                        } else if (phzBase.loadHuMode() == 2) {
                            switch (handCards.WANGS.size()) {
                                case 1:
                                    if (totalFan >= 2) {
                                        tempCardResult.setWangDiao(true);
                                        tempCardResult.setCanHu(true);
                                        cardResult.setWangDiao(true);
                                        cardResult.setDiaoCardResult(tempCardResult);
                                    }
                                    break;
                                case 2:
                                    if (totalFan >= 4) {
                                        tempCardResult.setWangDiao(true);
                                        tempCardResult.setCanHu(true);
                                        cardResult.setWangDiao(true);
                                        cardResult.setDiaoCardResult(tempCardResult);
                                    }
                                    break;
                                case 3:
                                    if (totalFan >= 8) {
                                        tempCardResult.setWangDiao(true);
                                        tempCardResult.setCanHu(true);
                                        cardResult.setWangDiao(true);
                                        cardResult.setDiaoCardResult(tempCardResult);
                                    }
                                    break;
                                case 4:
                                    if (totalFan >= 16) {
                                        tempCardResult.setWangDiao(true);
                                        tempCardResult.setCanHu(true);
                                        cardResult.setWangDiao(true);
                                        cardResult.setDiaoCardResult(tempCardResult);
                                    }
                                    break;
                            }
                        }
                    }
                }
            }

        } else if (cardResult.isWangChuang()) {
            JhPhzCardResult tempCardResult = new JhPhzCardResult();
            tempCardResult.setCurId(currentId);
            tempCardResult.setPhzBase(phzBase);
            tempCardResult.setRedCount(cardResult.getRedCount());
            tempCardResult.setSelf(isSelf);
            tempCardResult.setHandCards(mHandCards.copy());
            tempCardResult.addAll(cardResult.copyCardMessageList());
            JhPhzCardMessage cm1 = null, cm2 = null;
            for (JhPhzCardMessage cm : tempCardResult.getCardMessageList()) {
                if (cm.getHuXiEnum() == JhPhzHuXiEnums.WEI && JhPhzCardUtils.countCard(cm.getCards(), 201) >= 2 && JhPhzCardUtils.hasCard(cm.getCards(), JhPhzCardUtils.loadCardVal(currentId))) {
                    cm1 = cm;
                } else if (cm.getHuXiEnum() == JhPhzHuXiEnums.DUI) {
                    cm2 = cm;
                } else {
                    if (cm2 == null || cm2.getHuXiEnum() != JhPhzHuXiEnums.DUI) {
                    	  if (cm.getHuXiEnum() == JhPhzHuXiEnums.WEI || cm.getHuXiEnum() == JhPhzHuXiEnums.KAN) {
                              if (cm2 == null) {
                                  cm2 = cm;
                              } else {
                                  int val = JhPhzCardUtils.loadCardVal(cm.getCards().get(0));
                                  
                                  if(phzBase.loadXingMode()==1) {
                                  	int xingCardVal = JhPhzCardUtils.loadCardVal(xingCard);
                                  	if(xingCardVal == val ) {
                                  		 cm2 = cm;
                                  	}
                                  }else {
                                  	if (JhPhzCardUtils.redCardByVal(val)) {
                                      } else {
                                          cm2 = cm;
                                      }
                                  }
                                 
                              }
                          }
                    }
                }
            }

            if (cm1 != null && cm2 != null) {
                Integer card = JhPhzCardUtils.loadIdByVal(cm1.getCards(), 201);
                cm1.getCards().remove(card);
                cm1.init(JhPhzHuXiEnums.DUI, new ArrayList<>(cm1.getCards()));
                cm2.getCards().add(card);
                cm2.init(cm2.getHuXiEnum() == JhPhzHuXiEnums.DUI ? JhPhzHuXiEnums.KAN : JhPhzHuXiEnums.TI, new ArrayList<>(cm2.getCards()));

                if (tempCardResult.getTotalHuxi() >= phzBase.loadQihuxi()) {
                    int totalFan = 1;
                    tempCardResult.getFans().add(JhPhzCardUtils.wangCard(currentId) ? JhPhzFanEnums.WANG_DIAO_WANG : JhPhzFanEnums.WANG_DIAO);
                    for (JhPhzFanEnums fan : tempCardResult.getFans()) {
                        totalFan *= fan.getFan();
                    }

                    int tun1 = phzBase.loadBaseTun();
                    int tun2 = phzBase.loadXingTun(tempCardResult.loadValCount(JhPhzCardUtils.loadCardVal(xingCard)));
                    int tun3 = phzBase.loadCommonTun(tempCardResult.getTotalHuxi(false));
                    tempCardResult.setTotalTun(tun1 + tun2 + tun3);
                    tempCardResult.setHuxiTun(tun1 + tun3);
                    tempCardResult.setXingTun(tun2);

                    tempCardResult.setTotalFan(totalFan);
                    if (phzBase.loadHuMode() == 0) {
                        tempCardResult.setWangDiao(true);
                        tempCardResult.setCanHu(true);
                        cardResult.setWangDiao(true);
                        cardResult.setDiaoCardResult(tempCardResult);
                    } else if (phzBase.loadHuMode() == 1) {
                        tempCardResult.setWangDiao(true);
                        tempCardResult.setCanHu(true);
                        cardResult.setWangDiao(true);
                        cardResult.setDiaoCardResult(tempCardResult);
                    } else if (phzBase.loadHuMode() == 2) {
                        switch (handCards.WANGS.size()) {
                            case 1:
                                if (totalFan >= 2) {
                                    tempCardResult.setWangDiao(true);
                                    tempCardResult.setCanHu(true);
                                    cardResult.setWangDiao(true);
                                    cardResult.setDiaoCardResult(tempCardResult);
                                }
                                break;
                            case 2:
                                if (totalFan >= 4) {
                                    tempCardResult.setWangDiao(true);
                                    tempCardResult.setCanHu(true);
                                    cardResult.setWangDiao(true);
                                    cardResult.setDiaoCardResult(tempCardResult);
                                }
                                break;
                            case 3:
                                if (totalFan >= 8) {
                                    tempCardResult.setWangDiao(true);
                                    tempCardResult.setCanHu(true);
                                    cardResult.setWangDiao(true);
                                    cardResult.setDiaoCardResult(tempCardResult);
                                }
                                break;
                            case 4:
                                if (totalFan >= 16) {
                                    tempCardResult.setWangDiao(true);
                                    tempCardResult.setCanHu(true);
                                    cardResult.setWangDiao(true);
                                    cardResult.setDiaoCardResult(tempCardResult);
                                }
                                break;
                        }
                    }
                }
            }
        }

        JhPhzCardResult chuang = cardResult.getChuangCardResult();
        if (chuang != null && chuang != cardResult) {
            chuang.setHuCard(cardResult.getHuCard());
            chuang.setXingCard(cardResult.getXingCard());
        }

        JhPhzCardResult diao = cardResult.getDiaoCardResult();
        if (diao != null && diao != cardResult) {
            diao.setHuCard(cardResult.getHuCard());
            diao.setXingCard(cardResult.getXingCard());
        }

        return cardResult;
    }

    private static void remove(final List<Integer> list, final JhPhzLinkCard linkCard, final Map<JhPhzLinkCard, Integer> linkCardList, final List<Integer> wangs, final int currentId, final JhPhzBase phzBase, final JhPhzCardResult cardResult, final CountDownLatch countDownLatch, final Map<JhPhzLinkCard, String> filterMap, final String uuid) {
        EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    remove(list, linkCard, linkCardList, wangs, currentId, phzBase, cardResult, filterMap, uuid);
                } catch (Throwable t) {
                    LogUtil.errorLog.error("hupai exception:" + t.getMessage(), t);
                } finally {
                    countDownLatch.countDown();
                }
            }
        });
    }

    private static void remove(List<Integer> list, JhPhzLinkCard linkCard, Map<JhPhzLinkCard, Integer> linkCardList, List<Integer> wangs, int currentId, JhPhzBase phzBase, JhPhzCardResult cardResult, Map<JhPhzLinkCard, String> filterMap, final String uuid) {
        Map<Integer, Integer> map = JhPhzCardUtils.loadCardCount(list);
        Set<JhPhzCardMsg> lists = new HashSet<>();
        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
            List<List<Integer>> temps = JhPhzCardUtils.loadYijuhua(list, kv.getKey(), 0, true, false);
            for (List<Integer> t : temps) {
                lists.add(new JhPhzCardMsg(t));
            }
        }
        linkCard.setRest(list);
        boolean hasFourSameCard = cardResult.getHandCards().hasFourSameCard();

        if (wangs.size() > 0 && list.size() > 1) {
            boolean bl1 = true;
            if (wangs.size() >= 3 && list.size() == 2) {
                bl1 = false;
                lists.clear();
            } else if (list.size() == 3 && map.size() == 2 && (wangs.size() == 3 && JhPhzCardUtils.loadIdByVal(list, JhPhzCardUtils.loadCardVal(currentId)) > 0 || wangs.size() == 2 && hasFourSameCard)) {
                bl1 = false;
                lists.clear();
            } else if (list.size() == 4 && map.size() == 2 && map.entrySet().iterator().next().getValue().intValue() == 2) {
                if (wangs.size() == 2) {
                    bl1 = false;
                    lists.clear();
                } else if (wangs.size() == 1) {
                    JhPhzCardUtils.sort(list);
                    if (Math.abs(JhPhzCardUtils.loadCardVal(list.get(0)) - JhPhzCardUtils.loadCardVal(list.get(3))) == 100) {
                        bl1 = false;
                        lists.clear();
                    }
                }
            } else if (wangs.size() == 3 && hasFourSameCard && list.size() == 5 && JhPhzCardUtils.loadIdByVal(list, JhPhzCardUtils.loadCardVal(currentId)) > 0) {
                if (JhPhzCardUtils.hasSameValCard(list) && JhPhzCardUtils.hasSameValCardIgnoreCase(list)) {
                    boolean bl = true;
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        List<List<Integer>> temps = JhPhzCardUtils.loadYijuhua(list, kv.getKey(), 0, false, false);
                        if (temps.size() > 0) {
                            bl = false;
                            break;
                        }
                    }
                    if (bl) {
                        bl1 = false;
                        lists.clear();
                    }
                }
            } else if (wangs.size() == 4 && list.size() == 5 && JhPhzCardUtils.loadCardCount(list).size() <= 3) {
                bl1 = false;
                lists.clear();
            }

            if (hasFourSameCard && wangs.size() == 4 && list.size() == 4 && JhPhzCardUtils.loadIdByVal(list, JhPhzCardUtils.loadCardVal(currentId)) > 0) {
                if (JhPhzCardUtils.hasSameValCard(list)) {
                    boolean bl = true;
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        List<List<Integer>> temps = JhPhzCardUtils.loadYijuhua(list, kv.getKey(), 0, false, false);
                        if (temps.size() > 0) {
                            bl = false;
                            break;
                        }
                    }
                    if (bl) {
                        bl1 = false;
                        lists.clear();
                    }
                }
            }

            if (bl1) {
                boolean bl = true;
                if (list.size() == 2 && JhPhzCardUtils.sameCard(list.get(0), list.get(1))) {
                    bl = false;
                } else if (list.size() == 3) {
                    int idx = list.indexOf(Integer.valueOf(JhPhzCardUtils.loadIdByVal(list, JhPhzCardUtils.loadCardVal(currentId))));
                    if (idx >= 0) {
                        int idx1, idx2;
                        if (idx == 0) {
                            idx1 = 1;
                            idx2 = 2;
                        } else if (idx == 1) {
                            idx1 = 0;
                            idx2 = 2;
                        } else {
                            idx1 = 1;
                            idx2 = 0;
                        }
                        if (JhPhzCardUtils.sameCard(list.get(idx1), list.get(idx2))) {
                            bl = false;
                        }
                    }
                }

                if (bl) {
                    if (wangs.size() > 0) {
                        int wangId = wangs.get(0);
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            List<List<Integer>> temps = JhPhzCardUtils.loadYijuhua(list, kv.getKey(), wangId, true, false);
                            for (List<Integer> t : temps) {
                                lists.add(new JhPhzCardMsg(t));
                            }
                        }
                    }
                } else {
                    lists.clear();
                }
            }
        }

        if (lists.size() == 0) {
            canHuLinkCard(linkCardList, linkCard, cardResult, phzBase, uuid);
        } else {
            for (JhPhzCardMsg tmp : lists) {
                List<Integer> temp = new ArrayList<>(list);
                temp.removeAll(tmp.getIds());
                JhPhzLinkCard linkCard1 = new JhPhzLinkCard();
                linkCard1.setCurrent(tmp);
                linkCard1.setRest(temp);
                linkCard1.setPre(linkCard);

                if (filterMap.put(linkCard1, "1") == null) {
                    List<Integer> tempWs;

                    if (wangs.size() > 0) {
                        tempWs = new ArrayList<>(wangs);
                        tempWs.removeAll(tmp.getIds());
                    } else {
                        tempWs = wangs;
                    }

                    remove(temp, linkCard1, linkCardList, tempWs, currentId, phzBase, cardResult, filterMap, uuid);
                }
            }
        }
    }

    private static void canHuLinkCard(Map<JhPhzLinkCard, Integer> linkCardList, JhPhzLinkCard linkCard0, JhPhzCardResult cardResult, final JhPhzBase phzBase, final String uuid) {
        List<Integer> list = linkCard0.getRest();

        if (list != null && list.size() >= 6) {
            return;
        }

        List<List<Integer>> yjhLists = linkCard0.loadList(false);
        JhPhzHandCards handCards = cardResult.getHandCards().copy();

        List<Integer> wangList = new ArrayList<>(handCards.WANGS);
        if (wangList.size() > 0) {
            for (List<Integer> temp : yjhLists) {
                wangList.removeAll(temp);
            }
        }

        boolean isTianHu = phzBase.isTianHu();
        linkCard0.setHuCard(cardResult.getCurId());

        int val = JhPhzCardUtils.loadCardVal(cardResult.getCurId());
        List<JhPhzCardMessage> cardMessageList;

        JhPhzLinkCard linkCard1 = new JhPhzLinkCard();
        if (list == null || list.size() == 0) {
            checkListHu(linkCardList, cardResult, phzBase, uuid, yjhLists, handCards, wangList, isTianHu, val,
					linkCard1);
        } else {
            switch (list.size()) {
                case 1:
                    switch (wangList.size()) {
                        case 0:
//                        System.out.println("error");
                            break;
                        case 1:
                            if (JhPhzCardUtils.wangCard(cardResult.getCurId())) {
                                cardMessageList = new ArrayList<>(8);
                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, yjhLists, cardMessageList);
                                cardMessageList.add(new JhPhzCardMessage(JhPhzHuXiEnums.DUI, JhPhzCardUtils.asList(list.get(0), wangList.get(0))));
                                linkCard1.setYjhList(yjhLists);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                            } else {
                                if (JhPhzCardUtils.sameCard(list.get(0), cardResult.getCurId()) || isTianHu) {

                                    int wangHu = 1;

                                    List<Integer> list1 = null;
                                    for (List<Integer> temp : yjhLists) {
                                        if (temp.size() == 3) {
                                            if (JhPhzCardUtils.hasCard(temp, 201) && (JhPhzCardUtils.wangCard(temp.get(1)) || JhPhzCardUtils.sameCard(temp.get(0), temp.get(1)))) {
                                                list1 = temp;
                                                wangHu = 2;
                                                break;
                                            }
                                        }
                                    }
                                    if (wangHu == 2) {
                                        linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                        Integer card = JhPhzCardUtils.loadIdByVal(list1, 201);
                                        list1.remove(card);
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(0));
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, yjhLists, cardMessageList);
                                        cardMessageList.add(new JhPhzCardMessage(JhPhzHuXiEnums.WEI, JhPhzCardUtils.asList(list.get(0), card, wangList.get(0))));
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, list1));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), card));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);

                                            if (isTianHu) {
                                                linkCard1.setHuCard(list.get(0));
                                            }

                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0)));
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                    } else {
                                        linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, yjhLists, cardMessageList);
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(0));
                                        }
                                        cardMessageList.add(new JhPhzCardMessage(JhPhzHuXiEnums.DUI, JhPhzCardUtils.asList(list.get(0), wangList.get(0))));
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                } else {
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, yjhLists, cardMessageList);
                                    cardMessageList.add(new JhPhzCardMessage(JhPhzHuXiEnums.DUI, JhPhzCardUtils.asList(list.get(0), wangList.get(0))));
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    int xingVal = JhPhzCardUtils.loadXingVal(JhPhzCardUtils.loadCardVal(cardResult.getXingCard()), phzBase.loadXingMode());
                                    if (JhPhzCardUtils.loadCardVal(list.get(0)) != xingVal && JhPhzCardUtils.commonCardByVal(xingVal)) {
                                        List<Integer> tempList = new ArrayList<>(2);
                                        tempList.add(list.get(0));
                                        int idx = -1;
                                        Integer tempCard = -1;
                                        List<List<Integer>> yjhs = null;
                                        for (int i = 0, len = yjhLists.size(); i < len; i++) {
                                            List<Integer> tmpList = yjhLists.get(i);
                                            if (tmpList.size() == 3) {
                                                int v1 = JhPhzCardUtils.loadCardVal(tmpList.get(0));
                                                int v2 = JhPhzCardUtils.loadCardVal(tmpList.get(1));
                                                int v3 = JhPhzCardUtils.loadCardVal(tmpList.get(2));
                                                if (v1 < JhPhzCardUtils.WANGPAI_VAL && v2 < 200 && v3 < 200) {
                                                    if (v1 == v2) {
                                                        if (v1 != v3) {
                                                            if (tempList.size() == 2) {
                                                                tempList.clear();
                                                                tempList.add(list.get(0));
                                                            }

                                                            tempList.add(tmpList.get(2));
                                                            yjhs = JhPhzCardUtils.loadYijuhua(tempList, v3, wangList.get(0), true, true, false);
                                                            if (yjhs.size() == 1) {
                                                                idx = i;
                                                                tempCard = tmpList.get(2);
                                                                break;
                                                            }
                                                        }
                                                    } else if (v1 == v3) {
                                                        if (v1 != v2) {
                                                            if (tempList.size() == 2) {
                                                                tempList.clear();
                                                                tempList.add(list.get(0));
                                                            }

                                                            tempList.add(tmpList.get(2));
                                                            yjhs = JhPhzCardUtils.loadYijuhua(tempList, v2, wangList.get(0), true, true, false);
                                                            if (yjhs.size() == 1) {
                                                                idx = i;
                                                                tempCard = tmpList.get(1);
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (idx >= 0) {
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().get(idx).remove(tempCard);

                                            linkCard1.getYjhList().add(yjhs.get(0));

                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                    }
                                }
                            }

                            break;
                        case 2:
                            if (JhPhzCardUtils.wangCard(cardResult.getCurId())) {
                                cardMessageList = new ArrayList<>(8);
                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, yjhLists, cardMessageList);
                                cardMessageList.add(new JhPhzCardMessage(JhPhzHuXiEnums.WEI, JhPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1))));
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
//                            System.out.println("2 wang hu");

                                for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                    JhPhzHandCards handCards1 = handCards.copy();
                                    List<Integer> list1 = handCards1.KAN.remove(kv.getKey());
                                    list1.add(wangList.get(0));

                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list1);
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                                for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                    JhPhzHandCards handCards1 = handCards.copy();
                                    List<Integer> list1 = handCards1.WEI.remove(kv.getKey());
                                    list1.add(wangList.get(0));

                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list1);
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                                for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                    JhPhzHandCards handCards1 = handCards.copy();
                                    List<Integer> list1 = handCards1.WEI_CHOU.remove(kv.getKey());
                                    list1.add(wangList.get(0));

                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list1);
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }

//                                for (Map.Entry<Integer,List<Integer>> kv:handCards.PENG.entrySet()){
//                                    JhPhzHandCards handCards1=handCards.copy();
//                                    List<Integer> list1=handCards1.PENG.remove(kv.getKey());
//                                    list1.add(wangList.get(0));
//
//                                    cardMessageList = new ArrayList<>(8);
//                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
//                                    linkCard1=new JhPhzLinkCard();
//                                    linkCard1.setYjhList(copy(yjhLists,null));
////                                    linkCard1.getYjhList().add(list1);
//                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(1)));
//                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
//                                    addCardMessage(cardResult,list1,cardMessageList);
//                                    linkCard1.getYjhList().add(list1);
//                                    check(cardMessageList, linkCard1, phzBase, linkCardList,uuid);
//                                }
                            } else {
                                boolean tempBl = true;
//                                System.out.println("2 wang common hu");
                                if (JhPhzCardUtils.sameCard(list.get(0), cardResult.getCurId()) || isTianHu) {
                                    linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                    tempBl = false;
                                }
                                if (isTianHu) {
                                    linkCard1.setHuCard(list.get(0));
                                }
                                cardMessageList = new ArrayList<>(8);
                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, yjhLists, cardMessageList);
                                cardMessageList.add(new JhPhzCardMessage(JhPhzHuXiEnums.WEI, JhPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1))));

//                            System.out.println("2 wang hu");

                                if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid) || tempBl) {
                                    for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                        JhPhzHandCards handCards1 = handCards.copy();
                                        List<Integer> list1 = handCards1.KAN.remove(kv.getKey());
                                        list1.add(wangList.get(0));

                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list1);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        if (JhPhzCardUtils.sameCard(list.get(0), cardResult.getCurId()) || isTianHu) {
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                        }
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(0));
                                        }
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                    for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                        JhPhzHandCards handCards1 = handCards.copy();
                                        List<Integer> list1 = handCards1.WEI.remove(kv.getKey());
                                        list1.add(wangList.get(0));

                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list1);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (JhPhzCardUtils.sameCard(list.get(0), cardResult.getCurId()) || isTianHu) {
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                        }
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(0));
                                        }
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                    for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                        JhPhzHandCards handCards1 = handCards.copy();
                                        List<Integer> list1 = handCards1.WEI_CHOU.remove(kv.getKey());
                                        list1.add(wangList.get(0));

                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list1);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (JhPhzCardUtils.sameCard(list.get(0), cardResult.getCurId()) || isTianHu) {
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                        }
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(0));
                                        }
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }

//                                    for (Map.Entry<Integer,List<Integer>> kv:handCards.PENG.entrySet()){
//                                        JhPhzHandCards handCards1=handCards.copy();
//                                        List<Integer> list1=handCards1.PENG.remove(kv.getKey());
//                                        list1.add(wangList.get(0));
//
//                                        cardMessageList = new ArrayList<>(8);
//                                        JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
//                                        linkCard1=new JhPhzLinkCard();
//                                        linkCard1.setYjhList(copy(yjhLists,null));
////                                    linkCard1.getYjhList().add(list1);
//                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(1)));
//                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
//                                        addCardMessage(cardResult,list1,cardMessageList);
//                                        linkCard1.getYjhList().add(list1);
//                                        if (JhPhzCardUtils.sameCard(list.get(0), cardResult.getCurId())){
//                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
//                                        }
//                                        check(cardMessageList, linkCard1, phzBase, linkCardList,uuid);
//                                    }
                                }
                            }
                            break;
                        case 4:
                            if (JhPhzCardUtils.wangCard(cardResult.getCurId()) || isTianHu) {
//                            System.out.println("wang zha wang hu");
                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG_WANG);
                                yjhLists.add(JhPhzCardUtils.asList(wangList.get(1), wangList.get(2), wangList.get(3)));
                                yjhLists.add(JhPhzCardUtils.asList(list.get(0), wangList.get(0)));
                                cardMessageList = new ArrayList<>(8);
                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, yjhLists, cardMessageList);
                                if (isTianHu) {
                                    linkCard1.setHuCard(wangList.get(1));
                                }
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                            } else {
//                            System.out.println("wang zha hu");
                                if (JhPhzCardUtils.sameCard(list.get(0), cardResult.getCurId())) {
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(wangList.get(2), wangList.get(3)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(wangList.get(2), wangList.get(3), wangList.get(1)));
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                } else {
                                    List<Integer> list1 = null;
                                    for (List<Integer> temp : yjhLists) {
                                        if (temp.contains(cardResult.getCurId())) {
                                            list1 = temp;
                                            break;
                                        }
                                    }
                                    if (list1 != null) {
                                        int idx = list1.indexOf(cardResult.getCurId());
                                        list1.set(idx, wangList.get(0));

                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(cardResult.getCurId(), wangList.get(2), wangList.get(3)));
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(cardResult.getCurId(), wangList.get(3)));
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                    }
                                }
                            }
                            break;
                        default:
//                        System.out.println("error wang hu");
                    }
                    break;
                case 2:
                    switch (wangList.size()) {
                        case 0:
                            if (JhPhzCardUtils.sameCard(list.get(0), list.get(1))) {
                                yjhLists.add(JhPhzCardUtils.asList(list.get(0), list.get(1)));
                                linkCard1.setYjhList(yjhLists);
                                cardMessageList = new ArrayList<>(8);
                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, yjhLists, cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                            }
                            break;
                        case 1:
                            if (JhPhzCardUtils.sameCard(list.get(0), list.get(1))) {
                                cardMessageList = new ArrayList<>(8);
                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                linkCard1 = new JhPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(0)));
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                if (!handCards.hasFourSameCard()) {
                                    for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                        JhPhzHandCards handCards1 = handCards.copy();
                                        List<Integer> list1 = handCards1.KAN.remove(kv.getKey());
                                        list1.add(wangList.get(0));

                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list1);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1)));
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                    for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                        JhPhzHandCards handCards1 = handCards.copy();
                                        List<Integer> list1 = handCards1.WEI.remove(kv.getKey());
                                        list1.add(wangList.get(0));

                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list1);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1)));
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                    for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                        JhPhzHandCards handCards1 = handCards.copy();
                                        List<Integer> list1 = handCards1.WEI_CHOU.remove(kv.getKey());
                                        list1.add(wangList.get(0));

                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list1);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1)));
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }

//                                    for (Map.Entry<Integer,List<Integer>> kv:handCards.PENG.entrySet()){
//                                        JhPhzHandCards handCards1=handCards.copy();
//                                        List<Integer> list1=handCards1.PENG.remove(kv.getKey());
//                                        list1.add(wangList.get(0));
//
//                                        cardMessageList = new ArrayList<>(8);
//                                        JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
//                                        linkCard1=new JhPhzLinkCard();
//                                        linkCard1.setYjhList(copy(yjhLists,null));
////                                    linkCard1.getYjhList().add(list1);
//                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1)));
//                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
//                                        addCardMessage(cardResult,list1,cardMessageList);
//                                        linkCard1.getYjhList().add(list1);
//
//                                        check(cardMessageList, linkCard1, phzBase, linkCardList,uuid);
//                                    }
                                }
                            }
                            break;
                        case 2:
                            break;
                        case 3:
                            if (JhPhzCardUtils.sameCard(list.get(0), list.get(1))) {
                                if (JhPhzCardUtils.wangCard(cardResult.getCurId()) || isTianHu) {
//                            System.out.println("wang chuang wang hu");
                                    linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG_WANG);
                                    if (isTianHu) {
                                        linkCard1.setHuCard(wangList.get(0));
                                    }
                                    yjhLists.add(JhPhzCardUtils.asList(wangList.get(0), wangList.get(1), wangList.get(2)));
                                    yjhLists.add(JhPhzCardUtils.asList(list.get(0), list.get(1)));

                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, yjhLists, cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                } else if (JhPhzCardUtils.commonCard(cardResult.getCurId())) {
                                    if (JhPhzCardUtils.hasCard(list, val)) {
                                        linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                        yjhLists.add(JhPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                        yjhLists.add(JhPhzCardUtils.asList(list.get(1), wangList.get(2)));

                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, yjhLists, cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    } else {
                                        for (List<Integer> yjh : yjhLists) {
                                            int card0 = JhPhzCardUtils.loadIdByVal(yjh, val);
                                            if (card0 > 0) {
                                                yjh.remove(Integer.valueOf(card0));
                                                yjh.add(wangList.get(0));

                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                linkCard1.setYjhList(copy(yjhLists, null));

                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(card0, wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1)));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                                    linkCard1 = new JhPhzLinkCard();
                                                    linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                    linkCard1.setYjhList(copy(yjhLists, null));

                                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(card0, wangList.get(1)));
                                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(2)));

                                                    cardMessageList = new ArrayList<>(8);
                                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                                }

                                                break;
                                            }
                                        }
                                    }
                                }

                            } else {
                                if (val >= 200) {
                                    List<List<Integer>> tempYjh = JhPhzCardUtils.loadYijuhua(list, JhPhzCardUtils.loadCardVal(list.get(0)), wangList.get(0), true, false, false);
                                    if (tempYjh != null && tempYjh.size() == 1) {
                                        yjhLists.add(tempYjh.get(0));
                                        yjhLists.add(JhPhzCardUtils.asList(wangList.get(1), wangList.get(2)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_DIAO_WANG);

                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, yjhLists, cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    } else {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), wangList.get(1), wangList.get(2)));

                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), wangList.get(2)));

                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                } else if (JhPhzCardUtils.hasCard(list, val)) {
                                    Integer card = JhPhzCardUtils.loadIdByVal(list, val);
                                    list.remove(card);

                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(card, wangList.get(1), wangList.get(2)));

                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(card, wangList.get(2)));

                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                } else {
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), wangList.get(1), wangList.get(2)));
                                    boolean tempBl = true;
                                    if (isTianHu) {
                                        linkCard1.setHuCard(list.get(1));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                        tempBl = false;
                                    }
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid) || tempBl) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), wangList.get(2)));
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(1));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        if (JhPhzCardUtils.commonCardByVal(val) && yjhLists.size() > 0) {
                                            List<List<Integer>> yjhs = JhPhzCardUtils.loadYijuhua(JhPhzCardUtils.asList(list.get(0), list.get(1), cardResult.getCurId()), val, 0, true, true, false);
                                            if (yjhs.size() == 1) {
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                for (List<Integer> list1 : linkCard1.getYjhList()) {
                                                    int idx = list1.indexOf(cardResult.getCurId());
                                                    if (idx >= 0) {
                                                        list1.remove(idx);
                                                        list1.add(wangList.get(0));
                                                        break;
                                                    }
                                                }
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(cardResult.getCurId(), wangList.get(2)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    }
                                }
                            }

                            break;
                        case 4:
                            if (JhPhzCardUtils.sameCard(list.get(0), list.get(1))) {
                                if (JhPhzCardUtils.wangCard(cardResult.getCurId()) || isTianHu) {
//                            System.out.println("wang zha wang hu");
                                    linkCard1.addFan(JhPhzFanEnums.WANG_ZHA_WANG);
                                    if (isTianHu) {
                                        linkCard1.setHuCard(wangList.get(0));
                                    }
                                    yjhLists.add(JhPhzCardUtils.asList(wangList.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                    yjhLists.add(JhPhzCardUtils.asList(list.get(0), list.get(1)));
                                    linkCard1.setYjhList(yjhLists);

                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                } else {
                                    int cur = JhPhzCardUtils.loadIdByVal(list, val);
                                    if (cur > 0) {
                                        linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                        yjhLists.add(JhPhzCardUtils.asList(list.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        yjhLists.add(JhPhzCardUtils.asList(list.get(1), wangList.get(0)));
                                        linkCard1.setYjhList(yjhLists);

                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    } else {
                                        for (List<Integer> yjh : yjhLists) {
                                            int card0 = JhPhzCardUtils.loadIdByVal(yjh, val);
                                            if (card0 > 0) {
                                                yjh.remove(Integer.valueOf(card0));
                                                yjh.add(wangList.get(0));
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(card0, wangList.get(1), wangList.get(2), wangList.get(3)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1)));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                                    linkCard1 = new JhPhzLinkCard();
                                                    linkCard1.setYjhList(copy(yjhLists, null));
                                                    linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(card0, wangList.get(3)));
                                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(1), wangList.get(2)));

                                                    cardMessageList = new ArrayList<>(8);
                                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                                }
                                                break;
                                            }
                                        }
                                    }
//                            System.out.println("wang zha hu");
                                }
                            } else {
                                int cur = JhPhzCardUtils.loadIdByVal(list, val);
                                if (cur > 0 || isTianHu) {
                                    list.remove(Integer.valueOf(cur));
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                    if (isTianHu) {
                                        cur = list.remove(0);
                                        linkCard1.setHuCard(cur);
                                    }
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(cur, wangList.get(0), wangList.get(1), wangList.get(2)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(3)));

                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                        if (isTianHu) {
                                            linkCard1.setHuCard(cur);
                                        }
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(cur, wangList.get(3)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1), wangList.get(2)));

                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                } else {
                                    List<List<Integer>> tempYjh = JhPhzCardUtils.loadYijuhua(list, JhPhzCardUtils.loadCardVal(list.get(0)), wangList.get(0), true, false, false);
                                    if (tempYjh != null && tempYjh.size() == 1) {
                                        for (List<Integer> yjh : yjhLists) {
                                            int card0 = JhPhzCardUtils.loadIdByVal(yjh, val);
                                            if (card0 > 0) {
                                                yjh.remove(Integer.valueOf(card0));
                                                yjh.add(wangList.get(1));
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(card0, wangList.get(2), wangList.get(3)));
                                                linkCard1.getYjhList().add(tempYjh.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                                break;
                                            }
                                        }

                                    } else {

                                    }
                                }
                            }

                            break;
                        default:
//                        System.out.println("error wang hu");
                    }
                    break;

                case 3:
                    int idx = list.indexOf(Integer.valueOf(JhPhzCardUtils.loadIdByVal(list, val)));
                    if (idx >= 0) {
                        int idx1, idx2;
                        if (idx == 0) {
                            idx1 = 1;
                            idx2 = 2;
                        } else if (idx == 1) {
                            idx1 = 0;
                            idx2 = 2;
                        } else {
                            idx1 = 1;
                            idx2 = 0;
                        }
                        if (JhPhzCardUtils.sameCard(list.get(idx1), list.get(idx2))) {
                            switch (wangList.size()) {
                                case 1:
                                    break;
                                case 2:
//                                System.out.println("wang chuang hu");
                                    linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(cardResult.getCurId(), wangList.get(0), wangList.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(idx1), list.get(idx2)));

                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(cardResult.getCurId(), wangList.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(idx1), list.get(idx2), wangList.get(1)));

                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                    break;
                                case 3:
//                                System.out.println("wang zha hu");
                                    linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);

                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(cardResult.getCurId(), wangList.get(0), wangList.get(1), wangList.get(2)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(idx1), list.get(idx2)));
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(cardResult.getCurId(), wangList.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(idx1), list.get(idx2), wangList.get(1), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                    break;
                            }
                        } else if (wangList.size() >= 2) {
                            List<Integer> list1 = JhPhzCardUtils.loadIdsByVal(list, val);
                            if (list1.size() == 2) {
                                list.removeAll(list1);
                                list.add(list1.get(0));
                                List<List<Integer>> yjhs = JhPhzCardUtils.loadYijuhua(list, val, wangList.get(0), true, false, false);
                                if (yjhs != null && yjhs.size() == 1) {
                                    if (wangList.size() == 2) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(yjhs.get(0));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(1), wangList.get(1)));
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                    } else if (wangList.size() == 3) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(yjhs.get(0));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(1), wangList.get(1), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(2)));
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1), wangList.get(2)));
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                    }
                                } else {
//                                    list1.add(wangList.get(0));
//                                    yjhLists.add(list1);
                                    if (wangList.size() == 2) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    } else if (wangList.size() == 3) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                }
                                break;
                            }
                        }
                    } else if (JhPhzCardUtils.wangCardByVal(val) || isTianHu) {
                        if (JhPhzCardUtils.sameCard(list.get(0), list.get(1))) {
                            int val0 = JhPhzCardUtils.loadCardVal(list.get(2)) - JhPhzCardUtils.loadCardVal(list.get(0));
                            if (val0 == 100 || val0 == 0) {
                                switch (wangList.size()) {
                                    case 2:
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), list.get(2)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(wangList.get(0), wangList.get(1)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_DIAO_WANG);
                                        if (isTianHu) {
                                            linkCard1.setHuCard(wangList.get(0));
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        if (val0 == 100) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));

                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2), wangList.get(0), wangList.get(1)));
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list.get(2));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));

                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(0)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2), wangList.get(1)));
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list.get(0));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }

                                        break;
                                    case 3:
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), list.get(2)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(wangList.get(0), wangList.get(1), wangList.get(2)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG_WANG);
                                        if (isTianHu) {
                                            linkCard1.setHuCard(wangList.get(0));
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        if ((!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid) || isTianHu) && val0 == 100) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));

                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2), wangList.get(2)));
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list.get(2));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));

                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2), wangList.get(0), wangList.get(1), wangList.get(2)));
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list.get(2));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                        break;
                                }
                            } else {
                                switch (wangList.size()) {
                                    case 2:
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2), wangList.get(1)));
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(2));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2), wangList.get(0), wangList.get(1)));
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(2));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        break;
                                    case 3:
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2), wangList.get(2)));
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(2));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2), wangList.get(0), wangList.get(1), wangList.get(2)));
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(2));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        break;
                                }
                            }
                        } else if (JhPhzCardUtils.sameCard(list.get(1), list.get(2))) {
                            int val0 = JhPhzCardUtils.loadCardVal(list.get(2)) - JhPhzCardUtils.loadCardVal(list.get(0));
                            if (val0 == 100 || val0 == 0) {
                                switch (wangList.size()) {
                                    case 2:
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), list.get(2)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(wangList.get(0), wangList.get(1)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_DIAO_WANG);
                                        if (isTianHu) {
                                            linkCard1.setHuCard(wangList.get(0));
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        if (val0 == 100) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));

                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), list.get(2)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list.get(0));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));

                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), list.get(2), wangList.get(0)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list.get(0));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }

                                        break;
                                    case 3:
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), list.get(2)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(wangList.get(0), wangList.get(1), wangList.get(2)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG_WANG);
                                        if (isTianHu) {
                                            linkCard1.setHuCard(wangList.get(0));
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        if ((!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid) || isTianHu) && val0 == 100) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));

                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), list.get(2), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(2)));
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list.get(0));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));

                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), list.get(2)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1), wangList.get(2)));
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list.get(0));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                        break;
                                }
                            } else {
                                switch (wangList.size()) {
                                    case 2:
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), list.get(2), wangList.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(0));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), list.get(2)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(0));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        break;
                                    case 3:
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), list.get(2), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(2)));
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(0));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), list.get(2)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1), wangList.get(2)));
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(0));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        break;
                                }
                            }
                        }
                    } else {
                        List<List<Integer>> yjhs;
                        switch (wangList.size()) {
                            case 0:
                                yjhs = JhPhzCardUtils.loadYijuhua(list, JhPhzCardUtils.loadCardVal(list.get(0)), 0, true, false, false);
                                if (yjhs.size() == 1) {
                                    linkCard1 = new JhPhzLinkCard();
                                    yjhLists.add(yjhs.get(0));
                                    linkCard1.setYjhList(yjhLists);
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                                break;
                            case 2:
                                yjhs = JhPhzCardUtils.loadYijuhua(JhPhzCardUtils.asList(list.get(0), list.get(1)), JhPhzCardUtils.loadCardVal(list.get(0)), wangList.get(0), true, false, false);
                                if (yjhs.size() == 1) {
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(yjhs.get(0));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2), wangList.get(1)));
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                                yjhs = JhPhzCardUtils.loadYijuhua(JhPhzCardUtils.asList(list.get(1), list.get(2)), JhPhzCardUtils.loadCardVal(list.get(1)), wangList.get(0), true, false, false);
                                if (yjhs.size() == 1) {
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(yjhs.get(0));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                                yjhs = JhPhzCardUtils.loadYijuhua(JhPhzCardUtils.asList(list.get(0), list.get(2)), JhPhzCardUtils.loadCardVal(list.get(0)), wangList.get(0), true, false, false);
                                if (yjhs.size() == 1) {
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(yjhs.get(0));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), wangList.get(1)));
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }

                                if (JhPhzCardUtils.sameCard(list.get(0), list.get(1))) {
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2), wangList.get(0), wangList.get(1)));
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                } else if (JhPhzCardUtils.sameCard(list.get(1), list.get(2))) {
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), list.get(2)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                                break;
                        }
                    }
                    break;

                case 4:
                    if (wangList.size() == 4) {
                        List<Integer> list1 = null;
                        for (Integer integer : list) {
                            list1 = JhPhzCardUtils.loadIdsByVal(list, JhPhzCardUtils.loadCardVal(integer));
                            if (list1.size() == 2) {
                                break;
                            }
                        }
                        if (list1.size() == 2) {
                            list.removeAll(list1);
                            if (JhPhzCardUtils.commonCardByVal(val)) {
                                int card0 = JhPhzCardUtils.loadIdByVal(list, val);
                                if (card0 > 0) {
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), wangList.get(2), wangList.get(3)));
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0)));
                                        if (card0 == list.get(0).intValue()) {
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), wangList.get(2), wangList.get(3)));
                                        } else {
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(2), wangList.get(3)));
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                } else {
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), wangList.get(2), wangList.get(3)));
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0)));
                                    if (card0 == list.get(0).intValue()) {
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), wangList.get(2), wangList.get(3)));
                                    } else {
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(2), wangList.get(3)));
                                    }
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                            } else if (JhPhzCardUtils.wangCardByVal(val) || isTianHu) {
                                List<List<Integer>> tmpYjhs = JhPhzCardUtils.loadYijuhua(list, JhPhzCardUtils.loadCardVal(list.get(0)), wangList.get(0), true, false, false);
                                if (tmpYjhs != null && tmpYjhs.size() == 1) {
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG_WANG);
                                    if (isTianHu) {
                                        linkCard1.setHuCard(wangList.get(1));
                                    }
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                    linkCard1.getYjhList().add(tmpYjhs.get(0));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(wangList.get(1), wangList.get(2), wangList.get(3)));
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.addFan(JhPhzFanEnums.WANG_DIAO_WANG);
                                        if (isTianHu) {
                                            linkCard1.setHuCard(wangList.get(2));
                                        }
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(1)));
                                        linkCard1.getYjhList().add(tmpYjhs.get(0));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(wangList.get(2), wangList.get(3)));
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                } else {
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), wangList.get(2), wangList.get(3)));
                                    if (isTianHu) {
                                        linkCard1.setHuCard(list.get(1));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                    }
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    if (isTianHu) {
                                        linkCard1.setHuCard(list.get(0));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                    }
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), wangList.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(2), wangList.get(3)));
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), wangList.get(2), wangList.get(3)));
                                    if (isTianHu) {
                                        linkCard1.setHuCard(list.get(1));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                    }
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                            } else {
                                linkCard1 = new JhPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), wangList.get(2), wangList.get(3)));
                                cardMessageList = new ArrayList<>(8);
                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                linkCard1 = new JhPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0)));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), wangList.get(1)));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(2), wangList.get(3)));
                                cardMessageList = new ArrayList<>(8);
                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                linkCard1 = new JhPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0)));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(1), wangList.get(2), wangList.get(3)));
                                cardMessageList = new ArrayList<>(8);
                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                            }
                        }
                    } else if (wangList.size() == 2) {
                        Map<Integer, Integer> map = JhPhzCardUtils.loadCardCount(list);
                        int val1 = JhPhzCardUtils.loadCardVal(list.get(0));
                        if (map.size() == 2 && map.get(val1).intValue() == 2) {
                            List<Integer> list1 = JhPhzCardUtils.loadIdsByVal(list, val);
                            if (list1.size() == 2) {
                                list.removeAll(list1);

                                int val0 = val - JhPhzCardUtils.loadCardVal(list.get(0));
                                if (val0 == 100 || val0 == -100) {
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list.get(0), list.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(1), wangList.get(0), wangList.get(1)));
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                            JhPhzHandCards handCards1 = handCards.copy();
                                            List<Integer> list2 = handCards1.KAN.remove(kv.getKey());
                                            list2.add(wangList.get(0));

                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list2);
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list.get(0), list.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(1), wangList.get(1)));
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                        for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                            JhPhzHandCards handCards1 = handCards.copy();
                                            List<Integer> list2 = handCards1.WEI.remove(kv.getKey());
                                            list2.add(wangList.get(0));

                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list2);
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list.get(0), list.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(1), wangList.get(1)));
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                        for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                            JhPhzHandCards handCards1 = handCards.copy();
                                            List<Integer> list2 = handCards1.WEI_CHOU.remove(kv.getKey());
                                            list2.add(wangList.get(0));

                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list2);
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list.get(0), list.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(1), wangList.get(1)));
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }

//                                    for (Map.Entry<Integer,List<Integer>> kv:handCards.PENG.entrySet()){
//                                        JhPhzHandCards handCards1=handCards.copy();
//                                        List<Integer> list2=handCards1.PENG.remove(kv.getKey());
//                                        list2.add(wangList.get(0));
//
//                                        cardMessageList = new ArrayList<>(8);
//                                        JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
//                                        linkCard1=new JhPhzLinkCard();
//                                        linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
//                                        linkCard1.setYjhList(copy(yjhLists,null));
//                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0),list.get(0),list.get(1)));
//                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(1), wangList.get(1)));
//                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
//                                        addCardMessage(cardResult,list2,cardMessageList);
//                                        linkCard1.getYjhList().add(list2);
//                                        check(cardMessageList, linkCard1, phzBase, linkCardList,uuid);
//                                    }
                                    }
                                }

                                linkCard1 = new JhPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0), wangList.get(1)));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1)));
                                cardMessageList = new ArrayList<>(8);
                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                linkCard1 = new JhPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(0), wangList.get(1)));
                                cardMessageList = new ArrayList<>(8);
                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                    JhPhzHandCards handCards1 = handCards.copy();
                                    List<Integer> list2 = handCards1.KAN.remove(kv.getKey());
                                    list2.add(wangList.get(0));

                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                                for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                    JhPhzHandCards handCards1 = handCards.copy();
                                    List<Integer> list2 = handCards1.WEI.remove(kv.getKey());
                                    list2.add(wangList.get(0));

                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                                for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                    JhPhzHandCards handCards1 = handCards.copy();
                                    List<Integer> list2 = handCards1.WEI_CHOU.remove(kv.getKey());
                                    list2.add(wangList.get(0));

                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }

//                                for (Map.Entry<Integer,List<Integer>> kv:handCards.PENG.entrySet()){
//                                    JhPhzHandCards handCards1=handCards.copy();
//                                    List<Integer> list2=handCards1.PENG.remove(kv.getKey());
//                                    list2.add(wangList.get(0));
//
//                                    cardMessageList = new ArrayList<>(8);
//                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
//                                    linkCard1=new JhPhzLinkCard();
//                                    linkCard1.setYjhList(copy(yjhLists,null));
//                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0),list.get(1)));
//                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0),list1.get(1), wangList.get(1)));
//                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
//                                    addCardMessage(cardResult,list2,cardMessageList);
//                                    linkCard1.getYjhList().add(list2);
//                                    check(cardMessageList, linkCard1, phzBase, linkCardList,uuid);
//
//                                    cardMessageList = new ArrayList<>(8);
//                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
//                                    linkCard1=new JhPhzLinkCard();
//                                    linkCard1.setYjhList(copy(yjhLists,null));
//                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0),list.get(1), wangList.get(1)));
//                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0),list1.get(1)));
//                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
//                                    addCardMessage(cardResult,list2,cardMessageList);
//                                    linkCard1.getYjhList().add(list2);
//                                    check(cardMessageList, linkCard1, phzBase, linkCardList,uuid);
//                                }
                            } else {
                                linkCard1 = new JhPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2), list.get(3), wangList.get(0), wangList.get(1)));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1)));
                                cardMessageList = new ArrayList<>(8);
                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                linkCard1 = new JhPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2), list.get(3)));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(0), wangList.get(1)));
                                cardMessageList = new ArrayList<>(8);
                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                    JhPhzHandCards handCards1 = handCards.copy();
                                    List<Integer> list2 = handCards1.KAN.remove(kv.getKey());
                                    list2.add(wangList.get(0));

                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2), list.get(3)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2), list.get(3), wangList.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                                for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                    JhPhzHandCards handCards1 = handCards.copy();
                                    List<Integer> list2 = handCards1.WEI.remove(kv.getKey());
                                    list2.add(wangList.get(0));

                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2), list.get(3)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2), list.get(3), wangList.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                                for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                    JhPhzHandCards handCards1 = handCards.copy();
                                    List<Integer> list2 = handCards1.WEI_CHOU.remove(kv.getKey());
                                    list2.add(wangList.get(0));

                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2), list.get(3)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2), list.get(3), wangList.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }

//                                for (Map.Entry<Integer,List<Integer>> kv:handCards.PENG.entrySet()){
//                                    JhPhzHandCards handCards1=handCards.copy();
//                                    List<Integer> list2=handCards1.PENG.remove(kv.getKey());
//                                    list2.add(wangList.get(0));
//
//                                    cardMessageList = new ArrayList<>(8);
//                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
//                                    linkCard1=new JhPhzLinkCard();
//                                    linkCard1.setYjhList(copy(yjhLists,null));
//                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2),list.get(3)));
//                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0),list.get(1), wangList.get(1)));
//                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
//                                    addCardMessage(cardResult,list2,cardMessageList);
//                                    linkCard1.getYjhList().add(list2);
//                                    check(cardMessageList, linkCard1, phzBase, linkCardList,uuid);
//
//                                    cardMessageList = new ArrayList<>(8);
//                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
//                                    linkCard1=new JhPhzLinkCard();
//                                    linkCard1.setYjhList(copy(yjhLists,null));
//                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2),list.get(3), wangList.get(1)));
//                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0),list.get(1)));
//                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
//                                    addCardMessage(cardResult,list2,cardMessageList);
//                                    linkCard1.getYjhList().add(list2);
//                                    check(cardMessageList, linkCard1, phzBase, linkCardList,uuid);
//                                }
                            }
                        }
                    } else if (wangList.size() == 1) {
                        Map<Integer, Integer> map = JhPhzCardUtils.loadCardCount(list);
                        int val1 = JhPhzCardUtils.loadCardVal(list.get(0));
                        if (map.size() == 2 && map.get(val1).intValue() == 2) {
                            List<Integer> list1 = JhPhzCardUtils.loadIdsByVal(list, val);
                            if (list1.size() == 2) {
                                list.removeAll(list1);

                                int val0 = val - JhPhzCardUtils.loadCardVal(list.get(0));
                                if (val0 == 100 || val0 == -100) {
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list.get(0), list.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(1), wangList.get(0)));
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                                linkCard1 = new JhPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(0)));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                cardMessageList = new ArrayList<>(8);
                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                linkCard1 = new JhPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1)));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0)));
                                cardMessageList = new ArrayList<>(8);
                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                            } else {
                                linkCard1 = new JhPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(0)));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2), list.get(3)));
                                cardMessageList = new ArrayList<>(8);
                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                linkCard1 = new JhPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(0), list.get(1)));
                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list.get(2), list.get(3), wangList.get(0)));
                                cardMessageList = new ArrayList<>(8);
                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                            }
                        }
                    }
                    break;

                case 5:
                    if (wangList.size() == 4) {
                        Map<Integer, Integer> countMap = JhPhzCardUtils.loadCardCount(list);
                        if (countMap.size() <= 3) {
                            List<List<Integer>> lists = new ArrayList<>(3);
                            for (Map.Entry<Integer, Integer> kv : countMap.entrySet()) {
                                lists.add(JhPhzCardUtils.loadIdsByVal(list, kv.getKey().intValue()));
                            }
                            if (lists.size() == 2) {
                                if (JhPhzCardUtils.wangCardByVal(val) || isTianHu) {
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(lists.get(0));
                                    linkCard1.getYjhList().add(lists.get(1));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(wangList.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                    linkCard1.addFan(JhPhzFanEnums.WANG_ZHA_WANG);

                                    if (isTianHu)
                                        linkCard1.setHuCard(wangList.get(0));

                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                } else if (JhPhzCardUtils.hasCard(lists.get(0), val)) {
                                    if (lists.get(0).size() == 2) {
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), wangList.get(0)));
                                        linkCard1.getYjhList().add(lists.get(1));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(1), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), lists.get(1).get(2), wangList.get(2)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(1), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.KAN.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), lists.get(1).get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(1), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), lists.get(1).get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(1), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), lists.get(1).get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(1), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    } else if (lists.get(0).size() == 3) {
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(lists.get(1));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(2), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(2), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), wangList.get(0)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(2), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.KAN.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(1), lists.get(0).get(2), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(1), lists.get(0).get(2), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(1), lists.get(0).get(2), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }

                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(2), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(2), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.KAN.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(1), lists.get(0).get(2), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(1), lists.get(0).get(2), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(1), lists.get(0).get(2), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    }
                                } else if (JhPhzCardUtils.hasCard(lists.get(1), val)) {
                                    if (lists.get(1).size() == 2) {
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), wangList.get(0)));
                                        linkCard1.getYjhList().add(lists.get(0));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(1), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), lists.get(0).get(2), wangList.get(2)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(1), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.KAN.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), lists.get(0).get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(1), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), lists.get(0).get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(1), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), lists.get(0).get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(1), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    } else if (lists.get(1).size() == 3) {
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(lists.get(0));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(2), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(2), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), wangList.get(0)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(2), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.KAN.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(1), lists.get(1).get(2), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(1), lists.get(1).get(2), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(1), lists.get(1).get(2), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }

                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(2), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(2), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.KAN.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(1), lists.get(1).get(2), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(1), lists.get(1).get(2), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(1), lists.get(1).get(2), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    }
                                } else if (JhPhzCardUtils.commonCardByVal(val)) {
                                    Integer curCard = cardResult.getCurId();
                                    for (List<Integer> list1 : yjhLists) {
                                        if (list1.contains(curCard)) {
                                            list1.remove(curCard);
                                            list1.add(wangList.get(0));
                                            break;
                                        }
                                    }
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(lists.get(1));
                                    linkCard1.getYjhList().add(lists.get(0));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(curCard, wangList.get(1), wangList.get(2), wangList.get(3)));
                                    linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        if (lists.get(0).size() == 2 && JhPhzCardUtils.bigCard(lists.get(0).get(0))) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), wangList.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), lists.get(1).get(2)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(curCard, wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        } else if (lists.get(1).size() == 2 && JhPhzCardUtils.bigCard(lists.get(1).get(0))) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), wangList.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), lists.get(0).get(2)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(curCard, wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                    }
                                }
                            } else if (lists.size() == 3) {
                                List<Integer> list1, list2, list3;
                                if (lists.get(0).size() == 1) {
                                    list1 = lists.get(0);
                                    if (lists.get(1).size() >= 2) {
                                        list2 = lists.get(1);
                                        list3 = lists.get(2);
                                    } else {
                                        list2 = lists.get(2);
                                        list3 = lists.get(1);
                                    }
                                } else if (lists.get(1).size() == 1) {
                                    list1 = lists.get(1);
                                    if (lists.get(0).size() >= 2) {
                                        list2 = lists.get(0);
                                        list3 = lists.get(2);
                                    } else {
                                        list2 = lists.get(2);
                                        list3 = lists.get(0);
                                    }
                                } else {
                                    list1 = lists.get(2);
                                    if (lists.get(1).size() >= 2) {
                                        list2 = lists.get(1);
                                        list3 = lists.get(0);
                                    } else {
                                        list2 = lists.get(0);
                                        list3 = lists.get(1);
                                    }
                                }

                                if (list3.size() == 2) {
                                    if (JhPhzCardUtils.loadCardVal(list1.get(0)) == val || isTianHu) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list1.get(0));
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        boolean checkChuang = true;
                                        if (check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            checkChuang = false;
                                        }

                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list2);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list1.get(0));
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            checkChuang = false;
                                        }

                                        if (checkChuang) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list1.get(0));
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0), wangList.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                if (isTianHu) {
                                                    linkCard1.setHuCard(list1.get(0));
                                                }
                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                if (isTianHu) {
                                                    linkCard1.setHuCard(list1.get(0));
                                                }
                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                                for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                    JhPhzHandCards handCards1 = handCards.copy();
                                                    List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                    list10.add(wangList.get(0));

                                                    cardMessageList = new ArrayList<>(8);
                                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                    linkCard1 = new JhPhzLinkCard();
                                                    linkCard1.setYjhList(copy(yjhLists, null));
                                                    linkCard1.getYjhList().add(list10);
                                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                    linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                    if (isTianHu) {
                                                        linkCard1.setHuCard(list1.get(0));
                                                    }
                                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                                }
                                                for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                    JhPhzHandCards handCards1 = handCards.copy();
                                                    List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                    list10.add(wangList.get(0));

                                                    cardMessageList = new ArrayList<>(8);
                                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                    linkCard1 = new JhPhzLinkCard();
                                                    linkCard1.setYjhList(copy(yjhLists, null));
                                                    linkCard1.getYjhList().add(list10);
                                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                    linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                    if (isTianHu) {
                                                        linkCard1.setHuCard(list1.get(0));
                                                    }
                                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                                }
                                                for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                    JhPhzHandCards handCards1 = handCards.copy();
                                                    List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                    list10.add(wangList.get(0));

                                                    cardMessageList = new ArrayList<>(8);
                                                    JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                    linkCard1 = new JhPhzLinkCard();
                                                    linkCard1.setYjhList(copy(yjhLists, null));
                                                    linkCard1.getYjhList().add(list10);
                                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                    linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                    if (isTianHu) {
                                                        linkCard1.setHuCard(list1.get(0));
                                                    }
                                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                                }
                                            }
                                        }
                                    }

                                    List<List<Integer>> yjhList;
                                    if (JhPhzCardUtils.loadCardVal(list2.get(0)) == val || isTianHu) {
                                        yjhList = JhPhzCardUtils.loadYijuhua(JhPhzCardUtils.asList(list1.get(0), list2.get(0)), JhPhzCardUtils.loadCardVal(list1.get(0)), wangList.get(0), false, false);
                                        if (yjhList.size() == 1) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list3);
                                            linkCard1.getYjhList().add(yjhList.get(0));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(1), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list2.get(1));
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                    }

                                    if (JhPhzCardUtils.loadCardVal(list3.get(0)) == val || isTianHu) {
                                        yjhList = JhPhzCardUtils.loadYijuhua(JhPhzCardUtils.asList(list1.get(0), list3.get(0)), JhPhzCardUtils.loadCardVal(list1.get(0)), wangList.get(0), false, false);
                                        if (yjhList.size() == 1) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list2);
                                            linkCard1.getYjhList().add(yjhList.get(0));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(1), wangList.get(1), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list3.get(1));
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                    }
                                } else {
                                    if (list2.size() == 3 && JhPhzCardUtils.loadCardVal(list2.get(0)) == val) {
                                        List<List<Integer>> yjhList = JhPhzCardUtils.loadYijuhua(JhPhzCardUtils.asList(list1.get(0), list3.get(0)), JhPhzCardUtils.loadCardVal(list1.get(0)), wangList.get(0), false, false);
                                        if (yjhList.size() == 1) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1)));
                                            linkCard1.getYjhList().add(yjhList.get(0));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(2), wangList.get(1), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list2.get(2));
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                    }
                                }

                                int tempVal;
                                if (isTianHu) {
                                    if ((tempVal = JhPhzCardUtils.loadCardVal(list1.get(0)) - JhPhzCardUtils.loadCardVal(list2.get(0))) == 100 || tempVal == -100) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list2.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(wangList.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_ZHA_WANG);
                                        linkCard1.setHuCard(wangList.get(0));
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list3);
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                            linkCard1.setHuCard(list1.get(0));
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(0)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                            linkCard1.setHuCard(list1.get(0));
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                linkCard1.setHuCard(list1.get(0));
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                linkCard1.setHuCard(list1.get(0));
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                linkCard1.setHuCard(list1.get(0));
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    } else if ((tempVal = JhPhzCardUtils.loadCardVal(list1.get(0)) - JhPhzCardUtils.loadCardVal(list3.get(0))) == 100 || tempVal == -100) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list2);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list3.get(0), list3.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(wangList.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_ZHA_WANG);
                                        linkCard1.setHuCard(wangList.get(0));
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list3);
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                            linkCard1.setHuCard(list1.get(0));
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(0)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                            linkCard1.setHuCard(list1.get(0));
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                linkCard1.setHuCard(list1.get(0));
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                linkCard1.setHuCard(list1.get(0));
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                linkCard1.setHuCard(list1.get(0));
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    } else if (JhPhzCardUtils.loadYijuhua(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)), JhPhzCardUtils.loadCardVal(list1.get(0)), 0, false, true).size() == 1) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), list3.get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG_WANG);
                                        linkCard1.setHuCard(wangList.get(0));
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                    boolean tempBl = true;
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(0)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                    linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                    linkCard1.setHuCard(list1.get(0));
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        tempBl = false;
                                    }

                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                    linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                    linkCard1.setHuCard(list1.get(0));
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        tempBl = false;
                                    }

                                    if (tempBl) {
                                        for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                            JhPhzHandCards handCards1 = handCards.copy();
                                            List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                            list10.add(wangList.get(0));

                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list10);
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                            linkCard1.setHuCard(list1.get(0));
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                        for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                            JhPhzHandCards handCards1 = handCards.copy();
                                            List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                            list10.add(wangList.get(0));

                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list10);
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                            linkCard1.setHuCard(list1.get(0));
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                        for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                            JhPhzHandCards handCards1 = handCards.copy();
                                            List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                            list10.add(wangList.get(0));

                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list10);
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                            linkCard1.setHuCard(list1.get(0));
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                    }
                                } else if (JhPhzCardUtils.wangCardByVal(val)) {
                                    if ((tempVal = JhPhzCardUtils.loadCardVal(list1.get(0)) - JhPhzCardUtils.loadCardVal(list2.get(0))) == 100 || tempVal == -100) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list2.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(wangList.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_ZHA_WANG);
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    } else if ((tempVal = JhPhzCardUtils.loadCardVal(list1.get(0)) - JhPhzCardUtils.loadCardVal(list3.get(0))) == 100 || tempVal == -100) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list2);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list3.get(0), list3.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(wangList.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_ZHA_WANG);
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    } else if (JhPhzCardUtils.loadYijuhua(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)), JhPhzCardUtils.loadCardVal(list1.get(0)), 0, false, true).size() == 1) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), list3.get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG_WANG);
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                } else if (JhPhzCardUtils.loadCardVal(list1.get(0)) == val) {
                                    boolean tempBl = true;
                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list3);
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                    linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        tempBl = false;
                                    }

                                    linkCard1 = new JhPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(0)));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                    linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        tempBl = false;
                                    }

                                    if (tempBl) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(2)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1), wangList.get(2)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                            JhPhzHandCards handCards1 = handCards.copy();
                                            List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                            list10.add(wangList.get(0));

                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list10);
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                        for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                            JhPhzHandCards handCards1 = handCards.copy();
                                            List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                            list10.add(wangList.get(0));

                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list10);
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                        for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                            JhPhzHandCards handCards1 = handCards.copy();
                                            List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                            list10.add(wangList.get(0));

                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list10);
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                    }
                                } else if (JhPhzCardUtils.loadCardVal(list2.get(0)) == val) {
                                    if ((tempVal = JhPhzCardUtils.loadCardVal(list1.get(0)) - JhPhzCardUtils.loadCardVal(list2.get(0))) == 100 || tempVal == -100) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), wangList.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), wangList.get(0)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    } else if ((tempVal = JhPhzCardUtils.loadCardVal(list1.get(0)) - JhPhzCardUtils.loadCardVal(list3.get(0))) == 100 || tempVal == -100) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), wangList.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list3.get(0), list3.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), list1.get(0)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), wangList.get(0), wangList.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), list1.get(0)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), wangList.get(0), wangList.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), list1.get(0)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), wangList.get(0), wangList.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    } else if (JhPhzCardUtils.loadYijuhua(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)), JhPhzCardUtils.loadCardVal(list1.get(0)), 0, false, true).size() == 1) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(1), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(1), wangList.get(0), wangList.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(1), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(2)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(1), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(2)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(1), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(2)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }

                                    }
                                } else if (JhPhzCardUtils.loadCardVal(list3.get(0)) == val) {
                                    if ((tempVal = JhPhzCardUtils.loadCardVal(list1.get(0)) - JhPhzCardUtils.loadCardVal(list3.get(0))) == 100 || tempVal == -100) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list2);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list3.get(0), wangList.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(1), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list3.get(0), wangList.get(0)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(1), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list3.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list3.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list3.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    } else if ((tempVal = JhPhzCardUtils.loadCardVal(list1.get(0)) - JhPhzCardUtils.loadCardVal(list2.get(0))) == 100 || tempVal == -100) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), wangList.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list2.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(1), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), list1.get(0)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), wangList.get(0), wangList.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(1), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), list1.get(0)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), wangList.get(0), wangList.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(1), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), list1.get(0)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), wangList.get(0), wangList.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(1), wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    } else if (JhPhzCardUtils.loadYijuhua(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)), JhPhzCardUtils.loadCardVal(list1.get(0)), 0, false, true).size() == 1) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(0), wangList.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(1), wangList.get(3)));
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(1), wangList.get(2)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(1), wangList.get(2)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(1), wangList.get(2)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    }
                                } else if (JhPhzCardUtils.commonCardByVal(val)) {
                                    Integer curCard = cardResult.getCurId();
                                    for (List<Integer> list11 : yjhLists) {
                                        if (list11.contains(curCard)) {
                                            list11.remove(curCard);
                                            list11.add(wangList.get(0));
                                            break;
                                        }
                                    }

                                    if ((tempVal = JhPhzCardUtils.loadCardVal(list1.get(0)) - JhPhzCardUtils.loadCardVal(list2.get(0))) == 100 || tempVal == -100) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list2.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(curCard, wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(1));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list2.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(curCard, wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(1));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list2.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(curCard, wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(1));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list2.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(curCard, wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }

                                    } else if ((tempVal = JhPhzCardUtils.loadCardVal(list1.get(0)) - JhPhzCardUtils.loadCardVal(list3.get(0))) == 100 || tempVal == -100) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list3.get(0), list3.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(curCard, wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(1));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list3.get(0), list3.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(curCard, wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(1));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list3.get(0), list3.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(curCard, wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(1));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list3.get(0), list3.get(1)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(curCard, wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    } else if (JhPhzCardUtils.loadYijuhua(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)), JhPhzCardUtils.loadCardVal(list1.get(0)), 0, false, true).size() == 1) {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), list3.get(1), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(curCard, wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(1));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), list3.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(curCard, wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(1));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), list3.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(curCard, wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                JhPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(1));

                                                cardMessageList = new ArrayList<>(8);
                                                JhPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new JhPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), list3.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(curCard, wangList.get(3)));
                                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (wangList.size() == 3 && JhPhzCardUtils.hasCard(list, val)) {
                        Map<Integer, Integer> map = JhPhzCardUtils.loadCardCount(list);
                        List<List<Integer>> lists = new ArrayList<>(2);
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            if (kv.getValue().intValue() >= 2) {
                                lists.add(JhPhzCardUtils.loadIdsByVal(list, kv.getKey()));
                            }
                        }
                        for (List<Integer> list1 : lists) {
                            if (list1.size() == 2) {
                                List<Integer> list2 = new ArrayList<>(list);
                                list2.removeAll(list1);
                                List<List<Integer>> yjhs = JhPhzCardUtils.loadYijuhua(list2, JhPhzCardUtils.loadCardVal(list2.get(0)), 0, true, false, false);
                                if (yjhs.size() == 1) {
                                    List<Integer> list3 = yjhs.get(0);
                                    int card0 = JhPhzCardUtils.loadIdByVal(list3, val);
                                    if (card0 > 0) {
                                        list3.remove(Integer.valueOf(card0));
                                        list3.add(wangList.get(0));
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(card0, wangList.get(1), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list3);
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(card0, wangList.get(2)));
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                    } else {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(1), wangList.get(1), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                } else {
                                    yjhs = JhPhzCardUtils.loadYijuhua(list2, JhPhzCardUtils.loadCardVal(list2.get(0)), wangList.get(0), true, false, false);
                                    yjhs.addAll(JhPhzCardUtils.loadYijuhua(list2, JhPhzCardUtils.loadCardVal(list2.get(1)), wangList.get(0), true, false, false));
                                    for (List<Integer> list3 : yjhs) {
                                        List<Integer> list4 = new ArrayList<>(list2);
                                        list4.removeAll(list3);

                                        linkCard1 = new JhPhzLinkCard();
                                        if (val == JhPhzCardUtils.loadCardVal(list4.get(0))) {
                                            linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                        }
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list4.get(0), wangList.get(1), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        linkCard1 = new JhPhzLinkCard();
                                        if (val == JhPhzCardUtils.loadCardVal(list4.get(0))) {
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                        }
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list4.get(0), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                }
                                int val0 = JhPhzCardUtils.loadCardVal(list1.get(0));
                                int card0 = JhPhzCardUtils.loadIdByVal(list2, val0 > 100 ? (val0 - 100) : (val0 + 100));
                                if (card0 > 0) {
                                    list2.remove(Integer.valueOf(card0));
                                    linkCard1 = new JhPhzLinkCard();
                                    if (JhPhzCardUtils.hasCard(list2, val)) {
                                        linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                    }
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), card0));
                                    if (val == JhPhzCardUtils.loadCardVal(list2.get(0))) {
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(2)));
                                    } else {
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), wangList.get(2)));
                                    }
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    linkCard1 = new JhPhzLinkCard();
                                    if (JhPhzCardUtils.hasCard(list2, val)) {
                                        linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                    }
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), card0));
                                    if (val == JhPhzCardUtils.loadCardVal(list2.get(0))) {
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), wangList.get(2)));
                                    } else {
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(2)));
                                    }
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                            } else if (list1.size() == 3) {
                                List<Integer> list2 = new ArrayList<>(list);
                                list2.removeAll(list1);
                                list2.add(list1.get(2));
                                List<List<Integer>> yjhs = JhPhzCardUtils.loadYijuhua(list2, JhPhzCardUtils.loadCardVal(list2.get(0)), 0, true, false, false);
                                if (yjhs.size() == 1) {
                                    List<Integer> list3 = yjhs.get(0);
                                    int card0 = JhPhzCardUtils.loadIdByVal(list3, val);
                                    if (card0 > 0) {
                                        list3.remove(Integer.valueOf(card0));
                                        list3.add(wangList.get(0));
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(card0, wangList.get(1), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new JhPhzLinkCard();
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list3);
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(card0, wangList.get(2)));
                                            cardMessageList = new ArrayList<>(8);
                                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }

                                    } else {
                                        linkCard1 = new JhPhzLinkCard();
                                        linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), wangList.get(0)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(1), wangList.get(1), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                } else {
                                    yjhs = JhPhzCardUtils.loadYijuhua(list2, JhPhzCardUtils.loadCardVal(list2.get(0)), wangList.get(0), true, false, false);
                                    yjhs.addAll(JhPhzCardUtils.loadYijuhua(list2, JhPhzCardUtils.loadCardVal(list2.get(1)), wangList.get(0), true, false, false));
                                    for (List<Integer> list3 : yjhs) {
                                        List<Integer> list4 = new ArrayList<>(list2);
                                        list4.removeAll(list3);

                                        linkCard1 = new JhPhzLinkCard();
                                        if (val == JhPhzCardUtils.loadCardVal(list4.get(0))) {
                                            linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                        }
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list4.get(0), wangList.get(1), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        linkCard1 = new JhPhzLinkCard();
                                        if (val == JhPhzCardUtils.loadCardVal(list4.get(0))) {
                                            linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                        }
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list4.get(0), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                }
                                int val0 = JhPhzCardUtils.loadCardVal(list1.get(0));
                                int card0 = JhPhzCardUtils.loadIdByVal(list2, val0);
                                if (card0 > 0) {
                                    list2.remove(Integer.valueOf(card0));
                                    linkCard1 = new JhPhzLinkCard();
                                    if (JhPhzCardUtils.hasCard(list2, val)) {
                                        linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
                                    }
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), card0));
                                    if (val == JhPhzCardUtils.loadCardVal(list2.get(0))) {
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(2)));
                                    } else {
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), wangList.get(2)));
                                    }
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    linkCard1 = new JhPhzLinkCard();
                                    if (JhPhzCardUtils.hasCard(list2, val)) {
                                        linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
                                    }
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(JhPhzCardUtils.asList(list1.get(0), list1.get(1), card0));
                                    if (val == JhPhzCardUtils.loadCardVal(list2.get(0))) {
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), wangList.get(2)));
                                    } else {
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(0), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(JhPhzCardUtils.asList(list2.get(1), wangList.get(2)));
                                    }
                                    cardMessageList = new ArrayList<>(8);
                                    JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                            }
                        }
                    }

                    break;
            }
        }

    }

	private static void checkListHu(Map<JhPhzLinkCard, Integer> linkCardList, JhPhzCardResult cardResult,
			final JhPhzBase phzBase, final String uuid, List<List<Integer>> yjhLists, JhPhzHandCards handCards,
			List<Integer> wangList, boolean isTianHu, int val, JhPhzLinkCard linkCard1) {
		List<JhPhzCardMessage> cardMessageList;
		linkCard1.setYjhList(copy(yjhLists, null));
		switch (wangList.size()) {
		    case 0:
		        cardMessageList = new ArrayList<>(8);
		        JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
		        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

		        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
		        break;
		    case 1:
		        break;
		    case 2:
		        if (handCards.hasFourSameCard()) {
		            if (JhPhzCardUtils.wangCard(cardResult.getCurId()) || isTianHu) {
		                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO_WANG);
		                yjhLists.add(JhPhzCardUtils.asList(wangList.get(0), wangList.get(1)));
		                linkCard1.setYjhList(yjhLists);
		                cardMessageList = new ArrayList<>(8);
		                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
		                addCardMessageList(cardResult, yjhLists, cardMessageList);

		                if (isTianHu) {
		                    linkCard1.setHuCard(wangList.get(0));
		                }

		                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
		            } else if (JhPhzCardUtils.commonCard(cardResult.getCurId())) {
		                boolean checkDiao = true;
		                for (List<Integer> temp : yjhLists) {
		                    if (temp.size() == 3) {
		                        int card0 = JhPhzCardUtils.loadIdByVal(temp, val);
		                        if (card0 > 0) {
		                            List<Integer> tmpList = new ArrayList<>(temp);
		                            tmpList.remove(Integer.valueOf(card0));
		                            if (JhPhzCardUtils.sameCard(tmpList.get(0), tmpList.get(1))) {
		                                linkCard1 = new JhPhzLinkCard();
		                                linkCard1.setYjhList(copy(yjhLists, temp));
		                                linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
		                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(card0, wangList.get(0), wangList.get(1)));
		                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(tmpList.get(0), tmpList.get(1)));

		                                cardMessageList = new ArrayList<>(8);
		                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
		                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

		                                if (checkDiao) {
		                                    checkDiao = !check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
		                                } else {
		                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
		                                }
		                            }

		                            if (checkDiao) {
		                                checkDiao = false;
		                                linkCard1 = new JhPhzLinkCard();
		                                linkCard1.setYjhList(copy(yjhLists, temp));
		                                linkCard1.addFan(JhPhzFanEnums.WANG_DIAO);
		                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(tmpList.get(0), tmpList.get(1), wangList.get(0)));
		                                linkCard1.getYjhList().add(JhPhzCardUtils.asList(card0, wangList.get(1)));
		                                cardMessageList = new ArrayList<>(8);
		                                JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
		                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

		                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
		                            }
		                        }
		                    }
		                }
		            }else {
//                        	 linkCard1.addFan(JhPhzFanEnums.WANG_DIAO_WANG);
		                 yjhLists.add(JhPhzCardUtils.asList(wangList.get(0), wangList.get(1)));
		                 linkCard1.setYjhList(yjhLists);
		                 cardMessageList = new ArrayList<>(8);
		                 JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
		                 addCardMessageList(cardResult, yjhLists, cardMessageList);

		                 if (isTianHu) {
		                     linkCard1.setHuCard(wangList.get(0));
		                 }

		                 check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
		            }
		        }
		        break;
		    case 3:
		        if (handCards.hasFourSameCard()) {
		            break;
		        }
		        if (JhPhzCardUtils.wangCard(cardResult.getCurId()) || isTianHu) {
		            linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG_WANG);
		            List<Integer> temp = JhPhzCardUtils.asList(wangList.get(0), wangList.get(1), wangList.get(2));
		            yjhLists.add(temp);

		            linkCard1.setYjhList(yjhLists);
		            cardMessageList = new ArrayList<>(8);
		            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
		            addCardMessageList(cardResult, yjhLists, cardMessageList);
		            if (isTianHu) {
		                linkCard1.setHuCard(wangList.get(0));
		            }
		            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
		        } else if (JhPhzCardUtils.commonCard(cardResult.getCurId())) {
		            boolean checkChuang = true;
		            for (List<Integer> temp : yjhLists) {
		                if (temp.size() == 3) {
		                    int card0 = JhPhzCardUtils.loadIdByVal(temp, val);
		                    if (card0 > 0) {
		                        List<Integer> tmpList = new ArrayList<>(temp);
		                        tmpList.remove(Integer.valueOf(card0));
		                        if (JhPhzCardUtils.sameCard(tmpList.get(0), tmpList.get(1))) {
		                            linkCard1 = new JhPhzLinkCard();
		                            linkCard1.setYjhList(copy(yjhLists, temp));
		                            linkCard1.addFan(JhPhzFanEnums.WANG_ZHA);
		                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(card0, wangList.get(0), wangList.get(1), wangList.get(2)));
		                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(tmpList.get(0), tmpList.get(1)));

		                            cardMessageList = new ArrayList<>(8);
		                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
		                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

		                            if (checkChuang) {
		                                checkChuang = !check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
		                            } else {
		                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
		                            }
		                        }

		                        if (checkChuang) {
		                            checkChuang = false;
		                            linkCard1 = new JhPhzLinkCard();
		                            linkCard1.setYjhList(copy(yjhLists, temp));
		                            linkCard1.addFan(JhPhzFanEnums.WANG_CHUANG);
		                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(tmpList.get(0), tmpList.get(1), wangList.get(0)));
		                            linkCard1.getYjhList().add(JhPhzCardUtils.asList(card0, wangList.get(1), wangList.get(2)));
		                            cardMessageList = new ArrayList<>(8);
		                            JhPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
		                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

		                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
		                        }
		                    }
		                }
		            }
		        }

		        break;
		    default:
//                    System.out.println("error wang hu");
		}
	}


    public static int loadMaxFanCount(Set<JhPhzFanEnums> fans) {
        int fanCount = 1;
        for (JhPhzFanEnums fan : fans) {
            fanCount *= fan.getFan();
        }
        return fanCount;
    }

    public static boolean check(List<JhPhzCardMessage> cardMessageList, JhPhzLinkCard linkCard, JhPhzBase phzBase, Map<JhPhzLinkCard, Integer> linkCards, final String uuid) {
        linkCard.setCardMessageList(cardMessageList);
        linkCard.setHuxi(linkCard.loadHuxi());
        boolean bl = linkCard.getHuxi() >= phzBase.loadQihuxi();
        if (bl) {
            int fan = loadMaxFanCount(linkCard.getFans());
            int currentFan;
            synchronized (uuid) {
                currentFan = BEAT_SWITCH.get(uuid);
                if (fan > currentFan) {
                    BEAT_SWITCH.put(uuid, fan);
                }
            }

            if (currentFan <= fan) {
//                String ret = linkCards.get(linkCard);
//                if (ret == null) {
                linkCards.put(linkCard, fan);
//                } else {
//                    int r = Integer.parseInt(ret);
//                    if (r > fan) {
//                        bl = false;
//                    } else if (r == fan) {
//                        linkCard.appendValStr();
//                        bl = linkCards.put(linkCard, String.valueOf(fan)) == null;
//                    } else {
//                        linkCards.put(linkCard, String.valueOf(fan));
//                    }
//                }
            }
        }
        return bl;
    }

    public static void addCardMessageList(JhPhzCardResult cardResult, List<List<Integer>> yjhLists, List<JhPhzCardMessage> cardMessageList) {
        for (List<Integer> tmpList : yjhLists) {
            JhPhzCardMessage cm = new JhPhzCardMessage(JhPhzCardUtils.loadHuXiEnum(tmpList), tmpList);
            if (!cardResult.isSelf() && cm.getHuXiEnum() == JhPhzHuXiEnums.WEI && tmpList.contains(cardResult.getCurId()) && !JhPhzCardUtils.wangCard(cardResult.getCurId())) {
                cm = new JhPhzCardMessage(JhPhzHuXiEnums.PENG, tmpList);
            }
            cardMessageList.add(cm);
        }
    }

    public static void addCardMessage(JhPhzCardResult cardResult, List<Integer> yjh, List<JhPhzCardMessage> cardMessageList) {
        JhPhzCardMessage cm = new JhPhzCardMessage(yjh.size() == 4 ? JhPhzHuXiEnums.PAO : JhPhzCardUtils.loadHuXiEnum(yjh), yjh);
        if (!cardResult.isSelf() && cm.getHuXiEnum() == JhPhzHuXiEnums.WEI && yjh.contains(cardResult.getCurId()) && !JhPhzCardUtils.wangCard(cardResult.getCurId())) {
            cm = new JhPhzCardMessage(JhPhzHuXiEnums.PENG, yjh);
        }
        cardMessageList.add(cm);
    }

    private static List<List<Integer>> copy(List<List<Integer>> lists, List<Integer> notThis) {
        List<List<Integer>> result = new ArrayList<>(7);
        for (List<Integer> list : lists) {
            if (notThis != list) {
                result.add(new ArrayList<>(list));
            }
        }
        return result;
    }

}
