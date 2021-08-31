package com.sy.sanguo.common.sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sy.sanguo.game.dao.SqlDaoImpl;
import com.sy.sanguo.game.pdkuai.db.dao.SystemCommonInfoDao;

public class TableMobileReg {
	private static Map<String, String> sqlMap = new HashMap<String, String>();
	private static Map<String, String> sqlOrderInfoMap = new HashMap<String, String>();
	private static Map<String, String> sqlCreateTableMap = new HashMap<String, String>();
	private static Map<String, String> sqlUserExtendInfMap = new HashMap<String, String>();
	private static Map<String, String> sqlAuthorizationMap = new HashMap<String, String>();
	
	private static Map<String, String> sqlIndexMap = new HashMap<String, String>();
	
	static {
		sqlIndexMap.put("flatid_pf_unique", "CREATE UNIQUE INDEX flatid_pf_unique ON user_inf(flatId, pf)");
		
		sqlMap.put("totalCount", "ALTER TABLE user_inf ADD COLUMN `totalCount` BIGINT(20) DEFAULT '0' NOT NULL COMMENT '总局数';");
		sqlMap.put("os", "ALTER TABLE user_inf ADD COLUMN `os` varchar(50) COMMENT '系统';");
		sqlMap.put("headimgraw", "ALTER TABLE user_inf ADD COLUMN `headimgraw` varchar(255) COMMENT '未处理的头像';");
		sqlMap.put("gameSiteTableId", "ALTER TABLE `user_inf` ADD COLUMN `gameSiteTableId` BIGINT(20) DEFAULT '0' NOT NULL COMMENT '比赛场的牌桌ID';");
		sqlMap.put("loginExtend", "ALTER TABLE user_inf ADD COLUMN `loginExtend` varchar(512) COMMENT '登录工程拓展字段';");

		sqlOrderInfoMap.put("userId", "ALTER TABLE order_info ADD COLUMN `userId` bigint(20) NOT NULL DEFAULT '0';");
		
		sqlUserExtendInfMap.put("myConsume", "ALTER TABLE `user_extendinf` ADD COLUMN `myConsume` varchar(1000) DEFAULT NULL COMMENT '玩家消耗房卡记录'");
		sqlUserExtendInfMap.put("name", "ALTER TABLE `user_extendinf` ADD COLUMN `name` varchar(255) DEFAULT NULL COMMENT '用户名称' ");
		sqlUserExtendInfMap.put("totalMoney", "ALTER TABLE `user_extendinf` ADD COLUMN `totalMoney` float(10,2) NOT NULL DEFAULT '0.00' COMMENT '总额'");
		sqlUserExtendInfMap.put("shengMoney", "ALTER TABLE `user_extendinf` ADD COLUMN `shengMoney` double NOT NULL DEFAULT '0' COMMENT '已兑换金额'");
		sqlUserExtendInfMap.put("prizeFlag", "ALTER TABLE `user_extendinf` ADD COLUMN `prizeFlag` int(1) NOT NULL DEFAULT '0' COMMENT '排行榜领奖状态(0：不能领取 1：可领取 2：已领取)'");
		sqlUserExtendInfMap.put("bindSongCard", "ALTER TABLE `user_extendinf` ADD COLUMN `bindSongCard` INT(11) DEFAULT '0' NOT NULL COMMENT '绑定赠送房卡';");
		
		sqlAuthorizationMap.put("inviterId", "ALTER TABLE `weixin_authorization` ADD COLUMN `inviterId` bigint(20) NOT NULL DEFAULT '0'");
		sqlAuthorizationMap.put("inviterTime", "ALTER TABLE `weixin_authorization` ADD COLUMN `inviterTime` datetime DEFAULT NULL");
		
		sqlCreateTableMap.put("system_common_info", new StringBuffer()
		.append("CREATE TABLE `system_common_info` (")
		.append("`type` varchar(64) NOT NULL COMMENT '功能类型',")
		.append("`content` longtext NOT NULL COMMENT '内容',")
		.append("PRIMARY KEY (`type`)")
		.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8").toString());
		
		sqlCreateTableMap.put("daikai_table", new StringBuffer()
				.append("CREATE TABLE `daikai_table` (")
				.append("`tableId` bigint(11) NOT NULL DEFAULT '0' COMMENT '代开房间Id',")
				.append("`daikaiId` bigint(11) NOT NULL DEFAULT '0' COMMENT '代开人Id',")
				.append( "`serverId` int(11) NOT NULL DEFAULT '0' COMMENT '服务器Id',")
				.append("`playType` int(11) NOT NULL DEFAULT '0' COMMENT '玩法',")  
				.append("`needCard` int(11) NOT NULL DEFAULT '0' COMMENT '需要房卡数',") 
				.append("`state` int(1) NOT NULL DEFAULT '0' COMMENT '当前状态(1play,2结算)',") 
				.append("`createFlag` int(1) DEFAULT '0' COMMENT '是否已经开房',") 
				.append("`createPara` varchar(255) NOT NULL COMMENT '开房的参数',") 
				.append("`createStrPara` varchar(255) DEFAULT NULL COMMENT '开房str参数',") 
				.append("`createTime` datetime DEFAULT NULL COMMENT '开房时间',") 
				.append(" `daikaiTime` datetime NOT NULL COMMENT '代开时间',") 
				.append(" `returnFlag` int(1) DEFAULT '0' COMMENT '是否需要退还房卡',") 
				.append("`playerInfo` varchar(255) DEFAULT NULL COMMENT '玩家信息',")
				.append("`extend` varchar(255) DEFAULT NULL COMMENT '扩展字段',") 
				.append("PRIMARY KEY (`tableId`)")
				.append(")ENGINE=InnoDB DEFAULT CHARSET=utf8;").toString()) ;
		
		sqlCreateTableMap.put("hb_exchange_record", new StringBuffer()
		  .append("CREATE TABLE `hb_exchange_record` (")
		  .append("`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',")
		  .append("`userId` bigint(20) NOT NULL COMMENT '用户ID',")
		  .append("`money` double NOT NULL COMMENT '兑换金额',")
		  .append("`wxname` varchar(255) NOT NULL COMMENT '威信名称',")
		  .append("`phone` varchar(255) NOT NULL COMMENT '手机号码',")
		  .append("`createTime` datetime NOT NULL COMMENT '兑换时间',")
		  .append("`state` int(11) NOT NULL COMMENT '处理状态 (1：未处理，2：已处理)',")
		  .append("PRIMARY KEY (`id`)")
		  .append(") ENGINE=InnoDB DEFAULT CHARSET=utf8").toString());
		
		sqlCreateTableMap.put("hb_fafang_record", new StringBuffer()
		  .append("CREATE TABLE `hb_fafang_record` (")
		  .append("`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '唯一ID',")
		  .append("`tableId` bigint(20) NOT NULL COMMENT '牌桌ID',")
		  .append("`userId` bigint(20) NOT NULL COMMENT '用户ID',")
		  .append("`userName` varchar(128) NOT NULL COMMENT '用户名称',")
		  .append("`hbType` int(1) NOT NULL DEFAULT '0' COMMENT '红包类型(1：棋牌红包，2：幸运红包)',")
		  .append("`money` double NOT NULL COMMENT '红包金额',")
		  .append("`createTime` datetime NOT NULL COMMENT '创建时间',")
		  .append("PRIMARY KEY (`id`)")
		  .append(") ENGINE=InnoDB DEFAULT CHARSET=utf8").toString());
		
		sqlCreateTableMap.put("user_lucky", new StringBuffer()
		  .append("CREATE TABLE `user_lucky` (")
		  .append("`userId` bigint(20) NOT NULL,")
		  .append("`username` varchar(60) NOT NULL,")
		  .append("`sex` int(1) DEFAULT '0' COMMENT '1''男'' 2''女''',")
		  .append("`inviteeCount` int(10) DEFAULT '0' COMMENT '邀请的人数',")
		  .append("`invitorId` bigint(20) DEFAULT '0' COMMENT '我的邀请人的id',")
		  .append("`feedbackCount` int(10) DEFAULT '0',")
		  .append("`openCount` int(10) DEFAULT '0',")
		  .append("`activityStartTime` datetime DEFAULT NULL,")
		  .append("`prizeFlag` int(11) DEFAULT '0' COMMENT '是否领奖',")
		  .append("PRIMARY KEY (`userId`)")
		  .append(") ENGINE=InnoDB DEFAULT CHARSET=utf8").toString());
		
		sqlCreateTableMap.put("user_extendinf", new StringBuffer().append("CREATE TABLE `user_extendinf` ( `userId` bigint(20) NOT NULL, `cdk` text,  `extend` text,  `myConsume` varchar(1000) DEFAULT NULL COMMENT '玩家消耗房卡记录', `name` varchar(255) DEFAULT NULL COMMENT '用户名称', `totalMoney` double NOT NULL DEFAULT '0' COMMENT '总额', `shengMoney` double NOT NULL DEFAULT '0' COMMENT '剩余金额',  `prizeFlag` int(1) NOT NULL DEFAULT '0' COMMENT '排行榜领奖状态(0：不能领取 1：可领取 2：已领取)',  PRIMARY KEY (`userId`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8").toString());
		sqlCreateTableMap.put("system_message", new StringBuffer().append("CREATE TABLE `system_message` (  `id` int(11) NOT NULL AUTO_INCREMENT, `content` text COMMENT '公告内容',   `endTime` datetime DEFAULT NULL COMMENT '终止时间',  PRIMARY KEY (`id`)  ) ENGINE=InnoDB DEFAULT CHARSET=utf8").toString());
		sqlCreateTableMap.put("server_config", new StringBuffer().append("CREATE TABLE `server_config` (`id` int(11) NOT NULL,`name` varchar(100) DEFAULT NULL,`host` varchar(255) DEFAULT NULL,`chathost` varchar(255) DEFAULT NULL,`intranet` varchar(255) DEFAULT NULL,`gameType` varchar(100) DEFAULT NULL,`matchType` varchar(100) DEFAULT NULL,`onlineCount` int(11) DEFAULT '0',`extend` varchar(255) DEFAULT NULL,PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8").toString());
	}

	public static void check(SqlDaoImpl sqlDao) {
		checkCreateTable(sqlDao);
		checkUserInfo(sqlDao);
		checkOrderInfo(sqlDao);
		checkUserExtendInf(sqlDao);
		checkUserInfoIndex(sqlDao);
		checkAuthorizationInfo(sqlDao);	}
	
	private static void checkCreateTable(SqlDaoImpl sqlDao) {
		for (Entry<String, String> entry : sqlCreateTableMap.entrySet()) {
			if(!SystemCommonInfoDao.getInstance().isHasTableName(entry.getKey())){
				sqlDao.createTable(entry.getValue());
			}
		
		}
	}

	private static void checkOrderInfo(SqlDaoImpl sqlDao) {
		List<Map<String, Object>> list = sqlDao.showcolumns("order_info");
		if (list != null) {
			for (Entry<String, String> entry : sqlOrderInfoMap.entrySet()) {
				boolean find = false;
				for (Map<String, Object> columns : list) {
					String field = columns.get("Field").toString();
					if (field.equals(entry.getKey())) {
						find = true;
						break;
					}
				}
				if (!find) {
					sqlDao.update(entry.getValue());
				}
			}

		}

	}
	
	private static void checkAuthorizationInfo(SqlDaoImpl sqlDao) {
		List<Map<String, Object>> list = sqlDao.showcolumns("weixin_authorization");
		if (list != null) {
			for (Entry<String, String> entry : sqlAuthorizationMap.entrySet()) {
				boolean find = false;
				for (Map<String, Object> columns : list) {
					String field = columns.get("Field").toString();
					if (field.equals(entry.getKey())) {
						find = true;
						break;
					}
				}
				if (!find) {
					sqlDao.update(entry.getValue());
				}
			}

		}

	}
	
	private static void checkUserExtendInf(SqlDaoImpl sqlDao) {
		List<Map<String, Object>> list = sqlDao.showcolumns("user_extendinf");
		if (list != null) {
			for (Entry<String, String> entry : sqlUserExtendInfMap.entrySet()) {
				boolean find = false;
				for (Map<String, Object> columns : list) {
					String field = columns.get("Field").toString();
					if (field.equals(entry.getKey())) {
						find = true;
						break;
					}
				}
				if (!find) {
					sqlDao.update(entry.getValue());
				}
			}

		}
	}

	private static void checkUserInfo(SqlDaoImpl sqlDao) {
		List<Map<String, Object>> list = sqlDao.showcolumns("user_inf");
		if (list != null) {
			for (Entry<String, String> entry : sqlMap.entrySet()) {
				boolean find = false;
				for (Map<String, Object> columns : list) {
					String field = columns.get("Field").toString();
					if (field.equals(entry.getKey())) {
						find = true;
						break;
					}
				}
				if (!find) {
					sqlDao.update(entry.getValue());
				}
			}

		}
	}
	
	private static void checkUserInfoIndex(SqlDaoImpl sqlDao) {
		List<Map<String, Object>> list = sqlDao.showIndex("user_inf");
		if(list != null) {
			for(Entry<String, String> entry : sqlIndexMap.entrySet()) {
				boolean find = false;
				for(Map<String, Object> columns : list) {
					String keyName = columns.get("Key_name").toString();
					if(keyName.equals(entry.getKey())) {
						find = true;
						break;
					}
				}
				if(!find) {
					sqlDao.update(entry.getValue());
				}
			}
		}
	}
	
}
