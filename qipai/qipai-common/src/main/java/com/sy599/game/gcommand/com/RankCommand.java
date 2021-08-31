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
public class RankCommand extends BaseCommand {

	private static int rank_type_gold = 1;

	private static int rank_type_count = 2;

	private static int rank_type_jifen_lianTong = 3;

	private static int rank_type_jifen_yidong = 4;

	private static int rank_type_jifen_dianxin = 5;

	private static int rank_type_jifen_mangguo = 6;

	/**
	 * 联通PF
	 */
	private static String pf_lianTong = "liantong";
	/**
	 * 移动PF
	 */
	private static String pf_yidong = "yidong";
	/**
	 * 电信PF
	 */
	private static String pf_dianxin = "dianxin";
	/**
	 * 芒果PF
	 */
	private static String pf_mangguo = "mangguo";

	/**
	 * 财富榜
	 */
	private static Map<Integer, RankInfo> playerGoldRankMap;

	/**
	 * 局数榜
	 */
	private static Map<Integer, RankInfo> playerCountRankMap;

	/**
	 * 渠道积分榜
	 */
	private static Map<Integer, Map<Integer, RankInfo>> pfJifenRankMap;

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
		if(playerCountRankMap == null && playerGoldRankMap == null && pfJifenRankMap == null) {
			refreshRank();
		}
		Map<String, RankMsg.TypeRankInfo.Builder> jifenRankMap = new HashMap<>();
		jifenRankMap.put(pf_lianTong, getRankTypeInfoBuilder(rank_type_jifen_lianTong));
		jifenRankMap.put(pf_yidong, getRankTypeInfoBuilder(rank_type_jifen_yidong));
		jifenRankMap.put(pf_dianxin, getRankTypeInfoBuilder(rank_type_jifen_dianxin));
		jifenRankMap.put(pf_mangguo, getRankTypeInfoBuilder(rank_type_jifen_mangguo));
		if(requestType == 0) {
			if(jifenRankMap.containsKey(player.getPf())) {// 自己渠道积分排行放最前面
				allBuilders.addTypeRankInfos(jifenRankMap.get(player.getPf()));
				jifenRankMap.remove(player.getPf());
			}
			for(RankMsg.TypeRankInfo.Builder builder : jifenRankMap.values()) {
				allBuilders.addTypeRankInfos(builder);
			}
			allBuilders.addTypeRankInfos(getRankTypeInfoBuilder(rank_type_gold));
			allBuilders.addTypeRankInfos(getRankTypeInfoBuilder(rank_type_count));
		} else {// 积分排行榜
			if(jifenRankMap.containsKey(player.getPf())) {// 自己渠道积分排行放最前面
				allBuilders.addTypeRankInfos(jifenRankMap.get(player.getPf()));
				jifenRankMap.remove(player.getPf());
			}
			for(RankMsg.TypeRankInfo.Builder builder : jifenRankMap.values()) {
				allBuilders.addTypeRankInfos(builder);
			}
		}
		player.writeSocket(allBuilders.build());
	}

	@Override
	public void setMsgTypeMap() {
	}

	private RankMsg.TypeRankInfo.Builder getRankTypeInfoBuilder(int rankType) {
		Map<Integer, RankInfo> rankInfos = new HashMap<>();
		if(rankType == rank_type_gold) {// 财富榜
			rankInfos = playerGoldRankMap;
		} else if(rankType == rank_type_count) {// 局数榜
			rankInfos = playerCountRankMap;
		} else if(rankType == rank_type_jifen_lianTong) {// 联通积分榜
			rankInfos = pfJifenRankMap.get(rank_type_jifen_lianTong);
		} else if(rankType == rank_type_jifen_yidong) {// 移动积分榜
			rankInfos = pfJifenRankMap.get(rank_type_jifen_yidong);
		} else if(rankType == rank_type_jifen_dianxin) {// 电信积分榜
			rankInfos = pfJifenRankMap.get(rank_type_jifen_dianxin);
		} else if(rankType == rank_type_jifen_mangguo) {// 芒果积分榜
			rankInfos = pfJifenRankMap.get(rank_type_jifen_mangguo);
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
			if(rankType == rank_type_gold) {
				rankInfoMap = GoldDao.getInstance().selectGoldPlayerGoldRank(limitNum);
			} else if(rankType == rank_type_count) {
				rankInfoMap = GoldDao.getInstance().selectGoldPlayerCountRank(limitNum);
			} else if(rankType >= rank_type_jifen_lianTong && rankType <= rank_type_jifen_mangguo) {
				rankInfoMap = GoldDao.getInstance().selectGoldPlayerJiFen(getPfByRankType(rankType), limitNum);
			}
			int rank = 1;
			Map<Integer, RankInfo> playerCountMap = new HashMap<>();
			for(Map<String, Object> rankInfo : rankInfoMap) {
				Long userId = CommonUtil.object2Long(rankInfo.get("userId"));
				String userName = (String) rankInfo.get("userName");
				String headimgurl = (String) rankInfo.get("headimgurl");
				RankInfo info = null;
				if(rankType == rank_type_gold) {
					int totalGold = CommonUtil.object2Int(rankInfo.get("totalGold"));
					info = new RankInfo(rank, headimgurl, userName, userId.intValue(), totalGold);
				} else if(rankType == rank_type_count) {
					int playCount = CommonUtil.object2Int(rankInfo.get("playCount"));
					info = new RankInfo(rank, headimgurl, userName, userId.intValue(), playCount);
				} else if(rankType >= rank_type_jifen_lianTong && rankType <= rank_type_jifen_mangguo) {
					int jifen = CommonUtil.object2Int(rankInfo.get("jifen"));
					info = new RankInfo(rank, headimgurl, userName, userId.intValue(), jifen);
				}
				playerCountMap.put(userId.intValue(), info);
				rank ++;
			}
			if(rankType == rank_type_gold) {
				playerGoldRankMap = playerCountMap;
			} else if(rankType == rank_type_count) {
				playerCountRankMap = playerCountMap;
			} else if(rankType >= rank_type_jifen_lianTong && rankType <= rank_type_jifen_mangguo) {
				if(pfJifenRankMap == null)
					pfJifenRankMap = new HashMap<>();
				pfJifenRankMap.put(rankType, playerCountMap);
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
				initRank(rank_type_gold, limitNum);
				initRank(rank_type_count, limitNum);
				initRank(rank_type_jifen_lianTong, limitNum);
				initRank(rank_type_jifen_yidong, limitNum);
				initRank(rank_type_jifen_dianxin, limitNum);
				initRank(rank_type_jifen_mangguo, limitNum);
				LogUtil.msgLog.info("排行榜刷新完成");
			}
		} catch (Exception e) {
			LogUtil.errorLog.info("排行榜刷新异常:" + e.getMessage(), e);
		}
	}

	private static String getPfByRankType(int rankType) {
		if(rankType == rank_type_jifen_lianTong) {
			return pf_lianTong;
		} else if(rankType == rank_type_jifen_yidong) {
			return pf_yidong;
		} else if(rankType == rank_type_jifen_dianxin) {
			return pf_dianxin;
		} else if(rankType == rank_type_jifen_mangguo) {
			return pf_mangguo;// 芒果积分排行榜会将common标识的也算上
		}
		return "";
	}
}
