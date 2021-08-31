package com.sy599.game.qipai.hbgzp.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.sy599.game.qipai.hbgzp.bean.HbgzpPlayer;
import com.sy599.game.qipai.hbgzp.rule.Hbgzp;

public class HbgzpHelper {

	/**
	 * 删掉某个值
	 * 
	 * @param copy
	 * @return
	 */
	public static List<Hbgzp> dropVal(List<Hbgzp> copy, int val, int count) {
		List<Hbgzp> hongzhong = new ArrayList<>();
		Iterator<Hbgzp> iterator = copy.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			Hbgzp majiang = iterator.next();
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
	public static List<Hbgzp> dropVal(List<Hbgzp> copy, int val) {
		List<Hbgzp> hongzhong = new ArrayList<>();
		Iterator<Hbgzp> iterator = copy.iterator();
		while (iterator.hasNext()) {
			Hbgzp majiang = iterator.next();
			if (majiang.getVal() == val) {
				hongzhong.add(majiang);
				iterator.remove();
			}
		}
		return hongzhong;
	}

	/**
	 * 麻将val的个数  不包括捡的牌
	 * 
	 * @param majiangs
	 * @param majiangVal
	 * @return
	 */
	public static int getMajiangCount(HbgzpPlayer play,List<Hbgzp> majiangs, int majiangVal) {
		int count = 0;
		for (Hbgzp majiang : majiangs) {
			if (majiang.getVal() == majiangVal && !play.isChiCard(majiang)) {
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
	public static List<Hbgzp> getMajiangList(List<Hbgzp> majiangs, int majiangVal) {
		List<Hbgzp> list = new ArrayList<>();
		for (Hbgzp majiang : majiangs) {
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
	public static boolean isMajiangRepeat(List<Hbgzp> majiangs) {
		if (majiangs == null) {
			return false;
		}

		Map<Integer, Integer> map = new HashMap<>();
		for (Hbgzp mj : majiangs) {
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
	public static List<Integer> toMajiangIds(List<Hbgzp> majiangs) {
		List<Integer> majiangIds = new ArrayList<>();
		if (majiangs == null) {
			return majiangIds;
		}
		for (Hbgzp majiang : majiangs) {
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
	public static String toMajiangStrs(List<Hbgzp> majiangs) {
		StringBuffer sb = new StringBuffer();
		if (majiangs == null) {
			return sb.toString();
		}
		for (Hbgzp majiang : majiangs) {
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
	public static List<Integer> toMajiangVals(List<Hbgzp> majiangs) {
		List<Integer> majiangIds = new ArrayList<>();
		if (majiangs == null) {
			return majiangIds;
		}
		for (Hbgzp majiang : majiangs) {
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
	public static List<Integer> toRepeatMajiangVals(List<Hbgzp> majiangs) {
		List<Integer> majiangVals = new ArrayList<>();
		if (majiangs == null) {
			return majiangVals;
		}
		for (Hbgzp majiang : majiangs) {
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
	public static Map<Integer, Integer> toMajiangValMap(HbgzpPlayer play,List<Hbgzp> majiangs) {
		Map<Integer, Integer> majiangIds = new HashMap<>();
		if (majiangs == null) {
			return majiangIds;
		}
		for (Hbgzp majiang : majiangs) {
			if(play.isChiCard(majiang)){
				continue;
			}
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
	public static List<Hbgzp> toMajiang(List<Integer> majiangIds) {
		if (majiangIds == null) {
			return new ArrayList<>();
		}
		List<Hbgzp> majiangs = new ArrayList<>();
		for (int majiangId : majiangIds) {
			if (majiangId == 0) {
				continue;
			}
			majiangs.add(Hbgzp.getPaohzCard(majiangId));
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
	public static List<Hbgzp> explodeMajiang(String str, String delimiter) {
		List<Hbgzp> list = new ArrayList<>();
		if (StringUtils.isBlank(str) || str.equals("null") || str.equals("undefined"))
			return list;
		String strArray[] = str.split(delimiter);

		for (String val : strArray) {
			Hbgzp majiang = null;
			if (val.startsWith("mj")) {
				majiang = Hbgzp.valueOf(Hbgzp.class, val);
			} else {
				Integer intVal = (Integer.valueOf(val));
				if (intVal == 0) {
					continue;
				}
				majiang = Hbgzp.getPaohzCard(intVal);
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
	public static String implodeMajiang(List<Hbgzp> array, String delimiter) {
		StringBuilder sb = new StringBuilder("");
		for (Hbgzp i : array) {
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
	public static int getCountByVal(List<Hbgzp> copy, int val) {
		int count = 0;
		for (Hbgzp majiang : copy) {
			if (majiang.getVal() == val) {
				count++;
			}
		}
		return count;
	}

	public static Integer findMajiangIdByVal(List<Integer> copy, int val) {
		for (int majiangId : copy) {
			Hbgzp majiang = Hbgzp.getPaohzCard(majiangId);
			if (majiang.getVal() == val) {
				return majiangId;
			}
		}
		return 0;
	}

	public static Hbgzp findMajiangByVal(List<Hbgzp> copy, int val) {
		for (Hbgzp majiang : copy) {
			if (majiang.getVal() == val) {
				return majiang;
			}
		}
		return null;
	}

	public static List<Hbgzp> find(List<Integer> copy, List<Integer> valList) {
		List<Hbgzp> pai = new ArrayList<>();
		if (!valList.isEmpty()) {
			for (int zpId : valList) {
				Iterator<Integer> iterator = copy.iterator();
				while (iterator.hasNext()) {
					int card = iterator.next();
					Hbgzp majiang = Hbgzp.getPaohzCard(card);
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
