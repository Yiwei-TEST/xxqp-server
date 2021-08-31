package com.sy.sanguo.game.competition.model.param;

import com.sy.sanguo.game.competition.model.db.CompetitionPlayingDB;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayingList {
	//禁用报名
	private boolean disableSign;
	//底部标题
	private List<List<String>> bottomTitle;
	//结果列表
	private List<CompetitionPlayingDB> argList;
}
