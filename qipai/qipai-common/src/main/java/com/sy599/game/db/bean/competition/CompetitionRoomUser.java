package com.sy599.game.db.bean.competition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitionRoomUser {
	/**不匹配状态*/
	public static final int DONT_MATCH = 0;
	/**等待匹配状态*/
	public static final int WAIT_MATCH = 1;
	/**匹配成功*/
	public static final int MATCH_SUCCESS = 2;
	/**该玩家需要退赛*/
	public static final int MATCH_CANCEL = 3;

    private Long keyId;
    private Long roomId;
    private Long userId;
    private Date createdTime;
    private Long gameResult;
    private Integer status;	//状态0不匹配 1开始匹配 2匹配成功
    private String logIds;
	private Integer initScore;		//开局积分
	private Long playingId;		//赛场ID

	private Integer serverId;

    //结算分
    private Integer clearingScore;

    private Integer rank;

	public int getClearingScoreNotNull() {
		return clearingScore == null ? initScore : clearingScore;
	}

	public CompetitionRoomUser clone() {
		return CompetitionRoomUser.builder()
				.keyId(keyId)
				.roomId(roomId)
				.userId(userId)
				.createdTime(createdTime)
				.gameResult(gameResult)
				.status(status)
				.logIds(logIds)
				.initScore(initScore)
				.playingId(playingId)
				.clearingScore(clearingScore)
				.rank(rank)
				.build();
	}

	public boolean isNotRobot() {
		return userId>0;
	}
}
