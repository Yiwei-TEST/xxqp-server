package com.sy599.game.gold;

import com.sy599.game.character.Player;
import com.sy599.game.db.bean.gold.GoldRoomConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GoldRoomMatch {
    /*** 金币场匹配模式：快速加入***/
    public static final int MATCH_TYPE_FAST = 1;
    /*** 金币场匹配模式：智能匹配***/
    public static final int MATCH_TYPE_INTELLIGENT = 2;

    public static final int status_init = 0;
    public static final int status_matching = 1;
    public static final int status_matched = 2;
    public static final int status_succ = 3;
    public static final int status_quit = 4;
    public static final int status_del = 5;
    public static final int status_fail = 6;

    private Player player;
    private int matchType;
    private int playType;
    private int status;
    private long initConfigId; // 初始玩法配置Id
    private List<GoldRoomConfig> configList;
    private Set<Long> configIdSet;
    private long startTime;
    private GoldRoomConfig matched;
    private long matchPriority;    // 匹配权重
    private long lastTimeout; //上次超时时间
    private long maxRate;
    private boolean configListSorted;

    public GoldRoomMatch(Player player, int playType) {
        this.player = player;
        this.playType = playType;
        this.status = status_init;
        this.configList = new ArrayList<>();
        this.configIdSet = new HashSet<>();
        this.startTime = System.currentTimeMillis();
        this.lastTimeout = this.startTime;
        this.configListSorted = false;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getMatchType() {
        return matchType;
    }

    public void setMatchType(int matchType) {
        this.matchType = matchType;
    }

    public int getPlayType() {
        return playType;
    }

    public void setPlayType(int playType) {
        this.playType = playType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getInitConfigId() {
        return initConfigId;
    }

    public void setInitConfigId(long initConfigId) {
        this.initConfigId = initConfigId;
    }

    public List<GoldRoomConfig> getConfigList() {
        return configList;
    }

    public void setConfigList(List<GoldRoomConfig> configList) {
        this.configList = configList;
    }

    public Set<Long> getConfigIdSet() {
        return configIdSet;
    }

    public void setConfigIdSet(Set<Long> configIdSet) {
        this.configIdSet = configIdSet;
    }

    public GoldRoomConfig getMatched() {
        return matched;
    }

    public void setMatched(GoldRoomConfig matched) {
        this.matched = matched;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getMatchPriority() {
        return matchPriority;
    }

    public void setMatchPriority(long matchPriority) {
        this.matchPriority = matchPriority;
    }

    public long getLastTimeout() {
        return lastTimeout;
    }

    public void setLastTimeout(long lastTimeout) {
        this.lastTimeout = lastTimeout;
    }

    public long getMaxRate() {
        return maxRate;
    }

    public void setMaxRate(long maxRate) {
        this.maxRate = maxRate;
    }

    public boolean isInit() {
        return this.status == status_init;
    }

    public boolean isMatching() {
        return this.status == status_matching;
    }

    public boolean isMatched() {
        return this.status == status_matched;
    }

    public boolean isSucc() {
        return this.status == status_succ;
    }

    public boolean isQuit() {
        return this.status == status_quit;
    }

    public boolean isDel() {
        return this.status == status_del;
    }

    public boolean isFail() {
        return this.status == status_fail;
    }

    public void addAllGoldRoomConfig(List<GoldRoomConfig> list) {
        for (GoldRoomConfig config : list) {
            addGoldRoomConfig(config);
        }
    }

    public void addGoldRoomConfig(GoldRoomConfig config) {
        this.configList.add(config);
        this.configIdSet.add(config.getKeyId());
        if (config.getRate() > maxRate) {
            maxRate = config.getRate();
        }
        this.configListSorted = false;
    }

    public void sortConfigList() {
        if (!this.configListSorted) {
            return;
        }
        Collections.sort(configList, new Comparator<GoldRoomConfig>() {
            @Override
            public int compare(GoldRoomConfig o1, GoldRoomConfig o2) {
                if (o1.getPlayerCount() > o2.getPlayerCount()) {
                    return 1;
                } else if (o1.getPlayerCount() < o2.getPlayerCount()) {
                    return -1;
                } else {
                    if (o1.getRate() < o2.getRate()) {
                        return 1;
                    } else if (o1.getRate() > o2.getRate()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }
        });
        this.configListSorted = true;
    }
}
