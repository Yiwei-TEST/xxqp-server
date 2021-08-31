package com.sy599.game.gold;

import com.sy599.game.db.bean.gold.GoldRoomConfig;

import java.util.List;

public class GoldRoomMatchSucc {
    private GoldRoomConfig config;
    private List<GoldRoomMatch> matchedList;

    public GoldRoomMatchSucc(GoldRoomConfig config, List<GoldRoomMatch> matchedList) {
        this.config = config;
        this.matchedList = matchedList;
    }

    public GoldRoomConfig getConfig() {
        return config;
    }

    public void setConfig(GoldRoomConfig config) {
        this.config = config;
    }

    public List<GoldRoomMatch> getMatchedList() {
        return matchedList;
    }

    public void setMatchedList(List<GoldRoomMatch> matchedList) {
        this.matchedList = matchedList;
    }
}
