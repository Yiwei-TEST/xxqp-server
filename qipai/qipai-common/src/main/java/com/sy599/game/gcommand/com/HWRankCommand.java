package com.sy599.game.gcommand.com;

import com.sy.mainland.util.CommonUtil;
import com.sy599.game.character.Player;
import com.sy599.game.db.bean.RankInfo;
import com.sy599.game.db.dao.gold.GoldDao;
import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.msg.serverPacket.ComMsg;
import com.sy599.game.msg.serverPacket.RankMsg;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ResourcesConfigsUtil;
import com.sy599.game.websocket.netty.coder.MessageUnit;

import java.util.*;

/**
 * @author liuping
 * 芒果跑得快排行榜响应接口
 */
public class HWRankCommand extends BaseCommand {

	/**
	 * 每日赢金币
	 */
	private static int rank_type_daily_gold = 1;

	/**
	 * 等级
	 */
	private static int rank_type_level = 2;


	/**
	 * 财富榜
	 */
	private static Map<Integer, RankInfo> playerGoldRankMap;

	/**
	 * 局数榜
	 */
	private static Map<Integer, RankInfo> playerCountRankMap;

	@Override
	public void execute(Player player, MessageUnit message) throws Exception {
		ComMsg.ComReq req = (ComMsg.ComReq) this.recognize(ComMsg.ComReq.class, message);
		List<Integer> lists = req.getParamsList();
		if (lists == null || lists.size() == 0) {
			return;
		}
		int requestType = req.getParams(0);
		RankMsg.RankLists.Builder allBuilders = RankMsg.RankLists.newBuilder();
		allBuilders.setOpenType(requestType);
		if(playerCountRankMap == null && playerGoldRankMap == null) {
			refreshRank();
		}
		if(requestType == 0) {
			allBuilders.addTypeRankInfos(getRankTypeInfoBuilder(rank_type_daily_gold));
			allBuilders.addTypeRankInfos(getRankTypeInfoBuilder(rank_type_level));
		}
		player.writeSocket(allBuilders.build());
	}

	@Override
	public void setMsgTypeMap() {
	}

	private RankMsg.TypeRankInfo.Builder getRankTypeInfoBuilder(int rankType) {
		Map<Integer, RankInfo> rankInfos = new HashMap<>();
		if(rankType == rank_type_daily_gold) {// 财富榜
			rankInfos = playerGoldRankMap;
		} else if(rankType == rank_type_level) {// 局数榜
			rankInfos = playerCountRankMap;
		}
		RankMsg.TypeRankInfo.Builder typeBuilder = RankMsg.TypeRankInfo.newBuilder();
		typeBuilder.setTaskType(rankType);
		if(rankInfos != null) {
			int myRank = 0;
			RankInfo myRankInfo = rankInfos.get(player.getUserId());
			if(myRankInfo != null) {
				myRank = myRankInfo.getRank();
			}
			typeBuilder.setMyRank(myRank);
			List<RankInfo> allRank = new ArrayList<>(rankInfos.values());
			Collections.sort(allRank);
			for (RankInfo rankInfo : allRank) {
				typeBuilder.addRankInfos(rankInfo.getTaskInfoBuilder());
			}
		}
		return typeBuilder;
	}

	private static void initRank(int rankType, int limitNum) {
		try {
			List<HashMap<String, Object>> rankInfoMap = new ArrayList<>();
			if(rankType == rank_type_daily_gold) {
				rankInfoMap = GoldDao.getInstance().selectGoldPlayerGoldRank(limitNum);
			} else if(rankType == rank_type_level) {
				rankInfoMap = GoldDao.getInstance().selectGoldPlayerCountRank(limitNum);
			}
			int rank = 1;
			Map<Integer, RankInfo> playerCountMap = new HashMap<>();
			for(Map<String, Object> rankInfo : rankInfoMap) {
				Long userId = CommonUtil.object2Long(rankInfo.get("userId"));
				String userName = (String) rankInfo.get("userName");
				String headimgurl = (String) rankInfo.get("headimgurl");
				RankInfo info = null;
				if(rankType == rank_type_daily_gold) {
					int totalGold = CommonUtil.object2Int(rankInfo.get("totalGold"));
					info = new RankInfo(rank, headimgurl, userName, userId.intValue(), totalGold);
				} else if(rankType == rank_type_level) {
					int playCount = CommonUtil.object2Int(rankInfo.get("playCount"));
					info = new RankInfo(rank, headimgurl, userName, userId.intValue(), playCount);
				}
				playerCountMap.put(userId.intValue(), info);
				rank ++;
			}
			if(rankType == rank_type_daily_gold) {
				playerGoldRankMap = playerCountMap;
			} else if(rankType == rank_type_level) {
				playerCountRankMap = playerCountMap;
			}
		} catch(Exception e) {
			LogUtil.errorLog.info("初始化排行榜异常:" + e.getMessage(), e);
		}
	}

	/**
	 * 排行榜刷新  每10分钟刷新一次
	 */
	public static void refreshRank() {
		try {
			if ("1".equals(ResourcesConfigsUtil.loadServerPropertyValue("rank_refresh"))) {// 数据库ServerConfig配置开关控制是否刷新
				String rankLimitNum = ResourcesConfigsUtil.loadServerPropertyValue("rank_limit_num");// 数据库配置排行显示人数
				int limitNum = 50;
				if (rankLimitNum != null) {
					limitNum = Integer.parseInt(rankLimitNum);
				}
				initRank(rank_type_daily_gold, limitNum);
				initRank(rank_type_level, limitNum);
				LogUtil.msgLog.info("排行榜刷新完成");
			}
		} catch (Exception e) {
			LogUtil.errorLog.info("排行榜刷新异常:" + e.getMessage(), e);
		}
	}
}
