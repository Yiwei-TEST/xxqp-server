package com.sy.sanguo.game.competition.model.db;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.sy.sanguo.common.util.lenovo.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 跑马灯
 * @author Guang.OuYang
 * @date 2020/5/20-11:27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionRunningHorseLightDB extends CompetitionBaseDBPojo {
	public static final String SPACE_NAME = SPACE_NAME_1 + ".runningHorseLight";

	private long id;

	private Integer type;		//1跑马灯,2底部横幅

	private String content;		//内容

	private String playCount;	//时间段内当前播放次数

	private String bTime;		//开始时间

	private String eTime;		//结束时间

	private String diffsec;		//间隔时间

	private String srvno;		//服务器序号 黑名单,存在即不播,例如存在1,那么序号1的服务器不播

	private String mdlno;		//功能模块序号 黑名单,存在即不播,客户端暂未使用,例如存在比赛场为1,那么比赛场不播

	@JSONField(serialize = false)
	private Date createTime;
	@JSONField(serialize = false)
	private Date updateTime;
	@JSONField(serialize = false)
	private Date deleteTime;


	public CompetitionRunningHorseLightDB tempArgsFill(CompetitionPlayingDB playing) {
		if (type == null) {
			type = 1;
		}
		//十分钟内有效
		if (this.diffsec == null)
			this.setDiffsec("3000");
		if (this.playCount == null)
			this.setPlayCount("2");
		if (this.bTime == null)
			this.setBTime(DateUtil.getNow(true));
		if (this.eTime == null && playing != null)
			this.setETime(new SimpleDateFormat(DateUtil.STRING_DATE_FORMAT).format(playing.getMatchBefore().getTime() - (1 * 60 * 1000)));
		return this;
	}

	public static void main(String[] args) {
		System.out.println(JSONObject.toJSONString(CompetitionRunningHorseLightDB.builder()
				.content("{rankName1}在xx比赛中获得第一名")
				.bTime("2020-07-20 14:54:24")
				.eTime("2020-07-20 15:00:25")
				.diffsec("3000")
				.srvno("18,21")
				.mdlno("1,2,3")
				.build().tempArgsFill(null)));
	}
}

