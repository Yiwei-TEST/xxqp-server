package com.sy599.game.qipai.dtz.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.sy599.game.qipai.dtz.tool.CardToolDtz;

public class AutoHandPais {

	private Map<Integer, Integer> single = new TreeMap<>();//<牌数字，牌ID集合>
	private Map<Integer, List<Integer>> pair = new TreeMap<>();
	private Map<Integer, List<Integer>> three = new TreeMap<>();
	private Map<Integer, List<Integer>> bomb = new LinkedHashMap<>();
	private Map<Integer, List<Integer>> tongzi = new LinkedHashMap<>();//筒子的key为牌ID
	private Map<Integer, List<Integer>> bombdi = new TreeMap<>();
	private Map<Integer, List<Integer>> xi = new LinkedHashMap<>();//喜的key为牌ID
	
	public void order(){
		if(!getBomb().isEmpty()){
			setBombOrder(getBomb());
		}
		if(!getTongzi().isEmpty()){
			setOrder(getTongzi());
		}
		if(!getXi().isEmpty()){
			setOrder(getXi());
		}
	}
	public void setOrder(Map<Integer, List<Integer>> map){
		List<Map.Entry<Integer, List<Integer>>> list = new ArrayList<Map.Entry<Integer, List<Integer>>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Integer, List<Integer>>>() {
			@Override
			public int compare(Map.Entry<Integer, List<Integer>> o1, Map.Entry<Integer, List<Integer>> o2) {
				int val1 = CardToolDtz.toVal(o1.getKey());
				int val2 = CardToolDtz.toVal(o2.getKey());
				int color1 = CardToolDtz.toSuit(o1.getKey());
				int color2 = CardToolDtz.toSuit(o2.getKey());
				int flag = val1 - val2;
				if(flag == 0){
					return color1 - color2;
				}else{
					return flag;
				}
			}
		});
		map.clear();
		for(Map.Entry<Integer, List<Integer>> entry:list){
			map.put(entry.getKey(), entry.getValue());
		}
	}
	public void setBombOrder(Map<Integer, List<Integer>> map){
		List<Map.Entry<Integer, List<Integer>>> list = new ArrayList<Map.Entry<Integer, List<Integer>>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Integer, List<Integer>>>() {
			@Override
			public int compare(Map.Entry<Integer, List<Integer>> o1, Map.Entry<Integer, List<Integer>> o2) {
				int len1 = o1.getValue().size();
				int len2 = o2.getValue().size();
				int flag = len1 - len2;
				if(flag == 0){
					return o1.getKey() - o2.getKey();
				}else{
					return flag;
				}
			}
		});
		map.clear();
		for(Map.Entry<Integer, List<Integer>> entry:list){
			map.put(entry.getKey(), entry.getValue());
		}
	}
	
	public Map<Integer, Integer> getSingle() {
		return single;
	}
	public void setSingle(Map<Integer, Integer> single) {
		this.single = single;
	}
	public Map<Integer, List<Integer>> getPair() {
		return pair;
	}
	public void setPair(Map<Integer, List<Integer>> pair) {
		this.pair = pair;
	}
	public Map<Integer, List<Integer>> getThree() {
		return three;
	}
	public void setThree(Map<Integer, List<Integer>> three) {
		this.three = three;
	}
	public Map<Integer, List<Integer>> getBomb() {
		return bomb;
	}
	public void setBomb(Map<Integer, List<Integer>> bomb) {
		this.bomb = bomb;
	}
	public Map<Integer, List<Integer>> getTongzi() {
		return tongzi;
	}
	public void setTongzi(Map<Integer, List<Integer>> tongzi) {
		this.tongzi = tongzi;
	}
	public Map<Integer, List<Integer>> getBombdi() {
		return bombdi;
	}
	public void setBombdi(Map<Integer, List<Integer>> bombdi) {
		this.bombdi = bombdi;
	}
	public Map<Integer, List<Integer>> getXi() {
		return xi;
	}
	public void setXi(Map<Integer, List<Integer>> xi) {
		this.xi = xi;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("AutoHandPais [single=");
		if(single.isEmpty()){
			sb.append("#");
		}else{
			sb.append(single.values());
		}
		sb.append(" pair=");
		if(pair.isEmpty()){
			sb.append("#");
		}else{
			for(Entry<Integer, List<Integer>> entry : pair.entrySet()){
				sb.append(entry.getValue()).append("_");
			}
		}
		sb.append(" three=");
		if(three.isEmpty()){
			sb.append("#");
		}else{
			for(Entry<Integer, List<Integer>> entry : three.entrySet()){
				sb.append(entry.getValue()).append("_");
			}
		}
		sb.append(" bomb=");
		if(bomb.isEmpty()){
			sb.append("#");
		}else{
			for(Entry<Integer, List<Integer>> entry : bomb.entrySet()){
				sb.append(entry.getValue()).append("_");
			}
		}
		sb.append(" tongzi=");
		if(tongzi.isEmpty()){
			sb.append("#");
		}else{
			for(Entry<Integer, List<Integer>> entry : tongzi.entrySet()){
				sb.append(entry.getValue()).append("_");
			}
		}
		sb.append(" bombdi=");
		if(bombdi.isEmpty()){
			sb.append("#");
		}else{
			for(Entry<Integer, List<Integer>> entry : bombdi.entrySet()){
				sb.append(entry.getValue()).append("_");
			}
		}
		sb.append(" xi=");
		if(xi.isEmpty()){
			sb.append("#");
		}else{
			for(Entry<Integer, List<Integer>> entry : xi.entrySet()){
				sb.append(entry.getValue()).append("_");
			}
		}
		return sb.toString();
	}
}
