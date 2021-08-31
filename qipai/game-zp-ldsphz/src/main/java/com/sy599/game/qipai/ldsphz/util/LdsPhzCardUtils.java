package com.sy599.game.qipai.ldsphz.util;

import java.security.SecureRandom;
import java.util.*;

import com.sy599.game.GameServerConfig;

public final class LdsPhzCardUtils {

	public static final int WANGPAI_ID = 80;
	public static final int WANGPAI_VAL = 201;

	/**
	 * 获取一副牌（已经洗牌）
	 *
	 * @return
	 */
	public static List<Integer> loadCards() {
		return loadCards(0);
	}

	/**
	 * 获取一副牌（已经洗牌）
	 *
	 * @param wangCount
	 *            王牌数量
	 * @return
	 */
	public static List<Integer> loadCards(int wangCount) {
		List<Integer> list = new ArrayList<>(80 + wangCount);
		for (int i = 1; i <= 80; i++) {
			list.add(i);
		}
		for (int i = 1; i <= wangCount; i++) {
			list.add(WANGPAI_ID + i);
		}
		Collections.shuffle(list, new SecureRandom());
		return list;
	}

	/**
	 * 分牌,庄家15张，闲家14张，
	 *
	 * @param allCards
	 * @param count
	 * @param cardsCount
	 *            发牌数量
	 * @return 下标为count代表桌子上的牌
	 */
	public static List<List<Integer>> loadCards(List<Integer> allCards, int count, int cardsCount,int playerCount) {
		List<Integer> cards=new ArrayList<>(allCards);
		List<List<Integer>> result = new ArrayList<>();

		int size = cards.size();
		if (count <= 4 && size >= 80) {
			int index = 0;
			for (int i = 0; i <= count; i++) {
				List<Integer> list;
				if (i == 0) {
					list = new ArrayList<>(cardsCount + 1);
					for (int m = 0; m <= cardsCount; m++) {
						list.add(cards.get(index));
						index++;
					}
				} else if (i == count) {
					list = new ArrayList<>(size - index + 1);
					while (index < size) {
						list.add(cards.get(index));
						index++;
					}
				} else {
					list = new ArrayList<>(cardsCount);
					for (int m = 0; m < cardsCount; m++) {
						list.add(cards.get(index));
						index++;
					}
				}
				result.add(list);
			}
		}
		if(checkShuangLong(result,playerCount)){
			return result;
		}else {
			Collections.shuffle(allCards);
			return loadCards(allCards,count,cardsCount,playerCount);
		}

	}

	public static synchronized boolean checkShuangLong(List<List<Integer>> list,int playerCount){
		for (int i = 0; i < playerCount; i++) {
			List<Integer> ids = list.get(i);
			int []nums=new int[21];
			for (Integer id:ids) {
				int k;
				int val = GuihzCard.getPaohzCard(id).getVal();
				if(val<100){
					k=val-1;
				}else if(val<=110){
					k=val-91;
				}else {
					k=20;
				}
				nums[k]++;
			}
			int count=0;
			for (int j = 0; j < nums.length; j++) {
				if(nums[j]==4){
					count++;
				}
			}
			if (count>=2)
				return false;
		}
		return true;
	}

	public static int loadCardVal(int id) {
		if (id > WANGPAI_ID) {
			return WANGPAI_VAL;
		} else if (id <= 0) {
			return 0;
		} else {
			int val = id % 10;
			if (val == 0) {
				val = 10;
			}
			if (bigCard(id)) {
				val += 100;
			}
			return val;
		}
	}

	public static int getIdByVaule(int val) {
		GuihzCard card = GuihzCard.getGuihzCard(val);
		if (card == null) {
			return 0;
		}
		return card.getId();
	}

	/**
	 * 只保留胡操作
	 *
	 * @param actionList
	 * @return
	 */
	public static List<Integer> keepHu(List<Integer> actionList) {
		if (actionList == null || actionList.size() < 1) {
			return Collections.EMPTY_LIST;
		}
		List<Integer> res = new ArrayList<>();
		for (int i = 0; i < actionList.size(); i++) {
			if (i == 0) {
				res.add(actionList.get(i));
			} else {
				res.add(0);
			}
		}
		return res;
	}

	public static boolean smallCard(int id) {
		return id >= 1 && id <= 40;
	}

	public static boolean smallCardByVal(int val) {
		return val >= 1 && val <= 10;
	}

	public static boolean bigCard(int id) {
		return id >= 41 && id <= 80;
	}

	public static boolean bigCardByVal(int val) {
		return val >= 101 && val <= 110;
	}

	public static boolean commonCardByVal(int val) {
		return (val >= 1 && val <= 10) || (val >= 101 && val <= 110);
	}

	public static boolean commonCard(int id) {
		return id >= 1 && id <= 80;
	}

	public static boolean wangCard(int id) {
		return id > WANGPAI_ID;
	}

	public static boolean wangCardByVal(int val) {
		return val >= WANGPAI_VAL;
	}

	public static boolean validCard(int id) {
		return commonCard(id) || wangCard(id);
	}

	public static boolean sameCard(int id1, int id2) {
		return loadCardVal(id1) == loadCardVal(id2);
	}

	public static boolean sameType(int id1, int id2) {
		return smallCard(id1) && smallCard(id2) || bigCard(id1) && bigCard(id2) || wangCard(id1) && wangCard(id2);
	}

	public static void sort(List<Integer> list) {
		Collections.sort(list, new Comparator<Integer>() {
			@Override
			public int compare(Integer id1, Integer id2) {
				int val1 = loadCardVal(id1);
				int val2 = loadCardVal(id2);
				// if (bigCard(id1)) {
				// val1 -= 100;
				// }
				// if (bigCard(id2)) {
				// val2 -= 100;
				// }
				int val = val1 - val2;
				return val != 0 ? val : (id1 - id2);
			}
		});
	}

	public static boolean canTi(LdsPhzHandCards handCards, int id) {
		int val = loadCardVal(id);
		return handCards.KAN.containsKey(val);
	}

	public static boolean canPao(LdsPhzHandCards handCards, int id) {
		int val = loadCardVal(id);
		return handCards.KAN.containsKey(val) || handCards.WEI.containsKey(val) || handCards.PENG.containsKey(val);
	}

	public static boolean canWei(List<Integer> srcList, int id) {
		int count = 0;
		for (Integer temp : srcList) {
			if (sameCard(id, temp)) {
				count++;
			}
		}
		return count == 2;
	}

	public static boolean canPeng(List<Integer> srcList, int id) {
		int count = 0;
		for (Integer temp : srcList) {
			if (temp.intValue() == id) {
				return false;
			} else if (sameCard(id, temp)) {
				count++;
			}
		}
		return count == 2;
	}

	public static List<Integer> loadSameValCards(List<Integer> list, int id) {
		if (list == null || list.isEmpty()) {
			return Collections.emptyList();
		} else {
			List<Integer> result = new ArrayList<>();
			for (Integer temp : list) {
				if (sameCard(id, temp)) {
					result.add(temp);
				}
			}
			return result;
		}
	}

	public static List<List<Integer>> canJiao(List<Integer> srcList, int id) {
		int val = loadCardVal(id);
		List<List<Integer>> result = new ArrayList<>();
		// 绞牌
		List<Integer> similarVals = loadIdsByVal(srcList, val <= 10 ? val + 100 : val - 100);
		if (similarVals.size() == 0) {
			return result;
		}
		if (similarVals.size() == 2) {
			result.add(asList(similarVals.get(0), similarVals.get(1)));
		}
		int id1 = loadIdByVal(srcList, val);
		if (id1 > 0) {
			result.add(asList(similarVals.get(0), id1));
		}

		return result;
	}

	public static boolean hasSameValCard(List<Integer> list) {
		for (Integer integer : list) {
			List<Integer> temp = loadIdsByVal(list, loadCardVal(integer));
			if (temp.size() >= 2) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasSameValCardIgnoreCase(List<Integer> list) {
		for (Integer integer : list) {
			int val = loadCardVal(integer);
			List<Integer> temp = loadIdsByVal(list, val > 100 ? (val - 100) : (val + 100));
			if (temp.size() >= 1) {
				return true;
			}
		}
		return false;
	}

	public static LdsPhzHuXiEnums loadHuXiEnum(List<Integer> list) {
		sort(list);
		if (list.size() == 3) {
			int val1 = LdsPhzCardUtils.loadCardVal(list.get(0));
			int val2 = LdsPhzCardUtils.loadCardVal(list.get(1));
			int val3 = LdsPhzCardUtils.loadCardVal(list.get(2));
			if (val3 >= WANGPAI_VAL) {
				if (val2 >= WANGPAI_VAL) {
					return LdsPhzHuXiEnums.WEI;
				} else {
					if (val1 == val2) {
						return LdsPhzHuXiEnums.WEI;
					} else {
						if (sameType(list.get(0), list.get(1))) {
							if (val1 == 1 || val1 == 101) {
								return LdsPhzHuXiEnums.CHI123;
							} else if (redCard(list.get(0)) && redCard(list.get(1))) {
								return LdsPhzHuXiEnums.CHI2710;
							} else if ((val1 == 2 && val2 == 3) || (val1 == 102 && val2 == 103)) {
								return LdsPhzHuXiEnums.CHI123;
							} else {
								return LdsPhzHuXiEnums.CHI;
							}
						} else {
							return LdsPhzHuXiEnums.JIAO;
						}
					}
				}
			} else if (val1 == val2 && val3 == val1) {
				return LdsPhzHuXiEnums.WEI;
			} else if (val1 == 2 && val2 == 7 && val3 == 10) {
				return LdsPhzHuXiEnums.CHI2710;
			} else if (val1 == 102 && val2 == 107 && val3 == 110) {
				return LdsPhzHuXiEnums.CHI2710;
			} else if (val1 == 1 && val2 == 2 && val3 == 3) {
				return LdsPhzHuXiEnums.CHI123;
			} else if (val1 == 101 && val2 == 102 && val3 == 103) {
				return LdsPhzHuXiEnums.CHI123;
			} else if (!sameType(list.get(0), list.get(1)) || !sameType(list.get(2), list.get(1))) {
				return LdsPhzHuXiEnums.JIAO;
			} else {
				return LdsPhzHuXiEnums.CHI;
			}
		} else if (list.size() == 4) {
			return LdsPhzHuXiEnums.TI;
		} else {
			return LdsPhzHuXiEnums.DUI;
		}
	}

	public static List<LdsPhzCardMessage> loadCardMessageList(LdsPhzHandCards handCards, List<LdsPhzCardMessage> list) {
		if (list == null) {
			list = new ArrayList<>(8);
		}
		if (handCards.TI.size() > 0) {
			for (Map.Entry<Integer, List<Integer>> kv : handCards.TI.entrySet()) {
				list.add(new LdsPhzCardMessage(LdsPhzHuXiEnums.TI, kv.getValue()));
			}
		}

		if (handCards.PAO.size() > 0) {
			for (Map.Entry<Integer, List<Integer>> kv : handCards.PAO.entrySet()) {
				list.add(new LdsPhzCardMessage(LdsPhzHuXiEnums.PAO, kv.getValue()));
			}
		}

		if (handCards.KAN.size() > 0) {
			for (Map.Entry<Integer, List<Integer>> kv : handCards.KAN.entrySet()) {
				list.add(new LdsPhzCardMessage(LdsPhzHuXiEnums.KAN, kv.getValue()));
			}
		}

		if (handCards.WEI.size() > 0) {
			for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI.entrySet()) {
				list.add(new LdsPhzCardMessage(LdsPhzHuXiEnums.WEI, kv.getValue()));
			}
		}

		if (handCards.WEI_CHOU.size() > 0) {
			for (Map.Entry<Integer, List<Integer>> kv : handCards.WEI_CHOU.entrySet()) {
				list.add(new LdsPhzCardMessage(LdsPhzHuXiEnums.WEI_CHOU, kv.getValue()));
			}
		}

		if (handCards.PENG.size() > 0) {
			for (Map.Entry<Integer, List<Integer>> kv : handCards.PENG.entrySet()) {
				list.add(new LdsPhzCardMessage(LdsPhzHuXiEnums.PENG, kv.getValue()));
			}
		}

		if (handCards.CHI_JIAO.size() > 0) {
			for (List<Integer> temp : handCards.CHI_JIAO) {
				list.add(new LdsPhzCardMessage(LdsPhzHuXiEnums.JIAO, temp));
			}
		}

		if (handCards.CHI_COMMON.size() > 0) {
			for (List<Integer> temp : handCards.CHI_COMMON) {
				Map<Integer, Integer> map = new HashMap<>();
				for (Integer tmp : temp) {
					map.put(LdsPhzCardUtils.loadCardVal(tmp), 1);
				}
				if (map.containsKey(1)) {
					list.add(new LdsPhzCardMessage(LdsPhzHuXiEnums.CHI123, temp));
				} else if (map.containsKey(101)) {
					list.add(new LdsPhzCardMessage(LdsPhzHuXiEnums.CHI123, temp));
				} else if (map.containsKey(2) && map.containsKey(7) && map.containsKey(10)) {
					list.add(new LdsPhzCardMessage(LdsPhzHuXiEnums.CHI2710, temp));
				} else if (map.containsKey(102) && map.containsKey(107) && map.containsKey(110)) {
					list.add(new LdsPhzCardMessage(LdsPhzHuXiEnums.CHI2710, temp));
				} else {
					list.add(new LdsPhzCardMessage(LdsPhzHuXiEnums.CHI, temp));
				}
			}
		}
		return list;
	}

	public static List<LdsPhzChiCard> canChi(List<Integer> srcList, int id) {
		List<LdsPhzChiCard> result = new ArrayList<>();
		int val = loadCardVal(id);
		sort(srcList);
		// 绞牌
		List<Integer> similarVals = loadIdsByVal(srcList, val <= 10 ? val + 100 : val - 100);

		List<Integer> sameVals = loadIdsByVal(srcList, val);
		switch (sameVals.size()) {
		case 0: {
			if (similarVals.size() == 2) {
				result.add(new LdsPhzChiCard().buildChi(similarVals));
			}

			// 顺子
			int id1 = loadIdByVal(srcList, val - 2);
			int id2 = loadIdByVal(srcList, val - 1);
			if (id1 > 0 && id2 > 0) {
				result.add(new LdsPhzChiCard().buildChi(asList(id1, id2)));
			}
			id1 = id2;
			id2 = loadIdByVal(srcList, val + 1);
			if (id1 > 0 && id2 > 0) {
				result.add(new LdsPhzChiCard().buildChi(asList(id1, id2)));
			}
			id1 = id2;
			id2 = loadIdByVal(srcList, val + 2);
			if (id1 > 0 && id2 > 0) {
				result.add(new LdsPhzChiCard().buildChi(asList(id1, id2)));
			}

			// 2 7 10
			switch (val) {
			case 2:
				id1 = loadIdByVal(srcList, 7);
				id2 = loadIdByVal(srcList, 10);
				if (id1 > 0 && id2 > 0) {
					result.add(new LdsPhzChiCard().buildChi(asList(id1, id2)));
				}
				break;
			case 7:
				id1 = loadIdByVal(srcList, 2);
				id2 = loadIdByVal(srcList, 10);
				if (id1 > 0 && id2 > 0) {
					result.add(new LdsPhzChiCard().buildChi(asList(id1, id2)));
				}
				break;
			case 10:
				id1 = loadIdByVal(srcList, 2);
				id2 = loadIdByVal(srcList, 7);
				if (id1 > 0 && id2 > 0) {
					result.add(new LdsPhzChiCard().buildChi(asList(id1, id2)));
				}
				break;
			case 102:
				id1 = loadIdByVal(srcList, 107);
				id2 = loadIdByVal(srcList, 110);
				if (id1 > 0 && id2 > 0) {
					result.add(new LdsPhzChiCard().buildChi(asList(id1, id2)));
				}
				break;
			case 107:
				id1 = loadIdByVal(srcList, 102);
				id2 = loadIdByVal(srcList, 110);
				if (id1 > 0 && id2 > 0) {
					result.add(new LdsPhzChiCard().buildChi(asList(id1, id2)));
				}
				break;
			case 110:
				id1 = loadIdByVal(srcList, 102);
				id2 = loadIdByVal(srcList, 107);
				if (id1 > 0 && id2 > 0) {
					result.add(new LdsPhzChiCard().buildChi(asList(id1, id2)));
				}
				break;
			}

		}
			break;
		case 1:
			List<List<Integer>> yjhList = loadYijuhua(srcList, val, 0, true, true);
			if (yjhList.size() > 0) {
				for (List<Integer> temp : yjhList) {
					List<Integer> list0 = new ArrayList<>(srcList);
					list0.removeAll(temp);
					List<LdsPhzChiCard> list1 = canChi(list0, id);
					for (LdsPhzChiCard chiCard : list1) {
						result.add(chiCard.buildBi(temp));
					}
				}
			}

			if (similarVals.size() > 0) {
				result.add(new LdsPhzChiCard().buildChi(asList(sameVals.get(0), similarVals.get(0))));
			}

			break;
		case 2:
			List<List<Integer>> yjhList0 = loadYijuhua(srcList, val, 0, true, true);
			if (yjhList0.size() > 0) {
				for (List<Integer> temp : yjhList0) {
					List<Integer> list0 = new ArrayList<>(srcList);
					list0.removeAll(temp);
					List<LdsPhzChiCard> list1 = canChi(list0, id);
					for (LdsPhzChiCard chiCard : list1) {
						result.add(chiCard.buildBi(temp));
					}
				}
			}
			break;
		}

		// 滤重
		if (result.size() > 0 && sameVals.size() > 0) {
			List<LdsPhzChiCard> result0 = new ArrayList<>();
			for (LdsPhzChiCard chiCard : result) {
				LdsPhzChiCard pre = null;
				List<Integer> chi = chiCard.getChi();
				sort(chi);
				int val1 = loadCardVal(chi.get(0));
				int val2 = loadCardVal(chi.get(1));
				for (LdsPhzChiCard chiCard1 : result0) {
					List<Integer> chi1 = chiCard1.getChi();
					if (loadCardVal(chi1.get(0)) == val1 && loadCardVal(chi1.get(1)) == val2) {
						pre = chiCard1;
						break;
					}
				}
				if (pre == null) {
					result0.add(chiCard);
				} else {
					if (pre.getBiList().size() == 0) {
						pre.setBiList(chiCard.getBiList());
					} else if (chiCard.getBiList().size() > 0) {
						List<List<Integer>> news = new ArrayList<>();
						for (List<Integer> tmp : pre.getBiList()) {
							sort(tmp);
							int v1 = loadCardVal(tmp.get(0));
							int v2 = loadCardVal(tmp.get(1));
							int v3 = loadCardVal(tmp.get(2));
							for (List<Integer> tmp0 : chiCard.getBiList()) {
								sort(tmp0);
								int vv1 = loadCardVal(tmp0.get(0));
								int vv2 = loadCardVal(tmp0.get(1));
								int vv3 = loadCardVal(tmp0.get(2));

								if (vv1 == v1 && vv2 == v2 && vv3 == v3) {
								} else {
									List<Integer> pre1 = null;
									for (List<Integer> tmp1 : news) {
										int value1 = loadCardVal(tmp1.get(0));
										int value2 = loadCardVal(tmp1.get(1));
										int value3 = loadCardVal(tmp1.get(2));
										if (value1 == vv1 && value2 == vv2 && value3 == vv3) {
											pre1 = tmp1;
											break;
										}
									}
									if (pre1 == null) {
										news.add(tmp0);
									}
								}
							}
						}
						if (news.size() > 0) {
							pre.getBiList().addAll(news);
						}
					}
				}
			}

			result = result0;
		}

		return result;
	}

	public static int countDouble(List<Integer> srcList) {
		Map<Integer, Integer> map = loadCardCount(srcList);
		int count = 0;
		for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
			if (kv.getValue().intValue() == 2) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 一句话组合（最多支持一个王）
	 * 
	 * @param srcList
	 * @param containVal
	 * @param wangId
	 * @param canJiao
	 * @param sort
	 * @return
	 */
	public static List<List<Integer>> loadYijuhua(List<Integer> srcList, int containVal, int wangId, boolean canJiao,
			boolean sort) {
		return loadYijuhua(srcList, containVal, wangId, canJiao, sort, true);
	}

	/**
	 * 一句话组合（最多支持一个王）
	 * 
	 * @param srcList
	 * @param containVal
	 * @param wangId
	 * @param canJiao
	 * @param sort
	 * @param checkMultiDui
	 * @return
	 */
	public static List<List<Integer>> loadYijuhua(List<Integer> srcList, int containVal, int wangId, boolean canJiao,
			boolean sort, boolean checkMultiDui) {
		List<List<Integer>> result = new ArrayList<>(8);
		if (sort)
			sort(srcList);

		List<Integer> sameVals = loadIdsByVal(srcList, containVal);

		if (sameVals.size() == 0) {
			return result;
		}

		if (canJiao) {
			// 绞牌
			List<Integer> similarVals = loadIdsByVal(srcList, containVal <= 10 ? containVal + 100 : containVal - 100);

			if (sameVals.size() == 3) {
				result.add(asList(sameVals.get(0), sameVals.get(1), sameVals.get(2)));
				if (similarVals.size() == 1) {
					result.add(asList(sameVals.get(0), sameVals.get(1), similarVals.get(0)));
				} else if (similarVals.size() == 2) {
					result.add(asList(sameVals.get(0), sameVals.get(1), similarVals.get(0)));
					result.add(asList(similarVals.get(0), similarVals.get(1), sameVals.get(0)));
				}
			} else if (sameVals.size() == 1 && similarVals.size() == 2) {
				result.add(asList(similarVals.get(0), similarVals.get(1), sameVals.get(0)));
			} else if (sameVals.size() == 2 && similarVals.size() == 1) {
				result.add(asList(similarVals.get(0), sameVals.get(0), sameVals.get(1)));
			} else if (sameVals.size() == 2 && similarVals.size() == 2) {
				result.add(asList(similarVals.get(0), similarVals.get(1), sameVals.get(0)));
				result.add(asList(similarVals.get(0), sameVals.get(0), sameVals.get(1)));
			} else if (wangId > WANGPAI_ID) {
				if (sameVals.size() == 2) {
					if (checkMultiDui) {
						if (countDouble(srcList) > 1)
							result.add(asList(sameVals.get(0), sameVals.get(1), wangId));
					} else {
						result.add(asList(sameVals.get(0), sameVals.get(1), wangId));
					}
				} else {
					if (similarVals.size() > 0) {
						result.add(asList(sameVals.get(0), similarVals.get(0), wangId));
					}
				}
			}
		} else if (sameVals.size() == 3) {
			result.add(asList(sameVals.get(0), sameVals.get(1), sameVals.get(2)));
		}

		int id = sameVals.get(0);
		int val = loadCardVal(sameVals.get(0));

		// 顺子
		int id1 = loadIdByVal(srcList, val - 2);
		int id2 = loadIdByVal(srcList, val - 1);

		boolean add = true;

		if (id1 > 0 && id2 > 0) {
			result.add(asList(id1, id2, id));
		} else if (wangId > WANGPAI_ID) {
			if (id1 > 0) {
				result.add(asList(id1, wangId, id));
			} else if (id2 > 0) {
				result.add(asList(wangId, id2, id));
				add = false;
			}
		}
		id1 = id2;
		id2 = loadIdByVal(srcList, val + 1);
		if (id1 > 0 && id2 > 0) {
			result.add(asList(id1, id, id2));
		} else if (wangId > WANGPAI_ID) {
			if (id1 > 0) {
				if (add)
					result.add(asList(id1, id, wangId));
			} else if (id2 > 0) {
				result.add(asList(wangId, id, id2));
				add = false;
			}
		}
		id1 = id2;
		id2 = loadIdByVal(srcList, val + 2);
		if (id1 > 0 && id2 > 0) {
			result.add(asList(id, id1, id2));
		} else if (wangId > WANGPAI_ID) {
			if (id1 > 0) {
				if (add)
					result.add(asList(id, id1, wangId));
			} else if (id2 > 0) {
				result.add(asList(id, wangId, id2));
			}
		}

		// 2 7 10
		switch (val) {
		case 2:
			id1 = loadIdByVal(srcList, 7);
			id2 = loadIdByVal(srcList, 10);
			if (id1 > 0 && id2 > 0) {
				result.add(asList(id, id1, id2));
			} else if (wangId > WANGPAI_ID) {
				if (id1 > 0) {
					result.add(asList(id, id1, wangId));
				} else if (id2 > 0) {
					result.add(asList(id, wangId, id2));
				}
			}
			break;
		case 7:
			id1 = loadIdByVal(srcList, 2);
			id2 = loadIdByVal(srcList, 10);
			if (id1 > 0 && id2 > 0) {
				result.add(asList(id1, id, id2));
			} else if (wangId > WANGPAI_ID) {
				if (id1 > 0) {
					result.add(asList(id1, id, wangId));
				} else if (id2 > 0) {
					result.add(asList(wangId, id, id2));
				}
			}
			break;
		case 10:
			id1 = loadIdByVal(srcList, 2);
			id2 = loadIdByVal(srcList, 7);
			if (id1 > 0 && id2 > 0) {
				result.add(asList(id1, id2, id));
			} else if (wangId > WANGPAI_ID) {
				if (id1 > 0) {
					result.add(asList(id1, wangId, id));
				} else if (id2 > 0) {
					result.add(asList(wangId, id2, id));
				}
			}
			break;
		case 102:
			id1 = loadIdByVal(srcList, 107);
			id2 = loadIdByVal(srcList, 110);
			if (id1 > 0 && id2 > 0) {
				result.add(asList(id, id1, id2));
			} else if (wangId > WANGPAI_ID) {
				if (id1 > 0) {
					result.add(asList(id, id1, wangId));
				} else if (id2 > 0) {
					result.add(asList(id, wangId, id2));
				}
			}
			break;
		case 107:
			id1 = loadIdByVal(srcList, 102);
			id2 = loadIdByVal(srcList, 110);
			if (id1 > 0 && id2 > 0) {
				result.add(asList(id1, id, id2));
			} else if (wangId > WANGPAI_ID) {
				if (id1 > 0) {
					result.add(asList(id1, id, wangId));
				} else if (id2 > 0) {
					result.add(asList(wangId, id, id2));
				}
			}
			break;
		case 110:
			id1 = loadIdByVal(srcList, 102);
			id2 = loadIdByVal(srcList, 107);
			if (id1 > 0 && id2 > 0) {
				result.add(asList(id1, id2, id));
			} else if (wangId > WANGPAI_ID) {
				if (id1 > 0) {
					result.add(asList(id1, wangId, id));
				} else if (id2 > 0) {
					result.add(asList(wangId, id2, id));
				}
			}
			break;
		}

		return result;
	}

	/**
	 * 相同牌的个数
	 *
	 * @param list
	 * @return
	 */
	public static Map<Integer, Integer> loadCardCount(List<Integer> list) {
		Map<Integer, Integer> map = new HashMap<>();
		for (Integer temp : list) {
			int val = loadCardVal(temp);
			Integer count = map.get(val);
			if (count == null) {
				count = 1;
			} else {
				count += 1;
			}
			map.put(val, count);
		}
		return map;
	}

	public static int loadIdByVal(List<Integer> list, int val) {
		for (int i = 0, len = list.size(); i < len; i++) {
			int id = list.get(i);
			if (loadCardVal(id) == val) {
				return id;
			}
		}
		return -1;
	}

	public static int loadIdByVal(List<Integer> list, int val, boolean remove) {
		Iterator<Integer> iterator = list.iterator();
		while (iterator.hasNext()) {
			Integer id = iterator.next();
			if (loadCardVal(id) == val) {
				if (remove) {
					iterator.remove();
				}
				return id;
			}
		}
		return -1;
	}

	public static List<Integer> loadIdsByVal(List<Integer> list, int val) {
		List<Integer> result = new ArrayList<>(4);
		for (int i = 0, len = list.size(); i < len; i++) {
			int id = list.get(i);
			if (loadCardVal(id) == val) {
				result.add(id);
			}
		}
		return result;
	}

	public static boolean isAllWang(List<Integer> list) {
		boolean res = true;
		for (int i = 0, len = list.size(); i < len; i++) {
			int id = list.get(i);
			if (loadCardVal(id) != WANGPAI_VAL) {
				res = false;
				break;
			}
		}
		return res;
	}

	public static List<Integer> loadIdsByVal(List<Integer> list, int val, boolean remove) {
		List<Integer> result = new ArrayList<>(4);
		Iterator<Integer> iterator = list.iterator();
		while (iterator.hasNext()) {
			Integer id = iterator.next();
			if (loadCardVal(id) == val) {
				result.add(id);
				if (remove) {
					iterator.remove();
				}
			}
		}
		return result;
	}

	public static void removeByVal(List<Integer> list, int val, boolean all) {
		Iterator<Integer> iterator = list.iterator();
		while (iterator.hasNext()) {
			Integer id = iterator.next();
			if (loadCardVal(id) == val) {
				iterator.remove();
				if (!all) {
					return;
				}
			}
		}
	}

	public static int countHuxi0(List<Integer> list) {
		int total = 0;
		sort(list);
		Map<Integer, Integer> map = new HashMap<>();
		for (Integer tmp : list) {
			int val = LdsPhzCardUtils.loadCardVal(tmp);
			Integer count = map.get(val);
			map.put(val, count == null ? 1 : (count.intValue() + 1));
		}

		switch (list.size()) {
		case 3:
			Integer wangCount = map.get(201);
			if (wangCount == null) {
				int size = map.size();
				if (size == 3) {
					if (map.containsKey(1)) {
						total = (LdsPhzHuXiEnums.CHI123.getSmall());
					} else if (map.containsKey(101)) {
						total = (LdsPhzHuXiEnums.CHI123.getBig());
					} else if (map.containsKey(2) && map.containsKey(7) && map.containsKey(10)) {
						total = (LdsPhzHuXiEnums.CHI2710.getSmall());
					} else if (map.containsKey(102) && map.containsKey(107) && map.containsKey(110)) {
						total = (LdsPhzHuXiEnums.CHI2710.getBig());
					} else {
						total = (LdsPhzCardUtils.smallCard(list.get(0)) ? LdsPhzHuXiEnums.CHI.getSmall()
								: LdsPhzHuXiEnums.CHI.getBig());
					}
				} else if (size == 2) {
					total = (LdsPhzCardUtils.smallCard(list.get(1)) ? LdsPhzHuXiEnums.JIAO.getSmall()
							: LdsPhzHuXiEnums.JIAO.getBig());
				} else {
					total = (LdsPhzCardUtils.smallCard(list.get(0)) ? LdsPhzHuXiEnums.KAN.getSmall()
							: LdsPhzHuXiEnums.KAN.getBig());
				}
			} else {
				int val1 = loadCardVal(list.get(0));
				int val2 = loadCardVal(list.get(1));
				int val3 = loadCardVal(list.get(2));
				if (val1 == 201) {
					total = LdsPhzHuXiEnums.KAN.getBig();
				} else if (val2 == 201) {
					total = (LdsPhzCardUtils.smallCard(list.get(0)) ? LdsPhzHuXiEnums.KAN.getSmall()
							: LdsPhzHuXiEnums.KAN.getBig());
				} else if (val3 == 201) {
					if (val1 == val2) {
						total = (LdsPhzCardUtils.smallCard(list.get(0)) ? LdsPhzHuXiEnums.KAN.getSmall()
								: LdsPhzHuXiEnums.KAN.getBig());
					} else if (val2 - val1 == 1) {
						if (val1 == 1 || val1 == 101 || val1 == 2 || val1 == 102) {
							total = LdsPhzCardUtils.smallCard(list.get(0)) ? LdsPhzHuXiEnums.CHI123.getSmall()
									: LdsPhzHuXiEnums.CHI123.getBig();
						} else {
							total = LdsPhzCardUtils.smallCard(list.get(0)) ? LdsPhzHuXiEnums.CHI.getSmall()
									: LdsPhzHuXiEnums.CHI.getBig();
						}
					} else if (redCard(list.get(0)) && redCard(list.get(1)) && sameType(list.get(0), list.get(1))) {
						total = LdsPhzCardUtils.smallCard(list.get(0)) ? LdsPhzHuXiEnums.CHI2710.getSmall()
								: LdsPhzHuXiEnums.CHI2710.getBig();
					} else {
						total = LdsPhzHuXiEnums.JIAO.getBig();
					}
				}

			}
			break;
		case 2:
			total = LdsPhzCardUtils.smallCard(list.get(0)) ? LdsPhzHuXiEnums.DUI.getSmall()
					: LdsPhzHuXiEnums.DUI.getBig();
			break;
		case 4:
			total = LdsPhzCardUtils.smallCard(list.get(0)) ? LdsPhzHuXiEnums.TI.getSmall()
					: LdsPhzHuXiEnums.TI.getBig();
			break;
		}

		return total;
	}

	public static int countHuxi(List<List<Integer>> lists) {
		int total = 0;
		for (List<Integer> list : lists) {
			total += countHuxi0(list);
		}
		return total;
	}

	public static boolean redCard(int id) {
		return redCardByVal(loadCardVal(id));
	}

	public static boolean redCardByVal(int val) {
		return val == 2 || val == 7 || val == 10 || val == 102 || val == 107 || val == 110;
	}

	public static boolean hasRedCard(List<Integer> list) {
		for (Integer integer : list) {
			if (redCard(integer)) {
				return true;
			}
		}
		return false;
	}

	public static int countRedCard(List<Integer> list) {
		int count = 0;
		for (Integer integer : list) {
			if (redCard(integer)) {
				count++;
			}
		}
		return count;
	}

	public static boolean hasCard(Collection<Integer> list, int val) {
		for (Integer integer : list) {
			if (val == loadCardVal(integer)) {
				return true;
			}
		}
		return false;
	}

	public static int countCard(Collection<Integer> list, int val) {
		int count = 0;
		for (Integer integer : list) {
			if (val == loadCardVal(integer)) {
				count++;
			}
		}
		return count;
	}

	public static List<Integer> asList(Integer int1, Integer int2) {
		List<Integer> list = new ArrayList<>(4);
		list.add(int1);
		list.add(int2);
		return list;
	}

	public static List<Integer> asList(Integer int1, Integer int2, Integer int3) {
		List<Integer> list = new ArrayList<>(4);
		list.add(int1);
		list.add(int2);
		list.add(int3);
		return list;
	}

	public static List<Integer> asList(Integer int1, Integer int2, Integer int3, Integer int4) {
		List<Integer> list = new ArrayList<>(4);
		list.add(int1);
		list.add(int2);
		list.add(int3);
		list.add(int4);
		return list;
	}

	public static List<Integer> asList(Integer... ints) {
		List<Integer> list = new ArrayList<>(ints.length + 2);
		for (Integer integer : ints) {
			list.add(integer);
		}
		return list;
	}

	/**
	 * 计算醒的值
	 * 
	 * @param cardVal
	 *            醒牌值
	 * @param xingMode
	 *            醒模式 1翻醒
	 * @return
	 */
	public static int loadXingVal(int cardVal, int xingMode) {
		if (xingMode == 1) {
			// if (cardVal == 10) {
			// cardVal = 1;
			// } else if (cardVal == 110) {
			// cardVal = 101;
			// } else {
			// cardVal += 1;
			// }
			return cardVal;
		} else {
			return cardVal;
		}
	}

	public static synchronized List<List<Integer>> fapai2(List<Integer> copy, List<List<Integer>> t) {
		List<List<GuihzCard>> list = new ArrayList<>();
		Collections.shuffle(copy);
		List<GuihzCard> pai = new ArrayList<>();
		int j = 1;
		int testcount = 0;
		if (GameServerConfig.isDebug() && t != null && !t.isEmpty()) {
			for (List<Integer> zp : t) {
				list.add(find(copy, zp));
				testcount += zp.size();
			}
			if (list.size() == 3) {
				list.add(toGhzCards(copy));
				return getFaPaiList(list);
			} else if (list.size() == 4) {
				return getFaPaiList(list);
			}
		}
		List<Integer> copy2 = new ArrayList<>(copy);
		int fapaiCount = 14 * 3 + 1 - testcount;// 庄家20张 闲家19张
		if (pai.size() >= 15) {
			list.add(pai);
			pai = new ArrayList<>();
		}
		boolean test = false;
		if (list.size() > 0) {
			test = true;
		}
		for (int i = 0; i < fapaiCount; i++) {
			GuihzCard majiang = GuihzCard.getPaohzCard(copy.get(i));
			copy2.remove((Object) copy.get(i));
			if (test) {
				if (i < j * 14) {
					pai.add(majiang);
				} else {
					list.add(pai);
					pai = new ArrayList<>();
					pai.add(majiang);
					j++;
				}
			} else {
				if (i <= j * 14) {
					pai.add(majiang);
				} else {
					list.add(pai);
					pai = new ArrayList<>();
					pai.add(majiang);
					j++;
				}
			}
		}
		list.add(pai);
		List<GuihzCard> left = new ArrayList<>();
		for (int i = 0; i < copy2.size(); i++) {
			left.add(GuihzCard.getPaohzCard(copy2.get((i))));
		}
		list.add(left);// 剩下的牌

		List<List<Integer>> cards = getFaPaiList(list);
		return cards;
	}

	private static List<List<Integer>> getFaPaiList(List<List<GuihzCard>> list) {
		List<List<Integer>> cards = new ArrayList<List<Integer>>();

		for (List<GuihzCard> list2 : list) {
			List<Integer> list3 = new ArrayList<Integer>();
			for (GuihzCard card : list2) {
				list3.add(card.getId());
			}
			cards.add(list3);
		}
		return cards;
	}

	private static List<GuihzCard> find(List<Integer> copy, List<Integer> valList) {
		List<GuihzCard> pai = new ArrayList<>();
		if (!valList.isEmpty()) {
			for (int zpId : valList) {
				Iterator<Integer> iterator = copy.iterator();
				while (iterator.hasNext()) {
					int card = iterator.next();
					GuihzCard phz = GuihzCard.getPaohzCard(card);
					if (phz.getVal() == zpId) {
						pai.add(phz);
						iterator.remove();
						break;
					}
				}
			}

		}
		return pai;
	}

	/**
	 * Id转化为牌
	 * 
	 * @param phzIds
	 * @return
	 */
	public static List<GuihzCard> toGhzCards(List<Integer> phzIds) {
		List<GuihzCard> cards = new ArrayList<>();
		for (int id : phzIds) {
			cards.add(GuihzCard.getPaohzCard(id));
		}
		return cards;
	}


	public static synchronized List<List<Integer>> fapaiControl(List<Integer> copy, int playerCount, int cardsCount,int fapaiNum) {
		Collections.shuffle(copy);
		List<List<Integer>> list = loadCards(new ArrayList<>(copy), playerCount, cardsCount,playerCount);
		fapaiNum++;
		if(fapaiNum>=2)
			return list;
		for (int j = 0; j < playerCount; j++) {
			List<Integer> cards = list.get(j);
			int num=0;
			for (Integer id:cards) {
				if(id>80){
					num++;
				}
			}
			if(num>=3){
				return fapaiControl(copy, playerCount, cardsCount,fapaiNum);
			}
		}
		return list;
	}

	public static void main(String[] args) {
		int count=0;
		for (int i = 0; i < 100000; i++) {
			int count1=0;
			List<List<Integer>> list = fapaiControl(LdsPhzCardUtils.loadCards(3), 2, 14,0);
			for (int j = 0; j < 2; j++) {
				List<Integer> cards = list.get(j);
				int num=0;
				int nums[]=new int[21];
				for (Integer id:cards) {
					int val = GuihzCard.getPaohzCard(id).getVal();
					if(val<100){
						nums[val-1]++;
					}else if(val<=110){
						nums[val-91]++;
					}else {
						nums[20]++;
					}
				}
				for (int k = 0; k < nums.length; k++) {
					if(num==4)
						count1++;
				}
			}
			if(count1>=2)
				count++;
		}

		System.out.println("双龙次数"+count);


	}
}
