package com.sy599.game.qipai.yzlc.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sy599.game.qipai.yzlc.bean.PaohzDisAction;
import com.sy599.game.qipai.yzlc.rule.PaohuziMingTangRule;
import com.sy599.game.util.LogUtil;

public enum PaohzCard {
	//小牌
	phz1(1, 1), phz2(2, 2), phz3(3, 3), phz4(4, 4), phz5(5, 5), phz6(6, 6), phz7(7, 7), phz8(8, 8), phz9(9, 9), phz10(10, 10),
	phz11(11, 1), phz12(12, 2), phz13(13, 3), phz14(14, 4), phz15(15, 5), phz16(16, 6), phz17(17, 7), phz18(18, 8), phz19(19, 9), phz20(20, 10),
	phz21(21, 1), phz22(22, 2), phz23(23, 3), phz24(24, 4), phz25(25, 5), phz26(26, 6), phz27(27, 7), phz28(28, 8), phz29(29, 9), phz30(30, 10),
	phz31(31, 1), phz32(32, 2), phz33(33, 3), phz34(34, 4), phz35(35, 5), phz36(36, 6), phz37(37, 7), phz38(38, 8), phz39(39, 9), phz40(40, 10),
	//大牌
	phz41(41, 101), phz42(42, 102), phz43(43, 103), phz44(44, 104), phz45(45, 105), phz46(46, 106), phz47(47, 107), phz48(48, 108), phz49(49, 109), phz50(50, 110),
	phz51(51, 101), phz52(52, 102), phz53(53, 103), phz54(54, 104), phz55(55, 105), phz56(56, 106), phz57(57, 107), phz58(58, 108), phz59(59, 109), phz60(60, 110),
	phz61(61, 101), phz62(62, 102), phz63(63, 103), phz64(64, 104), phz65(65, 105), phz66(66, 106), phz67(67, 107), phz68(68, 108), phz69(69, 109), phz70(70, 110),
	phz71(71, 101), phz72(72, 102), phz73(73, 103), phz74(74, 104), phz75(75, 105), phz76(76, 106), phz77(77, 107), phz78(78, 108), phz79(79, 109), phz80(80, 110);

	private int id;

	private int val;

	PaohzCard(int id, int val) {
		this.id = id;
		this.val = val;
	}

	public int getId() {
		return id;
	}

	public int getVal() {
		return val;
	}

	public boolean isBig() {
		return val > 100;
	}

	/**
	 * 如果是大牌 返回同值小牌 如果是小牌 返回同值大牌
	 *
	 * @return
	 */
	public int getOtherVal() {
		if (isBig()) {
			return getPai();
		}
		else {
			return 100 + getPai();
		}
	}

	public int getPai() {
		return val % 100;
	}

	public int getCase() {
		if (isBig()) {
			return 100;
		}
		else {
			return 0;
		}
	}

	public static PaohzCard getPaohzCard(int id) {
		if (id == 0) {
			return null;
		}
		return PaohzCard.valueOf(PaohzCard.class, "phz" + id);
	}


	private static void prinl() {
		int k = 1;
		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 10; j++) {
				if (i <= 4) {
					System.out.println("phz" + k + "(" + k + "," + j + "),");

				}
				else {
					System.out.println("phz" + k + "(" + k + "," + (100 + j) + "),");

				}
				k++;
			}
		}
	}

	public static final String[] xiao_zi = { "一", "二", "三", "四", "五", "六", "七", "八", "九", "十" };

	public static final String[] da_zi = { "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖", "拾" };

	public String toString() {
		if (getVal() <= 10) {
			return xiao_zi[getVal() - 1];
		}
		else {
			return da_zi[getVal() % 100 - 1];
		}
	}

	public static List<PaohzCard> getPaohzCardsByVal(int val) {
		List<PaohzCard> cards = new ArrayList<>();
		if (val >= 1 && val <= 10) {
			for (int i = 0; i < 4; i++) {
				cards.add(getPaohzCard(val + 10 * i));
			}
		}
		if (val >= 101 && val <= 110) {
			for (int i = 0; i < 4; i++) {
				cards.add(getPaohzCard(val % 100 + 10 * i + 40));
			}
		}
		return cards;
	}


	/**
	 *@description 比较两个组合的大小
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/3/14
	 */
	public static boolean compareToBig(int action, List<PaohzCard> src, List<PaohzCard> desc) {
		return CompareAction.compare(action , src, desc);
	}

	/**
	 *@description 根据组合找到action 2单牌,3对子,4大面,6小面,7食盒,8坎,10龙,11顺子
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/3/14
	 */
	public static int findAction(List<PaohzCard> src){
		return new FindActionFilter().find(src).build();
	}

	/**
	 *@description 校验action类型, 根据牌型找到所属操作
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/3/14
	 */
	public static class FindActionFilter {
		//已知操作
		public static int[] actions = {
				PaohzDisAction.action_single_card,
				PaohzDisAction.action_double_card,
				PaohzDisAction.action_big_face,
				PaohzDisAction.action_small_face,
				PaohzDisAction.action_eat_box,
				PaohzDisAction.action_kan,
				PaohzDisAction.action_dragon,
				PaohzDisAction.action_straight };

		//内容比较器
		private static List<Function<List<PaohzCard>, Boolean>> predicates = Arrays.asList(
				v -> {        //单牌：单独一张字牌。比如单独一张二。
					return v.size() == 1;
				}
				,
				v -> {        //对牌：两张一样的牌。比如两张二。
					return v.size() == 2 && v.get(0).getVal() == v.get(1).getVal();
				}
				,
				v -> {        //大面：两张大写的字数牌加一张小写的同样字数牌。比如“贰贰二”。
					long count;
					boolean b = v.size() == 3 && (count = v.stream().filter(v1 -> v1.isBig()).count()) > 0 && count != v.size() && count > v.stream().filter(v1 -> !v1.isBig()).count()
							&& v.stream().map(v1 -> v1.getPai()).collect(Collectors.toSet()).size() == 1;
					//大在前
					return sortAndReturn(v, !b, b);
				},
				v -> {        //小面：两张小写的字数牌加一张大写的同样字数牌。比如“二二贰”。
					long count;
					boolean b = v.size() == 3 && (count = v.stream().filter(v1 -> v1.isBig()).count()) > 0 && count != v.size() && count < v.stream().filter(v1 -> !v1.isBig()).count()
							&& v.stream().map(v1 -> v1.getPai()).collect(Collectors.toSet()).size() == 1;
					//小在前
					return sortAndReturn(v, b, b);
				},
				v -> {        //食盒：两张大写的字数牌加两张小写的同样字数牌。比如“贰贰二二”。
					boolean b = v.size() == 4 && v.stream().filter(v1 -> v1.isBig()).count() == v.stream().filter(v1 -> !v1.isBig()).count() && v.stream().map(v1 -> v1.getPai()).collect(Collectors.toSet()).size() == 1;;
					//大在前
					return sortAndReturn(v, !b, b);
				},
				v -> {        //坎：三张一样的牌。比如“贰贰贰”、“三三三”。坎可以带同样字数不同大小写的牌，比如“三三三叁”、“陸陸陸六六”、“陸陸陸六六六”。
					long bigCount = 0;
				 	boolean b = v.size() >= 3 && ((bigCount = v.stream().filter(v1 -> v1.isBig()).count()) == 3 || (v.size() - bigCount == 3)) && v.stream().map(v1 -> v1.getPai()).collect(Collectors.toSet()).size() == 1;
					//小or大在前
					return sortAndReturn(v, bigCount <= (v.size() - bigCount), b);
				},
				v -> {        //龙：四张一样的牌。比如“六六六六”、“玖玖玖玖”。龙可以带同样字数不同大小写的牌，比如“三三三三叁”、“陸陸陸陸六六”、“陸陸陸陸六六
					long bigCount = 0;
					boolean b = v.size() >= 4 && ((bigCount = v.stream().filter(v1 -> v1.isBig()).count()) == 4 || (v.size() - bigCount == 4)) && v.stream().map(v1 -> v1.getPai()).collect(Collectors.toSet()).size() == 1;
					//小or大在前
					return sortAndReturn(v, bigCount <= (v.size() - bigCount), b);
				},
				v -> {        //顺子：三张字数相连并且大小写一样的牌。比如“六七八”、“陸柒捌”。（必须是3张，多了少了都不行）
					boolean b = v.size() == 3 && PaohuziMingTangRule.isSerialNumber(v.stream().mapToInt(v1 -> v1.getVal()).toArray(), false);
					//小在前
					return sortAndReturn(v, b, b);
				}
		);

		private int index = 0;
		private boolean res;

		public FindActionFilter find(List<PaohzCard> src) {
//			LogUtil.printDebug("排列组合before:{}", src);
			while (index < actions.length && !(res = predicates.get(index++).apply(src))) {
			}
//			LogUtil.printDebug("排列组合after:{}", src);
			return FindActionFilter.this;
		}

		public int build() {
			return res ? actions[index - 1] : -1;
		}

		private static boolean sortAndReturn(List<PaohzCard> v, boolean asc, boolean returnVal) {
			if(returnVal){
				if(asc)
					v.sort((v1, v2) -> Integer.valueOf(v1.getVal()).compareTo(v2.getVal()));
				else
					v.sort((v1, v2) -> Integer.valueOf(v2.getVal()).compareTo(v1.getVal()));
			}
			return returnVal;
		}

	}

	/**
	 *@description 比较两个操作之间哪一个大
	 *@param
	 *@return
	 *@author Guang.OuYang
	 *@date 2020/3/14
	 */
	public static class CompareAction {
		//已知操作
		private static int[] actions = FindActionFilter.actions;
		//内容比较器, 虽然条件一致, 暂且剥离出来, 防止后续需求变更
		private static Map<Integer, CompareActor<PaohzCard, Boolean>> predicates = new HashMap<Integer,CompareActor<PaohzCard, Boolean>>() {
			{
				this.put(actions[0], (src, desc) -> {        //单牌：单独一张字牌。比如单独一张二。
					return src.get(0).getPai() > desc.get(0).getPai();
				});
				this.put(actions[1], (src, desc) -> {        //对牌：两张一样的牌。比如两张二。
					return src.get(0).getPai() > desc.get(0).getPai();
				});
				this.put(actions[2], (src, desc) -> {        //大面：两张大写的字数牌加一张小写的同样字数牌。比如“贰贰二”。
					return src.get(0).getPai() > desc.get(0).getPai();
				});
				this.put(actions[3], (src, desc) -> {        //小面：两张小写的字数牌加一张大写的同样字数牌。比如“二二贰”。
					return src.get(0).getPai() > desc.get(0).getPai();
				});
				this.put(actions[4], (src, desc) -> {        //食盒：两张大写的字数牌加两张小写的同样字数牌。比如“贰贰二二”。
					return src.get(0).getPai() > desc.get(0).getPai();
				});
				this.put(actions[5], (src, desc) -> {        //坎：三张一样的牌。比如“贰贰贰”、“三三三”。坎可以带同样字数不同大小写的牌，比如“三三三叁”、“陸陸陸六六”、“陸陸陸六六六”。
					return src.get(0).getPai() > desc.get(0).getPai();
				});
				this.put(actions[6], (src, desc) -> {        //龙：四张一样的牌。比如“六六六六”、“玖玖玖玖”。龙可以带同样字数不同大小写的牌，比如“三三三三叁”、“陸陸陸陸六六”、“陸陸陸陸六六
					return src.get(0).getPai() > desc.get(0).getPai();
				});
				this.put(actions[7], (src, desc) -> {        //顺子：三张字数相连并且大小写一样的牌。比如“六七八”、“陸柒捌”。（必须是3张，多了少了都不行）
					return src.get(0).getPai() > desc.get(0).getPai();
				});
			}
		};

		public static boolean compare(int action , List<PaohzCard> src, List<PaohzCard> desc) {
			return src.stream().filter(v -> v.isBig()).count() == desc.stream().filter(v -> v.isBig()).count() && src.size() == desc.size() && predicates.get(action).apply(src, desc);
		}

		public interface CompareActor<T, R> {
			public R apply(List<T> src, List<T> desc);
		}
	}
}
