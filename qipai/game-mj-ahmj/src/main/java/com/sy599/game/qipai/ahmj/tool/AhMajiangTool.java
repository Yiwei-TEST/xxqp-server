package com.sy599.game.qipai.ahmj.tool;

import com.sy599.game.qipai.ahmj.bean.AhmjHu;
import com.sy599.game.qipai.ahmj.rule.AhmjRule;
import com.sy599.game.qipai.ahmj.constant.Ahmj;
import com.sy599.game.qipai.ahmj.rule.MajiangIndex;
import com.sy599.game.qipai.ahmj.rule.MajiangIndexArr;

import java.util.*;
import java.util.Map.Entry;

public class AhMajiangTool {
	/**
	 * 安化麻将胡牌
	 * 
	 * @param majiangIds
	 *            手牌
	 * @param gang
	 *            杠的牌
	 * @param peng
	 *            碰过的牌
	 * @param chi
	 *            吃过的牌
	 * @param buzhang
	 *            补张过的牌
	 * @param wang
	 *            王
	 * @param fourWang
	 *            是否4王
	 * @param isbegin
	 *            是否起手
	 * @param moMajiang
	 *            是否摸牌
	 * @param qGangHu
	 *            是否抢杠胡
	 * @return
	 */
	public static AhmjHu isHuAHMajiang(List<Ahmj> majiangIds, List<Ahmj> gang, List<Ahmj> peng, List<Ahmj> chi, List<Ahmj> buzhang, Ahmj wang, boolean fourWang, boolean isbegin,
                                       boolean moMajiang, boolean qGangHu) {
		return isHuAHMajiang(majiangIds, gang, peng, chi, buzhang, wang, fourWang, isbegin, moMajiang, qGangHu, false);
	}

	/**
	 * 安化麻将胡牌
	 * 
	 * @param majiangIds
	 *            手牌
	 * @param gang
	 *            杠的牌
	 * @param peng
	 *            碰过的牌
	 * @param chi
	 *            吃过的牌
	 * @param buzhang
	 *            补张过的牌
	 * @param wang
	 *            王
	 * @param isbegin
	 *            是否起手
	 * @param moMajiang
	 *            是否摸牌
	 * @param qGangHu
	 *            是否抢杠胡
	 * @param qGangHu
	 *            是否杠上花
	 * @return
	 */
	public static AhmjHu isHuAHMajiang(List<Ahmj> majiangIds, List<Ahmj> gang, List<Ahmj> peng, List<Ahmj> chi, List<Ahmj> buzhang, Ahmj wang, boolean fourWang, boolean isbegin,
                                       boolean moMajiang, boolean qGangHu, boolean gangshanghua) {
		AhmjHu hu = new AhmjHu();
		if (majiangIds == null || majiangIds.isEmpty()) {
			return hu;
		}

		if (majiangIds.size() % 3 != 2) {
			if (isbegin) {
				List<Integer> wangValList = null;
				if (fourWang) {
					wangValList = findFourWangValList(wang);
				} else {
					wangValList = findWangValList(wang);
				}

				List<Ahmj> wangList = findWangList(majiangIds, wangValList);
				hu.setWangMajiangList(wangList);
				hu.setWangValList(wangValList);
				// 是否开始抓王天胡
				AhmjRule.checkWangDaHu(hu);
				if (hu.getWangType() == 4 || hu.getWangType() == 7) {
					hu.setStartHu(true);
					hu.setHu(true);
				}
			}

			//System.out.println("%3！=2");
			return hu;

		}
		List<Integer> wangValList = null;
		if (fourWang) {
			wangValList = findFourWangValList(wang);
		} else {
			wangValList = findWangValList(wang);
		}

		List<Ahmj> wangList = findWangList(majiangIds, wangValList);
		hu.setWangMajiangList(wangList);
		hu.setWangValList(wangValList);
		List<Ahmj> withoutWang = new ArrayList<>(majiangIds);
		withoutWang.removeAll(wangList);
		hu.setWithoutWangMajiangs(withoutWang);
		AhmjRule.checkWangDaHu(hu);
		if (!qGangHu && !moMajiang && !wangList.isEmpty()) {
			// 有王除了杠上花和杠上炮之外 都不能接炮
			return hu;
		}

		// 三王及以上不能接杠上炮
		if (!gangshanghua && qGangHu && hu.getWangDahuNum() > 0) {
			return hu;
		}

		boolean isHu = isHuWangMajiang(majiangIds, hu);
		hu.setHu(isHu);
		AhmjRule.checkDahu(hu, majiangIds, gang, peng, chi, buzhang);
		if (!isHu && isbegin && (hu.getWangType() == 4 || hu.getWangType() == 7)) {
			hu.setStartHu(true);
			hu.setHu(true);
		}
		hu.setShowMajiangs(majiangIds);

		// 看胡的牌型
		if (wangList.isEmpty()) {
			// 有王

		} else {
			// 没有王

		}

		return hu;
	}

	/**
	 * 安化麻将胡牌
	 * 
	 * @param majiangIds
	 * @return
	 */
	public static boolean isHuWangMajiang(List<Ahmj> majiangIds, AhmjHu hu, boolean isNeed258jiang) {
		if (majiangIds == null || majiangIds.isEmpty()) {
			return false;
		}

		// List<List<Integer>> huList = new ArrayList<>();
		List<Ahmj> copy = new ArrayList<>(majiangIds);
		// 先去掉红中
		List<Ahmj> wangList = QipaiTool.dropMajiang(copy, hu.getWangValList());
		// if (wangList.size() == 4) {
		// // 4张红中直接胡
		// return true;
		// }
		if (majiangIds.size() % 3 != 2) {
			//System.out.println("%3！=2");
			return false;

		}

		MajiangIndexArr card_index = new MajiangIndexArr();
		QipaiTool.getMax(card_index, copy);
		if (check7duizi(copy, card_index, wangList.size())) {
			//System.out.println("胡牌7对");
			return true;
		}
		// 拆将
		if (chaijiang(card_index, copy, wangList.size(), isNeed258jiang)) {
			//System.out.println("胡牌");
			return true;
		} else {
			//System.out.println("不能胡");
			return false;
		}

	}

	// 拆将
	public static boolean chaijiang(MajiangIndexArr card_index, List<Ahmj> hasPais, int hongzhongnum, boolean needJiang258) {
		Map<Integer, List<Ahmj>> jiangMap = card_index.getJiang(needJiang258);
		for (Entry<Integer, List<Ahmj>> valEntry : jiangMap.entrySet()) {
			List<Ahmj> copy = new ArrayList<>(hasPais);
			MajiangHuLack lack = new MajiangHuLack(hongzhongnum);
			List<Ahmj> list = valEntry.getValue();
			int i = 0;
			for (Ahmj majiang : list) {
				i++;
				copy.remove(majiang);
				if (i >= 2) {
					break;
				}
			}
			lack.setHasJiang(true);
			boolean hu = chaipai(lack, copy, needJiang258);
			if (hu) {
				//System.out.println(JacksonUtil.writeValueAsString(lack));
				return hu;
			}
		}

		if (hongzhongnum > 0) {
			// 只剩下红中
			if (hasPais.isEmpty()) {
				return true;
			}
			// 没有将
			for (Ahmj majiang : hasPais) {
				List<Ahmj> copy = new ArrayList<>(hasPais);
				MajiangHuLack lack = new MajiangHuLack(hongzhongnum);
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
					//System.out.println(JacksonUtil.writeValueAsString(lack));
					return true;
				}
				if (!lack.isHasJiang() && hu) {
					if (lack.getHongzhongNum() == 2) {
						// 红中做将
						//System.out.println(JacksonUtil.writeValueAsString(lack));
						return true;
					}
				}
			}

			//System.out.println("没有找到将");
		}

		return false;
	}

	public static void sortMin(List<Ahmj> hasPais) {
		Collections.sort(hasPais, new Comparator<Ahmj>() {

			@Override
			public int compare(Ahmj o1, Ahmj o2) {
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
	public static boolean chaishun(MajiangHuLack lack, List<Ahmj> hasPais, boolean needJiang258) {
		if (hasPais.isEmpty()) {
			return true;
		}
		sortMin(hasPais);
		Ahmj minMajiang = hasPais.get(0);
		int minVal = minMajiang.getVal();
		List<Ahmj> minList = QipaiTool.getVal(hasPais, minVal);
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
		List<Ahmj> num1 = QipaiTool.getVal(hasPais, pai1);
		List<Ahmj> num2 = QipaiTool.getVal(hasPais, pai2);
		List<Ahmj> num3 = QipaiTool.getVal(hasPais, pai3);

		// 找到一句话的麻将
		List<Ahmj> hasMajiangList = new ArrayList<>();
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
				List<Ahmj> count = QipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
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

				List<Ahmj> count = QipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
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
				List<Ahmj> count1 = QipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
				List<Ahmj> count2 = QipaiTool.getVal(hasPais, hasMajiangList.get(1).getVal());
				List<Ahmj> count3 = QipaiTool.getVal(hasPais, hasMajiangList.get(2).getVal());
				if (count1.size() >= 2 && (count2.size() == 1 || count3.size() == 1)) {
					List<Ahmj> copy = new ArrayList<>(hasPais);
					copy.removeAll(count1);
					MajiangHuLack copyLack = lack.copy();
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

	public static boolean isCanAsJiang(Ahmj majiang, boolean isNeed258) {
		if (isNeed258) {
			if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
				return true;
			}
			return false;
		} else {
			return true;
		}

	}

	// 拆牌
	public static boolean chaipai(MajiangHuLack lack, List<Ahmj> hasPais, boolean isNeedJiang258) {
		if (hasPais.isEmpty()) {
			return true;

		}
		boolean hu = chaishun(lack, hasPais, isNeedJiang258);
		if (hu)
			return true;
		return false;
	}

	/**
	 * 红中麻将没有7小对，所以不用红中补
	 * 
	 * @param majiangIds
	 * @param card_index
	 */
	public static boolean check7duizi(List<Ahmj> majiangIds, MajiangIndexArr card_index, int hongzhongNum) {
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

			MajiangIndex index0 = card_index.getMajiangIndex(0);
			MajiangIndex index2 = card_index.getMajiangIndex(2);
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
	 * 安化麻将胡牌
	 * 
	 * @param majiangIds
	 * @return
	 */
	public static boolean isHuWangMajiang(List<Ahmj> majiangIds, AhmjHu hu) {
		return isHuWangMajiang(majiangIds, hu, true);

	}

	/**
	 * 找出王的值list
	 * 
	 * @param wang
	 * @return
	 */
	public static List<Integer> findWangValList(Ahmj wang) {
		List<Integer> list = new ArrayList<>();
		if (wang == null) {
			return list;
		}
		int fristWangVal = wang.getVal();
		int secondVal = 0;
		if ((fristWangVal + 1) % 10 == 0) {
			secondVal = (wang.getVal() / 10) * 10 + 1;
		} else {
			secondVal = fristWangVal + 1;
		}

		list.add(fristWangVal);
		list.add(secondVal);
		return list;

	}

	/**
	 * 找出王的值list
	 * 
	 * @param wang
	 * @return
	 */
	public static List<Integer> findFourWangValList(Ahmj wang) {
		List<Integer> list = new ArrayList<>();
		if (wang == null) {
			return list;
		}
		int fristWangVal = wang.getVal();
		int secondVal = 0;
		if ((fristWangVal + 1) % 10 == 0) {
			secondVal = (wang.getVal() / 10) * 10 + 1;
		} else {
			secondVal = fristWangVal + 1;
		}
		list.add(secondVal);
		return list;

	}

	/**
	 * 找出王的麻将
	 * 
	 * @param majiangIds
	 * @param wang
	 * @return
	 */
	public static List<Ahmj> findWangList(List<Ahmj> majiangIds, List<Integer> valList) {
		List<Ahmj> wangList = new ArrayList<>();
		for (Ahmj majiang : majiangIds) {
			if (valList.contains(majiang.getVal()) || majiang.getVal() == 1000) {
				wangList.add(majiang);
				if (majiang.getVal() == 1000 && !valList.contains(1000)) {
					valList.add(1000);
				}
			}
		}
		return wangList;

	}

}
