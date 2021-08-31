package com.sy.sanguo.game.pdkuai.staticdata.bean;

import java.util.List;

/**
 * Created by lc on 2017/3/16.
 */
public class PortConfig {
    private int serverId;
    private List<Integer> portList;

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public List<Integer> getPortList() {
        return portList;
    }

    public void setPortList(List<Integer> portList) {
        this.portList = portList;
    }
}
