package com.sy599.game.util.mj.util;

import com.sy599.game.util.mj.serialize.NormolMj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 听牌和打听工具类，不过由于项目中前端取的是牌的id,该类返回的是val，需要外层自行转换
 */
public class MjTingTool {
    public static int[] allVals={11,12,13,14,15,16,17,18,19,21,22,23,24,25,26,27,28,29,31,32,33,34,35,36,37,38,39,201,211,221,301,311,321,331};
    public static int[] normalVals={11,12,13,14,15,16,17,18,19,21,22,23,24,25,26,27,28,29,31,32,33,34,35,36,37,38,39};


    public static List<Integer> getTing(List<Integer> ids, List<Integer> bossVal,boolean haveFeng,boolean need258){
        List<Integer> tings=new ArrayList<>();
        if(ids.size()%3!=1)
            return tings;
        NormolMj[] fullMj= haveFeng?NormolMj.fullMjAndFeng:NormolMj.fullMj;
        for (int i = 0; i < fullMj.length; i++) {
            ids.add(fullMj[i].getId());
            if(MjHuTool.isHuAnd7dui(toMajiangValsById(ids),bossVal,need258)){
                tings.add(fullMj[i].getId());
            }
            ids.remove(ids.size()-1);
        }
        return tings;
    }

    public static Map<Integer, List<Integer>> getDaTing(List<Integer> ids, List<Integer> bossVal,boolean haveFeng,boolean need258){
        Map<Integer, List<Integer>> daTings=new HashMap<>();
        if(ids.size()%3!=2)
            return daTings;
        for (Integer id:ids) {
            List<Integer> clone=new ArrayList<>(ids);
            clone.remove(id);
            List<Integer> ting = getTing(clone,bossVal,haveFeng,need258);
            if(ting.size()>0)
                daTings.put(id,ting);
        }
        return daTings;
    }


    public static List<Integer> toMajiangValsById(List<Integer> ids) {
        List<Integer> majiangIds = new ArrayList<>();
        if (ids == null) {
            return majiangIds;
        }
        for (Integer id : ids) {
            majiangIds.add(NormolMj.getMajang(id).getVal());
        }
        return majiangIds;
    }


}
