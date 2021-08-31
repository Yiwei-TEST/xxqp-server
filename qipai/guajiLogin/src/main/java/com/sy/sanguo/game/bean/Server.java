package com.sy.sanguo.game.bean;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Server {
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
	private int check=1;
	private int onlineCount;
	private String extend;
	private int serverType=1;//游戏服类型0练习场1普通场
    private List<Integer> tmpGameType; //临时可用，但新创建不可用，用于迁移玩法后，旧房间依旧生效

	private Set<String> pfs = new HashSet<>();

	public String getExtend() {
		return extend;
	}

	public void setExtend(String extend) {
		this.extend = extend;

		if (StringUtils.isNotBlank(this.extend)){
			String[] strs=this.extend.split("\\|");
			for (String str:strs){
				if (StringUtils.isNotBlank(str)){
					pfs.add(str);
				}
			}
		}
	}

	public Set<String> getPfs() {
		return pfs;
	}

	public int getOnlineCount() {
		return onlineCount;
	}

	public void setOnlineCount(int onlineCount) {
		this.onlineCount = onlineCount;
	}

	public int getServerType() {
		return serverType;
	}

	public void setServerType(int serverType) {
		this.serverType = serverType;
	}

	public int getCheck() {
		return check;
	}

	public void setCheck(int check) {
		this.check = check;
	}

	public String getChathost() {
//		String spareIp = SysInfManager.getInstance().getSpareIp(id);
//		if(!StringUtils.isBlank(spareIp)){
//			return spareIp;
//		}
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

    public List<Integer> getTmpGameType() {
        return tmpGameType;
    }

    public void setTmpGameType(List<Integer> tmpGameType) {
        this.tmpGameType = tmpGameType;
    }
}
