package com.sy.sanguo.game.competition.model.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompetitionRoom {
	private long keyId;
	private long serverId;
	private int currentCount;
	private int maxCount;
}