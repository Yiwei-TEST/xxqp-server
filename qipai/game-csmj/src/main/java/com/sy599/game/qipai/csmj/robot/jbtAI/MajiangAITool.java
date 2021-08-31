package com.sy599.game.qipai.csmj.robot.jbtAI;

import com.sy599.game.qipai.csmj.bean.CsMjPlayer;
import com.sy599.game.qipai.csmj.bean.CsMjTable;
import com.sy599.game.qipai.csmj.constant.CsMjAction;
import com.sy599.game.qipai.csmj.rule.CsMj;
import com.sy599.game.qipai.csmj.rule.CsMjHelper;
import com.sy599.game.qipai.csmj.tool.CsMjTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * jbt节点判断
 * 1 条  ;2 筒 ;3万
 */
public class MajiangAITool {
    private static final int tiao = 1;
    private static final int tong = 2;
    private static final int wan = 3;

    public static boolean isTong8(CsMjPlayer player) {
        return getTongNum(player) > 8;
    }

    public static boolean isTiao8(CsMjPlayer player) {
        return getTiaoNum(player) > 8;
    }

    public static boolean isWan8(CsMjPlayer player) {
        return getWanNum(player) > 8;
    }

    public static boolean isJiang7(CsMjPlayer player) {
        return getJiangNum(player) > 7;
    }

    public static boolean isDui3(CsMjPlayer player) {
        // 手牌数=14，且对子数量大于3
        List<CsMj> handpais = new ArrayList<>(player.getHandMajiang());

        if (handpais.size() != 14) {
            return false;
        }
        return getDuiNum(handpais) > 3;
    }

    public static boolean isDui2(CsMjPlayer player) {
        //手牌数=11，下面没有吃，且对子数量大于2
        List<CsMj> handpais = new ArrayList<>(player.getHandMajiang());
        if (handpais.size() != 11 || player.getChi().size() > 0) {
            return false;
        }
        return getDuiNum(handpais) > 2;
    }

    public static boolean isDui1(CsMjPlayer player) {
        //手牌数=8，下面没有吃，且对子数量大于1
        List<CsMj> handpais = new ArrayList<>(player.getHandMajiang());
        if (handpais.size() != 8 || player.getChi().size() > 0) {
            return false;
        }
        return getDuiNum(handpais) > 1;
    }

    public static boolean isDui0(CsMjPlayer player) {
        //手牌数=5，下面没有吃且对子数量大于0
        List<CsMj> handpais = new ArrayList<>(player.getHandMajiang());
        if (handpais.size() != 5 || player.getChi().size() > 0) {
            return false;
        }
        return getDuiNum(handpais) > 0;
    }

    public static  HashMap<String, Object> isTingPai(CsMjPlayer player, int OnlyDaHu, int quanqiurJiang, List<CsMj> leftmajiangs) {
        //打出某张牌可以听牌= result.get("dismj")
        HashMap result = getTingPaiMap(player, OnlyDaHu, quanqiurJiang, leftmajiangs);
        if (null == result.get("tingmj")) {
             //System.out.println("isTingPai =  不能听牌");
            return null;
        } else {
            try {
                 //System.out.println("isTingPai =  能听牌 " + result.toString());
                return  result;
//                if (lef == 0) {
//                    List<CsMj> le = new ArrayList<>(player.getHandMajiang());
//                    List<CsMj> res = removePaiXing(le);
//                     //System.out.println("所听牌剩余张数为0 换牌听。随机打出：" + res.get(0));
//                    result.put("dismj", res.get(0));
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return   result ;
        }
    }

    public static boolean IsZuyijuhua(List<CsMj> hand, CsMj disMajiang) {
        // 与其它手牌组成一句话
        List<CsMj> hand2 = new ArrayList<>(hand);
        List<CsMj> result = CsMjTool.checkChi(hand2, disMajiang, null);
        if (result.size() > 0) {
             //System.out.println(disMajiang.toString() + " 其它手牌组成一句话 IsZuyijuhua = " + result.toString());
            return true;
        } else {
             //System.out.println("其它手牌组成一句话 IsZuyijuhua = false ");
            return false;
        }
    }
    public static  List<CsMj> IsZuyijuhua2(List<CsMj> hand, CsMj disMajiang) {
        // 与其它手牌组成一句话
        List<CsMj> hand2 = new ArrayList<>(hand);
        List<CsMj> result = CsMjTool.checkChi(hand2, disMajiang, null);
        if (result.size() > 0) {
             //System.out.println(disMajiang.toString() + " 其它手牌组成一句话 IsZuyijuhua = " + result.toString());
            return result;
        } else {
             //System.out.println("其它手牌组成一句话 IsZuyijuhua = false ");
            return result;
        }
    }
    public static boolean CanPeng(List<CsMj> hand,CsMj disMajiang){
        List<CsMj> hand2 = new ArrayList<>(hand);
        int mjval = disMajiang.getVal();
        int huase = disMajiang.getHuase();
        int num=0;
        for (CsMj mj:hand2
             ) {
            if(mj.getVal()==mjval && huase==mj.getHuase()){
                num++;
            }
        }

        if(num>=2){
             //System.out.println("能碰");
            return true;
        }else {
            return  false;
        }
         
    }
    public static   List<CsMj> getPengMjList(List<CsMj> hand,CsMj disMajiang){
        List<CsMj> hand2 = new ArrayList<>(hand);
        List<CsMj> re = new ArrayList<>();
        int mjval = disMajiang.getVal();
        int huase = disMajiang.getHuase();
        int num=0;
        for (CsMj mj:hand2 ) {
            if(mj.getVal()==mjval && huase==mj.getHuase()){
                re.add(mj);
                if(re.size()>=2){
                    re.add(disMajiang);
                    return re;
                }
            }
        }
        return re;
    }

    public static boolean IsWuGuanLian(List<CsMj> hand, CsMj disMajiang) {
        //与其它手牌无关联
        HashMap vk = getValNumMap(new ArrayList<>(hand));
        int disval = disMajiang.getVal();
        boolean canchi = IsZuyijuhua(hand, disMajiang);
        if (!canchi && null == vk.get(disval)) {
             //System.out.println("其它手牌组成无关联 IsWuGuanLian = true mj = " + disMajiang.toString());
            return true;
        } else {
            return false;
        }
    }

    public static boolean IsWuGuanLianHandCard(List<CsMj> hand, CsMj disMajiang) {
        //与其它手牌无关联
        HashMap vk = getValNumMap(new ArrayList<>(hand));
        int disval = disMajiang.getVal();
        boolean canchi = IsZuyijuhua(hand, disMajiang);
        int num = 0;
        if (null == vk.get(disval)) {
            num = 0;
        } else {
            num = (int) vk.get(disval);
        }
        if (!canchi && 1 == num) {
             //System.out.println("其它手牌组成无关联 IsWuGuanLianHandCard = true mj = " + disMajiang.toString());
            return true;
        } else {
            return false;
        }
    }

    public static List<CsMj> IsWuGuanLian19(List<CsMj> hand) {
        //1 条 2 筒 3万
        //手牌存在完全无关联的1和9，即1和9没有组成一对，也没有23或78和它们相邻
        int[] tiaoAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] tongAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] wanAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        List<CsMj> tiao = new ArrayList<>();
        List<CsMj> tong = new ArrayList<>();
        List<CsMj> wan = new ArrayList<>();
        for (CsMj mj : hand) {
            if (mj.getHuase() == 1) {
                tiaoAry[mj.getPai()] = tiaoAry[mj.getPai()] + 1;
                tiao.add(mj);
            }
            if (mj.getHuase() == 2) {
                tongAry[mj.getPai()] = tongAry[mj.getPai()] + 1;
                tong.add(mj);
            }
            if (mj.getHuase() == 3) {
                wanAry[mj.getPai()] = wanAry[mj.getPai()] + 1;
                wan.add(mj);
            }
        }
        List<CsMj> bzList = new ArrayList<>();
        if ((tiaoAry[1] == 1 && tiaoAry[2] == 0 && tiaoAry[3] == 0)) {
            for (CsMj mj : tiao) {
                if (mj.getPai() == 1) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
        }
        if (tongAry[1] == 1 && tongAry[2] == 0 && tongAry[3] == 0) {
            for (CsMj mj : tong) {
                if (mj.getPai() == 1) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
        }
        if (wanAry[1] == 1 && wanAry[2] == 0 && wanAry[3] == 0) {
            for (CsMj mj : wan) {
                if (mj.getPai() == 1) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
        }

        if ((tiaoAry[9] == 1 && tiaoAry[7] == 0 && tiaoAry[8] == 0)) {
            for (CsMj mj : tiao) {
                if (mj.getPai() == 9) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
        }
        if (tongAry[9] == 1 && tongAry[7] == 0 && tongAry[8] == 0) {
            for (CsMj mj : tong) {
                if (mj.getPai() == 9) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
        }
        if (wanAry[9] == 1 && wanAry[7] == 0 && wanAry[8] == 0) {
            for (CsMj mj : wan) {
                if (mj.getPai() == 9) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
        }
        if (bzList.size() > 0) {
            resortList(bzList);
             //System.out.println("手牌存在完全无关联的1和9 " + bzList);
        }
        return bzList;
    }

    public static List<CsMj> IsWuGuanLian3467(List<CsMj> hand) {
        //1 条 2 筒 3万
        //手牌存在完全无关联的3,4,6,7，比如3筒，没有一对，也没有1，2，4,5筒和它相邻
        int[] tiaoAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] tongAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] wanAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        List<CsMj> tiao = new ArrayList<>();
        List<CsMj> tong = new ArrayList<>();
        List<CsMj> wan = new ArrayList<>();
        for (CsMj mj : hand) {
            if (mj.getHuase() == 1) {
                tiaoAry[mj.getPai()] = tiaoAry[mj.getPai()] + 1;
                tiao.add(mj);
            }
            if (mj.getHuase() == 2) {
                tongAry[mj.getPai()] = tongAry[mj.getPai()] + 1;
                tong.add(mj);
            }
            if (mj.getHuase() == 3) {
                wanAry[mj.getPai()] = wanAry[mj.getPai()] + 1;
                wan.add(mj);
            }
        }
        List<CsMj> bzList = new ArrayList<>();
        int[] arycon = {3,4,6,7};
        for (int i = 0; i<arycon.length ; i++) {
            int mjval = arycon[i];
            if (tiaoAry[mjval] == 1 && tiaoAry[mjval - 2] == 0 && tiaoAry[mjval - 1] == 0 && tiaoAry[mjval + 2] == 0 && tiaoAry[mjval + 1] == 0) {
                for (CsMj mj : tiao) {
                    if (mj.getPai() == mjval) {
                        if (!bzList.contains(mj)) {
                            bzList.add(mj);
                        }
                    }
                }
            }
            if (tongAry[mjval] == 1 && tongAry[mjval - 2] == 0 && tongAry[mjval - 1] == 0 && tongAry[mjval + 2] == 0 && tongAry[mjval + 1] == 0) {
                for (CsMj mj : tong) {
                    if (mj.getPai() == mjval) {
                        if (!bzList.contains(mj)) {
                            bzList.add(mj);
                        }
                    }
                }
            }
            if (wanAry[mjval] == 1 && wanAry[mjval - 2] == 0 && wanAry[mjval - 1] == 0 && wanAry[mjval + 2] == 0 && wanAry[mjval + 1] == 0) {
                for (CsMj mj : wan) {
                    if (mj.getPai() == mjval) {
                        if (!bzList.contains(mj)) {
                            bzList.add(mj);
                        }
                    }
                }
            }
        }
        if (bzList.size() > 0) {
            resortList(bzList);
             //System.out.println(" 手牌存在完全无关联的3467 IsWuGuanLian3467 = " + bzList);
        }
        return bzList;
    }

    public static List<CsMj> IsWuGuanLian258(List<CsMj> hand) {
        //1 条 2 筒 3万
        //手牌存在完全无关联的258，比如2筒，没有一对，也没有134筒和它相邻
        int[] tiaoAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] tongAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] wanAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        List<CsMj> tiao = new ArrayList<>();
        List<CsMj> tong = new ArrayList<>();
        List<CsMj> wan = new ArrayList<>();
        for (CsMj mj : hand) {
            if (mj.getHuase() == 1) {
                tiaoAry[mj.getPai()] = tiaoAry[mj.getPai()] + 1;
                tiao.add(mj);
            }
            if (mj.getHuase() == 2) {
                tongAry[mj.getPai()] = tongAry[mj.getPai()] + 1;
                tong.add(mj);
            }
            if (mj.getHuase() == 3) {
                wanAry[mj.getPai()] = wanAry[mj.getPai()] + 1;
                wan.add(mj);
            }
        }
        List<CsMj> bzList = new ArrayList<>();
        int[] jiangarr = {2, 5, 8};
        for (int i = 0; i < jiangarr.length; i++) {
            int mjval = jiangarr[i];
            if (mjval == 2) {
                if (tiaoAry[2] == 1 && tiaoAry[1] == 0 && tiaoAry[3] == 0 && tiaoAry[4] == 0) {
                    for (CsMj mj : tiao) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                                break;
                            }
                        }
                    }
                }

                if (tongAry[2] == 1 && tongAry[1] == 0 && tongAry[3] == 0 && tongAry[4] == 0) {
                    for (CsMj mj : tong) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                                break;
                            }
                        }
                    }
                }
                if (wanAry[2] == 1 && wanAry[1] == 0 && wanAry[3] == 0 && wanAry[4] == 0) {
                    for (CsMj mj : wan) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                                break;
                            }
                        }
                    }
                }
            } else if (mjval == 5) {
                if (tiaoAry[5] == 1 && tiaoAry[6] == 0 && tiaoAry[7] == 0 && tiaoAry[3] == 0 && tiaoAry[4] == 0) {
                    for (CsMj mj : tiao) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                                break;
                            }
                        }
                    }
                }
                if (tongAry[5] == 1 && tongAry[6] == 0 && tongAry[7] == 0 && tongAry[3] == 0 && tongAry[4] == 0) {
                    for (CsMj mj : tong) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                                break;
                            }
                        }
                    }
                }
                if (wanAry[5] == 1 && wanAry[6] == 0 && wanAry[7] == 0 && wanAry[3] == 0 && wanAry[4] == 0) {
                    for (CsMj mj : wan) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                                break;
                            }
                        }
                    }
                }
            } else if (mjval == 8) {
                if (tiaoAry[8] == 1 && tiaoAry[6] == 0 && tiaoAry[7] == 0 && tiaoAry[9] == 0) {
                    for (CsMj mj : tiao) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                                break;
                            }
                        }
                    }
                }
                if (tongAry[8] == 1 && tongAry[6] == 0 && tongAry[7] == 0 && tongAry[9] == 0) {
                    for (CsMj mj : tong) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                                break;
                            }
                        }
                    }
                }
                if (wanAry[8] == 1 && wanAry[6] == 0 && wanAry[7] == 0 && wanAry[9] == 0) {
                    for (CsMj mj : wan) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (bzList.size() > 0) {
             //System.out.println(" 手牌存在完全无关联的258 IsWuGuanLian258 = " + bzList);
        }
        return bzList;
    }

    public static List<CsMj> IsBianZhang(List<CsMj> hand) {
        //1 条 2 筒 3万
        //手牌存在边张门子（12，89），即1和9没有组成一对，但有一个2或者8和它们相邻，也没有34或67
        // [0,0,0,0,0,0,0,0,0,0]  [0,1条数量,---,9条数量]
        int[] tiaoAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] tongAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] wanAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        List<CsMj> tiao = new ArrayList<>();
        List<CsMj> tong = new ArrayList<>();
        List<CsMj> wan = new ArrayList<>();
        for (CsMj mj : hand) {
            if (mj.getHuase() == 1) {
                tiaoAry[mj.getPai()] = tiaoAry[mj.getPai()] + 1;
                tiao.add(mj);
            }
            if (mj.getHuase() == 2) {
                tongAry[mj.getPai()] = tongAry[mj.getPai()] + 1;
                tong.add(mj);
            }
            if (mj.getHuase() == 3) {
                wanAry[mj.getPai()] = wanAry[mj.getPai()] + 1;
                wan.add(mj);
            }
        }
        List<CsMj> bzList = new ArrayList<>();
        if (tiaoAry[1] == 1 && tiaoAry[2] == 1 && tiaoAry[3] == 0 && tiaoAry[4] == 0) {
            // 边章12条
            for (CsMj mj : tiao) {
                if (mj.getPai() == 1 || mj.getPai() == 2) {
                    bzList.add(mj);
                }
            }
        }
        if (tiaoAry[8] == 1 && tiaoAry[9] == 1 && tiaoAry[6] == 0 && tiaoAry[7] == 0) {
            // 边章89条
            for (CsMj mj : tiao) {
                if (mj.getPai() == 8 || mj.getPai() == 9) {
                    bzList.add(mj);
                }
            }
        }

        if (tongAry[1] == 1 && tongAry[2] == 1 && tongAry[3] == 0 && tongAry[4] == 0) {
            // 边章12筒
            for (CsMj mj : tong) {
                if (mj.getPai() == 1 || mj.getPai() == 2) {
                    bzList.add(mj);
                }
            }
        }

        if (tongAry[8] == 1 && tongAry[9] == 1 && tongAry[6] == 0 && tongAry[7] == 0) {
            for (CsMj mj : tong) {
                if (mj.getPai() == 8 || mj.getPai() == 9) {
                    bzList.add(mj);
                }
            }
        }

        if (wanAry[1] == 1 && wanAry[2] == 1 && wanAry[3] == 0 && wanAry[4] == 0) {
            // 边章12万
            for (CsMj mj : wan) {
                if (mj.getPai() == 1 || mj.getPai() == 2) {
                    bzList.add(mj);
                }
            }
        }

        if (wanAry[8] == 1 && wanAry[9] == 1 && wanAry[6] == 0 && wanAry[7] == 0) {
            for (CsMj mj : wan) {
                if (mj.getPai() == 8 || mj.getPai() == 9) {
                    bzList.add(mj);
                }
            }
        }

        if (bzList.size() > 0) {
             //System.out.println("手牌存在边张门子（12，89） bzList=== " + bzList);
        }
        return bzList;
    }

    public static List<CsMj> IsKaBianZhang(List<CsMj> hand) {
        //1 条 2 筒 3万
        //手牌存在卡边张门子（124，689），即1和9没有组成一对，但有一个2或者8和它们相邻，也有4或者6，但没有35或57 -
        // [0,0,0,0,0,0,0,0,0,0]  [0,1条数量,---,9条数量]
        int[] tiaoAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] tongAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] wanAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        List<CsMj> tiao = new ArrayList<>();
        List<CsMj> tong = new ArrayList<>();
        List<CsMj> wan = new ArrayList<>();
        for (CsMj mj : hand) {
            if (mj.getHuase() == 1) {
                tiaoAry[mj.getPai()] = tiaoAry[mj.getPai()] + 1;
                tiao.add(mj);
            }
            if (mj.getHuase() == 2) {
                tongAry[mj.getPai()] = tongAry[mj.getPai()] + 1;
                tong.add(mj);
            }
            if (mj.getHuase() == 3) {
                wanAry[mj.getPai()] = wanAry[mj.getPai()] + 1;
                wan.add(mj);
            }
        }
        List<CsMj> bzList = new ArrayList<>();
        if (tiaoAry[1] == 1 && tiaoAry[2] >= 1 && tiaoAry[3] == 0 && tiaoAry[4] >= 1 && tiaoAry[5] == 0) {
            // 卡边张门子124
            for (CsMj mj : tiao) {
                if (mj.getPai() == 1) {
                    bzList.add(mj);
                }
            }
        }

        if (tiaoAry[9] == 1 && tiaoAry[6] >= 1 && tiaoAry[5] == 0 && tiaoAry[8] >= 1 && tiaoAry[7] == 0) {
            // 卡边张门子689 1个9 多个68 无57
            for (CsMj mj : tiao) {
                if (mj.getPai() == 9) {
                    bzList.add(mj);
                }
            }
        }

        if (tongAry[1] == 1 && tongAry[2] >= 1 && tongAry[3] == 0 && tongAry[4] >= 1 && tongAry[5] == 0) {
            // 卡边张门子124
            for (CsMj mj : tong) {
                if (mj.getPai() == 1) {
                    bzList.add(mj);
                }
            }
        }

        if (tongAry[9] == 1 && tongAry[6] >= 1 && tongAry[5] == 0 && tongAry[8] >= 1 && tongAry[7] == 0) {
            // 卡边张门子689 1个9 多个68 无57
            for (CsMj mj : tong) {
                if (mj.getPai() == 9) {
                    bzList.add(mj);
                }
            }
        }

        if (wanAry[1] == 1 && wanAry[2] >= 1 && wanAry[3] == 0 && wanAry[4] >= 1 && wanAry[5] == 0) {
            // 卡边张门子124
            for (CsMj mj : wan) {
                if (mj.getPai() == 1) {
                    bzList.add(mj);
                }
            }
        }

        if (wanAry[9] == 1 && wanAry[6] >= 1 && wanAry[5] == 0 && wanAry[8] >= 1 && wanAry[7] == 0) {
            // 卡边张门子689 1个9 多个68 无57
            for (CsMj mj : wan) {
                if (mj.getPai() == 9) {
                    bzList.add(mj);
                }
            }
        }

        if (bzList.size() > 0) {
             //System.out.println("手牌存在卡边张门子（124，689） bzList=== " + bzList);
        }
        return bzList;
    }

    public static List<CsMj> IsKaZhang(List<CsMj> hand) {
        //1 条 2 筒 3万
        //手牌存在卡张门子（35，79，46等），比如3和5没有组成一对，没有2或6和它们相邻，也没有4
        // [0,0,0,0,0,0,0,0,0,0]  [0,1条数量,---,9条数量]
        int[] tiaoAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] tongAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] wanAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        List<CsMj> tiao = new ArrayList<>();
        List<CsMj> tong = new ArrayList<>();
        List<CsMj> wan = new ArrayList<>();
        for (CsMj mj : hand) {
            if (mj.getHuase() == 1) {
                tiaoAry[mj.getPai()] = tiaoAry[mj.getPai()] + 1;
                tiao.add(mj);
            }
            if (mj.getHuase() == 2) {
                tongAry[mj.getPai()] = tongAry[mj.getPai()] + 1;
                tong.add(mj);
            }
            if (mj.getHuase() == 3) {
                wanAry[mj.getPai()] = wanAry[mj.getPai()] + 1;
                wan.add(mj);
            }
        }
        List<CsMj> bzList = new ArrayList<>();
        // IsKaZhang
        for (int i = 2; i <= 8; i++) {
            int mjval = i;//这是mjval 条同万
            if (mjval >= 2 && mjval <= 8) {
                if (tiaoAry[mjval] == 0 && tiaoAry[mjval - 1] == 1 && tiaoAry[mjval + 1] == 1) {
                    for (CsMj mj : tiao) {
                        if (mj.getPai() == (mjval - 1) || mj.getPai() == (mjval + 1)) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
                if (tongAry[mjval] == 0 && tongAry[mjval - 1] == 1 && tongAry[mjval + 1] == 1) {
                    for (CsMj mj : tong) {
                        if (mj.getPai() == (mjval - 1) || mj.getPai() == (mjval + 1)) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }

                if (wanAry[mjval] == 0 && wanAry[mjval - 1] == 1 && wanAry[mjval + 1] == 1) {
                    for (CsMj mj : wan) {
                        if (mj.getPai() == (mjval - 1) || mj.getPai() == (mjval + 1)) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
            }
        }


        if (bzList.size() > 0) {
            //优先19 3467 再258
            bzList = resortList(bzList);
             //System.out.println("存在卡张门子（35，79，46等 bzList=== " + bzList);
        }
        return bzList;
    }

    public static List<CsMj> IsBianZhangJiaDui(List<CsMj> hand) {
        //1 条 2 筒 3万
        //手牌存在边张门子加对（889，899，112，122）比如122，没有34
        // [0,0,0,0,0,0,0,0,0,0]  [0,1条数量,---,9条数量]
        int[] tiaoAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] tongAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] wanAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        List<CsMj> tiao = new ArrayList<>();
        List<CsMj> tong = new ArrayList<>();
        List<CsMj> wan = new ArrayList<>();
        for (CsMj mj : hand) {
            if (mj.getHuase() == 1) {
                tiaoAry[mj.getPai()] = tiaoAry[mj.getPai()] + 1;
                tiao.add(mj);
            }
            if (mj.getHuase() == 2) {
                tongAry[mj.getPai()] = tongAry[mj.getPai()] + 1;
                tong.add(mj);
            }
            if (mj.getHuase() == 3) {
                wanAry[mj.getPai()] = wanAry[mj.getPai()] + 1;
                wan.add(mj);
            }
        }
        List<CsMj> bzList = new ArrayList<>();
        //112
        if (tiaoAry[1] == 2 && tiaoAry[2] >= 1 && tiaoAry[3] == 0 && tiaoAry[4] == 0) {
            for (CsMj mj : tiao) {
                if (mj.getPai() == 1) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
        }
        //122
        if (tiaoAry[2] == 2 && tiaoAry[1] >= 1 && tiaoAry[3] == 0 && tiaoAry[4] == 0) {
            for (CsMj mj : tiao) {
                if (mj.getPai() == 2) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
        }
        //889
        if (tiaoAry[8] == 2 && tiaoAry[9] >= 1 && tiaoAry[6] == 0 && tiaoAry[7] == 0) {
            for (CsMj mj : tiao) {
                if (mj.getPai() == 8) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
        }
        //899
        if (tiaoAry[9] == 2 && tiaoAry[8] >= 1 && tiaoAry[6] == 0 && tiaoAry[7] == 0) {
            for (CsMj mj : tiao) {
                if (mj.getPai() == 9) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
        }

        if (bzList.size() > 0) {
             //System.out.println("手牌存在边张门子加对（889，899，112，122）   bzList=== " + bzList);
        }
        return bzList;
    }

    public static List<CsMj> IsKaZhangJiaDui(List<CsMj> hand) {
        //1 条 2 筒 3万
        //手牌存在卡张门子加对（886，799，113等）比如688，没有579
        // [0,0,0,0,0,0,0,0,0,0]  [0,1条数量,---,9条数量]
        int[] tiaoAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] tongAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] wanAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        List<CsMj> tiao = new ArrayList<>();
        List<CsMj> tong = new ArrayList<>();
        List<CsMj> wan = new ArrayList<>();
        for (CsMj mj : hand) {
            if (mj.getHuase() == 1) {
                tiaoAry[mj.getPai()] = tiaoAry[mj.getPai()] + 1;
                tiao.add(mj);
            }
            if (mj.getHuase() == 2) {
                tongAry[mj.getPai()] = tongAry[mj.getPai()] + 1;
                tong.add(mj);
            }
            if (mj.getHuase() == 3) {
                wanAry[mj.getPai()] = wanAry[mj.getPai()] + 1;
                wan.add(mj);
            }
        }
        List<CsMj> bzList = new ArrayList<>();
        // IsKaZhangjiadui
        for (int i = 2; i <= 8; i++) {
            int mjval = i;//这是mjval 条同万
            if (mjval >= 2 && mjval <= 8) {
                //卡张门子加对
                if (tiaoAry[mjval] == 0 && tiaoAry[mjval - 1] >= 1 && tiaoAry[mjval + 1] >= 1 && (tiaoAry[mjval - 1] >= 2 || tiaoAry[mjval + 1] >= 2)) {
                    for (CsMj mj : tiao) {
                        if (mj.getPai() == (mjval - 1) || mj.getPai() == (mjval + 1)) {
                            if (tiaoAry[mj.getPai()] >= 2) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
                if (tongAry[mjval] == 0 && tongAry[mjval - 1] >= 1 && tongAry[mjval + 1] >= 1 && (tongAry[mjval - 1] >= 2 || tongAry[mjval + 1] >= 2)) {
                    for (CsMj mj : tong) {
                        if (mj.getPai() == (mjval - 1) || mj.getPai() == (mjval + 1)) {
                            if (tongAry[mj.getPai()] >= 2) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
                if (wanAry[mjval] == 0 && wanAry[mjval - 1] >= 1 && wanAry[mjval + 1] >= 1 && (wanAry[mjval - 1] >= 2 || wanAry[mjval + 1] >= 2)) {
                    for (CsMj mj : wan) {
                        if (mj.getPai() == (mjval - 1) || mj.getPai() == (mjval + 1)) {
                            if (wanAry[mj.getPai()] >= 2) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
            }
        }


        if (bzList.size() > 0) {
            //优先19 3467 再258
            bzList = resortList(bzList);
             //System.out.println("手牌存在卡张门子加对（886，799，113等）  bzList=== " + bzList);
        }
        return bzList;
    }

    public static List<CsMj> IsXiangLianJiaKa(List<CsMj> hand) {
        //1 条 2 筒 3万
        //手牌存在相连门子带卡（346，578等）比如346，没有7
        // [0,0,0,0,0,0,0,0,0,0]  [0,1条数量,---,9条数量]
        int[] tiaoAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0,0,0};
        int[] tongAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0,0,0};
        int[] wanAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0,0,0};

        List<CsMj> tiao = new ArrayList<>();
        List<CsMj> tong = new ArrayList<>();
        List<CsMj> wan = new ArrayList<>();
        for (CsMj mj : hand) {
            if (mj.getHuase() == 1) {
                tiaoAry[mj.getPai()] = tiaoAry[mj.getPai()] + 1;
                tiao.add(mj);
            }
            if (mj.getHuase() == 2) {
                tongAry[mj.getPai()] = tongAry[mj.getPai()] + 1;
                tong.add(mj);
            }
            if (mj.getHuase() == 3) {
                wanAry[mj.getPai()] = wanAry[mj.getPai()] + 1;
                wan.add(mj);
            }
        }
        List<CsMj> bzList = new ArrayList<>();
        //
        for (int i = 1; i <= 8; i++) {
            int mjval = i;//这是mjval 条同万
                //相连门子带卡；模型   x-val-x-x 、、  **5val78
                if ( tiaoAry[mjval] == 0 && tiaoAry[mjval - 1] >= 1 && tiaoAry[mjval + 1] >= 1 && tiaoAry[mjval + 2] >= 1  ) {
                    for (CsMj mj : tiao) {
                        //if (mj.getPai() == (mjval - 1) || mj.getPai() == (mjval + 1) || mj.getPai() == (mjval + 2)) {
                         if (mj.getPai() == (mjval - 1)) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
                if (   tiaoAry[mjval] == 0 && tiaoAry[mjval - 1] >= 1 && tiaoAry[mjval - 2] >= 1 && tiaoAry[mjval + 1] >= 1&& tiaoAry[mjval + 2] ==0 && tiaoAry[mjval + 3] ==0) {
                    for (CsMj mj : tiao) {
                        //xx-val-x 、、  //34val6**  67*9
                        if ( mj.getPai() == (mjval + 1)) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
            if ( tongAry[mjval] == 0 && tongAry[mjval - 1] >= 1 && tongAry[mjval + 1] >= 1 && tongAry[mjval + 2] >= 1  ) {
                for (CsMj mj : tong) {
                    //if (mj.getPai() == (mjval - 1) || mj.getPai() == (mjval + 1) || mj.getPai() == (mjval + 2)) {
                    if (mj.getPai() == (mjval - 1)) {
                        if (!bzList.contains(mj)) {
                            bzList.add(mj);
                        }
                    }
                }
            }
            if (   tongAry[mjval] == 0 && tongAry[mjval - 1] >= 1 && tongAry[mjval - 2] >= 1 && tongAry[mjval + 1] >= 1&& tongAry[mjval + 2] ==0 && tongAry[mjval + 3] ==0) {
                for (CsMj mj : tong) {
                    //xx-val-x 、、  //34val6**  67*9
                    if ( mj.getPai() == (mjval + 1)) {
                        if (!bzList.contains(mj)) {
                            bzList.add(mj);
                        }
                    }
                }
            }
            if ( wanAry[mjval] == 0 && wanAry[mjval - 1] >= 1 && wanAry[mjval + 1] >= 1 && wanAry[mjval + 2] >= 1  ) {
                for (CsMj mj : wan) {
                    //if (mj.getPai() == (mjval - 1) || mj.getPai() == (mjval + 1) || mj.getPai() == (mjval + 2)) {
                    if (mj.getPai() == (mjval - 1)) {
                        if (!bzList.contains(mj)) {
                            bzList.add(mj);
                        }
                    }
                }
            }
            if (   wanAry[mjval] == 0 && wanAry[mjval - 1] >= 1 && wanAry[mjval - 2] >= 1 && wanAry[mjval + 1] >= 1&& wanAry[mjval + 2] ==0 && wanAry[mjval + 3] ==0) {
                for (CsMj mj : wan) {
                    //xx-val-x 、、  //34val6**  67*9
                    if ( mj.getPai() == (mjval + 1)) {
                        if (!bzList.contains(mj)) {
                            bzList.add(mj);
                        }
                    }
                }
            }

        }

        if (bzList.size() > 0) {
             //System.out.println("手牌存在相连门子带卡（346，578等）     bzList=== " + bzList);
            //应该出的牌
             //System.out.println("hand:" + hand);
             //System.out.println("移除:" + bzList);
//            hand.removeAll(bzList);
            bzList = resortList(bzList);
             //System.out.println("可出的剩余hand:" + bzList);
            return bzList;
        }

        if (bzList.size() == 0) {
            return new ArrayList<>();
        }
        return bzList;
    }

    public static List<CsMj> IsXiangLianJiaDui(List<CsMj> hand) {
        //1 条 2 筒 3万
        //手牌存在相连门子带对（344，778等）比如344，没有6和1
        // [0,0,0,0,0,0,0,0,0,0]  [0,1条数量,---,9条数量]
        int[] tiaoAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] tongAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] wanAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        List<CsMj> tiao = new ArrayList<>();
        List<CsMj> tong = new ArrayList<>();
        List<CsMj> wan = new ArrayList<>();
        for (CsMj mj : hand) {
            if (mj.getHuase() == 1) {
                tiaoAry[mj.getPai()] = tiaoAry[mj.getPai()] + 1;
                tiao.add(mj);
            }
            if (mj.getHuase() == 2) {
                tongAry[mj.getPai()] = tongAry[mj.getPai()] + 1;
                tong.add(mj);
            }
            if (mj.getHuase() == 3) {
                wanAry[mj.getPai()] = wanAry[mj.getPai()] + 1;
                wan.add(mj);
            }
        }
        List<CsMj> bzList = new ArrayList<>();
        //
        for (int i = 2; i <= 8; i++) {
            int mjval = i;//这是mjval 条同万
            if (mjval >= 3 && mjval <= 7) {
                // 模型   val-x-x 344  valval-x 334 比如344，没有6和1
                if ((mjval + 3) < 10 && tiaoAry[mjval] == 1 && tiaoAry[mjval + 1] >= 2 && tiaoAry[mjval + 3] >= 0 && tiaoAry[mjval - 2] == 0) {
                    for (CsMj mj : tiao) {
                        if (mj.getPai() == (mjval + 1)) {
                            //打拆对
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
                //778 没5和9
                if (tiaoAry[mjval] == 2 && tiaoAry[mjval - 1] >= 0 && tiaoAry[mjval - 2] == 0 && tiaoAry[mjval + 2] == 0) {
                    for (CsMj mj : tiao) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }

                if ((mjval + 3) < 10 && tongAry[mjval] == 1 && tongAry[mjval + 1] >= 2 && tongAry[mjval + 3] >= 0 && tongAry[mjval - 2] == 0) {
                    for (CsMj mj : tong) {
                        if (mj.getPai() == (mjval + 1)) {
                            //打拆对
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
                //778 没5和9
                if (tongAry[mjval] == 2 && tongAry[mjval - 1] >= 0 && tongAry[mjval - 2] == 0 && tongAry[mjval + 2] == 0) {
                    for (CsMj mj : tong) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }

                if ((mjval + 3) < 10 && wanAry[mjval] == 1 && wanAry[mjval + 1] >= 2 && wanAry[mjval + 3] >= 0 && wanAry[mjval - 2] == 0) {
                    for (CsMj mj : wan) {
                        if (mj.getPai() == (mjval + 1)) {
                            //打拆对
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
                //778 没5和9
                if (wanAry[mjval] == 2 && wanAry[mjval - 1] >= 0 && wanAry[mjval - 2] == 0 && wanAry[mjval + 2] == 0) {
                    for (CsMj mj : wan) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
            }
        }

        if (bzList.size() > 0) {
             //System.out.println("手牌：" + hand);
             //System.out.println("手牌存在相连门子带对（344，778等）   List=== " + bzList);
            //应该出的牌
            bzList = resortList(bzList);
             //System.out.println("可出的剩余hand:" + bzList);
            return bzList;
        }

        if (bzList.size() == 0) {
            return new ArrayList<>();
        }
        return bzList;
    }

    public static List<CsMj> IsDanDui(List<CsMj> hand) {
        //1 条 2 筒 3万
        //手牌存在单独一对（44，77等）比如44，没有2356; 9是没78  1 没23
        // [0,0,0,0,0,0,0,0,0,0]  [0,1条数量,---,9条数量]
        int[] tiaoAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0,0,0};
        int[] tongAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0,0,0};
        int[] wanAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0,0,0};

        List<CsMj> tiao = new ArrayList<>();
        List<CsMj> tong = new ArrayList<>();
        List<CsMj> wan = new ArrayList<>();
        for (CsMj mj : hand) {
            if (mj.getHuase() == 1) {
                tiaoAry[mj.getPai()] = tiaoAry[mj.getPai()] + 1;
                tiao.add(mj);
            }
            if (mj.getHuase() == 2) {
                tongAry[mj.getPai()] = tongAry[mj.getPai()] + 1;
                tong.add(mj);
            }
            if (mj.getHuase() == 3) {
                wanAry[mj.getPai()] = wanAry[mj.getPai()] + 1;
                wan.add(mj);
            }
        }
        List<CsMj> bzList = new ArrayList<>();
        int[] ary ={1,2,8,9};
        for (int i=0;i<ary.length;i++){
            int mjval = ary[i];
            if(mjval==1){
                //23
                if (tiaoAry[mjval] == 2 && tiaoAry[mjval +1]==0  && tiaoAry[mjval +2]==0){
                    for (CsMj mj : tiao) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
                if (tongAry[mjval] == 2 && tongAry[mjval +1]==0  && tongAry[mjval +2]==0){
                    for (CsMj mj : tong) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
                if (wanAry[mjval] == 2 && wanAry[mjval +1]==0  && wanAry[mjval +2]==0){
                    for (CsMj mj : wan) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
            }

            if(mjval==2){//134
                if (tiaoAry[mjval] == 2 && tiaoAry[mjval -1]==0  && tiaoAry[mjval +1]==0 && tiaoAry[mjval +2]==0){
                    for (CsMj mj : tiao) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
                if (tongAry[mjval] == 2 && tongAry[mjval -1]==0  && tongAry[mjval +1]==0 && tongAry[mjval +2]==0){
                    for (CsMj mj : tong) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
                if (wanAry[mjval] == 2 && wanAry[mjval -1]==0  && wanAry[mjval +1]==0 && wanAry[mjval +2]==0){
                    for (CsMj mj : wan) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
            }

            if(mjval==8){
                if (tiaoAry[mjval] == 2 && tiaoAry[mjval -1]==0  && tiaoAry[mjval -2]==0 && tiaoAry[mjval +1]==0){
                    for (CsMj mj : tiao) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
                if (tongAry[mjval] == 2 && tongAry[mjval -1]==0  && tongAry[mjval -2]==0 && tongAry[mjval +1]==0){
                    for (CsMj mj : tong) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
                if (wanAry[mjval] == 2 && wanAry[mjval -1]==0  && wanAry[mjval -2]==0 && wanAry[mjval +1]==0){
                    for (CsMj mj : wan) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
            }

            if(mjval==9){
                if (tiaoAry[mjval] == 2 && tiaoAry[mjval -1]==0  && tiaoAry[mjval -2]==0 ){
                    for (CsMj mj : tiao) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
                if (tongAry[mjval] == 2 && tongAry[mjval -1]==0  && tongAry[mjval -2]==0 ){
                    for (CsMj mj : tong) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
                if (wanAry[mjval] == 2 && wanAry[mjval -1]==0  && wanAry[mjval -2]==0 ){
                    for (CsMj mj : wan) {
                        if (mj.getPai() == mjval) {
                            if (!bzList.contains(mj)) {
                                bzList.add(mj);
                            }
                        }
                    }
                }
            }
        }
        for (int i = 3; i <= 7; i++) {
            int mjval = i;
            /// 4-7 单独一对
            if (tiaoAry[mjval] == 2 && tiaoAry[mjval - 2] == 0 && tiaoAry[mjval - 1] == 0 && tiaoAry[mjval + 1] == 0 && tiaoAry[mjval + 2] == 0) {
                for (CsMj mj : tiao) {
                    if (mj.getPai() == mjval) {
                        if (!bzList.contains(mj)) {
                            bzList.add(mj);
                        }
                    }
                }
            }
            if (tongAry[mjval] == 2 && tongAry[mjval - 2] == 0 && tongAry[mjval - 1] == 0 && tongAry[mjval + 1] == 0 && tongAry[mjval + 2] == 0) {
                for (CsMj mj : tong) {
                    if (mj.getPai() == mjval) {
                        if (!bzList.contains(mj)) {
                            bzList.add(mj);
                        }
                    }
                }
            }
            if (wanAry[mjval] == 2 && wanAry[mjval - 2] == 0 && wanAry[mjval - 1] == 0 && wanAry[mjval + 1] == 0 && wanAry[mjval + 2] == 0) {
                for (CsMj mj : wan) {
                    if (mj.getPai() == mjval) {
                        if (!bzList.contains(mj)) {
                            bzList.add(mj);
                        }
                    }
                }
            }
        }

        if (bzList.size() > 0) {
             //System.out.println("手牌：" + hand);
             //System.out.println("比如44，没有2356; 9是没78  1 没23   List=== " + bzList);
            //应该出的牌
            bzList = resortList(bzList);
             //System.out.println("可出的剩余hand:" + bzList);
            return bzList;
        }

        if (bzList.size() == 0) {
            return new ArrayList<>();
        }
        return bzList;
    }

    public static List<CsMj> IsXiangLian(List<CsMj> hand) {
        //1 条 2 筒 3万
        //手牌存在相连门子（34，78等）比如34，没有1和6
        // [0,0,0,0,0,0,0,0,0,0]  [0,1条数量,---,9条数量]
        int[] tiaoAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] tongAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] wanAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        List<CsMj> tiao = new ArrayList<>();
        List<CsMj> tong = new ArrayList<>();
        List<CsMj> wan = new ArrayList<>();
        for (CsMj mj : hand) {
            if (mj.getHuase() == 1) {
                tiaoAry[mj.getPai()] = tiaoAry[mj.getPai()] + 1;
                tiao.add(mj);
            }
            if (mj.getHuase() == 2) {
                tongAry[mj.getPai()] = tongAry[mj.getPai()] + 1;
                tong.add(mj);
            }
            if (mj.getHuase() == 3) {
                wanAry[mj.getPai()] = wanAry[mj.getPai()] + 1;
                wan.add(mj);
            }
        }
        List<CsMj> bzList = new ArrayList<>();
        for (int i = 3; i <= 7; i++) {
            int mjval = i;
            /// 比如34，没有1和6
            if (mjval + 3 < 10 && tiaoAry[mjval] == 1 && tiaoAry[mjval + 1] == 1 && tiaoAry[mjval - 2] == 0 && tiaoAry[mjval - 1] == 0 && tiaoAry[mjval + 2] == 0) {
                for (CsMj mj : tiao) {
                    if (mj.getPai() == mjval || mj.getPai() == mjval + 1) {
                        if (!bzList.contains(mj)) {
                            bzList.add(mj);
                        }
                    }
                }
            }
            if (mjval + 3 < 10 && tongAry[mjval] == 1 && tongAry[mjval + 1] == 1 && tongAry[mjval - 2] == 0 && tongAry[mjval - 1] == 0 && tongAry[mjval + 2] == 0) {
                for (CsMj mj : tong) {
                    if (mj.getPai() == mjval || mj.getPai() == mjval + 1) {
                        if (!bzList.contains(mj)) {
                            bzList.add(mj);
                        }
                    }
                }
            }
            if (mjval + 3 < 10 && wanAry[mjval] == 1 && wanAry[mjval + 1] == 1 && wanAry[mjval - 2] == 0 && wanAry[mjval - 1] == 0 && wanAry[mjval + 2] == 0) {
                for (CsMj mj : wan) {
                    if (mj.getPai() == mjval || mj.getPai() == mjval + 1) {
                        if (!bzList.contains(mj)) {
                            bzList.add(mj);
                        }
                    }
                }
            }


        }

        if (bzList.size() > 0) {
             //System.out.println("手牌：" + hand);
             //System.out.println("手牌存在相连门子（34，78等）比如34，没有1和6    List=== " + bzList);
            //应该出的牌
            bzList = resortList(bzList);
             //System.out.println("可出的剩余hand:" + bzList);
            return bzList;
        }

        if (bzList.size() == 0) {
            return new ArrayList<>();
        }
        return bzList;
    }

    public static List<CsMj> IsYou19(List<CsMj> hand) {
        //手牌存在 19
        // [0,0,0,0,0,0,0,0,0,0]  [0,1条数量,---,9条数量]
        int[] tiaoAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] tongAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] wanAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        List<CsMj> tiao = new ArrayList<>();
        List<CsMj> tong = new ArrayList<>();
        List<CsMj> wan = new ArrayList<>();
        for (CsMj mj : hand) {
            if (mj.getHuase() == 1) {
                tiaoAry[mj.getPai()] = tiaoAry[mj.getPai()] + 1;
                tiao.add(mj);
            }
            if (mj.getHuase() == 2) {
                tongAry[mj.getPai()] = tongAry[mj.getPai()] + 1;
                tong.add(mj);
            }
            if (mj.getHuase() == 3) {
                wanAry[mj.getPai()] = wanAry[mj.getPai()] + 1;
                wan.add(mj);
            }
        }
        List<CsMj> bzList = new ArrayList<>();
        if (tiaoAry[1] >= 1 || tiaoAry[9] >= 1) {
            for (CsMj mj : tiao) {
                if (mj.getPai() == 1 || mj.getPai() == 9) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
        }
        if (tongAry[1] >= 1 || tongAry[9] >= 1) {
            for (CsMj mj : tong) {
                if (mj.getPai() == 1 || mj.getPai() == 9) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
        }
        if (wanAry[1] >= 1 || wanAry[9] >= 1) {
            for (CsMj mj : wan) {
                if (mj.getPai() == 1 || mj.getPai() == 9) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
        }
        bzList = resortList(bzList);
         //System.out.println("手牌存在1或9 List= " + bzList);
        return bzList;
    }

    public static List<CsMj> IsYou258(List<CsMj> hand) {
        //手牌存在 258
        // [0,0,0,0,0,0,0,0,0,0]  [0,1条数量,---,9条数量]
        int[] tiaoAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] tongAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] wanAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        List<CsMj> tiao = new ArrayList<>();
        List<CsMj> tong = new ArrayList<>();
        List<CsMj> wan = new ArrayList<>();
        for (CsMj mj : hand) {
            if (mj.getHuase() == 1) {
                tiaoAry[mj.getPai()] = tiaoAry[mj.getPai()] + 1;
                tiao.add(mj);
            }
            if (mj.getHuase() == 2) {
                tongAry[mj.getPai()] = tongAry[mj.getPai()] + 1;
                tong.add(mj);
            }
            if (mj.getHuase() == 3) {
                wanAry[mj.getPai()] = wanAry[mj.getPai()] + 1;
                wan.add(mj);
            }
        }
        List<CsMj> bzList = new ArrayList<>();
        if (tiaoAry[2] >= 1 || tiaoAry[5] >= 1 || tiaoAry[8] >= 1) {
            for (CsMj mj : tiao) {
                if (mj.getPai() == 2 || mj.getPai() == 5 || mj.getPai() == 8) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
        }
        if (tongAry[2] >= 1 || tongAry[5] >= 1 || tongAry[8] >= 1) {
            for (CsMj mj : tong) {
                if (mj.getPai() == 2 || mj.getPai() == 5 || mj.getPai() == 8) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
        }

        if (wanAry[2] >= 1 || wanAry[5] >= 1 || wanAry[8] >= 1) {
            for (CsMj mj : wan) {
                if (mj.getPai() == 2 || mj.getPai() == 5 || mj.getPai() == 8) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
        }

        bzList = resortList(bzList);
         //System.out.println("手牌存在258  List= " + bzList);
        return bzList;
    }

    public static List<CsMj> IsYou3479(List<CsMj> hand) {

        //手牌存在 3479
        // [0,0,0,0,0,0,0,0,0,0]  [0,1条数量,---,9条数量]
        int[] tiaoAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] tongAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] wanAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        List<CsMj> tiao = new ArrayList<>();
        List<CsMj> tong = new ArrayList<>();
        List<CsMj> wan = new ArrayList<>();
        for (CsMj mj : hand) {
            if (mj.getHuase() == 1) {
                tiaoAry[mj.getPai()] = tiaoAry[mj.getPai()] + 1;
                tiao.add(mj);
            }
            if (mj.getHuase() == 2) {
                tongAry[mj.getPai()] = tongAry[mj.getPai()] + 1;
                tong.add(mj);
            }
            if (mj.getHuase() == 3) {
                wanAry[mj.getPai()] = wanAry[mj.getPai()] + 1;
                wan.add(mj);
            }
        }
        List<CsMj> bzList = new ArrayList<>();
        if (tiaoAry[3] >= 1 || tiaoAry[4] >= 1 || tiaoAry[7] >= 1 || tiaoAry[9] >= 1) {
            for (CsMj mj : tiao) {
                if (mj.getPai() == 3 || mj.getPai() == 4 || mj.getPai() == 7 || mj.getPai() == 9) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
        }

        if (tongAry[3] >= 1 || tongAry[4] >= 1 || tongAry[7] >= 1 || tongAry[9] >= 1) {
            for (CsMj mj : tong) {
                if (mj.getPai() == 3 || mj.getPai() == 4 || mj.getPai() == 7 || mj.getPai() == 9) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
        }

        if (wanAry[3] >= 1 || wanAry[4] >= 1 || wanAry[7] >= 1 || wanAry[9] >= 1) {
            for (CsMj mj : wan) {
                if (mj.getPai() == 3 || mj.getPai() == 4 || mj.getPai() == 7 || mj.getPai() == 9) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
        }

        bzList = resortList(bzList);
         //System.out.println("手牌存在3479  List= " + bzList);
        return bzList;
    }

    public static List<CsMj> getDuiJiang(List<CsMj> tiao, List<CsMj> tong, List<CsMj> wan, int[] tiaoAry, int[] tongAry, int[] wanAry) {
        //是否含有一对将
        List<CsMj> bzList = new ArrayList<>();
        int[] ary = {2, 5, 8};
        for (int mjval : ary) {
            for (CsMj mj : tiao) {
                if (mj.getPai() == mjval && tiaoAry[mjval] == 2) {
                        bzList.add(mj);
                        if(bzList.size()>=2){
                            //System.out.println("手牌里面含有将的对子：List= " + bzList);
                           return bzList;
                        }
                }

            }
            for (CsMj mj : tong) {
                if (mj.getPai() == mjval && tongAry[mjval] == 2) {
                        bzList.add(mj);
                        if(bzList.size()>=2){
                            //System.out.println("手牌里面含有将的对子：List= " + bzList);
                            return bzList;
                        }
                }
            }
            for (CsMj mj : wan) {
                if (mj.getPai() == mjval && wanAry[mjval] == 2) {
                        bzList.add(mj);
                        if(bzList.size()>=2){
                          //   //System.out.println("手牌里面含有将的对子：List= " + bzList);
                            return bzList;
                        }
                }
            }
        }

        if (bzList.size() > 0) {
            //System.out.println("手牌里面含有将的对子：List= " + bzList);
        }

        return bzList;
    }

    public static List<CsMj> getDuiJiang2(List<CsMj> tiao, List<CsMj> tong, List<CsMj> wan, int[] tiaoAry, int[] tongAry, int[] wanAry) {
        //是否含有一对将
        List<CsMj> bzList = new ArrayList<>();
        int[] ary = {2, 5, 8};
        for (int mjval : ary) {
            for (CsMj mj : tiao) {
                if (mj.getPai() == mjval && tiaoAry[mjval] == 2) {
                    bzList.add(mj);

                }

            }
            for (CsMj mj : tong) {
                if (mj.getPai() == mjval && tongAry[mjval] == 2) {
                    bzList.add(mj);

                }
            }
            for (CsMj mj : wan) {
                if (mj.getPai() == mjval && wanAry[mjval] == 2) {
                    bzList.add(mj);

                }
            }
        }


        return bzList;
    }

    public static List<CsMj> getDui134679(List<CsMj> tiao, List<CsMj> tong, List<CsMj> wan, int[] tiaoAry, int[] tongAry, int[] wanAry) {
        //是否含有非将的对子
        List<CsMj> bzList = new ArrayList<>();
        int[] ary = {1, 3, 4, 6, 7, 9};
        for (int mjval : ary) {
            for (CsMj mj : tiao) {
                if (mj.getPai() == mjval && tiaoAry[mjval] == 2) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
            for (CsMj mj : tong) {
                if (mj.getPai() == mjval && tongAry[mjval] == 2) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
            for (CsMj mj : wan) {
                if (mj.getPai() == mjval && wanAry[mjval] == 2) {
                    if (!bzList.contains(mj)) {
                        bzList.add(mj);
                    }
                }
            }
        }
        if (bzList.size() > 0) {
             //System.out.println("手牌里面含有非将的对子：List= " + bzList);
        }
        return bzList;
    }

    public static List<CsMj> getKan(List<CsMj> tiao, List<CsMj> tong, List<CsMj> wan, int[] tiaoAry, int[] tongAry, int[] wanAry) {
        //手牌中的坎牌 如555
        List<CsMj> bzList = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            if (tiaoAry[i] == 3) {
                for (CsMj mj : tiao) {
                    if (mj.getPai() == i) {
                        if (!bzList.contains(mj)) {
                            bzList.add(mj);
                        }
                    }
                }
            }
        }
        for (int i = 1; i <= 9; i++) {
            if (tongAry[i] == 3) {
                for (CsMj mj : tong) {
                    if (mj.getPai() == i) {
                        if (!bzList.contains(mj)) {
                            bzList.add(mj);
                        }
                    }
                }
            }
        }
        for (int i = 1; i <= 9; i++) {
            if (wanAry[i] == 3) {
                for (CsMj mj : wan) {
                    if (mj.getPai() == i) {
                        if (!bzList.contains(mj)) {
                            bzList.add(mj);
                        }
                    }
                }
            }
        }
        if (bzList.size() > 0) {
             //System.out.println("手中的坎牌：List = " + bzList);
        }
        return bzList;
    }


    public static List<CsMj> getShunzi(List<CsMj> list ,List<CsMj> returnlist ) {
        HashMap<String,Object> map1 = resetMap(list);
        List<CsMj> copy = new ArrayList<>(list);
        List<CsMj> tiao = (List<CsMj>) map1.get("tiao");
        List<CsMj> tong = (List<CsMj>) map1.get("tong");
        List<CsMj>  wan = (List<CsMj>) map1.get("wan");
        int[] tiaoAry = (int[]) map1.get("tiaoAry");
        int[] wanAry = (int[]) map1.get("wanAry");
        int[] tongAry = (int[]) map1.get("tongAry");

        List<CsMj> bzList = new ArrayList<>();
        List<String> bzListStr = new ArrayList<>();
         for(int n=1;n<=7;n++){
             if (tiaoAry[n] >= 1 && tiaoAry[n+1] >=  1 && tiaoAry[n + 2] >=  1) {
                 for (CsMj mj : tiao) {
                    if (mj.getPai() == n || mj.getPai() == (n +1) || mj.getPai() == (n + 2)) {
                        if (!bzListStr.contains(mj.toString())) {
                            bzList.add(mj);
                            bzListStr.add(mj.toString());
                        }
                    }
                }
                 returnlist.addAll(bzList);
                //  //System.out.println("tiao bzList "+bzList);
                 copy.removeAll(bzList);
                //  //System.out.println("cop "+copy);
                 returnlist= getShunzi(copy,returnlist);
                 break;
             }
         }
        for(int n=1;n<=7;n++){
            if (tongAry[n] >= 1 && tongAry[n+1] >=  1 && tongAry[n + 2] >=  1) {
                for (CsMj mj : tong) {
                    if (mj.getPai() == n || mj.getPai() == (n +1) || mj.getPai() == (n + 2)) {
                        if (!bzListStr.contains(mj.toString())) {
                            bzList.add(mj);
                            bzListStr.add(mj.toString());
                        }
                    }
                }
                returnlist.addAll(bzList);
               //  //System.out.println(" tong bzList "+bzList);
                copy.removeAll(bzList);
                //System.out.println("cop "+copy);
                returnlist= getShunzi(copy,returnlist);
                break;
            }
        }
        for(int n=1;n<=7;n++){
            if (wanAry[n] >= 1 && wanAry[n+1] >=  1 && wanAry[n + 2] >=  1) {
                for (CsMj mj : wan) {
                    if (mj.getPai() == n || mj.getPai() == (n +1) || mj.getPai() == (n + 2)) {
                        if (!bzListStr.contains(mj.toString())) {
                            bzList.add(mj);
                            bzListStr.add(mj.toString());
                        }
                    }
                }
                returnlist.addAll(bzList);
               //  //System.out.println("wan bzList "+bzList);
                copy.removeAll(bzList);
               //  //System.out.println("cop "+copy);
                returnlist= getShunzi(copy,returnlist);
                break;
            }
        }
//        int n = 2;
//        do {
//            if (tiaoAry[n - 1] >= 1 && tiaoAry[n] >=  1 && tiaoAry[n + 1] >=  1) {
//                for (CsMj mj : tiao) {
//                    if (mj.getPai() == n || mj.getPai() == (n - 1) || mj.getPai() == (n + 1)) {
//                        if (!bzListStr.contains(mj.toString())) {
////                            bzList.toString().contains(""+mj.toString())
//                            bzList.add(mj);
//                            bzListStr.add(mj.toString());
//                        }
//                    }
//                }
//                n = n + 3;
//            } else {
//                n++;
//            }
//        } while (n <= 8);
//
//        n = 2;
//        do {
//            if (tongAry[n - 1] >=  1 && tongAry[n] >=  1 && tongAry[n + 1] >=  1) {
//                for (CsMj mj : tong) {
//                    if (mj.getPai() == n || mj.getPai() == (n - 1) || mj.getPai() == (n + 1)) {
//                        if (!bzListStr.contains(mj.toString())) {
////                            bzList.toString().contains(""+mj.toString())
//                            bzList.add(mj);
//                            bzListStr.add(mj.toString());
//                        }
//                    }
//                }
//                n = n + 3;
//            } else {
//                n++;
//            }
//        } while (n <= 8);
//
//        n = 2;
//        do {
//            if (wanAry[n - 1] >=  1 && wanAry[n] >=  1 && wanAry[n + 1] >=  1) {
//                for (CsMj mj : wan) {
//                    if (mj.getPai() == n || mj.getPai() == (n - 1) || mj.getPai() == (n + 1)) {
//                        if (!bzListStr.contains(mj.toString())) {
////                            bzList.toString().contains(""+mj.toString())
//                            bzList.add(mj);
//                            bzListStr.add(mj.toString());
//                        }
//                    }
//                }
//                n = n + 3;
//            } else {
//                n++;
//            }
//        } while (n <= 8);
        if (returnlist.size() > 0) {
           //  //System.out.println(" 移除手牌中的顺子：List= " + returnlist);
        }
        return returnlist;
    }

    public static HashMap<String,Object> resetMap(List<CsMj> list2) {
        List<CsMj> list = new ArrayList<>(list2);
        HashMap<String,Object> map = new HashMap();
        int[] tiaoAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] tongAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] wanAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        List<CsMj> tiao = new ArrayList<>();
        List<CsMj> tong = new ArrayList<>();
        List<CsMj> wan = new ArrayList<>();
        for (CsMj mj : list) {
            if (mj.getHuase() == 1) {
                tiaoAry[mj.getPai()] = tiaoAry[mj.getPai()] + 1;
                tiao.add(mj);
            }
            if (mj.getHuase() == 2) {
                tongAry[mj.getPai()] = tongAry[mj.getPai()] + 1;
                tong.add(mj);
            }
            if (mj.getHuase() == 3) {
                wanAry[mj.getPai()] = wanAry[mj.getPai()] + 1;
                wan.add(mj);
            }
        }
        map.put("tiao",tiao);
        map.put("tong",tong);
        map.put("wan",wan);
        map.put("tiaoAry",tiaoAry);
        map.put("tongAry",tongAry);
        map.put("wanAry",wanAry);
        return map;

    }
    /**
     * 移出牌型（一对将(258)加已成牌型牌（顺子或坎） 345  555，无将对则不移对，为了更好筛选判断剩余牌型
     *
     * @param list
     */
    public static List<CsMj> removePaiXing(List<CsMj> list2) {
        List<CsMj> list =new ArrayList<>(list2);
        //判断是否含一对将。

        HashMap<String,Object> map1 = resetMap(list);
        List<CsMj> tiao = (List<CsMj>) map1.get("tiao");
        List<CsMj> tong = (List<CsMj>) map1.get("tong");
        List<CsMj>  wan = (List<CsMj>) map1.get("wan");
        int[] tiaoAry = (int[]) map1.get("tiaoAry");
        int[] wanAry = (int[]) map1.get("wanAry");
        int[] tongAry = (int[]) map1.get("tongAry");

        //System.out.println("removePaiXing:============================"+list2);
        List<CsMj> hasDuiJiang = getDuiJiang(tiao, tong, wan, tiaoAry, tongAry, wanAry);
        List<CsMj> copy = new ArrayList<>(list);
        if (hasDuiJiang.size() > 0) {
            //坎 将对 顺子
            //移除 坎
            List<CsMj> a = getKan(tiao, tong, wan, tiaoAry, tongAry, wanAry);
            if(a.size()>0){
                copy.removeAll(a);
                //System.out.println("移除坎："+a);
                //System.out.println("剩余："+copy);
            }
            //将对  拿一对将对  移除掉 再拿剩下的牌型判断类型
            map1 = resetMap(copy);
            tiao = (List<CsMj>) map1.get("tiao");
            tong = (List<CsMj>) map1.get("tong");
            wan = (List<CsMj>) map1.get("wan");
            tiaoAry = (int[]) map1.get("tiaoAry");
            wanAry = (int[]) map1.get("wanAry");
            tongAry = (int[]) map1.get("tongAry");

            List<CsMj> b = getDuiJiang(tiao, tong, wan, tiaoAry, tongAry, wanAry);
            if(b.size()==2){
                copy.removeAll(b);
                //System.out.println("移除一对将："+b);
                //System.out.println("剩余："+copy);
            }

            //移除 顺子
            List<CsMj> d = getShunzi(copy,new ArrayList<>());
            if(d.size()>0){
                copy.removeAll(d);
                //System.out.println("移除顺子："+d);
               //  //System.out.println("剩余："+copy);
            }
             //System.out.println("坎 将对 顺子 移除后：" + copy);
            return copy;
        } else {
            //移除 坎
            List<CsMj> a = getKan(tiao, tong, wan, tiaoAry, tongAry, wanAry);
            if(a.size()>0){
                copy.removeAll(a);
                //System.out.println("移除坎："+a);
                //System.out.println("剩余："+copy);
            }

            //移除 顺子

            List<CsMj> d = getShunzi(copy,new ArrayList<>());
            if(d.size()>0){
                copy.removeAll(d);
               //  //System.out.println("移除顺子："+d);
               //  //System.out.println("剩余："+copy);
            }
            //System.out.println("返回数据:"+copy);
            return copy;
        }
    }

    /**
     * 移出顺坎（相对于移出牌型，不用移将对，只需移坎和顺子，为了更好筛选判断剩余牌型
     * @param list
     * @return
     */
    public static List<CsMj> removeShunKan(List<CsMj> list) {
        HashMap<String,Object> map1 = resetMap(list);
        List<CsMj> tiao = (List<CsMj>) map1.get("tiao");
        List<CsMj> tong = (List<CsMj>) map1.get("tong");
        List<CsMj>  wan = (List<CsMj>) map1.get("wan");
        int[] tiaoAry = (int[]) map1.get("tiaoAry");
        int[] wanAry = (int[]) map1.get("wanAry");
        int[] tongAry = (int[]) map1.get("tongAry");

        //移除 坎
        List<CsMj> a = getKan(tiao, tong, wan, tiaoAry, tongAry, wanAry);
        List<CsMj>  copy = new ArrayList<>(list);
         //System.out.println("removeShunKan:============================");
        if(a.size()>0){
            copy.removeAll(a);
           //  //System.out.println("移除坎："+a);
            //System.out.println("剩余："+copy);
        }
        //移除 顺子
        List<CsMj> d = getShunzi(copy,new ArrayList<>());;
        if(d.size()>0){
            copy.removeAll(d);
            //System.out.println("移除顺子："+d);
           //  //System.out.println("剩余："+copy);
        }
         //System.out.println(copy);
            return copy;
    }

    /**
     * 是否含有多对将
     * @param list
     * @return
     */
    public static List<CsMj> isDuoDuiJiang(List<CsMj> list){
        int[] tiaoAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] tongAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] wanAry = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        List<CsMj> tiao = new ArrayList<>();
        List<CsMj> tong = new ArrayList<>();
        List<CsMj> wan = new ArrayList<>();
        for (CsMj mj : list) {
            if (mj.getHuase() == 1) {
                tiaoAry[mj.getPai()] = tiaoAry[mj.getPai()] + 1;
                tiao.add(mj);
            }
            if (mj.getHuase() == 2) {
                tongAry[mj.getPai()] = tongAry[mj.getPai()] + 1;
                tong.add(mj);
            }
            if (mj.getHuase() == 3) {
                wanAry[mj.getPai()] = wanAry[mj.getPai()] + 1;
                wan.add(mj);
            }
        }
        List<CsMj> b = getDuiJiang2(tiao, tong, wan, tiaoAry, tongAry, wanAry);
        if(b.size()>=4){
             //System.out.println("含有多对将---- "+b.toString());
        }
        return b;
    }
    public static int getTingPaiLeftNum(List<CsMj> tingmj, List<CsMj> leftmajiangs) {
        int leftTing = 0;
        for (CsMj m : tingmj) {
            for (CsMj t : leftmajiangs) {
                if (m.getHuase() == t.getHuase() && m.getPai() == t.getPai()) {
                    leftTing++;
                }
            }
        }
        return leftTing;
    }

    public static HashMap getTingPaiMap(CsMjPlayer player, int OnlyDaHu, int quanqiurJiang, List<CsMj> leftmajiangs) {
        List<CsMj> leftmajiang = new ArrayList<>(leftmajiangs);
        HashMap<String, Object> disMap = new HashMap<>();
        disMap.put("dismj", null);
        disMap.put("tingmj", null);
        disMap.put("leftting", 0);
        if (player.isAlreadyMoMajiang()) {
            List<CsMj> cards = new ArrayList<>(player.getHandMajiang());
            for (CsMj card : player.getHandMajiang()) {
                cards.remove(card);
                List<CsMj> huCards = CsMjTool.getTingMjs(cards, player.getGang(), player.getPeng(), player.getChi(),
                        player.getBuzhang(), true, OnlyDaHu == 1, quanqiurJiang);
                cards.add(card);
                if (huCards == null || huCards.size() == 0) {
                    continue;
                }

                if (null == disMap.get("tingmj")) {
                    disMap.put("dismj", card);
                    disMap.put("tingmj", huCards);
                } else {
                    List<CsMj> tingmj = (List<CsMj>) disMap.get("tingmj");
                    int leftTing = getTingPaiLeftNum(tingmj, leftmajiang);//剩余的听牌的张数
                    int leftting2 = getTingPaiLeftNum(huCards, leftmajiang);
                     //System.out.println("==================================================");
                     //System.out.println("hucards:" + huCards);
                     //System.out.println("tingmj:" + tingmj);
                     //System.out.println("leftTing:" + leftTing);
                     //System.out.println("leftting2:" + leftting2);
                    if (leftting2 > leftTing) {
                        disMap.put("dismj", card);
                        disMap.put("tingmj", huCards);
                        disMap.put("leftting", leftTing);
                         //System.out.println("更新dismap:" + disMap);
                    }
                     //System.out.println("==================================================");
                }
                //ting.setMajiangId(card.getId()); //打哪些牌。
//                for (CsMj mj : huCards) {
//                   // ting.addTingMajiangIds(mj.getId());//听哪些牌
//                }
            }
        } else {
            List<CsMj> cards = new ArrayList<>(player.getHandMajiang());
            List<CsMj> huCards = CsMjTool.getTingMjs(cards, player.getGang(), player.getPeng(), player.getChi(),
                    player.getBuzhang(), true, OnlyDaHu == 1, quanqiurJiang);
            if (huCards == null || huCards.size() == 0) {
                return disMap;
            }
//            for (CsMj mj : huCards) {
//                ting.addMajiangIds(mj.getId());
//            }
            disMap.put("dismj", null);
            disMap.put("tingmj", huCards);


        }
        return disMap;
    }


    public static int getDuiNum(List<CsMj> handpais) {
        int num = 0;
        //对子数量  四张 3张都算一对
        HashMap<Integer, Integer> duizMap = new HashMap<>();
        List<Integer> hand = CsMjHelper.toMajiangVals(handpais);
        for (Integer mjval : hand) {
            int val = 0;
            if (null == duizMap.get(mjval)) {
                val = 0;
            } else {
                val = duizMap.get(mjval);
            }
            if (val == 0) {
                duizMap.put(mjval, ++val);
            } else {
                duizMap.put(mjval, ++val);
            }
        }

        for (Integer i : duizMap.values()) {
            if (i >= 2) {
                num++;
            }
        }
         //System.out.println("getDuiNum = " + num);
        return num;
    }

    public static HashMap<Integer, Integer> getValNumMap(List<CsMj> handpais) {
        HashMap<Integer, Integer> val_numMap = new HashMap<>();
        List<Integer> hand = CsMjHelper.toMajiangVals(handpais);
        for (Integer mjval : hand) {
            int val = 0;
            if (null == val_numMap.get(mjval)) {
                val = 0;
            } else {
                val = val_numMap.get(mjval);
            }
            if (val == 0) {
                val_numMap.put(mjval, ++val);
            } else {
                val_numMap.put(mjval, ++val);
            }
        }
         //System.out.println("getValNumMap:" + val_numMap);
        return val_numMap;
    }


    public static int getJiangNum(CsMjPlayer player) {
        //下面都是将碰，没吃过,且将数量大于7（包含碰下去的牌，杠补只算3个牌)
        List<Integer> handpais = new ArrayList<>(player.getHandPais());
        List<CsMj> hand = new ArrayList<>();
        for (Integer i : handpais) {
            hand.add(CsMj.getMajang(i));
        }
        List<CsMj> chi = new ArrayList<>(player.getChi());
        List<CsMj> peng = new ArrayList<>(player.getPeng());
        List<CsMj> gang = new ArrayList<>(player.getGang());
        List<CsMj> buzhang = new ArrayList<>(player.getBuzhang());
        if (null != chi || chi.size() > 0) {
            return 0;
        }
        int jnum = 0;
        if (isAllJiang(peng) && isAllJiang(gang) && isAllJiang(buzhang)) {
            jnum = getJiangNum(peng, false) + getJiangNum(gang, true) + getJiangNum(buzhang, true);
        }
        jnum = jnum + getJiangNum(hand, false);
         //System.out.println(player.getName() + " isJiang7Num =" + jnum);
        return jnum;
    }

    public static boolean isAllJiang(List<CsMj> pai) {
        if (null == pai || pai.size() == 0) {
            return true;
        }
        for (CsMj mj : pai) {
            if (mj.getVal() != 2 || mj.getVal() != 5 || mj.getVal() != 8) {
                return false;
            }
        }
        return true;
    }

    public static boolean isJiang(CsMj mj) {
       if (mj.getPai() == 2 || mj.getPai() == 5 || mj.getPai() == 8) {
                return true;
       }
        return false;
    }

    public static int getJiangNum(List<CsMj> pai, boolean isGangBu) {
        if (null == pai || pai.size() == 0) {
            return 0;
        }
        int num = 0;
        for (CsMj mj : pai) {
            if (mj.getVal() == 2 || mj.getVal() == 5 || mj.getVal() == 8) {
                num++;
            }
        }
        if (isGangBu) {
            num = num - pai.size() / 4;
        }
        return num;
    }


    public static int getTongNum(CsMjPlayer player) {
        List<Integer> handpais = new ArrayList<>(player.getHandPais());
        List<CsMj> hand = new ArrayList<>();
        for (Integer i : handpais) {
            hand.add(CsMj.getMajang(i));
        }
        List<CsMj> chi = new ArrayList<>(player.getChi());
        List<CsMj> peng = new ArrayList<>(player.getPeng());
        List<CsMj> gang = new ArrayList<>(player.getGang());
        List<CsMj> buzhang = new ArrayList<>(player.getBuzhang());
        int cph = getHuaSeNum(chi, tong) + getHuaSeNum(peng, tong) + getHuaSeNum(peng, tong) + getHuaSeNum(hand, tong);
        int g = getHuaSeGangBuNum(gang, tong) + getHuaSeGangBuNum(buzhang, tong);
         //System.out.println(player.getName() + " getTongNum =" + (cph + g));
        return cph + g;
    }

    public static int getTiaoNum(CsMjPlayer player) {
        List<Integer> handpais = new ArrayList<>(player.getHandPais());
        List<CsMj> hand = new ArrayList<>();
        for (Integer i : handpais) {
            hand.add(CsMj.getMajang(i));
        }
        List<CsMj> chi = new ArrayList<>(player.getChi());
        List<CsMj> peng = new ArrayList<>(player.getPeng());
        List<CsMj> gang = new ArrayList<>(player.getGang());
        List<CsMj> buzhang = new ArrayList<>(player.getBuzhang());
        int cph = getHuaSeNum(chi, tiao) + getHuaSeNum(peng, tiao) + getHuaSeNum(peng, tiao) + getHuaSeNum(hand, tiao);
        int g = getHuaSeGangBuNum(gang, tiao) + getHuaSeGangBuNum(buzhang, tiao);
         //System.out.println(player.getName() + " getTiaoNum =" + (cph + g));
        return cph + g;
    }

    public static int getWanNum(CsMjPlayer player) {
        List<Integer> handpais = new ArrayList<>(player.getHandPais());
        List<CsMj> hand = new ArrayList<>();
        for (Integer i : handpais) {
            hand.add(CsMj.getMajang(i));
        }
        List<CsMj> chi = new ArrayList<>(player.getChi());
        List<CsMj> peng = new ArrayList<>(player.getPeng());
        List<CsMj> gang = new ArrayList<>(player.getGang());
        List<CsMj> buzhang = new ArrayList<>(player.getBuzhang());
        int cph = getHuaSeNum(chi, wan) + getHuaSeNum(peng, wan) + getHuaSeNum(peng, wan) + getHuaSeNum(hand, wan);
        int g = getHuaSeGangBuNum(gang, wan) + getHuaSeGangBuNum(buzhang, wan);
         //System.out.println(player.getName() + " getWanNum =" + (cph + g));
        return cph + g;
    }

    public static int getHuaSeNum(List<CsMj> pais, int huase) {
        // 获取 花色数量
        if (null == pais || pais.size() == 0) {
            return 0;
        }
        int num = 0;
        for (CsMj mj : pais) {
            int val = mj.getVal();
            int Pai = val % 10;
            int Huase = val / 10;
            if (Huase == huase) {
                num++;
            }
        }
        return num;
    }

    public static int getHuaSeGangBuNum(List<CsMj> pais, int huase) {
        // 获取 花色数量 杠补只算3个牌
        if (null == pais || pais.size() == 0) {
            return 0;
        }
        int num = 0;
        for (CsMj mj : pais) {
            int val = mj.getVal();
            int Pai = val % 10;
            int Huase = val / 10;
            if (Huase == huase) {
                num++;
            }
        }
        if (num > 0) {
            int m = pais.size() / 4;
            num = num - m;
        }
        return num;
    }

    public static List<CsMj> getXiaoHuMjList(List<CsMj> handMajiang, int xiaohuType) {
        List<CsMj>   xiaohulist = new ArrayList<>();
        List<CsMj>   hand = new ArrayList<>(handMajiang);
            if(xiaohuType== CsMjAction.LIULIUSHUN || xiaohuType==CsMjAction.ZHONGTULIULIUSHUN  ){

            }else if(xiaohuType== CsMjAction.DASIXI || xiaohuType== CsMjAction.ZHONGTUSIXI ){

            }else if(xiaohuType== CsMjAction.JIEJIEGAO){

            }else if(xiaohuType== CsMjAction.JINGTONGYUNU){

            }else if(xiaohuType== CsMjAction.SANTONG){

            }else{
                xiaohulist = new ArrayList<>(hand);
            }
        return xiaohulist;
    }


    /**
     * 将paiList转成valueMap
     *
     * @param paiList
     * @return Map<Integer, List < Integer>>
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
     * @return Map<Integer, List < Integer>>
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

    public static List<CsMj> resortList(List<CsMj> bzlist) {
        // 先1 9  再3467  再258
        List<CsMj> head = new ArrayList<>();
        List<CsMj> mid = new ArrayList<>();
        List<CsMj> end = new ArrayList<>();
        for (CsMj mj : bzlist) {
            if (mj.getPai() == 1 || mj.getPai() == 9) {
                head.add(mj);
            }

            if (mj.getPai() == 3 || mj.getPai() == 4 || mj.getPai() == 6 || mj.getPai() == 7) {
                mid.add(mj);
            }

            if (mj.getPai() == 2 || mj.getPai() == 5 || mj.getPai() == 8) {
                end.add(mj);
            }
        }
        List<CsMj> re = new ArrayList<>();
        re.addAll(head);
        re.addAll(mid);
        re.addAll(end);
        return re;
    }

    public static void main(String[] args) {
        List<CsMj> ab = new ArrayList<>();
//        ab.add(CsMj.getMajang(5));// 5tiao
//        ab.add(CsMj.getMajang(32));// 7筒
//        ab.add(CsMj.getMajang(23));
//        ab.add(CsMj.getMajang(2));
//        ab.add(CsMj.getMajang(3));
//        ab.add(CsMj.getMajang(4));  ab.add(CsMj.getMajang(31));
//        ab.add(CsMj.getMajang(5)); //ab.add(CsMj.getMajang(32));//
//        ab.add(CsMj.getMajang(6));
//        ab.add(CsMj.getMajang(7));
//        ab.add(CsMj.getMajang(8));          ab.add(CsMj.getMajang(9));
//        ab.add(CsMj.getMajang(10));
        ab.add(CsMj.getMajang(5));
        ab.add(CsMj.getMajang(32));
        ab.add(CsMj.getMajang(8));
        ab.add(CsMj.getMajang(35));
         //System.out.println(CsMj.getMajang(5));
        List<CsMj> a =removeShunKan(ab);
    }
}
