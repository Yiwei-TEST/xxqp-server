package com.sy599.game.db.bean.competition;

import com.alibaba.fastjson.JSONObject;
import com.sy599.game.db.bean.competition.param.CompetitionClearingModelRes;
import com.sy599.game.util.CompetitionUtil;
import com.sy599.game.util.LogUtil;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Date;

@Data
public class CompetitionRoom {

	public static final String RATIO_PREX =  "_/##/_" ;
    /*** 状态：新建***/
    public static final int STATE_NEW = 0;
    /*** 状态：准备好，可以开始匹配**/
    public static final int STATE_READY = 1;
    /*** 状态：已开局***/
    public static final int STATE_PLAYING = 2;
    /*** 状态：正常结束***/
    public static final int STATE_NORMAL_OVER = 3;

    public long lastCheckTime;

    private Long keyId;
    private Long tableId;
    private Long configId;
    private Long playingId;
    private Integer curStep;
    private Integer curRound;
    private String modeId;
    private Integer currentCount;
    private Integer maxCount;
    private Integer serverId;
    private Integer currentState;
    private Integer gameCount;
    private String tableMsg;
    private String ext;
    private String tableName;
    private Date createdTime;
    private Date modifiedTime;

    //登录服传递的创房选项
    private String createParamClearingModel;

    //初始分
    private int initScore;

    /******以下与DB无关*****/
    //赛事标题
    private String title;
    //倍率,底分
    private long rate = 1;
    //淘汰分数
    private int weedOutScore = 1;
    //当前轮次,创房参数
//    private int curStepForExt = 1;
//    //当前回合,创房参数
//    private int curRoundForExt = 1;

	public CompetitionRoom clone() {
		CompetitionRoom competitionRoom = new CompetitionRoom();
		try {
			BeanUtils.copyProperties(competitionRoom, this);
		} catch (Exception e) {
		}
		return competitionRoom;
	}

	public void setCompetitionClearingModelRes(CompetitionClearingModelRes req){
		this.createParamClearingModel = JSONObject.toJSONString(req);
	}

	public CompetitionClearingModelRes getCompetitionClearingModelRes() {
		CompetitionClearingModelRes competitionClearingModelRes = JSONObject.parseObject(this.createParamClearingModel, CompetitionClearingModelRes.class);
		CompetitionUtil.fillUrl(competitionClearingModelRes);
		return competitionClearingModelRes;
	}

	public void setExt(String ext) {
        this.ext = ext;
        initAddtitionInfo();
    }

    // 以下与db无关
    public boolean isFull() {
        return currentCount >= maxCount;
    }

    public boolean isNotStart() {
        return currentState == STATE_NEW || currentState == STATE_READY;
    }

    public boolean canStart() {
        return isFull() && isNotStart();
    }

    public boolean isPlaying() {
        return currentState == STATE_PLAYING;
    }

    public boolean isNormalOver() {
        return currentState == STATE_NORMAL_OVER;
    }


    public boolean isOver() {
        return isNormalOver();
    }


    public int loadMatchRatio() {
		return 0;
    }

    public long getRate() {
        return rate;
    }

	public long getSecondsRate() {
		CompetitionClearingModelRes req = getCompetitionClearingModelRes();
		String secondWeedOut = req.getSecondWeedOut();

		if (secondWeedOut != null && req.getWeedOutScore() > 0) {
			//当前人数
			int curHuman = req.getTotalHuman();

			//截止房间结算开赛时间秒
//			long roomExistsCycle = ((req.getCurrentMills() + (System.currentTimeMillis() - getCreatedTime().getTime())) - req.getPlayingOpenTime()) / 1000;
			long roomExistsCycle = (System.currentTimeMillis() - req.getPlayingOpenTime()) / 1000;

			//人数_分钟,时间,底分,淘汰分_...;
			String[] minsData = secondWeedOut.split(";");    //各个分钟数据

			int baseBasic = (int) getRate();
			int baseTotal = baseBasic;

			int weedOutBasic = 0;
			int weedOutTotal = weedOutBasic;

			for (String minData : minsData) {
				String[] singleData = minData.split("_");

				int maxHuman = Integer.valueOf(singleData[0]);

				//人数区间
				if (maxHuman >= curHuman) {
					//上一个时间区间
					int upTimeSection = 0;
					int upTimeScheduler = 0;
					int upBase = 0;
					int upWeedOut = 0;

					int timeSection = 0;
					int timeScheduler = 0;
					int base = 0;
					int weedOut = 0;
					for (int i = 1; i < singleData.length; i++) {
						//0人数,1时间,多少秒调度,底分,淘汰分
						String[] s = singleData[i].split(",");

						//时间区间
						timeSection = Integer.valueOf(s[0]) * 60;
						//n秒调度
						timeScheduler = Integer.valueOf(s[1]);
						base = Integer.valueOf(s[2]);
						weedOut = Integer.valueOf(s[3]);

						if (i != 1) {
							if (roomExistsCycle > timeSection) {
								long needCycleSeconds = (timeSection - upTimeSection) / upTimeScheduler;
								baseTotal += upBase * Math.max(needCycleSeconds, 1);
								weedOutTotal += upWeedOut * Math.max(needCycleSeconds, 1);

								//LogUtil.msgLog.info("底分: 淘汰分: ");
								//LogUtil.msgLog.info(" " + (timeSection / 60) + "分钟0秒:" + " " + baseTotal + " " + weedOutTotal);
							}
							else {
								long needCycleSeconds = roomExistsCycle - upTimeSection;
								baseTotal += upBase * Math.max(needCycleSeconds / timeScheduler, 0);
								weedOutTotal += upWeedOut * Math.max(needCycleSeconds / timeScheduler, 0);
								//LogUtil.msgLog.info("底分: 淘汰分: ");
								//LogUtil.msgLog.info(" " + ((roomExistsCycle / 60) + "分钟" + (roomExistsCycle % 60)) + "秒:" + " " + baseTotal + " " + weedOutTotal);
							}
						}

						upTimeSection = timeSection;
						upTimeScheduler = timeScheduler;
						upBase = base;
						upWeedOut = weedOut;
					}

					if (roomExistsCycle > timeSection) {
						long needCycleSeconds = (roomExistsCycle - timeSection) / timeScheduler;
						baseTotal += base * Math.max(needCycleSeconds, 0);
						weedOutTotal += weedOut * Math.max(needCycleSeconds, 0);

						//LogUtil.msgLog.info("底分: 淘汰分: ");
						//LogUtil.msgLog.info(" " + ((roomExistsCycle / 60) + "分钟" + (roomExistsCycle % 60)) + "秒:" + " " + baseTotal + " " + weedOutTotal);
					}
					break;
				}
			}


			LogUtil.msgLog.info(("competition|getSecondsRate|baseScore|" + getPlayingId() + "|" + getCurStep() + "|" + getCurRound() + "|" + baseTotal + "|" + roomExistsCycle));

			return baseTotal;
		}

		return getRate();
	}

    public void initAddtitionInfo() {
        try {
            if (StringUtils.isNotBlank(ext)) {
                String[] splits = ext.split(RATIO_PREX);
                rate = NumberUtils.toLong(splits[0]);
                title = splits[1];
				weedOutScore = NumberUtils.toInt(splits[2]);
//				curStep = NumberUtils.toInt(splits[3]);
//				curRound = NumberUtils.toInt(splits[4]);
            }
        } catch (Exception e) {
            LogUtil.errorLog.error("" + keyId, e);
        }
    }

	public String roundTypeTitile() {
		return curStep == 1 ? "打立出局" : "定局积分";
	}
}
