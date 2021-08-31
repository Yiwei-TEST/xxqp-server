package com.sy599.game.qipai.tcdpmj.tool.huTool;

import com.sy599.game.qipai.tcdpmj.bean.MjCardDisType;
import com.sy599.game.qipai.tcdpmj.constant.TcdpMj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TingTool {
    /**
     *
     * @param ids
     * @return
     */
    public static List<Integer> getTing(List<Integer> ids,int buVal){
        List<Integer> tings=new ArrayList<>();
        if(ids.size()%3!=1)
            return tings;
        TcdpMj[] fullMj= TcdpMj.fullMj;
        for (int i = 0; i < fullMj.length; i++) {
            if(fullMj[i].getVal()==buVal)
                break;
            ids.add(fullMj[i].getId());
            if(HuTool.isHuNew(ids,buVal)){
                tings.add(fullMj[i].getId());
            }
            ids.remove(ids.size()-1);
        }
        return tings;
    }

    public static Map<Integer, List<Integer>> getDaTing(List<Integer> ids,int buVal){
        Map<Integer, List<Integer>> daTings=new HashMap<>();
        if(ids.size()%3!=2)
            return daTings;
        for (Integer id:ids) {
            List<Integer> clone=new ArrayList<>(ids);
            clone.remove(id);
            List<Integer> ting = getTing(clone,buVal);
            if(ting.size()>0)
                daTings.put(id,ting);
        }
        return daTings;
    }



}
