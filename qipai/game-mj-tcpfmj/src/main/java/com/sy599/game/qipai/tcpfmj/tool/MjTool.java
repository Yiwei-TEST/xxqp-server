package com.sy599.game.qipai.tcpfmj.tool;

import java.util.*;

import com.sy599.game.qipai.tcpfmj.constant.TcpfMj;

/**
 * @author lc
 */
public class MjTool {
    public static synchronized List<List<TcpfMj>> fapai(List<Integer> copy, int playerCount) {
        List<List<TcpfMj>> list = new ArrayList<>();
        Collections.shuffle(copy);
        List<TcpfMj> allMjs = new ArrayList<>();
        for (int id : copy) {
            allMjs.add(TcpfMj.getMajang(id));
        }
        for (int i = 0; i < playerCount; i++) {
            if (i == 0) {
                list.add(new ArrayList<>(allMjs.subList(0, 14)));
            } else {
                list.add(new ArrayList<>(allMjs.subList(14 + (i - 1) * 13, 14 + (i - 1) * 13 + 13)));
            }
            if (i == playerCount - 1) {
                list.add(new ArrayList<>(allMjs.subList(14 + (i) * 13, allMjs.size())));
            }
        }
        return list;
    }

    public static synchronized List<List<TcpfMj>> fapai(List<Integer> copy, int playerCount, List<List<Integer>> t) {
        if(t.size()<playerCount)
            return null;
        List<List<TcpfMj>> list = new ArrayList<>();
        for (int i = 0; i < t.size(); i++) {
            List<Integer> zp = t.get(i);
            //跳过调的王牌
            if(i==0&&zp.size()==1)
                continue;
            list.add(find(copy, zp));
        }
        Collections.shuffle(copy);
        boolean buShouPai=false;
        if(buShouPai){
            //补张
            for (int i = 0; i < playerCount; i++) {
                List<TcpfMj> ahmjs = list.get(i);
                if(i==0){
                    for (int j = ahmjs.size(); j < 14; j++) {
                        ahmjs.add(TcpfMj.getMajang(copy.get(j)));
                        copy.remove(j);
                    }
                }else {
                    for (int j = ahmjs.size(); j < 13; j++) {
                        ahmjs.add(TcpfMj.getMajang(copy.get(j)));
                        copy.remove(j);
                    }
                }
            }
        }
        boolean buMoPai=false;
        if(list.size()>playerCount&&buMoPai){
            list.get(playerCount).addAll(MjHelper.toMajiang(copy));
        }else if(list.size()==playerCount){
            List<TcpfMj> l=new ArrayList<>();
            for (int i = 0; i < copy.size(); i++) {
                l.add(TcpfMj.getMajang(copy.get(i)));
            }
            list.add(l);
        }

        return list;
    }


    // 拆牌
    public static boolean chaipai(MjHuLack lack, List<TcpfMj> hasPais, boolean isNeedJiang258) {
        if (hasPais.isEmpty()) {
            return true;

        }
        boolean hu = chaishun(lack, hasPais, isNeedJiang258);
        if (hu)
            return true;
        return false;
    }

    public static void sortMin(List<TcpfMj> hasPais) {
        Collections.sort(hasPais, new Comparator<TcpfMj>() {

            @Override
            public int compare(TcpfMj o1, TcpfMj o2) {
                if (o1.getPai() < o2.getPai()) {
                    return -1;
                }
                if (o1.getPai() > o2.getPai()) {
                    return 1;
                }
                return 0;
            }

        });
    }

    /**
     * 拆顺
     *
     * @param hasPais
     * @return
     */
    public static boolean chaishun(MjHuLack lack, List<TcpfMj> hasPais, boolean needJiang258) {
        if (hasPais.isEmpty()) {
            return true;
        }
        sortMin(hasPais);
        TcpfMj minMajiang = hasPais.get(0);
        int minVal = minMajiang.getVal();
        List<TcpfMj> minList = MjQipaiTool.getVal(hasPais, minVal);
        if (minList.size() >= 3) {
            // 先拆坎子
            removeAllPai(hasPais, minList.subList(0, 3));
            //hasPais.removeAll(minList.subList(0, 3));
            return chaipai(lack, hasPais, needJiang258);
        }

        // 做顺子
        int pai1 = minVal;
        int pai2 = 0;
        int pai3 = 0;
        if (pai1 % 10 == 9) {
            pai1 = pai1 - 2;

        } else if (pai1 % 10 == 8) {
            pai1 = pai1 - 1;
        }
        pai2 = pai1 + 1;
        pai3 = pai2 + 1;

        List<Integer> lackList = new ArrayList<>();
        List<TcpfMj> num1 = MjQipaiTool.getVal(hasPais, pai1);
        List<TcpfMj> num2 = MjQipaiTool.getVal(hasPais, pai2);
        List<TcpfMj> num3 = MjQipaiTool.getVal(hasPais, pai3);

        // 找到一句话的麻将
        List<TcpfMj> hasMajiangList = new ArrayList<>();
        if (!num1.isEmpty()) {
            hasMajiangList.add(num1.get(0));
        }
        if (!num2.isEmpty()) {
            hasMajiangList.add(num2.get(0));
        }
        if (!num3.isEmpty()) {
            hasMajiangList.add(num3.get(0));
        }

        // 一句话缺少的麻将
        if (num1.isEmpty()) {
            lackList.add(pai1);
        }
        if (num2.isEmpty()) {
            lackList.add(pai2);
        }
        if (num3.isEmpty()) {
            lackList.add(pai3);
        }

        int lackNum = lackList.size();
        if (lackNum > 0) {
            if (lack.getHongzhongNum() <= 0) {
                return false;
            }

            // 做成一句话缺少2张以上的，没有将优先做将
            if (lackNum >= 2) {
                // 补坎子
                List<TcpfMj> count = MjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() >= 3) {
                    removeAllPai(hasPais, count);
                    //hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);

                } else if (count.size() == 2) {
                    if (!lack.isHasJiang() && isCanAsJiang(count.get(0), needJiang258)) {
                        // 没有将做将
                        lack.setHasJiang(true);
                        removeAllPai(hasPais, count);
                        //hasPais.removeAll(count);
                        return chaipai(lack, hasPais, needJiang258);
                    }

                    // 拿一张红中补坎子
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    removeAllPai(hasPais, count);
                    //hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);
                }

                // 做将
                if (!lack.isHasJiang() && isCanAsJiang(count.get(0), needJiang258) && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.setHasJiang(true);
                    removeAllPai(hasPais, count);
                    //hasPais.removeAll(count);
                    lack.addLack(count.get(0).getVal());
                    return chaipai(lack, hasPais, needJiang258);
                }
            } else if (lackNum == 1) {
                // 做将
                if (!lack.isHasJiang() && isCanAsJiang(minMajiang, needJiang258) && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.setHasJiang(true);
                    hasPais.remove(minMajiang);
                    lack.addLack(minMajiang.getVal());
                    return chaipai(lack, hasPais, needJiang258);
                }

                List<TcpfMj> count = MjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                if (count.size() == 2 && lack.getHongzhongNum() > 0) {
                    lack.changeHongzhong(-1);
                    lack.addLack(count.get(0).getVal());
                    removeAllPai(hasPais, count);
                    //hasPais.removeAll(count);
                    return chaipai(lack, hasPais, needJiang258);
                }
            }

            // 如果有红中则补上
            if (lack.getHongzhongNum() >= lackNum) {
                lack.changeHongzhong(-lackNum);
                removeAllPai(hasPais, hasMajiangList);
                //hasPais.removeAll(hasMajiangList);
                lack.addAllLack(lackList);

            } else {
                return false;
            }
        } else {
            // 可以一句话
            if (lack.getHongzhongNum() > 0) {
                List<TcpfMj> count1 = MjQipaiTool.getVal(hasPais, hasMajiangList.get(0).getVal());
                List<TcpfMj> count2 = MjQipaiTool.getVal(hasPais, hasMajiangList.get(1).getVal());
                List<TcpfMj> count3 = MjQipaiTool.getVal(hasPais, hasMajiangList.get(2).getVal());
                if (count1.size() >= 2 && (count2.size() == 1 || count3.size() == 1)) {
                    List<TcpfMj> copy = new ArrayList<>(hasPais);
                    removeAllPai(copy, count1);
                    //copy.removeAll(count1);
                    MjHuLack copyLack = lack.copy();
                    copyLack.changeHongzhong(-1);

                    copyLack.addLack(hasMajiangList.get(0).getVal());
                    if (chaipai(copyLack, copy, needJiang258)) {
                        return true;
                    }
                }
            }
            removeAllPai(hasPais, hasMajiangList);
            //hasPais.removeAll(hasMajiangList);
        }
        return chaipai(lack, hasPais, needJiang258);
    }

    public static void removeAllPai(List<TcpfMj> hasPais, List<TcpfMj> remPai) {
        for (TcpfMj majiang : remPai) {
            hasPais.remove(majiang);
        }
    }

    public static boolean isCanAsJiang(TcpfMj majiang, boolean isNeed258) {
        if (isNeed258) {
            if (majiang.getPai() == 2 || majiang.getPai() == 5 || majiang.getPai() == 8) {
                return true;
            }
            return false;
        } else {
            return true;
        }

    }

    public static List<TcpfMj> checkChi(List<TcpfMj> majiangs, TcpfMj dismajiang) {
        return checkChi(majiangs, dismajiang, null);
    }

    /**
     * 是否能吃
     *
     * @param majiangs
     * @param dismajiang
     * @return
     */
    public static List<TcpfMj> checkChi(List<TcpfMj> majiangs, TcpfMj dismajiang, List<Integer> wangValList) {
        int disMajiangVal = dismajiang.getVal();
        List<Integer> chi1 = new ArrayList<>(Arrays.asList(disMajiangVal - 2, disMajiangVal - 1));
        List<Integer> chi2 = new ArrayList<>(Arrays.asList(disMajiangVal - 1, disMajiangVal + 1));
        List<Integer> chi3 = new ArrayList<>(Arrays.asList(disMajiangVal + 1, disMajiangVal + 2));

        List<Integer> majiangIds = MjHelper.toMajiangVals(majiangs);
        if (wangValList == null || !checkWang(chi1, wangValList)) {
            if (majiangIds.containsAll(chi1)) {
                return findMajiangByVals(majiangs, chi1);
            }
        }
        if (wangValList == null || !checkWang(chi2, wangValList)) {
            if (majiangIds.containsAll(chi2)) {
                return findMajiangByVals(majiangs, chi2);
            }
        }
        if (wangValList == null || !checkWang(chi3, wangValList)) {
            if (majiangIds.containsAll(chi3)) {
                return findMajiangByVals(majiangs, chi3);
            }
        }
        return new ArrayList<TcpfMj>();
    }

    public static List<TcpfMj> findMajiangByVals(List<TcpfMj> majiangs, List<Integer> vals) {
        List<TcpfMj> result = new ArrayList<>();
        for (int val : vals) {
            for (TcpfMj majiang : majiangs) {
                if (majiang.getVal() == val) {
                    result.add(majiang);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 去掉红中
     *
     * @param copy
     * @return
     */
    public static List<TcpfMj> dropHongzhong(List<TcpfMj> copy) {
        List<TcpfMj> hongzhong = new ArrayList<>();
        Iterator<TcpfMj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            TcpfMj majiang = iterator.next();
            if (majiang.getVal() > 200) {
                hongzhong.add(majiang);
                iterator.remove();
            }
        }
        return hongzhong;
    }

    public static boolean checkWang(Object majiangs, List<Integer> wangValList) {
        if (majiangs instanceof List) {
            List list = (List) majiangs;
            for (Object majiang : list) {
                int val = 0;
                if (majiang instanceof TcpfMj) {
                    val = ((TcpfMj) majiang).getVal();
                } else {
                    val = (int) majiang;
                }
                if (wangValList.contains(val)) {
                    return true;
                }
            }
        }

        return false;

    }



    private static List<TcpfMj> find(List<Integer> copy, List<Integer> valList) {
        List<TcpfMj> pai = new ArrayList<>();
        if (!valList.isEmpty()) {
            for (int zpId : valList) {
                Iterator<Integer> iterator = copy.iterator();
                while (iterator.hasNext()) {
                    int card = iterator.next();
                    TcpfMj mj = TcpfMj.getMajang(card);
                    if (mj.getVal() == zpId) {
                        pai.add(mj);
                        iterator.remove();
                        break;
                    }
                }
            }
        }
        return pai;
    }

}
