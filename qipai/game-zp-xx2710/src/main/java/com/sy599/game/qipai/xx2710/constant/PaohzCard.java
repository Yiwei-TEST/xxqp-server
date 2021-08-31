package com.sy599.game.qipai.xx2710.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum PaohzCard {
	phz1(1, 1), phz2(2, 2), phz3(3, 3), phz4(4, 4), phz5(5, 5), phz6(6, 6), phz7(7, 7), phz8(8, 8), phz9(9, 9), phz10(10, 10),
	phz11(11, 1), phz12(12, 2), phz13(13, 3), phz14(14, 4), phz15(15, 5), phz16(16, 6), phz17(17, 7), phz18(18, 8), phz19(19, 9), phz20(20, 10),
	phz21(21, 1), phz22(22, 2), phz23(23, 3), phz24(24, 4), phz25(25, 5), phz26(26, 6), phz27(27, 7), phz28(28, 8), phz29(29, 9), phz30(30, 10),
	phz31(31, 1), phz32(32, 2), phz33(33, 3), phz34(34, 4), phz35(35, 5), phz36(36, 6), phz37(37, 7), phz38(38, 8), phz39(39, 9), phz40(40, 10),
	phz41(41, 101), phz42(42, 102), phz43(43, 103), phz44(44, 104), phz45(45, 105), phz46(46, 106), phz47(47, 107), phz48(48, 108), phz49(49, 109), phz50(50, 110),
	phz51(51, 101), phz52(52, 102), phz53(53, 103), phz54(54, 104), phz55(55, 105), phz56(56, 106), phz57(57, 107), phz58(58, 108), phz59(59, 109), phz60(60, 110),
	phz61(61, 101), phz62(62, 102), phz63(63, 103), phz64(64, 104), phz65(65, 105), phz66(66, 106), phz67(67, 107), phz68(68, 108), phz69(69, 109), phz70(70, 110),
	phz71(71, 101), phz72(72, 102), phz73(73, 103), phz74(74, 104), phz75(75, 105), phz76(76, 106), phz77(77, 107), phz78(78, 108), phz79(79, 109), phz80(80, 110),
	
	phz81(81,0),phz82(82,0),phz83(83,0),phz84(84,0),
	//85的作用是在检测胡牌的时候加入牌组，可以组成33一句话，不会有对子，最后结束的时候再删除，
	//85的作用是在检测听牌的时候加入牌组，替代任意卡牌，看是否可胡牌。
	phz85(85,0),phz86(86,0);
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
		} else {
			return 100 + getPai();
		}
	}

	public int getPai() {
		return val % 100;
	}

	public int getCase() {
		if (isBig()) {
			return 100;
		} else {
			return 0;
		}
	}
	static List<Integer> hongVal = Arrays.asList(2, 7, 10,102,107,110);
	public static boolean isHongpai(int v){
		return hongVal.contains(v);
	}

	public static PaohzCard getPaohzCard(int id) {
		if (id == 0) {
			return null;
		}
		return PaohzCard.valueOf(PaohzCard.class, "phz" + id);
	}

	public static void main(String[] args) {
		// prinl();
		System.out.println(114 % 100);
	}

	private static void prinl() {
		int k = 1;
		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 10; j++) {
				if (i <= 4) {
					System.out.println("phz" + k + "(" + k + "," + j + "),");

				} else {
					System.out.println("phz" + k + "(" + k + "," + (100 + j) + "),");

				}
				k++;
			}
		}
	}

    public static final String[] xiao_zi= {"一","二","三","四","五","六","七","八","九","十"};
    public static final String[] da_zi= {"壹","贰","叁","肆","伍","陆","柒","捌","玖","拾"};
    public String toString(){
        if(getVal() == 0){
			return "王";
        }else if(getVal()>100){
            return da_zi[getVal()%100-1];
        }else if(getVal() <= 10&&getVal() > 0){
			return xiao_zi[getVal()-1];
		}else {
			return "";
		}
    }


	public static List<PaohzCard> getPaohzCardsByVal(int val) {
		List<PaohzCard> cards=new ArrayList<>();
		if (val >=1&&val<=10) {
			for (int i = 0; i < 4; i++) {
				cards.add(getPaohzCard(val+10*i));
			}
		}
		if (val >=101&&val<=110) {
			for (int i = 0; i < 4; i++) {
				cards.add(getPaohzCard(val%100+10*i+40));
			}
		}
		return cards;
	}
}
