package com.sy599.game.qipai.tdhmj.rule;

import com.sy599.game.qipai.tdhmj.bean.TdhMjTable;
import com.sy599.game.qipai.tdhmj.bean.TdhMjiangHu;
import com.sy599.game.qipai.tdhmj.constant.TdhMjAction;
import com.sy599.game.qipai.tdhmj.tool.TdhMjQipaiTool;
import com.sy599.game.qipai.tdhmj.tool.TdhMjTool;
import com.sy599.game.util.DataMapUtil;

import java.util.*;
import java.util.Map.Entry;

public class TdhMjRule {

	

	public static Set<Integer> getJinTongYuNv(TdhMjIndexArr card_index) {
		Set<Integer> jinTongYuNuMjVals = new HashSet<>();
		for (int i = 1; i <= 3; i++) {
			TdhMjIndex jinTongYuNuIndex = card_index.getMajiangIndex(i);// 对子数量
			if (jinTongYuNuIndex != null) {
				for (int val : jinTongYuNuIndex.getMajiangValMap().keySet()) {
					if (val == 12 || val == 22) {
						jinTongYuNuMjVals.add(val);
					}
				}
			}
		}
		return jinTongYuNuMjVals;
	}

	public static Set<Integer> getJieJieGao(TdhMjIndexArr card_index) {
		Set<Integer> jiejieGaoMjVals = new HashSet<>();
		Set<Integer> rset = new HashSet<>();
		Map<Integer, Set<Integer>> huaseMap = new HashMap<>();
		for (int i = 1; i <= 3; i++) {
			TdhMjIndex jiejieGaoIndex = card_index.getMajiangIndex(i);// 对子数量
			if (jiejieGaoIndex != null) {
				for (int val : jiejieGaoIndex.getMajiangValMap().keySet()) {
					int huase = val / 10;
					if (huaseMap.containsKey(huase)) {
						Set<Integer> sets = huaseMap.get(huase);
						sets.add(val);
						if (isJieJieGao(sets)) {
							jiejieGaoMjVals.addAll(sets);
							return jiejieGaoMjVals;
						}
					} else {
						Set<Integer> set = new HashSet<>();
						set.add(val);
						huaseMap.put(huase, set);
					}
				}
			}
		}
		return jiejieGaoMjVals;
	}

	public static Set<Integer> getSantong(TdhMjIndexArr card_index) {
		Set<Integer> sanTongMjVals = new HashSet<>();
		Map<Integer, Set<Integer>> valMap = new HashMap<>();
		for (int i = 1; i <= 3; i++) {
			TdhMjIndex sanTongIndex = card_index.getMajiangIndex(i);// 对子数量
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
						return sanTongMjVals;
						// break;
					}
				}
			}
		}

		return sanTongMjVals;
	}

	public static boolean isSameHuaSe(Set<Integer> vals) {
		Set<Integer> huaseNumSet = new HashSet<>();
		for (int val : vals) {
			if (val % 10 == 0) {// 只有1到9的才能吃
				return false;
			}
			if (!huaseNumSet.contains(val / 10)) {
				huaseNumSet.add(val / 10);
			}
		}
		if (!huaseNumSet.isEmpty() && huaseNumSet.size() == 1) {
			return true;
		} else
			return false;
	}

	// private static boolean canChi(Set<Integer> vals, int val) {
	// Set<Integer> chi1 = new HashSet<>(Arrays.asList(val - 2, val - 1));
	// Set<Integer> chi2 = new HashSet<>(Arrays.asList(val - 1, val + 1));
	// Set<Integer> chi3 = new HashSet<>(Arrays.asList(val + 1, val + 2));
	// if (isSameHuaSe(chi1) && vals.containsAll(chi1)) {
	// return true;
	// }
	// if (isSameHuaSe(chi2) && vals.containsAll(chi2)) {
	// return true;
	// }
	// if (isSameHuaSe(chi3) && vals.containsAll(chi3)) {
	// return true;
	// }
	// return false;
	// }

	private static boolean isJieJieGao(Set<Integer> vals) {

		if (vals.size() < 3) {
			return false;
		}
		Set<Integer> jiejieGao = new HashSet<Integer>();
		ArrayList<Integer> list = new ArrayList<Integer>(vals);

		Collections.sort(list);

		for (int i = 0; i < list.size() - 2; i++) {
			int a = list.get(i) + 2;
			int b = list.get(i + 1) + 1;
			int c = list.get(i + 2);
			// 节节高
			if (a == b && b == c) {
				jiejieGao.add(list.get(i));
				jiejieGao.add(list.get(i + 1));
				jiejieGao.add(list.get(i + 2));
				vals.clear();
				vals.addAll(jiejieGao);
				return true;
			}
		}

		return false;

	}

	/**
	 * 0 碰碰胡 1将将胡 2清一色 3双豪华7小对 4豪华7小对 5:7小对 6全求人
	 * 
	 * @param majiangIds
	 * @param gang
	 * @param peng
	 * @param chi
	 * @param buzhang
	 * @return
	 */
	public static int[] checkDahu(TdhMjiangHu hu, List<TdhMj> majiangIds, List<TdhMj> gang, List<TdhMj> peng,
			List<TdhMj> chi, List<TdhMj> buzhang,boolean zimo,TdhMjTable table) {
		List<TdhMj> allMajiangs = new ArrayList<>();
		allMajiangs.addAll(majiangIds);
		allMajiangs.addAll(gang);
		allMajiangs.addAll(peng);
		allMajiangs.addAll(chi);
		allMajiangs.addAll(buzhang);
		int arr[] = new int[7];
		if (majiangIds.size() % 3 != 2) {
			return arr;
		}

		TdhMjIndexArr all_card_index = new TdhMjIndexArr();
		TdhMjQipaiTool.getMax(all_card_index, allMajiangs);

		// 1碰碰胡
		if (isPengPengHu(majiangIds, all_card_index) &&chi.size()==0 ) {
			arr[0] = 1;
			hu.setPengpengHu(true);
		}
	
		// 3清一色
		if (isqingyiseHu(allMajiangs, majiangIds)) {
			arr[2] = 1;
			hu.setQingyiseHu(true);
		}

		// 先去掉红中
		List<TdhMj> copy = new ArrayList<>(majiangIds);
		int hongzhongNum = TdhMjTool.dropHongzhong(copy).size();

		TdhMjIndexArr card_index = new TdhMjIndexArr();
		TdhMjQipaiTool.getMax(card_index, copy);
		// 4 7小对
		if (card_index.getDuiziNum() == 6 && hongzhongNum > 0) {
			arr[5] = 1;
		} else if (card_index.getDuiziNum() == 7) {
			// 是否有豪华7小对
			TdhMjIndex index = card_index.getMajiangIndex(3);
			if (index != null) {
				// 有4个一样的牌
				if (index.getLength() > 1) {
					// 双豪华7小对
					arr[3] = 1;

					hu.setShuang7xiaodui(true);
				} else {
					// 豪华7小对
					arr[4] = 1;

					hu.setHao7xiaodui(true);
				}
			} else {
				// 普通7小对
				arr[5] = 1;

				hu.set7Xiaodui(true);
			}
		}

		// 2将将胡
		boolean jiangjianghu= true;
		if (isJiangJiangHu(allMajiangs)) {
			if(table.getJiangHuZiMo()==1&&!zimo) {
				if(table.getGangDisMajiangs().size()==0) {
					if(arr[0]==1||arr[3]==1||arr[4]==1||arr[5]==1){
						
					}else{
						jiangjianghu =false;
					}
				}
				if(table.getLeftMajiangCount()==0) {
					jiangjianghu =true;
				}
			}
			if(table.getPengpengHuJiePao()==1&& !zimo&& hu.isPengpengHu()){
				jiangjianghu = false;
			}
			if(jiangjianghu) {
				arr[1] = 1;
				hu.setJiangjiangHu(true);
			}
		
		}
		
		// 6全求人
		if (majiangIds.size() == 2) {
//			boolean haveHz = false;
//			for (TdhMj majiang : majiangIds) {
//				if (majiang.isHongzhong()) {
//					haveHz = true;
//					break;
//				}
//
//			}

			// 两张牌只要有一个红中算可以胡了
			if ( majiangIds.get(0).getVal() == majiangIds.get(1).getVal() && chi.size()==0) {
				arr[6] = 1;
				hu.setQuanqiuren(true);
			}

		}
		List<Integer> list = DataMapUtil.toList(arr);
		if (list.contains(1)) {
			// 再检查一下清一色
			if (arr[2] != 1 && checkqingyise(allMajiangs)) {
				arr[2] = 1;
				hu.setQingyiseHu(true);
			}

			hu.setHu(true);
			hu.setDahu(true);
			hu.setDahuList(list);
		} else {
			list.clear();
			hu.setDahuList(list);
		}

		return arr;
	}

	private static boolean isqingyiseHu(List<TdhMj> allMajiangs, List<TdhMj> majiangIds) {
		boolean qingyise = checkqingyise(allMajiangs);
		if (qingyise) {
			boolean hu = TdhMjTool.isPingHu(majiangIds, false);
			return hu;
		}

		return false;
	}

	public static boolean checkqingyise(List<TdhMj> allMajiangs) {
		boolean qingyise = false;
		int se = 0;
		for (TdhMj mjiang : allMajiangs) {
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

	private static boolean isJiangJiangHu(List<TdhMj> majiangIds) {
		boolean jiangjianghu = true;
		for (TdhMj mjiang : majiangIds) {
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

	private static boolean isPengPengHu(List<TdhMj> majiangIds, TdhMjIndexArr card_index) {
		TdhMjIndex index4 = card_index.getMajiangIndex(3);
		TdhMjIndex index3 = card_index.getMajiangIndex(2);
		TdhMjIndex index2 = card_index.getMajiangIndex(1);
		TdhMjIndex index1 = card_index.getMajiangIndex(0);

		int sameCount = 0;
		if (index4 != null) {
			sameCount += index4.getLength();
		}
		if (index3 != null) {
			sameCount += index3.getLength();
		}

		// 3个相同或者4个相同有4个
		if (sameCount == 4 && index2 != null && index2.getLength() == 1) {
			return true;
		} else if (majiangIds.contains(TdhMj.getMajang(201)) && index4 != null && index4.getLength() == 2
				&& index1 != null && index1.getLength() == 1 && index2 == null) {
			return true;
		} else if (majiangIds.contains(TdhMj.getMajang(201)) && sameCount >= 3 && index2 == null && index1 != null
				&& index1.getLength() == 2) {
			return true;
		} else if (majiangIds.contains(TdhMj.getMajang(201)) && sameCount >= 2 && index2 != null
				&& index2.getLength() == 2 && index1 != null && index1.getLength() == 1) {
			return true;
		}
		return false;
	}

	public static void main(String[] args) {

		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(1);

		list.add(8);
		list.add(2);
		Collections.sort(list);
		System.out.println(list);
	}
}
