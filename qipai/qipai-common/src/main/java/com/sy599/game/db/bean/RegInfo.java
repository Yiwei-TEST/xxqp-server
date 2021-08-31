package com.sy599.game.db.bean;

import java.io.Serializable;
import java.util.Date;

public class RegInfo implements Serializable {
	private static final long serialVersionUID = -7326670062540108544L;
	private long userId;
	private String name;
	private int sex;
	private int enterServer;
	private String headimgurl;
	private String headimgraw;
	private String pf;
	private String pw;
	private String playedSid;
	private String sessCode;
	private Date regTime;
	private Date logTime;
	private String ip;
	private String mac;
	private String deviceCode;
	private String syvc;
	private String info;
	private String flatId;
	private String identity;
	private long freeCards;
	private long cards;
	private int regBindId;
	private Date syncTime;
	private Date logoutTime;
	private Date lastPlayTime;
	private String pay;
	private int payBindId;
	private long playingTableId;
	private String config;
	private String extend;
	private String loginExtend;
	private int loginDays;
	private String activity;
	private String record;
	private int drawLottery;
	private long usedCards;
	private int isOnLine;
	private String os;
	private long gameSiteTableId;
	private long totalCount;
	private Integer userState;//玩家状态：0禁止登陆，1正常，2红名
	private Date payBindTime;
	private Date preLoginTime;//上一次登陆时间（与logTime不在同一天）
	private String channel; // 渠道来源
	private long totalBureau;// 非金币场房间总局数
	private String phoneNum;
	private String phonePw;
	//牌桌状态：1开局，0未开局或不在房间内
	private int playState;

    private long coin;
    private long freeCoin;
    private long usedCoin;

    private Long goldRoomGroupId;
    private Integer isRobot;
    private String robotInfo;
	private Integer isSpAdmin;

    private int isCreateGroup;

	private String accName;
	private String accPwd;


	public Date getPreLoginTime() {
		return preLoginTime;
	}

	public void setPreLoginTime(Date preLoginTime) {
		this.preLoginTime = preLoginTime;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public int getEnterServer() {
		return enterServer;
	}

	public void setEnterServer(int enterServer) {
		this.enterServer = enterServer;
	}

	public String getHeadimgurl() {
		return headimgurl;
	}

	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}

	public String getHeadimgraw() {
		return headimgraw;
	}

	public void setHeadimgraw(String headimgraw) {
		this.headimgraw = headimgraw;
	}

	public String getPf() {
		return pf;
	}

	public void setPf(String pf) {
		this.pf = pf;
	}

	public String getPw() {
		return pw;
	}

	public void setPw(String pw) {
		this.pw = pw;
	}

	public String getPlayedSid() {
		return playedSid;
	}

	public void setPlayedSid(String playedSid) {
		this.playedSid = playedSid;
	}

	public String getSessCode() {
		return sessCode;
	}

	public void setSessCode(String sessCode) {
		this.sessCode = sessCode;
	}

	public Date getRegTime() {
		return regTime;
	}

	public void setRegTime(Date regTime) {
		this.regTime = regTime;
	}

	public Date getLogTime() {
		return logTime;
	}

	public void setLogTime(Date logTime) {
		this.logTime = logTime;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getDeviceCode() {
		return deviceCode;
	}

	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}

	public String getSyvc() {
		return syvc;
	}

	public void setSyvc(String syvc) {
		this.syvc = syvc;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getFlatId() {
		return flatId;
	}

	public void setFlatId(String flatId) {
		this.flatId = flatId;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public long getFreeCards() {
		return freeCards;
	}

	public void setFreeCards(long freeCards) {
		this.freeCards = freeCards;
	}

	public long getCards() {
		return cards;
	}

	public void setCards(long cards) {
		this.cards = cards;
	}

	public int getRegBindId() {
		return regBindId;
	}

	public void setRegBindId(int regBindId) {
		this.regBindId = regBindId;
	}

	public Date getSyncTime() {
		return syncTime;
	}

	public void setSyncTime(Date syncTime) {
		this.syncTime = syncTime;
	}

	public Date getLogoutTime() {
		return logoutTime;
	}

	public void setLogoutTime(Date logoutTime) {
		this.logoutTime = logoutTime;
	}

	public Date getLastPlayTime() {
		return lastPlayTime;
	}

	public void setLastPlayTime(Date lastPlayTime) {
		this.lastPlayTime = lastPlayTime;
	}

	public String getPay() {
		return pay;
	}

	public void setPay(String pay) {
		this.pay = pay;
	}

	public int getPayBindId() {
		return payBindId;
	}

	public void setPayBindId(int payBindId) {
		this.payBindId = payBindId;
	}

	public long getPlayingTableId() {
		return playingTableId;
	}

	public void setPlayingTableId(long playingTableId) {
		this.playingTableId = playingTableId;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public String getExtend() {
		return extend;
	}

	public void setExtend(String extend) {
		this.extend = extend;
	}

	public String getLoginExtend() {
		return loginExtend;
	}

	public void setLoginExtend(String loginExtend) {
		this.loginExtend = loginExtend;
	}

	public int getLoginDays() {
		return loginDays;
	}

	public void setLoginDays(int loginDays) {
		this.loginDays = loginDays;
	}

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public String getRecord() {
		return record;
	}

	public void setRecord(String record) {
		this.record = record;
	}

	public int getDrawLottery() {
		return drawLottery;
	}

	public void setDrawLottery(int drawLottery) {
		this.drawLottery = drawLottery;
	}

	public long getUsedCards() {
		return usedCards;
	}

	public void setUsedCards(long usedCards) {
		this.usedCards = usedCards;
	}

	public int getIsOnLine() {
		return isOnLine;
	}

	public void setIsOnLine(int isOnLine) {
		this.isOnLine = isOnLine;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public long getGameSiteTableId() {
		return gameSiteTableId;
	}

	public void setGameSiteTableId(long gameSiteTableId) {
		this.gameSiteTableId = gameSiteTableId;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public Integer getUserState() {
		return userState;
	}

	public void setUserState(Integer userState) {
		this.userState = userState;
	}

	public Date getPayBindTime() {
		return payBindTime;
	}

	public void setPayBindTime(Date payBindTime) {
		this.payBindTime = payBindTime;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public long getTotalBureau() {
		return totalBureau;
	}

	public void setTotalBureau(long totalBureau) {
		this.totalBureau = totalBureau;
	}

	public String getPhoneNum() {
		return phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	public String getPhonePw() {
		return phonePw;
	}

	public void setPhonePw(String phonePw) {
		this.phonePw = phonePw;
	}

	public int getPlayState() {
		return playState;
	}

	public void setPlayState(int playState) {
		this.playState = playState;
	}

    public long getCoin() {
        return coin;
    }

    public void setCoin(long coin) {
        this.coin = coin;
    }

    public long getFreeCoin() {
        return freeCoin;
    }

    public void setFreeCoin(long freeCoin) {
        this.freeCoin = freeCoin;
    }

    public long getUsedCoin() {
        return usedCoin;
    }

    public void setUsedCoin(long usedCoin) {
        this.usedCoin = usedCoin;
    }

    public Long getGoldRoomGroupId() {
        return goldRoomGroupId;
    }

    public void setGoldRoomGroupId(Long goldRoomGroupId) {
        this.goldRoomGroupId = goldRoomGroupId;
    }

    public Integer getIsRobot() {
        return isRobot;
    }

    public Integer getIsSpAdmin(){
		return isSpAdmin;
	}

	public void setIsSpAdmin(Integer isSpAdmin){this.isSpAdmin = isSpAdmin;}

    public void setIsRobot(Integer isRobot) {this.isRobot = isRobot;  }

    public String getRobotInfo() {
        return robotInfo;
    }

    public void setRobotInfo(String robotInfo) {
        this.robotInfo = robotInfo;
    }

	public int getIsCreateGroup() {
		return isCreateGroup;
	}

	public void setIsCreateGroup(int isCreateGroup) {
		this.isCreateGroup = isCreateGroup;
	}

	public String getAccName() {
		return accName;
	}

	public void setAccName(String accName) {
		this.accName = accName;
	}

	public String getAccPwd() {
		return accPwd;
	}

	public void setAccPwd(String accPwd) {
		this.accPwd = accPwd;
	}
}
