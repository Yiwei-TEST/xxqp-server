package com.sy599.game.qipai.zhengzmj.tool;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.zhengzmj.bean.ZhengzMjCardDisType;
import com.sy599.game.qipai.zhengzmj.bean.ZhengzMjDisAction;
import com.sy599.game.qipai.zhengzmj.bean.ZhengzMjPlayer;
import com.sy599.game.qipai.zhengzmj.bean.ZhengzMjTable;
import com.sy599.game.qipai.zhengzmj.constant.ZhengzMjConstants;
import com.sy599.game.qipai.zhengzmj.rule.ZhengzMjIndexArr;
import com.sy599.game.qipai.zhengzmj.rule.ZhengzMj;
import com.sy599.game.qipai.zhengzmj.rule.ZhengzMjIndex;
import com.sy599.game.qipai.zhengzmj.tool.hulib.util.HuUtil;
import com.sy599.game.util.JacksonUtil;

/**
 * @author lc
 */
public class ZhengzMjTool {


    public static synchronized List<List<ZhengzMj>> fapai(List<Integer> copy, List<List<Integer>> t) {
        List<List<ZhengzMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<ZhengzMj> pai = new ArrayList<>();
        int j = 1;

        int testcount = 0;
        if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
            for (List<Integer> zp : t) {
                list.add(ZhengzMjHelper.find(copy, zp));
                testcount += zp.size();
            }

            if (list.size() == 4) {
                list.add(ZhengzMjHelper.toMajiang(copy));
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
            ZhengzMj majiang = ZhengzMj.getMajang(copy.get(i));
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
        List<ZhengzMj> left = new ArrayList<>();
        for (int i = 0; i < copy2.size(); i++) {
            left.add(ZhengzMj.getMajang(copy2.get((i))));
        }
        list.add(left);
        return list;
    }

    public static synchronized List<List<ZhengzMj>> fapai(List<Integer> copy, int playerCount) {
        List<List<ZhengzMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<ZhengzMj> allMjs = new ArrayList<>();
        for (int id : copy) {
            allMjs.add(ZhengzMj.getMajang(id));
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

    public static synchronized List<List<ZhengzMj>> fapai(List<Integer> copy, int playerCount, List<List<Integer>> t) {
        List<List<ZhengzMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<List<ZhengzMj>> zpList = new ArrayList<>();
        if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
            for (List<Integer> zp : t) {
                zpList.add(ZhengzMjHelper.find(copy, zp));
            }
        }
        List<ZhengzMj> allMjs = new ArrayList<>();
        for (int id : copy) {
            allMjs.add(ZhengzMj.getMajang(id));
        }
        int count = 0;
        for (int i = 0; i < playerCount; i++) {
            if (i == 0) {
                if (zpList.size() > 0) {
                    List<ZhengzMj> pai = zpList.get(0);
                    int len = 14 - pai.size();
                    pai.addAll(allMjs.subList(count, len));
                    count += len;
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(0, 14)));
                }
            } else {
                if (zpList.size() > i) {
                    List<ZhengzMj> pai = zpList.get(i);
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
                    List<ZhengzMj> pai = zpList.get(i + 1);
                    pai.addAll(allMjs.subList(count, allMjs.size()));
                    list.add(new ArrayList<>(pai));
                } else {
                    list.add(new ArrayList<>(allMjs.subList(count, allMjs.size())));
                }
            }

        }
        return list;
    }

    public static synchronized List<List<ZhengzMj>> fapai(List<Integer> copy) {
        List<List<ZhengzMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<ZhengzMj> pai = new ArrayList<>();
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
            ZhengzMj majiang = ZhengzMj.getMajang(copy.get(i));
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
        List<ZhengzMj> left = new ArrayList<>();
        for (int i = 0; i < copy2.size(); i++) {
            left.add(ZhengzMj.getMajang(copy2.get((i))));
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
//	public static CSMjiangHu isHuChangsha(List<ZhengzMj> majiangIds, List<ZhengzMj> gang, List<ZhengzMj> peng, List<ZhengzMj> chi, List<ZhengzMj> buzhang, boolean isbegin) {
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
//    public static boolean isPingHu(List<ZhengzMj> majiangIds) {
//        return isPingHu(majiangIds, true);
//
//    }

    public static boolean isPingHu(List<ZhengzMj> majiangIds, int wangVal) {
        if (majiangIds == null || majiangIds.isEmpty()) {
            return false;
        }
        if (majiangIds.size() % 3 != 2) {
            System.out.println("%3！=2");
            return false;

        }

        // 先去掉红中
        List<ZhengzMj> copy = new ArrayList<>(majiangIds);
        List<ZhengzMj> hongzhongList = dropWangBa(copy,wangVal);

        ZhengzMjIndexArr card_index = new ZhengzMjIndexArr();
        ZhengzMjQipaiTool.getMax(card_index, copy);
        // 拆将
        if (chaijiang(card_index, copy, hongzhongList.size(), false)) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isHu(List<ZhengzMj> mjs, ZhengzMjTable table) {
    	
    	//int wangCount = ZhengzMjQipaiTool.getMajiangCount(mjs, table.getWangMjVal());
//        if (hongZhongCount >= 4&& table.isSiBaHZ()) {
//            // 4张红中直接胡
//            return true;
//        }
    	
    	return isHu(mjs, true,table,0);
    	
    }
    
 public static boolean disIsHu(List<ZhengzMj> mjs, ZhengzMjTable table,int moWang) {
//    	int wangCount = ZhengzMjQipaiTool.getMajiangCount(mjs, table.getWangMjVal());
////        if (hongZhongCount >= 4&& table.isSiBaHZ()) {
////            // 4张红中直接胡
////            return true;
////        }
//    	
    	return isHu(mjs, true,table,moWang);
    	
    }

    /**
     * 麻将胡牌
     *
     * @param mjs
     * @return
     */
    public static boolean isHu(List<ZhengzMj> mjs, boolean hu7dui,ZhengzMjTable table,int disWang) {
        return isHuNew(mjs, hu7dui, table, disWang);
        // 拆将
//        if (chaijiang(card_index, copy, hongzhongList.size(), false)) {
//            return true;
//        } else {
//            return false;
//        }

    }

	public static boolean isHuNew(List<ZhengzMj> mjs, boolean hu7dui, ZhengzMjTable table, int disWang) {
		if (mjs == null || mjs.isEmpty()) {
            return false;
        }
        List<ZhengzMj> copy = new ArrayList<>(mjs);
        
        // 先去掉王
        List<ZhengzMj> wangs = dropWangBa(copy,table.getWangMjVal());
        List<ZhengzMj> wangs2 = dropWangBa(copy,table.getWangMjVal2());
        
        if(disWang==table.getWangMjVal()&&!wangs.isEmpty()){
        	copy.add(wangs.remove(0));
        }else if(disWang==table.getWangMjVal2()&&!wangs2.isEmpty()){
        	copy.add(wangs2.remove(0));
        }
        
        
        //4混直接胡牌
        if(table.getHunPaiNum()==1&&wangs.size()==4&&disWang==0){
        	return true;
        }
        
//        if (hongzhongList.size() == 4) {
//            // 4张红中直接胡
//            return true;
//        }
        if (mjs.size() % 3 != 2) {
            return false;
        }
        
       // int hzCount = ZhengzMjTool.haveHongzhong(copy);
        if(hu7dui){
        	 boolean qidui = checkHuQdui(table, mjs);
             if(qidui){
             	return true;
             }
        }
        
        boolean isHu= HuUtil.isCanHu(copy, wangs.size()+wangs2.size());
        
        return isHu;
	}
    
    
    
    
    

	public static boolean checkHuQdui(ZhengzMjTable table, List<ZhengzMj> copy) {
		ZhengzMjIndexArr card_index = new ZhengzMjIndexArr();
        ZhengzMjQipaiTool.getMax(card_index, copy);
        	if(check7duizi(copy, card_index)){
        		return true;
        	}
//        	if(hzCount>0&&table.getWangMjVal()<200){
//        		//有红中再变下牌
//            	for(int i=1;i<=hzCount;i++){
//            		 List<ZhengzMj> cards2 = new ArrayList<>(copy);
//            		ZhengzMjTool.dropHzhong(cards2, i);
//            		cards2.addAll(ZhengzMj.getSameMjList(i, table.getWangMjVal(),cards2));
//            		if(check7duizi(cards2, card_index, wangCount)){
//            			table.setHuHzRepCount(i);
//                		return true;
//                	}
//            	}
//        	}
        	return false;
	}
	
	
	
	public static boolean checkHuQduiTing(ZhengzMjTable table, List<ZhengzMj> copy) {
		ZhengzMjIndexArr card_index = new ZhengzMjIndexArr();
        ZhengzMjQipaiTool.getMax(card_index, copy);
        	
        int duizi = card_index.getDuiziNum();
        if(duizi==7){
        	return true;
        }
//        	if(hzCount>0&&table.getWangMjVal()<200){
//        		//有红中再变下牌
//            	for(int i=1;i<=hzCount;i++){
//            		 List<ZhengzMj> cards2 = new ArrayList<>(copy);
//            		ZhengzMjTool.dropHzhong(cards2, i);
//            		cards2.addAll(ZhengzMj.getSameMjList(i, table.getWangMjVal(),cards2));
//            		if(check7duizi(cards2, card_index, wangCount)){
//            			table.setHuHzRepCount(i);
//                		return true;
//                	}
//            	}
//        	}
        	return false;
	}
	
    
    /**
     * 是否是清一色
     * @param allMajiangs
     * @return
     */
    public static boolean isQingyise(List<ZhengzMj> allMajiangs,int wangVal){
    	boolean qingyise = false;
		int se = 0;
		for (ZhengzMj mjiang : allMajiangs) {
			//mjiang.isHongzhong()
			if (mjiang.getVal()==wangVal) {
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
    public static boolean isHuQidui(List<ZhengzMj> allMajiangs,int wangVal){
         ZhengzMjIndexArr card_index = new ZhengzMjIndexArr();
         ZhengzMjQipaiTool.getMax(card_index, allMajiangs);
         if (check7duizi(allMajiangs, card_index)) {
             return true;
         }
         return false;
    }
    
    
    
    /**
     * 碰碰胡
     * @param majiangIds
     * @return
     */
    public static boolean isPengPengHu(List<ZhengzMj> majiangIds,int wangCount) {
		ZhengzMjIndexArr all_card_index = new ZhengzMjIndexArr();
		ZhengzMjQipaiTool.getMax(all_card_index, majiangIds);
		
		
		ZhengzMjIndex index4 = all_card_index.getMajiangIndex(3);
		ZhengzMjIndex index3 = all_card_index.getMajiangIndex(2);
		ZhengzMjIndex index2 = all_card_index.getMajiangIndex(1);
		ZhengzMjIndex index1 = all_card_index.getMajiangIndex(0);

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
		else if (wangCount>0 && index4 != null && index4.getLength() == 2 && index1 != null && index1.getLength() == 1 && index2 == null) {
			return true;
		} else if (wangCount>0 && sameCount >= 3 && index2 == null && index1 != null && index1.getLength() == 2) {
			return true;
		} else if (wangCount>0 && sameCount >= 2 && index2 != null && index2.getLength() == 2 && index1 != null && index1.getLength() == 1) {
			return true;
		}else if(wangCount>0 && sameCount > 3 && index2 == null && index1 != null && index1.getLength() == 1){
			return true;
		}else if(wangCount>0 && sameCount == 3 && index2 != null  && index2.getLength() == 2){
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
    public static boolean isHu7Dui(List<ZhengzMj> mjs, int hzCount) {
        ZhengzMjIndexArr card_index = new ZhengzMjIndexArr();
        ZhengzMjQipaiTool.getMax(card_index, mjs);
        return check7duizi(mjs, card_index);
    }

    public static boolean isTing(List<ZhengzMj> majiangIds, boolean hu7dui,int wangVal) {
        if (majiangIds == null || majiangIds.isEmpty()) {
            return false;
        }
        List<ZhengzMj> copy = new ArrayList<>(majiangIds);
        // 先去掉红中
        List<ZhengzMj> hongzhongList = dropWangBa(copy,wangVal);
        if (majiangIds.size() % 3 != 1) {
            return false;
        }
        ZhengzMjIndexArr card_index = new ZhengzMjIndexArr();
        ZhengzMjQipaiTool.getMax(card_index, copy);
        if (hu7dui && check7duizi(copy, card_index)) {
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
    public static boolean check7duizi(List<ZhengzMj> majiangIds, ZhengzMjIndexArr card_index) {
        if (majiangIds.size() == 14) {
            // 7小对
            int duizi = card_index.getDuiziNum();
            if (duizi == 7) {
                return true;
            }

        } 
        
//        
//        else if (majiangIds.size()  == 14) {
//            if (hongzhongNum == 0) {
//                return false;
//            }
//
//            ZhengzMjIndex index0 = card_index.getMajiangIndex(0);
//            ZhengzMjIndex index2 = card_index.getMajiangIndex(2);
//            int lackNum = index0 != null ? index0.getLength() : 0;
//            lackNum += index2 != null ? index2.getLength() : 0;
//
//            if (lackNum <= hongzhongNum) {
//                return true;
//            }
//
//            if (lackNum == 0) {
//                lackNum = 14 - majiangIds.size();
//                if (lackNum == hongzhongNum) {
//                    return true;
//                }
//            }
//
//        }
        return false;
    }

    // 拆将
    public static boolean chaijiang(ZhengzMjIndexArr card_index, List<ZhengzMj> hasPais, int hongzhongnum, boolean needJiang258) {
        Map<Integer, List<ZhengzMj>> jiangMap = card_index.getJiang(needJiang258);
        for (Entry<Integer, List<ZhengzMj>> valEntry : jiangMap.entrySet()) {
            List<ZhengzMj> copy = new ArrayList<>(hasPais);
            ZhengzMjHuLack lack = new ZhengzMjHuLack(hongzhongnum);
            List<ZhengzMj> list = valEntry.getValue();
            int i = 0;
            for (ZhengzMj majiang : list) {
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
            for (ZhengzMj majiang : hasPais) {
                List<ZhengzMj> copy = new ArrayList<>(hasPais);
                ZhengzMjHuLack lack = new ZhengzMjHuLack(hongzhongnum);
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
    public static boolean chaipai(ZhengzMjHuLack lack, List<ZhengzMj> hasPais, boolean isNeedJiang258) {
        if (hasPais.isEmpty()) {
            return true;

        }
        boolean hu = chaishun(lack, hasPais, isNeedJiang258);
        if (hu)
            return true;
        return false;
    }

    public static void sortMin(List<ZhengzMj> hasPais) {
        Collections.sort(hasPais, new Comparator<ZhengzMj>() {

            @Override
            public int compare(ZhengzMj o1, ZhengzMj o2) {
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
    public static boolean chaishun(ZhengzMjHuLack lack, List<ZhengzMj> hasPais, boolean needJiang258) {
        if (hasPais.isEmpty()) {
            return true;
        }
        sortMin(hasPais);
        ZhengzMj minMajiang = hasPais.get(0);
        int minVal = minMajiang.getVal();
        List<ZhengzMj> minList = ZhengzMjQipaiTool.getVal(hasPais, minVal);
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
        List<ZhengzMj> num1 = ZhengzMjQipaiTool.getVal(hasPais, pai1);
        List<ZhengzMj> num2 = ZhengzMjQipaiTool.getVal(hasPais, pai2);
        List<ZhengzMj> num3 = ZhengzMjQipaiTool.getVal(hasPais, pai3);

        // 找到一句话的麻将
        List<ZhengzMj> hasMajiangList = new ArrayList<>();
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
                List<ZhengzMj> count = ZhengzMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
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

                List<ZhengzMj> count = ZhengzMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
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
                List<ZhengzMj> count1 = ZhengzMjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                List<ZhengzMj> count2 = ZhengzMjQipaiTool.getVal(hasPais, hasMajiangList.get(1).getVal());
                List<ZhengzMj> count3 = ZhengzMjQipaiTool.getVal(hasPais, hasMajiangList.get(2).getVal());
                if (count1.size() >= 2 && (count2.size() == 1 || count3.size() == 1)) {
                    List<ZhengzMj> copy = new ArrayList<>(hasPais);
                    removeAllPai(copy, count1);
                    //copy.removeAll(count1);
                    ZhengzMjHuLack copyLack = lack.copy();
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

    public static void removeAllPai(List<ZhengzMj> hasPais, List<ZhengzMj> remPai) {
        for (ZhengzMj majiang : remPai) {
            hasPais.remove(majiang);
        }
    }

    public static boolean isCanAsJiang(ZhengzMj majiang, boolean isNeed258) {
        if (isNeed258) {
            if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
                return true;
            }
            return false;
        } else {
            return true;
        }

    }	

    public static List<ZhengzMj> checkChi(List<ZhengzMj> majiangs, ZhengzMj dismajiang,int wangVal) {
    	List<ZhengzMj> res = checkChi(majiangs, dismajiang, null);
//    	if(res.isEmpty()&& wangVal<200){//&&!haveWang(majiangs, wangVal)
//    		List<ZhengzMj> mjs = new ArrayList<ZhengzMj>();
//    		mjs.addAll(majiangs);
//    		if(haveHongzhong(majiangs)>0){
//    			mjs.add(ZhengzMj.getMajangVal2(wangVal));
//    		}
//    		
//    		if(dismajiang.isHongzhong()){
//    			dismajiang = ZhengzMj.getMajangVal2(wangVal);
//    		}
//    		
//    		res = checkChi(mjs, dismajiang, null);
//    	}
        return res;
    }
    
    public static HashMap<Integer,Integer>  getMingTangFans(ZhengzMj huMj,ZhengzMjPlayer player,ZhengzMjTable table,boolean zimo){
    	
    	HashMap<Integer,Integer> mingTangs = new HashMap<>();
    	
    	
    	
    	return mingTangs;
    }

	private static void addQiduiMingt(HashMap<Integer, Integer> mingTangs) {
		mingTangs.put(ZhengZMingTang.Qi_Dui_Hu, 2);
		mingTangs.remove(ZhengZMingTang.Wang_Gui_Wei);
		mingTangs.remove(ZhengZMingTang.SHUANG_WANGGUI_WEI);
		mingTangs.remove(ZhengZMingTang.Men_Qing);
	}
    
    
    
    
    
    
    public static int getQingYiseX(int[] infos,boolean xiaoDao){
    	
    	if(infos[6]>0||infos[8]>2||infos[3]!=infos[5]){
    		return 0;
    	}
    	
    	//没有王，那花色最多只能两种 有两种一定是小道，并且所有都要红中替换
    	if(infos[2]==0&&infos[8]<=2&&xiaoDao){
    		if(infos[3]==infos[5])
    		return 1;//清一色 小道
    	}
    	
    	//有王 那最多三种花色
    	if(infos[2]>0){
    		//王替换别的牌了
    		if(infos[8]==1){
    			return 2;//软清一色
    		}else if(infos[8]==2){
    			//有红中只有两花色，算清一色，又有王（王替代本花色）
    			if(infos[3]>0){
    				return 2;
    			}
    		}
    		
    	}
    	
    	
    	
    	
    	return 0;
    	
    	
    }
    
    
    public static int shiSanLang(List<ZhengzMj> majiangs){
    	HashSet<Integer> fengPais = new HashSet<Integer>();
    	 HashMap<Integer,List<Integer>> twsMjs = new HashMap<>();
    	 int fengCount = 0;
    	for (ZhengzMj majiang : majiangs) {
            if (majiang.isFeng()) {
            	fengCount++;
            	fengPais.add(majiang.getVal());
            }else{
            	List<Integer> mjs = twsMjs.get(majiang.getColourVal());
            	if(mjs==null){
            		mjs = new ArrayList<>();
            			twsMjs.put(majiang.getColourVal(), mjs);
            	}
            	mjs.add(majiang.getVal()%10);
            	
            	if(mjs.size()>3){
            		return 0;
            	}
            }
        }
    	
    	if(fengPais.size()<4||fengPais.size()<fengCount){
    		return 0;
    	}
    	
    	for(Entry<Integer,List<Integer>> entry: twsMjs.entrySet()){
    		List<Integer> mjs = entry.getValue();
    		if(!checkMjbetween(mjs)){
    			return 0;
    		}
    	}
    	
    	
    	if(fengCount==7){
    		return 2;
    	}
    	
    	return 1;
    }
    
    
    public static boolean pengpengHu(ZhengzMjPlayer player,int wangVal,ZhengzMj huMj,boolean zimo){
    	//||player.getGang().size()>0
    	if(player.getChi().size()>0){
    		return false;
    	}
    	
    	List<ZhengzMj> handMjs = new ArrayList<>(player.getHandMajiang());
    	if(!zimo){
    		handMjs.add(huMj);
    	}
    	handMjs.addAll(player.getPeng());
    	handMjs.addAll(player.getGang());
    	
    	int wangCount  = dropWangBa(handMjs, wangVal).size();
//    	
//    	ZhengzMjIndexArr card_index = new ZhengzMjIndexArr();
//        ZhengzMjQipaiTool.getMax(card_index, handMjs);
//        ZhengzMjIndex index3 = card_index.getMajiangIndex(2);
//    	
//        ZhengzMjIndex index2 = card_index.getMajiangIndex(1);
//        ZhengzMjIndex index1 = card_index.getMajiangIndex(0);
//        
//        
//        if(index3!=null){
//        	 List<ZhengzMj> mj3s =  index3.getMajiangs();
//             
//             for(ZhengzMj mj: mj3s){
//             	handMjs.remove(mj);
//             }
//        }
//       
        
        return isPengPengHu(handMjs,wangCount);
        
    	
    	
    }
    
    
    
    private static boolean checkMjbetween(List<Integer> mjs){
    	if(mjs.size()<2){
    		return true;
    	}
    	if(mjs.contains(3)&&(mjs.contains(7)||mjs.contains(8))  ||  (mjs.contains(2)&&mjs.contains(7))){
    		return false;
    	}
    	Collections.sort(mjs);
    	for (int i=0;i<mjs.size()-1;i++) {
    		if(mjs.get(i+1)-mjs.get(i)<3){
    			return false;
    		}
    		
        }
    	return true;
    	
    }
    public static int haveHongzhong(List<ZhengzMj> majiangs) {
    	int count = 0;
        for (ZhengzMj majiang : majiangs) {
            if (majiang.isHongzhong()) {
//                return true;
            	count++;
            }
        }
        return count;
    }
    
    
    
    
	  public static int getWangCounts(List<ZhengzMj> copy,int wangVal){
		  int count = 0;
	        Iterator<ZhengzMj> iterator = copy.iterator();
	        while (iterator.hasNext()) {
	            ZhengzMj majiang = iterator.next();
	            if (majiang.getVal() == wangVal) {
	            	count++;
	            }
	        }
	        return count;
	  }
	  
	  
    private static int getWangRepMjVal(int count,int repVal,List<ZhengzMj> majiangs){
    	
    	HashMap<Integer,Integer> map= new HashMap<Integer,Integer>();
    	for(ZhengzMj mj: majiangs){
    		
    		Integer valCount = map.get(mj.getVal());
    		
    		if(valCount==null){
    			map.put(mj.getVal(), 1);
    		}else{
    			map.put(mj.getVal(), valCount+1);
    		}
    	}
    	
    	Integer repValCount = map.get(repVal);
    	int realRepVal = 0;
    	//|| 4-repValCount>=count
    	if(repValCount==null){
    		realRepVal = repVal;
    	}else{
    		  for (ZhengzMj mj : ZhengzMj.fullMj) {
    			  Integer repcout  = map.get(mj.getVal());
    			  //||4-repcout>count
    			  if(repcout==null){
    				  realRepVal = mj.getVal();
    				  break;
    			  }
    		  }
    	}
    	
    	return realRepVal;
    	
    	
    }

	private static boolean checkyiTiaoLong(List<ZhengzMj> majiangs, int wangCount, ZhengzMjTable table) {
		int hzCount = ZhengzMjTool.haveHongzhong(majiangs);
		boolean longyitiao = false;
		// 有红中再变下牌
		for (int i = 0; i <= wangCount; i++) {
			List<ZhengzMj> cards2 = new ArrayList<>(majiangs);
			ZhengzMjTool.dropWangByCout(cards2, table.getWangMjVal(), i);
				boolean isHu = HuUtil.isCanHu(cards2, i);
				if(isHu){
					longyitiao  = true;
					break;
				}
				if (!isHu && hzCount > 0 && table.getWangMjVal() < 200) { 
					// 有红中再变下牌
					for (int j = 1; j <= hzCount; j++) {
//						if(j==2){
//							break;
//						}
						List<ZhengzMj> cards3 = new ArrayList<>(cards2);
						ZhengzMjTool.dropHzhong(cards3, j);
						cards3.addAll(ZhengzMj.getSameMjList(j, table.getWangMjVal(), majiangs));
						isHu = HuUtil.isCanHu(cards3, i);
						if (isHu) {
							table.setHuHzRepCount(j);
							longyitiao  = true;
							break;
						}
					}
				
				}
			}
		return longyitiao;
	}
    
    
    
    public static boolean dropAssignMj(List<ZhengzMj> majiangs,int val) {
    	  Iterator<ZhengzMj> iterator = majiangs.iterator();
          while (iterator.hasNext()) {
              ZhengzMj majiang = iterator.next();
              if (majiang.getVal() == val) {
                  iterator.remove();
                  break;
              }
          }
          return true;
    	
    }
    
    
    
    public static boolean haveWang(List<ZhengzMj> majiangs,int wangVal) {
        for (ZhengzMj majiang : majiangs) {
            if (majiang.getVal()==wangVal) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否能吃
     *
     * @param majiangs
     * @param dismajiang
     * @return
     */
    public static List<ZhengzMj> checkChi(List<ZhengzMj> majiangs, ZhengzMj dismajiang, List<Integer> wangValList) {
        int disMajiangVal = dismajiang.getVal();
        List<Integer> chi1 = new ArrayList<>(Arrays.asList(disMajiangVal - 2, disMajiangVal - 1));
        List<Integer> chi2 = new ArrayList<>(Arrays.asList(disMajiangVal - 1, disMajiangVal + 1));
        List<Integer> chi3 = new ArrayList<>(Arrays.asList(disMajiangVal + 1, disMajiangVal + 2));

        List<Integer> majiangIds = ZhengzMjHelper.toMajiangVals(majiangs);
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
        return new ArrayList<ZhengzMj>();
    }

    public static List<ZhengzMj> findMajiangByVals(List<ZhengzMj> majiangs, List<Integer> vals) {
        List<ZhengzMj> result = new ArrayList<>();
        for (int val : vals) {
            for (ZhengzMj majiang : majiangs) {
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
    public static List<ZhengzMj> dropWangBa(List<ZhengzMj> copy,int wangVal) {
        List<ZhengzMj> hongzhong = new ArrayList<>();
        
        if(wangVal==0){
        	return hongzhong;
        }
        
        Iterator<ZhengzMj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            ZhengzMj majiang = iterator.next();
            if (majiang.getVal() == wangVal) {
                hongzhong.add(majiang);
                iterator.remove();
            }
        }
        return hongzhong;
    }
    
    
    /**
     * 去掉红中
     *
     * @param copy
     * @return
     */
    public static List<ZhengzMj> dropHzhong(List<ZhengzMj> copy,int count) {
        List<ZhengzMj> hongzhong = new ArrayList<>();
        Iterator<ZhengzMj> iterator = copy.iterator();
        int hzCount = 0;
        while (iterator.hasNext()) {
            ZhengzMj majiang = iterator.next();
            if (majiang.isHongzhong() ) {
            	hzCount ++;
                hongzhong.add(majiang);
                iterator.remove();
            }
            if(hzCount==count){
            	break;
            }
        }
        return hongzhong;
    }
    
    public static List<ZhengzMj> dropWangByCout(List<ZhengzMj> copy,int wangVal,int count) {
        List<ZhengzMj> hongzhong = new ArrayList<>();
        if(count==0){
        	return hongzhong;
        }
        Iterator<ZhengzMj> iterator = copy.iterator();
        int hzCount = 0;
        while (iterator.hasNext()) {
            ZhengzMj majiang = iterator.next();
            if (majiang.getVal()==wangVal ) {
            	hzCount ++;
                hongzhong.add(majiang);
                iterator.remove();
            }
            if(hzCount==count){
            	break;
            }
        }
        return hongzhong;
    }
    
    

    public static boolean checkWang(Object majiangs, List<Integer> wangValList) {
        if (majiangs instanceof List) {
            List list = (List) majiangs;
            for (Object majiang : list) {
                int val = 0;
                if (majiang instanceof ZhengzMj) {
                    val = ((ZhengzMj) majiang).getVal();
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
    public static List<ZhengzMj> getSameMajiang(List<ZhengzMj> majiangs, ZhengzMj majiang, int num) {
        List<ZhengzMj> hongzhong = new ArrayList<>();
        int i = 0;
        for (ZhengzMj maji : majiangs) {
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
    public static List<ZhengzMj> dropMjId(List<ZhengzMj> copy, int id) {
        List<ZhengzMj> hongzhong = new ArrayList<>();
        Iterator<ZhengzMj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            ZhengzMj majiang = iterator.next();
            if (majiang.getId() == id) {
                hongzhong.add(majiang);
                iterator.remove();
            }
        }
        return hongzhong;
    }

    public static List<ZhengzMj> getPais(String paisStr) {
        String[] pais = paisStr.split(",");
        List<ZhengzMj> handPais = new ArrayList<>();
        for (String pai : pais) {
            for (ZhengzMj mj : ZhengzMj.values()) {
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
    public static List<ZhengzMj> getLackListOld(List<ZhengzMj> cards, int hzCount, boolean hu7dui) {
        if ((cards.size() + hzCount) % 3 != 1) {
            return Collections.emptyList();
        }
        List<ZhengzMj> lackPaiList = HuUtil.getLackPaiList(cards, hzCount);
        if (lackPaiList.size() == 1 && null == lackPaiList.get(0)) {
            // 听所有
            return lackPaiList;
        }
        Set<Integer> have = new HashSet<>();
        if (lackPaiList.size() > 0) {
            Iterator<ZhengzMj> iterator = lackPaiList.iterator();
            int lastIndex = cards.size();
            ZhengzMj mj;
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
            if (ZhengzMjTool.isHu7Dui(cards, hzCount + 1)) {
                int rmIndex = cards.size();
                for (ZhengzMj mj : ZhengzMj.fullMj) {
                    if (mj.isHongzhong()|| have.contains(mj.getVal())) {
                        continue;
                    }
                    cards.add(mj);
                    if (ZhengzMjTool.isHu7Dui(cards, hzCount)) {
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
    public static List<ZhengzMj> getLackList(int[] cardArr, int hzCount, boolean hu7dui,int wangVal,int wangVal2) {
        int cardNum = 0;
        for (int i = 0, length = cardArr.length; i < length; i++) {
            cardNum += cardArr[i];
        }
        if ((cardNum + hzCount) % 3 != 1) {
            return Collections.emptyList();
        }
        List<ZhengzMj> lackPaiList = new ArrayList<>();
        Set<Integer> have = new HashSet<>();
        for (ZhengzMj mj : ZhengzMj.fullMj) {
        	if(mj.getVal()==wangVal||mj.getVal()==wangVal2){
        		continue;
        	}
        	
        	//mj.isHongzhong(mj.getVal()==wangVal ||
            if ( have.contains(mj.getVal())) {
                continue;
            }
//            
//            if(wangVal<200){
//            	
//            }
            
            int cardIndex = HuUtil.getMjIndex(mj);
            cardArr[cardIndex] = cardArr[cardIndex] + 1;
            //hu7dui &&
            if (HuUtil.isCanHu7Dui(cardArr, 0)) {
                lackPaiList.add(mj);
                have.add(mj.getVal());
            }
            if (!lackPaiList.contains(mj)&&HuUtil.isCanHu(cardArr, hzCount)) {
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
    
    
    public static List<ZhengzMj> getLackList2(ZhengzMjTable table,List<ZhengzMj> handpais) {
   
        List<ZhengzMj> lackPaiList = new ArrayList<>();
        Set<Integer> have = new HashSet<>();
        for (ZhengzMj mj : ZhengzMj.fullMj) {
        	//mj.isHongzhong(mj.getVal()==wangVal ||
            if ( have.contains(mj.getVal())) {
                continue;
            }
            
//            if(!handpais.contains(mj)){
            	handpais.add(mj);
//            }
            if (!lackPaiList.contains(mj)&&checkHuQduiTing(table, handpais)) {
                lackPaiList.add(mj);
                have.add(mj.getVal());
                handpais.remove(mj);
                break;
            }
            handpais.remove(mj);
            
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
        List<ZhengzMj> handPais = getPais(paisStr);
        System.out.println(handPais);
        int count = 1;
        boolean canHu = false;
        long start = Clock.systemDefaultZone().millis();

        for (int i = 0; i < count; i++) {
//            canHu = isHu(handPais, true,11);
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
        List<ZhengzMj> lackPaiList = new ArrayList<>();
        start = Clock.systemDefaultZone().millis();
        for (int i = 0; i < count; i++) {
           // lackPaiList = getLackListOld(handPais, laiZiNum, true);
        }
        timeUse = Clock.systemDefaultZone().millis() - start;
        System.out.println("---count = " + count + " , timeUse = " + timeUse + " ms" + "tingList : " + lackPaiList);

        start = Clock.systemDefaultZone().millis();
        int[] cardArr = HuUtil.toCardArray(handPais);
        for (int i = 0; i < count; i++) {
          //  lackPaiList = getLackList(cardArr, laiZiNum, true);
        }
        timeUse = Clock.systemDefaultZone().millis() - start;
        System.out.println("---count = " + count + " , timeUse = " + timeUse + " ms" + "tingList : " + lackPaiList);

    }
}
