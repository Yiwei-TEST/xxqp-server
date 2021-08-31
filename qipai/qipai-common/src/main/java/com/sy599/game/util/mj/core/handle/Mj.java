package com.sy599.game.util.mj.core.handle;

import java.util.ArrayList;
import java.util.List;

/**
 * 麻将
 */
public enum Mj {

    // 108个麻将 条：11-19  筒：21-29  万：31-39
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
    mj209(209, 221), mj210(210, 221), mj211(211, 221), mj212(212, 221),

    // 万能牌
    mj1000(1000, 1000);

    private int id;
    private int val;

    static Mj[] mjArray;
    public static Mj[] fullMj = new Mj[27];
    public static List<Mj> allJiang = new ArrayList<>();
    public static List<Integer> allJiangVal = new ArrayList<>();

    public static List<Mj> allMa = new ArrayList<>();
    public static List<Integer> allMaVal = new ArrayList<>();

    static {
        init();
    }

    private static void init() {
        int maxVal = 0;
        for (Mj mj : Mj.values()) {
            if (mj.getVal() > maxVal && mj.getId() < 300) {
                maxVal = mj.getVal();
            }
        }
        mjArray = new Mj[maxVal];

        for (Mj mj : Mj.values()) {
            if (mj.getId() < 300) {
                int index = mj.getVal() - 1;
                mjArray[index] = mj;
            }
            if (mj.getId() <= 27) {
                fullMj[mj.getId() - 1] = mj;
                if (mj.isJiang()) {
                    allJiang.add(mj);
                    allJiangVal.add(mj.getVal());
                }
                if (mj.isMa()) {
                    allMa.add(mj);
                    allMaVal.add(mj.getVal());
                }
            } else {

            }
        }
    }

    Mj(int id, int val) {
        this.id = id;
        this.val = val;
    }

    /**
     * @return 1 条 2 筒 3万 30->4风 20->中发白
     */
    public int getColourVal() {
        return val / 10;
    }

    public static Mj getMajang(int id) {
        return Mj.valueOf(Mj.class, "mj" + id);
    }

    public static Mj getMajiangByValue(int value) {
        for (Mj mj : Mj.values()) {
            if (mj.getVal() == value) {
                return mj;
            }
        }
        return null;
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

    public static int getHongzhongNum(List<Mj> list) {
        int num = 0;
        for (Mj majiang : list) {
            if (majiang.isHongzhong() == true) {
                num++;
            }
        }
        return num;
    }

    public int getId() {
        return id;
    }

    public int getVal() {
        return val;
    }

    public boolean isJiang() {
        return isJiang(getVal());
    }

    public static boolean isJiang(int val) {
        int pai = val % 10;
        return pai == 2 || pai == 5 || pai == 8;
    }

    public int getHuase() {
        return val / 10;
    }

    public int getPai() {
        return val % 10;
    }

    public static void main(String[] args) {
        String majiangIds = "61,7,97,84";
        String[] ids = majiangIds.split(",");
        StringBuilder sb = new StringBuilder();
        if (ids != null && ids.length > 0) {
            for (String id : ids) {
                sb.append(idToMajiang(Integer.parseInt(id))).append(",");
            }
        }
        System.out.println(sb.toString());
    }

    @SuppressWarnings("unused")
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
                System.out.println("mj" + k + "(" + k + "," + l + "),");
                k++;
            }
        }
    }

    public static String idToMajiang(int id) {
        Mj mj = Mj.getMajang(id);
        return valueToMajiang(mj.val);
    }

    public static String valueToMajiang(int value) {
        int Pai = value % 10;
        int Huase = value / 10;
        //1 条 2 筒 3万  301东风 311南风  321西风 331北风 201红中  211发财  221白板  万能牌
        if (Huase == 1) {
            return Pai + "条";
        } else if (Huase == 2) {
            return Pai + "筒";
        } else if (Huase == 3) {
            return Pai + "万";
        } else if (Huase == 20) { // 红中
            return "红中";
        } else if (Huase == 21) {// 发财
            return "发财";
        } else if (Huase == 21) {// 白板
            return "白板";
        } else if (Huase == 30) {// 东风
            return "东风";
        } else if (Huase == 31) {// 南风
            return "南风";
        } else if (Huase == 32) {// 西风
            return "西风";
        } else if (Huase == 33) {// 北风
            return "北风";
        } else {
            return "万能牌";
        }
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
        } else if (Huase == 20) { // 红中
            return "红中";
        } else if (Huase == 21) {// 发财
            return "发财";
        } else if (Huase == 21) {// 白板
            return "白板";
        } else if (Huase == 30) {// 东风
            return "东风";
        } else if (Huase == 31) {// 南风
            return "南风";
        } else if (Huase == 32) {// 西风
            return "西风";
        } else if (Huase == 33) {// 北风
            return "北风";
        } else {
            return "万能牌";
        }
    }

    public static Mj getMajiangByVal(int val) {
        if (val > mjArray.length) {
            return null;
        }
        return mjArray[val - 1];
    }

    public boolean isMa() {
        return isMa(getVal());
    }

    public static boolean isMa(int val) {
        int pai = val % 10;
        return pai == 1 || pai == 5 || pai == 9;
    }
}
