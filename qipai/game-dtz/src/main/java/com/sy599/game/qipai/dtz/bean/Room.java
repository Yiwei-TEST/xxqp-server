package com.sy599.game.qipai.dtz.bean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Room {
	private long id;
	private Map<Long, DtzTable> tableMap = new ConcurrentHashMap<Long, DtzTable>();

	public Room(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public DtzTable getTable(long id) {
		return tableMap.get(id);
	}
	
}
