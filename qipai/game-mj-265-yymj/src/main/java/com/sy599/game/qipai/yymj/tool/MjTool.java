package com.sy599.game.qipai.yymj.tool;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.yymj.constant.MjAction;
import com.sy599.game.qipai.yymj.constant.MjConstants;
import com.sy599.game.qipai.yymj.rule.Mj;
import com.sy599.game.qipai.yymj.rule.MjHelper;
import com.sy599.game.qipai.yymj.rule.MjIndex;
import com.sy599.game.qipai.yymj.rule.MjIndexArr;
import com.sy599.game.qipai.yymj.rule.MjRule;
import com.sy599.game.qipai.yymj.bean.YyMjPlayer;
import com.sy599.game.qipai.yymj.bean.YyMjTable;
import com.sy599.game.qipai.yymj.bean.GameModel;
import com.sy599.game.qipai.yymj.bean.MjHuLack;
import com.sy599.game.qipai.yymj.bean.MjiangHu;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LogUtil;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

/**
 * @author liuping
 */
public class MjTool {


    public static synchronized List<List<Mj>> fapai(List<Integer> copy, int playerCount) {
        List<List<Mj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<Mj> allMjs = new ArrayList<>();
        for (int id : copy) {
            allMjs.add(Mj.getMajang(id));
        }
        for (int i = 0; i < playerCount; i++) {
            if (i == 0) {
                list.add(new ArrayList<>(allMjs.subList(0, 14)));
            } else {
                list.add(new ArrayList<>(allMjs.subList(14 + (i - 1) * 13, 14 + (i - 1) * 13 + 13)));
            }
            if (i == playerCount - 1) {
                list.add(new ArrayList<>(allMjs.subList(14 + (i) * 13, allMjs.size())));
            }

        }
        return list;
    }

    public static synchronized List<List<Mj>> fapai(List<Integer> copy, int playerCount, List<List<Integer>> t) {
        List<List<Mj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<List<Mj>> zpList = new ArrayList<>();
        if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
            for (List<Integer> zp : t) {
                zpList.add(MjHelper.find(copy, zp));
            }
        }
        List<Mj> allMjs = new ArrayList<>();
        for (int id : copy) {
            allMjs.add(Mj.getMajang(id));
        }
        int count = 0;
        for (int i = 0; i < playerCount; i++) {
            if (i == 0) {
                if (zpList.size() > 0) {
                    List<Mj> pai = zpList.get(0);
                    int len = 14 - pai.size();
                    pai.addAll(allMjs.subList(count, len));
                    count += len;
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(0, 14)));
                }
            } else {
                if (zpList.size() > i) {
                    List<Mj> pai = zpList.get(i);
                    int len = 13 - pai.size();
                    pai.addAll(allMjs.subList(count, count + len));
                    count += len;
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(count, count + 13)));
                    count += 13;
                }
            }
            if (i == playerCount - 1) {
                if (zpList.size() > i + 1) {
                    List<Mj> pai = zpList.get(i + 1);
                    pai.addAll(allMjs.subList(count, allMjs.size()));
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(count, allMjs.size())));
                }
            }

        }
        return list;
    }


//	/**
//	 * ????????????
//	 *
//	 * @param majiangIds
//	 * @param playType
//	 * @return
//	 */
//	public static boolean isHu(List<CsMj> majiangIds, int playType) {
//		if (playType == ZZMajiangConstants.play_type_zhuanzhuan) {
//			return isHuZhuanzhuan(majiangIds);
//
//		} else if (playType == ZZMajiangConstants.play_type_changesha) {
//			// return isHu(majiangIds);
//		} else if (playType == ZZMajiangConstants.play_type_hongzhong) {
//			return isHuZhuanzhuan(majiangIds);
//		}
//
//		return false;
//	}

    /**
     * ????????????
     * <p>
     * *@param handCardIds ??????
     * *@param gang ?????????
     * *@param peng ?????????
     * *@param chi ?????????
     * *@param buzhang ??????
     * *@param isBegin ????????????
     * *@param jiang258 ??????258??????
     * *@param table
     * *@param huMj ????????????
     * *@return
     *
     * @return 0?????? 1 ????????? 2????????? 3????????? 4?????????7?????? 5???????????? 6:7?????? 7????????? 8????????? 9????????? 10????????? 11?????????
     */
    public static MjiangHu isHu(List<Mj> handCardIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, boolean isbegin, boolean jiang258, Mj huMj, YyMjTable table, YyMjPlayer player) {
        return isHu(handCardIds, gang, peng, chi, buzhang, isbegin, jiang258, table, huMj, player, false);
    }


    /**
     * @param handCardIds ??????
     * @param gang        ?????????
     * @param peng        ?????????
     * @param chi         ?????????
     * @param buzhang     ??????
     * @param isBegin     ????????????
     * @param jiang258    ??????258??????
     * @param table
     * @param huMj        ????????????
     * @return
     * @description ????????????
     * @author Guang.OuYang
     * @date 2019/10/16
     */
    public static MjiangHu isHu(List<Mj> handCardIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, boolean isBegin, boolean jiang258, YyMjTable table, Mj huMj, YyMjPlayer player, boolean onlyYingZhuangHu) {
        MjiangHu hu = new MjiangHu();
        if (handCardIds == null || handCardIds.isEmpty()) {
            return hu;
        }

//        int kingCards = handCardIds.stream().filter(v -> v.getVal() == player.getPlayingTable(TjMjTable.class).getKingCard().getVal()).mapToInt(v -> 1).sum();

        //?????????????????????????????????????????????????????????????????????????????????,?????????+????????????,???????????????
        boolean canYingZhuang = table.getGameModel().isCreateKingCard()/* && (kingCards == 0 || player.getKingCardNumHuFlag() >= table.getGameModel().getSpecialPlay().getFloorHuNum())*/;

        //?????????????????????????????????
        isHuMain(handCardIds, gang, peng, chi, buzhang, jiang258, table, player, hu, canYingZhuang);

        if (hu.isHu()) {
            LogUtil.printDebug("????????????:{}", dahuListToString(hu.buildDahuList()));

            //?????????????????????????????????,????????????????????????,????????????????????????????????????
            boolean falseYingZhuang = table.getKingCard() != null
                    && !CollectionUtils.isEmpty(handCardIds)
                    && handCardIds.size() == 2
                    && handCardIds.get(0).getVal() == table.getKingCard().getVal()
                    && handCardIds.get(1).getVal() == table.getKingCard().getVal();

//            hu.setYingZhuang(hu.isYingZhuang() || !falseYingZhuang);
            hu.setYingZhuang((table.getKingCard() == null ?  false : handCardIds.stream().filter(v -> v.getVal() == table.getKingCard().getVal()).mapToInt(v -> 1).sum() == 0));

            hu.initDahuList();
        }

        if (canYingZhuang && !onlyYingZhuangHu) {
            int addYingZhuangScore = 0;
            MjiangHu hu2 = new MjiangHu();

            if (hu.isHu()) {
                //?????????????????????
                addYingZhuangScore = MjiangHu.calcMenZiScore(table, hu.getDahuList());
            }

            //???????????????
            isHuMain(handCardIds, gang, peng, chi, buzhang, jiang258, table, player, hu2, false);

            hu2.initDahuList();

            //??????????????????
            int notYingZhuangScore = MjiangHu.calcMenZiScore(table, hu2.getDahuList());

            if (addYingZhuangScore < notYingZhuangScore) {
                LogUtil.printDebug("????????????,??????????????????:{},??????:{},?????????????????????:{},??????:{}", addYingZhuangScore, dahuListToString(hu.getDahuList()), notYingZhuangScore, dahuListToString(hu2.getDahuList()));
                hu = hu2;
            }

            LogUtil.printDebug("???????????????:{}", dahuListToString( hu.buildDahuList()));
        }


        //?????????,??????
        //????????????14?????????????????????????????????????????????
        //??????????????????????????????
        //???????????????,?????????????????????, ????????????
        if (table.getGameModel().getSpecialPlay().isOpeningHu() && player.getSeat() == table.getLastWinSeat() && hu.isHu() && !hu.isHeitianhu() && table.getDisCardRound() == 0) {
            hu.setTianhu(true);
        }else if (table.getGameModel().getSpecialPlay().isOpeningHu() && player.getSeat() != table.getLastWinSeat() && hu.isHu() && !hu.isHeitianhu() && player.isFirstDisCard()) {
        	//????????????????????????????????????
            hu.setBaoTing(true);
        }

//        boolean isZimo = player.getPlayingTable(ChaosMjTable.class).getMoMajiangSeat() == player.getSeat() || player.getPlayingTable(ChaosMjTable.class).isBegin();

//        Mj floorCard = player.getPlayingTable(ChaosMjTable.class).getFloorCard();

//        int floorNum = isZimo && floorCard != null ? handCardIds.stream().filter(v -> v.getVal() == floorCard.getVal()).mapToInt(v -> 1).sum() : 0;

        //?????????????????????3?????????????????????????????????????????????????????????+?????????????????????????????????????????????????????????????????????
        //????????????????????????4?????????????????????????????????????????????????????????+???????????????????????????????????????
//        if (table.getGameModel().getSpecialPlay().isFloorHu() &&  floorNum >= table.getGameModel().getSpecialPlay().getFloorHuNum() + 1) {
//            hu.setDidihu(true);
//            hu.setHu(true);
//        } else if (table.getGameModel().getSpecialPlay().isFloorHu() && player.noNeedMoCard() && floorNum >= table.getGameModel().getSpecialPlay().getFloorHuNum()) {
//            hu.setDihu(true);
//            hu.setHu(true);
//        }


        if (hu.isHu()) {
            if (player.noNeedMoCard()) {
                hu.setZiMo(true);
            }else{
                hu.setDianPao(true);
            }

            hu.setShowMajiangs(handCardIds);
        }

        hu.initDahuList();

        player.setDahu(hu.getDahuList());
//        LogUtil.printDebug("????????????, ???:{}, ??????:{}, ??????????????????:{}", hu.isHu(), dahuListToString(hu.getDahuList()), table.getGameModel().getSpecialPlay().getFloorHuNum() + 1);

        return hu;
    }

    /**
     *@description ????????????
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2019/10/22
     */
    private static void isHuMain(List<Mj> handCardIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, boolean jiang258, YyMjTable table, YyMjPlayer player, MjiangHu hu, boolean canYingZhuang) {
        if (isPingHu(handCardIds, jiang258, canYingZhuang ? null : table.getKingCard())) {
            hu.setPingHu(true);
            hu.setHu(true);
        }

        LogUtil.printDebug(player.getName() + "??????????????????:{}, ??????;{}, ?????????:{}, ?????????{}", canYingZhuang, hu.isYingZhuang(), player.getKingCardNumHuFlag(), player.getFloorCardNumHuFlag());

        // ????????????
        MjRule.checkDahu(hu, handCardIds, gang, peng, chi, buzhang, table, canYingZhuang, player);
    }


    /***
     *  ????????????, ????????????????????????????????????, ???????????????????????????
     * @param majiangIds
     * @param gang
     * @param peng
     * @param chi
     * @param buzhang
     * @param jiang258
     *
     * @return
     */
    public static List<Mj> getTingMjs(List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, boolean jiang258, boolean dahu, int rule, YyMjTable table, YyMjPlayer player) {
        if (majiangIds == null || majiangIds.isEmpty()) {
            return null;
        }

        List<Mj> res = new LinkedList<>();

        List<Integer> lists = new ArrayList<Integer>();
        table.getSeatMap().values().stream().filter(v -> v.getSeat() != player.getSeat()).forEach(v -> lists.addAll(v.getHandPais()));
        lists.addAll(table.getLeftMajiangs().stream().map(v -> v.getId()).collect(Collectors.toList()));

        HashMap<Integer, Void> repeatedChecked = new HashMap<>();

        for (Integer id : (table.getGameModel().isNoneChar() ? MjConstants.noneChar : table.getGameModel().isNoneWind() ? MjConstants.noneWind : MjConstants.fullMj)) {
//        for (Integer id : lists) {
            int idx = getOtherId(majiangIds, id);
            if (idx == 0) {
                continue;
            }

            Mj mj = Mj.getMajang(idx);

            if (repeatedChecked.containsKey(mj.getVal())) {
              continue;
            }

            repeatedChecked.put(mj.getVal(), null);

            MjiangHu hu = new MjiangHu();
            majiangIds.add(mj);

            if (isPingHu(majiangIds, jiang258, table.getKingCard())) {
                hu.setPingHu(true);
                hu.setHu(true);
            } else {
                MjRule.checkDahu(hu, majiangIds, gang, peng, chi, buzhang, table, false, player);

                if (table.getGameModel().getSpecialPlay().isQuanQiuRenJiang() && hu.isQuanqiuren() && rule == 1 && hu.getDahuCount() == 1) {
                    if (!mj.isJiang()) {
                        hu.setHu(false);
                    }
                }
            }

//            LogUtil.printDebug("??????????????????, ???:{}, ??????:{}", hu.isHu(), dahuListToString(hu.getDahuList()));

            //????????????, ????????????????????????
            //??????????????????
            if (hu.isHu() && !hu.isHeitianhu()/* && !(player.noNeedMoCard() && hu.isHeitianhu())*/) {
                res.add(mj);
            }

            majiangIds.remove(mj);
        }

        //???????????????????????????
        if (res.size() == 1 && (table.getKingCard() == null || res.get(0).getVal() != table.getKingCard().getVal()) && !lists.stream().anyMatch(v->Mj.getMajang(v).getVal()==res.get(0).getVal())) {
            player.setDeathHu(true);
        }else{
            player.setDeathHu(false);
        }

        if (!res.isEmpty() && res.size() > 1)
            res.sort((v1, v2) -> Integer.valueOf(v1.getVal()).compareTo(v2.getVal()));
        return res;

    }


    public static int getOtherId(List<Mj> majiangIds, int id) {

        List<Integer> list = new ArrayList<>();
        Mj omj = Mj.getMajang(id);
        for (Integer idx : MjConstants.fullMj) {
            Mj cm = Mj.getMajang(idx);
            if (omj.getVal() == cm.getVal()) {
                list.add(idx);
            }
        }

        List<Integer> list2 = new ArrayList<>();
        for (Mj mj : majiangIds) {
            if (omj.getVal() == mj.getVal()) {
                list2.add(mj.getId());
            }
        }

        list.removeAll(list2);
        if (list.size() > 0) {
            return list.get(0);
        }
        return 0;
    }

    /**
     *@description ????????????
     *@param majiangIds ??????
     *@param needJiang258 ????????????258??????
     *@param kingCard ??????,?????????
     *@return
     *@author Guang.OuYang
     *@date 2019/10/18
     */
    public static boolean isPingHu(List<Mj> majiangIds, boolean needJiang258, Mj kingCard) {
        //?????????258??????
        needJiang258 = false;

        if (majiangIds == null || majiangIds.isEmpty()) {
            return false;
        }

        if (majiangIds.size() % 3 != 2) {
            return false;
        }

        // ???????????????
        List<Mj> copy = new ArrayList<>(majiangIds);
        List<Mj> kingCards = dropKingCard(copy, kingCard);

        MjIndexArr card_index = new MjIndexArr();
        MjQipaiTool.getMax(card_index, copy);
        // ??????
        if (chaijiang(card_index, copy, kingCards.size(), needJiang258)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * ??????????????????7??????????????????????????????
     *
     * @param majiangIds
     * @param card_index
     */
    public static boolean check7duizi(List<Mj> majiangIds, MjIndexArr card_index, int hongzhongNum) {
        if (majiangIds.size() == 14) {
            // 7??????
            int duizi = card_index.getDuiziNum();
            if (duizi == 7) {
                return true;
            }

        } else if (majiangIds.size() + hongzhongNum == 14) {
            if (hongzhongNum == 0) {
                return false;
            }

            MjIndex index0 = card_index.getMajiangIndex(0);
            MjIndex index2 = card_index.getMajiangIndex(2);
            int lackNum = index0 != null ? index0.getLength() : 0;
            lackNum += index2 != null ? index2.getLength() : 0;

            if (lackNum <= hongzhongNum) {
                return true;
            }

            if (lackNum == 0) {
                lackNum = 14 - majiangIds.size();
                if (lackNum == hongzhongNum) {
                    return true;
                }
            }

        }
        return false;
    }

    /**
     *@description ??????, ?????????,
     *@param card_index ?????????????????? 4???3???2???1?????????
     *@param hasPais ??????
     *@param kingCardSize ????????????
     *@param needJiang258 ??????258?????????
     *@return
     *@author Guang.OuYang
     *@date 2019/10/18
     */
    public static boolean chaijiang(MjIndexArr card_index, List<Mj> hasPais, int kingCardSize, boolean needJiang258) {
        Map<Integer, List<Mj>> jiangMap = card_index.getJiang(needJiang258);
        for (Entry<Integer, List<Mj>> valEntry : jiangMap.entrySet()) {
            List<Mj> copy = new ArrayList<>(hasPais);
            MjHuLack lack = new MjHuLack(kingCardSize);
            List<Mj> list = valEntry.getValue();
            int i = 0;
            for (Mj majiang : list) {
                i++;
                copy.remove(majiang);
                if (i >= 2) {
                    break;
                }
            }
            lack.setHasJiang(true);
            boolean hu = chaipai(lack, copy, needJiang258);
            if (hu) {
//                LogUtil.printDebug("?????????, ??????????????????:{}"+JacksonUtil.writeValueAsString(lack));
                return hu;
            }
        }

        if (kingCardSize > 0) {
            // ???????????????
            if (hasPais.isEmpty()) {
                return true;
            }
            // ?????????
            for (Mj majiang : hasPais) {
                List<Mj> copy = new ArrayList<>(hasPais);
                MjHuLack lack = new MjHuLack(kingCardSize);
                boolean isJiang = false;
                if (!needJiang258) {
                    // ????????????
                    isJiang = true;

                } else {
                    // ??????258??????
                    if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
                        isJiang = true;
                    }

                }
                if (isJiang) {
                    lack.setHasJiang(true);
                    lack.changeHongzhong(-1);
                    lack.addLack(majiang.getVal());
                    copy.remove(majiang);
                }

                boolean hu = chaipai(lack, copy, needJiang258);
                if (lack.isHasJiang() && hu) {
                    LogUtil.printDebug("??????????????????,??????:{}",JacksonUtil.writeValueAsString(lack));
                    return true;
                }
                if (!lack.isHasJiang() && hu) {
                    if (lack.getHongzhongNum() == 2) {
                        // ????????????
                        LogUtil.printDebug("??????????????????,????????????:{}", JacksonUtil.writeValueAsString(lack));
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // ??????
    public static boolean chaipai(MjHuLack lack, List<Mj> hasPais, boolean isNeedJiang258) {
        if (hasPais.isEmpty()) {
            return true;

        }
        boolean hu = chaishun(lack, hasPais, isNeedJiang258);
        if (hu)
            return true;
        return false;
    }

    public static void sortMin(List<Mj> hasPais) {
        Collections.sort(hasPais, new Comparator<Mj>() {

            @Override
            public int compare(Mj o1, Mj o2) {
                if (o1.getPai() < o2.getPai()) {
                    return -1;
                }
                if (o1.getPai() > o2.getPai()) {
                    return 1;
                }
                return 0;
            }

        });
    }

    /**
     * ??????
     *
     * @param hasPais
     * @return
     */
    public static boolean chaishun(MjHuLack lack, List<Mj> hasPais, boolean needJiang258) {
        if (hasPais.isEmpty()) {
            return true;
        }
        sortMin(hasPais);
        Mj minMajiang = hasPais.get(0);
        int minVal = minMajiang.getVal();
        List<Mj> minList = MjQipaiTool.getVal(hasPais, minVal);
        if (minList.size() >= 3) {
            // ????????????
            hasPais.removeAll(minList.subList(0, 3));
            return chaipai(lack, hasPais, needJiang258);
        }

        // ?????????
        int pai1 = minVal;
        int pai2 = 0;
        int pai3 = 0;
        if (pai1 % 10 == 9) {
            pai1 = pai1 - 2;

        } else if (pai1 % 10 == 8) {
            pai1 = pai1 - 1;
        }
        pai2 = pai1 + 1;
        pai3 = pai2 + 1;

        List<Integer> lackList = new ArrayList<>();
        List<Mj> num1 = MjQipaiTool.getVal(hasPais, pai1);
        List<Mj> num2 = MjQipaiTool.getVal(hasPais, pai2);
        List<Mj> num3 = MjQipaiTool.getVal(hasPais, pai3);

        // ????????????????????????
        List<Mj> hasMajiangList = new ArrayList<>();
        if (!num1.isEmpty()) {
            hasMajiangList.add(num1.get(0));
        }
        if (!num2.isEmpty()) {
            hasMajiangList.add(num2.get(0));
        }
        if (!num3.isEmpty()) {
            hasMajiangList.add(num3.get(0));
        }

        // ????????????????????????
        if (num1.isEmpty()) {
            lackList.add(pai1);
        }
        if (num2.isEmpty()) {
            lackList.add(pai2);
        }
        if (num3.isEmpty()) {
            lackList.add(pai3);
        }

        int lackNum = lackList.size();
        if (lackNum > 0) {
            if (lack.getHongzhongNum() <= 0) {
                return false;
            }

            // ?????????????????????2????????????????????????????????????
            if (lackNum >= 2) {
                // ?????????
                List<Mj> count = MjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() >= 3) {
                    hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);

                } else if (count.size() == 2) {
                    if (!lack.isHasJiang() && isCanAsJiang(count.get(0), needJiang258)) {
                        // ???????????????
                        lack.setHasJiang(true);
                        hasPais.removeAll(count);
                        return chaipai(lack, hasPais, needJiang258);
                    }

                    // ????????????????????????
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);
                }

                // ??????
                if (!lack.isHasJiang() && isCanAsJiang(count.get(0), needJiang258) && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.setHasJiang(true);
                    hasPais.removeAll(count);
                    lack.addLack(count.get(0).getVal());
                    return chaipai(lack, hasPais, needJiang258);
                }
            } else if (lackNum == 1) {
                // ??????
                if (!lack.isHasJiang() && isCanAsJiang(minMajiang, needJiang258) && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.setHasJiang(true);
                    hasPais.remove(minMajiang);
                    lack.addLack(minMajiang.getVal());
                    return chaipai(lack, hasPais, needJiang258);
                }

                List<Mj> count = MjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() == 2 && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);
                }
            }

            // ????????????????????????
            if (lack.getHongzhongNum() >= lackNum) {
                lack.changeHongzhong(-lackNum);
                hasPais.removeAll(hasMajiangList);
                lack.addAllLack(lackList);

            } else {
                return false;
            }
        } else {
            // ???????????????
            if (lack.getHongzhongNum() > 0) {
                List<Mj> count1 = MjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                List<Mj> count2 = MjQipaiTool.getVal(hasPais, hasMajiangList.get(1).getVal());
                List<Mj> count3 = MjQipaiTool.getVal(hasPais, hasMajiangList.get(2).getVal());
                if (count1.size() >= 2 && (count2.size() == 1 || count3.size() == 1)) {
                    List<Mj> copy = new ArrayList<>(hasPais);
                    copy.removeAll(count1);
                    MjHuLack copyLack = lack.copy();
                    copyLack.changeHongzhong(-1);

                    copyLack.addLack(hasMajiangList.get(0).getVal());
                    if (chaipai(copyLack, copy, needJiang258)) {
                        return true;
                    }
                }
            }

            hasPais.removeAll(hasMajiangList);
        }
        return chaipai(lack, hasPais, needJiang258);
    }

    public static boolean isCanAsJiang(Mj majiang, boolean isNeed258) {
        if (isNeed258) {
            if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
                return true;
            }
            return false;
        } else {
            return true;
        }

    }

    public static List<Mj> checkChi(List<Mj> majiangs, Mj dismajiang , YyMjTable table) {
        return checkChi(majiangs, dismajiang, table.getKingCard() == null ? null : Arrays.asList(table.getKingCard().getVal()));
    }

    /**
     * ????????????
     *
     * @param majiangs
     * @param dismajiang
     * @return
     */
    public static List<Mj> checkChi(List<Mj> majiangs, Mj dismajiang, List<Integer> wangValList) {
        int disMajiangVal = dismajiang.getVal();
        List<Integer> chi1 = new ArrayList<>(Arrays.asList(disMajiangVal - 2, disMajiangVal - 1));
        List<Integer> chi2 = new ArrayList<>(Arrays.asList(disMajiangVal - 1, disMajiangVal + 1));
        List<Integer> chi3 = new ArrayList<>(Arrays.asList(disMajiangVal + 1, disMajiangVal + 2));

        List<Integer> majiangIds = MjHelper.toMajiangVals(majiangs);
        if (wangValList == null || !checkWang(chi1, wangValList)) {
            if (majiangIds.containsAll(chi1)) {
                return findMajiangByVals(majiangs, chi1);
            }
        }
        if (wangValList == null || !checkWang(chi2, wangValList)) {
            if (majiangIds.containsAll(chi2)) {
                return findMajiangByVals(majiangs, chi2);
            }
        }
        if (wangValList == null || !checkWang(chi3, wangValList)) {
            if (majiangIds.containsAll(chi3)) {
                return findMajiangByVals(majiangs, chi3);
            }
        }
        return new ArrayList<Mj>();
    }

    public static List<Mj> findMajiangByVals(List<Mj> majiangs, List<Integer> vals) {
        List<Mj> result = new ArrayList<>();
        for (int val : vals) {
            for (Mj majiang : majiangs) {
                if (majiang.getVal() == val) {
                    result.add(majiang);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * ????????????
     *
     * @param copy
     * @return
     */
    public static List<Mj> dropKingCard(List<Mj> copy, Mj king) {
        List<Mj> kingCards = new ArrayList<>();
        Iterator<Mj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            Mj majiang = iterator.next();
            if (king != null && majiang.getVal() == king.getVal()) {
                kingCards.add(majiang);
                iterator.remove();
            }
        }
        return kingCards;
    }

    public static boolean checkWang(Object majiangs, List<Integer> wangValList) {
        if (majiangs instanceof List) {
            List list = (List) majiangs;
            for (Object majiang : list) {
                int val = 0;
                if (majiang instanceof Mj) {
                    val = ((Mj) majiang).getVal();
                } else {
                    val = (int) majiang;
                }
                if (wangValList.contains(val)) {
                    return true;
                }
            }
        }

        return false;

    }

    /**
     * ???????????????
     *
     * @param majiangs ?????????
     * @param majiang  ??????
     * @param num      ???????????????
     * @return
     */
    public static List<Mj> getSameMajiang(List<Mj> majiangs, Mj majiang, int num) {
        List<Mj> hongzhong = new ArrayList<>();
        int i = 0;
        for (Mj maji : majiangs) {
            if (maji.getVal() == majiang.getVal()) {
                hongzhong.add(maji);
                i++;
            }
            if (i >= num) {
                break;
            }
        }
        return hongzhong;

    }

    /**
     * ???????????????
     *
     * @param copy
     * @return
     */
    public static List<Mj> dropMjId(List<Mj> copy, int id) {
        List<Mj> hongzhong = new ArrayList<>();
        Iterator<Mj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            Mj majiang = iterator.next();
            if (majiang.getId() == id) {
                hongzhong.add(majiang);
                iterator.remove();
            }
        }
        return hongzhong;
    }

    public static void sortMinPoint(List<Mj> handPais) {
        Collections.sort(handPais, new Comparator<Mj>() {

            @Override
            public int compare(Mj o1, Mj o2) {
                return o1.getVal() - o2.getVal();
            }

        });
    }

//    public static void main(String[] args) {
////        testHuPai();
////		List<Integer> moTailPai = new ArrayList<>(Arrays.asList(0,1,2,4,5));
////		System.out.println(moTailPai);
////		moTailPai = addMoTailPai(moTailPai,15);
////		System.out.println(moTailPai);
//
//        testHuPai();
//    }

    public static void testHuPai() {
        String
                pais = "11,11,12,12,13,13,14,14,21,21,31,31,22,22";
//				pais = "12,12,12,12,13,13,14,14,21,21,31,31,22,22";
//		pais = "12,12,12,12,13,13,13,14,14,22,22,32,32,33";
//		pais = "11,11,11,33,33,33,37,37,37,39,39,39,24,24";
//		pais = "27,27,27,27,14,14,14,14,16,16,11,11,19,201";
//		pais = "11,12,13,17,18,19,31,32,33,35,36,37,28,28";
//		pais = "11,12,13,15,16,17,18,18,24,25,23";
//		pais = "32,32,33,33,34,15,15,21,21,22,22,24,24,36";
		pais = "11,11,11,12,12,12,13,13,13,14,14,14,15,36";

        List<Mj> handPais = getPais(pais);
        System.out.println(toString(handPais));
        List<Mj> gangList = new ArrayList<>();
        List<Mj> pengList = new ArrayList<>();
        List<Mj> chiList = new ArrayList<>();
        List<Mj> buZhangList = new ArrayList<>();
        boolean isBegin = false;
        List<Mj> copy = new ArrayList<>(handPais);
        boolean jiang258 = false;
        YyMjTable tjMjTable = new YyMjTable();
        tjMjTable.setGameModel(GameModel.builder().build());
        tjMjTable.setKingCard(Mj.getMajang(24));
        tjMjTable.setFloorCard(Mj.getMajang(23));
        tjMjTable.getGameModel().setSpecialPlay(new GameModel.SpecialPlay());
        tjMjTable.getGameModel().getSpecialPlay().setFloorHuNum(3);
        tjMjTable.getGameModel().getSpecialPlay().setSevenPairs(true);
        MjiangHu hu = isHu(copy, gangList, pengList, chiList, buZhangList, isBegin, jiang258, null, null, null);
        StringBuilder sb = new StringBuilder("");
        sb.append("hu:").append(hu.isHu());
        sb.append("--daHuList:").append(hu.getDahuList());
        sb.append("--daHuL:").append(dahuListToString(hu.getDahuList()));

        sb.append("--xiaoHuList:").append(hu.getXiaohuList());
        sb.append("--xiaoHu:").append(actListToString(hu.getXiaohuList()));
        System.out.println(sb.toString());
    }

    public static String toString(List<Mj> handPais) {
        sortMinPoint(handPais);
        String paiStr = "";
        for (Mj mj : handPais) {
            paiStr += mj + ",";
        }
        return paiStr;
    }

    public static List<Mj> getPais(String paisStr) {
        String[] pais = paisStr.split(",");
        List<Mj> handPais = new ArrayList<>();
        for (String pai : pais) {
            for (Mj mj : Mj.values()) {
                if (mj.getVal() == Integer.valueOf(pai) && !handPais.contains(mj)) {
                    handPais.add(mj);
                    break;
                }
            }
        }
        return handPais;
    }

    public static String dahuListToString(List<Integer> actList) {
        String[] str = new String[]{"1.?????????",
				"2.?????????",
				"3.?????????",
				"4.?????????",
				"5.??????7??????",
				"6.?????????7??????",
				"7.??????",
				"8.??????",
				"9.??????",
				"10.?????????",
				"11.??????",
				"12.?????????",
				"13.?????????",
				"14.??????",
				"15.?????????7??????",
				"16.??????",
				"17.??????",
				"18.??????",
				"19.??????",
				"20.??????"
        };
        StringBuilder sb = new StringBuilder();
        if (actList != null && actList.size() > 0) {
            sb.append("[");
            for (int i = 0; i < actList.size(); i++) {
                if (sb.length() > 1) {
                    sb.append(",");
                }

                sb.append(str[actList.get(i)]);
            }
            sb.append("]");
        }
        return sb.toString();
    }


    public static String actListToString(List<Integer> actList) {
        StringBuilder sb = new StringBuilder();
        if (actList != null && actList.size() > 0) {
            sb.append("[");
            for (int i = 0; i < actList.size(); i++) {
                if (actList.get(i) == 1) {
                    if (sb.length() > 1) {
                        sb.append(",");
                    }
                    if (i == MjAction.HU) {
                        sb.append("hu");
                    } else if (i == MjAction.PENG) {
                        sb.append("peng");
                    } else if (i == MjAction.MINGGANG) {
                        sb.append("mingGang");
                    } else if (i == MjAction.ANGANG) {
                        sb.append("anGang");
                    } else if (i == MjAction.CHI) {
                        sb.append("chi");
                    } else if (i == MjAction.BUZHANG) {
                        sb.append("buZhang");
                    } else if (i == MjAction.QUEYISE) {
                        sb.append("queYiSe");
                    } else if (i == MjAction.BANBANHU) {
                        sb.append("banBanHu");
                    } else if (i == MjAction.YIZHIHUA) {
                        sb.append("yiZhiHua");
                    } else if (i == MjAction.LIULIUSHUN) {
                        sb.append("liuLiuShun");
                    } else if (i == MjAction.DASIXI) {
                        sb.append("daSiXi");
                    } else if (i == MjAction.JINGTONGYUNU) {
                        sb.append("jinTongYuNv");
                    } else if (i == MjAction.JIEJIEGAO) {
                        sb.append("jieJieGao");
                    } else if (i == MjAction.SANTONG) {
                        sb.append("sanTong");
                    } else if (i == MjAction.ZHONGTUSIXI) {
                        sb.append("zhongTuSiXi");
                    } else if (i == MjAction.ZHONGTULIULIUSHUN) {
                        sb.append("zhongTuLiuLiuShun");
                    }
                }
            }
            sb.append("]");
        }
        return sb.toString();
    }

    public static List<Integer> addMoTailPai(List<Integer> moTailPai, int gangDice) {
        int leftMjCount = 5;
        int startIndex = 0;
        if (moTailPai.contains(0)) {
            int lastIndex = moTailPai.get(0);
            for (int i = 1; i < moTailPai.size(); i++) {
                if (moTailPai.get(i) == lastIndex + 1) {
                    lastIndex++;
                } else {
                    break;
                }
            }
            startIndex = lastIndex + 1;
        }
        if (gangDice == -1) {
            //??????????????????
            for (int i = 0, size = leftMjCount; i < size; i++) {
                int nowIndex = i + startIndex;
                if (!moTailPai.contains(nowIndex)) {
                    moTailPai.add(nowIndex);
                    break;
                }
            }

        } else {
            int duo = gangDice / 10 + gangDice % 10;
            //???????????????????????????
            for (int i = 0, j = 0; i < leftMjCount; i++) {
                int nowIndex = i + startIndex;
                if (nowIndex % 2 == 1) {
                    j++; //???????????????
                }
                if (moTailPai.contains(nowIndex)) {
                    if (nowIndex % 2 == 1) {
                        duo++;
                        leftMjCount = leftMjCount + 2;
                    }
                } else {
                    if (j == duo) {
                        moTailPai.add(nowIndex);
                        moTailPai.add(nowIndex - 1);
                        break;
                    }

                }
            }

        }
        Collections.sort(moTailPai);
        return moTailPai;
    }



    private static Integer[][] defaultArrays;

    static {
        defaultArrays = comboAllSerial(null);
    }

    /**
     *@description ?????????????????? 123,234,345,...
     *@param
     *@return
     *@author Guang.OuYang
     *@date 2019/9/26
     */
    public static Integer[][] comboAllSerial(GameModel gameModel) {
        if (defaultArrays != null) {
            return defaultArrays.clone();
        }


        int index = -1;
        int maxCombo = 7;
        //????????????????????????????????????123,234,345...
        //????????????(8+2)*2
        int maxSerial = 9;
        //??????????????????7,??????????????????9
        Integer[][] arrays = new Integer[3 * maxCombo][3];

        int basic = 10;
        for (int i = 1; i <= maxSerial; i++) {
            if (i <= maxCombo) {
                arrays[++index] = new Integer[]{i + basic, i + basic + 1, i + basic + 2};
//                arrays[++index] = new Integer[]{i + 100, i + 1 + 100, i + 2 + 100};
            }
            if (i == maxSerial && basic < 30) {
                i = 0;
                basic += 10;
            }
        }

        return arrays;
    }

    public static FindAnyComboResult findSerial(List<Mj> handCards, GameModel gameModel) {
        Integer[][] allCombo = comboAllSerial(gameModel);
        return new FindAnyCombo(allCombo).anyComboMathSizeNonEquals(handCards.stream().map(v -> v.getVal()));
    }

    /**
     * @param
     * @param removeRepeatedMath ??????????????????????????????
     * @return
     * @description ????????????????????????????????????, ???????????????O(n) ????????????10w????????????100,????????????100,?????????160ms
     * @author Guang.OuYang
     * @date 2019/9/20
     */
    public static boolean isSerialNumber(int[] array, boolean removeRepeatedMath) {
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;

        HashSet hashSet = new HashSet<>(array.length);

        int arraySum = 0;

        //??????????????????
        for (int i = 0; i < array.length; i++) {
            //?????????????????????
            if (!hashSet.contains(array[i]))
                arraySum += array[i];

            hashSet.add(array[i]);

            if (!(hashSet.size() > i)) {
                if (!removeRepeatedMath) {
//                    System.out.println("?????????????????????, ????????????");
                    return false;
                }
            }

            //?????????????????????
            if (array[i] < min) {
                min = array[i];
            }

            if (array[i] > max) {
                max = array[i];
            }
        }

        if (max == min) {
//            System.out.println("??????????????????1,??????????????????");
            return false;
        }

        int arraySize = max - min;

        //10-1=9 10-0=11
        int arrayReSize = max + 1 - min;//min == 0 || min == 1 ? arraySize + 1 : arraySize;

        //???????????????????????????, ????????????????????????????????????
        if (hashSet.size() != arrayReSize) {
//            System.out.println(min + "~" + max + "????????????:" + arrayReSize + ",??????"+(removeRepeatedMath?"???????????????":"")+"??????:" + arrRveRepeated.length);
            return false;
        }

        //????????????????????????????????????
        long sum = 0;
        for (int i = min; i <= max; i++) {
            sum += i;
        }

//        System.out.println("????????????:" + sum + ",??????:" + arraySum);
        return arraySum == sum;
    }


    /**
     * @param
     * @author Guang.OuYang
     * @description ????????????????????????[???????????????O(n - m)], ?????????200, ??????10w???, ??????:??????false,??????1ms/100???,??????:??????true,50ms/10w
     * @return
     * @date 2019/9/19
     */
    public static class FindAnyCombo {
        private List<FindAny> dataSrc = new ArrayList<>();

        /**
         * @param
         * @author Guang.OuYang
         * @description ????????????, ??????????????????
         * @return
         * @date 2019/9/19
         */
        @Data
        public class FindAny {
            //???????????????,?????????, ?????????->??????
            private HashMap<Integer, Boolean> src = new HashMap<>();
            //?????????,?????????,  ?????????->????????????
            private HashMap<Integer, Integer> srcCount = new HashMap<>();
            //???????????????,?????????,  ?????????->????????????
            private HashMap<Integer, Integer> initSrcCount = new HashMap<>();
            //?????????????????????
            private List<Integer> repeatedSrcKey = new ArrayList<>();

            public FindAny(Integer... val) {
                for (int i = 0; i < val.length; i++) {
                    src.put(val[i], false);
                    if (srcCount.get(val[i]) == null) {
                        srcCount.put(val[i], 1);
                    } else {
                        srcCount.put(val[i], srcCount.get(val[i]) + 1);
                    }

                    initSrcCount.putAll(srcCount);
                    repeatedSrcKey.add(val[i]);
                }

            }

            public boolean in(Integer number) {
                if (src.containsKey(number)) {
                    int v = srcCount.get(number) - 1;
                    srcCount.put(number, v);
                    if (v <= 0) {
                        src.put(number, true);
                    }
                    return true;
                }
                return false;
            }

            public boolean checkAllIn() {
                Iterator<Boolean> iterator = src.values().iterator();
                while (iterator.hasNext()) {
                    Boolean next = iterator.next();
                    if (!next) {
                        return false;
                    }
                }
                return true;
//                return src.values().stream().noneMatch(v -> !v.booleanValue());
            }

            public boolean clearMark() {
                srcCount.putAll(initSrcCount);
                srcCount.keySet().forEach(k -> src.put(k, false));
                return true;
            }
        }

        public FindAnyCombo(Integer[]... val) {
            for (int i = 0; i < val.length; i++) {
                if (val[i] == null) continue;
                dataSrc.add(new FindAny(val[i]));
            }
        }

        /**
         * @param
         * @return
         * @description ???????????????????????????1???
         * @author Guang.OuYang
         * @date 2019/9/20
         */
        public FindAnyComboResult anyComboMath(Stream<Integer> stream) {
            return assignComboMath(stream, 1, true);
        }

        public FindAnyComboResult anyComboMathSizeNonEquals(Stream<Integer> stream) {return assignComboMath(stream, 1, false);}

        /**
         * @param
         * @return
         * @description ?????????????????????????????????
         * @author Guang.OuYang
         * @date 2019/9/20
         */
        public FindAnyComboResult allComboMath(Stream<Integer> stream) {return assignComboMath(stream, dataSrc.size(), false); }
        public FindAnyComboResult allComboMathSizeEquals(Stream<Integer> stream) {return assignComboMath(stream, dataSrc.size(), true);}

        /**
         * @param stream         ???????????????????????????
         * @param matchComboSize ?????????????????????1???, ????????????????????????????????????
         * @param jump ?????????????????????????????????????????????
         * @return
         * @description ????????????????????????, ?????????200, ??????10w???, ??????:??????false,??????1ms/100???,??????:??????true,50ms/10w
         * @author Guang.OuYang
         * @date 2019/9/19
         */
        private FindAnyComboResult assignComboMath(Stream<Integer> stream, int matchComboSize, boolean jump) {
            FindAnyComboResult findAnyComboResult = new FindAnyComboResult();
            int i = 0;
            int iniSize = matchComboSize;//dataSrc.size();
            Iterator<Integer> iterator = stream.iterator();

            boolean find = false;
            int findCount = 0;
            co:
            while (iterator.hasNext()) {
                i++;
                Integer next = iterator.next();
                Iterator<FindAny> iterator1 = dataSrc.iterator();
                while (iterator1.hasNext()) {
                    FindAny findAny = iterator1.next();//2710
                    findAny.in(next);
                    //?????????????????????????????????check
                    find = (!jump || i % findAny.src.size() == 0) && findAny.checkAllIn();

                    //???????????????
                    if (find) {
                        findAnyComboResult.getValues().addAll(findAny.getRepeatedSrcKey());
                        findAnyComboResult.getFindCombos().add(findAny);
                        ++findCount;
                        iterator1.remove();
                    }

                    //???????????????
                    if (find = findCount == iniSize) {
                        break co;
                    }
                }
            }
            findAnyComboResult.setFind(find);
            return findAnyComboResult;
        }
    }

    @Data
    public static class FindAnyComboResult{
        private boolean find;
        private List<FindAnyCombo.FindAny> findCombos = new ArrayList<>();
        private List<Integer> values = new ArrayList<>();
    }

}
