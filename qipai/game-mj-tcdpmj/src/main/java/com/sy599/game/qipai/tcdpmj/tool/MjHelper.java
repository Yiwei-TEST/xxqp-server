package com.sy599.game.qipai.tcdpmj.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sy599.game.qipai.tcdpmj.constant.TcdpMj;

public class MjHelper {

    /**
     * 麻将val的个数
     *
     * @param majiangs
     * @param majiangVal
     * @return
     */
    public static int getMajiangCount(List<TcdpMj> majiangs, int majiangVal) {
        int count = 0;
        for (TcdpMj majiang : majiangs) {
            if (majiang.getVal() == majiangVal) {
                count++;
            }
        }
        return count;
    }

    /**
     * 麻将val的List
     *
     * @param majiangs
     * @param majiangVal
     * @return
     */
    public static List<TcdpMj> getMajiangList(List<TcdpMj> majiangs, int majiangVal) {
        List<TcdpMj> list = new ArrayList<>();
        for (TcdpMj majiang : majiangs) {
            if (majiang.getVal() == majiangVal) {
                list.add(majiang);
            }
        }
        return list;
    }

    public static List<TcdpMj> getGangMajiangList(List<TcdpMj> majiangs, int majiangVal) {
        int num=0;
        List<TcdpMj> list = new ArrayList<>();
        for (TcdpMj majiang : majiangs) {
            if (majiang.getVal() == majiangVal&&num<4) {
                list.add(majiang);
                num++;
            }
        }
        return list;
    }


    /**
     * 麻将转化为majiangIds
     *
     * @param majiangs
     * @return
     */
    public static List<Integer> toMajiangIds(List<TcdpMj> majiangs) {
        List<Integer> majiangIds = new ArrayList<>();
        if (majiangs == null) {
            return majiangIds;
        }
        for (TcdpMj majiang : majiangs) {
            majiangIds.add(majiang.getId());
        }
        return majiangIds;
    }

    /**
     * 麻将转化为majiangIds
     *
     * @param majiangs
     * @return
     */
    public static String toMajiangStrs(List<TcdpMj> majiangs) {
        StringBuffer sb = new StringBuffer();
        if (majiangs == null) {
            return sb.toString();
        }
        for (TcdpMj majiang : majiangs) {
            sb.append(majiang.getId()).append(",");

        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * 麻将转化为majiangIds
     *
     * @param majiangs
     * @return
     */
    public static List<Integer> toMajiangVals(List<TcdpMj> majiangs) {
        List<Integer> majiangIds = new ArrayList<>();
        if (majiangs == null) {
            return majiangIds;
        }
        for (TcdpMj majiang : majiangs) {
            majiangIds.add(majiang.getVal());
        }
        return majiangIds;
    }


    /**
     * 麻将转化为Map<val,valNum>
     *
     * @param majiangs
     * @return
     */
    public static Map<Integer, Integer> toMajiangValMap(List<TcdpMj> majiangs) {
        Map<Integer, Integer> majiangIds = new HashMap<>();
        if (majiangs == null) {
            return majiangIds;
        }
        for (TcdpMj majiang : majiangs) {
            if (majiangIds.containsKey(majiang.getVal())) {
                majiangIds.put(majiang.getVal(), majiangIds.get(majiang.getVal()) + 1);
            } else {
                majiangIds.put(majiang.getVal(), 1);
            }
        }
        return majiangIds;
    }

    /**
     * 麻将Id转化为麻将
     *
     * @param majiangIds
     * @return
     */
    public static List<TcdpMj> toMajiang(List<Integer> majiangIds) {
        if (majiangIds == null) {
            return new ArrayList<>();
        }
        List<TcdpMj> majiangs = new ArrayList<>();
        for (int majiangId : majiangIds) {
            if (majiangId == 0) {
                continue;
            }
            majiangs.add(TcdpMj.getMajang(majiangId));
        }
        return majiangs;
    }




    public static List<TcdpMj> find(List<Integer> copy, List<Integer> valList) {
        List<TcdpMj> pai = new ArrayList<>();
        if (!valList.isEmpty()) {
            for (int zpId : valList) {
                Iterator<Integer> iterator = copy.iterator();
                while (iterator.hasNext()) {
                    int card = iterator.next();
                    TcdpMj majiang = TcdpMj.getMajang(card);
                    if (majiang.getVal() == zpId) {
                        pai.add(majiang);
                        iterator.remove();
                        break;
                    }
                }
            }
        }
        return pai;
    }

    public static List<Integer> find(List<TcdpMj> copy, int  val) {
        List<Integer> pai = new ArrayList<>();
        Iterator<TcdpMj> iterator = copy.iterator();
        while (iterator.hasNext()) {
            TcdpMj majiang = iterator.next();
            if (majiang.getVal() == val) {
                pai.add(majiang.getId());
                break;
            }
        }
        return pai;
    }


    public static Map<Integer,List<Integer>> getGangList(List<TcdpMj> mjs){
        Map<Integer,List<Integer>> map=new HashMap<>();
        for (TcdpMj mj:mjs) {
            Integer val = mj.getVal();
            List<Integer> list = map.get(val);
            if(list==null){
                list=new ArrayList<>();
                map.put(val,list);
            }
            list.add(mj.getId());
        }

        Map<Integer,List<Integer>> rMap=new HashMap<>();
        for (Map.Entry<Integer,List<Integer>> entry:map.entrySet()){
            if(entry.getValue().size()==4)
                rMap.put(entry.getKey(),entry.getValue());
        }
        return rMap;
    }

    public static Map<Integer,List<Integer>> getGangListById(List<Integer> ids){
        Map<Integer,List<Integer>> map=new HashMap<>();
        for (Integer id:ids) {
            Integer val = TcdpMj.getMajang(id).getVal();
            List<Integer> list = map.get(val);
            if(list==null){
                list=new ArrayList<>();
                map.put(val,list);
            }
            list.add(id);
        }

        Map<Integer,List<Integer>> rMap=new HashMap<>();
        for (Map.Entry<Integer,List<Integer>> entry:map.entrySet()){
            if(entry.getValue().size()>=4)
                rMap.put(entry.getKey(),entry.getValue());
        }
        return rMap;
    }

    public static List<Integer> dropVal(List<TcdpMj> mjs, int val){
        List<Integer> list=new ArrayList<>();
        for (TcdpMj mj:mjs) {
            if(mj.getVal()!=val)
                list.add(mj.getId());
        }
        return list;
    }

    public static List<Integer> dropValById(List<Integer> ids,int val){
        int dropNum=0;
        List<Integer> list=new ArrayList<>();
        for (Integer id:ids) {
            if(TcdpMj.getMajang(id).getVal()!=val||dropNum>=4){
                list.add(id);
            }else {
                dropNum++;
            }
        }
        return list;
    }
}
