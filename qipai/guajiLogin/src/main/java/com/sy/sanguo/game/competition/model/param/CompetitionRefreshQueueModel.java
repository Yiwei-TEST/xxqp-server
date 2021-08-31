package com.sy.sanguo.game.competition.model.param;

import com.sy.sanguo.game.competition.model.db.CompetitionPlayingDB;
import com.sy.sanguo.game.competition.service.CompetitionPlayingService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionRefreshQueueModel {
    private long playingId;
    private int titleType;
    private int type;
    private int category;
    private int entrance;

    private boolean refreshTotalRankData;
    private boolean cgeTotalRankAndPush0;

    private List<CompetitionClearingPlay> currentStepAllPlay;
    private boolean cgeTotalRankStatus;
    private boolean cgeTotalRankScore;
    private boolean pushRankCge;


    private long noClearingTableCount;

    private boolean noExistsTable;
    private boolean needMatch;
    private boolean theNextStage;
    private boolean bigStage;
    private boolean nextStep;
    private CompetitionPlayingService.CompetitionClearingRiseInRankMode upRoundResolve;
    private CompetitionPlayingService.CompetitionClearingRiseInRankMode curRoundResolve;

    private CompetitionPlayingDB playing;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompetitionRefreshQueueModel)) return false;
        CompetitionRefreshQueueModel that = (CompetitionRefreshQueueModel) o;
        return getPlayingId() == that.getPlayingId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlayingId());
    }
}