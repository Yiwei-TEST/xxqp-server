package com.sy599.game.common.bean;

import com.sy599.game.character.Player;

import java.util.List;

/**
 * 创房参数
 */
public class CreateTableInfo {

    private Player player;
    private int tableType;
    private int playType;
    private int bureauCount;
    private List<Integer> intParams;
    private List<String> strParams;
    private boolean saveDb;

    public CreateTableInfo(Player player) {
        this.player = player;
    }

    public CreateTableInfo(Player player, int tableType, int play, int bureauCount, List<Integer> intParams, List<String> strParams, boolean saveDb) {
        this.player = player;
        this.tableType = tableType;
        this.playType = play;
        this.bureauCount = bureauCount;
        this.intParams = intParams;
        this.strParams = strParams;
        this.saveDb = saveDb;
    }

    public CreateTableInfo(Player player, int tableType, int play, int bureauCount, List<Integer> intParams, boolean saveDb) {
        this.player = player;
        this.tableType = tableType;
        this.playType = play;
        this.bureauCount = bureauCount;
        this.intParams = intParams;
        this.saveDb = saveDb;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getTableType() {
        return tableType;
    }

    public void setTableType(int tableType) {
        this.tableType = tableType;
    }

    public int getPlayType() {
        return playType;
    }

    public void setPlayType(int playType) {
        this.playType = playType;
    }

    public int getBureauCount() {
        return bureauCount;
    }

    public void setBureauCount(int bureauCount) {
        this.bureauCount = bureauCount;
    }

    public List<Integer> getIntParams() {
        return intParams;
    }

    public void setIntParams(List<Integer> intParams) {
        this.intParams = intParams;
    }

    public List<String> getStrParams() {
        return strParams;
    }

    public void setStrParams(List<String> strParams) {
        this.strParams = strParams;
    }

    public boolean isSaveDb() {
        return saveDb;
    }

    public void setSaveDb(boolean saveDb) {
        this.saveDb = saveDb;
    }
}
