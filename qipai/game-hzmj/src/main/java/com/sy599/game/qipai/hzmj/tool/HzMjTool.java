package com.sy599.game.qipai.hzmj.tool;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.hzmj.bean.HzMjTable;
import com.sy599.game.qipai.hzmj.rule.HzMj;
import com.sy599.game.qipai.hzmj.rule.HzMjIndex;
import com.sy599.game.qipai.hzmj.rule.HzMjIndexArr;
import com.sy599.game.qipai.hzmj.tool.hulib.util.HuUtil;
import com.sy599.game.util.JacksonUtil;

/**
 * @author lc
 */
public class HzMjTool {


    public static synchronized List<List<HzMj>> fapai(List<Integer> copy, List<List<Integer>> t) {
        List<List<HzMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<HzMj> pai = new ArrayList<>();
        int j = 1;

        int testcount = 0;
        if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
            for (List<Integer> zp : t) {
                list.add(HzMjHelper.find(copy, zp));
                testcount += zp.size();
            }

            if (list.size() == 4) {
                list.add(HzMjHelper.toMajiang(copy));
                System.out.println(JacksonUtil.writeValueAsString(list));
                return list;
            } else if (list.size() == 5) {
                return list;
            }
        }

        List<Integer> copy2 = new ArrayList<>(copy);
        int fapaiCount = 13 * 4 + 1 - testcount;
        if (pai.size() >= 14) {
            list.add(pai);
            pai = new ArrayList<>();
        }

        boolean test = false;
        if (list.size() > 0) {
            test = true;
        }

        for (int i = 0; i < fapaiCount; i++) {
            // 发牌张数=13*4+1 正好第一个发牌的人14张其他人13张
            HzMj majiang = HzMj.getMajang(copy.get(i));
            copy2.remove((Object) copy.get(i));
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
        List<HzMj> left = new ArrayList<>();
        for (int i = 0; i < copy2.size(); i++) {
            left.add(HzMj.getMajang(copy2.get((i))));
        }
        list.add(left);
        return list;
    }

    public static synchronized List<List<HzMj>> fapai(List<Integer> copy, int playerCount) {
        List<List<HzMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<HzMj> allMjs = new ArrayList<>();
        for (int id : copy) {
            allMjs.add(HzMj.getMajang(id));
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

    public static synchronized List<List<HzMj>> fapai(List<Integer> copy, int playerCount, List<List<Integer>> t) {
        List<List<HzMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<List<HzMj>> zpList = new ArrayList<>();
        if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
            for (List<Integer> zp : t) {
                zpList.add(HzMjHelper.find(copy, zp));
            }
        }
        List<HzMj> allMjs = new ArrayList<>();
        for (int id : copy) {
            allMjs.add(HzMj.getMajang(id));
        }
        int count = 0;
        for (int i = 0; i < playerCount; i++) {
            if (i == 0) {
                if (zpList.size() > 0) {
                    List<HzMj> pai = zpList.get(0);
                    int len = 14 - pai.size();
                    pai.addAll(allMjs.subList(count, len));
                    count += len;
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(0, 14)));
                }
            } else {
                if (zpList.size() > i) {
                    List<HzMj> pai = zpList.get(i);
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
                    List<HzMj> pai = zpList.get(i + 1);
                    pai.addAll(allMjs.subList(count, allMjs.size()));
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(count, allMjs.size())));
                }
            }

        }
        return list;
    }

    public static synchronized List<List<HzMj>> fapai(List<Integer> copy) {
        List<List<HzMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<HzMj> pai = new ArrayList<>();
        int j = 1;

        int testcount = 0;
        if (GameServerConfig.isDeveloper()) {
            if (list.size() == 5) {
                System.out.println(JacksonUtil.writeValueAsString(list));
                return list;
            }
        }

        List<Integer> copy2 = new ArrayList<>(copy);

        int fapaiCount = 13 * 4 + 1 - testcount;
        if (pai.size() >= 14) {
            list.add(pai);
            pai = new ArrayList<>();
        }

        boolean test = false;
        if (list.size() > 0) {
            test = true;
        }

        for (int i = 0; i < fapaiCount; i++) {
            // 发牌张数=13*4+1 正好第一个发牌的人14张其他人13张
            HzMj majiang = HzMj.getMajang(copy.get(i));
            copy2.remove((Object) copy.get(i));
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
        List<HzMj> left = new ArrayList<>();
        for (int i = 0; i < copy2.size(); i++) {
            left.add(HzMj.getMajang(copy2.get((i))));
        }
        list.add(left);
        return list;
    }

    /**
     * 长沙麻将胡牌
     *
     * @param majiangIds
     * @return 0平胡 1 碰碰胡 2将将胡 3清一色 4双豪华7小对 5豪华小对 6:7小对 7全求人 8大四喜 9板板胡 10缺一色
     * 11六六顺
     */
//	public static CSMjiangHu isHuChangsha(List<HzMj> majiangIds, List<HzMj> gang, List<HzMj> peng, List<HzMj> chi, List<HzMj> buzhang, boolean isbegin) {
//		CSMjiangHu hu = new CSMjiangHu();
//		if (majiangIds == null || majiangIds.isEmpty()) {
//			return hu;
//		}
//
//		if (isPingHu(majiangIds)) {
//			hu.setPingHu(true);
//			hu.setHu(true);
//			hu.setXiaohu(true);
//		}
//
//		ChangshaMajiangRule.checkDahu(hu, majiangIds, gang, peng, chi, buzhang);
//		if (isbegin) {
//			ChangshaMajiangRule.checkXiaoHu(hu, majiangIds);
//		}
//		if (hu.isHu()) {
//			hu.setShowMajiangs(majiangIds);
//		}
//		if (hu.isDahu()) {
//			hu.initDahuList();
//		}
//		return hu;
//	}
    public static boolean isPingHu(List<HzMj> majiangIds) {
        return isPingHu(majiangIds, true);

    }

    public static boolean isPingHu(List<HzMj> majiangIds, boolean needJiang258) {
        if (majiangIds == null || majiangIds.isEmpty()) {
            return false;
        }
        if (majiangIds.size() % 3 != 2) {
            System.out.println("%3！=2");
            return false;

        }

        // 先去掉红中
        List<HzMj> copy = new ArrayList<>(majiangIds);
        List<HzMj> hongzhongList = dropHongzhong(copy);

        HzMjIndexArr card_index = new HzMjIndexArr();
        HzMjQipaiTool.getMax(card_index, copy);
        // 拆将
        if (chaijiang(card_index, copy, hongzhongList.size(), needJiang258)) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isHu(List<HzMj> mjs, HzMjTable table) {
    	
    	int hongZhongCount = HzMjQipaiTool.getMajiangCount(mjs, HzMj.getHongZhongVal());
        if (hongZhongCount >= 4&& table.isSiBaHZ()) {
            // 4张红中直接胡
            return true;
        }
    	return isHu(mjs, table.isHu7dui());
    	
    }

    /**
     * 麻将胡牌
     *
     * @param mjs
     * @return
     */
    public static boolean isHu(List<HzMj> mjs, boolean hu7dui) {
        if (mjs == null || mjs.isEmpty()) {
            return false;
        }

        List<HzMj> copy = new ArrayList<>(mjs);
        // 先去掉红中
        List<HzMj> hongzhongList = dropHongzhong(copy);
//        if (hongzhongList.size() == 4) {
//            // 4张红中直接胡
//            return true;
//        }
        if (mjs.size() % 3 != 2) {
            return false;
        }
        HzMjIndexArr card_index = new HzMjIndexArr();
        HzMjQipaiTool.getMax(card_index, copy);
        if (hu7dui && check7duizi(copy, card_index, hongzhongList.size())) {
            return true;
        }
        return HuUtil.isCanHu(new ArrayList<>(copy), hongzhongList.size());
        // 拆将
//        if (chaijiang(card_index, copy, hongzhongList.size(), false)) {
//            return true;
//        } else {
//            return false;
//        }

    }
    
    /**
     * 是否是清一色
     * @param allMajiangs
     * @return
     */
    public static boolean isQingyise(List<HzMj> allMajiangs){
    	boolean qingyise = false;
		int se = 0;
		for (HzMj mjiang : allMajiangs) {
			if (mjiang.isHongzhong()) {
				continue;
			}
			if (se == 0) {
				qingyise = true;
				se = mjiang.getHuase();
				continue;
			}

			if (mjiang.getHuase() != se) {
				qingyise = false;
				break;
			}
		}
		return qingyise;
    }
    
    /***
     * 胡七对
     * @param allMajiangs
     * @return
     */
    public static boolean isHuQidui(List<HzMj> allMajiangs){
         // 先去掉红中
         List<HzMj> hongzhongList = dropHongzhong(allMajiangs);
         HzMjIndexArr card_index = new HzMjIndexArr();
         HzMjQipaiTool.getMax(card_index, allMajiangs);
         if (check7duizi(allMajiangs, card_index, hongzhongList.size())) {
             return true;
         }
         return false;
    }
    
    
    
    /**
     * 碰碰胡
     * @param majiangIds
     * @return
     */
    public static boolean isPengPengHu(List<HzMj> majiangIds) {
		HzMjIndexArr all_card_index = new HzMjIndexArr();
		HzMjQipaiTool.getMax(all_card_index, majiangIds);
		
		
		HzMjIndex index4 = all_card_index.getMajiangIndex(3);
		HzMjIndex index3 = all_card_index.getMajiangIndex(2);
		HzMjIndex index2 = all_card_index.getMajiangIndex(1);
		HzMjIndex index1 = all_card_index.getMajiangIndex(0);

		int sameCount = 0;
		if (index4 != null) {
			sameCount += index4.getLength();
		}
		if (index3 != null) {
			sameCount += index3.getLength();
		}
		
		// 3个相同或者4个相同有4个
		if (sameCount == 4 && index2 != null && index2.getLength() == 1 ) {
			return true;
		} 
		else if (majiangIds.contains(HzMj.getMajang(201)) && index4 != null && index4.getLength() == 2 && index1 != null && index1.getLength() == 1 && index2 == null) {
			return true;
		} else if (majiangIds.contains(HzMj.getMajang(201)) && sameCount >= 3 && index2 == null && index1 != null && index1.getLength() == 2) {
			return true;
		} else if (majiangIds.contains(HzMj.getMajang(201)) && sameCount >= 2 && index2 != null && index2.getLength() == 2 && index1 != null && index1.getLength() == 1) {
			return true;
		}
		return false;
	}
    
    

    /**
     * 是否胡七对
     *
     * @param mjs     去掉红中的麻将
     * @param hzCount
     * @return
     */
    public static boolean isHu7Dui(List<HzMj> mjs, int hzCount) {
        HzMjIndexArr card_index = new HzMjIndexArr();
        HzMjQipaiTool.getMax(card_index, mjs);
        return check7duizi(mjs, card_index, hzCount);
    }

    public static boolean isTing(List<HzMj> majiangIds, boolean hu7dui) {
        if (majiangIds == null || majiangIds.isEmpty()) {
            return false;
        }
        List<HzMj> copy = new ArrayList<>(majiangIds);
        // 先去掉红中
        List<HzMj> hongzhongList = dropHongzhong(copy);
        if (majiangIds.size() % 3 != 1) {
            return false;
        }
        HzMjIndexArr card_index = new HzMjIndexArr();
        HzMjQipaiTool.getMax(card_index, copy);
        if (hu7dui && check7duizi(copy, card_index, hongzhongList.size() + 1)) {
            return true;
        }
        return HuUtil.isCanHu(copy, hongzhongList.size() + 1);

        // 拆将
//        if (chaijiang(card_index, copy, hongzhongList.size() + 1, false)) {
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
    public static boolean check7duizi(List<HzMj> majiangIds, HzMjIndexArr card_index, int hongzhongNum) {
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

            HzMjIndex index0 = card_index.getMajiangIndex(0);
            HzMjIndex index2 = card_index.getMajiangIndex(2);
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
    public static boolean chaijiang(HzMjIndexArr card_index, List<HzMj> hasPais, int hongzhongnum, boolean needJiang258) {
        Map<Integer, List<HzMj>> jiangMap = card_index.getJiang(needJiang258);
        for (Entry<Integer, List<HzMj>> valEntry : jiangMap.entrySet()) {
            List<HzMj> copy = new ArrayList<>(hasPais);
            HzMjHuLack lack = new HzMjHuLack(hongzhongnum);
            List<HzMj> list = valEntry.getValue();
            int i = 0;
            for (HzMj majiang : list) {
                i++;
                copy.remove(majiang);
                if (i >= 2) {
                    break;
                }
            }
            lack.setHasJiang(true);
            boolean hu = chaipai(lack, copy, needJiang258);
            if (hu) {
                System.out.println(JacksonUtil.writeValueAsString(lack));
                return hu;
            }
        }

        if (hongzhongnum > 0) {
            // 只剩下红中
            if (hasPais.isEmpty()) {
                return true;
            }
            // 没有将
            for (HzMj majiang : hasPais) {
                List<HzMj> copy = new ArrayList<>(hasPais);
                HzMjHuLack lack = new HzMjHuLack(hongzhongnum);
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
                    System.out.println(JacksonUtil.writeValueAsString(lack));
                    return true;
                }
                if (!lack.isHasJiang() && hu) {
                    if (lack.getHongzhongNum() == 2) {
                        // 红中做将
                        System.out.println(JacksonUtil.writeValueAsString(lack));
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // 拆牌
    public static boolean chaipai(HzMjHuLack lack, List<HzMj> hasPais, boolean isNeedJiang258) {
        if (hasPais.isEmpty()) {
            return true;

        }
        boolean hu = chaishun(lack, hasPais, isNeedJiang258);
        if (hu)
            return true;
        return false;
    }

    public static void sortMin(List<HzMj> hasPais) {
        Collections.sort(hasPais, new Comparator<HzMj>() {

            @Override
            public int compare(HzMj o1, HzMj o2) {
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
    public static boolean chaishun(HzMjHuLack lack, List<HzMj> hasPais, boolean needJiang258) {
        if (hasPais.isEmpty()) {
            return true;
        }
        sortMin(hasPais);
        HzMj minMajiang = hasPais.get(0);
        int minVal = minMajiang.getVal();
        List<HzMj> minList = HzMjQipaiTool.getVal(hasPais, minVal);
        if (minList.size() >= 3) {
            // 先拆坎子
            removeAllPai(hasPais, minList.subList(0, 3));
            //hasPais.removeAll(minList.subList(0, 3));
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
        List<HzMj> num1 = HzMjQipaiTool.getVal(hasPais, pai1);
        List<HzMj> num2 = HzMjQipaiTool.getVal(hasPais, pai2);
        List<HzMj> num3 = HzMjQipaiTool.getVal(hasPais, pai3);

        // 找到一句话的麻将
        List<HzMj> hasMajiangList = new ArrayList<>();
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
                List<HzMj> count = HzMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() >= 3) {
                    removeAllPai(hasPais, count);
                    //hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);

                } else if (count.size() == 2) {
                    if (!lack.isHasJiang() && isCanAsJiang(count.get(0), needJiang258)) {
                        // 没有将做将
                        lack.setHasJiang(true);
                        removeAllPai(hasPais, count);
                        //hasPais.removeAll(count);
                        return chaipai(lack, hasPais, needJiang258);
                    }

                    // 拿一张红中补坎子
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    removeAllPai(hasPais, count);
                    //hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);
                }

                // 做将
                if (!lack.isHasJiang() && isCanAsJiang(count.get(0), needJiang258) && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.setHasJiang(true);
                    removeAllPai(hasPais, count);
                    //hasPais.removeAll(count);
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

                List<HzMj> count = HzMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() == 2 && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    removeAllPai(hasPais, count);
                    //hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);
                }
            }

            // 如果有红中则补上
            if (lack.getHongzhongNum() >= lackNum) {
                lack.changeHongzhong(-lackNum);
                removeAllPai(hasPais, hasMajiangList);
                //hasPais.removeAll(hasMajiangList);
                lack.addAllLack(lackList);

            } else {
                return false;
            }
        } else {
            // 可以一句话
            if (lack.getHongzhongNum() > 0) {
                List<HzMj> count1 = HzMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                List<HzMj> count2 = HzMjQipaiTool.getVal(hasPais, hasMajiangList.get(1).getVal());
                List<HzMj> count3 = HzMjQipaiTool.getVal(hasPais, hasMajiangList.get(2).getVal());
                if (count1.size() >= 2 && (count2.size() == 1 || count3.size() == 1)) {
                    List<HzMj> copy = new ArrayList<>(hasPais);
                    removeAllPai(copy, count1);
                    //copy.removeAll(count1);
                    HzMjHuLack copyLack = lack.copy();
                    copyLack.changeHongzhong(-1);

                    copyLack.addLack(hasMajiangList.get(0).getVal());
                    if (chaipai(copyLack, copy, needJiang258)) {
                        return true;
                    }
                }
            }
            removeAllPai(hasPais, hasMajiangList);
            //hasPais.removeAll(hasMajiangList);
        }
        return chaipai(lack, hasPais, needJiang258);
    }

    public static void removeAllPai(List<HzMj> hasPais, List<HzMj> remPai) {
        for (HzMj majiang : remPai) {
            hasPais.remove(majiang);
        }
    }

    public static boolean isCanAsJiang(HzMj majiang, boolean isNeed258) {
        if (isNeed258) {
            if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
                return true;
            }
            return false;
        } else {
            return true;
        }

    }

    public static List<HzMj> checkChi(List<HzMj> majiangs, HzMj dismajiang) {
        return checkChi(majiangs, dismajiang, null);
    }

    /**
     * 是否能吃
     *
     * @param majiangs
     * @param dismajiang
     * @return
     */
    public static List<HzMj> checkChi(List<HzMj> majiangs, HzMj dismajiang, List<Integer> wangValList) {
        int disMajiangVal = dismajiang.getVal();
        List<Integer> chi1 = new ArrayList<>(Arrays.asList(disMajiangVal - 2, disMajiangVal - 1));
        List<Integer> chi2 = new ArrayList<>(Arrays.asList(disMajiangVal - 1, disMajiangVal + 1));
        List<Integer> chi3 = new ArrayList<>(Arrays.asList(disMajiangVal + 1, disMajiangVal + 2));

        List<Integer> majiangIds = HzMjHelper.toMajiangVals(majiangs);
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
        return new ArrayList<HzMj>();
    }

    public static List<HzMj> findMajiangByVals(List<HzMj> majiangs, List<Integer> vals) {
        List<HzMj> result = new ArrayList<>();
        for (int val : vals) {
            for (HzMj majiang : majiangs) {
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
    public static List<HzMj> dropHongzhong(List<HzMj> copy) {
        List<HzMj> hongzhong = new ArrayList<>();
        Iterator<HzMj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            HzMj majiang = iterator.next();
            if (majiang.getVal() > 200) {
                hongzhong.add(majiang);
                iterator.remove();
            }
        }
        return hongzhong;
    }

    public static boolean checkWang(Object majiangs, List<Integer> wangValList) {
        if (majiangs instanceof List) {
            List list = (List) majiangs;
            for (Object majiang : list) {
                int val = 0;
                if (majiang instanceof HzMj) {
                    val = ((HzMj) majiang).getVal();
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
    public static List<HzMj> getSameMajiang(List<HzMj> majiangs, HzMj majiang, int num) {
        List<HzMj> hongzhong = new ArrayList<>();
        int i = 0;
        for (HzMj maji : majiangs) {
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
    public static List<HzMj> dropMjId(List<HzMj> copy, int id) {
        List<HzMj> hongzhong = new ArrayList<>();
        Iterator<HzMj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            HzMj majiang = iterator.next();
            if (majiang.getId() == id) {
                hongzhong.add(majiang);
                iterator.remove();
            }
        }
        return hongzhong;
    }

    public static List<HzMj> getPais(String paisStr) {
        String[] pais = paisStr.split(",");
        List<HzMj> handPais = new ArrayList<>();
        for (String pai : pais) {
            for (HzMj mj : HzMj.values()) {
                if (mj.getVal() == Integer.valueOf(pai) && !handPais.contains(mj)) {
                    handPais.add(mj);
                    break;
                }
            }
        }
        return handPais;
    }

    /**
     * 获取听牌列表
     * cards.size+hzCount = 3n+1
     *
     * @param cards   去掉红中的牌
     * @param hzCount 红中数
     * @param hu7dui  是否可胡七对
     * @return
     */
    public static List<HzMj> getLackListOld(List<HzMj> cards, int hzCount, boolean hu7dui) {
        if ((cards.size() + hzCount) % 3 != 1) {
            return Collections.emptyList();
        }
        List<HzMj> lackPaiList = HuUtil.getLackPaiList(cards, hzCount);
        if (lackPaiList.size() == 1 && null == lackPaiList.get(0)) {
            // 听所有
            return lackPaiList;
        }
        Set<Integer> have = new HashSet<>();
        if (lackPaiList.size() > 0) {
            Iterator<HzMj> iterator = lackPaiList.iterator();
            int lastIndex = cards.size();
            HzMj mj;
            while (iterator.hasNext()) {
                mj = iterator.next();
                // 去重
                if (have.contains(mj.getVal())) {
                    iterator.remove();
                } else {
                    have.add(mj.getVal());

                    //检验
                    cards.add(mj);
                    if (!HuUtil.isCanHu(cards, hzCount)) {
                        iterator.remove();
                    }
                    cards.remove(lastIndex);
                }
            }
            if (lackPaiList.size() == 27) {
                lackPaiList.clear();
                lackPaiList.add(null);
            }
        }
        if (hu7dui && cards.size() + hzCount + 1 == 14) {
            //检查七小对
            if (HzMjTool.isHu7Dui(cards, hzCount + 1)) {
                int rmIndex = cards.size();
                for (HzMj mj : HzMj.fullMj) {
                    if (mj.isHongzhong()|| have.contains(mj.getVal())) {
                        continue;
                    }
                    cards.add(mj);
                    if (HzMjTool.isHu7Dui(cards, hzCount)) {
                        lackPaiList.add(mj);
                        have.add(mj.getVal());
                    }
                    cards.remove(rmIndex);
                }
            }
        }


        return lackPaiList;
    }

    /**
     * 获取听牌列表
     * cards.size+hzCount = 3n+1
     *
     * @param cardArr 去掉红中的牌
     * @param hzCount 红中数
     * @param hu7dui  是否可胡七对
     * @return
     */
    public static List<HzMj> getLackList(int[] cardArr, int hzCount, boolean hu7dui) {
        int cardNum = 0;
        for (int i = 0, length = cardArr.length; i < length; i++) {
            cardNum += cardArr[i];
        }
        if ((cardNum + hzCount) % 3 != 1) {
            return Collections.emptyList();
        }
        List<HzMj> lackPaiList = new ArrayList<>();
        Set<Integer> have = new HashSet<>();
        for (HzMj mj : HzMj.fullMj) {
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


    public static void main(String[] args) {
        HuUtil.init();
        int laiZiVal = 331;
        int laiZiNum = 1;
        String paisStr = "331,331,331,11,12,13,21,22,23,31,32,33,39,39";
        paisStr = "33,33,33,36,36,36,38,38,39,39";
        paisStr = "37,37,15,16,17,24,24,25,25";
//        paisStr = "38,39,11,11,11,15,16,17,17,18,19,26";
        paisStr = "36,36,37,37,15,16,16,17,17,18,25,25";
        paisStr = "31,37,37,37,12,14,16,17,17,19,24,24,25";
        List<HzMj> handPais = getPais(paisStr);
        System.out.println(handPais);
        int count = 1;
        boolean canHu = false;
        long start = Clock.systemDefaultZone().millis();

        for (int i = 0; i < count; i++) {
            canHu = isHu(handPais, true);
        }
        long timeUse = Clock.systemDefaultZone().millis() - start;
        System.out.println("canHu = " + canHu + " , count = " + count + " , timeUse = " + timeUse + " ms");


        canHu = false;
        start = Clock.systemDefaultZone().millis();
        for (int i = 0; i < count; i++) {
            canHu = HuUtil.isCanHu(handPais, laiZiNum);
        }
        timeUse = Clock.systemDefaultZone().millis() - start;
        System.out.println("canHu = " + canHu + " , count = " + count + " , timeUse = " + timeUse + " ms");


        count = 1;
        List<HzMj> lackPaiList = new ArrayList<>();
        start = Clock.systemDefaultZone().millis();
        for (int i = 0; i < count; i++) {
            lackPaiList = getLackListOld(handPais, laiZiNum, true);
        }
        timeUse = Clock.systemDefaultZone().millis() - start;
        System.out.println("---count = " + count + " , timeUse = " + timeUse + " ms" + "tingList : " + lackPaiList);

        start = Clock.systemDefaultZone().millis();
        int[] cardArr = HuUtil.toCardArray(handPais);
        for (int i = 0; i < count; i++) {
            lackPaiList = getLackList(cardArr, laiZiNum, true);
        }
        timeUse = Clock.systemDefaultZone().millis() - start;
        System.out.println("---count = " + count + " , timeUse = " + timeUse + " ms" + "tingList : " + lackPaiList);

    }
}
