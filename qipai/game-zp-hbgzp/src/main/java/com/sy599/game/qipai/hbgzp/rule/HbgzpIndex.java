package com.sy599.game.qipai.hbgzp.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author lc
 * 
 */
public class HbgzpIndex {
	private Map<Integer, List<Hbgzp>> phzValMap;
	private List<Integer> valList;

	public HbgzpIndex() {
		phzValMap = new HashMap<>();
		valList = new ArrayList<>();
	}

	public void addPaohz(int val, List<Hbgzp> majiangs) {
		if (this.phzValMap == null) {
			this.phzValMap = new HashMap<Integer, List<Hbgzp>>();
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

	public Map<Integer, List<Hbgzp>> getPaohzValMap() {
		return phzValMap;
	}

	public List<Hbgzp> getPaohzList() {
		List<Hbgzp> list = new ArrayList<>();
		for (List<Hbgzp> phzList : phzValMap.values()) {
			list.addAll(phzList);
		}
		return list;
	}

	public void setPhzValMap(Map<Integer, List<Hbgzp>> majiangValMap) {
		this.phzValMap = majiangValMap;
	}
}
