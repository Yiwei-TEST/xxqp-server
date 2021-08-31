package com.sy599.game.qipai.yymj.rule;

import com.sy599.game.qipai.yymj.bean.YyMjPlayer;
import com.sy599.game.qipai.yymj.bean.YyMjTable;
import com.sy599.game.qipai.yymj.bean.GameModel;
import com.sy599.game.qipai.yymj.bean.MjiangHu;
import com.sy599.game.qipai.yymj.constant.MjConstants;
import com.sy599.game.qipai.yymj.tool.MjTool;
import com.sy599.game.qipai.yymj.tool.MjQipaiTool;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MjRule {


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
	public static void checkDahu(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, YyMjTable table, boolean canYingZhuang, YyMjPlayer player) {
		Mj kingCard = canYingZhuang ? null : table.getKingCard();

		List<Mj> allMajiangs = new ArrayList<>();
		allMajiangs.addAll(majiangIds);
		allMajiangs.addAll(gang);
		allMajiangs.addAll(peng);
		allMajiangs.addAll(chi);
		allMajiangs.addAll(buzhang);

		if (majiangIds.size() % 3 == 2) {
			MjIndexArr all_card_index = new MjIndexArr();
			MjQipaiTool.getMax(all_card_index, allMajiangs);

			// 七小对        可接炮         7个对子
			//豪华七小对    可接炮         5个对子加1个杠（没杠出去，下同）
			//双豪华七小对  可接炮         3个对子加2个杠
			//三豪华七小对  可接炮         1个对子加3个杠
			if (check7Dui(hu, majiangIds, gang, table, kingCard, /*arr,*/ !chi.isEmpty(), peng, chi, player)) {
			}else if (table.getGameModel().getSpecialPlay().isPpHu() && isPengPengHu(majiangIds, gang, peng, chi, buzhang, kingCard) && chi.size() == 0) {
				// 1碰碰胡检测, 七小对优先, 检测失败时检测碰碰胡
				//碰碰胡        可接炮         12张牌为坎或者碰
				hu.setPengpengHu(true);
			}

			// 将将胡        不可接炮       14个2、5、8胡牌
			//门清将将胡 特殊处理  门清本身不可接炮 特殊情况下允许
			if (table.getGameModel().getSpecialPlay().isJjHu() && isJiangJiangHu(hu, allMajiangs, majiangIds, gang, peng, chi, kingCard, player)) {
				hu.setJiangjiangHu(true);
			}

			// 清一色        可接炮         胡牌时全部为同一花色
			if (table.getGameModel().getSpecialPlay().isAllOfTheSameColor() && isQingYiSeHu(allMajiangs, majiangIds, kingCard)) {
				hu.setQingyiseHu(true);
			}

			//天胡          不可接炮       起手胡牌
//			if (table.getGameModel().getSpecialPlay().getKingHuNum() > 0 && isTianHu(hu, majiangIds, gang, peng, chi, player)) {
//				hu.setDaodihuh(true);
//			}

			//一条龙        可接炮         同花色有1到9凑齐且做顺子用（可选玩法）
			if (table.getGameModel().getSpecialPlay().isDragon() && isYiTiaoLong(hu, majiangIds, gang, peng, chi, player)) {
				hu.setYiTiaoLong(true);
			}

			//一字撬        可接炮         只剩一张牌胡牌，即单吊
			if (table.getGameModel().getSpecialPlay().isOneCharPry() && isYiZiQiao(hu, majiangIds, gang, peng, chi, player)) {
				hu.setYiZiQiao(true);
			}

			//门清          可接炮         没有吃碰杠就胡牌
			if (table.getGameModel().getSpecialPlay().isMenqing() && player.noNeedMoCard() && isMenQing(hu, majiangIds, gang, peng, chi, player)) {
				hu.setMenqing(true);
			}
			hu.initDahuList();

			if (hu.getDahuCount() > 0) {
				hu.setHu(true);
				hu.setDahu(true);
			}

			hu.setYingZhuang((table.getKingCard() == null ? false : majiangIds.stream().filter(v -> v.getVal() == table.getKingCard().getVal()).mapToInt(v -> 1).sum() == 0));

			hu.initDahuList();
		}
		return;
	}

	/**
	 * @param
	 * @return
	 * @description 5.十三幺26分
	 * 满足19条+19筒+19万+东南西北中发白牌型之后任意一张成对就能胡牌
	 * @author Guang.OuYang
	 * @date 2019/11/12
	 */
	public static boolean checkShiSanYao(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, YyMjTable table, YyMjPlayer player, Mj kingCard, int curkingCardNum) {
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
	public static boolean checkHunYiSe(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, YyMjTable table, YyMjPlayer player, Mj kingCard, int curkingCardNum, List<Mj> allMajiangs) {
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
	public static boolean checkZiYiSe(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, YyMjTable table, YyMjPlayer player, Mj kingCard, int curkingCardNum, List<Mj> allMajiangs) {
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
	public static boolean checkShiBaLuoHan(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, YyMjTable table, YyMjPlayer player, Mj kingCard, int curkingCardNum) {
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
	public static boolean checkYiJiuHu(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, YyMjTable table, YyMjPlayer player, Mj kingCard, int curkingCardNum, MjIndexArr all_card_index) {
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
	public static boolean checkQingYiJiu(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, YyMjTable table, YyMjPlayer player, Mj kingCard, int curkingCardNum, MjIndexArr all_card_index) {
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
	 *@description 一条龙        可接炮         同花色有1到9凑齐且做顺子用（可选玩法）
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2019/12/11
	 */
	public static boolean isYiTiaoLong(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, YyMjPlayer player) {
		if ((!hu.isHu() && hu.getDahuCount() <= 0)) {
			return false;
		}
		ArrayList<Mj> objects = new ArrayList<>();
		objects.addAll(majiangIds);
		objects.addAll(gang);
		objects.addAll(peng);

		//同花色
		Map<Integer, List<Mj>> collect = objects.stream().collect(Collectors.groupingBy(Mj::getHuase));
		//1~9
		boolean res = collect.values().stream().anyMatch(v -> {
			Map<Integer, List<Mj>> collect1 = v.stream().collect(Collectors.groupingBy(Mj::getPai));
			//有1个数字没有匹配到则不是
			return !IntStream.range(1, 10).anyMatch(i -> !collect1.containsKey(i));
		});
		return res;
	}

	/**
	 *@description 天胡          不可接炮       起手胡牌
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2019/12/11
	 */
	public static boolean isTianHu(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, YyMjPlayer player) {
		if ((!hu.isHu() && hu.getDahuCount() <= 0) || !player.noNeedMoCard() || !player.isFirstDisCard()) {
			return false;
		}

		return true;
	}

	/**
	 *@description 门清          可接炮         没有吃碰杠就胡牌
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2019/12/11
	 */
	public static boolean isMenQing(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, YyMjPlayer player) {
		if ((!hu.isHu() && hu.getDahuCount() <= 0) || !CollectionUtils.isEmpty(gang) || !CollectionUtils.isEmpty(peng) || !CollectionUtils.isEmpty(chi)) {
			return false;
		}

		return true;
	}


	/**
	 *@description 一字撬        可接炮         只剩一张牌胡牌，即单吊
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2019/12/11
	 */
	public static boolean isYiZiQiao(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, YyMjPlayer player) {
		//一字撬,可能自摸到手里之后有两张牌了
		if ((!hu.isHu() && hu.getDahuCount() <= 0) || player.getHandMajiang().size() > 2) {
			return false;
		}

		return true;
	}

	/**
	 *@description 7小对检测
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2019/10/29
	 */
	private static boolean check7Dui(MjiangHu hu, List<Mj> majiangIds, List<Mj> gang, YyMjTable table, Mj kingCard, boolean existsEat, List<Mj> peng, List<Mj> chi , YyMjPlayer player) {
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
			if (((num4 > 2) || num3 + num4 > 2 || ((num3 + num2 * 2) == kingCardNum && num3 + num2 > 2))  && table.getGameModel().getSpecialPlay().isSpecialSSuperSevenPairs()) {
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

		return hu.isSevenDui();
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

	/**
	 *@description 将将胡        不可接炮       14个2、5、8胡牌
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/4/2
	 */
	private static boolean isJiangJiangHu(MjiangHu hu, List<Mj> allMaJiang, List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, Mj kingCard, YyMjPlayer player) {
		boolean jiangjianghu = true;
		for (Mj mjiang : allMaJiang) {
			if (kingCard != null && mjiang.getVal() == kingCard.getVal()) {
				continue;
			}
			if (!mjiang.isJiang()) {
				jiangjianghu = false;
				break;
			}
		}

		//门清将将胡可以接炮, 同时满足门清和将将胡
		//没有勾选门清将将胡可接炮,非自摸的情况不算将将胡//门清将将胡可以接炮
		if (!player.getPlayingTable(YyMjTable.class).getGameModel().isMQJJHuInvite() && !player.noNeedMoCard()) {
			return false;
		}

		MjiangHu mjiangHu = new MjiangHu();
		mjiangHu.setHu(true);
		//门清+将将胡
		if (player.getPlayingTable(YyMjTable.class).getGameModel().isMQJJHuInvite() && jiangjianghu && isMenQing(mjiangHu, majiangIds, gang, peng, chi, player)/*(CollectionUtils.isEmpty(gang)&&CollectionUtils.isEmpty(peng)&&CollectionUtils.isEmpty(chi))*/) {
			//门清+将将胡,仅将将胡可以接炮,不算门清门子
//			hu.setMenqing(true);
		}else if(player.getPlayingTable(YyMjTable.class).getGameModel().isMQJJHuInvite() && !player.noNeedMoCard() ){		//勾了门清将将胡,但是不是门清,不能接炮
			return false;
		}


		return jiangjianghu;
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
	private static boolean isPengPengHu(List<Mj> majiangIds, List<Mj> gang, List<Mj> peng, List<Mj> chi, List<Mj> buzhang, Mj kingCard) {
		if ((chi != null && !chi.isEmpty()) || majiangIds.size() % 3 != 2) {
			return false;
		}

		majiangIds = new ArrayList<>(majiangIds);

		//杠下去的牌,可以不理会, 没有杠下去的4个需要凑成111 2格式
//		majiangIds.removeAll(gang);

		int kingCardNum = kingCard == null ? 0 : MjTool.dropKingCard(majiangIds, kingCard).size();

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
