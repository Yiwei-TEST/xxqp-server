package com.sy599.game.qipai.tcpfmj.tool.huTool;

import com.sy599.game.qipai.tcpfmj.bean.MjCardDisType;
import com.sy599.game.qipai.tcpfmj.constant.TcpfMj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TingTool {
    /**
     *
     * @param ids
     * @param bossVal
     * @param replaceVal
     * @param cardTypes
     * @return
     */
    public static List<Integer> getTing(List<Integer> ids, int bossVal, int replaceVal,int buVal, List<MjCardDisType> cardTypes){
        List<Integer> tings=new ArrayList<>();
        if(ids.size()%3!=1)
            return tings;
        TcpfMj[] fullMj= TcpfMj.fullMj;
        for (int i = 0; i < fullMj.length; i++) {
            ids.add(fullMj[i].getId());
            if(HuTool.isTingHu(ids,bossVal,replaceVal,buVal,cardTypes,fullMj[i].getId())){
                tings.add(fullMj[i].getId());
            }
            ids.remove(ids.size()-1);
        }
        return tings;
    }

    public static Map<Integer, List<Integer>> getDaTing(List<Integer> ids, int bossVal, int replaceVal, List<MjCardDisType> cardTypes,int buVal){
        Map<Integer, List<Integer>> daTings=new HashMap<>();
        if(ids.size()%3!=2)
            return daTings;
        for (Integer id:ids) {
            int val = TcpfMj.getMajang(id).getVal();
            if(buVal==val||bossVal==val)
                continue;
            List<Integer> clone=new ArrayList<>(ids);
            clone.remove(id);
            List<Integer> ting = getTing(clone,bossVal,replaceVal,buVal,cardTypes);
            if(ting.size()>0)
                daTings.put(id,ting);
        }
        return daTings;
    }



}
