package com.sy599.game.qipai.yjmj.tool;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage;
import com.sy599.game.GameServerConfig;
import com.sy599.game.msg.serverPacket.TableRes.YjClosingInfoRes;
import com.sy599.game.msg.serverPacket.TableRes.YjClosingPlayerInfoRes;
import com.sy599.game.qipai.yjmj.bean.YjMjHuLack;
import com.sy599.game.qipai.yjmj.bean.YjMjPlayer;
import com.sy599.game.qipai.yjmj.bean.YjMjTable;
import com.sy599.game.qipai.yjmj.bean.YjMjHu;
import com.sy599.game.qipai.yjmj.rule.*;
import com.sy599.game.util.JacksonUtil;
import com.sy599.game.util.LogUtil;

import java.util.*;
import java.util.Map.Entry;

/**
 * 沅江麻将工具类
 */
public class YjMjTool {

    @SuppressWarnings("unchecked")
    public static List<String> buildClosingInfoResLog(YjClosingInfoRes res) {
        List<String> list = new ArrayList<>();
        for (YjClosingPlayerInfoRes info : res.getClosingPlayersList()) {
            Map<String, Object> map = new HashMap<>();
            for (Entry<FieldDescriptor, Object> entry : info.getAllFields().entrySet()) {
                if (entry.getValue() instanceof List) {
                    List<Object> l = new ArrayList<>();
                    for (Object o : (List<?>) entry.getValue()) {
                        if (o instanceof String) {
                            l = (List<Object>) entry.getValue();
                            break;
                        } else if (o instanceof Integer) {
                            l = (List<Object>) entry.getValue();
                            break;
                        } else if (o instanceof Long) {
                            l = (List<Object>) entry.getValue();
                            break;
                        } else if (o instanceof GeneratedMessage) {
                            l.add(buildYjClosingInfoResOtherLog((GeneratedMessage) o));
                        }

                    }
                    map.put(entry.getKey().getName(), l);

                } else {
                    map.put(entry.getKey().getName(), entry.getValue());
                }
            }
            list.add(JacksonUtil.writeValueAsString(map));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> buildYjClosingInfoResOtherLog(GeneratedMessage res) {
        Map<String, Object> map = new HashMap<>();
        for (Entry<FieldDescriptor, Object> entry : res.getAllFields().entrySet()) {
            String name = entry.getKey().getName();
            if (!name.equals("closingPlayers")) {
                if (entry.getValue() instanceof List) {
                    List<Object> l = new ArrayList<>();
                    for (Object o : (List<?>) entry.getValue()) {
                        if (o instanceof String) {
                            l = (List<Object>) entry.getValue();
                            break;
                        } else if (o instanceof Integer) {
                            l = (List<Object>) entry.getValue();
                            break;
                        } else if (o instanceof Long) {
                            l = (List<Object>) entry.getValue();
                            break;
                        } else if (o instanceof GeneratedMessage) {
                            l.add(buildClosingInfoResOtherLog((GeneratedMessage) o));
                        }

                    }
                    map.put(entry.getKey().getName(), l);

                } else {
                    map.put(entry.getKey().getName(), entry.getValue());
                }
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> buildClosingInfoResOtherLog(GeneratedMessage res) {
        Map<String, Object> map = new HashMap<>();
        for (Entry<FieldDescriptor, Object> entry : res.getAllFields().entrySet()) {
            String name = entry.getKey().getName();
            if (!name.equals("closingPlayers")) {
                if (entry.getValue() instanceof List<?>) {
                    List<Object> l = new ArrayList<>();
                    for (Object o : (List<?>) entry.getValue()) {
                        if (o instanceof String) {
                            l = (List<Object>) entry.getValue();
                            break;
                        } else if (o instanceof Integer) {
                            l = (List<Object>) entry.getValue();
                            break;
                        } else if (o instanceof Long) {
                            l = (List<Object>) entry.getValue();
                            break;
                        } else if (o instanceof GeneratedMessage) {
                            l.add(buildClosingInfoResOtherLog((GeneratedMessage) o));
                        }

                    }
                    map.put(entry.getKey().getName(), l);

                } else {
                    map.put(entry.getKey().getName(), entry.getValue());
                }
            }
        }
        return map;
    }


    public static synchronized List<List<YjMj>> fapai(List<Integer> copy, int maxPlayerCount, List<List<Integer>> t) {
        List<List<YjMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<YjMj> pai = new ArrayList<>();
        int j = 1;

        int testcount = 0;
        if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
            for (List<Integer> zp : t) {
                list.add(MajiangHelper.find(copy, zp));
                testcount += zp.size();
            }
            if (list.size() == maxPlayerCount) {
                list.add(MajiangHelper.toMajiang(copy));
                return list;
            } else if (list.size() >= (maxPlayerCount + 1)) {
                list.get(maxPlayerCount).addAll(MajiangHelper.toMajiang(copy));
                return list;
            }
        }

        List<Integer> copy2 = new ArrayList<>(copy);
        int fapaiCount = 13 * maxPlayerCount + 1 - testcount;
        if (pai.size() >= 14) {
            list.add(pai);
            pai = new ArrayList<>();
        }

        boolean test = false;
        if (list.size() > 0) {
            test = true;
        }

        for (int i = 0; i < fapaiCount; i++) {
            // 发牌张数=13*maxPlayerCount+1 正好第一个发牌的人14张其他人13张
            YjMj majiang = YjMj.getMajang(copy.get(i));
            copy2.remove(copy.get(i));
            if (test) {
                if (i < j * 13) {
                    pai.add(majiang);
                } else {
                    list.add(pai);
                    pai = new ArrayList<>();
                    pai.add(majiang);
                    j++;
                }
            } else {
                if (i <= j * 13) {
                    pai.add(majiang);
                } else {
                    list.add(pai);
                    pai = new ArrayList<>();
                    pai.add(majiang);
                    j++;
                }
            }

        }
        list.add(pai);
        List<YjMj> left = new ArrayList<>();
        for (int i = 0; i < copy2.size(); i++) {
            left.add(YjMj.getMajang(copy2.get((i))));
        }
        list.add(left);
        return list;
    }

    public static synchronized List<List<YjMj>> fapai(List<Integer> copy, int maxPlayerCount) {
        List<List<YjMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<YjMj> pai = new ArrayList<>();
        int j = 1;

        int testcount = 0;
        List<Integer> copy2 = new ArrayList<>(copy);
        int fapaiCount = 13 * maxPlayerCount + 1 - testcount;
        if (pai.size() >= 14) {
            list.add(pai);
            pai = new ArrayList<>();
        }

        boolean test = false;
        if (list.size() > 0) {
            test = true;
        }

        for (int i = 0; i < fapaiCount; i++) {
            // 发牌张数=13*maxPlayerCount+1 正好第一个发牌的人14张其他人13张
            YjMj majiang = YjMj.getMajang(copy.get(i));
            copy2.remove(copy.get(i));
            if (test) {
                if (i < j * 13) {
                    pai.add(majiang);
                } else {
                    list.add(pai);
                    pai = new ArrayList<>();
                    pai.add(majiang);
                    j++;
                }
            } else {
                if (i <= j * 13) {
                    pai.add(majiang);
                } else {
                    list.add(pai);
                    pai = new ArrayList<>();
                    pai.add(majiang);
                    j++;
                }
            }

        }
        list.add(pai);
        List<YjMj> left = new ArrayList<>();
        for (int i = 0; i < copy2.size(); i++) {
            left.add(YjMj.getMajang(copy2.get((i))));
        }
        list.add(left);
        return list;
    }


//	/**
//	 * 麻将胡牌
//	 *
//	 * @param majiangIds
//	 * @param playType
//	 * @return
//	 */
//	public static boolean isHu(List<YjMj> majiangIds, int playType) {
//		if (playType == WanfaConstants.play_type_zhuanzhuan) {
//			return isHuZhuanzhuan(majiangIds);
//
//		} else if (playType == WanfaConstants.play_type_changesha) {
//			// return isHuChangsha(majiangIds);
//		} else if (playType == WanfaConstants.play_type_hongzhong) {
//			return isHuZhuanzhuan(majiangIds);
//		}
//
//		return false;
//	}

    /**
     * 沅江麻将胡牌
     *
     * @param player
     * @param isbegin
     * @param isGangMo
     * @return YjMjHu 0 碰碰胡  1 将将胡 2 清一色 3 七小队 4 豪华七小队 5 双豪华七小队 6三豪华七小队 7 杠爆 8 抢杠胡 9 海底捞 10 一条龙 11 门清 12 天胡 13 一字翘 14报听
     */
    public static YjMjHu isHuYuanjiang(YjMjPlayer player, boolean isbegin, boolean isGangMo) {
        List<YjMj> copyMajiangIds = new ArrayList<>(player.getHandMajiang());
        List<YjMj> gang = player.getGang();
        List<YjMj> aGangs = new ArrayList<>(player.getaGang());
        List<YjMj> peng = new ArrayList<>(player.getPeng());
        YjMjTable table = player.getPlayingTable(YjMjTable.class);
        boolean isMenQing = table.canMenQing();
        boolean isMaMaHu = table.canMaMaHu();
        YjMjHu hu = isHuYuanjiang(copyMajiangIds, gang, aGangs, peng, isbegin, isGangMo, isMenQing, isMaMaHu);
        return hu;
    }

    /**
     * 沅江麻将胡牌
     *
     * @param mjs
     * @return 0 碰碰胡  1 将将胡 2 清一色 3 七小队 4 豪华七小队 5 双豪华七小队 6三豪华七小队 7 杠爆 8 抢杠胡 9 海底捞 10 一条龙 11 门清 12 天胡 13 一字翘 14报听
     */
    public static YjMjHu isHuYuanjiang(List<YjMj> mjs, List<YjMj> gang, List<YjMj> aGangs, List<YjMj> peng, boolean isbegin, boolean isGangMo, boolean canMengQing, boolean canMaMaHu) {
        YjMjHu hu = new YjMjHu();
        if (mjs == null || mjs.isEmpty()) {
            return hu;
        }
        if (isPingHu(mjs)) {
            hu.setPingHu(true);
            hu.setHu(true);
            if (isGangMo) {
                hu.setGangBao(true);
            }
        }
        YjMjRule.checkDahu(hu, mjs, gang, aGangs, peng, canMengQing, canMaMaHu);

        if (isbegin && hu.isHu()) {
            hu.setTianHu(true);// 天胡
            hu.setDahu(true);
        }
        if (hu.isHu()) {
            hu.setShowMajiangs(mjs);
        }
        if (hu.isDahu()) {
            hu.initDahuList();
        }
        return hu;
    }

    public static boolean isPingHu(List<YjMj> majiangIds) {
        return isPingHu(majiangIds, false);
    }

    public static boolean isPingHu(List<YjMj> mjs, boolean needJiang258) {
        if (mjs == null || mjs.isEmpty()) {
            return false;
        }
        if (mjs.size() % 3 != 2) {
            return false;
        }
        List<YjMj> copy = new ArrayList<>(mjs);
        // 先去掉红中
        List<YjMj> hongzhongList = dropHongzhong(copy);

        return HuUtil.isCanHu(new ArrayList<>(copy), hongzhongList.size());

//        // 先去掉红中
//        List<YjMj> copy = new ArrayList<>(mjs);
//        List<YjMj> hongzhongList = dropHongzhong(copy);
//        YjMjIndexArr card_index = new YjMjIndexArr();
//        YjMjQipaiTool.getMax(card_index, copy);
//
//        // 拆将
//        if (chaijiang(card_index, copy, hongzhongList.size(), needJiang258)) {
//            return true;
//        } else {
//            return false;
//        }
    }

    /**
     * 红中麻将没有7小对，所以不用红中补
     *
     * @param majiangIds
     * @param card_index
     */
    public static boolean check7duizi(List<YjMj> majiangIds, YjMjIndexArr card_index, int hongzhongNum) {
        if (majiangIds.size() == 14) {
            // 7小对
            int duizi = card_index.getDuiziNum();
            if (duizi == 7) {
                return true;
            }

        } else if (majiangIds.size() + hongzhongNum == 14) {
            if (hongzhongNum == 0) {
                return false;
            }

            YjMjIndex index0 = card_index.getMajiangIndex(0);
            YjMjIndex index2 = card_index.getMajiangIndex(2);
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

    // 拆将
    public static boolean chaijiang(YjMjIndexArr card_index, List<YjMj> hasPais, int hongzhongnum, boolean needJiang258) {
        Map<Integer, List<YjMj>> jiangMap = card_index.getJiang(needJiang258);
        for (Entry<Integer, List<YjMj>> valEntry : jiangMap.entrySet()) {
            List<YjMj> copy = new ArrayList<>(hasPais);
            YjMjHuLack lack = new YjMjHuLack(hongzhongnum);
            List<YjMj> list = valEntry.getValue();
            int i = 0;
            for (YjMj majiang : list) {
                i++;
                copy.remove(majiang);
                if (i >= 2) {
                    break;
                }
            }
            lack.setHasJiang(true);
            boolean hu = chaipai(lack, copy, needJiang258);
            if (hu) {
                return hu;
            }
        }
        if (hongzhongnum > 0) {
            // 只剩下红中
            if (hasPais.isEmpty()) {
                return true;
            }
            // 没有将
            for (YjMj majiang : hasPais) {
                List<YjMj> copy = new ArrayList<>(hasPais);
                YjMjHuLack lack = new YjMjHuLack(hongzhongnum);
                boolean isJiang = false;
                if (!needJiang258) {
                    // 不需要将
                    isJiang = true;
                } else {
                    // 需要258做将
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
                    return true;
                }
                if (!lack.isHasJiang() && hu) {
                    if (lack.getHongzhongNum() == 2) {// 红中做将
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // 拆牌
    public static boolean chaipai(YjMjHuLack lack, List<YjMj> hasPais, boolean isNeedJiang258) {
        if (hasPais.isEmpty()) {
            return true;

        }
        boolean hu = chaishun(lack, hasPais, isNeedJiang258);
        if (hu)
            return true;
        return false;
    }

    public static void sortMin(List<YjMj> hasPais) {
        Collections.sort(hasPais, new Comparator<YjMj>() {

            @Override
            public int compare(YjMj o1, YjMj o2) {
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
     * 拆顺
     *
     * @param hasPais
     * @return
     */
    public static boolean chaishun(YjMjHuLack lack, List<YjMj> hasPais, boolean needJiang258) {
        if (hasPais.isEmpty()) {
            return true;
        }
        sortMin(hasPais);
        YjMj minMajiang = hasPais.get(0);
        int minVal = minMajiang.getVal();
        List<YjMj> minList = YjMjQipaiTool.getVal(hasPais, minVal);
        if (minList.size() >= 3) {
            // 先拆坎子
            hasPais.removeAll(minList.subList(0, 3));
            return chaipai(lack, hasPais, needJiang258);
        }

        // 做顺子
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
        List<YjMj> num1 = YjMjQipaiTool.getVal(hasPais, pai1);
        List<YjMj> num2 = YjMjQipaiTool.getVal(hasPais, pai2);
        List<YjMj> num3 = YjMjQipaiTool.getVal(hasPais, pai3);

        // 找到一句话的麻将
        List<YjMj> hasMajiangList = new ArrayList<>();
        if (!num1.isEmpty()) {
            hasMajiangList.add(num1.get(0));
        }
        if (!num2.isEmpty()) {
            hasMajiangList.add(num2.get(0));
        }
        if (!num3.isEmpty()) {
            hasMajiangList.add(num3.get(0));
        }

        // 一句话缺少的麻将
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

            // 做成一句话缺少2张以上的，没有将优先做将
            if (lackNum >= 2) {
                // 补坎子
                List<YjMj> count = YjMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() >= 3) {
                    hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);

                } else if (count.size() == 2) {
                    if (!lack.isHasJiang() && isCanAsJiang(count.get(0), needJiang258)) {
                        // 没有将做将
                        lack.setHasJiang(true);
                        hasPais.removeAll(count);
                        return chaipai(lack, hasPais, needJiang258);
                    }

                    // 拿一张红中补坎子
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);
                }

                // 做将
                if (!lack.isHasJiang() && isCanAsJiang(count.get(0), needJiang258) && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.setHasJiang(true);
                    hasPais.removeAll(count);
                    lack.addLack(count.get(0).getVal());
                    return chaipai(lack, hasPais, needJiang258);
                }
            } else if (lackNum == 1) {
                // 做将
                if (!lack.isHasJiang() && isCanAsJiang(minMajiang, needJiang258) && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.setHasJiang(true);
                    hasPais.remove(minMajiang);
                    lack.addLack(minMajiang.getVal());
                    return chaipai(lack, hasPais, needJiang258);
                }

                List<YjMj> count = YjMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() == 2 && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);
                }
            }

            // 如果有红中则补上
            if (lack.getHongzhongNum() >= lackNum) {
                lack.changeHongzhong(-lackNum);
                hasPais.removeAll(hasMajiangList);
                lack.addAllLack(lackList);

            } else {
                return false;
            }
        } else {
            // 可以一句话
            if (lack.getHongzhongNum() > 0) {
                List<YjMj> count1 = YjMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                List<YjMj> count2 = YjMjQipaiTool.getVal(hasPais, hasMajiangList.get(1).getVal());
                List<YjMj> count3 = YjMjQipaiTool.getVal(hasPais, hasMajiangList.get(2).getVal());
                if (count1.size() >= 2 && (count2.size() == 1 || count3.size() == 1)) {
                    List<YjMj> copy = new ArrayList<>(hasPais);
                    copy.removeAll(count1);
                    YjMjHuLack copyLack = lack.copy();
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

    public static boolean isCanAsJiang(YjMj majiang, boolean isNeed258) {
        if (isNeed258) {
            if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
                return true;
            }
            return false;
        } else {
            return true;
        }

    }

    public static List<YjMj> checkChi(List<YjMj> majiangs, YjMj dismajiang) {
        return checkChi(majiangs, dismajiang, null);
    }

    /**
     * 是否能吃
     *
     * @param majiangs
     * @param dismajiang
     * @return
     */
    public static List<YjMj> checkChi(List<YjMj> majiangs, YjMj dismajiang, List<Integer> wangValList) {
        int disMajiangVal = dismajiang.getVal();
        List<Integer> chi1 = new ArrayList<>(Arrays.asList(disMajiangVal - 2, disMajiangVal - 1));
        List<Integer> chi2 = new ArrayList<>(Arrays.asList(disMajiangVal - 1, disMajiangVal + 1));
        List<Integer> chi3 = new ArrayList<>(Arrays.asList(disMajiangVal + 1, disMajiangVal + 2));

        List<Integer> majiangIds = MajiangHelper.toMajiangVals(majiangs);
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
        return new ArrayList<YjMj>();
    }

    public static List<YjMj> findMajiangByVals(List<YjMj> majiangs, List<Integer> vals) {
        List<YjMj> result = new ArrayList<>();
        for (int val : vals) {
            for (YjMj majiang : majiangs) {
                if (majiang.getVal() == val) {
                    result.add(majiang);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 去掉红中
     *
     * @param copy
     * @return
     */
    public static List<YjMj> dropHongzhong(List<YjMj> copy) {
        List<YjMj> hongzhong = new ArrayList<>();
        Iterator<YjMj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            YjMj majiang = iterator.next();
            if (majiang.getVal() > 200) {
                hongzhong.add(majiang);
                iterator.remove();
            }
        }
        return hongzhong;
    }

    public static boolean checkWang(Object majiangs, List<Integer> wangValList) {
        if (majiangs instanceof List) {
            List<?> list = (List<?>) majiangs;
            for (Object majiang : list) {
                int val = 0;
                if (majiang instanceof YjMj) {
                    val = ((YjMj) majiang).getVal();
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
     * 相同的麻将
     *
     * @param majiangs 麻将牌
     * @param majiang  麻将
     * @param num      想要的数量
     * @return
     */
    public static List<YjMj> getSameMajiang(List<YjMj> majiangs, YjMj majiang, int num) {
        List<YjMj> hongzhong = new ArrayList<>();
        int i = 0;
        for (YjMj maji : majiangs) {
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
     * 先去某个值
     *
     * @param copy
     * @return
     */
    public static List<YjMj> dropMjId(List<YjMj> copy, int id) {
        List<YjMj> hongzhong = new ArrayList<>();
        Iterator<YjMj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            YjMj majiang = iterator.next();
            if (majiang.getId() == id) {
                hongzhong.add(majiang);
                iterator.remove();
            }
        }
        return hongzhong;
    }

    /**
     * 先去某个值
     *
     * @param copy
     * @return
     */
    public static List<YjMj> dropMjValue(List<YjMj> copy, int value) {
        List<YjMj> result = new ArrayList<>();
        Iterator<YjMj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            YjMj majiang = iterator.next();
            if (majiang.getVal() == value) {
                result.add(majiang);
                iterator.remove();
            }
        }
        return result;
    }

    /**
     * 获取听牌列表
     * cards.size+hzCount = 3n+1
     *
     * @param cardArr 去掉红中的牌
     * @param hzCount 红中数
     * @param hu7dui  是否可胡七对
     * @param jjHu    是否要检查将将胡，当摆出去的牌全是将时，
     * @return
     */
    public static List<YjMj> getLackList(int[] cardArr, int hzCount, boolean hu7dui, boolean jjHu, boolean mmHu) {
        int cardNum = 0;
        boolean isAllJiang = jjHu;
        boolean isAllMa = mmHu;
        for (int i = 0, length = cardArr.length; i < length; i++) {
            cardNum += cardArr[i];
            if (cardArr[i] > 0) {
                if (!YjMj.isJiang(HuUtil.index2MjVal(i))) {
                    isAllJiang = false;
                }
                if (!YjMj.isMa(HuUtil.index2MjVal(i))) {
                    isAllMa = false;
                }
            }
        }

        if ((cardNum + hzCount) % 3 != 1) {
            return Collections.emptyList();
        }
        List<YjMj> lackPaiList = new ArrayList<>();
        Set<Integer> have = new HashSet<>();

        if (isAllJiang) {
            lackPaiList.addAll(YjMj.allJiang);
            have.addAll(YjMj.allJiangVal);
        }

        if (isAllMa) {
            lackPaiList.addAll(YjMj.allMa);
            have.addAll(YjMj.allMaVal);
        }

        for (YjMj mj : YjMj.fullMj) {
            if (mj.isHongzhong() || have.contains(mj.getVal())) {
                continue;
            }
            int cardIndex = HuUtil.getMjIndex(mj);
            cardArr[cardIndex] = cardArr[cardIndex] + 1;
            if (hu7dui && HuUtil.isCanHu7Dui(cardArr, hzCount)) {
                lackPaiList.add(mj);
                have.add(mj.getVal());
            }
            if (HuUtil.isCanHu(cardArr, hzCount)) {
                lackPaiList.add(mj);
                have.add(mj.getVal());
            }
            cardArr[cardIndex] = cardArr[cardIndex] - 1;
        }
        if (lackPaiList.size() == 27) {
            lackPaiList.clear();
            lackPaiList.add(null);
        }
        return lackPaiList;
    }

    /**
     * 牌转化为Id
     *
     * @param phzs
     * @return
     */
    public static List<Integer> toIds(List<YjMj> phzs) {
        List<Integer> ids = new ArrayList<>();
        if (phzs == null) {
            return ids;
        }
        for (YjMj mj : phzs) {
            ids.add(mj.getId());
        }
        return ids;
    }

    public static void main(String[] args) {
        // System.out.println(findWangValList(Majiang.getMajang(9)));
        // List<List<Majiang>> list = fapai(new
        // ArrayList<>(MajiangConstants.zhuanzhuan_mjList));

        // List<Integer> majiangIds = new ArrayList<>(Arrays.asList(201, 201,
        // 201, 13, 14, 22, 23, 24, 24, 25, 35, 36, 38, 38));

        // List<Majiang> majiangs = toMajiang(majiangIds);
        //
        // List<Integer> majiangVals= toMajiangVals(majiangs);
        // System.out.println(JacksonUtil.writeValueAsString(majiangVals));
        // List<Majiang> huList = list.get(0);
        // List<Integer> huIdList = toMajiangVals(huList);
        // Collections.sort(huIdList);
        // System.out.println(JacksonUtil.writeValueAsString(list));
        // int pai = RobotAI.getInstance().outPaiHandle(0, huIdList, new
        // ArrayList<Integer>());
        // System.out.println(pai);
        // int count=0;
        // for(List<Majiang> majiangs:list){
        // count+=majiangs.size();
        // }
        // System.out.println(count);
        // for (List<Majiang> majiangs : list) {
        // // for (Majiang mjId : majiangs) {
        // // System.out.print(mjId.getVal() + " ");
        // // }
        // Card_index index = new Card_index();
        // getMax(index, majiangs);
        // System.out.println(majiangs.size() + " " + index.tostr());
        // ;
        //

        // List<Integer> a = new ArrayList<>(Arrays.asList(1, 2, 3));
        // System.out.println(a.subList(0, 3));
        List<YjMj> majiangIds = new ArrayList<>();
        // isHu(majiangs);
        // }
        // List<Integer> vals = Arrays.asList(11, 11, 23, 23, 23, 24, 24, 25,
        // 25, 26, 26);
        List<Integer> vals = Arrays.asList(27, 27, 31, 31, 32, 32, 33, 33, 34, 35, 36, 37, 38, 39);
        for (int val : vals) {
            for (YjMj majiang : YjMj.values()) {
                if (majiang.getVal() == val && !majiangIds.contains(majiang)) {
                    majiangIds.add(majiang);
                    break;
                }
            }
        }
        // majiangIds.add(Majiang.mj1);
        // majiangIds.add(Majiang.mj1);
        // majiangIds.add(Majiang.mj2);
        // majiangIds.add(Majiang.mj3);
        // majiangIds.add(Majiang.mj204);
        // List<Integer> wangValList = findWangValList(Majiang.getMajang(2));
        // List<Majiang> wangList = findWangList(majiangIds, wangValList);
        // AHMjiangHu hu = new AHMjiangHu();
        // hu.setWangMajiangList(wangList);
        // hu.setWangValList(wangValList);
        // isHuWangMajiang(majiangIds, hu);
        // AHMjiangHu hubean = isHuAHMajiang(majiangIds, new
        // ArrayList<Majiang>(), new ArrayList<Majiang>(), new
        // ArrayList<Majiang>(), new ArrayList<Majiang>(), Majiang.getMajang(2),
        // false, true, false,
        // false);
        // System.out.println(JacksonUtil.writeValueAsString(checkXiaoHu(majiangIds)));

        System.out.println(YjMj.allJiang);
        System.out.println(YjMj.allJiangVal);
    }
}
