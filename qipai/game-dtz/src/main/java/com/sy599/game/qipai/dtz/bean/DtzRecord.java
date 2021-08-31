package com.sy599.game.qipai.dtz.bean;

import java.util.List;
import java.util.Map;

public class DtzRecord {
	private long tid;
	private List<Map<String, Object>> resList;

	public long getTid() {
		return tid;
	}

	public void setTid(long tid) {
		this.tid = tid;
	}

	public List<Map<String, Object>> getResList() {
		return resList;
	}

	public void setResList(List<Map<String, Object>> resList) {
		this.resList = resList;
	}

}
