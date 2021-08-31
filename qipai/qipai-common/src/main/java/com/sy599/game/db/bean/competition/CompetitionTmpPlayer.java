package com.sy599.game.db.bean.competition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitionTmpPlayer {
	private Long userId;
	private int point;
	private int totalPoint;
	public boolean isNotRobot() {
		return userId>0;
	}
}
