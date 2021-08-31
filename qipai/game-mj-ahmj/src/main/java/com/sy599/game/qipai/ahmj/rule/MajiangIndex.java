package com.sy599.game.qipai.ahmj.rule;

import com.sy599.game.qipai.ahmj.constant.Ahmj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MajiangIndex {
	private Map<Integer, List<Ahmj>> majiangValMap;
	private List<Integer> valList;

	public MajiangIndex() {
		majiangValMap = new HashMap<>();
		valList = new ArrayList<>();
	}

	public void addMajiang(int val, List<Ahmj> majiangs) {
		if (this.majiangValMap == null) {
			this.majiangValMap = new HashMap<Integer, List<Ahmj>>();
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
	
	public List<Ahmj> getMajiangs(){
		List<Ahmj> majiangs=new ArrayList<>();
		if(majiangValMap!=null){
			for(Entry<Integer, List<Ahmj>> entry: majiangValMap.entrySet()){
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

	public Map<Integer, List<Ahmj>> getMajiangValMap() {
		return majiangValMap;
	}

	public void setMajiangValMap(Map<Integer, List<Ahmj>> majiangValMap) {
		this.majiangValMap = majiangValMap;
	}
}
