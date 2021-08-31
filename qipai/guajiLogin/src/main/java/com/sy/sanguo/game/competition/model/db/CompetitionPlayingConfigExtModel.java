package com.sy.sanguo.game.competition.model.db;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * 赛事
 * @author Guang.OuYang
 * @date 2020/5/20-11:27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionPlayingConfigExtModel  {
	private String runningHorseLightOpenApply;			//开放报名 跑马灯
	private String runningHorseLightBeforeLastOneMin;	//最后一分钟 跑马灯
	private String runningHorseLightChampion;			//冠军通告 跑马灯

	private String bottomTitle;	//结束后展示的冠军横幅  比赛结束后展示多少秒,唯一识别码,(userName1),(userId1)

	//额外配置,每秒增加的配置  倍率_淘汰分 : 60,1,2_60,2,500
	private String secondWeedOut;

	@JSONField(serialize = false)
	public CompetitionRunningHorseLightDB getRunningHorseLightOpenApplyModel() {
		if(StringUtils.isBlank(runningHorseLightOpenApply)){
			return null;
		}
		return JSONObject.parseObject(runningHorseLightOpenApply, CompetitionRunningHorseLightDB.class);
	}

	@JSONField(serialize = false)
	public CompetitionRunningHorseLightDB getRunningHorseLightBeforeLastOneMinModel() {
		if(StringUtils.isBlank(runningHorseLightBeforeLastOneMin)){
			return null;
		}
		return JSONObject.parseObject(runningHorseLightBeforeLastOneMin, CompetitionRunningHorseLightDB.class);
	}

	@JSONField(serialize = false)
	public CompetitionRunningHorseLightDB getRunningHorseLightChampionModel() {
		if(StringUtils.isBlank(runningHorseLightChampion)){
			return null;
		}
		return JSONObject.parseObject(runningHorseLightChampion, CompetitionRunningHorseLightDB.class);
	}

	public static void main(String[] args) {
		System.out.println(JSONObject.toJSONString(CompetitionPlayingConfigExtModel.builder()
				.runningHorseLightOpenApply(JSONObject.toJSONString(CompetitionRunningHorseLightDB.builder()
						.content("xx比赛即将开赛")
						.srvno("18,21")
						.mdlno("1,2,3")
						.build()))
				.runningHorseLightBeforeLastOneMin(JSONObject.toJSONString(CompetitionRunningHorseLightDB.builder()
						.content("xx比赛即将开放报名")
						.srvno("18,21")
						.mdlno("1,2,3")
						.build()))
				.runningHorseLightChampion(JSONObject.toJSONString(CompetitionRunningHorseLightDB.builder()
						.content("{rankName1}在xx比赛中获得第一名")
						.bTime("2020-07-20 14:54:24")
						.eTime("2020-07-20 15:00:25")
						.diffsec("3000")
						.srvno("18,21")
						.mdlno("1,2,3")
						.build()))
				.build()));
	}
}
