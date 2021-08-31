package com.sy599.game.qipai.yzphz.util;

import java.util.ArrayList;
import java.util.List;

public class YzPhzCardMsg {
    private final List<Integer> ids;
    private final List<Integer> vals;
    private final String valStr;

    public YzPhzCardMsg(final List<Integer> ids){
        this.ids=ids;
        YzPhzCardUtils.sort(this.ids);

        vals=new ArrayList<>(this.ids.size());
        StringBuilder valBuilder=new StringBuilder(4*this.ids.size());
        for (Integer id:this.ids){
            int id0=id.intValue();
            int val= YzPhzCardUtils.loadCardVal(id0);
            vals.add(val);
            valBuilder.append(",").append(val);
        }
        valStr=valBuilder.toString();
    }

    public List<Integer> getIds() {
        return ids;
    }

    public List<Integer> getVals() {
        return vals;
    }

    public String getValStr() {
        return valStr;
    }

    @Override
    public int hashCode() {
        return valStr.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj==this){
            return true;
        }else if (obj instanceof YzPhzCardMsg){
            return valStr.equals(((YzPhzCardMsg)obj).valStr);
        }else{
            return false;
        }
    }
}
