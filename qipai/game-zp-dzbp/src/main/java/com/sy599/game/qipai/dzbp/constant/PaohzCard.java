package com.sy599.game.qipai.dzbp.constant;

public enum PaohzCard {
	phz1(1, 101), phz2(2, 102), phz3(3, 103), phz4(4, 104), phz5(5, 105), phz6(6, 106), phz7(7, 107), phz8(8, 108), phz9(9, 109), phz10(10, 110), 
	phz11(11, 101), phz12(12, 102), phz13(13, 103), phz14(14, 104), phz15(15, 105), phz16(16, 106), phz17(17, 107), phz18(18, 108), phz19(19, 109), phz20(20, 110),
	phz21(21, 101), phz22(22, 102), phz23(23, 103), phz24(24, 104), phz25(25, 105), phz26(26, 106), phz27(27, 107), phz28(28, 108), phz29(29, 109), phz30(30, 110),
	phz31(31, 101), phz32(32, 102), phz33(33, 103), phz34(34, 104), phz35(35, 105), phz36(36, 106), phz37(37, 107), phz38(38, 108), phz39(39, 109), phz40(40, 110);
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
        if(getVal() <= 10){
            return xiao_zi[getVal()-1];
        }else{
            return da_zi[getVal()%100-1];
        }
    }

}
