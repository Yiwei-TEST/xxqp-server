package com.sy599.game.qipai.qzmj.tool.huTool;


import com.sy599.game.qipai.qzmj.bean.MjCardDisType;
import com.sy599.game.qipai.qzmj.constant.QzMj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TingTool {
    /**
     *
     * @param ids
     * @param chunWang
     * @param disTypes
     * @param baoTing   部分牌型不检测起手报听
     * @return
     */
    public static List<Integer> getTing(List<Integer> ids, List<Integer> chunWang,  List<MjCardDisType> disTypes, boolean baoTing){
        List<Integer> tings=new ArrayList<>();
        if(ids.size()%3!=1)
            return tings;
        QzMj[] fullMj= QzMj.fullMj;
        for (int i = 0; i < fullMj.length; i++) {
            ids.add(fullMj[i].getId());
            if(HuTool.isHu(ids,chunWang,disTypes,baoTing,true)){
                tings.add(fullMj[i].getId());
            }
            ids.remove(ids.size()-1);
        }
        return tings;
    }

    public static Map<Integer, List<Integer>> getDaTing(List<Integer> ids,List<Integer> chunWang,List<MjCardDisType> disTypes){
        Map<Integer, List<Integer>> daTings=new HashMap<>();
        if(ids.size()%3!=2)
            return daTings;
        for (Integer id:ids) {
            if(chunWang.contains(QzMj.getMajang(id).getVal()))
                continue;
            List<Integer> clone=new ArrayList<>(ids);
            clone.remove(id);
            List<Integer> ting = getTing(clone,chunWang,disTypes,false);
            if(ting.size()>0)
                daTings.put(id,ting);
        }
        return daTings;
    }



}
