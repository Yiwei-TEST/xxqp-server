package com.sy599.game.qipai.tdhmj.tool;

import org.apache.commons.lang3.StringUtils;

import com.sy599.game.qipai.tdhmj.rule.TdhMj;
import com.sy599.game.qipai.tdhmj.rule.TdhMjIndexArr;

import java.util.*;

public class TdhMjQipaiTool {
	/**
	 * 麻将转化为majiangIds
	 * 
	 * @param majiangs
	 * @return
	 */
	public static List<Integer> toRepeatMajiangVals(List<TdhMj> majiangs) {
		List<Integer> majiangVals = new ArrayList<>();
		if (majiangs == null) {
			return majiangVals;
		}
		for (TdhMj majiang : majiangs) {
			if (!majiangVals.contains(majiang.getVal())) {
				majiangVals.add(majiang.getVal());

			}
		}
		return majiangVals;
	}
	/**
	 * 检查麻将是否有重复
	 * 
	 * @param majiangs
	 * @return
	 */
	public static boolean isMajiangRepeat(List<TdhMj> majiangs) {
		if (majiangs == null) {
			return false;
		}

		Map<Integer, Integer> map = new HashMap<>();
		for (TdhMj mj : majiangs) {
			int count = 0;
			if (map.containsKey(mj.getId())) {
				count = map.get(mj.getId());
			}
			map.put(mj.getId(), count + 1);
		}
		for (int count : map.values()) {
			if (count > 1) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 麻将转化为majiangIds
	 * 
	 * @param majiangs
	 * @return
	 */
	public static String toMajiangStrs(List<TdhMj> majiangs) {
		StringBuffer sb = new StringBuffer();
		if (majiangs == null) {
			return sb.toString();
		}
		for (TdhMj majiang : majiangs) {
			sb.append(majiang.getId()).append(",");

		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}
	/**
	 * 将array组合成用delimiter分隔的字符串
	 * 
	 * @param array
	 * @param delimiter
	 * @return String
	 */
	public static String implodeMajiang(List<TdhMj> array, String delimiter) {
		StringBuilder sb = new StringBuilder("");
		for (TdhMj i : array) {
			sb.append(i.getId());
			sb.append(delimiter);
		}
		if (sb.length() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		return sb.toString();
	}
	/**
	 * 麻将val的个数
	 * 
	 * @param majiangs
	 * @param majiangVal
	 * @return
	 */
	public static int getMajiangCount(List<TdhMj> majiangs, int majiangVal) {
		int count = 0;
		for (TdhMj majiang : majiangs) {
			if (majiang.getVal() == majiangVal) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 麻将val的List
	 * 
	 * @param majiangs
	 * @param majiangVal
	 * @return
	 */
	public static List<TdhMj> getMajiangList(List<TdhMj> majiangs, int majiangVal) {
		List<TdhMj> list = new ArrayList<>();
		for (TdhMj majiang : majiangs) {
			if (majiang.getVal() == majiangVal) {
				list.add(majiang);
			}
		}
		return list;
	}
	public static Integer findMajiangIdByVal(List<Integer> copy, int val) {
		for (int majiangId : copy) {
			TdhMj majiang = TdhMj.getMajang(majiangId);
			if (majiang.getVal() == val) {
				return majiangId;
			}
		}
		return 0;
	}

	public static TdhMj findMajiangByVal(List<TdhMj> copy, int val) {
		for (TdhMj majiang : copy) {
			if (majiang.getVal() == val) {
				return majiang;
			}
		}
		return null;
	}
	/**
	 * 麻将转化为Map<val,valNum>
	 * 
	 * @param majiangs
	 * @return
	 */
	public static Map<Integer, Integer> toMajiangValMap(List<TdhMj> majiangs) {
		Map<Integer, Integer> majiangIds = new HashMap<>();
		if (majiangs == null) {
			return majiangIds;
		}
		for (TdhMj majiang : majiangs) {

			if (majiangIds.containsKey(majiang.getVal())) {
				majiangIds.put(majiang.getVal(), majiangIds.get(majiang.getVal()) + 1);
			} else {
				majiangIds.put(majiang.getVal(), 1);
			}
		}
		return majiangIds;
	}
	
	public static List<TdhMj> findMajiangByVals(List<TdhMj> majiangs, List<Integer> vals) {
		List<TdhMj> result = new ArrayList<>();
		for (int val : vals) {
			for (TdhMj majiang : majiangs) {
				if (majiang.getVal() == val) {
					result.add(majiang);
				}
			}
		}
		return result;
	}

	/**
	 * 将array组合成用delimiter分隔的字符串
	 * 
	 * @param str
	 * @param delimiter
	 * @return String
	 */
	public static List<TdhMj> explodeMajiang(String str, String delimiter) {
		List<TdhMj> list = new ArrayList<>();
		if (StringUtils.isBlank(str) || str.equals("null") || str.equals("undefined"))
			return list;
		String strArray[] = str.split(delimiter);

		for (String val : strArray) {
			TdhMj majiang = null;
			if (val.startsWith("mj")) {
				majiang = TdhMj.valueOf(TdhMj.class, val);
			} else {
				Integer intVal = (Integer.valueOf(val));
				if (intVal == 0) {
					continue;
				}
				majiang = TdhMj.getMajang(intVal);
			}
			list.add(majiang);
		}
		return list;
	}

	/**
	 * 麻将转化为majiangIds
	 * 
	 * @param majiangs
	 * @return
	 */
	public static List<Integer> toMajiangIds(List<TdhMj> majiangs) {
		List<Integer> majiangIds = new ArrayList<>();
		if (majiangs == null) {
			return majiangIds;
		}
		for (TdhMj majiang : majiangs) {
			majiangIds.add(majiang.getId());
		}
		return majiangIds;
	}

	/**
	 * 麻将转化为majiangIds
	 * 
	 * @param majiangs
	 * @return
	 */
	public static List<Integer> toMajiangVals(List<TdhMj> majiangs) {
		List<Integer> majiangIds = new ArrayList<>();
		if (majiangs == null) {
			return majiangIds;
		}
		for (TdhMj majiang : majiangs) {
			majiangIds.add(majiang.getVal());
		}
		return majiangIds;
	}

	/**
	 * 麻将Id转化为麻将
	 * 
	 * @param majiangIds
	 * @return
	 */
	public static List<TdhMj> toMajiang(List<Integer> majiangIds) {
		if (majiangIds == null) {
			return new ArrayList<>();
		}
		List<TdhMj> majiangs = new ArrayList<>();
		for (int majiangId : majiangIds) {
			if (majiangId == 0) {
				continue;
			}
			majiangs.add(TdhMj.getMajang(majiangId));
		}
		return majiangs;
	}

	/**
	 * 得到最大相同数
	 * 
	 * @param card_index
	 * @param list
	 */
	public static void getMax(TdhMjIndexArr card_index, List<TdhMj> list) {
		Map<Integer, List<TdhMj>> majiangMap = new HashMap<Integer, List<TdhMj>>();
		for (int i = 0; i < list.size(); i++) {
			TdhMj majiang = list.get(i);
			List<TdhMj> count = null;
			if (majiangMap.containsKey(majiang.getVal())) {
				count = majiangMap.get(majiang.getVal());
			} else {
				count = new ArrayList<>();
				majiangMap.put(majiang.getVal(), count);
			}
			count.add(majiang);
		}
		for (int majiangVal : majiangMap.keySet()) {
			List<TdhMj> majiangList = majiangMap.get(majiangVal);
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
	public static List<TdhMj> dropMajiang(List<TdhMj> copy, List<Integer> valList) {
		List<TdhMj> hongzhong = new ArrayList<>();
		Iterator<TdhMj> iterator = copy.iterator();
		while (iterator.hasNext()) {
			TdhMj majiang = iterator.next();
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
	public static List<TdhMj> dropMajiangId(List<Integer> copy, List<Integer> valList) {
		List<TdhMj> hongzhong = new ArrayList<>();
		Iterator<Integer> iterator = copy.iterator();
		while (iterator.hasNext()) {
			Integer majiangId = iterator.next();
			TdhMj majiang = TdhMj.getMajang(majiangId);
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
	public static List<TdhMj> dropVal(List<TdhMj> copy, int val, int count) {
		List<TdhMj> hongzhong = new ArrayList<>();
		Iterator<TdhMj> iterator = copy.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			TdhMj majiang = iterator.next();
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
	public static List<TdhMj> dropVal(List<TdhMj> copy, int val) {
		List<TdhMj> hongzhong = new ArrayList<>();
		Iterator<TdhMj> iterator = copy.iterator();
		while (iterator.hasNext()) {
			TdhMj majiang = iterator.next();
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
	public static List<TdhMj> getVal(List<TdhMj> copy, int val) {
		List<TdhMj> hongzhong = new ArrayList<>();
		Iterator<TdhMj> iterator = copy.iterator();
		while (iterator.hasNext()) {
			TdhMj majiang = iterator.next();
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
	public static List<TdhMj> getSameMajiang(List<TdhMj> majiangs, TdhMj majiang, int num) {
		List<TdhMj> hongzhong = new ArrayList<>();
		int i = 0;
		for (TdhMj maji : majiangs) {
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
	public static List<TdhMj> dropMjId(List<TdhMj> copy, int id) {
		List<TdhMj> hongzhong = new ArrayList<>();
		Iterator<TdhMj> iterator = copy.iterator();
		while (iterator.hasNext()) {
			TdhMj majiang = iterator.next();
			if (majiang.getId() == id) {
				hongzhong.add(majiang);
				iterator.remove();
			}
		}
		return hongzhong;
	}

	/**
	 * 找出指定val的牌子，每个val找出count张
	 * @param majiangs
	 * @param vals
	 * @param count
	 * @return
	 */
	public static List<TdhMj> findMajiangByValsAndCount(List<TdhMj> majiangs, List<Integer> vals, int count) {
		List<TdhMj> result = new ArrayList<>();
		Map<String, Integer> valCount = new HashMap<>();
		for (int val : vals) {
			for (TdhMj majiang : majiangs) {
				if (majiang.getVal() == val) {
					if (valCount.get(val+"") == null) {
						valCount.put("" + val, 1);
						result.add(majiang);
					} else {
						if (valCount.get(val+"") < count) {
							valCount.put("" + val, valCount.get("" + val) + 1);
							result.add(majiang);
						}
					}
				}
			}
		}
		return result;
	}
}
