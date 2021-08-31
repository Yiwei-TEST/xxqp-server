package com.sy599.game.qipai.tcpfmj.tool.huTool;

import java.util.ArrayList;
import java.util.List;

public class ReplacelValType implements Cloneable{
    private List<Integer> original;
    private List<Integer> replace;
    private int useBoss=0;
    private boolean isJiang;
    ReplacelValType(int[] zus,List<Integer> original,int useBoss){
        this.original=original;
        this.useBoss=useBoss;
        List<Integer> list=new ArrayList<>();
        for (int i = 0; i < zus.length; i++) {
            list.add(zus[i]);
        }
        this.replace= list;
        if(zus.length==2){
            isJiang=true;
        }
    }
    ReplacelValType(List<Integer> original,int useBoss){
        this.original=original;
        this.useBoss=useBoss;
    }

    public List<Integer> getOriginal() {
        return original;
    }

    public void setOriginal(List<Integer> original) {
        this.original = original;
    }

    public List<Integer> getReplace() {
        return replace;
    }

    public void setReplace(List<Integer> replace) {
        this.replace = replace;
    }

    public int getUseBoss() {
        return useBoss;
    }

    public void setUseBoss(int useBoss) {
        this.useBoss = useBoss;
    }

    public boolean isJiang(){
        return isJiang;
    }

    public ReplacelValType clone() {
        ReplacelValType o = null;
        try {
            o = (ReplacelValType) super.clone();
            if(o.getOriginal()!=null){
                o.setOriginal(new ArrayList<>(original));
                o.setReplace(new ArrayList<>(replace));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return o;
    }
}
