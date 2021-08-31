package com.sy599.game.qipai.ahmj.tool;

import com.sy599.game.GameServerConfig;
import com.sy599.game.qipai.ahmj.constant.AhmjConstants;
import com.sy599.game.qipai.ahmj.constant.Ahmj;
import com.sy599.game.qipai.ahmj.rule.MajiangIndex;
import com.sy599.game.qipai.ahmj.rule.MajiangIndexArr;
import com.sy599.game.util.JacksonUtil;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author lc
 * 
 */
public class AhmjTool {

	public static synchronized List<List<Ahmj>> fapai(List<Integer> copy, int playerCount, List<List<Integer>> t) {
		if(t.size()<playerCount)
			return null;
		List<List<Ahmj>> list = new ArrayList<>();
		for (List<Integer> zp : t) {
			list.add(find(copy, zp));
		}
		Collections.shuffle(copy);
		if(true){
			//不需要补张

		}else {
			//补张
			for (int i = 0; i < playerCount; i++) {
				List<Ahmj> ahmjs = list.get(i);
				if(i==0){
					for (int j = ahmjs.size(); j < 14; j++) {
						ahmjs.add(Ahmj.getMajang(copy.get(j)));
						copy.remove(j);
					}
				}else {
					for (int j = ahmjs.size(); j < 13; j++) {
						ahmjs.add(Ahmj.getMajang(copy.get(j)));
						copy.remove(j);
					}
				}
			}
		}

		if(list.size()>playerCount){
			List<Ahmj> l=list.get(playerCount);
			for (int i = 0; i < copy.size(); i++) {
				l.add(Ahmj.getMajang(copy.get(i)));
			}
			list.add(l);
		}else if(list.size()==playerCount){
			List<Ahmj> l=new ArrayList<>();
			for (int i = 0; i < copy.size(); i++) {
				l.add(Ahmj.getMajang(copy.get(i)));
			}
			list.add(l);
		}

		return list;
	}

	private static List<Ahmj> find(List<Integer> copy, List<Integer> valList) {
		List<Ahmj> pai = new ArrayList<>();
		if (!valList.isEmpty()) {
			for (int zpId : valList) {
				Iterator<Integer> iterator = copy.iterator();
				while (iterator.hasNext()) {
					int card = iterator.next();
					Ahmj mj = Ahmj.getMajang(card);
					if (mj.getVal() == zpId) {
						pai.add(mj);
						iterator.remove();
						break;
					}
				}
			}
		}
		return pai;
	}

	public static synchronized List<List<Ahmj>> fapai(List<Integer> copy, int playerCount) {
		List<List<Ahmj>> list = new ArrayList<>();
		Collections.shuffle(copy);
		List<Ahmj> allMjs = new ArrayList<>();
		for (int id : copy) {
			allMjs.add(Ahmj.getMajang(id));
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



	public static boolean isTing(List<Ahmj> majiangIds, boolean hu7dui) {
		return true;
	}

	/**
	 * 转转麻将胡牌
	 * 
	 * @param majiangIds
	 * @return
	 */
	public static boolean isHuZhuanzhuan(List<Ahmj> majiangIds) {
		if (majiangIds == null || majiangIds.isEmpty()) {
			return false;
		}

		List<Ahmj> copy = new ArrayList<>(majiangIds);
		// 先去掉红中
		List<Ahmj> hongzhongList = dropHongzhong(copy);
		if (hongzhongList.size() == 4) {
			// 4张红中直接胡
			return true;
		}
		if (majiangIds.size() % 3 != 2) {
			System.out.println("%3！=2");
			return false;

		}

		MajiangIndexArr card_index = new MajiangIndexArr();
		QipaiTool.getMax(card_index, copy);
		if (check7duizi(copy, card_index, hongzhongList.size())) {
			System.out.println("胡牌7对");
			return true;
		}
		// 拆将
		if (chaijiang(card_index, copy, hongzhongList.size(), false)) {
			System.out.println("胡牌");
			return true;
		} else {
			System.out.println("不能胡");
			return false;
		}

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

			System.out.println("没有找到将");
		}

		return false;
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


	/**
	 * 是否能吃
	 * 
	 * @param majiangs
	 * @param dismajiang
	 * @return
	 */
	public static List<Ahmj> checkChi(List<Ahmj> majiangs, Ahmj dismajiang, List<Integer> wangValList) {
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
		return new ArrayList<Ahmj>();
	}

	public static List<Ahmj> findMajiangByVals(List<Ahmj> majiangs, List<Integer> vals) {
		List<Ahmj> result = new ArrayList<>();
		for (int val : vals) {
			for (Ahmj majiang : majiangs) {
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
	public static List<Ahmj> dropHongzhong(List<Ahmj> copy) {
		List<Ahmj> hongzhong = new ArrayList<>();
		Iterator<Ahmj> iterator = copy.iterator();
		while (iterator.hasNext()) {
			Ahmj majiang = iterator.next();
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
				if (majiang instanceof Ahmj) {
					val = ((Ahmj) majiang).getVal();
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
	 * @param majiangs
	 *            麻将牌
	 * @param majiang
	 *            麻将
	 * @param num
	 *            想要的数量
	 * @return
	 */
	public static List<Ahmj> getSameMajiang(List<Ahmj> majiangs, Ahmj majiang, int num) {
		List<Ahmj> hongzhong = new ArrayList<>();
		int i = 0;
		for (Ahmj maji : majiangs) {
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
	public static List<Ahmj> dropMjId(List<Ahmj> copy, int id) {
		List<Ahmj> hongzhong = new ArrayList<>();
		Iterator<Ahmj> iterator = copy.iterator();
		while (iterator.hasNext()) {
			Ahmj majiang = iterator.next();
			if (majiang.getId() == id) {
				hongzhong.add(majiang);
				iterator.remove();
			}
		}
		return hongzhong;
	}

	public static void main(String[] args) {
		List<Ahmj> majiangIds = new ArrayList<>();
		List<Integer> vals = Arrays.asList(27, 27, 31, 31, 32, 32, 33, 33, 34, 35, 36, 37, 38, 39);
		for (int val : vals) {
			for (Ahmj majiang : Ahmj.values()) {
				if (majiang.getVal() == val && !majiangIds.contains(majiang)) {
					majiangIds.add(majiang);
					break;
				}
			}
		}
		boolean pinghu = isHuZhuanzhuan(majiangIds);
		System.out.println(pinghu);
	}
}
