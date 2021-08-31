package com.sy599.game.qipai.ldsphz.util;

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
import com.sy599.game.qipai.ldsphz.bean.LdsPhzBase;
import com.sy599.game.util.LogUtil;

public final class LdsPhzHuPaiUtils {

    private static final ExecutorService EXECUTOR_SERVICE = TaskExecutor.EXECUTOR_SERVICE;//Executors.newCachedThreadPool();
    private static final Map<String, Integer> BEAT_SWITCH = new ConcurrentHashMap<>();

    public static LdsPhzCardResult huPai(final LdsPhzHandCards mHandCards, final int currentId, final LdsPhzBase phzBase, boolean isSelf, Object user, int xingCard) {
        LdsPhzCardResult cardResult = new LdsPhzCardResult();
        cardResult.setCurId(currentId);
        cardResult.setPhzBase(phzBase);
        cardResult.setSelf(isSelf);

        final LdsPhzHandCards handCards;
        if (mHandCards.WANGS.size() > 0) {
            if (phzBase.loadHuMode() >= 0) {
                if (!isSelf) {
                    return cardResult;
                }
            }
            handCards = mHandCards.copy();
            if (LdsPhzCardUtils.wangCard(currentId)) {
                handCards.WANGS.add(currentId);
            }
        } else {
            if (LdsPhzCardUtils.wangCard(currentId)) {
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

        if (LdsPhzCardUtils.commonCard(currentId)) {
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
        cardResult.setRedCount(mHandCards.loadRedCardCount(true) + (LdsPhzCardUtils.redCard(currentId) ? 1 : 0));
        LdsPhzCardUtils.sort(list);

        cardResult.setHandCards(handCards);

        long time1 = System.currentTimeMillis();

        List<Integer> wangList = new ArrayList<>(handCards.WANGS);
//        Set<List<Integer>> rests = new HashSet<>();
        Map<Integer, Integer> map = LdsPhzCardUtils.loadCardCount(list);
        Set<LdsPhzCardMsg> lists = new HashSet<>();
        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
            List<List<Integer>> temps = LdsPhzCardUtils.loadYijuhua(list, kv.getKey(), 0, true, false);
            for (List<Integer> t : temps) {
                lists.add(new LdsPhzCardMsg(t));
            }
        }

        if (wangList.size() > 0 && list.size() > 1) {
            boolean bl1 = true;
            if (wangList.size() >= 3 && list.size() == 2) {
                bl1 = false;
                lists.clear();
            } else if (list.size() == 3 && map.size() == 2 && (wangList.size() == 3 && LdsPhzCardUtils.loadIdByVal(list, LdsPhzCardUtils.loadCardVal(currentId)) > 0 || wangList.size() == 2 && handCards.hasFourSameCard())) {
                bl1 = false;
                lists.clear();
            } else if (list.size() == 4 && map.size() == 2 && map.entrySet().iterator().next().getValue().intValue() == 2) {
                if (wangList.size() == 2) {
                    bl1 = false;
                    lists.clear();
                } else if (wangList.size() == 1) {
                    LdsPhzCardUtils.sort(list);
                    if (Math.abs(LdsPhzCardUtils.loadCardVal(list.get(0)) - LdsPhzCardUtils.loadCardVal(list.get(3))) == 100) {
                        bl1 = false;
                        lists.clear();
                    }
                }
            } else if (wangList.size() == 3 && list.size() == 5 && handCards.hasFourSameCard() && LdsPhzCardUtils.loadIdByVal(list, LdsPhzCardUtils.loadCardVal(currentId)) > 0) {
                if (LdsPhzCardUtils.hasSameValCard(list) && LdsPhzCardUtils.hasSameValCardIgnoreCase(list)) {
                    boolean bl = true;
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        List<List<Integer>> temps = LdsPhzCardUtils.loadYijuhua(list, kv.getKey(), 0, false, false);
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
            } else if (wangList.size() == 4 && list.size() == 5 && LdsPhzCardUtils.loadCardCount(list).size() <= 3) {
                bl1 = false;
                lists.clear();
            }

            if (wangList.size() == 4 && list.size() == 4 && handCards.hasFourSameCard() && LdsPhzCardUtils.loadIdByVal(list, LdsPhzCardUtils.loadCardVal(currentId)) > 0) {
                if (LdsPhzCardUtils.hasSameValCard(list)) {
                    boolean bl = true;
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        List<List<Integer>> temps = LdsPhzCardUtils.loadYijuhua(list, kv.getKey(), 0, false, false);
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
                if (list.size() == 2 && LdsPhzCardUtils.sameCard(list.get(0), list.get(1))) {
                    bl = false;
                    lists.clear();
                } else if (list.size() == 3) {
                    int idx = list.indexOf(Integer.valueOf(LdsPhzCardUtils.loadIdByVal(list, LdsPhzCardUtils.loadCardVal(currentId))));
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
                        if (LdsPhzCardUtils.sameCard(list.get(idx1), list.get(idx2))) {
                            bl = false;
                            lists.clear();
                        }
                    }
                }

                if (bl) {
//                    if(lists.size()==0){
                    Integer wangId = wangList.get(0);
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        List<List<Integer>> temps = LdsPhzCardUtils.loadYijuhua(list, kv.getKey(), wangId, true, false);
                        for (List<Integer> t : temps) {
                            lists.add(new LdsPhzCardMsg(t));
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

        Map<LdsPhzLinkCard, Integer> linkCardList = new ConcurrentHashMap<>();
        Map<LdsPhzLinkCard, String> filterMap = new ConcurrentHashMap<>();

        int bestFan;
        try {
            if (lists.size() > 0) {
                CountDownLatch countDownLatch = new CountDownLatch(lists.size());
                for (LdsPhzCardMsg tmp : lists) {
                    List<Integer> temp = new ArrayList<>(list);
                    temp.removeAll(tmp.getIds());
//                rests.add(tmp);
                    LdsPhzLinkCard linkCard = new LdsPhzLinkCard();
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
                LdsPhzLinkCard linkCard = new LdsPhzLinkCard();
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
            LdsPhzLinkCard best = null;
            int currentFans, bestFans = 1;
            LdsPhzCardResult bestResult = null;

            for (Map.Entry<LdsPhzLinkCard, Integer> lf : linkCardList.entrySet()) {

                if (lf.getValue().intValue() < bestFan) {
                    continue;
                }

                LdsPhzLinkCard linkCard = lf.getKey();
                if (linkCard.getHuxi() >= phzBase.loadQihuxi()) {
                    LdsPhzCardResult tempCardResult = new LdsPhzCardResult();
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
                            Map<Integer, Integer> map1 = LdsPhzCardUtils.loadCardCount(srcs);
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
                                linkCard.setHuCard(LdsPhzCardUtils.loadIdByVal(srcs, maxEntry.getKey()));
                            }
                        }

                        if (phzBase.loadXingMode() == 0) {
                            xingCard = linkCard.getHuCard();
                        }
                    }

                    int tun1 = phzBase.loadBaseTun();
                    int tun2 = phzBase.loadXingTun(tempCardResult.loadValCount(LdsPhzCardUtils.loadCardVal(xingCard)));
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
                        if (best.getFans().contains(LdsPhzFanEnums.WANG_ZHA_WANG)) {
                            if (!linkCard.getFans().contains(LdsPhzFanEnums.WANG_ZHA_WANG)) {
                                continue;
                            }
                            bl = true;
                        } else if (linkCard.getFans().contains(LdsPhzFanEnums.WANG_ZHA_WANG)) {
                            bl = true;
                        } else if (best.getFans().contains(LdsPhzFanEnums.WANG_ZHA) || best.getFans().contains(LdsPhzFanEnums.WANG_CHUANG_WANG)) {
                            if (!(linkCard.getFans().contains(LdsPhzFanEnums.WANG_ZHA) || linkCard.getFans().contains(LdsPhzFanEnums.WANG_CHUANG_WANG))) {
                                continue;
                            }
                            bl = true;
                        } else if (linkCard.getFans().contains(LdsPhzFanEnums.WANG_ZHA) || linkCard.getFans().contains(LdsPhzFanEnums.WANG_CHUANG_WANG)) {
                            bl = true;
                        } else if (best.getFans().contains(LdsPhzFanEnums.WANG_CHUANG) || best.getFans().contains(LdsPhzFanEnums.WANG_DIAO_WANG)) {
                            if (!(linkCard.getFans().contains(LdsPhzFanEnums.WANG_CHUANG) || linkCard.getFans().contains(LdsPhzFanEnums.WANG_DIAO_WANG))) {
                                continue;
                            }
                            bl = true;
                        } else if (linkCard.getFans().contains(LdsPhzFanEnums.WANG_CHUANG) || linkCard.getFans().contains(LdsPhzFanEnums.WANG_DIAO_WANG)) {
                            bl = true;
                        } else if (best.getFans().contains(LdsPhzFanEnums.WANG_DIAO)) {
                            if (!linkCard.getFans().contains(LdsPhzFanEnums.WANG_DIAO)) {
                                continue;
                            }
                            bl = true;
                        } else if (linkCard.getFans().contains(LdsPhzFanEnums.WANG_DIAO)) {
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
                for (LdsPhzFanEnums fan : cardResult.getFans()) {
                    if (fan.name().startsWith("WANG")) {
                        bl = false;
                        cardResult.setSelf(true);
                        break;
                    }
                }
                if (bl) {
                    cardResult.addFan(LdsPhzFanEnums.SELF);
                    cardResult.setSelf(true);
                }
            }

            int totalFan = 1;
            for (LdsPhzFanEnums fan : cardResult.getFans()) {
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
            LdsPhzCardResult tempCardResult = new LdsPhzCardResult();
            tempCardResult.setCurId(currentId);
            tempCardResult.setPhzBase(phzBase);
            tempCardResult.setRedCount(cardResult.getRedCount());
            tempCardResult.setSelf(isSelf);
            tempCardResult.setHandCards(mHandCards.copy());
            tempCardResult.addAll(cardResult.copyCardMessageList());
            LdsPhzCardMessage cm1 = null, cm2 = null;
            for (LdsPhzCardMessage cm : tempCardResult.getCardMessageList()) {
                if (cm.getHuXiEnum() == LdsPhzHuXiEnums.TI && LdsPhzCardUtils.countCard(cm.getCards(), 201) >= 3 && LdsPhzCardUtils.hasCard(cm.getCards(), LdsPhzCardUtils.loadCardVal(currentId))) {
                    cm1 = cm;
                } else if (cm.getHuXiEnum() == LdsPhzHuXiEnums.DUI) {
                    cm2 = cm;
                }
            }

            if (cm1 != null && cm2 != null) {
                Integer card = LdsPhzCardUtils.loadIdByVal(cm1.getCards(), 201);
                cm1.getCards().remove(card);
                cm1.init(LdsPhzHuXiEnums.WEI, new ArrayList<>(cm1.getCards()));
                cm2.getCards().add(card);
                cm2.init(LdsPhzHuXiEnums.KAN, new ArrayList<>(cm2.getCards()));

                if (tempCardResult.getTotalHuxi() >= phzBase.loadQihuxi()) {
                    int totalFan = 1;
                    tempCardResult.getFans().add(LdsPhzCardUtils.wangCard(currentId) ? LdsPhzFanEnums.WANG_CHUANG_WANG : LdsPhzFanEnums.WANG_CHUANG);
                    for (LdsPhzFanEnums fan : tempCardResult.getFans()) {
                        totalFan *= fan.getFan();
                    }

                    int tun1 = phzBase.loadBaseTun();
                    int tun2 = phzBase.loadXingTun(tempCardResult.loadValCount(LdsPhzCardUtils.loadCardVal(xingCard)));
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
                    LdsPhzCardResult tempCardResult1 = new LdsPhzCardResult();
                    tempCardResult1.setCurId(currentId);
                    tempCardResult1.setPhzBase(phzBase);
                    tempCardResult1.setRedCount(cardResult.getRedCount());
                    tempCardResult1.setSelf(isSelf);
                    tempCardResult1.setHandCards(mHandCards.copy());
                    tempCardResult1.addAll(cardResult.getChuangCardResult().copyCardMessageList());
                    cm1 = null;
                    cm2 = null;
                    for (LdsPhzCardMessage cm : tempCardResult1.getCardMessageList()) {
                        if (cm.getHuXiEnum() == LdsPhzHuXiEnums.WEI && LdsPhzCardUtils.countCard(cm.getCards(), 201) >= 2 && LdsPhzCardUtils.hasCard(cm.getCards(), LdsPhzCardUtils.loadCardVal(currentId))) {
                            cm1 = cm;
                        } else if (cm.getHuXiEnum() == LdsPhzHuXiEnums.DUI) {
                            cm2 = cm;
                        } else {
                            if (cm2 == null || cm2.getHuXiEnum() != LdsPhzHuXiEnums.DUI) {
                                if (cm.getHuXiEnum() == LdsPhzHuXiEnums.WEI || cm.getHuXiEnum() == LdsPhzHuXiEnums.KAN) {
                                    if (cm2 == null) {
                                        cm2 = cm;
                                    } else {
                                        int val = LdsPhzCardUtils.loadCardVal(cm2.getCards().get(0));
                                        if (val > 100) {
                                            if (LdsPhzCardUtils.redCardByVal(val)) {
                                            } else {
                                                cm2 = cm;
                                            }
                                        } else {
                                            if (LdsPhzCardUtils.redCardByVal(val)) {
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
                        card = LdsPhzCardUtils.loadIdByVal(cm1.getCards(), 201);
                        cm1.getCards().remove(card);
                        cm1.init(LdsPhzHuXiEnums.DUI, new ArrayList<>(cm1.getCards()));
                        cm2.getCards().add(card);
                        cm2.init(cm2.getHuXiEnum() == LdsPhzHuXiEnums.DUI ? LdsPhzHuXiEnums.KAN : LdsPhzHuXiEnums.TI, new ArrayList<>(cm2.getCards()));

                        if (tempCardResult1.getTotalHuxi() >= phzBase.loadQihuxi()) {
                            int totalFan = 1;
                            tempCardResult1.getFans().add(LdsPhzCardUtils.wangCard(currentId) ? LdsPhzFanEnums.WANG_DIAO_WANG : LdsPhzFanEnums.WANG_DIAO);
                            for (LdsPhzFanEnums fan : tempCardResult1.getFans()) {
                                totalFan *= fan.getFan();
                            }

                            int tun1 = phzBase.loadBaseTun();
                            int tun2 = phzBase.loadXingTun(tempCardResult1.loadValCount(LdsPhzCardUtils.loadCardVal(xingCard)));
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
                    card = LdsPhzCardUtils.loadIdByVal(cm1.getCards(), 201);
                    cm1.getCards().remove(card);
                    cm1.init(LdsPhzHuXiEnums.DUI, new ArrayList<>(cm1.getCards()));
                    cm2.getCards().add(card);
                    cm2.init(LdsPhzHuXiEnums.TI, new ArrayList<>(cm2.getCards()));

                    if (tempCardResult.getTotalHuxi() >= phzBase.loadQihuxi()) {
                        int totalFan = 1;
                        tempCardResult.getFans().add(LdsPhzCardUtils.wangCard(currentId) ? LdsPhzFanEnums.WANG_DIAO_WANG : LdsPhzFanEnums.WANG_DIAO);
                        for (LdsPhzFanEnums fan : tempCardResult.getFans()) {
                            totalFan *= fan.getFan();
                        }

                        int tun1 = phzBase.loadBaseTun();
                        int tun2 = phzBase.loadXingTun(tempCardResult.loadValCount(LdsPhzCardUtils.loadCardVal(xingCard)));
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
            LdsPhzCardResult tempCardResult = new LdsPhzCardResult();
            tempCardResult.setCurId(currentId);
            tempCardResult.setPhzBase(phzBase);
            tempCardResult.setRedCount(cardResult.getRedCount());
            tempCardResult.setSelf(isSelf);
            tempCardResult.setHandCards(mHandCards.copy());
            tempCardResult.addAll(cardResult.copyCardMessageList());
            LdsPhzCardMessage cm1 = null, cm2 = null;
            for (LdsPhzCardMessage cm : tempCardResult.getCardMessageList()) {
                if (cm.getHuXiEnum() == LdsPhzHuXiEnums.WEI && LdsPhzCardUtils.countCard(cm.getCards(), 201) >= 2 && LdsPhzCardUtils.hasCard(cm.getCards(), LdsPhzCardUtils.loadCardVal(currentId))) {
                    cm1 = cm;
                } else if (cm.getHuXiEnum() == LdsPhzHuXiEnums.DUI) {
                    cm2 = cm;
                } else {
                    if (cm2 == null || cm2.getHuXiEnum() != LdsPhzHuXiEnums.DUI) {
                    	  if (cm.getHuXiEnum() == LdsPhzHuXiEnums.WEI || cm.getHuXiEnum() == LdsPhzHuXiEnums.KAN) {
                              if (cm2 == null) {
                                  cm2 = cm;
                              } else {
                                  int val = LdsPhzCardUtils.loadCardVal(cm.getCards().get(0));
                                  
                                  if(phzBase.loadXingMode()==1) {
                                  	int xingCardVal = LdsPhzCardUtils.loadCardVal(xingCard);
                                  	if(xingCardVal == val ) {
                                  		 cm2 = cm;
                                  	}
                                  }else {
                                  	if (LdsPhzCardUtils.redCardByVal(val)) {
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
                Integer card = LdsPhzCardUtils.loadIdByVal(cm1.getCards(), 201);
                cm1.getCards().remove(card);
                cm1.init(LdsPhzHuXiEnums.DUI, new ArrayList<>(cm1.getCards()));
                cm2.getCards().add(card);
                cm2.init(cm2.getHuXiEnum() == LdsPhzHuXiEnums.DUI ? LdsPhzHuXiEnums.KAN : LdsPhzHuXiEnums.TI, new ArrayList<>(cm2.getCards()));

                if (tempCardResult.getTotalHuxi() >= phzBase.loadQihuxi()) {
                    int totalFan = 1;
                    tempCardResult.getFans().add(LdsPhzCardUtils.wangCard(currentId) ? LdsPhzFanEnums.WANG_DIAO_WANG : LdsPhzFanEnums.WANG_DIAO);
                    for (LdsPhzFanEnums fan : tempCardResult.getFans()) {
                        totalFan *= fan.getFan();
                    }

                    int tun1 = phzBase.loadBaseTun();
                    int tun2 = phzBase.loadXingTun(tempCardResult.loadValCount(LdsPhzCardUtils.loadCardVal(xingCard)));
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

        LdsPhzCardResult chuang = cardResult.getChuangCardResult();
        if (chuang != null && chuang != cardResult) {
            chuang.setHuCard(cardResult.getHuCard());
            chuang.setXingCard(cardResult.getXingCard());
        }

        LdsPhzCardResult diao = cardResult.getDiaoCardResult();
        if (diao != null && diao != cardResult) {
            diao.setHuCard(cardResult.getHuCard());
            diao.setXingCard(cardResult.getXingCard());
        }

        return cardResult;
    }

    private static void remove(final List<Integer> list, final LdsPhzLinkCard linkCard, final Map<LdsPhzLinkCard, Integer> linkCardList, final List<Integer> wangs, final int currentId, final LdsPhzBase phzBase, final LdsPhzCardResult cardResult, final CountDownLatch countDownLatch, final Map<LdsPhzLinkCard, String> filterMap, final String uuid) {
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

    private static void remove(List<Integer> list, LdsPhzLinkCard linkCard, Map<LdsPhzLinkCard, Integer> linkCardList, List<Integer> wangs, int currentId, LdsPhzBase phzBase, LdsPhzCardResult cardResult, Map<LdsPhzLinkCard, String> filterMap, final String uuid) {
        Map<Integer, Integer> map = LdsPhzCardUtils.loadCardCount(list);
        Set<LdsPhzCardMsg> lists = new HashSet<>();
        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
            List<List<Integer>> temps = LdsPhzCardUtils.loadYijuhua(list, kv.getKey(), 0, true, false);
            for (List<Integer> t : temps) {
                lists.add(new LdsPhzCardMsg(t));
            }
        }
        linkCard.setRest(list);
        boolean hasFourSameCard = cardResult.getHandCards().hasFourSameCard();

        if (wangs.size() > 0 && list.size() > 1) {
            boolean bl1 = true;
            if (wangs.size() >= 3 && list.size() == 2) {
                bl1 = false;
                lists.clear();
            } else if (list.size() == 3 && map.size() == 2 && (wangs.size() == 3 && LdsPhzCardUtils.loadIdByVal(list, LdsPhzCardUtils.loadCardVal(currentId)) > 0 || wangs.size() == 2 && hasFourSameCard)) {
                bl1 = false;
                lists.clear();
            } else if (list.size() == 4 && map.size() == 2 && map.entrySet().iterator().next().getValue().intValue() == 2) {
                if (wangs.size() == 2) {
                    bl1 = false;
                    lists.clear();
                } else if (wangs.size() == 1) {
                    LdsPhzCardUtils.sort(list);
                    if (Math.abs(LdsPhzCardUtils.loadCardVal(list.get(0)) - LdsPhzCardUtils.loadCardVal(list.get(3))) == 100) {
                        bl1 = false;
                        lists.clear();
                    }
                }
            } else if (wangs.size() == 3 && hasFourSameCard && list.size() == 5 && LdsPhzCardUtils.loadIdByVal(list, LdsPhzCardUtils.loadCardVal(currentId)) > 0) {
                if (LdsPhzCardUtils.hasSameValCard(list) && LdsPhzCardUtils.hasSameValCardIgnoreCase(list)) {
                    boolean bl = true;
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        List<List<Integer>> temps = LdsPhzCardUtils.loadYijuhua(list, kv.getKey(), 0, false, false);
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
            } else if (wangs.size() == 4 && list.size() == 5 && LdsPhzCardUtils.loadCardCount(list).size() <= 3) {
                bl1 = false;
                lists.clear();
            }

            if (hasFourSameCard && wangs.size() == 4 && list.size() == 4 && LdsPhzCardUtils.loadIdByVal(list, LdsPhzCardUtils.loadCardVal(currentId)) > 0) {
                if (LdsPhzCardUtils.hasSameValCard(list)) {
                    boolean bl = true;
                    for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                        List<List<Integer>> temps = LdsPhzCardUtils.loadYijuhua(list, kv.getKey(), 0, false, false);
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
                if (list.size() == 2 && LdsPhzCardUtils.sameCard(list.get(0), list.get(1))) {
                    bl = false;
                } else if (list.size() == 3) {
                    int idx = list.indexOf(Integer.valueOf(LdsPhzCardUtils.loadIdByVal(list, LdsPhzCardUtils.loadCardVal(currentId))));
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
                        if (LdsPhzCardUtils.sameCard(list.get(idx1), list.get(idx2))) {
                            bl = false;
                        }
                    }
                }

                if (bl) {
                    if (wangs.size() > 0) {
                        int wangId = wangs.get(0);
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            List<List<Integer>> temps = LdsPhzCardUtils.loadYijuhua(list, kv.getKey(), wangId, true, false);
                            for (List<Integer> t : temps) {
                                lists.add(new LdsPhzCardMsg(t));
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
            for (LdsPhzCardMsg tmp : lists) {
                List<Integer> temp = new ArrayList<>(list);
                temp.removeAll(tmp.getIds());
                LdsPhzLinkCard linkCard1 = new LdsPhzLinkCard();
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

    private static void canHuLinkCard(Map<LdsPhzLinkCard, Integer> linkCardList, LdsPhzLinkCard linkCard0, LdsPhzCardResult cardResult, final LdsPhzBase phzBase, final String uuid) {
        List<Integer> list = linkCard0.getRest();

        if (list != null && list.size() >= 6) {
            return;
        }

        List<List<Integer>> yjhLists = linkCard0.loadList(false);
        LdsPhzHandCards handCards = cardResult.getHandCards().copy();

        List<Integer> wangList = new ArrayList<>(handCards.WANGS);
        if (wangList.size() > 0) {
            for (List<Integer> temp : yjhLists) {
                wangList.removeAll(temp);
            }
        }

        boolean isTianHu = phzBase.isTianHu();
        linkCard0.setHuCard(cardResult.getCurId());

        int val = LdsPhzCardUtils.loadCardVal(cardResult.getCurId());
        List<LdsPhzCardMessage> cardMessageList;

        LdsPhzLinkCard linkCard1 = new LdsPhzLinkCard();
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
                            if (LdsPhzCardUtils.wangCard(cardResult.getCurId())) {
                                cardMessageList = new ArrayList<>(8);
                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, yjhLists, cardMessageList);
                                cardMessageList.add(new LdsPhzCardMessage(LdsPhzHuXiEnums.DUI, LdsPhzCardUtils.asList(list.get(0), wangList.get(0))));
                                linkCard1.setYjhList(yjhLists);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                            } else {
                                if (LdsPhzCardUtils.sameCard(list.get(0), cardResult.getCurId()) || isTianHu) {

                                    int wangHu = 1;

                                    List<Integer> list1 = null;
                                    for (List<Integer> temp : yjhLists) {
                                        if (temp.size() == 3) {
                                            if (LdsPhzCardUtils.hasCard(temp, 201) && (LdsPhzCardUtils.wangCard(temp.get(1)) || LdsPhzCardUtils.sameCard(temp.get(0), temp.get(1)))) {
                                                list1 = temp;
                                                wangHu = 2;
                                                break;
                                            }
                                        }
                                    }
                                    if (wangHu == 2) {
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                        Integer card = LdsPhzCardUtils.loadIdByVal(list1, 201);
                                        list1.remove(card);
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(0));
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, yjhLists, cardMessageList);
                                        cardMessageList.add(new LdsPhzCardMessage(LdsPhzHuXiEnums.WEI, LdsPhzCardUtils.asList(list.get(0), card, wangList.get(0))));
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, list1));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), card));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);

                                            if (isTianHu) {
                                                linkCard1.setHuCard(list.get(0));
                                            }

                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0)));
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                    } else {
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, yjhLists, cardMessageList);
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(0));
                                        }
                                        cardMessageList.add(new LdsPhzCardMessage(LdsPhzHuXiEnums.DUI, LdsPhzCardUtils.asList(list.get(0), wangList.get(0))));
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                } else {
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, yjhLists, cardMessageList);
                                    cardMessageList.add(new LdsPhzCardMessage(LdsPhzHuXiEnums.DUI, LdsPhzCardUtils.asList(list.get(0), wangList.get(0))));
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    int xingVal = LdsPhzCardUtils.loadXingVal(LdsPhzCardUtils.loadCardVal(cardResult.getXingCard()), phzBase.loadXingMode());
                                    if (LdsPhzCardUtils.loadCardVal(list.get(0)) != xingVal && LdsPhzCardUtils.commonCardByVal(xingVal)) {
                                        List<Integer> tempList = new ArrayList<>(2);
                                        tempList.add(list.get(0));
                                        int idx = -1;
                                        Integer tempCard = -1;
                                        List<List<Integer>> yjhs = null;
                                        for (int i = 0, len = yjhLists.size(); i < len; i++) {
                                            List<Integer> tmpList = yjhLists.get(i);
                                            if (tmpList.size() == 3) {
                                                int v1 = LdsPhzCardUtils.loadCardVal(tmpList.get(0));
                                                int v2 = LdsPhzCardUtils.loadCardVal(tmpList.get(1));
                                                int v3 = LdsPhzCardUtils.loadCardVal(tmpList.get(2));
                                                if (v1 < LdsPhzCardUtils.WANGPAI_VAL && v2 < 200 && v3 < 200) {
                                                    if (v1 == v2) {
                                                        if (v1 != v3) {
                                                            if (tempList.size() == 2) {
                                                                tempList.clear();
                                                                tempList.add(list.get(0));
                                                            }

                                                            tempList.add(tmpList.get(2));
                                                            yjhs = LdsPhzCardUtils.loadYijuhua(tempList, v3, wangList.get(0), true, true, false);
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
                                                            yjhs = LdsPhzCardUtils.loadYijuhua(tempList, v2, wangList.get(0), true, true, false);
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
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            linkCard1 = new LdsPhzLinkCard();
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
                            if (LdsPhzCardUtils.wangCard(cardResult.getCurId())) {
                                cardMessageList = new ArrayList<>(8);
                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, yjhLists, cardMessageList);
                                cardMessageList.add(new LdsPhzCardMessage(LdsPhzHuXiEnums.WEI, LdsPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1))));
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
//                            System.out.println("2 wang hu");

                                for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                    LdsPhzHandCards handCards1 = handCards.copy();
                                    List<Integer> list1 = handCards1.KAN.remove(kv.getKey());
                                    list1.add(wangList.get(0));

                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list1);
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                                for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                    LdsPhzHandCards handCards1 = handCards.copy();
                                    List<Integer> list1 = handCards1.WEI.remove(kv.getKey());
                                    list1.add(wangList.get(0));

                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list1);
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                                for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                    LdsPhzHandCards handCards1 = handCards.copy();
                                    List<Integer> list1 = handCards1.WEI_CHOU.remove(kv.getKey());
                                    list1.add(wangList.get(0));

                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list1);
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }

//                                for (Map.Entry<Integer,List<Integer>> kv:handCards.PENG.entrySet()){
//                                    LdsPhzHandCards handCards1=handCards.copy();
//                                    List<Integer> list1=handCards1.PENG.remove(kv.getKey());
//                                    list1.add(wangList.get(0));
//
//                                    cardMessageList = new ArrayList<>(8);
//                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
//                                    linkCard1=new LdsPhzLinkCard();
//                                    linkCard1.setYjhList(copy(yjhLists,null));
////                                    linkCard1.getYjhList().add(list1);
//                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(1)));
//                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
//                                    addCardMessage(cardResult,list1,cardMessageList);
//                                    linkCard1.getYjhList().add(list1);
//                                    check(cardMessageList, linkCard1, phzBase, linkCardList,uuid);
//                                }
                            } else {
                                boolean tempBl = true;
//                                System.out.println("2 wang common hu");
                                if (LdsPhzCardUtils.sameCard(list.get(0), cardResult.getCurId()) || isTianHu) {
                                    linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                    tempBl = false;
                                }
                                if (isTianHu) {
                                    linkCard1.setHuCard(list.get(0));
                                }
                                cardMessageList = new ArrayList<>(8);
                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, yjhLists, cardMessageList);
                                cardMessageList.add(new LdsPhzCardMessage(LdsPhzHuXiEnums.WEI, LdsPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1))));

//                            System.out.println("2 wang hu");

                                if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid) || tempBl) {
                                    for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                        LdsPhzHandCards handCards1 = handCards.copy();
                                        List<Integer> list1 = handCards1.KAN.remove(kv.getKey());
                                        list1.add(wangList.get(0));

                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list1);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        if (LdsPhzCardUtils.sameCard(list.get(0), cardResult.getCurId()) || isTianHu) {
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                        }
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(0));
                                        }
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                    for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                        LdsPhzHandCards handCards1 = handCards.copy();
                                        List<Integer> list1 = handCards1.WEI.remove(kv.getKey());
                                        list1.add(wangList.get(0));

                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list1);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (LdsPhzCardUtils.sameCard(list.get(0), cardResult.getCurId()) || isTianHu) {
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                        }
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(0));
                                        }
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                    for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                        LdsPhzHandCards handCards1 = handCards.copy();
                                        List<Integer> list1 = handCards1.WEI_CHOU.remove(kv.getKey());
                                        list1.add(wangList.get(0));

                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list1);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (LdsPhzCardUtils.sameCard(list.get(0), cardResult.getCurId()) || isTianHu) {
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                        }
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(0));
                                        }
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }

//                                    for (Map.Entry<Integer,List<Integer>> kv:handCards.PENG.entrySet()){
//                                        LdsPhzHandCards handCards1=handCards.copy();
//                                        List<Integer> list1=handCards1.PENG.remove(kv.getKey());
//                                        list1.add(wangList.get(0));
//
//                                        cardMessageList = new ArrayList<>(8);
//                                        LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
//                                        linkCard1=new LdsPhzLinkCard();
//                                        linkCard1.setYjhList(copy(yjhLists,null));
////                                    linkCard1.getYjhList().add(list1);
//                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(1)));
//                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
//                                        addCardMessage(cardResult,list1,cardMessageList);
//                                        linkCard1.getYjhList().add(list1);
//                                        if (LdsPhzCardUtils.sameCard(list.get(0), cardResult.getCurId())){
//                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
//                                        }
//                                        check(cardMessageList, linkCard1, phzBase, linkCardList,uuid);
//                                    }
                                }
                            }
                            break;
                        case 4:
                            if (LdsPhzCardUtils.wangCard(cardResult.getCurId()) || isTianHu) {
//                            System.out.println("wang zha wang hu");
                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG_WANG);
                                yjhLists.add(LdsPhzCardUtils.asList(wangList.get(1), wangList.get(2), wangList.get(3)));
                                yjhLists.add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0)));
                                cardMessageList = new ArrayList<>(8);
                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, yjhLists, cardMessageList);
                                if (isTianHu) {
                                    linkCard1.setHuCard(wangList.get(1));
                                }
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                            } else {
//                            System.out.println("wang zha hu");
                                if (LdsPhzCardUtils.sameCard(list.get(0), cardResult.getCurId())) {
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(wangList.get(2), wangList.get(3)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(wangList.get(2), wangList.get(3), wangList.get(1)));
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
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(cardResult.getCurId(), wangList.get(2), wangList.get(3)));
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(cardResult.getCurId(), wangList.get(3)));
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
                            if (LdsPhzCardUtils.sameCard(list.get(0), list.get(1))) {
                                yjhLists.add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));
                                linkCard1.setYjhList(yjhLists);
                                cardMessageList = new ArrayList<>(8);
                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, yjhLists, cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                            }
                            break;
                        case 1:
                            if (LdsPhzCardUtils.sameCard(list.get(0), list.get(1))) {
                                cardMessageList = new ArrayList<>(8);
                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                linkCard1 = new LdsPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(0)));
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                if (!handCards.hasFourSameCard()) {
                                    for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                        LdsPhzHandCards handCards1 = handCards.copy();
                                        List<Integer> list1 = handCards1.KAN.remove(kv.getKey());
                                        list1.add(wangList.get(0));

                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list1);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                    for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                        LdsPhzHandCards handCards1 = handCards.copy();
                                        List<Integer> list1 = handCards1.WEI.remove(kv.getKey());
                                        list1.add(wangList.get(0));

                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list1);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                    for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                        LdsPhzHandCards handCards1 = handCards.copy();
                                        List<Integer> list1 = handCards1.WEI_CHOU.remove(kv.getKey());
                                        list1.add(wangList.get(0));

                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list1);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }

//                                    for (Map.Entry<Integer,List<Integer>> kv:handCards.PENG.entrySet()){
//                                        LdsPhzHandCards handCards1=handCards.copy();
//                                        List<Integer> list1=handCards1.PENG.remove(kv.getKey());
//                                        list1.add(wangList.get(0));
//
//                                        cardMessageList = new ArrayList<>(8);
//                                        LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
//                                        linkCard1=new LdsPhzLinkCard();
//                                        linkCard1.setYjhList(copy(yjhLists,null));
////                                    linkCard1.getYjhList().add(list1);
//                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));
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
                            if (LdsPhzCardUtils.sameCard(list.get(0), list.get(1))) {
                                if (LdsPhzCardUtils.wangCard(cardResult.getCurId()) || isTianHu) {
//                            System.out.println("wang chuang wang hu");
                                    linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG_WANG);
                                    if (isTianHu) {
                                        linkCard1.setHuCard(wangList.get(0));
                                    }
                                    yjhLists.add(LdsPhzCardUtils.asList(wangList.get(0), wangList.get(1), wangList.get(2)));
                                    yjhLists.add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));

                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, yjhLists, cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                } else if (LdsPhzCardUtils.commonCard(cardResult.getCurId())) {
                                    if (LdsPhzCardUtils.hasCard(list, val)) {
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                        yjhLists.add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                        yjhLists.add(LdsPhzCardUtils.asList(list.get(1), wangList.get(2)));

                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, yjhLists, cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    } else {
                                        for (List<Integer> yjh : yjhLists) {
                                            int card0 = LdsPhzCardUtils.loadIdByVal(yjh, val);
                                            if (card0 > 0) {
                                                yjh.remove(Integer.valueOf(card0));
                                                yjh.add(wangList.get(0));

                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                linkCard1.setYjhList(copy(yjhLists, null));

                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(card0, wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                                    linkCard1 = new LdsPhzLinkCard();
                                                    linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                    linkCard1.setYjhList(copy(yjhLists, null));

                                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(card0, wangList.get(1)));
                                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(2)));

                                                    cardMessageList = new ArrayList<>(8);
                                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
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
                                    List<List<Integer>> tempYjh = LdsPhzCardUtils.loadYijuhua(list, LdsPhzCardUtils.loadCardVal(list.get(0)), wangList.get(0), true, false, false);
                                    if (tempYjh != null && tempYjh.size() == 1) {
                                        yjhLists.add(tempYjh.get(0));
                                        yjhLists.add(LdsPhzCardUtils.asList(wangList.get(1), wangList.get(2)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO_WANG);

                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, yjhLists, cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    } else {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), wangList.get(1), wangList.get(2)));

                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), wangList.get(2)));

                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                } else if (LdsPhzCardUtils.hasCard(list, val)) {
                                    Integer card = LdsPhzCardUtils.loadIdByVal(list, val);
                                    list.remove(card);

                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(card, wangList.get(1), wangList.get(2)));

                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(card, wangList.get(2)));

                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                } else {
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), wangList.get(1), wangList.get(2)));
                                    boolean tempBl = true;
                                    if (isTianHu) {
                                        linkCard1.setHuCard(list.get(1));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                        tempBl = false;
                                    }
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid) || tempBl) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), wangList.get(2)));
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(1));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        if (LdsPhzCardUtils.commonCardByVal(val) && yjhLists.size() > 0) {
                                            List<List<Integer>> yjhs = LdsPhzCardUtils.loadYijuhua(LdsPhzCardUtils.asList(list.get(0), list.get(1), cardResult.getCurId()), val, 0, true, true, false);
                                            if (yjhs.size() == 1) {
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                for (List<Integer> list1 : linkCard1.getYjhList()) {
                                                    int idx = list1.indexOf(cardResult.getCurId());
                                                    if (idx >= 0) {
                                                        list1.remove(idx);
                                                        list1.add(wangList.get(0));
                                                        break;
                                                    }
                                                }
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(cardResult.getCurId(), wangList.get(2)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    }
                                }
                            }

                            break;
                        case 4:
                            if (LdsPhzCardUtils.sameCard(list.get(0), list.get(1))) {
                                if (LdsPhzCardUtils.wangCard(cardResult.getCurId()) || isTianHu) {
//                            System.out.println("wang zha wang hu");
                                    linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA_WANG);
                                    if (isTianHu) {
                                        linkCard1.setHuCard(wangList.get(0));
                                    }
                                    yjhLists.add(LdsPhzCardUtils.asList(wangList.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                    yjhLists.add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));
                                    linkCard1.setYjhList(yjhLists);

                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                } else {
                                    int cur = LdsPhzCardUtils.loadIdByVal(list, val);
                                    if (cur > 0) {
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                        yjhLists.add(LdsPhzCardUtils.asList(list.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        yjhLists.add(LdsPhzCardUtils.asList(list.get(1), wangList.get(0)));
                                        linkCard1.setYjhList(yjhLists);

                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    } else {
                                        for (List<Integer> yjh : yjhLists) {
                                            int card0 = LdsPhzCardUtils.loadIdByVal(yjh, val);
                                            if (card0 > 0) {
                                                yjh.remove(Integer.valueOf(card0));
                                                yjh.add(wangList.get(0));
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(card0, wangList.get(1), wangList.get(2), wangList.get(3)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                                    linkCard1 = new LdsPhzLinkCard();
                                                    linkCard1.setYjhList(copy(yjhLists, null));
                                                    linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(card0, wangList.get(3)));
                                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(1), wangList.get(2)));

                                                    cardMessageList = new ArrayList<>(8);
                                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
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
                                int cur = LdsPhzCardUtils.loadIdByVal(list, val);
                                if (cur > 0 || isTianHu) {
                                    list.remove(Integer.valueOf(cur));
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                    if (isTianHu) {
                                        cur = list.remove(0);
                                        linkCard1.setHuCard(cur);
                                    }
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(cur, wangList.get(0), wangList.get(1), wangList.get(2)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(3)));

                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                        if (isTianHu) {
                                            linkCard1.setHuCard(cur);
                                        }
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(cur, wangList.get(3)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1), wangList.get(2)));

                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                } else {
                                    List<List<Integer>> tempYjh = LdsPhzCardUtils.loadYijuhua(list, LdsPhzCardUtils.loadCardVal(list.get(0)), wangList.get(0), true, false, false);
                                    if (tempYjh != null && tempYjh.size() == 1) {
                                        for (List<Integer> yjh : yjhLists) {
                                            int card0 = LdsPhzCardUtils.loadIdByVal(yjh, val);
                                            if (card0 > 0) {
                                                yjh.remove(Integer.valueOf(card0));
                                                yjh.add(wangList.get(1));
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(card0, wangList.get(2), wangList.get(3)));
                                                linkCard1.getYjhList().add(tempYjh.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
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
                    int idx = list.indexOf(Integer.valueOf(LdsPhzCardUtils.loadIdByVal(list, val)));
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
                        if (LdsPhzCardUtils.sameCard(list.get(idx1), list.get(idx2))) {
                            switch (wangList.size()) {
                                case 1:
                                    break;
                                case 2:
//                                System.out.println("wang chuang hu");
                                    linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(cardResult.getCurId(), wangList.get(0), wangList.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(idx1), list.get(idx2)));

                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(cardResult.getCurId(), wangList.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(idx1), list.get(idx2), wangList.get(1)));

                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                    break;
                                case 3:
//                                System.out.println("wang zha hu");
                                    linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);

                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(cardResult.getCurId(), wangList.get(0), wangList.get(1), wangList.get(2)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(idx1), list.get(idx2)));
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(cardResult.getCurId(), wangList.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(idx1), list.get(idx2), wangList.get(1), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                    break;
                            }
                        } else if (wangList.size() >= 2) {
                            List<Integer> list1 = LdsPhzCardUtils.loadIdsByVal(list, val);
                            if (list1.size() == 2) {
                                list.removeAll(list1);
                                list.add(list1.get(0));
                                List<List<Integer>> yjhs = LdsPhzCardUtils.loadYijuhua(list, val, wangList.get(0), true, false, false);
                                if (yjhs != null && yjhs.size() == 1) {
                                    if (wangList.size() == 2) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(yjhs.get(0));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(1), wangList.get(1)));
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                    } else if (wangList.size() == 3) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(yjhs.get(0));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(1), wangList.get(1), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(2)));
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1), wangList.get(2)));
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                    }
                                } else {
//                                    list1.add(wangList.get(0));
//                                    yjhLists.add(list1);
                                    if (wangList.size() == 2) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    } else if (wangList.size() == 3) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                }
                                break;
                            }
                        }
                    } else if (LdsPhzCardUtils.wangCardByVal(val) || isTianHu) {
                        if (LdsPhzCardUtils.sameCard(list.get(0), list.get(1))) {
                            int val0 = LdsPhzCardUtils.loadCardVal(list.get(2)) - LdsPhzCardUtils.loadCardVal(list.get(0));
                            if (val0 == 100 || val0 == 0) {
                                switch (wangList.size()) {
                                    case 2:
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), list.get(2)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(wangList.get(0), wangList.get(1)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO_WANG);
                                        if (isTianHu) {
                                            linkCard1.setHuCard(wangList.get(0));
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        if (val0 == 100) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));

                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2), wangList.get(0), wangList.get(1)));
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list.get(2));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));

                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(0)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2), wangList.get(1)));
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list.get(0));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }

                                        break;
                                    case 3:
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), list.get(2)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(wangList.get(0), wangList.get(1), wangList.get(2)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG_WANG);
                                        if (isTianHu) {
                                            linkCard1.setHuCard(wangList.get(0));
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        if ((!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid) || isTianHu) && val0 == 100) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));

                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2), wangList.get(2)));
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list.get(2));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));

                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2), wangList.get(0), wangList.get(1), wangList.get(2)));
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list.get(2));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                        break;
                                }
                            } else {
                                switch (wangList.size()) {
                                    case 2:
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2), wangList.get(1)));
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(2));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2), wangList.get(0), wangList.get(1)));
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(2));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        break;
                                    case 3:
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2), wangList.get(2)));
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(2));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2), wangList.get(0), wangList.get(1), wangList.get(2)));
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(2));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        break;
                                }
                            }
                        } else if (LdsPhzCardUtils.sameCard(list.get(1), list.get(2))) {
                            int val0 = LdsPhzCardUtils.loadCardVal(list.get(2)) - LdsPhzCardUtils.loadCardVal(list.get(0));
                            if (val0 == 100 || val0 == 0) {
                                switch (wangList.size()) {
                                    case 2:
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), list.get(2)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(wangList.get(0), wangList.get(1)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO_WANG);
                                        if (isTianHu) {
                                            linkCard1.setHuCard(wangList.get(0));
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        if (val0 == 100) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));

                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), list.get(2)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list.get(0));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));

                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), list.get(2), wangList.get(0)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list.get(0));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }

                                        break;
                                    case 3:
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), list.get(2)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(wangList.get(0), wangList.get(1), wangList.get(2)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG_WANG);
                                        if (isTianHu) {
                                            linkCard1.setHuCard(wangList.get(0));
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        if ((!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid) || isTianHu) && val0 == 100) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));

                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), list.get(2), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(2)));
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list.get(0));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));

                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), list.get(2)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1), wangList.get(2)));
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list.get(0));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                        break;
                                }
                            } else {
                                switch (wangList.size()) {
                                    case 2:
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), list.get(2), wangList.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(0));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), list.get(2)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(0));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        break;
                                    case 3:
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), list.get(2), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(2)));
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(0));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));

                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), list.get(2)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1), wangList.get(2)));
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list.get(0));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
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
                                yjhs = LdsPhzCardUtils.loadYijuhua(list, LdsPhzCardUtils.loadCardVal(list.get(0)), 0, true, false, false);
                                if (yjhs.size() == 1) {
                                    linkCard1 = new LdsPhzLinkCard();
                                    yjhLists.add(yjhs.get(0));
                                    linkCard1.setYjhList(yjhLists);
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                                break;
                            case 2:
                                yjhs = LdsPhzCardUtils.loadYijuhua(LdsPhzCardUtils.asList(list.get(0), list.get(1)), LdsPhzCardUtils.loadCardVal(list.get(0)), wangList.get(0), true, false, false);
                                if (yjhs.size() == 1) {
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(yjhs.get(0));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2), wangList.get(1)));
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                                yjhs = LdsPhzCardUtils.loadYijuhua(LdsPhzCardUtils.asList(list.get(1), list.get(2)), LdsPhzCardUtils.loadCardVal(list.get(1)), wangList.get(0), true, false, false);
                                if (yjhs.size() == 1) {
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(yjhs.get(0));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                                yjhs = LdsPhzCardUtils.loadYijuhua(LdsPhzCardUtils.asList(list.get(0), list.get(2)), LdsPhzCardUtils.loadCardVal(list.get(0)), wangList.get(0), true, false, false);
                                if (yjhs.size() == 1) {
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(yjhs.get(0));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), wangList.get(1)));
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }

                                if (LdsPhzCardUtils.sameCard(list.get(0), list.get(1))) {
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2), wangList.get(0), wangList.get(1)));
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                } else if (LdsPhzCardUtils.sameCard(list.get(1), list.get(2))) {
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), list.get(2)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
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
                            list1 = LdsPhzCardUtils.loadIdsByVal(list, LdsPhzCardUtils.loadCardVal(integer));
                            if (list1.size() == 2) {
                                break;
                            }
                        }
                        if (list1.size() == 2) {
                            list.removeAll(list1);
                            if (LdsPhzCardUtils.commonCardByVal(val)) {
                                int card0 = LdsPhzCardUtils.loadIdByVal(list, val);
                                if (card0 > 0) {
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), wangList.get(2), wangList.get(3)));
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0)));
                                        if (card0 == list.get(0).intValue()) {
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), wangList.get(2), wangList.get(3)));
                                        } else {
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(2), wangList.get(3)));
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                } else {
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), wangList.get(2), wangList.get(3)));
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0)));
                                    if (card0 == list.get(0).intValue()) {
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), wangList.get(2), wangList.get(3)));
                                    } else {
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(2), wangList.get(3)));
                                    }
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                            } else if (LdsPhzCardUtils.wangCardByVal(val) || isTianHu) {
                                List<List<Integer>> tmpYjhs = LdsPhzCardUtils.loadYijuhua(list, LdsPhzCardUtils.loadCardVal(list.get(0)), wangList.get(0), true, false, false);
                                if (tmpYjhs != null && tmpYjhs.size() == 1) {
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG_WANG);
                                    if (isTianHu) {
                                        linkCard1.setHuCard(wangList.get(1));
                                    }
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                    linkCard1.getYjhList().add(tmpYjhs.get(0));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(wangList.get(1), wangList.get(2), wangList.get(3)));
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO_WANG);
                                        if (isTianHu) {
                                            linkCard1.setHuCard(wangList.get(2));
                                        }
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(1)));
                                        linkCard1.getYjhList().add(tmpYjhs.get(0));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(wangList.get(2), wangList.get(3)));
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                } else {
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), wangList.get(2), wangList.get(3)));
                                    if (isTianHu) {
                                        linkCard1.setHuCard(list.get(1));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                    }
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    if (isTianHu) {
                                        linkCard1.setHuCard(list.get(0));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                    }
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), wangList.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(2), wangList.get(3)));
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), wangList.get(2), wangList.get(3)));
                                    if (isTianHu) {
                                        linkCard1.setHuCard(list.get(1));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                    }
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                            } else {
                                linkCard1 = new LdsPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(0), wangList.get(1)));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), wangList.get(2), wangList.get(3)));
                                cardMessageList = new ArrayList<>(8);
                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                linkCard1 = new LdsPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0)));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), wangList.get(1)));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(2), wangList.get(3)));
                                cardMessageList = new ArrayList<>(8);
                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                linkCard1 = new LdsPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0)));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), wangList.get(1)));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(1), wangList.get(2), wangList.get(3)));
                                cardMessageList = new ArrayList<>(8);
                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                            }
                        }
                    } else if (wangList.size() == 2) {
                        Map<Integer, Integer> map = LdsPhzCardUtils.loadCardCount(list);
                        int val1 = LdsPhzCardUtils.loadCardVal(list.get(0));
                        if (map.size() == 2 && map.get(val1).intValue() == 2) {
                            List<Integer> list1 = LdsPhzCardUtils.loadIdsByVal(list, val);
                            if (list1.size() == 2) {
                                list.removeAll(list1);

                                int val0 = val - LdsPhzCardUtils.loadCardVal(list.get(0));
                                if (val0 == 100 || val0 == -100) {
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list.get(0), list.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(1), wangList.get(0), wangList.get(1)));
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                            LdsPhzHandCards handCards1 = handCards.copy();
                                            List<Integer> list2 = handCards1.KAN.remove(kv.getKey());
                                            list2.add(wangList.get(0));

                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list2);
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list.get(0), list.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(1), wangList.get(1)));
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                        for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                            LdsPhzHandCards handCards1 = handCards.copy();
                                            List<Integer> list2 = handCards1.WEI.remove(kv.getKey());
                                            list2.add(wangList.get(0));

                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list2);
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list.get(0), list.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(1), wangList.get(1)));
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                        for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                            LdsPhzHandCards handCards1 = handCards.copy();
                                            List<Integer> list2 = handCards1.WEI_CHOU.remove(kv.getKey());
                                            list2.add(wangList.get(0));

                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list2);
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list.get(0), list.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(1), wangList.get(1)));
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }

//                                    for (Map.Entry<Integer,List<Integer>> kv:handCards.PENG.entrySet()){
//                                        LdsPhzHandCards handCards1=handCards.copy();
//                                        List<Integer> list2=handCards1.PENG.remove(kv.getKey());
//                                        list2.add(wangList.get(0));
//
//                                        cardMessageList = new ArrayList<>(8);
//                                        LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
//                                        linkCard1=new LdsPhzLinkCard();
//                                        linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
//                                        linkCard1.setYjhList(copy(yjhLists,null));
//                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0),list.get(0),list.get(1)));
//                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(1), wangList.get(1)));
//                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
//                                        addCardMessage(cardResult,list2,cardMessageList);
//                                        linkCard1.getYjhList().add(list2);
//                                        check(cardMessageList, linkCard1, phzBase, linkCardList,uuid);
//                                    }
                                    }
                                }

                                linkCard1 = new LdsPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0), wangList.get(1)));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));
                                cardMessageList = new ArrayList<>(8);
                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                linkCard1 = new LdsPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(0), wangList.get(1)));
                                cardMessageList = new ArrayList<>(8);
                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                    LdsPhzHandCards handCards1 = handCards.copy();
                                    List<Integer> list2 = handCards1.KAN.remove(kv.getKey());
                                    list2.add(wangList.get(0));

                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                                for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                    LdsPhzHandCards handCards1 = handCards.copy();
                                    List<Integer> list2 = handCards1.WEI.remove(kv.getKey());
                                    list2.add(wangList.get(0));

                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                                for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                    LdsPhzHandCards handCards1 = handCards.copy();
                                    List<Integer> list2 = handCards1.WEI_CHOU.remove(kv.getKey());
                                    list2.add(wangList.get(0));

                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }

//                                for (Map.Entry<Integer,List<Integer>> kv:handCards.PENG.entrySet()){
//                                    LdsPhzHandCards handCards1=handCards.copy();
//                                    List<Integer> list2=handCards1.PENG.remove(kv.getKey());
//                                    list2.add(wangList.get(0));
//
//                                    cardMessageList = new ArrayList<>(8);
//                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
//                                    linkCard1=new LdsPhzLinkCard();
//                                    linkCard1.setYjhList(copy(yjhLists,null));
//                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0),list.get(1)));
//                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0),list1.get(1), wangList.get(1)));
//                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
//                                    addCardMessage(cardResult,list2,cardMessageList);
//                                    linkCard1.getYjhList().add(list2);
//                                    check(cardMessageList, linkCard1, phzBase, linkCardList,uuid);
//
//                                    cardMessageList = new ArrayList<>(8);
//                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
//                                    linkCard1=new LdsPhzLinkCard();
//                                    linkCard1.setYjhList(copy(yjhLists,null));
//                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0),list.get(1), wangList.get(1)));
//                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0),list1.get(1)));
//                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
//                                    addCardMessage(cardResult,list2,cardMessageList);
//                                    linkCard1.getYjhList().add(list2);
//                                    check(cardMessageList, linkCard1, phzBase, linkCardList,uuid);
//                                }
                            } else {
                                linkCard1 = new LdsPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2), list.get(3), wangList.get(0), wangList.get(1)));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));
                                cardMessageList = new ArrayList<>(8);
                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                linkCard1 = new LdsPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2), list.get(3)));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(0), wangList.get(1)));
                                cardMessageList = new ArrayList<>(8);
                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                    LdsPhzHandCards handCards1 = handCards.copy();
                                    List<Integer> list2 = handCards1.KAN.remove(kv.getKey());
                                    list2.add(wangList.get(0));

                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2), list.get(3)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2), list.get(3), wangList.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                                for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                    LdsPhzHandCards handCards1 = handCards.copy();
                                    List<Integer> list2 = handCards1.WEI.remove(kv.getKey());
                                    list2.add(wangList.get(0));

                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2), list.get(3)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2), list.get(3), wangList.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                                for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                    LdsPhzHandCards handCards1 = handCards.copy();
                                    List<Integer> list2 = handCards1.WEI_CHOU.remove(kv.getKey());
                                    list2.add(wangList.get(0));

                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2), list.get(3)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2), list.get(3), wangList.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }

//                                for (Map.Entry<Integer,List<Integer>> kv:handCards.PENG.entrySet()){
//                                    LdsPhzHandCards handCards1=handCards.copy();
//                                    List<Integer> list2=handCards1.PENG.remove(kv.getKey());
//                                    list2.add(wangList.get(0));
//
//                                    cardMessageList = new ArrayList<>(8);
//                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
//                                    linkCard1=new LdsPhzLinkCard();
//                                    linkCard1.setYjhList(copy(yjhLists,null));
//                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2),list.get(3)));
//                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0),list.get(1), wangList.get(1)));
//                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
//                                    addCardMessage(cardResult,list2,cardMessageList);
//                                    linkCard1.getYjhList().add(list2);
//                                    check(cardMessageList, linkCard1, phzBase, linkCardList,uuid);
//
//                                    cardMessageList = new ArrayList<>(8);
//                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
//                                    linkCard1=new LdsPhzLinkCard();
//                                    linkCard1.setYjhList(copy(yjhLists,null));
//                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2),list.get(3), wangList.get(1)));
//                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0),list.get(1)));
//                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
//                                    addCardMessage(cardResult,list2,cardMessageList);
//                                    linkCard1.getYjhList().add(list2);
//                                    check(cardMessageList, linkCard1, phzBase, linkCardList,uuid);
//                                }
                            }
                        }
                    } else if (wangList.size() == 1) {
                        Map<Integer, Integer> map = LdsPhzCardUtils.loadCardCount(list);
                        int val1 = LdsPhzCardUtils.loadCardVal(list.get(0));
                        if (map.size() == 2 && map.get(val1).intValue() == 2) {
                            List<Integer> list1 = LdsPhzCardUtils.loadIdsByVal(list, val);
                            if (list1.size() == 2) {
                                list.removeAll(list1);

                                int val0 = val - LdsPhzCardUtils.loadCardVal(list.get(0));
                                if (val0 == 100 || val0 == -100) {
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list.get(0), list.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(1), wangList.get(0)));
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                                linkCard1 = new LdsPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(0)));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                cardMessageList = new ArrayList<>(8);
                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                linkCard1 = new LdsPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(0)));
                                cardMessageList = new ArrayList<>(8);
                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                            } else {
                                linkCard1 = new LdsPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1), wangList.get(0)));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2), list.get(3)));
                                cardMessageList = new ArrayList<>(8);
                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                linkCard1 = new LdsPhzLinkCard();
                                linkCard1.setYjhList(copy(yjhLists, null));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(0), list.get(1)));
                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list.get(2), list.get(3), wangList.get(0)));
                                cardMessageList = new ArrayList<>(8);
                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                            }
                        }
                    }
                    break;

                case 5:
                    if (wangList.size() == 4) {
                        Map<Integer, Integer> countMap = LdsPhzCardUtils.loadCardCount(list);
                        if (countMap.size() <= 3) {
                            List<List<Integer>> lists = new ArrayList<>(3);
                            for (Map.Entry<Integer, Integer> kv : countMap.entrySet()) {
                                lists.add(LdsPhzCardUtils.loadIdsByVal(list, kv.getKey().intValue()));
                            }
                            if (lists.size() == 2) {
                                if (LdsPhzCardUtils.wangCardByVal(val) || isTianHu) {
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(lists.get(0));
                                    linkCard1.getYjhList().add(lists.get(1));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(wangList.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                    linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA_WANG);

                                    if (isTianHu)
                                        linkCard1.setHuCard(wangList.get(0));

                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                } else if (LdsPhzCardUtils.hasCard(lists.get(0), val)) {
                                    if (lists.get(0).size() == 2) {
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), wangList.get(0)));
                                        linkCard1.getYjhList().add(lists.get(1));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(1), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), lists.get(1).get(2), wangList.get(2)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(1), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.KAN.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), lists.get(1).get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(1), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), lists.get(1).get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(1), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), lists.get(1).get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(1), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    } else if (lists.get(0).size() == 3) {
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(lists.get(1));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(2), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(2), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), wangList.get(0)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(2), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.KAN.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(1), lists.get(0).get(2), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(1), lists.get(0).get(2), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(1), lists.get(0).get(2), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }

                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(2), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(2), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.KAN.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(1), lists.get(0).get(2), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(1), lists.get(0).get(2), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(1), lists.get(0).get(2), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    }
                                } else if (LdsPhzCardUtils.hasCard(lists.get(1), val)) {
                                    if (lists.get(1).size() == 2) {
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), wangList.get(0)));
                                        linkCard1.getYjhList().add(lists.get(0));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(1), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), lists.get(0).get(2), wangList.get(2)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(1), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.KAN.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), lists.get(0).get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(1), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), lists.get(0).get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(1), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), lists.get(0).get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(1), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    } else if (lists.get(1).size() == 3) {
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(lists.get(0));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(2), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(2), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), wangList.get(0)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(2), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.KAN.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(1), lists.get(1).get(2), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(1), lists.get(1).get(2), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(1), lists.get(1).get(2), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }

                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(2), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(2), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.KAN.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(1), lists.get(1).get(2), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(1), lists.get(1).get(2), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list1 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list1.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list1);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(1), lists.get(1).get(2), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    }
                                } else if (LdsPhzCardUtils.commonCardByVal(val)) {
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
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(curCard, wangList.get(1), wangList.get(2), wangList.get(3)));
                                    linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        if (lists.get(0).size() == 2 && LdsPhzCardUtils.bigCard(lists.get(0).get(0))) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), wangList.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), lists.get(1).get(2)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(curCard, wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        } else if (lists.get(1).size() == 2 && LdsPhzCardUtils.bigCard(lists.get(1).get(0))) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(1).get(0), lists.get(1).get(1), wangList.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(lists.get(0).get(0), lists.get(0).get(1), lists.get(0).get(2)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(curCard, wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
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
                                    if (LdsPhzCardUtils.loadCardVal(list1.get(0)) == val || isTianHu) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list1.get(0));
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                        boolean checkChuang = true;
                                        if (check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            checkChuang = false;
                                        }

                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list2);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                        if (isTianHu) {
                                            linkCard1.setHuCard(list1.get(0));
                                        }
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            checkChuang = false;
                                        }

                                        if (checkChuang) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list1.get(0));
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0), wangList.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                if (isTianHu) {
                                                    linkCard1.setHuCard(list1.get(0));
                                                }
                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                if (isTianHu) {
                                                    linkCard1.setHuCard(list1.get(0));
                                                }
                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                                for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                    LdsPhzHandCards handCards1 = handCards.copy();
                                                    List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                    list10.add(wangList.get(0));

                                                    cardMessageList = new ArrayList<>(8);
                                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                    linkCard1 = new LdsPhzLinkCard();
                                                    linkCard1.setYjhList(copy(yjhLists, null));
                                                    linkCard1.getYjhList().add(list10);
                                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                    linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                    if (isTianHu) {
                                                        linkCard1.setHuCard(list1.get(0));
                                                    }
                                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                                }
                                                for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                    LdsPhzHandCards handCards1 = handCards.copy();
                                                    List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                    list10.add(wangList.get(0));

                                                    cardMessageList = new ArrayList<>(8);
                                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                    linkCard1 = new LdsPhzLinkCard();
                                                    linkCard1.setYjhList(copy(yjhLists, null));
                                                    linkCard1.getYjhList().add(list10);
                                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                    linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                    if (isTianHu) {
                                                        linkCard1.setHuCard(list1.get(0));
                                                    }
                                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                                }
                                                for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                    LdsPhzHandCards handCards1 = handCards.copy();
                                                    List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                    list10.add(wangList.get(0));

                                                    cardMessageList = new ArrayList<>(8);
                                                    LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                    linkCard1 = new LdsPhzLinkCard();
                                                    linkCard1.setYjhList(copy(yjhLists, null));
                                                    linkCard1.getYjhList().add(list10);
                                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                    linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
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
                                    if (LdsPhzCardUtils.loadCardVal(list2.get(0)) == val || isTianHu) {
                                        yjhList = LdsPhzCardUtils.loadYijuhua(LdsPhzCardUtils.asList(list1.get(0), list2.get(0)), LdsPhzCardUtils.loadCardVal(list1.get(0)), wangList.get(0), false, false);
                                        if (yjhList.size() == 1) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list3);
                                            linkCard1.getYjhList().add(yjhList.get(0));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(1), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list2.get(1));
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                    }

                                    if (LdsPhzCardUtils.loadCardVal(list3.get(0)) == val || isTianHu) {
                                        yjhList = LdsPhzCardUtils.loadYijuhua(LdsPhzCardUtils.asList(list1.get(0), list3.get(0)), LdsPhzCardUtils.loadCardVal(list1.get(0)), wangList.get(0), false, false);
                                        if (yjhList.size() == 1) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list2);
                                            linkCard1.getYjhList().add(yjhList.get(0));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(1), wangList.get(1), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list3.get(1));
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                    }
                                } else {
                                    if (list2.size() == 3 && LdsPhzCardUtils.loadCardVal(list2.get(0)) == val) {
                                        List<List<Integer>> yjhList = LdsPhzCardUtils.loadYijuhua(LdsPhzCardUtils.asList(list1.get(0), list3.get(0)), LdsPhzCardUtils.loadCardVal(list1.get(0)), wangList.get(0), false, false);
                                        if (yjhList.size() == 1) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1)));
                                            linkCard1.getYjhList().add(yjhList.get(0));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(2), wangList.get(1), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                            if (isTianHu) {
                                                linkCard1.setHuCard(list2.get(2));
                                            }
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                    }
                                }

                                int tempVal;
                                if (isTianHu) {
                                    if ((tempVal = LdsPhzCardUtils.loadCardVal(list1.get(0)) - LdsPhzCardUtils.loadCardVal(list2.get(0))) == 100 || tempVal == -100) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list2.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(wangList.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA_WANG);
                                        linkCard1.setHuCard(wangList.get(0));
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list3);
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                            linkCard1.setHuCard(list1.get(0));
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(0)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                            linkCard1.setHuCard(list1.get(0));
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                linkCard1.setHuCard(list1.get(0));
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                linkCard1.setHuCard(list1.get(0));
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                linkCard1.setHuCard(list1.get(0));
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    } else if ((tempVal = LdsPhzCardUtils.loadCardVal(list1.get(0)) - LdsPhzCardUtils.loadCardVal(list3.get(0))) == 100 || tempVal == -100) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list2);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list3.get(0), list3.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(wangList.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA_WANG);
                                        linkCard1.setHuCard(wangList.get(0));
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list3);
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                            linkCard1.setHuCard(list1.get(0));
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(0)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                            linkCard1.setHuCard(list1.get(0));
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                linkCard1.setHuCard(list1.get(0));
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                linkCard1.setHuCard(list1.get(0));
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                linkCard1.setHuCard(list1.get(0));
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    } else if (LdsPhzCardUtils.loadYijuhua(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)), LdsPhzCardUtils.loadCardVal(list1.get(0)), 0, false, true).size() == 1) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), list3.get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG_WANG);
                                        linkCard1.setHuCard(wangList.get(0));
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                    boolean tempBl = true;
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(0)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                    linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                    linkCard1.setHuCard(list1.get(0));
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        tempBl = false;
                                    }

                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                    linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                    linkCard1.setHuCard(list1.get(0));
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        tempBl = false;
                                    }

                                    if (tempBl) {
                                        for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                            LdsPhzHandCards handCards1 = handCards.copy();
                                            List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                            list10.add(wangList.get(0));

                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list10);
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                            linkCard1.setHuCard(list1.get(0));
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                        for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                            LdsPhzHandCards handCards1 = handCards.copy();
                                            List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                            list10.add(wangList.get(0));

                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list10);
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                            linkCard1.setHuCard(list1.get(0));
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                        for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                            LdsPhzHandCards handCards1 = handCards.copy();
                                            List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                            list10.add(wangList.get(0));

                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list10);
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                            linkCard1.setHuCard(list1.get(0));
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                    }
                                } else if (LdsPhzCardUtils.wangCardByVal(val)) {
                                    if ((tempVal = LdsPhzCardUtils.loadCardVal(list1.get(0)) - LdsPhzCardUtils.loadCardVal(list2.get(0))) == 100 || tempVal == -100) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list2.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(wangList.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA_WANG);
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    } else if ((tempVal = LdsPhzCardUtils.loadCardVal(list1.get(0)) - LdsPhzCardUtils.loadCardVal(list3.get(0))) == 100 || tempVal == -100) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list2);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list3.get(0), list3.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(wangList.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA_WANG);
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    } else if (LdsPhzCardUtils.loadYijuhua(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)), LdsPhzCardUtils.loadCardVal(list1.get(0)), 0, false, true).size() == 1) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), list3.get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG_WANG);
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                } else if (LdsPhzCardUtils.loadCardVal(list1.get(0)) == val) {
                                    boolean tempBl = true;
                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list3);
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                    linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        tempBl = false;
                                    }

                                    linkCard1 = new LdsPhzLinkCard();
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(list2);
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(0)));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(1), wangList.get(2), wangList.get(3)));
                                    linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    if (check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                        tempBl = false;
                                    }

                                    if (tempBl) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(2)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1), wangList.get(2)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                            LdsPhzHandCards handCards1 = handCards.copy();
                                            List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                            list10.add(wangList.get(0));

                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list10);
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                        for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                            LdsPhzHandCards handCards1 = handCards.copy();
                                            List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                            list10.add(wangList.get(0));

                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list10);
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                        for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                            LdsPhzHandCards handCards1 = handCards.copy();
                                            List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                            list10.add(wangList.get(0));

                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list10);
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                    }
                                } else if (LdsPhzCardUtils.loadCardVal(list2.get(0)) == val) {
                                    if ((tempVal = LdsPhzCardUtils.loadCardVal(list1.get(0)) - LdsPhzCardUtils.loadCardVal(list2.get(0))) == 100 || tempVal == -100) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), wangList.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), wangList.get(0)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    } else if ((tempVal = LdsPhzCardUtils.loadCardVal(list1.get(0)) - LdsPhzCardUtils.loadCardVal(list3.get(0))) == 100 || tempVal == -100) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), wangList.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list3.get(0), list3.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), list1.get(0)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), wangList.get(0), wangList.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), list1.get(0)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), wangList.get(0), wangList.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), list1.get(0)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), wangList.get(0), wangList.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    } else if (LdsPhzCardUtils.loadYijuhua(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)), LdsPhzCardUtils.loadCardVal(list1.get(0)), 0, false, true).size() == 1) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(1), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(1), wangList.get(0), wangList.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(1), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(2)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(1), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(2)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(1), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(2)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }

                                    }
                                } else if (LdsPhzCardUtils.loadCardVal(list3.get(0)) == val) {
                                    if ((tempVal = LdsPhzCardUtils.loadCardVal(list1.get(0)) - LdsPhzCardUtils.loadCardVal(list3.get(0))) == 100 || tempVal == -100) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list2);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list3.get(0), wangList.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(1), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(0), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list3.get(0), wangList.get(0)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(1), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list3.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list3.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list3.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    } else if ((tempVal = LdsPhzCardUtils.loadCardVal(list1.get(0)) - LdsPhzCardUtils.loadCardVal(list2.get(0))) == 100 || tempVal == -100) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), wangList.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list2.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(1), wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), list1.get(0)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), wangList.get(0), wangList.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(1), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), list1.get(0)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), wangList.get(0), wangList.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(1), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), list1.get(0)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), wangList.get(0), wangList.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(1), wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    } else if (LdsPhzCardUtils.loadYijuhua(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)), LdsPhzCardUtils.loadCardVal(list1.get(0)), 0, false, true).size() == 1) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(0), wangList.get(1), wangList.get(2)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(1), wangList.get(3)));
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(1), wangList.get(2)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(1), wangList.get(2)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(0));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(1), wangList.get(2)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    }
                                } else if (LdsPhzCardUtils.commonCardByVal(val)) {
                                    Integer curCard = cardResult.getCurId();
                                    for (List<Integer> list11 : yjhLists) {
                                        if (list11.contains(curCard)) {
                                            list11.remove(curCard);
                                            list11.add(wangList.get(0));
                                            break;
                                        }
                                    }

                                    if ((tempVal = LdsPhzCardUtils.loadCardVal(list1.get(0)) - LdsPhzCardUtils.loadCardVal(list2.get(0))) == 100 || tempVal == -100) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list2.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(curCard, wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(1));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list2.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(curCard, wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(1));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list2.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(curCard, wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(1));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list2.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list3.get(0), list3.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(curCard, wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }

                                    } else if ((tempVal = LdsPhzCardUtils.loadCardVal(list1.get(0)) - LdsPhzCardUtils.loadCardVal(list3.get(0))) == 100 || tempVal == -100) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list3.get(0), list3.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(curCard, wangList.get(1), wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(1));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list3.get(0), list3.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(curCard, wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(1));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list3.get(0), list3.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(curCard, wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(1));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list3.get(0), list3.get(1)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), list2.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(curCard, wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    } else if (LdsPhzCardUtils.loadYijuhua(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)), LdsPhzCardUtils.loadCardVal(list1.get(0)), 0, false, true).size() == 1) {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), list3.get(1), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(curCard, wangList.get(2), wangList.get(3)));
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.KAN.remove(kv.getKey());
                                                list10.add(wangList.get(1));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), list3.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(curCard, wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI.remove(kv.getKey());
                                                list10.add(wangList.get(1));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), list3.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(curCard, wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                            for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
                                                LdsPhzHandCards handCards1 = handCards.copy();
                                                List<Integer> list10 = handCards1.WEI_CHOU.remove(kv.getKey());
                                                list10.add(wangList.get(1));

                                                cardMessageList = new ArrayList<>(8);
                                                LdsPhzCardUtils.loadCardMessageList(handCards1, cardMessageList);
                                                linkCard1 = new LdsPhzLinkCard();
                                                linkCard1.setYjhList(copy(yjhLists, null));
                                                linkCard1.getYjhList().add(list10);
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list2.get(0), list3.get(0)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), list3.get(1), wangList.get(2)));
                                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(curCard, wangList.get(3)));
                                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

                                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (wangList.size() == 3 && LdsPhzCardUtils.hasCard(list, val)) {
                        Map<Integer, Integer> map = LdsPhzCardUtils.loadCardCount(list);
                        List<List<Integer>> lists = new ArrayList<>(2);
                        for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
                            if (kv.getValue().intValue() >= 2) {
                                lists.add(LdsPhzCardUtils.loadIdsByVal(list, kv.getKey()));
                            }
                        }
                        for (List<Integer> list1 : lists) {
                            if (list1.size() == 2) {
                                List<Integer> list2 = new ArrayList<>(list);
                                list2.removeAll(list1);
                                List<List<Integer>> yjhs = LdsPhzCardUtils.loadYijuhua(list2, LdsPhzCardUtils.loadCardVal(list2.get(0)), 0, true, false, false);
                                if (yjhs.size() == 1) {
                                    List<Integer> list3 = yjhs.get(0);
                                    int card0 = LdsPhzCardUtils.loadIdByVal(list3, val);
                                    if (card0 > 0) {
                                        list3.remove(Integer.valueOf(card0));
                                        list3.add(wangList.get(0));
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(card0, wangList.get(1), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list3);
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(card0, wangList.get(2)));
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }
                                    } else {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(1), wangList.get(1), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                } else {
                                    yjhs = LdsPhzCardUtils.loadYijuhua(list2, LdsPhzCardUtils.loadCardVal(list2.get(0)), wangList.get(0), true, false, false);
                                    yjhs.addAll(LdsPhzCardUtils.loadYijuhua(list2, LdsPhzCardUtils.loadCardVal(list2.get(1)), wangList.get(0), true, false, false));
                                    for (List<Integer> list3 : yjhs) {
                                        List<Integer> list4 = new ArrayList<>(list2);
                                        list4.removeAll(list3);

                                        linkCard1 = new LdsPhzLinkCard();
                                        if (val == LdsPhzCardUtils.loadCardVal(list4.get(0))) {
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                        }
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list4.get(0), wangList.get(1), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        linkCard1 = new LdsPhzLinkCard();
                                        if (val == LdsPhzCardUtils.loadCardVal(list4.get(0))) {
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                        }
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list4.get(0), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                }
                                int val0 = LdsPhzCardUtils.loadCardVal(list1.get(0));
                                int card0 = LdsPhzCardUtils.loadIdByVal(list2, val0 > 100 ? (val0 - 100) : (val0 + 100));
                                if (card0 > 0) {
                                    list2.remove(Integer.valueOf(card0));
                                    linkCard1 = new LdsPhzLinkCard();
                                    if (LdsPhzCardUtils.hasCard(list2, val)) {
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                    }
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), card0));
                                    if (val == LdsPhzCardUtils.loadCardVal(list2.get(0))) {
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(2)));
                                    } else {
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), wangList.get(2)));
                                    }
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    linkCard1 = new LdsPhzLinkCard();
                                    if (LdsPhzCardUtils.hasCard(list2, val)) {
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                    }
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), card0));
                                    if (val == LdsPhzCardUtils.loadCardVal(list2.get(0))) {
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), wangList.get(2)));
                                    } else {
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(2)));
                                    }
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                }
                            } else if (list1.size() == 3) {
                                List<Integer> list2 = new ArrayList<>(list);
                                list2.removeAll(list1);
                                list2.add(list1.get(2));
                                List<List<Integer>> yjhs = LdsPhzCardUtils.loadYijuhua(list2, LdsPhzCardUtils.loadCardVal(list2.get(0)), 0, true, false, false);
                                if (yjhs.size() == 1) {
                                    List<Integer> list3 = yjhs.get(0);
                                    int card0 = LdsPhzCardUtils.loadIdByVal(list3, val);
                                    if (card0 > 0) {
                                        list3.remove(Integer.valueOf(card0));
                                        list3.add(wangList.get(0));
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(card0, wangList.get(1), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        if (!check(cardMessageList, linkCard1, phzBase, linkCardList, uuid)) {
                                            linkCard1 = new LdsPhzLinkCard();
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                            linkCard1.setYjhList(copy(yjhLists, null));
                                            linkCard1.getYjhList().add(list3);
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(1)));
                                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(card0, wangList.get(2)));
                                            cardMessageList = new ArrayList<>(8);
                                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                        }

                                    } else {
                                        linkCard1 = new LdsPhzLinkCard();
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), wangList.get(0)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(1), wangList.get(1), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                } else {
                                    yjhs = LdsPhzCardUtils.loadYijuhua(list2, LdsPhzCardUtils.loadCardVal(list2.get(0)), wangList.get(0), true, false, false);
                                    yjhs.addAll(LdsPhzCardUtils.loadYijuhua(list2, LdsPhzCardUtils.loadCardVal(list2.get(1)), wangList.get(0), true, false, false));
                                    for (List<Integer> list3 : yjhs) {
                                        List<Integer> list4 = new ArrayList<>(list2);
                                        list4.removeAll(list3);

                                        linkCard1 = new LdsPhzLinkCard();
                                        if (val == LdsPhzCardUtils.loadCardVal(list4.get(0))) {
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                        }
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list4.get(0), wangList.get(1), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                        linkCard1 = new LdsPhzLinkCard();
                                        if (val == LdsPhzCardUtils.loadCardVal(list4.get(0))) {
                                            linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                        }
                                        linkCard1.setYjhList(copy(yjhLists, null));
                                        linkCard1.getYjhList().add(list3);
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list4.get(0), wangList.get(2)));
                                        cardMessageList = new ArrayList<>(8);
                                        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
                                    }
                                }
                                int val0 = LdsPhzCardUtils.loadCardVal(list1.get(0));
                                int card0 = LdsPhzCardUtils.loadIdByVal(list2, val0);
                                if (card0 > 0) {
                                    list2.remove(Integer.valueOf(card0));
                                    linkCard1 = new LdsPhzLinkCard();
                                    if (LdsPhzCardUtils.hasCard(list2, val)) {
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
                                    }
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), card0));
                                    if (val == LdsPhzCardUtils.loadCardVal(list2.get(0))) {
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(2)));
                                    } else {
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), wangList.get(2)));
                                    }
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
                                    addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);
                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);

                                    linkCard1 = new LdsPhzLinkCard();
                                    if (LdsPhzCardUtils.hasCard(list2, val)) {
                                        linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
                                    }
                                    linkCard1.setYjhList(copy(yjhLists, null));
                                    linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list1.get(0), list1.get(1), card0));
                                    if (val == LdsPhzCardUtils.loadCardVal(list2.get(0))) {
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), wangList.get(2)));
                                    } else {
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(0), wangList.get(0), wangList.get(1)));
                                        linkCard1.getYjhList().add(LdsPhzCardUtils.asList(list2.get(1), wangList.get(2)));
                                    }
                                    cardMessageList = new ArrayList<>(8);
                                    LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
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

	private static void checkListHu(Map<LdsPhzLinkCard, Integer> linkCardList, LdsPhzCardResult cardResult,
			final LdsPhzBase phzBase, final String uuid, List<List<Integer>> yjhLists, LdsPhzHandCards handCards,
			List<Integer> wangList, boolean isTianHu, int val, LdsPhzLinkCard linkCard1) {
		List<LdsPhzCardMessage> cardMessageList;
		linkCard1.setYjhList(copy(yjhLists, null));
		switch (wangList.size()) {
		    case 0:
		        cardMessageList = new ArrayList<>(8);
		        LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
		        addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

		        check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
		        break;
		    case 1:
		        break;
		    case 2:
		        if (handCards.hasFourSameCard()) {
		            if (LdsPhzCardUtils.wangCard(cardResult.getCurId()) || isTianHu) {
		                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO_WANG);
		                yjhLists.add(LdsPhzCardUtils.asList(wangList.get(0), wangList.get(1)));
		                linkCard1.setYjhList(yjhLists);
		                cardMessageList = new ArrayList<>(8);
		                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
		                addCardMessageList(cardResult, yjhLists, cardMessageList);

		                if (isTianHu) {
		                    linkCard1.setHuCard(wangList.get(0));
		                }

		                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
		            } else if (LdsPhzCardUtils.commonCard(cardResult.getCurId())) {
		                boolean checkDiao = true;
		                for (List<Integer> temp : yjhLists) {
		                    if (temp.size() == 3) {
		                        int card0 = LdsPhzCardUtils.loadIdByVal(temp, val);
		                        if (card0 > 0) {
		                            List<Integer> tmpList = new ArrayList<>(temp);
		                            tmpList.remove(Integer.valueOf(card0));
		                            if (LdsPhzCardUtils.sameCard(tmpList.get(0), tmpList.get(1))) {
		                                linkCard1 = new LdsPhzLinkCard();
		                                linkCard1.setYjhList(copy(yjhLists, temp));
		                                linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
		                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(card0, wangList.get(0), wangList.get(1)));
		                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(tmpList.get(0), tmpList.get(1)));

		                                cardMessageList = new ArrayList<>(8);
		                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
		                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

		                                if (checkDiao) {
		                                    checkDiao = !check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
		                                } else {
		                                    check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
		                                }
		                            }

		                            if (checkDiao) {
		                                checkDiao = false;
		                                linkCard1 = new LdsPhzLinkCard();
		                                linkCard1.setYjhList(copy(yjhLists, temp));
		                                linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO);
		                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(tmpList.get(0), tmpList.get(1), wangList.get(0)));
		                                linkCard1.getYjhList().add(LdsPhzCardUtils.asList(card0, wangList.get(1)));
		                                cardMessageList = new ArrayList<>(8);
		                                LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
		                                addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

		                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
		                            }
		                        }
		                    }
		                }
		            }else {
//                        	 linkCard1.addFan(LdsPhzFanEnums.WANG_DIAO_WANG);
		                 yjhLists.add(LdsPhzCardUtils.asList(wangList.get(0), wangList.get(1)));
		                 linkCard1.setYjhList(yjhLists);
		                 cardMessageList = new ArrayList<>(8);
		                 LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
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
		        if (LdsPhzCardUtils.wangCard(cardResult.getCurId()) || isTianHu) {
		            linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG_WANG);
		            List<Integer> temp = LdsPhzCardUtils.asList(wangList.get(0), wangList.get(1), wangList.get(2));
		            yjhLists.add(temp);

		            linkCard1.setYjhList(yjhLists);
		            cardMessageList = new ArrayList<>(8);
		            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
		            addCardMessageList(cardResult, yjhLists, cardMessageList);
		            if (isTianHu) {
		                linkCard1.setHuCard(wangList.get(0));
		            }
		            check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
		        } else if (LdsPhzCardUtils.commonCard(cardResult.getCurId())) {
		            boolean checkChuang = true;
		            for (List<Integer> temp : yjhLists) {
		                if (temp.size() == 3) {
		                    int card0 = LdsPhzCardUtils.loadIdByVal(temp, val);
		                    if (card0 > 0) {
		                        List<Integer> tmpList = new ArrayList<>(temp);
		                        tmpList.remove(Integer.valueOf(card0));
		                        if (LdsPhzCardUtils.sameCard(tmpList.get(0), tmpList.get(1))) {
		                            linkCard1 = new LdsPhzLinkCard();
		                            linkCard1.setYjhList(copy(yjhLists, temp));
		                            linkCard1.addFan(LdsPhzFanEnums.WANG_ZHA);
		                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(card0, wangList.get(0), wangList.get(1), wangList.get(2)));
		                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(tmpList.get(0), tmpList.get(1)));

		                            cardMessageList = new ArrayList<>(8);
		                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
		                            addCardMessageList(cardResult, linkCard1.getYjhList(), cardMessageList);

		                            if (checkChuang) {
		                                checkChuang = !check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
		                            } else {
		                                check(cardMessageList, linkCard1, phzBase, linkCardList, uuid);
		                            }
		                        }

		                        if (checkChuang) {
		                            checkChuang = false;
		                            linkCard1 = new LdsPhzLinkCard();
		                            linkCard1.setYjhList(copy(yjhLists, temp));
		                            linkCard1.addFan(LdsPhzFanEnums.WANG_CHUANG);
		                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(tmpList.get(0), tmpList.get(1), wangList.get(0)));
		                            linkCard1.getYjhList().add(LdsPhzCardUtils.asList(card0, wangList.get(1), wangList.get(2)));
		                            cardMessageList = new ArrayList<>(8);
		                            LdsPhzCardUtils.loadCardMessageList(handCards, cardMessageList);
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


    public static int loadMaxFanCount(Set<LdsPhzFanEnums> fans) {
        int fanCount = 1;
        for (LdsPhzFanEnums fan : fans) {
            fanCount *= fan.getFan();
        }
        return fanCount;
    }

    public static boolean check(List<LdsPhzCardMessage> cardMessageList, LdsPhzLinkCard linkCard, LdsPhzBase phzBase, Map<LdsPhzLinkCard, Integer> linkCards, final String uuid) {
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

    public static void addCardMessageList(LdsPhzCardResult cardResult, List<List<Integer>> yjhLists, List<LdsPhzCardMessage> cardMessageList) {
        for (List<Integer> tmpList : yjhLists) {
            LdsPhzCardMessage cm = new LdsPhzCardMessage(LdsPhzCardUtils.loadHuXiEnum(tmpList), tmpList);
            if (!cardResult.isSelf() && cm.getHuXiEnum() == LdsPhzHuXiEnums.WEI && tmpList.contains(cardResult.getCurId()) && !LdsPhzCardUtils.wangCard(cardResult.getCurId())) {
                cm = new LdsPhzCardMessage(LdsPhzHuXiEnums.PENG, tmpList);
            }
            cardMessageList.add(cm);
        }
    }

    public static void addCardMessage(LdsPhzCardResult cardResult, List<Integer> yjh, List<LdsPhzCardMessage> cardMessageList) {
        LdsPhzCardMessage cm = new LdsPhzCardMessage(yjh.size() == 4 ? LdsPhzHuXiEnums.PAO : LdsPhzCardUtils.loadHuXiEnum(yjh), yjh);
        if (!cardResult.isSelf() && cm.getHuXiEnum() == LdsPhzHuXiEnums.WEI && yjh.contains(cardResult.getCurId()) && !LdsPhzCardUtils.wangCard(cardResult.getCurId())) {
            cm = new LdsPhzCardMessage(LdsPhzHuXiEnums.PENG, yjh);
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
