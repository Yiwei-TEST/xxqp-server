package com.sy599.game.qipai.jzmj.rule;

import org.apache.commons.lang.StringUtils;

import java.util.*;

public class JzMjHelper {

	/**==有板板胡
	 * 麻将val的个数
	 * 
	 * @param majiangs
	 * @param majiangVal
	 * @return
	 */
	public static int getMajiangCount(List<JzMj> majiangs, int majiangVal) {
		int count = 0;
		for (JzMj majiang : majiangs) {
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
	public static List<JzMj> getMajiangList(List<JzMj> majiangs, int majiangVal) {
		List<JzMj> list = new ArrayList<>();
		for (JzMj majiang : majiangs) {
			if (majiang.getVal() == majiangVal) {
				list.add(majiang);
			}
		}
		return list;
	}

	/**
	 * 检查麻将是否有重复
	 *
	 * @param majiangs
	 * @return
	 */
	public static boolean isMajiangRepeat(List<JzMj> majiangs) {
		if (majiangs == null) {
			return false;
		}

		Map<Integer, Integer> map = new HashMap<>();
		for (JzMj mj : majiangs) {
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
	public static List<Integer> toMajiangIds(List<JzMj> majiangs) {
		List<Integer> majiangIds = new ArrayList<>();
		if (majiangs == null) {
			return majiangIds;
		}
		for (JzMj majiang : majiangs) {
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
	public static String toMajiangStrs(List<JzMj> majiangs) {
		StringBuffer sb = new StringBuffer();
		if (majiangs == null) {
			return sb.toString();
		}
		for (JzMj majiang : majiangs) {
			sb.append(majiang.getId()).append(",");

		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * 麻将转化为majiangIds
	 *
	 * @param majiangs
	 * @return
	 */
	public static List<Integer> toMajiangVals(List<JzMj> majiangs) {
		List<Integer> majiangIds = new ArrayList<>();
		if (majiangs == null) {
			return majiangIds;
		}
		for (JzMj majiang : majiangs) {
			majiangIds.add(majiang.getVal());
		}
		return majiangIds;
	}

	/**
	 * 麻将转化为majiangIds
	 *
	 * @param majiangs
	 * @return
	 */
	public static List<Integer> toRepeatMajiangVals(List<JzMj> majiangs) {
		List<Integer> majiangVals = new ArrayList<>();
		if (majiangs == null) {
			return majiangVals;
		}
		for (JzMj majiang : majiangs) {
			if (!majiangVals.contains(majiang.getVal())) {
				majiangVals.add(majiang.getVal());

			}
		}
		return majiangVals;
	}

	/**
	 * 麻将转化为Map<val,valNum>
	 *
	 * @param majiangs
	 * @return
	 */
	public static Map<Integer, Integer> toMajiangValMap(List<JzMj> majiangs) {
		Map<Integer, Integer> majiangIds = new HashMap<>();
		if (majiangs == null) {
			return majiangIds;
		}
		for (JzMj majiang : majiangs) {

			if (majiangIds.containsKey(majiang.getVal())) {
				majiangIds.put(majiang.getVal(), majiangIds.get(majiang.getVal()) + 1);
			} else {
				majiangIds.put(majiang.getVal(), 1);
			}
		}
		return majiangIds;
	}

	/**
	 * 麻将Id转化为麻将
	 *
	 * @param majiangIds
	 * @return
	 */
	public static List<JzMj> toMajiang(List<Integer> majiangIds) {
		if (majiangIds == null) {
			return new ArrayList<>();
		}
		List<JzMj> majiangs = new ArrayList<>();
		for (int majiangId : majiangIds) {
			if (majiangId == 0) {
				continue;
			}
			majiangs.add(JzMj.getMajang(majiangId));
		}
		return majiangs;
	}

	/**
	 * 将array组合成用delimiter分隔的字符串
	 *
	 * @param array
	 * @param delimiter
	 * @return String
	 */
	public static List<JzMj> explodeMajiang(String str, String delimiter) {
		List<JzMj> list = new ArrayList<>();
		if (StringUtils.isBlank(str) || str.equals("null") || str.equals("undefined"))
			return list;
		String strArray[] = str.split(delimiter);

		for (String val : strArray) {
			JzMj majiang = null;
			if (val.startsWith("mj")) {
				majiang = JzMj.valueOf(JzMj.class, val);
			} else {
				Integer intVal = (Integer.valueOf(val));
				if (intVal == 0) {
					continue;
				}
				majiang = JzMj.getMajang(intVal);
			}
			list.add(majiang);
		}
		return list;
	}

	/**
	 * 将array组合成用delimiter分隔的字符串
	 *
	 * @param array
	 * @param delimiter
	 * @return String
	 */
	public static String implodeMajiang(List<JzMj> array, String delimiter) {
		StringBuilder sb = new StringBuilder("");
		for (JzMj i : array) {
			sb.append(i.getId());
			sb.append(delimiter);
		}
		if (sb.length() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		return sb.toString();
	}

	/**
	 * val数量
	 *
	 * @param copy
	 * @param val
	 * @return
	 */
	public static int getCountByVal(List<JzMj> copy, int val) {
		int count = 0;
		for (JzMj majiang : copy) {
			if (majiang.getVal() == val) {
				count++;
			}
		}
		return count;
	}

	public static Integer findMajiangIdByVal(List<Integer> copy, int val) {
		for (int majiangId : copy) {
			JzMj majiang = JzMj.getMajang(majiangId);
			if (majiang.getVal() == val) {
				return majiangId;
			}
		}
		return 0;
	}

	public static JzMj findMajiangByVal(List<JzMj> copy, int val) {
		for (JzMj majiang : copy) {
			if (majiang.getVal() == val) {
				return majiang;
			}
		}
		return null;
	}

	public static List<JzMj> find(List<Integer> copy, List<Integer> valList) {
		List<JzMj> pai = new ArrayList<>();
		if (!valList.isEmpty()) {
			for (int zpId : valList) {
				Iterator<Integer> iterator = copy.iterator();
				while (iterator.hasNext()) {
					int card = iterator.next();
					JzMj majiang = JzMj.getMajang(card);
					if (majiang.getVal() == zpId) {
						pai.add(majiang);
						iterator.remove();
						break;
					}
				}
			}

		}
		return pai;
	}

}
