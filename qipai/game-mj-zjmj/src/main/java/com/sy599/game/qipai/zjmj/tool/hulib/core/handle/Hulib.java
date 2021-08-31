package com.sy599.game.qipai.zjmj.tool.hulib.core.handle;

import java.util.*;

public class Hulib {
	static Hulib m_hulib = new Hulib();
	public static Hulib getInstance() {
		return m_hulib;
	}

	public boolean get_hu_info(int[] hand_cards, int curCard, int gui_num, boolean tingAny) {
		int[] hand_cards_tmp = new int[34];
		for (int i = 0 ; i < 34 ; ++i) {
			hand_cards_tmp[i] = hand_cards[i];
		}

		if (curCard < 34) {
			hand_cards_tmp[curCard]++;
		}
		
//		int gui_num = 0;
//		if (gui_index < 34) {
//			gui_num = hand_cards_tmp[gui_index];
//			hand_cards_tmp[gui_index] = 0;
//		}

		ProbabilityItemTable ptbl = new ProbabilityItemTable();
		if (!split(hand_cards_tmp, gui_num, ptbl)) {
			return false;
		}

		return check_probability(ptbl, gui_num, tingAny);
	}
	
	public List<Integer> get_ting_info(int[] hand_cards, int curCard, int gui_num) {
		
		List<Integer> tingList = split_ting(hand_cards, gui_num);
		return tingList;
	}
	
	public List<Integer> split_ting(int[] cards, int gui_num) {
		
		ProbabilityItemTable ptblTing = new ProbabilityItemTable();
		boolean ting0 = _split_ting(cards, gui_num, 0, 0, 8, true, ptblTing);
		boolean ting1 = _split_ting(cards, gui_num, 1, 9, 17, true, ptblTing);
		boolean ting2 = _split_ting(cards, gui_num, 2, 18, 26, true, ptblTing);
		boolean ting3 = _split_ting(cards, gui_num, 3, 27, 33, false, ptblTing);
		
		ProbabilityItemTable ptbl = new ProbabilityItemTable();
		boolean noTing0 = _split(cards, gui_num, 0, 0, 8, true, ptbl);
		boolean noTing1 = _split(cards, gui_num, 1, 9, 17, true, ptbl);
		boolean noTing2 = _split(cards, gui_num, 2, 18, 26, true, ptbl);
		boolean noTing3 = _split(cards, gui_num, 3, 27, 33, false, ptbl);

		List<Integer> tingList = new ArrayList<>();
		Set<Integer> tingSet = new HashSet<>();
		ProbabilityItemTable ptblTemp = null;
		if (ting0 && noTing1 && noTing2 && noTing3) {
			ptblTemp = new ProbabilityItemTable();
			_split_ting(cards, gui_num, 0, 0, 8, true, ptblTemp);
			_split(cards, gui_num, 1, 9, 17, true, ptblTemp);
			_split(cards, gui_num, 2, 18, 26, true, ptblTemp);
			_split(cards, gui_num, 3, 27, 33, false, ptblTemp);
			check_probability_ting(ptblTemp, gui_num, tingSet);
		}
		if (!tingSet.isEmpty()) {
			tingList.addAll(tingSet);
		}
		tingSet.clear();	
		if (ting1 && noTing0 && noTing2 && noTing3) {
			ptblTemp = new ProbabilityItemTable();
			_split(cards, gui_num, 0, 0, 8, true, ptblTemp);
			_split_ting(cards, gui_num, 1, 9, 17, true, ptblTemp);
			_split(cards, gui_num, 2, 18, 26, true, ptblTemp);
			_split(cards, gui_num, 3, 27, 33, false, ptblTemp);
			check_probability_ting(ptblTemp, gui_num, tingSet);
		}
		for (int index : tingSet) {
			tingList.add(index + 9);
		}
		tingSet.clear();
		if (ting2 && noTing0 && noTing1 && noTing3) {
			ptblTemp = new ProbabilityItemTable();
			_split(cards, gui_num, 0, 0, 8, true, ptblTemp);
			_split(cards, gui_num, 1, 9, 17, true, ptblTemp);
			_split_ting(cards, gui_num, 2, 18, 26, true, ptblTemp);
			_split(cards, gui_num, 3, 27, 33, false, ptblTemp);
			check_probability_ting(ptblTemp, gui_num, tingSet);
		}
		for (int index : tingSet) {
			tingList.add(index + 18);
		}
		tingSet.clear();		
		if (ting3 && noTing0 && noTing1 && noTing2) {
			ptblTemp = new ProbabilityItemTable();
			_split(cards, gui_num, 0, 0, 8, true, ptblTemp);
			_split(cards, gui_num, 1, 9, 17, true, ptblTemp);
			_split(cards, gui_num, 2, 18, 26, true, ptblTemp);
			_split_ting(cards, gui_num, 3, 27, 33, false, ptblTemp);
			check_probability_ting(ptblTemp, gui_num, tingSet);
		}
		for (int index : tingSet) {
			tingList.add(index + 27);
		}
		
		return tingList;
	}
	
	public boolean _split_ting(int[] cards, int gui_num, int color, int min, int max, boolean chi, ProbabilityItemTable ptbl) {
		int key = 0;
		int num = 0;
		for (int i = min ; i <= max ; ++i) {
			key = key * 10 + cards[i];
			num = num + cards[i];
		}

		if (num > 0) {
			return list_probability_ting(color, gui_num, num + 1, key, chi, ptbl);// 这里面的num记得+1, 不然匹配不上做将的逻辑
		}

		return false;
	}
	
	boolean list_probability_ting(int color, int gui_num, int num, int key, boolean chi, ProbabilityItemTable ptbl) {
		boolean find = false;
		int anum = ptbl.array_num;
		List<Integer> tingList = null;
		for (int i = 0 ; i <= gui_num ; ++i) {
			int yu = (num + i) % 3;
			if (yu == 1)
				continue;
			boolean eye = (yu == 2);
			tingList = TableMgr.getInstance().check_ting(key, i, eye, chi);
			if (find || tingList != null) {
				ProbabilityItem item = ptbl.m[anum][ptbl.m_num[anum]];
				ptbl.m_num[anum]++;

				item.eye = eye;
				item.gui_num = i;
				item.tingList = tingList;
				find = true;
			}
		}

		if (ptbl.m_num[anum] <= 0) {
			return false;
		}

		ptbl.array_num++;
		return true;
	}
	
	boolean split(int[] cards, int gui_num, ProbabilityItemTable ptbl) {
		if (!_split(cards, gui_num, 0, 0, 8, true, ptbl))
			return false;
		if (!_split(cards, gui_num, 1, 9, 17, true, ptbl))
			return false;
		if (!_split(cards, gui_num, 2, 18, 26, true, ptbl))
			return false;
		if (!_split(cards, gui_num, 3, 27, 33, false, ptbl))
			return false;

		return true;
	}

	boolean _split(int[] cards, int gui_num, int color, int min, int max, boolean chi, ProbabilityItemTable ptbl) {
		int key = 0;
		int num = 0;

		for (int i = min ; i <= max ; ++i) {
			key = key * 10 + cards[i];
			num = num + cards[i];
		}

		if (num > 0) {// 有麻将才去搞
			if (!list_probability(color, gui_num, num, key, chi, ptbl))
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * 
	 * @param color 花色
	 * @param gui_num 鬼数量
	 * @param num 该花色麻将数量和
	 * @param key 麻将字符表示法
	 * @param chi 是否可以吃,可吃代表万索筒
	 * @param ptbl 存鬼,做将数据
	 * @return
	 */
	boolean list_probability(int color, int gui_num, int num, int key, boolean chi, ProbabilityItemTable ptbl) {
		boolean find = false;
		int anum = ptbl.array_num;
		for (int i = 0 ; i <= gui_num ; ++i) {
			int yu = (num + i) % 3;
			if (yu == 1)
				continue;
			boolean eye = (yu == 2);
			if (find || TableMgr.getInstance().check(key, i, eye, chi)) {
				ProbabilityItem item = ptbl.m[anum][ptbl.m_num[anum]];
				ptbl.m_num[anum]++;

				item.eye = eye;
				item.gui_num = i;
				item.key = key;
				item.color = color;
				find = true;
			}
		}

		if (ptbl.m_num[anum] <= 0) {
			return false;
		}

		ptbl.array_num++;
		return true;
	}

	boolean check_probability(ProbabilityItemTable ptbl, int gui_num, boolean tingAny) {
		// 全是鬼牌
		if (ptbl.array_num == 0) {
			return gui_num >= 2 || tingAny;
		}

		// 只有一种花色的牌的鬼牌
		if (ptbl.array_num == 1)
			return true;

		// 尝试组合花色，能组合则胡
		for (int i = 0 ; i < ptbl.m_num[0] ; ++i) {
			ProbabilityItem item = ptbl.m[0][i];
			boolean eye = item.eye;
			
			if (eye && tingAny) {
				continue;
			}

			int gui = gui_num - item.gui_num;
			if (check_probability_sub(ptbl, eye, gui, 1, ptbl.array_num, tingAny)) {
				return true;
			}
		}
		return false;
	}
	
	void check_probability_ting(ProbabilityItemTable ptbl, int gui_num, Set<Integer> tingSet) {
		
		List<Integer> tingList = new ArrayList<>();
		if (ptbl.array_num == 1) {
			
			List<Integer> list = ptbl.m[0][0].tingList;
			if (list != null) {
				tingSet.clear();
				tingSet.addAll(list);
			}
			return;// true;
		}
		
		for (int i = 0 ; i < ptbl.m_num[0] ; ++i) {
			ProbabilityItem item = ptbl.m[0][i];
			boolean eye = item.eye;
			
			if (item.tingList != null) {
				tingList.clear();
				tingList.addAll(item.tingList);
			}
			
			int gui = gui_num - item.gui_num;
			if (check_probability_ting_sub(ptbl, eye, gui, 1, ptbl.array_num, tingList)) {
				tingSet.addAll(tingList);
				//return true;
			}
		}
		
		//return false;
	}
	
	boolean check_probability_ting_sub(ProbabilityItemTable ptbl, boolean eye, int gui_num, int level, int max_level, List<Integer> tingList) {
	    boolean result = false;
		for (int i = 0 ; i < ptbl.m_num[level] ; ++i) {
			ProbabilityItem item = ptbl.m[level][i];

			if (eye && item.eye)
				continue;

			if (gui_num < item.gui_num)
				continue;
			
			if (item.tingList != null) {
				tingList.clear();
				tingList.addAll(item.tingList);
			}

			if (level < max_level - 1) {
				if (check_probability_ting_sub(ptbl, eye || item.eye, gui_num - item.gui_num, level + 1, ptbl.array_num, tingList)) {
					result = true;
				}
				continue;
			}

			if (!eye && !item.eye && item.gui_num > gui_num - 2) {
				continue;
			}

            result = true;
		}
		
		return result;
	}

	boolean check_probability_sub(ProbabilityItemTable ptbl, boolean eye, int gui_num, int level, int max_level, boolean tingAny) {
		for (int i = 0 ; i < ptbl.m_num[level] ; ++i) {
			ProbabilityItem item = ptbl.m[level][i];
			
			if (item.eye && tingAny) {
				continue;
			}

			if (eye && item.eye)
				continue;

			if (gui_num < item.gui_num)
				continue;

			if (level < max_level - 1) {
				if (check_probability_sub(ptbl, eye || item.eye, gui_num - item.gui_num, level + 1, ptbl.array_num, tingAny)) {
					return true;
				}
				continue;
			}

			if (!tingAny && !eye && !item.eye && item.gui_num > gui_num - 2) {
				continue;
			}
			
			return true;
		}

		return false;
	}

	boolean check_7dui(int[] cards) {
		int c = 0;
		for (int i = 0 ; i < 34 ; ++i) {
			if (cards[i] % 2 != 0)
				return false;
			c += cards[i];
		}

		if (c != 34)
			return false;

		return true;
	}
	
	boolean check_probability_group_sub(ProbabilityItemTable ptbl, boolean eye, int gui_num, int level, int max_level, List<ProbabilityItem> tempList) {
		for (int i = 0 ; i < ptbl.m_num[level] ; ++i) {
			ProbabilityItem item = ptbl.m[level][i];

			if (eye && item.eye)
				continue;

			if (gui_num < item.gui_num)
				continue;
			
			if (level < max_level - 1) {
				if (check_probability_group_sub(ptbl, eye || item.eye, gui_num - item.gui_num, level + 1, ptbl.array_num, tempList)) {
					tempList.add(item);
					return true;
				}
				continue;
			}

			if (!eye && !item.eye && item.gui_num > gui_num - 2) {
				continue;
			}
			
			tempList.add(item);
			return true;
		}
		
		return false;
	}
	
	
	List<List<ProbabilityItem>> getHuPaiGroup(ProbabilityItemTable ptbl, int gui_num) {
		List<List<ProbabilityItem>> lists = new ArrayList<>();
		
		if (ptbl.array_num == 0) {
			return lists;
		}
		
		if (ptbl.array_num == 1) {
			lists.add(new ArrayList<>(Arrays.asList(ptbl.m[0][0])));
			return lists;
		}
		
		List<ProbabilityItem> tempList = null;
		for (int i = 0 ; i < ptbl.m_num[0] ; ++i) {
			ProbabilityItem item = ptbl.m[0][i];
			boolean eye = item.eye;
			
			tempList = new ArrayList<>();
			
			int gui = gui_num - item.gui_num;
			if (check_probability_group_sub(ptbl, eye, gui, 1, ptbl.array_num, tempList)) {
				tempList.add(item);
				lists.add(tempList);
				
			}
		}
		
		return lists;
	}

	public int getFullPaiXing(int[] cards, int gui_num, Map<Integer, Integer> taiMap) {
		ProbabilityItemTable ptbl = new ProbabilityItemTable();
		if (!split(cards, gui_num, ptbl)) {
			return 0;
		}
		
		boolean hu = true;
		int temp_gui = gui_num;
		int minus_gui = 0;
		while (temp_gui >= 3) {
			temp_gui -= 3;
			hu = Hulib.getInstance().get_hu_info(cards, 34, temp_gui, false);
			if (!hu) {
				break;
			}

			minus_gui += 3;
		}

		List<List<ProbabilityItem>> lists = getHuPaiGroup(ptbl, gui_num - minus_gui);
		if (lists.size() > 1) {
			boolean allNotEye = true;
			for (ProbabilityItem temp : lists.get(0)) {
				if (temp.eye) {
					allNotEye = false;
					break;
				}
			}
			
			if (allNotEye) {
				lists = new ArrayList<>(Arrays.asList(lists.get(0)));
			}
		}
		
		List<ProbabilityItem> ziList = new ArrayList<>();
		for (List<ProbabilityItem> list : lists) {
			for (ProbabilityItem item : list) {
				if (item.color == 3) {
					ziList.add(item);
				}
			}
		}
		
		
		Map<Integer, Integer> cntMap = new HashMap<>();
		Map<Integer, Integer> temp = new HashMap<>();

		
		Integer maxCount = null;
		int count = 0;
		for (ProbabilityItem item : ziList) {
			count = getDaPaiCount(item, temp);
			if (maxCount == null || count > maxCount) {
				maxCount = count;
				cntMap.clear();
				cntMap.putAll(temp);
			}
		}

		if (cntMap != null) {
			taiMap.putAll(cntMap);
		}
		
//		System.out.println("-----------------胡牌---------------字牌台数:" + count);
		return minus_gui;
	}
	
	public int getDaPaiCount(ProbabilityItem item, Map<Integer, Integer> taiMap) {
		
		taiMap.clear();
		int key = item.key;
		boolean eye = item.eye;
		String keyStr = String.valueOf(key);
		Map<Integer, Boolean> zfbMap = new HashMap<>();
		
		int count = 0;
		int length = keyStr.length();
		if (eye) {
			int two = 0;
			int one = 0;
			boolean oneFlag = false;
			boolean zeroFlag = false;
			
			for (int i=0; i<length; i++) {
				if (keyStr.charAt(i) > 48) {
					if (7 - length + i == 0) {
						two += 1;
					} else if (7 - length + i > 3) {
						one += 1;
						if (keyStr.charAt(i) < 51) {
							oneFlag = true;
							zfbMap.put(7 - length + i, true);
						} else {
							zfbMap.put(7 - length + i, false);
						}
					} else {
						if (keyStr.charAt(i) < 51) {
							zeroFlag = true;
						}
					}
				}
			}
			
			if (zeroFlag) {
				count = two * 2 + one;
				
				if (two > 0) {
					taiMap.put(1, 2);
				}
				if (one > 0) {
					//taiMap.put(2, one);
					for (int temp : zfbMap.keySet()) {
						if (temp == 4) {
							taiMap.put(15, 1);
						} else if (temp == 5) {
							taiMap.put(16, 1);
						} else if (temp == 6) {
							taiMap.put(17, 1);
						}
					}
					
				}
			} else if (oneFlag) {
				if (two + one - 1 > 0) {
					count = two * 2 + one - 1;
					
					if (two > 0) {
						taiMap.put(1, 2);
					}
					if (one - 1 > 0) {
						//taiMap.put(2, one - 1);
						
						boolean flag = false;
						for (Map.Entry<Integer, Boolean> entry : zfbMap.entrySet()) {
							if (entry.getValue() && !flag) {
								flag = true;
								continue;
							}
							
							if (entry.getKey() == 4) {
								taiMap.put(15, 1);
							} else if (entry.getKey() == 5) {
								taiMap.put(16, 1);
							} else if (entry.getKey() == 6) {
								taiMap.put(17, 1);
							}
						}
						
					}
				} else {
//					count = -1;
//					taiMap.put(3, -1);
				}
			} else {
//				count = -1;
//				taiMap.put(3, -1);
			}
			
		} else {
			int two = 0;
			int one = 0;
			for (int i=0; i<length; i++) {
				if (keyStr.charAt(i) > 48) {
					if (7 - length + i == 0) {
						count += 2;
						two ++;
					} else if (7 - length + i > 3) {
						count += 1;
						one ++;
						zfbMap.put(7 - length + i, false);
					}
				}
			}
			
			if (two > 0) {
				taiMap.put(1, 2);
			}
			if (one > 0) {
				//taiMap.put(2, one);
				for (int temp : zfbMap.keySet()) {
					if (temp == 4) {
						taiMap.put(15, 1);
					} else if (temp == 5) {
						taiMap.put(16, 1);
					} else if (temp == 6) {
						taiMap.put(17, 1);
					}
				}
			}
		}
		
		return count;
	}
}

class ProbabilityItem {
	public boolean eye;
	public int gui_num;
	public List<Integer> tingList;
	public int key;
	public int color;

	public ProbabilityItem() {
		eye = false;
		gui_num = 0;
		tingList = null;
		key = -1;
		color = -1;
	}
};

class ProbabilityItemTable {
	ProbabilityItem[][] m = new ProbabilityItem[4][6];

	public int array_num;
	public int[] m_num;

	public ProbabilityItemTable() {
		for (int i = 0 ; i < m.length ; i++) {
			for (int j = 0 ; j < m[i].length ; j++) {
				m[i][j] = new ProbabilityItem();
			}
		}

		array_num = 0;
		m_num = new int[] {
				0, 0, 0, 0
		};

	}
}