package com.sy599.game.qipai.nanxmj.tool.huTool;

import java.util.ArrayList;
import java.util.List;

public class ReplaceLack implements Cloneable{

    // 胡牌时的牌组
    private List<ReplacelValType> replace=new ArrayList<>();
    private List<Integer> daHu=new ArrayList<>();
    public ReplaceLack() {
    }

    public List<ReplacelValType> getReplace() {
        return replace;
    }

    public void setReplace(List<ReplacelValType> replace) {
        this.replace = replace;
    }

    public ReplaceLack clone() {
        ReplaceLack o = null;
        try {
            o = (ReplaceLack) super.clone();
            if (replace != null) {
                List<ReplacelValType> l1=new ArrayList<>(replace.size());
                for (ReplacelValType type:replace) {
                    l1.add(type.clone());
                }
                o.setReplace(l1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return o;
    }


    public void addReplaceType(ReplacelValType rt){
        replace.add(rt);
    }


    public List<Integer> getDaHu() {
        return daHu;
    }

    private boolean isPPH(){
        for (ReplacelValType rt:replace) {
            if(!rt.isJiang()){
                List<Integer> replace = rt.getReplace();
                if(replace.get(0)!=replace.get(1)||replace.get(0)!=replace.get(2)||replace.get(1)!=replace.get(2))
                    return false;
            }
        }
        return true;
    }

    private boolean isJJH(){
        for (ReplacelValType rt:replace) {
            List<Integer> replace = rt.getReplace();
            for (Integer val:replace) {
                int yu=val%10;
                if(yu!=2&&yu!=5&&yu!=8)
                    return false;
            }
        }
        return true;
    }

    private boolean isQYS(){
        int x=-1;
        for (ReplacelValType rt:replace) {
            List<Integer> replace = rt.getReplace();
            for (Integer val:replace) {
                int chu=val/10;
                if(x==-1){
                    x=chu;
                }else {
                    if(x!=chu)
                        return false;
                }
            }
        }
        return true;
    }

    private boolean isQXD(){
        int count=0;
        int[] yus = new int[30];
        for (ReplacelValType rt:replace) {
            List<Integer> replace = rt.getReplace();
            for (Integer val:replace) {
                if(val==1000){
                    yus[0]++;
                }else {
                    yus[val-10]++;
                }
                count++;
            }
        }
        if(count!=14)
            return false;
        int no2=0;
        for (int i = 1; i < yus.length; i++) {
            if(yus[i]%2!=0){
                no2++;
            }
        }
        if(no2>yus[0])
            return false;
        return true;
    }
}
