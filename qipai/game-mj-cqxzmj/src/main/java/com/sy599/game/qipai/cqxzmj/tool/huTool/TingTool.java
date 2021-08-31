package com.sy599.game.qipai.cqxzmj.tool.huTool;

import com.sy599.game.qipai.cqxzmj.bean.CqxzMjPlayer;
import com.sy599.game.qipai.cqxzmj.constant.CqxzMj;
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
    public static List<Integer> getTing(List<Integer> ids, CqxzMjPlayer player){
        List<Integer> tings=new ArrayList<>();
        if(ids.size()%3!=1)
            return tings;
        CqxzMj[] fullMj= CqxzMj.fullMj;
        for (int i = 0; i < fullMj.length; i++) {
            ids.add(fullMj[i].getId());
            if(HuTool.isHu(ids,player.getDingQue())){
                tings.add(fullMj[i].getId());
            }
            ids.remove(ids.size()-1);
        }
        return tings;
    }

    public static Map<Integer, List<Integer>> getDaTing(List<Integer> ids, CqxzMjPlayer player) {
        Map<Integer, List<Integer>> daTings=new HashMap<>();
        if(ids.size()%3!=2)
            return daTings;
        for (Integer id:ids) {
            List<Integer> clone=new ArrayList<>(ids);
            clone.remove(id);
            List<Integer> ting = getTing(clone,player);
            if(ting.size()>0)
                daTings.put(id,ting);
        }
        return daTings;
    }


}
