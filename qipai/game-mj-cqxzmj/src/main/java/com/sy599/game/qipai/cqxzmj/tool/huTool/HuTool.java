package com.sy599.game.qipai.cqxzmj.tool.huTool;

import com.sy599.game.qipai.cqxzmj.constant.CqxzMj;
import com.sy599.game.qipai.cqxzmj.tool.MjHelper;
import com.sy599.game.util.mj.util.MjHuTool;

import java.util.*;

/**
 * 具体方法内容已经移到common包中，该类只做默认参数传参调用
 */
public class HuTool {

    public static boolean isHu(List<Integer> ids,int dingQue){
        List<Integer> vals = MjHelper.toMajiangValsById(ids);
        if(dingQue!=0)
            for (Integer val:vals) {
                if(val/10==dingQue)
                    return false;
            }
        return isHu(vals);
    }

    public static boolean isHu(List<Integer> vals){
        return MjHuTool.isHuAnd7dui(vals, new ArrayList<>(), false);
    }
}
