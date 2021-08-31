package com.sy599.game.qipai.daozmj.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DaozMjIndex {
	private Map<Integer, List<DzMj>> majiangValMap;
	private List<Integer> valList;

	public DaozMjIndex() {
		majiangValMap = new HashMap<>();
		valList = new ArrayList<>();
	}

	public void addMajiang(int val, List<DzMj> majiangs) {
		if (this.majiangValMap == null) {
			this.majiangValMap = new HashMap<Integer, List<DzMj>>();
		}
		this.majiangValMap.put(val, majiangs);
	}

	/**
	 * 符合的麻将值list
	 * @return
	 */
	public List<Integer> getValList() {
		return valList;
	}
	
	public List<DzMj> getMajiangs(){
		List<DzMj> majiangs=new ArrayList<>();
		if(majiangValMap!=null){
			for(Entry<Integer, List<DzMj>> entry: majiangValMap.entrySet()){
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

	public Map<Integer, List<DzMj>> getMajiangValMap() {
		return majiangValMap;
	}

	public void setMajiangValMap(Map<Integer, List<DzMj>> majiangValMap) {
		this.majiangValMap = majiangValMap;
	}
}
