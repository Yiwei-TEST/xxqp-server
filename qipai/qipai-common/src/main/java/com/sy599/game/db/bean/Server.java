package com.sy599.game.db.bean;

import java.util.List;

public class Server {

    /*** 以往普通***/
    public static final int SERVER_TYPE_NORMAL = 1;
    /*** 金币相关***/
    public static final int SERVER_TYPE_GOLD_ROOM = 2;
    /**比赛场相关*/
    public static final int SERVER_TYPE_COMPETITION_ROOM = 3;

	private int id;
	private String name;
	private int isOpen;
	private String status;
	private String host;
	private int port;
	private String chathost;
	private String intranet;
	private List<Integer> gameType;
	private List<Integer> matchType;
	private int ipConifg;
	private int onlineCount;
	private String extend;
	private int serverType=1;//游戏服类型0练习场1普通场
	private String httpsUri;
	private String wssUri;
	private List<Integer> tmpGameType; //临时可用，但新创建不可用，用于迁移玩法后，旧房间依旧生效

	public String getHttpsUri() {
		return httpsUri;
	}

	public void setHttpsUri(String httpsUri) {
		this.httpsUri = httpsUri;
	}

	public String getWssUri() {
		return wssUri;
	}

	public void setWssUri(String wssUri) {
		this.wssUri = wssUri;
	}

	public int getServerType() {
		return serverType;
	}

	public void setServerType(int serverType) {
		this.serverType = serverType;
	}

	public String getChathost() {
		return chathost;
	}

	public void setChathost(String chathost) {
		this.chathost = chathost;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIsOpen() {
		return isOpen;
	}

	public void setIsOpen(int isOpen) {
		this.isOpen = isOpen;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIntranet() {
		return intranet;
	}

	public void setIntranet(String intranet) {
		this.intranet = intranet;
	}

	public List<Integer> getGameType() {
		return gameType;
	}

	public void setGameType(List<Integer> gameType) {
		this.gameType = gameType;
	}

	public List<Integer> getMatchType() {
		return matchType;
	}

	public void setMatchType(List<Integer> matchType) {
		this.matchType = matchType;
	}

	public int getIpConifg() {
		return ipConifg;
	}

	public void setIpConifg(int ipConifg) {
		this.ipConifg = ipConifg;
	}

	public int getOnlineCount() {
		return onlineCount;
	}

	public void setOnlineCount(int onlineCount) {
		this.onlineCount = onlineCount;
	}

	public String getExtend() {
		return extend;
	}

	public void setExtend(String extend) {
		this.extend = extend;
	}

    public List<Integer> getTmpGameType() {
        return tmpGameType;
    }

    public void setTmpGameType(List<Integer> tmpGameType) {
        this.tmpGameType = tmpGameType;
    }
}
