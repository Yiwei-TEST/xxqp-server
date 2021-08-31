package com.sy599.game.util.mj.serialize;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum NormolMj {
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

    // 4风  301东风 311南风  321西风 331北风
    mj109(109, 301), mj110(110, 301), mj111(111, 301), mj112(112, 301),
    mj113(113, 311), mj114(114, 311), mj115(115, 311), mj116(116, 311),
    mj117(117, 321), mj118(118, 321), mj119(119, 321), mj120(120, 321),
    mj121(121, 331), mj122(122, 331), mj123(123, 331), mj124(124, 331),

    // 红中 中发白  201红中  211发财  221白板
    mj201(201, 201), mj202(202, 201), mj203(203, 201), mj204(204, 201),
    mj205(205, 211), mj206(206, 211), mj207(207, 211), mj208(208, 211),
    mj209(209, 221), mj210(210, 221), mj211(211, 221), mj212(212, 221);



    private int id;
    private int val;

    static NormolMj[] mjArray;
    public static NormolMj[] fullMj = new NormolMj[27];
    public static NormolMj[] fullMjAndFeng = new NormolMj[34];
    static {
        init();
    }

    private static void init() {
        int maxVal = 0;
        for(NormolMj mj : NormolMj.values()) {
            if (mj.getVal() > maxVal && mj.getId() < 300) {
                maxVal = mj.getVal();
            }
        }
        mjArray = new NormolMj[maxVal];

        for(NormolMj mj : NormolMj.values()) {
            if (mj.getId() < 300) {
                int index = mj.getVal() - 1;
                mjArray[index] = mj;
            }
        }
        initFullMj();
    }

    private static void initFullMj(){
        for (int i = 0; i < 27; i++) {
            fullMj[i]=getMajang(i+1);
        }
        for (int i = 0; i < 27; i++) {
            fullMjAndFeng[i]=getMajang(i+1);
        }
        fullMjAndFeng[27]=mj109;
        fullMjAndFeng[28]=mj113;
        fullMjAndFeng[29]=mj117;
        fullMjAndFeng[30]=mj121;
        fullMjAndFeng[31]=mj201;
        fullMjAndFeng[32]=mj205;
        fullMjAndFeng[33]=mj209;
    }


    NormolMj(int id, int val) {
        this.id = id;
        this.val = val;
    }

    /**
     * @return 1 条 2 筒 3万 30->4风 20->中发白
     */
    public int getColourVal() {
        return val / 10;
    }

    public static NormolMj getMajang(int id) {
        return NormolMj.valueOf(NormolMj.class, "mj" + id);
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

    public static NormolMj getMajiangByValue(int value) {
        for (NormolMj mj : NormolMj.values()) {
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
        if (Huase == 1) {
            return Pai + "条";
        } else if (Huase == 2) {
            return Pai + "筒";
        } else if (Huase == 3) {
            return Pai + "万";
        } else if (Huase == 20) {
            return "红中";
        } else if (Huase == 21) {
            return "发财";
        } else if (Huase == 22) {
            return "白板";
        } else if (Huase == 30) {
            return "东风";
        } else if (Huase == 31) {
            return "南风";
        } else if (Huase == 32) {
            return "西风";
        } else if (Huase == 33) {
            return "北风";
        } else if (Huase == 100) {
            return "万能牌";
        } else if (Huase == 99) {
            return "听牌打出";
        } else {
            return "未知牌";
        }
    }

    public static NormolMj getMajiangByVal(int val) {
        if (val > mjArray.length) {
            return null;
        }
        return mjArray[val-1];
    }





    /**
     * 牌组
     **/
    public static final Map<Integer, List<int[]>> paiZuMap = new HashMap<>();

    static {
        //初始化牌组
        for (int i = 11; i <= 19; i++) {
            paiZuMap.put(i, initPaiZu(i));
        }
        for (int i = 21; i <= 29; i++) {
            paiZuMap.put(i, initPaiZu(i));
        }
        for (int i = 31; i <= 39; i++) {
            paiZuMap.put(i, initPaiZu(i));
        }

    }

    public static List<int[]> getPaiZu(int val) {
        return paiZuMap.get(val);
    }

    public static Map<Integer, List<int[]>> getPaiZuMap() {
        return paiZuMap;
    }

    public static List<int[]> initPaiZu(int val) {
        int yu=val%10;
        List<int[]> res = new ArrayList<>();
        //三张一样
        res.add(new int[]{val, val, val});
        //做顺
        if (yu == 9) {
            res.add(new int[]{val - 2, val - 1, val});
        } else if (yu == 8) {
            res.add(new int[]{val - 2, val - 1, val});
            res.add(new int[]{val - 1, val, val + 1});
        } else if (yu== 1) {
            res.add(new int[]{val, val + 1, val + 2});
        } else if (yu== 2) {
            res.add(new int[]{val - 1, val, val + 1});
            res.add(new int[]{val, val + 1, val + 2});
        } else {
            res.add(new int[]{val - 2, val - 1, val});
            res.add(new int[]{val - 1, val, val + 1});
            res.add(new int[]{val, val + 1, val + 2});
        }
        return res;
    }
}
