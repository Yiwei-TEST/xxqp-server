package com.sy599.game.qipai.bsmj.rule;

import com.sy599.game.qipai.bsmj.bean.BsMjPlayer;
import com.sy599.game.qipai.bsmj.bean.BsMjTable;
import com.sy599.game.qipai.bsmj.bean.BsMjiangHu;
import com.sy599.game.qipai.bsmj.constant.BsMjAction;
import com.sy599.game.qipai.bsmj.tool.BsMjQipaiTool;
import com.sy599.game.qipai.bsmj.tool.BsMjTool;
import com.sy599.game.util.DataMapUtil;

import java.util.*;
import java.util.Map.Entry;

public class BsMjRule {

	/**
	 * 小胡
	 * @param majiangIds
	 * @return
	 *
	 *//*
	public static void checkXiaoHu(BsMjiangHu hu, List<BsMj> majiangIds, boolean begin) {
		BsMjAction actionData = new BsMjAction();
		BsMjIndexArr card_index = new BsMjIndexArr();
		BsMjQipaiTool.getMax(card_index, majiangIds);
		if(begin) {
			boolean findjiang = false;
			List<Integer> seList = new ArrayList<>(Arrays.asList(1, 2, 3));
			Map<Integer, List<BsMj>> huaseListMap = new HashMap<>();
			for (BsMj majiang : majiangIds) {
				if (!findjiang && majiang.isJiang()) {
					findjiang = true;
				}
				int huase = majiang.getHuase();
				if (seList.contains(huase)) {
					seList.remove((Object) huase);
				}
				if(huaseListMap.containsKey(huase)) {
					List<BsMj> list = huaseListMap.get(huase);
					list.add(majiang);
					huaseListMap.put(huase, list);
				} else {
					List<BsMj> list = new ArrayList<>();
					list.add(majiang);
					huaseListMap.put(huase, list);
				}
			}
			Set<Integer> sanTongMjVals = new HashSet<>();
			Map<Integer, Set<Integer>> valMap = new HashMap<>();
			for(int i = 1 ; i <= 3 ; i++ ) {
				BsMjIndex sanTongIndex = card_index.getMajiangIndex(i);// 对子数量
				if (sanTongIndex != null) {
					for (int mjVal : sanTongIndex.getMajiangValMap().keySet()) {
						int val = mjVal % 10;
						if (!valMap.containsKey(val)) {
							Set<Integer> set = new HashSet<>();
							set.add(mjVal);
							valMap.put(val, set);
						} else {
							Set<Integer> set = valMap.get(val);
							if (!set.contains(mjVal)) {
								set.add(mjVal);
							}
						}
						if (valMap.get(val).size() >= 3) {
							sanTongMjVals.addAll(valMap.get(val));
						}
					}
				}
			}
			Set<Integer> jiejieGaoMjVals = new HashSet<>();
			Map<Integer, Set<Integer>> huaseMap = new HashMap<>();
			for(int i = 1 ; i <= 3 ; i++ ) {
				BsMjIndex jiejieGaoIndex = card_index.getMajiangIndex(i);// 对子数量
				if (jiejieGaoIndex != null) {
					for (int val : jiejieGaoIndex.getMajiangValMap().keySet()) {
						int huase = val / 10;
						if (huaseMap.containsKey(huase)) {
							Set<Integer> sets = huaseMap.get(huase);
							sets.add(val);
							if (canChi(sets, val)) {
								jiejieGaoMjVals.addAll(sets);
							}
						} else {
							Set<Integer> set = new HashSet<>();
							set.add(val);
							huaseMap.put(huase, set);
						}
					}
				}
			}

			Set<Integer> jinTongYuNuMjVals = new HashSet<>();
			for(int i = 1 ; i <= 3 ; i++ ) {
				BsMjIndex jinTongYuNuIndex = card_index.getMajiangIndex(i);// 对子数量
				if (jinTongYuNuIndex != null) {
					for (int val : jinTongYuNuIndex.getMajiangValMap().keySet()) {
						if (val == 12 || val == 22) {
							jinTongYuNuMjVals.add(val);
						}
					}
				}
			}

			boolean yizhihua = false;
			List<BsMj> jiangMjs = new ArrayList<>();
			for(BsMj mj : majiangIds) {
				if(mj.isJiang()) {
					jiangMjs.add(mj);
				}
			}
			if(jiangMjs.size() == 1 && jiangMjs.get(0).getVal() % 10 == 5) {
				yizhihua = true;
			} else {
				for(List<BsMj> list : huaseListMap.values()) {
					if(list.size() == 1 && list.get(0).getVal() % 10 == 5) {
						yizhihua = true;
						break;
					}
				}
			}
			// 缺一色 起牌后玩家手上任缺一门
			if (!seList.isEmpty()) {
				actionData.addQueYiSe();
				hu.setQueyise(true);
				hu.setShowMajiangs(majiangIds);
			}
			// 板板胡 起牌后玩家手上没有一张258将牌
			if (!findjiang) {
				actionData.addBanBanHu();
				hu.setBanbanhu(true);
				hu.setShowMajiangs(majiangIds);
			}
			//一枝花：满足以下任意一种情况：起牌后，玩家手牌中只有一张将牌，且这张将牌数字为“5”，即可胡牌（等同小胡自摸）。起牌后，玩家手牌中某花色只有一张，且这张将牌数字为“5”，即可胡牌（等同小胡自摸）。
			if(yizhihua) {
				actionData.addYiZhiHua();
				hu.setYizhihua(true);
				hu.setShowMajiangs(majiangIds);
			}
			// 六六顺 起牌后玩家手上有两个刻字
			BsMjIndex index2 = card_index.getMajiangIndex(2);
			BsMjIndex index3 = card_index.getMajiangIndex(3);
			int count3 = index2 != null ? index2.getLength():0;
			int count4 = index3 != null ? index3.getLength():0;
			if (count3 + count4 > 1) {
				actionData.addLiuLiuShun();
				hu.setLiuliushun(true);
				if(index2 != null) {
					hu.setShowMajiangs(index2.getMajiangs());
				}
				if(index3 != null){
					hu.setShowMajiangs(index3.getMajiangs());
				}
			}
			// 大四喜 起牌后玩家手上有4张一样的牌
			if (index3 != null) {
				actionData.addDaSiXi();
				hu.setDasixi(true);
				hu.setShowMajiangs(index3.getMajiangs());
			}
			//金童玉女：起手一对二筒和一对二条，且仅限这两张牌
			if(jinTongYuNuMjVals.size() == 2){
				actionData.addJingTongYuNu();
				hu.setJinTongYuNu(true);
				List<Integer> vals = new ArrayList<>(jinTongYuNuMjVals);
				hu.setShowMajiangs(BsMjQipaiTool.findMajiangByValsAndCount(majiangIds, vals,2));
			}
			//节节高：起牌后，玩家手上有3连对将且同花色，列如：2个1万，2个2万，2个3万，即可胡牌（等同小胡自摸）
			if(!jiejieGaoMjVals.isEmpty()) {
				actionData.addJieJieGao();
				hu.setJiejiegao(true);
				List<Integer> vals = new ArrayList<>(jiejieGaoMjVals);
				hu.setShowMajiangs(BsMjQipaiTool.findMajiangByVals(majiangIds, vals));
			}
			//三同：起牌后，玩家手上有3对点数相同的3门牌，列如2个1万，2个1筒，2个1条，即可胡牌（等同小胡自摸）
			if(!sanTongMjVals.isEmpty()) {
				actionData.addSanTong();
				hu.setSantong(true);
				List<Integer> vals = new ArrayList<>(sanTongMjVals);
				hu.setShowMajiangs(BsMjQipaiTool.findMajiangByVals(majiangIds, vals));
			}


		} else {
			// 中途四喜
			BsMjIndex index3 = card_index.getMajiangIndex(3);
			if (index3 != null) {
				actionData.addZhongTuSiXi();
				hu.setZhongtusixi(true);
				hu.setShowMajiangs(index3.getMajiangs());
			}
			// 中途六六顺
			BsMjIndex index2 = card_index.getMajiangIndex(2);
			if (index2 != null && index2.getLength() > 1) {
				actionData.addZhongTuLiuLiuShun();
				hu.setZhongtuliuliushun(true);
				hu.setShowMajiangs(index2.getMajiangs());
			}
		}
		boolean xiaohu = false;
		List<Integer> list = DataMapUtil.toList(actionData.getArr());
		for (int val : list) {
			if (val > 0) {
				xiaohu = true;
				break;
			}
		}
		if (xiaohu) {
			hu.setXiaohu(true);
		} else {
			list.clear();
		}
		hu.setXiaohuList(list);
	}*/

	public static boolean isSameHuaSe(Set<Integer> vals) {
		Set<Integer> huaseNumSet = new HashSet<>();
		for(int val : vals) {
			if(val % 10 == 0) {// 只有1到9的才能吃
				return false;
			}
			if(!huaseNumSet.contains(val / 10)) {
				huaseNumSet.add(val / 10);
			}
		}
		if(!huaseNumSet.isEmpty() && huaseNumSet.size() == 1) {
			return true;
		} else
			return false;
	}

	private static boolean canChi(Set<Integer> vals, int val) {
		Set<Integer> chi1 = new HashSet<>(Arrays.asList(val - 2, val - 1));
		Set<Integer> chi2 = new HashSet<>(Arrays.asList(val - 1, val + 1));
		Set<Integer> chi3 = new HashSet<>(Arrays.asList(val + 1, val + 2));
		if (isSameHuaSe(chi1) && vals.containsAll(chi1)) {
			return true;
		}
		if (isSameHuaSe(chi2) && vals.containsAll(chi2)) {
			return true;
		}
		if (isSameHuaSe(chi3) && vals.containsAll(chi3)) {
			return true;
		}
		return false;
	}
	
	
	

	/**
	 * 0 碰碰胡 1字一色 2清一色 3双豪华7小对 4豪华7小对 5:7小对 6全求人
	 * 
	 * @param majiangIds
	 * @param gang
	 * @param peng
	 * @param chi
	 * @param buzhang
	 * @return
	 */
	public static int[] checkDahu(BsMjiangHu hu, List<BsMj> majiangIds, List<BsMj> gang, List<BsMj> peng,BsMj huCard,boolean pingHu,BsMjTable table) {
		List<BsMj> allMajiangs = new ArrayList<>();
		allMajiangs.addAll(majiangIds);
		allMajiangs.addAll(gang);
		allMajiangs.addAll(peng);
		int arr[] = new int[7];
		if (majiangIds.size() % 3 != 2) {
			return arr;
		}

		BsMjIndexArr all_card_index = new BsMjIndexArr();
		BsMjQipaiTool.getMax(all_card_index, allMajiangs);


		// 1碰碰胡(字一色)
		if (isPengPengHu(allMajiangs, all_card_index)) {
			arr[0] = 1;
			hu.setPengpengHu(true);
			if(BsMjQipaiTool.isAllZi(allMajiangs)) {
				hu.setZiYiSe(true);
			}
			
		}
		
		if (pingHu && table.getSiguiyi()==1) {
			if(huCard !=null) {
				
				List<BsMj> cards = BsMjQipaiTool.getVal(allMajiangs, huCard.getVal());
				// 2四归一
				if (cards.size() ==4) {
					hu.setShiGuiYi(true);
				}
			}
		}
		
		
		if(table.getYitiaolong() ==1 && yitiaolong(majiangIds)) {
//			hu.setHu(true);
			hu.setYitiaolong(true);
		}
		

		// 先去掉红中
		List<BsMj> copy = new ArrayList<>(majiangIds);
		//int hongzhongNum = BsMjTool.dropHongzhong(copy).size();

		BsMjIndexArr card_index = new BsMjIndexArr();
		BsMjQipaiTool.getMax(card_index, copy);
		// 4 7小对
//		if (card_index.getDuiziNum() == 6 && hongzhongNum > 0) {
//			arr[5] = 1;
//		}else 
		if(card_index.getDuiziNum() == 7){
			// 是否有豪华7小对
			BsMjIndex index = card_index.getMajiangIndex(3);
			if (index != null) {
				// 有4个一样的牌
				if (index.getLength() > 1) {
					// 双豪华7小对
					arr[3] = 1;

					hu.setLongZhuaBeiHu(true);
				} else {
					// 豪华7小对
					arr[4] = 1;
					hu.setLongZhuaBeiHu(true);
				}
			} else {
				// 普通7小对
				arr[5] = 1;

				hu.set7Xiaodui(true);
			}
		}

		// 6全求人 只有两个手牌
//		if (majiangIds.size() == 2) {
//			//boolean haveHz = false;
//			for (BsMj majiang : majiangIds) {
//				if (majiang.isHongzhong()) {
//					//haveHz = true;
//					break;
//				}
//
//			}
//
//			// 两张牌只要有一个红中算可以胡了 haveHz ||
//			if ( majiangIds.get(0).getVal() == majiangIds.get(1).getVal()) {
//				arr[6] = 1;
//				hu.setQuanqiuren(true);
//			}
//
//		}
		List<Integer> list2 = DataMapUtil.toList(arr);
		if (list2.contains(1)) {
			hu.setHu(true);
		}
				// 3清一色
		if (isqingyiseHu(allMajiangs, majiangIds,hu.isHu())) {
				arr[2] = 1;
				hu.setQingyiseHu(true);
		}
		
		
		List<Integer> list = DataMapUtil.toList(arr);
		if (list.contains(1)) {
			hu.setHu(true);
			hu.setDahu(true);
			hu.setDahuList(list);
		} else {
			list.clear();
			hu.setDahuList(list);
		}

		return arr;
	}

	private static boolean isqingyiseHu(List<BsMj> allMajiangs, List<BsMj> majiangIds,boolean isHu) {
		boolean qingyise = false;
		int se = 0;
		for (BsMj mjiang : allMajiangs) {
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
		if (qingyise && !isHu) {
			boolean hu = BsMjTool.isPingHu(majiangIds, false);
			return hu;
		}
		return qingyise;
	}

	private static boolean isJiangJiangHu(List<BsMj> majiangIds) {
		boolean jiangjianghu = true;
		for (BsMj mjiang : majiangIds) {
			if (mjiang.isHongzhong()) {
				continue;
			}
			if (!mjiang.isJiang()) {
				jiangjianghu = false;
				break;
			}
		}
		return jiangjianghu;
	}
	
	
	public static boolean yitiaolong(List<BsMj> majiangs){
		
		
		HashMap<Integer,HashSet<Integer>> mjMaps = new HashMap<Integer,HashSet<Integer>> ();
		//HashSet<Integer> set = new HashSet<Integer>();
		for (BsMj majiang : majiangs) {
			int color = majiang.getColourVal();
			if(color<=3) {
				HashSet<Integer> set = mjMaps.get(color);
				if(set == null) {
					set = new HashSet<Integer>(); 
					mjMaps.put(color, set);
				}
				set.add(majiang.getPai());
			}
		}
		
		//找出
		HashSet<Integer> keys  = new HashSet<Integer>();
		for(Entry<Integer, HashSet<Integer>> entry : mjMaps.entrySet()) {
			HashSet<Integer> set2  =entry.getValue();
			for(int i=1;i<=9;i++) {
				if(!set2.contains(i)) {
					keys.add(entry.getKey());
					break;
				}
			}
		}
		//拥有的花色都没1-9的顺子
		if(keys.size() == mjMaps.keySet().size()) {
			return false;
		}
		
		List<BsMj> copy = new ArrayList<BsMj>(majiangs);
		
		int key = 0;
		for(Entry<Integer, HashSet<Integer>> entry : mjMaps.entrySet()) {
			if(keys.contains(entry.getKey())) {
				continue;
			}
			key = entry.getKey();
			
			
		}
		
		for(int i=1;i<=9;i++) {
			BsMjQipaiTool.dropVal2(copy, key*10+i);
		}
		
		boolean hu = BsMjTool.isPingHu(copy, false);
		
		
		return hu;
	}
	

	private static boolean isPengPengHu(List<BsMj> majiangIds, BsMjIndexArr card_index) {
		BsMjIndex index4 = card_index.getMajiangIndex(3);
		BsMjIndex index3 = card_index.getMajiangIndex(2);
		BsMjIndex index2 = card_index.getMajiangIndex(1);
		BsMjIndex index1 = card_index.getMajiangIndex(0);

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
//		else if (majiangIds.contains(BsMj.getMajang(201)) && index4 != null && index4.getLength() == 2 && index1 != null && index1.getLength() == 1 && index2 == null) {
//			return true;
//		} else if (majiangIds.contains(BsMj.getMajang(201)) && sameCount >= 3 && index2 == null && index1 != null && index1.getLength() == 2) {
//			return true;
//		} else if (majiangIds.contains(BsMj.getMajang(201)) && sameCount >= 2 && index2 != null && index2.getLength() == 2 && index1 != null && index1.getLength() == 1) {
//			return true;
//		}
		return false;
	}
}
