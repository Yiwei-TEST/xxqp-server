package com.sy599.game.qipai.nanxmj.tool.huTool;

import com.sy599.game.qipai.nanxmj.bean.NanxMjPlayer;
import com.sy599.game.qipai.nanxmj.rule.NxMjingTang;
import com.sy599.game.qipai.nanxmj.tool.MjHelper;
import com.sy599.game.util.mj.util.MjHuTool;

import java.util.ArrayList;
import java.util.List;

/**
 * 具体方法内容已经移到common包中，该类只做默认参数传参调用
 */
public class HuTool {

    public static boolean isHu(List<Integer> ids, boolean seleMo, NanxMjPlayer player){
        List<Integer> vals = MjHelper.toMajiangValsById(ids);
        boolean isHu = MjHuTool.isHuAnd7dui(vals, new ArrayList<>(), false);
        //除只能胡将将胡外的大胡可点炮
        if(!seleMo){
            List<Integer> daHuType = NxMjingTang.getDaHuType(ids, player);
            return isHu&&daHuType.size()>0;
        }else {
            boolean isJJH= NxMjingTang.isMenQing(player.getCardTypes())&&NxMjingTang.isJiangJiangHu(player.getCardTypes(),ids);
            return isJJH||isHu;
        }
    }

    public static boolean isQGH(List<Integer> ids, NanxMjPlayer player,boolean xiaoHKQ){
        List<Integer> vals = MjHelper.toMajiangValsById(ids);
        boolean isHu = MjHuTool.isHuAnd7dui(vals, new ArrayList<>(), false);
        if(xiaoHKQ){
            return isHu;
        }else {
            List<Integer> daHuType = NxMjingTang.getDaHuType(ids, player);
            return isHu&&daHuType.size()>0;
        }
    }
}
