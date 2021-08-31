package com.sy.sanguo.game.pdkuai.manager;

import com.sy.sanguo.game.pdkuai.constants.SharedConstants;
import com.sy.sanguo.game.pdkuai.constants.SystemCommonInfoType;
import com.sy.sanguo.game.pdkuai.db.bean.SystemCommonInfo;
import com.sy.sanguo.game.pdkuai.db.dao.SystemCommonInfoDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统公共信息存储
 * 
 * @author lc
 * @date 2016-8-10
 * @version v1.0
 */
public class SystemCommonInfoManager {
	private static SystemCommonInfoManager _inst = new SystemCommonInfoManager();

	private Map<String, SystemCommonInfo> map = new HashMap<String, SystemCommonInfo>();
	private Map<String, SystemCommonInfo> updateMap = new HashMap<String, SystemCommonInfo>();

	public static SystemCommonInfoManager getInstance() {
		return _inst;
	}

	public SystemCommonInfo getSystemCommonInfo(SystemCommonInfoType type) {
		return map.get(type.name());
	}

	public synchronized void updateSystemCommonInfo(SystemCommonInfo info) {
		map.put(info.getType(), info);
		updateMap.put(info.getType(), info);
		saveDB();
	}

	/**
	 * 初始化数据
	 */
	public void initData() {
		List<SystemCommonInfo> infoList = SystemCommonInfoDao.getInstance().selectAll();
		if (infoList != null) {
			for (SystemCommonInfo info : infoList) {
				map.put(info.getType(), info);
			}
		}

		for (SystemCommonInfoType info : SystemCommonInfoType.values()) {
			if (map.get(info.name()) == null) {
				// 数据库里边没有初始化，就进行初始化
				SystemCommonInfo baseInfo = new SystemCommonInfo();
				baseInfo.setType(info.name());
				baseInfo.setContent("");
				if (info == SystemCommonInfoType.giveRoomCards) {
					baseInfo.setContent("5");
				}
				if (info == SystemCommonInfoType.blockIconTime) {
					baseInfo.setContent("0");
				}
				if (info == SystemCommonInfoType.luckHbTime) {
					baseInfo.setContent("0");
				}
				if (info == SystemCommonInfoType.luckHbMoney) {
					baseInfo.setContent("0");
				}
				if (info == SystemCommonInfoType.bindGiveRoomCards) {
					baseInfo.setContent("5");
				}
				SystemCommonInfoDao.getInstance().asynchSave(baseInfo);
				map.put(baseInfo.getType(), baseInfo);
			}
		}
		SystemCommonInfo bindGiveRoomCardsInfo = getSystemCommonInfo(SystemCommonInfoType.bindGiveRoomCards);
		if (bindGiveRoomCardsInfo.getContent() != null) {
			SharedConstants.bindGiveRoomCards = Integer.parseInt(bindGiveRoomCardsInfo.getContent());
		}

		SystemCommonInfo giveRoomCardsInfo = getSystemCommonInfo(SystemCommonInfoType.giveRoomCards);
		if (giveRoomCardsInfo.getContent() != null) {
			SharedConstants.giveRoomCards = Integer.parseInt(giveRoomCardsInfo.getContent());
		}

		SystemCommonInfo blockIconTimeInfo = getSystemCommonInfo(SystemCommonInfoType.blockIconTime);
		if (blockIconTimeInfo.getContent() != null) {
			SharedConstants.blockIconTime = Long.parseLong(blockIconTimeInfo.getContent());
		}
	}

	/**
	 * 提交到数据库
	 */
	public synchronized void saveDB() {
		for (SystemCommonInfo info : updateMap.values()) {
			SystemCommonInfoDao.getInstance().asynchUpdate(info);
		}

		updateMap.clear();
	}

	public static void main(String[] args) {
		/*
		 * JsonWrapper json = new JsonWrapper(""); json.putString(1, "hello");
		 * json.putString(2, "123"); Set<String> keys = json.getKeySet();
		 */

	}
}
