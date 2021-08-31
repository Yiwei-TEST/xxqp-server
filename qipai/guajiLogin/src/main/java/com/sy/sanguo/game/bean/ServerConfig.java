package com.sy.sanguo.game.bean;


public class ServerConfig {

	private int id;
	
	private String name;
	
	private String host;
	
	private String chathost;
	
	private String intranet;
	
	private String gameType;
	
	private String matchType;
	
	private int onlineCount;
	
	private String extend;

	private int serverType;

    private String tmpGameType;

	public int getServerType() {
		return serverType;
	}

	public void setServerType(int serverType) {
		this.serverType = serverType;
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

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getChathost() {
		return chathost;
	}

	public void setChathost(String chathost) {
		this.chathost = chathost;
	}

	public String getIntranet() {
		return intranet;
	}

	public void setIntranet(String intranet) {
		this.intranet = intranet;
	}

	public String getGameType() {
		return gameType;
	}

	public void setGameType(String gameType) {
		this.gameType = gameType;
	}

	public String getMatchType() {
		return matchType;
	}

	public void setMatchType(String matchType) {
		this.matchType = matchType;
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

    public String getTmpGameType() {
        return tmpGameType;
    }

    public void setTmpGameType(String tmpGameType) {
        this.tmpGameType = tmpGameType;
    }
}
