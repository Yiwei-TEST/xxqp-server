package com.sy599.game.qipai.pdkuai.tool;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONArray;
import com.sy599.game.qipai.pdkuai.bean.PdkPlayer;
import com.sy599.game.qipai.pdkuai.bean.PdkTable;
import com.sy599.game.qipai.pdkuai.util.CardUtils;
import com.sy599.game.qipai.pdkuai.util.CardValue;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.text.html.parser.Entity;

/**
 * 规则 查询牌型 等等
 *
 * @author lc
 */
public class CardTypeTool {

	/**
	 * @param from
	 *            是自己已有的牌
	 * @param oppo
	 *            对手出的牌
	 * @return
	 */
	public static List<Integer> canPlay(List<Integer> from, List<Integer> oppo, boolean isDisCards, PdkPlayer player,
			PdkTable table) {
		return CardTypeTool.getBestAI2(from, oppo, false, table);
	}


	/**
	 * 接牌或出牌
	 *
	 * @param curList
	 *            当前手牌
	 * @param oppo
	 *            要去接的牌，为空时表示自己第一个出牌
	 * @param nextDan
	 *            下家是否报单
	 * @param table
	 *            牌桌
	 * @return
	 */
	public static List<Integer> getBestAI2(List<Integer> curList, List<Integer> oppo, boolean nextDan, PdkTable table) {
		if (curList == null || curList.size() == 0) {
			return Collections.emptyList();
		}

		List<Integer> retList = new ArrayList<>();
		Map<Integer, Integer> map = CardTool.loadCards(curList);
		int val = 0;
		int count = map.size();

		if (oppo == null || oppo.size() == 0) {
			if (count == 1) {
				retList.addAll(curList);
				return retList;
			}

			int size = curList.size();
			switch (size) {
			case 2:
				if (nextDan) {
					for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
						val = kv.getKey().intValue();
					}
					retList.add(CardTool.loadCards(curList, val).get(0));
				} else {
					for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
						val = kv.getKey().intValue();
						break;
					}
					retList.add(CardTool.loadCards(curList, val).get(0));
				}
				break;
			case 3:
				for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
					if (kv.getValue().intValue() == 3) {
						retList.addAll(curList);
						break;
					}
				}
				for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
					if (kv.getValue().intValue() == 2) {
						val = kv.getKey().intValue();
						retList.addAll(CardTool.loadCards(curList, val));
						break;
					}
				}
				if (retList.size() == 0) {
					if (nextDan) {
						for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
							val = kv.getKey().intValue();
						}
						retList.add(CardTool.loadCards(curList, val).get(0));
					} else {
						for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
							val = kv.getKey().intValue();
							break;
						}
						retList.add(CardTool.loadCards(curList, val).get(0));
					}
				}
				break;
			case 4:
				for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
					if (kv.getValue().intValue() == 3) {
						if (kv.getKey().intValue() == 14 && table.getAAAZha() == 1) {
							if (nextDan) {
								val = kv.getKey().intValue();
								retList.addAll(CardTool.loadCards(curList, val));
							} else {
								for (Map.Entry<Integer, Integer> tmp : map.entrySet()) {
									if (tmp.getKey().intValue() != 14) {
										val = tmp.getKey().intValue();
										break;
									}
								}
								retList.addAll(CardTool.loadCards(curList, val));
							}
						} else {
							retList.addAll(curList);
							break;
						}
					}
				}
				if (retList.size() == 0) {
					for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
						if (kv.getValue().intValue() == 2) {
							val = kv.getKey().intValue();
							retList.addAll(CardTool.loadCards(curList, val));
							break;
						}
					}
					if (retList.size() == 0) {
						if (nextDan) {
							for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
								val = kv.getKey().intValue();
							}
							retList.add(CardTool.loadCards(curList, val).get(0));
						} else {
							for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
								val = kv.getKey().intValue();
								break;
							}
							retList.add(CardTool.loadCards(curList, val).get(0));
						}
					}
				}
				break;
			case 5:
				if (count == 2 || count == 3) {
					for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
						if (kv.getValue().intValue() == 3) {
							if (kv.getKey().intValue() == 14 && table.getAAAZha() == 1) {
								if (nextDan) {
									val = kv.getKey().intValue();
									retList.addAll(CardTool.loadCards(curList, val));
								} else {
									for (Map.Entry<Integer, Integer> tmp : map.entrySet()) {
										if (tmp.getKey().intValue() != 14) {
											val = tmp.getKey().intValue();
											break;
										}
									}
									retList.addAll(CardTool.loadCards(curList, val));
								}
							} else {
								retList.addAll(curList);
								break;
							}
						} else if (kv.getValue().intValue() == 4) {
							if (nextDan) {
								val = kv.getKey().intValue();
								retList.addAll(CardTool.loadCards(curList, val));
							} else {
								for (Map.Entry<Integer, Integer> tmp : map.entrySet()) {
									if (tmp.getKey().intValue() != kv.getKey().intValue()) {
										val = tmp.getKey().intValue();
										break;
									}
								}
								retList.addAll(CardTool.loadCards(curList, val));
							}
							break;
						}
					}
					if (retList.size() == 0) {
						if (nextDan) {
							int maxVal = 0;
							for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
								val = kv.getKey().intValue();
								if (kv.getValue() == 2) {
									retList.addAll(CardTool.loadCards(curList, val));
									return retList;
								}
								if (val > maxVal)
									maxVal = val;
							}
							val = maxVal;
						} else {
							for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
								val = kv.getKey().intValue();
								break;
							}
						}
						retList.add(CardTool.loadCards(curList, val).get(0));
					}
				} else if (count == 5) {
					boolean isShun = true;
					int pre = 0;
					int current = 0;
					for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
						val = kv.getKey().intValue();
						if (current == 0) {
							current = val;
						} else {
							if (pre == 0) {
								pre = current;
							}
							current = val;
							if (current >= 15 || current - pre != 1) {
								isShun = false;
								break;
							}
						}
					}

					if (isShun) {
						retList.addAll(curList);
					} else {
						for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
							if (kv.getValue().intValue() == 2) {
								val = kv.getKey().intValue();
								retList.addAll(CardTool.loadCards(curList, val));
								break;
							}
						}
						if (retList.size() == 0) {
							if (nextDan) {
								for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
									val = kv.getKey().intValue();
								}
								retList.add(CardTool.loadCards(curList, val).get(0));
							} else {
								for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
									val = kv.getKey().intValue();
									break;
								}
								retList.add(CardTool.loadCards(curList, val).get(0));
							}
						}
					}
				} else {
					for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
						if (kv.getValue().intValue() == 2) {
							val = kv.getKey().intValue();
							retList.addAll(CardTool.loadCards(curList, val));
							break;
						}
					}
					if (retList.size() == 0) {
						if (nextDan) {
							for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
								val = kv.getKey().intValue();
							}
							retList.add(CardTool.loadCards(curList, val).get(0));
						} else {
							for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
								val = kv.getKey().intValue();
								break;
							}
							retList.add(CardTool.loadCards(curList, val).get(0));
						}
					}
				}
				break;
			default:
				if (count == size) {
					boolean isShun = true;
					int pre = 0;
					int current = 0;
					for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
						val = kv.getKey().intValue();
						if (current == 0) {
							current = val;
						} else {
							if (pre == 0) {
								pre = current;
							}
							current = val;
							if (current >= 15 || current - pre != 1) {
								isShun = false;
								break;
							}
						}
					}
					if (isShun) {
						retList.addAll(curList);
					}
				} else if (count * 2 == size) {
					boolean isShun = true;
					int pre = 0;
					int current = 0;
					for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
						if (kv.getValue().intValue() != 2) {
							isShun = false;
							break;
						}
						val = kv.getKey().intValue();
						if (current == 0) {
							current = val;
						} else {
							if (pre == 0) {
								pre = current;
							}
							current = val;
							if (current >= 15 || current - pre != 1) {
								isShun = false;
								break;
							}
						}
					}
					if (isShun) {
						retList.addAll(curList);
					}
				}

				val = 0;
				if (retList.size() == 0) {
					for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
						if (kv.getValue().intValue() == 3) {
							if (kv.getKey().intValue() == 14) {
								if (table.getAAAZha() != 1) {
									val = kv.getKey().intValue();
									break;
								}
							} else {
								val = kv.getKey().intValue();
								break;
							}
						}
					}
					if (val > 0) {
						List<Integer> daiValues = new ArrayList<>();
						for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
							if (kv.getValue().intValue() == 1) {
								daiValues.add(kv.getKey().intValue());
							}
						}
						if (daiValues.size() >= 2) {
							retList.addAll(CardTool.loadCards(curList, val));
							retList.add(CardTool.loadCards(curList, daiValues.get(0).intValue()).get(0));
							retList.add(CardTool.loadCards(curList, daiValues.get(1).intValue()).get(0));
						} else {
							for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
								if (kv.getValue().intValue() == 2) {
									daiValues.add(kv.getKey().intValue());
								}
							}
							if (daiValues.size() >= 2) {
								retList.addAll(CardTool.loadCards(curList, val));
								retList.add(CardTool.loadCards(curList, daiValues.get(0).intValue()).get(0));
								retList.add(CardTool.loadCards(curList, daiValues.get(1).intValue()).get(0));
							} else {
								for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
									int val0 = kv.getKey().intValue();
									if (kv.getValue().intValue() == 3 && val != val0) {
										if (kv.getKey().intValue() == 14) {
											if (table.getAAAZha() != 1) {
												daiValues.add(val0);
											}
										} else {
											daiValues.add(val0);
										}
									}
								}

								if (daiValues.size() < 2) {
									for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
										if (kv.getValue().intValue() == 4) {
											retList = CardTool.loadCards(curList, kv.getKey().intValue());
										}
									}
								}

								if (retList.size() == 0) {
									retList.addAll(CardTool.loadCards(curList, val));
									if (daiValues.size() > 1) {
										if (curList.size() == 4 || curList.size() == 5) {
											retList = new ArrayList<>(curList);
										} else {
											List<Integer> daiPais0 = CardTool.loadCards(curList,
													daiValues.get(0).intValue());
											if (daiPais0.size() >= 2) {
												retList.add(daiPais0.get(0));
												retList.add(daiPais0.get(1));
											} else {
												if (daiValues.size() > 1) {
													retList.add(daiPais0.get(0));
													retList.add(CardTool.loadCards(curList, daiValues.get(1).intValue())
															.get(0));
												}
											}
										}
									}
								}
							}
						}
					}

					if (retList.size() == 0) {
						val = 0;
						for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
							if (kv.getValue().intValue() == 2) {
								val = kv.getKey().intValue();
								break;
							}
						}
						if (val == 0) {
							for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
								if (kv.getValue().intValue() == 3) {
									if (kv.getKey().intValue() == 14) {
										if (table.getAAAZha() != 1) {
											val = kv.getKey().intValue();
											break;
										}
									} else {
										val = kv.getKey().intValue();
										break;
									}
								}
							}
						}
						if (val > 0) {
							List<Integer> list0 = CardTool.loadCards(curList, val);
							retList.add(list0.get(0));
							retList.add(list0.get(1));
						} else {
							if (nextDan) {
								for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
									if (kv.getValue().intValue() == 4) {
										val = kv.getKey().intValue();
										retList.addAll(CardTool.loadCards(curList, val));
										break;
									} else {
										val = kv.getKey().intValue();
									}
								}
								if (retList.size() == 0) {
									retList.add(CardTool.loadCards(curList, val).get(0));
								}
							} else {
								// 优先不拆炸弹
								for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
									if (kv.getValue() != 4
											&& !(kv.getKey() == 14 && kv.getValue() == 3 && table.getAAAZha() == 1)) {
										if (val == 0) {
											val = kv.getKey().intValue();
										} else if (val > kv.getKey().intValue()) {
											val = kv.getKey().intValue();
										}
									}
								}
								if (val == 0) {
									for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
										// if(table.getChai4Zha()==0&&kv.getValue()==4)
										// continue;
										// if(kv.getKey()==14&&kv.getValue()==3&&table.getAAAZha()==1&&table.getChai4Zha()==0)
										// continue;
										val = kv.getKey().intValue();
										break;
									}
								}
								retList.add(CardTool.loadCards(curList, val).get(0));
							}
						}
					}
				}
			}
		} else {
			if (nextDan && oppo.size() == 1) {
				for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
					if (kv.getValue().intValue() == 4) {
						val = kv.getKey().intValue();
						retList.addAll(CardTool.loadCards(curList, val));
						break;
					} else if (kv.getValue().intValue() == 3 && kv.getKey().intValue() == 14
							&& table.getAAAZha() == 1) {
						val = kv.getKey().intValue();
						retList.addAll(CardTool.loadCards(curList, val));
						break;
					} else {
						val = kv.getKey().intValue();
					}
				}
				if (retList.size() == 0 && val > CardTool.loadCardValue(oppo.get(0))) {
					retList.add(CardTool.loadCards(curList, val).get(0));
				}
			} else {
				CardUtils.Result result = CardUtils.calcCardValue(CardUtils.loadCards(oppo), table.getSiDai(),
						table.getIsFirstCardType32() == 1, table.getAAAZha() == 1);
				if (result.getType() > 0) {
					List<CardValue> cardValueList = CardUtils.searchBiggerCardValues(CardUtils.loadCards(curList),
							result, table.getAAAZha() == 1);
					if (cardValueList != null && cardValueList.size() > 0) {
						result = CardUtils.calcCardValue(cardValueList, table.getSiDai(),
								table.getIsFirstCardType32() == 1, table.getAAAZha() == 1);
						if ((result.getType() == 11 || result.getType() == 22 || result.getType() == 33)
								&& result.getMax() >= 15) {
							return retList;
						} else {
							retList = CardUtils.loadCardIds(cardValueList);
						}
					}
				}
			}
		}

		return retList;
	}


	/**
	 * 接牌或出牌 相比getBestAI2， 简化自己出牌逻辑为：出最小单张
	 *
	 * @param curList
	 *            当前手牌
	 * @param oppo
	 *            要去接的牌，为空时表示自己第一个出牌
	 * @param nextDan
	 *            下家是否报单
	 * @param table
	 *            牌桌
	 * @return
	 */
	public static List<Integer> getBestAI3(List<Integer> curList, List<Integer> oppo, boolean nextDan, PdkTable table) {
		if (curList == null || curList.size() == 0) {
			return Collections.emptyList();
		}

		List<Integer> retList = new ArrayList<>();
		Map<Integer, Integer> map = CardTool.loadCards(curList);
		int val = 0;
		int count = map.size();

		if (oppo == null || oppo.size() == 0) {
			if (count == 1) {
				retList.addAll(curList);
				return retList;
			}
			//出最小单张
			retList.add(curList.get(curList.size() - 1));
		} else {
			if (nextDan && oppo.size() == 1) {
				for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
					if (kv.getValue().intValue() == 4) {
						val = kv.getKey().intValue();
						retList.addAll(CardTool.loadCards(curList, val));
						break;
					} else if (kv.getValue().intValue() == 3 && kv.getKey().intValue() == 14
							&& table.getAAAZha() == 1) {
						val = kv.getKey().intValue();
						retList.addAll(CardTool.loadCards(curList, val));
						break;
					} else {
						val = kv.getKey().intValue();
					}
				}
				if (retList.size() == 0 && val > CardTool.loadCardValue(oppo.get(0))) {
					retList.add(CardTool.loadCards(curList, val).get(0));
				}
			} else {
				CardUtils.Result result = CardUtils.calcCardValue(CardUtils.loadCards(oppo), table.getSiDai(),
						table.getIsFirstCardType32() == 1, table.getAAAZha() == 1);
				if (result.getType() > 0) {
					List<CardValue> cardValueList = CardUtils.searchBiggerCardValues(CardUtils.loadCards(curList),
							result, table.getAAAZha() == 1);
					if (cardValueList != null && cardValueList.size() > 0) {
						result = CardUtils.calcCardValue(cardValueList, table.getSiDai(),
								table.getIsFirstCardType32() == 1, table.getAAAZha() == 1);
						if ((result.getType() == 11 || result.getType() == 22 || result.getType() == 33)
								&& result.getMax() >= 15) {
							return retList;
						} else {
							retList = CardUtils.loadCardIds(cardValueList);
						}
					}
				}
			}
		}

		return retList;
	}

	public static final int px_danpai = 1;//绝对单牌（一张不能组成顺子的牌）
	public static final int px_duizi = 2;//所有对子
	public static final int px_santiao = 3;//所有3个的
	public static final int px_shunzi = 4;//顺子 不连续得顺子，不会拆对来做2条顺子
	public static final int px_liandui = 5;//连对
	public static final int px_feiji = 6;//连3条（飞机）
	public static final int px_zhadan = 0;//炸弹
	
	/**
	 * 根据牌型获取牌的数量
	 * @param type
	 * @param pais
	 * @return
	 */
	public static Integer getPaiNumByPaiXing(int type,List<Integer> pais){
		switch (type) {
		case px_danpai:
			return 1;
		case px_duizi:
			return 2;
		case px_santiao:
			return 5;
		case px_shunzi:
			return pais.isEmpty()?0:pais.size();
		case px_liandui:
			return pais.isEmpty()?0:pais.size()*2;
		case px_feiji:
			return pais.isEmpty()?0:pais.size()*3+pais.size()*2;
		case px_zhadan:
			return 4;
		default:
			break;
		}
		return 0;
	}
	
	/**
	 * 根据牌获取牌型
	 * @param type
	 * @param pais
	 * @param handCards
	 * @return
	 */
	public static List<Integer> getPaisByPaiXing(int type,List<Integer> pais,List<Integer> handCards,boolean...bs){
		if(pais.isEmpty() || handCards.isEmpty()){
			return new ArrayList<>();
		}
		List<Integer> copyHandCards = new ArrayList<>(handCards);
		Collections.sort(pais);
		switch (type) {
		case px_danpai:
			return getCardByCardVal(handCards, pais.get(0),1);
		case px_duizi:
			return getCardByCardVal(handCards, pais.get(0),2);
		case px_santiao:
			List<Integer> stCards = new ArrayList<>();
			stCards.addAll(getCardByCardVal(copyHandCards, pais.get(0),3));
			copyHandCards.removeAll(stCards);
			if(bs==null || bs.length<=0){
				int stCount = 2;
//				Collections.sort(copyHandCards, new Comparator<Integer>() {
//					 @Override
//		             public int compare(Integer i, Integer j) {
//		                 return j % 100 >= i%100 ? -1:1;
//		             }
//				});
//				stCards.addAll(copyHandCards.subList(0, copyHandCards.size() > stCount?stCount: copyHandCards.size()));//带2张小牌
				stCards.addAll(getStCards(copyHandCards, stCount));
			}
			return stCards;
		case px_shunzi:
			List<Integer> szCards = new ArrayList<>();
			for (Integer card : pais) {
				szCards.addAll(getCardByCardVal(handCards, card,1));
			}
			return szCards;
		case px_liandui:
			List<Integer> ldCards = new ArrayList<>();
			for (Integer card : pais) {
				ldCards.addAll(getCardByCardVal(handCards, card,2));
			}
			return ldCards;
		case px_feiji:
			List<Integer> fjCards = new ArrayList<>();
			for (Integer card : pais) {
				fjCards.addAll(getCardByCardVal(handCards, card,3));
			}
			copyHandCards.removeAll(fjCards);
			if(bs==null || bs.length<=0){
				int fjCount = pais.size() * 2;
//				Collections.sort(copyHandCards, new Comparator<Integer>() {
//					 @Override
//		             public int compare(Integer i, Integer j) {
//		                 return j % 100 >= i%100 ? -1:1;
//		             }
//				});
//				fjCards.addAll(copyHandCards.subList(0, copyHandCards.size() > fjCount?fjCount: copyHandCards.size()));//带2张小牌
				fjCards.addAll(getStCards(copyHandCards, fjCount));
			}
			
			return fjCards;
		case px_zhadan:
			return getCardByCardVal(handCards, pais.get(0),4);
		default:
			break;
		}
		return new ArrayList<>();
	}
	
	/**
	 * 获取3条要带的牌
	 * @return
	 */
	private static List<Integer> getStCards(List<Integer> handCards,int num){
		List<Integer> daiValues = new ArrayList<>();
		if(handCards.size() <= num){
			return handCards;
		}
//		Map<Integer, List<List<Integer>>> allPaixing = CardTypeTool.getAllPaiXing(handCards,false);
//		List<Integer> allShunzi = new ArrayList<>();//顺子涉及到的所有牌
//		if(!allPaixing.isEmpty()){
//			List<List<Integer>> shunzi = allPaixing.get(CardTypeTool.px_shunzi);
//			if(!shunzi.isEmpty()){
//				for (List<Integer> sz : shunzi) {
//					if(sz.isEmpty()){
//						continue;
//					}
//					allShunzi.addAll(sz);
//				}
//			}
//		}
		
		Map<Integer, Integer> map = CardTool.loadCards(handCards);
		List<Integer> copyHandCards = new ArrayList<>(handCards);
		for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
			//不带A2
			if (kv.getValue().intValue() == 1 && kv.getKey() < 14) {
				daiValues.addAll(getCardByCardVal(copyHandCards, kv.getKey().intValue(), 1));
				copyHandCards.removeAll(getCardByCardVal(copyHandCards, kv.getKey().intValue(), 1));
			}
			if(daiValues.size() == num){
				return daiValues;
			}
		}
		for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
			//不带A2
			if (kv.getValue().intValue() == 2 && kv.getKey() < 14) {
//				daiValues.add(kv.getKey().intValue());
//				daiValues.add(kv.getKey().intValue());
				daiValues.addAll(getCardByCardVal(copyHandCards, kv.getKey().intValue(), 2));
				copyHandCards.removeAll(getCardByCardVal(copyHandCards, kv.getKey().intValue(), 2));
			}
			if(daiValues.size() == num){
				return daiValues;
			}
			if(daiValues.size() > num){
				return daiValues.subList(0, num);
			}
		}
		
		if(daiValues.size() < num){
//			List<Integer> cardVals = CardTool.getCardsVals(copyHandCards);
//			Collections.sort(cardVals);
			Collections.sort(copyHandCards, new Comparator<Integer>() {
				 @Override
	             public int compare(Integer i, Integer j) {
	                 return j % 100 >= i%100 ? -1:1;
	             }
			});
			int needNum = num-daiValues.size();
			daiValues.addAll(copyHandCards.subList(0, needNum));
		}

		return daiValues;
			
	}
	
	/**
	 * 移除牌型
	 * @param type
	 * @param pais
	 * @param handCards
	 * @return
	 */
	public static List<Integer> removePaisByPaiXing(int type,List<Integer> pais,List<Integer> handCards){
		if(pais.isEmpty()){
			return handCards;
		}
		if(handCards.isEmpty()){
			return new ArrayList<>();
		}
		List<Integer> copyHandCards = new ArrayList<>(handCards);
		Collections.sort(pais);
		switch (type) {
		case px_danpai:
			copyHandCards.removeAll(getCardByCardVal(copyHandCards, pais.get(0),1));
			return copyHandCards;
		case px_duizi:
			copyHandCards.removeAll(getCardByCardVal(copyHandCards, pais.get(0),2));
			return copyHandCards;
		case px_santiao:
			List<Integer> stCards = new ArrayList<>();
			stCards.addAll(getCardByCardVal(copyHandCards, pais.get(0),3));
			copyHandCards.removeAll(stCards);
			int stCount = 2;
			Collections.sort(copyHandCards, new Comparator<Integer>() {
				@Override
				public int compare(Integer i, Integer j) {
					return j % 100 >= i%100 ? -1:1;
				}
			});
			stCards.addAll(copyHandCards.subList(0, copyHandCards.size() > stCount?stCount: copyHandCards.size()));//带2张小牌
			copyHandCards.removeAll(stCards);
			return copyHandCards;
		case px_shunzi:
			List<Integer> szCards = new ArrayList<>();
			for (Integer card : pais) {
				szCards.addAll(getCardByCardVal(handCards, card,1));
			}
			copyHandCards.removeAll(szCards);
			return copyHandCards;
		case px_liandui:
			List<Integer> ldCards = new ArrayList<>();
			for (Integer card : pais) {
				ldCards.addAll(getCardByCardVal(handCards, card,2));
			}
			copyHandCards.removeAll(ldCards);
			return copyHandCards;
		case px_feiji:
			List<Integer> fjCards = new ArrayList<>();
			for (Integer card : pais) {
				fjCards.addAll(getCardByCardVal(handCards, card,3));
			}
			copyHandCards.removeAll(fjCards);
			int fjCount = pais.size() * 2;
			Collections.sort(copyHandCards, new Comparator<Integer>() {
				@Override
				public int compare(Integer i, Integer j) {
					return j % 100 >= i%100 ? -1:1;
				}
			});
			fjCards.addAll(copyHandCards.subList(0, copyHandCards.size() > fjCount?fjCount: copyHandCards.size()));//带2张小牌
			copyHandCards.removeAll(fjCards);
			return copyHandCards;
//			return fjCards;
		case px_zhadan:
			copyHandCards.removeAll(getCardByCardVal(handCards, pais.get(0),4));
			return copyHandCards;
		default:
			break;
		}
		return copyHandCards;
	}
	
	/**
	 * 获得所有的基本牌型
	 * @param handCards
	 * @return
	 */
	public static Map<Integer, List<List<Integer>>> getAllPaiXing(List<Integer> handCards,boolean isResiList) {
//		if (handCards.size() < 7) {// 手牌大于7张时，找牌数最多得牌型来出
//			return null;
//		}
		Map<Integer, List<List<Integer>>> paiMap = new HashMap<>();
		List<Integer> copyHandCard = new ArrayList<>(handCards);// 其他人剩下的牌
		Map<Integer, Integer> map = CardTool.loadCards(copyHandCard);
		int count = map.size();// 牌的数量
		// 顺子个数
		List<List<Integer>> allShunzi = new ArrayList<>();
		List<Integer> shunzi = new ArrayList<>();
		List<Integer> allSan = new ArrayList<>();
		List<List<Integer>> allLianSan = new ArrayList<>();
		List<Integer> allDuizi = new ArrayList<>();
		List<Integer> allZhadan = new ArrayList<>();
		List<List<Integer>> allLianDui = new ArrayList<>();
		List<Integer> liandui = new ArrayList<>();
		List<Integer> lianSan = new ArrayList<>();
		int shunCurVal = 0;
		int duiCurVal = 0;
		int sanCurVal = 0;
		int i = 0;
		for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
			i++;
			int cardVal = kv.getKey().intValue();
			//順子
			if (shunCurVal == 0) {
				shunCurVal = cardVal;
				shunzi.add(cardVal);
			} else {
				if (shunCurVal + 1 == cardVal) {// 15是2
					if (cardVal != 15 && (isResiList || kv.getValue().intValue() != 4)) {//炸弹不拆成顺子
						shunCurVal = cardVal;
						shunzi.add(cardVal);
					}
				} else {
					if (shunzi.size() >= 5) {
						allShunzi.add(new ArrayList<>(shunzi));
						shunzi.clear();
					}
					shunCurVal = cardVal;
					shunzi.clear();
					shunzi.add(cardVal);
				}
				
			}
			//三个的
			if(kv.getValue().intValue() == 3){
//				allSan.add(getCardByCardVal(handCards, kv.getKey()));
				allSan.add(kv.getKey());
				if(sanCurVal == 0){
					sanCurVal = cardVal;
					lianSan.add(cardVal);
				}else {
					if (sanCurVal + 1 == cardVal) {// 15是2
						if (cardVal != 15) {
							sanCurVal = cardVal;
							lianSan.add(cardVal);
						}
					} else {
						if (lianSan.size() >= 2) {
							allLianSan.add(new ArrayList<>(lianSan));
							lianSan.clear();
						}
						sanCurVal = cardVal;
						lianSan.clear();
						lianSan.add(cardVal);
					}
				}
			}
			//对子
			if(kv.getValue().intValue() == 2 || kv.getValue().intValue() == 3 || (isResiList&&kv.getValue().intValue() == 4)){//不计炸弹，炸弹不拆成对
//				List<Integer> duiList = getCardByCardVal(handCards, kv.getKey());
//				if(duiList.size()>2){
//					allDuizi.add(duiList.subList(0, 2));
//				}else{
//					allDuizi.add(duiList);
//				}
				allDuizi.add(kv.getKey());
				if(duiCurVal == 0){
					duiCurVal = cardVal;
					liandui.add(cardVal);
				}else {
					if (duiCurVal + 1 == cardVal) {// 15是2
						if (cardVal != 15) {
							duiCurVal = cardVal;
							liandui.add(cardVal);
						}
					} else {
						if (liandui.size() >= 2) {
							allLianDui.add(new ArrayList<>(liandui));
							liandui.clear();
						}
						duiCurVal = cardVal;
						liandui.clear();
						liandui.add(cardVal);
					}
				}
			}
			if (kv.getValue().intValue() >= 4) {
				allZhadan.add(kv.getKey());
			}
			if(i == count){
				if(shunzi.size() >=5){
					allShunzi.add(new ArrayList<>(shunzi));
				}
				if(liandui.size() >=2){
					allLianDui.add(new ArrayList<>(liandui));
				}
				if(lianSan.size() >=2){
					allLianSan.add(new ArrayList<>(lianSan));
				}
			}
			
		}
		//顺子处理掐头去尾
//		List<List<Integer>> finalAllShunzi = new ArrayList<>();
//		if(!allShunzi.isEmpty() && !allSan.isEmpty()){
//			for (List<Integer> sz:allShunzi) {
//				if(sz != null && sz.size() > 5){
//					int end = sz.get(sz.size()-1);
//					if(allSan.contains(end)){
//						sz.remove(sz.size()-1);
//					}
//					int begin = sz.get(0);
//					if(allSan.contains(begin)){
//						sz.remove(0);
//					} 
//					
//				}
//			}
//		}
		
		paiMap.put(px_duizi, allDuizi.isEmpty()?new ArrayList<>():Arrays.asList(allDuizi));
		paiMap.put(px_santiao, allSan.isEmpty()?new ArrayList<>():Arrays.asList(allSan));//不包含要带的牌
		paiMap.put(px_zhadan, allZhadan.isEmpty()?new ArrayList<>():Arrays.asList(allZhadan));
		paiMap.put(px_shunzi, allShunzi);
		paiMap.put(px_liandui, allLianDui);
		paiMap.put(px_feiji, allLianSan);
		Set<Integer> set = new HashSet<>();
		for (List<List<Integer>> mapVals:paiMap.values()) {
			if(!mapVals.isEmpty()){
				for (List<Integer>  mapValsList: mapVals) {
					if(!mapValsList.isEmpty()){
						set.addAll(mapValsList);
					}
				}
			}
		}
		List<Integer> danpaiList = new ArrayList<>();
		for (Integer key : map.keySet()) {
			if(!set.contains(key)){
				danpaiList.add(key);
			}
		}
		paiMap.put(px_danpai, danpaiList.isEmpty()?new ArrayList<>():Arrays.asList(danpaiList));
		return paiMap;
	}

	
	private static List<Integer> getCardByCardVal(List<Integer> handCards ,int cardVal,int count){
		List<Integer> list = new ArrayList<>();
		if(handCards.isEmpty()){
			return list;
		}
		for (Integer card : handCards) {
			if(card%100 == cardVal){
				list.add(card);
				if(list.size() == count){
					return list;
				}
			}
		}
		return list;
	}
	private static List<Integer> getCardByCardsVal(List<Integer> handCards ,List<Integer> cardVals){
		List<Integer> list = new ArrayList<>();
		if(handCards.isEmpty()){
			return list;
		}
		for (Integer card : handCards) {
			if(cardVals.contains(card%100)){
				list.add(card);
			}
		}
		return list;
	}
	/**
	 * 判断是不是最大的对子
	 * @param duiVal
	 * @return
	 */
	private static boolean isMaxDuizi(int duiVal,List<Integer> allPdkCards){
		Map<Integer, Integer> map = CardTool.loadCards(allPdkCards);
		for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
			if(kv.getValue() >= 2 && kv.getKey() > duiVal){
				return false;
			}
		}
		return true;
	}
	/**
	 * 获取我手里 对于别人来说是最大的单牌
	 * @param duiVal
	 * @return
	 */
	private static List<Integer> getMaxDanpai(List<Integer> handCards,List<Integer> allPdkCards){
		List<Integer> mapDanpai = new ArrayList<>();
		Map<Integer, Integer> map = CardTool.loadCards(allPdkCards);
		for (Integer myCard:handCards) {
			boolean isMax = true;
			for (Integer card : map.keySet()) {
				if(myCard < card){
					isMax = false;
					break;
				}
			}
			if(isMax){
				mapDanpai.add(myCard);
			}
		}
		return mapDanpai;
	}
	/**
	 * 判断是不是最大的单牌
	 * @param val
	 * @return
	 */
	private static boolean isMaxDanpai(int val,List<Integer> allPdkCards){
		Map<Integer, Integer> map = CardTool.loadCards(allPdkCards);
		for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
			if(kv.getKey() > val){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 获取所有未出现的牌（不含自己的手牌）
	 * @param handCards
	 * @param allPdkCards
	 * @return
	 */
	public static List<Integer> getAllResidueCards(List<Integer> handCards, List<Integer> allPdkCards,Map<Integer, List<List<Integer>>> playedOutCard){
		List<Integer> allOutCard = new ArrayList<>();
		List<Integer> oldPlayCard = new ArrayList<>(allPdkCards);// 其他人剩下的牌
		oldPlayCard.removeAll(handCards);
		if (!playedOutCard.isEmpty()) {
			for (List<List<Integer>> outCard : playedOutCard.values()) {
				if (!outCard.isEmpty()) {
					for (List<Integer> ll : outCard) {
						if (!ll.isEmpty()) {
							allOutCard.addAll(ll);
							oldPlayCard.removeAll(ll);
						}
					}
				}
			}
		}
		return oldPlayCard;
	}
	
	/**
	 * 根据已经出掉的牌，结合手牌获取最大牌型
	 */
	public static List<Integer> getChuPaixing(List<Integer> handCards, List<Integer> allPdkCards,Map<Integer, List<List<Integer>>> playedOutCard,boolean nextDan) {
		Collections.sort(handCards,new ComparatorList());  
		List<Integer> allOutCard = new ArrayList<>();
		List<Integer> oldPlayCard = new ArrayList<>(allPdkCards);// 其他人剩下的牌
		oldPlayCard.removeAll(handCards);
		if (!playedOutCard.isEmpty()) {
			for (List<List<Integer>> outCard : playedOutCard.values()) {
				if (!outCard.isEmpty()) {
					for (List<Integer> ll : outCard) {
						if (!ll.isEmpty()) {
							allOutCard.addAll(ll);
							oldPlayCard.removeAll(ll);
						}
					}
				}
			}
		}
		//获取所有牌型
		Map<Integer, List<List<Integer>>> allPaixing = getAllPaiXing(handCards,false);
		List<Integer> chupaiList = new ArrayList<>();
		if(handCards.size() > 7){//大于7张，按数量最多出牌 飞机->大于5张的顺子->5张顺子（三带2）->连对->对子->单牌
			List<List<Integer>> feijis = allPaixing.get(px_feiji);
			if(!feijis.isEmpty()){
				chupaiList = chuFeiji(feijis, handCards,allPdkCards,nextDan);
				if(!chupaiList.isEmpty()){
					return chupaiList;
				}
			}
			List<List<Integer>> lianduis = allPaixing.get(px_liandui);
			List<List<Integer>> shunzis = allPaixing.get(px_shunzi);
			
		}
		
		return chupaiList;
//		Map<Integer, Integer> map = CardTool.loadCards(handCards);
//		int val = 0;
//		int count = map.size();// 牌的数量
//		if (count > 5) {//
//
//		}
	}
	
	private static List<Integer> chuFeiji(List<List<Integer>> feijis,List<Integer> handCards,List<Integer> allPdkCards,boolean nextDan){
		if(feijis.isEmpty()){
			return null;
		}
		List<Integer> chupaiList = new ArrayList<>();
		//有飞机,出大飞机
		List<Integer> feiji = feijis.get(feijis.size() -1);
		for (Integer card:handCards) {
			if(feiji.contains(CardTool.loadCardValue(card))){
				chupaiList.add(card);
			}
		}
		int daipaiCount = feiji.size() * 2;//带牌数量
		List<Integer> residueCards = new ArrayList<>(handCards);
		residueCards.removeAll(chupaiList);
		//剩余牌牌型
		Map<Integer, List<List<Integer>>> residuePaixing = getAllPaiXing(residueCards,false);
		List<Integer> copyResidueCards = new ArrayList<>(residueCards);
//		//剩下的牌 减去要带的牌数量，有没有能一手出完的牌型
//		for (Entry<Integer, List<List<Integer>>> paixing : residuePaixing.entrySet()) {
//			if(paixing.getKey() == px_danpai){//单牌就不管了
//				continue;
//			}
//		}
		//剩下的牌数
		int remainCard = residueCards.size() - daipaiCount;
		List<List<Integer>> zhadans = residuePaixing.get(px_zhadan);
		if(!zhadans.isEmpty()){
			List<Integer> allZhadan = zhadans.get(0);
			//飞机带完牌之后 手里只有炸弹的情况下 先出炸弹
			if(remainCard <= allZhadan.size() * 4){
				chupaiList.clear();
				chupaiList.addAll(getCardByCardVal(handCards, allZhadan.get(0),4));
				return chupaiList;
			}
			//出完飞机  有炸弹的情况下最多还剩下1张牌或2张牌
			if(remainCard - allZhadan.size() * 4 == 1){
				List<Integer> zhadanCards = getCardByCardVal(residueCards, allZhadan.get(0),4);
				copyResidueCards.removeAll(zhadanCards);
				chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
				return chupaiList;
			}else if(remainCard - allZhadan.size() * 4 == 2){
				List<List<Integer>> duizis = residuePaixing.get(px_duizi);
				if(duizis.isEmpty()){
					List<Integer> zhadanCards = getCardByCardVal(residueCards, allZhadan.get(0),4);
					copyResidueCards.removeAll(zhadanCards);
					chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
					return chupaiList;
				}else{
					List<Integer> zhadanCards = getCardByCardVal(residueCards, allZhadan.get(0),4);
					copyResidueCards.removeAll(zhadanCards);
					List<Integer> allDuizi =  duizis.get(0);
					List<Integer> duizi = getCardByCardVal(residueCards, allDuizi.get(allDuizi.size()-1),2);
					copyResidueCards.removeAll(duizi);
					chupaiList.addAll(copyResidueCards);
					return chupaiList;
				}
			}
		}
		
		if(remainCard == 1){
			chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
			return chupaiList;
		}else if(remainCard == 2){
			List<List<Integer>> duizis = residuePaixing.get(px_duizi);
			if(duizis.isEmpty()){
				chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
				return chupaiList;
			}else{
				List<Integer> allDuizi =  duizis.get(0);
				List<Integer> duizi = getCardByCardVal(residueCards, allDuizi.get(allDuizi.size()-1),2);
				copyResidueCards.removeAll(duizi);
				chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
				return chupaiList;
			}
		}else if(remainCard == 3){
			List<List<Integer>> santiaos = residuePaixing.get(px_santiao);
			if(santiaos.isEmpty()){
				List<List<Integer>> duizis = residuePaixing.get(px_duizi);
				if(duizis.isEmpty()){
					chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
					return chupaiList;
				}else{
					List<Integer> allDuizi =  duizis.get(0);
					List<Integer> duizi = getCardByCardVal(residueCards, allDuizi.get(allDuizi.size()-1),2);
					copyResidueCards.removeAll(duizi);
					chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
					return chupaiList;
				}
			}else{
				List<Integer> allDuizi =  santiaos.get(0);
				List<Integer> duizi = getCardByCardVal(residueCards, allDuizi.get(allDuizi.size()-1),2);
				copyResidueCards.removeAll(duizi);
				chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
				return chupaiList;
			}
		}else if(remainCard == 4){
			List<List<Integer>> lianduis = residuePaixing.get(px_liandui);
			if(!lianduis.isEmpty()){
				List<Integer> allLianDuizi =  lianduis.get(lianduis.size()-1);
					List<Integer> liandui = getCardByVal(residueCards, allLianDuizi.subList(allLianDuizi.size()-2, allLianDuizi.size()),2);
					copyResidueCards.removeAll(liandui);
					chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
					return chupaiList;
			}
			List<List<Integer>> santiaos = residuePaixing.get(px_santiao);
			if(!santiaos.isEmpty()){
				List<Integer> allSantiao =  santiaos.get(0);
				List<Integer> santiao = getCardByCardVal(residueCards, allSantiao.get(allSantiao.size()-1),3);
				copyResidueCards.removeAll(santiao);
				chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
				return chupaiList;
			}
			List<List<Integer>> duizis = residuePaixing.get(px_duizi);
			if(!duizis.isEmpty()){
				List<Integer> allDuizi = duizis.get(0);
				List<Integer> maxDanpai = getMaxDanpai(copyResidueCards, allPdkCards);
				if(isMaxDuizi(allDuizi.get(allDuizi.size()-1), allPdkCards) || maxDanpai.isEmpty()){//是最大的对子或者下家报单
					if(allDuizi.size()>=2){//有多对牌，留2个最大的对子
						copyResidueCards.removeAll(getCardByCardVal(residueCards, allDuizi.get(allDuizi.size()-1), 2));
						copyResidueCards.removeAll(getCardByCardVal(residueCards, allDuizi.get(allDuizi.size()-2), 2));
						chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
						return chupaiList;
					}else{
						copyResidueCards.removeAll(getCardByCardVal(residueCards, allDuizi.get(allDuizi.size()-1), 2));
						chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
						return chupaiList;
					}
				}else{
					copyResidueCards.removeAll(getCardByCardVal(residueCards, allDuizi.get(allDuizi.size()-1), 2));
					chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
					return chupaiList;
				}
			}else{
				chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
				return chupaiList;
			}
		}else if(remainCard >= 5){
			List<List<Integer>> lianduis = residuePaixing.get(px_liandui);
			if(!lianduis.isEmpty() && remainCard == 6){
				for (List<Integer> lianduiList :lianduis ) {
					if(lianduiList.size() >= 3){
						List<Integer> liandui = getCardByVal(residueCards, lianduiList.subList(lianduiList.size()-3, lianduiList.size()),2);
						copyResidueCards.removeAll(liandui);
						chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
						return chupaiList;
					}
				}
			}
			List<List<Integer>> shunzis = residuePaixing.get(px_shunzi);//剩下的牌里面有没有顺子
			if(!shunzis.isEmpty()){//如果剩下的手牌减去飞机要带的牌，还有5个以上,并且有顺子，则留顺子
				List<Integer> shunzi = shunzis.get(shunzis.size() -1);
				if(remainCard >= shunzi.size()){
					
					List<Integer> shunziCards = getCardByVal(residueCards, shunzi,1);
					copyResidueCards.removeAll(shunziCards);
					chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
				}else{
					List<Integer> shunziCards = getCardByVal(residueCards, shunzi,1);
					copyResidueCards.removeAll(shunziCards);
					if(copyResidueCards.size() < daipaiCount){
						copyResidueCards.addAll(shunziCards.subList(0, daipaiCount-copyResidueCards.size()));
					}
					chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
				}
				return chupaiList;
			}
			
			List<List<Integer>> santiaos = residuePaixing.get(px_santiao);
			if(!santiaos.isEmpty()){
				List<Integer> allSantiao =  santiaos.get(0);
				List<Integer> santiao = getCardByCardVal(residueCards, allSantiao.get(allSantiao.size()-1),3);
				copyResidueCards.removeAll(santiao);
				chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
				return chupaiList;
			}
			if(!lianduis.isEmpty()){
				List<Integer> allLianDuizi =  lianduis.get(lianduis.size()-1);
				List<Integer> liandui = getCardByVal(residueCards, allLianDuizi.subList(allLianDuizi.size()-2, allLianDuizi.size()),2);
				copyResidueCards.removeAll(liandui);
				Map<Integer, List<List<Integer>>> remainPx = getAllPaiXing(copyResidueCards,false);
				List<List<Integer>> duizis = remainPx.get(px_duizi);
				if(!duizis.isEmpty() && copyResidueCards.size() - daipaiCount >= 2){
					List<Integer> dui = duizis.get(0);
					List<Integer> rmDui = getCardByCardVal(copyResidueCards, dui.get(dui.size()-1), 2);
					copyResidueCards.removeAll(rmDui);
				}
				chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
				return chupaiList;
			}
			List<List<Integer>> duizis = residuePaixing.get(px_duizi);
			if(!duizis.isEmpty()){
				List<Integer> allDuizi = duizis.get(0);
				List<Integer> maxDanpai = getMaxDanpai(copyResidueCards, allPdkCards);
				if(isMaxDuizi(allDuizi.get(allDuizi.size()-1), allPdkCards) || maxDanpai.isEmpty()){//是最大的对子或者下家报单
					if(allDuizi.size()>=2){//有多对牌，留2个最大的对子
						copyResidueCards.removeAll(getCardByCardVal(residueCards, allDuizi.get(allDuizi.size()-1), 2));
						copyResidueCards.removeAll(getCardByCardVal(residueCards, allDuizi.get(allDuizi.size()-2), 2));
//						chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
//						return chupaiList;
					}else{
						copyResidueCards.removeAll(getCardByCardVal(residueCards, allDuizi.get(allDuizi.size()-1), 2));
//						chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
//						return chupaiList;
					}
				}else{
					copyResidueCards.removeAll(getCardByCardVal(residueCards, allDuizi.get(allDuizi.size()-1), 2));
//					chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
//					return chupaiList;
				}
				Map<Integer, List<List<Integer>>> remainPx = getAllPaiXing(copyResidueCards,false);
				List<List<Integer>> remainDuizis = remainPx.get(px_duizi);
				if(!remainDuizis.isEmpty() && copyResidueCards.size() - daipaiCount >= 2 ){
					List<Integer> dui = remainDuizis.get(0);
					List<Integer> rmDui = getCardByCardVal(copyResidueCards, dui.get(dui.size()-1), 2);
					copyResidueCards.removeAll(rmDui);
				}
				chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
				return chupaiList;
			}else{
				chupaiList.addAll(copyResidueCards.subList(0, daipaiCount));
				return chupaiList;
			}
		}
		return chupaiList;
	}

	/**
	 * 根据val找牌
	 * @param cards
	 * @param valus
	 * @return
	 */
	private static List<Integer> getCardByVal(List<Integer> cards,List<Integer> valus,int count){
		List<Integer> zhaoCards = new ArrayList<>();
		List<Integer> copyCards = new ArrayList<>(cards);
		
		for (Integer val:valus) {
			List<Integer> list = new ArrayList<>();
			for (Integer card:copyCards) {
				if(card % 100 == val){
					list.add(card);
					if(list.size() == count){
						zhaoCards.addAll(list);
						list.clear();
						break;
					}
				}
			}
		}
		return zhaoCards;
	}
	
	public static boolean isDisDanpai(List<Integer> disCards){
		if(disCards.isEmpty()){
			return false;
		}
		if(disCards.size() == 1){
			return true;
		}
		return false;
	}
	
	public static boolean isDisDuizi(List<Integer> disCards){
		if(disCards.isEmpty() || disCards.size() != 2){//对子只能是2张
			return false;
		}
		Map<Integer, Integer> map = CardTool.loadCards(disCards);
		for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
			if(kv.getValue().intValue() != 2){
				return false;
			}
		}
		return true;
	}
	public static boolean isDisFeiji(List<Integer> disCards){
		if(disCards.isEmpty() || disCards.size() < 10){//飞机最少10张，可能15张
			return false;
		}
		Map<Integer, Integer> map = CardTool.loadCards(disCards);
		List<Integer> threes = new ArrayList<>();
		for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
			if(kv.getValue().intValue() >= 3){
				threes.add(kv.getKey());
			}
		}
		if(threes.isEmpty() || threes.size() < 2){
			return false;
		}
		//牌型中只要3张数的牌有2种以上，肯定是飞机了
		return true;
	}
	public static boolean isDisLiandui(List<Integer> disCards){
		if(disCards.isEmpty() || disCards.size() < 4){//连对最少4
			return false;
		}
		Collections.sort(disCards);
		int begin = 0;
		Map<Integer, Integer> map = CardTool.loadCards(disCards);
		for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
			if(begin != 0 && begin+1 != kv.getKey()){
				return false;
			}
			if(kv.getValue().intValue() != 2){
				return false;
			}
			begin = kv.getKey();
		}
		return true;
	}
	public static boolean isDisSantiao(List<Integer> disCards){
		if(disCards.isEmpty() || disCards.size() != 5){//三条只能是5张
			return false;
		}
		Map<Integer, Integer> map = CardTool.loadCards(disCards);
		List<Integer> threes = new ArrayList<>();
		for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
			if(kv.getValue().intValue() >= 3){
				threes.add(kv.getKey());
			}
		}
		if(threes.isEmpty()){
			return false;
		}
		return true;
	}
	public static boolean isDisShunzi(List<Integer> disCards){
		if(disCards.isEmpty() || disCards.size() < 5){//顺子最少都有5张
			return false;
		}
		Collections.sort(disCards);
		int begin = 0;
		Map<Integer, Integer> map = CardTool.loadCards(disCards);
		for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
			if(begin != 0 && begin+1 != kv.getKey()){
				return false;
			}
			if(kv.getValue().intValue() > 1){//有不是单牌的 则不是顺子
				return false;
			}
			begin = kv.getKey();
		}
		return true;
	}
	public static boolean isDisZhadan(List<Integer> disCards){
		if(disCards.isEmpty() || disCards.size() !=4){//炸弹只能是4张
			return false;
		}
		Map<Integer, Integer> map = CardTool.loadCards(disCards);
		for (Map.Entry<Integer, Integer> kv : map.entrySet()) {
			if(kv.getValue().intValue() == 4){
				return true;
			}
		}
		return false;
	}
	public static void main(String[] args) {
//		 List<Integer> curList = Arrays.asList(304, 307, 111, 208, 108, 408,304, 405,303,106,207);
//		 List<Integer> oppo = new ArrayList<>();
//		 boolean nextDan = false;
//		 PdkTable table = new PdkTable();
//		 table.setAAAZha(1);
//		 table.setIsFirstCardType32(0);
//		 table.setSiDai(3);
//		 List<Integer> chu = getBestAI2(curList, oppo, nextDan, table);
//		 System.out.println(chu);
		// List<Integer> curList = Arrays.asList(211, 311, 111, 208, 108, 408,
		// 304, 404);
		// List<Integer> curList =
		// Arrays.asList(108,108,107,109,110,111,105,112,113,114,115);
//		List<Integer> curList = Arrays.asList(103,203,204,204, 204, 205,305,405, 306,  209,309,409,108, 110,111, 212, 213);
		List<Integer> curList = Arrays.asList(103,203,303,104, 204,304,113, 312,306,212,210,207,206,108,208);
//		List<Integer> curList = Arrays.asList(103,104,105,105,106,106,107);
		List<Integer> oppo = new ArrayList<>();
		boolean nextDan = false;
		PdkTable table = new PdkTable();
		table.setAAAZha(1);
		table.setIsFirstCardType32(0);
		table.setSiDai(3);
		Map<Integer, List<List<Integer>>> chu = getAllPaiXing(curList,false);
		for (Entry<Integer, List<List<Integer>>> map : chu.entrySet()) {
			 System.out.print(map.getKey());
			 System.out.print(" : ");
			 System.out.println(map.getValue());
		}
		System.out.println("出牌");
		List<Integer> chupai  = getChuPaixing(curList, new ArrayList<>(), new HashMap<>(),nextDan);
		System.out.println(chupai);
		List<Integer> liupai = new ArrayList<>(curList);
		liupai.removeAll(chupai);
		System.out.println("留牌");
		System.out.println(liupai);
		
		System.out.println(removePaisByPaiXing(2,Arrays.asList(4), curList));

		List<Integer> list = new ArrayList<>(Arrays.asList(2));
		System.out.println(list);
		System.out.println(curList.subList(0, 2));
	}
	
	
	static class ComparatorList implements Comparator<Integer> {
		@Override
		public int compare(Integer o1, Integer o2) {
			return o1 % 100 > o2 % 100 ? 1 : -1;
		}
	}
}
