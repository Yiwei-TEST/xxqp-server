package com.sy599.game.qipai.tjmj.rule;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.tjmj.bean.*;
import com.sy599.game.qipai.tjmj.constant.MjAction;
import com.sy599.game.qipai.tjmj.constant.MjConstants;
import com.sy599.game.qipai.tjmj.tool.MjTool;
import com.sy599.game.qipai.tjmj.tool.MjQipaiTool;
import com.sy599.game.util.DataMapUtil;
import com.sy599.game.util.LogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.w3c.dom.ls.LSException;
import sun.security.krb5.internal.ktab.KeyTabInputStream;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MjRule {

	/**
	 * 小胡
	 * 
	 * @param majiangIds
	 * @return
	 *
	 */
	public static void checkXiaoHu3(MjiangHu hu, List<Mj> majiangIds, boolean begin, TjMjTable table) {
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
	public static void checkXiaoHu2(MjiangHu hu, List<Mj> majiangIds, boolean begin, TjMjTable table, TjMjPlayer player) {
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

	public static int[] checkDahu(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, TjMjTable table, boolean canYingZhuang, TjMjPlayer player, int subKingNum) {
		return checkDahu(hu, majiangIds,gang,peng,chi,buzhang,table,canYingZhuang,player, false, subKingNum);
	}

	/**
	 * 0 碰碰胡 1将将胡 2清一色 3双豪华7小对 4豪华7小对 5:7小对 6全求人 7黑天胡
	 *
	 * @param majiangIds
	 * @param gang
	 * @param peng
	 * @param chi
	 * @param buzhang
	 * @param  isTianDiHuCheck 天地胡移除王牌或地牌时会造成牌数量%3后!=2
	 * @return
	 */
	public static int[] checkDahu(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, TjMjTable table, boolean canYingZhuang, TjMjPlayer player, boolean isTianDiHuCheck, int subKingNum) {
		Mj[] kingCard = {canYingZhuang ? null : table.getKingCard() , canYingZhuang ? null : table.getKingCard2()};

		List<Mj> allMajiangs = new ArrayList<>();
		allMajiangs.addAll(majiangIds);
		allMajiangs.addAll(gang);
		allMajiangs.addAll(peng);
		allMajiangs.addAll(chi);
		allMajiangs.addAll(buzhang);
		int arr[] = new int[8];


		//最后两张牌是王牌做平胡计算
		if (majiangIds.size() == 2) {
			//两张王牌
			if (table.isKingCard(majiangIds.get(0)) && table.isKingCard(majiangIds.get(1))) {
				hu.setHu(true);
				hu.setPingHu(true);
				//王牌是258强行硬庄
				boolean eqLastCardId = majiangIds.get(0).getVal() == majiangIds.get(1).getVal();
				if ((table.getKingCard().isJiang() && eqLastCardId)
						|| (table.getGameModel().isEightKing() && table.getKingCard2().isJiang() && eqLastCardId)
						|| (table.getGameModel().isEightKing() && table.getKingCard().isJiang() && table.getKingCard2().isJiang())
				) {
					hu.setYingZhuang(true);
				}
			}
		}
		//数量校验
		boolean numCheck = majiangIds.size() % 3 != 2;

		if (numCheck && !isTianDiHuCheck) {
			return arr;
		}

		MjIndexArr all_card_index = new MjIndexArr();
		MjQipaiTool.getMax(all_card_index, allMajiangs);

		// 7小对检测
		// 1碰碰胡检测, 七小对优先, 检测失败时检测碰碰胡
		if (!numCheck && !check7Dui(hu, majiangIds, peng, table, arr, !chi.isEmpty(), gang, subKingNum, kingCard)
				&& table.getGameModel().getSpecialPlay().isPpHu() && isPengPengHu(majiangIds, gang , peng, chi , buzhang, subKingNum, kingCard) && chi.size() == 0) {
			//FIXME 新碰碰胡算法测试,老算法通过,新算法未通过
			try{
				if (!isPengPengHu2(majiangIds, gang , peng, chi , buzhang, subKingNum, kingCard)) {
					LogUtil.msgLog.info("isPengPengHu2CalcFalse:{},{},{},{},{},{},{},{}", majiangIds, gang, peng, chi, buzhang, subKingNum, table.getKingCard(), table.getKingCard2());
				}
			}catch(Exception e){
				LogUtil.errorLog.error("pengpenghu2 {}", e);
			}

			arr[0] = 1;
			hu.setPengpengHu(true);
		}
		//FIXME 新碰碰胡算法测试老算法未通过,但是新算法通过
		else if(!(hu.isXiaodui() || hu.isHao7xiaodui() || hu.isShuang7xiaodui()) && !hu.isPengpengHu()){

			//FIXME 新碰碰胡算法测试老算法未通过,但是新算法通过
			try{
				if (isPengPengHu2(majiangIds, gang , peng, chi , buzhang, subKingNum, kingCard)) {
					LogUtil.msgLog.info("isPengPengHu2CalcTrue:{},{},{},{},{},{},{},{}", majiangIds, gang, peng, chi, buzhang, subKingNum, table.getKingCard(), table.getKingCard2());
				}
			}catch(Exception e){
				LogUtil.errorLog.error("pengpenghu2 {}", e);
			}
		}

		// 2将将胡
		if (table.getGameModel().getSpecialPlay().isJjHu() && isJiangJiangHu(allMajiangs, subKingNum, kingCard)) {
			arr[1] = 1;
			hu.setJiangjiangHu(true);
		}

		// 3清一色
		if (table.getGameModel().getSpecialPlay().isAllOfTheSameColor() && isQingYiSeHu(allMajiangs, majiangIds, subKingNum, kingCard)) {
			arr[2] = 1;
			hu.setQingyiseHu(true);
			hu.setHu(true);
		}

		//7黑天胡 庄家起手14张牌、闲家起手13张牌+摸第一张牌后，手牌无“王”无将无顺子无刻子。则可胡牌（不能炮胡）
		//黑天胡.必须手里14张, 同时不与任何牌型叠加, 需首轮出牌, 没有听牌提示
		if (table.getGameModel().getSpecialPlay().isBlackSkyHu() && player.isFirstDisCard() && player.noNeedMoCard() && hu.getDahuCountAll() <= 0 && isHeiTianHu(allMajiangs, majiangIds, table.getGameModel(), kingCard)) {
			arr[7] = 1;
			hu.setHeitianhu(true);
		}

		List<Integer> list = DataMapUtil.toList(arr);
		if (list.contains(1)) {
			// 再检查一下清一色
			if (arr[2] != 1 && table.getGameModel().getSpecialPlay().isAllOfTheSameColor() && checkQingYiSe(allMajiangs, subKingNum, kingCard)) {
				arr[2] = 1;
				hu.setQingyiseHu(true);
			}
//			hu.setHu(true);

			if (list.get(2) == 1) {
				list.set(2, 0);
			}
			//仅有清一色不能倒牌, 这里验证是否仅存在清一色
			hu.setHu(hu.isHu() || list.contains(1));
			hu.setDahu(true);
//			hu.setDahuList(list);
		} else {
			list.clear();
//			hu.setDahuList(list);
		}

		hu.initDahuList();

		return arr;
	}

	/**
	 *@description 7小对检测
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2019/10/29
	 */
	private static boolean check7Dui(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, TjMjTable table, int[] arr, boolean existsEat, List<Mj> peng, int subKingNum, Mj...kingCard) {
		//7小对, 无论上层逻辑是否排除牌数限制, 一定需要偶数张牌
		if (!CollectionUtils.isEmpty(gang) || !CollectionUtils.isEmpty(peng) || existsEat || majiangIds.size() % 2 != 0) {
			return false;
		}

		List<Mj> copy = new ArrayList<>(majiangIds);

		int kingCardNum = kingCard == null || kingCard.length <= 0 ? 0 : MjTool.dropKingCard(copy, subKingNum, kingCard).size();

		MjIndexArr card_index = new MjIndexArr();
		MjQipaiTool.getMax(card_index, copy);

		int num1 = card_index.getMajiangIndex(0) != null ? card_index.getMajiangIndex(0).getLength() : 0;//1个1,1个王
		int num2 = card_index.getMajiangIndex(1) != null ? card_index.getMajiangIndex(1).getLength() : 0;//
		int num3 = card_index.getMajiangIndex(2) != null ? card_index.getMajiangIndex(2).getLength() : 0;//1个3,1个王
//		int num4 = card_index.getMajiangIndex(3) != null ? card_index.getMajiangIndex(3).getLength() : 0;
		int dui = card_index.getDuiziNum();

		if ((table.getGameModel().getSpecialPlay().isSevenPairs()
				|| table.getGameModel().getSpecialPlay().isSuperSevenPairs()
				|| table.getGameModel().getSpecialPlay().isSpecialSuperSevenPairs()
				|| table.getGameModel().getSpecialPlay().isSpecialSSuperSevenPairs()
				//对子单牌数量刚好等于王牌数量
		) && (dui == 7 || (kingCardNum >= num3 + num1 /*&& (num3 + num1 - kingCardNum % 2) == 2)*/)) && !existsEat) {
			// 是否有豪华7小对
			MjIndex index = card_index.getMajiangIndex(3);
			if (index != null) {
				// 有4个一样的牌
				if (index.getLength() > 1 && table.getGameModel().getSpecialPlay().isSpecialSuperSevenPairs()) {
					// 双豪华7小对
					arr[3] = 1;

					hu.setShuang7xiaodui(true);
				} else if(table.getGameModel().getSpecialPlay().isSuperSevenPairs()){
					// 豪华7小对
					arr[4] = 1;

					hu.setHao7xiaodui(true);
				} else {
					// 普通7小对
					arr[5] = 1;
					hu.setXiaodui(true);
				}
			} else {
				// 普通7小对
				arr[5] = 1;
				hu.setXiaodui(true);
			}
//			if (card_index.getMajiangIndex(0)==null||card_index.getMajiangIndex(0).getLength()==0) {
//				int comboNum = num2 - (num2 - (kingCardNum / 2));
//				if (comboNum >= 0) {
//					if (num2 > 1 && table.getGameModel().getSpecialPlay().isSpecialSuperSevenPairs() && !hu.isShuang7xiaodui()) {
//						// 双豪华7小对
//						arr[3] = 1;
//
//						hu.setShuang7xiaodui(true);
//						hu.setHao7xiaodui(false);
//						hu.setXiaodui(false);
//					} else if(num2 > 0 && table.getGameModel().getSpecialPlay().isSuperSevenPairs() && !hu.isShuang7xiaodui() && !hu.isHao7xiaodui()){
//						// 豪华7小对
//						arr[4] = 1;
//
//						hu.setHao7xiaodui(true);
//						hu.setXiaodui(false);
//					}
//				}
//
//			}
		}

		return hu.isSevenDui();
	}

	private static boolean isQingYiSeHu(List<Mj> allMajiangs, List<Mj> majiangIds, int subKingNum, Mj...kingCard) {
		boolean qingyise = checkQingYiSe(allMajiangs, subKingNum, kingCard);
		if (qingyise) {
			return MjTool.isPingHu(majiangIds, false, subKingNum, kingCard);
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
	private static boolean isHeiTianHu(List<Mj> allMajiangs, List<Mj> majiangIds, GameModel gameModel, Mj...kingCard) {
		//黑田胡不支持硬庄
		if (kingCard == null || kingCard.length <= 0 || kingCard[0] == null) {
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
			if (kingCard != null && isKingCard(next, kingCard)) {
				foundKingCard = true;
			}

			//将牌
			if (next.getPai() == 2 || next.getPai() == 5 || next.getPai() == 8) {
				found258 = true;
			}

			if ((foundKingCard || kingCard == null || kingCard.length <= 0) && found258) {
				break;
			}
		}

		return !foundKingCard && !found258 && card_index.getMajiangIndex(2) == null && card_index.getMajiangIndex(3) == null /*&&(kingCard != null && !allMajiangs.stream().anyMatch(v -> v.getVal() == kingCard.getVal())) && !MjTool.findSerial(allMajiangs, gameModel).isFind()*/;
	}

	public static boolean checkQingYiSe(List<Mj> allMajiangs, int subKingNum, Mj... kingCard) {
		subKingNum = -subKingNum;
		boolean qingyise = false;
		int se = 0;
		for (int i = allMajiangs.size() - 1; i >= 0; i--) {
			Mj mjiang = allMajiangs.get(i);
			if (kingCard != null && isKingCard(mjiang, kingCard) && ++subKingNum>0) {
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

	private static boolean isJiangJiangHu(List<Mj> majiangIds, int subKingNum, Mj... kingCard) {
		subKingNum = -subKingNum;
		boolean jiangjianghu = true;
		for (int i = majiangIds.size() - 1; i >= 0; i--) {
			Mj mjiang = majiangIds.get(i);
			if (kingCard != null && isKingCard(mjiang, kingCard) && ++subKingNum>0) {
				continue;
			}
			if (!mjiang.isJiang()) {
				jiangjianghu = false;
				break;
			}
		}
		return jiangjianghu;
	}

	@Deprecated
	private static boolean isPengPengHu(List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, int subKingNum, Mj... kingCard) {
		//吃了的不算碰碰胡
		if (!CollectionUtils.isEmpty(chi)) {
			return false;
		}

		subKingNum = -subKingNum;
		//王牌数量
		List<Mj> kings = new ArrayList<>();
		//val->num
		Map<Integer, Integer> repeated = new HashMap<>();
		for (int i = majiangIds.size() - 1; i >= 0; i--) {
			Mj v = majiangIds.get(i);
			if (kingCard != null && isKingCard(v, kingCard) && ++subKingNum>0) {
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
			//对子和单牌都存在|没有对子
		} else if (kingSize > 0 && ((num2>0&&(kingSize-(num2/2)-(num1*2))>=3)||((kingSize-(num1*2))>=2))) {
			return true;
		}

		return false;
	}

	/**
	 *@description 碰碰胡, 1张单牌需要2张王,1对需要1张王,杠下去的算一坎,没有杠下去的需要拆开为111 2
	 * 凑将优先级   对子>单牌>4个>3个
	 * 1)有对子:去掉一张对子,匹配1张单牌2张王,1对1张王,没有杠下去的4个2张王,王如果出来刚好等于所需的数量
	 * 2)无对子:去掉一张单牌做对子,匹配1张单牌2张王, 没有杠下去的4个2张王, 王如果出来刚好等于所需的数量
	 * 3)无对子无单无4个仅2张王牌做将
	 * 4)无对子无单有4个, 抽出一张4个凑成对子+坎 111 2*  其余都是坎 111 2**
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/2/26
	 */
	private static boolean isPengPengHu2(List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, int subKingNum, Mj... kingCard) {
		if ((chi != null && !chi.isEmpty()) || majiangIds.size() % 3 != 2) {
			return false;
		}

		majiangIds = new ArrayList<>(majiangIds);

		//杠下去的牌,可以不理会, 没有杠下去的4个需要凑成111 2格式
//		majiangIds.removeAll(gang);

		int kingCardNum = kingCard == null || kingCard.length <= 0 ? 0 : MjTool.dropKingCard(majiangIds, subKingNum, kingCard).size();

		MjIndexArr card_index = new MjIndexArr();
		MjQipaiTool.getMax(card_index, majiangIds);

		//一张单牌需要两张王
		int one = card_index.getMajiangIndex(0) == null ? 0 : card_index.getMajiangIndex(0).getLength() * 2;
		//一个对子一张王
		int two = card_index.getMajiangIndex(1) == null ? 0 : card_index.getMajiangIndex(1).getLength();
		//四个需要1个凑成一对,或者需要2个凑成2坎
		int four = card_index.getMajiangIndex(3) == null ? 0 : card_index.getMajiangIndex(3).getLength() * 2;

		//有对子,移除一张对子,其他匹配
		return (two > 0 && (kingCardNum == (two - 1 + one) + (four) || kingCardNum - ((two - 1 + one) + (four)) >= 3))
				//无对子,用一张单牌凑成对子,其他匹配
				|| (one > 0 && two <= 0 && ((one - 1) + ((four)) == kingCardNum || kingCardNum - ((one - 1) + ((four))) >= 3))
				//没有对子,没有单牌,仅有坎和4个的,比如:2组4个,1坎,3个王  11,11,11,11,12,12,12,12,14,14,14,26,26,26
				|| (one + two == 0 && four > 0 && (kingCardNum == (four - 1) || (kingCardNum - (four - 1) >= 3)))
				//没有对子,没有单牌,没有4个,仅有王牌需要格式111 22
				|| (one + two + four == 0 && kingCardNum % 3 == 2)
				;
	}

	public static boolean isKingCard(Mj v, Mj[] kingCard) {
		return Stream.of(kingCard).filter(v1 -> v1 != null && v1.getVal() == v.getVal()).count() > 0;
	}

	public static void main(String[] args) {
		ArrayList<Integer> copy = new ArrayList<>(MjConstants.zhuanzhuan_mjList);
		List<Integer> hand = Arrays.asList(11, 11, 15, 23, 23, 23, 23, 25, 25, 25, 26, 26, 26, 26);
		//正常
		hand = Arrays.asList(11,11,11,12,12,12,13,13,13,14,14,14,15,15);
//		hand = Arrays.asList(11,11,16,16,12,12,13,13,13,14,14,14,15,15);
//		hand = Arrays.asList(11,11,16,16,12,12,12,13,13,14,14,14,15,15);
//		hand = Arrays.asList(16,16,16,15,12,13,13,13,13,14,14,14,15,15);
//		hand = Arrays.asList(23,23,23,19,19,19,32,32,13,13,13,14,14,13);
//		hand = Arrays.asList(23,23,23,12,12);
		hand = Arrays.asList(21,21,21,32,32,34,34,35,35,22,22,22,26,26);
		hand = Arrays.asList(21,21,21,32,32,35,35,35,22,22,22,22,26,26);
//		hand = Arrays.asList(11,11,11,11,12,12,13,13,13,14,14,14,26,26);
		hand = Arrays.asList(11,11,11,11,12,12,13,13,13,14,14,14,26,26);
		hand = Arrays.asList(11,11,11,11,12,12,13,13,13,14,14,26,26,26);
		//碰碰胡,手里4个拆开
		hand = Arrays.asList(11,11,11,11,12,12,12,12,14,14,14,26,26,26);
		//非碰碰胡,手里4个需要拆开
		hand = Arrays.asList(11,11,11,11,12,12,12,12,13,14,14,26,26,26);
		//没有对子,没有单牌,仅有的都是王
		hand = Arrays.asList(11,11,11,11,12,15,15,15,14,14,14,26,26,26);
		hand = Arrays.asList(11,11,11,11,15,15,15,15,14,14,14,26,26,26);

		hand = Arrays.asList(29,35,38,38,15,15,26,26,11,11,11,23,23,23);
		hand = Arrays.asList(22,38,11,11,26,38,11,29,26,35,35,23,23,23);
		hand = Arrays.asList(22,22,38,11,11,26,38,11,29,26,35,23,23,23);
		hand = Arrays.asList(38,11,11,26,38,11,29,26,35,15,15,23,23,23);
		hand = Arrays.asList(37,37,37,11,11,11,17,17,17,14,36);
		hand = Arrays.asList(23,17,12,23,17);
		hand = Arrays.asList(34,35,11,11,11,15,15,15,16,16,16,18,18,18);
		hand = Arrays.asList(17,17,17,17,26,26,19,19,19,19,13);
		hand = Arrays.asList(17,17,17,17,26,26,19,19,19,19,13);

		int king = 19;
		int king2 = 0;
		hand = Arrays.asList(37,37,37,37,35,35,35,35,36,36,27);
		king = 35; king2 = 0;
		hand = Arrays.asList(33,33,37,37,38,38,16,16,24,24,24,25,25,37);
		king = 38; king2 = 0;

		List<Mj> mjs = MjHelper.find(copy, hand);

//		List<Mj> gang = MjHelper.find(copy, Arrays.asList(36, 36, 36, 36));
//		gang.add(mjs.get(hand.size()-1));
//		System.out.println(mjs);
		System.out.println(isPengPengHu(mjs, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), 0, Mj.getMajangByVal(king)));
		System.out.println(isPengPengHu2(mjs, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), 0, Mj.getMajangByVal(king), Mj.getMajangByVal(king2)));
	}
}
