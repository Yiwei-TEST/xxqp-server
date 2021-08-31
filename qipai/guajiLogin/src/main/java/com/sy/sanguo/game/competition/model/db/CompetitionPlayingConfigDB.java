package com.sy.sanguo.game.competition.model.db;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.sy.sanguo.game.competition.model.param.CompetitionClearingModelRes.Round;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 赛事
 * @author Guang.OuYang
 * @date 2020/5/20-11:27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionPlayingConfigDB extends CompetitionBaseDBPojo {
	public static final String SPACE_NAME = SPACE_NAME_1 + ".playing_config";

	private long id;

	private Integer titleType;	//标题分类, 1话费赛, 2赢豆赛

	private Integer type;// 比赛类型, 1报名赛(人满开赛), 2定时赛(到点开赛)

	private Integer category;// 场次类型, 具体玩法

	private Integer entrance;// 赛事入口,不同入口不同ID   titleType+type+category+entrance联合主键

	private Integer iterationType;    //迭代类型, 1分钟赛,2日赛,3周赛

	private Integer iterationMin; //迭代分钟, 从报名开始计算, n分钟之后重新生成一个未开赛的比赛

	private Integer consumerId;// 消耗,0免费,1白金点,2门票

	private Integer consumerVal;// 消耗值

	private Boolean shareFreeSign;//分享免费报名

	private Integer initScore;// 初始积分

	private Integer beginHuman;// 开赛人数阈值

	private Integer maxHuman;// 最大人数阈值

	private Integer endHuman;// 剩余人数就结算阈值,这个值仅在轮次不满足最大轮次时但是人数又不足下一轮时触发, 如: 3人,轮次10,小结后轮次仅5,剩余3人触发大结

	private String titleCode;// 赛事名称 (国际化)

	private String beforeXMinPushTitle;//赛前推送标题

	private String cancelPushTitle;//自动退赛推送标题

	private String endPushTitle;//离线结赛推送标题

	private String desc;// 比赛描述

	private Integer applyBeforeMin ;	//赛前n分钟可以开放报名

	private String applyBefore;// 报名开始时间 没有年月日

	private String applyAfter;//   报名结束时间 没有年月日

	private String matchBefore;// 赛事开启时间段开始 没有年月日

	private String matchAfter;//   赛事开启时间段结束 没有年月日

//	private String stepOutDesc;// 单局淘汰策略 轮次_每一轮淘汰n, 最大轮次后剩余n人数可计算

	//淘汰分不为0,淘汰名次必须大于0
	private String stepRoundDesc;// 打立出局结束人数,晋级人数,n轮低于分数淘汰_n轮每桌仅前n名晋级,n轮低于分数淘汰_n轮每桌仅前n名晋级;淘汰赛结束人数,晋级人数,n轮低于分数淘汰_n轮每桌仅前n名晋级,n轮低于分数淘汰_n轮每桌仅前n名晋级

	private String roomConfigIds;//打立出局回合配置id,打立出局回合配置id,;淘汰赛回合配置id,淘汰赛回合配置id

	private Boolean iteration;// 迭代0关闭, 最后一场比赛后不再生成新的比赛场次

	private String shardingTakeOver;    //分片接管, 当前执行该任务的分片

	private String stepRate;    //每轮底分变更 轮次,回合底分,回合底分; 大于当前配置回合使用最后一个底分

	private String stepConvertRate;		//下一轮的倍率

	private String awards;	//结算奖励  rank,awardId,awardVal;

	private String disableStartTime;//开赛段

	private String disableEndTime;//禁赛段

	private String logo ; //是否带今天logo

	private String startBeforeNotifyExt;	//赛前n分钟推送:20,15,10,1

	private String loginCallBackUrl;

	private Integer orderField;		//排序

	private String extModel; //额外参数

	//临时变量
	private String loginCallBackClearing = "competitionPlaying!clearing.action";
	private String loginCallBackCancel = "competitionPlaying!cancel.action";

	private Date createTime;//

	private Date updateTime;//

	private Date deleteTime;//

	private boolean updateAndDelOldPlaying;		//更新配置同时删除已经生成的比赛


	//-------------------------------------------------------------------------------------------------------temp
	//每个轮次需要淘汰的人数
	@JSONField(serialize = false)
	private transient List<Round> stepUpgradeDetails;
	@JSONField(serialize = false)
	private transient CompetitionPlayingConfigExtModel competitionPlayingConfigExtModel;

	@JSONField(serialize = false)
	public CompetitionPlayingConfigExtModel getExtModelPojo() {
		try {
			if (StringUtils.isNotBlank(extModel) && competitionPlayingConfigExtModel == null) {
				competitionPlayingConfigExtModel = JSONObject.parseObject(extModel, CompetitionPlayingConfigExtModel.class);
			}
		}
		catch (Exception e) {
			LogUtil.e("competition|notifyAllPlay|runningHorseLight|error", e);
		}

		return competitionPlayingConfigExtModel;
	}

	@JSONField(serialize = false)
	public List<Round> getStepUpgradeDetails(CompetitionPlayingDB playing) {
		if (stepUpgradeDetails == null && getStepRoundDesc() != null) {
			stepUpgradeDetails = new ArrayList<>(2);
			//赛制总长度, 总轮次
			String[] scaleTotal = getStepRoundDesc().split(";");
			int step = 1;
			int uTotal = 0;
			for (String scale : scaleTotal) {
				int total = step == 1 ? playing.getCurHuman()/*getMaxHuman()*/ : uTotal;
				String[] curStepInfo = scale.split(",");
				uTotal = NumberUtils.toInt(curStepInfo[1]);
				//这里的赛制认为每一轮内所有局数都是一个配置所以取前1
				//0淘汰分,1每桌前n晋级
				String[] stepCategory = curStepInfo[2].split("_");
				boolean lowScore = stepCategory[0].equalsIgnoreCase("1");
				boolean topN = stepCategory[1].equalsIgnoreCase("1");
				stepUpgradeDetails.add(Round.builder().stepCategory(lowScore || (!lowScore && !topN) ? -1 : Integer.valueOf(stepCategory[1])).step(step++).total(total).upgrade(uTotal).build());
			}
		}
		return stepUpgradeDetails;
	}
}
