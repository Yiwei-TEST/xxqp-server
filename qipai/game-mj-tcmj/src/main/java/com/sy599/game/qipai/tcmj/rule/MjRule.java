package com.sy599.game.qipai.tcmj.rule;

import com.sy599.game.qipai.tcmj.bean.GameModel;
import com.sy599.game.qipai.tcmj.bean.MjiangHu;
import com.sy599.game.qipai.tcmj.constant.MjAction;
import com.sy599.game.qipai.tcmj.constant.MjConstants;
import com.sy599.game.qipai.tcmj.tool.MjTool;
import com.sy599.game.qipai.tcmj.tool.MjQipaiTool;
import com.sy599.game.qipai.tcmj.bean.TcMjPlayer;
import com.sy599.game.qipai.tcmj.bean.TcMjTable;
import com.sy599.game.util.DataMapUtil;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MjRule {

	/**
	 * 小胡
	 * 
	 * @param majiangIds
	 * @return
	 *
	 */
	public static void checkXiaoHu3(MjiangHu hu, List<Mj> majiangIds, boolean begin, TcMjTable table) {
		int playerNum = 4;
		if (table != null) {
			playerNum = table.getMaxPlayerCount();
		}
		MjAction actionData = new MjAction();
		MjIndexArr card_index = new MjIndexArr();
		MjQipaiTool.getMax(card_index, majiangIds);
		if (begin) {
			boolean findjiang = false;
			List<Integer> seList = new ArrayList<>(Arrays.asList(1, 2, 3));
			Map<Integer, List<Mj>> huaseListMap = new HashMap<>();
			for (Mj majiang : majiangIds) {
				if (!findjiang && majiang.isJiang()) {
					findjiang = true;
				}
				int huase = majiang.getHuase();
				if (seList.contains(huase)) {
					seList.remove((Object) huase);
				}
				if (huaseListMap.containsKey(huase)) {
					List<Mj> list = huaseListMap.get(huase);
					list.add(majiang);
					huaseListMap.put(huase, list);
				} else {
					List<Mj> list = new ArrayList<>();
					list.add(majiang);
					huaseListMap.put(huase, list);
				}
			}

			// 两人场要缺两门才算缺一色
			if (seList.size() == 1 && playerNum == 2 && table.getGameModel().getSpecialPlay().isQueYiMen()) {
				seList.clear();
			}

			// Set<Integer> sanTongMjVals = new HashSet<>();
			Set<Integer> sanTongMjVals = getSantong(card_index);
			Set<Integer> jiejieGaoMjVals = getJieJieGao(card_index);

			Set<Integer> jinTongYuNuMjVals = getJinTongYuNv(card_index);

			boolean yizhihua = false;
			List<Mj> jiangMjs = new ArrayList<>();
			for (Mj mj : majiangIds) {
				if (mj.isJiang()) {
					jiangMjs.add(mj);
				}
			}
			if (jiangMjs.size() == 1 && jiangMjs.get(0).getVal() % 10 == 5) {
				yizhihua = true;
			} else {
				for (List<Mj> list : huaseListMap.values()) {
					if (list.size() == 1 && list.get(0).getVal() % 10 == 5) {
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
				hu.setHeitianhu(true);
				hu.setShowMajiangs(majiangIds);
			}
			// 一枝花：满足以下任意一种情况：起牌后，玩家手牌中只有一张将牌，且这张将牌数字为“5”，即可胡牌（等同小胡自摸）。起牌后，玩家手牌中某花色只有一张，且这张将牌数字为“5”，即可胡牌（等同小胡自摸）。
			if (yizhihua) {
				actionData.addYiZhiHua();
				hu.setYizhihua(true);
				hu.setShowMajiangs(majiangIds);
			}
			// 六六顺 起牌后玩家手上有两个刻字
			MjIndex index2 = card_index.getMajiangIndex(2);
			MjIndex index3 = card_index.getMajiangIndex(3);
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
				hu.setShowMajiangs(MjQipaiTool.findMajiangByValsAndCount(majiangIds, vals, 2));
			}
			// 节节高：起牌后，玩家手上有3连对将且同花色，列如：2个1万，2个2万，2个3万，即可胡牌（等同小胡自摸）
			if (!jiejieGaoMjVals.isEmpty()) {
				actionData.addJieJieGao();
				hu.setJiejiegao(true);
				List<Integer> vals = new ArrayList<>(jiejieGaoMjVals);
				hu.setShowMajiangs(MjQipaiTool.findMajiangByVals(majiangIds, vals));
			}
			// 三同：起牌后，玩家手上有3对点数相同的3门牌，列如2个1万，2个1筒，2个1条，即可胡牌（等同小胡自摸）
			if (!sanTongMjVals.isEmpty()) {
				actionData.addSanTong();
				hu.setSantong(true);
				List<Integer> vals = new ArrayList<>(sanTongMjVals);
				hu.setShowMajiangs(MjQipaiTool.findMajiangByVals(majiangIds, vals));
			}

		} else {
			// 中途四喜
			MjIndex index3 = card_index.getMajiangIndex(3);
			if (index3 != null) {
				actionData.addZhongTuSiXi();
				hu.setZhongtusixi(true);
				hu.setShowMajiangs(index3.getMajiangs());
			}
			// 中途六六顺
			MjIndex index2 = card_index.getMajiangIndex(2);
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
	public static void checkXiaoHu2(MjiangHu hu, List<Mj> majiangIds, boolean begin, TcMjTable table, TcMjPlayer player) {
		int playerNum = 4;
		if (table != null) {
			playerNum = table.getMaxPlayerCount();
		}
		MjAction actionData = new MjAction();
		MjIndexArr card_index = new MjIndexArr();
		MjQipaiTool.getMax(card_index, majiangIds);
		if (begin) {
			boolean findjiang = false;
			List<Integer> seList = new ArrayList<>(Arrays.asList(1, 2, 3));
			Map<Integer, List<Mj>> huaseListMap = new HashMap<>();
			for (Mj majiang : majiangIds) {
				if (!findjiang && majiang.isJiang()) {
					findjiang = true;
				}
				int huase = majiang.getHuase();
				if (seList.contains(huase)) {
					seList.remove((Object) huase);
				}
				if (huaseListMap.containsKey(huase)) {
					List<Mj> list = huaseListMap.get(huase);
					list.add(majiang);
					huaseListMap.put(huase, list);
				} else {
					List<Mj> list = new ArrayList<>();
					list.add(majiang);
					huaseListMap.put(huase, list);
				}
			}

			// 两人场要缺两门才算缺一色
			if (seList.size() == 1 && playerNum == 2 && table.getGameModel().getSpecialPlay().isQueYiMen()) {
				seList.clear();
			}

			// Set<Integer> sanTongMjVals = new HashSet<>();

			boolean yizhihua = false;
			List<Mj> jiangMjs = new ArrayList<>();
			for (Mj mj : majiangIds) {
				if (mj.isJiang()) {
					jiangMjs.add(mj);
				}
			}
			if (jiangMjs.size() == 1 && jiangMjs.get(0).getVal() % 10 == 5) {
				yizhihua = true;
			} else {
				for (List<Mj> list : huaseListMap.values()) {
					if (list.size() == 1 && list.get(0).getVal() % 10 == 5) {
						yizhihua = true;
						break;
					}
				}
			}
			// 缺一色 起牌后玩家手上任缺一门
			if (table.getGameModel().getSpecialPlay().isQueYiSe() && !seList.isEmpty()) {
				actionData.addQueYiSe();
				hu.setQueyise(true);
				hu.setShowMajiangs(majiangIds);
				hu.addXiaoHu(MjAction.QUEYISE, new HashMap<Integer, List<Mj>>());
			}
			// 黑天胡 起牌后玩家手上没有一张258将牌
			if (table.getGameModel().getSpecialPlay().isBlackSkyHu() && player.isFirstDisCard() && !findjiang) {
				actionData.addBanBanHu();
				hu.setHeitianhu(true);
				hu.setShowMajiangs(majiangIds);
				hu.addXiaoHu(MjAction.BANBANHU, new HashMap<Integer, List<Mj>>());
			}
			// 一枝花：满足以下任意一种情况：起牌后，玩家手牌中只有一张将牌，且这张将牌数字为“5”，即可胡牌（等同小胡自摸）。起牌后，玩家手牌中某花色只有一张，且这张将牌数字为“5”，即可胡牌（等同小胡自摸）。
			if (table.getGameModel().getSpecialPlay().isYiZhiHua() && yizhihua) {
				actionData.addYiZhiHua();
				hu.setYizhihua(true);
				hu.addXiaoHu(MjAction.YIZHIHUA, new HashMap<Integer, List<Mj>>());
				hu.setShowMajiangs(majiangIds);
			}

			// 六六顺 起牌后玩家手上有两个刻字

			MjIndex index3 = card_index.getMajiangIndex(3);

			// 大四喜 起牌后玩家手上有4张一样的牌
			if (table.getGameModel().getSpecialPlay().isDaSiXi() && index3 != null) {
				actionData.addDaSiXi();
				hu.addXiaoHu(MjAction.DASIXI, index3.getMajiangValMap());
				hu.setDasixi(true);
				hu.setShowMajiangs(index3.getMajiangs());

				// 删除掉
				card_index = removeMj(majiangIds, index3);
			}

			MjIndex index2 = card_index.getMajiangIndex(2);

			int count3 = index2 != null ? index2.getLength() : 0;
			if (count3 > 1) {
				actionData.addLiuLiuShun();
				hu.setLiuliushun(true);
				if (index2 != null) {
					hu.setShowMajiangs(index2.getMajiangs());
				}

				Map<Integer, List<Mj>> map = index2.getMajiangValMap();

				int count = 0;
				Map<Integer, List<Mj>> map2 = new HashMap<>();
				int key = 0;
				for (Entry<Integer, List<Mj>> entry : map.entrySet()) {
					count++;
					if (count % 2 == 1) {
						key = entry.getKey();
						if (count3 == 3 && count == 3) {
							break;
						}
						map2.put(entry.getKey(), entry.getValue());
					} else if (count % 2 == 0) {
						List<Mj> list = map2.get(key);
						if (list != null) {
							list.addAll(entry.getValue());
						}
					}
				}

				hu.addXiaoHu(MjAction.LIULIUSHUN, map2);

				card_index = removeMj(majiangIds, index2);

			}

			Set<Integer> jinTongYuNuMjVals = getJinTongYuNv(card_index);

			// 金童玉女：起手一对二筒和一对二条，且仅限这两张牌
			if (table.getGameModel().getSpecialPlay().isJinTongYuNv() && jinTongYuNuMjVals.size() == 2) {
				actionData.addJingTongYuNu();
				hu.setJinTongYuNu(true);
				List<Integer> vals = new ArrayList<>(jinTongYuNuMjVals);

				List<Mj> list = MjQipaiTool.findMajiangByValsAndCount(majiangIds, vals, 2);
				Map<Integer, List<Mj>> map2 = new HashMap<>();
				map2.put(list.get(0).getVal(), list);
				hu.addXiaoHu(MjAction.JINGTONGYUNU, map2);

				// 删除掉
				majiangIds.removeAll(list);
				card_index = new MjIndexArr();
				MjQipaiTool.getMax(card_index, majiangIds);
				// hu.setShowMajiangs();
			}

			Set<Integer> jiejieGaoMjVals = getJieJieGao(card_index);

			// 节节高：起牌后，玩家手上有3连对将且同花色，列如：2个1万，2个2万，2个3万，即可胡牌（等同小胡自摸）
			if (table.getGameModel().getSpecialPlay().isJieJieGao() && !jiejieGaoMjVals.isEmpty()) {
				actionData.addJieJieGao();
				hu.setJiejiegao(true);

				List<Integer> vals = new ArrayList<>(jiejieGaoMjVals);

				List<Mj> list = MjQipaiTool.findMajiangByValsAndCount(majiangIds, vals, 2);
				Map<Integer, List<Mj>> map2 = new HashMap<>();
				map2.put(list.get(0).getVal(), list);
				hu.addXiaoHu(MjAction.JIEJIEGAO, map2);

				// 删除掉
				majiangIds.removeAll(list);
				card_index = new MjIndexArr();
				MjQipaiTool.getMax(card_index, majiangIds);

				// 再查一遍节节高
				jiejieGaoMjVals = getJieJieGao(card_index);

				if (!jiejieGaoMjVals.isEmpty()) {
					List<Integer> vals2 = new ArrayList<>(jiejieGaoMjVals);
					List<Mj> list2 = MjQipaiTool.findMajiangByValsAndCount(majiangIds, vals2, 2);
					map2.put(list2.get(0).getVal(), list2);

					hu.addXiaoHu(MjAction.JIEJIEGAO, map2);

					// 删除掉
					majiangIds.removeAll(list2);
					card_index = new MjIndexArr();
					MjQipaiTool.getMax(card_index, majiangIds);

				}

				// hu.setShowMajiangs(CsMjQipaiTool.findMajiangByVals(majiangIds,
				// vals));
				// CsMjQipaiTool.findMajiangByValsAndCount(handPais, vals,2);
			}

			Set<Integer> sanTongMjVals = getSantong(card_index);
			// 三同：起牌后，玩家手上有3对点数相同的3门牌，列如2个1万，2个1筒，2个1条，即可胡牌（等同小胡自摸）
			if (table.getGameModel().getSpecialPlay().isSanTong() && !sanTongMjVals.isEmpty()) {
				actionData.addSanTong();
				hu.setSantong(true);

				List<Integer> vals = new ArrayList<>(sanTongMjVals);

				List<Mj> list = MjQipaiTool.findMajiangByValsAndCount(majiangIds, vals, 2);
				Map<Integer, List<Mj>> map2 = new HashMap<>();
				map2.put(list.get(0).getVal(), list);
				hu.addXiaoHu(MjAction.SANTONG, map2);

				// 删除掉
				majiangIds.removeAll(list);
				card_index = new MjIndexArr();
				MjQipaiTool.getMax(card_index, majiangIds);

				sanTongMjVals = getSantong(card_index);

				if (!sanTongMjVals.isEmpty()) {
					List<Integer> vals2 = new ArrayList<>(sanTongMjVals);
					List<Mj> list2 = MjQipaiTool.findMajiangByValsAndCount(majiangIds, vals2, 2);
					map2.put(list2.get(0).getVal(), list2);
					hu.addXiaoHu(MjAction.SANTONG, map2);
				}
			}
		} else {
			// 中途四喜
			MjIndex index3 = card_index.getMajiangIndex(3);
			if (table.getGameModel().getSpecialPlay().isZhongTuSiXi() && index3 != null) {
				actionData.addZhongTuSiXi();
				hu.setZhongtusixi(true);
				hu.setShowMajiangs(index3.getMajiangs());
				hu.addXiaoHu(MjAction.ZHONGTUSIXI, index3.getMajiangValMap());
				card_index = removeMj(majiangIds, index3);
			}

			// 中途六六顺
			MjIndex index2 = card_index.getMajiangIndex(2);
			if (table.getGameModel().getSpecialPlay().isZhongTuLiuLiuShun() && index2 != null && index2.getLength() > 1) {
				actionData.addZhongTuLiuLiuShun();
				hu.setZhongtuliuliushun(true);
				hu.setShowMajiangs(index2.getMajiangs());
				Map<Integer, List<Mj>> map = index2.getMajiangValMap();
				int count = 0;
				Map<Integer, List<Mj>> map2 = new HashMap<>();
				int key = 0;
				for (Entry<Integer, List<Mj>> entry : map.entrySet()) {
					count++;
					if (count % 2 == 1) {
						key = entry.getKey();
						// 三个三个的算1次
						if (index2.getLength() == 3 && count == 3) {
							break;
						}
						map2.put(entry.getKey(), entry.getValue());
					} else if (count % 2 == 0) {
						List<Mj> list = map2.get(key);
						if (list != null) {
							list.addAll(entry.getValue());
						}
					}
				}
				hu.addXiaoHu(MjAction.ZHONGTULIULIUSHUN, map2);

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

	private static MjIndexArr removeMj(List<Mj> majiangIds, MjIndex index3) {
		MjIndexArr card_index;
		List<Mj> list = new ArrayList<Mj>();
		// list.addAll(index3.getMajiangValMap().values());
		for (List<Mj> mjs : index3.getMajiangValMap().values()) {
			list.addAll(mjs);
		}

		majiangIds.removeAll(list);
		card_index = new MjIndexArr();
		MjQipaiTool.getMax(card_index, majiangIds);
		return card_index;
	}

	public static Set<Integer> getJinTongYuNv(MjIndexArr card_index) {
		Set<Integer> jinTongYuNuMjVals = new HashSet<>();
		for (int i = 1; i <= 3; i++) {
			MjIndex jinTongYuNuIndex = card_index.getMajiangIndex(i);// 对子数量
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

	public static Set<Integer> getJieJieGao(MjIndexArr card_index) {
		Set<Integer> jiejieGaoMjVals = new HashSet<>();
		Set<Integer> rset = new HashSet<>();
		Map<Integer, Set<Integer>> huaseMap = new HashMap<>();
		for (int i = 1; i <= 3; i++) {
			MjIndex jiejieGaoIndex = card_index.getMajiangIndex(i);// 对子数量
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

	public static Set<Integer> getSantong(MjIndexArr card_index) {
		Set<Integer> sanTongMjVals = new HashSet<>();
		Map<Integer, Set<Integer>> valMap = new HashMap<>();
		for (int i = 1; i <= 3; i++) {
			MjIndex sanTongIndex = card_index.getMajiangIndex(i);// 对子数量
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
	 * 0 碰碰胡 1将将胡 2清一色 3双豪华7小对 4豪华7小对 5:7小对 6全求人 7黑天胡
	 * 
	 * @param majiangIds
	 * @param gang
	 * @param peng
	 * @param chi
	 * @param buzhang
	 * @return
	 */
	public static void checkDahu(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, TcMjTable table, boolean canYingZhuang, TcMjPlayer player) {
		Mj kingCard = canYingZhuang ? null : table.getKingCard();

		List<Mj> allMajiangs = new ArrayList<>();
		allMajiangs.addAll(majiangIds);
		allMajiangs.addAll(gang);
		allMajiangs.addAll(peng);
		allMajiangs.addAll(chi);
		allMajiangs.addAll(buzhang);

		//玩家当前手牌王数量
		int curkingCardNum = kingCard != null ? majiangIds.stream().filter(v -> v.getVal() == kingCard.getVal()).mapToInt(v -> 1).sum() : 0;

		//最后两张牌是王牌做平胡计算
		if (majiangIds.size() == 2) {
			//两张王牌
			if (table.getKingCard() != null && majiangIds.get(0).getVal() == table.getKingCard().getVal() && majiangIds.get(1).getVal() == table.getKingCard().getVal()) {
				hu.setHu(true);
				hu.setPingHu(true);
//				//王牌是258强行硬庄
//				if ((table.getKingCard().getPai() == 2 || table.getKingCard().getPai() == 5 || table.getKingCard().getPai() == 8)) {
//					hu.setYingZhuang(true);
//				}
			}
		}

		if (majiangIds.size() % 3 == 2) {
			MjIndexArr all_card_index = new MjIndexArr();
			MjQipaiTool.getMax(all_card_index, allMajiangs);

			//十八罗汉和七小对(不能下水)碰碰胡互斥
	//		13.十八罗汉36分 四条杠加一对将共18张牌胡牌。
			if (table.getGameModel().getSpecialPlay().isShiBaLuoHan() && checkShiBaLuoHan(hu, majiangIds, gang, peng, chi, buzhang, table, player, kingCard, curkingCardNum)) {
				hu.setShiBaLuoHan(true);
			}
			// 7小对检测
			// 1碰碰胡检测, 七小对优先, 检测失败时检测碰碰胡
			else if (!check7Dui(hu, majiangIds, gang, table, kingCard, /*arr,*/ !chi.isEmpty(), peng) && table.getGameModel().getSpecialPlay().isPpHu() && isPengPengHu(majiangIds, gang , peng, chi , buzhang, kingCard) && chi.size() == 0) {
	//			arr[0] = 1;
				hu.setPengpengHu(true);
			}

			//7黑天胡 庄家起手14张牌、闲家起手13张牌+摸第一张牌后，手牌无“王”无将无顺子无刻子。则可胡牌（不能炮胡）
			if (table.getGameModel().getSpecialPlay().isBlackSkyHu() && player.isFirstDisCard() && isHeiTianHu(allMajiangs, majiangIds, kingCard, table.getGameModel())) {
	//			arr[7] = 1;
				hu.setHeitianhu(true);
			}

	//		5.十三幺26分 满足19条+19筒+19万+东南西北中发白牌型之后任意一张成对就能胡牌
			if (table.getGameModel().getSpecialPlay().isShiSanYao() && checkShiSanYao(hu, majiangIds, gang, peng, chi, buzhang, table, player, kingCard, curkingCardNum)) {
				hu.setShiSanYao(true);
			}

			// 2将将胡
			if (table.getGameModel().getSpecialPlay().isJjHu() && isJiangJiangHu(allMajiangs, kingCard)) {
	//			arr[1] = 1;

				hu.setJiangjiangHu(true);
			}

	//		12.字一色20分 只有东南西北中发白。
			if (table.getGameModel().getSpecialPlay().isZiYiSe() && checkZiYiSe(hu, majiangIds, gang, peng, chi, buzhang, table, player, kingCard, curkingCardNum, allMajiangs)) {
				hu.setZiYiSe(true);
			}
			// 3清一色
			else if (table.getGameModel().getSpecialPlay().isAllOfTheSameColor() && isQingYiSeHu(allMajiangs, majiangIds, kingCard)) {
	//			arr[2] = 1;
				hu.setQingyiseHu(true);
			}
			//10.混一色4分 清一色加字牌。
			else if (table.getGameModel().getSpecialPlay().isHunYiSe() && checkHunYiSe(hu, majiangIds, gang, peng, chi, buzhang, table, player, kingCard, curkingCardNum, allMajiangs)) {
				hu.setHunYiSe(true);
			}

			//		15.清一九20分 只有一九组成的刻子加一对将。
			if (table.getGameModel().getSpecialPlay().isQingYiJiu() && checkQingYiJiu(hu, majiangIds, gang, peng, chi, buzhang, table, player, kingCard, curkingCardNum, all_card_index)) {
				hu.setQingYiJiu(true);
			}
			//		14.一九胡10分 一九和字牌组成的刻子加一对将。
			else if (table.getGameModel().getSpecialPlay().isYiJiuHu() && checkYiJiuHu(hu, majiangIds, gang, peng, chi, buzhang, table, player, kingCard, curkingCardNum, all_card_index)) {
				hu.setYiJiuHu(true);
			}

			hu.initDahuList();

	//		List<Integer> list = DataMapUtil.toList(arr);
	//		if (list.contains(1)) {
			if (hu.getDahuCount() > 0) {
				hu.setHu(true);
				hu.setDahu(true);
			}

			hu.setYingZhuang((table.getKingCard() == null ?  false : majiangIds.stream().filter(v -> v.getVal() == table.getKingCard().getVal()).mapToInt(v -> 1).sum() == 0));

			hu.initDahuList();
		}
		return /*arr*/;
	}

	/**
	 * @param
	 * @return
	 * @description 5.十三幺26分
	 * 满足19条+19筒+19万+东南西北中发白牌型之后任意一张成对就能胡牌
	 * @author Guang.OuYang
	 * @date 2019/11/12
	 */
	public static boolean checkShiSanYao(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, TcMjTable table, TcMjPlayer player, Mj kingCard, int curkingCardNum) {
		int curkingCardNumBak = curkingCardNum;
		if (player.isChiPengGang() || gang.size()>0 || table.getGameModel().isNoneWind() || table.getGameModel().isNoneChar()) {
			return false;
		}

		List<Integer> handCardVals = majiangIds.stream().map(v -> v.getVal()).collect(Collectors.toList());
		List<Integer> huCardVals = new ArrayList<Integer>(Arrays.asList(11, 19, 21, 29, 31, 39, 301, 311, 321, 331, 201, 211, 221));


		//统计所有牌出现的次数
		HashMap<Integer, Integer> handCardValCount = new HashMap<Integer, Integer>();
		huCardVals.forEach(v -> handCardValCount.put(v, 0));

		//两个出现的允许次数
		int doubleNumCount = 1;

		Iterator<Integer> iterator1 = handCardVals.iterator();
		while (iterator1.hasNext()) {
			Integer v = iterator1.next();
			Integer curValCount = handCardValCount.get(v);
			//不存在牌
			if (curValCount != null && curValCount == 0) {
				handCardValCount.put(v, 1);
				//已经有一张一样的牌,允许出现一对的次数
			} else if (curValCount != null && curValCount == 1 && doubleNumCount > 0) {
				--doubleNumCount;
				handCardValCount.put(v, 2);
			} else if (curkingCardNum > 0 && kingCard != null && v == kingCard.getVal()) {
				--curkingCardNum;
			} else {
				//不是十三幺
				return false;
			}
		}

		//没用王牌
		if (curkingCardNum == curkingCardNumBak && table.getGameModel().isCreateKingCard() && table.getGameModel().isCreateKingCard()) {
			hu.setYingZhuang(true);
		}

		return true;
	}

	/**
	 * @param
	 * @return
	 * @description 10.混一色4分
	 * 清一色加字牌。
	 * @author Guang.OuYang
	 * @date 2019/11/12
	 */
	public static boolean checkHunYiSe(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, TcMjTable table, TcMjPlayer player, Mj kingCard, int curkingCardNum, List<Mj> allMajiangs) {
		if (table.getGameModel().isNoneChar() || (hu.getDahuCount() <= 0 && !hu.isHu())) {
			return false;
		}
		boolean hunyise = false;
		int se = 0;
		for (Mj mjiang : allMajiangs) {
			if (kingCard != null && mjiang.getVal() == kingCard.getVal()) {
				continue;
			}

			if (se == 0) {
				hunyise = true;
				if (mjiang.getHuase() < 20)
					se = mjiang.getHuase();
				continue;
			}

			if (mjiang.getHuase() != se && mjiang.getHuase() < 20) {
				hunyise = false;
				break;
			}
		}

		return hunyise;
	}

	/**
	 * @param
	 * @return
	 * @description 12.字一色20分
	 * 只有东南西北中发白。
	 * @author Guang.OuYang
	 * @date 2019/11/12
	 */
	public static boolean checkZiYiSe(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, TcMjTable table, TcMjPlayer player, Mj kingCard, int curkingCardNum, List<Mj> allMajiangs) {
		if (table.getGameModel().isNoneChar() || (hu.getDahuCount() <= 0 && !hu.isHu())) {
			return false;
		}

		boolean ziyise = true;

		for (Mj mjiang : allMajiangs) {
			if (mjiang.getHuase() < 20 && (kingCard == null || kingCard.getVal() != mjiang.getVal())) {
				ziyise = false;
				break;
			}
		}


		return ziyise;
	}

	/**
	 * @param
	 * @return
	 * @description 13.十八罗汉36分
	 * 四条杠加一对将共18张牌胡牌。
	 * @author Guang.OuYang
	 * @date 2019/11/12
	 */
	public static boolean checkShiBaLuoHan(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, TcMjTable table, TcMjPlayer player, Mj kingCard, int curkingCardNum) {
		if (gang.size()/4 == 4 && majiangIds.size() == 2 && majiangIds.get(0).getVal() == majiangIds.get(1).getVal()) {
			return true;
		}
		return false;
	}

	/**
	 * @param
	 * @return
	 * @description 14.一九胡10分
	 * 全是一九和字牌组成的刻子加一对将。  要字牌
	 * @author Guang.OuYang
	 * @date 2019/11/12
	 */
	public static boolean checkYiJiuHu(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, TcMjTable table, TcMjPlayer player, Mj kingCard, int curkingCardNum, MjIndexArr all_card_index) {
		return checkYiJiuHu(hu, majiangIds, gang, peng, chi, next -> (next.getPai() != 1 && next.getPai() != 9) && next.getHuase() < 20 && (table.getKingCard() == null || next.getVal() != table.getKingCard().getVal()), true);
	}

	/**
	 * @param
	 * @return
	 * @description 15.清一九20分
	 * 只有一九组成的刻子加一对将。 不要字牌
	 * @author Guang.OuYang
	 * @date 2019/11/12
	 */
	public static boolean checkQingYiJiu(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, TcMjTable table, TcMjPlayer player, Mj kingCard, int curkingCardNum, MjIndexArr all_card_index) {
		return checkYiJiuHu(hu, majiangIds, gang, peng, chi, next -> ((next.getPai() != 1 && next.getPai() != 9) || next.getHuase() >= 20) && (table.getKingCard() == null || next.getVal() != table.getKingCard().getVal()), false);
	}

	/**
	 *@description 一九胡检测
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2019/12/11
	 */
	public static boolean checkYiJiuHu(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, Predicate<? super Mj> predicate, boolean needChar) {
		if ((!hu.isHu() && hu.getDahuCount() <= 0) || !chi.isEmpty() || (!hu.isPengpengHu() && !hu.isXiaodui() && !hu.isHao7xiaodui() && !hu.isShuang7xiaodui() && !hu.isSan7xiaodui())) {
			return false;
		}
		ArrayList<Mj> objects = new ArrayList<>();
		objects.addAll(majiangIds);
		objects.addAll(gang);
		objects.addAll(peng);
		int existsOne = 0, existsNine = 0, existsChar = 0;
		Iterator<Mj> iterator = objects.iterator();
		while (iterator.hasNext()) {
			Mj next = iterator.next();
			if (predicate.test(next)) {
				return false;
			}
			if (next.getHuase() >= 20) {
				existsChar++;
			}else if (next.getPai() == 1) {
				existsOne++;
			} else if (next.getPai() == 9) {
				existsNine++;
			}
		}
		return (existsOne > 0 || existsNine > 0) && ((!needChar && existsChar == 0) || (needChar && existsChar > 0));
	}


	/**
	 *@description 7小对检测
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2019/10/29
	 */
	private static boolean check7Dui(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, TcMjTable table, Mj kingCard, boolean existsEat, List<Mj> peng) {
		if (!CollectionUtils.isEmpty(gang) || !CollectionUtils.isEmpty(peng) || existsEat) {
			return false;
		}

		List<Mj> copy = new ArrayList<>(majiangIds);
		int kingCardNum = kingCard == null ? 0 : MjTool.dropKingCard(copy, kingCard).size();

		MjIndexArr card_index = new MjIndexArr();
		MjQipaiTool.getMax(card_index, copy);

		int num1 = card_index.getMajiangIndex(0) != null ? card_index.getMajiangIndex(0).getLength() : 0;//1个1,1个王
		int num2 = card_index.getMajiangIndex(1) != null ? card_index.getMajiangIndex(1).getLength() : 0;//
		int num3 = card_index.getMajiangIndex(2) != null ? card_index.getMajiangIndex(2).getLength() : 0;//1个3,1个王
		int num4 = card_index.getMajiangIndex(3) != null ? card_index.getMajiangIndex(3).getLength() : 0;
		int dui = card_index.getDuiziNum();

		if ((table.getGameModel().getSpecialPlay().isSevenPairs()
				|| table.getGameModel().getSpecialPlay().isSuperSevenPairs()
				|| table.getGameModel().getSpecialPlay().isSpecialSuperSevenPairs()
				|| table.getGameModel().getSpecialPlay().isSpecialSSuperSevenPairs()
				//对子单牌数量刚好等于王牌数量
		) && (dui == 7 || (kingCardNum >= num3 + num1))) {
			// 是否有豪华7小对
			// 有4个一样的牌
			if (((num4 > 2) || num3 + num4 > 2 || ((num3 + num2 * 2) == kingCardNum && num3 + num2 > 2)) && table.getGameModel().getSpecialPlay().isSpecialSSuperSevenPairs()) {
				hu.setSan7xiaodui(true);
			} else if ((num4 > 1 || num3 + num4 > 1 || ((num3 + num2 * 2) == kingCardNum && num3 + num2 > 1)) && table.getGameModel().getSpecialPlay().isSpecialSuperSevenPairs()) {
				// 双豪华7小对
				hu.setShuang7xiaodui(true);
			} else if ((num4 > 0 || num3 + num4 > 0 || (num2 > 0 && kingCardNum >= 2)) && table.getGameModel().getSpecialPlay().isSuperSevenPairs()) {
				// 豪华7小对
				hu.setHao7xiaodui(true);
			} else {
				// 普通7小对
				hu.setXiaodui(true);
			}
		}

		return hu.isXiaodui() || hu.isHao7xiaodui() || hu.isShuang7xiaodui() || hu.isSan7xiaodui();
	}

	private static boolean isQingYiSeHu(List<Mj> allMajiangs, List<Mj> majiangIds, Mj kingCard) {
		boolean qingyise = checkQingYiSe(allMajiangs, kingCard);
		if (qingyise) {
			boolean hu = MjTool.isPingHu(majiangIds, false, kingCard);
			return hu;
		}

		return false;
	}

	/**
	 *@description 黑天胡 庄家起手14张牌、闲家起手13张牌+摸第一张牌后，手牌无“王”无将无顺子无刻子。则可胡牌（不能炮胡）
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2019/10/21
	 */
	private static boolean isHeiTianHu(List<Mj> allMajiangs, List<Mj> majiangIds, Mj kingCard, GameModel gameModel) {
		//黑田胡不支持王牌变化,这里王牌一定不为空
		if (gameModel.getSpecialPlay().isFloorHu() && kingCard == null) {
			return false;
		}

		MjIndexArr card_index = new MjIndexArr();
		MjQipaiTool.getMax(card_index, allMajiangs);

		boolean foundKingCard = false;

		boolean found258 = false;
		//寻找王牌&将牌, 没有258则不能组成顺子, 这里不做顺子检测
		Iterator<Mj> iterator = allMajiangs.iterator();
		while (iterator.hasNext()) {
			Mj next = iterator.next();
			//王牌
			if (kingCard != null && next.getVal() == kingCard.getVal()) {
				foundKingCard = true;
			}

			//将牌
			if (next.getPai() == 2 || next.getPai() == 5 || next.getPai() == 8) {
				found258 = true;
			}

			if ((foundKingCard || kingCard == null) && found258) {
				break;
			}
		}

		return !foundKingCard && !found258 && card_index.getMajiangIndex(2) == null && card_index.getMajiangIndex(3) == null /*&&(kingCard != null && !allMajiangs.stream().anyMatch(v -> v.getVal() == kingCard.getVal())) && !MjTool.findSerial(allMajiangs, gameModel).isFind()*/;
	}

	public static boolean checkQingYiSe(List<Mj> allMajiangs, Mj kingCard) {
		boolean qingyise = false;
		int se = 0;
		for (Mj mjiang : allMajiangs) {
			if (kingCard != null && mjiang.getVal() == kingCard.getVal()) {
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

	private static boolean isJiangJiangHu(List<Mj> majiangIds, Mj kingCard) {
		boolean jiangjianghu = true;
		for (Mj mjiang : majiangIds) {
			if (kingCard != null && mjiang.getVal() == kingCard.getVal()) {
				continue;
			}
			if (!mjiang.isJiang()) {
				jiangjianghu = false;
				break;
			}
		}
		return jiangjianghu;
	}

	private static boolean isPengPengHu(List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, Mj kingCard) {
		//吃了的不算碰碰胡
		if (!CollectionUtils.isEmpty(chi)) {
			return false;
		}

		//王牌数量
		List<Mj> kings = new ArrayList<>();
		//val->num
		Map<Integer, Integer> repeated = new HashMap<>();
		Iterator<Mj> iterator = majiangIds.iterator();
		while (iterator.hasNext()) {
			Mj v = iterator.next();
			if (kingCard != null && kingCard.getVal() == v.getVal()) {
				kings.add(v);
				continue;
			}
			if (repeated.containsKey(v.getVal())) {
				repeated.put(v.getVal(), repeated.get(v.getVal()) + 1);
			} else {
				repeated.put(v.getVal(), 1);
			}
		}

		int kingSize = kings.size();

		List<Integer> singleCard = new ArrayList<>();
		//每个相同牌个数的数量,次数=>数量
		Map<Integer, Integer> repeatedSize = new HashMap<>();

		Iterator<Entry<Integer, Integer>> iterator1 = repeated.entrySet().iterator();
		while (iterator1.hasNext()) {
			Entry<Integer, Integer> next = iterator1.next();
			//碰碰胡, 这里只至多需要出现过个对子
			if (repeatedSize.containsKey(next.getValue())) {
				repeatedSize.put(next.getValue(), repeatedSize.get(next.getValue()) + 1);
			} else {
				repeatedSize.put(next.getValue(), 1);
			}
			if (next.getValue() == 1) {
				singleCard.add(next.getKey());
			}
		}

		//杠和碰的数量不理会
		int num1 = repeatedSize.getOrDefault(1, 0);
		int num2 = repeatedSize.getOrDefault(2, 0);
//		int num3 = repeatedSize.getOrDefault(3, 0).intValue();
		int num4 = repeatedSize.getOrDefault(4, 0);

		//没有王牌,没有单牌,一个对子,认为是碰碰胡
		if (((kingSize == 0 || kingSize == 3) && num1 == 0 && num4 == 0 && (num2 == 1 || kingSize == 2))) {
			return true;
			//存在单牌和对子数量加起来恰好是王牌数量,没有4个
		} else if (kingSize > 0 && ((num1 * 2 + (num2 - 1) == kingSize))) {
			return true;
		} else if (kingSize > 0 && num4 < 3 && num4 > 0) {
			//对子, 单牌, 单+对不存在
			if (num4 >= 2 && (num2 == 0 || (num4 + num2 + 1 == kingSize)) && (num1 == 0 || (num1 == 1 && kingSize == 3))) {
				return true;
				//对子, 单牌, 单+对
			} else if ((num2 == 0 || num4 + num2 == kingSize) && (num1 == 0 || num4 + num1 * 2 == kingSize) && (num1 + num2 == 0 || num4 + num2 + num1 * 2 == kingSize)) {
				return true;
			}
		}

		return false;
	}

	public static void main(String[] args) {
		ArrayList<Integer> copy = new ArrayList<>(MjConstants.fullMj);
		List<Integer> hand = Arrays.asList(11, 11, 15, 23, 23, 23, 23, 25, 25, 25, 26, 26, 26, 26);
		//正常
		hand = Arrays.asList(11,11,11,12,12,12,13,13,13,14,14,14,15,15);
//		//2王 1对 1单
		hand = Arrays.asList(11,11,11,12,12,15,13,13,13,14,14,14,26,26);
		//4单0对4王
		hand = Arrays.asList(11,11,11,12,12,12,13,15,16,13,26,26,26,26);
		//3单1对3王
		hand = Arrays.asList(11,11,11,12,12,12,13,15,16,13,17,26,26,26);
		hand = Arrays.asList(35, 35, 35, 35, 38, 38, 38, 18, 18, 18);
////		//2王 3对
//		hand = Arrays.asList(11,11,15,12,12,15,13,13,13,14,14,14,26,26);
////		//3王 2对 1单
//		hand = Arrays.asList(11,11,26,12,12,15,13,13,13,14,14,14,26,26);
////		//4王 1对 1单
//		hand = Arrays.asList(11,26,26,12,12,15,13,13,13,14,14,14,26,26);
////		//4王 3对 2单
//		hand = Arrays.asList(11,26,26,12,12,11,13,13,15,16,14,14,26,26);
////		//3王 3对 3单
//		hand = Arrays.asList(11,26,26,12,12,11,13,17,15,16,14,14,26,26);
////		//5对4王
//		hand = Arrays.asList(11,11,12,12,13,13,14,14,15,15,26,26,26,26);
////		//1单2对2王
//		hand = Arrays.asList(11,11,11,17,12,12,13,13,13,14,14,14,26,26);
////		//1个4,1对,2王
//		hand = Arrays.asList(11,11,11,11,12,12,13,13,13,14,14,14,26,26);
//		//1个4,2单,0对,2王
//		hand = Arrays.asList(11,11,11,11,12,19,13,13,13,14,14,14,26,26);

		List<Mj> mjs = MjHelper.find(copy, hand);
		System.out.println(mjs);
		System.out.println(isPengPengHu(mjs, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Mj.getMajang(15)));
	}
}
