package com.sy599.game.qipai.xplp.rule;


public enum XpLp {
    // 108个麻将 条、筒、万
    mj1(1, 11), mj2(2, 12), mj3(3, 13), mj4(4, 14), mj5(5, 15), mj6(6, 16), mj7(7, 17), mj8(8, 18), mj9(9, 19),
    mj10(10, 21), mj11(11, 22), mj12(12, 23), mj13(13, 24), mj14(14, 25), mj15(15, 26), mj16(16, 27), mj17(17, 28), mj18(18, 29),
    mj19(19, 31), mj20(20, 32), mj21(21, 33), mj22(22, 34), mj23(23, 35), mj24(24, 36), mj25(25, 37), mj26(26, 38), mj27(27, 39),

    mj28(28, 11), mj29(29, 12), mj30(30, 13), mj31(31, 14), mj32(32, 15), mj33(33, 16), mj34(34, 17), mj35(35, 18), mj36(36, 19),
    mj37(37, 21), mj38(38, 22), mj39(39, 23), mj40(40, 24), mj41(41, 25), mj42(42, 26), mj43(43, 27), mj44(44, 28), mj45(45, 29),
    mj46(46, 31), mj47(47, 32), mj48(48, 33), mj49(49, 34), mj50(50, 35), mj51(51, 36), mj52(52, 37), mj53(53, 38), mj54(54, 39),

    mj55(55, 11), mj56(56, 12), mj57(57, 13), mj58(58, 14), mj59(59, 15), mj60(60, 16), mj61(61, 17), mj62(62, 18), mj63(63, 19),
    mj64(64, 21), mj65(65, 22), mj66(66, 23), mj67(67, 24), mj68(68, 25), mj69(69, 26), mj70(70, 27), mj71(71, 28), mj72(72, 29),
    mj73(73, 31), mj74(74, 32), mj75(75, 33), mj76(76, 34), mj77(77, 35), mj78(78, 36), mj79(79, 37), mj80(80, 38), mj81(81, 39),

    mj82(82, 11), mj83(83, 12), mj84(84, 13), mj85(85, 14), mj86(86, 15), mj87(87, 16), mj88(88, 17), mj89(89, 18), mj90(90, 19),
    mj91(91, 21), mj92(92, 22), mj93(93, 23), mj94(94, 24), mj95(95, 25), mj96(96, 26), mj97(97, 27), mj98(98, 28), mj99(99, 29),
    mj100(100, 31), mj101(101, 32), mj102(102, 33), mj103(103, 34), mj104(104, 35), mj105(105, 36), mj106(106, 37), mj107(107, 38), mj108(108, 39),

    // 红中 中发白  201红中  211发财  221白板(飘花（林冲）、牛婆（H公）、    老钱（千万)
    mj201(201, 201), mj202(202, 201), mj203(203, 201), mj204(204, 201),
    mj205(205, 202), mj206(206, 202), mj207(207, 202), mj208(208, 202),
    mj209(209, 203), mj210(210, 203), mj211(211, 203), mj212(212, 203);

//    // 报听后玩家盖住的牌
//    mj999(999, 999),
//
//    // 万能牌，非癞子玩法，当癞子用
//    mj1000(1000, 1000);


    private int id;
    private int val;

    public static XpLp[] fullMj = new XpLp[30];
    static {
        init();
    }

    private static void init() {
        int maxVal = 0;
        for(XpLp mj : XpLp.values()) {
            if (mj.getVal() > maxVal && mj.getId() < 300) {
                maxVal = mj.getVal();
            }
        }

        for(XpLp mj : XpLp.values()) {
            if(mj.getId() <= 27){
                fullMj[mj.getId()-1] = mj;
            }
            if(mj.getId() > 200){
            	fullMj[mj.getVal()-174] = mj;
            }
        }
    }



    XpLp(int id, int val) {
        this.id = id;
        this.val = val;
    }

    /**
     * @return 1 条 2 筒 3万 30->4风 20->中发白
     */
    public int getColourVal() {
        return val / 10;
    }

    public static XpLp getMajang(int id) {
        return XpLp.valueOf(XpLp.class, "mj" + id);
        
    }

    /**
     * 1条是万能风和中发白
     *
     * @return
     */
    public boolean isAllPowerful() {
        // 1条是万能风和中发白
        return val == 11;
    }

    /**
     * 是否是风牌
     *
     * @return
     */
    public boolean isFeng() {
        return val > 300 && val < 400;
    }

    /**
     * 是否是中发白牌
     *
     * @return
     */
    public boolean isZhongFaBai() {
        return val > 200 && val < 300;
    }

    public boolean isHongzhong() {
        return val == 201;
    }

    public int getId() {
        return id;
    }

    public int getVal() {
        return val;
    }

    public boolean isJiang() {
        int pai = getPai();
        return pai == 2 || pai == 5 || pai == 8;
    }

    public int getHuase() {
        return val / 10;
    }

    public int getPai() {
        return val % 10;
    }

    private static void prinl() {
        int k = 1;
        for (int l = 1; l <= 4; l++)
            for (int i = 1; i <= 3; i++) {
                for (int j = 1; j <= 9; j++) {
                    int val = i * 10 + j;
                    System.out.println("mj" + k + "(" + k + "," + val + "),");
                    k++;
                }
            }

        // 风
        for (int l = 1; l <= 4; l++) {
            for (int i = 301; i <= 304; i++) {
                int val = i;
                System.out.println("mj" + k + "(" + k + "," + val + "),");
                k++;
            }
        }

        k = 201;
        // 中发白
        for (int l = 201; l <= 203; l++) {
            for (int i = 1; i <= 4; i++) {
                int val = i;
                System.out.println("mj" + k + "(" + k + "," + l + "),");
                k++;
            }
        }
    }

    public static XpLp getMajiangByValue(int value) {
        for (XpLp mj : XpLp.values()) {
            if (mj.getVal() == value) {
                return mj;
            }
        }
        return null;
    }

    public String toString() {
        int Pai = val % 10;
        int Huase = val / 10;
        //1 条 2 筒 3万  301东风 311南风  321西风 331北风 201红中  211发财  221白板  万能牌
        
        if(val > 200){
        	switch (val) {
			case 201:
				return "飘花";
			case 202:
				return "牛婆";
			case 203:
				return "老钱";
			default:
				break;
			}
        }else{
        	if (Huase == 1) {
                return Pai + "条";
            } else if (Huase == 2) {
                return Pai + "筒";
            } else if (Huase == 3) {
                return Pai + "万";
            }
        }
        return "未知牌："+val;
    }

}
