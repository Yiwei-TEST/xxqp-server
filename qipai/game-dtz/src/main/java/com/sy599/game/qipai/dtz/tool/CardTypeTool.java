package com.sy599.game.qipai.dtz.tool;

import com.sy599.game.qipai.dtz.bean.Card_index;
import com.sy599.game.qipai.dtz.bean.DtzPlayer;
import com.sy599.game.qipai.dtz.bean.Model;
import com.sy599.game.qipai.dtz.rule.CardType;
import com.sy599.game.util.StringUtil;

import java.util.*;

/**
 * 规则 查询牌型 等等
 * 
 * @author lc
 * 
 */
public class CardTypeTool {
	public static void main(String[] args) {
		// 104,312,210,305,309,405,205,413,304,106,105,310,407,307,111,411

		// totalPoint: 16
		// cards: 415
		// cards: 114
		// cards: 113
		// cards: 312
		// cards: 210
		// cards: 110
		// cards: 309
		// cards: 209
		// cards: 107
		// cards: 203
		// [407,307,207,107,306,206,106,404,403,103]:[213,412,406,405,305,105,304,204,104,203]

		// ArrayList<Integer> oppo = new ArrayList<>(Arrays.asList(103, 203,
		// 304, 404));
		System.out.println(getLianCount(new ArrayList<>(Arrays.asList(1, 2, 4, 5, 6, 8, 9, 10, 11))));
		// oppo.add(211);
		// oppo.add(211);
		// oppo.add(211);

		// 307,207,206,106
		// ArrayList<Integer> from = new ArrayList<>(Arrays.asList(106, 206,
		// 307,407));
		ArrayList<Integer> oppo = new ArrayList<>(Arrays.asList(208, 308));
		ArrayList<Integer> from = new ArrayList<>(Arrays.asList(410, 310, 210, 107, 306, 206, 106, 404, 403, 103));
		// from.add(307);
		// from.add(207);
		// from.add(207);
		// from.add(308);
		// from.add(308);
		// from.add(211);
		// from.add(211);
		// from.add(211);
		// from.add(114);
		// oppo.add(104);
		// System.out.println(Collections.max(from));
		// setOrder(curList);
		// Model model = new Model();
		// getTwo(curList, model);
		// getTwoTwo(curList, model, 2);
		System.out.println(canPlay(from, oppo, true));
		// System.out.println(checkCards(from, oppo));
		//
		System.out.println(jugdeType(oppo));
		// System.out.println(0 % 100);

	}

	// public static void removeCards(List<Integer> nowCards, List<Integer>
	// removeCards) {
	//
	// }

	public static int getPlaneLength(DtzPlayer player, List<Integer> cards, CardType cardType) {
		return 0;
	}

	/**
	 * @param from
	 *            是自己已有的牌
	 * @param oppo
	 *            对手出的牌
	 * @return
	 */
	public static int isCanPlay(List<Integer> from, List<Integer> oppo) {
		List<Integer> list = canPlay(from, oppo);
		return (list == null || list.isEmpty()) ? 0 : 1;
	}


	public static List<Integer> canPlay(List<Integer> from, List<Integer> oppo) {
		return canPlay(from, oppo, false);
	}

	private static int getThreeLianSize(Card_index card_index_oppo) {
		List<Integer> threeList = new ArrayList<>();
		threeList.addAll(card_index_oppo.a[2]);
		threeList.addAll(card_index_oppo.a[3]);
		Collections.sort(threeList);
		int card_index_oppo_size = 1;
		for (int i = 0; i < threeList.size(); i++) {
			if (i + 1 >= threeList.size()) {
				break;
			}
			int left = threeList.get(i);
			int right = threeList.get(i + 1);
			if (right - left == 1) {
				card_index_oppo_size++;
			}

		}
		return card_index_oppo_size;
	}

	/**
	 * @param from
	 *            是自己已有的牌
	 * @param oppo
	 *            对手出的牌
	 * @return
	 */
	public static List<Integer> canPlay(List<Integer> from, List<Integer> oppo, boolean isDisCards) {
		setOrder(from);
		// oppo是对手出的牌,from是自己已有的牌,to是药走出的牌
		List<Integer> to = new ArrayList<>();
		List<String> list = new Vector<String>();// 装要走出的牌的name
		CardType cType = CardTypeTool.jugdeType(oppo);
		// 按重复数排序,这样只需比较第一张牌
		oppo = CardTypeTool.getOrder2(oppo);
		Model model = new Model();
		CardTypeTool.getBoomb(from, model);
		Model oppoModel = null;
		switch (cType) {
		case c1:
			getSingle(from, model);
			for (int len = model.a1.size(), i = len - 1; i >= 0; i--) {
				if (CardTypeTool.getValueByName(model.a1.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
					list.add(model.a1.get(i));
					break;
				}
			}

			if (list.size() == 0) {
				for (int i = 0, leni = from.size(); i < leni; i++) {
					if (CardTypeTool.getValue(from.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
						to.add(from.get(i));
					}
				}
			}

			break;
		case c2:
			CardTypeTool.getTwo(from, model);

			for (int len = model.a2.size(), i = len - 1; i >= 0; i--) {
				if (CardTypeTool.getValueByName(model.a2.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
					list.add(model.a2.get(i));
					break;
				}
			}

			if (list.size() == 0) {
				for (int len = model.a3.size(), i = len - 1; i >= 0; i--) {

					if (CardTypeTool.getValueByName(model.a3.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
						List<Integer> t = CardTypeTool.getCardsByName(from, model.a3.get(i));
						to.add(t.get(0));
						to.add(t.get(1));
					}
				}
			}

			break;
		case c3:
			CardTypeTool.getThree(from, model);

			for (int len = model.a3.size(), i = len - 1; i >= 0; i--) {
				if (CardTypeTool.getValueByName(model.a3.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
					list.add(model.a3.get(i));
					break;
				}
			}
			break;
		case c31:
			CardTypeTool.getThree(from, model);

			int len1 = model.a3.size();
			int len2 = model.a1.size();
			if (!(len1 < 1 || len2 < 1)) {
				for (int len = len1, i = len - 1; i >= 0; i--) {
					if (CardTypeTool.getValueByName(model.a3.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
						list.add(model.a3.get(i));
						break;
					}
				}
				if (list.size() > 0) {
					list.add(model.a1.get(len2 - 1));
				}
			}
			break;
		case c32:
			oppoModel = new Model();
			CardTypeTool.getThree(oppo, oppoModel);

			CardTypeTool.getThree(from, model);
			CardTypeTool.getSingle(from, model);
			len1 = model.a3.size();
			len2 = model.a1.size();
			if (from.size() >= oppo.size() && !(len1 < 1)) {
				for (int len = len1, i = len - 1; i >= 0; i--) {
					if (i >= model.a3.size() || i >= oppoModel.a3.size()) {
						// LogUtil.e("----------" +
						// JacksonUtil.writeValueAsString(model.a3) + " " +
						// JacksonUtil.writeValueAsString(oppoModel.a3));
					}
					if (CardTypeTool.getValueByName(model.a3.get(i)) > CardTypeTool.getValueByName(oppoModel.a3.get(0))) {
						list.add(model.a3.get(i));
						break;
					}
				}
				if (list.size() > 0) {
					// 打得起
					int j = 1;
					for (int i = 0; i < model.a1.size(); i++) {
						if (j > 2) {
							break;
						}
						list.add(model.a1.get(i));
						j++;
					}

				}
			}

			break;
		case c411:

			len1 = model.a4.size();
			len2 = model.a1.size();
			if (!(len1 < 1 || len2 < 2)) {
				for (int len = len1, i = len - 1; i >= 0; i--) {
					if (CardTypeTool.getValueByName(model.a4.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
						list.add(model.a4.get(i));
						break;
					}
				}
				if (list.size() > 0) {
					list.add(model.a1.get(len2 - 1));
					list.add(model.a1.get(len2 - 2));
				}
			}
			break;
		case c422:

			len1 = model.a4.size();
			len2 = model.a2.size();
			if (!(len1 < 1 || len2 < 2)) {
				for (int len = len1, i = len - 1; i >= 0; i--) {
					if (CardTypeTool.getValueByName(model.a4.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
						list.add(model.a4.get(i));
						break;
					}
				}
				if (list.size() > 0) {
					list.add(model.a2.get(len2 - 1));
					list.add(model.a2.get(len2 - 2));
				}
			}
			break;
		case c123:
			CardTypeTool.get123(from, model, oppo.size());
			for (int len = model.a123.size(), i = len - 1; i >= 0; i--) {
				String[] s = model.a123.get(i).split(",");
				if (s.length == oppo.size() && CardTypeTool.getValueByName(model.a123.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
					list.add(model.a123.get(i));
					break;
				}
			}
			break;
		case c1122:
			CardTypeTool.getTwo(from, model);
			CardTypeTool.getTwoTwo(from, model, oppo.size() / 2);
			for (int len = model.a112233.size(), i = len - 1; i >= 0; i--) {
				String[] s = model.a112233.get(i).split(",");
				if (s.length == oppo.size() && CardTypeTool.getValueByName(model.a112233.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
					list.add(model.a112233.get(i));
					break;
				}
			}
//			if (CardTypeTool.jugdeType(from) == CardType.c1122) {
//				if (Collections.max(oppo) > Collections.max(from)) {
//					return Arrays.asList();
//				}
//				else {
//					return Arrays.asList(1);
//				}
//			}
			break;
		case c11122234:
			Card_index card_index_from = new Card_index();
			for (int i = 0; i < 4; i++) {
				card_index_from.a[i] = new Vector<Integer>();
			}
			// 求出各种数字出现频率
			CardTypeTool.getMax(card_index_from, from); // a[0,1,2,3]分别表示重复1,2,3,4次的牌

			Card_index card_index_oppo = new Card_index();
			for (int i = 0; i < 4; i++) {
				card_index_oppo.a[i] = new Vector<Integer>();
			}
			// 求出各种数字出现频率
			CardTypeTool.getMax(card_index_oppo, oppo); // a[0,1,2,3]分别表示重复1,2,3,4次的牌
			int card_index_from_size = getThreeLianSize(card_index_from);
			int card_index_oppo_size = getThreeLianSize(card_index_oppo);
			int threeLengthsize = (int) (Math.ceil((oppo.size() / (float) 5)));
			if (card_index_oppo_size > threeLengthsize) {
				card_index_oppo_size = threeLengthsize;
			}

			setOrder(oppo);
			oppoModel = new Model();
			CardTypeTool.getThree(oppo, oppoModel);
			CardTypeTool.getPlane(oppo, oppoModel, card_index_oppo_size);

			CardTypeTool.getThree(from, model);
			CardTypeTool.getPlane(from, model, card_index_oppo_size);
			if (from.size() >= oppo.size() && card_index_from_size >= card_index_oppo_size) {
				len1 = model.a111222.size();
				for (int o = len1 - 1; o >= 0; o--) {
					// String[] s = model.a111222.get(o).split(",");
					String plane = model.a111222.get(o);
					if (CardTypeTool.getValueByName(model.a111222.get(o)) > CardTypeTool.getValueByName(oppoModel.a111222.get(0))) {
						// 能打的起
						int disCount = card_index_oppo_size * 3 + card_index_oppo_size * 2;
						if (from.size() <= disCount) {
							// 可以一次性出完牌
							for (int i = 0; i < from.size(); i++) {
								list.add(from.get(i) + "");

							}

						} else {
							List<Integer> play = StringUtil.explodeToIntList(plane);
							for (int value : play) {
								list.add(value + "");
							}
							// 需要带牌
							for (int i = 0; i < from.size(); i++) {
								if (list.size() >= disCount) {
									break;
								}
								if (!list.contains(from.get(i) + "")) {
									list.add(from.get(i) + "");

								}

							}

						}
						break;
					}

				}
			}

			break;
		case c1112223344:
			CardTypeTool.getPlane(from, model);
			len1 = model.a111222.size();
			len2 = model.a2.size();

			if (!(len1 < 1 || len2 < 2)) {
				for (int i = len1 - 1; i >= 0; i--) {
					String[] s = model.a111222.get(i).split(",");
					if ((s.length / 3 <= len2) && (s.length * 4 == oppo.size()) && CardTypeTool.getValueByName(model.a111222.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
						list.add(model.a111222.get(i));
						for (int j = 1; j <= s.length / 3; j++) {
							list.add(model.a2.get(len2 - j));
						}
					}
				}
			}
			break;
		case c4:
			CardTypeTool.getBoomb(from, model);
			for (int len = model.a4.size(), i = len - 1; i >= 0; i--) {
				if (CardTypeTool.getValueByName(model.a4.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
					list.add(model.a4.get(i));
					break;
				}
			}
		default:
			break;
		}
		if (list.size() == 0) {
			if (cType != CardType.c4) {
				if (model.a4.size() > 0) {
					// 不是出牌,如果是炸弹张数必须是4
					if (!isDisCards || (isDisCards && from.size() == 4)) {
						list.add(model.a4.get(model.a4.size() - 1));
						for (String s : list) {
							to.addAll(CardTypeTool.getCardsByName(from, s));
						}
					}

				}
			}
			// to = null;
		} else {
			for (String s : list) {
				to.addAll(CardTypeTool.getCardsByName(from, s));
			}
		}
		return to;
	}

	/**
	 * 机器人判断出牌
	 * 
	 * @param curList
	 *            自己已有的牌
	 * @param oppo
	 *            对手出的牌
	 * @return 出的牌
	 */
	public static List<Integer> getBestAI(List<Integer> curList, List<Integer> oppo) {
		setOrder(curList);
		ArrayList<Integer> list = new ArrayList<Integer>(curList);
		Model model = new Model();
		Model modelSingle = new Model();

		// 找出所有对子,3带，炸弹，飞机，双顺，单顺
		CardTypeTool.getTwo(list, model);
		CardTypeTool.getThree(list, model);
		CardTypeTool.get123(list, model);
		CardTypeTool.getBoomb(list, model);
		CardTypeTool.getTwoTwo(list, model);
		CardTypeTool.getPlane(list, model);
		CardTypeTool.getSingle(list, model);
		// 去除model里面独立牌型
		CardTypeTool.checkModel(list, model, modelSingle);
		// 现在分别计算每种可能性的权值,和手数，取最大的那个(注意有些牌型是相关的，组成这个就不能组成其他)
		// 所以组成一种牌型前要判断这种牌型的牌还是否存在
		// 先比较手数再比较权值
		Model bestModel = null, myModel = null;
		int value = 0;
		int time = 99;
		for (int i = 0, len1 = model.a4.size(); i <= len1; i++) {
			for (int j = 0, len2 = model.a3.size(); j <= len2; j++) {
				for (int k = 0, len3 = model.a2.size(); k <= len3; k++) {
					for (int l = 0, len4 = model.a123.size(); l <= len4; l++) {
						for (int m = 0, len5 = model.a112233.size(); m <= len5; m++) {
							for (int n = 0, len6 = model.a111222.size(); n <= len6; n++) {
								ArrayList<Integer> newlist = new ArrayList<Integer>(list);

								bestModel = CardTypeTool.getBestModel(newlist, model, new int[] { i, j, k, l, m, n });
								// 加上独立的牌
								bestModel.a1.addAll(modelSingle.a1);
								bestModel.a2.addAll(modelSingle.a2);
								bestModel.a3.addAll(modelSingle.a3);
								bestModel.a4.addAll(modelSingle.a4);
								bestModel.a123.addAll(modelSingle.a123);
								bestModel.a112233.addAll(modelSingle.a112233);
								bestModel.a111222.addAll(modelSingle.a111222);
								// 加上单牌
								for (Integer singleCard : newlist) {
									bestModel.a1.add(singleCard.toString());
								}
								// 计算手数，计算权值
								if (CardTypeTool.getTimes(bestModel) < time) {
									time = CardTypeTool.getTimes(bestModel);
									myModel = bestModel;
								} else if (CardTypeTool.getTimes(bestModel) == time && CardTypeTool.getCountValues(bestModel) > value) {
									value = CardTypeTool.getCountValues(bestModel);
									myModel = bestModel;
								}
							}
						}
					}
				}
			}
		}
		// 开始出牌
		List<Integer> showCardslList = new ArrayList<Integer>();
		if (oppo == null || oppo.isEmpty()) {
			showCards(myModel, showCardslList, curList);
		} else {
			showCards2(myModel, showCardslList, curList, oppo);
			// showCardslList = canPlay(curList, oppo);
		}

		// 被动出牌
		if (showCardslList == null || showCardslList.size() == 0) {
			return null;
		}
		return showCardslList;
	}

	/**
	 * 出牌
	 * 
	 * @param current
	 *            我出的牌
	 * @param oppo
	 *            对手出的牌
	 * @return
	 */
	public static ArrayList<Integer> disCards(ArrayList<Integer> current, ArrayList<Integer> oppo) {
		ArrayList<Integer> list = new ArrayList<Integer>();

		for (int i = 0, len = current.size(); i < len; i++) {
			Integer card = current.get(i);
			list.add(card);
		}
		CardType myType = CardTypeTool.jugdeType(list);
		if (oppo == null)// 我主动走牌
		{
			if (myType != CardType.c0) {
				return list;
			} else {
				return null;
			}
		} else {// 我跟牌
			String oppoString = "";
			for (int i = 0; i < oppo.size(); i++) {
				oppoString += oppo.get(i);
			}
			if (CardTypeTool.isCanPlay(list, oppo) == 1) {
				return list;
			} else {
				return null;
			}
		}

	}

	/**
	 * 判断牌型
	 * 
	 * @param list
	 * @return
	 */
	public static CardType jugdeType(List<Integer> list) {
		if (list == null || list.isEmpty()) {
			return CardType.c0;
		}
		//设定大小顺序
		setOrder(list);
		int len = list.size();
		// 双王,化为对子返回
		if (len == 2 && CardTypeTool.getValue(list.get(0)) == 17 && CardTypeTool.getValue(list.get(1)) == 16) {
			return CardType.c4;
		}
		// 单牌,对子，3不带，4个一样炸弹
		if (len <= 4) { // 如果第一个和最后个相同，说明全部相同
			if (CardTypeToolDtz.isTongZi(list)) {
				return CardType.cTZ;
			}
			if (CardTypeToolDtz.isBomb(CardToolDtz.toValueList(list))) {
				return CardType.c4;
			}
			if (list.size() > 0 && CardTypeTool.getValue(list.get(0)) == CardTypeTool.getValue(list.get(len - 1))) {
				switch (len) {
				case 1:
					return CardType.c1;
				case 2:
					return CardType.c2;
				case 3:
					return CardType.c3;
				case 4:
					return CardType.c4;
				}
			}
			// 当第一个和最后个不同时,3带1
			if (len == 4 && ((CardTypeTool.getValue(list.get(0)) == CardTypeTool.getValue(list.get(len - 2))) || CardTypeTool.getValue(list.get(1)) == CardTypeTool.getValue(list.get(len - 1)))) {
				return CardType.c32;
			}

		}
		// 当5张以上时，连字，3带2，飞机，2顺，4带2等等
		if (len >= 4) {// 现在按相同数字最大出现次数
			if (CardTypeToolDtz.isBomb(CardToolDtz.toValueList(list))) {
				return CardType.c4;
			}
			Card_index card_index = new Card_index();
			for (int i = 0; i < 4; i++) {
				card_index.a[i] = new Vector<Integer>();
			}
			// 求出各种数字出现频率
			CardTypeTool.getMax(card_index, list); // a[0,1,2,3]分别表示重复1,2,3,4次的牌
			// 3带2 -----必含重复3次的牌
			if (card_index.a[2].size() == 1 && len == 5) {
				return CardType.c32;
			}
			// 4带2(单,双)
			if (card_index.a[3].size() == 1 && len == 5) {
				return CardType.c32;
				// return CardType.c411;
			}
			// if (card_index.a[3].size() == 1 && card_index.a[1].size() == 2 &&
			// len == 8) {
			// return CardType.c422;
			// }
			// 单连,保证不存在王
			if ((CardTypeTool.getColor(list.get(0)) != 5) && (card_index.a[0].size() == len) && (CardTypeTool.getValue(list.get(0)) - CardTypeTool.getValue(list.get(len - 1)) == len - 1)) {
				return CardType.c123;
			}
			// 连队
			if (card_index.a[1].size() == len / 2 && len % 2 == 0 && len / 2 >= 2 && (CardTypeTool.getValue(list.get(0)) - CardTypeTool.getValue(list.get(len - 1)) == (len / 2 - 1))) {
				return CardType.c1122;
			}
			// // 飞机
			// if (card_index.a[2].size() == len / 3 && (len % 3 == 0) &&
			// (CardTypeTool.getValue(list.get(0)) -
			// CardTypeTool.getValue(list.get(len - 1)) == (len / 3 - 1))) {
			// return CardType.c111222;
			// }
			// 飞机带n单,n/2对
			List<Integer> threeList = new ArrayList<>();
			threeList.addAll(card_index.a[2]);
			if (!card_index.a[3].isEmpty()) {
				threeList.addAll(card_index.a[3]);
				Collections.sort(threeList);
			}
			int near = getLianCount(threeList);
			if (near >= 2 && len <= (near * 3) + (near * 2)) {
				if (near == 0) {
					return CardType.c0;

				} else {
					// && ((Integer) (card_index.a[2].get(len / 4 - 1)) -
					// (Integer)
					// (card_index.a[2].get(0)) == len / 4 - 1
					return CardType.c11122234;
				}

			}

			// 飞机带n双
			if (card_index.a[2].size() >= 2 && card_index.a[2].size() == len / 5 && card_index.a[2].size() == len / 5
					&& ((Integer) (card_index.a[2].get(len / 5 - 1)) - (Integer) (card_index.a[2].get(0)) == len / 5 - 1) && len == card_index.a[2].size() * 5) {
				return CardType.c1112223344;
			}

		}
		return CardType.c0;
	}

	public static int getLianCount(List<Integer> list) {
		if (list.size() == 1) {
			return 1;
		}
		int maxNear = 0;
		int near = 1;
		for (int i = 0; i < list.size(); i++) {
			if (i + 1 >= list.size()) {
				break;
			}
			int left = list.get(i);
			int right = list.get(i + 1);
			if (right - left == 1) {
				near++;
				if (near > maxNear) {
					maxNear = near;
				}
			} else {
				near = 1;
			}

		}
		return maxNear;
	}

	/**
	 * 返回值
	 * 
	 * @param card
	 * @return
	 */
	public static int getValue(int card) {
		int i = card % 100;
		return i;
	}

	/**
	 * 返回花色
	 * 
	 * @param card
	 * @return
	 */
	public static int getColor(int card) {
		return card / 100;
	}

	/**
	 * 得到最大相同数
	 * 
	 * @param card_index
	 * @param list
	 */
	public static void getMax(Card_index card_index, List<Integer> list) {
		int count[] = new int[17];// 1-16各算一种,王算第16种
		for (int i = 0; i < 17; i++) {
			count[i] = 0;
		}
		for (int i = 0, len = list.size(); i < len; i++) {
			if (CardTypeTool.getColor(list.get(i)) == 5) {
				count[16]++;
			} else {
				count[CardTypeTool.getValue(list.get(i)) - 1]++;
			}
		}
		for (int i = 0; i < 17; i++) {
			switch (count[i]) {
			case 1:
				card_index.a[0].add(i + 1);
				break;
			case 2:
				card_index.a[1].add(i + 1);
				break;
			case 3:
				card_index.a[2].add(i + 1);
				break;
			case 4:
				card_index.a[3].add(i + 1);
				break;
			}
		}
	}

	/**
	 * 检查牌的是否能出
	 * 
	 * @param c
	 *            点选的牌
	 * @param oppo
	 *            当前最大的牌
	 * @return 1能出 0不能
	 */
	public static int checkCards(List<Integer> c, List<Integer> oppo) {
		// 找出当前最大的牌是哪个电脑出的,c是点选的牌
		List<Integer> currentlist = oppo;
		CardType cType = CardTypeTool.jugdeType(c);
		CardType cType2 = CardTypeTool.jugdeType(currentlist);
		// 如果张数不同直接过滤
		if (cType != CardType.c4 && (cType != CardType.c32 && c.size() != currentlist.size())) {
			return 0;
		}
		// 比较我的出牌类型
		if (cType != CardType.c4 && cType != cType2) {

			return 0;
		}
		// 比较出的牌是否要大
		// 我是炸弹
		if (cType == CardType.c4) {
			if (c.size() == 2) {
				return 1;
			}
			if (cType2 != CardType.c4) {
				return 1;
			}
		}

		// 单牌,对子,3带,4炸弹
		if (cType == CardType.c1 || cType == CardType.c2 || cType == CardType.c3 || cType == CardType.c4) {
			if (CardTypeTool.getValue(c.get(0)) <= CardTypeTool.getValue(currentlist.get(0))) {
				return 0;
			} else {
				return 1;
			}
		}
		// 顺子,连队，飞机裸
		if (cType == CardType.c123 || cType == CardType.c1122 || cType == CardType.c111222) {
			if (CardTypeTool.getValue(c.get(0)) <= CardTypeTool.getValue(currentlist.get(0))) {
				return 0;
			} else {
				return 1;
			}
		}
		// 按重复多少排序
		// 3带1,3带2 ,飞机带单，双,4带1,2,只需比较第一个就行，独一无二的
		if (cType == CardType.c31 || cType == CardType.c32 || cType == CardType.c411 || cType == CardType.c422 || cType == CardType.c11122234 || cType == CardType.c1112223344) {
			ArrayList<Integer> a1 = CardTypeTool.getOrderBySame(c); // 我出的牌
			ArrayList<Integer> a2 = CardTypeTool.getOrderBySame(currentlist);// 当前最大牌
			if (CardTypeTool.getValue(a1.get(0)) < CardTypeTool.getValue(a2.get(0))) {
				return 0;
			}
		}
		return 1;
	}

	// 拆对子
	public static void getTwo(List<Integer> list, Model model) {
		// ArrayList<Short> del = new Vector<Card>();// 要删除的Cards
		// 连续2张相同
		for (int i = 0, len = list.size(); i < len; i++) {
			if (i + 1 < len && CardTypeTool.getValue(list.get(i)) == CardTypeTool.getValue(list.get(i + 1))) {
				String s = list.get(i) + ",";
				s += list.get(i + 1);
				model.a2.add(s);
				// for (int j = i; j <= i + 1; j++)
				// del.add(list.get(j));
				i = i + 1;
			}
		}
		// list.removeAll(del);
	}

	// 拆3带
	public static void getThree(List<Integer> list, Model model) {
		// ArrayList<Short> del = new Vector<Card>();// 要删除的Cards
		// 连续3张相同
		for (int i = 0, len = list.size(); i < len; i++) {
			if (i + 2 < len && CardTypeTool.getValue(list.get(i)) == CardTypeTool.getValue(list.get(i + 2))) {
				String s = list.get(i) + ",";
				s += list.get(i + 1) + ",";
				s += list.get(i + 2);
				model.a3.add(s);
				// for (int j = i; j <= i + 2; j++)
				// del.add(list.get(j));
				i = i + 2;
			}
		}
		// list.removeAll(del);
	}

	// 拆炸弹
	public static void getBoomb(List<Integer> list, Model model) {
		// ArrayList<Short> del = new Vector<Card>();// 要删除的Cards
		if (list.size() < 1) {
			return;
		}
		// 王炸
		if (list.size() >= 2 && CardTypeTool.getValue(list.get(0)) == 17 && CardTypeTool.getValue(list.get(1)) == 16) {
			model.a4.add(list.get(0) + "," + list.get(1)); // 按名字加入
			// del.add(list.get(0));
			// del.add(list.get(1));
		}
		// 如果王不构成炸弹咋先拆单
		/*
		 * if (Common.getColor(list.get(0)) == 5 && Common.getColor(list.get(1))
		 * != 5) { //del.add(list.get(0)); model.a1.add(list.get(0)); }
		 */
		// list.removeAll(del);
		// 一般的炸弹
		for (int i = 0, len = list.size(); i < len; i++) {
			if (i + 3 < len && CardTypeTool.getValue(list.get(i)) == CardTypeTool.getValue(list.get(i + 3))) {
				String s = list.get(i) + ",";
				s += list.get(i + 1) + ",";
				s += list.get(i + 2) + ",";
				s += list.get(i + 3);
				model.a4.add(s);
				// for (int j = i; j <= i + 3; j++)
				// del.add(list.get(j));
				i = i + 3;
			}
		}
		// list.removeAll(del);
	}

	// 拆双顺
	public static void getTwoTwo(List<Integer> list, Model model) {
		// List<String> del = new Vector<String>();// 要删除的Cards
		// 从model里面的对子找
		List<String> l = model.a2;
		if (l.size() < 2) {
			return;
		}
		Short s[] = new Short[l.size()];
		for (int i = 0, len = l.size(); i < len; i++) {
			String[] name = l.get(i).split(",");
			s[i] = Short.parseShort(name[0]);
		}
		// s0,1,2,3,4 13,9,8,7,6
		for (int i = 0, len = l.size(); i < len; i++) {
			int k = i;
			for (int j = i; j < len; j++) {
				if (s[i] - s[j] == j - i) {
					k = j;
				}
			}
			if (k - i >= 2)// k=4 i=1
			{// 说明从i到k是连队
				String ss = "";
				for (int j = i; j < k; j++) {
					ss += l.get(j) + ",";
					// del.add(l.get(j));
				}
				ss += l.get(k);
				model.a112233.add(ss);
				// del.add(l.get(k));
				i = k;
			}
		}
		// l.removeAll(del);
	}

	// 拆双顺
	public static void getTwoTwo(List<Integer> list, Model model, int length) {
		// List<String> del = new Vector<String>();// 要删除的Cards
		// 从model里面的对子找
		List<String> l = model.a2;
		if (l.size() < 2) {
			return;
		}
		Short s[] = new Short[l.size()];
		for (int i = 0, len = l.size(); i < len; i++) {
			String[] name = l.get(i).split(",");
			s[i] = Short.parseShort(name[0]);
		}
		// s0,1,2,3,4 13,9,8,7,6
		List<String> twotwoIndex = calcLian(l, s, length);
		model.a112233.addAll(twotwoIndex);

	}

	public static List<String> calcLian(List<String> l, Short s[], int length) {
		List<String> twotwoIndex = new ArrayList<>();
		for (int i = 0; i < s.length; i++) {
			if (i + length > s.length) {
				break;
			}

			int s1 = 0;
			List<Integer> twotwo = new ArrayList<>();
			int liannum = 0;
			for (int j = 0; j < length; j++) {
				int j_index = i + j;
				int s2 = s[j_index];
				if (getValue(s1) - getValue(s2) == 1) {
					liannum++;
				} else {
					liannum = 1;
					twotwo.clear();
				}
				s1 = s2;
				twotwo.add(j_index);
				if (liannum == length) {
					StringBuffer sb = new StringBuffer();
					for (int index : twotwo) {
						sb.append(l.get(index)).append(",");
					}
					if (sb.length() > 0) {
						sb.deleteCharAt(sb.length() - 1);
					}
					twotwoIndex.add(sb.toString());
					break;
				}
				// twotwoIndex.add(l.get(i));
				// twotwoIndex.add(l.get(i+1));
			}

		}
		return twotwoIndex;
	}

	// 拆飞机
	public static void getPlane(List<Integer> list, Model model, int length) {
		// List<String> del = new Vector<String>();// 要删除的Cards
		// 从model里面的3带找
		List<String> l = model.a3;
		if (l.size() < 2) {
			return;
		}
		Short s[] = new Short[l.size()];
		for (int i = 0, len = l.size(); i < len; i++) {
			String[] name = l.get(i).split(",");
			s[i] = Short.parseShort(name[0]);
		}
		List<String> twotwoIndex = calcLian(l, s, length);
		model.a111222.addAll(twotwoIndex);

		// l.removeAll(del);
	}

	// 拆飞机
	public static void getPlane(List<Integer> list, Model model) {
		// List<String> del = new Vector<String>();// 要删除的Cards
		// 从model里面的3带找
		List<String> l = model.a3;
		if (l.size() < 2) {
			return;
		}
		Short s[] = new Short[l.size()];
		for (int i = 0, len = l.size(); i < len; i++) {
			String[] name = l.get(i).split(",");
			s[i] = Short.parseShort(name[0]);
		}
		for (int i = 0, len = l.size(); i < len; i++) {
			int k = i;
			for (int j = i; j < len; j++) {
				if (s[i] - s[j] == j - i) {
					k = j;
				}
			}
			if (k != i) {// 说明从i到k是飞机
				String ss = "";
				for (int j = i; j < k; j++) {
					ss += l.get(j) + ",";
					// del.add(l.get(j));
				}
				ss += l.get(k);
				model.a111222.add(ss);
				// del.add(l.get(k));
				i = k;
			}
		}
		// l.removeAll(del);
	}

	// 拆连子
	public static void get123(List<Integer> list, Model model) {
		// ArrayList<Short> del = new Vector<Card>();// 要删除的Cards
		if (list.size() < 5) {
			return;
		}
		// 先要把所有不重复的牌归为一类，防止3带，对子影响
		ArrayList<Integer> list2 = new ArrayList<Integer>(list);
		ArrayList<Integer> temp = new ArrayList<Integer>();
		List<Integer> integers = new Vector<Integer>();
		for (Integer card : list2) {
			if (integers.indexOf(CardTypeTool.getValue(card)) < 0 && CardTypeTool.getColor(card) != 5 && CardTypeTool.getValue(card) != 15) {
				integers.add(CardTypeTool.getValue(card));
				temp.add(card);
			}
		}
		CardTypeTool.setOrder(temp);
		for (int i = 0, len = temp.size(); i < len; i++) {
			int k = i;
			for (int j = i; j < len; j++) {
				if (CardTypeTool.getValue(temp.get(i)) - CardTypeTool.getValue(temp.get(j)) == j - i) {
					k = j;
				}
			}
			if (k - i >= 4) {
				String s = "";
				for (int j = i; j < k; j++) {
					s += temp.get(j) + ",";
				}
				s += temp.get(k);
				model.a123.add(s);
				i = k;
			}
		}
		// list.removeAll(del);
	}

	// 拆连子
	public static void get123(List<Integer> list, Model model, int length) {
		// ArrayList<Short> del = new Vector<Card>();// 要删除的Cards
		if (list.size() < 5) {
			return;
		}
		// 先要把所有不重复的牌归为一类，防止3带，对子影响
		ArrayList<Integer> list2 = new ArrayList<Integer>(list);
		ArrayList<Integer> temp = new ArrayList<Integer>();
		List<Integer> integers = new Vector<Integer>();
		for (Integer card : list2) {
			if (integers.indexOf(CardTypeTool.getValue(card)) < 0 && CardTypeTool.getColor(card) != 5 && CardTypeTool.getValue(card) != 15) {
				integers.add(CardTypeTool.getValue(card));
				temp.add(card);
			}
		}
		CardTypeTool.setOrder(temp);
		for (int i = 0, len = temp.size(); i < len; i++) {
			int k = i;
			for (int j = i; j < len; j++) {
				if (CardTypeTool.getValue(temp.get(i)) - CardTypeTool.getValue(temp.get(j)) == j - i) {
					k = j;
				}
			}
			if (k - i >= 4) {
				String s = "";
				for (int j = i; j < k; j++) {
					s += temp.get(j) + ",";
				}
				s += temp.get(k);
				String[] zArr = s.split(",");
				for (int z = 0; z < zArr.length; z++) {
					if (z + length > zArr.length) {
						break;
					}
					StringBuffer sb = new StringBuffer();
					for (int y = 0; y < length; y++) {
						String zs = zArr[z + y];
						sb.append(zs).append(",");
					}
					if (sb.length() > 0) {
						sb.deleteCharAt(sb.length() - 1);
					}
					model.a123.add(sb.toString());
				}
				i = k;
			}
		}
		// list.removeAll(del);
	}

	// 拆单牌
	public static void getSingle(List<Integer> list, Model model) {
		// ArrayList<Short> del = new Vector<Card>();// 要删除的Cards
		// 1
		for (int i = 0, len = list.size(); i < len; i++) {
			model.a1.add(list.get(i).toString());
			// del.add(list.get(i));
		}
		CardTypeTool.delSingle(model.a2, model);
		CardTypeTool.delSingle(model.a3, model);
		CardTypeTool.delSingle(model.a4, model);
		CardTypeTool.delSingle(model.a123, model);
		CardTypeTool.delSingle(model.a112233, model);
		CardTypeTool.delSingle(model.a111222, model);
		// list.removeAll(del);
	}

	// 取单
	public static void delSingle(List<String> list, Model model) {
		for (int i = 0, len = list.size(); i < len; i++) {
			String s[] = list.get(i).split(",");
			for (int j = 0; j < s.length; j++) {
				model.a1.remove(s[j]);
			}
		}
	}

	// 去除独立牌型
	public static void checkModel(List<Integer> list, Model model1, Model modelSingle) {
		// 找出与其他不相关的牌型
		for (int i = 0, len = model1.a2.size(); i < len; i++) {
			int flag = 0;
			// Log.i("mylog","..."+ model1.a2.get(i));
			String s[] = model1.a2.get(i).split(",");
			// flag+=checkModel_1(model1.a2, s);
			flag += checkModel_1(model1.a3, s);
			flag += checkModel_1(model1.a4, s);
			flag += checkModel_1(model1.a112233, s);
			flag += checkModel_1(model1.a111222, s);
			flag += checkModel_1(model1.a123, s);
			// Log.i("mylog", "a2:flag"+flag);
			if (flag == 0) {
				modelSingle.a2.add(model1.a2.get(i));
				list.removeAll(CardTypeTool.getCardsByName(list, model1.a2.get(i)));
			}
		}
		model1.a2.removeAll(modelSingle.a2);
		for (int i = 0, len = model1.a3.size(); i < len; i++) {
			int flag = 0;
			String s[] = model1.a3.get(i).split(",");
			flag += checkModel_1(model1.a2, s);
			// flag+=checkModel_1(model1.a3, s);
			flag += checkModel_1(model1.a4, s);
			flag += checkModel_1(model1.a112233, s);
			flag += checkModel_1(model1.a111222, s);
			flag += checkModel_1(model1.a123, s);
			if (flag == 0) {
				modelSingle.a3.add(model1.a3.get(i));
				list.removeAll(CardTypeTool.getCardsByName(list, model1.a3.get(i)));

			}
		}
		model1.a3.removeAll(modelSingle.a3);
		for (int i = 0, len = model1.a4.size(); i < len; i++) {
			int flag = 0;
			String s[] = model1.a4.get(i).split(",");
			flag += checkModel_1(model1.a2, s);
			flag += checkModel_1(model1.a3, s);
			// flag+=checkModel_1(model1.a4, s);
			flag += checkModel_1(model1.a112233, s);
			flag += checkModel_1(model1.a111222, s);
			flag += checkModel_1(model1.a123, s);
			if (flag == 0) {
				modelSingle.a4.add(model1.a4.get(i));
				list.removeAll(CardTypeTool.getCardsByName(list, model1.a4.get(i)));
			}
		}
		model1.a4.removeAll(modelSingle.a4);
		for (int i = 0, len = model1.a112233.size(); i < len; i++) {
			int flag = 0;
			String s[] = model1.a112233.get(i).split(",");
			flag += checkModel_1(model1.a2, s);
			flag += checkModel_1(model1.a3, s);
			flag += checkModel_1(model1.a4, s);
			// flag+=checkModel_1(model1.a112233, s);
			flag += checkModel_1(model1.a111222, s);
			flag += checkModel_1(model1.a123, s);
			if (flag == 0) {
				modelSingle.a112233.add(model1.a112233.get(i));
				list.removeAll(CardTypeTool.getCardsByName(list, model1.a112233.get(i)));
			}
		}
		model1.a112233.removeAll(modelSingle.a112233);
		for (int i = 0, len = model1.a111222.size(); i < len; i++) {
			int flag = 0;
			String s[] = model1.a111222.get(i).split(",");
			flag += checkModel_1(model1.a2, s);
			flag += checkModel_1(model1.a3, s);
			flag += checkModel_1(model1.a4, s);
			flag += checkModel_1(model1.a112233, s);
			// flag+=checkModel_1(model1.a111222, s);
			flag += checkModel_1(model1.a123, s);
			if (flag == 0) {
				modelSingle.a111222.add(model1.a111222.get(i));
				list.removeAll(CardTypeTool.getCardsByName(list, model1.a111222.get(i)));
			}
		}
		model1.a111222.removeAll(modelSingle.a111222);
		for (int i = 0, len = model1.a123.size(); i < len; i++) {
			int flag = 0;
			String s[] = model1.a123.get(i).split(",");
			flag += checkModel_1(model1.a2, s);
			flag += checkModel_1(model1.a3, s);
			flag += checkModel_1(model1.a4, s);
			flag += checkModel_1(model1.a112233, s);
			flag += checkModel_1(model1.a111222, s);
			// flag+=checkModel_1(model1.a123, s);
			if (flag == 0) {
				modelSingle.a123.add(model1.a123.get(i));
				list.removeAll(CardTypeTool.getCardsByName(list, model1.a123.get(i)));
			}
		}
		model1.a123.removeAll(modelSingle.a123);
	}

	public static int checkModel_1(List<String> list, String[] s) {
		for (int j = 0, len2 = list.size(); j < len2; j++) {
			String ss[] = list.get(j).split(",");
			for (int k = 0; k < ss.length; k++) {
				for (int m = 0; m < s.length; m++) {
					if (s[m].equals(ss[k])) {
						return 1;
					}
				}
			}
		}
		return 0;
	}

	// 主动出牌
	public static void showCards(Model model, List<Integer> to, List<Integer> from) {

		List<String> list = new Vector<String>();
		if (model.a123.size() > 0) {
			list.add(model.a123.get(model.a123.size() - 1));
		}
		// 有单出单 (除开3带，飞机能带的单牌)
		else if (model.a1.size() > (model.a111222.size() * 2 + model.a3.size()) && CardTypeTool.getValueByName(model.a1.get(model.a1.size() - 1)) < 15) {
			list.add(model.a1.get(model.a1.size() - 1));
		} else if (model.a1.size() > (model.a111222.size() * 2 + model.a3.size())) {
			list.add(model.a1.get(0));
		}
		// 有对子出对子 (除开3带，飞机)
		else if (model.a2.size() > (model.a111222.size() * 2 + model.a3.size()) && CardTypeTool.getValueByName(model.a2.get(model.a2.size() - 1)) < 15) {
			list.add(model.a2.get(model.a2.size() - 1));
		}
		// 有3带就出3带，没有就出光3
		else if (model.a3.size() > 0 && CardTypeTool.getValueByName(model.a3.get(model.a3.size() - 1)) < 15) {
			// 3带单,且非关键时刻不能带王，2
			if (model.a1.size() > 0) {
				list.add(model.a1.get(model.a1.size() - 1));
				if (model.a1.size() > 0) {
					list.add(model.a1.get(model.a1.size() - 1));
				}// 3带对
			} else if (model.a2.size() > 0) {
				list.add(model.a2.get(model.a2.size() - 1));
			}
			list.add(model.a3.get(model.a3.size() - 1));
		}// 有双顺出双顺
		else if (model.a112233.size() > 0) {
			list.add(model.a112233.get(model.a112233.size() - 1));
		}// 有飞机出飞机
		else if (model.a111222.size() > 0) {
			String name[] = model.a111222.get(0).split(",");
			// 带单
			if (name.length / 3 <= model.a1.size()) {
				list.add(model.a111222.get(model.a111222.size() - 1));
				for (int i = 0; i < name.length / 3; i++) {
					list.add(model.a1.get(i));
				}
			} else if (name.length / 3 <= model.a2.size())// 带双
			{
				list.add(model.a111222.get(model.a111222.size() - 1));
				for (int i = 0; i < name.length / 3; i++) {
					list.add(model.a2.get(i));
				}
			}

		} else if (model.a1.size() > (model.a111222.size() * 2 + model.a3.size())) {
			list.add(model.a1.get(model.a1.size() - 1));
		} else if (model.a2.size() > (model.a111222.size() * 2 + model.a3.size())) {
			list.add(model.a2.get(model.a2.size() - 1));
		} else if (CardTypeTool.getValueByName(model.a3.get(0)) < 15 && model.a3.size() > 0) {
			// 3带单,且非关键时刻不能带王，2
			if (model.a1.size() > 0) {
				list.add(model.a1.get(model.a1.size() - 1));
			}// 3带对
			else if (model.a2.size() > 0) {
				list.add(model.a2.get(model.a2.size() - 1));
			}
			list.add(model.a3.get(model.a3.size() - 1));
		}
		// 有炸弹出炸弹
		else if (model.a4.size() > 0) {
			// 4带2,1
			int sizea1 = model.a1.size();
			int sizea2 = model.a2.size();
			if (sizea1 >= 2) {
				list.add(model.a1.get(sizea1 - 1));
				list.add(model.a1.get(sizea1 - 2));
				list.add(model.a4.get(0));

			} else if (sizea2 >= 2) {
				list.add(model.a2.get(sizea1 - 1));
				list.add(model.a2.get(sizea1 - 2));
				list.add(model.a4.get(0));

			} else {// 直接炸
				list.add(model.a4.get(0));
			}
		}
		for (String s : list) {
			to.addAll(CardTypeTool.getCardsByName(from, s));
		}

	}

	/**
	 * @param model
	 * @param to
	 *            走出的牌
	 * @param from
	 *            自己已有的牌
	 * @param oppo
	 *            对手出的牌
	 */
	public static void showCards2(Model model, List<Integer> to, List<Integer> from, List<Integer> oppo) {
		// oppo是对手出的牌,from是自己已有的牌,to是药走出的牌
		List<String> list = new Vector<String>();// 装要走出的牌的name
		CardType cType = CardTypeTool.jugdeType(oppo);
		// 按重复数排序,这样只需比较第一张牌
		oppo = CardTypeTool.getOrder2(oppo);
		switch (cType) {
		case c1:
			for (int len = model.a1.size(), i = len - 1; i >= 0; i--) {
				if (CardTypeTool.getValueByName(model.a1.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
					list.add(model.a1.get(i));
					break;
				}
			}

			if (list.size() == 0) {
				for (int i = 0, leni = from.size(); i < leni; i++) {
					if (CardTypeTool.getValue(from.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
						to.add(from.get(i));
						return;
					}
				}
			}

			break;
		case c2:
			for (int len = model.a2.size(), i = len - 1; i >= 0; i--) {
				if (CardTypeTool.getValueByName(model.a2.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
					list.add(model.a2.get(i));
					break;
				}
			}

			if (list.size() == 0) {
				for (int len = model.a3.size(), i = len - 1; i >= 0; i--) {

					if (CardTypeTool.getValueByName(model.a3.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
						List<Integer> t = CardTypeTool.getCardsByName(from, model.a3.get(i));
						to.add(t.get(0));
						to.add(t.get(1));
						return;
					}
				}
			}

			break;
		case c3:

			for (int len = model.a3.size(), i = len - 1; i >= 0; i--) {
				if (CardTypeTool.getValueByName(model.a3.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
					list.add(model.a3.get(i));
					break;
				}
			}
			break;
		case c31:

			int len1 = model.a3.size();
			int len2 = model.a1.size();
			if (!(len1 < 1 || len2 < 1)) {
				for (int len = len1, i = len - 1; i >= 0; i--) {
					if (CardTypeTool.getValueByName(model.a3.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
						list.add(model.a3.get(i));
						break;
					}
				}
				if (list.size() > 0) {
					list.add(model.a1.get(len2 - 1));
				}
			}
			break;
		case c32:

			len1 = model.a3.size();
			len2 = model.a2.size();
			if (!(len1 < 1 || len2 < 1)) {
				for (int len = len1, i = len - 1; i >= 0; i--) {
					if (CardTypeTool.getValueByName(model.a3.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
						list.add(model.a3.get(i));
						break;
					}
				}
				if (list.size() > 0) {
					list.add(model.a2.get(len2 - 1));
				}
			}
			break;
		case c411:

			len1 = model.a4.size();
			len2 = model.a1.size();
			if (!(len1 < 1 || len2 < 2)) {
				for (int len = len1, i = len - 1; i >= 0; i--) {
					if (CardTypeTool.getValueByName(model.a4.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
						list.add(model.a4.get(i));
						break;
					}
				}
				if (list.size() > 0) {
					list.add(model.a1.get(len2 - 1));
					list.add(model.a1.get(len2 - 2));
				}
			}
			break;
		case c422:

			len1 = model.a4.size();
			len2 = model.a2.size();
			if (!(len1 < 1 || len2 < 2)) {
				for (int len = len1, i = len - 1; i >= 0; i--) {
					if (CardTypeTool.getValueByName(model.a4.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
						list.add(model.a4.get(i));
						break;
					}
				}
				if (list.size() > 0) {
					list.add(model.a2.get(len2 - 1));
					list.add(model.a2.get(len2 - 2));
				}
			}
			break;
		case c123:

			for (int len = model.a123.size(), i = len - 1; i >= 0; i--) {
				String[] s = model.a123.get(i).split(",");
				if (s.length == oppo.size() && CardTypeTool.getValueByName(model.a123.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
					list.add(model.a123.get(i));
					break;
				}
			}
			break;
		case c1122:

			for (int len = model.a112233.size(), i = len - 1; i >= 0; i--) {
				String[] s = model.a112233.get(i).split(",");
				if (s.length == oppo.size() && CardTypeTool.getValueByName(model.a112233.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
					list.add(model.a112233.get(i));
					break;
				}
			}
			break;
		case c11122234:
			Card_index card_index_from = new Card_index();
			for (int i = 0; i < 4; i++) {
				card_index_from.a[i] = new Vector<Integer>();
			}
			// 求出各种数字出现频率
			CardTypeTool.getMax(card_index_from, from); // a[0,1,2,3]分别表示重复1,2,3,4次的牌

			Card_index card_index_oppo = new Card_index();
			for (int i = 0; i < 4; i++) {
				card_index_oppo.a[i] = new Vector<Integer>();
			}
			// 求出各种数字出现频率
			CardTypeTool.getMax(card_index_oppo, oppo); // a[0,1,2,3]分别表示重复1,2,3,4次的牌
			int card_index_from_size = card_index_from.a[2].size();
			List<Integer> threeList = new ArrayList<>();
			threeList.addAll(card_index_oppo.a[2]);
			threeList.addAll(card_index_oppo.a[3]);

			int card_index_oppo_size = 1;
			for (int i = 0; i < threeList.size(); i++) {
				if (i + 1 >= threeList.size()) {
					break;
				}
				int left = threeList.get(i);
				int right = threeList.get(i + 1);
				if (right - left == 1) {
					card_index_oppo_size++;
				}

			}
			// int card_index_oppo_size = card_index_oppo.a[2].size() +
			// card_index_oppo.a[3].size();// 3334444

			CardTypeTool.getThree(from, model);
			CardTypeTool.getPlane(from, model, card_index_oppo_size);
			if (card_index_from_size >= card_index_oppo_size) {
				len1 = model.a111222.size();
				for (int o = len1 - 1; o >= 0; o--) {
					// String[] s = model.a111222.get(o).split(",");
					String plane = model.a111222.get(o);
					if (CardTypeTool.getValueByName(model.a111222.get(o)) > CardTypeTool.getValue(oppo.get(0))) {
						// 能打的起
						int disCount = card_index_oppo_size * 3 + card_index_oppo_size * 2;
						if (from.size() <= disCount) {
							// 可以一次性出完牌
							for (int i = 0; i < from.size(); i++) {
								list.add(from.get(i) + "");

							}

						} else {
							List<Integer> play = StringUtil.explodeToIntList(plane);
							for (int value : play) {
								list.add(value + "");
							}
							// 需要带牌
							for (int i = 0; i < from.size(); i++) {
								if (list.size() >= disCount) {
									break;
								}
								if (!list.contains(from.get(i) + "")) {
									list.add(from.get(i) + "");

								}

							}

						}
						break;
					}

				}
			}
			// len1 = model.a111222.size();
			// len2 = model.a1.size();
			//
			// if (!(len1 < 1 || len2 < 2)) {
			// for (int i = len1 - 1; i >= 0; i--) {
			// String[] s = model.a111222.get(i).split(",");
			// if ((s.length / 3 <= len2) && (s.length * 4 == oppo.size()) &&
			// CardTypeTool.getValueByName(model.a111222.get(i)) >
			// CardTypeTool.getValue(oppo.get(0))) {
			// list.add(model.a111222.get(i));
			// for (int j = 1; j <= s.length / 3; j++) {
			// list.add(model.a1.get(len2 - j));
			// }
			// }
			// }
			// }

			break;
		case c1112223344:

			len1 = model.a111222.size();
			len2 = model.a2.size();

			if (!(len1 < 1 || len2 < 2)) {
				for (int i = len1 - 1; i >= 0; i--) {
					String[] s = model.a111222.get(i).split(",");
					if ((s.length / 3 <= len2) && (s.length * 4 == oppo.size()) && CardTypeTool.getValueByName(model.a111222.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
						list.add(model.a111222.get(i));
						for (int j = 1; j <= s.length / 3; j++) {
							list.add(model.a2.get(len2 - j));
						}
					}
				}
			}
			break;
		case c4:
			for (int len = model.a4.size(), i = len - 1; i >= 0; i--) {
				if (CardTypeTool.getValueByName(model.a4.get(i)) > CardTypeTool.getValue(oppo.get(0))) {
					list.add(model.a4.get(i));
					break;
				}
			}
		default:
			break;
		}
		if (list.size() == 0) {
			if (cType != CardType.c4) {
				if (model.a4.size() > 0) {
					list.add(model.a4.get(model.a4.size() - 1));
					for (String s : list) {
						to.addAll(CardTypeTool.getCardsByName(from, s));
					}
					return;
				}
			}
			to = null;
		} else {
			for (String s : list) {
				to.addAll(CardTypeTool.getCardsByName(from, s));
			}
		}
	}

	// 统计各种牌型权值，手数
	public static Model getBestModel(ArrayList<Integer> list2, Model oldModel, int[] n) {
		// a4 a3 a2 a123 a112233 a111222
		Model temp = new Model();
		// 处理炸弹
		for (int i = 0; i < n[0]; i++) {
			if (CardTypeTool.isExists(list2, oldModel.a4.get(i))) {
				temp.a4.add(oldModel.a4.get(i));
				list2.removeAll(CardTypeTool.getCardsByName(list2, oldModel.a4.get(i)));
			}
		}
		// 3带
		for (int i = 0; i < n[1]; i++) {
			if (CardTypeTool.isExists(list2, oldModel.a3.get(i))) {
				temp.a3.add(oldModel.a3.get(i));
				list2.removeAll(CardTypeTool.getCardsByName(list2, oldModel.a3.get(i)));
			}
		}
		// 对子
		for (int i = 0; i < n[2]; i++) {
			if (CardTypeTool.isExists(list2, oldModel.a2.get(i))) {
				temp.a2.add(oldModel.a2.get(i));
				list2.removeAll(CardTypeTool.getCardsByName(list2, oldModel.a2.get(i)));
			}
		}
		// 顺子
		for (int i = 0; i < n[3]; i++) {
			if (CardTypeTool.isExists(list2, oldModel.a123.get(i))) {
				temp.a123.add(oldModel.a123.get(i));
				list2.removeAll(CardTypeTool.getCardsByName(list2, oldModel.a123.get(i)));
			}
		}
		// 双顺
		for (int i = 0; i < n[4]; i++) {
			if (CardTypeTool.isExists(list2, oldModel.a112233.get(i))) {
				temp.a112233.add(oldModel.a112233.get(i));
				list2.removeAll(CardTypeTool.getCardsByName(list2, oldModel.a112233.get(i)));
			}
		}
		// 飞机
		for (int i = 0; i < n[5]; i++) {
			if (CardTypeTool.isExists(list2, oldModel.a111222.get(i))) {
				temp.a111222.add(oldModel.a111222.get(i));
				list2.removeAll(CardTypeTool.getCardsByName(list2, oldModel.a111222.get(i)));
			}
		}
		return temp;
	}

	// 通过name返回值
	public static int getValueByName(String ss) {
		String s[] = ss.split(",");
		return getValue(Short.parseShort(s[0]));
	}

	// 按照重复次数排序
	public static ArrayList<Integer> getOrder2(List<Integer> list) {
		ArrayList<Integer> list2 = new ArrayList<Integer>(list);
		ArrayList<Integer> list3 = new ArrayList<Integer>();
		// List<Integer> list4 = new Vector<Integer>();
		int len = list2.size();
		int a[] = new int[20];
		for (int i = 0; i < 20; i++) {
			a[i] = 0;
		}
		for (int i = 0; i < len; i++) {
			a[CardTypeTool.getValue(list2.get(i))]++;
		}
		int max = 0;
		for (int i = 0; i < 20; i++) {
			max = 0;
			for (int j = 19; j >= 0; j--) {
				if (a[j] > a[max]) {
					max = j;
				}
			}

			for (int k = 0; k < len; k++) {
				if (CardTypeTool.getValue(list2.get(k)) == max) {
					list3.add(list2.get(k));
				}
			}
			list2.remove(list3);
			a[max] = 0;
		}
		return list3;
	}

	// 计算手数
	public static int getTimes(Model model) {
		int count = 0;
		count += model.a4.size() + model.a3.size() + model.a2.size();
		count += model.a111222.size() + model.a112233.size() + model.a123.size();
		int temp = 0;
		temp = model.a1.size() - model.a3.size() * 2 - model.a4.size() * 3 - model.a111222.size() * 3;
		count += temp;
		return count;
	}

	// 计算权值 单1 对子2 带3 炸弹10 飞机7 双顺5 顺子4
	public static int getCountValues(Model model) {
		int count = 0;
		count += model.a1.size() + model.a2.size() * 2 + model.a3.size() * 3;
		count += model.a4.size() * 10 + model.a111222.size() * 7 + model.a112233.size() * 5 + model.a123.size() * 4;
		return count;
	}

	// 通过name得到card
	public static List<Integer> getCardsByName(List<Integer> list, String s) {
		String[] name = s.split(",");
		ArrayList<Integer> temp = new ArrayList<Integer>();
		int c = 0;
		for (int i = 0, len = list.size(); i < len; i++) {
			if (list.get(i) == Short.parseShort(name[c])) {
				temp.add(list.get(i));
				if (c == name.length - 1) {
					return temp;
				}
				c++;
				i = 0;
			}
		}
		return temp;
	}

	// 判断某牌型还存在list不
	public static Boolean isExists(ArrayList<Integer> list, String s) {
		String name[] = s.split(",");
		int c = 0;
		for (int i = 0, len = list.size(); i < len; i++) {
			if (list.get(i) == Short.parseShort(name[c])) {
				if (c == name.length - 1) {
					return true;
				}
				c++;
				i = 0;
			}
		}

		return false;
	}

	/**
	 * 按照重复次数排序
	 * 
	 * @param list
	 * @return
	 */
	public static ArrayList<Integer> getOrderBySame(List<Integer> list) {
		ArrayList<Integer> list2 = new ArrayList<Integer>(list);
		ArrayList<Integer> list3 = new ArrayList<Integer>();
		List<Integer> list4 = new Vector<Integer>();
		int len = list2.size();
		int a[] = new int[20];
		for (int i = 0; i < 20; i++) {
			a[i] = 0;
		}
		for (int i = 0; i < len; i++) {
			a[CardTypeTool.getValue(list2.get(i))]++;
		}
		int max = 0;
		for (int i = 0; i < 20; i++) {
			max = 0;
			for (int j = 19; j >= 0; j--) {
				if (a[j] > a[max]) {
					max = j;
				}
			}

			for (int k = 0; k < len; k++) {
				if (CardTypeTool.getValue(list2.get(k)) == max) {
					list3.add(list2.get(k));
				}
			}
			list2.remove(list3);
			a[max] = 0;
		}
		return list3;
	}

	/**
	 * 设定牌的顺序
	 * 
	 * @param list
	 */
	public static void setOrder(List<Integer> list) {
		Collections.sort(list, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				int a1 = CardTypeTool.getColor(o1);// 花色
				int a2 = CardTypeTool.getColor(o2);
				int b1 = CardTypeTool.getValue(o1);// 数值
				int b2 = CardTypeTool.getValue(o2);
				int flag = 0;
				flag = b2 - b1;
				if (flag == 0) {
					return a2 - a1;
				} else {
					return flag;
				}
			}
		});
	}

	/**
	 * 获取拥有4张一个
	 * 
	 * @param cards
	 * @return
	 */
	public static List<Integer> getBoomCount(List<Integer> cards) {
		setOrder(cards);
		List<Integer> boomCount = new ArrayList<>();
		List<Integer> boomTemp = new ArrayList<>();
		int nowValue = 0;
		for (int card : cards) {
			int value = getValue(card);
			if (nowValue == value) {
				if (!boomTemp.contains(nowValue)) {
					boomTemp.add(nowValue);

				}
				boomTemp.add(value);
			} else {
				boomTemp.clear();
			}
			nowValue = value;
			if (boomTemp.size() == 4) {
				// 4张一样的牌是炸弹
				boomCount.addAll(boomTemp);
			}

		}
		return boomCount;
	}

	/**
	 * 
	 * @param value
	 * @param cards
	 * @param addCards
	 * @param addNum
	 */
	public static void getCards(int value, List<Integer> cards, List<String> addCards, int addNum) {
		int find = 0;
		for (int cardValue : cards) {
			if (addCards.contains(cardValue)) {
				continue;
			}
			if (getValue(cardValue) == value) {
				addCards.add(cardValue + "");
				find++;
			}
			if (find >= addNum) {
				break;
			}
		}
	}

	/**
	 * 最大的牌
	 * 
	 * @param cards
	 * @return
	 */
	public static int getMax(List<Integer> cards) {
		int max = 0;
		for (int card : cards) {
			int value = getValue(card);
			if (value > getValue(max)) {
				max = card;
			}
		}
		return max;
	}
}

// /**
// * 判断牌型
// *
// * @author upstream
// *
// */
// class Card_index {
// List<Integer> a[] = new Vector[4];// 单张
// }
