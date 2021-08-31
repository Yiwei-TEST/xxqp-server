package com.sy599.game.qipai.ldsphz.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.sy599.game.qipai.ldsphz.bean.LdsPhzBase;

public class LdsPhzCardResult {
	private boolean canHu = false;// 是否可以胡牌
	private int curId = 0;
	private int totalHuxi = 0;// 总胡息
	private int redTotal = 0;// 红牌总数
	private int redCount = 0;// 手牌中的红牌数
	private LdsPhzHandCards handCards;// 手牌实体类
	private boolean self = false;
	private boolean wangDiao = false;
	private boolean wangChuang = false;
	private boolean wangZha = false;
	private Set<LdsPhzFanEnums> fans = new HashSet<>();
	private LdsPhzBase phzBase;
	private int xingCard;
	private int huCard;
	private int minFan = 1;
	private int totalFan = 1;
	private int totalTun = 0;
	private int huxiTun = 0;
	private int xingTun = 0;

	private List<LdsPhzCardMessage> cardMessageList = new ArrayList<>(8);
	private List<String> replaceCards = new ArrayList<>(8);
	private Map<String, LdsPhzCardMessage> checkCmMap = new HashMap<>(8);

	private LdsPhzCardResult chuangCardResult;
	private LdsPhzCardResult diaoCardResult;

	private LdsPhzCardMessage wangCardMessage = null;

	public List<String> getReplaceCards() {
		return replaceCards;
	}

	public int getHuxiTun() {
		return huxiTun;
	}

	public void setHuxiTun(int huxiTun) {
		this.huxiTun = huxiTun;
	}

	public int getXingTun() {
		return xingTun;
	}

	public void setXingTun(int xingTun) {
		this.xingTun = xingTun;
	}

	public int getHuCard() {
		return huCard;
	}

	public void setHuCard(int huCard) {
		this.huCard = huCard;
	}

	public int getXingCard() {
		return xingCard;
	}

	public void setXingCard(int xingCard) {
		this.xingCard = xingCard;
	}

	public int getMinFan() {
		return minFan;
	}

	public void setMinFan(int minFan) {
		this.minFan = minFan;
	}

	public boolean reFan() {
		return fans.remove(LdsPhzFanEnums.HONG_HU) || fans.remove(LdsPhzFanEnums.HONG_TO_HEI)
				|| fans.remove(LdsPhzFanEnums.HONG_TO_DIAN) || fans.remove(LdsPhzFanEnums.DIAN_HU)
				|| fans.remove(LdsPhzFanEnums.HEI_HU);
	}

	public int calc() {
		wangCardMessage = null;
		redTotal = 0;
		totalHuxi = 0;
		replaceCards.clear();
		reFan();
		List<Integer> comCards = new ArrayList<>(26);
		List<Integer> indexList = new ArrayList<>(4);
		List<Integer> wangCardList = null;

		if (cardMessageList == null || cardMessageList.size() == 0) {
			return 0;
		}

		for (LdsPhzCardMessage cm : cardMessageList) {
			totalHuxi += cm.loadHuxi();
			List<Integer> list = cm.getCards();
			for (Integer integer : list) {
				if (LdsPhzCardUtils.commonCard(integer)) {
					comCards.add(integer);
				}
			}
			LdsPhzCardUtils.sort(list);
			switch (list.size()) {
			case 2:
				if (LdsPhzCardUtils.wangCard(list.get(0))) {
					if (redCount > 1) {
						redTotal += 2;
					}
					wangCardList = list;
				} else if (LdsPhzCardUtils.wangCard(list.get(1))) {
					if (LdsPhzCardUtils.redCard(list.get(0))) {
						redTotal += 2;
					}
					replaceCards.add(new StringBuilder(5).append("|").append(list.get(0)).append("|").toString());
				} else {
					if (LdsPhzCardUtils.redCard(list.get(0))) {
						redTotal += 1;
					}
					if (LdsPhzCardUtils.redCard(list.get(1))) {
						redTotal += 1;
					}
				}
				break;
			case 3:
				int val1 = LdsPhzCardUtils.loadCardVal(list.get(0));
				int val2 = LdsPhzCardUtils.loadCardVal(list.get(1));
				if (LdsPhzCardUtils.wangCard(list.get(0))) {
					if (redCount > 1) {
						redTotal += 3;
					}
					wangCardList = list;
					wangCardMessage = cm;
				} else if (LdsPhzCardUtils.wangCard(list.get(1))) {
					if (LdsPhzCardUtils.redCard(list.get(0))) {
						redTotal += 3;
					}
					replaceCards.add(new StringBuilder(5).append("|").append(list.get(0)).append("|").toString());
					replaceCards.add(new StringBuilder(5).append("|").append(list.get(0)).append("|").toString());
				} else if (LdsPhzCardUtils.wangCard(list.get(2))) {
					if (val2 - val1 == 1) {
						if (val1 == 3 || val1 == 103 || val1 == 5 || val1 == 105) {
							if (redCount > 1) {
								redTotal += 1;
								if (val1 == 3) {
									replaceCards.add("|2|");
								} else if (val1 == 105) {
									replaceCards.add("|57|");
								} else if (val1 == 103) {
									replaceCards.add("|52|");
								} else if (val1 == 5) {
									replaceCards.add("|7|");
								}
								indexList.add(replaceCards.size() - 1);
							} else {
								if (val1 == 3) {
									replaceCards.add("|5|");
								} else if (val1 == 105) {
									replaceCards.add("|64|");
								} else if (val1 == 103) {
									replaceCards.add("|55|");
								} else if (val1 == 5) {
									replaceCards.add("|4|");
								}
							}
						} else if (val1 == 8 || val1 == 108) {
							redTotal += 1;
							if (val1 == 8) {
								replaceCards.add("|7|10|");
							} else if (val1 == 108) {
								replaceCards.add("|57|60");
							}
						} else if (val2 == 10 || val2 == 110) {
							if (val2 == 10) {
								replaceCards.add("|8|");
							} else if (val2 == 110) {
								replaceCards.add("|58|");
							}
						} else if (val1 == 1 || val1 == 101) {
							if (val1 == 1) {
								replaceCards.add("|3|");
							} else if (val1 == 101) {
								replaceCards.add("|53|");
							}
						} else {
							String tempStr = new StringBuilder(9).append("|").append((list.get(0) - 1)).append("|")
									.append((list.get(1) + 1)).append("|").toString();
							if (val1 == 2) {
								checkCmMap.put(replaceCards.size() + "," + "4", cm);
							} else if (val1 == 102) {
								checkCmMap.put(replaceCards.size() + "," + "104", cm);
							}
							replaceCards.add(tempStr);
						}

						if (LdsPhzCardUtils.redCard(list.get(0))) {
							redTotal += 1;
						}
						if (LdsPhzCardUtils.redCard(list.get(1))) {
							redTotal += 1;
						}
					} else if (val2 - val1 == 2) {
						replaceCards
								.add(new StringBuilder(5).append("|").append(list.get(0) + 1).append("|").toString());
						if (val1 == 1 || val1 == 101 || val1 == 6 || val1 == 106) {
							redTotal += 1;
						}

						if (LdsPhzCardUtils.redCard(list.get(0))) {
							redTotal += 1;
						}
						if (LdsPhzCardUtils.redCard(list.get(1))) {
							redTotal += 1;
						}
					} else if (val2 - val1 == 0) {
						if (LdsPhzCardUtils.redCard(list.get(0))) {
							redTotal += 3;
						}
						replaceCards.add(new StringBuilder(5).append("|").append(list.get(0)).append("|").toString());
					} else if (val2 - val1 == 100) {
						if (LdsPhzCardUtils.redCard(list.get(0))) {
							redTotal += 3;
						}
						replaceCards.add(new StringBuilder(9).append("|").append((list.get(0))).append("|")
								.append((list.get(1))).append("|").toString());
					} else if (LdsPhzCardUtils.redCard(list.get(0)) && LdsPhzCardUtils.redCard(list.get(1))) {
						redTotal += 3;
						if (val1 == 2 && val2 == 7) {
							replaceCards.add("|10|");
						} else if (val1 == 2 && val2 == 10) {
							replaceCards.add("|7|");
						} else if (val1 == 7 && val2 == 10) {
							replaceCards.add("|2|");
						} else if (val1 == 102 && val2 == 107) {
							replaceCards.add("|60|");
						} else if (val1 == 102 && val2 == 110) {
							replaceCards.add("|57|");
						} else if (val1 == 107 && val2 == 110) {
							replaceCards.add("|52|");
						}
					}
				} else {
					if (LdsPhzCardUtils.redCard(list.get(0))) {
						redTotal += 1;
					}
					if (LdsPhzCardUtils.redCard(list.get(1))) {
						redTotal += 1;
					}
					if (LdsPhzCardUtils.redCard(list.get(2))) {
						redTotal += 1;
					}
				}
				break;
			case 4:
				if (LdsPhzCardUtils.wangCard(list.get(0))) {
					if (redCount > 1) {
						redTotal += 4;
					}
					wangCardList = list;
					wangCardMessage = cm;
				} else if (LdsPhzCardUtils.wangCard(list.get(1))) {
					if (LdsPhzCardUtils.redCard(list.get(0))) {
						redTotal += 4;
					}
					replaceCards.add(new StringBuilder(5).append("|").append(list.get(0)).append("|").toString());
					replaceCards.add(new StringBuilder(5).append("|").append(list.get(0)).append("|").toString());
					replaceCards.add(new StringBuilder(5).append("|").append(list.get(0)).append("|").toString());
				} else if (LdsPhzCardUtils.wangCard(list.get(2))) {
					if (LdsPhzCardUtils.redCard(list.get(0))) {
						redTotal += 4;
					}
					replaceCards.add(new StringBuilder(5).append("|").append(list.get(0)).append("|").toString());
					replaceCards.add(new StringBuilder(5).append("|").append(list.get(0)).append("|").toString());
				} else if (LdsPhzCardUtils.wangCard(list.get(3))) {
					if (LdsPhzCardUtils.redCard(list.get(0))) {
						redTotal += 4;
					}
					replaceCards.add(new StringBuilder(5).append("|").append(list.get(0)).append("|").toString());
				} else {
					if (LdsPhzCardUtils.redCard(list.get(0))) {
						redTotal += 4;
					}
				}
				break;
			}
		}

		if (redTotal == 0) {
			fans.add(LdsPhzFanEnums.HEI_HU);
			if (wangCardList != null) {
				int max = 0;
				int val = 0;
				Map<Integer, Integer> countMap = LdsPhzCardUtils.loadCardCount(comCards);

				for (String str : replaceCards) {
					String[] strs = str.split("\\|");
					for (String s : strs) {
						if (s.length() > 0) {
							Integer v = LdsPhzCardUtils.loadCardVal(Integer.parseInt(s));
							Integer c = countMap.get(v);
							if (c == null) {
								countMap.put(v, 1);
							} else {
								countMap.put(v, 1 + c.intValue());
							}
						}
					}
				}

				for (Map.Entry<Integer, Integer> kv : countMap.entrySet()) {
					if (kv.getValue().intValue() > max
							|| (kv.getValue().intValue() == max && kv.getKey().intValue() > 100)) {
						max = kv.getValue().intValue();
						val = kv.getKey().intValue();
					}
				}
				List<String> strList = new ArrayList<>(replaceCards);
				int card = (val >= 1 && val <= 10) ? val : ((val >= 101 && val <= 110) ? (40 + (val % 100)) : 0);
				for (int i = 0; i < strList.size(); i++) {
					String[] strs = strList.get(i).split("\\|");
					boolean update = false;
					for (String s : strs) {
						if (s.length() > 0) {
							if (val == LdsPhzCardUtils.loadCardVal(Integer.parseInt(s))) {
								max++;
								update = true;
							}
						}
					}
					if (update) {
						replaceCards.set(i, new StringBuilder(5).append("|").append(card).append("|").toString());

					}
				}
				for (int i = 0; i < wangCardList.size(); i++) {
					replaceCards.add(new StringBuilder(5).append("|").append(card).append("|").toString());
				}
				if (card <= 40 && wangCardMessage != null && wangCardMessage.getCards().size() >= 3) {
					if (totalHuxi >= phzBase.loadQihuxi() + 3) {
						totalHuxi -= 3;
						wangCardMessage.init(wangCardMessage.getCards().size() == 3 ? LdsPhzHuXiEnums.WANG_SMALL_KAN
								: LdsPhzHuXiEnums.WANG_SMALL_TI, wangCardMessage.getCards());
					} else {
						max = 0;
						val = 0;
						for (Map.Entry<Integer, Integer> kv : countMap.entrySet()) {
							if (kv.getKey().intValue() > 100 && kv.getValue().intValue() > max) {
								max = kv.getValue().intValue();
								val = kv.getKey().intValue();
							}
						}
						card = (val >= 1 && val <= 10) ? val : ((val >= 101 && val <= 110) ? (40 + (val % 100)) : 49);
						for (int i = 0; i < strList.size(); i++) {
							String[] strs = strList.get(i).split("\\|");
							boolean update = false;
							for (String s : strs) {
								if (s.length() > 0) {
									if (val == LdsPhzCardUtils.loadCardVal(Integer.parseInt(s))) {
										max++;
										update = true;
									}
								}
							}
							if (update) {
								replaceCards.set(i,
										new StringBuilder(5).append("|").append(card).append("|").toString());
							}
						}
						int len = replaceCards.size() - 1;
						for (int i = 0; i < wangCardList.size(); i++) {
							replaceCards.set(len - i,
									new StringBuilder(5).append("|").append(card).append("|").toString());
						}
					}
				}
			}
		} else if (redTotal == 1) {
			fans.add(LdsPhzFanEnums.DIAN_HU);
			if (wangCardList != null) {
				int max = 0;
				int val = 0;
				Map<Integer, Integer> countMap = LdsPhzCardUtils.loadCardCount(comCards);

				for (String str : replaceCards) {
					String[] strs = str.split("\\|");
					for (String s : strs) {
						if (s.length() > 0) {
							Integer v = LdsPhzCardUtils.loadCardVal(Integer.parseInt(s));
							Integer c = countMap.get(v);
							if (c == null) {
								countMap.put(v, 1);
							} else {
								countMap.put(v, 1 + c.intValue());
							}
						}
					}
				}

				for (Map.Entry<Integer, Integer> kv : countMap.entrySet()) {
					if (kv.getValue().intValue() > max
							|| (kv.getValue().intValue() == max && kv.getKey().intValue() > 100)) {
						max = kv.getValue().intValue();
						val = kv.getKey().intValue();
					}
				}
				List<String> strList = new ArrayList<>(replaceCards);
				int card = (val >= 1 && val <= 10) ? val : ((val >= 101 && val <= 110) ? (40 + (val % 100)) : 0);
				for (int i = 0; i < strList.size(); i++) {
					String[] strs = strList.get(i).split("\\|");
					boolean update = false;
					for (String s : strs) {
						if (s.length() > 0) {
							if (val == LdsPhzCardUtils.loadCardVal(Integer.parseInt(s))) {
								max++;
								update = true;
							}
						}
					}
					if (update) {
						replaceCards.set(i, new StringBuilder(5).append("|").append(card).append("|").toString());
					}
				}
				for (int i = 0; i < wangCardList.size(); i++) {
					replaceCards.add(new StringBuilder(5).append("|").append(card).append("|").toString());
				}

				if (card <= 40 && wangCardMessage != null && wangCardMessage.getCards().size() >= 3) {
					if (totalHuxi >= phzBase.loadQihuxi() + 3) {
						totalHuxi -= 3;
						wangCardMessage.init(wangCardMessage.getCards().size() == 3 ? LdsPhzHuXiEnums.WANG_SMALL_KAN
								: LdsPhzHuXiEnums.WANG_SMALL_TI, wangCardMessage.getCards());
					} else {
						max = 0;
						val = 0;
						for (Map.Entry<Integer, Integer> kv : countMap.entrySet()) {
							if (kv.getKey().intValue() > 100 && kv.getValue().intValue() > max) {
								max = kv.getValue().intValue();
								val = kv.getKey().intValue();
							}
						}
						card = (val >= 1 && val <= 10) ? val : ((val >= 101 && val <= 110) ? (40 + (val % 100)) : 49);
						for (int i = 0; i < strList.size(); i++) {
							String[] strs = strList.get(i).split("\\|");
							boolean update = false;
							for (String s : strs) {
								if (s.length() > 0) {
									if (val == LdsPhzCardUtils.loadCardVal(Integer.parseInt(s))) {
										max++;
										update = true;
									}
								}
							}
							if (update) {
								replaceCards.set(i,
										new StringBuilder(5).append("|").append(card).append("|").toString());
							}
						}
						int len = replaceCards.size() - 1;
						for (int i = 0; i < wangCardList.size(); i++) {
							replaceCards.set(len - i,
									new StringBuilder(5).append("|").append(card).append("|").toString());
						}
					}
				}
			}
		}
		// else if (redTotal1>=15){
		// fans.add(LdsPhzFanEnums.HONG_TO_HEI);
		// }else if (redTotal1>=13){
		// fans.add(LdsPhzFanEnums.HONG_TO_DIAN);
		// }
		else if (redTotal >= phzBase.loadRedCount()) {
			int baseRed;

			if (redTotal > 9) {
				baseRed = 10;
				fans.add(LdsPhzFanEnums.ALL_HONG);
				// baseRed = phzBase.loadRed2BlackCount();
				// fans.add(LdsPhzFanEnums.HONG_TO_HEI);
				// } else if (phzBase.checkRed2Dian() && redTotal >=
				// phzBase.loadRed2DianCount()) {
				// baseRed = phzBase.loadRed2DianCount();
				// fans.add(LdsPhzFanEnums.HONG_TO_DIAN);
				// }
			} else {
				baseRed = phzBase.loadRedCount();
				fans.add(LdsPhzFanEnums.HONG_HU);
			}

			if (indexList.size() > 0) {
				int m = redTotal - baseRed;
				if (m > 0) {
					while (m > 0) {
						int idx = indexList.remove(0);
						String temp = replaceCards.get(idx);
						if ("|2|".equals(temp)) {
							replaceCards.set(idx, "|2|5|");
						} else if ("|52|".equals(temp)) {
							replaceCards.set(idx, "|52|55|");
						} else if ("|7|".equals(temp)) {
							replaceCards.set(idx, "|7|4|");
						} else if ("|57|".equals(temp)) {
							replaceCards.set(idx, "|57|54|");
						}
						m--;
						if (indexList.size() == 0) {
							break;
						}
					}
				}
			}

			if (wangCardList != null) {
				if (redTotal - wangCardList.size() >= baseRed) {
					int max = 0;
					int val = 0;
					Map<Integer, Integer> countMap = LdsPhzCardUtils.loadCardCount(comCards);

					for (String str : replaceCards) {
						String[] strs = str.split("\\|");
						for (String s : strs) {
							if (s.length() > 0) {
								Integer v = LdsPhzCardUtils.loadCardVal(Integer.parseInt(s));
								Integer c = countMap.get(v);
								if (c == null) {
									countMap.put(v, 1);
								} else {
									countMap.put(v, 1 + c.intValue());
								}
							}
						}
					}

					for (Map.Entry<Integer, Integer> kv : countMap.entrySet()) {
						if (kv.getValue().intValue() > max
								|| (kv.getValue().intValue() == max && kv.getKey().intValue() > 100)) {
							max = kv.getValue().intValue();
							val = kv.getKey().intValue();
						}
					}
					List<String> strList = new ArrayList<>(replaceCards);
					int card = (val >= 1 && val <= 10) ? val : ((val >= 101 && val <= 110) ? (40 + (val % 100)) : 0);
					for (int i = 0; i < strList.size(); i++) {
						String[] strs = strList.get(i).split("\\|");
						boolean update = false;
						for (String s : strs) {
							if (s.length() > 0) {
								if (val == LdsPhzCardUtils.loadCardVal(Integer.parseInt(s))) {
									max++;
									update = true;
								}
							}
						}
						if (update) {
							replaceCards.set(i, new StringBuilder(5).append("|").append(card).append("|").toString());
						}
					}
					for (int i = 0; i < wangCardList.size(); i++) {
						replaceCards.add(new StringBuilder(5).append("|").append(card).append("|").toString());
					}
					if (card <= 40 && wangCardMessage != null && wangCardMessage.getCards().size() >= 3) {
						if (totalHuxi >= phzBase.loadQihuxi() + 3) {
							totalHuxi -= 3;
							wangCardMessage.init(wangCardMessage.getCards().size() == 3 ? LdsPhzHuXiEnums.WANG_SMALL_KAN
									: LdsPhzHuXiEnums.WANG_SMALL_TI, wangCardMessage.getCards());
						} else {
							max = 0;
							val = 0;
							for (Map.Entry<Integer, Integer> kv : countMap.entrySet()) {
								if (kv.getKey().intValue() > 100 && kv.getValue().intValue() > max) {
									max = kv.getValue().intValue();
									val = kv.getKey().intValue();
								}
							}
							card = (val >= 1 && val <= 10) ? val
									: ((val >= 101 && val <= 110) ? (40 + (val % 100)) : 50);
							for (int i = 0; i < strList.size(); i++) {
								String[] strs = strList.get(i).split("\\|");
								boolean update = false;
								for (String s : strs) {
									if (s.length() > 0) {
										if (val == LdsPhzCardUtils.loadCardVal(Integer.parseInt(s))) {
											max++;
											update = true;
										}
									}
								}
								if (update) {
									replaceCards.set(i,
											new StringBuilder(5).append("|").append(card).append("|").toString());
								}
							}
							int len = replaceCards.size() - 1;
							for (int i = 0; i < wangCardList.size(); i++) {
								replaceCards.set(len - i,
										new StringBuilder(5).append("|").append(card).append("|").toString());
							}
						}
					}
				} else {
					int max = 0;
					int val = 0;
					Map<Integer, Integer> countMap = LdsPhzCardUtils.loadCardCount(comCards);

					for (String str : replaceCards) {
						String[] strs = str.split("\\|");
						for (String s : strs) {
							if (s.length() > 0) {
								Integer v = LdsPhzCardUtils.loadCardVal(Integer.parseInt(s));
								Integer c = countMap.get(v);
								if (c == null) {
									countMap.put(v, 1);
								} else {
									countMap.put(v, 1 + c.intValue());
								}
							}
						}
					}

					for (Map.Entry<Integer, Integer> kv : countMap.entrySet()) {
						int tempV = kv.getKey();
						if (LdsPhzCardUtils.redCardByVal(tempV) && (kv.getValue().intValue() > max
								|| (kv.getValue().intValue() == max && kv.getKey().intValue() > 100))) {
							max = kv.getValue().intValue();
							val = kv.getKey().intValue();
						}
					}
					List<String> strList = new ArrayList<>(replaceCards);
					int card = (val >= 1 && val <= 10) ? val : ((val >= 101 && val <= 110) ? (40 + (val % 100)) : 0);
					for (int i = 0; i < strList.size(); i++) {
						String[] strs = strList.get(i).split("\\|");
						boolean update = false;
						for (String s : strs) {
							if (s.length() > 0) {
								if (val == LdsPhzCardUtils.loadCardVal(Integer.parseInt(s))) {
									max++;
									update = true;
								}
							}
						}
						if (update) {
							replaceCards.set(i, new StringBuilder(5).append("|").append(card).append("|").toString());
						}
					}
					for (int i = 0; i < wangCardList.size(); i++) {
						replaceCards.add(new StringBuilder(5).append("|").append(card).append("|").toString());
					}
					if (card <= 40 && wangCardMessage != null && wangCardMessage.getCards().size() >= 3) {
						if (totalHuxi >= phzBase.loadQihuxi() + 3) {
							totalHuxi -= 3;
							wangCardMessage.init(wangCardMessage.getCards().size() == 3 ? LdsPhzHuXiEnums.WANG_SMALL_KAN
									: LdsPhzHuXiEnums.WANG_SMALL_TI, wangCardMessage.getCards());
						} else {
							max = 0;
							val = 0;
							for (Map.Entry<Integer, Integer> kv : countMap.entrySet()) {
								if (LdsPhzCardUtils.redCardByVal(kv.getKey().intValue()) && kv.getKey().intValue() > 100
										&& kv.getValue().intValue() > max) {
									max = kv.getValue().intValue();
									val = kv.getKey().intValue();
								}
							}
							card = (val >= 1 && val <= 10) ? val
									: ((val >= 101 && val <= 110) ? (40 + (val % 100)) : 50);
							for (int i = 0; i < strList.size(); i++) {
								String[] strs = strList.get(i).split("\\|");
								boolean update = false;
								for (String s : strs) {
									if (s.length() > 0) {
										if (val == LdsPhzCardUtils.loadCardVal(Integer.parseInt(s))) {
											max++;
											update = true;
										}
									}
								}
								if (update) {
									replaceCards.set(i,
											new StringBuilder(5).append("|").append(card).append("|").toString());
								}
							}
							int len = replaceCards.size() - 1;
							for (int i = 0; i < wangCardList.size(); i++) {
								replaceCards.set(len - i,
										new StringBuilder(5).append("|").append(card).append("|").toString());
							}
						}
					}
				}
			}
		} else {
			while (indexList.size() > 0) {
				int idx = indexList.remove(0);
				String temp = replaceCards.get(idx);
				if ("|2|".equals(temp)) {
					replaceCards.set(idx, "|2|5|");
				} else if ("|52|".equals(temp)) {
					replaceCards.set(idx, "|52|55|");
				} else if ("|7|".equals(temp)) {
					replaceCards.set(idx, "|7|4|");
				} else if ("|57|".equals(temp)) {
					replaceCards.set(idx, "|57|54|");
				}
			}

			if (wangCardList != null) {
				int max = 0;
				int val = 0;
				Map<Integer, Integer> countMap = LdsPhzCardUtils.loadCardCount(comCards);

				for (String str : replaceCards) {
					String[] strs = str.split("\\|");
					for (String s : strs) {
						if (s.length() > 0) {
							Integer v = LdsPhzCardUtils.loadCardVal(Integer.parseInt(s));
							Integer c = countMap.get(v);
							if (c == null) {
								countMap.put(v, 1);
							} else {
								countMap.put(v, 1 + c.intValue());
							}
						}
					}
				}

				for (Map.Entry<Integer, Integer> kv : countMap.entrySet()) {
					if (kv.getValue().intValue() > max
							|| (kv.getValue().intValue() == max && kv.getKey().intValue() > 100)) {
						max = kv.getValue().intValue();
						val = kv.getKey();
					}
				}
				List<String> strList = new ArrayList<>(replaceCards);
				int card = (val >= 1 && val <= 10) ? val : ((val >= 101 && val <= 110) ? (40 + (val % 100)) : 0);
				for (int i = 0; i < strList.size(); i++) {
					String[] strs = strList.get(i).split("\\|");
					boolean update = false;
					for (String s : strs) {
						if (s.length() > 0) {
							if (val == LdsPhzCardUtils.loadCardVal(Integer.parseInt(s))) {
								max++;
								update = true;
							}
						}
					}
					if (update) {
						replaceCards.set(i, new StringBuilder(5).append("|").append(card).append("|").toString());
					}
				}
				for (int i = 0; i < wangCardList.size(); i++) {
					replaceCards.add(new StringBuilder(5).append("|").append(card).append("|").toString());
				}
				if (card <= 40 && wangCardMessage != null && wangCardMessage.getCards().size() >= 3) {
					if (totalHuxi >= phzBase.loadQihuxi() + 3) {
						totalHuxi -= 3;
						wangCardMessage.init(wangCardMessage.getCards().size() == 3 ? LdsPhzHuXiEnums.WANG_SMALL_KAN
								: LdsPhzHuXiEnums.WANG_SMALL_TI, wangCardMessage.getCards());
					} else {
						max = 0;
						val = 0;
						for (Map.Entry<Integer, Integer> kv : countMap.entrySet()) {
							if (kv.getKey().intValue() > 100 && kv.getValue().intValue() > max) {
								max = kv.getValue().intValue();
								val = kv.getKey().intValue();
							}
						}
						card = (val >= 1 && val <= 10) ? val : ((val >= 101 && val <= 110) ? (40 + (val % 100)) : 50);
						for (int i = 0; i < strList.size(); i++) {
							String[] strs = strList.get(i).split("\\|");
							boolean update = false;
							for (String s : strs) {
								if (s.length() > 0) {
									if (val == LdsPhzCardUtils.loadCardVal(Integer.parseInt(s))) {
										max++;
										update = true;
									}
								}
							}
							if (update) {
								replaceCards.set(i,
										new StringBuilder(5).append("|").append(card).append("|").toString());
							}
						}
						int len = replaceCards.size() - 1;
						for (int i = 0; i < wangCardList.size(); i++) {
							replaceCards.set(len - i,
									new StringBuilder(5).append("|").append(card).append("|").toString());
						}
					}
				}
			}
		}

		return redTotal;
	}

	public boolean isCanHu() {
		return canHu;
	}

	public void setCanHu(boolean canHu) {
		this.canHu = canHu;
	}

	public int getTotalHuxi() {
		return getTotalHuxi(true);
	}

	public int getTotalHuxi(boolean reload) {
		if (reload) {
			calc();
		}
		return totalHuxi;
	}

	public void setTotalHuxi(int totalHuxi) {
		this.totalHuxi = totalHuxi;
	}

	public int getRedTotal() {
		return redTotal;
	}

	public void setRedTotal(int redTotal) {
		this.redTotal = redTotal;
	}

	public int getRedCount() {
		return redCount;
	}

	public void setRedCount(int redCount) {
		this.redCount = redCount;
	}

	public LdsPhzHandCards getHandCards() {
		return handCards;
	}

	public void setHandCards(LdsPhzHandCards handCards) {
		this.handCards = handCards;
	}

	public boolean isSelf() {
		return self;
	}

	public void setSelf(boolean self) {
		this.self = self;
	}

	public boolean isWangDiao() {
		return wangDiao;
	}

	public void setWangDiao(boolean wangDiao) {
		this.wangDiao = wangDiao;
	}

	public boolean isWangChuang() {
		return wangChuang;
	}

	public void setWangChuang(boolean wangChuang) {
		this.wangChuang = wangChuang;
	}

	public boolean isWangZha() {
		return wangZha;
	}

	public void setWangZha(boolean wangZha) {
		this.wangZha = wangZha;
	}

	public Set<LdsPhzFanEnums> getFans() {
		return fans;
	}

	public void addFan(LdsPhzFanEnums fan) {
		this.fans.add(fan);
	}

	public void removeFan(LdsPhzFanEnums fan) {
		this.fans.remove(fan);
	}

	public void addFans(Set<LdsPhzFanEnums> fans) {
		this.fans.addAll(fans);
	}

	public int getCurId() {
		return curId;
	}

	public void setCurId(int curId) {
		this.curId = curId;
	}

	public boolean add(LdsPhzCardMessage cardMessage) {
		boolean ret = true;
		for (LdsPhzCardMessage cm : cardMessageList) {
			if (cm.eq(cardMessage)) {
				ret = false;
				break;
			}
		}
		if (ret) {
			ret = cardMessageList.add(cardMessage);
		}
		return ret;
	}

	public void addAll(List<LdsPhzCardMessage> list) {
		for (LdsPhzCardMessage cm : list) {
			add(cm);
		}
	}

	public boolean remove(LdsPhzCardMessage cardMessage) {
		boolean ret = false;
		Iterator<LdsPhzCardMessage> it = cardMessageList.iterator();
		while (it.hasNext()) {
			LdsPhzCardMessage cm = it.next();
			if (cm.eq(cardMessage)) {
				ret = true;
				it.remove();
				break;
			}
		}
		return ret;
	}

	public boolean contain(LdsPhzCardMessage cardMessage) {
		boolean ret = false;
		for (LdsPhzCardMessage cm : cardMessageList) {
			if (cm.eq(cardMessage)) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	public int loadHuxi() {
		int total = 0;
		for (LdsPhzCardMessage cm : cardMessageList) {
			total += cm.loadHuxi();
		}
		return total;
	}

	public List<LdsPhzCardMessage> getCardMessageList() {
		return cardMessageList;
	}

	public List<LdsPhzCardMessage> copyCardMessageList() {
		List<LdsPhzCardMessage> list = new ArrayList<>();
		for (LdsPhzCardMessage cm : cardMessageList) {
			list.add(new LdsPhzCardMessage(cm.getHuXiEnum(), new ArrayList<>(cm.getCards())));
		}
		return list;
	}

	public void setChuangCardResult(LdsPhzCardResult chuangCardResult) {
		this.chuangCardResult = chuangCardResult;
	}

	public void setDiaoCardResult(LdsPhzCardResult diaoCardResult) {
		this.diaoCardResult = diaoCardResult;
	}

	public LdsPhzCardResult getChuangCardResult() {
		if (chuangCardResult == null) {
			for (LdsPhzFanEnums fan : fans) {
				if (fan == LdsPhzFanEnums.WANG_CHUANG || fan == LdsPhzFanEnums.WANG_CHUANG_WANG) {
					chuangCardResult = this;
					break;
				}
			}
		}
		return chuangCardResult;
	}

	public LdsPhzCardResult getDiaoCardResult() {
		if (diaoCardResult == null) {
			for (LdsPhzFanEnums fan : fans) {
				if (fan == LdsPhzFanEnums.WANG_DIAO || fan == LdsPhzFanEnums.WANG_DIAO_WANG) {
					diaoCardResult = this;
					break;
				}
			}
		}
		return diaoCardResult;
	}

	public int getTotalFan() {
		return totalFan;
	}

	public void setTotalFan(int totalFan) {
		this.totalFan = totalFan;
	}

	public LdsPhzBase getPhzBase() {
		return phzBase;
	}

	public void setPhzBase(LdsPhzBase phzBase) {
		this.phzBase = phzBase;
	}

	public int loadValCount(final int val) {
		int count = 0;
		if (phzBase.loadXingMode() == 0) {// 跟醒
			if (val >= 200) {
				List<Integer> list = loadReplaceCards0();
				int tempCardVal = 0;
				int count1 = 0;
				int count101 = 0;
				for (Integer integer : list) {
					int val0 = LdsPhzCardUtils.loadCardVal(integer);
					if (val0 < 200) {
						int temp = 0;

						for (LdsPhzCardMessage cm : getCardMessageList()) {
							temp += LdsPhzCardUtils.loadIdsByVal(cm.getCards(), val0).size();
						}
						temp += LdsPhzCardUtils.loadIdsByVal(loadReplaceCards(val0), val0).size();

						if (temp > count) {
							count = temp;
							tempCardVal = val0;
						}

						else if (temp == count && (tempCardVal == 4 || tempCardVal == 104)) {
							tempCardVal = val0;
						}

						if (val0 == 1) {
							count1 = temp;
						} else if (val0 == 101) {
							count101 = temp;
						}
					}
				}

				xingCard = tempCardVal;

				boolean reCheck = false;
				for (int i = 0, len = replaceCards.size(); i < len; i++) {
					String[] strs = StringUtils.split(replaceCards.get(i), "|");
					if (strs.length == 2 && LdsPhzCardUtils.loadCardVal(Integer.parseInt(strs[1])) == tempCardVal) {
						if (tempCardVal == 4 && LdsPhzCardUtils.loadCardVal(Integer.parseInt(strs[0])) == 1) {
							if (totalHuxi - 3 >= phzBase.loadQihuxi()) {
								if (phzBase.loadCommonTun(totalHuxi - 3)
										+ phzBase.loadXingTun(count) >= phzBase.loadCommonTun(totalHuxi)
												+ phzBase.loadXingTun(count1)) {
									LdsPhzCardMessage cardMessage = checkCmMap.get(i + ",4");
									if (cardMessage != null) {
										cardMessage.setCheckHuxi(false);
										replaceCards.set(i, new StringBuilder(5).append("|").append(strs[1]).append("|")
												.toString());
										totalHuxi -= 3;
									}
								} else {
									replaceCards.set(i,
											new StringBuilder(5).append("|").append(strs[0]).append("|").toString());
									count--;
									reCheck = true;
								}
							} else {
								replaceCards.set(i,
										new StringBuilder(5).append("|").append(strs[0]).append("|").toString());
								count--;
								reCheck = true;
							}
						} else if (tempCardVal == 104
								&& LdsPhzCardUtils.loadCardVal(Integer.parseInt(strs[0])) == 101) {
							if (totalHuxi - 6 >= phzBase.loadQihuxi()) {
								if (phzBase.loadCommonTun(totalHuxi - 6)
										+ phzBase.loadXingTun(count) >= phzBase.loadCommonTun(totalHuxi)
												+ phzBase.loadXingTun(count101)) {
									LdsPhzCardMessage cardMessage = checkCmMap.get(i + ",104");
									if (cardMessage != null) {
										cardMessage.setCheckHuxi(false);
										replaceCards.set(i, new StringBuilder(5).append("|").append(strs[1]).append("|")
												.toString());
										totalHuxi -= 6;
									}
								} else {
									replaceCards.set(i,
											new StringBuilder(5).append("|").append(strs[0]).append("|").toString());
									count--;
									reCheck = true;
								}
							} else {
								replaceCards.set(i,
										new StringBuilder(5).append("|").append(strs[0]).append("|").toString());
								count--;
								reCheck = true;
							}
						} else {
							replaceCards.set(i,
									new StringBuilder(5).append("|").append(strs[1]).append("|").toString());
						}
					}
				}

				if (reCheck) {
					return loadValCount(val);
				}
			} else {
				for (LdsPhzCardMessage cm : getCardMessageList()) {
					count += LdsPhzCardUtils.loadIdsByVal(cm.getCards(), val).size();
				}
				count += LdsPhzCardUtils.loadIdsByVal(loadReplaceCards(val), val).size();

				if (val == 4 && checkCmMap.size() > 0) {
					boolean reCheck = false;
					Iterator<Map.Entry<String, LdsPhzCardMessage>> its = checkCmMap.entrySet().iterator();
					while (its.hasNext()) {
						Map.Entry<String, LdsPhzCardMessage> kv = its.next();
						if (kv.getKey().endsWith(",4")) {
							if (totalHuxi - 3 >= phzBase.loadQihuxi()) {
								if (phzBase.loadCommonTun(totalHuxi - 3) + phzBase.loadXingTun(count) >= phzBase
										.loadCommonTun(totalHuxi)) {
									replaceCards.set(
											Integer.parseInt(kv.getKey().substring(0, kv.getKey().indexOf(","))),
											"|4|");
									kv.getValue().setCheckHuxi(false);
									totalHuxi -= 3;
								} else {
									replaceCards.set(
											Integer.parseInt(kv.getKey().substring(0, kv.getKey().indexOf(","))),
											"|1|");
									count--;
									reCheck = true;
								}
							} else {
								replaceCards.set(Integer.parseInt(kv.getKey().substring(0, kv.getKey().indexOf(","))),
										"|1|");
								count--;
								reCheck = true;
							}
							its.remove();
						}
					}

					if (reCheck) {
						return loadValCount(val);
					}
				} else if (val == 104 && checkCmMap.size() > 0) {
					boolean reCheck = false;
					Iterator<Map.Entry<String, LdsPhzCardMessage>> its = checkCmMap.entrySet().iterator();
					while (its.hasNext()) {
						Map.Entry<String, LdsPhzCardMessage> kv = its.next();
						if (kv.getKey().endsWith(",104")) {
							if (totalHuxi - 6 >= phzBase.loadQihuxi()) {
								if (phzBase.loadCommonTun(totalHuxi - 6) + phzBase.loadXingTun(count) >= phzBase
										.loadCommonTun(totalHuxi)) {
									replaceCards.set(
											Integer.parseInt(kv.getKey().substring(0, kv.getKey().indexOf(","))),
											"|54|");
									kv.getValue().setCheckHuxi(false);
									totalHuxi -= 6;
								} else {
									replaceCards.set(
											Integer.parseInt(kv.getKey().substring(0, kv.getKey().indexOf(","))),
											"|51|");
									count--;
									reCheck = true;
								}
							} else {
								replaceCards.set(Integer.parseInt(kv.getKey().substring(0, kv.getKey().indexOf(","))),
										"|51|");
								count--;
								reCheck = true;
							}
							its.remove();
						}
					}

					if (reCheck) {
						return loadValCount(val);
					}
				}
			}
		} else if (phzBase.loadXingMode() == 1) {// 翻醒
			if (val >= 200) {
				List<Integer> list = new ArrayList<>();
				for (LdsPhzCardMessage cm : getCardMessageList()) {
					list.addAll(cm.getCards());
				}
				list.addAll(loadReplaceCards(0));
				int tempCardVal = 0;
				int count1 = 0;
				int count101 = 0;

				Map<Integer, Integer> map = LdsPhzCardUtils.loadCardCount(list);
				for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
					int temp = kv.getValue().intValue();
					int val0 = kv.getKey().intValue();
					if (temp > count) {
						count = temp;
						tempCardVal = val0;
					}

					else if (temp == count && (tempCardVal == 4 || tempCardVal == 104)) {
						tempCardVal = val0;
					}

					if (val0 == 1) {
						count1 = temp;
					} else if (val0 == 101) {
						count101 = temp;
					}
				}
				xingCard = tempCardVal;

				boolean reCheck = false;
				for (int i = 0, len = replaceCards.size(); i < len; i++) {
					String[] strs = StringUtils.split(replaceCards.get(i), "|");
					if (strs.length == 2 && LdsPhzCardUtils.loadCardVal(Integer.parseInt(strs[1])) == tempCardVal) {
						if (tempCardVal == 4 && LdsPhzCardUtils.loadCardVal(Integer.parseInt(strs[0])) == 1) {
							if (totalHuxi - 3 >= phzBase.loadQihuxi()) {
								if (phzBase.loadCommonTun(totalHuxi - 3)
										+ phzBase.loadXingTun(count) >= phzBase.loadCommonTun(totalHuxi)
												+ phzBase.loadXingTun(count1)) {
									LdsPhzCardMessage cardMessage = checkCmMap.get(i + ",4");
									if (cardMessage != null) {
										cardMessage.setCheckHuxi(false);
										replaceCards.set(i, new StringBuilder(5).append("|").append(strs[1]).append("|")
												.toString());
										totalHuxi -= 3;
									}
								} else {
									replaceCards.set(i,
											new StringBuilder(5).append("|").append(strs[0]).append("|").toString());
									count--;
									reCheck = true;
								}
							} else {
								replaceCards.set(i,
										new StringBuilder(5).append("|").append(strs[0]).append("|").toString());
								count--;
								reCheck = true;
							}
						} else if (tempCardVal == 104
								&& LdsPhzCardUtils.loadCardVal(Integer.parseInt(strs[0])) == 101) {
							if (totalHuxi - 6 >= phzBase.loadQihuxi()) {
								if (phzBase.loadCommonTun(totalHuxi - 6)
										+ phzBase.loadXingTun(count) >= phzBase.loadCommonTun(totalHuxi)
												+ phzBase.loadXingTun(count101)) {
									LdsPhzCardMessage cardMessage = checkCmMap.get(i + ",104");
									if (cardMessage != null) {
										cardMessage.setCheckHuxi(false);
										replaceCards.set(i, new StringBuilder(5).append("|").append(strs[1]).append("|")
												.toString());
										totalHuxi -= 6;
									}
								} else {
									replaceCards.set(i,
											new StringBuilder(5).append("|").append(strs[0]).append("|").toString());
									count--;
									reCheck = true;
								}
							} else {
								replaceCards.set(i,
										new StringBuilder(5).append("|").append(strs[0]).append("|").toString());
								count--;
								reCheck = true;
							}
						} else {
							replaceCards.set(i,
									new StringBuilder(5).append("|").append(strs[1]).append("|").toString());
						}
					}
				}

				if (reCheck) {
					return loadValCount(val);
				}
			} else {
				int val0 = LdsPhzCardUtils.loadXingVal(val, 1);
				for (LdsPhzCardMessage cm : getCardMessageList()) {
					count += LdsPhzCardUtils.loadIdsByVal(cm.getCards(), val0).size();
				}

				int wangRepSize = LdsPhzCardUtils.loadIdsByVal(loadReplaceCards(val0), val0).size();
				count += wangRepSize;

				if (wangRepSize == 0) {
					for (LdsPhzCardMessage cm : getCardMessageList()) {

						boolean allWang = LdsPhzCardUtils.isAllWang(cm.getCards());
						if (allWang) {
							int size = cm.getCards().size();
							
							HashMap<String, Integer> cardMap = new HashMap<String, Integer>();
							for (String str : replaceCards) {
								if (cardMap.get(str) == null) {
									cardMap.put(str, 1);
								} else {
									cardMap.put(str, cardMap.get(str) + 1);
								}
							}

							String key = null;
							int num = 0;
							for (Map.Entry<String, Integer> entry : cardMap.entrySet()) {
								if (num < entry.getValue()) {
									key = entry.getKey();
									num = entry.getValue();
								}
							}
							if(num>size) {
								num = size;
							}
							for (int i = 0; i < num; i++) {
								replaceCards.remove(key);
								replaceCards.add("|" + LdsPhzCardUtils.getIdByVaule(val) + "|");
							}
							count += size;
						}
					}
				}

				boolean reCheck = false;
				// if (val0 == 4 && checkCmMap.size() > 0) {
				// Iterator<Map.Entry<String, LdsPhzCardMessage>> its =
				// checkCmMap.entrySet().iterator();
				// while (its.hasNext()) {
				// Map.Entry<String, LdsPhzCardMessage> kv = its.next();
				// if (kv.getKey().endsWith(",4")) {
				// if (totalHuxi - 3 >= phzBase.loadQihuxi()) {
				// if (phzBase.loadCommonTun(totalHuxi - 3) +
				// phzBase.loadXingTun(count) >=
				// phzBase.loadCommonTun(totalHuxi)) {
				// replaceCards.set(Integer.parseInt(kv.getKey().substring(0,
				// kv.getKey().indexOf(","))), "|4|");
				// kv.getValue().setCheckHuxi(false);
				// totalHuxi -= 3;
				// } else {
				// replaceCards.set(Integer.parseInt(kv.getKey().substring(0,
				// kv.getKey().indexOf(","))), "|1|");
				// count--;
				// reCheck = true;
				// }
				// } else {
				// replaceCards.set(Integer.parseInt(kv.getKey().substring(0,
				// kv.getKey().indexOf(","))), "|1|");
				// count--;
				// reCheck = true;
				// }
				// its.remove();
				// }
				// }
				// } else if (val0 == 104 && checkCmMap.size() > 0) {
				// Iterator<Map.Entry<String, LdsPhzCardMessage>> its =
				// checkCmMap.entrySet().iterator();
				// while (its.hasNext()) {
				// Map.Entry<String, LdsPhzCardMessage> kv = its.next();
				// if (kv.getKey().endsWith(",104")) {
				// if (totalHuxi - 6 >= phzBase.loadQihuxi()) {
				// if (phzBase.loadCommonTun(totalHuxi - 6) +
				// phzBase.loadXingTun(count) >=
				// phzBase.loadCommonTun(totalHuxi)) {
				// replaceCards.set(Integer.parseInt(kv.getKey().substring(0,
				// kv.getKey().indexOf(","))), "|54|");
				// kv.getValue().setCheckHuxi(false);
				// totalHuxi -= 6;
				// } else {
				// replaceCards.set(Integer.parseInt(kv.getKey().substring(0,
				// kv.getKey().indexOf(","))), "|51|");
				// count--;
				// reCheck = true;
				// }
				// } else {
				// replaceCards.set(Integer.parseInt(kv.getKey().substring(0,
				// kv.getKey().indexOf(","))), "|51|");
				// count--;
				// reCheck = true;
				// }
				//
				// its.remove();
				// }
				// }
				// }

				if (reCheck) {
					return loadValCount(val);
				}
			}
		}

		return count;
	}

	public List<Integer> loadReplaceCards0() {
		List<Integer> retList = new ArrayList<>();
		for (String str : getReplaceCards()) {
			String[] strs = str.split("\\|");
			for (String s : strs) {
				if (s.length() > 0) {
					retList.add(Integer.parseInt(s));
				}
			}
		}
		return retList;
	}

	public List<Integer> loadReplaceCards(int val) {
		List<Integer> retList = new ArrayList<>();
		for (String str : getReplaceCards()) {
			String[] strs = str.split("\\|");
			boolean ok = false;
			if (val > 0 && val < 200) {
				for (String s : strs) {
					if (s.length() > 0) {
						if (val == LdsPhzCardUtils.loadCardVal(Integer.parseInt(s))) {
							ok = true;
							break;
						}
					}
				}
			}
			if (ok) {
				int card = (val >= 1 && val <= 10) ? val : ((val >= 101 && val <= 110) ? (40 + (val % 100)) : 0);
				retList.add(card);
			} else {
				retList.add(Integer.parseInt(str.substring(1, str.indexOf("|", 1))));
			}
		}
		return retList;
	}

	public int getTotalTun() {
		return totalTun;
	}

	public void setTotalTun(int totalTun) {
		this.totalTun = totalTun;
	}
}
