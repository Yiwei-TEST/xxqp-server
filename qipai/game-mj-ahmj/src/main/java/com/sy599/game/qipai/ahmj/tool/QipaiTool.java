package com.sy599.game.qipai.ahmj.tool;

import com.sy599.game.qipai.ahmj.constant.Ahmj;
import com.sy599.game.qipai.ahmj.rule.MajiangIndexArr;

import java.util.*;

public class QipaiTool {

	public static Ahmj findMajiangByVal(List<Integer> copy, int val) {
		for (Integer id : copy) {
			if (Ahmj.getMajang(id).getVal() == val) {
				return Ahmj.getMajang(id);
			}
		}
		return null;
	}

	/**
	 * 得到最大相同数
	 * 
	 * @param card_index
	 * @param list
	 */
	public static void getMax(MajiangIndexArr card_index, List<Ahmj> list) {
		Map<Integer, List<Ahmj>> majiangMap = new HashMap<Integer, List<Ahmj>>();
		for (int i = 0; i < list.size(); i++) {
			Ahmj majiang = list.get(i);
			List<Ahmj> count = null;
			if (majiangMap.containsKey(majiang.getVal())) {
				count = majiangMap.get(majiang.getVal());
			} else {
				count = new ArrayList<>();
				majiangMap.put(majiang.getVal(), count);
			}
			count.add(majiang);
		}
		for (int majiangVal : majiangMap.keySet()) {
			List<Ahmj> majiangList = majiangMap.get(majiangVal);
			switch (majiangList.size()) {
			case 1:
				card_index.addMajiangIndex(0, majiangList, majiangVal);
				break;
			case 2:
				card_index.addMajiangIndex(1, majiangList, majiangVal);
				break;
			case 3:
				card_index.addMajiangIndex(2, majiangList, majiangVal);
				break;
			case 4:
				card_index.addMajiangIndex(3, majiangList, majiangVal);
				break;
			}
		}
	}
	/**
	 * 去掉麻将中指定的val
	 * 
	 * @param copy
	 * @return
	 */
	public static List<Ahmj> dropMajiang(List<Ahmj> copy, List<Integer> valList) {
		List<Ahmj> hongzhong = new ArrayList<>();
		Iterator<Ahmj> iterator = copy.iterator();
		while (iterator.hasNext()) {
			Ahmj majiang = iterator.next();
			if (valList.contains(majiang.getVal())) {
				hongzhong.add(majiang);
				iterator.remove();
			}
		}
		return hongzhong;
	}

	/**
	 * 去掉麻将中指定的val
	 * 
	 * @param copy
	 * @return
	 */
	public static List<Ahmj> dropMajiangId(List<Integer> copy, List<Integer> valList) {
		List<Ahmj> hongzhong = new ArrayList<>();
		Iterator<Integer> iterator = copy.iterator();
		while (iterator.hasNext()) {
			Integer majiangId = iterator.next();
			Ahmj majiang = Ahmj.getMajang(majiangId);
			if (valList.contains(majiang.getVal())) {
				hongzhong.add(majiang);
				iterator.remove();
			}
		}
		return hongzhong;
	}

	public static List<Integer> dropHongzhongVal(List<Integer> copy) {
		List<Integer> hongzhong = new ArrayList<>();
		Iterator<Integer> iterator = copy.iterator();
		while (iterator.hasNext()) {
			Integer majiang = iterator.next();
			if (majiang > 200) {
				hongzhong.add(majiang);
				iterator.remove();
			}
		}
		return hongzhong;
	}

	/**
	 * 删掉某个值
	 * 
	 * @param copy
	 * @return
	 */
	public static List<Ahmj> dropVal(List<Ahmj> copy, int val, int count) {
		List<Ahmj> hongzhong = new ArrayList<>();
		Iterator<Ahmj> iterator = copy.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			Ahmj majiang = iterator.next();
			if (majiang.getVal() == val) {
				i++;
				hongzhong.add(majiang);
				iterator.remove();
				if (count == i) {
					break;
				}
			}
		}
		return hongzhong;
	}

	/**
	 * 删掉某个值
	 * 
	 * @param copy
	 * @return
	 */
	public static List<Ahmj> dropVal(List<Ahmj> copy, int val) {
		List<Ahmj> hongzhong = new ArrayList<>();
		Iterator<Ahmj> iterator = copy.iterator();
		while (iterator.hasNext()) {
			Ahmj majiang = iterator.next();
			if (majiang.getVal() == val) {
				hongzhong.add(majiang);
				iterator.remove();
			}
		}
		return hongzhong;
	}

	/**
	 * 得到某个值的麻将
	 * 
	 * @param copy
	 * @return
	 */
	public static List<Ahmj> getVal(List<Ahmj> copy, int val) {
		List<Ahmj> hongzhong = new ArrayList<>();
		Iterator<Ahmj> iterator = copy.iterator();
		while (iterator.hasNext()) {
			Ahmj majiang = iterator.next();
			if (majiang.getVal() == val) {
				hongzhong.add(majiang);
			}
		}
		return hongzhong;
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
}
