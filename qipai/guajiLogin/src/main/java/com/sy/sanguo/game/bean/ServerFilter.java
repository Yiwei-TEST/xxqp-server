package com.sy.sanguo.game.bean;

public class ServerFilter {
	/**
	 * 校验的pf+channelId
	 */
	private String pfCid;
	/**
	 * 黑名单:首位为关闭的最小id,第二位为最大(与白名单互斥)
	 */
	private int[] blackList;
	/**
	 * 白名单:首位为开启的最小id,第二位为最大(与黑名单互斥)
	 */
	private int[] whiteList;
	/**
	 * 是否完整匹配,如果为0则以startWhith校验
	 */
	private int equals;
	/**
	 * 是否显示公告
	 */
	private int useNotice;
	private int noticeId;
	
	public String getPfCid() {
		return pfCid;
	}
	public void setPfCid(String pfCid) {
		this.pfCid = pfCid;
	}
	public int[] getBlackList() {
		return blackList;
	}
	public void setBlackList(int[] blackList) {
		this.blackList = blackList;
	}
	public int[] getWhiteList() {
		return whiteList;
	}
	public void setWhiteList(int[] whiteList) {
		this.whiteList = whiteList;
	}
	public int getEquals() {
		return equals;
	}
	public void setEquals(int equals) {
		this.equals = equals;
	}
	public int getUseNotice() {
		return useNotice;
	}
	public void setUseNotice(int useNotice) {
		this.useNotice = useNotice;
	}
	public int getNoticeId() {
		return noticeId;
	}
	public void setNoticeId(int noticeId) {
		this.noticeId = noticeId;
	}
	
}
