package com.sy599.game.qipai.xplp.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class XplpIndex {
    private Map<Integer, List<XpLp>> majiangValMap;
    private List<Integer> valList;

    public XplpIndex() {
        majiangValMap = new HashMap<>();
        valList = new ArrayList<>();
    }

    public void addMajiang(int val, List<XpLp> majiangs) {
        if (this.majiangValMap == null) {
            this.majiangValMap = new HashMap<Integer, List<XpLp>>();
        }
        this.majiangValMap.put(val, majiangs);
    }

    /**
     * 符合的麻将值list
     *
     * @return
     */
    public List<Integer> getValList() {
        return valList;
    }

    public List<XpLp> getMajiangs() {
        List<XpLp> majiangs = new ArrayList<>();
        if (majiangValMap != null) {
            for (Entry<Integer, List<XpLp>> entry : majiangValMap.entrySet()) {
                majiangs.addAll(entry.getValue());
            }
        }
        return majiangs;
    }

    /**
     * 符合的麻将值的长度
     *
     * @return
     */
    public int getLength() {
        return valList.size();
    }

    public void setValList(List<Integer> valList) {
        this.valList = valList;
    }

    public void addVal(int val) {
        if (this.valList == null) {
            this.valList = new ArrayList<Integer>();
        }
        this.valList.add(val);
    }

    public Map<Integer, List<XpLp>> getMajiangValMap() {
        return majiangValMap;
    }

    public void setMajiangValMap(Map<Integer, List<XpLp>> majiangValMap) {
        this.majiangValMap = majiangValMap;
    }
}
