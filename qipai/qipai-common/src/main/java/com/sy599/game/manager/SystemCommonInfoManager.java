package com.sy599.game.manager;

import com.sy.mainland.util.MD5Util;
import com.sy599.game.common.constant.SharedConstants;
import com.sy599.game.common.constant.SystemCommonInfoType;
import com.sy599.game.db.bean.SystemCommonInfo;
import com.sy599.game.db.dao.SystemCommonInfoDao;
import com.sy599.game.util.GeneralUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统公共信息存储
 * 
 * @author lc
 * @date 2016-8-10
 * @version v1.0
 */
public class SystemCommonInfoManager {
	private static SystemCommonInfoManager _inst = new SystemCommonInfoManager();

	private Map<String, SystemCommonInfo> map = new ConcurrentHashMap<>();
	private Map<String, SystemCommonInfo> updateMap = new ConcurrentHashMap<>();

	public static SystemCommonInfoManager getInstance() {
		return _inst;
	}

	public SystemCommonInfo getSystemCommonInfo(SystemCommonInfoType type) {
		return map.get(type.name());
	}

	public synchronized void updateSystemCommonInfo(SystemCommonInfo info) {
		map.put(info.getType(), info);
		updateMap.put(info.getType(), info);
	}

	public synchronized int updateStartGame(SystemCommonInfo startGameInfo,int flag) {
		if (startGameInfo == null){
			startGameInfo = getSystemCommonInfo(SystemCommonInfoType.isStartGame);
		}
		if (startGameInfo != null) {
			String oldContent = startGameInfo.getContent();
			String mark;
			if (flag == 1){
				mark = MD5Util.getMD5String(GeneralUtil.loadCurrentServerMsg());
			}else{
				mark = "0";
			}

			if (mark.equals(oldContent)){
				return 0;
			}else{
				int ret = SystemCommonInfoDao.getInstance().updateServerSwitch(startGameInfo.getType(),oldContent,mark);
				if (ret>0){
					startGameInfo.setContent(mark);
					map.put(startGameInfo.getType(), startGameInfo);
				}
				return ret;
			}
		}
		return -1;
	}

	public synchronized void updateShutDownGameVersion(int version) {
		SystemCommonInfo info = getSystemCommonInfo(SystemCommonInfoType.gameShutDownVersion);
		if (info != null) {
			info.setContent(version + "");
			updateSystemCommonInfo(info);
		}

	}

	public synchronized void updateGameVersion(int version) {
		SystemCommonInfo info = getSystemCommonInfo(SystemCommonInfoType.gameVersion);
		if (info != null) {
			info.setContent(version + "");
			updateSystemCommonInfo(info);
		}

	}

	public synchronized void updateGameStartUpTime(String time) {
		SystemCommonInfo info = getSystemCommonInfo(SystemCommonInfoType.gameStartUpTime);
		if (info != null) {
			info.setContent(time);
			updateSystemCommonInfo(info);
		}

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
				if (info == SystemCommonInfoType.paomadeng) {
					baseInfo.setContent("{}");
				}
				if (info == SystemCommonInfoType.isConsumeCards) {
					baseInfo.setContent("1");
				}
				if (info == SystemCommonInfoType.isStartGame) {
					baseInfo.setContent(MD5Util.getMD5String(GeneralUtil.loadCurrentServerMsg()));
				}
				if (info == SystemCommonInfoType.gameVersion) {
					baseInfo.setContent("0");
				}
				if (info == SystemCommonInfoType.gameShutDownVersion) {
					baseInfo.setContent("0");
				}
				if (info == SystemCommonInfoType.gameStartUpTime) {
					baseInfo.setContent("");
				}
				if (info == SystemCommonInfoType.goldGive) {
					baseInfo.setContent(SystemCommonInfoType.goldGive.getContent());
				}

				SystemCommonInfoDao.getInstance().save(baseInfo);
				map.put(baseInfo.getType(), baseInfo);
			}
		}

		SystemCommonInfo info = getSystemCommonInfo(SystemCommonInfoType.isConsumeCards);
		if (info.getContent().equals("1")) {
			SharedConstants.consumecards = true;
		} else {
			SharedConstants.consumecards = false;
		}
		SystemCommonInfo goldInfo = getSystemCommonInfo(SystemCommonInfoType.isConsumeGold);
		SharedConstants.consumegold = goldInfo.getContent().equals("1");
	}

	/**
	 * 当前幸运红包金额总数
	 * 
	 * @param time
	 * @return
	 */
	public int getLoginLuckHbSystemCommonInfo(long time) {
		SystemCommonInfo timeInfo = SystemCommonInfoDao.getInstance().selectLogin("luckHbTime");
		if (timeInfo == null) {
			return 0;
		}

		if (Long.parseLong(timeInfo.getContent()) != time) {
			return 0;
		}

		SystemCommonInfo hb = SystemCommonInfoDao.getInstance().selectLogin("luckHbMoney");
		if (hb == null) {
			return 0;
		}

		return Integer.parseInt(hb.getContent());

	}

	/**
	 * 更新幸运红包金额总数
	 * 
	 * @param time
	 * @return
	 */
	public void updateLoginLuckHbSystemCommonInfo(long time, int money) {
		SystemCommonInfo timeInfo = SystemCommonInfoDao.getInstance().selectLogin("luckHbTime");
		if (timeInfo == null || Long.parseLong(timeInfo.getContent()) != time) {
			SystemCommonInfoDao.getInstance().updateLogin("luckHbTime", time + "");
			SystemCommonInfoDao.getInstance().updateLogin("luckHbMoney", money + "");
		} else {
			SystemCommonInfoDao.getInstance().sumLogin("luckHbMoney", money + "");
		}

	}

	public SystemCommonInfo getLoginSystemCommonInfo(String type) {
		return SystemCommonInfoDao.getInstance().selectLogin(type);
	}

	/**
	 * 提交到数据库
	 */
	public synchronized void saveDB(boolean asyn) {
		Iterator<Map.Entry<String,SystemCommonInfo>> it = updateMap.entrySet().iterator();
		while (it.hasNext()){
			if (asyn){
				SystemCommonInfoDao.getInstance().asynchUpdate(it.next().getValue());
			}else{
				SystemCommonInfoDao.getInstance().update(it.next().getValue());
			}
			it.remove();
		}
	}
}
