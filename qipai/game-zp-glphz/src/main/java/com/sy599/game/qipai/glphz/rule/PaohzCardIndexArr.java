package com.sy599.game.qipai.glphz.rule;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sy599.game.qipai.glphz.constant.PaohzCard;
import com.sy599.game.qipai.glphz.tool.PaohuziTool;
import com.sy599.game.util.JacksonUtil;

/**
 * 判断牌型
 * 
 * @author lc
 * 
 */
public class PaohzCardIndexArr {
	PaohuziIndex[] a = new PaohuziIndex[4];

	public void addPaohzCardIndex(int count, List<PaohzCard> majiangList, int val) {
		if (a[count] == null) {
			a[count] = new PaohuziIndex();
		}
		a[count].addPaohz(val, majiangList);
		a[count].addVal(val);
	}

	/**
	 * 根据牌的张数得到牌
	 * 
	 * @param size
	 *            张数
	 * @return
	 */
	public Map<Integer, List<PaohzCard>> getPaohzCardMap(int size) {
		Map<Integer, List<PaohzCard>> map = new HashMap<>();
		for (int i = 0; i < a.length; i++) {
			if (size <= i + 1) {
				PaohuziIndex majiangIndex = a[i];
				if (majiangIndex != null) {
					map.putAll(majiangIndex.getPaohzValMap());
				}
			}

		}
		return map;
	}


	public List<List<PaohzCard>> getDuizis() {
		List<List<PaohzCard>> list = new ArrayList<>();
		for (int i = 0; i < a.length; i++) {
			if (2 <= i + 1) {
				PaohuziIndex index = a[i];
				if (index != null) {
					for (Entry<Integer, List<PaohzCard>> entry : index.getPaohzValMap().entrySet()) {
						// if(PaohuziTool.c2710Listentry.getKey())
						int val = entry.getValue().get(0).getVal();
						if (!PaohuziTool.c2710List.contains(val)) {
							list.add(0, entry.getValue());

						} else {
							list.add(entry.getValue());
						}
					}
				}
			}

		}
		return list;
	}

	/**
	 * 牌的张数大于2的 (对子数)
	 * 
	 * @return
	 */
	public int getDuiziNum() {
		int num = 0;
		for (int i = 1; i < a.length; i++) {
			PaohuziIndex majiangIndex = a[i];
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
			PaohuziIndex majiangIndex = a[i];
			if (majiangIndex == null) {
				continue;
			}
			num += majiangIndex.getLength();

		}
		return num;
	}

	public List<PaohzCard> getKeziList() {
		List<PaohzCard> list = new ArrayList<>();
		for (int i = 2; i < a.length; i++) {
			PaohuziIndex majiangIndex = a[i];
			if (majiangIndex == null) {
				continue;
			}
			list.addAll(majiangIndex.getPaohzList());

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
	public PaohuziIndex getPaohzCardIndex(int index) {
		PaohuziIndex majiangIndex = a[index];
		if (majiangIndex == null) {
			// return new PaohzCardIndex();
		}
		return majiangIndex;
	}

	public PaohuziIndex[] getA() {
		return a;
	}

	public String tostr() {
		int i = 0;
		String str = "";
		for (PaohuziIndex majiang : a) {
			if (majiang == null) {
				continue;
			}
			str += i + "  " + JacksonUtil.writeValueAsString(majiang.getValList()) + " -->" + JacksonUtil.writeValueAsString(majiang.getPaohzValMap()) + "\n";
			i++;
		}
		return str;

	}
}
