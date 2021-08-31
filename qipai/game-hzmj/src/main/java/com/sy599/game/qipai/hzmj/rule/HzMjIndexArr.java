package com.sy599.game.qipai.hzmj.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sy599.game.util.JacksonUtil;

/**
 * 判断牌型
 * 
 * @author upstream
 * 
 */
public class HzMjIndexArr {
	HzMjIndex[] a = new HzMjIndex[5];

	public void addMajiangIndex(int count, List<HzMj> majiangList, int val) {
		if (a[count] == null) {
			a[count] = new HzMjIndex();
		}
		a[count].addMajiang(val, majiangList);
		a[count].addVal(val);
	}

	/**
	 * 根据牌的张数得到牌
	 * 
	 * @param size
	 *            张数
	 * @return
	 */
	public Map<Integer, List<HzMj>> getMajiangMap(int size) {
		Map<Integer, List<HzMj>> map = new HashMap<>();
		for (int i = 0; i < a.length; i++) {
			if (size <= i + 1) {
				HzMjIndex majiangIndex = a[i];
				if (majiangIndex != null) {
					map.putAll(majiangIndex.getMajiangValMap());
				}
			}

		}
		return map;
	}

	public Map<Integer, List<HzMj>> getJiang(boolean need258) {
		Map<Integer, List<HzMj>> map = new HashMap<>();
		for (int i = 0; i < a.length; i++) {
			if (2 <= i + 1) {
				HzMjIndex majiangIndex = a[i];
				if (majiangIndex != null) {
					if (need258) {
						for (Entry<Integer, List<HzMj>> entry : majiangIndex.getMajiangValMap().entrySet()) {
							int pai = entry.getKey() % 10;
							if (pai == 2 || pai == 5 || pai == 8) {
								map.put(entry.getKey(), entry.getValue());
							}
						}
					} else {
						map.putAll(majiangIndex.getMajiangValMap());

					}
				}
			}

		}
		return map;
	}

	/**
	 * 牌的张数大于2的 (对子数)
	 * 
	 * @return
	 */
	public int getDuiziNum() {
		int num = 0;
		for (int i = 1; i < a.length; i++) {
			HzMjIndex majiangIndex = a[i];
			if (majiangIndex == null) {
				continue;
			}
			if (i == 3) {
				num += majiangIndex.getLength() * 2;
			} else {
				num += majiangIndex.getLength();

			}
		}
		return num;
	}

	/**
	 * 牌的张数大于3的 (刻字数)
	 * 
	 * @return
	 */
	public int getKeziNum() {
		int num = 0;
		for (int i = 2; i < a.length; i++) {
			HzMjIndex majiangIndex = a[i];
			if (majiangIndex == null) {
				continue;
			}
			num += majiangIndex.getLength();

		}
		return num;
	}

	public List<HzMj> getKeziList() {
		List<HzMj> list = new ArrayList<>();
		for (int i = 2; i < a.length; i++) {
			HzMjIndex majiangIndex = a[i];
			if (majiangIndex == null) {
				continue;
			}
			list.addAll(majiangIndex.getMajiangs());

		}
		return list;
	}

	/**
	 * 得到牌
	 * 
	 * @param index
	 *            0一张 , 1 二张 , 2 三张 , 3 四张
	 * @return
	 */
	public HzMjIndex getMajiangIndex(int index) {
		HzMjIndex majiangIndex = a[index];
		if (majiangIndex == null) {
			// return new HzMjIndex();
		}
		return majiangIndex;
	}

	public String tostr() {
		int i = 0;
		String str = "";
		for (HzMjIndex majiang : a) {
			if (majiang == null) {
				continue;
			}
			str += i + "  " + JacksonUtil.writeValueAsString(majiang.getValList()) + " -->" + JacksonUtil.writeValueAsString(majiang.getMajiangValMap()) + "\n";
			i++;
		}
		return str;

	}
}
