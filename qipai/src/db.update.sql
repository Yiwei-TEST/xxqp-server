/*数据库更新日志 记录下更新人    更新日期  更新内容 */
/*金币场补给记录表*/
CREATE TABLE `t_gold_remedy` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) NOT NULL,
  `drawTime` datetime NOT NULL COMMENT '领取时间',
  `remedy` int(11) DEFAULT '0' COMMENT '补救金额',
  PRIMARY KEY (`keyId`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;
/*金币场玩家信息表*/
CREATE TABLE `t_gold_user` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userId` bigint(19) NOT NULL COMMENT '玩家id',
  `userName` varchar(100) DEFAULT NULL COMMENT '玩家名称',
  `userNickname` varchar(100) DEFAULT NULL COMMENT '玩家昵称',
  `playCount` int(9) NOT NULL COMMENT '局数',
  `playCountWin` int(9) NOT NULL COMMENT '胜局数',
  `playCountLose` int(9) NOT NULL COMMENT '败局数',
  `playCountEven` int(9) NOT NULL COMMENT '平局数',
  `freeGold` int(19) NOT NULL COMMENT '免费的金币',
  `Gold` int(19) NOT NULL COMMENT '付费的金币',
  `usedGold` int(19) NOT NULL COMMENT '消费的金币',
  `vipexp` bigint(20) NOT NULL DEFAULT '0' COMMENT 'vip经验',
  `exp` bigint(20) NOT NULL DEFAULT '0' COMMENT '经验值',
  `sex` int(2) NOT NULL DEFAULT '1' COMMENT '性别',
  `signature` text COMMENT '个性签名',
  `headimgurl` varchar(255) DEFAULT NULL COMMENT '头像',
  `headimgraw` varchar(255) DEFAULT '' COMMENT '原生头像',
  `extend` varchar(255) DEFAULT NULL COMMENT '扩展',
  `regTime` timestamp NULL DEFAULT NULL COMMENT '注册时间',
  `lastLoginTime` timestamp NULL DEFAULT NULL COMMENT '最后登录时间',
  `grade` int(9) DEFAULT '0' COMMENT '芒果跑得快段位',
  `gradeExp` int(9) DEFAULT '0' COMMENT '芒果跑得快段位经验值',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_userId` (`userId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;
/*金币场房间表*/
CREATE TABLE `t_gold_room` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `currentCount` int(3) NOT NULL COMMENT '当前房间人数',
  `maxCount` int(3) NOT NULL COMMENT '最大人数',
  `serverId` int(10) NOT NULL COMMENT '服id',
  `currentState` varchar(20) NOT NULL COMMENT '当前状态（0：未开始，1：已开始，2：已结束）',
  `tableMsg` varchar(255) NOT NULL COMMENT '房间信息',
  `modeId` varchar(32) NOT NULL COMMENT '模式ID',
  `gameCount` int(3) NOT NULL COMMENT '局数',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifiedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`keyId`),
  KEY `idx_common` (`modeId`,`currentState`,`serverId`),
  KEY `idx_createdTime` (`createdTime`)
) ENGINE=InnoDB AUTO_INCREMENT=10000000 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='无房号金币场房间';
/*金币场房间玩家信息表*/
CREATE TABLE `t_gold_room_user` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `roomId` bigint(20) NOT NULL COMMENT '房间id',
  `userId` varchar(32) NOT NULL COMMENT '玩家ID',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gameResult` int(11) NOT NULL DEFAULT '0' COMMENT '结果',
  `logIds` varchar(512) DEFAULT NULL COMMENT 'logId多个以逗号隔开',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_room` (`roomId`,`userId`),
  KEY `idx_userId` (`userId`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `t_gold_user_firstmyth` (
  `recordDate` int(10) NOT NULL DEFAULT '0' COMMENT '记录日期',
  `userId` bigint(20) NOT NULL DEFAULT '0' COMMENT '玩家ID',
  `userName` varchar(60) NOT NULL DEFAULT '' COMMENT '玩家昵称',
  `record1` int(10) NOT NULL DEFAULT '0' COMMENT 'pdk单日赢分最高',
  `record2` int(10) NOT NULL DEFAULT '0' COMMENT 'pdk单日全关别人最多',
  `record3` int(10) NOT NULL DEFAULT '0' COMMENT 'pdk单日输分最多',
  `record4` int(10) NOT NULL DEFAULT '0' COMMENT 'pdk单日炸弹最多',
  `record5` int(10) NOT NULL DEFAULT '0' COMMENT 'pdk单日组局游戏最多',
  `record6` int(10) NOT NULL DEFAULT '0' COMMENT 'pdk单日飞机最多',
  `record7` int(10) NOT NULL DEFAULT '0' COMMENT 'pdk单日被全关最多',
  `record8` int(10) NOT NULL DEFAULT '0' COMMENT 'mj单日赢分最高',
  `record9` int(10) NOT NULL DEFAULT '0' COMMENT 'mj单日自摸最多',
  `record10` int(10) NOT NULL DEFAULT '0' COMMENT 'mj单日输分最多',
  `record11` int(10) NOT NULL DEFAULT '0' COMMENT 'mj单日放炮最多',
  `record12` int(10) NOT NULL DEFAULT '0' COMMENT 'mj单日组局游戏最多',
  `record13` int(10) NOT NULL DEFAULT '0' COMMENT 'mj单日最多杠',
  `record14` int(10) NOT NULL DEFAULT '0' COMMENT 'mj单日点杠最多',
  `rewardRecord` varchar(255) DEFAULT NULL COMMENT '领奖记录',
  PRIMARY KEY (`recordDate`,`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

CREATE TABLE `roomgold_consume_statistics` (
  `consumeDate` date NOT NULL COMMENT '消耗时期',
  `commonGold` int(11) DEFAULT NULL COMMENT '付费金币数',
  `freeGold` int(11) DEFAULT NULL COMMENT '免费金币数',
  `freeGoldSum` int(11) DEFAULT '0' COMMENT '免费金币剩余总数',
  `commonGoldSum` int(11) DEFAULT '0' COMMENT '收费金币剩余总数',
  PRIMARY KEY (`consumeDate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/* ------hyz gslogin数据库更新  2017-11-10 代开表*/
ALTER TABLE daikai_table add COLUMN `assisCreateNo` VARCHAR (255) DEFAULT NULL COMMENT '群助手创房编号';
ALTER TABLE daikai_table add COLUMN `assisGroupNo` VARCHAR (255) DEFAULT NULL COMMENT '群助手创房群编号';

/*-----------------------------刘平 2017-11-2 陇南摆叫麻将 玩家牌局返利活动记录 */
CREATE TABLE `activity_game_rebate` (
  `userId` bigint(20) DEFAULT NULL COMMENT '玩家ID',
  `name` varchar(60) DEFAULT NULL COMMENT '昵称',
  `wanfaId` int(11) DEFAULT NULL COMMENT '玩法ID',
  `number` int(11) DEFAULT NULL COMMENT '达标局数',
  `gameTime` datetime DEFAULT NULL COMMENT '游戏时间',
  `payBindId` int(8) DEFAULT '0' COMMENT '邀请码ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*------------------------- gslogin数据库更新 操作*/
CREATE TABLE `t_action` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `actionType` varchar(20) NOT NULL COMMENT '操作类型（0代理操作，1正常消耗）',
  `actionNo` varchar(32) NOT NULL COMMENT '操作序号（代理操作建议存日期，正常消耗建议存房间id加局数）',
  `actionContent` varchar(256) NOT NULL COMMENT '操作内容',
  `beforeContent` varchar(256) DEFAULT NULL COMMENT '操作之前的值',
  `afterContent` varchar(256) DEFAULT NULL COMMENT '操作之后的值',
  `contentType` varchar(20) NOT NULL COMMENT '操作内容 的 类型',
  `userId` varchar(32) NOT NULL COMMENT '被操作的人',
  `actionUser` varchar(32) NOT NULL COMMENT '操作人',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `groupId` int(10) NOT NULL,
  PRIMARY KEY (`keyId`),
  KEY `idx` (`userId`,`contentType`,`actionType`,`createdTime`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*------------------------- gslogin数据库更新*/
ALTER TABLE user_firstmyth add COLUMN `gameType` int(10) NOT NULL DEFAULT 0 COMMENT '游戏类型';
ALTER TABLE user_firstmyth add COLUMN `groupId` bigint(20) NOT NULL DEFAULT 0 COMMENT '军团ID';
ALTER TABLE `user_firstmyth` DROP PRIMARY KEY ,ADD PRIMARY KEY (`recordDate`, `userId`, `gameType`, `groupId`);

/*----hyz gslogin数据库更新*/
ALTER TABLE `room`
MODIFY COLUMN `players`  varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '加入玩家id';

/*----hyz gslogin数据库更新 2017-12-23 活动接口使用 可不添加*/
CREATE TABLE `activity` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `beginTime` datetime NOT NULL COMMENT '活动开始时间',
  `endTime` datetime NOT NULL COMMENT '活动结束时间',
  `them` varchar(30) DEFAULT NULL COMMENT '活动主题',
  `showContent` text COMMENT '活动详细描述',
  `extend` varchar(255) DEFAULT NULL COMMENT '扩展内容，可以逗号分隔',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

/*----lz 登录数据库更新 2018-01-08 t_third_relation现网必须要更新，有军团t_group_table要更新*/
CREATE TABLE `t_third_relation` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT,
  `userId` bigint(19) NOT NULL COMMENT '玩家ID',
  `thirdPf` varchar(32) NOT NULL COMMENT '第三方平台标识',
  `thirdId` varchar(256) NOT NULL DEFAULT '' COMMENT '第三方平台玩家Id',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `checkedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '检查时间',
  `currentState` varchar(2) NOT NULL DEFAULT '1' COMMENT '是否有效',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_third` (`thirdId`,`thirdPf`),
  KEY `idx_userId` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='第三方关系表';

ALTER TABLE `t_group_table`
ADD COLUMN `userId`  varchar(20) NULL COMMENT '创建人' AFTER `maxCount`,
ADD INDEX `idx_user` (`groupId`, `userId`, `currentState`) ;

/*---hyz 数据库更新 2018-01-15 有军团必须更新*/
ALTER TABLE t_table_user ADD COLUMN `isWinner` smallint(1) DEFAULT '0' COMMENT '是否大赢家';

/*----lz 登录数据库更新 2018-01-15*/
ALTER TABLE `t_table_record` ADD INDEX `idx_time` (`createdTime`, `groupId`) ;

/*----qr 登录数据库更新 2018-01-15彩票点击统计*/
CREATE TABLE `t_lottery_statistics` (
  `recordDate` int(10) NOT NULL DEFAULT '0' COMMENT '记录日期',
  `userId` bigint(20) NOT NULL DEFAULT '0' COMMENT '玩家ID',
  `userName` varchar(60) NOT NULL DEFAULT '' COMMENT '玩家名',
  `record1` int(10) NOT NULL DEFAULT '0' COMMENT '点彩票',
  `record2` int(10) NOT NULL DEFAULT '0' COMMENT '点下载APP',
  `record3` int(10) NOT NULL DEFAULT '0' COMMENT '兑换成功',
  PRIMARY KEY (`recordDate`,`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*---hyz 解散房间表 2018-01-24*/
CREATE TABLE `un_room_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `roomId` bigint(20) NOT NULL DEFAULT '0' COMMENT '房间id',
  `agencyId` bigint(20) DEFAULT '0' COMMENT '代理邀请码',
  `serverId` int(11) DEFAULT '0' COMMENT '服务器id',
  `players` varchar(128) DEFAULT NULL COMMENT '加入玩家id',
  `createTime` datetime DEFAULT NULL COMMENT '解散房间时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

/*---lizhou 数据统计表 2018-01-30*/
CREATE TABLE `t_data_statistics` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dataDate` bigint(12) NOT NULL COMMENT '统计日期(最多可按分钟统计)',
  `dataCode` varchar(32) NOT NULL COMMENT '数据标识',
  `userId` varchar(20) NOT NULL COMMENT '玩家ID',
  `gameType` varchar(20) NOT NULL COMMENT '玩法',
  `dataType` varchar(20) NOT NULL COMMENT '数据类别',
  `dataValue` int(10) NOT NULL COMMENT '统计结果',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_unique` (`dataDate`,`dataCode`,`dataType`,`userId`) USING BTREE,
  KEY `idx_common` (`dataCode`,`dataType`,`dataDate`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8
;
/*---qinran 活动领取记录 2018-02-06*/
CREATE TABLE `activity_reward` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `activityId` int(10) NOT NULL DEFAULT '0' COMMENT '活动ID',
  `userId` bigint(19) NOT NULL DEFAULT '0' COMMENT '玩家ID',
  `type` int(10) NOT NULL DEFAULT '0' COMMENT '类型 1钻石2现金红包',
  `state` int(10) NOT NULL DEFAULT '0' COMMENT '状态 1已领取',
  `rewardIndex` int(10) NOT NULL DEFAULT '0' COMMENT '奖励',
  `rewardDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `reward` varchar(100) DEFAULT '' COMMENT '奖励内容',
  `rewardNum` int(10) NOT NULL DEFAULT '0' COMMENT '奖励数',
  PRIMARY KEY (`keyId`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

/*---lz 活动领取记录 2018-02-25*/

CREATE TABLE `t_user_extend` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT,
  `userId` varchar(20) NOT NULL COMMENT '用户ID',
  `msgType` smallint(5) NOT NULL COMMENT '信息类别（可用于分区）',
  `msgKey` varchar(32) NOT NULL COMMENT '信息key',
  `msgValue` varchar(256) DEFAULT NULL COMMENT '信息vlue',
  `msgDesc` varchar(256) DEFAULT NULL COMMENT '描述（备用）',
  `msgState` varchar(2) NOT NULL COMMENT '状态（0不可用，1：可用）',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modifiedTime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_unique` (`userId`,`msgType`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='用户扩展信息';

/* hyz 动态展示大厅代理信息*/
CREATE TABLE `agency_show` (
  `keyId` int(11) NOT NULL AUTO_INCREMENT,
  `weixin_name` varchar(255) DEFAULT NULL COMMENT '微信名',
  PRIMARY KEY (`keyId`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

ALTER TABLE t_group_table ADD COLUMN `playedBureau` int(2) DEFAULT '0' COMMENT '实际打的局数';
ALTER TABLE t_group_table ADD COLUMN `players` varchar(255) DEFAULT NULL COMMENT '玩家的名称';
ALTER TABLE t_group_table ADD COLUMN `overTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '结束时间';

ALTER TABLE `t_gold_user`
ADD COLUMN `regTime`  timestamp NULL DEFAULT NULL COMMENT '注册时间',
ADD COLUMN `lastLoginTime`  timestamp NULL DEFAULT NULL COMMENT '最后登录时间';

/*积分钻石兑换信息表*/
CREATE TABLE `t_item_exchange` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userId` varchar(20) NOT NULL COMMENT '玩家Id',
  `itemType` varchar(20) NOT NULL COMMENT '商品类别',
  `itemId` varchar(20) NOT NULL COMMENT '商品id',
  `itemName` varchar(64) NOT NULL COMMENT '商品名称',
  `itemAmount` int(10) NOT NULL COMMENT '商品价值',
  `itemCount` int(10) NOT NULL COMMENT '商品数量',
  `itemGive` int(10) NOT NULL COMMENT '商品赠送数量',
  `itemMsg` varchar(1024) NOT NULL COMMENT '商品详细信息',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`keyId`),
  KEY `idx1` (`userId`,`itemType`,`createdTime`),
  KEY `idx2` (`createdTime`,`itemType`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='商品兑换信息表';

/*20180330*/
CREATE TABLE `t_resources_configs` (
  `keyId` int(9) NOT NULL AUTO_INCREMENT,
  `msgType` varchar(32) NOT NULL,
  `msgKey` varchar(64) NOT NULL,
  `msgValue` varchar(256) NOT NULL,
  `msgDesc` varchar(64) NOT NULL,
  `configTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_unique` (`msgType`,`msgKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='配置信息表';
/*201807181102*/
ALTER TABLE `user_inf` ADD COLUMN `preLoginTime`  timestamp NULL DEFAULT NULL COMMENT '上一次登陆时间（与logTime不在同一天）';
/*201807181102 cvs等配置文件*/
CREATE TABLE `t_base_config` (
  `keyId` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `msgType` varchar(32) CHARACTER SET utf8mb4 NOT NULL COMMENT '类型',
  `msgValue` varchar(256) CHARACTER SET utf8mb4 NOT NULL COMMENT '值(json数组)',
  PRIMARY KEY (`keyId`),
  KEY `idx_type` (`msgType`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*201807181102 俱乐部匹配模式*/
CREATE TABLE `t_group_match` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT,
  `groupCode` varchar(32) NOT NULL,
  `userId` varchar(20) NOT NULL,
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_userId` (`userId`),
  KEY `idx_group` (`groupCode`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8;


/*201807211100芒果跑得快排行榜配置*/
INSERT INTO `t_resources_configs` (msgType, msgKey, msgValue, msgDesc, configTime) VALUES ('ServerConfig', 'rank_refresh', '1', '排行榜刷新开关', '2018-7-4 12:34:44');
INSERT INTO `t_resources_configs` (msgType, msgKey, msgValue, msgDesc, configTime) VALUES ('ServerConfig', 'rank_limit_num', '50', '排行榜上榜人数', '2018-7-4 12:34:44');

/*201807211100芒果跑得快七天登陆奖励配置*/
INSERT INTO `t_resources_configs` (msgType, msgKey, msgValue, msgDesc, configTime) VALUES ('ServerConfig', 'seven_sign_reward', '500,1000,1500,2000,2500,3000,20_11_70;31_10_29;41_10_1', '七日签到活动奖励配置', '2018-7-5 12:34:44');

/*201807211100小甘瓜分现金红包活动配置*/
INSERT INTO `t_resources_configs` (msgType, msgKey, msgValue, msgDesc, configTime) VALUES ('ServerConfig', 'red_bag_config', '5000,2018-7-11,2018-7-22,2018-7-11,2018-7-26,12:00:00,23:59:59,领取时间12:00:00-23:59:59,1、活动时间7.11-7.22，提现时间11日00:00-26日00:00;2、每日登陆可转动转盘领取红包一个，红包累计金额满5元才能提现;3、每日玩牌4局以上（含4局）可再领取红包一个;', '瓜分红包', '2018-7-5 12:34:44');
INSERT INTO `t_resources_configs` (msgType, msgKey, msgValue, msgDesc, configTime) VALUES ('ServerConfig', 'red_bag_reward_diamond_grade', '1:30,2:60,3:90,4:120,5:160', '瓜分红包', '2018-7-4 12:34:44');
INSERT INTO `t_resources_configs` (msgType, msgKey, msgValue, msgDesc, configTime) VALUES ('ServerConfig', 'red_bag_reward_grades', '0.18,0.28,0.38,0.58,0.68,0.88,1.28,1.58,1.88,2.88,8.88,18.88', '瓜分红包', '2018-7-4 12:34:44');
INSERT INTO `t_resources_configs` (msgType, msgKey, msgValue, msgDesc, configTime) VALUES ('ServerConfig', 'red_bag_reward_grade_login', '1:0.68:1.28,2:0.18:0.38,3:0.18:0.38,4:0.18:0.38,5:0.18:0.38,6:0.18:0.38,7:0.18:0.38', '瓜分红包', '2018-7-4 12:34:44');
INSERT INTO `t_resources_configs` (msgType, msgKey, msgValue, msgDesc, configTime) VALUES ('ServerConfig', 'red_bag_reward_grade_game', '1:0.58:1.28,2:0.28:0.88,3:0.28:0.88,4:0.28:0.88,5:0.28:0.88,6:0.28:0.88,7:0.58:1.28', '瓜分红包', '2018-7-4 12:34:44');

/*201807211100增加客服号配置*/
ALTER TABLE `t_resources_configs` MODIFY COLUMN `msgValue` text NOT NULL;
INSERT INTO `t_resources_configs` (msgType, msgKey, msgValue, msgDesc, configTime) VALUES ('ServerConfig', 'weixin_keFuHao', '陇南客服号_xiaogankefu008;兰州二报_lzmj166;兰州翻金_lzmj166;酒泉客服号_xiaogankefu888;武威客服号_wwmj222;陇西客服号_lxmj020&lxmj021;平凉客服号_plmj066;二报客服号_gsmj088;秦安客服号_qamj066&qamj088;滑水客服号_qymj066;咣咣客服号_mxmj066&mxmj088;客服微信号_xiaogankefu001&xiaogankefu002;张掖客服号_xiaogankefu020&xiaogankefu021;会牌客服号_xiaogankefu030&xiaogankefu031;微信公众号_小甘游戏;', '客服号填写格式：客服号_客服名&客服名;', '2018-07-24 10:30:17');

/*201807251100增加芒果跑得快段位配置*/
ALTER TABLE `t_gold_user`
ADD COLUMN `grade`  int(9) NULL DEFAULT 0 COMMENT '芒果跑得快段位',
ADD COLUMN `gradeExp`  int(9) NULL DEFAULT 0 COMMENT '芒果跑得快段位经验值';

/*201807251100增加芒果跑得快段位配置   游戏后台数据库system_common_info表执行*/
insert into system_common_info values ("mangguoResetJiFenTime", "2018-08-11 00:00:00");
insert into system_common_info values ("isMangGuoJiFenReset", "0");

/*201807251100增加芒果跑得快积分日志*/
CREATE TABLE `t_jifen_recordlog` (
  `id` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userId` varchar(20) NOT NULL COMMENT '玩家Id',
  `jifen` int(10) NOT NULL COMMENT '获得积分数',
  `sourceType` int(10) NOT NULL COMMENT '获得积分来源',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;

/*201807251100增加芒果跑得快渠道授权表*/
CREATE TABLE `mangguo_authorization` (
  `unionId` varchar(255) NOT NULL DEFAULT '' COMMENT 'unionId',
  `pf` varchar(255) DEFAULT NULL COMMENT '芒果渠道名称',
  `createTime` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`unionId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

/*201807261100 玩家用户表增加channel字段（渠道来源 芒果跑得快）*/
ALTER TABLE `user_inf`
ADD COLUMN `channel` varchar(255) NULL COMMENT '渠道来源 芒果跑得快';

/*201808041100 增加现金红包记录*/
CREATE TABLE `redbag_info` (
  `userId` bigint(20) NOT NULL COMMENT '玩家ID',
  `redBagType` int(11) DEFAULT 0 COMMENT '红包类型 1钻石 2现金红包',
  `redbag` float(11,2) DEFAULT '0.00' COMMENT '领取的红包金额',
  `receiveDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取红包时间',
  `drawDate` timestamp NULL DEFAULT NULL COMMENT '提现时间',
  `sourceType` int(11) DEFAULT NULL COMMENT '红包来源',
  `sourceTypeName` varchar(60) NOT NULL COMMENT '红包来源名',
  PRIMARY KEY (`userId`,`redBagType`,`receiveDate`),
  KEY `user_date` (`userId`,`redBagType`,`receiveDate`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='现金红包记录';

/*201808041100 增加小甘瓜分现金红包记录*/
CREATE TABLE `activity_redbag` (
  `userId` bigint(20) NOT NULL COMMENT '玩家ID',
  `receiveDate` varchar(20) NOT NULL COMMENT '领取红包时间 yyyy-mm-dd',
  `gameNum` int(11) DEFAULT NULL COMMENT '今日玩牌局数',
  `receiveNum` int(11) DEFAULT NULL COMMENT '当天已领取红包次数',
  `receiveRecords` text COMMENT '玩家当天红包领取记录',
  `loginRedBag` float(11,2) DEFAULT '0.00' COMMENT '登陆红包',
  `gameRedBag` float(11,2) DEFAULT '0.00' COMMENT '打牌红包',
  `lastReceiveTime` timestamp NULL DEFAULT NULL COMMENT '最后登录时间',
  PRIMARY KEY (`userId`,`receiveDate`),
  KEY `user_date` (`userId`,`receiveDate`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='瓜分现金红包活动记录(玩家数据)  KEY==>玩家ID_领取红包时间';

CREATE TABLE `t_redbag_system` (
  `createdTime` datetime DEFAULT NULL COMMENT '创建奖池时间',
  `dayPoolNum` float(11,2) DEFAULT NULL COMMENT '现金红包奖池(每日凌晨0点重置)',
  `receiveRecords` text COMMENT '现金红包领取记录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='瓜分现金红包活动系统数据';

/**比赛场**/
CREATE TABLE `t_match` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `serverId` int(9) NOT NULL COMMENT '服ID',
  `tableMsg` varchar(256) NOT NULL COMMENT '创房参数',
  `matchType` varchar(32) NOT NULL COMMENT '类别',
  `tableCount` int(3) NOT NULL COMMENT '房间人数',
  `matchProperty` varchar(20) NOT NULL COMMENT '1：人满即开，时间：定时开',
  `matchRule` varchar(128) NOT NULL COMMENT '竞技规则：（eg：36_30;1_1_21,2_3_12,3_3_6,4_3_3,5_3_1）',
  `currentCount` int(5) NOT NULL COMMENT '当前人数',
  `minCount` int(5) NOT NULL COMMENT '最少人数',
  `maxCount` int(5) NOT NULL COMMENT '最大人数',
  `currentState` varchar(8) NOT NULL COMMENT '当前状态（0，未开始，1_*正在进行第几场，2已结束，3未开局过期结束）',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `finishedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '完成时间',
  `matchPay` varchar(32) NOT NULL DEFAULT '0' COMMENT '报名费',
  `matchName` varchar(64) DEFAULT NULL COMMENT '比赛场名称',
  `matchDesc` varchar(256) DEFAULT NULL COMMENT '描述信息',
  `restTable` int(4) NOT NULL DEFAULT '0' COMMENT '剩下的桌数',
  `matchExt` varchar(1024) DEFAULT NULL COMMENT '扩展信息',
  `startTime` bigint(19) NOT NULL DEFAULT '0' COMMENT '比赛开始时间',
  PRIMARY KEY (`keyId`),
  KEY `idx_common` (`matchType`,`currentState`),
  KEY `idx_server` (`serverId`,`currentState`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `t_match_user` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `matchId` bigint(19) NOT NULL COMMENT '比赛场Id',
  `matchType` varchar(32) NOT NULL COMMENT '类别',
  `userId` varchar(20) NOT NULL COMMENT '玩家Id',
  `currentState` varchar(8) NOT NULL COMMENT '当前状态(0准备、1开局、2结束、3被解散、4出局)',
  `currentNo` int(3) NOT NULL DEFAULT '0' COMMENT '当前局数',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifiedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `currentScore` int(10) NOT NULL DEFAULT '0' COMMENT '分数',
  `userRank` int(4) NOT NULL DEFAULT '0' COMMENT '排名',
  `userAward` varchar(256) CHARACTER SET utf8mb4 DEFAULT '' COMMENT '奖励信息',
  `awardState` varchar(8) NOT NULL DEFAULT '0' COMMENT '奖励领取状态（0无奖励，1未领取，2已领取）',
  `reliveCount` int(2) DEFAULT '0' COMMENT '复活次数',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_unique` (`matchId`,`userId`),
  KEY `idx_userId` (`userId`,`currentState`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

/**玩家房卡获取/消耗日志记录**/
CREATE TABLE `user_card_record` (
  `id` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userId` bigint(20) NOT NULL COMMENT '玩家ID',
  `freeCard` int(11) NOT NULL COMMENT '玩家当前免费房卡数',
  `cards` int(11) NOT NULL COMMENT '玩家当前房卡数',
  `addFreeCard` int(11) NOT NULL COMMENT '玩家本次操作(消耗/获得)免费房卡数',
  `addCard` int(11) NOT NULL COMMENT '玩家本次操作(消耗/获得)免费房卡数',
  `recordType` int(1) NOT NULL COMMENT '操作类型(1消耗  0获得)',
  `playType` int(11) NOT NULL COMMENT '操作所属玩法ID 0表示不属于玩法类操作',
  `sourceType` int(11) NOT NULL DEFAULT '0' COMMENT '操作来源',
  `sourceName` varchar(100) DEFAULT NULL COMMENT '操作来源名',
  `createTime` datetime DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`userId`,`createTime`),
  KEY `idx_date` (`createTime`,`sourceType`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

/**海外百人玩法**/
ALTER TABLE table_inf MODIFY COLUMN `players` text DEFAULT NULL COMMENT '玩家信息';

/**海外龙虎斗玩家日志记录**/
CREATE TABLE `t_lhd_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `userId` bigint(20) NOT NULL COMMENT '玩家ID',
  `betInfo` text COMMENT '下注信息',
  `betGold` int(11) NOT NULL COMMENT '下注金额',
  `result` tinyint(3) NOT NULL COMMENT '对局结果 1龙 2虎 3和',
  `winGold` int(11) NOT NULL DEFAULT '0' COMMENT '输赢金币数',
  `createTime` datetime DEFAULT NULL COMMENT '时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1185 DEFAULT CHARSET=utf8;

/**user_inf 增加非金币场房间总局数统计**/
ALTER TABLE `user_inf`
ADD COLUMN `totalBureau` bigint(20) NULL DEFAULT 0 COMMENT '非金币场房间总局数';
update `user_inf` set `totalBureau` = `totalCount`;

/** 信用分**/
ALTER TABLE `t_group_user`
ADD COLUMN `credit` int(11) NULL DEFAULT 0 COMMENT '玩家信用分值' AFTER `userGroup`;

ALTER TABLE `t_group_table`
ADD COLUMN `type` int(11) NULL DEFAULT 1 COMMENT '房间类型：1：普通房，2：信用房' AFTER `tableId`;

ALTER TABLE `user_playlog`
ADD COLUMN `type` int(11) NULL DEFAULT 1 COMMENT '房间类型：1：普通房，2：信用房' AFTER `maxPlayerCount`;

CREATE TABLE `t_group_credit_log` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) DEFAULT '0' COMMENT '俱乐部id',
  `userId` bigint(11) DEFAULT '0' COMMENT '用户id',
  `optUserId` bigint(11) DEFAULT '0' COMMENT '操作员id：默认为0',
  `tableId` bigint(20) DEFAULT '0' COMMENT '牌桌id',
  `credit` int(11) DEFAULT '0' COMMENT '信用分值',
  `curCredit` int(11) DEFAULT NULL COMMENT '操作后信用分',
  `type` int(11) DEFAULT NULL COMMENT '类型：1：管理加减分，2：佣金，3：牌局',
  `flag` int(11) DEFAULT '0' COMMENT '是否有效：0否，1是',
  `createdTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`keyId`),
  KEY `groupId_credit` (`groupId`,`credit`,`userId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=141 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

ALTER TABLE `t_table_user`
ADD COLUMN `winLoseCredit` int(10) NULL DEFAULT 0 COMMENT '胜负信用分' AFTER `playResult`,
ADD COLUMN `commissionCredit` int(10) NULL DEFAULT 0 COMMENT '信用分佣金' AFTER `winLoseCredit`;

ALTER TABLE `user_inf` add COLUMN `photo` VARCHAR(255) NULL COMMENT '玩家个人相册';

-- 打筒子快乐四喜玩法
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player4_pay0_600', '10', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player4_pay1_600', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player4_pay3_600', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player3_pay0_600', '10', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player3_pay1_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player3_pay3_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player2_pay0_600', '15', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player2_pay1_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player2_pay3_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player4_pay0_1000', '15', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player4_pay1_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player4_pay3_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player3_pay0_1000', '20', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player3_pay1_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player3_pay3_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player2_pay0_1000', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player2_pay1_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type210_count30_player2_pay3_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player4_pay0_600', '10', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player4_pay1_600', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player4_pay3_600', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player3_pay0_600', '10', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player3_pay1_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player3_pay3_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player2_pay0_600', '15', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player2_pay1_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player2_pay3_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player4_pay0_1000', '15', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player4_pay1_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player4_pay3_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player3_pay0_1000', '20', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player3_pay1_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player3_pay3_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player2_pay0_1000', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player2_pay1_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type211_count30_player2_pay3_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player4_pay0_600', '10', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player4_pay1_600', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player4_pay3_600', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player3_pay0_600', '10', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player3_pay1_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player3_pay3_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player2_pay0_600', '15', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player2_pay1_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player2_pay3_600', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player4_pay0_1000', '15', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player4_pay1_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player4_pay3_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player3_pay0_1000', '20', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player3_pay1_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player3_pay3_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player2_pay0_1000', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player2_pay1_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs` (msgType,msgKey,msgValue,msgDesc) VALUES ('PayConfig', 'pay_type212_count30_player2_pay3_1000', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');

-- 信用分佣金分成模式
ALTER TABLE `t_group_relation`
ADD COLUMN `creditCommissionRate` int(11) NULL DEFAULT 0 COMMENT '信用分佣金分成模式,数据范围(0-10):1代表分成比例为小组长10%,群主90%,' AFTER `teamGroup`;

INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('ServerConfig', 'admin_invite_by_id', '1', '管理员通过id邀请玩家进群');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('ServerConfig', 'admin_diss_all_table', '1', '管理员解散所有房间');


-- 是否是打筒子app
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('ServerConfig', 'isDtzApp', '1', '是否是打筒子app');
-- 新活动
INSERT INTO `t_base_config`(`msgType`, `msgValue`) VALUES ('ActivityConfigConfig', '["24","呼朋唤友","1","1","-1","1","2018-11-27 00:00:00","2038-10-3 23:59:59","2_4_6","8_10_2018-11-29 23:59:59","","17","1"]');
INSERT INTO `t_base_config`(`msgType`, `msgValue`) VALUES ('ActivityConfigConfig', '["25","老玩家回归","1","1","-1","2","2018-11-27 00:00:00","2038-10-3 23:59:59","15;10;7;20","1_1_10;2_1_20;3_1_30;4_1_30;5_1_30;6_1_30;7_2_0|1_2_1.88_200;2_2_2.88_200;3_2_3.88_200;4_2_5.88_150;5_2_6.88_100;6_2_7.88_100;7_2_8.88_50","","25","1"]');
INSERT INTO `t_base_config`(`msgType`, `msgValue`) VALUES ('ActivityConfigConfig', '["26","新人礼包","1","1","-1","3","2018-11-27 00:00:00","2038-10-3 23:59:59","1;2|2;1|3;3|4;1|5;1&1;10|2;10|3;20|4;20|5;20&7","20_1_20;50_1_30;100_1_50;160_2_10","","26","1"]');



-- 20181123 小组日志
ALTER TABLE `t_group_credit_log`
ADD COLUMN `userGroup` int(11) NULL DEFAULT -1 COMMENT '小组id' AFTER `flag`;
ALTER TABLE `t_table_user`
ADD COLUMN `userGroup` int(11) NULL DEFAULT -1 COMMENT '小组id' AFTER `isWinner`;

ALTER TABLE `t_group_credit_log`
ADD COLUMN `mode` int(11) NULL DEFAULT 0 COMMENT '是否正向数据:optUserId主动操作userId为正向' AFTER `userGroup`;
--分成比例由原在的1表示10%变为10表示10%,现在存的是实际百分比
update t_group_relation set creditCommissionRate = creditCommissionRate*10 where creditCommissionRate > 0;


-- 新增分成模式字段，1为分成模式A，2为分成模式B，3为分成模式C
ALTER TABLE `t_group`
ADD COLUMN `leaderSharingModel`  int(3) NOT NULL DEFAULT 1 COMMENT '分成模式，1为分成模式A，2为分成模式B，3为分成模式C' AFTER `groupMode`;
ALTER TABLE `t_group`
ADD COLUMN `groupExtConfig`  varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '赢家的分成模式以及比例,json格式' AFTER `leaderSharingModel`;
ALTER TABLE `t_group_user`
ADD COLUMN `creditPurse`  int(11) NULL DEFAULT 0 COMMENT '零钱包' AFTER `credit`;
ALTER TABLE `t_group_user`
ADD COLUMN `creditTotal`  int(11) NULL DEFAULT 0 COMMENT '总收入积分' AFTER `creditPurse`;

--俱乐部房间 添加表示房间名的字段 tableName
ALTER TABLE `t_group_table` ADD COLUMN `tableName` varchar(255) DEFAULT '' COMMENT '房间名' AFTER `tableId`;

-- 安化俱乐部多玩法 实时局数统计
ALTER TABLE `t_group_table`
ADD COLUMN `dealCount`  int(11) NULL DEFAULT 0 COMMENT '发牌次数' AFTER `playedBureau`;
-- 安化牌桌局数同步  注意 注意 注意  更新这个字段时 由于overTime会根据时间搓更新 也会更新到当前时间 会影响战绩的正常显示
update t_group_table set dealCount = playedBureau where currentState in("0","1");

-- 打筒子俱乐部优化
INSERT INTO t_resources_configs ( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('ServerConfig', 'group_hall_all_tables', '1', '俱乐部大厅显示所有桌子');

-- 分享名片功能
CREATE TABLE `user_group_playlog` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `tableid` int(20) DEFAULT NULL COMMENT '房号',
  `userid` int(20) DEFAULT NULL COMMENT '创建人ID',
  `groupid` int(20) DEFAULT NULL COMMENT '所属亲友圈',
  `players` varchar(255) DEFAULT NULL COMMENT '玩家id',
  `count` int(20) DEFAULT NULL COMMENT '总局数',
  `score` varchar(255) DEFAULT NULL COMMENT '得分',
  `creattime` varchar(20) DEFAULT NULL COMMENT '创建房时间',
  `overtime` varchar(255) DEFAULT NULL COMMENT '结束时间',
  `playercount` int(11) DEFAULT NULL,
  `gamename` varchar(25) DEFAULT NULL COMMENT '游戏名',
  `totalCount` int(20) DEFAULT '0' COMMENT '游戏总局数',
  `diFenScore` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '算完底分的分',
  `diFen` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '底分',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=141 DEFAULT CHARSET=utf8;

-- 打筒子智能补房配置，不需要修改的请不要配置
INSERT INTO t_resources_configs ( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('ServerConfig', 'autoCreateTableCountLimit', '2', '俱乐部牌桌少于xx个时智能补房');
INSERT INTO t_resources_configs ( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('ServerConfig', 'autoCreateTableCount', '2', '俱乐部智能补xx个牌桌');

-- 跑得快托管限制
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('ServerConfig', 'autoTimeOutPdkNormal2', '20000', '跑得快普通场托管限制后倒计时时间20秒');

-- 快乐跑胡子增加半边天炸玩法 2人8局和12局
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type131_count12_player2_pay0', '20', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type131_count12_player2_pay1', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type131_count12_player2_pay3', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type131_count8_player2_pay0', '15', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type131_count8_player2_pay1', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type131_count8_player2_pay3', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');


--20190305 大联盟升级
ALTER TABLE t_group_user
ADD COLUMN `promoterLevel` int(5) NOT NULL DEFAULT '0' COMMENT '拉手级别：小组长下一级为1，依次往下，最下级成员为5',
ADD COLUMN `promoterName` varchar(100) NOT NULL DEFAULT '' COMMENT '拉手组名字',
ADD COLUMN `promoterId1` bigint(20) NOT NULL DEFAULT '0' COMMENT '一级推广员id',
ADD COLUMN `promoterId2` bigint(20) NOT NULL DEFAULT '0' COMMENT '二级推广员id',
ADD COLUMN `promoterId3` bigint(20) NOT NULL DEFAULT '0' COMMENT '三级推广员id',
ADD COLUMN `promoterId4` bigint(20) NOT NULL DEFAULT '0' COMMENT '四级推广员id';

ALTER TABLE `t_group`
ADD COLUMN `creditAllotMode` tinyint(1) NULL DEFAULT 1 COMMENT '分成模式：1大赢家分成，2参与分成' AFTER `isCredit`;

ALTER TABLE t_group_credit_log
ADD COLUMN `promoterId1` bigint(20) DEFAULT '0' COMMENT '一级拉手id' AFTER `userGroup`,
ADD COLUMN `promoterId2` bigint(20) DEFAULT '0' COMMENT '二级拉手id' AFTER `promoterId1`,
ADD COLUMN `promoterId3` bigint(20) DEFAULT '0' COMMENT '三级拉手id' AFTER `promoterId2`,
ADD COLUMN `promoterId4` bigint(20) DEFAULT '0' COMMENT '四级拉手id' AFTER `promoterId3`;

ALTER TABLE `t_group_table_config`
ADD COLUMN `creditMsg` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '信用房间配置' AFTER `createdTime`;

CREATE TABLE `t_group_credit_config` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) NOT NULL DEFAULT '0' COMMENT '俱乐部id',
  `preUserId` bigint(20) DEFAULT NULL COMMENT '上级玩家id',
  `userId` bigint(20) NOT NULL DEFAULT '0' COMMENT '玩家id',
  `configId` bigint(20) NOT NULL DEFAULT '0' COMMENT 't_group_table_config.id',
  `credit` int(11) NOT NULL DEFAULT '0' COMMENT 'credit值：固定数值，或百分比比例',
  `maxCreditLog` int(11) DEFAULT '0' COMMENT '记录修改时允许最大分值',
  `createdTime` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `lastUpTime` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`keyId`) USING BTREE,
  KEY `groupId_index` (`groupId`) USING BTREE,
  KEY `preUserId_index` (`preUserId`) USING BTREE,
  KEY `userId_index` (`userId`) USING BTREE,
  KEY `configId_index` (`configId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=85 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--小组下成员为1级
update t_group_user set promoterLevel=1 where userRole = 2 and userGroup !='0';

-- 20190312大联盟升级
ALTER TABLE `t_group_user`
ADD COLUMN `creditCommissionRate` int(10) NOT NULL DEFAULT 0 COMMENT '上级给自己的赠送分分成比例，百分制整数' AFTER `promoterId4`;


ALTER TABLE `t_group_table`
ADD COLUMN `creditMsg` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '信用房间配置' AFTER `players`;

-- 20190320红中麻将的玩法扣钻信息
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count8_player2_pay0', '20', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count8_player2_pay1', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count8_player2_pay3', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count12_player2_pay0', '30', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count12_player2_pay1', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count12_player2_pay3', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count16_player2_pay0', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count16_player2_pay1', '80', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count16_player2_pay3', '80', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count8_player3_pay0', '14', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count8_player3_pay1', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count8_player3_pay3', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count12_player3_pay0', '20', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count12_player3_pay1', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count12_player3_pay3', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count16_player3_pay0', '27', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count16_player3_pay1', '80', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count16_player3_pay3', '80', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count8_player4_pay0', '10', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count8_player4_pay1', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count8_player4_pay3', '40', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count12_player4_pay0', '15', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count12_player4_pay1', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count12_player4_pay3', '60', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count16_player4_pay0', '20', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count16_player4_pay1', '80', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'PayConfig', 'pay_type221_count16_player4_pay3', '80', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');

-- 20190403欢乐金币场分享活动
ALTER TABLE `user_share`
ADD COLUMN `type` int(1) NULL DEFAULT 1 COMMENT '分享类型：1：普通分享（游戏分享），2：金币场分享' AFTER `userId`;

-- 20190410手机绑定及登录相关
alter table user_inf add  phonePw varchar(60)  comment '手机登录密码';
alter table user_inf add  phoneNum varchar(11) default null comment '手机号';
alter table user_inf add unique phone_unique (`phoneNum`);

CREATE TABLE `user_msg_verify`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) NULL DEFAULT NULL,
  `verifyCode` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '短信验证码',
  `sendTime` datetime(0) NULL DEFAULT NULL COMMENT '发送时间',
  `phoneNum` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '\r\n验证手机号',
  `isUse` int(1) NULL DEFAULT 0 COMMENT '是否已经被使用过（验证成功，才会改变其状态值）',
  `ip` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'ip地址',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `ip_unique`(`ip`) USING BTREE,
  UNIQUE INDEX `userId_unique`(`userId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 47 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用于记录短信验证相关信息' ROW_FORMAT = Dynamic;



-- 20190413亲友圈增加公告内容字段
 alter table  `t_group` add  `content` varchar(512) DEFAULT '' COMMENT '公告内容';


-- 20190419 安化亲友圈增加拒绝游戏邀请字段
ALTER TABLE t_group_user ADD COLUMN `refuseInvite` INT(2) DEFAULT '1' COMMENT '是否拒绝亲友圈游戏邀请: 0拒绝 1允许';

-- 2019/04/19安化跑得快游戏配置切牌倒计时配置
INSERT INTO t_resource_config(msgType, msgKey, msgValue, msgDesc) VALUES('ServerConfig','pdkQp_timeout',15000,'跑得快切牌倒计时');

-- 20190424 只仍白金岛项目需要创建
CREATE TABLE `bjd_data_statistics` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dataDate` bigint(12) NOT NULL COMMENT '统计日期(最多可按分钟统计)',
  `dataCode` varchar(32) NOT NULL COMMENT '数据标识',
  `userId` varchar(20) NOT NULL COMMENT '玩家ID',
  `gameType` varchar(20) NOT NULL COMMENT '玩法',
  `dataType` varchar(20) NOT NULL COMMENT '数据类别',
  `dataValue` int(10) NOT NULL COMMENT '统计结果',
  PRIMARY KEY (`keyId`) USING BTREE,
  UNIQUE KEY `idx_unique` (`dataDate`,`dataCode`,`dataType`,`userId`) USING BTREE,
  KEY `idx_common` (`dataCode`,`dataType`,`dataDate`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- 20190424 只仍白金岛项目需要创建
CREATE TABLE `bjd_group_newer_bind` (
  `userId` bigint(11) NOT NULL,
  `groupId` bigint(11) DEFAULT NULL,
  PRIMARY KEY (`userId`),
  KEY `idx_uid` (`userId`) USING BTREE,
  KEY `idx_gid` (`groupId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--20190425 login更新，短信验证签名配置信息
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('ServerConfig', 'smsConfig', '联盛科技', '短信验证配置：签名模板');


--20190506 设置小组分成人数下限
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('ServerConfig', 'groupIncomeNum', '2', '小组参与分成最低人数');

-- 20190508 信用分日志增加包间名字
ALTER TABLE `t_group_credit_log`
ADD COLUMN `roomName` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '包间名字' AFTER `createdTime`;

-- 20190517 战绩记录表增加通用信息字段
ALTER TABLE `user_playlog`
ADD COLUMN `generalExt` text CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT '通用信息：json格式' AFTER `type`;

-- 20190520 玩家增加牌局状态
alter table `user_inf` add COLUMN  `playState` smallint(1) DEFAULT '0' COMMENT '牌桌状态：1开局，0未开局或不在房间内';



-- 20190515 信用分兑换比例
ALTER TABLE `t_group`
ADD COLUMN `creditRate` int(11) NULL DEFAULT 100 COMMENT '信用分兑换比例：10表示1:10' AFTER `creditAllotMode`;

CREATE TABLE `t_group_commission_config` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) NOT NULL DEFAULT '0' COMMENT '俱乐部id',
  `userId` bigint(20) NOT NULL DEFAULT '0' COMMENT '玩家id',
  `seq` int(11) DEFAULT '0' COMMENT '档位序号',
  `minCredit` int(11) DEFAULT NULL COMMENT '区段最小值',
  `maxCredit` int(11) DEFAULT NULL COMMENT '区段最大值',
  `credit` int(11) DEFAULT '0' COMMENT '留给自己的值',
  `leftCredit` int(11) DEFAULT '0' COMMENT '留给下级可配置的最大分',
  `maxCreditLog` int(11) DEFAULT '0' COMMENT '记录修改时允许最大分值',
  `createdTime` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `lastUpTime` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`keyId`) USING BTREE,
  UNIQUE KEY `uniq_index` (`groupId`,`userId`,`seq`) USING BTREE,
  KEY `groupId_index` (`groupId`) USING BTREE,
  KEY `userId_index` (`userId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=97 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

ALTER TABLE `t_group_user`
ADD COLUMN `ext` varchar(500) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '用于记录一些信息，json格式字符串' AFTER `refuseInvite`;

-- 20190610迁服功能
ALTER TABLE `server_config`
ADD COLUMN `tmpGameType` varchar(100) NULL COMMENT '临时可用，但新创建不可用，用于迁移玩法后，旧房间依旧生效' AFTER `serverType`;

-- 20160612亲友圈牌局日志
CREATE TABLE `log_group_table` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `dataDate` bigint(10) NOT NULL COMMENT '日期：格式20160101',
  `groupId` bigint(10) NOT NULL COMMENT '亲友圈id',
  `userGroup` bigint(10) NOT NULL COMMENT '小组id',
  `gameType` int(10) NOT NULL COMMENT '玩法id',
  `bureau` int(10) NOT NULL COMMENT '局数：按分结算的计为分数，如剥皮，打筒子',
  `userId` bigint(10) NOT NULL COMMENT '用户id',
  `player2Count1` int(10) DEFAULT '0' COMMENT '2人于玩法完整大局数',
  `player2Count2` int(10) DEFAULT '0' COMMENT '2人于玩法所有大局数',
  `player2Count3` int(10) DEFAULT '0' COMMENT '2人于玩法所有小局数',
  `player3Count1` int(10) DEFAULT '0' COMMENT '3人于玩法完整大局数',
  `player3Count2` int(10) DEFAULT '0' COMMENT '3人于玩法所有大局数',
  `player3Count3` int(10) DEFAULT '0' COMMENT '3人于玩法所有小局数',
  `player4Count1` int(10) DEFAULT '0' COMMENT '4人于玩法完整大局数',
  `player4Count2` int(10) DEFAULT '0' COMMENT '4人于玩法所有大局数',
  `player4Count3` int(10) DEFAULT '0' COMMENT '4人于玩法所有小局数',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_unique` (`dataDate`,`groupId`,`userGroup`,`gameType`,`bureau`,`userId`) USING BTREE,
  KEY `idx_dataDate` (`dataDate`) USING BTREE,
  KEY `idx_groupId` (`groupId`) USING BTREE,
  KEY `idx_userGroup` (`userGroup`) USING BTREE,
  KEY `idx_gameType` (`gameType`) USING BTREE,
  KEY `idx_userId` (`userId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='亲友圈用户局数日志';

--20190613长沙麻将扣费配置

INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ( 'PayConfig', 'pay_type222_count8_player2_pay0', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:03');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ( 'PayConfig', 'pay_type222_count8_player2_pay1', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:04');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ( 'PayConfig', 'pay_type222_count8_player2_pay3', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:06');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ('PayConfig', 'pay_type222_count4_player2_pay0', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:08');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ( 'PayConfig', 'pay_type222_count4_player2_pay1', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:10');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ( 'PayConfig', 'pay_type222_count4_player2_pay3', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:11');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ( 'PayConfig', 'pay_type222_count8_player3_pay0', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:13');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ( 'PayConfig', 'pay_type222_count8_player3_pay1', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:14');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ( 'PayConfig', 'pay_type222_count8_player3_pay3', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:16');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ('PayConfig', 'pay_type222_count12_player3_pay0', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:17');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ( 'PayConfig', 'pay_type222_count12_player3_pay1', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:19');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ( 'PayConfig', 'pay_type222_count12_player3_pay3', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:20');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ( 'PayConfig', 'pay_type222_count16_player3_pay0', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:22');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ( 'PayConfig', 'pay_type222_count16_player3_pay1', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:24');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ( 'PayConfig', 'pay_type222_count16_player3_pay3', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:25');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ( 'PayConfig', 'pay_type222_count8_player4_pay0', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:27');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ('PayConfig', 'pay_type222_count8_player4_pay1', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:28');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ( 'PayConfig', 'pay_type222_count8_player4_pay3', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:30');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ('PayConfig', 'pay_type222_count4_player4_pay0', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:31');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ( 'PayConfig', 'pay_type222_count4_player4_pay1', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:33');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ( 'PayConfig', 'pay_type222_count4_player4_pay3', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:34');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ('PayConfig', 'pay_type222_count16_player4_pay0', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:37');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ( 'PayConfig', 'pay_type222_count16_player4_pay1', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:38');
INSERT INTO `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ('PayConfig', 'pay_type222_count16_player4_pay3', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量', '2019-06-14 19:45:42');


-- 20190614郴州字牌玩法配置
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count8_player2_pay0', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count8_player2_pay1', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count8_player2_pay3', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count12_player2_pay0', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count12_player2_pay1', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count12_player2_pay3', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count20_player2_pay0', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count20_player2_pay1', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count20_player2_pay3', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count8_player3_pay0', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count8_player3_pay1', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count8_player3_pay3', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count12_player3_pay0', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count12_player3_pay1', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count12_player3_pay3', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count20_player3_pay0', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count20_player3_pay1', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count20_player3_pay3', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count8_player4_pay0', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count8_player4_pay1', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count8_player4_pay3', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count12_player4_pay0', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count12_player4_pay1', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count12_player4_pay3', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count20_player4_pay0', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count20_player4_pay1', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('PayConfig', 'pay_type198_count20_player4_pay3', '0', 'pay_游戏玩法_局数_人数_付费方式_附加信息=耗钻数量');

-- 20190628玩家是否是新玩家
ALTER TABLE `t_table_user`
ADD COLUMN `isNewPlayer` tinyint(1) NULL DEFAULT 0 COMMENT '是否是新玩家' AFTER `userGroup`;


-- 20190701亲友圈牌局日志,增加大赢家数
ALTER TABLE `log_group_table`
ADD COLUMN `dyjCount` int(10) NULL DEFAULT 0 COMMENT '大赢家数' AFTER `player4Count3`;

-- 20190709
CREATE TABLE `playlog_user` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) DEFAULT NULL COMMENT '用户id',
  `logId` bigint(20) DEFAULT NULL COMMENT 'playlog_table.id',
  `createTime` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`keyId`),
  KEY `idx_uid_time` (`userId`,`createTime`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='关于非信用房间的用户数据';

CREATE TABLE `playlog_table` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `tableId` bigint(20) DEFAULT NULL COMMENT '房号',
  `creatorId` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `groupId` bigint(20) DEFAULT NULL COMMENT '所属亲友圈',
  `playerCount` int(1) DEFAULT NULL COMMENT '玩家数',
  `players` varchar(255) DEFAULT NULL COMMENT '玩家id',
  `scores` varchar(255) DEFAULT NULL COMMENT '玩家得分',
  `totalCount` int(20) DEFAULT '0' COMMENT '游戏总局数',
  `finishCount` int(20) DEFAULT NULL COMMENT '总局数',
  `tableMsg` varchar(255) DEFAULT NULL COMMENT '游戏描述信息',
  `overTime` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `createTime` timestamp NULL DEFAULT NULL COMMENT '创建房时间',
  PRIMARY KEY (`keyId`) USING BTREE,
  KEY `idx_overtime` (`overTime`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8 COMMENT='关于非信用房间的房间结算数据';

-- 20190716增群主id
ALTER TABLE `playlog_table`
ADD COLUMN `groupMasterId` bigint(20) NULL DEFAULT 0 COMMENT '亲友圈群主id' AFTER `groupId`;

-- 信用分*100
ALTER TABLE `t_group_user`
MODIFY COLUMN `credit` bigint(20) NULL DEFAULT 0 COMMENT '玩家信用分值' AFTER `userGroup`;

ALTER TABLE `t_group_credit_log`
MODIFY COLUMN `credit` bigint(20) NULL DEFAULT 0 COMMENT '信用分值' AFTER `tableId`,
MODIFY COLUMN `curCredit` bigint(20) NULL DEFAULT 0 COMMENT '操作后信用分' AFTER `credit`;


ALTER TABLE `t_group_commission_config`
MODIFY COLUMN `minCredit` bigint(20) NULL DEFAULT NULL COMMENT '区段最小值' AFTER `seq`,
MODIFY COLUMN `maxCredit` bigint(20) NULL DEFAULT NULL COMMENT '区段最大值' AFTER `minCredit`,
MODIFY COLUMN `credit` bigint(20) NULL DEFAULT 0 COMMENT '留给自己的值' AFTER `maxCredit`,
MODIFY COLUMN `leftCredit` bigint(20) NULL DEFAULT 0 COMMENT '留给下级可配置的最大分' AFTER `credit`,
MODIFY COLUMN `maxCreditLog` bigint(20) NULL DEFAULT 0 COMMENT '记录修改时允许最大分值' AFTER `leftCredit`;


ALTER TABLE `t_table_user`
MODIFY COLUMN `winLoseCredit` bigint(20) NULL DEFAULT 0 COMMENT '胜负信用分' AFTER `playResult`,
MODIFY COLUMN `commissionCredit` bigint(20) NULL DEFAULT 0 COMMENT '信用分佣金' AFTER `winLoseCredit`;

update t_group_user set credit = credit*100;

update t_group_credit_log set credit = credit*100 , curCredit = curCredit*100 , createdTime = createdTime;

update t_group_commission_config set minCredit = minCredit*100,maxCredit = maxCredit*100,credit = credit*100,leftCredit = leftCredit*100,maxCreditLog = maxCreditLog*100;

update t_table_user set winLoseCredit = winLoseCredit * 100,commissionCredit = commissionCredit*100;

-- 20190902
ALTER TABLE `t_group_user`
ADD COLUMN `creditLock` int(1) NULL DEFAULT 0 COMMENT '信用分上锁：0未锁，1锁定' AFTER `ext`;

-- 20190917 俱乐部赠送统计
CREATE TABLE `log_group_commission` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `dataDate` bigint(20) NOT NULL DEFAULT '0' COMMENT '日期：格式20160101',
  `groupId` bigint(20) DEFAULT '0' COMMENT '俱乐部id',
  `userId` bigint(20) DEFAULT '0' COMMENT '用户id',
  `credit` bigint(20) DEFAULT '0' COMMENT '从下级获得的赠送分',
  `commissionCredit` bigint(20) DEFAULT NULL COMMENT '下级产生的赠送分',
  `commissionCount` int(11) DEFAULT '0' COMMENT '下级产生的赠送次数',
  `zjsCount` int(11) DEFAULT '0' COMMENT '下级产生的总大局数',
  `dyjCount` int(11) DEFAULT '0' COMMENT '下级产生的大赢家数',
  `totalPay` int(11) DEFAULT '0' COMMENT '下级产生的消耗',
  `selfWinCredit` bigint(20) DEFAULT '0' COMMENT '自己当天输赢比赛分',
  `selfCommissionCredit` bigint(20) DEFAULT '0' COMMENT '自己产生的赠送分',
  `selfCommissionCount` int(11) DEFAULT '0' COMMENT '自己产生的赠送次数',
  `selfZjsCount` int(11) DEFAULT '0' COMMENT '自己产生的总局数',
  `selfDyjCount` int(11) DEFAULT '0' COMMENT '自己产生的大赢家数',
  `selfTotalPay` int(11) DEFAULT '0' COMMENT '自己产生的消耗',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_uniq` (`dataDate`,`groupId`,`userId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=393 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- 20190919
CREATE TABLE `db_lock` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `lockKey` varchar(100) COLLATE utf8_bin NOT NULL COMMENT '锁的key值',
  `unLockKey` varchar(100) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '结束时间之前释放锁需要提供此key',
  `startTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '开始时间',
  `overTime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '结束时间',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `uniq_key` (`lockKey`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- 20190927 玩家进出俱乐部消息日志
CREATE TABLE `log_group_user_alert` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(10) NOT NULL COMMENT '亲友圈id',
  `userId` bigint(10) NOT NULL DEFAULT '0' COMMENT '直接用户id',
  `optUserId` bigint(10) DEFAULT '0' COMMENT '间接用户id',
  `type` int(10) DEFAULT '1' COMMENT '类型：1、同意邀请，2、同意申请，3、踢出',
  `createdTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`keyId`),
  KEY `idx_groupId` (`groupId`,`createdTime`) USING BTREE,
  KEY `idx_groupId_userId` (`groupId`,`userId`,`createdTime`) USING BTREE,
  KEY `idx_groupId_optUserId` (`groupId`,`optUserId`,`createdTime`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=58 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='亲友圈用户进出俱乐部消息';

-- 手机号加密存储
ALTER TABLE `user_inf`
MODIFY COLUMN `phoneNum` varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机号' AFTER `phonePw`;

-- 20191011 白金岛棋牌更新到字牌圈亲友圈结构
ALTER TABLE `t_group_user`
MODIFY COLUMN `userRole` int(5) NOT NULL COMMENT '1：会长，\r\n2：副会长，\r\n5000：董事，\r\n10000：主管，\r\n20000：管理，\r\n30000：组长，\r\n90000：普通成员' AFTER `inviterId`,
ADD COLUMN `promoterId` bigint(20) NULL DEFAULT 0 COMMENT '直接上级id' AFTER `credit`,
ADD COLUMN `promoterId5` bigint(20) NULL DEFAULT 0 COMMENT '五级推广员id' AFTER `promoterId4`,
ADD COLUMN `promoterId6` bigint(20) NULL DEFAULT 0 COMMENT '六级推广员id' AFTER `promoterId5`,
ADD COLUMN `promoterId7` bigint(20) NULL DEFAULT 0 COMMENT '七级推广员id' AFTER `promoterId6`,
ADD COLUMN `promoterId8` bigint(20) NULL DEFAULT 0 COMMENT '八级推广员id' AFTER `promoterId7`,
ADD COLUMN `promoterId9` bigint(20) NULL DEFAULT 0 COMMENT '九级推广员id' AFTER `promoterId8`,
ADD COLUMN `promoterId10` bigint(20) NULL DEFAULT 0 COMMENT '十级推广员id' AFTER `promoterId9`;

ALTER TABLE `t_group`
ADD COLUMN `switchInvite` tinyint(1) NULL DEFAULT 0 COMMENT '亲友圈邀请人进群是否需要对方同意：0不需要，1需要' AFTER `content`;

ALTER TABLE `t_group_credit_log`
DROP INDEX `index_all`,
DROP INDEX `index_userGroup`,
DROP INDEX `index_createdTime`,
DROP INDEX `index_credit`,
ADD INDEX `index_all`(`groupId`, `createdTime`, `userId`, `type`) USING BTREE,
ADD INDEX `idx_gId_time_uId_type`(`groupId`, `createdTime`, `optUserId`, `type`) USING BTREE,
ADD INDEX `groupId_credit`(`groupId`, `credit`, `userId`) USING BTREE;


CREATE TABLE `t_group_user_log` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `groupId` int(10) NOT NULL COMMENT '军团id',
  `userId` bigint(19) NOT NULL COMMENT '玩家id',
  `credit` bigint(20) DEFAULT '0' COMMENT '玩家信用分值',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`keyId`) USING BTREE,
  UNIQUE KEY `idx_groupId` (`groupId`,`userId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=322 DEFAULT CHARSET=utf8 COMMENT='军团成员被删除后，记录信用分';


-- 20191031
CREATE TABLE `t_group_credit_log_master` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) DEFAULT '0' COMMENT '俱乐部id',
  `userId` bigint(11) DEFAULT '0' COMMENT '用户id',
  `optUserId` bigint(11) DEFAULT '0' COMMENT '操作员id：默认为0',
  `tableId` bigint(20) DEFAULT '0' COMMENT '牌桌id',
  `credit` bigint(20) DEFAULT '0' COMMENT '信用分值',
  `curCredit` bigint(20) DEFAULT '0' COMMENT '操作后信用分',
  `type` int(11) DEFAULT NULL COMMENT '类型：1：管理加减分，2：佣金，3：牌局',
  `flag` int(11) DEFAULT '0' COMMENT '是否有效：0否，1是',
  `userGroup` int(11) DEFAULT '-1' COMMENT '小组id',
  `promoterId1` bigint(20) DEFAULT '0' COMMENT '一级拉手id',
  `promoterId2` bigint(20) DEFAULT '0' COMMENT '二级拉手id',
  `promoterId3` bigint(20) DEFAULT '0' COMMENT '三级拉手id',
  `promoterId4` bigint(20) DEFAULT '0' COMMENT '四级拉手id',
  `mode` int(11) DEFAULT '0' COMMENT '是否正向数据:optUserId主动操作userId为正向',
  `createdTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `roomName` varchar(100) COLLATE utf8_bin DEFAULT '' COMMENT '包间名字',
  PRIMARY KEY (`keyId`),
  KEY `index_all` (`groupId`,`createdTime`,`userId`,`type`) USING BTREE,
  KEY `idx_gId_time_uId_type` (`groupId`,`createdTime`,`optUserId`,`type`) USING BTREE,
  KEY `groupId_credit` (`groupId`,`credit`,`userId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3775 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='该表仅记录群主对自己的信用分操作记录';

-- 20191121 增加跑得快调牌概率表


SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_pdk_config
-- ----------------------------
DROP TABLE IF EXISTS `t_pdk_config`;
CREATE TABLE `t_pdk_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` int(11) DEFAULT '0' COMMENT '配牌条件类型',
  `val` int(11) DEFAULT '0' COMMENT '条件类型值',
  `rate` varchar(64) DEFAULT '' COMMENT '概率',
  `descriptMsg` varchar(24) DEFAULT '' COMMENT '描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_pdk_config
-- ----------------------------
INSERT INTO `t_pdk_config` VALUES ('1', '1', '60', '10;5;10', '赢60分');
INSERT INTO `t_pdk_config` VALUES ('2', '1', '80', '10;5;10', '赢80分');
INSERT INTO `t_pdk_config` VALUES ('3', '1', '100', '30;30;30', '赢100分');
INSERT INTO `t_pdk_config` VALUES ('4', '2', '3', '10;10;10', '连胜3场');
INSERT INTO `t_pdk_config` VALUES ('5', '3', '0', '10;10;10', '整局赢的次数通过公式决定（赢半数+1）');
INSERT INTO `t_pdk_config` VALUES ('6', '4', '40', '10;10;0;0', '输40分');
INSERT INTO `t_pdk_config` VALUES ('7', '4', '60', '10;10;5;5', '输60分');
INSERT INTO `t_pdk_config` VALUES ('8', '4', '80', '30;30;30;10', '输80分');
INSERT INTO `t_pdk_config` VALUES ('9', '5', '3', '10;10;5;0', '连输3把');
INSERT INTO `t_pdk_config` VALUES ('10', '6', '0', '10;10;0;10', '整局输的局数（输半数+1）');

-- 20191202 战绩详情查询赠送信息
ALTER TABLE `t_group_credit_log`
ADD COLUMN `groupTableId` bigint(20) NULL DEFAULT 0 COMMENT 't_group_table.id' AFTER `roomName`;

ALTER TABLE `t_group_credit_log`
ADD INDEX `idx_groupTableId`(`groupTableId`) USING BTREE;



-- 20191105 亲友圈成长
ALTER TABLE `user_inf`
ADD COLUMN `coin` bigint(20) NULL DEFAULT 0 COMMENT '金币' AFTER `playState`,
ADD COLUMN `freeCoin` bigint(20) NULL DEFAULT 0 COMMENT '免费金币' AFTER `coin`,
ADD COLUMN `usedCoin` bigint(20) NULL DEFAULT 0 COMMENT '消耗的金币' AFTER `freeCoin`;

ALTER TABLE `t_group`
ADD COLUMN `switchCoin` tinyint(1) NULL DEFAULT 0 COMMENT '是否开启新金币模式' AFTER `switchInvite`,
ADD COLUMN `exp` bigint(20) NULL DEFAULT 0 COMMENT '当前等级经验' AFTER `switchCoin`,
ADD COLUMN `totalExp` bigint(20) NULL DEFAULT 0 COMMENT '总经验' AFTER `exp`,
ADD COLUMN `level` int(11) NULL DEFAULT 1 COMMENT '当前等级' AFTER `totalExp`,
ADD COLUMN `creditExpToday` bigint(20) NULL DEFAULT 0 COMMENT '玩家每日下分增加经验，零点清0' AFTER `level`,
ADD COLUMN `refreshTimeDaily` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '每日刷新时间' AFTER `creditExpToday`;

ALTER TABLE `t_group_user`
ADD COLUMN `exp` bigint(20) NULL DEFAULT 0 COMMENT '当前等级经验' AFTER `creditLock`,
ADD COLUMN `totalExp` bigint(20) NULL DEFAULT 0 COMMENT '总经验' AFTER `exp`,
ADD COLUMN `level` int(11) NULL DEFAULT 1 COMMENT '当前等级' AFTER `totalExp`,
ADD COLUMN `creditExpToday` bigint(20) NULL DEFAULT 0 COMMENT '玩家每日下分经验，零点清0' AFTER `level`,
ADD COLUMN `refreshTimeDaily` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '每日刷新时间' AFTER `creditExpToday`,
ADD COLUMN `frameId` int(11) NULL DEFAULT 0 COMMENT '头相框id' AFTER `refreshTimeDaily`;

CREATE TABLE `log_group_exp` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) DEFAULT '0',
  `userId` bigint(20) DEFAULT '0',
  `optUserId` bigint(20) DEFAULT '0',
  `tableId` bigint(20) DEFAULT '0',
  `credit` bigint(20) DEFAULT '0',
  `exp` bigint(20) DEFAULT '0',
  `curLevel` int(11) DEFAULT '0',
  `curExp` bigint(20) DEFAULT '0',
  `createdTime` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`keyId`),
  KEY `idx_time_gid_uid` (`createdTime`,`groupId`,`userId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=189 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='亲友圈经验变化表';

CREATE TABLE `log_group_user_exp` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) DEFAULT '0',
  `userId` bigint(20) DEFAULT '0',
  `optUserId` bigint(20) DEFAULT '0',
  `tableId` bigint(20) DEFAULT '0',
  `credit` bigint(20) DEFAULT '0',
  `exp` bigint(20) DEFAULT '0',
  `curLevel` int(11) DEFAULT '0',
  `curExp` bigint(20) DEFAULT '0',
  `createdTime` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`keyId`)
) ENGINE=InnoDB AUTO_INCREMENT=331 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='亲友圈成员经验变化表';

CREATE TABLE `log_group_user_level` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) DEFAULT '0' COMMENT '俱乐部id',
  `userId` bigint(20) DEFAULT '0' COMMENT '用户id',
  `level` int(11) DEFAULT '0' COMMENT '等级',
  `stat` tinyint(1) DEFAULT '1' COMMENT '状态：1可以领取，2已领取',
  `createdTime` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `lastUpTime` timestamp NULL DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `uniq_gid_uid_level` (`groupId`,`userId`,`level`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='亲友圈成员升级记录表';


CREATE TABLE `sys_group_level_config` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `level` int(11) DEFAULT '1' COMMENT '等级',
  `name` varchar(50) COLLATE utf8_bin DEFAULT '' COMMENT '名称',
  `exp` bigint(20) DEFAULT '0' COMMENT '升到下一级所需经验',
  `totalExp` bigint(20) DEFAULT '0' COMMENT '本级所需总经验',
  `creditExpLimit` bigint(20) DEFAULT '0' COMMENT '群主下分每日增加上限',
  `playExp` bigint(20) DEFAULT '0' COMMENT '打一局的经验',
  `memberCount` int(11) DEFAULT '0' COMMENT '最大成员上限',
  `bgList` varchar(255) COLLATE utf8_bin DEFAULT '' COMMENT '解锁的背景列表：格式，id,id,id....',
  `tbList` varchar(255) COLLATE utf8_bin DEFAULT '' COMMENT '解锁的桌子列表：格式，id,id,id....',
  `createdTime` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `lastUpTime` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `uniq_level` (`level`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='亲友圈等级配置表';


CREATE TABLE `sys_group_user_level_config` (
  `keyId` bigint(20) NOT NULL,
  `level` int(11) DEFAULT '1' COMMENT '等级',
  `name` varchar(50) COLLATE utf8_bin DEFAULT '' COMMENT '名称',
  `exp` bigint(20) DEFAULT '0' COMMENT '升到下一级所需经验',
  `totalExp` bigint(20) DEFAULT '0' COMMENT '本级所需总经验',
  `creditExpLimit` bigint(20) DEFAULT '0' COMMENT '玩家下分每日增加上限',
  `playExp` bigint(20) DEFAULT '0' COMMENT '打一局的经验',
  `headimgList` varchar(255) COLLATE utf8_bin DEFAULT '' COMMENT '所有解锁的头像列表，格式，id,id,id.....',
  `goldRate` int(11) DEFAULT '0' COMMENT '商城购买金币时的加成：1%=100，100%=10000',
  `createdTime` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `lastUpTime` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `uniq_level` (`level`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='亲友圈成员等级配置表';


INSERT INTO `sys_group_level_config`(`keyId`, `level`, `name`, `exp`, `totalExp`, `creditExpLimit`, `playExp`, `memberCount`, `bgList`, `tbList`, `createdTime`, `lastUpTime`) VALUES (1, 1, '白钻', 100000, 0, 10000, 10, 500, '1', '1', '2019-11-09 10:49:04', '2019-11-13 10:02:23');
INSERT INTO `sys_group_level_config`(`keyId`, `level`, `name`, `exp`, `totalExp`, `creditExpLimit`, `playExp`, `memberCount`, `bgList`, `tbList`, `createdTime`, `lastUpTime`) VALUES (2, 2, '蓝钻', 3000000, 100000, 10000, 10, 1000, '1,2', '1', '2019-11-09 10:49:49', '2019-12-11 15:42:57');
INSERT INTO `sys_group_level_config`(`keyId`, `level`, `name`, `exp`, `totalExp`, `creditExpLimit`, `playExp`, `memberCount`, `bgList`, `tbList`, `createdTime`, `lastUpTime`) VALUES (3, 3, '黄钻', 6000000, 3100000, 10000, 10, 2000, '1,2,3', '1', '2019-11-09 10:49:49', '2019-12-11 15:43:06');
INSERT INTO `sys_group_level_config`(`keyId`, `level`, `name`, `exp`, `totalExp`, `creditExpLimit`, `playExp`, `memberCount`, `bgList`, `tbList`, `createdTime`, `lastUpTime`) VALUES (4, 4, '红钻', 9000000, 9100000, 10000, 10, 4000, '1,2,3,4', '1', '2019-11-09 10:49:49', '2019-12-11 14:30:39');
INSERT INTO `sys_group_level_config`(`keyId`, `level`, `name`, `exp`, `totalExp`, `creditExpLimit`, `playExp`, `memberCount`, `bgList`, `tbList`, `createdTime`, `lastUpTime`) VALUES (5, 5, '银冠', 18000000, 18100000, 10000, 10, 8000, '1,2,3,4,5', '1', '2019-11-09 10:49:49', '2019-12-11 14:30:47');
INSERT INTO `sys_group_level_config`(`keyId`, `level`, `name`, `exp`, `totalExp`, `creditExpLimit`, `playExp`, `memberCount`, `bgList`, `tbList`, `createdTime`, `lastUpTime`) VALUES (6, 6, '蓝冠', 30000000, 36100000, 10000, 10, 15000, '1,2,3,4,5,6', '1', '2019-11-09 10:49:49', '2019-12-11 14:30:55');
INSERT INTO `sys_group_level_config`(`keyId`, `level`, `name`, `exp`, `totalExp`, `creditExpLimit`, `playExp`, `memberCount`, `bgList`, `tbList`, `createdTime`, `lastUpTime`) VALUES (7, 7, '金冠', 0, 66100000, 0, 0, 20000, '1,2,3,4,5,6,7', '1', '2019-11-09 10:49:49', '2019-12-12 19:46:59');

INSERT INTO `sys_group_user_level_config`(`keyId`, `level`, `name`, `exp`, `totalExp`, `creditExpLimit`, `playExp`, `headimgList`, `goldRate`, `createdTime`, `lastUpTime`) VALUES (1, 1, '富农', 3000, 0, 2000, 100, '1', 0, '2019-11-09 10:56:37', '2019-12-03 16:10:24');
INSERT INTO `sys_group_user_level_config`(`keyId`, `level`, `name`, `exp`, `totalExp`, `creditExpLimit`, `playExp`, `headimgList`, `goldRate`, `createdTime`, `lastUpTime`) VALUES (2, 2, '财主', 10000, 3000, 2000, 100, '1,2', 200, '2019-11-09 10:56:37', '2019-12-11 14:26:51');
INSERT INTO `sys_group_user_level_config`(`keyId`, `level`, `name`, `exp`, `totalExp`, `creditExpLimit`, `playExp`, `headimgList`, `goldRate`, `createdTime`, `lastUpTime`) VALUES (3, 3, '土豪', 50000, 13000, 2000, 100, '1,2,3', 500, '2019-11-09 10:56:37', '2019-12-11 14:26:55');
INSERT INTO `sys_group_user_level_config`(`keyId`, `level`, `name`, `exp`, `totalExp`, `creditExpLimit`, `playExp`, `headimgList`, `goldRate`, `createdTime`, `lastUpTime`) VALUES (4, 4, '大富翁', 150000, 63000, 2000, 100, '1,2,3,4', 800, '2019-11-09 10:56:37', '2019-12-11 14:27:00');
INSERT INTO `sys_group_user_level_config`(`keyId`, `level`, `name`, `exp`, `totalExp`, `creditExpLimit`, `playExp`, `headimgList`, `goldRate`, `createdTime`, `lastUpTime`) VALUES (5, 5, '子爵', 500000, 213000, 2000, 100, '1,2,3,4,5', 1000, '2019-11-09 10:56:37', '2019-12-11 14:27:05');
INSERT INTO `sys_group_user_level_config`(`keyId`, `level`, `name`, `exp`, `totalExp`, `creditExpLimit`, `playExp`, `headimgList`, `goldRate`, `createdTime`, `lastUpTime`) VALUES (6, 6, '伯爵', 1000000, 713000, 2000, 100, '1,2,3,4,5,6', 1500, '2019-11-09 10:56:37', '2019-12-11 14:27:11');
INSERT INTO `sys_group_user_level_config`(`keyId`, `level`, `name`, `exp`, `totalExp`, `creditExpLimit`, `playExp`, `headimgList`, `goldRate`, `createdTime`, `lastUpTime`) VALUES (7, 7, '侯爵', 2000000, 1713000, 2000, 100, '1,2,3,4,5,6,7', 2000, '2019-11-09 10:56:37', '2019-12-11 14:27:14');
INSERT INTO `sys_group_user_level_config`(`keyId`, `level`, `name`, `exp`, `totalExp`, `creditExpLimit`, `playExp`, `headimgList`, `goldRate`, `createdTime`, `lastUpTime`) VALUES (8, 8, '公爵', 0, 3713000, 0, 0, '1,2,3,4,5,6,7,8', 2500, '2019-11-09 10:56:37', '2019-12-12 19:47:12');


-- 20191227 亲友圈玩法限制
ALTER TABLE `t_group`
ADD COLUMN `gameIds` varchar(255) DEFAULT NULL COMMENT '游戏ID' AFTER `refreshTimeDaily`;


-- 20200107 信用分流水日志
ALTER TABLE `t_group_credit_log`
ADD COLUMN `promoterId5` bigint(20) NULL DEFAULT 0 COMMENT '五级拉手id' AFTER `promoterId4`,
ADD COLUMN `promoterId6` bigint(20) NULL DEFAULT 0 COMMENT '六级拉手id' AFTER `promoterId5`,
ADD COLUMN `promoterId7` bigint(20) NULL DEFAULT 0 COMMENT '七级拉手id' AFTER `promoterId6`,
ADD COLUMN `promoterId8` bigint(20) NULL DEFAULT 0 COMMENT '八级拉手id' AFTER `promoterId7`,
ADD COLUMN `promoterId9` bigint(20) NULL DEFAULT 0 COMMENT '九级拉手id' AFTER `promoterId8`,
ADD COLUMN `promoterId10` bigint(20) NULL DEFAULT 0 COMMENT '十级拉手id' AFTER `promoterId9`;

-- 20200108
ALTER TABLE `t_group_table`
ADD COLUMN `playType` int(11) NULL DEFAULT 0 COMMENT '玩法' AFTER `creditMsg`;

-- 20200113
CREATE TABLE `t_group_user_reject` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) DEFAULT '0' COMMENT '亲友圈id',
  `userIdKey` bigint(20) DEFAULT '0' COMMENT '用户1id*10000拼接用户2id整数，规则：id小的在前',
  `userId1` bigint(20) DEFAULT '0' COMMENT '用户id',
  `userId2` bigint(20) DEFAULT '0' COMMENT '用户id',
  `createdTime` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`keyId`),
  KEY `idx_gid_uid1` (`groupId`,`userId1`) USING BTREE,
  KEY `idx_gid_uid2` (`groupId`,`userId2`) USING BTREE,
  KEY `idx_gid_userIdkey` (`groupId`,`userIdKey`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='亲友圈互斥名单';

-- 分表配置
CREATE TABLE `sys_partition` (
  `keyId` int(11) NOT NULL AUTO_INCREMENT,
  `type` int(10) DEFAULT '1' COMMENT '分表类型：1：俱乐部数据',
  `seq` int(10) DEFAULT '1' COMMENT '分表序号',
  `isHash` smallint(1) DEFAULT '0' COMMENT '是否散列：0否，1是',
  `ids` varchar(1000) COLLATE utf8_bin DEFAULT '' COMMENT 'id列表，格式：id,id,id....',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_uniq` (`type`,`seq`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='分表配置表';


-- 单独记录上下分
CREATE TABLE `t_group_credit_log_transfer` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) DEFAULT '0' COMMENT '俱乐部id',
  `userId` bigint(11) DEFAULT '0' COMMENT '用户id',
  `optUserId` bigint(11) DEFAULT '0' COMMENT '操作员id：默认为0',
  `tableId` bigint(20) DEFAULT '0' COMMENT '牌桌id',
  `credit` bigint(20) DEFAULT '0' COMMENT '信用分值',
  `curCredit` bigint(20) DEFAULT '0' COMMENT '操作后信用分',
  `type` int(11) DEFAULT NULL COMMENT '类型：1：管理加减分，2：佣金，3：牌局',
  `flag` int(11) DEFAULT '0' COMMENT '是否有效：0否，1是',
  `userGroup` int(11) DEFAULT '-1' COMMENT '小组id',
  `promoterId1` bigint(20) DEFAULT '0' COMMENT '一级拉手id',
  `promoterId2` bigint(20) DEFAULT '0' COMMENT '二级拉手id',
  `promoterId3` bigint(20) DEFAULT '0' COMMENT '三级拉手id',
  `promoterId4` bigint(20) DEFAULT '0' COMMENT '四级拉手id',
  `promoterId5` bigint(20) DEFAULT '0' COMMENT '五级拉手id',
  `promoterId6` bigint(20) DEFAULT '0' COMMENT '六级拉手id',
  `promoterId7` bigint(20) DEFAULT '0' COMMENT '七级拉手id',
  `promoterId8` bigint(20) DEFAULT '0' COMMENT '八级拉手id',
  `promoterId9` bigint(20) DEFAULT '0' COMMENT '九级拉手id',
  `promoterId10` bigint(20) DEFAULT '0' COMMENT '十级拉手id',
  `mode` int(11) DEFAULT '0' COMMENT '是否正向数据:optUserId主动操作userId为正向',
  `createdTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `roomName` varchar(100) COLLATE utf8_bin DEFAULT '' COMMENT '包间名字',
  `groupTableId` bigint(20) DEFAULT '0' COMMENT 't_group_table.id',
  PRIMARY KEY (`keyId`),
  KEY `index_all` (`groupId`,`createdTime`,`userId`,`type`) USING BTREE,
  KEY `idx_gId_time_uId_type` (`groupId`,`createdTime`,`optUserId`,`type`) USING BTREE,
  KEY `groupId_credit` (`groupId`,`credit`,`userId`) USING BTREE,
  KEY `idx_groupTableId` (`groupTableId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- 20200220
ALTER TABLE `t_group_table`
ADD COLUMN `payMsg` varchar(50) NULL DEFAULT '' COMMENT '支付信息' AFTER `playType`;

--20200229

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_pdk_config
-- ----------------------------
DROP TABLE IF EXISTS `t_pdk_config`;
CREATE TABLE `t_pdk_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` int(11) DEFAULT '0' COMMENT '配牌条件类型',
  `val` int(11) DEFAULT '0' COMMENT '条件类型值',
  `rate` varchar(64) DEFAULT '' COMMENT '概率',
  `descriptMsg` varchar(24) DEFAULT '' COMMENT '描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_pdk_config
-- ----------------------------
INSERT INTO `t_pdk_config` VALUES ('1', '1', '60', '10;5;10', '赢60分');
INSERT INTO `t_pdk_config` VALUES ('2', '1', '80', '10;5;10', '赢80分');
INSERT INTO `t_pdk_config` VALUES ('3', '1', '100', '30;30;30', '赢100分');
INSERT INTO `t_pdk_config` VALUES ('4', '2', '3', '10;10;10', '连胜3场');
INSERT INTO `t_pdk_config` VALUES ('5', '3', '0', '10;10;10', '整局赢的次数通过公式决定（赢半数+1）');
INSERT INTO `t_pdk_config` VALUES ('6', '4', '40', '10;10;0;0', '输40分');
INSERT INTO `t_pdk_config` VALUES ('7', '4', '60', '10;10;5;5', '输60分');
INSERT INTO `t_pdk_config` VALUES ('8', '4', '80', '30;30;30;10', '输80分');
INSERT INTO `t_pdk_config` VALUES ('9', '5', '3', '10;10;5;0', '连输3把');
INSERT INTO `t_pdk_config` VALUES ('10', '6', '0', '10;10;0;10', '整局输的局数（输半数+1）');

-- 亲友圈私密房
ALTER TABLE `t_group_table`
ADD COLUMN `isPrivate` tinyint(1) NULL DEFAULT 0 COMMENT '是否私密房间：0否，1是' AFTER `payMsg`;

-- 亲友圈好友关系表
CREATE TABLE `t_group_user_friend` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) DEFAULT '0' COMMENT '亲友圈id',
  `userIdKey` bigint(20) DEFAULT '0' COMMENT '用户1id*10000拼接用户2id整数，规则：id小的在前',
  `userId1` bigint(20) DEFAULT '0' COMMENT '用户id',
  `userId2` bigint(20) DEFAULT '0' COMMENT '用户id',
  `createdTime` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`keyId`),
  KEY `idx_gid_uid1` (`groupId`,`userId1`) USING BTREE,
  KEY `idx_gid_uid2` (`groupId`,`userId2`) USING BTREE,
  KEY `idx_gid_userIdkey` (`groupId`,`userIdKey`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='亲友圈好友名单';

-- 棋分
ALTER TABLE `t_group_user`
ADD COLUMN `score` bigint(20) NULL DEFAULT 0 COMMENT '棋分' AFTER `frameId`;

-- 20200601新金币场-----------------------------
DROP TABLE IF EXISTS `mission_config`;
CREATE TABLE `mission_config`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tag` int(5) NULL DEFAULT NULL COMMENT '1每日任务，2挑战任务',
  `missionExplain` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '任务描述',
  `awardExplain` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '奖励描述',
  `type` int(5) NULL DEFAULT NULL COMMENT '1签到，2局数统计，3分享任务',
  `finishNum` int(11) NULL DEFAULT NULL COMMENT '要达数量才能完成',
  `awardId` int(11) NULL DEFAULT NULL COMMENT '奖励道具id',
  `awardIcon` int(11) NULL DEFAULT NULL COMMENT '道具图标',
  `awardNum` int(11) NULL DEFAULT NULL COMMENT '奖励数量',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `seven_gold_sign`;
CREATE TABLE `seven_gold_sign`  (
  `userId` bigint(20) NOT NULL,
  `lastSignTime` date NOT NULL COMMENT '最后签到时间',
  `sevenSign` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '0' COMMENT '七天签到记录 格式：0,1,2,3,4,5,6,7',
  PRIMARY KEY (`userId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `seven_sign_config`;
CREATE TABLE `seven_sign_config`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `dayNum` int(4) NOT NULL,
  `goldNum` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `mission_about`;
CREATE TABLE `mission_about`  (
  `userId` bigint(20) NOT NULL,
  `completeId` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '已完成没领取的任务奖励id，“,”号隔开',
  `dayMissionState` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '每日任务',
  `otherMissionState` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `brokeAward` int(3) NULL DEFAULT 0 COMMENT '破产补助领取次数',
  PRIMARY KEY (`userId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

ALTER TABLE `user_inf`
ADD COLUMN `isReceiveBDAward` INT(2) NULL DEFAULT 0 COMMENT '是否已领取绑定奖励';

ALTER TABLE `mission_about`
ADD COLUMN `brokeShare` INT(2) NULL DEFAULT 0 COMMENT '破产补助领取分享次数';
ALTER TABLE `mission_about`
ADD COLUMN `dayTime` DATE NULL DEFAULT now() COMMENT '';




-- pdk1mb~pdknmb所有库

ALTER TABLE `table_inf`
ADD COLUMN `tableType` int(1) NULL DEFAULT 0 COMMENT '牌桌类型：0其他，1新金币场' AFTER `playType`;


-- 1kz库
-- 需要创建分表 1-16
CREATE TABLE `user_gold_record` (
  `id` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userId` bigint(20) NOT NULL COMMENT '玩家ID',
  `freeGold` int(11) DEFAULT NULL COMMENT '玩家当前免费房卡数',
  `gold` int(11) DEFAULT NULL COMMENT '玩家当前房卡数',
  `addFreeGold` int(11) DEFAULT NULL COMMENT '玩家本次操作(消耗/获得)免费房卡数',
  `addGold` int(11) DEFAULT NULL COMMENT '玩家本次操作(消耗/获得)免费房卡数',
  `recordType` int(1) DEFAULT NULL COMMENT '操作类型(1消耗  0获得)',
  `playType` int(11) DEFAULT NULL COMMENT '操作所属玩法ID 0表示不属于玩法类操作',
  `sourceType` int(11) DEFAULT '0' COMMENT '操作来源',
  `createTime` datetime DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user` (`userId`,`createTime`) USING BTREE,
  KEY `idx_date` (`sourceType`,`createTime`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='用户金币变化表';


-- login库

-- 需要创建分表 1-60
CREATE TABLE `t_group_gold_log` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) DEFAULT '0' COMMENT '俱乐部id',
  `userId` bigint(11) DEFAULT '0' COMMENT '用户id',
  `optUserId` bigint(11) DEFAULT '0' COMMENT '操作员id',
  `tableId` bigint(20) DEFAULT '0' COMMENT '牌桌id',
  `gold` bigint(20) DEFAULT '0' COMMENT '金币值',
  `curGold` bigint(20) DEFAULT '0' COMMENT '操作后金币',
  `type` int(11) DEFAULT NULL COMMENT '类型：1：管理加减分，2：佣金，3：牌局',
  `flag` int(11) DEFAULT '0' COMMENT '是否有效：0否，1是',
  `promoterId1` bigint(20) DEFAULT '0' COMMENT '一级拉手id',
  `promoterId2` bigint(20) DEFAULT '0' COMMENT '二级拉手id',
  `promoterId3` bigint(20) DEFAULT '0' COMMENT '三级拉手id',
  `promoterId4` bigint(20) DEFAULT '0' COMMENT '四级拉手id',
  `promoterId5` bigint(20) DEFAULT '0' COMMENT '五级拉手id',
  `promoterId6` bigint(20) DEFAULT '0' COMMENT '六级拉手id',
  `promoterId7` bigint(20) DEFAULT '0' COMMENT '七级拉手id',
  `promoterId8` bigint(20) DEFAULT '0' COMMENT '八级拉手id',
  `promoterId9` bigint(20) DEFAULT '0' COMMENT '九级拉手id',
  `promoterId10` bigint(20) DEFAULT '0' COMMENT '十级拉手id',
  `mode` int(11) DEFAULT '0' COMMENT '是否正向数据:optUserId主动操作userId为正向',
  `createdTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `roomName` varchar(100) COLLATE utf8_bin DEFAULT '' COMMENT '包间名字',
  PRIMARY KEY (`keyId`),
  KEY `idx_gid_uid_time_type` (`groupId`,`userId`,`createdTime`,`type`) USING BTREE,
  KEY `idx_gId_optuid_time_type` (`groupId`,`optUserId`,`createdTime`,`type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='亲友圈金币抽水表';

INSERT INTO `t_resources_configs`(`msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ('ServerConfig', 'user_gold_record_table_count', '16', 'user_gold_record分表数量');

ALTER TABLE `user_inf`
ADD COLUMN `goldRoomGroupId` bigint(20) NULL DEFAULT 0 COMMENT '金币场亲友圈id';

ALTER TABLE `user_inf`
ADD COLUMN `isReceiveBDAward` INT(2) NULL DEFAULT 0 COMMENT '是否已领取绑定奖励';

ALTER TABLE `t_group`
ADD COLUMN `goldRoomSwitch` tinyint(1) NULL DEFAULT 0 COMMENT '金币场开关：0否，1是' AFTER `gameIds`,
ADD COLUMN `goldRoomRate` int(10) NULL DEFAULT -1 COMMENT '金币场系统抽水比例' AFTER `goldRoomSwitch`;

DROP TABLE IF EXISTS `mission_config`;
CREATE TABLE `mission_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tag` int(5) DEFAULT NULL COMMENT '1每日任务，2挑战任务',
  `missionExplain` varchar(20) DEFAULT '' COMMENT '任务描述',
  `awardExplain` varchar(30) DEFAULT '' COMMENT '奖励描述',
  `type` int(5) DEFAULT NULL COMMENT '1签到，2局数统计，3分享任务',
  `finishNum` int(11) DEFAULT NULL COMMENT '要达数量才能完成',
  `awardId` int(11) DEFAULT NULL COMMENT '奖励道具id',
  `awardIcon` int(11) DEFAULT NULL COMMENT '道具图标',
  `awardNum` int(11) DEFAULT NULL COMMENT '奖励数量',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

CREATE TABLE `mission_about` (
  `userId` bigint(20) NOT NULL,
  `completeId` varchar(50) NOT NULL COMMENT '已完成没领取的任务奖励id，“,”号隔开',
  `dayMissionState` varchar(255) DEFAULT NULL COMMENT '每日任务',
  `otherMissionState` varchar(255) DEFAULT NULL,
  `brokeAward` int(3) DEFAULT '0' COMMENT '破产补助领取次数',
  `brokeShare` int(2) DEFAULT '0' COMMENT '破产补助领取分享次数',
  `dayTime` date DEFAULT '0000-00-00',
  PRIMARY KEY (`userId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

CREATE TABLE `seven_gold_sign` (
  `userId` bigint(20) NOT NULL,
  `lastSignTime` date NOT NULL COMMENT '最后签到时间',
  `sevenSign` varchar(20) DEFAULT '0' COMMENT '七天签到记录 格式：0,1,2,3,4,5,6,7',
  PRIMARY KEY (`userId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

CREATE TABLE `seven_sign_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `dayNum` int(4) NOT NULL,
  `goldNum` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;


CREATE TABLE `t_gold_room_area` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `parentId` bigint(20) DEFAULT '1' COMMENT '上级id(t_gold_room_area.keyId)',
  `state` tinyint(1) DEFAULT NULL COMMENT '状态：1、有效，2、失效',
  `name` varchar(50) COLLATE utf8_bin DEFAULT '' COMMENT '名字',
  `order` int(10) DEFAULT '0' COMMENT '排序值',
  `createdTime` datetime DEFAULT NULL COMMENT '创建时间',
  `lastUpTime` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`keyId`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='金币场玩法配置表';

CREATE TABLE `t_gold_room_config` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `state` tinyint(1) DEFAULT '1' COMMENT '状态：1、有效，2、失效',
  `playType` int(10) DEFAULT '0' COMMENT '玩法',
  `name` varchar(50) COLLATE utf8_bin DEFAULT '' COMMENT '名字',
  `playerCount` tinyint(1) DEFAULT '0' COMMENT '人数',
  `totalBureau` tinyint(1) DEFAULT '0' COMMENT '局数',
  `tableMsg` varchar(100) COLLATE utf8_bin DEFAULT '' COMMENT '玩法配置',
  `goldMsg` varchar(100) COLLATE utf8_bin DEFAULT '' COMMENT '金币配置',
  `areaId` bigint(20) DEFAULT '0' COMMENT '区域id',
  `order` int(10) DEFAULT '0' COMMENT '排序值',
  `desc` varchar(500) COLLATE utf8_bin DEFAULT '' COMMENT '描述信息',
  `createdTime` datetime DEFAULT NULL COMMENT '创建时间',
  `lastUpTime` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`keyId`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='金币场玩法配置表';

CREATE TABLE `t_gold_room_group_limit` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupIds` varchar(255) COLLATE utf8_bin NOT NULL COMMENT '限制匹配的亲友圈id列表，格式：gid1,gid2',
  `createdTime` datetime DEFAULT NULL,
  `lastUpTime` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`keyId`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='亲友圈匹配限制';

CREATE TABLE `t_gold_room_table_record` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `goldRoomId` bigint(20) DEFAULT '0' COMMENT 't_gold_room.keyId',
  `tableId` bigint(20) NOT NULL COMMENT '房间id',
  `playNo` int(5) NOT NULL COMMENT '牌局序号',
  `recordType` int(2) NOT NULL COMMENT '结算类型（0：小结算，1：大结算）',
  `resultMsg` text COMMENT '牌局结算信息',
  `logId` bigint(20) DEFAULT NULL COMMENT '日志id',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`keyId`) USING BTREE,
  KEY `idx_tableId` (`tableId`) USING BTREE,
  KEY `idx_logId` (`logId`) USING BTREE,
  KEY `idx_goldRoomId` (`goldRoomId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='战绩信息表';

DROP TABLE IF EXISTS `t_gold_room`;
CREATE TABLE `t_gold_room` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tableId` bigint(20) DEFAULT '0' COMMENT '牌桌id',
  `configId` bigint(20) DEFAULT NULL COMMENT 't_gold_room_config.keyId',
  `modeId` varchar(50) DEFAULT '' COMMENT '配置',
  `serverId` tinyint(1) NOT NULL COMMENT '服务id',
  `currentCount` tinyint(1) NOT NULL COMMENT '当前人数',
  `maxCount` tinyint(1) NOT NULL COMMENT '最大人数',
  `gameCount` tinyint(1) NOT NULL COMMENT '局数',
  `currentState` tinyint(1) NOT NULL COMMENT '状态：0、未开始，1、可匹配，2、开局，3、结束',
  `tableMsg` varchar(255) NOT NULL DEFAULT '' COMMENT '房间信息',
  `goldMsg` varchar(255) DEFAULT '' COMMENT '金币信息',
  `tableName` varchar(100) DEFAULT '' COMMENT '名字',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifiedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`keyId`) USING BTREE,
  KEY `idx_common` (`configId`,`currentState`,`serverId`) USING BTREE,
  KEY `idx_createdTime` (`createdTime`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='无房号金币场房间';


CREATE TABLE `t_gold_room_freeze` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tableId` bigint(20) DEFAULT '0' COMMENT '牌桌id',
  `configId` bigint(20) DEFAULT NULL COMMENT 't_gold_room_config.keyId',
  `modeId` varchar(50) DEFAULT '' COMMENT '配置',
  `serverId` tinyint(1) NOT NULL COMMENT '服务id',
  `currentCount` tinyint(1) NOT NULL COMMENT '当前人数',
  `maxCount` tinyint(1) NOT NULL COMMENT '最大人数',
  `gameCount` tinyint(1) NOT NULL COMMENT '局数',
  `currentState` tinyint(1) NOT NULL COMMENT '状态：0、未开始，1、可匹配，2、开局，3、结束',
  `tableMsg` varchar(255) NOT NULL DEFAULT '' COMMENT '房间信息',
  `goldMsg` varchar(255) DEFAULT '' COMMENT '金币信息',
  `tableName` varchar(100) DEFAULT '' COMMENT '名字',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifiedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`keyId`) USING BTREE,
  KEY `idx_common` (`configId`,`currentState`,`serverId`) USING BTREE,
  KEY `idx_createdTime` (`createdTime`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='无房号金币场房间';


DROP TABLE IF EXISTS `t_gold_room_user`;
CREATE TABLE `t_gold_room_user` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `roomId` bigint(20) NOT NULL COMMENT '房间id',
  `groupId` bigint(20) DEFAULT NULL COMMENT '亲友圈id',
  `userId` bigint(10) NOT NULL COMMENT '玩家ID',
  `gameResult` bigint(20) NOT NULL DEFAULT '0' COMMENT '结果',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `logIds` varchar(512) DEFAULT NULL COMMENT 'logId多个以逗号隔开',
  PRIMARY KEY (`keyId`) USING BTREE,
  UNIQUE KEY `idx_room` (`roomId`,`userId`) USING BTREE,
  KEY `idx_userId` (`userId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


CREATE TABLE `t_gold_room_user_freeze` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `roomId` bigint(20) NOT NULL COMMENT '房间id',
  `groupId` bigint(20) DEFAULT NULL COMMENT '亲友圈id',
  `userId` bigint(10) NOT NULL COMMENT '玩家ID',
  `gameResult` bigint(20) NOT NULL DEFAULT '0' COMMENT '结果',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `logIds` varchar(512) DEFAULT NULL COMMENT 'logId多个以逗号隔开',
  PRIMARY KEY (`keyId`) USING BTREE,
  UNIQUE KEY `idx_room` (`roomId`,`userId`) USING BTREE,
  KEY `idx_userId` (`userId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `t_goods_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` tinyint(1) DEFAULT NULL COMMENT '类型：1、钻石换金币，2、金币换钻石',
  `name` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '商品名称',
  `desc` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '描述',
  `discount` int(11) DEFAULT '100' COMMENT '消耗折扣（百分比）,无折扣时设置100',
  `amount` bigint(20) DEFAULT '0' COMMENT '获取的兑换数量',
  `count` bigint(20) DEFAULT '0' COMMENT '消耗的兑换币数量',
  `give` bigint(20) DEFAULT '0' COMMENT '赠送数量',
  `ratio` int(11) DEFAULT '100' COMMENT '获取的比例（百分比）',
  `createdTime` datetime DEFAULT NULL COMMENT '创建时间',
  `lasatUpTime` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='兑换表';

CREATE TABLE `t_group_gold_commission_config` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupId` bigint(20) NOT NULL DEFAULT '0' COMMENT '俱乐部id',
  `userId` bigint(20) NOT NULL DEFAULT '0' COMMENT '玩家id',
  `seq` int(11) DEFAULT '0' COMMENT '档位序号',
  `minValue` bigint(20) DEFAULT NULL COMMENT '区段最小值',
  `maxValue` bigint(20) DEFAULT NULL COMMENT '区段最大值',
  `value` bigint(20) DEFAULT '0' COMMENT '留给自己的值',
  `leftValue` bigint(20) DEFAULT '0' COMMENT '留给下级可配置的最大分',
  `maxLog` bigint(20) DEFAULT '0' COMMENT '记录修改时允许最大分值',
  `createdTime` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `lastUpTime` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`keyId`) USING BTREE,
  UNIQUE KEY `uniq_index` (`groupId`,`userId`,`seq`) USING BTREE,
  KEY `userId_index` (`userId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `t_solo_room_config` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `soloType` tinyint(1) DEFAULT '1' COMMENT '类型：1金币',
  `state` tinyint(1) DEFAULT '1' COMMENT '状态：1、有效，2、失效',
  `playType` int(10) DEFAULT '0' COMMENT '玩法',
  `name` varchar(50) COLLATE utf8_bin DEFAULT '' COMMENT '名字',
  `playerCount` tinyint(1) DEFAULT '0' COMMENT '人数',
  `totalBureau` tinyint(1) DEFAULT '0' COMMENT '局数',
  `tableMsg` varchar(100) COLLATE utf8_bin DEFAULT '' COMMENT '玩法配置',
  `order` int(10) DEFAULT '0' COMMENT '排序值',
  `desc` varchar(500) COLLATE utf8_bin DEFAULT '' COMMENT '描述信息',
  `createdTime` datetime DEFAULT NULL COMMENT '创建时间',
  `lastUpTime` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`keyId`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='solo玩法配置表';

CREATE TABLE `t_solo_room_table_record` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `winnerId` bigint(10) DEFAULT NULL COMMENT '赢家id',
  `loserId` bigint(10) DEFAULT NULL COMMENT '输家id',
  `playType` int(10) DEFAULT NULL COMMENT '玩法id',
  `tableId` bigint(20) DEFAULT NULL COMMENT '牌桌id',
  `gold` bigint(10) DEFAULT NULL COMMENT '金币值',
  `logId` bigint(20) DEFAULT NULL COMMENT '日志id',
  `createdTime` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`keyId`),
  KEY `idx_wid_time` (`winnerId`,`createdTime`) USING BTREE,
  KEY `idx_lid_time` (`loserId`,`createdTime`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='solo房间金币记录';

CREATE TABLE `gold_data_statistics` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dataDate` bigint(12) DEFAULT NULL COMMENT '统计日期(最多可按分钟统计)',
  `dataType` int(10) DEFAULT NULL COMMENT '数据类别',
  `userId` bigint(20) DEFAULT NULL COMMENT '玩家ID',
  `dataCount` int(10) DEFAULT '0' COMMENT '统计次数',
  `dataValue` bigint(20) DEFAULT '0' COMMENT '统计结果',
  PRIMARY KEY (`keyId`) USING BTREE,
  UNIQUE KEY `idx_unique` (`dataDate`,`dataType`,`userId`) USING BTREE,
  KEY `idx_common` (`dataType`,`dataDate`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `log_group_gold_commission` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `dataDate` bigint(20) NOT NULL DEFAULT '0' COMMENT '日期：格式20160101',
  `groupId` bigint(20) DEFAULT '0' COMMENT '俱乐部id',
  `userId` bigint(20) DEFAULT '0' COMMENT '用户id',
  `gold` bigint(20) DEFAULT '0' COMMENT '从下级获得的赠送分',
  `commission` bigint(20) DEFAULT NULL COMMENT '下级产生的赠送分',
  `commissionCount` int(11) DEFAULT '0' COMMENT '下级产生的赠送次数',
  `zjsCount` int(11) DEFAULT '0' COMMENT '下级产生的总大局数',
  `selfWin` bigint(20) DEFAULT '0' COMMENT '自己当天输赢比赛分',
  `selfCommission` bigint(20) DEFAULT '0' COMMENT '自己产生的赠送分',
  `selfCommissionCount` int(11) DEFAULT '0' COMMENT '自己产生的赠送次数',
  `selfZjsCount` int(11) DEFAULT '0' COMMENT '自己产生的总局数',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_uniq` (`dataDate`,`groupId`,`userId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3515 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE `log_group_gold_table` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `dataDate` bigint(10) NOT NULL COMMENT '日期：格式20160101',
  `groupId` bigint(10) DEFAULT NULL COMMENT '亲友圈id',
  `configId` bigint(20) DEFAULT NULL COMMENT '配置id',
  `userId` bigint(10) DEFAULT NULL COMMENT '用户id',
  `playType` int(10) DEFAULT NULL COMMENT '玩法id',
  `ticket` bigint(20) DEFAULT '0' COMMENT '门票',
  `ticketCount` int(10) DEFAULT '0' COMMENT '门票次数',
  `commission` bigint(20) DEFAULT '0' COMMENT '抽水',
  `commissionCount` int(10) DEFAULT '0' COMMENT '抽水次数',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_unique` (`dataDate`,`groupId`,`configId`,`userId`) USING BTREE,
  KEY `idx_dataDate` (`dataDate`) USING BTREE,
  KEY `idx_groupId` (`groupId`) USING BTREE,
  KEY `idx_gameType` (`playType`) USING BTREE,
  KEY `idx_userId` (`userId`) USING BTREE,
  KEY `idx_configId` (`configId`)
) ENGINE=InnoDB AUTO_INCREMENT=3273 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='亲友圈用户局数日志';


-- 数据：

INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (1, 1, 15, '15张跑得快无红10', 2, 1, '1,15,0,0,0,0,0,2,1,3,0,3,0,0,0,0,0,0,0,0,0,60,0,10,2,0,0,1,0,1,0,0,0,0', '200,1500,100', 1, 0, '15张跑得快2人无红10，可4带321【100底】——通用', '2020-06-01 09:23:05', '2020-06-01 09:57:28');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (2, 1, 15, '15张跑得快无红10', 2, 1, '1,15,0,0,0,0,0,2,1,3,0,3,0,0,0,0,0,0,0,0,0,60,0,10,2,0,0,1,0,1,0,0,0,0', '400,15000,500', 1, 0, '15张跑得快2人无红10，可4带321【500底】——通用', '2020-06-01 09:23:05', '2020-06-01 09:57:30');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (3, 1, 15, '15张跑得快无红10', 2, 1, '1,15,0,0,0,0,0,2,1,3,0,3,0,0,0,0,0,0,0,0,0,60,0,10,2,0,0,1,0,1,0,0,0,0', '500,30000,1000', 1, 0, '15张跑得快2人无红10，可4带321【1000底】——通用', '2020-06-01 09:23:05', '2020-06-01 09:57:31');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (4, 1, 15, '15张跑得快无红10', 2, 1, '1,15,0,0,0,0,0,2,1,3,0,3,0,0,0,0,0,0,0,0,0,60,0,10,2,0,0,1,0,1,0,0,0,0', '800,90000,3000', 1, 0, '15张跑得快2人无红10，可4带321【3000底】——通用', '2020-06-01 09:23:05', '2020-06-01 09:57:33');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (5, 1, 15, '15张跑得快无红10', 2, 1, '1,15,0,0,0,0,0,2,1,3,0,3,0,0,0,0,0,0,0,0,0,60,0,10,2,0,0,1,0,1,0,0,0,0', '2000,300000,10000', 1, 0, '15张跑得快2人无红10，可4带321【10000底】——通用', '2020-06-01 09:23:05', '2020-06-01 09:57:37');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (6, 1, 15, '15张跑得快无红10', 3, 1, '1,15,0,0,0,0,0,3,1,3,0,3,0,0,0,0,0,0,0,0,0,60,0,10,2,0,0,1,0,1,0,0,0,0', '200,1500,100', 1, 0, '15张跑得快3人无红10，可4带321【100底】——通用', '2020-06-01 09:23:05', '2020-06-01 09:58:27');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (7, 1, 15, '15张跑得快无红10', 3, 1, '1,15,0,0,0,0,0,3,1,3,0,3,0,0,0,0,0,0,0,0,0,60,0,10,2,0,0,1,0,1,0,0,0,0', '400,15000,500', 1, 0, '15张跑得快3人无红10，可4带321【500底】——通用', '2020-06-01 09:23:05', '2020-06-01 09:58:28');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (8, 1, 15, '15张跑得快无红10', 3, 1, '1,15,0,0,0,0,0,3,1,3,0,3,0,0,0,0,0,0,0,0,0,60,0,10,2,0,0,1,0,1,0,0,0,0', '500,30000,1000', 1, 0, '15张跑得快3人无红10，可4带321【1000底】——通用', '2020-06-01 09:23:05', '2020-06-01 09:58:28');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (9, 1, 15, '15张跑得快无红10', 3, 1, '1,15,0,0,0,0,0,3,1,3,0,3,0,0,0,0,0,0,0,0,0,60,0,10,2,0,0,1,0,1,0,0,0,0', '800,90000,3000', 1, 0, '15张跑得快3人无红10，可4带321【3000底】——通用', '2020-06-01 09:23:05', '2020-06-01 09:58:29');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (10, 1, 15, '15张跑得快无红10', 3, 1, '1,15,0,0,0,0,0,3,1,3,0,3,0,0,0,0,0,0,0,0,0,60,0,10,2,0,0,1,0,1,0,0,0,0', '2000,300000,10000', 1, 0, '15张跑得快3人无红10，可4带321【10000底】——通用', '2020-06-01 09:23:05', '2020-06-01 09:58:31');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (11, 1, 16, '16张跑得快无红10', 2, 1, '1,16,0,0,0,0,0,2,1,3,0,3,0,0,0,0,0,0,0,0,0,60,0,10,2,0,0,1,0,1,0,0,0,0', '200,1500,100', 1, 0, '16张跑得快2人无红10，可4带321【100底】——通用', '2020-06-01 09:23:05', '2020-06-01 09:23:40');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (12, 1, 16, '16张跑得快无红10', 2, 1, '1,16,0,0,0,0,0,2,1,3,0,3,0,0,0,0,0,0,0,0,0,60,0,10,2,0,0,1,0,1,0,0,0,0', '400,15000,500', 1, 0, '16张跑得快2人无红10，可4带321【500底】——通用', '2020-06-01 09:23:05', '2020-06-01 09:31:13');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (13, 1, 16, '16张跑得快无红10', 2, 1, '1,16,0,0,0,0,0,2,1,3,0,3,0,0,0,0,0,0,0,0,0,60,0,10,2,0,0,1,0,1,0,0,0,0', '500,30000,1000', 1, 0, '16张跑得快2人无红10，可4带321【1000底】——通用', '2020-06-01 09:23:05', '2020-06-01 09:40:20');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (14, 1, 16, '16张跑得快无红10', 2, 1, '1,16,0,0,0,0,0,2,1,3,0,3,0,0,0,0,0,0,0,0,0,60,0,10,2,0,0,1,0,1,0,0,0,0', '800,90000,3000', 1, 0, '16张跑得快2人无红10，可4带321【3000底】——通用', '2020-06-01 09:23:05', '2020-06-01 09:41:23');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (15, 1, 16, '16张跑得快无红10', 2, 1, '1,16,0,0,0,0,0,2,1,3,0,3,0,0,0,0,0,0,0,0,0,60,0,10,2,0,0,1,0,1,0,0,0,0', '2000,300000,10000', 1, 0, '16张跑得快2人无红10，可4带321【10000底】——通用', '2020-06-01 09:23:05', '2020-06-01 09:41:23');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (16, 1, 16, '16张跑得快无红10', 3, 1, '1,16,0,0,0,0,0,3,1,3,0,3,0,0,0,0,0,0,0,0,0,60,0,10,2,0,0,1,0,1,0,0,0,0', '200,1500,100', 1, 0, '16张跑得快3人无红10，可4带321【100底】——通用', '2020-06-01 09:23:05', '2020-06-01 09:23:40');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (17, 1, 16, '16张跑得快无红10', 3, 1, '1,16,0,0,0,0,0,3,1,3,0,3,0,0,0,0,0,0,0,0,0,60,0,10,2,0,0,1,0,1,0,0,0,0', '400,15000,500', 1, 0, '16张跑得快3人无红10，可4带321【500底】——通用', '2020-06-01 09:23:05', '2020-06-01 09:31:13');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (18, 1, 16, '16张跑得快无红10', 3, 1, '1,16,0,0,0,0,0,3,1,3,0,3,0,0,0,0,0,0,0,0,0,60,0,10,2,0,0,1,0,1,0,0,0,0', '500,30000,1000', 1, 0, '16张跑得快3人无红10，可4带321【1000底】——通用', '2020-06-01 09:23:05', '2020-06-01 09:40:20');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (19, 1, 16, '16张跑得快无红10', 3, 1, '1,16,0,0,0,0,0,3,1,3,0,3,0,0,0,0,0,0,0,0,0,60,0,10,2,0,0,1,0,1,0,0,0,0', '800,90000,3000', 1, 0, '16张跑得快3人无红10，可4带321【3000底】——通用', '2020-06-01 09:23:05', '2020-06-01 09:41:23');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (20, 1, 16, '16张跑得快无红10', 3, 1, '1,16,0,0,0,0,0,3,1,3,0,3,0,0,0,0,0,0,0,0,0,60,0,10,2,0,0,1,0,1,0,0,0,0', '2000,300000,10000', 1, 0, '16张跑得快3人无红10，可4带321【10000底】——通用', '2020-06-01 09:23:05', '2020-06-01 09:41:23');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (21, 1, 33, '邵阳剥皮普通抽10张', 2, 1, '1,33,0,0,0,0,0,2,200,3,0,0,0,3,10,0,0,0,0,0,0,0,0,60,0,25,2,2,0,0,0', '200,1500,100', 1, 0, '邵阳剥皮2人无红黑抽10张底牌【100底】——通用', '2020-06-01 10:06:57', '2020-06-01 10:07:01');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (22, 1, 33, '邵阳剥皮普通抽10张', 2, 1, '1,33,0,0,0,0,0,2,200,3,0,0,0,3,10,0,0,0,0,0,0,0,0,60,0,25,2,2,0,0,0', '400,15000,500', 1, 0, '邵阳剥皮2人无红黑抽10张底牌【500底】——通用', '2020-06-01 10:06:57', '2020-06-01 10:07:01');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (23, 1, 33, '邵阳剥皮普通抽10张', 2, 1, '1,33,0,0,0,0,0,2,200,3,0,0,0,3,10,0,0,0,0,0,0,0,0,60,0,25,2,2,0,0,0', '500,30000,1000', 1, 0, '邵阳剥皮2人无红黑抽10张底牌【1000底】——通用', '2020-06-01 10:06:57', '2020-06-01 10:07:01');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (24, 1, 33, '邵阳剥皮普通抽10张', 2, 1, '1,33,0,0,0,0,0,2,200,3,0,0,0,3,10,0,0,0,0,0,0,0,0,60,0,25,2,2,0,0,0', '800,90000,3000', 1, 0, '邵阳剥皮2人无红黑抽10张底牌【3000底】——通用', '2020-06-01 10:06:57', '2020-06-01 10:07:01');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (25, 1, 33, '邵阳剥皮普通抽10张', 2, 1, '1,33,0,0,0,0,0,2,200,3,0,0,0,3,10,0,0,0,0,0,0,0,0,60,0,25,2,2,0,0,0', '2000,300000,10000', 1, 0, '邵阳剥皮2人无红黑抽10张底牌【10000底】——通用', '2020-06-01 10:06:57', '2020-06-01 10:07:01');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (26, 1, 33, '邵阳剥皮普通', 3, 1, '1,33,0,0,0,0,0,3,200,3,0,0,0,3,10,0,0,0,0,0,0,0,0,60,0,25,2,2,0,0,0', '200,1500,100', 1, 0, '邵阳剥皮3人无红黑【100底】——通用', '2020-06-01 10:06:57', '2020-06-01 10:19:34');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (27, 1, 33, '邵阳剥皮普通', 3, 1, '1,33,0,0,0,0,0,3,200,3,0,0,0,3,10,0,0,0,0,0,0,0,0,60,0,25,2,2,0,0,0', '400,15000,500', 1, 0, '邵阳剥皮3人无红黑【500底】——通用', '2020-06-01 10:06:57', '2020-06-01 10:19:36');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (28, 1, 33, '邵阳剥皮普通', 3, 1, '1,33,0,0,0,0,0,3,200,3,0,0,0,3,10,0,0,0,0,0,0,0,0,60,0,25,2,2,0,0,0', '500,30000,1000', 1, 0, '邵阳剥皮3人无红黑【1000底】——通用', '2020-06-01 10:06:57', '2020-06-01 10:19:38');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (29, 1, 33, '邵阳剥皮普通', 3, 1, '1,33,0,0,0,0,0,3,200,3,0,0,0,3,10,0,0,0,0,0,0,0,0,60,0,25,2,2,0,0,0', '800,90000,3000', 1, 0, '邵阳剥皮3人无红黑【3000底】——通用', '2020-06-01 10:06:57', '2020-06-01 10:19:40');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (30, 1, 33, '邵阳剥皮普通', 3, 1, '1,33,0,0,0,0,0,3,200,3,0,0,0,3,10,0,0,0,0,0,0,0,0,60,0,25,2,2,0,0,0', '2000,300000,10000', 1, 0, '邵阳剥皮3人无红黑【10000底】——通用', '2020-06-01 10:06:57', '2020-06-01 10:19:46');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (31, 1, 33, '邵阳剥皮红黑抽10张', 2, 1, '1,33,0,0,0,0,0,2,200,3,0,1,0,3,10,0,0,0,0,0,0,0,0,60,0,25,2,2,0,0,0', '300,3000,100', 1, 0, '邵阳剥皮2人红黑抽10张底牌【100底】——通用', '2020-06-01 10:06:57', '2020-06-01 10:07:01');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (32, 1, 33, '邵阳剥皮红黑抽10张', 2, 1, '1,33,0,0,0,0,0,2,200,3,0,1,0,3,10,0,0,0,0,0,0,0,0,60,0,25,2,2,0,0,0', '600,20000,500', 1, 0, '邵阳剥皮2人红黑抽10张底牌【500底】——通用', '2020-06-01 10:06:57', '2020-06-01 10:07:01');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (33, 1, 33, '邵阳剥皮红黑抽10张', 2, 1, '1,33,0,0,0,0,0,2,200,3,0,1,0,3,10,0,0,0,0,0,0,0,0,60,0,25,2,2,0,0,0', '800,40000,1000', 1, 0, '邵阳剥皮2人红黑抽10张底牌【1000底】——通用', '2020-06-01 10:06:57', '2020-06-01 10:07:01');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (34, 1, 33, '邵阳剥皮红黑抽10张', 2, 1, '1,33,0,0,0,0,0,2,200,3,0,1,0,3,10,0,0,0,0,0,0,0,0,60,0,25,2,2,0,0,0', '1000,120000,3000', 1, 0, '邵阳剥皮2人红黑抽10张底牌【3000底】——通用', '2020-06-01 10:06:57', '2020-06-01 10:07:01');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (35, 1, 33, '邵阳剥皮红黑抽10张', 2, 1, '1,33,0,0,0,0,0,2,200,3,0,1,0,3,10,0,0,0,0,0,0,0,0,60,0,25,2,2,0,0,0', '3000,400000,10000', 1, 0, '邵阳剥皮2人红黑抽10张底牌【10000底】——通用', '2020-06-01 10:06:57', '2020-06-01 10:07:01');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (36, 1, 33, '邵阳剥皮红黑', 3, 1, '1,33,0,0,0,0,0,3,200,3,0,1,0,3,10,0,0,0,0,0,0,0,0,60,0,25,2,2,0,0,0', '300,3000,100', 1, 0, '邵阳剥皮3人红黑【100底】——通用', '2020-06-01 10:06:57', '2020-06-01 10:07:01');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (37, 1, 33, '邵阳剥皮红黑', 3, 1, '1,33,0,0,0,0,0,3,200,3,0,1,0,3,10,0,0,0,0,0,0,0,0,60,0,25,2,2,0,0,0', '600,20000,500', 1, 0, '邵阳剥皮3人红黑【500底】——通用', '2020-06-01 10:06:57', '2020-06-01 10:07:01');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (38, 1, 33, '邵阳剥皮红黑', 3, 1, '1,33,0,0,0,0,0,3,200,3,0,1,0,3,10,0,0,0,0,0,0,0,0,60,0,25,2,2,0,0,0', '800,40000,1000', 1, 0, '邵阳剥皮3人红黑【1000底】——通用', '2020-06-01 10:06:57', '2020-06-01 10:07:01');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (39, 1, 33, '邵阳剥皮红黑', 3, 1, '1,33,0,0,0,0,0,3,200,3,0,1,0,3,10,0,0,0,0,0,0,0,0,60,0,25,2,2,0,0,0', '1000,120000,3000', 1, 0, '邵阳剥皮3人红黑【3000底】——通用', '2020-06-01 10:06:57', '2020-06-01 10:07:01');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (40, 1, 33, '邵阳剥皮红黑', 3, 1, '1,33,0,0,0,0,0,3,200,3,0,1,0,3,10,0,0,0,0,0,0,0,0,60,0,25,2,2,0,0,0', '3000,400000,10000', 1, 0, '邵阳剥皮3人红黑【10000底】——通用', '2020-06-01 10:06:57', '2020-06-01 10:07:01');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (41, 1, 222, '长沙麻将双色6鸟加分', 2, 1, '1,222,3,1,6,0,1,2,1,1,1,0,1,0,1,1,0,1,0,0,5,2,2,0,0,0,1,0,60,2,1,28,0,1,0,0,0,0,1,0,0,0,0,0', '200,1500,100', 1, 0, '长沙麻将双色抓6鸟加分【100底】——通用', '2020-06-01 10:26:00', '2020-06-01 10:26:04');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (42, 1, 222, '长沙麻将双色6鸟加分', 2, 1, '1,222,3,1,6,0,1,2,1,1,1,0,1,0,1,1,0,1,0,0,5,2,2,0,0,0,1,0,60,2,1,28,0,1,0,0,0,0,1,0,0,0,0,0', '400,15000,500', 1, 0, '长沙麻将双色抓6鸟加分【500底】——通用', '2020-06-01 10:26:00', '2020-06-01 10:26:04');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (43, 1, 222, '长沙麻将双色6鸟加分', 2, 1, '1,222,3,1,6,0,1,2,1,1,1,0,1,0,1,1,0,1,0,0,5,2,2,0,0,0,1,0,60,2,1,28,0,1,0,0,0,0,1,0,0,0,0,0', '500,30000,1000', 1, 0, '长沙麻将双色抓6鸟加分【1000底】——通用', '2020-06-01 10:26:00', '2020-06-01 10:26:04');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (44, 1, 222, '长沙麻将双色6鸟加分', 2, 1, '1,222,3,1,6,0,1,2,1,1,1,0,1,0,1,1,0,1,0,0,5,2,2,0,0,0,1,0,60,2,1,28,0,1,0,0,0,0,1,0,0,0,0,0', '800,60000,2000', 1, 0, '长沙麻将双色抓6鸟加分【2000底】——通用', '2020-06-01 10:26:00', '2020-06-01 10:26:04');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (45, 1, 222, '长沙麻将双色6鸟加分', 2, 1, '1,222,3,1,6,0,1,2,1,1,1,0,1,0,1,1,0,1,0,0,5,2,2,0,0,0,1,0,60,2,1,28,0,1,0,0,0,0,1,0,0,0,0,0', '2000,300000,10000', 1, 0, '长沙麻将双色抓6鸟加分【10000底】——通用', '2020-06-01 10:26:00', '2020-06-01 10:26:04');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (46, 1, 222, '长沙麻将三色2鸟加倍', 2, 1, '1,222,3,3,2,0,1,2,1,1,1,1,1,1,1,1,0,1,0,0,5,2,2,0,0,0,0,1,60,1,0,42,0,1,0,0,0,0,1,0,0,1,0,1', '300,3000,100', 1, 0, '长沙麻将三色2鸟加倍【100底】——通用', '2020-06-01 10:26:00', '2020-06-01 10:26:04');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (47, 1, 222, '长沙麻将三色2鸟加倍', 2, 1, '1,222,3,3,2,0,1,2,1,1,1,1,1,1,1,1,0,1,0,0,5,2,2,0,0,0,0,1,60,1,0,42,0,1,0,0,0,0,1,0,0,1,0,1', '600,20000,500', 1, 0, '长沙麻将三色2鸟加倍【500底】——通用', '2020-06-01 10:26:00', '2020-06-01 10:26:04');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (48, 1, 222, '长沙麻将三色2鸟加倍', 2, 1, '1,222,3,3,2,0,1,2,1,1,1,1,1,1,1,1,0,1,0,0,5,2,2,0,0,0,0,1,60,1,0,42,0,1,0,0,0,0,1,0,0,1,0,1', '8000,40000,1000', 1, 0, '长沙麻将三色2鸟加倍【1000底】——通用', '2020-06-01 10:26:00', '2020-06-01 10:26:04');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (49, 1, 222, '长沙麻将三色2鸟加倍', 2, 1, '1,222,3,3,2,0,1,2,1,1,1,1,1,1,1,1,0,1,0,0,5,2,2,0,0,0,0,1,60,1,0,42,0,1,0,0,0,0,1,0,0,1,0,1', '1000,80000,2000', 1, 0, '长沙麻将三色2鸟加倍【2000底】——通用', '2020-06-01 10:26:00', '2020-06-01 10:26:04');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (50, 1, 222, '长沙麻将三色2鸟加倍', 2, 1, '1,222,3,3,2,0,1,2,1,1,1,1,1,1,1,1,0,1,0,0,5,2,2,0,0,0,0,1,60,1,0,42,0,1,0,0,0,0,1,0,0,1,0,1', '3000,400000,10000', 1, 0, '长沙麻将三色2鸟加倍【10000底】——通用', '2020-06-01 10:26:00', '2020-06-01 10:26:04');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (51, 1, 221, '红中麻将可点炮6鸟加分', 2, 1, '1,221,3,6,0,1,1,2,60,1,0,0,0,1,1,0,0,0,1,1,0,5,2,0,0,1,2,1,2,0,0,0,0,0,0,1,0,0', '200,1500,100', 1, 0, '红中麻将可点炮6鸟加1分【100底】——通用', '2020-06-01 10:26:00', '2020-06-01 11:10:24');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (52, 1, 221, '红中麻将可点炮6鸟加分', 2, 1, '1,221,3,6,0,1,1,2,60,1,0,0,0,1,1,0,0,0,1,1,0,5,2,0,0,1,2,1,2,0,0,0,0,0,0,1,0,0', '400,15000,500', 1, 0, '红中麻将可点炮6鸟加1分【500底】——通用', '2020-06-01 10:26:00', '2020-06-01 11:10:25');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (53, 1, 221, '红中麻将可点炮6鸟加分', 2, 1, '1,221,3,6,0,1,1,2,60,1,0,0,0,1,1,0,0,0,1,1,0,5,2,0,0,1,2,1,2,0,0,0,0,0,0,1,0,0', '500,30000,1000', 1, 0, '红中麻将可点炮6鸟加1分【1000底】——通用', '2020-06-01 10:26:00', '2020-06-01 11:10:25');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (54, 1, 221, '红中麻将可点炮6鸟加分', 2, 1, '1,221,3,6,0,1,1,2,60,1,0,0,0,1,1,0,0,0,1,1,0,5,2,0,0,1,2,1,2,0,0,0,0,0,0,1,0,0', '800,60000,2000', 1, 0, '红中麻将可点炮6鸟加1分【2000底】——通用', '2020-06-01 10:26:00', '2020-06-01 11:10:26');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (55, 1, 221, '红中麻将可点炮6鸟加分', 2, 1, '1,221,3,6,0,1,1,2,60,1,0,0,0,1,1,0,0,0,1,1,0,5,2,0,0,1,2,1,2,0,0,0,0,0,0,1,0,0', '2000,300000,10000', 1, 0, '红中麻将可点炮6鸟加1分【10000底】——通用', '2020-06-01 10:26:00', '2020-06-01 11:10:41');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (56, 1, 117, '打筒子三副可带牌', 2, 1, '1,117,3,600,0,1,0,2,1,1,1,0,60,1,0,2', '200,1500,10', 1, 0, '打筒子三副可带牌【10底】——通用', '2020-06-01 10:26:00', '2020-06-01 10:26:04');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (57, 1, 117, '打筒子三副可带牌', 2, 1, '1,117,3,600,0,1,0,2,1,1,1,0,60,1,0,2', '400,15000,50', 1, 0, '打筒子三副可带牌【50底】——通用', '2020-06-01 10:26:00', '2020-06-01 10:26:04');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (58, 1, 117, '打筒子三副可带牌', 2, 1, '1,117,3,600,0,1,0,2,1,1,1,0,60,1,0,2', '500,30000,100', 1, 0, '打筒子三副可带牌【100底】——通用', '2020-06-01 10:26:00', '2020-06-01 10:26:04');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (59, 1, 117, '打筒子三副可带牌', 2, 1, '1,117,3,600,0,1,0,2,1,1,1,0,60,1,0,2', '800,60000,200', 1, 0, '打筒子三副可带牌【200底】——通用', '2020-06-01 10:26:00', '2020-06-01 10:26:04');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (60, 1, 117, '打筒子三副可带牌', 2, 1, '1,117,3,600,0,1,0,2,1,1,1,0,60,1,0,2', '2000,300000,1000', 1, 0, '打筒子三副可带牌【1000底】——通用', '2020-06-01 10:26:00', '2020-06-01 10:26:04');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (61, 1, 210, '打筒子四喜不可带牌', 2, 1, '1,210,3,600,0,1,0,2,1,1,1,0,60,0,2,2', '200,1500,10', 1, 0, '打筒子四喜不可带牌【10底】——通用', '2020-06-01 10:26:00', '2020-06-01 10:26:04');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (62, 1, 210, '打筒子四喜不可带牌', 2, 1, '1,210,3,600,0,1,0,2,1,1,1,0,60,0,2,2', '400,15000,50', 1, 0, '打筒子四喜不可带牌【50底】——通用', '2020-06-01 10:26:00', '2020-06-01 10:26:04');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (63, 1, 210, '打筒子四喜不可带牌', 2, 1, '1,210,3,600,0,1,0,2,1,1,1,0,60,0,2,2', '500,30000,100', 1, 0, '打筒子四喜不可带牌【100底】——通用', '2020-06-01 10:26:00', '2020-06-01 10:26:04');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (64, 1, 210, '打筒子四喜不可带牌', 2, 1, '1,210,3,600,0,1,0,2,1,1,1,0,60,0,2,2', '800,60000,200', 1, 0, '打筒子四喜不可带牌【200底】——通用', '2020-06-01 10:26:00', '2020-06-01 10:26:04');
INSERT INTO `t_gold_room_config`(`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `goldMsg`, `areaId`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (65, 1, 210, '打筒子四喜不可带牌', 2, 1, '1,210,3,600,0,1,0,2,1,1,1,0,60,0,2,2', '2000,300000,1000', 1, 0, '打筒子四喜不可带牌【1000底】——通用', '2020-06-01 10:26:00', '2020-06-01 10:26:04');

INSERT INTO `t_solo_room_config`(`keyId`, `soloType`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (1, 1, 1, 16, '16张跑得快挑战赛', 2, 1, '1,16,0,0,0,0,0,2,1,3,0,3,0,0,0,0,0,0,0,0,0,0,0,10,2,0,0,1,0,1,0,0,0,0', 0, '', '2020-05-26 19:11:49', '2020-06-01 20:07:33');
INSERT INTO `t_solo_room_config`(`keyId`, `soloType`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (2, 1, 1, 33, '邵阳剥皮挑战赛', 2, 1, '1,33,0,0,0,0,0,2,200,3,200,0,0,3,0,0,0,0,0,0,0,0,0,0,0,25,2,2,0,0,0', 0, '', '2020-05-29 15:43:25', '2020-06-01 20:07:34');
INSERT INTO `t_solo_room_config`(`keyId`, `soloType`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (3, 1, 1, 221, '红中麻将挑战赛', 2, 1, '1,221,3,0,0,0,1,2,0,1,0,0,0,1,0,0,0,0,1,0,0,5,2,0,0,0,2,0,2,0,0,0,0,0,0,0,0,0', 0, '', '2020-05-29 15:46:16', '2020-06-01 20:07:34');
INSERT INTO `t_solo_room_config`(`keyId`, `soloType`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `order`, `desc`, `createdTime`, `lastUpTime`) VALUES (4, 1, 1, 231, '三打哈挑战赛', 2, 1, '1,231,3,0,1,0,0,2,0,80,0,1,0,0,0,1,0,0,0,0', 0, '', '2020-05-29 15:48:15', '2020-06-01 20:07:35');


INSERT INTO `t_goods_item`(`id`, `type`, `name`, `desc`, `discount`, `amount`, `count`, `give`, `ratio`, `createdTime`, `lasatUpTime`) VALUES (1, 1, '1000白金豆', '1000白金豆', 100, 1, 1000, 0, 100, '2020-05-25 16:15:23', '2020-05-27 17:38:55');
INSERT INTO `t_goods_item`(`id`, `type`, `name`, `desc`, `discount`, `amount`, `count`, `give`, `ratio`, `createdTime`, `lasatUpTime`) VALUES (2, 1, '10000白金豆', '10000白金豆', 100, 10, 10000, 0, 100, '2020-05-25 17:06:21', '2020-05-27 17:38:57');
INSERT INTO `t_goods_item`(`id`, `type`, `name`, `desc`, `discount`, `amount`, `count`, `give`, `ratio`, `createdTime`, `lasatUpTime`) VALUES (3, 1, '100000白金豆', '100000白金豆', 100, 100, 100000, 0, 100, '2020-06-01 20:07:05', '2020-06-01 20:07:11');


INSERT INTO `seven_sign_config`(`id`, `dayNum`, `goldNum`) VALUES (1, 1, 200);
INSERT INTO `seven_sign_config`(`id`, `dayNum`, `goldNum`) VALUES (2, 2, 500);
INSERT INTO `seven_sign_config`(`id`, `dayNum`, `goldNum`) VALUES (3, 3, 800);
INSERT INTO `seven_sign_config`(`id`, `dayNum`, `goldNum`) VALUES (4, 4, 1300);
INSERT INTO `seven_sign_config`(`id`, `dayNum`, `goldNum`) VALUES (5, 5, 1800);
INSERT INTO `seven_sign_config`(`id`, `dayNum`, `goldNum`) VALUES (6, 6, 2400);
INSERT INTO `seven_sign_config`(`id`, `dayNum`, `goldNum`) VALUES (7, 7, 3000);

INSERT INTO `t_gold_room_area`(`keyId`, `parentId`, `state`, `name`, `order`, `createdTime`, `lastUpTime`) VALUES (1, 0, 1, '通用', 0, '2020-05-14 15:43:14', '2020-05-29 09:38:06');
INSERT INTO `t_gold_room_area`(`keyId`, `parentId`, `state`, `name`, `order`, `createdTime`, `lastUpTime`) VALUES (2, 0, 1, '长沙', 1, '2020-05-15 11:03:50', '2020-05-29 09:38:18');
INSERT INTO `t_gold_room_area`(`keyId`, `parentId`, `state`, `name`, `order`, `createdTime`, `lastUpTime`) VALUES (3, 0, 1, '株洲', 2, '2020-05-15 11:03:54', '2020-05-29 09:38:19');
INSERT INTO `t_gold_room_area`(`keyId`, `parentId`, `state`, `name`, `order`, `createdTime`, `lastUpTime`) VALUES (4, 0, 1, '邵阳', 3, '2020-05-15 11:04:11', '2020-05-29 09:38:20');


ALTER TABLE `t_gold_room`
ADD COLUMN `groupIdLimit` tinyint(1) NULL DEFAULT 0 COMMENT '亲友圈限制：0否，1是' AFTER `tableId`;

ALTER TABLE `t_gold_room_freeze`
ADD COLUMN `groupIdLimit` tinyint(1) NULL DEFAULT 0 COMMENT '亲友圈限制：0否，1是' AFTER `tableId`;

ALTER TABLE `mission_config`
ADD COLUMN `ext` varchar(50) NULL DEFAULT "" COMMENT '额外限制条件';

-- ALTER TABLE mission_about DROP COLUMN completeId;

ALTER TABLE `mission_config`
ADD COLUMN `onOff` tinyint(1) NULL DEFAULT 1 COMMENT '开关';

ALTER TABLE `mission_config`
ADD COLUMN `sort` INT(2) NULL DEFAULT 0 COMMENT '排序';

-- 20200612金币场大厅
CREATE TABLE `t_gold_room_hall` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8_bin DEFAULT '' COMMENT '名字',
  `type` tinyint(1) DEFAULT '1' COMMENT '类型：1、打开玩法列表，2、直接匹配模式',
  `extMsg` varchar(255) COLLATE utf8_bin DEFAULT '' COMMENT '属性',
  `playTypes` varchar(255) COLLATE utf8_bin DEFAULT '' COMMENT '玩法列表',
  `description` varchar(255) COLLATE utf8_bin DEFAULT '' COMMENT '描述',
  `createdTime` datetime DEFAULT NULL COMMENT '创建时间',
  `lastUpTime` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后一次更新',
  PRIMARY KEY (`keyId`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- 亲友圈金币房间模式
ALTER TABLE `t_group_table_config`
ADD COLUMN `goldMsg` varchar(100) NULL DEFAULT '' COMMENT '金币房配置';

ALTER TABLE `t_group_table`
ADD COLUMN `goldMsg` varchar(100) NULL DEFAULT '' COMMENT '金币房配置';

CREATE TABLE `log_group_gold_win` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `dataDate` bigint(20) NOT NULL DEFAULT '0' COMMENT '日期：格式20160101',
  `groupId` bigint(20) DEFAULT '0' COMMENT '俱乐部id',
  `userId` bigint(20) DEFAULT '0' COMMENT '用户id',
  `win` bigint(20) DEFAULT '0' COMMENT '所有下级及自己（selfWin > 0）',
  `lose` bigint(20) DEFAULT '0' COMMENT '所有下级及自己（selfWin < 0）',
  `jsCount` int(10) DEFAULT '0' COMMENT '所有下级及自己',
  `selfGroupWin` bigint(20) DEFAULT '0' COMMENT '自己及下一级普通成员',
  `selfGroupLose` bigint(20) DEFAULT '0' COMMENT '自己及下一级普通成员',
  `selfGroupJsCount` int(20) DEFAULT '0' COMMENT '自己及下一级普通成员',
  `selfWin` bigint(20) DEFAULT '0' COMMENT '自己总输赢',
  `selfJsCount` int(10) DEFAULT '0' COMMENT '自己局数',
  `tag` smallint(1) DEFAULT '0' COMMENT '标记：0未标记，1已标记',
  `lastUpTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `idx_uniq` (`dataDate`,`groupId`,`userId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4168 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


INSERT INTO `t_gold_room_hall`(`keyId`, `name`, `type`, `extMsg`, `playTypes`, `description`, `createdTime`, `lastUpTime`) VALUES (1, '跑得快', 1, '1001,1', '15,16', '跑得快', '2020-06-12 15:13:43', '2020-06-12 17:48:48');
INSERT INTO `t_gold_room_hall`(`keyId`, `name`, `type`, `extMsg`, `playTypes`, `description`, `createdTime`, `lastUpTime`) VALUES (2, '邵阳剥皮', 1, '2001,2', '33', '邵阳剥皮', '2020-06-12 16:26:29', '2020-06-12 17:48:50');
INSERT INTO `t_gold_room_hall`(`keyId`, `name`, `type`, `extMsg`, `playTypes`, `description`, `createdTime`, `lastUpTime`) VALUES (3, '红中麻将', 1, '3001,3', '221', '红中麻将', '2020-06-12 16:27:14', '2020-06-12 17:48:52');
INSERT INTO `t_gold_room_hall`(`keyId`, `name`, `type`, `extMsg`, `playTypes`, `description`, `createdTime`, `lastUpTime`) VALUES (4, '长沙麻将', 1, '3002,4', '222', '长沙麻将', '2020-06-12 16:29:54', '2020-06-12 17:48:56');
INSERT INTO `t_gold_room_hall`(`keyId`, `name`, `type`, `extMsg`, `playTypes`, `description`, `createdTime`, `lastUpTime`) VALUES (5, '打筒子', 1, '1002,5', '117,210', '打筒子', '2020-06-12 16:51:30', '2020-06-12 17:48:58');
INSERT INTO `t_gold_room_hall`(`keyId`, `name`, `type`, `extMsg`, `playTypes`, `description`, `createdTime`, `lastUpTime`) VALUES (6, '娄底放炮罚', 1, '2002,6', '199', '娄底放炮罚', '2020-06-12 16:52:16', '2020-06-16 15:37:04');
INSERT INTO `t_gold_room_hall`(`keyId`, `name`, `type`, `extMsg`, `playTypes`, `description`, `createdTime`, `lastUpTime`) VALUES (7, '斗地主', 1, '1003,7', '264', '斗地主', '2020-06-15 10:20:39', '2020-06-16 17:20:08');

-- 20200619
ALTER TABLE `user_extendinf`
ADD COLUMN `lastUpNameTime` datetime(0) NULL COMMENT '最后修改昵称时间' ,
ADD COLUMN `lastUpHeadimgTime` datetime(0) NULL COMMENT '最后修改头像时间',
ADD COLUMN `pwErrorMsg` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '' COMMENT '密码错误限制' AFTER `lastUpHeadimgTime`;

-- 20200621
ALTER TABLE `activity`
ADD COLUMN `showBeginTime` datetime(0) NULL COMMENT '活动面板开始展示，早于此时间则无法查看该活动内容' ,
ADD COLUMN `showEndTime` datetime(0) NULL COMMENT '活动面板结束展示，晚于此时间则无法查看该活动内容';

ALTER TABLE `mission_about`
ADD COLUMN `ext` varchar(255) NULL DEFAULT '' COMMENT '扩展字段';

-- 20200623 d端午粽子活动物品表

CREATE TABLE `t_gold_room_activity_user_item` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userid` bigint(20) NOT NULL COMMENT '用户id',
  `activityBureau` varchar(200) DEFAULT '' COMMENT '活动局数详情',
  `activityItemNum` bigint(20) DEFAULT '0' COMMENT '活动物品',
  `daterecord` varchar(500) DEFAULT '' COMMENT '活动日期',
  `activityDesc` varchar(50) DEFAULT '' COMMENT '活动描述',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifiedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isReward` tinyint(1) DEFAULT '0' COMMENT '0:未领取；1已领取；',
  PRIMARY KEY (`keyId`) USING BTREE,
  KEY `idx_ac_userid` (`userid`) USING BTREE,
  KEY `idx_ac_activityItemNum` (`activityItemNum`) USING BTREE,
  KEY `idx_ac_modifiedTime` (`modifiedTime`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1033 DEFAULT CHARSET=utf8 COMMENT='金币场活动物品数量表';

INSERT INTO `activity`( `beginTime`, `endTime`, `them`, `showContent`, `extend`, `showBeginTime`, `showEndTime`) VALUES ('2020-06-25 00:00:00', '2020-06-27 23:59:59', '101', '20年端午龙舟活动', NULL, '2020-06-21 00:00:00', '2020-06-27 23:59:59');
INSERT INTO `mission_config`( `tag`, `missionExplain`, `awardExplain`, `type`, `finishNum`, `awardId`, `awardIcon`, `awardNum`, `onOff`, `sort`, `ext`) VALUES ( 101, '龙舟5场', '奖励500', 2, 5, 1, 1, 500, 1, 0,'');
INSERT INTO `mission_config`( `tag`, `missionExplain`, `awardExplain`, `type`, `finishNum`, `awardId`, `awardIcon`, `awardNum`, `onOff`, `sort`, `ext`) VALUES ( 101, '龙舟10场', '奖励1500', 2, 10, 1, 1, 1500, 1, 0,'');
INSERT INTO `mission_config`( `tag`, `missionExplain`, `awardExplain`, `type`, `finishNum`, `awardId`, `awardIcon`, `awardNum`, `onOff`, `sort`, `ext`) VALUES ( 101, '龙舟20场', '奖励3000', 2, 20, 1, 1, 3000, 1, 0,'');
INSERT INTO `mission_config`( `tag`, `missionExplain`, `awardExplain`, `type`, `finishNum`, `awardId`, `awardIcon`, `awardNum`, `onOff`, `sort`, `ext`) VALUES ( 101, '龙舟30场', '奖励5000', 2, 30, 1, 1, 5000, 1, 0,'');
INSERT INTO `mission_config`( `tag`, `missionExplain`, `awardExplain`, `type`, `finishNum`, `awardId`, `awardIcon`, `awardNum`, `onOff`, `sort`, `ext`) VALUES ( 101, '龙舟50场', '奖励10000', 2, 50, 1, 1, 10000, 1, 0,'');
-- 端午活动配置 login库
INSERT INTO  `t_resources_configs` (  `msgType`, `msgKey`, `msgValue`, `msgDesc`  ) VALUES (  'GoldRoomActivityConfig', 'novice', '1,1,2;1,6,11,16,21,26,31,36,41,46,51,68,56,61,67,69', '粽子活动新手场配置' );
INSERT INTO  `t_resources_configs` (  `msgType`, `msgKey`, `msgValue`, `msgDesc` ) VALUES (  'GoldRoomActivityConfig', 'senior', '1,4,2;3,8,13,18,23,28,33,38,43,48,53,60,65', '粽子活动高级场配置' );
INSERT INTO  `t_resources_configs` (  `msgType`, `msgKey`, `msgValue`, `msgDesc` ) VALUES (  'GoldRoomActivityConfig', 'primary', '1,2,2;73,66,70,71,72', '粽子活动初级场配置' );
INSERT INTO  `t_resources_configs` (  `msgType`, `msgKey`, `msgValue`, `msgDesc` ) VALUES (  'GoldRoomActivityConfig', 'mediate', '1,3,2;2,7,12,17,22,27,32,37,42,47,52,57,62', '粽子活动中级场配置' );

INSERT INTO  `activity` ( `beginTime`, `endTime`, `them`, `showContent`, `extend`, `showBeginTime`, `showEndTime`) VALUES (  '2020-06-24 00:00:00', '2020-06-27 23:59:59', '102', '欢乐金币攒粽子', '1_5', '2020-06-24 00:00:00', '2020-06-27 23:59:59');



-- 20200629
ALTER TABLE `t_group_user_friend`
ADD COLUMN `userIdKeyStr` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '用户1id*10000拼接用户2id整数，规则：id小的在前';

update t_group_user_friend set userIdKeyStr = userIdkey;

ALTER TABLE `t_group_user_friend`
ADD INDEX `idx_gid_userIdkeyStr`(`groupId`, `userIdKeyStr`) USING BTREE;


ALTER TABLE `t_group_user_reject`
ADD COLUMN `userIdKeyStr` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '用户1id*10000拼接用户2id整数，规则：id小的在前';

update t_group_user_reject set userIdKeyStr = userIdkey;

ALTER TABLE `t_group_user_reject`
ADD INDEX `idx_gid_userIdkeyStr`(`groupId`, `userIdKeyStr`) USING BTREE;








------------------------------比赛场

/*
SQLyog Ultimate v12.09 (64 bit)
MySQL - 5.7.19 : Database - testbjdqplogin
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`testbjdqplogin` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `testbjdqplogin`;

/*Table structure for table `t_competition_apply` */

CREATE TABLE `t_competition_apply` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) NOT NULL,
  `consumerId` int(11) NOT NULL COMMENT '货币类型',
  `consumerVal` int(11) NOT NULL COMMENT '具体消耗',
  `shareFreeSign` tinyint(1) DEFAULT NULL COMMENT '分享免费报名',
  `playingId` bigint(20) NOT NULL COMMENT '赛场ID,外键',
  `status` int(11) DEFAULT NULL COMMENT '1正常,2退赛,3退赛退费',
  `push` tinyint(1) DEFAULT '0' COMMENT '1赛前推送',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `deleteTime` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `playingid_index` (`playingId`),
  KEY `status_index` (`status`),
  KEY `push_index` (`push`),
  KEY `userid_index` (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `t_competition_clearing_award` */

CREATE TABLE `t_competition_clearing_award` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '赛事结算奖励',
  `rank` int(11) DEFAULT NULL COMMENT '排名',
  `playingId` bigint(20) DEFAULT NULL COMMENT '赛事ID',
  `userId` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `awardId` int(11) DEFAULT NULL COMMENT '奖励物品',
  `awardVal` int(11) DEFAULT NULL COMMENT '数量',
  `status` int(11) DEFAULT '0' COMMENT '状态:1到账',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleteTime` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `t_competition_playing` */

CREATE TABLE `t_competition_playing` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bindServerId` bigint(20) DEFAULT NULL COMMENT '进阶赛绑定的ID',
  `playingConfigId` bigint(20) NOT NULL COMMENT '比赛配置ID外键',
  `titleType` int(11) DEFAULT NULL COMMENT '赛制入口:1话费赛,2赢豆赛',
  `type` int(11) DEFAULT NULL COMMENT '比赛大类型',
  `category` int(11) DEFAULT NULL COMMENT '比赛小类型',
  `entrance` int(11) DEFAULT NULL COMMENT '赛事入口, 与类型子类型入口联合主键',
  `consumerId` int(11) DEFAULT NULL COMMENT '消耗类型',
  `consumerVal` int(11) DEFAULT NULL COMMENT '消耗数值',
  `shareFreeSign` tinyint(1) DEFAULT NULL COMMENT '允许分享免费报名',
  `initScore` int(11) DEFAULT NULL COMMENT '初始积分',
  `curHuman` int(11) DEFAULT '0' COMMENT '当前参与人数',
  `beginHuman` int(11) DEFAULT NULL COMMENT '可开始参与人数阈值,结合开赛时间使用',
  `maxHuman` int(11) DEFAULT NULL COMMENT '最大人数阈值',
  `endHuman` int(11) DEFAULT NULL COMMENT '赛事可结束人数阈值,结合开赛时间使用',
  `stepOutDesc` varchar(500) DEFAULT NULL COMMENT '单局淘汰策略json',
  `titleCode` varchar(100) DEFAULT NULL COMMENT '标题国际化code',
  `desc` varchar(100) DEFAULT NULL COMMENT '描述',
  `status` int(11) DEFAULT NULL COMMENT '0等待报名,1报名中,2比赛中,3结算中,4结算完毕,6人数不足或淘汰的',
  `beginPlayingPushStatus` int(11) DEFAULT '0' COMMENT '赛前推送',
  `applyBeforeMin` int(11) DEFAULT '0' COMMENT '赛前n分钟开始报名',
  `applyBefore` timestamp NULL DEFAULT NULL COMMENT '报名开始时间',
  `applyAfter` timestamp NULL DEFAULT NULL COMMENT '报名结束时间,没有结束时间则使用开赛时间作为结束报名时间',
  `matchBefore` timestamp NULL DEFAULT NULL COMMENT '比赛正式开始时间',
  `matchAfter` timestamp NULL DEFAULT NULL COMMENT '比赛结束时间',
  `upStep` int(11) DEFAULT '0' COMMENT '上一个进行到的轮次',
  `upRound` int(11) DEFAULT '0' COMMENT '上一个进行到的回合',
  `curStep` int(11) DEFAULT '0' COMMENT '当前进行到轮次',
  `curRound` int(11) DEFAULT NULL COMMENT '当前进行到回合',
  `stepRoundDesc` varchar(300) DEFAULT NULL COMMENT '打立出局结束人数,晋级人数,n轮低于分数淘汰_n轮每桌仅前n名晋级,n轮低于分数淘汰_n轮每桌仅前n名晋级;淘汰赛结束人数,晋级人数,n轮低于分数淘汰_n轮每桌仅前n名晋级,n轮低于分数淘汰_n轮每桌仅前n名晋级',
  `iteration` tinyint(1) DEFAULT NULL COMMENT '0迭代关闭,最后一场比赛后不再生成新的比赛场次',
  `shardingTakeOver` varchar(50) DEFAULT '0' COMMENT '分片接管,当前执行该活动的分片',
  `curPlayingTableCount` int(11) DEFAULT '0' COMMENT '当前进行中的牌桌',
  `disableStartTime` varchar(50) DEFAULT NULL COMMENT '禁赛段为空不限制',
  `disableEndTime` varchar(50) DEFAULT NULL COMMENT '禁赛段为空不限制',
  `openTime` timestamp NULL DEFAULT NULL COMMENT '开赛时间',
  `logo` varchar(200) DEFAULT NULL COMMENT 'logo',
  `ext` varchar(300) DEFAULT NULL COMMENT '额外字段,开赛数据',
  `orderField` int(11) DEFAULT '0' COMMENT '排序越大越高',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleteTime` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `status_index` (`status`),
  KEY `playing_config_id_index` (`playingConfigId`),
  KEY `apply_match_index` (`applyAfter`,`matchBefore`),
  KEY `type_status_index` (`type`,`status`),
  KEY `playing_type_category_index` (`titleType`,`type`,`category`,`entrance`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `t_competition_playing_config` */

CREATE TABLE `t_competition_playing_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `titleType` int(11) DEFAULT NULL COMMENT '赛制入口:1话费赛,2赢豆赛',
  `type` int(11) DEFAULT NULL COMMENT '比赛类型, 1报名赛(人满开赛), 2定时赛(到点开赛)',
  `category` int(11) DEFAULT NULL COMMENT '场次类型, 具体玩法',
  `entrance` int(11) DEFAULT NULL COMMENT '赛事入口, 与类型子类型入口联合主键',
  `iterationType` int(11) DEFAULT NULL COMMENT '赛事类型:1分钟赛,2日赛,3周赛',
  `iterationMin` int(11) DEFAULT NULL COMMENT '迭代分钟, 从创建比赛开始计算, n分钟之后重新生成一个未开赛的比赛',
  `consumerId` int(11) DEFAULT NULL COMMENT '消耗',
  `consumerVal` int(11) DEFAULT NULL COMMENT '消耗值',
  `shareFreeSign` tinyint(1) DEFAULT '1' COMMENT '分享免费报名',
  `initScore` int(11) DEFAULT NULL COMMENT '初始积分',
  `beginHuman` int(11) DEFAULT NULL COMMENT '开赛人数阈值',
  `maxHuman` int(11) DEFAULT NULL COMMENT '最大人数阈值',
  `endHuman` int(11) DEFAULT NULL COMMENT '剩余人数就结算阈值,这个值仅在轮次不满足最大轮次时但是人数又不足下一轮时触发, 如: 3人,轮次10,小结后轮次仅5,剩余3人触发大结',
  `titleCode` varchar(50) DEFAULT NULL COMMENT '赛事名称 (国际化)',
  `beforeXMinPushTitle` varchar(255) DEFAULT NULL COMMENT '赛前推送描述',
  `desc` varchar(300) DEFAULT NULL COMMENT '比赛描述',
  `applyBeforeMin` int(11) DEFAULT '0' COMMENT '开赛前N分钟允许报名',
  `applyBefore` varchar(10) DEFAULT NULL COMMENT '报名开始时间',
  `applyAfter` varchar(10) DEFAULT NULL COMMENT '报名结束时间',
  `matchBefore` varchar(10) DEFAULT NULL COMMENT '赛事开启时间段开始',
  `matchAfter` varchar(10) DEFAULT NULL COMMENT '赛事开启时间段结束',
  `stepRoundDesc` varchar(200) DEFAULT NULL COMMENT '打立出局结束人数,晋级人数,分数淘汰,每桌前n名晋级;淘汰赛结束人数,晋级人数,分数淘汰,每桌前n名晋级',
  `iteration` tinyint(1) DEFAULT NULL COMMENT '迭代0关闭, 最后一场比赛后不再生成新的比赛场次',
  `shardingTakeOver` varchar(50) DEFAULT '0' COMMENT '分片接管, 当前执行该任务的分片',
  `stepRate` varchar(200) DEFAULT NULL COMMENT '每轮底分变更 轮次_底分',
  `stepConvertRate` varchar(200) DEFAULT NULL COMMENT '每轮以百分比结算分数的倍率  A,A1;B;C',
  `roomConfigIds` varchar(200) NOT NULL COMMENT '创房选项id: 打立出局回合配置id,打立出局回合配置id,;淘汰赛回合配置id,淘汰赛回合配置id',
  `awards` varchar(500) DEFAULT NULL COMMENT '清算奖励',
  `disableStartTime` varchar(50) DEFAULT NULL COMMENT '开赛时段',
  `disableEndTime` varchar(50) DEFAULT NULL COMMENT '禁赛时段',
  `loginCallBackUrl` varchar(255) DEFAULT NULL COMMENT '回调url',
  `logo` varchar(200) DEFAULT NULL COMMENT 'logo展示',
  `startBeforeNotifyExt` varchar(255) DEFAULT NULL COMMENT '赛前推送分钟',
  `orderField` int(11) DEFAULT '0' COMMENT '排序越大越高',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleteTime` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `type_category_index` (`titleType`,`type`,`category`,`entrance`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `t_competition_room` */

CREATE TABLE `t_competition_room` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tableId` bigint(20) DEFAULT '0' COMMENT '牌桌id',
  `configId` bigint(20) DEFAULT NULL COMMENT 't_competition_room_config.keyId',
  `playingId` bigint(20) DEFAULT NULL COMMENT '赛场id',
  `modeId` varchar(50) DEFAULT '' COMMENT '配置',
  `serverId` tinyint(1) NOT NULL COMMENT '服务id',
  `currentCount` tinyint(1) NOT NULL COMMENT '当前人数',
  `maxCount` tinyint(1) NOT NULL COMMENT '最大人数',
  `gameCount` tinyint(1) NOT NULL COMMENT '局数',
  `currentState` tinyint(1) NOT NULL COMMENT '状态：0、未开始，1、可匹配，2、开局，3、结束',
  `tableMsg` varchar(255) NOT NULL DEFAULT '' COMMENT '房间信息',
  `ext` varchar(255) DEFAULT '' COMMENT '额外信息',
  `tableName` varchar(100) DEFAULT '' COMMENT '名字',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifiedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `createParamClearingModel` varchar(500) DEFAULT NULL COMMENT '创房时登陆服传递选项',
  PRIMARY KEY (`keyId`) USING BTREE,
  KEY `idx_common` (`configId`,`currentState`,`serverId`) USING BTREE,
  KEY `idx_createdTime` (`createdTime`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='无房号比赛场房间';

/*Table structure for table `t_competition_room_config` */

CREATE TABLE `t_competition_room_config` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `state` tinyint(1) DEFAULT '1' COMMENT '状态：1、有效，2、失效',
  `playType` int(10) DEFAULT '0' COMMENT '玩法',
  `name` varchar(50) COLLATE utf8_bin DEFAULT '' COMMENT '名字',
  `playerCount` tinyint(1) DEFAULT '0' COMMENT '人数',
  `totalBureau` tinyint(1) DEFAULT '0' COMMENT '局数',
  `tableMsg` varchar(100) COLLATE utf8_bin DEFAULT '' COMMENT '玩法配置',
  `strParams` varchar(100) COLLATE utf8_bin DEFAULT '' COMMENT '额外信息',
  `order` int(10) DEFAULT '0' COMMENT '排序值',
  `desc` varchar(500) COLLATE utf8_bin DEFAULT '' COMMENT '描述信息',
  `createdTime` datetime DEFAULT NULL COMMENT '创建时间',
  `lastUpTime` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`keyId`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='比赛场玩法配置表';

/*Table structure for table `t_competition_room_user` */

CREATE TABLE `t_competition_room_user` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `roomId` bigint(20) DEFAULT '0' COMMENT '房间id',
  `userId` varchar(32) NOT NULL COMMENT '玩家ID',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gameResult` int(11) NOT NULL DEFAULT '0' COMMENT '结果',
  `logIds` varchar(512) DEFAULT NULL COMMENT 'logId多个以逗号隔开',
  `initScore` int(11) DEFAULT NULL COMMENT '初始积分',
  `playingId` bigint(20) NOT NULL COMMENT '赛场ID',
  PRIMARY KEY (`keyId`),
  UNIQUE KEY `userId_playingId_unique` (`playingId`,`userId`),
  UNIQUE KEY `idx_userId` (`userId`),
  UNIQUE KEY `idx_room` (`roomId`,`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='无房号比赛场匹配玩家';

/*Table structure for table `t_competition_score_detail` */

CREATE TABLE `t_competition_score_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '赛事积分流水',
  `userId` bigint(20) DEFAULT NULL,
  `playingId` bigint(20) DEFAULT NULL COMMENT '赛场ID',
  `lastScore` int(11) DEFAULT NULL COMMENT '当前积分',
  `lastRank` int(11) DEFAULT NULL COMMENT '当前名次',
  `lastTableRank` int(11) DEFAULT NULL COMMENT '桌子内结算排名',
  `lastStep` int(11) DEFAULT NULL COMMENT '当前轮次',
  `lastRound` int(11) DEFAULT NULL COMMENT '当前回合',
  `lastStatus` int(11) DEFAULT NULL COMMENT '1:晋级,2:淘汰',
  `remark` varchar(500) DEFAULT NULL COMMENT '结算消息备注',
  `scoreBasicRatio` float DEFAULT '1' COMMENT '积分换算比例',
  `roomId` bigint(20) DEFAULT NULL COMMENT '结算房间号',
  `weedTopNumber` int(11) DEFAULT NULL COMMENT '淘汰策略:名次',
  `weedTopScore` int(11) DEFAULT NULL COMMENT '淘汰策略:分数',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleteTime` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `playingId_step_round_roomId_unique` (`userId`,`playingId`,`lastStep`,`lastRound`,`roomId`),
  KEY `playingId_index` (`playingId`),
  KEY `lastStep_index` (`lastStep`),
  KEY `lastRound_index` (`lastRound`),
  KEY `lastRank_index` (`lastRank`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `t_competition_score_total` */

CREATE TABLE `t_competition_score_total` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` bigint(20) NOT NULL COMMENT '用户ID',
  `rank` int(11) DEFAULT '0' COMMENT '当前排名',
  `score` int(11) DEFAULT '0' COMMENT '当前分数',
  `playingId` bigint(20) DEFAULT NULL COMMENT '赛事ID',
  `status` int(11) DEFAULT '1' COMMENT '1正常,2淘汰',
  `createTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleteTime` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `userId_playingId_unique` (`userId`,`playingId`),
  KEY `playingId_index` (`playingId`)
) ENGINE=InnoDB AUTO_INCREMENT=10121 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;



insert into `t_competition_room_config` (`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `strParams`, `order`, `desc`, `createdTime`, `lastUpTime`) values('1','1','16','16张跑得快无红10 底分:{p}','3','1','1,16,0,0,0,0,0,3,1,3,0,0,0,0,0,0,0,0,0,0,0,15,0,20,2,0,0,2,0,1,0,0,0,0','','0','16张跑得快3人无红10-通用-打立出局','2020-06-01 09:23:05','2020-07-02 11:42:34');
insert into `t_competition_room_config` (`keyId`, `state`, `playType`, `name`, `playerCount`, `totalBureau`, `tableMsg`, `strParams`, `order`, `desc`, `createdTime`, `lastUpTime`) values('2','1','16','16张跑得快无红10 底分:{p}','3','3','1,16,0,0,0,0,0,3,1,3,0,0,0,0,0,0,0,0,0,0,0,15,0,20,2,0,0,2,0,1,0,0,0,0','','0','16张跑得快3人无红10-通用-定局淘汰','2020-07-01 18:25:06','2020-07-02 11:42:35');

------------------------------比赛场end

-- 20200716
-- 1kz库
CREATE TABLE `t_gold_room_match_player` (
  `userId` bigint(10) NOT NULL DEFAULT '0' COMMENT '玩家ID',
  `status` tinyint(1) DEFAULT '0' COMMENT '状态：0离开匹配，1正在匹配',
  `serverId` int(10) DEFAULT '0' COMMENT '所在服id',
  `matchType` smallint(1) DEFAULT '1' COMMENT '匹配类型：1快速加入，2智能匹配',
  `playType` int(10) DEFAULT '0' COMMENT '玩法',
  `configId` bigint(20) DEFAULT '0' COMMENT '快速加入时选择配置id：t_gold_room_config.keyId',
  `groupId` bigint(20) DEFAULT '0' COMMENT '亲友圈id',
  `createdTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`userId`) USING BTREE,
  UNIQUE KEY `uniq_userId` (`userId`) USING BTREE,
  KEY `idx_serverId_status` (`serverId`,`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='此表用于服务器重启时恢复匹配队列，同时用玩家重连接';

alter table mission_config modify column type int comment '1签到，2普通对局任务，3分享任务 ,4挑战赛对局任务';
INSERT INTO `mission_config`(`tag`, `missionExplain`, `awardExplain`, `type`, `finishNum`, `awardId`, `awardIcon`, `awardNum`, `onOff`, `sort`,
 `ext`) VALUES (1, '参加比赛场任意比赛1次', '奖励200', 4, 1, 1, 1, 200, 1, 0, '');
INSERT INTO `mission_config`(`tag`, `missionExplain`, `awardExplain`, `type`, `finishNum`, `awardId`, `awardIcon`, `awardNum`, `onOff`, `sort`,
`ext`) VALUES (1, '参加比赛场任意比赛5次', '奖励500', 4, 5, 1, 1, 500, 1, 0, '');

-- 20200717
CREATE TABLE `user_gold_bean_record` (
  `id` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userId` bigint(20) NOT NULL COMMENT '玩家ID',
  `goldBean` bigint(11) NOT NULL COMMENT '玩家当前免费房卡数',
  `addGoldBean` bigint(11) NOT NULL COMMENT '玩家本次操作(消耗/获得)免费房卡数',
  `recordType` int(1) NOT NULL COMMENT '操作类型(1消耗  0获得)',
  `playType` int(11) NOT NULL COMMENT '操作所属玩法ID 0表示不属于玩法类操作',
  `sourceType` int(11) NOT NULL DEFAULT '0' COMMENT '操作来源',
  `createTime` datetime DEFAULT NULL COMMENT '操作时间',
  `groupId` bigint(20) DEFAULT '0' COMMENT '亲友圈id',
  `tableId` bigint(20) DEFAULT '0' COMMENT '牌桌id',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user` (`userId`,`createTime`) USING BTREE,
  KEY `idx_date` (`sourceType`,`createTime`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10788 DEFAULT CHARSET=utf8;

-- 20200722 金币场礼券投放活动配置 开始 login库
INSERT INTO `activity` (`beginTime`, `endTime`, `them`, `showContent`, `extend`, `showBeginTime`, `showEndTime`) VALUES ( '2020-07-20 00:00:00', '2030-07-25 23:59:59', '103', '金币场礼券活动', NULL, '2020-07-20 00:00:00', '2030-07-25 23:59:59');

ALTER TABLE `t_gold_room_activity_user_item`
ADD COLUMN `everydayLimit`  int(10) NULL AFTER `isReward`;

INSERT INTO  `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'GoldRoomGiftCertActivityConfig', 'novice',  '0,0,100,100;1', '金币场礼券活动新手场配置 1开启,送几张,每几局,每日限制;玩法');
INSERT INTO  `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'GoldRoomGiftCertActivityConfig', 'primary', '1,1,10,100;73,66,70,71,72', '金币场礼券活动初级场配置 1开启,送几张,每几局,每日限制;玩法');
INSERT INTO  `t_resources_configs` (`msgType`, `msgKey`, `msgValue`, `msgDesc` ) VALUES ( 'GoldRoomGiftCertActivityConfig', 'mediate', '1,2,10,100;2,7,12,17,22,27,32,37,42,47,52,57,62', '金币场礼券活动中级场配置 1开启,送几张,每几局,每日限制;玩法');
INSERT INTO  `t_resources_configs` ( `msgType`, `msgKey`, `msgValue`, `msgDesc`) VALUES ( 'GoldRoomGiftCertActivityConfig', 'senior',  '1,3,10,100;3,8,13,18,23,28,33,38,43,48,53,60,65,58,63', '金币场礼券活动高级场配置 1开启,送几张,每几局,每日限制;玩法');
-- 金币场礼券投放活动配置 结束


DROP TABLE IF EXISTS `user_twin_reward`;
CREATE TABLE `user_twin_reward` (
  `userId` bigint(20) NOT NULL,
  `tWinCount` int(11) DEFAULT '0' COMMENT '累计胜利次数',
  `tWinIds` varchar(32) DEFAULT '' COMMENT '领取的奖励胜利次数',
  PRIMARY KEY (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `t_twin_reward`;
CREATE TABLE `t_twin_reward` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键Id',
  `winCount` int(11) DEFAULT '0',
  `goldenBean` varchar(32) DEFAULT '' COMMENT '礼券',
  `baijinBean` varchar(32) DEFAULT '' COMMENT '白金豆',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_twin_reward
-- ----------------------------
INSERT INTO `t_twin_reward` VALUES ('1', '2', '1_2_10', '500_2000_90');
INSERT INTO `t_twin_reward` VALUES ('2', '3', '1_2_10', '500_2000_90');
INSERT INTO `t_twin_reward` VALUES ('3', '4', '1_2_10', '500_2000_90');
INSERT INTO `t_twin_reward` VALUES ('4', '5', '1_2_10', '500_2000_90');
INSERT INTO `t_twin_reward` VALUES ('5', '6', '1_2_10', '500_2000_90');
INSERT INTO `t_twin_reward` VALUES ('6', '7', '1_2_10', '500_2000_90');
INSERT INTO `t_twin_reward` VALUES ('7', '8', '1_2_10', '500_2000_90');


ALTER TABLE `user_twin_reward`
ADD COLUMN `tempVal`  int(11) DEFAULT '0' COMMENT '看视频领取奖励' AFTER `tWinIds`;

/*
SQLyog Ultimate v12.09 (64 bit)
MySQL - 5.7.19 : Database - testbjdqplogin
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`testbjdqplogin` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `testbjdqplogin`;

/*Table structure for table `t_competition_running_horse_light` */

CREATE TABLE `t_competition_running_horse_light` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '跑马灯',
  `content` varchar(300) DEFAULT NULL,
  `playCount` varchar(50) DEFAULT NULL,
  `bTime` varchar(50) DEFAULT NULL,
  `eTime` varchar(50) DEFAULT NULL,
  `diffsec` varchar(50) DEFAULT NULL,
  `srvno` varchar(50) DEFAULT NULL,
  `mdlno` varchar(50) DEFAULT NULL,
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleteTime` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `createTime_idx` (`createTime`)
) ENGINE=InnoDB AUTO_INCREMENT=76367 DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

ALTER TABLE t_competition_playing ADD COLUMN beginPlayingRHLPushStatus VARCHAR(20) COMMENT '跑马灯推送状态' AFTER beginPlayingPushStatus ;
ALTER TABLE `t_competition_playing_config` ADD COLUMN extModel VARCHAR(500) COMMENT '额外参数' AFTER orderField ;
CREATE UNIQUE INDEX `userIdPlayingIdUnique` ON `t_competition_clearing_award`(`userId`, `playingId`);
ALTER TABLE `t_competition_score_total` ADD COLUMN inRoom BOOL DEFAULT FALSE COMMENT '处于房间内' AFTER `status` ;
ALTER TABLE `t_competition_apply` ADD COLUMN play int(11) DEFAULT 0 COMMENT '在赛场' AFTER push ;
ALTER TABLE `t_competition_apply` ADD COLUMN signShow INT(11) DEFAULT 1 COMMENT '处于报名界面' AFTER play ;

insert into `t_competition_playing_config` (`titleType`, `type`, `category`, `entrance`, `iterationType`, `iterationMin`, `consumerId`, `consumerVal`, `shareFreeSign`, `initScore`, `beginHuman`, `maxHuman`, `endHuman`, `titleCode`, `beforeXMinPushTitle`, `desc`, `applyBeforeMin`, `applyBefore`, `applyAfter`, `matchBefore`, `matchAfter`, `stepRoundDesc`, `iteration`, `shardingTakeOver`, `stepRate`, `stepConvertRate`, `roomConfigIds`, `awards`, `disableStartTime`, `disableEndTime`, `loginCallBackUrl`, `logo`, `startBeforeNotifyExt`, `orderField`, `extModel`, `createTime`, `updateTime`, `deleteTime`) values('1','2','16','10','2','1','1','5000',NULL,'10000','24','100','2','1000礼券争夺赛','您报名的\"1000礼券争夺赛\"还有{p}分钟开始，请做好准备。','每天22点开启比赛','10',NULL,NULL,'22:00:00',NULL,'15,9,500_0;9,3,0_1;3,1,0_1','1','0','100;400;800','1;1','1;2','1,2,600_2,2,300_3,2,100_4,1,8000_5,1,7000_6,1,6000_7,1,3000_8,1,3000_9,1,3000',NULL,NULL,'http://192.168.1.178:8080/guajiLogin','2','4,3,2,1','1000','{\"runningHorseLightBeforeLastOneMin\":\"{\\\"content\\\":\\\"1000礼券争夺赛即将开赛\\\",\\\"playCount\\\":\\\"5\\\",\\\"diffsec\\\":\\\"0\\\"}\",\"runningHorseLightChampion\":\"{\\\"content\\\":\\\"恭喜{rankName1}在1000礼券争夺赛获得第一名，赢得海量礼券兑换话费！\\\",\\\"playCount\\\":\\\"3\\\",\\\"diffsec\\\":\\\"0\\\"}\",\"runningHorseLightOpenApply\":\"{\\\"content\\\":\\\"1000礼券争夺赛已经开放报名，请在比赛场报名参赛，礼券可以兑换话费哦！\\\",\\\"playCount\\\":\\\"10\\\",\\\"diffsec\\\":\\\"0\\\"}\",\"secondWeedOut\":\"60,2,100_60,2,1000;0;\"}','2020-08-03 17:45:39','2020-08-04 22:00:01',NULL);



-- 20200720 机器人
ALTER TABLE `user_inf`
ADD COLUMN `isRobot` tinyint(1) NULL DEFAULT 0 COMMENT '是否机器人：0否，1是' ,
ADD COLUMN `robotInfo` varchar(255) NULL COMMENT '机器人信息' ;

ALTER TABLE `t_gold_room_config`
ADD COLUMN `robotState` tinyint(1) NULL DEFAULT 0 COMMENT '是否开启机器人' ,
ADD COLUMN `robotHours` varchar(50) NULL DEFAULT '' COMMENT '投放机器人时间：1,3,5表示1点，3点，5点这三个小时开投放机器人';

CREATE TABLE `t_robot` (
  `userId` bigint(20) NOT NULL COMMENT 'user_inf.userId',
  `type` int(11) DEFAULT '0' COMMENT '类型：1：金币场',
  `used` tinyint(1) DEFAULT '0' COMMENT '是否正在被使用：0否，1是',
  `usedCount` int(11) DEFAULT '0' COMMENT '使用次数',
  `generalExt` varchar(255) DEFAULT NULL COMMENT '信息',
  `playTypes` varchar(100) DEFAULT '' COMMENT '玩法列表',
  `hours` varchar(100) DEFAULT '' COMMENT '小时时间列表',
  `createTime` datetime DEFAULT NULL COMMENT '创建时间',
  `lastUseTime` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最近被使用时间',
  PRIMARY KEY (`userId`) USING BTREE,
  KEY `idx_type_used` (`type`,`used`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='机器人';

CREATE TABLE `t_robot_sys_prop` (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` tinyint(1) DEFAULT NULL COMMENT '类型：1头像，2昵称',
  `prop` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '值',
  `used` tinyint(1) DEFAULT NULL COMMENT '是否被使用：0否，1是',
  `createTime` datetime DEFAULT NULL COMMENT '创建时间',
  `lastUpTime` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最好更新时间',
  PRIMARY KEY (`keyId`),
  KEY `idx_type_used` (`type`,`used`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=148338 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='机器人属性表';

-- 20200812 金币场级别配置
ALTER TABLE `t_gold_room_config`
ADD COLUMN `level` int(10) NULL DEFAULT 1 COMMENT '配置等级：1：新手，2：初级，3：中级，4：高级' AFTER `robotHours`;

-- 20200825 七夕活动配置
DROP TABLE IF EXISTS `invite_queqiao`;
CREATE TABLE `invite_queqiao`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sendId` bigint(11) NULL DEFAULT NULL COMMENT '发送者id',
  `acceptId` bigint(11) NULL DEFAULT NULL COMMENT '接收者id',
  `sendTime` bigint(20) NULL DEFAULT 0 COMMENT '邀请时间',
  `isAllow` int(2) NULL DEFAULT 0 COMMENT '是否接受邀请',
  `isRead` int(2) NULL DEFAULT 0 COMMENT '是否已读',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `index_send`(`sendId`) USING BTREE,
  INDEX `index_accept`(`acceptId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 66 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

INSERT INTO `activity`( `beginTime`, `endTime`, `them`, `showContent`, `extend`, `showBeginTime`, `showEndTime`) VALUES ( '2020-08-24 00:00:00', '2020-08-28 23:59:59', '105', '七夕鹊桥活动', NULL, '2020-08-24 00:00:00', '2020-08-28 23:59:59');

INSERT INTO `mission_config`( `tag`, `missionExplain`, `awardExplain`, `type`, `finishNum`, `awardId`, `awardIcon`, `awardNum`, `onOff`, `sort`, `ext`) VALUES (105, '鹊桥10', '奖励1000', 2, 1, 1, 1, 1000, 1, 0, '');
INSERT INTO `mission_config`( `tag`, `missionExplain`, `awardExplain`, `type`, `finishNum`, `awardId`, `awardIcon`, `awardNum`, `onOff`, `sort`, `ext`) VALUES (105, '鹊桥20', '奖励3000', 2, 2, 1, 1, 3000, 1, 0, '');
INSERT INTO `mission_config`( `tag`, `missionExplain`, `awardExplain`, `type`, `finishNum`, `awardId`, `awardIcon`, `awardNum`, `onOff`, `sort`, `ext`) VALUES (105, '鹊桥30', '奖励5000', 2, 3, 1, 1, 5000, 1, 0, '');
INSERT INTO `mission_config`( `tag`, `missionExplain`, `awardExplain`, `type`, `finishNum`, `awardId`, `awardIcon`, `awardNum`, `onOff`, `sort`, `ext`) VALUES (105, '鹊桥40', '奖励8000', 2, 4, 1, 1, 8000, 1, 0, '');
INSERT INTO `mission_config`( `tag`, `missionExplain`, `awardExplain`, `type`, `finishNum`, `awardId`, `awardIcon`, `awardNum`, `onOff`, `sort`, `ext`) VALUES (105, '鹊桥50', '奖励10000', 2, 5, 1, 1, 10000, 1, 0, '');
INSERT INTO `mission_config`( `tag`, `missionExplain`, `awardExplain`, `type`, `finishNum`, `awardId`, `awardIcon`, `awardNum`, `onOff`, `sort`, `ext`) VALUES (105, '鹊桥60', '奖励12000', 2, 6, 1, 1, 12000, 1, 0, '');
INSERT INTO `mission_config`( `tag`, `missionExplain`, `awardExplain`, `type`, `finishNum`, `awardId`, `awardIcon`, `awardNum`, `onOff`, `sort`, `ext`) VALUES (105, '鹊桥70', '奖励16000', 2, 7, 1, 1, 16000, 1, 0, '');
INSERT INTO `mission_config`( `tag`, `missionExplain`, `awardExplain`, `type`, `finishNum`, `awardId`, `awardIcon`, `awardNum`, `onOff`, `sort`, `ext`) VALUES (105, '鹊桥77', '奖励20000', 2, 8, 1, 1, 20000, 1, 0, '');
INSERT INTO `mission_config`( `tag`, `missionExplain`, `awardExplain`, `type`, `finishNum`, `awardId`, `awardIcon`, `awardNum`, `onOff`, `sort`, `ext`) VALUES (105, '鹊桥神秘大奖', '奖励20000', 2, 9, 1, 1, 20000, 1, 0, '100');

INSERT INTO `activity` (`beginTime`, `endTime`, `them`, `showContent`, `extend`, `showBeginTime`, `showEndTime`) VALUES ( '2020-08-24 00:00:00', '2020-08-28 23:59:59', '106', '金币场七夕集7活动', NULL, '2020-08-24 00:00:00', '2020-09-04 23:59:59');

##------------------------------------------------------------------------------ 20200820比赛场超1k人升级方案
ALTER TABLE `t_competition_room_user` ADD COLUMN serverId int(11) default 0 COMMENT '服务器id' AFTER playingId ;
ALTER TABLE `t_competition_room` ADD COLUMN curStep INT(11) DEFAULT 1 COMMENT '当前轮次' AFTER playingId ;
ALTER TABLE `t_competition_room` ADD COLUMN curRound INT(11) DEFAULT 1 COMMENT '当前回合' AFTER playingId ;
ALTER TABLE `t_competition_playing` ADD COLUMN aliveHuman INT(11) DEFAULT 0 COMMENT '当前存活' AFTER curHuman ;
CREATE INDEX `curStep_curRound_index` ON `t_competition_room`(`curStep`, `curRound`);
CREATE INDEX `playing_status_index` ON `t_competition_score_total`(`playingId`,`status`);
ALTER TABLE `t_competition_room_user` ADD COLUMN rank INT(11) DEFAULT 0 COMMENT '当前排名' AFTER serverId ;
DROP INDEX playingId_index on t_competition_score_total;
ALTER TABLE `t_competition_apply` ADD COLUMN signShow int(11) DEFAULT 1 COMMENT '处于报名界面' AFTER play ;

update t_competition_playing_config set extModel=null;
--------------------------------------------------------------------------------------------------------------------
ALTER TABLE `t_competition_running_horse_light` ADD COLUMN `type` int(11) DEFAULT 1 COMMENT '1跑马灯,2赛后底部横幅' AFTER id ;
ALTER TABLE `t_competition_playing_config` MODIFY COLUMN `applyBefore` varchar(20) ;
ALTER TABLE `t_competition_playing_config` MODIFY COLUMN `applyAfter` varchar(20) ;
ALTER TABLE `t_competition_playing_config` MODIFY COLUMN `matchBefore` varchar(20) ;
ALTER TABLE `t_competition_playing_config` MODIFY COLUMN `matchAfter` varchar(20) ;


##---20201026親友圈开放创建入口

 ALTER TABLE `user_inf` ADD COLUMN `isCreateGroup` int(10) NOT NULL DEFAULT '0' COMMENT '是否可以创建亲友圈0：不，1可以';


 -- 赠钻记录表
CREATE TABLE `t_senddiamonds_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sendUserid` int(11) NOT NULL COMMENT '赠送用户ID',
  `acceptUserid` int(11) NOT NULL COMMENT '接受用户ID',
  `diamondNum` int(11) NOT NULL COMMENT '赠送数量',
  `sendTime` datetime NOT NULL COMMENT '赠送时间',
  PRIMARY KEY (`id`),
  KEY `idx_sendTime` (`sendTime`) USING BTREE,
  KEY `idx_acceptuserid` (`acceptUserid`) USING BTREE,
  KEY `idx_senduserid` (`sendUserid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8 COMMENT='用户赠钻记录表'


--20201029权限
CREATE TABLE `authority` (
`id`  int(11) NOT NULL AUTO_INCREMENT ,
`userId`  bigint(20) NULL DEFAULT NULL ,
`quanxianId`  int(11) NULL DEFAULT NULL COMMENT '权限ID 1：钻石赠送  2：业务员' ,
`createTime`  bigint(20) NULL DEFAULT NULL ,
PRIMARY KEY (`id`)
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci
ROW_FORMAT=Dynamic
;

ALTER TABLE `user_inf` ADD COLUMN `isSpAdmin`  int(11) NULL DEFAULT 0 COMMENT '是不是超级管理员 0：普通用户，1：是（可以分配权限）' AFTER `robotInfo`;
ALTER TABLE `t_group` ADD COLUMN `isCreditUpTime`  bigint(20) NULL DEFAULT 0 COMMENT '开启亲友圈比赛房时间' AFTER `isCredit`;

ALTER TABLE user_inf ADD COLUMN `isCreateGroup` int(10) NOT NULL DEFAULT '0' COMMENT '是否可以创建亲友圈0：不，1可以';

--20201110
DROP TABLE IF EXISTS `t_group_warn`;
CREATE TABLE `t_group_warn` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `groupId` int(11) DEFAULT NULL COMMENT '亲友圈ID',
  `userId` bigint(20) DEFAULT NULL COMMENT '角色ID',
  `warnScore` int(11) DEFAULT NULL,
  `warnSwitch` int(11) DEFAULT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4;

-- 20201116
ALTER TABLE`t_group_credit_log`
ADD INDEX `idx_gid_uid_time_type`(`groupId`, `userId`, `createdTime`, `type`) USING BTREE;

--------20210326
---------------20210114----------
ALTER TABLE `t_group_user`
ADD COLUMN `isSpy` int(2) NOT NULL DEFAULT 0 COMMENT '为1时为内陪玩  2为可调摸牌';

--------------------20210329-------------

ALTER TABLE `user_inf`
ADD COLUMN `accName` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '绑定账号' AFTER `phoneNum`,
ADD COLUMN `accPwd` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '绑定账号密码' AFTER `accName`;


CREATE TABLE `t_group_fake_table` (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `configId` bigint(19) NOT NULL COMMENT '绑定的玩法配置Id',
  `parentGroup` bigint(19) NOT NULL DEFAULT '0' COMMENT '上级id',
  `groupId` bigint(19) NOT NULL COMMENT '军团id',
  `tableName` varchar(100) DEFAULT NULL COMMENT '房间名',
  `tableMode` varchar(200) DEFAULT NULL,
  `modeMsg` varchar(1024) NOT NULL COMMENT '房间信息',
  `gameType` int(5) NOT NULL COMMENT '游戏玩法',
  `payType` int(5) NOT NULL COMMENT '付费方式',
  `playedBureau` int(5) DEFAULT '0' COMMENT '当前打到的局数',
  `gameCount` int(5) NOT NULL COMMENT '牌局数',
  `overCount` int(5) NOT NULL COMMENT '打到该局数就切新大局',
  `playerCount` int(5) NOT NULL COMMENT '牌局人数上限',
  `descMsg` varchar(1024) DEFAULT NULL COMMENT '信息简介',
  `tableOrder` int(5) NOT NULL COMMENT '排序（越小越靠前）',
  `playCount` bigint(19) NOT NULL DEFAULT '0' COMMENT '玩的数量',
  `createdTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `creditMsg` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '信用房间配置',
  `goldMsg` varchar(100) DEFAULT '' COMMENT '金币房配置',
  `roundRefrshTime` int(10) NOT NULL COMMENT '下次局数刷新时间戳',
  PRIMARY KEY (`keyId`) USING BTREE,
  KEY `idx_group` (`parentGroup`,`groupId`,`gameType`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='军团牌局模式表';


CREATE TABLE `t_group_fake_table_headimage`  (
  `keyId` bigint(19) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(60) NOT NULL COMMENT '用户名',
  `headimgurl` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '头像链接',
  `fakeTableId` bigint(19) NULL DEFAULT 0 COMMENT '假桌子id 0当前未被假桌子使用',
  PRIMARY KEY (`keyId`) USING BTREE,
  INDEX `idx_fakeTableId`(`fakeTableId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '假桌表' ROW_FORMAT = Dynamic;



---------------20210507--------------
INSERT INTO `t_resources_configs`( `msgType`, `msgKey`, `msgValue`, `msgDesc`, `configTime`) VALUES ('ServerConfig', 'weixin_keFuHao', '滴滴滴', '微信客服号', '2018-06-29 13:57:00');


ALTER TABLE `t_group_fake_table`
ADD COLUMN `hiding` int(5) NOT NULL DEFAULT 1 COMMENT '是否隐藏';

ALTER TABLE `t_group_fake_table`
DROP INDEX `idx_group`,
ADD INDEX `idx_group`(`parentGroup`, `configId`, `gameType`, `hiding`) USING BTREE,
ADD INDEX `idx_hiding`(`hiding`) USING BTREE;



----------------20210521--------------
ALTER TABLE `t_group_user`
ADD COLUMN `tempCredit` int(11) NULL DEFAULT 0 COMMENT '临时中转积分。 小局后转入credit' AFTER `credit`

------------------20210611-----------
ALTER TABLE `t_group_user`
ADD COLUMN `creditPurse` int(11) NULL DEFAULT 0 COMMENT '零钱包' AFTER `credit`;

ALTER TABLE `t_group_credit_log`
ADD COLUMN `curCreditPurse` bigint(20) NULL DEFAULT 0 COMMENT '操作后零钱包分数' AFTER `curCredit`;



-------------------20210811-------------
ALTER TABLE `t_group_table` ADD INDEX `idx_currentState_groupId` (`currentState`, `groupId`);
