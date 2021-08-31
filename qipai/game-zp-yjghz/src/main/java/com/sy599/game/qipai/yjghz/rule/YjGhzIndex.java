package com.sy599.game.qipai.yjghz.rule;

import com.sy599.game.qipai.yjghz.constant.YjGhzCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lc
 */
public class YjGhzIndex {
    private Map<Integer, List<YjGhzCard>> phzValMap;
    private List<Integer> valList;

    public YjGhzIndex() {
        phzValMap = new HashMap<>();
        valList = new ArrayList<>();
    }

    public void addPaohz(int val, List<YjGhzCard> majiangs) {
        if (this.phzValMap == null) {
            this.phzValMap = new HashMap<Integer, List<YjGhzCard>>();
        }
        this.phzValMap.put(val, majiangs);
    }

    /**
     * 符合的麻将值list
     *
     * @return
     */
    public List<Integer> getValList() {
        return valList;
    }

//	public List<PaohzCard> getPhzs() {
//		List<PaohzCard> phzs = new ArrayList<>();
//		if (phzValMap != null) {
//			for (Entry<Integer, List<PaohzCard>> entry : phzValMap.entrySet()) {
//				phzs.addAll(entry.getValue());
//			}
//		}
//		return phzs;
//	}

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

    public Map<Integer, List<YjGhzCard>> getPaohzValMap() {
        return phzValMap;
    }

    public List<YjGhzCard> getPaohzList() {
        List<YjGhzCard> list = new ArrayList<>();
        for (List<YjGhzCard> phzList : phzValMap.values()) {
            list.addAll(phzList);
        }
        return list;
    }

    public void setPhzValMap(Map<Integer, List<YjGhzCard>> majiangValMap) {
        this.phzValMap = majiangValMap;
    }
}
