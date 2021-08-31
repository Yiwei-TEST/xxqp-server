package com.sy599.game.qipai.hbgzp.bean;

import java.util.ArrayList;
import java.util.List;

public class HbgzpHu {

	private boolean isHu;
	private boolean pingHu;
	private List<Integer> daHuList = new ArrayList<>();
	private boolean isZiMo;

	public boolean isDaHu(){
		return !(daHuList==null || daHuList.isEmpty());
	}
	public boolean isDaHu(int index){
		return daHuList!=null && daHuList.contains(index);
	}
	public boolean isHu() {
		return isHu;
	}
	public void setHu(boolean isHu) {
		this.isHu = isHu;
	}
	public boolean isPingHu() {
		return pingHu;
	}
	public void setPingHu(boolean pingHu) {
		this.pingHu = pingHu;
	}
	public List<Integer> getDaHuList() {
		return daHuList;
	}
	public void setDaHuList(List<Integer> daHuList) {
		this.daHuList = daHuList;
	}
	public boolean isZiMo() {
		return isZiMo;
	}
	public void setZiMo(boolean isZiMo) {
		this.isZiMo = isZiMo;
	}
}
