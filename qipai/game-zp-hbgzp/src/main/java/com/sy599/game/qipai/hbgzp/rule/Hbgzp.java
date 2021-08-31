package com.sy599.game.qipai.hbgzp.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Hbgzp {
	// 1-10  各5个
    hp1(1, 1), hp2(2, 2), hp3(3, 3), hp4(4, 4), hp5(5, 5), hp6(6, 6), hp7(7, 7), hp8(8, 8), hp9(9, 9),hp10(10, 10),
    hp11(11, 1), hp12(12, 2), hp13(13, 3), hp14(14, 4), hp15(15, 5), hp16(16, 6), hp17(17, 7), hp18(18, 8),hp19(19, 9), hp20(20, 10),
    hp21(21, 1), hp22(22, 2), hp23(23, 3), hp24(24, 4), hp25(25, 5), hp26(26, 6), hp27(27, 7),hp28(28, 8), hp29(29, 9), hp30(30, 10), 
    hp31(31, 1), hp32(32, 2), hp33(33, 3), hp34(34, 4), hp35(35, 5), hp36(36, 6),hp37(37, 7), hp38(38, 8), hp39(39, 9), hp40(40, 10),
    hp41(41, 1), hp42(42, 2), hp43(43, 3), hp44(44, 4), hp45(45, 5),hp46(46, 6), hp47(47, 7), hp48(48, 8), hp49(49, 9), hp50(50, 10),
    /**
     *  化 101 、三 3、千 102
    	孔 103 、一 1 、己 104
    	七 7 、十10 、土 105
    	八 8 、九 9、子 106 
    	二2、四4、五5、六6
     */
    
    hp51(51, 101), hp52(52, 102), hp53(53, 103), hp54(54, 104), hp55(55, 105), hp56(56, 106), 
    hp57(57, 101), hp58(58, 102), hp59(59, 103), hp60(60, 104), hp61(61, 105), hp62(62, 106), 
    hp63(63, 101), hp64(64, 102), hp65(65, 103), hp66(66, 104), hp67(67, 105), hp68(68, 106), 
    hp69(69, 101), hp70(70, 102), hp71(71, 103), hp72(72, 104), hp73(73, 105), hp74(74, 106),
    hp75(75, 101), hp76(76, 102), hp77(77, 103), hp78(78, 104), hp79(79, 105), hp80(80, 106), 
    /**
     * 	可 201  、 知202、礼 203
    	上 204、大 205 、人 206
     */
    hp81(81, 201), hp82(82, 202), hp83(83, 203), hp84(84, 204), hp85(85, 205), hp86(86, 206), 
    hp87(87, 201), hp88(88, 202), hp89(89, 203), hp90(90, 204), hp91(91, 205), hp92(92, 206),
    hp93(93, 201), hp94(94, 202), hp95(95, 203), hp96(96, 204), hp97(97, 205), hp98(98, 206),
    hp99(99, 201), hp100(100, 202), hp101(101, 203), hp102(102, 204), hp103(103, 205), hp104(104, 206),
    hp105(105, 201), hp106(106, 202), hp107(107, 203), hp108(108,204), hp109(109, 205), hp110(110, 206),

	 
    hpf1(-1, 1), hpf2(-2, 2), hpf3(-3, 3), hpf4(-4, 4), hpf5(-5, 5), hpf6(-6, 6), 
   	hpf7(-7, 7),hpf8(-8, 8), hpf9(-9, 9), hpf10(-10, 10),
   	hpf11(-11, 101), hpf12(-12, 102), hpf13(-13, 103), hpf14(-14, 104), hpf15(-15, 105), 
   	hpf16(-16, 106), hpf17(-17, 201), hpf18(-18, 202), hpf19(-19, 203), hpf20(-20, 204),
   	hpf21(-21, 205),hpf22(-22, 206),
   	//花牌
   	hpf31(-31, 1),hpf32(-32, 3),hpf33(-33, 5),hpf34(-34, 7),hpf35(-35, 9)
   	;

	public static List<Hbgzp> huCardList = new ArrayList<>(
			Arrays.asList(hpf1,hpf2,hpf3,hpf4,hpf5,hpf6,hpf7,hpf8,hpf9,hpf10,
					hpf11,hpf12,hpf13,hpf14,hpf15,hpf16,hpf17,hpf18,hpf19,hpf20,hpf21,hpf22
					,hpf31,hpf32,hpf33,hpf34,hpf35));
	


    private int id;
    private int val;

    Hbgzp(int id, int val) {
        this.id = id;
        this.val = val;
    }

    /**
     * @return 1 条 2 筒 3万 30->4风 20->中发白
     */
    public int getColourVal() {
        return val / 10;
    }

    public static Hbgzp getPaohzCard(int id) {
        return Hbgzp.valueOf(Hbgzp.class, "hp" + id);
    }


    public int getId() {
        return id;
    }

    public int getVal() {
        return val;
    }
    
    private static List<Integer> hongpaiVal = Arrays.asList(3,5,7,201,202,203,204,205,206);
	
	public boolean isHongpai(){
		return hongpaiVal.contains(val);
	}
	
	public static boolean isHongpai(int val){
		return hongpaiVal.contains(val);
	}
	
	private static List<Integer> huapaiId = Arrays.asList(1,11,3,13,5,15,7,17,9,19,-31,-32,-33,-34,-35);
	
	public boolean isHuapai(){
		return huapaiId.contains(this.id);
	}
	public static boolean isHuapai(int id){
		return huapaiId.contains(id);
	}
	private static List<Integer> suanzipaiVal = Arrays.asList(3,5,7);
	
	public boolean isSuanzipai(){
		return suanzipaiVal.contains(val);
	}
	public static boolean isSuanzipai(int val){
		return suanzipaiVal.contains(val);
	}
    
	/**
	 * 这张牌要加的子
	 * 乙三五七九带花。带花的牌算一个子。花乙花九都算1个子。
		三五七：三1个子，花三2个子，五2个子，花五4个子，七1个子，花七2个子

	 * @return
	 */
	public int getSuanzi(){
		int zi = 0;
		if(isHuapai()){
			if(val == 5){
				zi +=2;
			}else{
				zi++;
			}
		}
		if(isSuanzipai()){
			if(val == 5){
				zi +=2;
			}else{
				zi++;
			}
		}
		return zi;
	}
	
	
    public int getPai() {
//        return val % 10;
        return val;
    }


    public static Hbgzp getMajiangByValue(int value) {
        for (Hbgzp mj : Hbgzp.values()) {
            if (mj.getVal() == value) {
                return mj;
            }
        }
        return null;
    }

    public static final String[] num_zi= {"一","二","三","四","五","六","七","八","九","十"};
	public static final String[] hua_zi= {"化","千","孔","己","土","子"};
	public static final String[] ke_zi= {"可","知","礼","上","大","人"};
	 
	public String toString(){
		if(getVal() <= 10){
			return num_zi[getVal()-1];
		}else if(getVal() > 100 && getVal()< 200){
			return hua_zi[getVal()%100-1];
		}else if(getVal() > 200){
			return ke_zi[getVal()%200-1];
		}
		return "未知:"+getVal();
	}
	
	public static String toStringVal(int val){
		if(val < 0){
			val = 0-val;
		}
		if(val <= 10){
			return num_zi[val-1];
		}else if(val > 100 && val< 200){
			return hua_zi[val%100-1];
		}else if(val > 200){
			return ke_zi[val%200-1];
		}
		return "未知:"+val;
	}
	
	public static void main(String[] args) {
		System.out.println(ke_zi[206%200-1]);
	}
	
}
