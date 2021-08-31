package com.sy599.game.qipai.sypaohuzi.rule;

import java.util.*;

/**
 * 一． 起手大胡守胡规则 1. 清一色:当机器人起手摸同索、筒、万相同张等于或大于9张时会死守清一色牌型。 2.
 * 七小对:当机器人起手对牌数量等于或大于5时会死守七小对。 3. 全大：由序数牌7、8、9 组成的和牌.当手上牌全大牌超过9张时死守全大牌型。 4.
 * 全中：由序数牌4、5、6 组成的和牌.当手上牌全大牌超过9张时死守全中牌型。 5.
 * 全小：由序数牌1、2、3组成的和牌.当手上牌全大牌超过9张时死守全小牌型。 6. 清龙： 和牌中，有同花色123、456、789
 * 相连的序数牌.当手上有青龙超过7张时死守青龙牌型。 7. 混一色：当机器人起手摸到索、筒、万当中一色和自牌组成共同超过9张时死守混一色牌型。 8.
 * 碰碰胡:当机器人起手对牌加刻子（三张）数量等于或大于4时会死守碰碰胡。 9. 优先级:
 * 清一色>七小对>全大>全中>全小>青龙>混一色>碰碰胡（如出现满足多个起手条件时机器人优先选择打法） 10.
 * 牌局中手牌达到以上标准时，机器人也会选择死守大胡。 二． 牌局中规则 1. 进张弃牌：挂单无用废牌（缺张2张以上，比如1.4.7和字牌.），
 * 有字牌先打单张自牌
 * >中缺张（中缺一张，如1.3或者4.6）或边张（89或者12）>对子（两对以上时拆，只一对拆连张，优先选择拆牌面上以出多的牌）>连张（56）
 * 如弃牌需在出现相同等级的需选择时机器人考虑在剩余可摸牌中有较多张保留。（45索和45万选择弃牌，在可摸牌中选择36索或者36万较多者保留）
 * 如果可摸牌中还相同，随机选择弃牌。 2. 杠牌或补张:能杠则杠。 3.
 * 吃牌:最后4张牌有吃必吃可做全求人，如手上没有或者不是顺子，比如123或者456.在不是死守大胡的情况下机器人必吃。 4.
 * 碰牌:拥有对牌数量不小于1对时，有碰则碰。（七小对胡除外）。 5. 听牌：优先听牌牌面上以出牌最少的牌听。如所听牌牌面上已经全出了，则不听牌。 6.
 * 胡牌:有胡必胡，优先级第一。
 * 
 * @author user
 * @date 2012-12-3
 * @version v1.0
 */
public class RobotAI {

	private static RobotAI robotAI;

	public static RobotAI getInstance() {
		if (robotAI == null) {
			robotAI = new RobotAI();
		}
		return robotAI;
	}

	/**
	 * 出牌处理方法
	 * 挂单无用废牌（缺张2张以上，比如1.4.7和字牌.），有字牌先打单张自牌>中缺张（中缺一张，如1.3或者4.6）或边张（89或者12）
	 * >对子（两对以上时拆，只一对拆连张，优先选择拆牌面上以出多的牌。）>连张（56）。
	 * 如弃牌需在出现相同等级的需选择时机器人考虑在剩余可摸牌中有较多张保留
	 * 。（45索和45万选择弃牌，在可摸牌中选择36索或者36万较多者保留）如果可摸牌中还相同，随机选择弃牌。
	 * 
	 * @param paiType
	 *            void
	 * @throws
	 */
	public int outPaiHandle(int paiType, List<Integer> handPais, List<Integer> displayedPais) {
		int outPai = 0;// 要出的牌
		Collections.sort(handPais);
		List<Integer> tempList = new ArrayList<Integer>(handPais);
		Comparator<Integer> compare = null;
		if (paiType == 1) {// 守七对
			compare = new QiDuiPaiCompare(handPais, displayedPais);
		} else {
			checkFormed(tempList);// 挑出已成型的牌
			compare = new PaiCompare(tempList, handPais, displayedPais);
		}

		Collections.sort(tempList, compare);// 对杂牌进行排序
		if (!tempList.isEmpty()) {
			outPai = tempList.get(0);

		} else {
			if (handPais.size() > 0)
				outPai = handPais.get(0);
		}
		// System.out.println("初始牌-"+handPais+"-杂牌-"+tempList+"-out-"+outPai);
		return outPai;
	}

	/**
	 * 守七对的出牌比较器
	 * 
	 * @author Administrator
	 * @date 2013-1-10
	 * @version v1.0
	 */
	private class QiDuiPaiCompare implements Comparator<Integer> {

		/** 手牌 */
		private List<Integer> handPais;

		/** 已经显示的牌 */
		private List<Integer> displayedPais;

		public QiDuiPaiCompare(List<Integer> handPais, List<Integer> displayedPais) {
			this.handPais = handPais;
			this.displayedPais = displayedPais;
		}

		@Override
		public int compare(Integer p1, Integer p2) {
			Map<Integer, List<Integer>> valueMap = getValueMap(handPais);
			List<Integer> tempList = new ArrayList<Integer>(displayedPais);
			tempList.addAll(handPais);// 牌面上已显示的牌加上当前玩家的手牌
			Map<Integer, List<Integer>> valueMap2 = getValueMap(tempList);

			int num1 = valueMap.get(p1).size();
			int num2 = valueMap.get(p2).size();

			if (num1 == num2) {
				if (num1 % 2 != 0) {
					int num3 = 0;
					int num4 = 0;
					if (valueMap2.containsKey(p1))
						num3 = 4 - valueMap2.get(p1).size();
					if (valueMap2.containsKey(p2))
						num4 = 4 - valueMap2.get(p2).size();
					return num3 - num4;
				}
			} else {
				int num3 = Math.abs(2 - num1);
				int num4 = Math.abs(2 - num2);

				if (num3 == num4) {
					return num2 - num1;
				} else {
					return num4 - num3;
				}
			}

			return 0;
		}

	}

	/**
	 * 杂牌优先级的比较类 出牌规则： 1.优先保留牌数量较多者。
	 * 2.若牌数量相等：单牌优先出风牌，对牌优先保留连牌数量较多者；若连牌数量相等优先保留间牌数量较多者。若间牌数量相等保留剩余牌数量较多者
	 * 3.普通单牌
	 * ：优先保留连牌数量较多者；若连牌数量相等优先保留间牌数量较多者；以上情况都相同保留花色较多者；若花色数量相同最后考虑保留接近中间牌（接近5的）
	 * 
	 * @author Administrator
	 * @date 2012-12-29
	 * @version v1.0
	 */
	private class PaiCompare implements Comparator<Integer> {

		/** 杂牌 */
		private List<Integer> scatteredPais;

		/** 手牌 */
		private List<Integer> handPais;

		/** 已经显示的牌 */
		private List<Integer> displayedPais;

		public PaiCompare(List<Integer> scatteredPais, List<Integer> handPais, List<Integer> displayedPais) {
			this.scatteredPais = scatteredPais;
			this.handPais = handPais;
			this.displayedPais = displayedPais;
		}

		@Override
		public int compare(Integer p1, Integer p2) {
			int r = 0;
			Map<Integer, List<Integer>> valueMap = getValueMap(scatteredPais);
			r = valueMap.get(p1).size() - valueMap.get(p2).size();// 优先保留数量多的牌（对牌）

			// 牌数量相等
			if (r == 0) {
				int size1 = 0;// 剩余牌1数量
				int size2 = 0;// 剩余牌2数量
				List<Integer> tempList = new ArrayList<Integer>(displayedPais);
				tempList.addAll(handPais);// 牌面上已显示的牌加上当前玩家的手牌
				Map<Integer, List<Integer>> valueMap2 = getValueMap(tempList);

				if (valueMap2.containsKey(p1)) {
					size1 = 4 - valueMap2.get(p1).size();
				} else {
					size1 = 4;
				}

				if (valueMap2.containsKey(p2)) {
					size2 = 4 - valueMap2.get(p2).size();
				} else {
					size2 = 4;
				}

				// 有风牌的情况，先出风牌
				if (p1 / 10 == 5 || p2 / 10 == 5) {
					// logger.info(p1+"----独风--"+p2);
					r = p2 / 10 - p1 / 10;

					if (r == 0 || valueMap.get(p1).size() > 1) {
						r = size1 - size2;
					}

				} else {
					int num1 = 0;
					int num2 = 0;
					int num11 = 0;
					int num22 = 0;

					if (valueMap.containsKey(p1 - 1)) {
						num1 += valueMap.get(p1 - 1).size();
						if (valueMap2.containsKey(p1 - 2)) {
							num11 += (4 - valueMap2.get(p1 - 2).size());
						} else {
							num11 += 4;
						}
					}

					if (valueMap.containsKey(p1 + 1)) {
						num1 += valueMap.get(p1 + 1).size();
						if (valueMap2.containsKey(p1 + 2)) {
							num11 += (4 - valueMap2.get(p1 + 2).size());
						} else {
							num11 += 4;
						}
					}

					if (valueMap.containsKey(p2 - 1)) {
						num2 += valueMap.get(p2 - 1).size();
						if (valueMap2.containsKey(p2 - 2)) {
							num22 += (4 - valueMap2.get(p2 - 2).size());
						} else {
							num22 += 4;
						}
					}

					if (valueMap.containsKey(p2 + 1)) {
						num2 += valueMap.get(p2 + 1).size();
						if (valueMap2.containsKey(p2 + 2)) {
							num22 += (4 - valueMap2.get(p2 + 2).size());
						} else {
							num22 += 4;
						}
					}

					if (num1 == num2) {// 连牌数量相等
						// logger.info(p1+"---连牌数量相等--"+p2);
						if (num1 == 0) {// 无连牌
							// logger.info(p1+"---无连牌--"+p2);
							int num3 = 0;
							int num4 = 0;
							int num33 = 0;
							int num44 = 0;
							if (valueMap.containsKey(p1 - 2)) {
								num3 += valueMap.get(p1 - 2).size();
								if (valueMap2.containsKey(p1 - 1)) {
									num33 += (4 - valueMap2.get(p1 - 1).size());
								} else {
									num33 += 4;
								}
							}

							if (valueMap.containsKey(p1 + 2)) {
								num3 += valueMap.get(p1 + 2).size();
								if (valueMap2.containsKey(p1 + 1)) {
									num33 += (4 - valueMap2.get(p1 + 1).size());
								} else {
									num33 += 4;
								}
							}

							if (valueMap.containsKey(p2 - 2)) {
								num4 += valueMap.get(p2 - 2).size();
								if (valueMap2.containsKey(p1 - 1)) {
									num44 += (4 - valueMap2.get(p1 - 1).size());
								} else {
									num44 += 4;
								}
							}

							if (valueMap.containsKey(p2 + 2)) {
								num4 += valueMap.get(p2 + 2).size();
								if (valueMap2.containsKey(p1 + 1)) {
									num44 += (4 - valueMap2.get(p1 + 1).size());
								} else {
									num44 += 4;
								}
							}

							if (num3 == num4) {// 间牌数量相等
								// logger.info(p1+"---间牌数量相等--"+p2);

								if (valueMap.get(p1).size() > 1) {// 非单牌
									r = size1 - size2;
								}

								if (r == 0) {// 剩余牌数量相等
									if (num33 == num44) {
										Map<Integer, List<Integer>> styleMap = getStyleMap(handPais);
										int num5 = styleMap.get(p1 / 10).size();
										int num6 = styleMap.get(p2 / 10).size();

										if (num5 != num6 && (num5 < 3 || num6 < 3)) {
											// logger.info(p1+"-考虑花色-"+p2);
											r = num5 - num6;// 保留花色中较多张者
										} else {
											r = Math.abs(p2 % 10 - 5) - Math.abs(p1 % 10 - 5);// 保留接近5的牌（中间牌）
										}
									} else {
										r = num33 - num44;// 保留需求牌剩余数量较多者
									}
								}

							} else {
								// logger.info(p1+"---间牌数量不等--"+p2);
								if (num3 > 0 && num4 > 0) {// 都有间牌，保留间牌数量较少者
									r = num4 - num3;// 间牌数量不等，保留间牌数量较多者
								} else {
									r = num3 - num4;// 一个无间牌，保留有间牌者
								}
							}
						} else {// 都有连牌且数量相等
							// logger.info(p1+"---有连牌--"+p2);

							if (valueMap.get(p1).size() > 1) {
								r = size1 - size2;
							}

							if (r == 0) {// 剩余牌数量相等
								if (num11 == num22) {
									r = Math.abs(p2 % 10 - 5) - Math.abs(p1 % 10 - 5);// 都有连牌且数量相等，保留接近5的牌（中间牌）
								} else {
									r = num11 - num22;// 保留连牌剩余牌数量较多者
								}
							}
							//
						}

					} else {// 连牌数量不等
						// logger.info(p1+"---连牌数量不等--"+p2);
						if (valueMap.get(p1).size() == 1) {// 独牌
							// logger.info(p1+"---独牌--"+p2);
							if (num1 > 0 && num2 > 0) {
								// logger.info(p1+"---都有连牌--"+p2);
								r = num2 - num1;// 都有连牌，保留连牌数量少的
							} else {
								// logger.info(p1+"---一个无连牌--"+p2);
								r = num1 - num2;// 一个无连牌，保留有连牌的
							}
						} else {
							// logger.info(p1+"---对牌--"+p2);
							r = num1 - num2;// 对牌，保留连牌数量较多者
						}
					}
				}
			}

			return r;
		}

	}

	/**
	 * 检查并删除已经成型的牌集合
	 * 
	 * @param paiList
	 * @return List<Integer>
	 * @throws
	 */
	public List<Integer> checkFormed(List<Integer> paiList) {
		List<Integer> list = new ArrayList<Integer>();
		List<Integer> keList = new ArrayList<Integer>();// 刻或杠集合
		Map<Integer, List<Integer>> valueMap = getValueMap(paiList);

		// 拣刻子
		for (int key : valueMap.keySet()) {
			if (valueMap.get(key).size() >= 3) {
				paiList.removeAll(valueMap.get(key));
				list.addAll(valueMap.get(key));
			}
		}

		paiList.removeAll(keList);// 删除已挑出的刻子

		// 接下来开始挑顺子
		Map<Integer, List<Integer>> styleMap = getStyleMap(paiList);

		for (int style : styleMap.keySet()) {
			if (style < 4 && styleMap.get(style).size() >= 3) {
				valueMap = getValueMap(styleMap.get(style));
				List<Integer> keyList = new ArrayList<Integer>(valueMap.keySet());
				Collections.sort(keyList);
				List<Integer> tempList = new ArrayList<Integer>();
				tempList.add(keyList.get(0));

				for (int i = 0; i < keyList.size() - 1; i++) {
					if (keyList.get(i) + 1 != keyList.get(i + 1)) {
						if (tempList.size() >= 3) {// 有顺子
							List<Integer> pkList = new ArrayList<Integer>();
							for (int key : tempList) {
								pkList.addAll(valueMap.get(key));
							}
							list.addAll(checkShunzi(pkList, paiList));
						}
						tempList.clear();
					}
					tempList.add(keyList.get(i + 1));
				}

				if (tempList.size() >= 3) {// 有顺子

					List<Integer> pkList = new ArrayList<Integer>();
					for (int key : tempList) {
						pkList.addAll(valueMap.get(key));
					}
					list.addAll(checkShunzi(pkList, paiList));
				}
			}
		}

		// logger.info("已成型的牌:"+list);
		return list;
	}

	/**
	 * 检查顺子，并从paiList删除挑出的顺子
	 * 
	 * @param pkList
	 *            单一花色可组顺的牌集合
	 * @param paiList
	 *            手牌
	 * @return List<Integer>
	 * @throws
	 */
	private List<Integer> checkShunzi(List<Integer> pkList, List<Integer> paiList) {
		List<Integer> list = new ArrayList<Integer>();
		Map<Integer, List<Integer>> valueMap = getValueMap(pkList);
		List<Integer> keyList = new ArrayList<Integer>(valueMap.keySet());
		Collections.sort(keyList);

		boolean b = false;// 是否从小到大开始拣

		if (keyList.size() > 3) {
			// 从少牌一方开始拣
			if (valueMap.get(keyList.get(0)).size() < valueMap.get(keyList.get(keyList.size() - 1)).size()) {
				b = true;
			} else if (valueMap.get(keyList.get(0)).size() > valueMap.get(keyList.get(keyList.size() - 1)).size()) {
				b = false;
			} else {
				Map<Integer, List<Integer>> valueMap2 = getValueMap(paiList);
				int count1 = 0;
				int count2 = 0;

				if (keyList.get(0) % 10 >= 3 && valueMap2.containsKey(keyList.get(0) - 2)) {
					count1 = valueMap2.get(keyList.get(0) - 2).size();
				}

				if (keyList.get(keyList.size() - 1) % 10 <= 7 && valueMap2.containsKey(keyList.get(keyList.size() - 1) + 2)) {
					count2 = valueMap2.get(keyList.get(keyList.size() - 1) + 2).size();
				}

				if (count1 != count2) {
					if (count1 < count2) {
						if (count1 > 0)
							b = false;
						else
							b = true;
					} else {
						if (count2 > 0)
							b = true;
						else
							b = false;
					}
				} else {
					if (count1 > 0) {
						// 1,2,4,5,6,7,9的情况 优先挑456
						if (keyList.get(0) % 10 == 4 && valueMap2.containsKey(keyList.get(0) - 3)) {
							b = true;
						}
						// 1,3,4,5,6,8,9的情况优先挑456
						else if (keyList.get(0) % 10 == 3 && valueMap2.containsKey(keyList.get(keyList.size() - 1) + 3)) {
							b = false;
						} else {
							b = Math.abs(keyList.get(0) % 10 - 5) > Math.abs(keyList.get(keyList.size() - 1) % 10 - 5) ? true : false;
						}
					} else {// 两头牌型几乎一样
						b = Math.abs(keyList.get(0) % 10 - 5) > Math.abs(keyList.get(keyList.size() - 1) % 10 - 5) ? true : false;
					}
				}

			}
		}

		if (b) {
			for (int i = keyList.get(0); i < keyList.get(keyList.size() - 1) - 1; i++) {// 从小牌（左边）开始拣
				while (paiList.contains(i) && paiList.contains(i + 1) && paiList.contains(i + 2)) {
					paiList.remove((Object) i);
					paiList.remove((Object) (i + 1));
					paiList.remove((Object) (i + 2));
					list.add(i);
					list.add(i + 1);
					list.add(i + 2);
				}
			}

		} else {
			for (int i = keyList.get(keyList.size() - 1); i > keyList.get(0) + 1; i--) {// 从大牌（右边）开始拣
				while (paiList.contains(i) && paiList.contains(i - 1) && paiList.contains(i - 2)) {
					paiList.remove((Object) (i - 2));
					paiList.remove((Object) (i - 1));
					paiList.remove((Object) i);
					list.add(i - 2);
					list.add(i - 1);
					list.add(i);
				}
			}
		}

		return list;
	}

	/**
	 * 检查碰
	 */
	public List<Integer> checkPeng(List<Integer> paiList, Integer pai) {
		List<Integer> opList = new ArrayList<Integer>();// 即将执行动作的牌（组碰或杠）
		Map<Integer, List<Integer>> valueMap = getValueMap(paiList);
		List<Integer> tempList = new ArrayList<Integer>(paiList);
		checkFormed(tempList);
		Map<Integer, List<Integer>> valueMap2 = getValueMap(tempList);

		if (valueMap2.containsKey(pai) && valueMap.get(pai).size() == 2) {
			opList.addAll(valueMap.get(pai));
		}

		return opList;
	}

	/**
	 * 检查明杠
	 */
	public List<Integer> checkGang(List<Integer> handPais, Integer pai) {
		List<Integer> opList = new ArrayList<Integer>();// 即将执行动作的牌（组碰或杠）
		Map<Integer, List<Integer>> valueMap = getValueMap(handPais);

		if (valueMap.containsKey(pai) && valueMap.get(pai).size() == 3) {
			opList.addAll(valueMap.get(pai));
		}

		return opList;
	}

	/**
	 * 检查吃处理
	 * 
	 * @param handPais
	 * @param pai
	 *            上家打的牌
	 * @return List<Integer> 组吃的集合
	 * @throws
	 */
	public List<Integer> checkChi(List<Integer> handPais, Integer pai) {
		List<Integer> opList = new ArrayList<Integer>();// 即将执行动作的牌（组吃）
		return opList;
	}

	/**
	 * 将paiList转成valueMap
	 * 
	 * @param paiList
	 * @return Map<Integer,List<Integer>>
	 * @throws
	 */
	public Map<Integer, List<Integer>> getValueMap(List<Integer> paiList) {
		Map<Integer, List<Integer>> valueMap = new HashMap<Integer, List<Integer>>();// 以牌代号为key
		for (Integer p : paiList) {
			List<Integer> pList = null;
			if (valueMap.containsKey(p)) {
				pList = valueMap.get(p);
			} else {
				pList = new ArrayList<Integer>();
			}
			pList.add(p);
			valueMap.put(p, pList);
		}

		return valueMap;
	}

	/**
	 * 将paiList转成styleMap
	 * 
	 * @param paiList
	 * @return Map<Integer,List<Integer>>
	 * @throws
	 */
	public Map<Integer, List<Integer>> getStyleMap(List<Integer> paiList) {
		Map<Integer, List<Integer>> styleMap = new HashMap<Integer, List<Integer>>();// 以花色为key
		for (Integer p : paiList) {
			int style = p / 10;
			List<Integer> pList = null;
			if (styleMap.containsKey(style)) {
				pList = styleMap.get(style);
			} else {
				pList = new ArrayList<Integer>();
			}
			pList.add(p);
			styleMap.put(style, pList);
		}

		return styleMap;
	}

	/**
	 * 检查对牌是否超过5对，
	 * 
	 * @param handPais
	 * @param handPais
	 * @return boolean
	 * @throws
	 */
	public boolean checkQiDui(List<Integer> handPais) {
		List<Integer> paiList = new ArrayList<Integer>(handPais);
		Map<Integer, List<Integer>> valueMap = getValueMap(paiList);
		int count = 0;// 对子数量
		for (int key : valueMap.keySet()) {
			if (valueMap.get(key).size() >= 2) {
				count++;
			}
		}

		if (count >= 5)
			return true;// 七小对
		return false;
	}

}