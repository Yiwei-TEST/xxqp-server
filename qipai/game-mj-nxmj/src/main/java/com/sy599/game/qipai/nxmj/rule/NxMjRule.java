package com.sy599.game.qipai.nxmj.rule;

import com.sy599.game.qipai.nxmj.bean.NxMjTable;
import com.sy599.game.qipai.nxmj.bean.NxMjiangHu;
import com.sy599.game.qipai.nxmj.constant.NxMjAction;
import com.sy599.game.qipai.nxmj.tool.NxMjQipaiTool;
import com.sy599.game.qipai.nxmj.tool.NxMjTool;
import com.sy599.game.util.DataMapUtil;

import java.util.*;
import java.util.Map.Entry;

public class NxMjRule {

	/**
	 * 小胡
	 * 
	 * @param majiangIds
	 * @return
	 *
	 */
	public static void checkXiaoHu3(NxMjiangHu hu, List<NxMj> majiangIds, boolean begin, NxMjTable table) {
		int playerNum = 4;
		if (table != null) {
			playerNum = table.getMaxPlayerCount();
		}
		NxMjAction actionData = new NxMjAction();
		NxMjIndexArr card_index = new NxMjIndexArr();
		NxMjQipaiTool.getMax(card_index, majiangIds);
		if (begin) {
			boolean findjiang = false;
			List<Integer> seList = new ArrayList<>(Arrays.asList(1, 2, 3));
			Map<Integer, List<NxMj>> huaseListMap = new HashMap<>();
			for (NxMj majiang : majiangIds) {
				if (!findjiang && majiang.isJiang()) {
					findjiang = true;
				}
				int huase = majiang.getHuase();
				if (seList.contains(huase)) {
					seList.remove((Object) huase);
				}
				if (huaseListMap.containsKey(huase)) {
					List<NxMj> list = huaseListMap.get(huase);
					list.add(majiang);
					huaseListMap.put(huase, list);
				} else {
					List<NxMj> list = new ArrayList<>();
					list.add(majiang);
					huaseListMap.put(huase, list);
				}
			}

			// 两人场要缺两门才算缺一色
			if (seList.size() == 1 && playerNum == 2 && table.getQueYiMen() == 1) {
				seList.clear();
			}

			// Set<Integer> sanTongMjVals = new HashSet<>();
			Set<Integer> sanTongMjVals = getSantong(card_index);
			Set<Integer> jiejieGaoMjVals = getJieJieGao(card_index);

			Set<Integer> jinTongYuNuMjVals = getJinTongYuNv(card_index);

			boolean yizhihua = false;
			boolean yidianhong = false;
			List<NxMj> jiangMjs = new ArrayList<>();
			for (NxMj mj : majiangIds) {
				if (mj.isJiang()) {
					jiangMjs.add(mj);
				}
			}
			if (jiangMjs.size() == 1 ) {
				yizhihua = true;
			} 
//			else {
				for (List<NxMj> list : huaseListMap.values()) {
					if (list.size() == 1 && list.get(0).getVal() % 10 == 5) {
						yizhihua = true;
						break;
					}
				}
//			}
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
			// 一枝花：满足以下任意一种情况：起牌后，玩家手牌中只有一张将牌，且这张将牌数字为“5”，即可胡牌（等同小胡自摸）。起牌后，玩家手牌中某花色只有一张，且这张将牌数字为“5”，即可胡牌（等同小胡自摸）。
			if (yizhihua) {
				actionData.addYiZhiHua();
				hu.setYizhihua(true);
				hu.setShowMajiangs(majiangIds);
			}
			if (yidianhong) {
				actionData.addYiDianHong();
				hu.setYidianhong(true);
				hu.setShowMajiangs(majiangIds);
			}
			// 六六顺 起牌后玩家手上有两个刻字
			NxMjIndex index2 = card_index.getMajiangIndex(2);
			NxMjIndex index3 = card_index.getMajiangIndex(3);
			int count3 = index2 != null ? index2.getLength() : 0;
			int count4 = index3 != null ? index3.getLength() : 0;
			if (count3 + count4 > 1) {
				actionData.addLiuLiuShun();
				hu.setLiuliushun(true);
				if (index2 != null) {
					hu.setShowMajiangs(index2.getMajiangs());
				}
				if (index3 != null) {
					hu.setShowMajiangs(index3.getMajiangs());
				}
			}
			// 大四喜 起牌后玩家手上有4张一样的牌
			if (index3 != null) {
				actionData.addDaSiXi();
				hu.setDasixi(true);
				hu.setShowMajiangs(index3.getMajiangs());
			}
			// 金童玉女：起手一对二筒和一对二条，且仅限这两张牌
			if (jinTongYuNuMjVals.size() == 2) {
				actionData.addJingTongYuNu();
				hu.setJinTongYuNu(true);
				List<Integer> vals = new ArrayList<>(jinTongYuNuMjVals);
				hu.setShowMajiangs(NxMjQipaiTool.findMajiangByValsAndCount(majiangIds, vals, 2));
			}
			// 节节高：起牌后，玩家手上有3连对将且同花色，列如：2个1万，2个2万，2个3万，即可胡牌（等同小胡自摸）
			if (!jiejieGaoMjVals.isEmpty()) {
				actionData.addJieJieGao();
				hu.setJiejiegao(true);
				List<Integer> vals = new ArrayList<>(jiejieGaoMjVals);
				hu.setShowMajiangs(NxMjQipaiTool.findMajiangByVals(majiangIds, vals));
			}
			// 三同：起牌后，玩家手上有3对点数相同的3门牌，列如2个1万，2个1筒，2个1条，即可胡牌（等同小胡自摸）
			if (!sanTongMjVals.isEmpty()) {
				actionData.addSanTong();
				hu.setSantong(true);
				List<Integer> vals = new ArrayList<>(sanTongMjVals);
				hu.setShowMajiangs(NxMjQipaiTool.findMajiangByVals(majiangIds, vals));
			}

		} else {
			// 中途四喜
			NxMjIndex index3 = card_index.getMajiangIndex(3);
			if (index3 != null) {
				actionData.addZhongTuSiXi();
				hu.setZhongtusixi(true);
				hu.setShowMajiangs(index3.getMajiangs());
			}
			// 中途六六顺
			NxMjIndex index2 = card_index.getMajiangIndex(2);
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
	}

	/**
	 * 小胡
	 * 
	 * @param majiangIds
	 * @return
	 *
	 */
	public static void checkXiaoHu2(NxMjiangHu hu, List<NxMj> majiangIds, boolean begin, NxMjTable table) {
		int playerNum = 4;
		if (table != null) {
			playerNum = table.getMaxPlayerCount();
		}
		NxMjAction actionData = new NxMjAction();
		NxMjIndexArr card_index = new NxMjIndexArr();
		NxMjQipaiTool.getMax(card_index, majiangIds);
		if (begin) {
			boolean findjiang = false;
			List<Integer> seList = new ArrayList<>(Arrays.asList(1, 2, 3));
			Map<Integer, List<NxMj>> huaseListMap = new HashMap<>();
			for (NxMj majiang : majiangIds) {
				if (!findjiang && majiang.isJiang()) {
					findjiang = true;
				}
				int huase = majiang.getHuase();
				if (seList.contains(huase)) {
					seList.remove((Object) huase);
				}
				if (huaseListMap.containsKey(huase)) {
					List<NxMj> list = huaseListMap.get(huase);
					list.add(majiang);
					huaseListMap.put(huase, list);
				} else {
					List<NxMj> list = new ArrayList<>();
					list.add(majiang);
					huaseListMap.put(huase, list);
				}
			}

			// 两人场要缺两门才算缺一色
			if (seList.size() == 1 && playerNum == 2 && table.getQueYiMen() == 1) {
				seList.clear();
			}

			// Set<Integer> sanTongMjVals = new HashSet<>();

			boolean yizhihua = false;
			boolean yidianhong = false;
			List<NxMj> jiangMjs = new ArrayList<>();
			for (NxMj mj : majiangIds) {
				if (mj.isJiang()) {
					jiangMjs.add(mj);
				}
			}
			if (jiangMjs.size() == 1) {
				yidianhong = true;
			}
//			else {
				for (List<NxMj> list : huaseListMap.values()) {
					if (list.size() == 1 && list.get(0).getVal() % 10 == 5) {
						yizhihua = true;
						break;
					}
				}
//			}
			// 缺一色 起牌后玩家手上任缺一门
			if (!seList.isEmpty()) {
				actionData.addQueYiSe();
				hu.setQueyise(true);
				hu.setShowMajiangs(majiangIds);
				hu.addXiaoHu(NxMjAction.QUEYISE, new HashMap<Integer, List<NxMj>>());
			}
			// 板板胡 起牌后玩家手上没有一张258将牌
			if (!findjiang) {
				actionData.addBanBanHu();
				hu.setBanbanhu(true);
				hu.setShowMajiangs(majiangIds);
				hu.addXiaoHu(NxMjAction.BANBANHU, new HashMap<Integer, List<NxMj>>());
			}
			// 一枝花：满足以下任意一种情况：起牌后，玩家手牌中只有一张将牌，且这张将牌数字为“5”，即可胡牌（等同小胡自摸）。起牌后，玩家手牌中某花色只有一张，且这张将牌数字为“5”，即可胡牌（等同小胡自摸）。
			if (yizhihua) {
				actionData.addYiZhiHua();
				hu.setYizhihua(true);
				hu.addXiaoHu(NxMjAction.YIZHIHUA, new HashMap<Integer, List<NxMj>>());
				hu.setShowMajiangs(majiangIds);
			}
			// 一点红：满足以下任意一种情况：起牌后，玩家手牌中只有一张将牌，且这张将牌数字为“5”，即可胡牌（等同小胡自摸）。起牌后，玩家手牌中某花色只有一张，且这张将牌数字为“5”，即可胡牌（等同小胡自摸）。
			if (yidianhong) {
				actionData.addYiDianHong();
				hu.setYidianhong(true);
				hu.addXiaoHu(NxMjAction.YIDIANHONG, new HashMap<Integer, List<NxMj>>());
				hu.setShowMajiangs(majiangIds);
			}

			// 六六顺 起牌后玩家手上有两个刻字

			NxMjIndex index3 = card_index.getMajiangIndex(3);

			// 大四喜 起牌后玩家手上有4张一样的牌
			if (index3 != null) {
				actionData.addDaSiXi();
				hu.addXiaoHu(NxMjAction.DASIXI, index3.getMajiangValMap());
				hu.setDasixi(true);
				hu.setShowMajiangs(index3.getMajiangs());

				// 删除掉
				//card_index = removeMj(majiangIds, index3);
			}

			NxMjIndex index2 = card_index.getMajiangIndex(2);

			int count3 = index2 != null ? index2.getLength() : 0;
			if(index3!=null){
				count3+=index3.getLength();
			}
			if (count3 > 1) {
				actionData.addLiuLiuShun();
				hu.setLiuliushun(true);
				
				
				Map<Integer, List<NxMj>> map = new HashMap<>();
				if(index2!=null&&index2.getLength()<2){
					//hu.setShowMajiangs(index3.getMajiangs());
				}
				if (index2 != null) {
					hu.setShowMajiangs(index2.getMajiangs());
					
					Map<Integer, List<NxMj>> map2 = index2.getMajiangValMap();
					for (Entry<Integer, List<NxMj>> entry : map2.entrySet()) { 
//						if(map2.size()<2){
						if(table.checkXiaoHuCards(NxMjAction.ZHONGTULIULIUSHUN,entry.getKey())){
							continue;
						}
						map.put(entry.getKey(), entry.getValue());
//						}
					}
					
				}
				
				
				
				if(index3!=null){
					Map<Integer, List<NxMj>> map3 = index3.getMajiangValMap();
					for (Entry<Integer, List<NxMj>> entry : map3.entrySet()) { 
//						if(map2.size()<2){
						if(table.checkXiaoHuCards(NxMjAction.ZHONGTULIULIUSHUN,entry.getKey())){
							continue;
						}
						 List<NxMj> valus = new ArrayList<>(entry.getValue());
						 valus.remove(0);
						map.put(entry.getKey(), valus);
//						}
					}
				}
				if(map.size()>1){
					int count = 0;
					Map<Integer, List<NxMj>> map2 = new HashMap<>();
					int key = 0;
					for (Entry<Integer, List<NxMj>> entry : map.entrySet()) { 
						count++;
						if (count % 2 == 1) {
							key = entry.getKey();
							if (count3 == 3 && count == 3) {
								break;
							}
							// List<CsMj> valus = new ArrayList<>(entry.getValue());
							map2.put(entry.getKey(), entry.getValue());
						} else if (count % 2 == 0) {
							List<NxMj> list = map2.get(key);
							if (list != null) {
								list.addAll(entry.getValue());
							}
						}
					}
					hu.addXiaoHu(NxMjAction.LIULIUSHUN, map2);
				}
			
				//card_index = removeMj(majiangIds, index2);

			}

			Set<Integer> jinTongYuNuMjVals = getJinTongYuNv(card_index);

			// 金童玉女：起手一对二筒和一对二条，且仅限这两张牌
			if (jinTongYuNuMjVals.size() == 2) {
				actionData.addJingTongYuNu();
				hu.setJinTongYuNu(true);
				List<Integer> vals = new ArrayList<>(jinTongYuNuMjVals);

				List<NxMj> list = NxMjQipaiTool.findMajiangByValsAndCount(majiangIds, vals, 2);
				Map<Integer, List<NxMj>> map2 = new HashMap<>();
				map2.put(list.get(0).getVal(), list);
				hu.addXiaoHu(NxMjAction.JINGTONGYUNU, map2);

				// 删除掉
				//majiangIds.removeAll(list);
				card_index = new NxMjIndexArr();
				NxMjQipaiTool.getMax(card_index, majiangIds);
				// hu.setShowMajiangs();
			}

			Set<Integer> jiejieGaoMjVals = getJieJieGao(card_index);

			// 节节高：起牌后，玩家手上有3连对将且同花色，列如：2个1万，2个2万，2个3万，即可胡牌（等同小胡自摸）
			if (!jiejieGaoMjVals.isEmpty()) {
				actionData.addJieJieGao();
				hu.setJiejiegao(true);

				List<Integer> vals = new ArrayList<>(jiejieGaoMjVals);

				List<NxMj> list = NxMjQipaiTool.findMajiangByValsAndCount(majiangIds, vals, 2);
				Map<Integer, List<NxMj>> map2 = new HashMap<>();
				map2.put(list.get(0).getVal(), list);
				hu.addXiaoHu(NxMjAction.JIEJIEGAO, map2);

				// 删除掉
				//majiangIds.removeAll(list);
				card_index = new NxMjIndexArr();
				NxMjQipaiTool.getMax(card_index, majiangIds);

				// 再查一遍节节高
				jiejieGaoMjVals = getJieJieGao(card_index);

				if (!jiejieGaoMjVals.isEmpty()) {
					List<Integer> vals2 = new ArrayList<>(jiejieGaoMjVals);
					List<NxMj> list2 = NxMjQipaiTool.findMajiangByValsAndCount(majiangIds, vals2, 2);
					map2.put(list2.get(0).getVal(), list2);

					hu.addXiaoHu(NxMjAction.JIEJIEGAO, map2);

					// 删除掉
					//majiangIds.removeAll(list2);
					card_index = new NxMjIndexArr();
					NxMjQipaiTool.getMax(card_index, majiangIds);

				}

				// hu.setShowMajiangs(CsMjQipaiTool.findMajiangByVals(majiangIds,
				// vals));
				// CsMjQipaiTool.findMajiangByValsAndCount(handPais, vals,2);
			}

			Set<Integer> sanTongMjVals = getSantong(card_index);
			// 三同：起牌后，玩家手上有3对点数相同的3门牌，列如2个1万，2个1筒，2个1条，即可胡牌（等同小胡自摸）
			if (!sanTongMjVals.isEmpty()) {
				actionData.addSanTong();
				hu.setSantong(true);

				List<Integer> vals = new ArrayList<>(sanTongMjVals);

				List<NxMj> list = NxMjQipaiTool.findMajiangByValsAndCount(majiangIds, vals, 2);
				Map<Integer, List<NxMj>> map2 = new HashMap<>();
				map2.put(list.get(0).getVal(), list);
				hu.addXiaoHu(NxMjAction.SANTONG, map2);

				// 删除掉
				majiangIds.removeAll(list);
				card_index = new NxMjIndexArr();
				NxMjQipaiTool.getMax(card_index, majiangIds);

				sanTongMjVals = getSantong(card_index);

				if (!sanTongMjVals.isEmpty()) {
					List<Integer> vals2 = new ArrayList<>(sanTongMjVals);
					List<NxMj> list2 = NxMjQipaiTool.findMajiangByValsAndCount(majiangIds, vals2, 2);
					map2.put(list2.get(0).getVal(), list2);
					hu.addXiaoHu(NxMjAction.SANTONG, map2);
				}
			}
		} else {
			// 中途四喜
			NxMjIndex index3 = card_index.getMajiangIndex(3);
			if (index3 != null) {
				actionData.addZhongTuSiXi();
				hu.setZhongtusixi(true);
				hu.setShowMajiangs(index3.getMajiangs());
				hu.addXiaoHu(NxMjAction.ZHONGTUSIXI, index3.getMajiangValMap());
				//card_index = removeMj(majiangIds, index3);
			}

			// 中途六六顺
			NxMjIndex index2 = card_index.getMajiangIndex(2);

			int count3 = index2 != null ? index2.getLength() : 0;
			if(index3!=null){
				count3+=index3.getLength();
			}
			if (count3 > 1) {
				Map<Integer, List<NxMj>> map = new HashMap<>();
				if(index2!=null&&index2.getLength()<2){
				//	hu.setShowMajiangs(index3.getMajiangs());
				}
				if (index2 != null) {
					hu.setShowMajiangs(index2.getMajiangs());
					
					Map<Integer, List<NxMj>> map2 = index2.getMajiangValMap();
					for (Entry<Integer, List<NxMj>> entry : map2.entrySet()) { 
//						if(map2.size()<2){
						if(table.checkXiaoHuCards(NxMjAction.ZHONGTULIULIUSHUN,entry.getKey())){
							continue;
						}
						map.put(entry.getKey(), entry.getValue());
//						}
					}
				}
				
				if(index3!=null){
					Map<Integer, List<NxMj>> map3 = index3.getMajiangValMap();
					for (Entry<Integer, List<NxMj>> entry : map3.entrySet()) { 
//						if(map2.size()<2){
						if(table.checkXiaoHuCards(NxMjAction.ZHONGTULIULIUSHUN,entry.getKey())){
							continue;
						}
						 List<NxMj> valus = new ArrayList<>(entry.getValue());
						 valus.remove(0);
						map.put(entry.getKey(),valus);
//						}
					}
				}
				
				if(map.size()>1){
					actionData.addZhongTuLiuLiuShun();
					hu.setZhongtuliuliushun(true);
					//hu.setShowMajiangs(index2.getMajiangs());
					int count = 0;
					Map<Integer, List<NxMj>> map2 = new HashMap<>();
					int key = 0;
					for (Entry<Integer, List<NxMj>> entry : map.entrySet()) {
						count++;
						if (count % 2 == 1) {
							key = entry.getKey();
							// 三个三个的算1次index2.getLength() == 3 && 
							if (count == 3) {
								break;
							}
							 List<NxMj> valus = new ArrayList<>(entry.getValue());
								map2.put(entry.getKey(), valus);
						} else if (count % 2 == 0) {
							List<NxMj> list = map2.get(key);
							if (list != null) {
								list.addAll(entry.getValue());
							}
						}
					}
					hu.addXiaoHu(NxMjAction.ZHONGTULIULIUSHUN, map2);
				}

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
	}

	private static NxMjIndexArr removeMj(List<NxMj> majiangIds, NxMjIndex index3) {
		NxMjIndexArr card_index;
		List<NxMj> list = new ArrayList<NxMj>();
		// list.addAll(index3.getMajiangValMap().values());
		for (List<NxMj> mjs : index3.getMajiangValMap().values()) {
			list.addAll(mjs);
		}

		majiangIds.removeAll(list);
		card_index = new NxMjIndexArr();
		NxMjQipaiTool.getMax(card_index, majiangIds);
		return card_index;
	}

	public static Set<Integer> getJinTongYuNv(NxMjIndexArr card_index) {
		Set<Integer> jinTongYuNuMjVals = new HashSet<>();
		for (int i = 1; i <= 3; i++) {
			NxMjIndex jinTongYuNuIndex = card_index.getMajiangIndex(i);// 对子数量
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

	public static Set<Integer> getJieJieGao(NxMjIndexArr card_index) {
		Set<Integer> jiejieGaoMjVals = new HashSet<>();
		Set<Integer> rset = new HashSet<>();
		Map<Integer, Set<Integer>> huaseMap = new HashMap<>();
		for (int i = 1; i <= 3; i++) {
			NxMjIndex jiejieGaoIndex = card_index.getMajiangIndex(i);// 对子数量
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

	public static Set<Integer> getSantong(NxMjIndexArr card_index) {
		Set<Integer> sanTongMjVals = new HashSet<>();
		Map<Integer, Set<Integer>> valMap = new HashMap<>();
		for (int i = 1; i <= 3; i++) {
			NxMjIndex sanTongIndex = card_index.getMajiangIndex(i);// 对子数量
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
	public static int[] checkDahu(NxMjiangHu hu, List<NxMj> majiangIds, List<NxMj> gang, List<NxMj> peng,
			List<NxMj> chi, List<NxMj> buzhang) {
		List<NxMj> allMajiangs = new ArrayList<>();
		allMajiangs.addAll(majiangIds);
		allMajiangs.addAll(gang);
		allMajiangs.addAll(peng);
		allMajiangs.addAll(chi);
		allMajiangs.addAll(buzhang);
		int arr[] = new int[7];
		if (majiangIds.size() % 3 != 2) {
			return arr;
		}

		NxMjIndexArr all_card_index = new NxMjIndexArr();
		NxMjQipaiTool.getMax(all_card_index, allMajiangs);

		// 1碰碰胡
		if (isPengPengHu(majiangIds, all_card_index)&&chi.size()==0) {
			arr[0] = 1;
			hu.setPengpengHu(true);
		}
		// 2将将胡
		if (isJiangJiangHu(allMajiangs)) {
			arr[1] = 1;

			hu.setJiangjiangHu(true);
		}
		// 3清一色
		if (isqingyiseHu(allMajiangs, majiangIds)) {
			arr[2] = 1;

			hu.setQingyiseHu(true);
		}

		// 先去掉红中
		List<NxMj> copy = new ArrayList<>(majiangIds);
		int hongzhongNum = NxMjTool.dropHongzhong(copy).size();

		NxMjIndexArr card_index = new NxMjIndexArr();
		NxMjQipaiTool.getMax(card_index, copy);
		// 4 7小对
		if (card_index.getDuiziNum() == 6 && hongzhongNum > 0) {
			arr[5] = 1;
		} else if (card_index.getDuiziNum() == 7) {
			// 是否有豪华7小对
			NxMjIndex index = card_index.getMajiangIndex(3);
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

		// 6全求人
		if (majiangIds.size() == 2) {
			boolean haveHz = false;
			for (NxMj majiang : majiangIds) {
				if (majiang.isHongzhong()) {
					haveHz = true;
					break;
				}

			}

			// 两张牌只要有一个红中算可以胡了
			if (haveHz || majiangIds.get(0).getVal() == majiangIds.get(1).getVal()) {
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

	private static boolean isqingyiseHu(List<NxMj> allMajiangs, List<NxMj> majiangIds) {
		boolean qingyise = checkqingyise(allMajiangs);
		if (qingyise) {
			boolean hu = NxMjTool.isPingHu(majiangIds, false);
			return hu;
		}

		return false;
	}

	public static boolean checkqingyise(List<NxMj> allMajiangs) {
		boolean qingyise = false;
		int se = 0;
		for (NxMj mjiang : allMajiangs) {
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

	private static boolean isJiangJiangHu(List<NxMj> majiangIds) {
		boolean jiangjianghu = true;
		for (NxMj mjiang : majiangIds) {
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

	private static boolean isPengPengHu(List<NxMj> majiangIds, NxMjIndexArr card_index) {
		NxMjIndex index4 = card_index.getMajiangIndex(3);
		NxMjIndex index3 = card_index.getMajiangIndex(2);
		NxMjIndex index2 = card_index.getMajiangIndex(1);
		NxMjIndex index1 = card_index.getMajiangIndex(0);

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
		} else if (majiangIds.contains(NxMj.getMajang(201)) && index4 != null && index4.getLength() == 2
				&& index1 != null && index1.getLength() == 1 && index2 == null) {
			return true;
		} else if (majiangIds.contains(NxMj.getMajang(201)) && sameCount >= 3 && index2 == null && index1 != null
				&& index1.getLength() == 2) {
			return true;
		} else if (majiangIds.contains(NxMj.getMajang(201)) && sameCount >= 2 && index2 != null
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
