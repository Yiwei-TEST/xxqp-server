package com.sy599.game.qipai.penghuzi.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sy599.game.qipai.penghuzi.constant.PenghzCard;

/**
 * @author lc
 * 
 */
public class PenghuziIndex {
	private Map<Integer, List<PenghzCard>> phzValMap;
	private List<Integer> valList;

	public PenghuziIndex() {
		phzValMap = new HashMap<>();
		valList = new ArrayList<>();
	}

	public void addPaohz(int val, List<PenghzCard> majiangs) {
		if (this.phzValMap == null) {
			this.phzValMap = new HashMap<Integer, List<PenghzCard>>();
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

//	public List<PenghzCard> getPhzs() {
//		List<PenghzCard> phzs = new ArrayList<>();
//		if (phzValMap != null) {
//			for (Entry<Integer, List<PenghzCard>> entry : phzValMap.entrySet()) {
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

	public Map<Integer, List<PenghzCard>> getPaohzValMap() {
		return phzValMap;
	}

	public List<PenghzCard> getPaohzList() {
		List<PenghzCard> list = new ArrayList<>();
		for (List<PenghzCard> phzList : phzValMap.values()) {
			list.addAll(phzList);
		}
		return list;
	}

	public void setPhzValMap(Map<Integer, List<PenghzCard>> majiangValMap) {
		this.phzValMap = majiangValMap;
	}
}
