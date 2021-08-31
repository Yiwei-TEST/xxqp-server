package com.sy599.game.qipai.yjmj.bean;

import com.sy599.game.qipai.yjmj.rule.YjMj;
import com.sy599.game.util.DataMapUtil;

import java.util.Collections;
import java.util.List;

/**
 * 胡牌类型
 * 0 碰碰胡  1 将将胡 2 清一色 3 七小队 4 豪华七小队 5 双豪华七小队 6 三豪华七小队 7 杠爆 8 抢杠胡 9 海底捞 10一条龙 11 门清 12 天胡 13 一字翘 14报听
 */
public class YjMjHu {

    /*** 大胡类型数**/
    public static final int daHuCount = 16;

    /*** 碰碰胡 **/
    public static final int daHu_pengPengHu = 0;
    /*** 将将胡**/
    public static final int daHu_jiangJiangHu = 1;
    /*** 清一色**/
    public static final int daHu_qingYiSe = 2;
    /*** 小七对**/
    public static final int daHu_xiao7Dui = 3;
    /*** 豪华小七对**/
    public static final int daHu_haoXiao7Dui = 4;
    /*** 双豪华小七对**/
    public static final int daHu_shuangHaoXiao7Dui = 5;
    /*** 三豪华小七对**/
    public static final int daHu_sanHaoXiao7Dui = 6;
    /*** 杠暴、杠上花**/
    public static final int daHu_gangBao = 7;
    /*** 抢杠胡**/
    public static final int daHu_qiangGangHu = 8;
    /*** 海底捞**/
    public static final int daHu_haiDiLao = 9;
    /*** 一条龙**/
    public static final int daHu_yiTiaoLong = 10;
    /*** 门清**/
    public static final int daHu_mengQing = 11;
    /*** 天胡**/
    public static final int daHu_tianHu = 12;
    /*** 一字翘**/
    public static final int daHu_yiZiQiao = 13;
    /*** 报听**/
    public static final int daHu_baoTing = 14;
    /*** 码码胡**/
    public static final int daHu_maMaHu = 15;


    /*** 平胡**/
    private boolean pingHu;

    /**
     * 0 碰碰胡
     */
    private boolean pengpengHu;

    /**
     * 1 将将胡
     */
    private boolean jiangjiangHu;

    /**
     * 2 清一色
     */
    private boolean qingyiseHu;

    /**
     * 3 七小队
     */
    private boolean xiao7dui;

    /**
     * 4 豪华七小队
     */
    private boolean hao7xiaodui;

    /**
     * 5 双豪华七小队
     */
    private boolean shuang7xiaodui;

    /**
     * 6   三豪华七小队
     */
    private boolean san7xiaodui;

    /**
     * 7 杠爆（杠牌后补张的牌胡牌）
     */
    private boolean gangBao;

    /**
     * 8 抢杠胡（胡别人摸杠的那张牌）
     */
    private boolean qiangGangHu;

    /**
     * 9 海底捞（最后四张牌胡牌）
     */
    private boolean haidilao;

    /**
     * 10 一条龙（同花色1到9凑齐）
     */
    private boolean yiTiaoLong;

    /**
     * 11 门清（大门子不下牌）
     */
    private boolean mengQing;

    /**
     * 12 天胡 （起手胡牌）
     */
    private boolean tianHu;

    /**
     * 13 一字翘 （只剩一张牌胡牌）
     */
    private boolean yiZiQiao;

    /**
     * 14报听
     */
    private boolean baoting;

    /**
     * 15 码码胡
     */
    private boolean maMaHu;

    /**
     * 是否胡了
     */
    private boolean isHu;

    /**
     * 是否大胡
     */
    private boolean isDahu;

    /**
     * 大胡列表 除平胡外 0-14
     */
    private List<Integer> dahuList;

    private List<YjMj> showMajiangs;

    private int dahuPoint;

    /**
     * 小门子，输家基础出分为1分
     */
    public static int xiaoMengZiBasePoint = 1;

    /**
     * 大门子，输家基础出分为3分
     */
    public static int daMengZiBasePoint = 3;

    public List<Integer> buildDahuList() {
        int[] arr = new int[daHuCount];
        if (pengpengHu) {
            arr[daHu_pengPengHu] = 1;
        }
        if (jiangjiangHu) {
            arr[daHu_jiangJiangHu] = 1;
        }
        if (qingyiseHu) {
            arr[daHu_qingYiSe] = 1;
        }
        if (xiao7dui) {
            arr[daHu_xiao7Dui] = 1;
        }
        if (hao7xiaodui) {
            arr[daHu_haoXiao7Dui] = 1;
        }
        if (shuang7xiaodui) {
            arr[daHu_shuangHaoXiao7Dui] = 1;
        }
        if (san7xiaodui) {
            arr[daHu_sanHaoXiao7Dui] = 1;
        }
        if (gangBao) {
            arr[daHu_gangBao] = 1;
        }
        if (qiangGangHu) {
            arr[daHu_qiangGangHu] = 1;
        }
        if (haidilao) {
            arr[daHu_haiDiLao] = 1;
        }
        if (yiTiaoLong) {
            arr[daHu_yiTiaoLong] = 1;
        }
        if (mengQing) {
            arr[daHu_mengQing] = 1;
        }
        if (tianHu) {
            arr[daHu_tianHu] = 1;
        }
        if (yiZiQiao) {
            arr[daHu_yiZiQiao] = 1;
        }
        if (baoting) {
            arr[daHu_baoTing] = 1;
        }
        if (maMaHu) {
            arr[daHu_maMaHu] = 1;
        }
        List<Integer> dahu = DataMapUtil.indexToValList(arr);
        if (!dahu.isEmpty()) {
            return dahu;
        }
        return Collections.emptyList();
    }

    /**
     * 获得大胡积分
     *
     * @return
     */
    public int getDahuPointByList() {
        int dahuNum = 0;// 大门子个数
        int point = 0;
        for (int dahu : dahuList) {
            if (dahu == daHu_haoXiao7Dui) {// 豪华7小对  算两个门子
                dahuNum += 2;
            } else if (dahu == daHu_shuangHaoXiao7Dui) {// 双豪华7小对 算三个门子
                dahuNum += 3;
            } else if (dahu == daHu_sanHaoXiao7Dui) {// 三豪华7小对 算四个门子
                dahuNum += 4;
            } else {
                dahuNum += 1;
            }
        }
        point += daMengZiBasePoint * Math.pow(2, dahuNum - 1);
        return point;
    }


    public boolean isPingHu() {
        return pingHu;
    }

    public void setPingHu(boolean pingHu) {
        this.pingHu = pingHu;
    }

    public boolean isPengpengHu() {
        return pengpengHu;
    }

    public void setPengpengHu(boolean pengpengHu) {
        this.pengpengHu = pengpengHu;
    }

    public boolean isJiangjiangHu() {
        return jiangjiangHu;
    }

    public void setJiangjiangHu(boolean jiangjiangHu) {
        this.jiangjiangHu = jiangjiangHu;
    }

    public boolean isQingyiseHu() {
        return qingyiseHu;
    }

    public void setQingyiseHu(boolean qingyiseHu) {
        this.qingyiseHu = qingyiseHu;
    }

    public boolean isShuang7xiaodui() {
        return shuang7xiaodui;
    }

    public void setShuang7xiaodui(boolean shuang7xiaodui) {
        this.shuang7xiaodui = shuang7xiaodui;
    }

    public boolean isHao7xiaodui() {
        return hao7xiaodui;
    }

    public void setHao7xiaodui(boolean hao7xiaodui) {
        this.hao7xiaodui = hao7xiaodui;
    }

    public boolean isXiao7dui() {
        return xiao7dui;
    }

    public void setXiao7dui(boolean xiao7dui) {
        this.xiao7dui = xiao7dui;
    }

    public boolean isHu() {
        return isHu;
    }

    public void setHu(boolean isHu) {
        this.isHu = isHu;
    }

    public boolean isSan7xiaodui() {
        return san7xiaodui;
    }

    public void setSan7xiaodui(boolean san7xiaodui) {
        this.san7xiaodui = san7xiaodui;
    }

    public boolean isDahu() {
        return isDahu;
    }

    public void setDahu(boolean isDahu) {
        this.isDahu = isDahu;
    }

    public List<Integer> getDahuList() {
        return dahuList;
    }

    public void setDahuList(List<Integer> dahuList) {
        this.dahuList = dahuList;
    }

    public void initDahuList() {
        this.dahuList = buildDahuList();
        if (!this.dahuList.isEmpty()) {
            isDahu = true;
        }
    }

    public void setShowMajiangs(List<YjMj> showMajiangs) {
        this.showMajiangs = showMajiangs;
    }

    public List<YjMj> getShowMajiangs() {
        return showMajiangs;
    }

    public int getDahuPoint() {
        addToDahu(getDahuList());
        return dahuPoint;
    }

    public void setDahuPoint(int dahuPoint) {
        this.dahuPoint = dahuPoint;
    }

    public void addToDahu(List<Integer> dahuList) {
        if (this.dahuList == null) {
            this.initDahuList();
        }
        for (int dahu : dahuList) {
            if (!this.dahuList.contains(dahu)) {
                this.dahuList.add(dahu);
            }
        }
        setDahuPoint(getDahuPointByList());
    }

    public boolean isGangBao() {
        return gangBao;
    }

    public void setGangBao(boolean gangBao) {
        this.gangBao = gangBao;
    }

    public boolean isQiangGangHu() {
        return qiangGangHu;
    }

    public void setQiangGangHu(boolean qiangGangHu) {
        this.qiangGangHu = qiangGangHu;
    }

    public boolean isHaidilao() {
        return haidilao;
    }

    public void setHaidilao(boolean haidilao) {
        this.haidilao = haidilao;
    }

    public boolean isYiTiaoLong() {
        return yiTiaoLong;
    }

    public void setYiTiaoLong(boolean yiTiaoLong) {
        this.yiTiaoLong = yiTiaoLong;
    }

    public boolean isMengQing() {
        return mengQing;
    }

    public void setMengQing(boolean mengQing) {
        this.mengQing = mengQing;
    }

    public boolean isTianHu() {
        return tianHu;
    }

    public void setTianHu(boolean tianHu) {
        this.tianHu = tianHu;
    }

    public boolean isYiZiQiao() {
        return yiZiQiao;
    }

    public void setYiZiQiao(boolean yiZiQiao) {
        this.yiZiQiao = yiZiQiao;
    }

    public boolean isBaoting() {
        return baoting;
    }

    public void setBaoting(boolean baoting) {
        this.baoting = baoting;
    }

    public boolean isMaMaHu() {
        return maMaHu;
    }

    public void setMaMaHu(boolean maMaHu) {
        this.maMaHu = maMaHu;
    }

    /**
     * 是否小7对胡
     *
     * @return
     */
    public boolean isXiao7duiHu() {
        return isXiao7dui() || isHao7xiaodui() || isShuang7xiaodui() || isSan7xiaodui();
    }

    /**
     * 大胡
     *
     * @return
     */
    public static String getDahuNames(List<Integer> daHu) {
        if (daHu == null || daHu.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int hu : daHu) {
            switch (hu) {
                case daHu_pengPengHu:
                    sb.append("碰碰胡,");
                    break;
                case daHu_jiangJiangHu:
                    sb.append("将将胡,");
                    break;
                case daHu_qingYiSe:
                    sb.append("清一色,");
                    break;
                case daHu_xiao7Dui:
                    sb.append("七小队,");
                    break;
                case daHu_haoXiao7Dui:
                    sb.append("豪华七小队,");
                    break;
                case daHu_shuangHaoXiao7Dui:
                    sb.append("双豪华七小队,");
                    break;
                case daHu_sanHaoXiao7Dui:
                    sb.append("三豪华七小队,");
                    break;
                case daHu_gangBao:
                    sb.append("杠爆,");
                    break;
                case daHu_qiangGangHu:
                    sb.append("抢杠胡,");
                    break;
                case daHu_haiDiLao:
                    sb.append("海底捞,");
                    break;
                case daHu_yiTiaoLong:
                    sb.append("一条龙,");
                    break;
                case daHu_mengQing:
                    sb.append("门清,");
                    break;
                case daHu_tianHu:
                    sb.append("碰碰胡,");
                    break;
                case daHu_yiZiQiao:
                    sb.append("一字翘,");
                    break;
                case daHu_baoTing:
                    sb.append("报听,");
                    break;
                case daHu_maMaHu:
                    sb.append("码码胡,");
                    break;
                default:
                    sb.append(hu + " ");
                    break;
            }
        }
        return sb.toString();
    }
}
