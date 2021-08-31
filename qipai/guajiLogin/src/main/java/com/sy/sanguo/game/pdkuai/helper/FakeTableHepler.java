package com.sy.sanguo.game.pdkuai.helper;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.MathUtil;
import com.sy.sanguo.game.bean.group.GroupFakeTable;
import com.sy.sanguo.game.constants.GroupConstants;
import com.sy.sanguo.game.dao.group.GroupDaoManager;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FakeTableHepler {
	public static final Object lock = new Object();
	private static final Map<Long,String> freeFakeHeadImgMap = new ConcurrentHashMap<>();//对应t_group_fake_table_headimage中未被使用的头像


	public static String removeFreeFakeHeadImg(Object keyId){
		synchronized(lock) {
			return freeFakeHeadImgMap.remove(keyId);
		}
	}

	public static void addFreeFakeHeadImg(Long keyId, String imgUrl){
		synchronized(lock) {
			freeFakeHeadImgMap.put(keyId,imgUrl);
		}
	}


	public static void checkFakeTableRefresh() {
		try{
			Long now = new Date().getTime()/1000;
			List<Map<String,Object>> fakeTableList = GroupDaoManager.getInstance().loadAllFakeTable();
			if(fakeTableList != null && fakeTableList.size() > 0){
				for (Map<String, Object> tableMap : fakeTableList){
					Long fakeTableId = Long.parseLong(tableMap.get("keyId").toString());
					Long refreshTime = Long.parseLong(tableMap.get("roundRefrshTime").toString());
					Integer overCount = Integer.parseInt(tableMap.get("overCount").toString());
					Integer playedBureau = Integer.parseInt(tableMap.get("playedBureau").toString());
					Integer playerCount = Integer.parseInt(tableMap.get("playerCount").toString());
					if(now > refreshTime){
						synchronized(lock) {
							//更新局数
							boolean newDJ = false;    //是否开始新一大局
							if (playedBureau >= overCount-1)
								newDJ = true;
							int newPlayedBureau = newDJ ? 0 : playedBureau + 1;
							long nextRefreshTime = now + MathUtil.mt_rand(GroupConstants.FAKE_TABLE_REFRESH_TIME_MINI, GroupConstants.FAKE_TABLE_REFRESH_TIME_MAX);
							int result = GroupDaoManager.getInstance().updateFakeTablePlayedBureau(fakeTableId, newPlayedBureau, nextRefreshTime);
							if (result == 1 && newDJ) {    //更新玩家头像
								List<Long> freeHeadList = new ArrayList<>();        //入选的闲置头像
								int count = 0;
								Iterator<Long> iterator = freeFakeHeadImgMap.keySet().iterator();
								while (iterator.hasNext() && count < playerCount) {
									Long key = iterator.next();
									freeHeadList.add(key);
									iterator.remove();
									count++;
								}
								if (freeHeadList.size() > 0) {
									//老头像回收，fakeTableId 置0
									freeFakeTableHeadImg(fakeTableId);
									//关联新的头像
									GroupDaoManager.getInstance().updateFakeHeadimageByKeyId(StringUtils.join(freeHeadList, ","), fakeTableId);
								}

							}
						}
					}
				}
			}
		}
		catch (Exception e) {
			GameBackLogger.SYS_LOG.error(e.getMessage(), e);
		}
	}

	public static void setFreeFakeTableHeadImg(Long fakeTableId) throws Exception {
		synchronized(lock) {
			freeFakeTableHeadImg(fakeTableId);
		}
	}

	private static void freeFakeTableHeadImg(Long fakeTableId) throws Exception {
		List<Map<String, Object>> oldFakeImgList = GroupDaoManager.getInstance().loadFakeImgByTableId(fakeTableId);
		GroupDaoManager.getInstance().setFreeFakeHeadimageByfakeTableId(fakeTableId);
		for (Map<String, Object> fakeImgMap : oldFakeImgList) {    //回收为可用闲置头像
			freeFakeHeadImgMap.put(Long.parseLong(fakeImgMap.get("keyId").toString()), fakeImgMap.get("headimgurl").toString());
		}
	}

	public static void setFakeTableHeadImg(GroupFakeTable fakeTable) {
		try {
			synchronized (lock) {
				List<Long> freeHeadList = new ArrayList<>();        //入选的闲置头像
				int count = 0;
				Iterator<Long> iterator = freeFakeHeadImgMap.keySet().iterator();
				while (iterator.hasNext() && count < fakeTable.getPlayerCount()) {
					Long key = iterator.next();
					freeHeadList.add(key);
					iterator.remove();
					count++;
				}
				if (freeHeadList.size() > 0) {
					//关联新的头像
					GroupDaoManager.getInstance().updateFakeHeadimageByKeyId(StringUtils.join(freeHeadList, ","), fakeTable.getKeyId());
				}
			}
		} catch (Exception e) {
			GameBackLogger.SYS_LOG.error(e.getMessage(), e);
		}
	}

	public static void refreshFreeFakeHeadImgMap(){
		try {
			List<Map<String, Object>> freeFakeImgList = GroupDaoManager.getInstance().loadFakeImgByTableId(0);
			synchronized(lock) {
				for (Map<String, Object> fakeImgMap : freeFakeImgList) {
					freeFakeHeadImgMap.put(Long.parseLong(fakeImgMap.get("keyId").toString()), fakeImgMap.get("headimgurl").toString());
				}
			}
		}
		catch (Exception e) {
			GameBackLogger.SYS_LOG.error(e.getMessage(), e);
		}
	}

}
